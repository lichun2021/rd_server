package com.hawk.activity.type.impl.stronestleader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.config.ActivityConfig;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.speciality.StrongestEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.rank.ActivityRank;
import com.hawk.activity.type.impl.rank.ActivityRankContext;
import com.hawk.activity.type.impl.rank.ActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.activity.type.impl.stronestleader.cfg.ActivityCircularCfg;
import com.hawk.activity.type.impl.stronestleader.cfg.ActivityCircularKVCfg;
import com.hawk.activity.type.impl.stronestleader.cfg.ActivityCircularRankRewardCfg;
import com.hawk.activity.type.impl.stronestleader.cfg.ActivityCircularTargetRewardCfg;
import com.hawk.activity.type.impl.stronestleader.entity.ActivityStrongestLeaderEntity;
import com.hawk.activity.type.impl.stronestleader.entity.StrongestLeaderGlobalData;
import com.hawk.activity.type.impl.stronestleader.rank.StrongestRank;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetContext;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetParser;
import com.hawk.game.protocol.Activity.HistoryRankList;
import com.hawk.game.protocol.Activity.HistoryStageRankPB;
import com.hawk.game.protocol.Activity.LeaderAllInfo;
import com.hawk.game.protocol.Activity.LeaderStageInfo;
import com.hawk.game.protocol.Activity.LearderTargetPB;
import com.hawk.game.protocol.Activity.MyScorePB;
import com.hawk.game.protocol.Activity.RankPB;
import com.hawk.game.protocol.Activity.StageMyRankPB;
import com.hawk.game.protocol.Activity.StageRankInfo;
import com.hawk.game.protocol.Activity.StageRankList;
import com.hawk.game.protocol.Activity.TotalRankList;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;

import redis.clients.jedis.Tuple;

/**
 * 最强指挥官
 * 
 * @author PhilChen
 *
 */
public class StrongestLeaderActivity extends ActivityBase {

	private StrongestLeaderGlobalData globalData;

	private Map<Integer, Map<String, StrongestRank>> playerHistoryStageMap = new HashMap<>();

	public StrongestLeaderActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.STRONGEST_LEADER;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new StrongestLeaderActivity(config.getActivityId(), activityEntity);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityStrongestLeaderEntity> queryList = HawkDBManager.getInstance()
				.query("from ActivityStrongestLeaderEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ActivityStrongestLeaderEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityStrongestLeaderEntity entity = new ActivityStrongestLeaderEntity(playerId, termId);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
	}

	/**
	 * 获取活动全局数据
	 * 
	 * @param stageId
	 * @return
	 */
	private StrongestLeaderGlobalData getGlobalData() {
		if (globalData == null) {
			String dataStr = ActivityLocalRedis.getInstance().get(ActivityRedisKey.STRONGEST_ACTIVITY_GLOBAL_DATA);
			if (HawkOSOperator.isEmptyString(dataStr)) {
				globalData = new StrongestLeaderGlobalData();
				initStageInfo(globalData);
				updateGlobalData(globalData);
				logger.debug("[strongest] create activity global data");
			} else {
				globalData = JSON.parseObject(dataStr, StrongestLeaderGlobalData.class);
			}
		}
		return globalData;
	}

	/**
	 * 更新redis活动全局数据
	 * 
	 * @param globalData
	 */
	private void updateGlobalData(StrongestLeaderGlobalData globalData) {
		String jsonString = JSON.toJSONString(globalData);
		ActivityLocalRedis.getInstance().set(ActivityRedisKey.STRONGEST_ACTIVITY_GLOBAL_DATA, jsonString);
	}

	public int getStageId() {
		return getGlobalData().getStageId();
	}

	@Override
	public void onShow() {
		// 重置活动数据
		// 重置排行榜
		ActivityRankProvider<StrongestRank> stageRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_STAGE_RANK, StrongestRank.class);
		ActivityRankProvider<StrongestRank> totalRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_TOTALL_RANK, StrongestRank.class);
		stageRankProvider.clean();
		totalRankProvider.clean();
		logger.debug("[strongest] activity show, clean rank list.");

		// 初始化玩家排名缓存数据
		Map<Integer, Map<String, StrongestRank>> playerHistoryStageMap = new HashMap<>();
		ConfigIterator<ActivityCircularCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityCircularCfg.class);
		while (configIterator.hasNext()) {
			ActivityCircularCfg next = configIterator.next();
			Map<String, StrongestRank> stageMap = new HashMap<>();
			playerHistoryStageMap.put(next.getStageId(), stageMap);
		}
		this.playerHistoryStageMap = playerHistoryStageMap;
	}

	@Override
	public void onOpen() {
		initStageInfo(getGlobalData());
	}

	private void initStageInfo(StrongestLeaderGlobalData stageGlobalData) {
		// 活动开始，初始进入第一阶段
		ActivityCircularCfg circularCfg = null;
		ConfigIterator<ActivityCircularCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityCircularCfg.class);
		while (configIterator.hasNext()) {
			ActivityCircularCfg next = configIterator.next();
			if (next.getOrder() == 1) {
				circularCfg = next;
				break;
			}
		}
		if (circularCfg == null) {
			logger.error("[strongest] ActivityCircularCfg order 1 not found.");
			return;
		}
		long now = HawkTime.getMillisecond();
		// 初始化活动全局信息
		// 切换到指定阶段
		toStage(now, stageGlobalData, circularCfg);
	}
	
	

	@Override
	public void onEnd() {
		// 活动结束，邮件发放最后一个阶段奖励
		ActivityRankProvider<StrongestRank> stageRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_STAGE_RANK, StrongestRank.class);
		ActivityCircularCfg currentStageCfg = HawkConfigManager.getInstance().getConfigByKey(ActivityCircularCfg.class, getStageId());
		if (currentStageCfg != null) {
			Object[] title = new Object[1];
			title[0] = currentStageCfg.getName();
			Object[] subTitle = new Object[2];
			subTitle[0] = currentStageCfg.getName();
			sendRankReward(currentStageCfg, currentStageCfg.getRankId(), MailId.STRONGEST_STAGE_RANK_AWARD, title, subTitle, stageRankProvider);
		}

		// 活动结束，邮件发放总排名奖励
		ActivityRankProvider<StrongestRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_TOTALL_RANK, StrongestRank.class);
		ActivityCircularKVCfg kvInstance = HawkConfigManager.getInstance().getKVInstance(ActivityCircularKVCfg.class);
		Object[] title = new Object[0];
		Object[] subTitle = new Object[0];
		sendRankReward(null, kvInstance.getCycleRankId(), MailId.STRONGEST_TOTAL_RANK_AWARD, title, subTitle, rankProvider);

		// 将总排名的前10记录到历史最强排名数据中
		rankProvider.doRankSort();
		List<StrongestRank> rankList = rankProvider.getRankList();
		JSONObject jsonObject = new JSONObject();
		long time = HawkTime.getMillisecond();
		jsonObject.put("time", time);
		jsonObject.put("rankList", rankList);
		String jsonString = jsonObject.toJSONString();

		ActivityLocalRedis.getInstance().lpush(ActivityRedisKey.STRONGEST_ACTIVITY_HISTORY_RANK_LIST, jsonString);
	}

	@Override
	public void onHidden() {
		ConfigIterator<ActivityCircularCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityCircularCfg.class);
		while (configIterator.hasNext()) {
			ActivityCircularCfg next = configIterator.next();
			ActivityLocalRedis.getInstance().del(ActivityRedisKey.STRONGEST_STAGE_RANK + ":" + next.getStageId());
		}
	}

	@Override
	public void onTick() {
		// 阶段切换驱动
		checkStageChange();
	}
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		initPlayerStageData(playerId, null);
	}

	@Subscribe
	public void onEvent(StrongestEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		StrongestLeaderGlobalData globalData = getGlobalData();
		long now = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		// 已过活动开启时间,不再接受活动积分时间,积分不再增长
		if (now >= endTime) {
			return;
		}
		// 阶段尚未开始
		if (globalData.getStageStartTime() <= 0 || now < globalData.getStageStartTime()) {
			return;
		}
		
		// 上一个阶段过了，没有及时刷新（只在trunk，还未合并debug）
		//if (globalData.getStageEndTime() > 0 && now > globalData.getStageEndTime()) {
		//	return;
		//}
		
		int stageId = globalData.getStageId();
		ActivityCircularCfg stageCfg = HawkConfigManager.getInstance().getConfigByKey(ActivityCircularCfg.class, stageId);

		List<StrongestTargetParser<?>> parsers = StrongestTargetContext.getParser(event.getClass());
		if (parsers == null) {
			logger.error("StrongestTargetParser not found, targetType: {}", stageCfg.getTargetType());
			return;
		}
		
		Optional<ActivityStrongestLeaderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityStrongestLeaderEntity entity = opEntity.get();
		boolean update = false;
		for (StrongestTargetParser<?> parser : parsers) {
			if (parser.getTargetType() != stageCfg.getTargetType()) {
				continue;
			}
			initPlayerStageData(playerId, event);
			if (parser.onEvent(entity, stageCfg, event.convert())) {
				update = true;
			}
		}
		if (update) {
			// 推送积分变化
			pushTargetScoreChange(playerId, entity.getTotalScore());
			// 判断是否达成
			int factoryLevel = getDataGeter().getConstructionFactoryLevel(playerId);
			for (Integer targetId : entity.getTargetList()) {
				if (entity.isAchieveTarget(targetId)) {
					continue;
				}
				ActivityCircularTargetRewardCfg targetCfg = HawkConfigManager.getInstance().getConfigByKey(ActivityCircularTargetRewardCfg.class, targetId);
				if (targetCfg == null) {
					logger.error("ActivityCircularTargetRewardCfg not found, targetId: {} factoryLevel: {}", targetId, factoryLevel);
					continue;
				}
				if (entity.getTotalScore() >= targetCfg.getScore()) {
					// 记录达成目标
					entity.addAchieveTarget(targetId);
					// 邮件发放奖励
					Object[] title = new Object[1];
					title[0] = stageCfg.getName();
					Object[] subTitle = new Object[1];
					subTitle[0] = stageCfg.getName();
					Object[] content = new Object[3];
					content[0] = getActivityCfg().getActivityName();
					content[1] = stageCfg.getName();
					content[2] = targetCfg.getScore();
					sendMailToPlayer(playerId, MailId.STRONGEST_TARGET_AWARD, title, subTitle, content, targetCfg.getRewardList());
					
					ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), MailId.STRONGEST_TARGET_AWARD_VALUE);
				}
			}
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					// 更新阶段排行榜信息
					ActivityRankProvider<StrongestRank> stageRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_STAGE_RANK, StrongestRank.class);
					if (stageRankProvider != null) {
						StrongestRank rank = StrongestRank.valueOf(playerId, entity.getTotalScore());
						stageRankProvider.insertIntoRank(rank);
						getDataGeter().strongestLeaderScoreRecord(playerId, ActivityRankType.STRONGEST_STAGE_RANK, stageId, entity.getTotalScore());
					} else {
						logger.error("activity rank provider is null, rankType: {}", ActivityRankType.STRONGEST_STAGE_RANK);
					}
					// 刷新总排行数值
					ActivityRankProvider<StrongestRank> totalRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_TOTALL_RANK, StrongestRank.class);
					if (totalRankProvider != null) {
						long totalScore = getPlayerTotalScore(entity);
						StrongestRank rank = StrongestRank.valueOf(playerId, totalScore);
						totalRankProvider.insertIntoRank(rank);
						getDataGeter().strongestLeaderScoreRecord(playerId, ActivityRankType.STRONGEST_TOTALL_RANK, stageId, totalScore);
					} else {
						logger.error("activity rank provider is null, rankType: {}", ActivityRankType.STRONGEST_TOTALL_RANK);
					}
					return null;
				}
			});
		}
	}

	/**
	 * 获取玩家所有阶段总积分
	 * 
	 * @param playerId
	 * @return
	 */
	private long getPlayerTotalScore(ActivityStrongestLeaderEntity entity) {
		long totalScore = 0;

		// 历史阶段累计积分
		ActivityCircularCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ActivityCircularCfg.class, entity.getStageId());
		ConfigIterator<ActivityCircularCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityCircularCfg.class);
		while (configIterator.hasNext()) {
			ActivityCircularCfg next = configIterator.next();
			if (next.getOrder() >= cfg.getOrder()) {
				continue;
			}
			StrongestRank stageRank = getPlayerHistoryStageRank(entity.getPlayerId(), next.getStageId());
			long score = next.getScoreWeightCof() * stageRank.getScore() / 10000;
			totalScore += score;
		}

		// 添加当前阶段积分
		ActivityCircularCfg stageCfg = cfg;
		totalScore += stageCfg.getScoreWeightCof() * entity.getTotalScore() / 10000;
		return totalScore;
	}

	private void checkStageChange() {
		// 检测当前时间，是否进行阶段切换
		long now = HawkTime.getMillisecond();
		StrongestLeaderGlobalData globalData = getGlobalData();
		if (globalData.getStageStartTime() <= 0 || now < globalData.getStageEndTime()) {
			return;
		}
		ActivityCircularCfg currentStageCfg = HawkConfigManager.getInstance().getConfigByKey(ActivityCircularCfg.class, globalData.getStageId());
		if (currentStageCfg == null) {
			logger.error("[strongest] ActivityCircularCfg not found, stageId: {}", globalData.getStageId());
			return;
		}
		ActivityCircularCfg nextStageCfg = null;
		ConfigIterator<ActivityCircularCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityCircularCfg.class);
		while (configIterator.hasNext()) {
			ActivityCircularCfg next = configIterator.next();
			if (currentStageCfg.getOrder() + 1 == next.getOrder()) {
				nextStageCfg = next;
				break;
			}
		}
		if (nextStageCfg == null) {
			// 不存在下一个阶段配置
			return;
		}

		// 邮件发放阶段奖励
		ActivityRankProvider<StrongestRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_STAGE_RANK, StrongestRank.class);
		Object[] title = new Object[1];
		title[0] = currentStageCfg.getName();
		Object[] subTitle = new Object[1];
		subTitle[0] = currentStageCfg.getName();
		sendRankReward(currentStageCfg, currentStageCfg.getRankId(), MailId.STRONGEST_STAGE_RANK_AWARD, title, subTitle, rankProvider);
		// 切换到下一个阶段
		toStage(now, globalData, nextStageCfg);

		// 刷新总排名列表
		ActivityRankProvider<StrongestRank> totalRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_TOTALL_RANK, StrongestRank.class);
		totalRankProvider.doRankSort();
	}

	/**
	 * 发送阶段排名邮件奖励
	 * 
	 * @param stageCfg
	 */
	private void sendRankReward(ActivityCircularCfg stageCfg, int rankId, MailId mailId, Object[] title, Object[] subTitle, ActivityRankProvider<StrongestRank> rankProvider) {
		List<ActivityCircularRankRewardCfg> rankListReward = getRankListReward(rankId);
		for (ActivityCircularRankRewardCfg rewardCfg : rankListReward) {
			int highRank = rewardCfg.getRankUpper();
			int lowRank = rewardCfg.getRankLower();
			List<Builder> rewardList = rewardCfg.getRewardList();
			List<StrongestRank> rankPlayers = rankProvider.getRanks(highRank, lowRank);
			for (StrongestRank strongestRank : rankPlayers) {
				// 邮件发送奖励
				Object[] content;
				if (stageCfg != null) {
					content = new Object[3];
					content[0] = getActivityCfg().getActivityName();
					content[1] = stageCfg.getName();
					content[2] = strongestRank.getRank();
				} else {
					content = new Object[2];
					content[0] = getActivityCfg().getActivityName();
					content[1] = strongestRank.getRank();
				}
				
				sendMailToPlayer(strongestRank.getId(), mailId, title, subTitle, content, rewardList);
				ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(strongestRank.getId(), ActivityBtns.ActivityChildCellBtn, getActivityType(),
						mailId.getNumber());
				logger.info("strongest send rank reward, playerId: {}, rank: {}, score: {}, cfgId: {}, rankId: {}", strongestRank.getId(), strongestRank.getRank(),
						strongestRank.getScore(), rewardCfg.getId(), rewardCfg.getRankId());
			}
		}
	}

	private List<ActivityCircularRankRewardCfg> getRankListReward(int rankId) {
		List<ActivityCircularRankRewardCfg> list = new ArrayList<>();
		ConfigIterator<ActivityCircularRankRewardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityCircularRankRewardCfg.class);
		while (configIterator.hasNext()) {
			ActivityCircularRankRewardCfg next = configIterator.next();
			if (next.getRankId() == rankId) {
				list.add(next);
			}
		}
		return list;
	}

	/**
	 * 切换到指定阶段
	 * 
	 * @param now
	 * @param globalData
	 * @param nextStageCfg
	 */
	private void toStage(long now, StrongestLeaderGlobalData globalData, ActivityCircularCfg nextStageCfg) {
		int oldStageId = globalData.getStageId();
		globalData.setStageId(nextStageCfg.getStageId());
		long stageStartTime = getStageStartTime(now, nextStageCfg);
		long stageEndTime = stageStartTime + nextStageCfg.getContinueTime();
		globalData.setStageStartTime(stageStartTime);
		globalData.setStageEndTime(stageEndTime);
		updateGlobalData(globalData);
		if (nextStageCfg.getOrder() > 1) {
			// 重置当前阶段的排名信息
			ActivityRankProvider<StrongestRank> stageRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_STAGE_RANK, StrongestRank.class);
			stageRankProvider.clean();
		}
		logger.debug("[strongest] change to stage, oldStageId: {}, currentStageId: {}, stageStartTime: {}, stageEndTime: {}", oldStageId, globalData.getStageId(),
				HawkTime.formatTime(stageStartTime), HawkTime.formatTime(stageEndTime));
	}
	
	/**
	 * 获取当前阶段的开启时间
	 * @param now
	 * @param nextStageCfg
	 * @return
	 */
	private long getStageStartTime(long now, ActivityCircularCfg nextStageCfg){
		int termId = getActivityTermId();
		long openTime = getTimeControl().getStartTimeByTermId(termId);
		ConfigIterator<ActivityCircularCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityCircularCfg.class);
		long startTime = openTime;
		for(ActivityCircularCfg cfg : configIterator){
			if(cfg.getOrder() < nextStageCfg.getOrder()){
				startTime += cfg.getPrepareTime() + cfg.getContinueTime();
			}
		}
		startTime += nextStageCfg.getPrepareTime();
		return startTime;
	}

	/**
	 * 获取当前阶段所有信息
	 * 
	 * @param playerId
	 */
	public void pushStageInfo(String playerId) {
		// 更新玩家的活动阶段信息
		initPlayerStageData(playerId, null);

		Optional<ActivityStrongestLeaderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityStrongestLeaderEntity entity = opEntity.get();
		StrongestLeaderGlobalData globalData = getGlobalData();
		int stageId = globalData.getStageId();
		long stageStartTime = globalData.getStageStartTime();
		long stageEndTime = globalData.getStageEndTime();

		LeaderAllInfo.Builder builder = LeaderAllInfo.newBuilder();
		LeaderStageInfo.Builder stageInfoBuilder = LeaderStageInfo.newBuilder();
		StageRankList.Builder stageRankListBuilder = StageRankList.newBuilder();
		TotalRankList.Builder totalRankListBuilder = TotalRankList.newBuilder();

		stageInfoBuilder.setStageEndTime(stageEndTime);
		stageInfoBuilder.setStageStartTime(stageStartTime);
		MyScorePB scorePB = MyScorePB.newBuilder().setScore(entity.getTotalScore()).build();
		stageInfoBuilder.setScore(scorePB);
		stageInfoBuilder.setStageId(stageId);
		ActivityCircularCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ActivityCircularCfg.class, stageId);
		if (cfg == null) {
			logger.error("[strongest] ActivityCircularCfg not found, stageId: {}", stageId);
		}
		// 阶段目标信息
		for (Integer targetId : entity.getTargetList()) {
			ActivityCircularTargetRewardCfg targetCfg = HawkConfigManager.getInstance().getConfigByKey(ActivityCircularTargetRewardCfg.class, targetId);
			LearderTargetPB.Builder targetBuilder = LearderTargetPB.newBuilder();
			targetBuilder.setTargetId(targetId);
			targetBuilder.setTargetScore(targetCfg.getScore());
			boolean achieveTarget = entity.isAchieveTarget(targetCfg.getTargetId());
			targetBuilder.setIsAchieve(achieveTarget);
			stageInfoBuilder.addTarget(targetBuilder);
		}

		// 我的所有阶段名次列表
		ConfigIterator<ActivityCircularCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityCircularCfg.class);
		while (configIterator.hasNext()) {
			ActivityCircularCfg next = configIterator.next();
			if (next.getOrder() >= cfg.getOrder()) {
				continue;
			}
			StrongestRank stageRank = getPlayerHistoryStageRank(playerId, next.getStageId());
			StageMyRankPB.Builder myRankPBBuillder = StageMyRankPB.newBuilder();
			myRankPBBuillder.setStageId(next.getStageId());
			myRankPBBuillder.setRank(stageRank.getRank());
			stageRankListBuilder.addMyStageRank(myRankPBBuillder);
		}
		// 当前阶段玩家的排名
		ActivityRankProvider<StrongestRank> stageRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_STAGE_RANK, StrongestRank.class);
		StrongestRank playerStageRank = stageRankProvider.getRank(playerId);
		StageMyRankPB.Builder myRankPBBuillder = StageMyRankPB.newBuilder();
		myRankPBBuillder.setStageId(stageId);
		myRankPBBuillder.setRank(playerStageRank.getRank());
		stageRankListBuilder.addMyStageRank(myRankPBBuillder);

		configIterator.resetIterator();
		// 获取历史个阶段的排行列表
		while (configIterator.hasNext()) {
			ActivityCircularCfg next = configIterator.next();
			if (next.getOrder() >= cfg.getOrder()) {
				continue;
			}
			String key = ActivityRedisKey.STRONGEST_STAGE_RANK + ":" + next.getStageId();
			Set<Tuple> ranks = ActivityLocalRedis.getInstance().zrevrange(key, 0, ActivityConfig.getInstance().getActivityCircularRankSize());
			StageRankInfo.Builder stageRankInfoBuilder = StageRankInfo.newBuilder();
			stageRankInfoBuilder.setStageId(next.getStageId());
			int index = 1;
			for (Tuple tuple : ranks) {
				long score = (long) tuple.getScore();
				StrongestRank strongestRank = StrongestRank.valueOf(tuple.getElement(), score, index);
				RankPB.Builder playerRankPB = buildPlayerRankPB(strongestRank);
				if (playerRankPB != null) {
					stageRankInfoBuilder.addRankList(playerRankPB);
					index++;
				}
			}
			stageRankListBuilder.addAllStageList(stageRankInfoBuilder);
		}
		// 加入当前阶段排行榜
		List<StrongestRank> rankList = stageRankProvider.getRankList();
		StageRankInfo.Builder stageRankInfoBuilder = StageRankInfo.newBuilder();
		stageRankInfoBuilder.setStageId(stageId);
		for (StrongestRank strongestRank : rankList) {
			RankPB.Builder playerRankPB = buildPlayerRankPB(strongestRank);
			if (playerRankPB != null) {
				stageRankInfoBuilder.addRankList(playerRankPB);
			}
		}
		stageRankListBuilder.addAllStageList(stageRankInfoBuilder);

		// 总排行列表
		ActivityRankProvider<StrongestRank> totalRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_TOTALL_RANK, StrongestRank.class);
		List<StrongestRank> totalRankList = totalRankProvider.getRankList();

		for (StrongestRank activityRank : totalRankList) {
			RankPB.Builder rankPBBuilder = buildPlayerRankPB(activityRank);
			if (rankPBBuilder != null) {
				totalRankListBuilder.addRankList(rankPBBuilder);
			}
		}
		// 总排行中我的排行
		ActivityRank playerRank = totalRankProvider.getRank(playerId);
		if (playerRank != null) {
			RankPB.Builder rankPBBuilder = buildPlayerRankPB(playerRank);
			if (rankPBBuilder != null) {
				totalRankListBuilder.setMyRank(rankPBBuilder);
			}
		}

		builder.setStageInfo(stageInfoBuilder.build());
		builder.setStageRankList(stageRankListBuilder.build());
		builder.setTotalRankList(totalRankListBuilder.build());
		pushToPlayer(playerId, HP.code.PULL_LEADER_STAGE_INFO_S_VALUE, builder);
	}

	private StrongestRank getPlayerHistoryStageRank(String playerId, int stageId) {
		if (playerHistoryStageMap.isEmpty()) {
			Map<Integer, Map<String, StrongestRank>> playerHistoryStageMap = new HashMap<>();
			ConfigIterator<ActivityCircularCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityCircularCfg.class);
			while (configIterator.hasNext()) {
				ActivityCircularCfg next = configIterator.next();
				Map<String, StrongestRank> map = new HashMap<>();
				playerHistoryStageMap.put(next.getStageId(), map);
			}
			this.playerHistoryStageMap = playerHistoryStageMap;
		}
		Map<String, StrongestRank> stageMap = playerHistoryStageMap.get(stageId);
		StrongestRank strongestRank = stageMap.get(playerId);
		if (strongestRank != null) {
			return strongestRank;
		}
		RedisIndex playerRank = ActivityLocalRedis.getInstance().zrevrank(ActivityRedisKey.STRONGEST_STAGE_RANK + ":" + stageId, playerId);
		if (playerRank != null) {
			long score = playerRank.getScore().longValue();
			strongestRank = StrongestRank.valueOf(playerId, score, playerRank.getIndex().intValue() + 1);
		} else {
			strongestRank = StrongestRank.valueOf(playerId, 0, 0);
		}
		stageMap.put(playerId, strongestRank);
		return strongestRank;
	}

	/**
	 * 尝试初始化玩家阶段数据
	 * 
	 * @param playerId
	 * @param event 
	 */
	private void initPlayerStageData(String playerId, StrongestEvent event) {
		ActivityDataProxy dataGeter = getDataGeter();
		int stageId = getStageId();
		Optional<ActivityStrongestLeaderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityStrongestLeaderEntity entity = opEntity.get();
		// 阶段未发生变化,不进行处理
		if (entity.getStageId() == stageId) {
			return;
		}

		ActivityCircularCfg currencfg = HawkConfigManager.getInstance().getConfigByKey(ActivityCircularCfg.class, stageId);
		if (currencfg == null) {
			logger.error("ActivityCircularCfg is not found, stageId: {}", stageId);
			return;
		}

		entity.setStageId(stageId);
		entity.cleanTarget();
		entity.setScore(0);
		// 初始化目标列表
		int factoryLevel = dataGeter.getConstructionFactoryLevel(playerId);
		for (Integer targetId : currencfg.getTargetIdList()) {
			ConfigIterator<ActivityCircularTargetRewardCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityCircularTargetRewardCfg.class);
			while (cfgIterator.hasNext()) {
				ActivityCircularTargetRewardCfg targetCfg = cfgIterator.next();
				if (targetCfg.getTargetId() != targetId) {
					continue;
				}
				if (factoryLevel < targetCfg.getLvMin() || factoryLevel > targetCfg.getLvMax()) {
					continue;
				}
				entity.addTargetId(targetCfg.getId());
			}
		}

		// 初始填充目标初始数据
		StrongestTargetParser<?> parser = StrongestTargetContext.getParser(currencfg.getTargetType());
		if (parser == null) {
			logger.error("StrongestTargetParser is not found, targetType: {}", currencfg.getTargetType());
			return;
		}
		parser.recordPlayerData(dataGeter, playerId, entity, event == null ? null : event.convert());

	}

	/**
	 * 组装排名协议数据
	 * 
	 * @param playerRank
	 * @return
	 */
	private RankPB.Builder buildPlayerRankPB(ActivityRank playerRank) {
		ActivityDataProxy dataGeter = getDataGeter();
		if (!dataGeter.checkPlayerExist(playerRank.getId())) {
			return null;
		}
		RankPB.Builder rankPBBuilder = RankPB.newBuilder();
		try {
			String playerName = dataGeter.getPlayerName(playerRank.getId());
			List<Integer> protectVals = dataGeter.getPersonalProtectVals(playerRank.getId());
			String guildName = dataGeter.getGuildTagByPlayerId(playerRank.getId());
			rankPBBuilder.setPlayerName(playerName);
			rankPBBuilder.addAllPersonalProtectSwitch(protectVals);
			if (!HawkOSOperator.isEmptyString(guildName)) {
				rankPBBuilder.setGuildName(guildName);
			}
		} catch (Exception e) {
			HawkException.catchException(e, playerRank.getId());
		}
		
		rankPBBuilder.setPlayerId(playerRank.getId());
		rankPBBuilder.setRank(playerRank.getRank());
		rankPBBuilder.setScore(playerRank.getScore());
		return rankPBBuilder;
	}

	/**
	 * 获取历史最强指挥官每期信息
	 * 
	 * @param playerId
	 */
	public void pushHistoryRankList(String playerId) {
		List<String> historyList = ActivityLocalRedis.getInstance().lall(ActivityRedisKey.STRONGEST_ACTIVITY_HISTORY_RANK_LIST);
		Collections.reverse(historyList);
		int stageIndex = 1;
		HistoryRankList.Builder builder = HistoryRankList.newBuilder();
		for (String string : historyList) {
			JSONObject jsonObject = JSON.parseObject(string);
			JSONArray array = jsonObject.getJSONArray("rankList");
			long time = jsonObject.getLongValue("time");
			HistoryStageRankPB.Builder stageRankPBBuilder = HistoryStageRankPB.newBuilder();
			stageRankPBBuilder.setStageIndex(stageIndex);
			stageRankPBBuilder.setTime(HawkTime.formatTime(time, HawkTime.FORMAT_YMD));
			for (Object rankObj : array) {
				StrongestRank strongestRank = JSON.parseObject(rankObj.toString(), StrongestRank.class);
				RankPB.Builder rankPBBuilder = buildPlayerRankPB(strongestRank);
				if (rankPBBuilder != null) {
					stageRankPBBuilder.addRankList(rankPBBuilder);
				}
			}
			builder.addStageList(stageRankPBBuilder);
			stageIndex++;
		}

		pushToPlayer(playerId, HP.code.PULL_LEADER_HISTORY_RANK_LIST_S_VALUE, builder);
	}

	/**
	 * 推送积分信息变化
	 * 
	 * @param playerId
	 * @param score
	 */
	public void pushTargetScoreChange(String playerId, long score) {
		MyScorePB.Builder builder = MyScorePB.newBuilder();
		builder.setScore(score);
		pushToPlayer(playerId, HP.code.PUSH_LEADER_TARGET_SCORE_CHANGE_VALUE, builder);
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<ActivityStrongestLeaderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityStrongestLeaderEntity entity = opEntity.get();
		// 活动积分记为0分
		entity.setScore(0);
		// 从阶段排行中移除
		ActivityRankProvider<StrongestRank> stageRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_STAGE_RANK, StrongestRank.class);
		if (stageRankProvider != null) {
			stageRankProvider.remMember(playerId);
			stageRankProvider.doRankSort();
		}
		// 从总排行中移除
		ActivityRankProvider<StrongestRank> totalRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_TOTALL_RANK, StrongestRank.class);
		if (totalRankProvider != null) {
			totalRankProvider.remMember(playerId);
			totalRankProvider.doRankSort();
		}
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<ActivityStrongestLeaderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityStrongestLeaderEntity entity = opEntity.get();
		// 更新阶段排行榜信息
		ActivityRankProvider<StrongestRank> stageRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_STAGE_RANK, StrongestRank.class);
		if (stageRankProvider != null) {
			if (entity.getTotalScore() > 0) {
				StrongestRank rank = StrongestRank.valueOf(playerId, entity.getTotalScore());
				stageRankProvider.insertIntoRank(rank);
			}
		}
		// 刷新总排行数值
		ActivityRankProvider<StrongestRank> totalRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_TOTALL_RANK, StrongestRank.class);
		if (totalRankProvider != null) {
			long totalScore = getPlayerTotalScore(entity);
			if (totalScore > 0) {
				StrongestRank rank = StrongestRank.valueOf(playerId, totalScore);
				totalRankProvider.insertIntoRank(rank);
			}
		}
	}
	
	/**
	 * 移除排行榜数据
	 */
	public void removePlayerRank(String playerId) {
		if (this.getActivityEntity().getActivityState() == com.hawk.activity.type.ActivityState.HIDDEN) {
			return;
		}
		// 从阶段排行中移除
		ActivityRankProvider<StrongestRank> stageRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_STAGE_RANK, StrongestRank.class);
		if (stageRankProvider != null) {
			stageRankProvider.remMember(playerId);
			stageRankProvider.doRankSort();
		}
		// 从总排行中移除
		ActivityRankProvider<StrongestRank> totalRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.STRONGEST_TOTALL_RANK, StrongestRank.class);
		if (totalRankProvider != null) {
			totalRankProvider.remMember(playerId);
			totalRankProvider.doRankSort();
		}
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
}
