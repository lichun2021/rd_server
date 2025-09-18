package com.hawk.game.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.hawk.activity.type.impl.backToNewFly.cfg.BackToNewFlyBuildCfg;
import com.hawk.activity.type.impl.backToNewFly.cfg.BackToNewFlyKvCfg;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BuildingCreateEvent;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.BuildingLevelUpSpreadEvent;
import com.hawk.activity.event.impl.CityResourceCollectEvent;
import com.hawk.activity.event.impl.ResourceRateChangeEvent;
import com.hawk.activity.type.impl.baseBuild.cfg.BaseBuildCfg;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.BuildAreaCfg;
import com.hawk.game.config.BuildLevelUpAwardCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.MissionCfg;
import com.hawk.game.config.StoryMissionCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.MissionEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.StoryMissionEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.AddVipExpInvoker;
import com.hawk.game.invoker.QuestionnaireCheckInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.PlayerHeroModule;
import com.hawk.game.msg.BuildingLevelUpMsg;
import com.hawk.game.msg.BuildingRemoveMsg;
import com.hawk.game.msg.SuperSoldierTriggeTaskMsg;
import com.hawk.game.msg.TravelShopBuildingFinishMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Building.BuildingCreateReq;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Building.BuildingRemovePushPB;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.Building.BuildingUpdatePush;
import com.hawk.game.protocol.Building.BuildingUpgradeReq;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.LimitType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Queue.QueuePBSimple;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SuperSoldier.SupersoldierTaskType;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.guildtask.event.BuildingLvlUpTaskEvent;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventBuildingCreate;
import com.hawk.game.service.mssion.event.EventBuildingUpgrade;
import com.hawk.game.service.mssion.event.EventResourceProductionRate;
import com.hawk.game.service.mssion.event.EventUnlockGround;
import com.hawk.game.strengthenguide.StrengthenGuideManager;
import com.hawk.game.strengthenguide.msg.SGBuildingLevelUpMsg;
import com.hawk.game.strengthenguide.msg.SGBuildingRemoveMsg;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.BuildingConditionType;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.game.util.GsConst.QuestionnaireConst;
import com.hawk.game.util.GsConst.QueueReusage;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.Source;

/**
 * 建筑服务类
 * 
 * @author
 *
 */
public class BuildingService {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	private static BuildingService instance = new BuildingService();

	private BuildingService() {

	}

	public static BuildingService getInstance() {
		return instance;
	}

	/**
	 * 新建建筑的队列完成后，处理造建筑逻辑
	 * 
	 * @param msg
	 * @return
	 */
	public boolean createBuildingFinish(Player player, BuildingBaseEntity buildingEntity, BuildingUpdateOperation operation, int hpCode) {
		player.getData().refreshNewBuilding(buildingEntity);
		// 推送建筑信息
		buildingEntity.setLastUpgradeTime(HawkApp.getInstance().getCurrentTime());
		pushBuildingRefresh(player, buildingEntity, operation, hpCode);
		buildingCreateComplete(player, buildingEntity);
		LogUtil.logBuildFlow(player, buildingEntity, 0, 1);
		return true;
	}

	/**
	 * 计算单个建筑提供的资源最大数量加成
	 * 
	 * @param entity
	 * @param isAdd
	 *            true增加， false减少
	 */
	public void refreshResStoreAndOutput(PlayerData playerData, BuildingBaseEntity entity, boolean isAdd) {
		BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId());
		if (cfg == null || !cfg.isResBuilding()) {
			return;
		}
		
		long add = cfg.getResLimit();
		long perHour = cfg.getResPerHour();
		if (!isAdd) {
			add = -add;
			perHour = -perHour;
		}
		
		switch (cfg.getBuildType()) {
		case BuildingType.ORE_REFINING_PLANT_VALUE:
			playerData.addOreMaxStore(add);
			playerData.addOreOutputPerHour(perHour);
			break;
		case BuildingType.OIL_WELL_VALUE:
			playerData.addOilMaxStore(add);
			playerData.addOilOutputPerHour(perHour);
			break;
		case BuildingType.STEEL_PLANT_VALUE:
			playerData.addSteelMaxStore(add);
			playerData.addSteelOutputPerHour(perHour);
			break;
		case BuildingType.RARE_EARTH_SMELTER_VALUE:
			playerData.addRareMaxStore(add);
			playerData.addRareOutputPerHour(perHour);
			break;
		default:
			break;
		}
	}

	/**
	 * 资源建筑通知任务模块刷新资源产出量
	 * 
	 * @param cfg
	 * @param add
	 *            为true表示升级或添加资源建筑增加资源产出，false表示拆除资源建筑减少资源产出
	 * @param isRebuild
	 *            是否为改建动作
	 */
	public void refreshResourceRate(Player player, final BuildingCfg cfg, boolean add, boolean isRebuild) {
		if (cfg == null || !cfg.isResBuilding()) {
			return;
		}

		int addRate = cfg.getResPerHour();
		if (add) {
			BuildingCfg prevCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, cfg.getFrontStage());
			if (prevCfg != null && !isRebuild) {
				addRate -= prevCfg.getResPerHour();
			}
		} else {
			addRate = -addRate;
		}

		int resourceType = 0;
		switch (cfg.getBuildType()) {
		case BuildingType.ORE_REFINING_PLANT_VALUE:
			resourceType = PlayerAttr.GOLDORE_UNSAFE_VALUE;
			break;
		case BuildingType.OIL_WELL_VALUE:
			resourceType = PlayerAttr.OIL_UNSAFE_VALUE;
			break;
		case BuildingType.STEEL_PLANT_VALUE:
			resourceType = PlayerAttr.STEEL_UNSAFE_VALUE;
			break;
		case BuildingType.RARE_EARTH_SMELTER_VALUE:
			resourceType = PlayerAttr.TOMBARTHITE_UNSAFE_VALUE;
			break;
		default:
			break;
		}

		if (resourceType > 0) {
			// 刷新前资源产出率
			int currRate = (int) player.getData().getResourceOutputRate(resourceType);
			MissionManager.getInstance().postMsg(player, new EventResourceProductionRate(resourceType, currRate - addRate, currRate));
			ActivityManager.getInstance().postEvent(new ResourceRateChangeEvent(player.getId(), cfg.getBuildType(), resourceType, addRate));
		}
	}

	/**
	 * 创建建筑条件判断
	 * 
	 * @param req
	 * @param hpCode
	 * @return
	 */
	public boolean buildCheck(Player player, BuildingCreateReq req, int hpCode) {
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, req.getBuildCfgId());
		// 该建筑配置是否存在
		if (buildingCfg == null) {
			player.sendError(hpCode, Status.SysError.CONFIG_ERROR_VALUE, 0);
			logger.error("building create failed, config not exist, playerId: {}, cfgId: {}", player.getId(), req.getBuildCfgId());
			return false;
		}

		// 共享地块建筑逻辑判断
		if (!shareBlockBuildCheck(player, buildingCfg, req.getIndex(), hpCode)) {
			return false;
		}

		// 判断前置建筑条件，即判断建筑是否已解锁
		if (!checkFrontCondition(player, buildingCfg.getFrontBuildIds(), buildingCfg.getFrontConditionParamMap(), hpCode)) {
			return false;
		}

		// 检查此建筑的限制类型是否存在
		LimitType limitType = LimitType.valueOf(buildingCfg.getLimitType());
		if (limitType == null) {
			logger.error("building limit type not exist, playerId: {}, limitType: {}", player.getId(), buildingCfg.getLimitType());
			return false;
		}
		List<BuildingBaseEntity> buildings = player.getData().getBuildingListByLimitTypeIgnoreStatus(LimitType.valueOf(buildingCfg.getLimitType()));
		int buildingLimit = player.getData().getBuildingNumLimit(buildingCfg.getLimitType());
		// 检查同类型建筑的数目是否已达上限
		if (buildings.size() >= buildingLimit) {
			logger.error("building create failed, building count: {}, building limit: {}, playerId: {}, cfgId: {}",
					buildings.size(), buildingLimit, player.getId(), req.getBuildCfgId());
			player.sendError(hpCode, Status.Error.BUILDING_NUM_MAX, 0);
			return false;
		}

		return true;
	}
	
	
	/**
	 * 共享地块上建造条件判断
	 * 
	 * @param player
	 * @param buildingCfg
	 * @param buildIndex
	 * @param hpCode
	 * @return
	 */
	private boolean shareBlockBuildCheck(Player player, BuildingCfg buildingCfg, String buildIndex, int hpCode) {
		if (!BuildAreaCfg.isShareBlockBuildType(buildingCfg.getBuildType())) {
			List<BuildingBaseEntity> buildingList = player.getData().getBuildingListByType(BuildingType.valueOf(buildingCfg.getBuildType()));
			for (BuildingBaseEntity building : buildingList) {
				if (building.getBuildIndex().equals(buildIndex)) {
					player.sendError(hpCode, Status.Error.BUILDING_COORDINATE_USED_VALUE, 0);
					logger.error("building block used error, playerId: {}, blockId: {}, buildingType: {}", player.getId(), buildIndex, buildingCfg.getBuildType());
					return false;
				}
			}
			return true;
		}
		
		int areaId = BuildAreaCfg.getAreaByBlock(Integer.valueOf(buildIndex));
		Set<Integer> unlockedAreas = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
		if (!unlockedAreas.contains(areaId)) {
			player.sendError(hpCode, Status.Error.BUILDING_AREA_NOT_UNLOCKED, 0);
			logger.error("building create failed, area not unlocked, playerId: {}, blockId: {}, areaId: {}", player.getId(), buildIndex, areaId);
			return false;
		}
		
		// 地块被尤里占领
		if (!GsConfig.getInstance().isRobotMode() && player.isBuildingLockByYuri(buildIndex)) {
			player.sendError(hpCode, Status.Error.BUILDING_AREA_NOT_UNLOCKED, 0);
			logger.error("building create failed, area occupied by yuri, playerId: {}, areaId: {}", player.getId(), areaId);
			return false;
		}
		
		// 地块与建筑类型不配
		BuildAreaCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildAreaCfg.class, areaId);
		List<Integer> buildTypes = cfg.getBuildTypeList();
		if (!buildTypes.contains(buildingCfg.getBuildType())) {
			player.sendError(hpCode, Status.Error.BUILDING_POSITION_ERROR_VALUE, 0);
			logger.error("building create failed, block not match buildType, playerId: {}, blockId: {}, buildType: {}", player.getId(), buildIndex, buildingCfg.getBuildType());
			return false;
		}
		
		// 获取同类共享地块的建筑, 非共享地块的建筑以建筑本身的建筑类型为准
		Optional<BuildingBaseEntity> optional = player.getData().getBuildingEntitiesIgnoreStatus().stream()
				.filter(e -> BuildAreaCfg.isShareBlockBuildType(e.getType()) && buildIndex.equals(e.getBuildIndex())).findAny();
		// 地块已被占用
		if (optional.isPresent()) {
			player.sendError(hpCode, Status.Error.BUILDING_COORDINATE_USED_VALUE, 0);
			logger.error("building block used error, playerId: {}, blocked: {}", player.getId(), buildIndex);
			return false;
		}
		
		return true;
	}

	/**
	 * 创建建筑处理
	 * 
	 * @param buildingEntity
	 * @param buildingCfg
	 */
	private void buildingCreateComplete(Player player, BuildingBaseEntity buildingEntity) {
		int buildingCfgId = buildingEntity.getBuildingCfgId();
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingCfgId);
		if (buildingCfg == null) {
			return;
		}
		// 任务事件
		MissionManager.getInstance().postMsg(player, new EventBuildingCreate(buildingCfgId));
		// 活动事件
		List<BuildingBaseEntity> buildings = player.getData().getBuildingListByType(BuildingType.valueOf(buildingCfg.getBuildType()));
		ActivityManager.getInstance().postEvent(new BuildingCreateEvent(player.getId(), buildingCfg.getBuildType(), buildings.size()));
		ActivityManager.getInstance().postEvent(new BuildingLevelUpEvent(player.getId(), buildingCfg.getBuildType(), buildingCfg.getLevel(), 0, false, buildingCfg.getBattlePoint()));
		// 联盟任务-建筑升级
		GuildService.getInstance().postGuildTaskMsg(new BuildingLvlUpTaskEvent(player.getGuildId()));
		//解锁旅行商人
		HawkApp.getInstance().postMsg(player.getXid(), TravelShopBuildingFinishMsg.valueOf(buildingCfg.getBuildType()));

		// 资源建筑计算资源产出和资源储量
		if (buildingCfg.isResBuilding()) {
			refreshResStoreAndOutput(player.getData(), buildingEntity, true);
			refreshResourceRate(player, buildingCfg, true, false);
		}
		
		addExp(player, buildingCfg, Action.PLAYER_BUILDING_CREATE);
		player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
		specificBuildingCreateHandler(player, buildingEntity, buildingCfg);
		refreshBuildEffect(player, buildingCfg, true);
		
		if (buildingEntity.getType() == ConstProperty.getInstance().getAirdropUnlockBuildType()) {
			try {
				WharfService.getInstance().syncWharfInfo(player);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		if (buildingEntity.getType() == BuildingType.WISHING_WELL_VALUE) {
			WishingService.getInstance().syncWishingInfo(player);
		}
		
		if(player.getGuildId() != null && buildingEntity.getType() == BuildingType.EMBASSY_VALUE){
			GuildService.getInstance().refreshGuildHelpNum(player.getGuildId(), null, null);
		}
		
		HawkLog.debugPrintln("building create complete, playerId: {}, buildingId: {}, cfgId: {}, type: {}", 
				player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId(), buildingEntity.getType());
	}

	/**
	 * 特殊建筑建造完成关联逻辑处理
	 * 
	 * @param buildingEntity
	 * @param buildingCfg
	 */
	private void specificBuildingCreateHandler(Player player, BuildingBaseEntity buildingEntity, BuildingCfg buildingCfg) {
		int buildType = buildingCfg.getBuildType();
		switch (buildType) {
		case BuildingType.TRADE_CENTRE_VALUE:
		case BuildingType.EMBASSY_VALUE:
			WorldPlayerService.getInstance().updatePlayerPointInfo(player.getId(), player.getName(), player.getCityLv(), player.getIcon(), player.getData().getPersonalProtectVals());
			break;
		case BuildingType.HOSPITAL_STATION_VALUE:
			if (ArmyService.getInstance().getCureFinishCount(player) > 0) {
				player.getPush().pushBuildingStatus(buildingEntity, Const.BuildingStatus.CURE_FINISH_HARVEST);
				break;
			}
			if (ArmyService.getInstance().getWoundedCount(player) > 0) {
				player.getPush().pushBuildingStatus(buildingEntity, Const.BuildingStatus.SOLDIER_WOUNDED);
				break;
			}
			break;
		case BuildingType.PLANT_HOSPITAL_VALUE:
			if (ArmyService.getInstance().getPlantCureFinishCount(player) > 0) {
				player.getPush().pushBuildingStatus(buildingEntity, Const.BuildingStatus.PLANT_CURE_FINISH_HARVEST);
				break;
			}
			if (ArmyService.getInstance().getPlantWoundedCount(player) > 0) {
				player.getPush().pushBuildingStatus(buildingEntity, Const.BuildingStatus.PLANT_SOLDIER_WOUNDED);
				break;
			}
			break;
		case BuildingType.BARRACKS_VALUE:
		case BuildingType.WAR_FACTORY_VALUE:
		case BuildingType.AIR_FORCE_COMMAND_VALUE:
		case BuildingType.REMOTE_FIRE_FACTORY_VALUE:
			for (BuildingBaseEntity entity : player.getData().getBuildingListByType(BuildingType.valueOf(buildType))) {
				if (entity.getStatus() == Const.BuildingStatus.SOILDER_HARVEST_VALUE) {
					player.getPush().pushBuildingStatus(entity, Const.BuildingStatus.SOILDER_HARVEST);
					break;
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 建筑升级条件判断
	 * 
	 * @param req
	 * @param buildingEntity
	 * @return
	 */
	public boolean buildUpgradeCheck(Player player, BuildingUpgradeReq req, HawkProtocol protocol) {
		// 参数检验
		if (!req.hasId() || !req.hasBuildCfgId()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID, 0);
			logger.error("building upgrade failed, params is null, playerId: {}", player.getId());
			return false;
		}

		BuildingBaseEntity buildingEntity = player.getData().getBuildingBaseEntity(req.getId());
		// 玩家建筑列表是否已经有该建筑
		if (buildingEntity == null) {
			player.sendError(protocol.getType(), Status.Error.BUILDING_NOT_EXISIT, 0);
			logger.error("building upgrade failed, building not exist, playerId: {}, buildingId: {}", player.getId(), req.getId());
			return false;
		}

		// 建筑的配置id已发生了变化，用来堵截玩家网络不好，连点造成多次升级的问题
		if (req.getBuildCfgId() != buildingEntity.getBuildingCfgId()) {
			player.sendError(protocol.getType(), Status.Error.BUILDING_DATA_ALREADY_REFRESH, 0);
			logger.error("building upgrade failed, playerId: {}, build cfgId: {}, req cfgId: {}", player.getId(), buildingEntity.getBuildingCfgId(), req.getBuildCfgId());
			return false;
		}

		// 该建筑配置是否存在
		BuildingCfg bulidCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		if (bulidCfg == null) {
			player.sendError(protocol.getType(), Status.Error.BUILDINGF_CONFIG_ID_ERROR, 0);
			logger.error("building upgrade config error, buildCfg is null, playerId: {}, cfgId: {}", player.getId() , buildingEntity.getBuildingCfgId());
			return false;
		}
		BuildingCfg nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, bulidCfg.getPostStage());
		if (nextLevelCfg == null) {
			player.sendError(protocol.getType(), Status.Error.BUILDINGF_CONFIG_ID_ERROR, 0);
			logger.error("building upgrade config error, nextLevelCfg is null, playerId: {}, cfgId: {}", player.getId(), buildingEntity.getBuildingCfgId());
			return false;
		}
		
		int buildControlLevel = ConstProperty.getInstance().getBuildControlLevel();
		if (buildControlLevel > 0) {
			boolean equal = nextLevelCfg.getLevel() == buildControlLevel && (nextLevelCfg.getHonor() > 0 || nextLevelCfg.getProgress() > 0);
			if (equal || nextLevelCfg.getLevel() > buildControlLevel) {
				logger.error("building upgrade control limit, playerId: {}, cfgId: {}", player.getId(), buildingEntity.getBuildingCfgId());
				return false;
			}
		}

		// 检查该建筑的前置建筑是否存在
		if (!checkFrontCondition(player, nextLevelCfg.getFrontBuildIds(), nextLevelCfg.getFrontConditionParamMap(), protocol.getType())) {
			return false;
		}

		return checkBuildingStatus(player, buildingEntity, protocol.getType());
	}
	
	
	/**
	 * 建筑升级条件判断
	 * 
	 * @param req
	 * @param buildingEntity
	 * @return
	 */
	public boolean buildUpgradeCheckMultiply(Player player, BuildingUpgradeReq req, HawkProtocol protocol) {
		// 参数检验
		if (!req.hasId() || !req.hasBuildCfgId()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID, 0);
			logger.error("building upgrade failed, params is null, playerId: {}", player.getId());
			return false;
		}

		BuildingBaseEntity buildingEntity = player.getData().getBuildingBaseEntity(req.getId());
		// 玩家建筑列表是否已经有该建筑
		if (buildingEntity == null) {
			player.sendError(protocol.getType(), Status.Error.BUILDING_NOT_EXISIT, 0);
			logger.error("building upgrade failed, building not exist, playerId: {}, buildingId: {}", player.getId(), req.getId());
			return false;
		}

		// 建筑的配置id已发生了变化，用来堵截玩家网络不好，连点造成多次升级的问题
		if (req.getBuildCfgId() != buildingEntity.getBuildingCfgId()) {
			player.sendError(protocol.getType(), Status.Error.BUILDING_DATA_ALREADY_REFRESH, 0);
			logger.error("building upgrade failed, playerId: {}, build cfgId: {}, req cfgId: {}", player.getId(), buildingEntity.getBuildingCfgId(), req.getBuildCfgId());
			return false;
		}

		// 该建筑配置是否存在
		BuildingCfg bulidCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		if (bulidCfg == null) {
			player.sendError(protocol.getType(), Status.Error.BUILDINGF_CONFIG_ID_ERROR, 0);
			logger.error("building upgrade config error, buildCfg is null, playerId: {}, cfgId: {}", player.getId() , buildingEntity.getBuildingCfgId());
			return false;
		}
		//一键升X级
		int multi = 1;
		if(req.hasMultiply() && req.getMultiply() >=2){
			multi = req.getMultiply();
		}
		//检查一下
		for(int i=1;i<=multi;i++){
			BuildingCfg nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, bulidCfg.getPostStage());
			if (nextLevelCfg == null) {
				player.sendError(protocol.getType(), Status.Error.BUILDINGF_CONFIG_ID_ERROR, 0);
				logger.error("building upgrade config error, nextLevelCfg is null, playerId: {}, cfgId: {}", player.getId(), buildingEntity.getBuildingCfgId());
				return false;
			}
			
			int buildControlLevel = ConstProperty.getInstance().getBuildControlLevel();
			if (buildControlLevel > 0) {
				boolean equal = nextLevelCfg.getLevel() == buildControlLevel && (nextLevelCfg.getHonor() > 0 || nextLevelCfg.getProgress() > 0);
				if (equal || nextLevelCfg.getLevel() > buildControlLevel) {
					logger.error("building upgrade control limit, playerId: {}, cfgId: {}", player.getId(), buildingEntity.getBuildingCfgId());
					return false;
				}
			}
			int[] frontBuildIds = nextLevelCfg.getFrontBuildIds();
			Map<Integer, Integer> frontConditionParams = nextLevelCfg.getFrontConditionParamMap();
			if(i>=2){
				//如果连续升级 需要把当前建筑升第2级时建筑 自身限制拿出去
				List<Integer> outList = new ArrayList<>();
				outList.add(bulidCfg.getId());
				frontBuildIds = nextLevelCfg.getFrontBuildIdsWithout(outList);
			}
			// 检查该建筑的前置建筑是否存在
			if (!checkFrontCondition(player, frontBuildIds, frontConditionParams, protocol.getType())) {
				return false;
			}
			bulidCfg = nextLevelCfg;
		}
		return checkBuildingStatus(player, buildingEntity, protocol.getType());
		
	}
	
	
	/**
	 * 判断建筑的时间队列状态
	 * 
	 * @param buildingEntity
	 * @return
	 */
	public boolean checkBuildingStatus(Player player, BuildingBaseEntity buildingEntity, int hpCode) {
		QueueEntity queueEntity  = player.getData().getQueueByBuildingType(buildingEntity.getType());
		if (queueEntity == null) {
			return true;
		}
		
		logger.error("check building status, playerId: {}, buildingId: {}, buildCfgId: {}, building status : {}, queueType: {}, protocol: {}", 
				player.getId(), buildingEntity.getId(), buildingEntity.getBuildingCfgId(), queueEntity.getStatus(), queueEntity.getQueueType(), hpCode);
		
		switch (queueEntity.getQueueType()) {
		case QueueType.SOILDER_QUEUE_VALUE: 
		case QueueType.SOLDIER_ADVANCE_QUEUE_VALUE: {
			player.sendError(hpCode, Status.Error.BUILDING_STATUS_TRAINING_VALUE, 0);
			return false;
		}
		
		case QueueType.TRAP_QUEUE_VALUE: {
			player.sendError(hpCode, Status.Error.BUILDING_STATUS_MAKING_TRAP_VALUE, 0);
			return false;
		}
		
		case QueueType.CURE_QUEUE_VALUE: {
			player.sendError(hpCode, Status.Error.BUILDING_STATUS_CURE_VALUE, 0);  // TODO 错误码对应文案需要修改
			return false;
		}
			
		case QueueType.SCIENCE_QUEUE_VALUE: {
			player.sendError(hpCode, Status.Error.TECHNOLOGY_IS_RESEARCHING_VALUE, 0);
			return false;
		}
		
		case QueueType.BUILDING_QUEUE_VALUE: {
			if (!queueEntity.getItemId().equals(buildingEntity.getId())) {
				return true;
			}
			
			switch (queueEntity.getStatus()) {
			case QueueStatus.QUEUE_STATUS_CREATE_VALUE:
				// 建造中。。。
				player.sendError(hpCode, Status.Error.BUILDING_STATUS_CREATE, 0);
				return false;
			case QueueStatus.QUEUE_STATUS_COMMON_VALUE:
				// 升级中。。。
				player.sendError(hpCode, Status.Error.BUILDING_STATUS_UPGRADE, 0);
				return false;
			case QueueStatus.QUEUE_STATUS_REMOVE_VALUE:
				// 拆除中。。。
				player.sendError(hpCode, Status.Error.BUILDING_STATUS_REMOVE, 0);
				return false;
			default:
				return true;
			}
		}
		
		default:
			return true;
		}
		
	}

	/**
	 * 建筑升级,立即完成和队列结束时调用
	 * 
	 * @param buildingEntity
	 * @return
	 */
	public void buildingUpgrade(Player player, BuildingBaseEntity buildingEntity, BuildingUpdateOperation operation) {
		BuildingCfg oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		final BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
		if (buildingCfg == null) {
			HawkLog.warnPrintln("building upgrade failed, playerId: {}, build cfgId: {}", player.getId(), buildingEntity.getBuildingCfgId());
		}
		
		long now = HawkTime.getMillisecond();
		// 资源建筑.升级前先收取资源
		if (buildingCfg.isResBuilding()) {
			long timeLong = now - buildingEntity.getLastResCollectTime();
			AwardItems award = AwardItems.valueOf();
			player.collectResource(buildingEntity.getId(), buildingEntity.getBuildingCfgId(), timeLong, award, false);
			if (award.hasAwardItem()) {
				award.rewardTakeAffectAndPush(player, Action.BUILDING_COLLECT_RES);
			}
			
			//抛给活动处理
			CityResourceCollectEvent event = new CityResourceCollectEvent(player.getId());
			event.getCollectTime().add((int)(timeLong / 1000));
			ActivityManager.getInstance().postEvent(event);
			// 升级前扣除资源最大储量
			refreshResStoreAndOutput(player.getData(), buildingEntity, false);
		}
		// 升级建筑
		upgradeBuilding(player, buildingEntity, buildingCfg);
		buildingEntity.setLastUpgradeTime(now);
		// 刷新建筑
		pushBuildingRefresh(player, buildingEntity, operation, HP.code.BUILDING_UPDATE_PUSH_VALUE);
		// 添加升级前的资源最大值
		if (buildingCfg.isResBuilding()) {
			refreshResStoreAndOutput(player.getData(), buildingEntity, true);
			refreshResourceRate(player, buildingCfg, true, false);
		}

		// 大本升级处理
		if (buildingEntity.getType() == Const.BuildingType.CONSTRUCTION_FACTORY_VALUE) {
			onMainBuildingUpgrade(player, oldBuildCfg, buildingCfg);
			GameUtil.scoreBatch(player,ScoreType.CITY_LEVEL, buildingCfg.getLevel());
		}

		addExp(player, buildingCfg, Action.PLAYER_BUILDING_UPGRADE);
		player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
		refreshBuildEffect(player, buildingCfg, true);
		// 任务事件
		MissionManager.getInstance().postMsg(player, new EventBuildingUpgrade(buildingCfg.getId(), oldBuildCfg.getLevel(), buildingCfg.getLevel()));
		// 联盟任务-建筑升级
		GuildService.getInstance().postGuildTaskMsg(new BuildingLvlUpTaskEvent(player.getGuildId()));
		
		PlayerHeroModule heroModule = player.getModule(GsConst.ModuleType.HERO);
		heroModule.checkHeroArchiveOpenAward();
	}
	
	public void buildingUpgrade(Player player, BuildingBaseEntity buildingEntity, BuildingUpdateOperation operation,int multi) {
		for(int i=1;i<= multi;i++){
			this.buildingUpgrade(player, buildingEntity, operation);
		}
	}
	/**
	 * 大本升级处理
	 * 
	 * @param buildingCfg
	 */
	public void onMainBuildingUpgrade(Player player, BuildingCfg oldBuildCfg, BuildingCfg buildingCfg) {
		player.setCityLevel(buildingCfg.getLevel());
		try {
			GlobalData.getInstance().notifyMainBuildingLevelup(oldBuildCfg.getLevel(), buildingCfg.getLevel());
			if (buildingCfg.getWallFireSpeed() != oldBuildCfg.getWallFireSpeed() 
					|| buildingCfg.getWallFireSpeedOnBlackLand() != oldBuildCfg.getWallFireSpeedOnBlackLand()) {
				CityManager.getInstance().cityWallFireSpeedChange(player);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 记录打本升级时间
		player.getEntity().addFactoryLevelUpTime(buildingCfg.getLevel(), HawkApp.getInstance().getCurrentTime());

		if (buildingCfg.getLevel() == WorldMapConstProperty.getInstance().getStepCityLevel1()) {
			StatusDataEntity entity = player.getData().getStatusById(Const.EffType.CITY_SHIELD_VALUE);
			if (entity.getVal() == GsConst.ProtectState.NEW_PLAYER) {
				player.removeCityShield();
			}

			int cnt = player.getData().getItemNumByItemId(Const.ItemId.ITEM_SELECT_MOVE_CITY_NEW_VALUE);
			if (cnt > 0) {
				ConsumeItems consumeItems = ConsumeItems.valueOf();
				consumeItems.addItemConsume(Const.ItemId.ITEM_SELECT_MOVE_CITY_NEW_VALUE, cnt);
				if (consumeItems.checkConsume(player)) {
					consumeItems.consumeAndPush(player, Action.NEWLY_LEAVE);
				}
			}
		}
		
		if (GsConfig.getInstance().isRobotMode()) {
			int vipExp = GameConstCfg.getInstance().getVipExpByCityLevel(buildingCfg.getLevel());
			if (vipExp > 0) {
				player.dealMsg(MsgId.ADD_VIP_EXP, new AddVipExpInvoker(player, vipExp));
			}
		} 

		// 如果是大本等级提升, 更新到自己的城点信息中去
		WorldPlayerService.getInstance().updatePlayerPointInfo(player.getId(), player.getName(), player.getCityLevel(), player.getIcon(), player.getData().getPersonalProtectVals());
		
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
		
		updateAccountRoleInfo(player, buildingCfg.getLevel());
		
		// 更新大本等级达成时间
		if (buildingCfg.getLevel() >= 40) {
			GlobalData.getInstance().updateCityRankTime(player.getId());
		}
	}
	
	/**
	 * 更新账号角色信息
	 * 
	 * @param player
	 * @param cityLevel
	 */
	public void updateAccountRoleInfo(Player player, int cityLevel) {
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(player.getId());
				if (accountRoleInfo != null && cityLevel > 1) {
					accountRoleInfo.setCityLevel(cityLevel);
					RedisProxy.getInstance().addAccountRole(accountRoleInfo);
					RelationService.getInstance().notifyInvitorCityLevelUp(player, cityLevel);
				}
				
				return null;
			}
		}, threadIdx);
	}
	
	/**
	 * 建筑改建、建造、升级消耗
	 * 
	 * @param buildingEntity
	 * @param newBuildCfg
	 * @param costTime
	 * @param immediate
	 * @param action
	 * @param hpCode
	 * @return
	 */
	public List<ItemInfo> consume(Player player, BuildingBaseEntity buildingEntity, BuildingCfg newBuildCfg,
			double costTime, boolean immediate, boolean useGold, Action action, int hpCode) {
		ConsumeItems consume = ConsumeItems.valueOf();
		//资源消耗
		this.addResConsume(player, consume, buildingEntity, newBuildCfg, immediate, useGold, action);
		//时间消耗
		this.addTimeConsume(player, immediate,costTime, consume, 1);
		if (!consume.checkConsume(player, hpCode)) {
			logger.error("consume error, playerId: {}, buildCfgId: {}, immediate: {}, useGold: {}", 
					player.getId(), buildingEntity != null ? buildingEntity.getBuildingCfgId() : newBuildCfg.getId(), immediate, useGold);
			return null;
		}

		AwardItems realCostItems = consume.consumeAndPush(player, action);
		return realCostItems.getAwardItems();
	}
	
	/**
	 * 资源消耗
	 * @param player
	 * @param consume
	 * @param buildingEntity
	 * @param newBuildCfg
	 * @param costTime
	 * @param immediate
	 * @param useGold
	 * @param action
	 * @return
	 */
	public ConsumeItems addResConsume(Player player, ConsumeItems consume,BuildingBaseEntity buildingEntity, BuildingCfg newBuildCfg,
			boolean immediate, boolean useGold, Action action){
		
		List<ItemInfo> resItems = newBuildCfg.getCostItems();
		if (resItems != null) {
			// 资源消耗作用加成
			int[] goldArr = player.getEffect().getEffValArr(EffType.BUILD_UPGRADE_ALL_RES_REDUCE, EffType.EFF_1461, EffType.BUILD_UPGRADE_GOLDORE_REDUCE, EffType.EFF_1462);
			int[] oillArr = player.getEffect().getEffValArr(EffType.BUILD_UPGRADE_ALL_RES_REDUCE, EffType.EFF_1461, EffType.BUILD_UPGRADE_OIL_REDUCE, EffType.EFF_1463);
			int[] tombArr = player.getEffect().getEffValArr(EffType.BUILD_UPGRADE_ALL_RES_REDUCE, EffType.EFF_1461, EffType.BUILD_UPGRADE_TOMBARTHITE_REDUCE, EffType.EFF_1464);
			int[] stelArr = player.getEffect().getEffValArr(EffType.BUILD_UPGRADE_ALL_RES_REDUCE, EffType.EFF_1461, EffType.BUILD_UPGRADE_STEEL_REDUCE, EffType.EFF_1465);
			int[] medal = player.getEffect().getEffValArr(EffType.BLACK_TECH_367811);
			
			GameUtil.reduceByEffect(resItems, goldArr, oillArr, tombArr, stelArr, medal);
			if (buildingEntity != null && buildingEntity.getType() == BuildingType.CONSTRUCTION_FACTORY_VALUE) {
				GameUtil.reduceByEffect(resItems, GsConst.MEDAL_STAR_ID,  player.getEffect().getEffValArr(EffType.EFF_367814));
			}

			consume.addConsumeInfo(resItems, immediate || useGold);
		} else {
			logger.warn("building operate res config is null, playerId: {}, buildCfgId: {}, action: {}", player.getId(), newBuildCfg.getId(), action.name().toLowerCase());
		}
		
		return consume;
	}
	
	/**
	 * 时间转金币消耗
	 * @param player
	 * @param costTime
	 * @param consume
	 * @param multFree
	 */
	public void addTimeConsume(Player player,boolean immediate, double costTime, ConsumeItems consume,int multFree){
		if(!immediate){
			return;
		}
		long needTime = (long) Math.ceil(costTime / 1000d);
		int freeTime = player.getFreeBuildingTime() * multFree;
		if (needTime > freeTime) {
			consume.addConsumeInfo(PlayerAttr.GOLD, GameUtil.caculateTimeGold(needTime - freeTime, SpeedUpTimeWeightType.TIME_WEIGHT_BUILDING));
		}
	}

	/**
	 * 计算建筑升级、建造需要时间 （（建筑原始时间/（1+作用号400）
	 * 
	 * @param queueBuildType
	 * @param buildTime
	 * @param level 建筑目标等级
	 * @return
	 */
	public double getCostTime(Player player, int buildTime, int level) {
		double needTime = buildTime * 1000d * (1 - (player.getEffect().getEffVal(EffType.EFF_1466) + player.getEffect().getEffVal(EffType.EFF_519)) * GsConst.EFF_PER);
		int effVal = player.getData().getEffVal(EffType.CITY_SPD_BUILD);
		effVal +=  player.getData().getEffVal(EffType.BACK_PRIVILEGE_CITY_SPD_BUILD);
		effVal += getBuildSpeedEffByLevel(player, level);
		needTime /= 1d + effVal * GsConst.EFF_PER;
		needTime -= 1000d * player.getData().getEffVal(EffType.CITY_BUILD_REDUCE_TIME);
		return Math.max(0, needTime);
	}
	
	/**
	 * 根据建筑等级获取对应的加速作用号
	 * @param buildLevel
	 */
	private int getBuildSpeedEffByLevel(Player player, int buildLevel) {
		EffType[] effTypes = {EffType.BUILD_SPEED_LEVEL1_PER, EffType.BUILD_SPEED_LEVEL2_PER, EffType.BUILD_SPEED_LEVEL3_PER};
		return GameUtil.getBuildSpeedEffByLevel(player, effTypes, buildLevel);
	}

	/**
	 * 检查该建筑的前置建筑是否存在
	 * 
	 * @param frontBuildingIds
	 * @return
	 */
	public boolean checkFrontCondition(Player player, int[] frontBuildingIds, Map<Integer, Integer> frontConditionParams, int hpCode) {
		if (player.isInDungeonMap()) { // 在副本中, 建筑一律不可信
			return false;
		}
		// 不需要前置建筑
		if (frontBuildingIds != null) {
			// 检查前置建筑
			for (int cfgId : frontBuildingIds) {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, cfgId);
				if (buildingCfg == null) {
					continue;
				}
				
				Optional<BuildingBaseEntity> op = player.getData().getBuildingEntities().stream()
						.filter(e -> e.getType() == buildingCfg.getBuildType())
						.filter(e -> e.getBuildingCfgId() >= cfgId)
						.findAny();
				
				if (!op.isPresent()) {
					if (hpCode > 0) {
						player.sendError(hpCode, Status.Error.BUILDING_FRONT_NOT_EXISIT, 0);
						logger.error("front building check failed, playerId: {}, front buildingId: {}, protocol: {}", player.getId(), cfgId, hpCode);
					}
					return false;
				}
			}
		}

		if (frontConditionParams == null) {
			return true;
		}
		
		for (Entry<Integer, Integer> entry : frontConditionParams.entrySet()) {
			int condType = entry.getKey();
			int condParam = entry.getValue();
			switch (condType) {
				case BuildingConditionType.DRAMA_CHAPTER_PASS: {
					StoryMissionEntity entity = player.getData().getStoryMissionEntity();
					if (entity.getChapterId() <= condParam) {
						if (hpCode > 0) {
							player.sendError(hpCode, Status.Error.BUILD_DRAMA_CHAPTER_COND_ERROR, 0);
						}
						return false;
					}
					break;
				}
				case BuildingConditionType.DRAMA_TASK_MISSION: {
					StoryMissionCfg storyMissionCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionCfg.class, condParam);
					if (storyMissionCfg == null) {
						if (hpCode > 0) {
							player.sendError(hpCode, Status.Error.BUILD_DRAMA_MISSION_COND_ERROR, 0);
						}
						return false;
					}
					
					StoryMissionEntity storyMissionEntity = player.getData().getStoryMissionEntity();
					if (storyMissionEntity.getChapterId() < storyMissionCfg.getChapter()) {
						if (hpCode > 0) {
							player.sendError(hpCode, Status.Error.BUILD_DRAMA_MISSION_COND_ERROR, 0);
						}
						return false;
					}
					
					if (storyMissionEntity.getChapterId() == storyMissionCfg.getChapter()) {
						for (MissionEntityItem mission : storyMissionEntity.getMissionItems()) {
							if (mission.getCfgId() == condParam && mission.getState() == GsConst.MissionState.STATE_NOT_FINISH) {
								if (hpCode > 0) {
									player.sendError(hpCode, Status.Error.BUILD_DRAMA_MISSION_COND_ERROR, 0);
								}
								return false;
							}
						}
					}
					
					break;
				}
				case BuildingConditionType.GENERAL_MISSION: {
					MissionCfg missionCfg = HawkConfigManager.getInstance().getConfigByKey(MissionCfg.class, condParam);
					if (missionCfg == null) {
						if (hpCode > 0) {
							player.sendError(hpCode, Status.Error.BUILD_GENERAL_MISSION_COND_ERROR, 0);
						}
						return false;
					}
					
					int minOrder = Integer.MAX_VALUE, maxOrder = 0;
					for(MissionEntity entity : player.getData().getOpenedMissions()) {
						MissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MissionCfg.class, entity.getCfgId());
						if (entity.getCfgId() == condParam) {
							if (MissionState.STATE_NOT_FINISH == entity.getState()) {
								if (hpCode > 0) {
									player.sendError(hpCode, Status.Error.BUILD_GENERAL_MISSION_COND_ERROR, 0);
								}
								return false;
							}
							break;
						}
						
						if (cfg.getOrder() < minOrder) {
							minOrder = cfg.getOrder();
						}
						
						if (cfg.getOrder() > maxOrder) {
							maxOrder = cfg.getOrder();
						}
					}
					
					if (missionCfg.getOrder() < minOrder) {
						break;
					}
					
					if (missionCfg.getOrder() > maxOrder) {
						if (hpCode > 0) {
							player.sendError(hpCode, Status.Error.BUILD_GENERAL_MISSION_COND_ERROR, 0);
						}
						return false;
					}
					break;
				}
				case BuildingConditionType.TECH_RESEARCH: {
					TechnologyCfg techCfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, condParam);
					if (techCfg == null) {
						if (hpCode > 0) {
							player.sendError(hpCode, Status.Error.BUILD_TECH_LEVEL_COND_ERROR, 0);
						}
						return false;
					}
					
					TechnologyEntity techEntity = player.getData().getTechEntityByTechId(techCfg.getTechId());
					if (techEntity == null || techEntity.getLevel() < techCfg.getLevel()) {
						if (hpCode > 0) {
							player.sendError(hpCode, Status.Error.BUILD_TECH_LEVEL_COND_ERROR, 0);
						}
						return false;
					}
					break;
				}
			}
		}
		
		return true;
	}

	/**
	 * 检查建筑队列此时的状态
	 * 
	 * @param buildingEntity
	 * @param protocol
	 * @return
	 */
	public boolean checkQueueStatus(Player player, long costTime, int protoType) {
		List<QueueEntity> queueList = player.getData().getBusyCommonQueue(QueueType.BUILDING_QUEUE_VALUE);
		// 普通城建队列未使用
		if (queueList.size() == 0) {
			return true;
		}
		
		QueueEntity queueEntity = player.getData().getPaidQueue();
		long now = HawkTime.getMillisecond();
		// 第二城建队列也被占用了
		if (queueEntity.getReusage() != QueueReusage.FREE.intValue()) {
			player.sendError(protoType, Status.Error.BUILDING_QUEUE_STATUS_BUSY, 0);
			logger.error("building queue is occupied, playerId: {}, queue count: {}", player.getId(), queueList.size());
			return false;
		} 
		
		if (player.getData().isSecondBuildUnlock()) {
			return true;
		}
		
		// 第二城建队列不可用，剩余时间不够了
		if (queueEntity.getEnableEndTime() < now + costTime) {
			player.sendError(protoType, Status.Error.PAID_QUEUE_TIME_NOT_ENOUGH, 0);
			logger.error("paidQueue time remaining not enough, playerId: {}, time remaining: {}, needTime: {}",
					player.getId(), queueEntity.getEnableEndTime() - now, costTime);
			return false;
		}
		
		return true;
	}

	/**
	 * 刷新建筑相关作用号
	 * 
	 * @param buildingCfg
	 */
	public void refreshBuildEffect(Player player, BuildingCfg buildingCfg, boolean push) {
		Map<Integer, Integer> buildEffectMap = buildingCfg.getBuildEffectMap();
		if (buildEffectMap.size() > 0) {
			int i = 0;
			EffType[] effType = new EffType[buildEffectMap.size()];
			for (Entry<Integer, Integer> entry : buildEffectMap.entrySet()) {
				effType[i++] = EffType.valueOf(entry.getKey());
				player.getEffect().resetEffectBuilding(player, entry.getKey(), entry.getValue());
			}
			
			if (push) {
				player.getPush().syncPlayerEffect(effType);
			}
		}
	}

	/**
	 * 升级建筑
	 * 
	 * @param nextCfgId
	 * @param status
	 */
	private void upgradeBuilding(Player player, BuildingBaseEntity buildingEntity, BuildingCfg nextCfg) {
		if (nextCfg == null) {
			return;
		}
		int oldBuildingCfgId = buildingEntity.getBuildingCfgId();
		BuildingCfg oldBuildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildingCfgId);
		buildingEntity.setBuildingCfgId(nextCfg.getId());
		buildingEntity.setResUpdateTime(HawkTime.getMillisecond());

		// 触发问卷系统检测
		player.dealMsg(MsgId.BUILDING_LEVELUP_QUESTIONAIRE_CHECK, new QuestionnaireCheckInvoker(player, QuestionnaireConst.CONDITION_BUILDING_LEVEL, 0));

		// 抛出活动事件
		ActivityManager.getInstance().postEvent(new BuildingLevelUpEvent(player.getId(), nextCfg.getBuildType(), nextCfg.getLevel(), nextCfg.getProgress(), false, 
				nextCfg.getBattlePoint() - oldBuildingCfg.getBattlePoint()));
		
		ActivityManager.getInstance().postEvent( new BuildingLevelUpSpreadEvent(player.getId(),
				nextCfg.getBuildType(), nextCfg.getLevel()));

		// 建筑升级事件
		HawkTaskManager.getInstance().postMsg(player.getXid(), BuildingLevelUpMsg.valueOf(nextCfg.getBuildType(), nextCfg.getLevel(), nextCfg));

		// 我要变强
		StrengthenGuideManager.getInstance().postMsg(new SGBuildingLevelUpMsg(player));
		
		if (buildingEntity.getType() == ConstProperty.getInstance().getAirdropUnlockBuildType()) {
			try {
				WharfService.getInstance().syncWharfInfo(player);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (buildingEntity.getType() == BuildingType.WISHING_WELL_VALUE) {
			WishingService.getInstance().syncWishingInfo(player);
		} else if (buildingEntity.getType() == BuildingType.CITY_WALL_VALUE) {
			updateCityDef(player);
		} 

		LogUtil.logBuildFlow(player, buildingEntity, oldBuildingCfg.getLevel(), nextCfg.getLevel());
		
		// 发送荣耀奖励
		if (nextCfg.getHonor() > 0) {
			BuildLevelUpAwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildLevelUpAwardCfg.class, nextCfg.getId());
			if (cfg != null) {
				MailService.getInstance().sendMail(MailParames.newBuilder()
	                    .setPlayerId(player.getId())
	                    .setMailId(MailId.CASTLE_LEVELUP_AWARD)
	                    .addContents(new Object[] {nextCfg.getHonor()})
	                    .addRewards(cfg.getRewardItems())
	                    .setAwardStatus(MailRewardStatus.NOT_GET)
	                    .build());
			}
		}
	}

	/**
	 * 建筑建造或升级完成时添加经验
	 * @param player
	 * @param cfg
	 * @param action
	 */
	public void addExp(Player player, BuildingCfg cfg, Action action) {
		if (cfg.getExp() <= 0) {
			return;
		}
		
		int addExp = cfg.getExp();
		if (player.getExpDec() > 0) {
			Map<String, Integer> removeBuilds = player.getData().getRemoveBuildingExps();
			String removeType = BuildingCfg.getBuildingRemoveType(cfg.getBuildType());
			if (removeBuilds.containsKey(removeType)) {
				int removeExps = removeBuilds.get(removeType);
				removeExps -= cfg.getExp();
				player.getPlayerBaseEntity().setExpDec(Math.max(0, player.getExpDec() - cfg.getExp()));
				if (removeExps <= 0) {
					addExp = Math.abs(removeExps);
					removeBuilds.remove(removeType);
				} else {
					addExp = 0;
					removeBuilds.put(removeType, removeExps);
				}
				RedisProxy.getInstance().updateBuildingRemoveExp(player.getId(), removeType, removeExps);
			}
		}
		
		if (addExp <= 0) {
			return;
		}
		
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addExp(addExp);
		awardItems.rewardTakeAffectAndPush(player, action, true, RewardOrginType.BUILDING_LEVELUP_REWARD);
	}

	/**
	 * 刷新建筑
	 * 
	 * @param buildingEntity
	 * @param operation
	 * @param hpCode
	 */
	public void pushBuildingRefresh(Player player, BuildingBaseEntity buildingEntity, BuildingUpdateOperation operation, int hpCode) {
		BuildingUpdatePush.Builder pushBuilder = BuildingUpdatePush.newBuilder();
		BuildingPB.Builder buildingPB = BuilderUtil.genBuildingBuilder(player, buildingEntity);
		pushBuilder.setBuilding(buildingPB);
		pushBuilder.setOperation(operation);
		player.sendProtocol(HawkProtocol.valueOf(hpCode, pushBuilder));
	}

	/**
	 * 城防值检测更新
	 */
	public void updateCityDef(Player player) {
		int newMaxCityDef = player.getData().getRealMaxCityDef();
		int maxCityDef = player.getData().getMaxCityDef();
		
		int cityDefAdd = newMaxCityDef - maxCityDef;
		// 考虑作用号对城防值的影响
		if (maxCityDef > 0 && cityDefAdd > 0) {
			HawkLog.logPrintln("cityDef update by upgrade building, playerId: {}, before add: {}, add value: {}", 
					player.getId(), player.getPlayerBaseEntity().getCityDefVal(), cityDefAdd);
			CityManager.getInstance().increaseCityDef(player, cityDefAdd);
			player.getPush().syncCityDef(false);
		}
		
		player.getData().setMaxCityDef(newMaxCityDef);
	}
	
	/**
	 * 移除建筑 
	 */
	public void removeBuilding(BuildingBaseEntity buildingEntity, Player player) {
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		// 重新计算最大资源数量
		BuildingService.getInstance().refreshResStoreAndOutput(player.getData(), buildingEntity, false);
		BuildingService.getInstance().refreshResourceRate(player, buildingCfg, false, false);
		BuildingRemovePushPB.Builder builder = BuildingRemovePushPB.newBuilder();
		builder.setBuildingUuid(buildingEntity.getId());
		player.getData().removeBuilding(buildingEntity);
		// 刷新战力
		player.refreshPowerElectric(PowerChangeReason.BUILD_REMOVE);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.BUILDING_REMOVE_PUSH, builder));
		
		LogUtil.logBuildFlow(player, buildingEntity, buildingCfg.getLevel(), 0);
		
		int expTotal = buildingCfg.getExp();
		BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingCfg.getFrontStage());
		while (cfg != null) {
			expTotal += cfg.getExp();
			cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, cfg.getFrontStage());
		}
		
		player.getPlayerBaseEntity().setExpDec(player.getExpDec() + expTotal);
		Map<String, Integer> removeBuildExps = player.getData().getRemoveBuildingExps();
		String removeType = BuildingCfg.getBuildingRemoveType(buildingCfg.getBuildType());
		int removeExps  = removeBuildExps.containsKey(removeType) ? removeBuildExps.get(removeType) : 0;
		removeExps += expTotal;
		removeBuildExps.put(removeType, removeExps);
		RedisProxy.getInstance().updateBuildingRemoveExp(player.getId(), removeType, removeExps);
		// 拆除的是医院建筑
		if (buildingEntity.getType() == BuildingType.HOSPITAL_STATION_VALUE) {
			ArmyService.getInstance().removeLastHospital(player);
		}
		
		HawkApp.getInstance().postMsg(player.getXid(), BuildingRemoveMsg.valueOf(buildingEntity));
		
		//我要变强
		StrengthenGuideManager.getInstance().postMsg(new SGBuildingRemoveMsg(player));
	}
	
	/**
	 * 处理购买基地飞升效果
	 */
	public void dealBaseBuild(Player player) {
		List<Integer> mailContent = new ArrayList<>();
		// 建筑升级
		ConfigIterator<BaseBuildCfg> actCfgs = HawkConfigManager.getInstance().getConfigIterator(BaseBuildCfg.class);
		int baseId = 0;
		for (BaseBuildCfg cfg : actCfgs) {
			if (cfg.getBuildType() == BuildingType.CONSTRUCTION_FACTORY_VALUE) {
				baseId = cfg.getBuildId();
			}
		}
		BuildingCfg baseBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, baseId);
		if (baseBuildCfg == null) {
			return;
		}

		// 解锁地块
		ConfigIterator<BuildAreaCfg> areaCfgs = HawkConfigManager.getInstance().getConfigIterator(BuildAreaCfg.class);
		Set<Integer> unlockedAreas = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
		for (BuildAreaCfg cfg : areaCfgs) {
			int areaId = cfg.getId();
			if (!cfg.isAllowedUnlock() || !cfg.isNeedClick() || cfg.getUnlockCityLevel() > baseBuildCfg.getLevel()) {
				continue;
			}
			if (unlockedAreas.contains(areaId)) {
				continue;
			}
			player.unlockArea(areaId);
			// 推送建筑信息
			player.getPush().synUnlockedArea();
			// 解锁地块任务
			MissionManager.getInstance().postMsg(player, new EventUnlockGround(areaId));
            MissionManager.getInstance().postSuperSoldierTaskMsg(player, new SuperSoldierTriggeTaskMsg(SupersoldierTaskType.UNLOCK_AREA_TASK, 1));
			BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.BUIDING_AREA_UNLOCK, Params.valueOf("buildAreaId", areaId));
		}

		List<QueueEntity> queueList = player.getData().getBusyCommonQueue(QueueType.BUILDING_QUEUE_VALUE);
		ConfigIterator<BaseBuildCfg> _actCfgs = HawkConfigManager.getInstance().getConfigIterator(BaseBuildCfg.class);
		for (BaseBuildCfg cfg : _actCfgs) {
			BuildingCfg targetCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, cfg.getBuildId());
			if (targetCfg == null) {
				// TODO 配置错误
				continue;
			}
			BuildingType buildType = BuildingType.valueOf(cfg.getBuildType());
			BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(buildType);
			if (buildingEntity == null) {
				mailContent.add(0);
			} else {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
				mailContent.add(buildingCfg.getLevel());
			}
			if (cfg.isDIY()) {
				if (buildingEntity == null) {
					List<Integer> emptyBlocks = getEmptyBlocks(player);
					int block = -1;
					// 无空地块,拆除一个建筑
					if (emptyBlocks.isEmpty()) {
						block = removeBlockBuild(player, queueList);
					} else {
						block = emptyBlocks.get(0);
					}
					if (block != -1) {
						BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (buildType.getNumber() * 100) + 1);
						buildingEntity = player.getData().createBuildingEntity(buildingCfg, String.valueOf(block), false);
						BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY,
								HP.code.BUILDING_CREATE_PUSH_VALUE);
					}
				} else {
					QueueEntity currQueue = null;
					for (QueueEntity queue : queueList) {
						// 如果该建筑正在建造或者升级
						if (buildingEntity.getId().equals(queue.getItemId())) {
							currQueue = queue;
							break;
						}
					}
					if (currQueue != null) {
						// 删除队列
						QueuePBSimple.Builder delete = QueuePBSimple.newBuilder();
						delete.setId(currQueue.getId());
						delete.setQueueType(QueueType.valueOf(currQueue.getQueueType()));
						player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_CANCEL_PUSH, delete));
						player.getData().removeQueue(currQueue);
						queueList.remove(currQueue);
					}
				}

			} else {
				buildingEntity = player.getData().getBuildingEntityByType(buildType);
				if (buildingEntity == null) {
					BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (buildType.getNumber() * 100) + 1);
					buildingEntity = player.getData().createBuildingEntity(buildingCfg, "1", false);
					BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY,
							HP.code.BUILDING_CREATE_PUSH_VALUE);
				} else {
					QueueEntity currQueue = null;
					for (QueueEntity queue : queueList) {
						// 如果该建筑正在建造或者升级
						if (buildingEntity.getId().equals(queue.getItemId())) {
							currQueue = queue;
						}
					}
					if (currQueue != null) {
						// 删除队列
						QueuePBSimple.Builder delete = QueuePBSimple.newBuilder();
						delete.setId(currQueue.getId());
						delete.setQueueType(QueueType.valueOf(currQueue.getQueueType()));
						player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_CANCEL_PUSH, delete));
						player.getData().removeQueue(currQueue);
						queueList.remove(currQueue);
					}
				}

			}
			if (buildingEntity != null) {
				// 将指定建筑升到指定等级
				for (int i = 0; i < 30; i++) {
					BuildingCfg buildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
					if (buildCfg == null || buildCfg.getLevel() >= targetCfg.getLevel()) {
						break;
					}
					BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
				}
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
				mailContent.add(buildingCfg.getLevel());
			}
		}
		// 通知客户端建筑升级完毕
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_BASE_BUILD_FINISH));
		MailService.getInstance().sendMail(MailParames.newBuilder()
                .setPlayerId(player.getId())
                .setMailId(MailId.BASE_BUILD_INFO)
                .addContents(mailContent.toArray(new Integer[mailContent.size()]))
                .build());
		
		
	}

	/**
	 * 处理购买基地飞升效果
	 */
	public void dealBackToNewFlyBuild(Player player) {
		List<Integer> mailContent = new ArrayList<>();
		// 建筑升级
		ConfigIterator<BackToNewFlyBuildCfg> actCfgs = HawkConfigManager.getInstance().getConfigIterator(BackToNewFlyBuildCfg.class);
		int baseId = 0;
		for (BackToNewFlyBuildCfg cfg : actCfgs) {
			if (cfg.getBuildType() == BuildingType.CONSTRUCTION_FACTORY_VALUE) {
				baseId = cfg.getBuildId();
			}
		}
		BuildingCfg baseBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, baseId);
		if (baseBuildCfg == null) {
			return;
		}

		// 解锁地块
		ConfigIterator<BuildAreaCfg> areaCfgs = HawkConfigManager.getInstance().getConfigIterator(BuildAreaCfg.class);
		Set<Integer> unlockedAreas = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
		for (BuildAreaCfg cfg : areaCfgs) {
			int areaId = cfg.getId();
			if (!cfg.isAllowedUnlock() || !cfg.isNeedClick() || cfg.getUnlockCityLevel() > baseBuildCfg.getLevel()) {
				continue;
			}
			if (unlockedAreas.contains(areaId)) {
				continue;
			}
			player.unlockArea(areaId);
			// 推送建筑信息
			player.getPush().synUnlockedArea();
			// 解锁地块任务
			MissionManager.getInstance().postMsg(player, new EventUnlockGround(areaId));
			MissionManager.getInstance().postSuperSoldierTaskMsg(player, new SuperSoldierTriggeTaskMsg(SupersoldierTaskType.UNLOCK_AREA_TASK, 1));
			BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.BUIDING_AREA_UNLOCK, Params.valueOf("buildAreaId", areaId));
		}

		List<QueueEntity> queueList = player.getData().getBusyCommonQueue(QueueType.BUILDING_QUEUE_VALUE);
		ConfigIterator<BackToNewFlyBuildCfg> _actCfgs = HawkConfigManager.getInstance().getConfigIterator(BackToNewFlyBuildCfg.class);
		for (BackToNewFlyBuildCfg cfg : _actCfgs) {
			BuildingCfg targetCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, cfg.getBuildId());
			if (targetCfg == null) {
				// TODO 配置错误
				continue;
			}
			BuildingType buildType = BuildingType.valueOf(cfg.getBuildType());
			BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(buildType);
			if (buildingEntity == null) {
				mailContent.add(0);
			} else {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
				mailContent.add(buildingCfg.getLevel());
			}
			if (cfg.isDIY()) {
				if (buildingEntity == null) {
					List<Integer> emptyBlocks = getEmptyBlocks(player);
					int block = -1;
					// 无空地块,拆除一个建筑
					if (emptyBlocks.isEmpty()) {
						block = removeBlockBuild(player, queueList);
					} else {
						block = emptyBlocks.get(0);
					}
					if (block != -1) {
						BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (buildType.getNumber() * 100) + 1);
						buildingEntity = player.getData().createBuildingEntity(buildingCfg, String.valueOf(block), false);
						BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY,
								HP.code.BUILDING_CREATE_PUSH_VALUE);
					}
				} else {
					QueueEntity currQueue = null;
					for (QueueEntity queue : queueList) {
						// 如果该建筑正在建造或者升级
						if (buildingEntity.getId().equals(queue.getItemId())) {
							currQueue = queue;
							break;
						}
					}
					if (currQueue != null) {
						// 删除队列
						QueuePBSimple.Builder delete = QueuePBSimple.newBuilder();
						delete.setId(currQueue.getId());
						delete.setQueueType(QueueType.valueOf(currQueue.getQueueType()));
						player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_CANCEL_PUSH, delete));
						player.getData().removeQueue(currQueue);
						queueList.remove(currQueue);
					}
				}

			} else {
				buildingEntity = player.getData().getBuildingEntityByType(buildType);
				if (buildingEntity == null) {
					BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (buildType.getNumber() * 100) + 1);
					buildingEntity = player.getData().createBuildingEntity(buildingCfg, "1", false);
					BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY,
							HP.code.BUILDING_CREATE_PUSH_VALUE);
				} else {
					QueueEntity currQueue = null;
					for (QueueEntity queue : queueList) {
						// 如果该建筑正在建造或者升级
						if (buildingEntity.getId().equals(queue.getItemId())) {
							currQueue = queue;
						}
					}
					if (currQueue != null) {
						// 删除队列
						QueuePBSimple.Builder delete = QueuePBSimple.newBuilder();
						delete.setId(currQueue.getId());
						delete.setQueueType(QueueType.valueOf(currQueue.getQueueType()));
						player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_CANCEL_PUSH, delete));
						player.getData().removeQueue(currQueue);
						queueList.remove(currQueue);
					}
				}

			}
			if (buildingEntity != null) {
				// 将指定建筑升到指定等级
				for (int i = 0; i < 30; i++) {
					BuildingCfg buildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
					if (buildCfg == null || buildCfg.getLevel() >= targetCfg.getLevel()) {
						break;
					}
					BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
				}
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
				mailContent.add(buildingCfg.getLevel());
			}
		}
		// 通知客户端建筑升级完毕
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_BASE_BUILD_FINISH));
		BackToNewFlyKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackToNewFlyKvCfg.class);
		MailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.setMailId(MailId.valueOf(kvCfg.getRewardMail()))
				.addContents(mailContent.toArray(new Integer[mailContent.size()]))
				.addRewards(ItemInfo.valueListOf(kvCfg.getRewardItemGetNew()))
				.build());


	}
	
	/**
	 * 移除重复的等级最低的DIY建筑
	 * @param player
	 * @return
	 */
	private int removeBlockBuild(Player player, List<QueueEntity> queueList) {
		QueueEntity delQueue = null;
		// 优先移除拆除中的建筑
		for (QueueEntity queueEntity : queueList) {
			if (queueEntity.getStatus() == QueueStatus.QUEUE_STATUS_REMOVE_VALUE) {
				delQueue = queueEntity;
			}
		}
		if (delQueue != null) {
			String buildId = delQueue.getItemId();
			BuildingBaseEntity delBuild = player.getData().getBuildingBaseEntity(buildId);
			// 正在删除diy区的建筑
			if (delBuild != null && BuildAreaCfg.isShareBlockBuildType(delBuild.getType())) {
				int buildIndex = Integer.valueOf(delBuild.getBuildIndex());
				QueuePBSimple.Builder delete = QueuePBSimple.newBuilder();
				delete.setId(delQueue.getId());
				delete.setQueueType(QueueType.valueOf(delQueue.getQueueType()));
				player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_CANCEL_PUSH, delete));
				player.getData().removeQueue(delQueue);
				queueList.remove(delQueue);
				removeBuilding(delBuild, player);
				return buildIndex;
			}
		}
		Map<Integer, HawkTuple3<String, Integer, Integer>> selectMap = new HashMap<>();
		for (BuildingBaseEntity buildEntity : player.getData().getBuildingEntities()) {
			int buildType = buildEntity.getType();
			int buildId = buildEntity.getBuildingCfgId();
			if (BuildAreaCfg.isShareBlockBuildType(buildType)) {
				HawkTuple3<String, Integer, Integer> tuple = selectMap.get(buildType);
				if (tuple == null) {
					selectMap.put(buildType, new HawkTuple3<String, Integer, Integer>(buildEntity.getId(), buildEntity.getBuildingCfgId(), 1));
				} else {
					int cnt = tuple.third + 1;
					if (buildId < tuple.second) {
						selectMap.put(buildType, new HawkTuple3<String, Integer, Integer>(buildEntity.getId(), buildId, cnt));
					} else {
						selectMap.put(buildType, new HawkTuple3<String, Integer, Integer>(tuple.first, tuple.second, cnt));
					}
				}
			}
		}
		if (selectMap.isEmpty()) {
			return -1;
		}
		HawkTuple2<String, Integer> selecter = null;
		for (Entry<Integer, HawkTuple3<String, Integer, Integer>> entry : selectMap.entrySet()) {
			HawkTuple3<String, Integer, Integer> tuple3 = entry.getValue();
			if (tuple3.third <= 1) {
				continue;
			}
			if (selecter == null) {
				selecter = new HawkTuple2<String, Integer>(tuple3.first, tuple3.second);
			} else {
				BuildingCfg oldCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, selecter.second);
				BuildingCfg newCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, tuple3.second);
				if (newCfg.getLevel() < oldCfg.getLevel()) {
					selecter = new HawkTuple2<String, Integer>(tuple3.first, tuple3.second);
				}
			}
		}
		if (selecter == null) {
			return -1;
		}
		String buildId = selecter.first;
		BuildingBaseEntity buildingEntity = player.getData().getBuildingBaseEntity(buildId);
		if (buildingEntity == null) {
			return -1;
		}
		int block = Integer.valueOf(buildingEntity.getBuildIndex());
		QueueEntity currQueue = null;
		for (QueueEntity queue : queueList) {
			// 如果该建筑正在建造或者升级
			if (buildingEntity.getId().equals(queue.getItemId())) {
				currQueue = queue;
			}
		}
		if (currQueue != null) {
			// 删除队列
			QueuePBSimple.Builder delete = QueuePBSimple.newBuilder();
			delete.setId(currQueue.getId());
			delete.setQueueType(QueueType.valueOf(currQueue.getQueueType()));
			player.sendProtocol(HawkProtocol.valueOf(HP.code.QUEUE_CANCEL_PUSH, delete));
			player.getData().removeQueue(currQueue);
			queueList.remove(currQueue);
		}
		removeBuilding(buildingEntity, player);
		return block;
	}

	/**
	 * 获取空闲地块
	 * @param player
	 * @return
	 */
	public List<Integer> getEmptyBlocks(Player player){
		Set<Integer> areaIds = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
		List<Integer> emptyBlocks = new ArrayList<>();
		for(int areaId : areaIds){
			BuildAreaCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildAreaCfg.class, areaId);
			emptyBlocks.addAll(cfg.getBlockList());
		}
		for(BuildingBaseEntity buildEntity : player.getData().getBuildingEntities()){
			int buildType = buildEntity.getType();
			if(BuildAreaCfg.isShareBlockBuildType(buildType)){
				Integer block = Integer.valueOf(buildEntity.getBuildIndex());
				emptyBlocks.remove(block);
			}
		}
		return emptyBlocks;
	}
	
}
