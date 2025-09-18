
package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.CrossTechCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.CrossTechEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.msg.CrossTechQueueFinishMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.CrossTech.HPTechnologySync;
import com.hawk.game.protocol.CrossTech.LevelUpTechnologyReq;
import com.hawk.game.protocol.CrossTech.LvlUpType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.QueueService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventStartTechUpgrade;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.Source;

/**
 * 远征科技
 * @author shadow
 */
public class PlayerCrossTechModule extends PlayerModule {

	static Logger logger = LoggerFactory.getLogger("Server");

	public PlayerCrossTechModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		technologyEntityCheck();
		syncTechnologyInfo(player);
		return true;
	}
	
	
	/**
	 * 数据库中出现了  重复techId的数据
	 * 留下等级高的
	 * 出现了多条科技都在研究中的情况
	 * 还要看下是否有研究队列在进行中
	 */
	private void technologyEntityCheck(){
		if(this.player.isInDungeonMap()){
			return;
		}
		Map<Integer,CrossTechEntity> entityMap = new HashMap<>();
		Set<CrossTechEntity> dels = new HashSet<>();
		for (CrossTechEntity entity : player.getData().getCrossTechEntities()) {
			int techId = entity.getTechId();
			int level = entity.getLevel();
			//重复了，留下等级高的
			if(entityMap.containsKey(techId)){
				CrossTechEntity dupEntity = entityMap.get(techId);
				logger.info("technologyEntityCheck chek dupEntity,playerId: {}, techId: {},techLevel:{},dupTechId: {},dupTechLevel:{}",
						player.getId(), entity.getTechId(),entity.getLevel(),dupEntity.getTechId(),dupEntity.getLevel());
				if(level > dupEntity.getLevel()){
					entityMap.put(techId, entity);
					dels.add(dupEntity);
					logger.info("technologyEntityCheck chek add remove dupEntity,playerId: {}, techId: {},techLevel:{}",
							player.getId(), dupEntity.getTechId(),dupEntity.getLevel());
				}else{
					dels.add(entity);
					logger.info("technologyEntityCheck chek add remove dupEntity,playerId: {}, techId: {},techLevel:{}",
							player.getId(), entity.getTechId(),entity.getLevel());
				}
			}else{
				entityMap.put(techId, entity);
			}
		}
		//删除数据
		for(CrossTechEntity entity : dels){
			entity.delete();
			player.getData().getCrossTechEntities().remove(entity);
			logger.info("technologyEntityCheck remove Entity action,playerId: {}, techId: {},techLevel:{}",
					player.getId(), entity.getTechId(),entity.getLevel());
			
		}
		//查看研究中的科技是否有对应的队列
		for (CrossTechEntity entity : player.getData().getCrossTechEntities()) {
			if(entity.isResearching()){
				int techCfgId = entity.getNextLevelCfgId();
				Optional<QueueEntity> op = player.getData().getQueueEntities().stream()
						.filter(e ->e.getQueueType() == QueueType.CROSS_TECH_QUEUE_VALUE)
						.filter(e ->e.getItemId().equals(String.valueOf(techCfgId)) )
						.findAny();
				if(!op.isPresent()){
					logger.info("technologyEntityCheck queue null,playerId: {}, techId: {},techLevel:{}",
							player.getId(), entity.getTechId(),entity.getLevel());
					CrossTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossTechCfg.class, techCfgId);
					if(cfg != null){
						entity.setLevel(entity.getLevel() + 1);
						logger.info("technologyEntityCheck queue null level up,playerId: {}, techId: {},techLevel:{}",
								player.getId(), entity.getTechId(),entity.getLevel());
					}
					entity.setResearching(false);
				}
			}
		}
	}

	/**
	 * 升级科技
	 * @param id 科技Id
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CROSS_TECH_UPLEVEL_C_VALUE)
	protected boolean onLevelUp(HawkProtocol protocol) {
		LevelUpTechnologyReq req = protocol.parseProtocol(LevelUpTechnologyReq.getDefaultInstance());
		int cfgId = req.getTechId();
		LvlUpType type = req.getType();
		// 花水晶秒时间和资源升级
		boolean immediate = type == LvlUpType.BUY_RES_AND_TIME;
		
		// 航母建筑
		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.AIRCRAFT_CARRIER);
		// 研究科技的作战实验室建筑不存在
		if (buildingEntity == null) {
			logger.error("crosstech level up failed, crossTechLab building not exist, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.Error.CODITION_NOT_MATCH);
			return false;
		}
		// 前置开启条件
		long unlockTime = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime() + CrossConstCfg.getInstance().getServerDelayTime();
		if (HawkTime.getMillisecond() < unlockTime) {
			logger.error("crosstech level up failed, playerId: {}, unlockTime: {}", player.getId(), unlockTime);
			sendError(protocol.getType(), Status.SysError.DATA_ERROR);
			return false;
		}
		
		Optional<QueueEntity> op = player.getData().getQueueEntities().stream().filter(e -> e.getQueueType() == QueueType.CROSS_TECH_QUEUE_VALUE).findAny();
		// 正在进行科技研究
		if(!immediate && op.isPresent()) {
			logger.error("crossTechLab building is researching, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.Error.BUILDING_STATUS_TECHNOLOGY);
			return false;
		}

		CrossTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossTechCfg.class, cfgId);
		// 科技配置数据错误
		if (cfg == null) {
			logger.error("crosstech level up failed, tech config error, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR_VALUE);
			return false;
		}

		if (!checkCondition(cfg)) {
			logger.error("crosstech level up failed, front condition not match, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.Error.CODITION_NOT_MATCH);
			return false;
		}

		CrossTechEntity entity = player.getData().getCrossTechEntityByTechId(cfg.getTechId());
		if (entity == null) {
			entity = player.getData().createCrossTechEntity(cfg);
		}
		// 科技已达到对应的等级
		if(entity.getLevel() >= cfg.getLevel() ){
			logger.error("crosstech level up failed, lvl already arrive, playerId: {}, cfgId: {}", player.getId(), cfgId);
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}

		List<ItemInfo> costItems = consume(cfg, type, cfg.getLevelUpTime());
		if (costItems == null) {
			logger.error("crosstech level up failed, resource not enough, playerId: {}, cfgId: {}", player.getId(), cfgId);
			return false;
		}

		entity.setResearching(true);
//		// 科技研究操作打点
		LogUtil.logCrossTechResearchOperation(player, cfg.getTechId(), entity.getLevel(), 1);
		
		if (immediate) {
			techLevelUp(cfg.getTechId());
		} else {
			QueueService.getInstance().addReusableQueue(player, QueueType.CROSS_TECH_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE,
					String.valueOf(cfgId), BuildingType.AIRCRAFT_CARRIER_VALUE, cfg.getLevelUpTime(), costItems, GsConst.QueueReusage.CROSS_TECH_UPGRADE);
		}
		
		BehaviorLogger.log4Service(player, Source.CROSS_TECH_OPERATION, Action.CROSS_TECH_LEVEL_UP, Params.valueOf("id", cfg.getId()));
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
	private List<ItemInfo> consume(CrossTechCfg cfg, LvlUpType type, double needTime) {
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		// 考虑到作用号或其它加成的影响，在改方法体里面封装科技研究原始消耗
		List<ItemInfo> itemInfos = new ArrayList<>();
		itemInfos.addAll(cfg.getItemList());
		itemInfos.addAll(cfg.getCostList());
//		// 资源消耗作用加成
//		int[] goldArr = player.getEffect().getEffValArr(EffType.TECH_RESERH_REDUCE, EffType.TECH_RESEARCH_CONSUME_REDUCE,
//				EffType.EFF_1467, EffType.RESEARCH_GOLDORE_REDUCE_PER, EffType.EFF_1468);
//		int[] oillArr = player.getEffect().getEffValArr(EffType.TECH_RESERH_REDUCE, EffType.TECH_RESEARCH_CONSUME_REDUCE,
//				EffType.EFF_1467, EffType.RESEARCH_OIL_REDUCE_PER, EffType.EFF_1469);
//		int[] tombArr = player.getEffect().getEffValArr(EffType.TECH_RESERH_REDUCE, EffType.TECH_RESEARCH_CONSUME_REDUCE,
//				EffType.EFF_1467, EffType.RESEARCH_TOMBARTHITE_REDUCE_PER, EffType.EFF_1470);
//		int[] stelArr = player.getEffect().getEffValArr(EffType.TECH_RESERH_REDUCE, EffType.TECH_RESEARCH_CONSUME_REDUCE,
//				EffType.EFF_1467, EffType.RESEARCH_STEEL_REDUCE_PER, EffType.EFF_1471);
//
//		GameUtil.reduceByEffect(itemInfos, goldArr, oillArr, tombArr, stelArr);
		
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

		if (!consumeItems.checkConsume(player, HP.code.CROSS_TECH_UPLEVEL_C_VALUE)) {
			return null;
		}

		AwardItems awardItems = consumeItems.consumeAndPush(player, Action.CROSS_TECH_LEVEL_UP);
		return awardItems.getAwardItems();
	}
	
	/**
	 * 检查科技研究条件
	 * @param techCfg
	 * @return
	 */
	private boolean checkCondition(CrossTechCfg techCfg) {
		List<List<Integer>> conditionTechList = techCfg.getConditionTechList();
		// 无前置条件
		if (CollectionUtils.isEmpty(conditionTechList)) {
			return true;
		}
		boolean meetLimit = false;
		for (List<Integer> andList : conditionTechList) {
			boolean meet = true;
			for (Integer condition : andList) {
				CrossTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossTechCfg.class, condition);
				if (cfg == null) {
					logger.error("tech level up failed, front tech condition not match, playerId: {}, techCfgId: {}", player.getId(), condition);
					meet = false;
					break;
				}
				CrossTechEntity entity = player.getData().getCrossTechEntityByTechId(cfg.getTechId());
				if (entity == null || entity.getLevel() < cfg.getLevel()) {
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
	private boolean onTechQueueFinishMsg(CrossTechQueueFinishMsg msg) {
		int scienceId = msg.getScienceId();
		CrossTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossTechCfg.class, scienceId);
		int techId = cfg.getTechId();
		return techLevelUp(techId);
	}

	/**
	 * 科技升级
	 * @param techId
	 * @return
	 */
	private boolean techLevelUp(int techId) {
		CrossTechEntity entity = player.getData().getCrossTechEntityByTechId(techId);
		if (entity == null || !entity.isResearching()) {
			return false;
		}
		int nextLevelCfg = entity.getNextLevelCfgId();
		CrossTechCfg nextcfg = HawkConfigManager.getInstance().getConfigByKey(
				CrossTechCfg.class, nextLevelCfg);
		if(nextcfg == null){
			return false;
		}
		int beforeLvl = entity.getLevel();
		int afterLvl = beforeLvl + 1;
		getPlayerData().getPlayerEffect().addEffectCrossTech(player, entity);
		entity.setLevel(afterLvl);
		entity.setResearching(false);
		player.getPush().syncCrossTechLevelUpFinish(entity.getCfgId());
		player.refreshPowerElectric(PowerChangeReason.CROSS_TECH_LVUP);
		HPTechnologySync.Builder builder = HPTechnologySync.newBuilder();
		builder.addTechId(techId);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PLAYER_CROSS_TECH_SYNC_S, builder);
		player.sendProtocol(protocol);
		
		// 科技研究操作打点
				LogUtil.logCrossTechResearchOperation(player, techId, entity.getLevel(), 2);
		return true;
	}
	
	/**
	 * 获取某类科技的总战力
	 * @param techType
	 * @return
	 */
	public int getTechBattlePointByType(int techType){
		int techBattlePoint = 0;
		List<CrossTechEntity> technologyEntities = getPlayerData().getCrossTechEntities();
		for (CrossTechEntity technologyEntity : technologyEntities) {
			if (technologyEntity.getLevel() > 0) {
				CrossTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossTechCfg.class, technologyEntity.getCfgId());
				if (cfg != null && cfg.getTechType() == techType) {
					techBattlePoint += cfg.getBattlePoint();
				}
			}
		}
		return techBattlePoint;
	}
	
	/**
	 * 同步科技信息
	 */
	public void syncTechnologyInfo(Player player) {
		HPTechnologySync.Builder builder = HPTechnologySync.newBuilder();
		long serverOpenTime = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		if (!player.isCsPlayer()){
			builder.setUnlockTime(serverOpenTime + CrossConstCfg.getInstance().getServerDelayTime());
		}
		for (CrossTechEntity entity : player.getData().getCrossTechEntities()) {
			if (entity.getLevel() != 0) {
				builder.addTechId(entity.getCfgId());
			}
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PLAYER_CROSS_TECH_SYNC_S, builder);
		player.sendProtocol(protocol);
	}
	
}
