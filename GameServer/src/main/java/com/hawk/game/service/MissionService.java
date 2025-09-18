package com.hawk.game.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.collection.ConcurrentHashTable;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Table;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.MissionCfg;
import com.hawk.game.entity.MissionEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.BuildLevelUpRefreshMissionInvoker;
import com.hawk.game.invoker.CityLevelUpRefreshMissionInvoker;
import com.hawk.game.invoker.FunMissionRefreshInvoker;
import com.hawk.game.invoker.PlayerLevelUpRefreshMissionInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mission.MissionBonusRes;
import com.hawk.game.protocol.Mission.MissionPB;
import com.hawk.game.protocol.Mission.MissionRefreshRes;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.mssion.MissionContext;
import com.hawk.game.service.mssion.funtype.IFunMission;
import com.hawk.game.util.GsConst.MissionFunType;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.TaskType;

/**
 * 任务服务类
 * 
 * @author lating
 *
 */
public class MissionService {
	
	static final Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 单例实例
	 */
	private static MissionService instance;
	/**
	 * 记录玩家已开启且还未领取完任务奖励的任务标记
	 */
	public Map<String, List<Integer>> playerMissionTag = new HashMap<String, List<Integer>>();
	/**
	 * 累计型任务数据<playerId, missionId, num>
	 */
	private Table<String, Integer, Integer> overlayMissionTable = ConcurrentHashTable.create();
	
	/**
	 * 任务开启条件类型
	 */
	enum MissionCoditionType {
		PLAYER_LEVEL,
		BUILD_LEVEL,
		PRE_MISSION
	}
	
	public static MissionService getInstance() {
		if (instance == null) {
			instance = new MissionService();
			
			Set<Integer> missionTypes = MissionCfg.getMissionTypes();
			HawkLog.logPrintln("mission config mission types: {}", missionTypes.stream().sorted().collect(Collectors.toList()));
			Set<Integer> accumulatedTypeSet = MissionCfg.getAccumulatedTypes();
			HawkLog.logPrintln("mission config accumulated types: {}", accumulatedTypeSet.stream().sorted().collect(Collectors.toList()));
		}
		
		return instance;
	}
	
	/**
	 * 创建角色时初始化任务数据
	 * @param player
	 */
	public void initMissionList(Player player, List<HawkDBEntity> initDbEntities) {
		try {
			List<MissionEntity> missionList = player.getData().getMissionEntities();
			for(Integer cfgId : MissionCfg.getInitMissions()) {
				MissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MissionCfg.class, cfgId);
				// 大本限制不开启或已创建的不再创建
				if(cfg.getDefaultVal() == 0 && (checkMissionExist(player, cfg.getId()) || cfg.getCastleClass() <= player.getCityLv())) {
					continue;
				}
				
				MissionEntity entity = new MissionEntity();
				entity.setId(HawkOSOperator.randomUUID());
				entity.setPlayerId(player.getId());
				entity.setCfgId(cfg.getId());
				entity.resetTypeId(cfg.getMissionTypeId());
				entity.setState(MissionState.STATE_NOT_OPEN);
				missionList.add(entity);
				initDbEntities.add(entity);
			}
			
			// 遍历未开启已创建的任务，条件满足则加到推送列表中
			openMissions(player);
			
		} catch(Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 玩家登陆时加载累计型任务数据
	 * 
	 * @param playerId
	 */
	public void loadOverlayMissionData(String playerId) {
		// 从redis中加载累计型任务累计数量值，使得玩家在线时可以直接从内存获取数据，而不用每次访问redis
		Map<String, String> overlayMissionAttrMap = RedisProxy.getInstance().getOverlayMissionAttr(playerId);
		for (Entry<String, String> entry : overlayMissionAttrMap.entrySet()) {
			overlayMissionTable.put(playerId, Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
		}
		
		// 当玩家还没有累计型任务数据时，先默认造一个（这个数据并不会入库，其作用只是保证table中包含playerId，防止后面重复读redis）
		if (!overlayMissionTable.rowKeySet().contains(playerId)) {
			overlayMissionTable.put(playerId, 0, 0);	
		}
	}
	
	/**
	 * 玩家下线时从内存中清除累计型任务数据, 防止流失玩家的数据永久占据内存
	 * 
	 * @param playerId
	 */
	public void unloadOverlayMissionData(String playerId) {
		// 累计型任务数据<playerId, missionId, num>
		Set<Integer> missionIds = overlayMissionTable.columnKeySet();
		for (Integer missionId : missionIds) {
			overlayMissionTable.remove(playerId, missionId);
		}
	}
	
	/**
	 * 大本升级检测大本等级截止条件任务
	 * @param player
	 * @param cityLevel
	 */
	public void onCityLevelUp(Player player, int cityLevel) {
		player.dealMsg(MsgId.CITY_LEVELUP_MISSION_REFRESH, new CityLevelUpRefreshMissionInvoker(player, cityLevel));
	}

	/**
	 * 根据建筑等级条件刷任务
	 * @param player
	 * @param buildCfgId
	 */
	public void refreshBuildLevelConditionMission(Player player, int buildCfgId) {
		player.dealMsg(MsgId.BUILD_LEVELUP_MISSION_REFRESH, new BuildLevelUpRefreshMissionInvoker(player, buildCfgId));
	}
	
	/**
	 * 根据玩家等级条件刷新任务
	 * @param player
	 */
	public void refreshPlayerLevelConditionMission(Player player) {
		player.dealMsg(MsgId.PLAYER_LEVELUP_MISSION_REFRESH, new PlayerLevelUpRefreshMissionInvoker(player));
	}
	
	/**
	 * 异步任务刷新
	 * @param player
	 * @param funType
	 * @param funId
	 * @param funVal
	 */
	public void missionRefresh(Player player, MissionFunType funType, int funId, int funVal) {
		if (funVal <= 0) {
			return;
		}
		player.dealMsg(MsgId.MISSION_REFRESH, new FunMissionRefreshInvoker(player, funType, funId, funVal));
	}
	
	/**
	 * 同步任务刷新
	 * @param player
	 * @param funType
	 * @param funId
	 * @param funVal
	 */
	public void missionRefreshAsync(Player player, MissionFunType funType, int funId, int funVal) {
		if (funVal <= 0) {
			return;
		}
		
		// 对累计型任务数据先进行累加
		missionAttrOverlay(player, funType, funId, funVal);
		if (player.isActiveOnline()) {
			// 判断任务是否已刷出
			if (checkMissionExist(player.getId(), funType, funId)) {
				missionRefreshInner(player, funType, funId, funVal);
			}
		} else {
			for (MissionEntity missionEntity : player.getData().getOpenedMissions()) {
				refreshMissionSingle(player, missionEntity, funType.intValue(), funId, funVal);
			}
		}
	}
	
	/**
	 * 玩家登陆任务检测
	 * @param player
	 */
	public void missionLoginCheck(Player player) {
		List<Integer> types = new ArrayList<Integer>();
		List<MissionEntity> list = player.getData().getOpenedMissions();
		for (MissionEntity missionEntity : list) {
			if(missionEntity.getState() == MissionState.STATE_BONUS){
				continue;
			}
			
			MissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MissionCfg.class, missionEntity.getCfgId());
			if (cfg == null) {
				continue;
			}
			
			missionEntity.resetTypeId(cfg.getMissionTypeId());
			types.add(missionEntity.getTypeId());
			// 考虑到在更改配置的情况下任务完成情况能相应更新，登录时需要刷新任务数据
			missionNumAttrAsync(player, cfg, missionEntity);
		}
		
		addMissionTag(player.getId(), types.toArray(new Integer[0]));
	}
	
	/**
	 * 同步新刷出的任务
	 * @param player
	 * @param missionEntities
	 */
	public void syncNewOpenedMissions(Player player, List<MissionEntity> missionEntities) {
		if (missionEntities.size() <= 0) {
			return;
		}
		
		MissionBonusRes.Builder res = MissionBonusRes.newBuilder();
		res.setRemoveMissionId("0");
		for (MissionEntity missionEntity : missionEntities) {
			res.addAddMission(entityToPB(missionEntity));
		}
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.MISSION_UPDATE_SYNC_S, res);
		player.sendProtocol(protocol);
	}

	/**
	 * 任务领取奖励
	 * @param protoId
	 * @param missionId
	 */
	public void bonusMission(Player player, String missionId) {
		MissionEntity missionEntity = player.getData().getMissionById(missionId);
		if (missionEntity == null) {
			logger.error("bonus mission failed, missionEntity not exist, playerId: {}, missionId: {}", player.getId(), missionId);
			return;
		}

		if (missionEntity.getState() != MissionState.STATE_FINISH) {
			logger.error("bonus mission failed, mission status not match, playerId: {}, missionId: {}, state: {}, cfgId: {}", player.getId(), missionId, missionEntity.getState(), missionEntity.getCfgId());
			player.sendError(HP.code.MISSION_BONUS_S_VALUE, Status.Error.MISSION_NOT_FINISH, 0);
			return;
		}

		MissionBonusRes.Builder res = MissionBonusRes.newBuilder();
		try {
			missionEntity.setState(MissionState.STATE_BONUS);
			MissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MissionCfg.class, missionEntity.getCfgId());
			// 移除完成的任务
			removeMission(player, missionEntity);
			logTaskFlow(player, cfg, MissionState.STATE_BONUS);
			// 添加新任务
			List<MissionEntity> afterMissions = refreshPostPosMissions(player, cfg.getId());
			if(afterMissions != null && afterMissions.size() > 0) {
				for (MissionEntity entity : afterMissions) {
					res.addAddMission(entityToPB(entity));
				}
			}
			
			AwardItems award = AwardItems.valueOf();
			award.addItemInfos(cfg.getRewardItemInfo());
			award.rewardTakeAffectAndPush(player, Action.MISSION_BONUS, true, RewardOrginType.MISSION_REWARD);
			res.setRemoveMissionId(missionEntity.getId());
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		logger.debug("mission bonus success, playerId: {}, missionId: {}, cfgId: {}, typeId: {}", player.getId(), missionId, missionEntity.getCfgId(), missionEntity.getTypeId());
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.MISSION_BONUS_S_VALUE, res);
		player.sendProtocol(protocol);
		// 刷新完成任务个数的任务
		missionRefresh(player, MissionFunType.FUN_MISSION_ACCOMPLISH, 0, 1);
	}
	
	/**
	 * 刷后置任务
	 * @param player
	 * @param preMissionId
	 * @return
	 */
	private List<MissionEntity> refreshPostPosMissions(Player player, int preMissionId) {
		List<Integer> afterMissions = MissionCfg.getAfterMissionsByPreMission(preMissionId);
		// 遍历未开启已创建的任务，条件满足则加到推送列表中
		List<MissionEntity> newOpenMissions = openMission(player, afterMissions, preMissionId);
		return newOpenMissions;
	}
	
	/**
	 * 累计型任务累计数量增加
	 * 
	 * @param player
	 * @param funType
	 * @param funId
	 * @param num
	 */
	private void missionAttrOverlay(Player player, MissionFunType funType, int funId, int num) {
		if(funType.isOverlay()) {
			// 只有玩家在线时才加载redis数据，防止流失玩家的数据永久占用内存
			if (player.isActiveOnline() && !overlayMissionTable.rowKeySet().contains(player.getId())) {
				loadOverlayMissionData(player.getId());
			}
			
			int typeId = MissionCfg.getTypeId(funType, funId);
			Integer oldNum = overlayMissionTable.get(player.getId(), typeId);
			if (oldNum == null && !player.isActiveOnline()) {
				oldNum = RedisProxy.getInstance().getOverlayMissionAttr(typeId, player.getId());
			}
			
			num += (oldNum == null ? 0 : oldNum);
			// 只有玩家在线时才将刷新的数据更新到table中，防止流失玩家的数据永久占据内存
			if (overlayMissionTable.rowKeySet().contains(player.getId())) {
				overlayMissionTable.put(player.getId(), typeId, num);
			}
			
			// 及时写，缓存读
			RedisProxy.getInstance().addOverlayMissionAttr(typeId, player.getId(), num);
		}
	}

	/**
	 * 刷新任务
	 * @param player
	 * @param funType 任务类型
	 * @param funId   任务类型下的功能id
	 * @param funVal  增量
	 */
	private void missionRefreshInner(Player player, MissionFunType funType, int funId, int funVal) {
		List<MissionEntity> result = new ArrayList<>();
		for (MissionEntity missionEntity : player.getData().getOpenedMissions()) {
			if (!refreshMissionSingle(player, missionEntity, funType.intValue(), funId, funVal)) {
				continue;
			}
			
			if (!result.contains(missionEntity)) {
				result.add(missionEntity);
			}
			logger.debug("player mission refresh, playerId: {}, cfgId: {}, addNum: {}, entityNum: {}, state: {}", 
					player.getId(), missionEntity.getCfgId(), funVal, missionEntity.getNum(), missionEntity.getState());
		}

		if (result.size() > 0) {
			MissionRefreshRes.Builder res = MissionRefreshRes.newBuilder();
			for (MissionEntity tmpEntity : result) {
				res.addRefreshMission(entityToPB(tmpEntity));
			}
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.MISSION_REFRESH_S_VALUE, res);
			player.sendProtocol(protocol);
		}
	}
	
	/**
	 * 刷新单个任务
	 * 
	 * @param player
	 * @param missionEntity
	 * @param funType
	 * @param funId
	 * @param funVal
	 * @return
	 */
	private boolean refreshMissionSingle(Player player, MissionEntity missionEntity, int funType, int funId, int funVal) {
		// 已完成的任务直接跳过
		if (missionEntity.getState() == MissionState.STATE_FINISH
				|| missionEntity.getState() == MissionState.STATE_BONUS) {
			return false;
		}
		
		MissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MissionCfg.class, missionEntity.getCfgId());
		// 任务不匹配
		if (Objects.isNull(cfg) || cfg.getFunType() != funType || cfg.getFunId() != funId) {
			return false;
		}
		
		missionEntity.setNum(Math.max(0, missionEntity.getNum() + funVal));
		// 达到完成条件数量时改变任务状态
		if (missionEntity.getNum() >= cfg.getFunVal()) {
			missionEntity.setNum(cfg.getFunVal());
			missionEntity.setState(MissionState.STATE_FINISH);
			logTaskFlow(player, cfg, MissionState.STATE_FINISH);
		}
		
		return true;
	}
	
	/**
	 * 创建新的任务时判断已完成情况
	 * @param player
	 * @param cfg
	 * @param entity
	 */
	private void missionNumAttrAsync(Player player, MissionCfg cfg, MissionEntity entity) {
		IFunMission mission = MissionContext.getInstance().getMissions(cfg.getFunType());
		if (mission != null) {
			long num = mission.genMissionNum(player.getData(), cfg.getFunId());
			if (num > entity.getNum()) {
				entity.setNum((int) Math.min(Integer.MAX_VALUE - 1, num));
			}
		}
		
		// 累计型任务数值处理
		if(cfg.isOverlayMission()) {
			if (!overlayMissionTable.rowKeySet().contains(player.getId())) {
				loadOverlayMissionData(player.getId());
			}
			
			Integer num = overlayMissionTable.get(player.getId(), cfg.getMissionTypeId());
			if (num == null && !player.isActiveOnline()) {
				num = RedisProxy.getInstance().getOverlayMissionAttr(cfg.getMissionTypeId(), player.getId());
			}
			
			if (num != null && num > entity.getNum()) {
				entity.setNum(num);
			}
		}
		
		if (entity.getNum() >= cfg.getFunVal()) {
			entity.setNum(cfg.getFunVal());
			entity.setState(MissionState.STATE_FINISH);
		}
	}
	
	/**
	 * 创建并开启任务
	 * @param player
	 * @param missionIds
	 * @return
	 */
	public List<MissionEntity> openMission(Player player, List<Integer> missionIds, Integer... preMissionId) {
		
		List<MissionEntity> missionList = player.getData().getMissionEntities();
		for(Integer cfgId : missionIds) {
			MissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MissionCfg.class, cfgId);
			// 大本限制不开启或已创建的不再创建
			if(cfg.getDefaultVal() == 0 && (checkMissionExist(player, cfg.getId()) || cfg.getCastleClass() <= player.getCityLv())) {
				continue;
			}
			
			MissionEntity entity = new MissionEntity();
			entity.setId(HawkOSOperator.randomUUID());
			entity.setPlayerId(player.getId());
			entity.setCfgId(cfg.getId());
			entity.resetTypeId(cfg.getMissionTypeId());
			entity.setState(MissionState.STATE_NOT_OPEN);
			if (preMissionId.length > 0 && cfg.getPreMissions() != null) {
				entity.addUnfinishPreMission(cfg.getPreMissions());
				entity.removeUnfinishPreMission(preMissionId[0]);
			}
			
			if (!entity.create(true)) {
				HawkLog.errPrintln("create mission entity failed, playerId: {}, missionId: {}", player.getId(), cfg.getId());
				continue;
			}
			
			missionList.add(entity);
		}
		
		// 遍历未开启已创建的任务，条件满足则加到推送列表中
		List<MissionEntity> newOpenMissions = openMissions(player, preMissionId);
		
		return newOpenMissions;
	}
	
	/**
	 * 开启任务
	 * @param player
	 * @return
	 */
	private List<MissionEntity> openMissions(Player player, Integer... preMissionId) {
		List<MissionEntity> missions = player.getData().getMissionEntities();
		List<MissionEntity> newMissions = new ArrayList<>();
		for(MissionEntity entity : missions) {
			if (entity.getState() != MissionState.STATE_NOT_OPEN) {
				continue;
			}
			
			List<Integer> unfinishPreMissions = entity.getUnfinishPreMissions();
			if (unfinishPreMissions != null) {
				if (preMissionId.length > 0) {
					unfinishPreMissions.remove(preMissionId[0]);
				}
				// 还有未完成的前置任务
				if (!unfinishPreMissions.isEmpty()) {
					continue;
				}
			} 
			
			MissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MissionCfg.class, entity.getCfgId());
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, cfg.getBuildingClass());
			int buildLevel = 0, buildingMaxLevel = 0;
			if (buildingCfg != null) {
				buildLevel = buildingCfg.getLevel();
				buildingMaxLevel = player.getData().getBuildingMaxLevel(buildingCfg.getBuildType());
			}
			
			// 开启条件成立判断, 成立则添加到playerData中: 玩家等级、前置建筑、 前置任务
			if(cfg.getDefaultVal() > 0 || (buildingMaxLevel >= buildLevel && player.getLevel() >= cfg.getLevel())) {
				entity.setState(MissionState.STATE_NOT_FINISH);
				// 对于在登录前已创建出来，而登录时又没有开启的任务，typeId并没有设置过，这里开启时需要重新设置
				entity.resetTypeId(cfg.getMissionTypeId());
				missionNumAttrAsync(player, cfg, entity);
				newMissions.add(entity);
				addMissionTag(player.getId(), entity.getTypeId());
				if(entity.getState() == MissionState.STATE_FINISH) {
					logTaskFlow(player, cfg, MissionState.STATE_FINISH);
				} else {
					logTaskFlow(player, cfg, MissionState.STATE_NOT_FINISH);
				}
				
				logger.debug("open new mission, playerId: {}, cfgId: {}, typeId: {}, num: {}, state: {}", player.getId(), cfg.getId(), entity.getTypeId(), entity.getNum(), entity.getState());
			}
		}
		
		return newMissions;
	}
	
	/**
	 * 删除完成任务
	 * @param entity
	 * @return
	 */
	private void removeMission(Player player, MissionEntity entity) {
		removeMissionTag(player, entity.getTypeId());
		player.getData().getMissionEntities().remove(entity);
		entity.delete();
		
		List<MissionEntity> list = player.getData().getOpenedMissions();
		for (MissionEntity missionEntity : list) {
			if (entity.getTypeId() == missionEntity.getTypeId()) {
				addMissionTag(player.getId(), missionEntity.getTypeId());
				break;
			}
		}
	}
	
	/**
	 * 清除标记
	 */
	public void removeMissionTag(Player player, Integer... typeIds) {
		List<Integer> list = playerMissionTag.get(player.getId());
		if (list == null) {
			list = new ArrayList<Integer>();
			playerMissionTag.put(player.getId(), list);
		}
		
		if(typeIds.length <= 0) {
			list.clear();
		} else {
			for(Integer typeId : typeIds) {
				list.remove(typeId);
			}
		}
	}
	
	/**
	 * 添加标记
	 */
	private void addMissionTag(String playerId, Integer... types) {
		List<Integer> list = playerMissionTag.get(playerId);
		if (list == null) {
			list = new ArrayList<Integer>();
			playerMissionTag.put(playerId, list);
		}
		
		for (Integer type : types) {
			if (!list.contains(type)) {
				list.add(type);
			}
		}
	}

	/**
	 * 检查任务是否已刷出
	 * @return true表示任务已刷出
	 */
	private boolean checkMissionExist(String playerId, MissionFunType funType, int funId) {
		List<Integer> list = playerMissionTag.get(playerId);
		if (list == null) {
			list = new ArrayList<Integer>();
			playerMissionTag.put(playerId, list);
		}
		
		int typeId = MissionCfg.getTypeId(funType, funId);
		if (!list.contains(typeId)) {
			return false;
		}
		return true;
	}
	
	/**
	 * @return 查检任务列表是否包含此任务
	 * @param cfgId
	 */
	private boolean checkMissionExist(Player player, int cfgId) {
		Optional<MissionEntity> op = player.getData().getMissionEntities().stream().filter(e -> e.getCfgId() == cfgId).findAny();
		return op.isPresent();
	}
	
	/**
	 * 将entity转为pb对象
	 * @param entity
	 */
	public MissionPB entityToPB(MissionEntity entity) {
		MissionPB.Builder builder = MissionPB.newBuilder();
		builder.setMissionId(entity.getId());
		builder.setCfgId(entity.getCfgId());
		builder.setState(entity.getState());
		builder.setNum(entity.getNum());
		return builder.build();
	}
	
	/**
	 * 记录任务流水日志
	 * @param player
	 * @param cfg
	 * @param state
	 */
	private void logTaskFlow(Player player, MissionCfg cfg, int state) {
		int type = cfg.getFunType();
		if(MissionFunType.valueOf(type) != null) {
			type = MissionFunType.valueOf(type).intLogTypeVal();
		}
		
		LogUtil.logTaskFlow(player, TaskType.GENERAL_MISSION, type, cfg.getId(), state, 0);
	}
	
}
