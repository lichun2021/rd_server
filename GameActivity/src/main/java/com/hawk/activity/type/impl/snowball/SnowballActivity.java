package com.hawk.activity.type.impl.snowball;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.GuildDismissEvent;
import com.hawk.activity.event.impl.SnowballGoalAssistanceEvent;
import com.hawk.activity.event.impl.SnowballGoalEvent;
import com.hawk.activity.event.impl.SnowballKickEvent;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.snowball.cfg.SnowballCfg;
import com.hawk.activity.type.impl.snowball.cfg.SnowballGoalCfg;
import com.hawk.activity.type.impl.snowball.cfg.SnowballRankRewardCfg;
import com.hawk.activity.type.impl.snowball.cfg.SnowballScoreRewardCfg;
import com.hawk.activity.type.impl.snowball.cfg.SnowballStageCfg;
import com.hawk.activity.type.impl.snowball.entity.SnowballEntity;
import com.hawk.activity.type.impl.snowball.rank.SnowballRankObject;
import com.hawk.game.protocol.Activity.SnowballBoxInfo;
import com.hawk.game.protocol.Activity.SnowballBuildingInfo;
import com.hawk.game.protocol.Activity.SnowballPageInfo;
import com.hawk.game.protocol.Activity.SnowballRankResp;
import com.hawk.game.protocol.Activity.SnowballRankType;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.SnowballLastAtkPush;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

import redis.clients.jedis.Tuple;

/**
 * 雪球大战
 * @author golden
 *
 */
public class SnowballActivity extends ActivityBase {

	/**
	 * 万分比
	 */
	public static final double PER = 0.0001;

	/**
	 * redis key 过期时间(3天)
	 */
	public static final int EXPIRE_TIME = 259200;

	/**
	 * 进球信息 坐标id-玩家列表
	 */
	public static Map<Integer, List<String>> goalInfoMap = new ConcurrentHashMap<>();

	/**
	 * 个人伤害排行
	 */
	private SnowballRankObject selfRank = new SnowballRankObject(SnowballRankType.SELF_SNOWBALL_RANK);

	/**
	 * 联盟伤害排行
	 */
	private SnowballRankObject guildRank = new SnowballRankObject(SnowballRankType.GUILD_SNOWBALL_RANK);

	/**
	 * 是否经过初始化
	 */
	private boolean isInit;

	/**
	 * 场次(当前没有开启的话，turn为0)
	 */
	private int turnId;

	/**
	 * 阶段id
	 */
	private int stageId;
	
	/**
	 * 当前场次剩余时间
	 */
	private long turnRemainTime;
	
	/**
	 * 上次排行版检测时间
	 */
	private long lastRankCheckTime;
	
	/**
	 * 构造
	 */
	public SnowballActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	/**
	 * 活动类型
	 */
	@Override
	public ActivityType getActivityType() {
		return ActivityType.SNOWBALL;
	}

	/**
	 * 实例
	 */
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SnowballActivity activity = new SnowballActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	public void onTick() {
		if (!isInit) {
			init();
			isInit = true;
		}
		
		int currentTurnId = getCurrentTurnId();
		if (turnId != currentTurnId) {
			
			int markTurnId = turnId;
			
			turnId = currentTurnId;
			
			// 该场次雪球大战结束
			if (turnId == 0) {
				sendBroadcast(Const.NoticeCfgId.SNOWBALL_NOTICE_213, null);
				
				Set<String> onlinePlayers = getDataGeter().getOnlinePlayers();
				for (String playerId : onlinePlayers) {
					try {
						sendStatisticMail(playerId, markTurnId);
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
			}
			goalInfoMap.clear();
		}
		
		// 公告检测
		stageNoticeTick();
		
		// 刷新排行榜
		long currentTime = HawkTime.getMillisecond();
		SnowballCfg cfg = HawkConfigManager.getInstance().getKVInstance(SnowballCfg.class);
		if (HawkTime.getMillisecond() - lastRankCheckTime > cfg.getRankPeriod()) {
			refreshRankInfo();
			lastRankCheckTime = currentTime;
		}
	}

	@Override
	public void onOpen() {
		selfRank = new SnowballRankObject(SnowballRankType.SELF_SNOWBALL_RANK);
		guildRank = new SnowballRankObject(SnowballRankType.GUILD_SNOWBALL_RANK);
		goalInfoMap = new ConcurrentHashMap<>();
	}

	/**
	 * 联盟解散
	 */
	@Subscribe
	public void onGuildDismiss(GuildDismissEvent event) {
		guildRank.removeRank(event.getGuildId(), getActivityTermId());
	}

	/**
	 * 刷新排行榜
	 */
	public void refreshRankInfo() {
		int termId = getActivityTermId();
		selfRank.refreshRank(termId);
		guildRank.refreshRank(termId);
	}

	/**
	 * 初始化
	 */
	public void init() {
		// 加载进球信息
		loadGoalInfo();
		
		// 初始化场次
		turnId = getCurrentTurnId();
		
		// 当前阶段
		stageId = getCurrentStage();
		
		// 轮次剩余时间
		turnRemainTime = getTurnRemainTime();
		
		logger.info("snowballActivity init, turnId:{}, stageId:{}, turnRemainTime:{}", turnId, stageId, turnRemainTime);
	}

	/**
	 * 获取redis key
	 */
	public String getSnowballGoalInfoKey() {
		return String.format(ActivityRedisKey.SNOWBALL_GOAL_INFO, getActivityTermId(), String.valueOf(getCurrentTurnRefreshTime()));
	}

	/**
	 * 加载进球信息
	 */
	public void loadGoalInfo() {

		if (!isOpening(null) || !isInProgress()) {
			return;
		}

		Map<String, String> goalInfo = ActivityGlobalRedis.getInstance().hgetAll(getSnowballGoalInfoKey());
		if (goalInfo == null || goalInfo.isEmpty()) {
			return;
		}

		for (Entry<String, String> entry : goalInfo.entrySet()) {
			List<String> playerIdArr = new ArrayList<>();

			Integer pointId = Integer.valueOf(entry.getKey());
			String[] playerIds = entry.getValue().split(",");

			for (int i = 0; i < playerIds.length; i++) {
				playerIdArr.add(playerIds[i]);
			}

			goalInfoMap.put(pointId, playerIdArr);
			
			logger.info("snowballActivity loadGoalInfo, pointId:{}, playerIdAttr:{}", pointId, Arrays.toString(playerIdArr.toArray()));
		}
	}

	/**
	 * 踢球
	 */
	@Subscribe
	public void onKick(SnowballKickEvent event) {

		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		if (!isInProgress()) {
			return;
		}
		
		int atkTimesBefore = 0;
		String playerId = event.getPlayerId();
		for (String record : event.getRecord()) {
			if (!record.equals(playerId)) {
				continue;
			}
			atkTimesBefore++;
		}

		int kickScore = SnowballCfg.getInstance().getAtkScore();
		if (atkTimesBefore >= SnowballCfg.getInstance().getKickOnceLimit()) {
			kickScore = 0;
		}

		int continueOnceAtkIncrease = SnowballCfg.getInstance().getContinueAtkIncrease();
		int continueAtkIncreaseMax = SnowballCfg.getInstance().getContinueAtkIncreaseMax();
		int continueAtkIncrease = Math.min(continueAtkIncreaseMax, continueOnceAtkIncrease * atkTimesBefore);

		// 踢球积分
		int continueScore = (int) Math.ceil((kickScore * continueAtkIncrease * PER));

		// 助攻积分
		int assistScore = 0;
		for (String record : event.getRecord()) {
			if (!getDataGeter().isInTheSameGuild(playerId, record)) {
				continue;
			}
			assistScore = SnowballCfg.getInstance().getAssistScore();
		}

		int addScore = kickScore + continueScore + assistScore;

		// 排行榜
		selfRank.addRankScore(playerId, getActivityTermId(), addScore);
		String guildId = getDataGeter().getGuildId(playerId);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			guildRank.addRankScore(guildId, getActivityTermId(), addScore);
		}
		
		// 发踢球奖励
		sendKickSnowballMail(playerId, kickScore, continueScore, assistScore);
		
		// 添加积分
		Optional<SnowballEntity> opEntity = getPlayerDataEntity(playerId);
		SnowballEntity entity = opEntity.get();
		entity.setTurnId(turnId);
		entity.addScore(addScore);
		entity.addKickScore(kickScore);
		entity.addContinueKickScore(continueScore);
		entity.addAssisScore(assistScore);

		// 日志
		logger.info("snowball onKick, playerId:{}, guildId:{}, atkTimesBefore:{}, kickScore:{}, continueScore:{}, assistScore:{}, addScore:{}, turnId:{}, entityScore:{}, "
				+ "entityKickScore:{}, entityContinueKickScore:{}, entityAssisScore:{}", event.getPlayerId(), guildId, atkTimesBefore, kickScore, continueScore, assistScore,
				addScore, turnId, entity.getScore(), entity.getKickScore(), entity.getContinueKickScore(), entity.getAssisScore());
		
		pushPageInfo(playerId);
	}

	/**
	 * 进球
	 */
	@Subscribe
	public void onGoal(SnowballGoalEvent event) {
		
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		if (!isInProgress()) {
			return;
		}
		
		int pointId = combineXAndY(event.getX(), event.getY());

		// 添加进球信息
		List<String> goalInfo = getGoalInfo(pointId);
		goalInfo.add(event.getPlayerId());

		// 记录到redis
		String value = SerializeHelper.collectionToString(goalInfo, SerializeHelper.BETWEEN_ITEMS);
		ActivityGlobalRedis.getInstance().hset(getSnowballGoalInfoKey(), String.valueOf(pointId), value, EXPIRE_TIME);

		int addScore = 0;
		SnowballGoalCfg goalCfg = HawkConfigManager.getInstance().getConfigByKey(SnowballGoalCfg.class, getBuildingGoalCfgId(pointId));
		if (goalCfg != null) {
			addScore = goalCfg.getGoalScore();
		} else {
			return;
		}

		// 更新排行榜
		selfRank.addRankScore(event.getPlayerId(), getActivityTermId(), addScore);
		String guildId = getDataGeter().getGuildId(event.getPlayerId());
		if (!HawkOSOperator.isEmptyString(guildId)) {
			guildRank.addRankScore(guildId, getActivityTermId(), addScore);
		}

		// 添加积分
		Optional<SnowballEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		SnowballEntity entity = opEntity.get();
		entity.setTurnId(turnId);
		entity.addScore(addScore);
		entity.addGoalScore(addScore);

		// 发进球奖励
		sendGoalAward(event);
		
		// 发阶段达成进球奖励
		sendStageGoalAward(event);
		
		// 公告
		String playerName = getDataGeter().getPlayerName(event.getPlayerId());
		sendBroadcast(NoticeCfgId.SNOWBALL_NOTICE_214, null, playerName, event.getX(), event.getY());
		addWorldBroadcastMsg(ChatType.CHAT_ALLIANCE, NoticeCfgId.SNOWBALL_NOTICE_214, null, playerName, event.getX(), event.getY());
		
		// 日志
		logger.info("snowball onGoal, playerId:{}, guildId:{}, pointId:{}, addScore:{}, entityScore:{}, entityGoalScore:{}, turnId:{}", event.getPlayerId(), guildId, 
				pointId, addScore, entity.getScore(), entity.getGoalScore(), turnId);
		
		pushPageInfo(event.getPlayerId());
	}

	/**
	 * 获取建筑进球配置id
	 */
	public int getBuildingGoalCfgId(int pointId) {
		int cfgId = 0;

		// 进球数量
		int goalCout = getGoalInfo(pointId).size();

		int targetCount = 0;
		ConfigIterator<SnowballGoalCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(SnowballGoalCfg.class);
		while (cfgIter.hasNext()) {
			SnowballGoalCfg cfg = cfgIter.next();
			if (pointId != cfg.getPointId()) {
				continue;
			}
			if (goalCout > cfg.getTarget()) {
				continue;
			}
			if (targetCount != 0 && targetCount < cfg.getTarget()) {
				continue;
			}
			targetCount = cfg.getTarget();
			cfgId = cfg.getId();
		}
		return cfgId;
	}

	/**
	 * 进球助攻
	 */
	@Subscribe
	public void onGoalAssistance(SnowballGoalAssistanceEvent event) {
		int assistScore = SnowballCfg.getInstance().getGoalAssistScore();

		// 排行榜
		selfRank.addRankScore(event.getPlayerId(), getActivityTermId(), assistScore);
		String guildId = getDataGeter().getGuildId(event.getPlayerId());
		if (!HawkOSOperator.isEmptyString(guildId)) {
			guildRank.addRankScore(guildId, getActivityTermId(), assistScore);
		}

		// 添加积分
		Optional<SnowballEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		SnowballEntity entity = opEntity.get();
		entity.setTurnId(turnId);
		entity.addScore(assistScore);
		entity.addGoalAssisScore(assistScore);
		
		// 发积分邮件
		sendGoalAssistanceScoreMail(event.getPointId(), event.getPlayerId(), assistScore, event.getNumber());
		
		pushPageInfo(entity.getPlayerId());
		
		// 日志
		logger.info("snowball onGoalAssistance, playerId:{}, guildId:{}, assistScore:{}, entityScore:{}, entityGoalAssisScore:{}, turnId:{}", event.getPlayerId(), guildId,
				assistScore, entity.getScore(), entity.getGoalAssisScore(), turnId);
	}

	/**
	 * 获取当前阶段
	 */
	public int getCurrentStage() {
		int currentStage = 1;

		ConfigIterator<SnowballStageCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(SnowballStageCfg.class);
		while (cfgIter.hasNext()) {
			SnowballStageCfg cfg = cfgIter.next();

			// 检测解锁条件达成
			boolean unlock = true;
			for (Entry<Integer, Integer> entry : cfg.getUnlockMap().entrySet()) {
				// 目标点/建筑
				int targetId = entry.getKey();
				// 目标值
				int targetValue = entry.getValue();
				// 该建筑目前进球数
				int hasScore = getGoalInfo(targetId).size();
				if (hasScore < targetValue) {
					unlock = false;
				}
			}

			if (unlock && cfg.getStageId() > currentStage) {
				currentStage = cfg.getStageId();
			}
		}

		return currentStage;
	}

	/**
	 * 获取进球信息
	 */
	public List<String> getGoalInfo(int pointId) {
		List<String> goalInfo = goalInfoMap.get(pointId);
		if (goalInfo == null) {
			goalInfo = new ArrayList<>();
			List<String> oldValue = goalInfoMap.putIfAbsent(pointId, goalInfo);
			if (oldValue != null) {
				goalInfo = oldValue;
			}
		}
		return goalInfo;
	}

	/**
	 * 两个int分作高地位组合一个int值
	 */
	public int combineXAndY(int x, int y) {
		return (y << 16) | x;
	}

	/**
	 * 把int值拆分高16低16,index = 0 x;index = 1 y
	 */
	public int[] splitXAndY(int value) {
		return new int[] { (value & 0x0000ffff), (value >> 16) & 0x0000ffff };
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SnowballEntity> queryList = HawkDBManager.getInstance()
				.query("from SnowballEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SnowballEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SnowballEntity entity = new SnowballEntity();
		entity.setPlayerId(playerId);
		entity.setTermId(termId);
		return entity;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		// 推界面信息
		pushPageInfo(playerId);
		
		// 检测玩家轮次
		loginCheckPlayerTurn(playerId);
	}

	/**
	 * 推送界面信息
	 */
	public void pushPageInfo(String playerId) {
		SnowballPageInfo.Builder builder = SnowballPageInfo.newBuilder();

		Optional<SnowballEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SnowballEntity entity = opEntity.get();

		for (Integer received : entity.getReceived()) {
			SnowballBoxInfo.Builder boxBuilder = SnowballBoxInfo.newBuilder();
			boxBuilder.setCfgId(received);
			boxBuilder.setHasReceived(true);
			builder.addBox(boxBuilder);
		}

		for (Entry<Integer, List<String>> entry : goalInfoMap.entrySet()) {
			SnowballBuildingInfo.Builder buildingBuilder = SnowballBuildingInfo.newBuilder();
			int[] pos = splitXAndY(entry.getKey());
			buildingBuilder.setX(pos[0]);
			buildingBuilder.setY(pos[1]);
			buildingBuilder.setCount(entry.getValue().size());
			int ownCount = 0;
			for (String goalPlayerId : entry.getValue()) {
				if (!goalPlayerId.equals(playerId)) {
					continue;
				}
				ownCount++;
			}
			buildingBuilder.setOwnCount(ownCount);
			builder.addBuild(buildingBuilder);
		}

		builder.setScore(entity.getScore());
		builder.setStageId(getCurrentStage());
		builder.setTrunId(getCurrentTurnId());
		pushToPlayer(playerId, HP.code.SNOWBALL_PAGE_INFO_RESP_VALUE, builder);
	}

	/**
	 * 拉取榜单信息
	 */
	public void pullRankInfo(String playerId, SnowballRankType rankType) {
		Optional<SnowballEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SnowballEntity entity = opEntity.get();

		SnowballRankResp.Builder builder = null;
		switch (rankType) {
		case SELF_SNOWBALL_RANK:
			builder = selfRank.buildRankInfoResp(entity);
			break;

		case GUILD_SNOWBALL_RANK:
			builder = guildRank.buildRankInfoResp(entity);
			break;
		}

		if (builder != null) {
			pushToPlayer(playerId, HP.code.SNOWBALL_GET_RANK_INFO_RESP_VALUE, builder);
		}

	}

	/**
	 * 获取排行奖励配置
	 */
	private SnowballRankRewardCfg getRankReward(int rank, SnowballRankType rankType) {
		SnowballRankRewardCfg rankCfg = null;
		ConfigIterator<SnowballRankRewardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SnowballRankRewardCfg.class);
		for (SnowballRankRewardCfg cfg : configIterator) {
			if (rankType.getNumber() == cfg.getRankType() && rank <= cfg.getRankLower() && rank >= cfg.getRankUpper()) {
				rankCfg = cfg;
				break;
			}
		}
		return rankCfg;
	}

	/**
	 * 活动是否进行中(刷新雪球阶段)
	 */
	public boolean isInProgress() {
		return getCurrentTurnRefreshTime() > 0L;
	}

	/**
	 * 获取当前轮次刷新时间点
	 */
	public long getCurrentTurnRefreshTime() {

		if (!isOpening(null)) {
			return 0L;
		}

		long currentTime = HawkTime.getMillisecond();

		// 活动持续时间
		long continueTime = SnowballCfg.getInstance().getContinueTime();

		long zeroTime = HawkTime.getAM0Date().getTime();
		
		List<Long> refreshTimeArr = getRefreshTimeArr();
		for (Long refreshTime : refreshTimeArr) {
			if (currentTime >= refreshTime && currentTime <= refreshTime + continueTime) {
				return zeroTime + refreshTime;
			}
		}

		return 0L;
	}

	/**
	 * 获取相对于今天凌晨开始刷新的时间点
	 */
	public List<Long> getRefreshTimeArr() {
		List<Long> refreshTime = new ArrayList<>();

		long zeroTime = HawkTime.getAM0Date().getTime();
		List<Long> beginTimeMillSeconds = SnowballCfg.getInstance().getBeginTimeMillSeconds();
		for (Long beginTime : beginTimeMillSeconds) {
			refreshTime.add(zeroTime + beginTime);
		}
		return refreshTime;
	}

	/**
	 * 获取宝箱奖励
	 */
	public void getBoxReward(String playerId, int cfgId) {

		Optional<SnowballEntity> opEntity = getPlayerDataEntity(playerId);
		SnowballEntity entity = opEntity.get();

		// 已经领过这个档位的奖励了
		Set<Integer> received = entity.getReceived();
		if (received.contains(cfgId)) {
			return;
		}

		SnowballScoreRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SnowballScoreRewardCfg.class, cfgId);
		if (cfg == null) {
			return;
		}

		// 积分不足
		if (entity.getScore() < cfg.getTarget()) {
			return;
		}

		// 发奖
		getDataGeter().takeReward(playerId, cfg.getRewardList(), Action.SNOWBALL_BOX_AWARD, true);
		
		entity.addReceived(cfgId);

		responseSuccess(playerId, HP.code.SNOWBALL_GET_BOX_REWARD_REQ_VALUE);
	}

	/**
	 * 活动结束，发奖
	 */
	@Override
	public void onEnd() {
		int termId = getActivityTermId();
		sendSelfRankReward(termId);
		sendGuildRankReward(termId);
	}

	/**
	 * 合服处理，发奖
	 */
	@Override
	public boolean handleForMergeServer() {
		if (this.getActivityEntity().getActivityState() == com.hawk.activity.type.ActivityState.HIDDEN) {
			return true;
		}

		int termId = getActivityTermId();
		sendSelfRankReward(termId);
		sendGuildRankReward(termId);

		return true;

	}

	/**
	 * 获取当前场次
	 * 
	 * 这个方法默认了活动开启时间在每天第一场开启之前
	 */
	public int getCurrentTurnId() {
		// 活动未开启
		if (!isOpening(null)) {
			return 0;
		}

		// 当前没有场次开启
		if (!isInProgress()) {
			return 0;
		}

		// 当前场次
		int turnCount = 0;

		// 每天开启的时间毫秒数(距离当日零点)
		List<Long> openNodes = SnowballCfg.getInstance().getBeginTimeMillSeconds();

		// 当前是今天的第几场
		long todayZeroTime = HawkTime.getAM0Date().getTime();
		for (Long openNode : openNodes) {
			if (HawkTime.getMillisecond() < todayZeroTime + openNode) {
				continue;
			}
			turnCount++;
		}

		// 和活动开启间隔的天数*每天开启的场次数量
		int termId = getActivityTermId();
		Date beginDate = HawkTime.getAM0Date(new Date(getTimeControl().getStartTimeByTermId(termId)));
		int betweenDays = HawkTime.calcBetweenDays(beginDate, new Date(HawkTime.getMillisecond()));
		turnCount += betweenDays * openNodes.size();

		return turnCount;
	}

	/**
	 * 登录检测玩家轮次
	 */
	public void loginCheckPlayerTurn(String playerId) {
		Optional<SnowballEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SnowballEntity entity = opEntity.get();
		if (entity.getTurnId() != turnId) {
			sendStatisticMail(playerId, entity.getTurnId());
		}
	}
	
	/**
	 * 清除玩家轮次进球信息
	 */
	public void clearTurnScore(SnowballEntity entity) {
		entity.setTurnId(0);
		entity.setKickScore(0);
		entity.setContinueKickScore(0);
		entity.setAssisScore(0);
		entity.setGoalScore(0);
		entity.setGoalAssisScore(0);
	}

	/**
	 * 发踢球奖励
	 */
	private void sendKickSnowballMail(String playerId, int kickScore, int continueScore, int assistScore) {
		// 邮件：推雪球成功
		Object[] params = new Object[1];
		params[0] = kickScore;
		getDataGeter().sendMail(playerId, MailId.SNOWBALL_MAIL_1, null, null, params, null, false);
	}

	/**
	 * 发进球奖励(只给踢进球的那个人发)
	 */
	public void sendGoalAward(SnowballGoalEvent event) {
		List<String> goalInfo = getGoalInfo(event.getPointId());
		int goalCount = goalInfo.size();

		// 发奖配置
		SnowballGoalCfg sendCfg = null;
		ConfigIterator<SnowballGoalCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(SnowballGoalCfg.class);
		while (cfgIterator.hasNext()) {
			SnowballGoalCfg cfg = cfgIterator.next();
			if (goalCount > cfg.getTarget() || cfg.getPointId() != event.getPointId()) {
				continue;
			}
			sendCfg = cfg;
		}

		if (sendCfg == null) {
			return;
		}

		// 进球次数达到xx就不再发奖了
		int termId = getActivityTermId();
		int turnId = getCurrentTurnId();
		int times = 0;
		String timesStr = ActivityGlobalRedis.getInstance().get(getGoalRewadKey(termId, turnId, event.getPlayerId()));
		if (!HawkOSOperator.isEmptyString(timesStr)) {
			times = Integer.parseInt(timesStr);
		}
		
		times = times + 1;
		
		if (times > SnowballCfg.getInstance().getGoalRewardLimit()) {
			
			int[] pos = splitXAndY(event.getPointId());
			Object[] title = new Object[2];
			title[0] = pos[0];
			title[1] = pos[1];
			
			Object[] subTitle = new Object[2];
			subTitle[0] = pos[0];
			subTitle[1] = pos[1];
			
			// 邮件：雪球进球奖励邮件
			Object[] params = new Object[2];
			params[0] = pos[0];
			params[1] = pos[1];
			
			getDataGeter().sendMail(event.getPlayerId(), MailId.SNOWBALL_MAIL_23, title, subTitle, params, null, false);
			
		} else {
			ActivityGlobalRedis.getInstance().set(getGoalRewadKey(termId, turnId, event.getPlayerId()), String.valueOf(times), 86400);
			
			int[] pos = splitXAndY(event.getPointId());
			Object[] title = new Object[2];
			title[0] = pos[0];
			title[1] = pos[1];
			
			Object[] subTitle = new Object[2];
			subTitle[0] = pos[0];
			subTitle[1] = pos[1];
			
			// 邮件：雪球进球奖励邮件
			Object[] params = new Object[3];
			params[0] = pos[0];
			params[1] = pos[1];
			params[2] = SnowballCfg.getInstance().getGoalRewardLimit() - times;
			
			getDataGeter().takeReward(event.getPlayerId(), sendCfg.getGoalAward(), 1, Action.SNOWBALL_GOAL_AWARD, MailId.SNOWBALL_MAIL_3_VALUE, title, subTitle, params);
		}
	}
	
	private String getGoalRewadKey(int termId, int turnId, String playerId) {
		return ActivityRedisKey.SNOWBALL_GOAL_REWARD + ":" + termId + ":" + turnId + ":" + playerId;
	}
	
	/**
	 * 发阶段达成进球奖励
	 */
	public void sendStageGoalAward(SnowballGoalEvent event) {
		List<String> goalInfo = getGoalInfo(event.getPointId());
		int goalCount = goalInfo.size();

		// 发奖配置
		SnowballGoalCfg sendCfg = null;
		ConfigIterator<SnowballGoalCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(SnowballGoalCfg.class);
		while (cfgIterator.hasNext()) {
			SnowballGoalCfg cfg = cfgIterator.next();
			if (cfg.getTarget() != goalCount || cfg.getPointId() != event.getPointId()) {
				continue;
			}
			sendCfg = cfg;
		}

		if (sendCfg == null) {
			return;
		}

		// 放到一个map里面
		Map<String, Integer> goalMap = new HashMap<>();
		for (String playerId : goalInfo) {
			Integer value = goalMap.get(playerId);
			if (value == null) {
				goalMap.put(playerId, 1);
				continue;
			}
			goalMap.put(playerId, value + 1);
		}

		for (Entry<String, Integer> entry : goalMap.entrySet()) {
			if (entry.getValue() < sendCfg.getPlayerRewardValue()) {
				continue;
			}
			String playerId = entry.getKey();

			// 邮件：雪球进阶段奖励有阿金
			int[] pos = splitXAndY(event.getPointId());
			Object[] title = new Object[3];
			title[0] = pos[0];
			title[1] = pos[1];
			title[2] = getCurrentStage();
			
			Object[] subTitle = new Object[3];
			subTitle[0] = pos[0];
			subTitle[1] = pos[1];
			subTitle[2] = getCurrentStage();
			
			Object[] params = new Object[5];
			params[0] = pos[0];
			params[1] = pos[1];
			params[2] = getCurrentStage();
			params[3] = pos[0];
			params[4] = pos[1];
			getDataGeter().sendMail(playerId, MailId.SNOWBALL_MAIL_4, title, subTitle, params, sendCfg.getRewardList(), false);
		}
	}
	
	/**
	 * 发送个人排行奖励
	 */
	public void sendSelfRankReward(int termId) {
		try {
			selfRank.refreshRank(termId);

			MailId mailId = MailId.SNOWBALL_MAIL_5;
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			Set<Tuple> rankTuple = selfRank.getRankTuples();
			int rank = 0;
			for (Tuple tuple : rankTuple) {
				rank++;
				String playerId = tuple.getElement();
				SnowballRankRewardCfg rankCfg = getRankReward(rank, selfRank.rankType);
				if (rankCfg == null) {
					HawkLog.errPrintln("Snowball self rank cfg error! playerId: {}, rank :{}", playerId, rank);
					continue;
				}
				// 邮件发送奖励
				Object[] content;
				content = new Object[2];
				content[0] = getActivityCfg().getActivityName();
				content[1] = rank;

				sendMailToPlayer(playerId, mailId, title, subTitle, content, rankCfg.getRewardList());

				HawkLog.logPrintln("Snowball send self rankReward, playerId: {}, rank: {}, cfgId: {}", playerId, rank, rankCfg.getId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 发送联盟排行奖励
	 */
	public void sendGuildRankReward(int termId) {
		try {
			guildRank.refreshRank(termId);

			MailId mailId = MailId.SNOWBALL_MAIL_6;
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			Set<Tuple> rankTuple = guildRank.getRankTuples();
			int rank = 0;
			for (Tuple tuple : rankTuple) {
				rank++;
				String guildId = tuple.getElement();
				SnowballRankRewardCfg rankCfg = getRankReward(rank, guildRank.rankType);
				if (rankCfg == null) {
					HawkLog.errPrintln("Snowball guild rank cfg error! guildId: {}, rank :{}", guildId, rank);
					continue;
				}
				// 邮件发送奖励
				Object[] content;
				content = new Object[2];
				content[0] = getActivityCfg().getActivityName();
				content[1] = rank;
				Collection<String> ids = getDataGeter().getGuildMemberIds(guildId);
				for (String playerId : ids) {
					sendMailToPlayer(playerId, mailId, title, subTitle, content, rankCfg.getRewardList());
					HawkLog.logPrintln("Snowball send guild rankReward, guildId: {}, playerId: {}, rank: {}, cfgId: {}", guildId, playerId, rank, rankCfg.getId());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 发统计邮件
	 */
	public void sendStatisticMail(String playerId, int trunId) {
		Optional<SnowballEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SnowballEntity entity = opEntity.get();
		
		if (entity.getTurnId() != 0) {
			try {
				// 邮件：推雪球成功
				
				Object[] title = new Object[1];
				title[0] = trunId;
				
				Object[] subTitle = new Object[1];
				subTitle[0] = trunId;
				
				Object[] params = new Object[7];
				params[0] = entity.getTurnId();
				params[1] = entity.getKickScore() + entity.getContinueKickScore() + entity.getAssisScore() + entity.getGoalScore() + entity.getGoalAssisScore();
				params[2] = entity.getKickScore();
				params[3] = entity.getContinueKickScore();
				params[4] = entity.getAssisScore();
				params[5] = entity.getGoalScore();
				params[6] = entity.getGoalAssisScore();
				getDataGeter().sendMail(playerId, MailId.SNOWBALL_MAIL_7, title, subTitle, params, null, false);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		clearTurnScore(entity);
		
		SnowballLastAtkPush.Builder builder = SnowballLastAtkPush.newBuilder();
		builder.setHasLastAtk(false);
		builder.setLastAtkNum(0);
		builder.setAtkTimes(0);
		pushToPlayer(playerId, HP.code.WORLD_SNOWBALL_END_VALUE, builder);
	}
	
	/**
	 * 发进球助攻积分邮件
	 */
	public void sendGoalAssistanceScoreMail(int pointId, String playerId, int score, int number) {
		// 邮件：雪球进球助攻奖励
		int[] pos = splitXAndY(pointId);
		
		Object[] title = new Object[2];
		title[0] = pos[0];
		title[1] = pos[1];
		
		Object[] subTitle = new Object[2];
		subTitle[0] = pos[0];
		subTitle[1] = pos[1];
		
		Object[] params = new Object[4];
		params[0] = pos[0];
		params[1] = pos[1];
		params[2] = number;
		params[3] = score;
		getDataGeter().sendMail(playerId, MailId.SNOWBALL_MAIL_18, title, subTitle, params, null, false);
	}
	
	/**
	 * 阶段检测
	 */
	public void stageNoticeTick() {
		if (!isOpening(null)) {
			stageId = 0;
			return;
		}
		
		if (!isInProgress()) {
			stageId = 0;
			return;
		}
		
		if (getCurrentStage() == stageId) {
			return;
		}
		
		stageId = getCurrentStage();
		
		SnowballStageCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SnowballStageCfg.class, stageId);
		if (cfg.getNoticeId() == 0) {
			return;
		}
		
		NoticeCfgId noticeId = NoticeCfgId.valueOf(cfg.getNoticeId());
		sendBroadcast(noticeId, null);
	}
	
	/**
	 * 轮次结束提前公告
	 */
	public void turnClosePreNotice() {
		if (!isOpening(null)) {
			return;
		}
		
		if (!isInProgress()) {
			return;
		}
		
		long realRemmainTime = getTurnRemainTime();
		long turnRemainNoticeTime = SnowballCfg.getInstance().getTurnRemainNoticeTime();
		if (turnRemainTime >= turnRemainNoticeTime && realRemmainTime < turnRemainNoticeTime) {
			sendBroadcast(NoticeCfgId.SNOWBALL_NOTICE_212, null);
		}
		turnRemainTime = realRemmainTime;
	}
	
	/**
	 * 获取该场次剩余时间
	 */
	public long getTurnRemainTime() {
		if (!isOpening(null)) {
			return 0L;
		}
		
		if (!isInProgress()) {
			return 0L;
		}
		
		long currentTime = HawkTime.getMillisecond();

		// 活动持续时间
		long continueTime = SnowballCfg.getInstance().getContinueTime();

		List<Long> refreshTimeArr = getRefreshTimeArr();
		for (Long refreshTime : refreshTimeArr) {
			if (currentTime >= refreshTime && currentTime <= refreshTime + continueTime) {
				return refreshTime + continueTime - currentTime;
			}
		}
		
		return 0L;
	}
}
