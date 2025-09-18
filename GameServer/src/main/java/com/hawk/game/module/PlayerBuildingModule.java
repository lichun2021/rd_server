package com.hawk.game.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.collection.ConcurrentHashTable;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Table;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.CityResourceCollectEvent;
import com.hawk.game.GsConfig;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.BaseUpgradeCfg;
import com.hawk.game.config.BuildAreaCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.StoryMissionEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.msg.BuildingQueueCancelMsg;
import com.hawk.game.msg.BuildingQueueFinishMsg;
import com.hawk.game.msg.BuildingRecoverFinishMsg;
import com.hawk.game.msg.GuardOutFireMsg;
import com.hawk.game.msg.SuperSoldierTriggeTaskMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Building.BuildingAreaUnlockReq;
import com.hawk.game.protocol.Building.BuildingAwardTakeInfo;
import com.hawk.game.protocol.Building.BuildingAwardTakePB;
import com.hawk.game.protocol.Building.BuildingCreateReq;
import com.hawk.game.protocol.Building.BuildingMoveReq;
import com.hawk.game.protocol.Building.BuildingMoveResp;
import com.hawk.game.protocol.Building.BuildingRemovePushPB;
import com.hawk.game.protocol.Building.BuildingRemoveReq;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.Building.BuildingUpgradeReq;
import com.hawk.game.protocol.Building.CollectRecruits;
import com.hawk.game.protocol.Building.PushLastResCollectTime;
import com.hawk.game.protocol.Building.ResBuildingInfo;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SuperSoldier.SupersoldierTaskType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.BuildingService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.QueueService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.guildtask.event.BuildingLvlUpTaskEvent;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventBuildUpDoing;
import com.hawk.game.service.mssion.event.EventBuildingUpgrade;
import com.hawk.game.service.mssion.event.EventUnlockGround;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.Source;

import redis.clients.jedis.Jedis;

/**
 * 建筑模块
 *
 * @author julia
 */
public class PlayerBuildingModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 上次刷新时间
	 */
	private long lastTickTime = 0;
	/**
	 * tick次数
	 */
	private int tickCount = 0;
	
	/**
	 * 构造函数
	 * 
	 * @param player
	 */
	public PlayerBuildingModule(Player player) {
		super(player);
	}

	/**
	 * 更新，城防需要走tick更新原因在于，城防值上限除了建筑表本身的配置外，可能还受科技或其他作用号的影响
	 */
	@Override
	public boolean onTick() {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		if (lastTickTime > 0 && currentTime >= lastTickTime + 1000) {
			lastTickTime = currentTime;
			BuildingService.getInstance().updateCityDef(player);
			if (++tickCount > 10) {
				tickCount = 0;
				CityManager.getInstance().cityDefCalculate(player);
			}
		}
		
		return super.onTick();
	}

	/**
	 * 玩家登陆处理(数据同步)
	 */
	@Override
	protected boolean onPlayerLogin() {
		long now = HawkTime.getMillisecond();
		lastTickTime = now;
		// 检测异常
		GameUtil.checkBuildingStatus(player);
		checkAndRemoveRepeatedBuilding();
		autoUnlockAreaToOldPlayer();
		fixStatusData();
		
		// 初始化资源建筑的存储量和产出速率
		for (BuildingBaseEntity entity : player.getData().getBuildingEntities()) {
			BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId());
			if (cfg == null || !cfg.isResBuilding()) {
				continue;
			}

			if (entity.getLastResCollectTime() > now) {
				entity.setLastResCollectTime(now);
			}
			BuildingService.getInstance().refreshResStoreAndOutput(player.getData(), entity, true);
		}
		
		player.getPush().syncBuildingEntityInfo();
		player.getPush().synUnlockedArea();
		BuildingService.getInstance().updateCityDef(player);
		// 同步城防状态
		player.getPush().syncCityDef(false);
		syncResOutputUnitBuff();
		syncBuildingAwardInfo();
		
		// 删除过期时间
		try (Jedis jedis = LocalRedis.getInstance().getRedisSession().getJedis()) {
			String key = LocalRedis.getInstance().getLocalIdentify() + ":build_award:" + player.getId();
			jedis.persist(key);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		BuildingCfg cfg = player.getData().getBuildingCfgByType(Const.BuildingType.CONSTRUCTION_FACTORY);
		// 兼容处理--玩家登陆时推送大本的升级时间,推送当前玩家大本的等级
		if (cfg != null) {
			ActivityManager.getInstance().postEvent(new BuildingLevelUpEvent(player.getId(), cfg.getBuildType(), cfg.getLevel(), cfg.getProgress(), true, 0));
		}
		
		
		int costGold1 = GameUtil.caculateResGold(PlayerAttr.valueOf(1007), 3000000);
		int costGold2 = GameUtil.caculateResGold(PlayerAttr.valueOf(1007), 3000000);
		
		int costGold3 = GameUtil.caculateResGold(PlayerAttr.valueOf(1007), 6000000);
		System.out.println(costGold1 + costGold2);
		System.out.println(costGold3);
		return true;
	}
	
	/**
	 * 修复城点保护罩StatusEntity数据
	 */
	private void fixStatusData() {
		try {
			StatusDataEntity entity = player.getData().getStatusById(Const.EffType.CITY_SHIELD_VALUE);
			if (entity == null) {
				HawkLog.logPrintln("player login fixStatusData, add city_shield entity, playerId: {}", player.getId());
				player.getData().addStatusBuff(Const.EffType.CITY_SHIELD_VALUE, HawkTime.getMillisecond() + 1000);
			}
			
			StoryMissionEntity missionEntity = player.getData().getStoryMissionEntity();
			if (missionEntity.getChapterId() != 7) {
				return;
			}
			
			MissionEntityItem missionItem = missionEntity.getStoryMissionItem(7060);
			if (missionItem != null && missionItem.getValue() == 7 && player.getCityLevel() >= 8) {
				HawkLog.logPrintln("player login fixStatusData, playerId: {}", player.getId());
				cityLevelUp();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 执行打本升级的后续逻辑
	 */
	private void cityLevelUp() {
		int cnt = player.getData().getItemNumByItemId(Const.ItemId.ITEM_SELECT_MOVE_CITY_NEW_VALUE);
		if (cnt > 0) {
			ConsumeItems consumeItems = ConsumeItems.valueOf();
			consumeItems.addItemConsume(Const.ItemId.ITEM_SELECT_MOVE_CITY_NEW_VALUE, cnt);
			if (consumeItems.checkConsume(player)) {
				consumeItems.consumeAndPush(player, Action.NEWLY_LEAVE);
			}
		}
		
		// 如果是大本等级提升, 更新到自己的城点信息中去
		WorldPlayerService.getInstance().updatePlayerPointInfo(player.getId(), player.getName(), player.getCityLevel(), player.getIcon(), player.getData().getPersonalProtectVals());
		
		BuildingCfg buildingCfg = player.getData().getBuildingCfgByType(BuildingType.CONSTRUCTION_FACTORY);
		// 刷新建筑工厂等级排行榜
		// 荣耀建筑等级处理
		int rankScore = buildingCfg.getLevel();
		int honor = buildingCfg.getHonor();
		int progress = buildingCfg.getProgress();
		// 当前大本建筑进行了荣耀升级
		if (honor > 0 || progress > 0) {
			rankScore = buildingCfg.getLevel() * RankService.HONOR_CITY_OFFSET + progress;
		}
		player.updateRankScore(MsgId.CITY_LEVEL_RANK_REFRESH, RankType.PLAYER_CASTLE_KEY, rankScore);
		MissionService.getInstance().onCityLevelUp(player, buildingCfg.getLevel());
		
		// 触发新解锁的资源建筑作用号加成推送
		player.getEffect().syncNewUnlockedBuildingBuff(player);
		
		BuildingService.getInstance().updateAccountRoleInfo(player, buildingCfg.getLevel());
		//
		BuildingService.getInstance().addExp(player, buildingCfg, Action.PLAYER_BUILDING_UPGRADE);
		player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
		BuildingService.getInstance().refreshBuildEffect(player, buildingCfg, true);
		// 任务事件
		MissionManager.getInstance().postMsg(player, new EventBuildingUpgrade(buildingCfg.getId(), buildingCfg.getLevel() - 1, buildingCfg.getLevel()));
		// 联盟任务-建筑升级
		GuildService.getInstance().postGuildTaskMsg(new BuildingLvlUpTaskEvent(player.getGuildId()));
	}
	
	/**
	 * 老玩家特殊处理
	 */
	private void autoUnlockAreaToOldPlayer() {
		// 新玩家跳过
		if (player.getCreateTime() >= ConstProperty.getInstance().getNewbieVersionTimeValue()) {
			return;
		}
		
		try {
			Set<Integer> unlockedAreas = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
			ConfigIterator<BuildAreaCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BuildAreaCfg.class);
			while (iterator.hasNext()) {
				BuildAreaCfg cfg = iterator.next();
				int areaId = cfg.getId();
				if (unlockedAreas.contains(areaId)) {
					continue;
				}
				
				player.unlockArea(areaId);			
				// 解锁地块任务
				MissionManager.getInstance().postMsg(player, new EventUnlockGround(areaId));
				MissionManager.getInstance().postSuperSoldierTaskMsg(player, new SuperSoldierTriggeTaskMsg(SupersoldierTaskType.UNLOCK_AREA_TASK, 1));
				BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.BUIDING_AREA_UNLOCK, Params.valueOf("buildAreaId", areaId));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		try {
			List<BuildingBaseEntity> buildingList = player.getData().getBuildingListByType(BuildingType.SUER_SOLDIER_BUILD);
			if (!buildingList.isEmpty()) {
				return;
			}
			int buildCfgId = 222501;
			final BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildCfgId);
			BuildingBaseEntity building = player.getData().createBuildingEntity(buildingCfg, "1", false);
			player.getData().refreshNewBuilding(building);
			BuildingService.getInstance().refreshBuildEffect(player, buildingCfg, false);
			LogUtil.logBuildFlow(player, building, 0, 1);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 检测删除重复建筑
	 */
	protected void checkAndRemoveRepeatedBuilding() {
		try {
			Table<Integer, String, BuildingBaseEntity> buildingMap = ConcurrentHashTable.create();
			for (BuildingBaseEntity entity : player.getData().getBuildingEntitiesIgnoreStatus()) {
				if (!buildingMap.contains(entity.getType(), entity.getBuildIndex())) {
					buildingMap.put(entity.getType(), entity.getBuildIndex(), entity);
					continue;
				}
				
				BuildingBaseEntity oldBuilding = buildingMap.get(entity.getType(), entity.getBuildIndex());
				if (oldBuilding.getBuildingCfgId() < entity.getBuildingCfgId()) {
					buildingMap.put(entity.getType(), entity.getBuildIndex(), entity);
				}
			}
			
			List<BuildingBaseEntity> removeBuildings = new LinkedList<BuildingBaseEntity>();
			for (BuildingBaseEntity entity : player.getData().getBuildingEntitiesIgnoreStatus()) {
				if (entity != buildingMap.get(entity.getType(), entity.getBuildIndex())) {
					removeBuildings.add(entity);
				}
			}
			
			if (!removeBuildings.isEmpty()) {
				player.getData().getBuildingEntitiesIgnoreStatus().removeAll(removeBuildings);
				removeBuildings.stream().forEach(e -> e.delete(true));
				HawkLog.logPrintln("removeRepeatedBuilding, playerId: {}, remove count: {}", player.getId(), removeBuildings.size());
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

	@Override
	protected boolean onPlayerLogout() {
		lastTickTime = 0;
		player.getData().setMaxCityDef(0);
		CityManager.getInstance().wallFireSpeedStore(player);
		return true;
	}
	
	/**
	 * 同步资源田建筑增产作用号单元数据
	 */
	protected void syncResOutputUnitBuff() {
		player.getData().getPlayerEffect().syncResOutputBuff(player);
	}
	
	/**
	 * 同步建筑奖励        
	 */
	private void syncBuildingAwardInfo() {
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
		if (accountInfo != null && accountInfo.isNewly()) {
			return;
		}
		
		Map<String, String> rewardInfoMap = LocalRedis.getInstance().getBuildRewardInfo(player.getId());
		if (rewardInfoMap.isEmpty()) {
			return;
		}
		
		BuildingAwardTakeInfo.Builder rewardInfo = BuildingAwardTakeInfo.newBuilder();
		for (Entry<String, String> entry : rewardInfoMap.entrySet()) {
			BuildingAwardTakePB.Builder builder = BuildingAwardTakePB.newBuilder();
			builder.setCfgId(Integer.parseInt(entry.getKey()));
			builder.setAwardIndex(Integer.parseInt(entry.getValue()));
			rewardInfo.addAwardTakeInfo(builder);
		}
		
		if (rewardInfo.getAwardTakeInfoCount() > 0) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.BUILDING_AWARD_PUSH_VALUE, rewardInfo));
		} 
	}
	

	/**
	 * 收取资源
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COLLECT_RESOURCE_C_VALUE)
	private boolean onCollectResource(HawkProtocol protocol) {
		// 判断模块关闭
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.GATHERRESINCITY)) {
			return false;
		}
		
		CollectRecruits req = protocol.parseProtocol(CollectRecruits.getDefaultInstance());
		List<String> buildingUUidList = req.getIdList();
		if (buildingUUidList.isEmpty()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		// 防重
		Set<String> buildingUuidSet = new HashSet<>();
		buildingUuidSet.addAll(buildingUUidList);
		
		long curTime = HawkTime.getMillisecond();
		Map<BuildingBaseEntity, Integer> buildingCollectIntervals = new HashMap<>();
		AwardItems award = AwardItems.valueOf();
		PushLastResCollectTime.Builder responseBuilder = PushLastResCollectTime.newBuilder();
		for(String id : buildingUUidList) { 
			BuildingBaseEntity buildingEntity = player.getData().getBuildingBaseEntity(id);
			
			if (buildingEntity == null || !BuildingCfg.isResBuildingType(buildingEntity.getType())) {
				logger.error("resource building not exist, playerId: {}, uuid: {}", player.getId(), id);
				sendError(protocol.getType(), Status.Error.BUILDING_NOT_EXISIT);
				return false;
			}
			
			// 被尤里占领,不能收取
			if(!GsConfig.getInstance().isRobotMode() && player.isBuildingLockByYuri(buildingEntity.getBuildIndex())){
				continue;
			}

			long collectInterval = curTime - buildingEntity.getLastResCollectTime();
			// 收取时间间隔小于1s时不让收取
			if (collectInterval < 1000) {
				logger.error("collect resource interval too short, playerId: {}, buildCfgId: {}, uuid: {}, lastTime: {}", 
						player.getId(), buildingEntity.getBuildingCfgId(), id, buildingEntity.getLastResCollectTime());
				continue;
			}
			
			player.collectResource(id, buildingEntity.getBuildingCfgId(), collectInterval, award, false);
			buildingCollectIntervals.put(buildingEntity, (int) collectInterval / 1000);
			
			ResBuildingInfo.Builder builder = ResBuildingInfo.newBuilder();
			builder.setId(id);
			builder.setTime(curTime);
			responseBuilder.addBuildingInfos(builder);
			
			logger.debug("collect resource, playerId: {}, buildCfgId: {}, uuid: {}, lastTime: {}, timeLong: {}", 
					player.getId(), buildingEntity.getBuildingCfgId(), id, buildingEntity.getLastResCollectTime(), collectInterval);
		}
		
		if (award.hasAwardItem()) {
			addExtraResource(award);
			
			// 计算石油转化作用号(注意会改变award，在发奖前调用)
			player.calcOilChangeEff(award);
			
			award.rewardTakeAffectAndPush(player, Action.BUILDING_COLLECT_RES);
		}
		
		// 同步收取时间
		HawkProtocol resp = HawkProtocol.valueOf(HP.code.PUSH_LAST_RESOURCE_COLLECT_TIME_S, responseBuilder);
		player.sendProtocol(resp);
		
		if (!buildingCollectIntervals.isEmpty()) {
			buildingCollectIntervals.keySet().stream().forEach(e -> e.setLastResCollectTime(curTime));
			//活动那边多次收取的时候算一次
			CityResourceCollectEvent event = new CityResourceCollectEvent(player.getId());
			event.setCollectTime(new ArrayList<Integer>(buildingCollectIntervals.values()));
			ActivityManager.getInstance().postEvent(event);
		}		
		
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 * 收取资源时获得额外安全资源
	 */
	private void addExtraResource(AwardItems award) {
		try {
			addExtraResource(PlayerAttr.GOLDORE_UNSAFE_VALUE, PlayerAttr.GOLDORE_VALUE, award);
			addExtraResource(PlayerAttr.OIL_UNSAFE_VALUE, PlayerAttr.OIL_VALUE, award);
			addExtraResource(PlayerAttr.STEEL_UNSAFE_VALUE, PlayerAttr.STEEL_VALUE, award);
			addExtraResource(PlayerAttr.TOMBARTHITE_UNSAFE_VALUE, PlayerAttr.TOMBARTHITE_VALUE, award);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 添加额外安全资源
	 * 
	 * @param unsafeCount
	 * @param safeResItemId
	 * @param award
	 */
	private void addExtraResource(int unsafeResItemId, int safeResItemId, AwardItems award) {		
		ItemInfo unsafeResItem = award.getItem(ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE, unsafeResItemId);
		int unsafeCount = (int) unsafeResItem.getCount();
		if (unsafeCount <= 0) {
			return;
		}
		
		double collectExtraEff = player.getEffect().getEffVal(EffType.COLLECT_RESOURCE_EXTRA_PER) * GsConst.EFF_PER;
		
		int count = 0;
		if (collectExtraEff > 0) {
			boolean goudaCrit = GameUtil.randomProbability(ConstProperty.getInstance().getGouda_crit());
			if (goudaCrit) {
				collectExtraEff *= 2;
			}
			count = (int)(unsafeCount * collectExtraEff);
		}
		
		award.addItem(ItemType.PLAYER_ATTR_VALUE, safeResItemId, count);
	}

	/**
	 * 解锁区块
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BUILDING_AREA_UNLOCK_C_VALUE)
	protected boolean unlockArea(HawkProtocol protocol) {
		BuildingAreaUnlockReq req = protocol.parseProtocol(BuildingAreaUnlockReq.getDefaultInstance());
		int areaId = req.getAreaId();
		// 判断区块是否已解锁
		Set<Integer> unlockedAreas = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
		if (unlockedAreas.contains(areaId)) {
			sendError(protocol.getType(), Status.Error.BUILDING_AREA_UNLOCKED_VALUE);
			logger.error("the area already unlocked, playerId: {}, areaId: {}", player.getId(), areaId);
			return false;
		}

		// 判断是否是当前可解锁的区块
		List<Integer> areaList = BuildAreaCfg.getUnlockedArea(player.getCityLevel());
		if (!areaList.contains(areaId)) {
			sendError(protocol.getType(), Status.Error.BUILDING_AREA_UNLOCK_NOT_ALLOWED);
			logger.error("the area not allowed unlock, playerId: {}, areaId: {}", player.getId(), areaId);
			return false;
		}

		// 解锁区域消耗
		BuildAreaCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildAreaCfg.class, areaId);
		if (cfg == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			logger.error("build area config error, playerId: {}, areaId: {}", player.getId(), areaId);
			return false;
		}
		
		int chapterId = player.getData().getStoryMissionEntity().getChapterId();
		if (chapterId < cfg.getUnlockDramaLevel()) {
			sendError(protocol.getType(), Status.Error.BUILDING_AREA_UNLOCK_NOT_ALLOWED);
			logger.error("the area not allowed unlock, playerId: {}, areaId: {}, chapterId: {}", player.getId(), areaId, chapterId);
			return false;
		}
		
		// 不允许解锁
		if (!cfg.isAllowedUnlock()) {
			sendError(protocol.getType(), Status.Error.BUILDING_AREA_UNLOCK_NOT_ALLOWED);
			logger.error("build area unlock not allowed, playerId: {}, areaId: {}", player.getId(), areaId);
			return false;
		}
		
		player.unlockArea(areaId);
		
		List<ItemInfo> rewardItems = cfg.getUnlockAwardItems();
		if (!rewardItems.isEmpty()) {
			AwardItems awardItem = AwardItems.valueOf();
			awardItem.addItemInfos(rewardItems);
			awardItem.rewardTakeAffectAndPush(player, Action.BUIDING_AREA_UNLOCK, true);
		}
		
		player.responseSuccess(protocol.getType());
		// 推送建筑信息
		player.getPush().synUnlockedArea();
		// 解锁地块任务
		MissionManager.getInstance().postMsg(player, new EventUnlockGround(areaId));
        MissionManager.getInstance().postSuperSoldierTaskMsg(player, new SuperSoldierTriggeTaskMsg(SupersoldierTaskType.UNLOCK_AREA_TASK, 1));
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.BUIDING_AREA_UNLOCK, Params.valueOf("buildAreaId", areaId));
		return true;
	}
	
	/**
	 * 领取大本升级奖励（选择性奖励）
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BUILDING_AWARD_TAKE_C_VALUE)
	protected boolean takeBuildingAward(HawkProtocol protocol) {
		BuildingAwardTakePB req = protocol.parseProtocol(BuildingAwardTakePB.getDefaultInstance());
		int cfgId = req.getCfgId();
		int index = req.getAwardIndex();
		// 参数错误
		if (cfgId <= 0 || index <= 0) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("take baseBuildingUpgradeReward param error, playerId: {}, cfgId: {}, index: {}", player.getId(), cfgId, index);
			return false;
		}
		
		// 配置信息未找到
		BaseUpgradeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BaseUpgradeCfg.class, cfgId);
		if (cfg == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR_VALUE);
			HawkLog.errPrintln("take baseBuildingUpgradeReward config error, playerId: {}, cfgId: {}", player.getId(), cfgId);
			return false;
		}
		
		// 建筑等级不对
		if (cfg.getBaseBuildId() > player.getData().getBuildingEntityByType(BuildingType.CONSTRUCTION_FACTORY).getBuildingCfgId()) {
			sendError(protocol.getType(), Status.Error.BUILDING_LEVEL_NOT_MATCH);
			HawkLog.errPrintln("take baseBuildingUpgradeReward failed, playerId: {}, cfgId: {}, config buildId: {}, cityLevel: {}", player.getId(), cfgId, cfg.getBaseBuildId(), player.getCityLevel());
			return false;
		}
		
		// 看有没有领取过
		Map<String, String> rewardInfoMap = LocalRedis.getInstance().getBuildRewardInfo(player.getId());
		if (rewardInfoMap.containsKey(String.valueOf(cfgId))) {
			sendError(protocol.getType(), Status.Error.BUILDIGN_AWARD_HAS_TAKEN);
			HawkLog.errPrintln("take baseBuildingUpgradeReward failed, reward has taken, playerId: {}, cfgId: {}", player.getId(), cfgId);
			return false;
		}
		
		// 看奖励是否存在
		ItemInfo rewardItemInfo = cfg.getReward(index);
		if (rewardItemInfo == null) {
			sendError(protocol.getType(), Status.Error.BUILDIGN_AWARD_NOT_EXIST);
			HawkLog.errPrintln("take baseBuildingUpgradeReward failed, reward not exist, playerId: {}, cfgId: {}, index: {}", player.getId(), cfgId, index);
			return false;
		}
		
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItem(rewardItemInfo);
		awardItem.rewardTakeAffectAndPush(player, Action.BUILD_SELECTABLE_REWARD, true);
		
		// 存储奖励信息
		LocalRedis.getInstance().updateBuildRewardInfo(player.getId(), cfgId, index);
		
		player.responseSuccess(protocol.getType());
		
		syncBuildingAwardInfo();
		
		return true;
	}

	/**
	 * 拆除建筑
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BUILDING_REMOVE_C_VALUE)
	private boolean onRemoveBuilding(HawkProtocol protocol) {
		BuildingRemoveReq req = protocol.parseProtocol(BuildingRemoveReq.getDefaultInstance());
		final String uuid = req.getUuid();
		BuildingBaseEntity buildingEntity = getPlayerData().getBuildingBaseEntity(uuid);
		// 该建筑是否存在
		if (buildingEntity == null) {
			sendError(protocol.getType(), Status.Error.BUILDING_NOT_EXISIT);
			logger.error("building remove failed, building not exist, playerId: {}, buildingId: {}, buildingEntity: {}", player.getId(), uuid, buildingEntity);
			return false;
		}
		
		// 被尤里占领,不能操作
		if (!GsConfig.getInstance().isRobotMode() &&  player.isBuildingLockByYuri(buildingEntity.getBuildIndex())) {
			sendError(protocol.getType(), Status.Error.BUILDING_YURI_LOCK);
			logger.error("building remove failed, building occupied by yuri, playerId: {}, buildingId: {}, buildingEntity: {}", player.getId(), uuid, buildingEntity);
			return false;
		}

		// 判断建筑是否正在走时间队列
		if (!BuildingService.getInstance().checkBuildingStatus(player, buildingEntity, protocol.getType())) {
			return false;
		}

		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		// 非共享地块的建筑不能拆除
		if (buildingCfg == null || !BuildAreaCfg.isShareBlockBuildType(buildingCfg.getBuildType())) {
			sendError(protocol.getType(), Status.Error.BUILDING_TYPE_NOT_SHATE_BLOCK);
			logger.error("building remove failed, buildingType error, playerId: {}, buildingId: {}, buildType: {}", player.getId(), uuid, buildingCfg == null ? 0 : buildingCfg.getBuildType());
			return false;
		}

		// 有伤兵待治疗或有兵待收取，不允许拆除
		if (buildingEntity.getType() == BuildingType.HOSPITAL_STATION_VALUE) {
			Map<String, QueueEntity> queueMap = player.getData().getQueueEntitiesByType(QueueType.CURE_QUEUE_VALUE);
			if (!queueMap.isEmpty()) {
				sendError(protocol.getType(), Status.Error.BUILDING_STATUS_CURE);
				logger.error("building remove failed, hospital is busy, playerId: {}, buildingId: {}, building cfgId: {}", player.getId(), uuid, buildingEntity.getBuildingCfgId());
				return false;
			}
			
			if (buildingEntity.getStatus() == BuildingStatus.SOLDIER_WOUNDED_VALUE) {
				sendError(protocol.getType(), Status.Error.SOLDIER_WOUND_NEED_CURE);
				logger.error("building remove failed, wouned soldier need to cure, playerId: {}, buildingId: {}, building cfgId: {}, status: {}",
						player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId(), buildingEntity.getStatus());
				return false;
			}
			
			if (buildingEntity.getStatus() == BuildingStatus.CURE_FINISH_HARVEST_VALUE) {
				sendError(protocol.getType(), Status.Error.CURE_FINISH_SOLDIER_NOT_NONE);
				logger.error("building remove failed, cure finish soldier need collect, playerId: {}, buildingId: {}, building cfgId: {}, status: {}",
						player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId(), buildingEntity.getStatus());
				return false;
			}
		}

		boolean immediate = req.getImmediately();
		double costTime = BuildingService.getInstance().getCostTime(player, buildingCfg.getDismantleTime(), buildingCfg.getLevel());
		// 检查建筑队列是否被占用
		if (!immediate && !BuildingService.getInstance().checkQueueStatus(player, (long) costTime, protocol.getType())) {
			return false;
		}

		if (immediate) {
			long needTime = (long) Math.ceil(costTime / 1000d);
			int freeTime = player.getFreeBuildingTime();
			if (needTime > freeTime) {
				ConsumeItems consume = ConsumeItems.valueOf();
				consume.addConsumeInfo(PlayerAttr.GOLD, GameUtil.caculateTimeGold(needTime - freeTime, SpeedUpTimeWeightType.TIME_WEIGHT_BUILDING));
				if (!consume.checkConsume(player, protocol.getType())) {
					logger.error("building remove consume error, playerId: {}, buildCfgId: {}", player.getId(), buildingCfg.getId());
					return false;
				}
				consume.consumeAndPush(player, Action.PLAYER_BUILDING_REMOVE);
			}
			BuildingService.getInstance().removeBuilding(buildingEntity, player);
		} else {
			QueueService.getInstance().addReusableQueue(player, QueueType.BUILDING_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_REMOVE_VALUE,
					buildingEntity.getId(), buildingEntity.getType(), costTime, null, GsConst.QueueReusage.BUILDING_OPERATION);
		}

		player.responseSuccess(protocol.getType());
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.PLAYER_BUILDING_REMOVE,
				Params.valueOf("id", buildingEntity.getId()), Params.valueOf("cfgId", buildingEntity.getBuildingCfgId()));
		return true;
	}

	
	/**
	 * 建筑移动
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BUILDING_MOVE_C_VALUE)
	private boolean onBuildingMove(HawkProtocol protocol) {
		BuildingMoveReq req = protocol.parseProtocol(BuildingMoveReq.getDefaultInstance());
		String buildingId = req.getId();
		int targetIndex = req.getTargetIndex();
		if (HawkOSOperator.isEmptyString(buildingId) || targetIndex <= 0) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			logger.error("buildingMove param error, playerId: {}, buildingId: {}, buildIndex: {}", player.getId(), buildingId, targetIndex);
			return false;
		}
		
		BuildingBaseEntity buildingEntity = player.getData().getBuildingBaseEntity(buildingId);
		if (buildingEntity == null) {
			sendError(protocol.getType(), Status.Error.BUILDING_NOT_EXISIT);
			logger.error("buildingMove entity not exist, playerId: {}, buildingId: {}", player.getId(), buildingId);
			return false;
		}
		
		// 被尤里占领,不能操作
		if (!GsConfig.getInstance().isRobotMode() && player.isBuildingLockByYuri(buildingEntity.getBuildIndex())) {
			sendError(protocol.getType(), Status.Error.BUILDING_YURI_LOCK);
			logger.error("buildingMove failed, building occupied by yuri, playerId: {}, buildingId: {}, buildingEntity: {}", player.getId(), buildingId, buildingEntity);
			return false;
		}
		
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		// 非共享地块的建筑不能移动
		if (buildingCfg == null || !BuildAreaCfg.isShareBlockBuildType(buildingCfg.getBuildType())) {
			sendError(protocol.getType(), Status.Error.MOVE_BUILDING_TYPE_ERROR);
			logger.error("buildingMove entity type error, playerId: {}, buildingId: {}, buildType: {}", player.getId(), buildingId, buildingCfg == null ? 0 : buildingCfg.getBuildType());
			return false;
		}
		
		String targetIndexString = String.valueOf(targetIndex);
		// 移动建筑的地标与目标地块地标相同
		if (buildingEntity.getBuildIndex().equals(targetIndexString)) {
			sendError(protocol.getType(), Status.Error.BUILD_INDEX_NOT_CHANGE);
			logger.error("buildingMove failed, playerId: {}, buildingId: {}, index: {}, target index: {}", player.getId(), buildingId, buildingEntity.getBuildIndex(), targetIndex);
			return false;
		}
		
		String targetIndexBuildingId = ""; 
		Optional<BuildingBaseEntity> optional = player.getData().getBuildingEntitiesIgnoreStatus().stream()
				.filter(e -> BuildAreaCfg.isShareBlockBuildType(e.getType()) && e.getBuildIndex().equals(targetIndexString)).findAny();
		if (optional.isPresent()) {
			BuildingBaseEntity targetIndexBuilding = optional.get();
			targetIndexBuildingId = targetIndexBuilding.getId();
			targetIndexBuilding.setBuildIndex(buildingEntity.getBuildIndex());
		} else {
			
			// 判断地块有没有被解锁出来
			int areaId = BuildAreaCfg.getAreaByBlock(targetIndex);
			Set<Integer> unlockedAreas = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
			if (!unlockedAreas.contains(areaId)) {
				sendError(protocol.getType(), Status.Error.TARGET_BUILD_INDEX_LOCKED);
				logger.error("buildingMove failed, target area not unlocked, playerId: {}, blockId: {}, areaId: {}", player.getId(), targetIndex, areaId);
				return false;
			}
		}
		
		String sourceIndex = buildingEntity.getBuildIndex();
		buildingEntity.setBuildIndex(targetIndexString);
		
		BuildingMoveResp.Builder resp = BuildingMoveResp.newBuilder();
		resp.setChangeBuildId(buildingId);
		resp.setChangeBuildIndex(targetIndex);
		if (!HawkOSOperator.isEmptyString(targetIndexBuildingId)) {
			resp.setBeChangedBuildIndex(Integer.valueOf(sourceIndex));
			resp.setBeChangedBuildId(targetIndexBuildingId);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.BUILDING_MOVE_S_VALUE, resp));
		
		HawkLog.debugPrintln("buildingMove success, playerId: {}, index: {}, buildingId: {}, targetIndex: {}, target buildingId: {}", player.getId(), sourceIndex, buildingEntity.getId(), targetIndex, targetIndexBuildingId);
		
		return true;
	}

	/**
	 * 创建建筑
	 *
	 * @param hpCode
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.BUILDING_CREATE_C_VALUE)
	private boolean onBuildingCreate(HawkProtocol protocol) {
		BuildingCreateReq req = protocol.parseProtocol(BuildingCreateReq.getDefaultInstance());
		if (!BuildingService.getInstance().buildCheck(player, req, protocol.getType())) {
			return false;
		}

		boolean immediate = req.hasImmediately() ? req.getImmediately() : false;
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, req.getBuildCfgId());
		double buildTime = BuildingService.getInstance().getCostTime(player, buildingCfg.getBuildTime(), 1);
		// 检查建筑队列是否被占用
		if (!immediate && !BuildingService.getInstance().checkQueueStatus(player, (long) buildTime, protocol.getType())) {
			return false;
		}

		List<ItemInfo> resCost = BuildingService.getInstance().consume(player, null, buildingCfg, buildTime, immediate, 
				req.hasUseGold() ? req.getUseGold() : false, Action.PLAYER_BUILDING_CREATE, protocol.getType());
		if (resCost == null) {
			return false;
		}

		BuildingBaseEntity buildingEntity = player.getData().createBuildingEntity(buildingCfg, req.getIndex(), false);
		if (immediate) {
			BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
		} else {
			BuildingService.getInstance().pushBuildingRefresh(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_COMMON, HP.code.BUILDING_CREATE_PUSH_VALUE);
			String itemId = buildingEntity.getId();
			QueueService.getInstance().addReusableQueue(player, QueueType.BUILDING_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_CREATE_VALUE, itemId,
					buildingCfg.getBuildType(), buildTime, resCost, GsConst.QueueReusage.BUILDING_OPERATION);
		}

		// 创建成功
		player.responseSuccess(protocol.getType());
		BehaviorLogger.log4Service(player, Source.BUILDING_ADD, Action.PLAYER_BUILDING_CREATE,
				Params.valueOf("id", buildingEntity.getId()),
				Params.valueOf("cfgId", buildingEntity.getBuildingCfgId()),
				Params.valueOf("buildIndex", buildingEntity.getBuildIndex()));
		return true;
	}

	/**
	 * 建筑升级
	 *
	 * @param hpCode
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.BUILDING_UPGRADE_C_VALUE)
	private boolean onBuildingUpgrade(HawkProtocol protocol) {
		BuildingUpgradeReq req = protocol.parseProtocol(BuildingUpgradeReq.getDefaultInstance());
		if (!BuildingService.getInstance().buildUpgradeCheckMultiply(player, req, protocol)) {
			return false;
		}
		int multi = 1;
		//一键升级验证
		if(req.hasMultiply() && req.getMultiply() >=2){
			multi = req.getMultiply();
			int cityLevel = player.getCityLevel();
			BuildingCfg limitCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, 
					ConstProperty.getInstance().getBuildContinuousUpgradeCondition());
			if(Objects.isNull(limitCfg)){
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
				logger.error("building onBuildingUpgradeMultiply failed, limit level is err, playerId: {}", player.getId(),cityLevel);
				return false;
			}
			if(cityLevel < limitCfg.getLevel()){
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
				logger.error("building onBuildingUpgradeMultiply failed, city level is err, playerId: {}", player.getId(),cityLevel);
				return false;
			}
			//一键升级超过上限
			if(multi > ConstProperty.getInstance().getBuildContinuousUpgradeLimit()){
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
				logger.error("building onBuildingUpgradeMultiply failed, params is err, playerId: {}", player.getId(),multi);
				return false;
			}
		}
		
		boolean immediate = req.getImmediately();
		boolean useGold = req.hasUseGold() ? req.getUseGold() : false;
		BuildingBaseEntity buildingEntity = player.getData().getBuildingBaseEntity(req.getId());
		// 被尤里占领,不能操作
		if(!GsConfig.getInstance().isRobotMode() && player.isBuildingLockByYuri(buildingEntity.getType(), buildingEntity.getBuildIndex())){
			sendError(protocol.getType(), Status.Error.BUILDING_YURI_LOCK);
			return false;
		}
		BuildingCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		double buildTime = 0;
		ConsumeItems consume = ConsumeItems.valueOf();
		//计算资源消耗  时间也单算
		for(int level = 1;level<=multi;level++){
			BuildingCfg nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, currCfg.getPostStage());
			double levelBuildTime = BuildingService.getInstance().getCostTime(player, nextLevelCfg.getBuildTime(), nextLevelCfg.getLevel());
			BuildingService.getInstance().addResConsume(player,consume, buildingEntity, nextLevelCfg, immediate,useGold, Action.PLAYER_BUILDING_UPGRADE);
			buildTime += levelBuildTime;
			currCfg = nextLevelCfg;
		}
		//计算时间消耗
		BuildingService.getInstance().addTimeConsume(player, immediate,buildTime, consume, multi);
		// 检查建筑队列是否被占用
		if (!immediate && !BuildingService.getInstance().checkQueueStatus(player, (long) buildTime, protocol.getType())) {
			return false;
		}
		//检查消耗
		if (!consume.checkConsume(player, protocol.getType())) {
			logger.error("consume error, playerId: {}, buildCfgId: {}, immediate: {}, useGold: {}", 
					player.getId(), buildingEntity != null ? buildingEntity.getBuildingCfgId() : currCfg.getId(), immediate, useGold);
			return false;
		}
		AwardItems realCostItems = consume.consumeAndPush(player, Action.PLAYER_BUILDING_UPGRADE);
		List<ItemInfo> resCost  =  realCostItems.getAwardItems();
		if (resCost == null) {
			return false;
		}

		// 建筑升级操作打点
		LogUtil.logBuildLvUpOperation(player, buildingEntity, currCfg.getLevel(), immediate);
		
		if (!immediate) {
			QueueService.getInstance().addReusableQueue(player, QueueType.BUILDING_QUEUE_VALUE, QueueStatus.QUEUE_STATUS_COMMON_VALUE, buildingEntity.getId(),
					currCfg.getBuildType(), buildTime, resCost, GsConst.QueueReusage.BUILDING_OPERATION,multi);
			HawkLog.debugPrintln("upgrade building queue opened, playerId: {}, uuid: {}, cfgId: {}", player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId());
		} else {
			BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY,multi);
		}

		player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
		player.responseSuccess(protocol.getType());
		BehaviorLogger.log4Service(player, Source.BUILDING_UPGRADE, Action.PLAYER_BUILDING_UPGRADE,
				Params.valueOf("id", buildingEntity.getId()),
				Params.valueOf("cfgId", buildingEntity.getBuildingCfgId()));
		MissionManager.getInstance().postMsg(player, new EventBuildUpDoing(buildingEntity.getBuildingCfgId()));
		return true;
	}
	
	
	
	/**
	 * 城墙灭火
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BUILDING_OUTFIRE_C_VALUE)
	private boolean onOutFire(HawkProtocol protocol) {
		long onFireEndTime = player.getData().getPlayerBaseEntity().getOnFireEndTime();
		if (onFireEndTime <= HawkTime.getMillisecond()) {
			sendError(protocol.getType(), Status.Error.CITY_NOT_ON_FIRE);
			logger.error("city not onfire status, playerId: {}, onFireEndTime: {}", player.getId(), HawkTime.formatTime(onFireEndTime));
			return false;
		}

		ConsumeItems consume = ConsumeItems.valueOf();
		// 城墙灭火消耗水晶的配置
		consume.addConsumeInfo(ConstProperty.getInstance().getOutFireCostItems(), false);
		// 检查需要的资源是否足够
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}
		// 消耗水晶
		consume.consumeAndPush(player, Action.PLAYER_OUT_FIRE);
		CityManager.getInstance().outFire(player);
		player.responseSuccess(protocol.getType());

		return true;
	}
	
	@MessageHandler
	private void onGuardOutFire(GuardOutFireMsg outFireMsg) {
		long onFireEndTime = player.getData().getPlayerBaseEntity().getOnFireEndTime();
		if (onFireEndTime <= HawkTime.getMillisecond()) {
			Player.logger.error("playerId:{} already out fire ", player.getId());
			RelationService.getInstance().synGuardUpdate(outFireMsg.getHelpPlayerId(), player.getId());
			
			return;
		}
		
		CityManager.getInstance().outFire(player);
		RelationService.getInstance().synGuardUpdate(outFireMsg.getHelpPlayerId(), player.getId());
		
		Player sourcePlayer = GlobalData.getInstance().makesurePlayer(outFireMsg.getHelpPlayerId());
		MailParames.Builder builder = MailParames.newBuilder()
				.setPlayerId(player.getId()).setMailId(MailId.GUARD_PUT_OUT_FIRE);
		builder.addContents(sourcePlayer.getName());
		builder.addContents(sourcePlayer.getName());
		SystemMailService.getInstance().sendMail(builder.build());
	}
	
	
	

	/**
	 * 修复城防
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BUILDING_REPAIR_C_VALUE)
	private boolean onCityDefRepair(HawkProtocol protocol) {
		int cityDefVal = player.getData().getPlayerBaseEntity().getCityDefVal();
		int cityDefMax = player.getData().getRealMaxCityDef(); // 城防值上限
		if (cityDefVal >= cityDefMax) {
			sendError(protocol.getType(), Status.Error.CITY_DEF_EXCEED_LIMIT);
			logger.error("cityDef value touch uplimit, playerId: {}, cityDefVal: {}, cityDefMax: {}", player.getId(), cityDefVal, cityDefMax);
			return false;
		}

		long cityDefNextRepairTime = player.getData().getPlayerBaseEntity().getCityDefNextRepairTime();
		if (cityDefNextRepairTime == 0 || cityDefNextRepairTime > HawkTime.getMillisecond()) {
			sendError(protocol.getType(), Status.Error.CITY_REPAIR_EARLY);
			logger.error("city wall repair time error, playerId: {}, next time: {}", player.getId(), cityDefNextRepairTime);
			return false;
		}
		
		logger.debug("cityDef repair, playerId: {}, cityDefVal: {}, cityDefMax: {}", player.getId(), cityDefVal, cityDefMax);
		
		// 修复城防, 不需要消耗资源
		CityManager.getInstance().repairCity(player);
		player.refreshPowerElectric(PowerChangeReason.CITY_REPAIR);
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 * 城防信息请求
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CITYDEF_REQ_C_VALUE)
	private boolean onCityDefReq(HawkProtocol protocol) {
		BuildingService.getInstance().updateCityDef(player);
		CityManager.getInstance().cityDefCalculate(player, true);
		return true;
	}
	
	/**
	 * 超时空急救站冷却恢复结束
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	private boolean onBuildingRecoverFinish(BuildingRecoverFinishMsg msg) {
		return true;
	}
	
	/**
	 * 建筑队列完成
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	private boolean onBuildingQueueFinish(BuildingQueueFinishMsg msg) {
		String itemId = msg.getItemId();
		final int queueStatus = msg.getStatus(); // 建造，升级，拆除
		if (HawkOSOperator.isEmptyString(itemId)) {
			logger.error("building queue finish handle failed, param buildingId is null, playerId: {}", player.getId());
			return false;
		}

		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityIgnoreStatus(itemId);
		if (buildingEntity == null) {
			logger.error("building queue finish handle failed, buildingEntity is null, playerId: {}, buildingId: {}", player.getId(), itemId);
			return false;
		}

		HawkLog.debugPrintln("building queue finish, playerId: {}, queueStatus: {}", player.getId(), queueStatus);
		// 建造队列
		if (queueStatus == QueueStatus.QUEUE_STATUS_CREATE_VALUE) {
			BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_COMMON, HP.code.BUILDING_UPDATE_PUSH_VALUE);
		} else if (queueStatus == QueueStatus.QUEUE_STATUS_REMOVE_VALUE) {
			// 拆除队列
			BuildingService.getInstance().removeBuilding(buildingEntity, player);
		} else {
			// 升级队列
			BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_COMMON,msg.getMulti());
		}

		return true;
	}

	/**
	 * 建筑队列取消
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	private boolean onBuildingQueueCancel(BuildingQueueCancelMsg msg) {
		String cancelBackRes = msg.getCancelBackRes();
		if (HawkOSOperator.isEmptyString(cancelBackRes)) {
			return true;
		}

		final String itemId = msg.getItemId();
		final int queueStatus = msg.getStatus(); // 建造，升级，改建，拆除
		if (HawkOSOperator.isEmptyString(itemId)) {
			logger.error("building queue cancel handle failed, param buildingId is null, playerId: {}", player.getId());
			return false;
		}

		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityIgnoreStatus(itemId);
		if (buildingEntity == null) {
			logger.error("building queue cancel handle failed, playerId: {}, buildingId: {}, buildingEntity: {}", player.getId(), itemId, buildingEntity);
			return false;
		}

		AwardItems awardItem = AwardItems.valueOf(cancelBackRes);
		// 建造队列
		if (queueStatus == QueueStatus.QUEUE_STATUS_CREATE_VALUE) {
			awardItem.scale(ConstProperty.getInstance().getBuildCancelReclaimRate() / 10000d);
			awardItem.rewardTakeAffectAndPush(player, Action.PLAYER_BUILDING_CREATE_CANCEL);
			// 删除建筑
			BuildingRemovePushPB.Builder builder = BuildingRemovePushPB.newBuilder();
			builder.setBuildingUuid(buildingEntity.getId());
			getPlayerData().removeBuilding(buildingEntity);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.BUILDING_REMOVE_PUSH, builder));

		} else {
			awardItem.scale(ConstProperty.getInstance().getBuildCancelReclaimRate() / 10000d);
			awardItem.rewardTakeAffectAndPush(player, Action.PLAYER_BUILDING_UPGRADE_CANCEL);
		}

		return true;
	}
}
