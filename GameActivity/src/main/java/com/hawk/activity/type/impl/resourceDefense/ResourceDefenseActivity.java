package com.hawk.activity.type.impl.resourceDefense;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import com.hawk.activity.event.impl.ResourceDefenseSuccessEvent;
import com.hawk.activity.type.impl.resourceDefense.cfg.*;
import com.hawk.game.protocol.*;
import com.hawk.gamelib.GameConst;
import com.hawk.serialize.string.SerializeHelper;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.ResourceDefenseBeStealEvent;
import com.hawk.activity.event.impl.ResourceDefenseUnlockEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.resourceDefense.entity.ResourceDefenseEntity;
import com.hawk.activity.type.impl.resourceDefense.temp.ResourceDefenseTemplate;
import com.hawk.game.protocol.Activity.RDBuyExpInfo;
import com.hawk.game.protocol.Activity.RDChargeResp;
import com.hawk.game.protocol.Activity.RDRecord;
import com.hawk.game.protocol.Activity.RDRecord.Builder;
import com.hawk.game.protocol.Activity.RDRecordType;
import com.hawk.game.protocol.Activity.RDResourceInfo;
import com.hawk.game.protocol.Activity.RDSRSingleInfo;
import com.hawk.game.protocol.Activity.RDStationInfo;
import com.hawk.game.protocol.Activity.RDStationState;
import com.hawk.game.protocol.Activity.RDStealResourcePageInfo;
import com.hawk.game.protocol.Activity.RDStealResp;
import com.hawk.game.protocol.Activity.ResourceDefensePageInfo;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.log.Action;

/**
 * 资源保卫战
 * @author golden
 *
 */
public class ResourceDefenseActivity extends ActivityBase {

	/**
	 * 日志
	 */
	private static final Logger logger = LoggerFactory.getLogger("Server");

	
	/**
	 * 参与活动的玩家Id
	 */
	public List<String> playerIds = new CopyOnWriteArrayList<>();
	
	/**
	 * 已经偷取的玩家记录
	 */
	public Map<String, Map<String, Long>> stealPlayer = new HashMap<>();

	/**
	 * 2帮助玩家防御的次数 3,帮助玩家偷取的次数
 	 */
	private final int AGENT_RECORD_2  = 1;
	private final int AGENT_RECORD_3  = 2;

	/**
	 * 是否经过初始化
	 */
	private boolean isInit;
	
	public ResourceDefenseActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.RESOURCE_DEFENSE;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ResourceDefenseActivity activity = new ResourceDefenseActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ResourceDefenseEntity> queryList = HawkDBManager.getInstance().query("from ResourceDefenseEntity where playerId = ? and termId = ? and invalid = 0", playerId,
				termId);
		
		ResourceDefenseEntity retEntity = null;
		
		int exp = 0;
		if (queryList != null && queryList.size() > 0) {
			for (ResourceDefenseEntity entity : queryList) {
				if (entity.getExp() >= exp) {
					exp = entity.getExp();
					retEntity = entity;
				}
			}
		}
		return retEntity;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ResourceDefenseEntity entity = new ResourceDefenseEntity(playerId, termId);
		
		for (Integer stationType : getStationTypes()) {
			ResourceDefenseTemplate station = new ResourceDefenseTemplate(stationType);
			entity.addStation(station);
		}
		return entity;
	}

	/**
	 * 获取资源站类型
	 */
	public Set<Integer> getStationTypes() {
		Set<Integer> types = new HashSet<>();
		ConfigIterator<ResourceDefenseStationCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(ResourceDefenseStationCfg.class);
		while(cfgIterator.hasNext()) {
			ResourceDefenseStationCfg cfg = cfgIterator.next();
			types.add(cfg.getStationType());
		}
		return types;
	}
	
	@Override
	public void onTick() {
		if (!isInit) {
			init();
		}
	}
	
	/**
	 * 初始化
	 */
	public void init() {

		playerIds = new CopyOnWriteArrayList<>();

		String sql = String.format("select playerId from activity_resource_defense where termId=%d order by exp desc limit %d", getActivityTermId(), 2000);
		
		List<String> playerIds = HawkDBManager.getInstance().executeQuery(sql, null);
		for (String playerId : playerIds) {
			this.playerIds.add(playerId);
		}
		
		isInit = true;
	}
	
	@Override
	public void onOpen() {
		playerIds = new CopyOnWriteArrayList<>();
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		syncPageInfo(playerId);
	}

	/**
	 * 同步界面信息
	 */
	public void syncPageInfo(String playerId) {

		// 活动未开启
		if (!isOpening(null)) {
			return;
		}

		int hp = HP.code.RESOURCE_DEFENSE_PAGE_INFO_RESP_VALUE;

		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();

		checkStealTimes(playerId);
		
		// 刷新资源站状态
		refreshStationState(entity);

		ResourceDefensePageInfo.Builder builder = ResourceDefensePageInfo.newBuilder();
		
		// 等级、经验
		builder.setResourceStationLevel(getStationLevel(entity.getExp()));
		builder.setResourceStationExp(entity.getExp());
		
		// 资源库
		for (Entry<Integer, Integer> resInfo : getCurrentResourceInfoMap(entity).entrySet()) {
			RDResourceInfo.Builder resInfoBuilder = RDResourceInfo.newBuilder();
			resInfoBuilder.setResourceType(resInfo.getKey());
			resInfoBuilder.setResourceNum(resInfo.getValue());
			builder.addResourceInfo(resInfoBuilder);
		}
		
		// 资源站信息
		for (ResourceDefenseTemplate stationInfo : entity.getStationInfoList()) {
			RDStationInfo.Builder stationBuilder = RDStationInfo.newBuilder();
			stationBuilder.setStationId(stationInfo.getStationType());
			
			RDStationState state = RDStationState.valueOf(stationInfo.getState());
			if (state == RDStationState.RDSTATION_NONE) {
				stationBuilder.setState(state);
			} else {
				if (stationInfo.hasResource()) {
					stationBuilder.setState(state);
				} else {
					stationBuilder.setState(RDStationState.RDSTATION_DOING);
				}
			}
			
			// 资源站配置
			stationBuilder.setEndTime(stationInfo.getLastTickTime() + getStationResPeroid(entity, stationInfo));
			
			// 可帮助
			stationBuilder.setCanHelp(stationInfo.getRequestHelp() == 0);
			
			stationBuilder.setCanSpeedUp(stationInfo.getSpeedValue() == 0);
			
			stationBuilder.setCanReceive(stationInfo.hasResource());
			
			builder.addStationInfo(stationBuilder);
		}
		
		builder.setUnlockSenior(entity.unlcokSuper());
		
		// 任务信息
		for (int receiveMissionId : entity.getReceivedRewardIdSet()) {
			builder.addReceiveMissionId(receiveMissionId);
		}
		
		// 购买经验
		for (Entry<Integer, Integer> buyExpInfo : entity.getBuyExpInfoMap().entrySet()) {
			RDBuyExpInfo.Builder buyExpInfoBuilder = RDBuyExpInfo.newBuilder();
			buyExpInfoBuilder.setCfgId(buyExpInfo.getKey());
			buyExpInfoBuilder.setBuyTimes(buyExpInfo.getValue());
			builder.addBuyExpInfo(buyExpInfoBuilder);
		}
		
		// 偷取次数
		builder.setStealTimes(entity.getStealTimes());
		builder.setRemainStealTimes(entity.getCanStealTimes());
		List<Builder> records = getRecord(playerId, ResourceDefenseCfg.getInstance().getRecordLimit());
		for (Builder record : records) {
			builder.addRecord(record);
		}
		/**
		 * 特工数据
		 */
		Activity.PBAgentPageInfo.Builder pbAgentBuilder = genAgentInfoPB(entity);
		builder.setAgentPageInfo(pbAgentBuilder);

		builder.setLastStealTimesAdd(entity.getStealTimesTick());
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(hp, builder));
	}

	/**
	 * 获取当前资源数量
	 */
	public Map<Integer, Integer> getCurrentResourceInfoMap(ResourceDefenseEntity entity) {
		Map<Integer, Integer> resourceInfo = new HashMap<>();
		
		for (ResourceDefenseTemplate stationInfo : entity.getStationInfoList()) {
			for (Entry<Integer, Integer> info : stationInfo.getResourceMap().entrySet()) {
				Integer beforeCount = resourceInfo.getOrDefault(info.getKey(), 0);
				resourceInfo.put(info.getKey(), info.getValue() + beforeCount);
			}
		}
		
		return resourceInfo;
	}

	/**
	 * 获取资源站等级
	 */
	public int getStationLevel(int stationExp) {
		int level = 1;
		ConfigIterator<ResourceDefenseLevelCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ResourceDefenseLevelCfg.class);
		while (configIterator.hasNext()) {
			ResourceDefenseLevelCfg cfg = configIterator.next();
			if (stationExp >= cfg.getExp()) {
				level = cfg.getLevel();
			}
		}
		return level;
	}

	/**
	 * 开采资源站
	 */
	public void buildStation(String playerId, int stationId) {

		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();

		ResourceDefenseTemplate station = null;
		
		// 取对应资源站
		List<ResourceDefenseTemplate> stationInfoList = entity.getStationInfoList();
		for (ResourceDefenseTemplate stationInfo : stationInfoList) {
			if (stationInfo.getStationType() == stationId) {
				station = stationInfo;
				break;
			}
		}

		// 资源站不存在
		if (station == null) {
			return;
		}
		
		// 资源站不是废弃状态
		if (station.getState() != RDStationState.RDSTATION_NONE_VALUE) {
			return;
		}
		
		long currentTime = HawkTime.getMillisecond();
		station.setState(RDStationState.RDSTATION_DOING_VALUE);
		station.setBeginTime(currentTime);
		station.setLastTickTime(currentTime);
		station.setSpeedValue(0);
		entity.notifyUpdate();
		
		responseSuccess(playerId, HP.code.RESOURCE_DEFENSE_BUILD_STATION_REQ_VALUE);

		syncPageInfo(playerId);

		// tlog
		getDataGeter().logResourceDefenseBuild(playerId, station.getStationType());
		
		if (!playerIds.contains(playerId)) {
			this.playerIds.add(playerId);
		}
	}

	/**
	 * 刷新资源站状态 
	 */
	public void refreshStationState(ResourceDefenseEntity entity) {

		checkBuyExpReset(entity);

		long currentTime = HawkTime.getMillisecond();

		// 资源站等级
		int stationLevel = getStationLevel(entity.getExp());

		for (ResourceDefenseTemplate station : entity.getStationInfoList()) {

			// 资源站配置
			ResourceDefenseStationCfg stationCfg = getStationCfg(stationLevel, station.getStationType());
			
			// 废弃状态不处理
			if (station.getState() == RDStationState.RDSTATION_NONE_VALUE) {
				continue;
			}

			// tick时间满了
			if (station.getTickTimes() >= stationCfg.getTimes()) {
				if (!station.hasResource()) {
					station.reset();
				}
				continue;
			}
			
			long peroid = getStationResPeroid(entity, station);
			
			// 到了产出资源的时间
			if (currentTime - station.getLastTickTime() > peroid) {
				// 已经tick的次数
				int beforeTickTimes = station.getTickTimes();
				
				// 计算tick次数
				int addTickTimes = (int)((currentTime - station.getLastTickTime()) / peroid);
				addTickTimes = Math.min(addTickTimes, stationCfg.getTimes() - beforeTickTimes);

				// 添加tick次数
				station.addTickTimes(addTickTimes);
				station.setState(RDStationState.RDSTATION_CHARGE_VALUE);
				station.setLastTickTime(station.getLastTickTime() + addTickTimes * peroid);
				
				entity.notifyUpdate();
				
				try {
					for (int i = 0; i < addTickTimes; i++) {
						Integer reward = HawkRand.randomWeightObject(stationCfg.getRewardMap());
						if (reward != null) {
							station.addResourceCount(reward, 1);
						}
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				// 加经验
				addExp(entity, stationCfg.getCollectExp() * addTickTimes, "doCharge");
			}
		}
	}

	/**
	 * 获取资源站配置
	 */
	public ResourceDefenseStationCfg getStationCfg(int stationLevel, int stationType) {
		ConfigIterator<ResourceDefenseStationCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ResourceDefenseStationCfg.class);
		while(iterator.hasNext()) {
			ResourceDefenseStationCfg cfg = iterator.next();
			if (cfg.getStationType() == stationType && cfg.getLevel() == stationLevel) {
				return cfg;
			}
		}
		return HawkConfigManager.getInstance().getConfigByIndex(ResourceDefenseStationCfg.class, 0);
	}

	/**
	 * 增加经验
	 */
	private void addExp(ResourceDefenseEntity entity, int exp, String action) {
		int beforeExp = entity.getExp();
		int beforeLevel = getStationLevel(beforeExp);

		entity.addExp(exp);

		int afterExp = entity.getExp();
		int afterLevel = getStationLevel(afterExp);

		logger.info("resourceDefense, addExp, playerId:{}, beforeExp:{}, beforeLevel:{}, afterExp:{}, afterLevel:{}, action:{}",
				entity.getPlayerId(), beforeExp, beforeLevel, afterExp, afterLevel, action);
		
		// tlog
		getDataGeter().logResourceDefenseExp(entity.getPlayerId(), exp, afterExp, afterLevel);
	}

	/**
	 * 加速
	 */
	public void doSpeedUp(String playerId, int stationId) {
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();

		ResourceDefenseTemplate station = null;
		
		// 取对应资源站
		List<ResourceDefenseTemplate> stationInfoList = entity.getStationInfoList();
		for (ResourceDefenseTemplate stationInfo : stationInfoList) {
			if (stationInfo.getStationType() == stationId) {
				station = stationInfo;
				break;
			}
		}

		// 资源站不存在
		if (station == null) {
			return;
		}
		
		// 废弃状态，不可以加速
		if (station.getState() == RDStationState.RDSTATION_NONE_VALUE) {
			return;
		}
		
		if (station.getSpeedValue() != 0) {
			return;
		}
		
		ResourceDefenseStationCfg stationCfg = getStationCfg(getStationLevel(entity.getExp()), station.getStationType());
		station.setSpeedValue(stationCfg.getSpeedUp());
		entity.notifyUpdate();
		
		responseSuccess(playerId, HP.code.RESOURCE_DEFENSE_SPEEDUP_STATION_REQ_VALUE);
		
		// 同步界面信息
		syncPageInfo(playerId);
	}
	
	/**
	 * 收取
	 */
	public void doCharge(String playerId, int stationId) {
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();

		ResourceDefenseTemplate station = null;

		// 取对应资源站
		List<ResourceDefenseTemplate> stationInfoList = entity.getStationInfoList();
		for (ResourceDefenseTemplate stationInfo : stationInfoList) {
			if (stationInfo.getStationType() == stationId) {
				station = stationInfo;
				break;
			}
		}

		// 资源站不存在
		if (station == null) {
			return;
		}

		if (!station.hasResource()) {
			syncPageInfo(playerId);
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_CHARGE_REQ_VALUE, Status.ResourceDefenseError.RD_RESOURCE_BE_STEAL_VALUE);
			return;
		}
		
		// 不可以收取
		if (station.getState() != RDStationState.RDSTATION_CHARGE_VALUE) {
			return;
		}
		
		RDChargeResp.Builder builder = RDChargeResp.newBuilder();
		
		// 奖励
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		for (Entry<Integer, Integer> resource : station.getResourceMap().entrySet()) {
			RDResourceInfo.Builder info = RDResourceInfo.newBuilder();
			info.setResourceType(resource.getKey());
			info.setResourceNum(resource.getValue());
			builder.addResourceInfo(info);
			
			RewardItem.Builder rewardBuilder = RewardHelper.toRewardItem(ItemType.TOOL_VALUE, resource.getKey(), resource.getValue());
			rewardList.add(rewardBuilder);
			
		}
		ActivityReward reward = new ActivityReward(rewardList, Action.RESOURCE_DEFENSE_CHARGE);

		// 清空资源站
		ResourceDefenseStationCfg stationCfg = getStationCfg(getStationLevel(entity.getExp()), station.getStationType());
		if (station.getTickTimes() >= stationCfg.getTimes()) {
			station.reset();
		} else {
			// 开采
			station.setState(RDStationState.RDSTATION_DOING_VALUE);
			station.clearResource();
		}
		entity.notifyUpdate();
		
		// 发奖
		postReward(entity.getPlayerId(), reward);
		
		responseSuccess(playerId, HP.code.RESOURCE_DEFENSE_CHARGE_REQ_VALUE);
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RESOURCE_DEFENSE_CHARGE_RESP, builder));
		
		syncPageInfo(playerId);
	}

	/**
	 * 获取奖励
	 */
	public void getReward(String playerId, int cfgId) {
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();

		// 配置不存在
		ResourceDefenseRewardsCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ResourceDefenseRewardsCfg.class, cfgId);
		if (cfg == null) {
			logger.error("resourceDefense, getAward, cfgIdError, cfgId:{}", cfgId);
			return;
		}

		// 等级不足，不能领奖
		if (cfg.getLevel() > getStationLevel(entity.getExp())) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_GET_MISSION_REQ_VALUE, Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		
		// 未解锁高级奖励
		if (cfg.getQuality() == 2 && !entity.unlcokSuper()) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_GET_MISSION_REQ_VALUE, Status.ResourceDefenseError.RD_AWARD_SUPER_NOT_UNLOCK_VALUE);
			return;
		}

		// 档位奖励已经领取过
		if (entity.getReceivedRewardIdSet().contains(cfgId)) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_GET_MISSION_REQ_VALUE, Status.ResourceDefenseError.RD_AWARD_ALREADY_RECEIVED_VALUE);
			return;
		}

		// 先设置状态为已领取
		entity.addReceivedRewardId(cfgId);

		ActivityReward reward = new ActivityReward(cfg.getRewardList(), Action.RESOURCE_DEFENSE_LEVEL_REWARD);
		// bigrewards.setOrginType(RewardOrginType.BOUNTY_HUNTER_BIG_REWARD, activity.getActivityId());
		postReward(entity.getPlayerId(), reward);

		responseSuccess(playerId, HP.code.RESOURCE_DEFENSE_GET_MISSION_REQ_VALUE);
		
		syncPageInfo(playerId);
		
		logger.info("resourceDefense, getReward, playerId:{}, cfgId:{}", entity.getPlayerId(), cfgId);
	}

	/**
	 * 是否可以解锁高级奖励
	 */
	public boolean resourceDefenseUnlockCheck(String playerId) {
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		ResourceDefenseEntity entity = opEntity.get();

		if (entity.unlcokSuper()) {
			return false;
		}
		return true;
	}

	/**
	 * 获取所有仇敌
	 */
	public Set<String> getAllEmemy(String playerId) {
		Set<String> playerIds = new HashSet<>();
		
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return playerIds;
		}
		ResourceDefenseEntity entity = opEntity.get();
		for (ResourceDefenseTemplate station : entity.getStationInfoList()) {
			playerIds.addAll(station.getBeStealList());
		}
		return playerIds;
	}
	
	/**
	 * 偷取界面
	 */
	public void stealPage(String reqPlayerId) {
		RDStealResourcePageInfo.Builder builder = RDStealResourcePageInfo.newBuilder();

		// 所有在线玩家乱序
		List<String> playerIds = new ArrayList<>();
		playerIds.addAll(this.playerIds);
		Collections.shuffle(playerIds);

		Set<String> enemys = getAllEmemy(reqPlayerId);
		
		int searchCount = 0;
		
		// 列表数量
		int count = 0;
		// 活动开启后30分钟不给刷玩家列表,减轻跨天服务器压力
		long cross0Time = HawkTime.getMillisecond() - getTimeControl().getStartTimeByTermId(getActivityTermId());
		if (cross0Time > ResourceDefenseCfg.getInstance().getStealPageBanTime()) {
			int listCount = ResourceDefenseCfg.getInstance().getPickupListNum();
			for (String playerId : playerIds) {
				
				searchCount++;
				
				if (searchCount > 50) {
					break;
				}
				
				if (count >= listCount) {
					break;
				}
				
				if (playerId.equals(reqPlayerId)) {
					continue;
				}
				
				if (getDataGeter().isCrossPlayer(playerId)) {
					continue;
				}

				if(!getDataGeter().isPlayerExist(playerId)){
					continue;
				}

				Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					continue;
				}
				ResourceDefenseEntity entity = opEntity.get();
				
				// 玩家被偷次数达到上限
				if (entity.getBeStealTimes() >= ResourceDefenseCfg.getInstance().getPickupCoverLimit()) {
					this.playerIds.remove(entity.getPlayerId());
					continue;
				}
				
				if (getStealPlayerMap(reqPlayerId).containsKey(playerId)) {
					continue;
				}
				
				// 刷新资源数量
				refreshStationState(entity);
				
				// 玩家资源总数
				long resourceCount = 0;
				Map<Integer, Integer> currentResourceInfoMap = getCurrentResourceInfoMap(entity);
				for (Entry<Integer, Integer> currentResource : currentResourceInfoMap.entrySet()) {
					resourceCount += currentResource.getValue();
				}
				
				// 玩家资源过少，不能被偷
				if (resourceCount <= ResourceDefenseCfg.getInstance().getCanBeStealResLimit()) {
					continue;
				}
				
				RDSRSingleInfo.Builder singleInfo = RDSRSingleInfo.newBuilder();
				try {
					singleInfo.setPlayerId(entity.getPlayerId());
					singleInfo.setPlayerName(getDataGeter().getPlayerName(entity.getPlayerId()));
					singleInfo.setStationLevel(getStationLevel(entity.getExp()));
					singleInfo.setIcon(getDataGeter().getIcon(entity.getPlayerId()));
					singleInfo.setPfIcon(getDataGeter().getPfIcon(entity.getPlayerId()));
				} catch(Exception e) {
					HawkLog.errPrintln("resource defense stealPage error, id:{}", playerId);
					continue;
				}
				
				count++;
				
				String guildId = getDataGeter().getGuildId(entity.getPlayerId());
				if (!HawkOSOperator.isEmptyString(guildId)) {
					singleInfo.setGuildTag(getDataGeter().getGuildTag(guildId));
				}
				singleInfo.setEnemy(enemys.contains(playerId));
				
				for (Entry<Integer, Integer> currentResource : currentResourceInfoMap.entrySet()) {
					RDResourceInfo.Builder rdBuilder = RDResourceInfo.newBuilder();
					rdBuilder.setResourceType(currentResource.getKey());
					rdBuilder.setResourceNum(currentResource.getValue());
					singleInfo.addResourceInfo(rdBuilder);
				}
				
				builder.addSingleInfo(singleInfo);
			}
		}
		if (count == 0) {
			/**常规的刷机器人,,有改动*/
			List<Integer> robotIds = getRandomRobotIdList();
			for (int robotId : robotIds) {
				RDSRSingleInfo.Builder singleInfo = genRobotInfoPB(robotId, 0);
				builder.addSingleInfo(singleInfo);
			}
		}

		/**
		 * 这里要判断,,,之前是否有高级机器人存在,,type =3存在, 把高级机器人数据带过去
		 */
		Optional<ResourceDefenseEntity> reqPlayerEntity = getPlayerDataEntity(reqPlayerId);
		if (reqPlayerEntity.isPresent()) {
			ResourceDefenseEntity entity = reqPlayerEntity.get();
			/**高级机器人已经存在*/
			int robotId = ResourceDefenseRobotCfg.getGreatRobotId();
			if (entity.getGreatRobotInfoMap().containsKey(robotId)){
				int greatRobotStealTimes = entity.getGreatRobotInfoMap().get(robotId);
				RDSRSingleInfo.Builder singleInfo = genRobotInfoPB(robotId, greatRobotStealTimes);
				builder.addSingleInfo(singleInfo);
			}
		}

		int hp = HP.code.RESOURCE_DEFENSE_STEAL_PAGE_RESP_VALUE;
		PlayerPushHelper.getInstance().pushToPlayer(reqPlayerId, HawkProtocol.valueOf(hp, builder));
		
	}

	/**
	 * 偷取
	 */
	public void steal(String playerId, String targetPlayerId, int stationId) {
		
		if (getDataGeter().isCrossPlayer(targetPlayerId)) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_HELP_REQ_VALUE, Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE);
			return;
		}
		
		if (playerId.equals(targetPlayerId)) {
			return;
		}
		
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();

		Optional<ResourceDefenseEntity> opTarEntity = getPlayerDataEntity(targetPlayerId);
		if (!opTarEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity tarEntity = opTarEntity.get();

		// 玩家偷取次数达到上限
		if (entity.getCanStealTimes() <= 0) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_STEAL_VALUE, Status.ResourceDefenseError.RD_STEAL_TIMES_LIMIT_VALUE);
			return;
		}

		// 目标玩家被偷次数达到上限
		if (tarEntity.getBeStealTimes() >= ResourceDefenseCfg.getInstance().getPickupCoverLimit()) {
			RDStealResp.Builder builder = RDStealResp.newBuilder();
			builder.setSuccess(false);
			builder.setBeTimesLimit(true);
			int hp = HP.code.RESOURCE_DEFENSE_STEAL_RESP_VALUE;
			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(hp, builder));
			return;
		}

		
		// ----------------------资源保卫战优化需求: 偷取同一玩家有cd---------
		Map<String, Long> stealPlayerMap = getStealPlayerMap(playerId);
		
		// 偷取同一个玩家cd
		if (stealPlayerMap.containsKey(targetPlayerId)) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_STEAL_VALUE, Status.ResourceDefenseError.RD_STEAL_ONE_CD_VALUE);
			return;
		}
		// -------------------------------------------------------------------------
		
		
		// 资源站等级
		int stationLevel = getStationLevel(entity.getExp());
		ResourceDefenseLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(ResourceDefenseLevelCfg.class, stationLevel);
		int pickupWeight = levelCfg.getPickupWeight();
		/**触发特工技能 成功概率值
		 * 偷取即消耗次数，不成功也消耗
		 */
		HawkTuple2<Integer, Integer> agentRateValue = triggerAgentSkillValue(entity, Activity.PBAgentSkillType.STEAL_ADD_RATE);
		if (agentRateValue.second > 0){
			entity.addAgentSkillUseTimes(agentRateValue.first);
		}
		// 偷取失败
		if (HawkRand.randInt(10000) > pickupWeight + agentRateValue.second) {
			RDStealResp.Builder builder = RDStealResp.newBuilder();
			builder.setSuccess(false);
			int hp = HP.code.RESOURCE_DEFENSE_STEAL_RESP_VALUE;
			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(hp, builder));
			
			// 添加偷取失败记录
			Builder record = RDRecord.newBuilder();
			record.setTime(HawkTime.getMillisecond());
			record.setType(RDRecordType.STEAL_FAILE);
			
			String guildId = getDataGeter().getGuildId(playerId);
			if (!HawkOSOperator.isEmptyString(guildId)) {
				String tag = getDataGeter().getGuildTag(guildId);
				record.setGuildTag(tag);
			}
			record.setName(getDataGeter().getPlayerName(playerId));
			record.setStationId(stationId);
			addRecord(targetPlayerId, record);
			
			entity.setStealTimes(entity.getStealTimes() + 1);

			/**触发特工技能 偷取失败返还次数*/
			HawkTuple2<Integer, Integer> agentFailGiveNum = triggerAgentSkillValue(entity, Activity.PBAgentSkillType.STEAL_FAIL_GIVE_NUM);
			if (agentFailGiveNum.second > 0){
				entity.addAgentSkillUseTimes(agentFailGiveNum.first);
			}
			entity.setCanStealTimes(entity.getCanStealTimes() - 1 + agentFailGiveNum.second);
			entity.notifyUpdate();

			syncPageInfo(playerId);
			stealPlayerMap.put(targetPlayerId, HawkTime.getMillisecond());

			/** 被偷玩家数据修改,防御成功 */
			ActivityManager.getInstance().postEvent(new ResourceDefenseSuccessEvent(targetPlayerId));
			return;
		}

		ResourceDefenseTemplate station = null;
		
		// 取对应资源站
		List<ResourceDefenseTemplate> stationInfoList = tarEntity.getStationInfoList();
		for (ResourceDefenseTemplate stationInfo : stationInfoList) {
			if (stationInfo.getStationType() == stationId) {
				station = stationInfo;
				break;
			}
		}

		// 资源站不存在
		if (station == null) {
			RDStealResp.Builder builder = RDStealResp.newBuilder();
			builder.setSuccess(false);
			int hp = HP.code.RESOURCE_DEFENSE_STEAL_RESP_VALUE;
			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(hp, builder));
			return;
		}
		
		// 随机偷取
		Map<Integer, Integer> tarResourceInfoMap = station.getResourceMap();
		if (tarResourceInfoMap.isEmpty()) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_STEAL_VALUE, Status.ResourceDefenseError.RD_STEAL_TARGET_REFRESH_VALUE);
			others(playerId, targetPlayerId);
			return;
		}
		
		int resCount = 0;
		for (Entry<Integer, Integer> res : tarResourceInfoMap.entrySet()) {
			resCount += res.getValue();
		}
		
		if (resCount <= 0) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_STEAL_VALUE, Status.ResourceDefenseError.RD_STEAL_TARGET_REFRESH_VALUE);
			others(playerId, targetPlayerId);
			return;
		}
		
		Integer rewardId = HawkRand.randomWeightObject(tarResourceInfoMap);
		if (rewardId == null) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_STEAL_VALUE, Status.ResourceDefenseError.RD_STEAL_TARGET_REFRESH_VALUE);
			others(playerId, targetPlayerId);
			return;
		}
		int value = 1;
		/**触发特工技能 增加奖励个数值*/
		HawkTuple2<Integer, Integer> agentAddValue = triggerAgentSkillValue(entity, Activity.PBAgentSkillType.STEAL_ADD_NUM);
		if (agentAddValue.second > 0){
			entity.addAgentSkillUseTimes(agentAddValue.first);
		}
		value += agentAddValue.second;
		// 偷取成功发奖
		List<RewardItem.Builder> stealList = new ArrayList<>();
		RewardItem.Builder rewardBuilder = RewardHelper.toRewardItem(ItemType.TOOL_VALUE, rewardId, value);
		stealList.add(rewardBuilder);
		ActivityReward reward = new ActivityReward(stealList, Action.RESOURCE_DEFENSE_STEAL);
		postReward(entity.getPlayerId(), reward);
		/**触发概率技能偷取成功的记录*/
		if (agentRateValue.second > 0){
			entity.updateAgentRecord(AGENT_RECORD_3, 1);
		}
		/**触发额外偷数量技能偷取成功的记录*/
		if (agentAddValue.second > 0){
			entity.updateAgentRecord(AGENT_RECORD_3, 1);
		}
		// 偷取成功返回
		RDStealResp.Builder builder = RDStealResp.newBuilder();
		builder.setSuccess(true);
		RDResourceInfo.Builder rdResourceInfo = RDResourceInfo.newBuilder();
		rdResourceInfo.setResourceType(rewardId);
		rdResourceInfo.setResourceNum(value);
		builder.addResourceInfo(rdResourceInfo);
		int hp = HP.code.RESOURCE_DEFENSE_STEAL_RESP_VALUE;
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(hp, builder));

		// 目标玩家扣资源
		ActivityManager.getInstance().postEvent(new ResourceDefenseBeStealEvent(targetPlayerId, stationLevel, stationId, rewardId, playerId));

		// tlog
		getDataGeter().logResourceDefenseSteal(playerId, targetPlayerId);
		
		entity.setStealTimes(entity.getStealTimes() + 1);
		entity.setCanStealTimes(entity.getCanStealTimes() - 1);
		
		ResourceDefenseStationCfg tarStationCfg = getStationCfg(getStationLevel(tarEntity.getExp()), station.getStationType());
		/**触发特工技能 额外增加经验 返回1 双倍经验*/
		HawkTuple2<Integer, Integer> agentExpTimes = triggerAgentSkillValue(entity, Activity.PBAgentSkillType.STEAL_ADD_EXP);
		if (agentExpTimes.second > 0){
			entity.addAgentSkillUseTimes(agentExpTimes.first);
		}

		addExp(entity, tarStationCfg.getPickupExp() * (1 + agentExpTimes.second), "steal");
		
		stealPlayerMap.put(targetPlayerId, HawkTime.getMillisecond());

		syncPageInfo(playerId);
	}

	
	/**
	 * 偷机器人
	 */
	public void stealRobot(String playerId, int robotId) {
		
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();

		// 玩家偷取次数达到上限
		if (entity.getCanStealTimes() <= 0) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_STEAL_VALUE, Status.ResourceDefenseError.RD_STEAL_TIMES_LIMIT_VALUE);
			return;
		}

		ResourceDefenseRobotCfg robotCfg = HawkConfigManager.getInstance().getConfigByKey(ResourceDefenseRobotCfg.class, robotId);
		// 玩家偷取高级机器人达到上限
		if (robotCfg.getType() == Activity.PBRobotType.ROBOT_2_VALUE || robotCfg.getType() == Activity.PBRobotType.ROBOT_3_VALUE) {
			//机器人每日可偷晶体个数  这个是每日限制,
			int limitNum = ResourceDefenseCfg.getInstance().getRobotStealNumMap().getOrDefault(robotCfg.getType(), 0);
			if (entity.getStealRobotInfoMap().getOrDefault(robotCfg.getType(), 0) >= limitNum){
				//高级机器人可偷取次数已用尽
				sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_STEAL_VALUE, Status.ResourceDefenseError.RD_ROBOT_STEAL_TIME_LIMIT_VALUE);
				return;
			}
		}
		if (robotCfg.getType() == Activity.PBRobotType.ROBOT_3_VALUE) {
			if (!entity.getGreatRobotInfoMap().containsKey(robotId)){
				//高级机器人未激活
				sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_STEAL_VALUE, Status.ResourceDefenseError.RD_GREAT_ROBOT_NO_ACTIVE_VALUE);
				return;
			}
			int hasStealGreatRobotTimes = entity.getGreatRobotInfoMap().getOrDefault(robotId, 0);
			Map<Integer, Integer> tarResourceInfoMap = robotCfg.getResourceInfoMap();
			int resourceNumLimit = tarResourceInfoMap.values().stream().findFirst().get();
			if (hasStealGreatRobotTimes >= resourceNumLimit){
				//高级机器人资源站已经没有资源可偷取
				sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_STEAL_VALUE, Status.ResourceDefenseError.RD_GREAT_ROBOT_NO_RESOURCE_VALUE);
				return;
			}
		}
		/**触发特工技能 成功概率值*/
		HawkTuple2<Integer, Integer> agentRateValue = triggerAgentSkillValue(entity, Activity.PBAgentSkillType.STEAL_ADD_RATE);
		if (agentRateValue.second > 0){
			entity.addAgentSkillUseTimes(agentRateValue.first);
		}
		// 偷取失败
		if (HawkRand.randInt(10000) >  robotCfg.getPickupWeight() + agentRateValue.second ) {  //ResourceDefenseCfg.getInstance().getRobotStealRate()
			RDStealResp.Builder builder = RDStealResp.newBuilder();
			builder.setSuccess(false);
			int hp = HP.code.RESOURCE_DEFENSE_STEAL_RESP_VALUE;
			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(hp, builder));

			/**触发特工技能 偷取失败返还次数*/
			HawkTuple2<Integer, Integer> agentFailGiveNum = triggerAgentSkillValue(entity, Activity.PBAgentSkillType.STEAL_FAIL_GIVE_NUM);
			if (agentFailGiveNum.second > 0){
				entity.addAgentSkillUseTimes(agentFailGiveNum.first);
			}
			entity.setStealTimes(entity.getStealTimes() + 1);
			entity.setCanStealTimes(entity.getCanStealTimes() - 1 + agentFailGiveNum.second);
			
			syncPageInfo(playerId);
			return;
		}
		
		// 随机偷取
		Map<Integer, Integer> tarResourceInfoMap = robotCfg.getResourceInfoMap();//ResourceDefenseCfg.getInstance().getRobotResourceInfoMap();
		Integer rewardId = HawkRand.randomWeightObject(tarResourceInfoMap);
		
		// 偷取成功发奖
		List<RewardItem.Builder> stealList = new ArrayList<>();
		int value = 1;
		/**触发特工技能 增加个数值*/
		HawkTuple2<Integer, Integer> agentAddValue = triggerAgentSkillValue(entity, Activity.PBAgentSkillType.STEAL_ADD_NUM);
		if (agentAddValue.second > 0){
			entity.addAgentSkillUseTimes(agentAddValue.first);
		}
		value += agentAddValue.second;

		RewardItem.Builder rewardBuilder = RewardHelper.toRewardItem(ItemType.TOOL_VALUE, rewardId, value);
		stealList.add(rewardBuilder);
		ActivityReward reward = new ActivityReward(stealList, Action.RESOURCE_DEFENSE_STEAL);
		postReward(entity.getPlayerId(), reward);

		/**触发概率技能偷取成功的记录*/
		if (agentRateValue.second > 0){
			entity.updateAgentRecord(AGENT_RECORD_3, 1);
		}
		/**触发额外偷数量技能偷取成功的记录*/
		if (agentAddValue.second > 0){
			entity.updateAgentRecord(AGENT_RECORD_3, 1);
		}
		// 偷取成功返回
		RDStealResp.Builder builder = RDStealResp.newBuilder();
		builder.setSuccess(true);
		RDResourceInfo.Builder rdResourceInfo = RDResourceInfo.newBuilder();
		rdResourceInfo.setResourceType(rewardId);
		rdResourceInfo.setResourceNum(value);
		builder.addResourceInfo(rdResourceInfo);
		int hp = HP.code.RESOURCE_DEFENSE_STEAL_RESP_VALUE;
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(hp, builder));

		// tlog
		getDataGeter().logResourceDefenseSteal(playerId, "robot_" + robotId);
		
		entity.setStealTimes(entity.getStealTimes() + 1);
		entity.setCanStealTimes(entity.getCanStealTimes() - 1);
		

		/**触发特工技能 额外增加经验 返回1 双倍经验*/
		HawkTuple2<Integer, Integer> agentExpTimes = triggerAgentSkillValue(entity, Activity.PBAgentSkillType.STEAL_ADD_EXP);
		if (agentExpTimes.second > 0){
			entity.addAgentSkillUseTimes(agentExpTimes.first);
		}
		addExp(entity, robotCfg.getPickupExp() * (1 + agentExpTimes.second ), "steal"); //ResourceDefenseCfg.getInstance().getRobotStealExpAdd()
		/**
		 * 修改高级机器人的数据,
		 */
		if (robotCfg.getType() == Activity.PBRobotType.ROBOT_2_VALUE || robotCfg.getType() == Activity.PBRobotType.ROBOT_3_VALUE) {
			//更新次数
			entity.addRobotStealTimes(robotCfg.getType());
		}
		//高级机器人数据修改
		if (robotCfg.getType() == Activity.PBRobotType.ROBOT_3_VALUE) {
			if (!entity.getGreatRobotInfoMap().containsKey(robotId)) {
				//高级机器人未激活
				return;
			}
			//更新次数
			entity.addGreatRobotStealTimes(robotId);
		}

		syncPageInfo(playerId);
	}
	
	/**
	 * 获取已经偷取玩家的map
	 */
	private Map<String, Long> getStealPlayerMap(String playerId) {
		long current = HawkTime.getMillisecond();
		Map<String, Long> stealPlayerMap = stealPlayer.get(playerId);
		if (stealPlayerMap == null) {
			stealPlayerMap = new HashMap<>();
			stealPlayer.put(playerId, stealPlayerMap);
		}
		
		// 维护下内存， 别让这个列表太大
		List<String> removePlayerIds = new ArrayList<>();
		for (Entry<String, Long> steal : stealPlayerMap.entrySet()) {
			if (current - steal.getValue() > ResourceDefenseCfg.getInstance().getStealOneCd()) {
				removePlayerIds.add(steal.getKey());
			}
		}
		for (String removePlayerId : removePlayerIds) {
			stealPlayerMap.remove(removePlayerId);
		}
		return stealPlayerMap;
	}

	/**
	 * 被偷取
	 */
	@Subscribe
	public void beSteal(ResourceDefenseBeStealEvent event) {
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();

		// 玩家被偷次数达到上限
		if (entity.getBeStealTimes() >= ResourceDefenseCfg.getInstance().getPickupCoverLimit()) {
			return;
		}
		entity.setBeStealTimes(entity.getBeStealTimes() + 1);
		
		ResourceDefenseTemplate station = null;
		
		// 取对应资源站
		List<ResourceDefenseTemplate> stationInfoList = entity.getStationInfoList();
		for (ResourceDefenseTemplate stationInfo : stationInfoList) {
			if (stationInfo.getStationType() == event.getStationId()) {
				station = stationInfo;
				break;
			}
		}

		// 资源站不存在
		if (station == null) {
			return;
		}
		
		station.addResourceCount(event.getResId(), -1);
		
		station.addBeSteal(event.getTargetPlayerId());
		
		entity.notifyUpdate();
		
		// 添加记录
		Builder record = RDRecord.newBuilder();
		record.setTime(HawkTime.getMillisecond());
		record.setType(RDRecordType.STEAL);
		
		String guildId = getDataGeter().getGuildId(event.getTargetPlayerId());
		if (!HawkOSOperator.isEmptyString(guildId)) {
			String tag = getDataGeter().getGuildTag(guildId);
			record.setGuildTag(tag);
		}
		record.setName(getDataGeter().getPlayerName(event.getTargetPlayerId()));
		record.setResType(event.getResId());
		addRecord(event.getPlayerId(), record);
	}

	/**
	 * 被偷取时防御成功
	 */
	@Subscribe
	public void onDefenseSuccess(ResourceDefenseSuccessEvent event) {
		String playerId = event.getPlayerId();
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();

		// 玩家被偷次数达到上限
		if (entity.getBeStealTimes() >= ResourceDefenseCfg.getInstance().getPickupCoverLimit()) {
			return;
		}
		//被偷玩家数据修改
		entity.setBeStealTimes(entity.getBeStealTimes() + 1);

		//被偷玩家有特工
		if (entity.unlcokSuper()){
			/**添加特工防御成功的记录*/
			entity.updateAgentRecord(AGENT_RECORD_2, 1);
		}
		entity.notifyUpdate();

		boolean isOnline = this.getDataGeter().isOnlinePlayer(playerId);
		if(isOnline){
			syncPageInfo(playerId);
		}
		logger.info("onDefenseSuccess, playerId:{}, unlockSuper:{}, isOnline:{}", entity.getPlayerId(), entity.unlcokSuper(), isOnline);
	}

	/**
	 * 解锁高级奖励
	 */
	@Subscribe
	public void unlockSuper(ResourceDefenseUnlockEvent event) {
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();
		entity.setUnlcokSuper(1);
		addExp(entity, ResourceDefenseCfg.getInstance().getUnlockGiftExp(), "super");

		/**添加特工技能*/
		refreshAgentSkill(entity);
		/**添加特工生效的记录*/
		entity.initAgentRecord();
		entity.notifyUpdate();
		/**同步*/
		syncPageInfo(entity.getPlayerId());
	}

	/**
	 * 检测购买经验次数重置
	 */
	public void checkBuyExpReset(ResourceDefenseEntity entity) {
		if (!needBuyExpRefresh(entity)) {
			return;
		}

		entity.getBuyExpInfoMap().clear();
		entity.setBuyExpRefreshTime(HawkTime.getMillisecond());
		logger.info("resourceDefense, buy exp reset, playerId:{}", entity.getPlayerId());
	}

	/**
	 * 是否需要刷新购买次数
	 */
	private boolean needBuyExpRefresh(ResourceDefenseEntity entity) {
		return false;
	}

	/**
	 * 购买经验
	 */
	@SuppressWarnings("deprecation")
	public void buyExp(String playerId, int cfgId, int protoType) {
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();
		Map<Integer, Integer> buyExpInfoMap = entity.getBuyExpInfoMap();

		// 次数限制
		int buyTimes = buyExpInfoMap.getOrDefault(cfgId, 0);
		if (buyTimes >= ResourceDefenseCfg.getInstance().getPurchaseExp()) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_BUY_EXP_REQ_VALUE, Status.ResourceDefenseError.RD_BUY_EXP_TIMES_LIMIT_VALUE);
			return;
		}

		ResourceDefenseExpCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ResourceDefenseExpCfg.class, cfgId);

		// 判断消耗
		List<RewardItem.Builder> cost = RewardHelper.toRewardItemList(cfg.getPrice());
		boolean consumeResult = getDataGeter().consumeItems(playerId, cost, protoType, Action.RESOURCE_DEFENSE_BUYEXP);
		if (consumeResult == false) {
			return;
		}

		buyExpInfoMap.put(cfgId, buyTimes + 1);

		addExp(entity, cfg.getExp(), "buyExp");

		responseSuccess(playerId, HP.code.RESOURCE_DEFENSE_BUY_EXP_REQ_VALUE);
		
		syncPageInfo(playerId);
	}

	/**
	 * 跨天
	 */
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!event.isCrossDay()) {
			return;
		}

		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}

		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}

		ResourceDefenseEntity entity = opEntity.get();
		entity.setStealTimes(0);
		entity.setBeStealTimes(0);


		//特工技能召唤高级机器人数据清理
		entity.getStealRobotInfoMap().clear();
		entity.getGreatRobotInfoMap().clear();
		//特工技能刷新次数重置
		entity.setSkillRefreshTimes(0);
		entity.getGreatRobotInfoMap().clear();
		entity.setActiveSkill(0);
		entity.setFreeRefreshTimes(0);

		//刷新技能
		if (entity.unlcokSuper()){
			refreshAgentSkill(entity);
		}

		if (!playerIds.contains(playerId)) {
			this.playerIds.add(playerId);
		}
		//同步主界面信息
		syncPageInfo(playerId);
		//同步偷取界面信息
		stealPage(playerId);
	}
	
	/**
	 * 请求帮助
	 */
	public void requestHelp(String playerId, int stationId) {
		// 没有联盟不能请求帮助
		if (HawkOSOperator.isEmptyString(getDataGeter().getGuildId(playerId))) {
			return;
		}
		
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();

		ResourceDefenseTemplate station = null;
		
		// 取对应资源站
		List<ResourceDefenseTemplate> stationInfoList = entity.getStationInfoList();
		for (ResourceDefenseTemplate stationInfo : stationInfoList) {
			if (stationInfo.getStationType() == stationId) {
				station = stationInfo;
				break;
			}
		}

		// 资源站不存在
		if (station == null) {
			return;
		}
		
		// 已经请求过帮助
		if (station.getRequestHelp() != 0) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_HELP_STATION_REQ_VALUE, Status.ResourceDefenseError.RD_BE_FOR_HELP_ALREADY_VALUE);
			return;
		}
		station.setRequestHelp(1);
		
		entity.notifyUpdate();
		
		// 发送请求帮助公告
		getDataGeter().addWorldBroadcastMsg(Const.ChatType.GUILD_HREF, getDataGeter().getGuildId(playerId),
				Const.NoticeCfgId.RESOURCE_DEFENSE_HELP, playerId, playerId, stationId);
		
		responseSuccess(playerId, HP.code.RESOURCE_DEFENSE_HELP_STATION_REQ_VALUE);
		
		syncPageInfo(playerId);
	}
	
	/**
	 * 帮助
	 */
	public void help(String playerId, String targetPlayerId, int stationId) {
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(targetPlayerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();
		
		ResourceDefenseTemplate station = null;
		
		// 取对应资源站
		List<ResourceDefenseTemplate> stationInfoList = entity.getStationInfoList();
		for (ResourceDefenseTemplate stationInfo : stationInfoList) {
			if (stationInfo.getStationType() == stationId) {
				station = stationInfo;
				break;
			}
		}

		// 资源站不存在
		if (station == null) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_HELP_REQ_VALUE, Status.ResourceDefenseError.RD_RESOURCE_REFRESH_VALUE);
			return;
		}

		// 不能帮助自己
		if (playerId.equals(targetPlayerId)) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_HELP_REQ_VALUE, Status.ResourceDefenseError.RD_RESOURCE_HELP_OWN_VALUE);
			return;
		}
		
		// 次数限制
		if (station.getBeHelpList().size() >= ResourceDefenseCfg.getInstance().getBeHelpTimesLimit()) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_HELP_REQ_VALUE, Status.ResourceDefenseError.RD_BE_HELP_TIME_LIMIT_VALUE);
			return;
		}

		// 已经帮助过
		if (station.getBeHelpList().contains(playerId)) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_HELP_REQ_VALUE, Status.ResourceDefenseError.RD_HELP_ALREADY_HELP_VALUE);
			return;
		}
		
		station.addBeHelpPlayer(playerId);
		
		int stationLevel = getStationLevel(entity.getExp());
		
		// 资源站配置
		ResourceDefenseStationCfg stationCfg = getStationCfg(stationLevel, station.getStationType());
		
		if (station.getState() == RDStationState.RDSTATION_NONE_VALUE) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_HELP_REQ_VALUE, Status.ResourceDefenseError.RD_RESOURCE_REFRESH_VALUE);
			return;
		}
		
		if (station.getTickTimes() >= stationCfg.getTimes()) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_HELP_REQ_VALUE, Status.ResourceDefenseError.RD_RESOURCE_REFRESH_VALUE);
			return;
		}
		
		// 时间减少
		station.setSpeedValue(station.getSpeedValue() + ResourceDefenseCfg.getInstance().getHelpTimeReduce());
		
		entity.notifyUpdate();
		
		refreshStationState(entity);
		
		responseSuccess(playerId, HP.code.RESOURCE_DEFENSE_HELP_REQ_VALUE);
		
		// 添加记录
		Builder record = RDRecord.newBuilder();
		record.setTime(HawkTime.getMillisecond());
		record.setType(RDRecordType.HELP);
		
		String guildId = getDataGeter().getGuildId(playerId);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			String tag = getDataGeter().getGuildTag(guildId);
			record.setGuildTag(tag);
		}
		record.setStationId(stationId);
		record.setName(getDataGeter().getPlayerName(playerId));
		
		ResourceDefenseStationCfg cfg = getStationCfg(getStationLevel(entity.getExp()), station.getStationType());
		long reduceMill = cfg.getPeroid() / 10000 * ResourceDefenseCfg.getInstance().getHelpTimeReduce();
		
		record.setReduceTime((int)(reduceMill / 1000));
		record.setCurrTimes(station.getBeHelpList().size());
		record.setAllTimes(ResourceDefenseCfg.getInstance().getBeHelpTimesLimit());
		addRecord(targetPlayerId, record);
		
		if (getDataGeter().getOnlinePlayers().contains(targetPlayerId)) {
			syncPageInfo(targetPlayerId);
		}
	}
	
	/**
	 * 查看其他玩家
	 */
	public void others(String playerId, String targetPlayerId) {
		String[] playerIdArr = SerializeHelper.split(targetPlayerId, SerializeHelper.ATTRIBUTE_SPLIT);
		//如果是机器人
		if (playerIdArr.length == 2 && playerIdArr[0].equals("robot")) {
			//机器人id
			int robotId =  NumberUtils.toInt(playerIdArr[1]);
			//if (targetPlayerId.equals("robot")) {
			ResourceDefensePageInfo.Builder builder = ResourceDefensePageInfo.newBuilder();
			//机器人资源站配置
			ResourceDefenseRobotCfg robotCfg = HawkConfigManager.getInstance().getConfigByKey(ResourceDefenseRobotCfg.class, robotId);
			if (robotCfg == null){
				return;
			}
			//机器人资源站等级
			int robotStationLv = robotCfg.getLevel();
			builder.setPlayerId(targetPlayerId);
			builder.setResourceStationLevel(robotStationLv); //ResourceDefenseCfg.getInstance().getRobotLevel()
			int size = HawkConfigManager.getInstance().getConfigSize(ResourceDefenseLevelCfg.class);
			int lv = Math.min(robotStationLv + 1 , size);
			ResourceDefenseLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(ResourceDefenseLevelCfg.class, lv);
			builder.setResourceStationExp(levelCfg.getExp());
			builder.setUnlockSenior(true);

			/**
			 * 修改版本,机器人资源站的数据从ResourceDefenseRobotCfg 组装
			 */
			Map<Integer, Integer> tarResourceInfoMap = robotCfg.getResourceInfoMap();
			//剩余偷取次数
			int leftStealTimes = tarResourceInfoMap.values().stream().findFirst().get();
			if (robotCfg.getType() == Activity.PBRobotType.ROBOT_3_VALUE){
				Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
				if (opEntity.isPresent()) {
					ResourceDefenseEntity entity = opEntity.get();
					leftStealTimes = leftStealTimes - entity.getGreatRobotInfoMap().getOrDefault(robotId, 0);
				}
			}

			Map<Integer, Integer> stationTypeMap = robotCfg.getShowStationTypeMap();
			for (Map.Entry<Integer, Integer> entry: stationTypeMap.entrySet()) {
				//资源站类型
				int stationType = entry.getKey();
				//资源站是否开启
				boolean isOpen = (entry.getValue() == 1);
				RDStationInfo.Builder stationBuilder = RDStationInfo.newBuilder();
				stationBuilder.setStationId(stationType);
				// 资源站信息
				if (isOpen) {
					stationBuilder.setState(RDStationState.RDSTATION_CHARGE);
					stationBuilder.setEndTime(HawkTime.getMillisecond() + 86400 * 1000L);
					stationBuilder.setCanHelp(true);
					stationBuilder.setCanSpeedUp(true);
					stationBuilder.setCanReceive(true);
					stationBuilder.setCanBeSteal(true);
					//高级机器人特殊处理
					if (robotCfg.getType() == Activity.PBRobotType.ROBOT_3_VALUE){
						if (leftStealTimes <= 0){
							stationBuilder.setCanBeSteal(false);
							stationBuilder.setState(RDStationState.RDSTATION_DOING);
							stationBuilder.setCanReceive(false);
						}
					}
				} else {
					stationBuilder.setState(RDStationState.RDSTATION_DOING);
					stationBuilder.setEndTime(HawkTime.getMillisecond() + 86400 * 1000L);
					stationBuilder.setCanHelp(false);
					stationBuilder.setCanSpeedUp(false);
					stationBuilder.setCanReceive(false);
					stationBuilder.setCanBeSteal(false);
				}
				builder.addStationInfo(stationBuilder);
			}
			for (Entry<Integer, Integer> stationInfo : robotCfg.getResourceInfoMap().entrySet()) {
				RDResourceInfo.Builder resInfoBuilder = RDResourceInfo.newBuilder();
				resInfoBuilder.setResourceType(stationInfo.getKey());
				//剩余偷取次数
				if(robotCfg.getType() == Activity.PBRobotType.ROBOT_3_VALUE){
					resInfoBuilder.setResourceNum(leftStealTimes);
				}else{
					resInfoBuilder.setResourceNum(stationInfo.getValue());
				}
				builder.addResourceInfo(resInfoBuilder);
			}

			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RESOURCE_DEFENSE_OTHERS_RESP_VALUE, builder));
			return;
		}
		
		if (getDataGeter().isCrossPlayer(targetPlayerId)) {
			sendErrorAndBreak(playerId, HP.code.RESOURCE_DEFENSE_HELP_REQ_VALUE, Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE);
			return;
		}
		
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(targetPlayerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();

		// 刷新资源站状态
		refreshStationState(entity);

		ResourceDefensePageInfo.Builder builder = ResourceDefensePageInfo.newBuilder();
		
		// 等级、经验
		builder.setResourceStationLevel(getStationLevel(entity.getExp()));
		builder.setResourceStationExp(entity.getExp());
		
		// 资源库
		for (Entry<Integer, Integer> resInfo : getCurrentResourceInfoMap(entity).entrySet()) {
			RDResourceInfo.Builder resInfoBuilder = RDResourceInfo.newBuilder();
			resInfoBuilder.setResourceType(resInfo.getKey());
			resInfoBuilder.setResourceNum(resInfo.getValue());
			builder.addResourceInfo(resInfoBuilder);
		}
		
		// 资源站信息
		for (ResourceDefenseTemplate stationInfo : entity.getStationInfoList()) {
			RDStationInfo.Builder stationBuilder = RDStationInfo.newBuilder();
			stationBuilder.setStationId(stationInfo.getStationType());
			
			RDStationState state = RDStationState.valueOf(stationInfo.getState());
			if (state == RDStationState.RDSTATION_NONE) {
				stationBuilder.setState(state);
			} else {
				if (stationInfo.hasResource()) {
					stationBuilder.setState(state);
				} else {
					stationBuilder.setState(RDStationState.RDSTATION_DOING);
				}
			}
			
			// 资源站配置
			stationBuilder.setEndTime(stationInfo.getLastTickTime() + getStationResPeroid(entity, stationInfo));
			
			// 可被偷取次数
			stationBuilder.setCanHelp(stationInfo.getRequestHelp() == 0);
			
			stationBuilder.setCanSpeedUp(stationInfo.getSpeedValue() == 0);
			
			stationBuilder.setCanReceive(stationInfo.hasResource());
			
			stationBuilder.setCanBeSteal(stationInfo.hasResource());
			
			builder.addStationInfo(stationBuilder);
		}
		
		builder.setUnlockSenior(entity.unlcokSuper());
		
		// 任务信息
		for (int receiveMissionId : entity.getReceivedRewardIdSet()) {
			builder.addReceiveMissionId(receiveMissionId);
		}
		
		// 购买经验
		for (Entry<Integer, Integer> buyExpInfo : entity.getBuyExpInfoMap().entrySet()) {
			RDBuyExpInfo.Builder buyExpInfoBuilder = RDBuyExpInfo.newBuilder();
			buyExpInfoBuilder.setCfgId(buyExpInfo.getKey());
			buyExpInfoBuilder.setBuyTimes(buyExpInfo.getValue());
			builder.addBuyExpInfo(buyExpInfoBuilder);
		}
		
		// 偷取次数
		builder.setStealTimes(entity.getStealTimes());
		builder.setRemainStealTimes(entity.getCanStealTimes());
		
		List<Builder> records = getRecord(targetPlayerId, ResourceDefenseCfg.getInstance().getRecordLimit(), RDRecordType.STEAL, RDRecordType.STEAL_FAILE);
		for (Builder record : records) {
			builder.addRecord(record);
		}
 		
		builder.setPlayerId(targetPlayerId);
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RESOURCE_DEFENSE_OTHERS_RESP_VALUE, builder));
	}
	
	/**
	 * 获取资源站生产资源周期
	 */
	public long getStationResPeroid(ResourceDefenseEntity entity, ResourceDefenseTemplate station) {
		int speedValue = station.getSpeedValue();
		ResourceDefenseStationCfg cfg = getStationCfg(getStationLevel(entity.getExp()), station.getStationType());
		long peroid = cfg.getPeroid() - cfg.getPeroid() * speedValue / 10000;
		return Math.max(0L, peroid);
	}

	/**
	 * redis的key
	 */
	public String getRecordKey(String playerId) {
		return String.format(ActivityRedisKey.RESOURCE_DEFENSE_RECORD, getActivityTermId(), playerId);
	}
	
	/**
	 * 添加记录
	 */
	public void addRecord(String playerId, Builder builder) {
		HawkRedisSession redis = ActivityGlobalRedis.getInstance().getRedisSession();
		redis.lPush(getRecordKey(playerId).getBytes(), 3600 * 24 *3, builder.build().toByteArray());
	}


	/**
	 * 获取记录
	 */
	public List<Builder> getRecord(String playerId, int maxCount, RDRecordType... recordTypes) {

		List<Builder> builderList = new ArrayList<>();
		try {
			HawkRedisSession redis = ActivityGlobalRedis.getInstance().getRedisSession();
			List<byte[]> infoList = redis.lRange(getRecordKey(playerId).getBytes(), 0, maxCount - 1, 3600 * 24 *3);
			if (infoList != null) {
				for (byte[] info : infoList) {
					Builder builder = RDRecord.newBuilder();
					builder.mergeFrom(info);
					
					if (recordTypes == null || recordTypes.length <= 0) {
						builderList.add(builder);
						
					} else {
						boolean add = false;
						for (RDRecordType type : recordTypes) {
							if (type.equals(builder.getType())) {
								add = true;
							}
						}
						if (add) {
							builderList.add(builder);
						}
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return builderList;
	}
	
	/**
	 * 检测偷取次数
	 */
	public void checkStealTimes(String playerId) {
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();
		
		// 配置
		ResourceDefenseCfg cfg = ResourceDefenseCfg.getInstance();
		
		// 当前时间
		long current = HawkTime.getMillisecond();
		
		// 周期增加
		long lastTickTime = entity.getStealTimesTick();
		if (lastTickTime != 0L) {
			int addTimes = (int)((current - lastTickTime) / cfg.getStealAddPeriod());
			if (addTimes > 0) {
				addCanStealTimes(entity, addTimes, false);
				entity.setStealTimesTick(lastTickTime + cfg.getStealAddPeriod() * addTimes);
			}
		} else {
			entity.setStealTimesTick(current);
		}
		
		// 跨天增加
		long stealTimesZeroTick = entity.getStealTimesZeroTick();
		if (stealTimesZeroTick == 0L || !HawkTime.isSameDay(stealTimesZeroTick, HawkTime.getMillisecond())) {
			addCanStealTimes(entity, cfg.getStealZeroTimeAdd(), true);
			entity.setStealTimesZeroTick(current);
		}
	}
	
	/**
	 * 添加可偷取次数
	 */
	public void addCanStealTimes(ResourceDefenseEntity entity, int add, boolean crossDay) {
		int max = ResourceDefenseCfg.getInstance().getStealTimesMax();
		int canStealTimes = entity.getCanStealTimes();
		int afterStealTimes = Math.min(canStealTimes + add, max);
		entity.setCanStealTimes(afterStealTimes);
		logger.info("addCanStealTimes, playerId:{}, crossDay:{}, canStealTimes:{}, afterStealTimes:{}",
				entity.getPlayerId(), crossDay, canStealTimes, afterStealTimes);
	}


	/**
	 * 激活高级机器人
	 */
	public void onActiveGreatRobot(String playerId, int skillId, int protoType){
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();
		//未激活特工技能
		if (!entity.isActiveSkill()){
			sendErrorAndBreak(playerId, protoType, Status.ResourceDefenseError.RD_SKILL_IS_NO_ACTIVE_VALUE);
			return;
		}
		HawkTuple2<Integer, Integer> agentGreatRobot = triggerAgentSkillValue(entity, Activity.PBAgentSkillType.STEAL_GREATE_ROBOT);
		//有技能,没次数,则是次数已经用尽
		if (agentGreatRobot.first > 0 && agentGreatRobot.second <= 0){
			sendErrorAndBreak(playerId, protoType, Status.ResourceDefenseError.RD_SKILL_USE_TIME_LIMIT_VALUE);
			return;
		}
		//没技能,则是未激活
		if (agentGreatRobot.first <= 0 ){
			sendErrorAndBreak(playerId, protoType, Status.ResourceDefenseError.RD_SKILL_ROBOT_NO_EXIST_VALUE);
			return;
		}
		long now = HawkTime.getMillisecond();
		if(now <= entity.getCdTime()){
			sendErrorAndBreak(playerId, protoType, Status.ResourceDefenseError.RD_GREAT_ROBOT_CD_TIME_VALUE);
			return;
		}
		entity.setCdTime(now + TimeUnit.SECONDS.toMillis(3));
		//更新超级机器人数据
		int robotId = ResourceDefenseRobotCfg.getGreatRobotId();
		Map<Integer, Integer> greatRobotInfoMap = entity.getGreatRobotInfoMap();
		greatRobotInfoMap.put(robotId, 0);
		//技能使用的次数更新
		entity.addAgentSkillUseTimes(skillId);
		entity.notifyUpdate();
		//高级机器人激活后 push给客户端 让客户端增量添加到他的缓存中
		RDSRSingleInfo.Builder singleInfoBuilder = genRobotInfoPB(robotId, 0);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RESOURCE_DEFENSE_GREAT_ROBOT_ADD_RESP_VALUE, singleInfoBuilder));

		logger.info("ResourceDefenseActivity onActiveGreatRobot success, playerId:{}, skillId:{}, robotId:{}",playerId,skillId, robotId );
		responseSuccess(playerId, protoType);

		//push
		syncPageInfo(playerId);

	}

	/**
	 * 随机特工的技能请求
	 */
	public void onRefreshAgentSkill(String playerId, int protoType){
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();
		if (entity.isActiveSkill()){
			sendErrorAndBreak(playerId, protoType, Status.ResourceDefenseError.RD_SKILL_IS_ACTIVE_VALUE);
			return;
		}
		/**消耗的判断*/
		ResourceDefenseAgentCfg agentCfg = HawkConfigManager.getInstance().getConfigByIndex(ResourceDefenseAgentCfg.class, 0);
		//每日免费刷新次数
		int dayFreeTimes = entity.getFreeRefreshTimes();
		ResourceDefenseCfg cfg = HawkConfigManager.getInstance().getKVInstance(ResourceDefenseCfg.class);
		//剩余免费刷新次数
		int leftFreeRefreshTimes = Math.max((cfg.getAbilityfreeFreshNum() - dayFreeTimes), 0);
		//消耗物品刷新
		if (leftFreeRefreshTimes <= 0){
			/**每天的刷新次数*/
			int dayRefreshTime = agentCfg.getRefreshNumLimit();
			if (entity.getSkillRefreshTimes() >= dayRefreshTime){
				sendErrorAndBreak(playerId, protoType, Status.ResourceDefenseError.RD_SKILL_REFRESH_TIME_LIMIT_VALUE);
				return;
			}
			// 判断消耗
			List<RewardItem.Builder> cost = agentCfg.getRefreshCostItem();
			boolean consumeResult = getDataGeter().consumeItems(playerId, cost, protoType, Action.RESOURCE_DEFENSE_REFRESH_SKILL);
			if (consumeResult == false) {
				return;
			}
			entity.addSkillRefreshTimes();
		}else {//每日免费刷新
			//增加每日刷新次数1次
			int beforeTimes = entity.getFreeRefreshTimes();
			entity.setFreeRefreshTimes(Math.max((beforeTimes + 1), 0));
		}
		//刷新技能
		refreshAgentSkill(entity);
		entity.notifyUpdate();

		//tlog 记录
		Map<Integer, Integer> skillMap = entity.getAgentSkillInfoMap();
		this.getDataGeter().logResourceDefenseSkillRefreshAndActive(playerId,getActivityTermId(),2, SerializeHelper.collectionToString(skillMap.keySet(),SerializeHelper.ATTRIBUTE_SPLIT));
		//返回成功
		responseSuccess(playerId, protoType);

		logger.info("ResourceDefenseActivity onRefreshAgentSkill success, playerId:{}, dayFreeTimes:{},leftFreeRefreshTimes:{},dayRefreshTimes:{}",
				playerId,dayFreeTimes, leftFreeRefreshTimes,entity.getSkillRefreshTimes());
		//push
		syncPageInfo(playerId);
	}
	/**
	 * 随机特工的技能
	 */
	public void refreshAgentSkill(ResourceDefenseEntity entity){
		ResourceDefenseAgentCfg agentCfg = HawkConfigManager.getInstance().getConfigByIndex(ResourceDefenseAgentCfg.class, 0);
		//随机到的组别
		int randomGroup = HawkRand.randomWeightObject(agentCfg.getAbilityRefreshMap());

		ResourceDefenseAbilityCfg cfg = HawkConfigManager.getInstance().getConfigByIndex(ResourceDefenseAbilityCfg.class,0);
		Map<Integer, Map<Integer, Integer>> groupMap = cfg.getGroupMap();

		Map<Integer, Integer> map = groupMap.getOrDefault(randomGroup, new HashMap<>());
		//组别中随机出来的3个id
		List<Integer> idList = HawkRand.randomWeightObject(new ArrayList<>(map.keySet()),new ArrayList<>(map.values()), 3);
		Map<Integer, Integer> skillMap = new HashMap<>();
		for (Integer skillId:idList) {
			skillMap.put(skillId, 0);
		}
		entity.setAgentSkillInfoMap(skillMap);
		entity.notifyUpdate();
		logger.info("ResourceDefenseActivity refreshAgentSkill success, playerId:{}, skillIds:{}",entity.getPlayerId(), idList);
	}

	/**
	 * 激活特工技能
	 */
	public void onActiveAgentSkill(String playerId, int protoType){
		Optional<ResourceDefenseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ResourceDefenseEntity entity = opEntity.get();
		//没有可激活的技能
		if (entity.getAgentSkillInfoMap().isEmpty()){
			sendErrorAndBreak(playerId, protoType, Status.ResourceDefenseError.RD_NO_SKILL_ACTIVE_VALUE);
			return;
		}
		//技能已经激活
		if (entity.isActiveSkill()){
			sendErrorAndBreak(playerId, protoType, Status.ResourceDefenseError.RD_SKILL_IS_ACTIVE_VALUE);
			return;
		}
		entity.setActiveSkill(1);
		entity.notifyUpdate();

		//tlog 记录
		Map<Integer, Integer> skillMap = entity.getAgentSkillInfoMap();
		this.getDataGeter().logResourceDefenseSkillRefreshAndActive(playerId, getActivityTermId(),1,SerializeHelper.collectionToString(skillMap.keySet(), SerializeHelper.ATTRIBUTE_SPLIT));
		//返回成功
		responseSuccess(playerId, protoType);
		logger.info("ResourceDefenseActivity onActiveAgentSkill success, playerId:{}",playerId);

		//push
		syncPageInfo(playerId);

	}


	/**
	 * 触发特工技能作用值  0代表没有,或是触发失败
	 * @param entity
	 * @param type
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HawkTuple2<Integer, Integer> triggerAgentSkillValue(ResourceDefenseEntity entity, Activity.PBAgentSkillType type){
		if (!entity.isActiveSkill()){
			return new HawkTuple2(0,0);
		}
		/**玩家可用的技能的技能*/
		int canUseSkillId = getCanUseSkillId(entity, type);
		if(canUseSkillId == 0){
			return new HawkTuple2(0,0);
		}
		/**技能作用值*/
		int effectValue = getAgentSkillValue(canUseSkillId);
		//tlog 特工能力 生效
		this.getDataGeter().logResourceDefenseAgentSkillEffect(entity.getPlayerId(), getActivityTermId(), canUseSkillId);
		return new HawkTuple2(canUseSkillId,effectValue);
	}

	/**
	 * 此类型,玩家可用的技能id
	 * @param entity
	 * @param skillType
	 * @return
	 */
	public int getCanUseSkillId(ResourceDefenseEntity entity, Activity.PBAgentSkillType skillType){
		Map<Integer, Integer>  skillMap = entity.getAgentSkillInfoMap();
		for (Entry<Integer, Integer> entry :skillMap.entrySet()) {
			int skillId = entry.getKey();
			int useTimes = entry.getValue();
			ResourceDefenseAbilityCfg abilityCfg = HawkConfigManager.getInstance().getConfigByKey(ResourceDefenseAbilityCfg.class,skillId);
			int type = abilityCfg.getSkillType();
			if (skillType.getNumber() != type){
				continue;
			}
			int limitTimes = abilityCfg.getEffectNum();
			//达到次数限制跳过
			if (useTimes >= limitTimes){
				continue;
			}
			return skillId;
		}
		return 0;
	}

	/**
	 * 触发特工技能
	 * 1.多偷取1个
	 * 2.偷取概率提高
	 * 3.刷出特殊机器人
	 * 4.偷取获得额外经验 双倍经验
	 * 5.偷取失败返还次数 返还当前消耗的次数
	 */
	public int getAgentSkillValue(int skillId){
		ResourceDefenseAbilityCfg abilityCfg = HawkConfigManager.getInstance().getConfigByKey(ResourceDefenseAbilityCfg.class,skillId);
		int skillType = abilityCfg.getSkillType();
		switch (skillType){
			case Activity.PBAgentSkillType.STEAL_ADD_NUM_VALUE:
			case  Activity.PBAgentSkillType.STEAL_ADD_EXP_VALUE:
				int rand = HawkRand.randInt(GameConst.RANDOM_MYRIABIT_BASE);
				if (rand < abilityCfg.getPara()){
					return 1;
				}
				break;
			case Activity.PBAgentSkillType.STEAL_ADD_RATE_VALUE:
				return abilityCfg.getPara();
			case Activity.PBAgentSkillType.STEAL_GREATE_ROBOT_VALUE:
			case Activity.PBAgentSkillType.STEAL_FAIL_GIVE_NUM_VALUE:
				return 1;
			default:
				break;
		}
		return 0;
	}


	/**
	 * 随机出出的机器人id 只针对1 和 2 类型,,,3高级,特殊处理(特工触发)
	 */
	public List<Integer> getRandomRobotIdList(){
		Map<Integer, Integer> robotRefreshMap = ResourceDefenseCfg.getInstance().getRobotRefreshWeightMap();
		//先随机类型
		int robotType = HawkRand.randomWeightObject(robotRefreshMap);
		int robotNum =  ResourceDefenseCfg.getInstance().getRobotShowNumMap().get(robotType);
		Map<Integer, Integer> robotWeightMap = ResourceDefenseRobotCfg.getTypeIdWeightMapByType(robotType);
		//从类型中随机个数
		List<Integer> robotIdList = HawkRand.randomWeightObject(new ArrayList<>(robotWeightMap.keySet()),new ArrayList<>(robotWeightMap.values()), robotNum);
		return robotIdList;
	}

	/**
	 * 机器人构造信息PB
	 * 如果有高级机器人,则将剩余次数传进来
	 * @param robotId
	 * @param greatRobotStealTimes 高级机器人资源站已经被偷取次数
	 * @return
	 */
 	public RDSRSingleInfo.Builder genRobotInfoPB(int robotId,int greatRobotStealTimes){
		RDSRSingleInfo.Builder singleInfo = RDSRSingleInfo.newBuilder();
		singleInfo.setPlayerId("robot_" + robotId);
		ResourceDefenseRobotCfg robotCfg = HawkConfigManager.getInstance().getConfigByKey(ResourceDefenseRobotCfg.class, robotId);
		singleInfo.setStationLevel(robotCfg.getLevel());

		for (Entry<Integer, Integer> currentResource :robotCfg.getResourceInfoMap().entrySet()) {
			RDResourceInfo.Builder rdBuilder = RDResourceInfo.newBuilder();
			rdBuilder.setResourceType(currentResource.getKey());
			if (ResourceDefenseRobotCfg.getGreatRobotId() == robotId){
				int leftResourceNum = Math.max((currentResource.getValue() - greatRobotStealTimes), 0);
				rdBuilder.setResourceNum(leftResourceNum);
			}else {
				rdBuilder.setResourceNum(currentResource.getValue());
			}
			singleInfo.addResourceInfo(rdBuilder);
		}
		return singleInfo;
	}

	/**
	 * 特工信息构建pb
	 * @param entity
	 * @return
	 */
	public Activity.PBAgentPageInfo.Builder genAgentInfoPB(ResourceDefenseEntity entity){
		Activity.PBAgentPageInfo.Builder pbAgentBuilder = Activity.PBAgentPageInfo.newBuilder();
		ResourceDefenseAgentCfg agentCfg = HawkConfigManager.getInstance().getConfigByIndex(ResourceDefenseAgentCfg.class, 0);
		int leftTimes = agentCfg.getRefreshNumLimit() - entity.getSkillRefreshTimes();
		//剩余次数
		pbAgentBuilder.setRemainRefreshTimes(Math.max(leftTimes, 0));
		//剩余免费次数
		ResourceDefenseCfg cfg = HawkConfigManager.getInstance().getKVInstance(ResourceDefenseCfg.class);
		int skillFreeRefreshTimes = cfg.getAbilityfreeFreshNum() - entity.getFreeRefreshTimes();
		pbAgentBuilder.setFreeRefreshTimes(Math.max(skillFreeRefreshTimes, 0));
		//特工技能是否激活
		pbAgentBuilder.setAgentActiveSkill(entity.isActiveSkill());
		//特工技能使用数据
		Map<Integer, Integer> agentSkillMap = entity.getAgentSkillInfoMap();
		for (Entry<Integer, Integer> entry : agentSkillMap.entrySet()) {
			Activity.PBAgentInfo.Builder pbAgentInfo = Activity.PBAgentInfo.newBuilder();
			pbAgentInfo.setAgentSkillId(entry.getKey());
			pbAgentInfo.setUseSkillNum(entry.getValue());
			pbAgentBuilder.addPbAgentInfo(pbAgentInfo);
		}
		//特工履历信息
		if (!entity.getAgentRecordList().isEmpty()){
			Activity.PBAgentRecord.Builder pbAgentRecord = Activity.PBAgentRecord.newBuilder();
			pbAgentRecord.setStartTime(entity.getAgentRecord(0));
			pbAgentRecord.setDefenseTimes((int)entity.getAgentRecord(1));
			pbAgentRecord.setStealTimes((int) entity.getAgentRecord(2));
			pbAgentBuilder.setPbAgentRecord(pbAgentRecord);
		}
		return pbAgentBuilder;
	}
}