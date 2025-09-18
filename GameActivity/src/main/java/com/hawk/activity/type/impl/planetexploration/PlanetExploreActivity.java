package com.hawk.activity.type.impl.planetexploration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDaysActivityEvent;
import com.hawk.activity.event.impl.PlanetCollectEvent;
import com.hawk.activity.event.impl.PlanetExploreScoreEvent;
import com.hawk.activity.event.impl.PlanetPointDispearEvent;
import com.hawk.activity.event.impl.PlanetScoreAddEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.planetexploration.cache.PersonScore;
import com.hawk.activity.type.impl.planetexploration.cache.TreasureRefreshInfo;
import com.hawk.activity.type.impl.planetexploration.cfg.PlanetExploreAchieveCfg;
import com.hawk.activity.type.impl.planetexploration.cfg.PlanetExploreKVCfg;
import com.hawk.activity.type.impl.planetexploration.cfg.PlanetExploreRankCfg;
import com.hawk.activity.type.impl.planetexploration.cfg.PlanetExploreRewardCfg;
import com.hawk.activity.type.impl.planetexploration.entity.PlanetCollectInfo;
import com.hawk.activity.type.impl.planetexploration.entity.PlanetExploreEntity;
import com.hawk.activity.type.impl.planetexploration.rank.PlanetExploreRank;
import com.hawk.activity.type.impl.planetexploration.rank.impl.PersonRank;
import com.hawk.game.protocol.Activity.PersonalCollectInfo;
import com.hawk.game.protocol.Activity.PlanetCollectInfoSync;
import com.hawk.game.protocol.Activity.PlanetExploreInfo;
import com.hawk.game.protocol.Activity.PlanetExploreScoreRank;
import com.hawk.game.protocol.Activity.PlanetPointInfo;
import com.hawk.game.protocol.Activity.PlanetPointRefreshInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;

/***
 * 星能探索活动
 * 
 * @author lating
 */
public class PlanetExploreActivity extends ActivityBase implements AchieveProvider{
	/**
	 * 排行数据
	 */
	PlanetExploreRank<?> rankObj = new PersonRank();
	
	public static final int expireTime = 7 * 86400; //7天的秒数
	
	/**
	 * 矿点刷新信息
	 */
	private TreasureRefreshInfo refreshInfo = new TreasureRefreshInfo(); 
	
	/**
	 * 全服积分数据，为降低redis QPS，先在内存中缓存一个数，然后定期往redis更新
	 */
	private AtomicLong serverScore = new AtomicLong(-1);
	private long lastStoreTime = 0L;
	private long serverScoreLast = 0L;
	/**
	 * 同步锁
	 */
	private AtomicInteger lock = new AtomicInteger(0);
	private final static int TICK_RUNNING   = -1; //正在执行tick
	private final static int NORMAL          = 0; //正常值
	private final static int MERGE_PROCESSED = 1; //已执行合服处理逻辑
	
	public PlanetExploreActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PLANET_EXPLORE_347;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PlanetExploreActivity activity = new PlanetExploreActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PlanetExploreEntity> queryList = HawkDBManager.getInstance().query("from PlanetExploreEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PlanetExploreEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PlanetExploreEntity entity = new PlanetExploreEntity(playerId, termId);
		return entity;
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
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<PlanetExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		PlanetExploreEntity entity = opEntity.get();
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(PlanetExploreAchieveCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.PLANET_EXPLORE_ACHIEVE_REWARD;
	}
	
	@Override
	public void onOpen() {
		this.clear();
		Set<String> onlinePlayers = getDataGeter().getOnlinePlayers();
		for(String id : onlinePlayers){
			callBack(id, GameConst.MsgId.INIT_PLANET_EXPLORE_ACHIEVE, ()->{
				initAchieveItems(id);
			});
		}
	}
	
	/**
	 * 清除数据
	 */
	private void clear() {
		rankObj = new PersonRank();
		refreshInfo = new TreasureRefreshInfo(); 
		serverScore.set(-1);
		lastStoreTime = 0L;
	}
	
	@Override
	public void onTick() {
		HawkLog.debugPrintln("PlanetExploreActivity onTick into"); //每10秒tick一次
		
		lock.compareAndSet(NORMAL, TICK_RUNNING); //这里如果是正常值（没有执行过合服处理），则设置成-1表示ontick正在执行
		if (lock.get() == MERGE_PROCESSED) { //表示已经执行过合服处理了，这里不再执行tick
			return;
		}
		try {
			//起服后第一次，从redis加载数据
			if (serverScore.get() < 0) {
				getRefreshInfo();
				long score = getServerScore();
				serverScore.set(score);
			}
			
			try {
				rankObj.doRankSort(); //排序
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			//刷矿点时间检测
			long now = HawkTime.getMillisecond();
			treasureRefreshTick(now);
			
			if (lastStoreTime <= 0) {
				lastStoreTime = now;
				return;
			}
			
			//serverScore存redis
			if (now - lastStoreTime >= 120000L && serverScoreLast != serverScore.get()) {
				serverScoreLast = serverScore.get();
				lastStoreTime = now;
				storeServerScore();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			lock.compareAndSet(TICK_RUNNING, NORMAL); //ontick执行完，回归正常值
		}
	}
	
	/**
	 * 全服积分存redis
	 */
	private void storeServerScore() {
		String key = String.format(ActivityRedisKey.PLANET_EXPLORE_SERVER_SCORE, getActivityTermId());
		String val = String.valueOf(serverScore.get());
		ActivityLocalRedis.getInstance().getRedisSession().setString(key, val, expireTime);
	}
	
	/**
	 * 获取全服积分
	 * @return
	 */
	private long getServerScore() {
		String key = String.format(ActivityRedisKey.PLANET_EXPLORE_SERVER_SCORE, getActivityTermId());
		String val = ActivityLocalRedis.getInstance().getRedisSession().getString(key);
		return HawkOSOperator.isEmptyString(val) ? 0 : Long.valueOf(val);
	}
	
	/**
	 * 获取刷矿点信息
	 */
	private void getRefreshInfo() {
		String key = String.format(ActivityRedisKey.PLANET_EXPLORE_TREAREFRESH_INFO, getActivityTermId());
		String val = ActivityLocalRedis.getInstance().getRedisSession().getString(key);
		if (!HawkOSOperator.isEmptyString(val)) {
			refreshInfo = TreasureRefreshInfo.str2Object(val);
		}
	}
	
	/**
	 * 星能矿刷新信息存redis
	 */
	private void storeRefreshInfo() {
		String key = String.format(ActivityRedisKey.PLANET_EXPLORE_TREAREFRESH_INFO, getActivityTermId());
		String value = refreshInfo.toString();
		ActivityLocalRedis.getInstance().getRedisSession().setString(key, value, expireTime);
	}
	
	/**
	 * 结算积分，刷新矿点
	 * @param timeNow
	 */
	private void treasureRefreshTick(long timeNow) {
		if (refreshInfo.getNearRefreshTime() == 0) {
			refreshInfo.setNearRefreshTime(timeNow);
			storeRefreshInfo();
			return;
		}
		
		PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		long[] refreshTimeArr = cfg.getRefreshTimeArr();
		boolean refresh = false;
		for(long refreshTime : refreshTimeArr) {
			if (refreshTime <= timeNow && refreshTime > refreshInfo.getNearRefreshTime()) {
				refreshInfo.setNearRefreshTime(timeNow);
				refresh = true;
				break;
			}
		}
		
		if (!refresh) {
			return;
		}
		
		long serverScoreVal = serverScore.get();
		serverScoreVal = Math.max(serverScoreVal, 0);
		int refreshCount = (int) (serverScoreVal / cfg.getRefreshPoint());
		if (refreshCount > 0) {
			refreshInfo.setLastScore(refreshCount * cfg.getRefreshPoint());
		}
		int refreshCountNew = refreshCount - refreshInfo.getRefreshCount();
		if (refreshCountNew <= 0) {
			storeRefreshInfo();
			return;
		}
		
		refreshInfo.setRefreshCount(refreshCount);
		//刷新出矿点
		List<Integer> pointList = getDataGeter().planetExploreRefreshResPoint(refreshCountNew);
		refreshInfo.addPoint(timeNow, pointList);
		
		storeRefreshInfo();
		//termId, scoreNow, lastScore, refreshCount, refreshTotal
		Map<String, Object> param = new HashMap<>();
		param.put("termId", getActivityTermId());
        param.put("scoreNow", serverScoreVal);
        param.put("lastScore", refreshInfo.getLastScore());
        param.put("refreshTotal", refreshCount);
        param.put("refreshCount", refreshCountNew);
        getDataGeter().logActivityCommon(LogInfoType.planet_explore_refresh, param);
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<PlanetExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		long now = HawkTime.getMillisecond();
		PlanetExploreEntity entity = opEntity.get();
		if (HawkTime.isSameDay(entity.getDayTime(), now)) {
			return;
		}
		
		HawkLog.logPrintln("plant explore activity cross day, playerId: {}", playerId);
		entity.setDayTime(now);
		entity.setCollectCount(0);
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(playerId);
		} else {
			resetAchieveItems(playerId, entity); //只有部分成就是需要跨天重置的
		}
	}
	
	/**
	 * 采集事件
	 * @param event
	 */
	@Subscribe
	public void onCollectEvent(PlanetCollectEvent event){
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<PlanetExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		int posX = event.getPosX();
		int posY = event.getPosY();
		PlanetExploreEntity entity = opEntity.get();
		Optional<PlanetCollectInfo> optional = entity.getCollectInfoList().stream().filter(e -> e.getPosX() == posX && e.getPosY() == posY).findAny();
		PlanetCollectInfo collectInfo = null;
		if (optional.isPresent()) {
			collectInfo = optional.get();
		} else {
			collectInfo = PlanetCollectInfo.valueOf(posX, posY);
			entity.getCollectInfoList().add(collectInfo);
		}
		
		entity.setCollectCount(entity.getCollectCount() + event.getCollectNum());
		collectInfo.getCollectTimeCountMap().put(event.getTime(), event.getCollectNum());
		entity.notifyUpdate();
		HawkLog.logPrintln("planet explore collect event, playerId: {}, collectCount: {}, posX: {}, posY: {}", playerId, event.getCollectNum(), posX, posY);
		
		Map<String, Object> param = new HashMap<>();
		param.put("termId", getActivityTermId());
		param.put("posX", posX);
		param.put("posY", posY);
        param.put("collectNum", event.getCollectNum());
        param.put("collectTotal", entity.getCollectCount());
        getDataGeter().logActivityCommon(playerId, LogInfoType.planet_explore_collect, param);
	}
	
	/**
	 * 星能矿点消失事件
	 * @param event
	 */
	@Subscribe
	public void onPointDispearEvent(PlanetPointDispearEvent event){
		removePoint(event.getPlayerId(), event.getPosX(), event.getPosY());
	}
	
	/**
	 * 移除点
	 * @param playerId
	 * @param posX
	 * @param posY
	 */
	public void removePoint(String playerId, int posX, int posY) {
		if (refreshInfo != null) {
			int point = posX * 10000 + posY;
			refreshInfo.removePoint(point);
			HawkLog.logPrintln("PlanetExploreActivity onPointDispearEvent, playerId: {}, posX: {}, posY: {}", playerId, posX, posY);
			Map<String, Object> param = new HashMap<>();
			param.put("termId", getActivityTermId());
	        param.put("posX", posX);
	        param.put("posY", posY);
	        getDataGeter().logActivityCommon(playerId, LogInfoType.planet_explore_point_remove, param);
		}
	}
	
	/**
	 * 通过gm命令添加积分
	 * @param event
	 */
	@Subscribe
	public void onGMScoreAdd(PlanetScoreAddEvent event){
		String playerId = event.getPlayerId();
		int scoreAdd = event.getScoreAdd();
		boolean add2Person = event.isAdd2Person();
		if (scoreAdd <= 0) {
			HawkLog.errPrintln("PlanetExploreActivity onGMScoreAdd failed, playerId: {}, scoreAdd: {}", playerId, scoreAdd);
			return;
		}
		
		if(!isOpening(playerId)){
			return;
		}
		Optional<PlanetExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		PlanetExploreEntity entity = opEntity.get();
		PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		if (add2Person) {
			entity.scoreAdd(scoreAdd);
			rankObj.addScore(scoreAdd, playerId);
			//个人累计积分, 成就那边要加上
			int score = (int)Math.min(Integer.MAX_VALUE, entity.getScore());
			ActivityManager.getInstance().postEvent(new PlanetExploreScoreEvent(playerId, score)); //这里就是要传全量，不传增量，增量有风险
		}
		
		//全服积分要加上
		if (serverScore.get() < cfg.getServerMaxPoint()) {
			int add = (int) Math.min(scoreAdd, cfg.getServerMaxPoint() - serverScore.get());
			if (add > 0) {
				serverScore.addAndGet(add);
			}
		}
		//考虑到并发
		if (serverScore.get() > cfg.getServerMaxPoint()) {
			serverScore.set(cfg.getServerMaxPoint());
		}
		HawkLog.logPrintln("PlanetExploreActivity onGMScoreAdd, playerId: {}, scoreAdd: {}, new personScore: {}, new serverScore: {}", playerId, scoreAdd, entity.getScore(), serverScore.get());
		syncActivityInfo(playerId);
		
		long now = HawkTime.getMillisecond();
		refreshInfo.setNearRefreshTime(now);
		long serverScoreVal = serverScore.get();
		refreshInfo.setLastScore(serverScoreVal);
		int refreshCount = (int) (serverScoreVal / cfg.getRefreshPoint());
		int refreshCountNew = refreshCount - refreshInfo.getRefreshCount();
		if (refreshCountNew <= 0) {
			String key = String.format(ActivityRedisKey.PLANET_EXPLORE_TREAREFRESH_INFO, getActivityTermId());
			String value = refreshInfo.toString();
			ActivityLocalRedis.getInstance().getRedisSession().setString(key, value, expireTime);
			HawkLog.logPrintln("PlanetExploreActivity onGMScoreAdd, refreshPoint break");
			return;
		}
		
		refreshInfo.setRefreshCount(refreshCount);
		//刷新出矿点
		List<Integer> pointList = getDataGeter().planetExploreRefreshResPoint(refreshCountNew);
		refreshInfo.addPoint(now, pointList);
		
		String key = String.format(ActivityRedisKey.PLANET_EXPLORE_TREAREFRESH_INFO, getActivityTermId());
		String value = refreshInfo.toString();
		ActivityLocalRedis.getInstance().getRedisSession().setString(key, value, expireTime);
		HawkLog.logPrintln("PlanetExploreActivity onGMScoreAdd, refreshPoint count: {}, pointList: {}", refreshCountNew, pointList);
	}
	
	/**
	 * 判断是否可以采集
	 * @param playerId
	 * @return
	 */
	public int collectCheck(String playerId) {
		if(!isOpening(playerId)){
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		Optional<PlanetExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		PlanetExploreEntity entity = opEntity.get();
		PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		if (entity.getCollectCount() >= cfg.getPlayerDailyGetMax()) { 
			return Status.Error.PLANET_EXPLORE_COLLECT_LIMIT_VALUE;
		}
		
		return 0;
	}
	
	/***
	 * 初始化成就
	 * @param playerId
	 * @return
	 */
	public void initAchieveItems(String playerId) {
		Optional<PlanetExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		PlanetExploreEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			entity.setDayTime(HawkTime.getMillisecond());
			ConfigIterator<PlanetExploreAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PlanetExploreAchieveCfg.class);
			List<AchieveItem> itemList = new ArrayList<>();
			while (configIterator.hasNext()) {
				PlanetExploreAchieveCfg cfg = configIterator.next();				
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());				
				itemList.add(item);
			}
			entity.setItemList(itemList);
			ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
			ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, 1, this.providerActivityId()));
		}
	}
	
	/**
	 * 重置成就任务数据
	 * @param playerId
	 * @param entity
	 */
	private void resetAchieveItems(String playerId, PlanetExploreEntity entity) {
		List<AchieveItem> oldItems = entity.getItemList(); 
		// 积分成就/不重置任务的数据保留
		List<AchieveItem> newAchieveList = new ArrayList<>();
		List<AchieveItem> addList = new ArrayList<>();
		for(AchieveItem achieveItem : oldItems){
			PlanetExploreAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlanetExploreAchieveCfg.class, achieveItem.getAchieveId());
			if (cfg.getDailyReset() > 0) {
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				addList.add(item);
				newAchieveList.add(item);
			} else {
				newAchieveList.add(achieveItem);
			}
		}

		if (!addList.isEmpty()) {
			entity.resetItemList(newAchieveList);
			// 初始化成就数据
			ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, addList), true);
			ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, 1, this.providerActivityId()));
		}
	}
	
	/**
	 * 购买探索道具
	 * @param playerId
	 * @param count
	 */
	protected int onExploreItemBuy(String playerId, int count) {
		if (count <= 0) {
			HawkLog.errPrintln("PlanetExploreActivity buyExploreItem param error, playerId: {}, count: {}", playerId, count);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		Optional<PlanetExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("PlanetExploreActivity buyExploreItem data entity error, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		PlanetExploreEntity entity = opEntity.get();
		PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		//购买消耗
        List<RewardItem.Builder> consume = RewardHelper.toRewardItemImmutableList(cfg.getBuyItemPrice());
        boolean cost = this.getDataGeter().cost(playerId, consume, count, Action.PLANET_EXPLORE_BUY_ITEM, true);
        if (!cost) {
        	HawkLog.errPrintln("PlanetExploreActivity buyExploreItem error, consume failed, playerId: {}, count: {}, remain: {}", 
        			playerId, count, getDataGeter().getItemNum(playerId, consume.get(0).getItemId()));
        	return Status.Error.ITEM_NOT_ENOUGH_VALUE;
        }
        
        //购买获得
        List<RewardItem.Builder> rewardItemBuilder = RewardHelper.toRewardItemImmutableList(cfg.getUseItem());
        rewardItemBuilder.forEach(e -> e.setItemCount(count));
        this.getDataGeter().takeReward(playerId, rewardItemBuilder, 1, Action.PLANET_EXPLORE_BUY_ITEM, true, RewardOrginType.ACTIVITY_REWARD);
		
		HawkLog.logPrintln("PlanetExploreActivity buyExploreItem, playerId: {}, count: {}, personScore: {}, serverScore: {}", playerId, count, entity.getScore(), serverScore.get());
		syncActivityInfo(playerId);
		return 0;
	}
	
	/**
	 * 探索抽奖
	 * @param playerId
	 * @param timesNum
	 */
	protected int onPlanetExplore(String playerId, int timesNum){
		PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		if (timesNum <= 0 || timesNum > cfg.getExploreBatchLimit()) {
			HawkLog.errPrintln("PlanetExploreActivity planetExplore param error, playerId: {}, timesNum: {}, cfg limit: {}", playerId, timesNum, cfg.getExploreBatchLimit());
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		Optional<PlanetExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("PlanetExploreActivity planetExplore data entity error, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		long endTime = this.getTimeControl().getEndTimeByTermId(getActivityTermId());
		if (endTime - HawkTime.getMillisecond() < cfg.getNoExplorationTime() * 1000L) {
			HawkLog.errPrintln("PlanetExploreActivity planetExplore break, near endTime, playerId: {}", playerId);
        	return Status.Error.PLANET_EXPLORE_FINAL_LIMIT_VALUE;
		}
		
		PlanetExploreEntity entity = opEntity.get();
		if (entity.getExploreTimes() + timesNum > cfg.getDrawMax()) {
			HawkLog.errPrintln("PlanetExploreActivity planetExplore break, draw limit, playerId: {}, exploreTimes: {}, timesNum: {}", playerId, entity.getExploreTimes(), timesNum);
        	return Status.Error.PLANET_EXPLORE_DRAW_LIMIT_VALUE;
		}
		
		//抽奖消耗
        List<RewardItem.Builder> consume = RewardHelper.toRewardItemImmutableList(cfg.getUseItem());
        boolean cost = this.getDataGeter().cost(playerId, consume, timesNum, Action.PLANET_EXPLORE, true);
        if (!cost) {
        	HawkLog.errPrintln("PlanetExploreActivity planetExplore error, consume failed, playerId: {}, timesNum: {}, item remain: {}", 
        			playerId, timesNum, getDataGeter().getItemNum(playerId, consume.get(0).getItemId()));
        	return Status.Error.ITEM_NOT_ENOUGH_VALUE;
        }
        
        //抽奖奖励发放
        List<RewardItem.Builder> rewardItemBuilder = PlanetExploreRewardCfg.rewardRandom(timesNum);
        this.getDataGeter().takeReward(playerId, rewardItemBuilder, 1, Action.PLANET_EXPLORE, true, RewardOrginType.FULLY_ARMED_EXPLORE);
		
        int count = timesNum, scoreTotal = 0;
        while (count > 0) {
        	count--;
        	scoreTotal += cfg.randomPoint();
        }
        
        entity.setExploreTimes(entity.getExploreTimes() + timesNum);
		entity.scoreAdd(scoreTotal);
		//这里加的实际上是全量，而不是增量
		rankObj.addScore(entity.getScore(), playerId); // 合服、拆服这块怎么处理: 全服积分清零，排行榜也清零

		//个人累计积分, 成就那边要加上
		int score = (int)Math.min(Integer.MAX_VALUE, entity.getScore());
		ActivityManager.getInstance().postEvent(new PlanetExploreScoreEvent(playerId, score)); //这里就是要传全量，不传增量，增量有风险
		
		//全服积分要加上
		if (serverScore.get() < cfg.getServerMaxPoint()) {
			int add = (int) Math.min(scoreTotal, cfg.getServerMaxPoint() - serverScore.get());
			if (add > 0) {
				serverScore.addAndGet(add);
			}
		}
		//考虑到并发
		if (serverScore.get() > cfg.getServerMaxPoint()) {
			serverScore.set(cfg.getServerMaxPoint());
		}
		
		HawkLog.logPrintln("PlanetExploreActivity planetExplore, playerId: {}, timesNum: {}, score: {}, new personScore: {}, new serverScore: {}", 
				playerId, timesNum, scoreTotal, entity.getScore(), serverScore.get());
		syncActivityInfo(playerId);
		
		Map<String, Object> param = new HashMap<>();
		param.put("termId", getActivityTermId());
        param.put("timesNum", timesNum);
        param.put("scoreAdd", scoreTotal);
        param.put("playerScore", entity.getScore());
        param.put("serverScore", serverScore.get());
        getDataGeter().logActivityCommon(playerId, LogInfoType.planet_explore_draw, param);
		return 0;
	}
	
	/**
	 * 同步挖掘信息
	 * @param playerId
	 */
	protected void syncCollectInfo(String playerId) {
		Optional<PlanetExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("PlanetExploreActivity syncCollectInfo data entity error, playerId: {}", playerId);
			return;
		}
		
		PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		PlanetExploreEntity entity = opEntity.get();
		PlanetCollectInfoSync.Builder builder = PlanetCollectInfoSync.newBuilder();
		int count = 0;
		//星能锅刷新信息，按时间倒序排
		List<Long> sortedTimeList = refreshInfo.getSortedHisTime();
		for (int i = sortedTimeList.size() - 1; i >= 0; i--) {
			long time = sortedTimeList.get(i);
			List<Integer> pointList = refreshInfo.getHistoryPointMap().get(time);
			
			PlanetPointRefreshInfo.Builder pointRefreshInfo = PlanetPointRefreshInfo.newBuilder();
			pointRefreshInfo.setRefreshTime(time);
			for (Integer pointXY : pointList){
				int posX = pointXY / 10000;
				int posY = pointXY % 10000;
				PlanetPointInfo.Builder pointBuilder = PlanetPointInfo.newBuilder();
				pointBuilder.setPosX(posX);
				pointBuilder.setPosY(posY);
				pointRefreshInfo.addPlanetPoint(pointBuilder);
				count++;
			}
			builder.addRefreshInfo(pointRefreshInfo);
			if (count > cfg.getServerLogMax()) { //条目数有上限控制
				break;
			}
		}
		
		count = 0;
		//个人采集记录, 按时间倒序排
		for (int i = entity.getCollectInfoList().size() - 1; i >= 0; i--) {
			PlanetCollectInfo info = entity.getCollectInfoList().get(i);
			int posX = info.getPosX();
			int posY = info.getPosY();
			List<Long> sortedList = info.getSortedTimeList();
			for (int j = sortedList.size() - 1; j >= 0; j--) {
				long time = sortedList.get(j);
				PersonalCollectInfo.Builder collectInfo = PersonalCollectInfo.newBuilder();
				collectInfo.setPosX(posX);
				collectInfo.setPosY(posY);
				collectInfo.setCollectTime(time);
				collectInfo.setCollectCount(info.getCollectTimeCountMap().get(time));
				builder.addCollectInfo(collectInfo); 
				count++;
			}
			if (count > cfg.getPlayerLogMax()) { //条目数有上限控制
				break;
			}
		}
		
		pushToPlayer(playerId, HP.code2.ACTIVITY_PLANET_EXPLORE_COLLECT_INFO_RESP_VALUE, builder);
	}
	
	/****
	 * 同步排行榜信息
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId){
		Optional<PlanetExploreEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("PlanetExploreActivity syncActivityInfo data entity error, playerId: {}", playerId);
			return;
		}
		PlanetExploreInfo.Builder info = PlanetExploreInfo.newBuilder();
		buildPersonTotalRank(info);
		buildMyRank(playerId, info);
		
		PlanetExploreEntity entity = opEntity.get();
		long serverScoreVal = serverScore.get();
		serverScoreVal = Math.max(serverScoreVal, 0);
		int scoreAddNew = (int) (serverScoreVal - refreshInfo.getLastScore());
		
		PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		int itemCount = getDataGeter().getItemNum(playerId, cfg.getItemId());
		info.setItemCount(itemCount);  // 3 我的探索币个数
		info.setMyScore(entity.getScore()); //4 我的探索积分数
		info.setServerScore(serverScoreVal); //5 全服探索积分数
		info.setServerScoreRemainder((int)(serverScoreVal % cfg.getRefreshPoint())); //6 积分进度（余数） 向策划确认了，这里不管就用serverScoreVal就可以
		info.setServerScoreAdd(scoreAddNew);  //7 当前新增积分
		info.setPointCount(scoreAddNew / cfg.getRefreshPoint());  //8 星能锅数量
		info.setExploreProgress((int) (serverScoreVal * 100 / cfg.getServerMaxPoint())); //9 探索进度
		info.setServerScoreLast(refreshInfo.getLastScore()); //10 上期积分
		
		long now = HawkTime.getMillisecond();
		long timeLong = getDataGeter().getPlanetPointLifeTime(cfg.getRefreshTargetId());
		if (refreshInfo.getNearRefreshTime() > 0 && refreshInfo.getNearRefreshTime() + timeLong > now) {
			info.setPointDispearTime(refreshInfo.getNearRefreshTime() + timeLong); //14 锅点消失时间
			int count = 0;
			Iterator<Entry<Integer, Long>> pointItrator = refreshInfo.getPointMap().entrySet().iterator();
			while (pointItrator.hasNext()) {
				Entry<Integer, Long> entry = pointItrator.next();
				if (now - entry.getValue() > timeLong) {
					//过期的点，要移除
					pointItrator.remove();
					continue;
				} 
				count++;
				Integer pointXY = entry.getKey();
				int posX = pointXY / 10000;
				int posY = pointXY % 10000;
				PlanetPointInfo.Builder builder = PlanetPointInfo.newBuilder();
				builder.setPosX(posX);
				builder.setPosY(posY);
				
				int total = 0;
				Optional<PlanetCollectInfo> optional = entity.getCollectInfoList().stream().filter(e -> e.getPosX() == posX && e.getPosY() == posY).findAny();
				if (optional.isPresent()) {
					for (int val : optional.get().getCollectTimeCountMap().values()) {
						total += val;
					}
				}
				
				int remain = (int)getDataGeter().getPlanetPointResRemain(posX, posY);
				builder.setRemainCount(remain);
				builder.setCollectCount(total);
				info.addPlanetPoint(builder); //12 锅点信息
			}
			info.setCollectablePointCount(count); //11 可采星能锅数量
		}
		
		long[] timeArr = cfg.getRefreshTimeArr();
		boolean today = false;
		for (long time : timeArr) {
			if (time > now) {
				today = true;
				info.setNextRefreshTime(time); //13 下一次刷出星能矿点的时间
				break;
			}
		}
		
		if (!today) {
			long tomorrow = timeArr[0] + HawkTime.DAY_MILLI_SECONDS;
			long endTime = this.getTimeControl().getEndTimeByTermId(getActivityTermId());
			if (tomorrow < endTime) {
				info.setNextRefreshTime(tomorrow); //13 下一次刷出星能矿点的时间
			}
		}
		
		//每日采集数量是否展示（客户端不展示）
		info.setExploreTimes(entity.getExploreTimes()); 
		pushToPlayer(playerId, HP.code2.ACTIVITY_PLANET_EXPLORE_INFO_SYNC_VALUE, info);
	}
	
	/***
	 * 构建个人榜
	 * @param rankBuilder
	 */
	@SuppressWarnings("unchecked")
	private void buildPersonTotalRank(PlanetExploreInfo.Builder builder){
		List<PersonScore> personList = (List<PersonScore>) rankObj.getRankList();
		if(personList == null){
			return;
		}
		int rank = 0;
		for(PersonScore data : personList){
			try {
				rank ++;
				PlanetExploreScoreRank.Builder personBuilder = PlanetExploreScoreRank.newBuilder();
				String playerName = getDataGeter().getPlayerName(data.getElement());
				String guildName = getDataGeter().getGuildNameByByPlayerId(data.getElement());
				String guildTag = getDataGeter().getGuildTagByPlayerId(data.getElement());
				if(!HawkOSOperator.isEmptyString(playerName)){
					personBuilder.setPlayerName(playerName);
					personBuilder.setPlayerId(data.getElement());
				}
				if(!HawkOSOperator.isEmptyString(guildName)){
					personBuilder.setGuildName(guildName);
				}
				if(!HawkOSOperator.isEmptyString(guildTag)){
					personBuilder.setGuildTag(guildTag);
				}
				personBuilder.setScore((long)data.getScore());
				personBuilder.setRank(rank);
				builder.addPersonRank(personBuilder);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/***
	 * 构建我的个人排名
	 * @param playerId
	 */
	private void buildMyRank(String playerId, PlanetExploreInfo.Builder builder){
		String key = String.format(ActivityRedisKey.PLANET_EXPLORE_SCORE_RANK, getActivityTermId());
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(key, playerId);
		int rank = 0;
		long score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = RankScoreHelper.getRealScore(index.getScore().longValue());
		}
		PlanetExploreScoreRank.Builder myBuilder = PlanetExploreScoreRank.newBuilder();
		myBuilder.setRank(rank);
		myBuilder.setScore(score);
		builder.setMyRank(myBuilder);
	}
	
	@Override
	public void onEnd() {
		int termId = getActivityTermId(); //这个时候获取termid，还是能获取到正确的值
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				sendReward(termId);
				return null;
			}
		});
	}
	
	/**
	 * 活动结束后发奖.
	 * @param termId
	 */
	private void sendReward(int termId) {
		try {
			sendRewardMail(MailId.PLANET_EXPLORE_SCORE_RANK, new Object[0], new Object[0], termId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/***
	 * 发送个人排名奖励
	 */
	@SuppressWarnings("unchecked")
	private void sendRewardMail(MailId mailId, Object[] title, Object[] subTitle, int termId){
		List<PlanetExploreRankCfg> rankListReward = getPeronRankRewardConfig();
		int high = getPersonRankHasRewardMaxRank();
		if(high == 0){
			HawkLog.errPrintln("PlanetExploreActivity sendRewardMail rankReward error, config error");
			return;
		}

		List<PersonScore> rankPlayers = (List<PersonScore>) rankObj.getHasRewardRankList(termId, high);
		int maxRank = 0;
		for (PersonScore s : rankPlayers) {
			maxRank++;
			long score = (long)s.getScore();
			for (PlanetExploreRankCfg rewardCfg : rankListReward) {
				if (rewardCfg.getRankLow() < maxRank || rewardCfg.getRankHigh() > maxRank) {
					continue;
				}
				try {
					Object[] content;
					content = new Object[2];
					content[0] = score;
					content[1] = maxRank;
					sendMailToPlayer(s.getElement(), mailId, title, subTitle, content, rewardCfg.getRewardList());
					HawkLog.logPrintln("PlanetExploreActivity send rankReward, playerId:{}, score: {}, rank:{}, cfgId:{}", s.getElement(), score, maxRank, rewardCfg.getId());
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}
	
	/**
	 * 获取排名奖励配置
	 * @return
	 */
	private List<PlanetExploreRankCfg> getPeronRankRewardConfig(){
		List<PlanetExploreRankCfg> configList = new ArrayList<>();
		ConfigIterator<PlanetExploreRankCfg> ite = HawkConfigManager.getInstance().getConfigIterator(PlanetExploreRankCfg.class);
		while(ite.hasNext()){
			PlanetExploreRankCfg config = ite.next();
			configList.add(config);
		}
		return configList;
	}

	/***
	 * 获取个人榜最次的上榜排名
	 * @return
	 */
	private int getPersonRankHasRewardMaxRank(){
		int max = 0;
		ConfigIterator<PlanetExploreRankCfg> ite = HawkConfigManager.getInstance().getConfigIterator(PlanetExploreRankCfg.class);
		while(ite.hasNext()){
			PlanetExploreRankCfg config = ite.next();
			if(config.getRankLow() > max){
				max = config.getRankLow();
			}
		}
		return max;
	}
	
	/**
	 * 合服处理
	 */
	@Override
	public boolean handleForMergeServer() {
		if (this.getActivityEntity().getActivityState() == ActivityState.HIDDEN) {
			return true;
		}

		//通过锁来防止这里执行修改了数据后，ontick处又修改了数据
		lock.compareAndSet(NORMAL, MERGE_PROCESSED); //如果是正常值（表示此时ontick不在执行中），则设置成1表示已执行合服处理
		while (lock.get() == TICK_RUNNING) { //表示ontick正在执行，先等待
			HawkOSOperator.osSleep(100);
			lock.compareAndSet(NORMAL, MERGE_PROCESSED); //回归到正常值了，则设置成1表示已执行合服处理
		}
		
		int termId = this.getActivityTermId();
		sendReward(termId);

		clear();
		//清除全服积分数据
		String key1 = String.format(ActivityRedisKey.PLANET_EXPLORE_SERVER_SCORE, termId);
		ActivityLocalRedis.getInstance().getRedisSession().del(key1);
		//清除全服刷星能矿相关数据
		String key2 = String.format(ActivityRedisKey.PLANET_EXPLORE_TREAREFRESH_INFO, termId);
		ActivityLocalRedis.getInstance().getRedisSession().del(key2);
		//清除全服排行榜数据
		String key3 = String.format(ActivityRedisKey.PLANET_EXPLORE_SCORE_RANK, termId);
		ActivityLocalRedis.getInstance().getRedisSession().del(key3);
		return true;
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}
	
	/**
	 * 删除排行榜数据
	 */
	public void removePlayerRank(String playerId) {
		if (this.getActivityEntity().getActivityState() == com.hawk.activity.type.ActivityState.HIDDEN) {
			return;
		}
		rankObj.remove(playerId);
		rankObj.doRankSort();
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}

}
