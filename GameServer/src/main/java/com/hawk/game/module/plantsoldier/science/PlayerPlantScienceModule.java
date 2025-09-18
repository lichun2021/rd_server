package com.hawk.game.module.plantsoldier.science;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.plantsoldier.science.cfg.PlantScienceCfg;
import com.hawk.game.msg.PlantScienceQueueFinishMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantScienceLvlUpReq;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantScienceLvlUpType;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.QueueService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventStartTechUpgrade;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.Source;


/**
 * 泰能科技
 * @author shadow
 */
public class PlayerPlantScienceModule extends PlayerModule {

	static Logger logger = LoggerFactory.getLogger("Server");

	public PlayerPlantScienceModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		 player.getPlantScience().plantScienceSync();
		return true;
	}

	/**
	 * 升级科技
	 * @param id 科技Id
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.PLAYER_PLANT_SCIENCE_LEVEL_UP_VALUE)
	protected boolean onLevelUp(HawkProtocol protocol) {
		PBPlantScienceLvlUpReq req = protocol.parseProtocol(PBPlantScienceLvlUpReq.getDefaultInstance());
		int cfgId = req.getTechId();
		PBPlantScienceLvlUpType type = req.getType();
		// 花水晶秒时间和资源升级
		boolean immediate = type == PBPlantScienceLvlUpType.PLANT_SCIENCE_LEVEL_UP_BUY_RES_AND_TIME;
		
		// 航母建筑
		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.PLANT_SCIENCE);
		// 研究科技的作战实验室建筑不存在
		if (buildingEntity == null) {
			logger.error("onLevelUp level up failed, building not exist, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.Error.CODITION_NOT_MATCH);
			return false;
		}
		
		Optional<QueueEntity> op = player.getData().getQueueEntities().stream().
				filter(e -> e.getQueueType() == QueueType.PLANT_SCIENCE_QUEUE_VALUE).findAny();
		// 正在进行科技研究
		if(!immediate && op.isPresent()) {
			logger.error("onLevelUp building is researching, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.Error.BUILDING_STATUS_TECHNOLOGY);
			return false;
		}

		PlantScienceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantScienceCfg.class, cfgId);
		// 科技配置数据错误
		if (cfg == null) {
			logger.error("onLevelUp failed, tech config error, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR_VALUE);
			return false;
		}

		if (!checkCondition(cfg)) {
			logger.error("onLevelUp failed, front condition not match, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.Error.CODITION_NOT_MATCH);
			return false;
		}
		
		PlantScience plantScience = player.getPlantScience();
		PlantScienceComponent component = plantScience.getComponentScienceId(cfg.getTechId());
		if (component == null) {
			component = plantScience.createScienceComponent(cfg.getTechId(), 0);
		}
		// 科技已达到对应的等级
		if(component.getLevel() >= cfg.getLevel() ){
			logger.error("plantScience failed, lvl already arrive, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		double needTime = cfg.getLevelUpTime();
		int speedEffPer = player.getData().getEffVal(EffType.CITY_SPD_SCIENCE);
		speedEffPer += player.getData().getEffVal(EffType.BACK_PRIVILEGE_CITY_SPD_SCIENCE);
		speedEffPer += player.getData().getEffVal(EffType.PLANT_SOLDIER_4126);
		needTime = needTime /(1d + speedEffPer * GsConst.EFF_PER) * (1 - (player.getEffect().getEffVal(EffType.EFF_1472) + player.getEffect().getEffVal(EffType.EFF_521)) * GsConst.EFF_PER);
		
		List<ItemInfo> costItems = consume(cfg, type, needTime);
		if (costItems == null) {
			logger.error("crosstech level up failed, resource not enough, playerId: {}, cfgId: {}", player.getId(), cfgId);
			return false;
		}
		//Tlog
		LogUtil.logPlantScienceResearchOperation(player, cfg.getTechId(), component.getLevel(), 1, plantScience.getTechPower());
		component.setState(PlantScienceState.RESEARCH.getNum());
		if (immediate) {
			player.getPlantScience().techLevelUp(cfg.getTechId());
		} else {
			plantScience.notifyChange();
			QueueService.getInstance().addReusableQueue(player, QueueType.PLANT_SCIENCE_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
					String.valueOf(cfgId), BuildingType.PLANT_SCIENCE_VALUE, needTime, costItems, GsConst.QueueReusage.PLANT_SCIENCE_UPGRADE);
		}
		BehaviorLogger.log4Service(player, Source.PLANT_SCIENCE_UPGRADE, Action.PLANT_SCIENCE_LEVEL_UP, Params.valueOf("id", cfg.getId()));
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
	private List<ItemInfo> consume(PlantScienceCfg cfg, PBPlantScienceLvlUpType type, double needTime) {
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		// 考虑到作用号或其它加成的影响，在改方法体里面封装科技研究原始消耗
		List<ItemInfo> itemInfos = new ArrayList<>();
		itemInfos.addAll(cfg.getItemList());
		itemInfos.addAll(cfg.getCostList());
		
		int eff344 = player.getData().getEffVal(EffType.EFF_344);
		for (ItemInfo itemInfo : itemInfos) {
			double count = itemInfo.getCount() * (1 - eff344 * GsConst.EFF_PER);;
			int itemCount = (int) Math.ceil(count);
			itemInfo.setCount(Math.max(0, itemCount));
		}
		GameUtil.reduceByEffect(itemInfos, 21063002, player.getEffect().getEffValArr(EffType.EFF_345));
		GameUtil.reduceByEffect(itemInfos, 21063003, player.getEffect().getEffValArr(EffType.EFF_346));
		GameUtil.reduceByEffect(itemInfos, 21063005, player.getEffect().getEffValArr(EffType.EFF_347));
		
		switch (type) {
		case PLANT_SCIENCE_LEVEL_UP_NORMAL:
			consumeItems.addConsumeInfo(itemInfos, false);
			break;
		case PLANT_SCIENCE_LEVEL_UP_BUY_RES:
			consumeItems.addConsumeInfo(itemInfos, true);
			break;
		case PLANT_SCIENCE_LEVEL_UP_BUY_RES_AND_TIME:
			consumeItems.addConsumeInfo(itemInfos, true);
			long needSecond = (long) Math.ceil(needTime / 1000d);
			int freeTime = player.getFreeTechTime();
			if (needSecond > freeTime) {
				consumeItems.addConsumeInfo(PlayerAttr.GOLD, GameUtil.caculateTimeGold(needSecond - freeTime, SpeedUpTimeWeightType.TIME_WEIGHT_TECHNOLOGY));
			}
			break;
		}

		if (!consumeItems.checkConsume(player, HP.code2.PLAYER_PLANT_SCIENCE_LEVEL_UP_VALUE)) {
			return null;
		}

		AwardItems awardItems = consumeItems.consumeAndPush(player, Action.PLANT_SCIENCE_LEVEL_UP);
		return awardItems.getAwardItems();
	}
	
	/**
	 * 检查科技研究条件
	 * @param techCfg
	 * @return
	 */
	private boolean checkCondition(PlantScienceCfg techCfg) {
		List<List<Integer>> conditionTechList = techCfg.getConditionTechList();
		List<Integer> conditionBuildList = techCfg.getConditionBuildList();
		// 无前置条件
		if (CollectionUtils.isEmpty(conditionTechList) && 
				CollectionUtils.isEmpty(conditionBuildList)) {
			return true;
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

		boolean meetLimit = false;
		if(conditionTechList.isEmpty()){
			meetLimit = true;
		}
		for (List<Integer> andList : conditionTechList) {
			boolean meet = true;
			for (Integer condition : andList) {
				PlantScienceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantScienceCfg.class, condition);
				if (cfg == null) {
					logger.error("tech level up failed, front tech condition not match, playerId: {}, techCfgId: {}", player.getId(), condition);
					meet = false;
					break;
				}
				PlantScienceComponent component = player.getPlantScience().getComponentScienceId(cfg.getTechId());
				if (component == null || component.getLevel() < cfg.getLevel()) {
					meet = false;
					break;
				}
			}
			if (meet) {
				meetLimit = true;
				break;
			}
		}
		return meetLimit;
	}

	/**
	 * 科技队列已完成
	 * @return
	 */
	@MessageHandler
	private boolean onTechQueueFinishMsg(PlantScienceQueueFinishMsg msg) {
		int scienceId = msg.getScienceId();
		PlantScienceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantScienceCfg.class, scienceId);
		int techId = cfg.getTechId();
		return player.getPlantScience().techLevelUp(techId);
	}

	
	
	
}
