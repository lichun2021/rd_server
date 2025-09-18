package com.hawk.activity.type.impl.blood_corps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.blood_corps.cfg.BloodCorpsAchieveCfg;
import com.hawk.activity.type.impl.blood_corps.cfg.BloodCorpsActivityKVCfg;
import com.hawk.activity.type.impl.blood_corps.cfg.BloodCorpsRankRewardCfg;
import com.hawk.activity.type.impl.blood_corps.entity.BloodCorpsEntity;
import com.hawk.activity.type.impl.blood_corps.rank.BloodRankObject;
import com.hawk.game.protocol.Activity.BloodRankListResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.gamelib.player.PowerChangeData;
import com.hawk.gamelib.player.PowerData;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;

import redis.clients.jedis.Tuple;

/**
 * 铁血军团活动
 * @author Jesse
 *
 */
public class BloodCorpsActivity extends ActivityBase implements AchieveProvider {
	
	static final Logger logger = LoggerFactory.getLogger("Server");
	
	private long lastCheckTime = 0;
	
	private BloodRankObject rankObject= new BloodRankObject();

	public BloodCorpsActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return !isHidden(playerId);
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_BLOOD_CORPS, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	@Override
	public void onEnd() {
		int termId = getActivityTermId();
		rankObject.refreshRank(termId);
		sendRankReward();
	}
	
	@Override
	public void onHidden() {
		int termId = getActivityTermId();
		rankObject.clearRank(termId);
		ActivityLocalRedis.getInstance().del(ActivityRedisKey.BLOOD_CORPS_CHECK_TIME + termId);
	}
	
	@Subscribe 
	public void onPowerChange(BattlePointChangeEvent event) {
		long currTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		if (currTime >= endTime) {
			return;
		}
		
		PowerChangeReason reason = event.getReason();
		// 伤兵治愈战力变化,不进行更新
		if (event.isSoliderCure() || reason == PowerChangeReason.LMJY_INIT || reason == PowerChangeReason.NATION_HOSPITAL_COLLECT) {
			return;
		}
		PowerChangeData changeData = event.getChangeData();
		String playerId = event.getPlayerId();
		Optional<BloodCorpsEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		// 玩家当前战力数据
		PowerData  powerData = event.getPowerData();
		// 如果玩家当前建筑战力,等于此次建筑变化战力,则此次战力变化事件不进行处理
		if(powerData.getBuildBattlePoint() == changeData.getBuildBattleChange()){
			HawkLog.logPrintln("BloodCorpsActivity discard powerChange, playerId: {}, changeData: {}", playerId, changeData);
			return;
		}
		// 只记录建筑+科技+部队的战力增量
		int techAdd = Math.max(0, changeData.getTechBattleChange() + changeData.getPlantScienceBattlePoint());
		int buildingAdd = changeData.getBuildBattleChange();
		BloodCorpsEntity dataEntity = opDataEntity.get();
		
		if (techAdd > 100000 || buildingAdd > 100000) {
			HawkLog.logPrintln("BloodCorpsActivity value error, playerId:{}, techAdd:{}, buildingAdd:{}", playerId, techAdd, buildingAdd);
			return;
		}
		
		int armyAdd = Math.max(0, changeData.getArmyBattleChange());
		// 除了初始化/奖励/训练/晋升士兵外,其他来源的士兵战力变化不计入分数
		if (armyAdd > 0 && !(reason == PowerChangeReason.INIT_SOLDIER || reason == PowerChangeReason.AWARD_SOLDIER || reason == PowerChangeReason.TRAIN_SOLDIER)) {
			armyAdd = 0;
			changeData.setArmyBattleChange(0);
		}
		
		int totalAdd = techAdd + buildingAdd + armyAdd;
		if (totalAdd != 0) {
			String redisKey = ActivityRedisKey.BLOOD_CORPS_RANK + getActivityTermId();
			double result = ActivityLocalRedis.getInstance().zIncrby(redisKey, playerId, totalAdd);
			HawkLog.logPrintln("BloodCorpsActivity powerscore add, playerId: {}, techAdd: {}, buildingAdd: {}, armyAdd:{}, result: {}", playerId, techAdd,buildingAdd,armyAdd, result);
		}
		
		dataEntity.addScore(changeData);
		pushToPlayer(playerId, HP.code.BLOOD_SCORE_INFO_SYNC_VALUE, dataEntity.genScoreInfo());

	}
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		BloodCorpsActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BloodCorpsActivityKVCfg.class);
		// 不需要跨天重置
		if (!kvCfg.isScoreDailyReset()) {
			return;
		}
		
		if (!isOpening(event.getPlayerId())) {
			return;
		}

		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();

		Optional<BloodCorpsEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}

		BloodCorpsEntity dataEntity = opDataEntity.get();
		List<AchieveItem> items = new ArrayList<>();
		ConfigIterator<BloodCorpsAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(BloodCorpsAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			BloodCorpsAchieveCfg cfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			items.add(item);
		}
		dataEntity.resetItemList(items);
		dataEntity.resetScore();
		// 推送给客户端
		AchievePushHelper.pushAchieveUpdate(playerId, dataEntity.getItemList());
		// 推送活动积分数据
		pushToPlayer(playerId, HP.code.BLOOD_SCORE_INFO_SYNC_VALUE, dataEntity.genScoreInfo());

	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<BloodCorpsEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		BloodCorpsEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		// 初始添加成就项
		ConfigIterator<BloodCorpsAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(BloodCorpsAchieveCfg.class);
		while (configIterator.hasNext()) {
			BloodCorpsAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}

		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<BloodCorpsEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		BloodCorpsEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public BloodCorpsAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(BloodCorpsAchieveCfg.class, achieveId);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.BLOOD_CORPS_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_BLOOD_CORPS_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		BloodCorpsActivity activity = new BloodCorpsActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<BloodCorpsEntity> queryList = HawkDBManager.getInstance()
				.query("from BloodCorpsEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			BloodCorpsEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		BloodCorpsEntity entity = new BloodCorpsEntity(playerId, termId);
		return entity;
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	@Override
	public void onTick() {
		long currTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		long hiddenTime = getTimeControl().getHiddenTimeByTermId(termId);
		if (currTime >= hiddenTime) {
			return;
		}
		
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		if (currTime > endTime) {
			if (rankObject.getRankInfos().isEmpty()) {
				rankObject.refreshRank(termId);
			}
			return;
		}

		boolean hasChange = false;
		BloodCorpsActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BloodCorpsActivityKVCfg.class);
		long gap = kvCfg.getRankPeriod();
		if (lastCheckTime == 0) {
			String recodTime = ActivityLocalRedis.getInstance().get(ActivityRedisKey.BLOOD_CORPS_CHECK_TIME + termId);
			if (!HawkOSOperator.isEmptyString(recodTime)) {
				lastCheckTime = Long.valueOf(recodTime);
			}
		}
		// 如果跨天且需要跨天重置排行
		if (!HawkTime.isSameDay(currTime, lastCheckTime) && kvCfg.getIsRankDailyReset()) {
			rankObject.refreshRank(termId);
			sendRankReward();
			rankObject.clearRank(termId);
			lastCheckTime = currTime;
			hasChange = true;
		}

		if (currTime - lastCheckTime > gap) {
			rankObject.refreshRank(termId);
			lastCheckTime = currTime;
			hasChange = true;
		}
		if (hasChange) {
			ActivityLocalRedis.getInstance().set(ActivityRedisKey.BLOOD_CORPS_CHECK_TIME + getActivityTermId(), String.valueOf(lastCheckTime));
		}
	}
	
	
	/**
	 * 发送阶段排名邮件奖励
	 * 
	 * @param stageCfg
	 */
	private void sendRankReward() {
		MailId mailId = MailId.BLOOD_CORPS_RANK_REWARD;
		Object[] title = new Object[0];
		Object[] subTitle = new Object[0];
		BloodCorpsActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BloodCorpsActivityKVCfg.class);
		int scoreLimit = cfg.getRankRewardNeedPower();
		Set<Tuple> rankTuple = rankObject.getRankTuples();
		int rank = 0;
		for (Tuple tuple : rankTuple) {
			rank++;
			String playerId = tuple.getElement();
			long score = (long) tuple.getScore();
			if (score < scoreLimit) {
				continue;
			}
			BloodCorpsRankRewardCfg rankCfg = getRankReward(rank);
			if (rankCfg == null) {
				HawkLog.errPrintln("BloodCorpsActivity rank cfg error! playerId: {}, rank :{}", playerId, rank);
				continue;
			}
			
			try {
				// 邮件发送奖励
				Object[] content;
				content = new Object[2];
				content[0] = getActivityCfg().getActivityName();
				content[1] = rank;

				sendMailToPlayer(playerId, mailId, title, subTitle, content, rankCfg.getRewardList());
				logger.info("BloodCorpsActivity send rank reward, playerId: {}, rank: {}, score:{}, cfgId: {}", playerId, rank, score, rankCfg.getId());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 获取排行奖励配置
	 * @param rank
	 * @return
	 */
	private BloodCorpsRankRewardCfg getRankReward(int rank){
		BloodCorpsRankRewardCfg rankCfg = null;
		ConfigIterator<BloodCorpsRankRewardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(BloodCorpsRankRewardCfg.class);
		for(BloodCorpsRankRewardCfg cfg : configIterator){
			if(rank <=cfg.getRankLower() && rank>=cfg.getRankUpper()){
				rankCfg = cfg;
				break;
			}
		}
		return rankCfg;
	}
	
	/**
	 * 拉取排行列表
	 * @param playerId
	 */
	public void pullInfo(String playerId) {
		Optional<BloodCorpsEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		BloodCorpsEntity entity = opEntity.get();
		BloodRankListResp.Builder builder = rankObject.buildRankInfoList(entity);
		pushToPlayer(playerId, HP.code.GET_BLOOD_RANK_LIST_RESP_VALUE, builder);
	}
	
	/**
	 * 移除排行榜数据
	 */
	public void removePlayerRank(String playerId) {
		if (isActivityClose(null)) {
			return;
		}
		try {			
			rankObject.removeRank(getActivityTermId(), playerId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
}
