package com.hawk.activity.type.impl.allianceCarnival;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.CreateGuildEvent;
import com.hawk.activity.event.impl.JoinGuildEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.allianceCarnival.cfg.AllianceCarnivalAchieveCfg;
import com.hawk.activity.type.impl.allianceCarnival.cfg.AllianceCarnivalCfg;
import com.hawk.activity.type.impl.allianceCarnival.cfg.AllianceCarnivalLevelCfg;
import com.hawk.activity.type.impl.allianceCarnival.entity.ACBInfo;
import com.hawk.activity.type.impl.allianceCarnival.entity.ACMInfo;
import com.hawk.activity.type.impl.allianceCarnival.entity.AllianceCarnivalEntity;
import com.hawk.activity.type.impl.allianceCarnival.rank.ACRankInfo;
import com.hawk.activity.type.impl.allianceCarnival.rank.ACRankObj;
import com.hawk.game.protocol.Activity.ACExpAdd;
import com.hawk.game.protocol.Activity.ACMissionInfo;
import com.hawk.game.protocol.Activity.ACMissionState;
import com.hawk.game.protocol.Activity.ACRankInfoMsg;
import com.hawk.game.protocol.Activity.ACRankMsg;
import com.hawk.game.protocol.Activity.AllianceCarnivalPageInfo;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/**
 * 联盟总动员
 * @author golden
 *
 */
public class AllianceCarnivalActivity extends ActivityBase implements AchieveProvider {

	/**
	 * 日志
	 */
	public static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 联盟总动员任务信息
	 */
	public static final String ACM_INFO = "acm_info:%s:%s";
	
	/**
	 * 联盟总动员基础信息
	 */
	public static final String ACB_INFO = "acb_info:%s:%s";
	
	/**
	 * redis key 过期时间
	 */
	public static final int EXPIRESECONDS = 3600 * 24 * 30;

	/**
	 * 所有联盟，总动员基础信息
	 */
	public static Map<String, ACBInfo> baseInfos = new ConcurrentHashMap<>();

	/**
	 * 所有联盟任务集合 guildId,missions
	 */
	public static Map<String, Map<String, ACMInfo>> guildMissions = new ConcurrentHashMap<>();

	/**
	 * 排行榜
	 */
	public static ACRankObj rankObj = new ACRankObj();

	/**
	 * 是否经过初始化
	 */
	private boolean isInit;
	
	/**
	 * 构造
	 */
	public AllianceCarnivalActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	/**
	 * 活动类型
	 */
	@Override
	public ActivityType getActivityType() {
		return ActivityType.ALLIANCE_CARNIVAL;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		AllianceCarnivalActivity activity = new AllianceCarnivalActivity(config.getActivityId(), activityEntity);
		// 加入成就管理器
		AchieveContext.registeProvider(activity);
		return activity;
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
		guildMissions = new ConcurrentHashMap<>();
		baseInfos = new ConcurrentHashMap<>();
		String sql = String.format("select id from guild_info where invalid = 0");
		List<String> guildIds = HawkDBManager.getInstance().executeQuery(sql, null);
		do {
			if (guildIds == null || guildIds.isEmpty()) {
				break;
			}
			
			for (String guildId : guildIds) {
				try {
					loadACBInfo(guildId);
					loadACMInfo(guildId);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		} while (false);

		// 初始化排行榜
		rankObj.init(getActivityTermId());
		isInit = true;
	}

	/**
	 * 活动开启
	 */
	@Override
	public void onOpen() {
	}

	public void onHidden() {
		rankObj = new ACRankObj();
		baseInfos = new ConcurrentHashMap<>();
		guildMissions = new ConcurrentHashMap<>();
	}

	/**
	 * 加载联盟总动员基础信息
	 */
	public void loadACBInfo(String guildId) {
		String acbKey = getACBKey(getActivityTermId(), guildId);
		String info = ActivityGlobalRedis.getInstance().get(acbKey);
		if (info == null) {
			return;
		}
		baseInfos.put(guildId, new ACBInfo(info, true));
		logger.info("allianceCarnival load baseInfo, guildId:{}, info:{}", guildId, info);
	}

	/**
	 * 加载联盟总动员任务信息
	 */
	public void loadACMInfo(String guildId) {
		String acmKey = getACMKey(getActivityTermId(), guildId);
		Map<String, String> rsMissions = ActivityGlobalRedis.getInstance().hgetAll(acmKey);
		if (rsMissions == null || rsMissions.isEmpty()) {
			return;
		}

		// 遍历加入缓存中
		Map<String, ACMInfo> missions = new ConcurrentHashMap<>();
		for (Entry<String, String> info : rsMissions.entrySet()) {
			missions.put(info.getKey(), new ACMInfo(info.getValue()));
		}
		guildMissions.put(guildId, missions);
		logger.info("allianceCarnival load missions, guildId:{}", guildId);
	}

	/**
	 * 检测联盟任务
	 */
	public Map<String, ACMInfo> checkAndGetGuildMissions(String guildId) {
		Map<String, ACMInfo> missions = checkAndGetGuildMissions(guildId, 0);
		return missions;
	}

	/**
	 * 检测联盟任务
	 */
	public Map<String, ACMInfo> checkAndGetGuildMissions(String guildId, int beforeMissionId) {
		Map<String, ACMInfo> missions = guildMissions.get(guildId);
		if (missions == null) {
			missions = new ConcurrentHashMap<>();
			guildMissions.put(guildId, missions);
		}

		// 如果是删除/接受之前的任务而刷新出来的任务，则有cd
		long delayTime = 0;
		AllianceCarnivalAchieveCfg beforeTask = HawkConfigManager.getInstance().getConfigByKey(AllianceCarnivalAchieveCfg.class, beforeMissionId);
		if (beforeMissionId != 0 && beforeTask != null) {
			delayTime = beforeTask.getRefreshDelay();
		}

		int refreshCount = AllianceCarnivalCfg.getInstance().getRefreshCount() - missions.size();
		for (int i = 0; i < refreshCount; i++) {
			AllianceCarnivalAchieveCfg task = randomTask();
			ACMInfo acmInfo = new ACMInfo(task, delayTime);
			missions.put(acmInfo.getUuid(), acmInfo);
			logger.info("allianceCarnival refreshMission, guildId:{}, beforeMissionId:{}, refreshMission:{}, afterMissionCount:{}", guildId, beforeMissionId, acmInfo.toString(),
					missions.size());
		}
		return missions;
	}

	/**
	 * 检测联盟基础信息
	 */
	public ACBInfo checkAndGetGuildBaseInfo(String guildId) {
		if (!isOpening(null)) {
			return new ACBInfo(guildId);
		}

		ACBInfo acbInfo = baseInfos.get(guildId);
		if (acbInfo != null) {
			return acbInfo;
		}
		acbInfo = new ACBInfo(guildId);
		baseInfos.put(guildId, acbInfo);
		logger.info("allianceCarnival checkAndGetGuildBaseInfo, guildId:{}", guildId);
		return acbInfo;
	}

	/**
	 * redis的key
	 */
	public String getACMKey(int termId, String guildId) {
		return String.format(ACM_INFO, termId, guildId);
	}

	/**
	 * redis的key
	 */
	public String getACBKey(int termId, String guildId) {
		return String.format(ACB_INFO, termId, guildId);
	}

	/**
	 * 从db拉取玩家数据
	 */
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<AllianceCarnivalEntity> queryList = HawkDBManager.getInstance().query("from AllianceCarnivalEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	/**
	 * 创建玩家实体
	 */
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		if (isShowing(playerId)) {
			return null;
		}
		
		AllianceCarnivalEntity entity = new AllianceCarnivalEntity();
		entity.setPlayerId(playerId);
		entity.setTermId(termId);
		String initGuildId = getDataGeter().getGuildId(playerId);
		entity.setInitGuildId(initGuildId == null ? "" : initGuildId);
		String guildName = getDataGeter().getGuildName(initGuildId);
		entity.setInitGuildName(guildName == null ? "" : guildName);
		entity.setBuyTime(HawkTime.getNextAM0Date());
		// 设置初始大本等级
		int cityLevel = getDataGeter().getConstructionFactoryLevel(playerId);
		entity.setInitCityLevel(cityLevel);
		if (!HawkOSOperator.isEmptyString(initGuildId)) {
			reissueRewardMail(playerId, initGuildId);
		}
		
		return entity;
	}

	/**
	 * 随机任务
	 */
	public AllianceCarnivalAchieveCfg randomTask() {
		Map<AllianceCarnivalAchieveCfg, Integer> multiWeightMap = new HashMap<>();
		ConfigIterator<AllianceCarnivalAchieveCfg> its = HawkConfigManager.getInstance().getConfigIterator(AllianceCarnivalAchieveCfg.class);
		for (AllianceCarnivalAchieveCfg cfg : its) {
			multiWeightMap.put(cfg, cfg.getRefreshWeight());
		}

		AllianceCarnivalAchieveCfg cfg = HawkRand.randomWeightObject(multiWeightMap);
		return cfg;
	}

	/**
	 * 玩家登录
	 */
	@Override
	public void onPlayerLogin(String playerId) {
		if (!isOpening(null)) {
			return;
		}

		String guildId = getDataGeter().getGuildId(playerId);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			checkAndGetGuildMissions(guildId);
			checkAndGetGuildBaseInfo(guildId);
		}
		sync(playerId);
	}

	/**
	 * 创建联盟
	 */
	@Subscribe
	public void onCreateGuildEvent(CreateGuildEvent event) {
		String guildId = event.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		checkAndGetGuildMissions(guildId);
		checkAndGetGuildBaseInfo(guildId);
		checkAndSendAward(guildId);
	}

	/**
	 * 加入联盟
	 */
	@Subscribe
	public void onJoinGuildEvent(JoinGuildEvent event) {
		String playerId = event.getPlayerId();
		if (HawkOSOperator.isEmptyString(playerId)) {
			return;
		}

		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		checkAndGetGuildMissions(guildId);
		checkAndGetGuildBaseInfo(guildId);
		checkAndSendAward(guildId);
	}
	
	/**
	 * 停服
	 */
	@Override
	public void shutdown() {
		logger.info("allianceCarnival shutdown start");
		int termId = getActivityTermId();
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		for (Entry<String, Map<String, ACMInfo>> guildMission : guildMissions.entrySet()) {
			try {
				String guildId = guildMission.getKey();
				Map<String, String> setMap = new HashMap<>();
				Collection<ACMInfo> missions = guildMission.getValue().values();
				for (ACMInfo mission : missions) {
					setMap.put(mission.getUuid(), mission.toString());
				}

				redisSession.hmSet(getACMKey(termId, guildId), setMap, EXPIRESECONDS);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		for (Entry<String, ACBInfo> baseInfo : baseInfos.entrySet()) {
			redisSession.setString(getACBKey(termId, baseInfo.getKey()), baseInfo.getValue().toString(), EXPIRESECONDS);
		}
		logger.info("allianceCarnival shutdown finish");
	}

	/**
	 * 玩家成就是否激活
	 */
	@Override
	public boolean isProviderActive(String playerId) {
		if (!isOpening(null)) {
			return false;
		}

		Optional<AllianceCarnivalEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		AllianceCarnivalEntity entity = opEntity.get();
		List<AchieveItem> achieves = entity.getAchieve();
		if (achieves == null || achieves.isEmpty()) {
			return false;
		}

		if (achieves.size() > 1) {
			return true;
		}
		boolean outOfData = achieveOutOfData(entity.getReceiveMissionTime(), achieves.get(0).getAchieveId());
		return !outOfData;
	}

	/**
	 * 成就是否同步
	 */
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return true;
	}

	/**
	 * 获取成就数据项
	 */
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<AllianceCarnivalEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		AllianceCarnivalEntity playerDataEntity = opPlayerDataEntity.get();
		AchieveItems items = new AchieveItems(playerDataEntity.getAchieve(), playerDataEntity);
		return Optional.of(items);
	}

	/**
	 * 获取成就配置
	 */
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(AllianceCarnivalAchieveCfg.class, achieveId);
	}

	/**
	 * 领奖行为
	 */
	@Override
	public Action takeRewardAction() {
		return Action.ALLIANCE_CARNIVAL_AWARD;
	}

	/**
	 * 同步数据
	 */
	public void sync(String playerId) {
		if (isShowing(playerId)) {
			AllianceCarnivalPageInfo.Builder builder = AllianceCarnivalPageInfo.newBuilder();
			String guildId = getDataGeter().getGuildId(playerId);
			guildId = guildId == null ? "" : guildId;
			builder.setInitGuildId(guildId);
			
			pushToPlayer(playerId, HP.code.ALLIANCE_CARNIVAL_PAGE_INFO_PUSH_VALUE, builder);
			return;	
		}
		
		Optional<AllianceCarnivalEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		AllianceCarnivalEntity entity = opEntity.get();
		AllianceCarnivalPageInfo.Builder builder = AllianceCarnivalPageInfo.newBuilder();
		String initGuildId = entity.getInitGuildId() == null ? "" : entity.getInitGuildId();
		builder.setInitGuildId(initGuildId);
		
		String guildName = getDataGeter().getGuildName(initGuildId);
		guildName = guildName == null ? "" : guildName;
		builder.setInitGuildName(guildName);
		builder.setInitCityLevel(entity.getInitCityLevel());
		if (getDataGeter().isCrossPlayer(playerId)) {
			pushToPlayer(playerId, HP.code.ALLIANCE_CARNIVAL_PAGE_INFO_PUSH_VALUE, builder);
			return;
		}

		// 玩家当前所在联盟
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			pushToPlayer(playerId, HP.code.ALLIANCE_CARNIVAL_PAGE_INFO_PUSH_VALUE, builder);
			return;
		}

		ACBInfo acbInfo = checkAndGetGuildBaseInfo(guildId);
		builder.setLevel(acbInfo.getCurrentLevel());
		builder.setExp(acbInfo.getCurrentExp());
		builder.setUnlockCount(acbInfo.getJoinCount());
		builder.setReceiveTimes(entity.getReceiveTimes());
		builder.setBuyTimes(entity.getBuyTimes());
		builder.setPlayerExp(entity.getExp());
		builder.setExchangeTimes(entity.getExchangeNumber());
		builder.setDayBuyNumber(entity.getDayBuyNumber());
		builder.setPayGift(entity.getPayGiftTime() > 0);
		builder.setAllianceExp(acbInfo.getExp());
		builder.setPLevel(getCurrentPLevel(entity));
		builder.setPExp(getCurrentPExp(entity));

		// 联盟所有任务
		Map<String, ACMInfo> guildMission = checkAndGetGuildMissions(guildId);
		for (ACMInfo info : guildMission.values()) {
			ACMissionInfo.Builder missionInfo = ACMissionInfo.newBuilder();
			missionInfo.setState(info.getState());
			missionInfo.setUuid(info.getUuid());
			missionInfo.setMissionId(info.getAchieveId());
			missionInfo.setEndTime(info.getRefreshTime());
			builder.addMission(missionInfo);
		}

		// 玩家已经接受的任务
		List<AchieveItem> receivedAchieve = entity.getAchieve();
		if (receivedAchieve != null && !receivedAchieve.isEmpty()) {
			AchieveItem achieve = receivedAchieve.get(0);
			ACMissionInfo.Builder missionInfo = ACMissionInfo.newBuilder();
			missionInfo.setState(ACMissionState.AC_RECEIVED);
			missionInfo.setMissionId(achieve.getAchieveId());
			long receiveTime = entity.getReceiveMissionTime();
			AllianceCarnivalAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceCarnivalAchieveCfg.class, achieve.getAchieveId());
			missionInfo.setEndTime(receiveTime + cfg.getTimeLimit());
			builder.addMission(missionInfo);
		}

		pushToPlayer(playerId, HP.code.ALLIANCE_CARNIVAL_PAGE_INFO_PUSH_VALUE, builder);
	}

	/**
	 * 接收任务
	 */
	public void receive(String playerId, String uuid) {
		if (getDataGeter().isCrossPlayer(playerId)) {
			return;
		}

		Optional<AllianceCarnivalEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}

		AllianceCarnivalEntity entity = opPlayerDataEntity.get();
		// 判断是否有联盟
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_RECEIVE_MISSION_REQ_VALUE, Status.AllianceCarnivalError.AC_HAVE_NO_ALLIANCE_VALUE);
			return;
		}

		// 没有初始联盟
		if (HawkOSOperator.isEmptyString(entity.getInitGuildId())) {
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_RECEIVE_MISSION_REQ_VALUE, Status.AllianceCarnivalError.AC_NEW_PLAYER_LIMIT_VALUE);
		}

		// 和起始联盟不同
		if (!entity.getInitGuildId().equals(guildId)) {
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_RECEIVE_MISSION_REQ_VALUE, Status.AllianceCarnivalError.AC_INITGUILD_NOT_SAME_VALUE);
			return;
		}

		// 判断是否有任务
		Map<String, ACMInfo> missions = checkAndGetGuildMissions(guildId);
		if (!missions.containsKey(uuid)) {
			sync(playerId);
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_RECEIVE_MISSION_REQ_VALUE, Status.AllianceCarnivalError.AC_MISSION_NOT_FOUND_VALUE);
			return;
		}

		// 接受任务次数拦截
		if (entity.getReceiveTimes() >= entity.getBuyTimes() + AllianceCarnivalCfg.getInstance().getDefaultReceiveTime()) {
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_RECEIVE_MISSION_REQ_VALUE, Status.AllianceCarnivalError.AC_RECEIVE_TIMES_ERROR_VALUE);
			return;
		}

		// 之前已经有的成就，直接删除掉
		Optional<AchieveItems> achieveItems = getAchieveItems(playerId);
		if (achieveItems.isPresent() && !achieveItems.get().getItems().isEmpty()) {
			logger.warn("allianceCarnival receive, clear before, playerId:{}, items:{}", playerId, achieveItems.get().getItems().toString());
			entity.clearAchieve();
		}

		// 玩家接收新的任务
		AchieveItem achieve = AchieveItem.valueOf(missions.get(uuid).getAchieveId());
		entity.addAchieve(achieve);
		entity.setReceiveMissionTime(HawkTime.getMillisecond());
		entity.setReceiveTimes(entity.getReceiveTimes() + 1);

		// 移除之前的任务
		ACMInfo acmInfo = missions.get(uuid);
		acmInfo.remove(guildId);
		missions.remove(uuid);

		// 联盟刷新出新的任务
		checkAndGetGuildMissions(guildId, achieve.getAchieveId());
		// 通用成功返回
		responseSuccess(playerId, HP.code.ALLIANCE_CARNIVAL_RECEIVE_MISSION_REQ_VALUE);

		// 推成就
		AchievePushHelper.pushAchieveAdd(playerId, getAchieveItems(playerId).get().getItems());
		getDataGeter().logACMissionReceive(playerId, achieve.getAchieveId(), getDataGeter().getGuildMemberIds(guildId).size());
		sync(playerId);
		logger.info("allianceCarnival receive, playerId:{}, uuid:{}", playerId, uuid);
	}

	/**
	 * 补发奖励邮件，只在接受第一个任务的时候调用
	 */
	public void reissueRewardMail(String playerId, String guildId) {
		ACBInfo acbInfo = checkAndGetGuildBaseInfo(guildId);
		// 补发基础奖励
		for (int i = 1; i <= acbInfo.getBaseSendLevel(); i++) {
			AllianceCarnivalLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceCarnivalLevelCfg.class, i);
			if (levelCfg == null) {
				continue;
			}
			List<Builder> baseReward = levelCfg.getBaseRewardList();
			getDataGeter().sendMail(playerId, MailId.ALLIANCE_CARNIVAL_BASE_AWARD, null, null, new Object[] { i }, baseReward, false);
		}
	}

	/**
	 * 任务是否过期
	 */
	public boolean achieveOutOfData(long receiveTime, int achieveId) {
		AllianceCarnivalAchieveCfg config = HawkConfigManager.getInstance().getConfigByKey(AllianceCarnivalAchieveCfg.class, achieveId);
		if (config == null) {
			return true;
		}

		if (receiveTime + config.getTimeLimit() < HawkTime.getMillisecond()) {
			return true;
		}
		return false;
	}

	/**
	 * 放弃任务 
	 */
	public void abandon(String playerId, String uuid) {
		if (getDataGeter().isCrossPlayer(playerId)) {
			return;
		}

		// 判断是否有联盟
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_ABANDON_MISSION_REQ_VALUE, Status.AllianceCarnivalError.AC_HAVE_NO_ALLIANCE_VALUE);
			return;
		}

		Optional<AllianceCarnivalEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		AllianceCarnivalEntity entity = opPlayerDataEntity.get();
		// 之前已经有的成就，直接删除掉
		Optional<AchieveItems> achieveItems = getAchieveItems(playerId);
		if (achieveItems.isPresent() && !achieveItems.get().getItems().isEmpty()) {
			logger.info("allianceCarnival abandon, playerId:{}, uuid:{}, items:{}", playerId, uuid, achieveItems.get().getItems().toString());
			for (AchieveItem achieve : entity.getAchieve()) {
				AchievePushHelper.pushAchieveDelete(playerId, achieve);
				boolean outOfData = achieveOutOfData(entity.getReceiveMissionTime(), achieve.getAchieveId());
				getDataGeter().logACMissionAbandon(playerId, achieve.getAchieveId(), outOfData);
			}
			entity.clearAchieve();
		}

		// 重新同步任务
		sync(playerId);
		// 通用成功返回
		responseSuccess(playerId, HP.code.ALLIANCE_CARNIVAL_ABANDON_MISSION_REQ_VALUE);
	}

	/**
	 * 删除任务
	 */
	public void delete(String playerId, String uuid) {
		if (getDataGeter().isCrossPlayer(playerId)) {
			return;
		}

		// 判断是否有联盟
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_DELETE_MISSION_REQ_VALUE, Status.AllianceCarnivalError.AC_HAVE_NO_ALLIANCE_VALUE);
			return;
		}

		// 权限检测
		boolean auth = getDataGeter().checkGuildAuthority(playerId, AuthId.ALLIANCE_CARNIVAL_DELETE);
		if (!auth) {
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_DELETE_MISSION_REQ_VALUE, Status.AllianceCarnivalError.AC_DELETE_MISSION_AUTH_VALUE);
			return;
		}

		// 判断是否有任务
		Map<String, ACMInfo> missions = checkAndGetGuildMissions(guildId);
		ACMInfo acmInfo = missions.get(uuid);
		if (acmInfo != null) {
			acmInfo.remove(guildId);
		}
		missions.remove(uuid);
		if (acmInfo != null) {
			checkAndGetGuildMissions(guildId, acmInfo.getAchieveId());
		}

		sync(playerId);
		// 通用成功返回
		responseSuccess(playerId, HP.code.ALLIANCE_CARNIVAL_DELETE_MISSION_REQ_VALUE);
		logger.info("allianceCarnival delete, playerId:{}, uuid:{}", playerId, uuid);
	}

	/**
	 * 排行榜
	 */
	public void pushRank(String playerId) {
		if (getDataGeter().isCrossPlayer(playerId)) {
			return;
		}

		ACRankInfoMsg.Builder builder = ACRankInfoMsg.newBuilder();
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			pushToPlayer(playerId, HP.code.ALLIANCE_CARNIVAL_RANK_INFO_RESP_VALUE, builder);
			return;
		}

		Map<String, ACRankInfo> guildRank = rankObj.getGuildRank(guildId);
		for (ACRankInfo rank : guildRank.values()) {
			if (rank.getExp() <= 0) {
				continue;
			}
			if (!getDataGeter().checkPlayerExist(rank.getPlayerId())) {
				continue;
			}
			ACRankMsg.Builder msg = ACRankMsg.newBuilder();
			msg.setPlayerId(rank.getPlayerId());
			String playerName = getDataGeter().getPlayerName(rank.getPlayerId());
			msg.setPlayerName(playerName);
			msg.addAllPersonalProtectSwitch(getDataGeter().getPersonalProtectVals(rank.getPlayerId()));
			msg.setExp(rank.getExp());
			msg.setFinishTimes(rank.getFinishTimes());
			msg.setCityLevel(getDataGeter().getConstructionFactoryLevel(rank.getPlayerId()));
			builder.addRankMsg(msg);
		}
		pushToPlayer(playerId, HP.code.ALLIANCE_CARNIVAL_RANK_INFO_RESP_VALUE, builder);
	}

	/**
	 * 领奖
	 */
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		addExp(achieveId, playerId);
		removeFinishMission(playerId);
		return AchieveProvider.super.onTakeReward(playerId, achieveId);
	}

	/**
	 * 删除完成的任务
	 */
	public void removeFinishMission(String playerId) {
		if (!isOpening(null)) {
			return;
		}

		Optional<AllianceCarnivalEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		AllianceCarnivalEntity entity = opPlayerDataEntity.get();
		entity.clearAchieve();
		sync(playerId);
	}

	/**
	 * 增加经验
	 */
	public void addExp(int achieveId, String playerId) {
		if (!isOpening(null)) {
			return;
		}

		Optional<AllianceCarnivalEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}

		AllianceCarnivalEntity entity = opPlayerDataEntity.get();
		// 判断是否有联盟
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			logger.info("allianceCarnival addExp have no guild, playerId:{}, achieveId:{}", playerId, achieveId);
			return;
		}

		// 和起始联盟不同
		if (HawkOSOperator.isEmptyString(entity.getInitGuildId()) || !entity.getInitGuildId().equals(guildId)) {
			logger.info("allianceCarnival addExp init guild not same, playerId:{}, achieveId:{}, guild:{}, initGuildId:{}", playerId, achieveId, guildId, entity.getInitGuildId());
			return;
		}

		// 成就配置
		AllianceCarnivalAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceCarnivalAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			return;
		}

		// 真实の增加经验
		ACBInfo acbInfo = checkAndGetGuildBaseInfo(guildId);
		acbInfo.addExp(achieveCfg.getExp());
		entity.setExp(entity.getExp() + achieveCfg.getExp());
		// 补发奖励
		if (entity.getFinishTimes() == 0) {
			// reissueRewardMail(playerId, guildId);
			// 设置联盟参与活动人数
			ACBInfo baseInfo = checkAndGetGuildBaseInfo(guildId);
			baseInfo.addJoinCount(1);
		}

		entity.setFinishTimes(entity.getFinishTimes() + 1);
		// 更新排行榜
		rankObj.updateRankInfo(entity);
		// 检测奖励发放
		checkAndSendAward(guildId);
		// 日志
		getDataGeter().logACMissionFinish(playerId, achieveCfg.getExp(), getDataGeter().getGuildMemberIds(guildId).size());

		ACExpAdd.Builder builder = ACExpAdd.newBuilder();
		builder.setAddValue(achieveCfg.getExp());
		pushToPlayer(playerId, HP.code.ALLIANCE_CARNIVAL_ADD_EXP_PUSH_VALUE, builder);
		logger.info("allianceCarnival addExp, playerId:{}, achieveId:{}, guild:{}, addExp:{}, afterExp:{}, guildAfterExp:{}", playerId, achieveId, guildId, achieveCfg.getExp(),
				entity.getExp(), acbInfo.getExp());
	}

	/**
	 * 检测奖励发放
	 */
	public void checkAndSendAward(String guildId) {
		if (!isOpening(null)) {
			return;
		}
		
		int index = Math.abs(guildId.hashCode()) % HawkTaskManager.getInstance().getExtraThreadNum();
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				checkSendBaseAward(guildId);
				checkSendAdvAward(guildId);
				return null;
			}
		}, index);
	}

	/**
	 * 检测基础奖励发放
	 */
	public void checkSendBaseAward(String guildId) {
		ACBInfo acbInfo = checkAndGetGuildBaseInfo(guildId);
		Collection<String> guildMemberIds = getDataGeter().getGuildMemberIds(guildId);
		for (int i = 1; i <= acbInfo.getCurrentLevel() - acbInfo.getBaseSendLevel(); i++) {
			try {
				int sendLevel = acbInfo.getBaseSendLevel() + i;
				AllianceCarnivalLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceCarnivalLevelCfg.class, sendLevel);
				if (levelCfg == null) {
					continue;
				}

				List<Builder> baseReward = levelCfg.getBaseRewardList();
				for (String playerId : guildMemberIds) {
					Optional<AllianceCarnivalEntity> opPlayerDataEntity = getThisPlayerDataEntity(playerId);
					if (!opPlayerDataEntity.isPresent()) {
						continue;
					}
					AllianceCarnivalEntity entity = opPlayerDataEntity.get();
					// 玩家没有联盟，就不给他发
					if (HawkOSOperator.isEmptyString(entity.getInitGuildId())) {
						continue;
					}
					// 玩家初始联盟和本联盟是否一致
					if (!entity.getInitGuildId().equals(guildId)) {
						continue;
					}

					getDataGeter().sendMail(playerId, MailId.ALLIANCE_CARNIVAL_BASE_AWARD, null, null, new Object[] { sendLevel }, baseReward, false);
				}

				getDataGeter().addWorldBroadcastMsg(Const.ChatType.GUILD_HREF, guildId, Const.NoticeCfgId.AC_BASE_AWARD, null, sendLevel, levelCfg.getAdvancePlayerNum());
				logger.info("allianceCarnival checkSendBaseAward, guild:{}, sendLevel:{}", guildId, sendLevel);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		acbInfo.setBaseSendLevel(acbInfo.getCurrentLevel());
	}

	/**
	 * 检测进阶奖励发放
	 */
	public void checkSendAdvAward(String guildId) {
		ACBInfo acbInfo = checkAndGetGuildBaseInfo(guildId);
		Collection<String> guildMemberIds = getDataGeter().getGuildMemberIds(guildId);
		for (int i = 1; i <= acbInfo.getCurrentLevel(); i++) {
			try {
				int sendLevel = i;
				AllianceCarnivalLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceCarnivalLevelCfg.class, sendLevel);
				if (levelCfg == null) {
					continue;
				}

				int advancePlayerNum = levelCfg.getAdvancePlayerNum();				
				List<Builder> advReward = levelCfg.getAdvRewardList();
				List<Builder> payReward = levelCfg.getPayRewardList();
				for (String playerId : guildMemberIds) {
					Optional<AllianceCarnivalEntity> opPlayerDataEntity = getThisPlayerDataEntity(playerId);
					if (!opPlayerDataEntity.isPresent()) {
						continue;
					}

					AllianceCarnivalEntity entity = opPlayerDataEntity.get();
					//玩家的经验不够
					if (entity.getExp() < advancePlayerNum) {
						continue;
					}
					//奖励已经发放.
					if (entity.getSendAdvLevel() >= sendLevel) {
						continue;
					}
					
					// 玩家没有联盟，就不给他发
					if (HawkOSOperator.isEmptyString(entity.getInitGuildId())) {
						continue;
					}
					// 玩家初始联盟和本联盟是否一致
					if (!entity.getInitGuildId().equals(guildId)) {
						continue;
					}
					
					entity.setSendAdvLevel(sendLevel);
					getDataGeter().sendMail(playerId, MailId.ALLIANCE_CARNIVAL_ADVA_AWARD, null, null, new Object[] { sendLevel }, advReward, false);
					// 发放典藏宝箱奖励
					if (entity.getPayGiftTime() > 0) {
						getDataGeter().sendMail(playerId, MailId.ALLIANCE_CARNIVAL_PAY_REWARD, null, null, new Object[] { sendLevel }, payReward, false);
					}
				}
				
				logger.info("allianceCarnival checkSendAdvAward, guild:{}, sendLevel:{}", guildId, sendLevel);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}	
	}

	/**
	 * 购买次数
	 */
	public void buyTimes(String playerId) {
		if (getDataGeter().isCrossPlayer(playerId)) {
			return;
		}
		Optional<AllianceCarnivalEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}

		AllianceCarnivalEntity entity = opPlayerDataEntity.get();
		if(HawkTime.getMillisecond() > entity.getBuyTime()){
			entity.setBuyTime(HawkTime.getNextAM0Date());
			entity.setDayBuyNumber(0);
		}
		
		int buyLimit = AllianceCarnivalCfg.getInstance().getBuyLimit();
		if (entity.getDayBuyNumber() >= buyLimit) {
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_BUY_TIMES_VALUE, Status.AllianceCarnivalError.AC_BUY_TIMES_LIMIT_VALUE);
			return;
		}

		// 消耗
		int price = AllianceCarnivalCfg.getInstance().getBuyPrice();
		boolean costSuccess = getDataGeter().consumeGold(playerId, price, HP.code.ALLIANCE_CARNIVAL_BUY_TIMES_VALUE, Action.ALLIANCE_CARNIVAL_BUY_TIMES);
		if (!costSuccess) {
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_BUY_TIMES_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}

		entity.addBuyTimes();
		entity.addDayBuyNumber();
		sync(playerId);
		getDataGeter().logACMBuyTimes(playerId);
		// 通用成功返回
		responseSuccess(playerId, HP.code.ALLIANCE_CARNIVAL_BUY_TIMES_VALUE);
		logger.info("allianceCarnivalBuyTimes, playerId:{}, afterBuyTimes:{}", playerId, entity.getBuyTimes());
	}
	
	
	/**
	 * 满级后兑换
	 */
	public void exchange(String playerId) {
		int maxLevel = AllianceCarnivalCfg.getInstance().getAwardLvLimit();
		int point = AllianceCarnivalCfg.getInstance().getAwardScore();
		String award = AllianceCarnivalCfg.getInstance().getAward();
		String payAward = AllianceCarnivalCfg.getInstance().getPayAward();
		
		// 玩家当前所在联盟
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_EXCHANGE_REQ_VALUE, Status.AllianceCarnivalError.AC_HAVE_NO_ALLIANCE_VALUE);
			return;
		}
		ACBInfo acbInfo = checkAndGetGuildBaseInfo(guildId);
		int currentLevel = acbInfo.getCurrentLevel();
		if(currentLevel<maxLevel){
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_EXCHANGE_REQ_VALUE, Status.AllianceCarnivalError.AC_LEVEL_LIMIT_VALUE);
			return;
		}
		Optional<AllianceCarnivalEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		AllianceCarnivalEntity entity = opEntity.get();
		
		//当前经验
		int currentExp =  acbInfo.getCurrentExp();
		if(currentExp > point * (entity.getExchangeNumber()+1)) {
			//剩余可用积分
			currentExp = currentExp-(point * entity.getExchangeNumber());
			//发放道具数量
			int rate = currentExp/point;
			int backRate = entity.getExchangeNumber();
			entity.addExchangeNumber(rate);			
			entity.notifyUpdate();
			
			List<RewardItem.Builder> baseRewardList = new ArrayList<>();
			if (!HawkOSOperator.isEmptyString(award)) {
				baseRewardList.addAll(RewardHelper.toRewardItemImmutableList(award));
			}
			
			if (entity.getPayGiftTime() > 0 && !HawkOSOperator.isEmptyString(payAward)) {
				baseRewardList.addAll(RewardHelper.toRewardItemImmutableList(payAward));
			}
			
			this.getDataGeter().takeReward(playerId, baseRewardList, rate,
					Action.ALLIANCE_CARNIVAL_EXCHANGE_TIMES, true, RewardOrginType.ALLIANCE_CARNIVAL_EXCHANGE_REWARD);
			logger.info("exchange, playerId:{},beforeNumber:{},afterNumber:{},curExchangeNumber:{}", playerId, backRate,entity.getExchangeNumber(),rate);
			
			sync(playerId);
		}else{
			sendErrorAndBreak(playerId, HP.code.ALLIANCE_CARNIVAL_EXCHANGE_REQ_VALUE, Status.AllianceCarnivalError.AC_POINT_LIMIT_VALUE);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends HawkDBEntity> Optional<T> getThisPlayerDataEntity(String playerId) {
		ActivityCfg cfg = getActivityCfg();
		if (cfg == null) {
			return Optional.empty();
		}
		
		int termId = getActivityTermId(playerId);
		// 活动未开启
		if (termId == 0) {
			return Optional.empty();
		}

		// 缓存获取
		HawkDBEntity entity = PlayerDataHelper.getInstance().getActivityDataEntity(playerId, getActivityType());
		if (entity != null) {
			IActivityDataEntity dataEntity = (IActivityDataEntity) entity;
			if (dataEntity.getTermId() == termId) {
				return Optional.of((T) dataEntity);
			}
		}

		// 数据库获取
		entity = loadFromDB(playerId, termId);
		if (entity != null) {
			entity = PlayerDataHelper.getInstance().putActivityDataEntity(playerId, getActivityType(), entity);
			return Optional.of((T) entity);
		}
		
		return Optional.ofNullable((T) entity);
	}
	
	@Override
	public void onEnd() {
		if (!isInit) {
			init();
		}		
	}
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		Optional<AllianceCarnivalEntity> opEntity = getPlayerDataEntity(playerId);
		AllianceCarnivalEntity entity = opEntity.get();
		if (event.isCrossDay() && HawkTime.getMillisecond() > entity.getBuyTime() ) {
			entity.setBuyTime(HawkTime.getNextAM0Date());
			entity.setDayBuyNumber(0);
			entity.notifyUpdate();
			sync(entity.getPlayerId());
		}
	}	
	
	/**
	 * 购买事件
	 */
	@Subscribe
	public void onPurchaseEvent(PayGiftBuyEvent event) {
		if (!isOpening(null)) {
			HawkLog.logPrintln("alliance carnival purchase failed, activity not open, playerId: {}", event.getPlayerId());
			return;
		}
		AllianceCarnivalCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceCarnivalCfg.class);
		if (!cfg.getAndroidPayId().equals(event.getGiftId()) && !cfg.getIosPayId().equals(event.getGiftId())) {
			return;
		}
		
		Optional<AllianceCarnivalEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		AllianceCarnivalEntity entity = opEntity.get();
		if (entity.getPayGiftTime() > 0) {
			HawkLog.logPrintln("alliance carnival purchase success, playerId: {}, already payTime: {}", event.getPlayerId(), entity.getPayGiftTime());
			return;
		}
		
		entity.setPayGiftTime(HawkTime.getMillisecond());
		HawkLog.logPrintln("alliance carnival purchase success, playerId: {}", event.getPlayerId());
		PlayerPushHelper.getInstance().pushToPlayer(entity.getPlayerId(), HawkProtocol.valueOf(HP.code2.PAY_GIFT_SUCC_SYNC_VALUE));
		try {
			sync(entity.getPlayerId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 补发奖励
		int sendLevel = entity.getSendAdvLevel(), exchangeTimes = entity.getExchangeNumber();
		for (int level = 1; level <= sendLevel; level++) {
			AllianceCarnivalLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceCarnivalLevelCfg.class, level);
			List<Builder> payReward = levelCfg.getPayRewardList();
			getDataGeter().sendMail(entity.getPlayerId(), MailId.ALLIANCE_CARNIVAL_PAY_REWARD, null, null, new Object[] { level }, payReward, false);
		}
		
		if (exchangeTimes > 0) {
			String payAward = AllianceCarnivalCfg.getInstance().getPayAward();
			List<RewardItem.Builder> payRewardList = new ArrayList<>();
			payRewardList.addAll(RewardHelper.toRewardItemImmutableList(payAward));
			payRewardList.forEach(e -> e.setItemCount(e.getItemCount() * exchangeTimes));
			getDataGeter().sendMail(entity.getPlayerId(), MailId.ALLIANCE_CARNIVAL_PAY_REWARD_ISSUE, null, null, null, payRewardList, false);
		}
	}
	
	/**
	 * 购买条件检测
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean onPurchaseCheck(String playerId) {
		if (!isOpening(null)) {
			HawkLog.logPrintln("alliance carnival purchase failed, activity not open, playerId: {}", playerId);
			return false;
		}
		
		Optional<AllianceCarnivalEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.logPrintln("alliance carnival purchase failed, data error, playerId: {}", playerId);
			return false;
		}
		
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			HawkLog.logPrintln("alliance carnival purchase failed, playerId: {}, no guild", playerId);
			return false;
		}
		
		AllianceCarnivalEntity entity = opEntity.get();
		if (entity.getPayGiftTime() > 0) {
			HawkLog.logPrintln("alliance carnival purchase success, playerId: {}, already payTime: {}", playerId, entity.getPayGiftTime());
			return false;
		}
		
		return true;
	}
	
	/**
	 * 获取个人当前等级
	 * 
	 * @return
	 */
	private int getCurrentPLevel(AllianceCarnivalEntity entity) {
		int currentLevel = 0;
		ConfigIterator<AllianceCarnivalLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(AllianceCarnivalLevelCfg.class);
		while (iterator.hasNext()) {
			AllianceCarnivalLevelCfg config = iterator.next();
			// config 里面 的 advancePlayerNum 字段具有迷惑性，它实际代表的含义是玩家个人相关的任务经验值
			if (entity.getExp() >= config.getAdvancePlayerNum()) {
				currentLevel = config.getLevel();
			} else {
				break;
			}
		}

		return currentLevel;
	}
	
	/**
	 * 获取个人当前经验
	 * 
	 * @param entity
	 * @return
	 */
	private int getCurrentPExp(AllianceCarnivalEntity entity) {
		int currentExp = entity.getExp();
		ConfigIterator<AllianceCarnivalLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(AllianceCarnivalLevelCfg.class);
		while (iterator.hasNext()) {
			AllianceCarnivalLevelCfg config = iterator.next();
			// config 里面 的 advancePlayerNum 字段具有迷惑性，它实际代表的含义是玩家个人相关的任务经验值
			if (entity.getExp() >= config.getAdvancePlayerNum()) {
				currentExp = entity.getExp() - config.getAdvancePlayerNum();
			} else {
				break;
			}
		}

		return currentExp;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
}
