package com.hawk.game.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.TechnologyLevelUpEvent;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.msg.CancelTechQueueMsg;
import com.hawk.game.msg.TechQueueFinishMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.skill.tech.ITechSkill;
import com.hawk.game.player.skill.tech.TechSkillFactory;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Technology.LevelUpTechnologyReq;
import com.hawk.game.protocol.Technology.LvlUpType;
import com.hawk.game.protocol.Technology.PBCastTechSkillReq;
import com.hawk.game.service.QueueService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventStartTechUpgrade;
import com.hawk.game.service.mssion.event.EventTechTypePower;
import com.hawk.game.service.mssion.event.EventTechnologyUpgrade;
import com.hawk.game.strengthenguide.StrengthenGuideManager;
import com.hawk.game.strengthenguide.msg.SGPlayerTechnologyUpdgrdeMsg;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.Source;

/**
 * 玩家科技模块
 * @author shadow
 * @reviewer lating
 */
public class PlayerTechnologyModule extends PlayerModule {

	static Logger logger = LoggerFactory.getLogger("Server");

	public PlayerTechnologyModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		player.getPush().syncTechnologyInfo();
		player.getPush().syncTechSkillInfo();
		return true;
	}

	/**
	 * 升级科技
	 * @param id 科技Id
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TECHNOLOGY_UPLEVEL_C_VALUE)
	private boolean onLevelUp(HawkProtocol protocol) {
		LevelUpTechnologyReq req = protocol.parseProtocol(LevelUpTechnologyReq.getDefaultInstance());
		int cfgId = req.getTechId();
		LvlUpType type = req.getType();
		// 花水晶秒时间和资源升级
		boolean immediate = type == LvlUpType.BUY_RES_AND_TIME;

		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.FIGHTING_LABORATORY);
		// 研究科技的作战实验室建筑不存在
		if (buildingEntity == null) {
			logger.error("tech level up failed, Lab building not exist, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.Error.CODITION_NOT_MATCH);
			return false;
		}
		
		QueueEntity queue = player.getData().getQueueEntityByItemId(buildingEntity.getId());
		// 作战实验室建筑正在升级
		if (queue != null) {
			logger.error("tech level up failed, Lab building is upgrading, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.Error.BUILDING_STATUS_UPGRADE);
			return false;
		}
		
		Optional<QueueEntity> op = player.getData().getQueueEntities().stream().filter(e -> e.getQueueType() == QueueType.SCIENCE_QUEUE_VALUE).findAny();
		// 正在进行科技研究
		if(!immediate && op.isPresent()) {
			logger.error("tech Lab building is researching, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.Error.BUILDING_STATUS_TECHNOLOGY);
			return false;
		}

		TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, cfgId);
		// 科技配置数据错误
		if (cfg == null) {
			logger.error("tech level up failed, tech config error, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR_VALUE);
			return false;
		}

		if (!checkCondition(cfg)) {
			logger.error("tech level up failed, front condition not match, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.Error.CODITION_NOT_MATCH);
			return false;
		}

		TechnologyEntity entity = player.getData().getTechEntityByTechId(cfg.getTechId());
		if (entity == null) {
			entity = player.getData().createTechnologyEntity(cfg);
		}
		// 科技已达到对应的等级
		if(entity.getLevel() >= cfg.getLevel() ){
			logger.error("tech level up failed, lvl already arrive, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}

		double needTime = cfg.getLevelUpTime() * 1000 * (1 - (player.getEffect().getEffVal(EffType.EFF_1472) + player.getEffect().getEffVal(EffType.EFF_521)) * GsConst.EFF_PER);
		int speedEffPer = player.getData().getEffVal(EffType.CITY_SPD_SCIENCE);
		speedEffPer += player.getData().getEffVal(EffType.BACK_PRIVILEGE_CITY_SPD_SCIENCE);
		List<Integer> frontBuild = cfg.getConditionBuildList();
		if (!frontBuild.isEmpty()) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, frontBuild.get(0));
			speedEffPer += getTechSpeedEffByLevel(player, buildingCfg.getLevel());
		}
		
		needTime /= 1d + speedEffPer * GsConst.EFF_PER;
		List<ItemInfo> costItems = consume(cfg, type, needTime);
		if (costItems == null) {
			logger.error("tech level up failed, resource not enough, playerId: {}, cfgId: {}", player.getId(), cfgId);
			return false;
		}

		entity.setResearching(true);
		// 科技研究操作打点
		LogUtil.logTechResearchOperation(player, cfg.getTechId(), entity.getLevel(), immediate);
		
		if (immediate) {
			techLevelUp(cfg.getTechId());
		} else {
			QueueService.getInstance().addReusableQueue(player, QueueType.SCIENCE_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
					String.valueOf(cfgId), BuildingType.FIGHTING_LABORATORY_VALUE, needTime, costItems, GsConst.QueueReusage.TECH_UPGRADE);
		}
		
		BehaviorLogger.log4Service(player, Source.TECHNOLOGY_OPERATION, Action.TECHNOLOGY_LEVEL_UP, Params.valueOf("id", cfg.getId()));
		player.responseSuccess(protocol.getType());
		
		MissionManager.getInstance().postMsg(player, new EventStartTechUpgrade(cfg.getTechId()));
		return true;
	}

	/**
	 * 科技研究消耗资源
	 * @param cfg
	 * @param immediate
	 * @param needTime
	 * @return 科技研究原始消耗的资源
	 */
	private List<ItemInfo> consume(TechnologyCfg cfg, LvlUpType type, double needTime) {
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		// 考虑到作用号或其它加成的影响，在改方法体里面封装科技研究原始消耗
		List<ItemInfo> itemInfos = new ArrayList<>();
		itemInfos.addAll(cfg.getItemList());
		itemInfos.addAll(cfg.getCostList());
		// 资源消耗作用加成
		int[] goldArr = player.getEffect().getEffValArr(EffType.TECH_RESERH_REDUCE, EffType.TECH_RESEARCH_CONSUME_REDUCE,
				EffType.EFF_1467, EffType.RESEARCH_GOLDORE_REDUCE_PER, EffType.EFF_1468);
		int[] oillArr = player.getEffect().getEffValArr(EffType.TECH_RESERH_REDUCE, EffType.TECH_RESEARCH_CONSUME_REDUCE,
				EffType.EFF_1467, EffType.RESEARCH_OIL_REDUCE_PER, EffType.EFF_1469);
		int[] tombArr = player.getEffect().getEffValArr(EffType.TECH_RESERH_REDUCE, EffType.TECH_RESEARCH_CONSUME_REDUCE,
				EffType.EFF_1467, EffType.RESEARCH_TOMBARTHITE_REDUCE_PER, EffType.EFF_1470);
		int[] stelArr = player.getEffect().getEffValArr(EffType.TECH_RESERH_REDUCE, EffType.TECH_RESEARCH_CONSUME_REDUCE,
				EffType.EFF_1467, EffType.RESEARCH_STEEL_REDUCE_PER, EffType.EFF_1471);

		int[] medal = player.getEffect().getEffValArr(EffType.BLACK_TECH_367812);
		
		GameUtil.reduceByEffect(itemInfos, goldArr, oillArr, tombArr, stelArr, medal);
		int[] eff336Arr = player.getEffect().getEffValArr(EffType.EFF_336); //实际资源 = 基础资源*（1 - 各类资源减少）*（1 - 【本作用值】）
		GameUtil.reduceByEffect(itemInfos, eff336Arr, eff336Arr, eff336Arr, eff336Arr, eff336Arr);
		int[] eff340Arr = player.getEffect().getEffValArr(EffType.EFF_340); //实际资源 = 基础资源*（1 - 各类资源减少）*（1 - 【本作用值】）
		int[] eff341Arr = player.getEffect().getEffValArr(EffType.EFF_341); //实际资源 = 基础资源*（1 - 各类资源减少）*（1 - 【本作用值】）
		int[] eff342Arr = player.getEffect().getEffValArr(EffType.EFF_342); //实际资源 = 基础资源*（1 - 各类资源减少）*（1 - 【本作用值】）
		int[] eff343Arr = player.getEffect().getEffValArr(EffType.EFF_343); //实际资源 = 基础资源*（1 - 各类资源减少）*（1 - 【本作用值】）
		int[] emp = new int[0];
		GameUtil.reduceByEffect(itemInfos, eff340Arr, eff341Arr, eff342Arr, eff343Arr, emp);
		
		switch (type) {
		case NORMAL:
			consumeItems.addConsumeInfo(itemInfos, false);
			break;
		case BUY_RES:
			consumeItems.addConsumeInfo(itemInfos, true);
			break;
		case BUY_RES_AND_TIME:
			consumeItems.addConsumeInfo(itemInfos, true);
			long needSecond = (long) Math.ceil(needTime / 1000d);
			int freeTime = player.getFreeTechTime();
			if (needSecond > freeTime) {
				consumeItems.addConsumeInfo(PlayerAttr.GOLD, GameUtil.caculateTimeGold(needSecond - freeTime, SpeedUpTimeWeightType.TIME_WEIGHT_TECHNOLOGY));
			}
			break;
		}

		if (!consumeItems.checkConsume(player, HP.code.TECHNOLOGY_UPLEVEL_C_VALUE)) {
			return null;
		}

		AwardItems awardItems = consumeItems.consumeAndPush(player, Action.TECHNOLOGY_LEVEL_UP);
		return awardItems.getAwardItems();
	}
	
	/**
	 * 检查科技研究条件
	 * @param techCfg
	 * @return
	 */
	private boolean checkCondition(TechnologyCfg techCfg) {
		int conditionVipLvl = techCfg.getFrontVip();
		List<Integer> conditionTechList = techCfg.getConditionTechList();
		List<Integer> conditionBuildList = techCfg.getConditionBuildList();

		if(player.getVipLevel() < conditionVipLvl){
			return false;
		}
		if (!conditionBuildList.isEmpty()) {
			for (int condition : conditionBuildList) {
				BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, condition);
				if (cfg == null) {
					logger.error("tech level up failed, front build condition not match, playerId: {}, buildingCfgId: {}", player.getId(), condition);
					return false;
				}
				List<BuildingBaseEntity> entities = player.getData().getBuildingListByType(BuildingType.valueOf(cfg.getBuildType()));
				if (entities == null || entities.size() == 0) {
					logger.error("tech level up failed, front build condition not match, playerId: {}, buildType: {}", player.getId(), cfg.getBuildType());
					return false;
				}
				boolean match = false;
				for (BuildingBaseEntity buildingBaseEntity : entities) {
					if (buildingBaseEntity.getBuildingCfgId() >= condition) {
						match = true;
						break;
					}
				}
				if(!match){
					return false;
				}
			}
		}

		if (conditionTechList != null) {
			for (Integer condition : conditionTechList) {
				TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, condition);
				if (cfg == null) {
					logger.error("tech level up failed, front tech condition not match, playerId: {}, techCfgId: {}", player.getId(), condition);
					return false;
				}
				TechnologyEntity entity = player.getData().getTechEntityByTechId(cfg.getTechId());
				boolean match = false;
				if (entity != null && entity.getLevel() >= cfg.getLevel()) {
					match = true;
					break;
				}
				if(!match){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 科技队列已完成
	 * @return
	 */
	@MessageHandler
	private boolean onTechQueueFinishMsg(TechQueueFinishMsg msg) {
		int scienceId = msg.getScienceId();
		TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, scienceId);
		int techId = cfg.getTechId();
		return techLevelUp(techId);
	}

	/**
	 * 科技升级
	 * @param techId
	 * @return
	 */
	private boolean techLevelUp(int techId) {
		TechnologyEntity entity = player.getData().getTechEntityByTechId(techId);
		if (entity == null || !entity.isResearching()) {
			return false;
		}

		int beforeLvl = entity.getLevel();
		int afterLvl = beforeLvl + 1;
		TechnologyCfg befCfg = null;
		if (beforeLvl > 0) {
			befCfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, entity.getCfgId());
		}
		int befPower = befCfg == null ? 0 : befCfg.getBattlePoint();
		getPlayerData().getPlayerEffect().addEffectTech(player, entity);
		entity.setLevel(afterLvl);
		entity.setResearching(false);
		player.getPush().syncTechnologyLevelUpFinish(entity.getCfgId());
		player.refreshPowerElectric(PowerChangeReason.TECH_LVUP);

		TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, entity.getCfgId());
		// 如果科技解锁技能,则推送科技技能信息
		if (cfg.getTechSkill() > 0) {
			player.getPush().syncTechSkillInfo();
		}

		MissionManager.getInstance().postMsg(player, new EventTechnologyUpgrade(techId, beforeLvl, afterLvl, cfg.getTechType()));
		MissionManager.getInstance().postMsg(player, new EventTechTypePower(cfg.getTechType(), getTechBattlePointByType(cfg.getTechType())));
		ActivityManager.getInstance().postEvent(new TechnologyLevelUpEvent(player.getId(), 1,techId, cfg.getBattlePoint() - befPower));
		BehaviorLogger.log4Service(player, Source.TECHNOLOGY_OPERATION, Action.TECHNOLOGY_LEVELUP_FINISH, Params.valueOf("id", entity.getCfgId()));

		// 我要变强消息
		StrengthenGuideManager.getInstance().postMsg(new SGPlayerTechnologyUpdgrdeMsg(player, techId));

		return true;
	}

	/**
	 * 取消科技研究
	 * @return
	 */
	@MessageHandler
	private boolean onQuitResearch(CancelTechQueueMsg msg) {
		int techId = msg.getTechId();
		String cancelBackRes = msg.getCancelBackRes();
		TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, techId);
		TechnologyEntity entity = player.getData().getTechEntityByTechId(cfg.getTechId());
		if (entity == null || !entity.isResearching()) {
			return false;
		}

		if (!HawkOSOperator.isEmptyString(cancelBackRes)) {
			AwardItems awardItem = AwardItems.valueOf(cancelBackRes);
			awardItem.scale(ConstProperty.getInstance().getResearchCancelReclaimRate() / 10000d);
			awardItem.rewardTakeAffectAndPush(player, Action.TECHNOLOGY_CANCEL);
		}

		if (entity.getLevel() == 0) {
			player.getData().removeTechnologyEntity(entity);
		}
		BehaviorLogger.log4Service(player, Source.TECHNOLOGY_OPERATION, Action.TECHNOLOGY_CANCEL, Params.valueOf("id", cfg.getId()));
		return true;
	}
	
	/**
	 * 释放技能
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CAST_TECH_SKILL_REQ_C_VALUE)
	private boolean onCastSkill(HawkProtocol protocol) {
		PBCastTechSkillReq req = protocol.parseProtocol(PBCastTechSkillReq.getDefaultInstance());
		ITechSkill skill = TechSkillFactory.getInstance().getSkill(req.getSkillId());
		// 技能不存在
		if (skill == null) {
			sendError(HP.code.CAST_TECH_SKILL_REQ_C_VALUE, Status.Error.SKILL_CFG_ERROR_VALUE);
			logger.error("cast tech skill failed , skill not exist, playerId: {}, skillId: {}", player.getId(), req.getSkillId());
			return false;
		}
		skill.onCastSkill(player);
		return true;
	}
	
	/**
	 * 根据建筑等级获取对应的加速作用号
	 * @param buildLevel
	 */
	private int getTechSpeedEffByLevel(Player player, int buildLevel) {
		EffType[] effTypes = {EffType.RESEARCH_SPEED_LEVEL1_PER, EffType.RESEARCH_SPEED_LEVEL2_PER, EffType.RESEARCH_SPEED_LEVEL3_PER};
		return GameUtil.getBuildSpeedEffByLevel(player, effTypes, buildLevel);
	}
	
	/**
	 * 获取某类科技的总战力
	 * @param techType
	 * @return
	 */
	private int getTechBattlePointByType(int techType){
		int techBattlePoint = 0;
		List<TechnologyEntity> technologyEntities = getPlayerData().getTechnologyEntities();
		for (TechnologyEntity technologyEntity : technologyEntities) {
			if (technologyEntity.getLevel() > 0) {
				TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, technologyEntity.getCfgId());
				if (cfg != null && cfg.getTechType() == techType) {
					techBattlePoint += cfg.getBattlePoint();
				}
			}
		}
		return techBattlePoint;
	}
	
}
