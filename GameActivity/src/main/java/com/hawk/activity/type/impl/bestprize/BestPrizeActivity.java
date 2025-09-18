package com.hawk.activity.type.impl.bestprize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.timer.HawkTimerEntry;
import org.hawk.timer.HawkTimerListener;
import org.hawk.timer.HawkTimerManager;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BestPrizeDrawEvent;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDaysActivityEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.impl.VipLevelupEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.tools.ListSplitter;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.bestprize.cfg.BestPrizeAchieveCfg;
import com.hawk.activity.type.impl.bestprize.cfg.BestPrizeExchangeCfg;
import com.hawk.activity.type.impl.bestprize.cfg.BestPrizeKVCfg;
import com.hawk.activity.type.impl.bestprize.cfg.BestPrizePoolAwardCfg;
import com.hawk.activity.type.impl.bestprize.cfg.BestPrizePoolCfg;
import com.hawk.activity.type.impl.bestprize.cfg.BestPrizeShopCfg;
import com.hawk.activity.type.impl.bestprize.cfg.BestPrizeTimeCfg;
import com.hawk.activity.type.impl.bestprize.entity.BestPrizeEntity;
import com.hawk.activity.type.impl.bestprize.entity.DrawRecord;
import com.hawk.activity.type.impl.bestprize.entity.SmallPoolInfo;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.common.ServerInfo;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.BestPrizeBigPoolResp;
import com.hawk.game.protocol.Activity.BestPrizeDrawReq;
import com.hawk.game.protocol.Activity.BestPrizeDrawResp;
import com.hawk.game.protocol.Activity.BestPrizeExchangeInfo;
import com.hawk.game.protocol.Activity.BestPrizeShopInfo;
import com.hawk.game.protocol.Activity.BigPoolBigAwardPB;
import com.hawk.game.protocol.Activity.BigPoolInfoPB;
import com.hawk.game.protocol.Activity.DrawRewardPB;
import com.hawk.game.protocol.Activity.PBBestPrizeInfo;
import com.hawk.game.protocol.Activity.PoolAwardInfoPB;
import com.hawk.game.protocol.Activity.SmallPoolInfoPB;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst;

/**
 * 新春头奖专柜活动
 * 
 * @author lating
 *
 */
public class BestPrizeActivity extends ActivityBase implements AchieveProvider, IExchangeTip<BestPrizeExchangeCfg> {
	/**
	 * tick时间
	 */
	private long lastTickTime;
	/**
	 * 本服所属的服务器分组id
	 */
	private int groupId = -1;
	/**
	 * 本服所属服务器分组包含哪些区服
	 */
	private List<String> serverList = new ArrayList<>();
	/**
	 * 各个大奖池下的小奖池数量
	 */
	private Map<Integer, Integer> poolCountMap = new ConcurrentHashMap<>();
	/**
	 * 是否已添加闹钟
	 */
	private boolean addAlarm = false;
	

	public BestPrizeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.BEST_PRIZE_361;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		BestPrizeActivity activity = new BestPrizeActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<BestPrizeEntity> queryList = HawkDBManager.getInstance().query("from BestPrizeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			BestPrizeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		BestPrizeEntity entity = new BestPrizeEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onEnd() {
		addAlarm = false;
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				Set<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
				for (String playerId : onlinePlayerIds) {
					clearDrawConsumeItems(playerId);
				} 
				return null;
			}
		});
	}
	
	@Override
	public boolean isHidden(String playerId) {
		int groupId = getGroupId();
		if (groupId == 0) {
			return true;
		}
		return super.isHidden(playerId);
	}
	
	@Override
	public boolean isActivityClose(String playerId) {
		BestPrizeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BestPrizeKVCfg.class);
		int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
		int vipLevel = this.getDataGeter().getVipLevel(playerId);
		if (cfg.getBaseLevelLimit() > cityLevel || cfg.getVipLevelLimit() > vipLevel) {
			return true;
		}
		
		// 不在分组内,则关闭此活动
		int groupId = getGroupId();
		return groupId == 0;
	}

	@Override
	public void onOpen() {
		long timeNow = HawkTime.getMillisecond();
		lastTickTime = timeNow;
		groupId = -1;
		serverList.clear();
		getGroupId();
		getServerList();
		if (groupId < 0) {
			return;
		} 
		
		addAlarm = true;
		addAlarm();
		//初始化奖池
		initPoolInfo();
		
		Set<String> onlinePlayers = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayers){
			if (!isOpening(playerId)) {
				continue;
			}
			callBack(playerId, GameConst.MsgId.BEST_PRIZE_INIT, ()->{
				initAchieveItems(playerId);
				syncActivityInfo(playerId);
			});
		}
	}
	
	/**
	 * 初始化各个大奖池下的小奖池信息
	 */
	private void initPoolInfo() {
		String serverId = getDataGeter().getServerId();
		String lockKey = addPoolLockKey();
		boolean setNxSucc = getRedis().setNx(lockKey, serverId);
		if (!setNxSucc) {
			return;
		}
		
		boolean empty = false;
		try {
			Map<String, String> poolSizeMap = getPoolSizeMap();
			if (!poolSizeMap.isEmpty()) {
				refreshPoolCountLocal(poolSizeMap);
				return;
			}
			empty = true;
			String poolSizeKey = smallPoolSizeKey();
			ConfigIterator<BestPrizePoolCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BestPrizePoolCfg.class);
			while(iterator.hasNext()) {
				try {
					BestPrizePoolCfg cfg = iterator.next();
					int bigPool = cfg.getId();
					int smallPoolCount = cfg.getStartPoolValue();
					poolCountMap.put(bigPool, smallPoolCount);
					getRedis().hSet(poolSizeKey, String.valueOf(bigPool), String.valueOf(smallPoolCount), getExpireSeconds());
					String poolInfoKey = smallPoolInfoKey(bigPool);
					int rewardACount = BestPrizePoolAwardCfg.getPoolRewardACount(bigPool);
					for(int poolId = 1; poolId <= smallPoolCount; poolId++) {
						SmallPoolInfo poolInfo = new SmallPoolInfo(poolId, rewardACount);
						getRedis().hSet(poolInfoKey, String.valueOf(poolId), JSONObject.toJSONString(poolInfo), getExpireSeconds());
					}
					HawkLog.logPrintln("BestPrizeActivity init pool, bigPool: {}, smallPoolCount: {}, rewardACount: {}", bigPool, smallPoolCount, rewardACount);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			getRedis().del(lockKey);
			if (empty) {
				getRedis().setString(getAddPoolTimeKey(), String.valueOf(HawkTime.getMillisecond()), getExpireSeconds());
			}
		}
	}

	/**
	 * 当前服务器所属分组id
	 * @return
	 */
	private int getGroupId() {
		if (groupId > -1) {
			return groupId;
		}

		BestPrizeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BestPrizeKVCfg.class);
		String serverId = getDataGeter().getServerId();
		String lockKey = groupKey();
		String result = getRedis().hGet(lockKey, serverId);
		if (StringUtils.isEmpty(result)) {
			List<String> serverList = getOpenServerList();
			List<List<String>> list = ListSplitter.splitList(serverList, cfg.getGroupAmount());
			for (int i = 0; i < list.size(); i++) {
				HawkLog.logPrintln("BestPrizeActivity server group init, totalCount: {}, groupId: {}, serverList: {}", serverList.size(), (i + 1), list.get(i));
				for (String server : list.get(i)) {
					getRedis().hSetNx(lockKey, server, Integer.valueOf(i + 1).toString());
				}
			}
			getRedis().expire(lockKey, getExpireSeconds());
		}
		groupId = NumberUtils.toInt(getRedis().hGet(lockKey, serverId));
		return groupId;
	}

	/**
	 * 获取当前服务器所属分组下的区服列表
	 * @return
	 */
	private List<String> getServerList() {
		if (!serverList.isEmpty()) {
			return serverList;
		}

		String groupId = String.valueOf(getGroupId());
		Map<String, String> result = getRedis().hGetAll(groupKey());
		for (Entry<String, String> ent : result.entrySet()) {
			if (ent.getValue().equals(groupId)) {
				serverList.add(ent.getKey());
			}
		}
		return serverList;
	}

	/**
	 * 获取符合开启条件的区服
	 */
	private List<String> getOpenServerList() {
		List<ServerInfo> serverInfoList = getDataGeter().getServerList();
		Collections.sort(serverInfoList, Comparator.comparing(ServerInfo::getOpenTime));
		int termId = getActivityTermId();
		Collections.shuffle(serverInfoList, new Random(termId)); //乱序

		List<String> result = new ArrayList<>();
		long now = HawkTime.getMillisecond();
		long serverDelay = getTimeControl().getServerDelay();
		for (ServerInfo sinfo : serverInfoList) {
			long timeLimit = HawkTime.parseTime(sinfo.getOpenTime()) + serverDelay;
			if (timeLimit < now) {
				result.add(sinfo.getId());
			}
		}
		
		return result;
	}
	
	@Override
	public void onTick() {
		long currTime = HawkApp.getInstance().getCurrentTime();
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		if (currTime >= endTime) {
			lastTickTime = 0;
			return;
		}
		
		//起服后第一次执行
		if (lastTickTime == 0) {
			lastTickTime = currTime;
			getGroupId();
			getServerList();
			serverStartInit();
		}
		if (groupId < 0) {
			return;
		}
		
		if (!addAlarm) {
			addAlarm = true;
			addAlarm();
		}
		
		if (currTime - lastTickTime >= 5000L) {
			lastTickTime = currTime;
			refreshPoolCountLocal(null);
		}
	}
	
	/**
	 * 定时增加奖池
	 */
	private void addAlarm() {
		try {
			for (int hour : BestPrizeKVCfg.getInstance().getAddPoolTimeHourList()) {
				HawkTimerManager.getInstance().addAlarm("BestPrizeAddPool-" + hour, 0, 0, hour, -1, -1, new HawkTimerListener() {
					protected void handleAlarm(HawkTimerEntry entry) {
						HawkLog.logPrintln("BestPrizeActivity alarm ring");
						addPoolCheck();
					}
				});
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 起服初始化
	 */
	private void serverStartInit() {
		if (groupId < 0) {
			return;
		}
		
		Map<String, String> poolSizeMap = getPoolSizeMap();
		if (poolSizeMap.isEmpty()) {
			initPoolInfo();
			return;
		}
		
		refreshPoolCountLocal(poolSizeMap);
		long now = HawkTime.getMillisecond();
		long am0Time = HawkTime.getAM0Date().getTime();
		String timeStr = this.getString(getAddPoolTimeKey(), 0);
		long lastAddPoolTime = HawkOSOperator.isEmptyString(timeStr) ? now : Long.parseLong(timeStr);
		
		List<Integer> hourList = BestPrizeKVCfg.getInstance().getAddPoolTimeHourList();
		for (int i = 0; i < hourList.size(); i++) {
			long hourTime = am0Time + hourList.get(i) * 3600000L;
			if (lastAddPoolTime < hourTime && hourTime <= now) {
				addPoolCheck();
				break;
			}
		}
	}
	
	/**
	 * 刷新奖池数量
	 */
	private void refreshPoolCountLocal(Map<String, String> poolSizeMap) {
		if (poolSizeMap == null) {
			poolSizeMap = getPoolSizeMap();
		}
		poolSizeMap.entrySet().forEach(e -> poolCountMap.put(Integer.parseInt(e.getKey()), Integer.parseInt(e.getValue())));
	}
	
	/**
	 * 动态增加奖池： 添加小奖池数量 = 所属大奖奖池中初始开放小奖池数量  -（剩余A奖+最终奖）
	 */
	private void addPoolCheck() {
		String serverId = getDataGeter().getServerId();
		String lockKey = addPoolLockKey();
		boolean setNxSucc = getRedis().setNx(lockKey, serverId);
		if (!setNxSucc) {
			return;
		}
		
		try {
			refreshPoolCountLocal(null);
			ConfigIterator<BestPrizePoolCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BestPrizePoolCfg.class);
			while(iterator.hasNext()) {
				try {
					bigPoolCheck(iterator.next());
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			getRedis().del(lockKey);
			getRedis().setString(getAddPoolTimeKey(), String.valueOf(HawkTime.getMillisecond()), getExpireSeconds());
		}
	}
	
	/**
	 * 动态增加奖池
	 * @param cfg
	 */
	private void bigPoolCheck(BestPrizePoolCfg cfg) {
		int bigPool = cfg.getId(), remainCount = 0;
		String poolInfoKey = smallPoolInfoKey(bigPool);
		Map<String, String> smallPoolMap = getRedis().hGetAll(poolInfoKey);
		for (String poolInfoStr : smallPoolMap.values()) {
			SmallPoolInfo poolObj = JSONObject.parseObject(poolInfoStr, SmallPoolInfo.class);
			remainCount += poolObj.getRemainRewardA(); //剩余A奖
		}
		int addPoolCount = cfg.getStartPoolValue() - remainCount;
		if (addPoolCount <= 0) {
			HawkLog.logPrintln("BestPrizeActivity add pool break, bigPool: {}, remainCount: {}, addCount: {}", bigPool, remainCount, addPoolCount);
			return;
		}
		
		String dailyAddKey = smallPoolDailyAddKey(bigPool);
		String ymdStr = String.valueOf(HawkTime.getYyyyMMddIntVal());
		String addTodayStr = getRedis().hGet(dailyAddKey, ymdStr);
		int addToday = HawkOSOperator.isEmptyString(addTodayStr) ? 0 : Integer.parseInt(addTodayStr);
		if (addToday >= cfg.getPoolLimit()) {
			HawkLog.logPrintln("BestPrizeActivity add pool break, bigPool: {}, addToday: {}, config limit: {}", bigPool, addToday, cfg.getPoolLimit());
			return;
		}
		
		addPoolCount = Math.min(addPoolCount, cfg.getPoolLimit() - addToday);
		getRedis().hIncrBy(dailyAddKey, ymdStr, addPoolCount, 24 * 3600);
		int oldCount = poolCountMap.getOrDefault(bigPool, 0);
		int newCount = oldCount + addPoolCount;
		poolCountMap.put(bigPool, newCount);
		String poolSizeKey = smallPoolSizeKey();
		getRedis().hSet(poolSizeKey, String.valueOf(bigPool), String.valueOf(newCount), getExpireSeconds());
		int poolRewardACount = BestPrizePoolAwardCfg.getPoolRewardACount(bigPool);
		for (int id = oldCount + 1; id <= newCount; id++) {
			SmallPoolInfo poolInfo = new SmallPoolInfo(id, poolRewardACount);
			getRedis().hSet(poolInfoKey, String.valueOf(id), JSONObject.toJSONString(poolInfo), getExpireSeconds());
		}
		
		int addCountToday = addToday + addPoolCount;
		HawkLog.logPrintln("BestPrizeActivity add pool, bigPool: {}, oldCount: {}, remainCount: {}, addCount: {}, add today: {}, total count: {}", bigPool, oldCount, remainCount, addPoolCount, addCountToday, newCount);
		
		Map<String, Object> param = new HashMap<>();
		param.put("termId", getActivityTermId());
		param.put("groupId", groupId);
		param.put("bigPool", bigPool);             //大奖池ID
		param.put("remainCount", remainCount);     //剩余A奖+最终奖数量
	    param.put("addCount", addPoolCount);       //本次添加小奖池的数量
	    param.put("addCountToday", addCountToday); //当天添加小奖池数量总数
	    param.put("oldPoolNumMax", oldCount);      //添加前小奖池最大编号
	    param.put("newPoolNumMax", newCount);      //添加后小奖池最大编号
        getDataGeter().logActivityCommon(LogInfoType.best_prize_pool_add, param);
	}
	
	@Subscribe
	public void onEvent(BuildingLevelUpEvent event) {
		if (event.isLogin() || event.getBuildType() != Const.BuildingType.CONSTRUCTION_FACTORY_VALUE) {
			return;
		}
		BestPrizeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BestPrizeKVCfg.class);
		if(event.getLevel() != cfg.getBaseLevelLimit() || event.getProgress() != 0){ 
			return;
		}
		long currTime = HawkApp.getInstance().getCurrentTime();
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		if (currTime >= endTime) {
			return;
		}
		String playerId = event.getPlayerId();
		int vipLevel = this.getDataGeter().getVipLevel(playerId);
		//开启活动
		if (vipLevel >= cfg.getVipLevelLimit() && isOpening(playerId)) {
			ActivityManager.getInstance().syncAllActivityInfo(playerId);
			initAchieveItems(playerId);
			syncActivityInfo(playerId);
		}
	}
	
	@Subscribe
    public void onVipLevelUpEvent(VipLevelupEvent event) {
		BestPrizeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BestPrizeKVCfg.class);
		int vipLimit = cfg.getVipLevelLimit();
		boolean matchSuccess = event.getOldLevel() < vipLimit && event.getLevel() >= vipLimit;
		if (!matchSuccess) {
			return;
		}
		long currTime = HawkApp.getInstance().getCurrentTime();
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		if (currTime >= endTime) {
			return;
		}
		
		String playerId = event.getPlayerId();
		int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
		//开启活动
		if (cityLevel >= cfg.getBaseLevelLimit() && isOpening(playerId)) {
			ActivityManager.getInstance().syncAllActivityInfo(playerId);
			initAchieveItems(playerId);
			syncActivityInfo(playerId);
		}
    }
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			clearPoint(playerId);
			clearDrawConsumeItems(playerId);
			return;
		}
		Optional<BestPrizeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		BestPrizeEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(playerId);
		}
		
		//每天重置数据
		if (!HawkTime.isToday(entity.getDayTime())) {
			entity.setDayTime(HawkTime.getMillisecond());
			ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, 1, this.providerActivityId()));
			shopResetDaily(entity);
		}
		syncActivityInfo(playerId);
	}
	
	/**
	 * 商店重置
	 * @param entity
	 */
	private void shopResetDaily(BestPrizeEntity entity) {
		for (int shopId : entity.getShopItemMap().keySet()) {
			BestPrizeShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BestPrizeShopCfg.class, shopId);
			//每日刷新类型
			if (cfg.getRefreshType() == 1) {
				entity.getShopItemMap().put(shopId, 0);
				entity.notifyUpdate();
			}
		}
	}
	
	/**
	 * 活动结束清除积分
	 */
	private void clearPoint(String playerId) {
		//活动结束了，删除积分
		int itemCount = getDataGeter().getItemNum(playerId, BestPrizeExchangeCfg.getConsumeItemId());
		if (itemCount > 0) {
			RewardItem.Builder consumeItems = RewardHelper.toRewardItem(30000, BestPrizeExchangeCfg.getConsumeItemId(), itemCount);
			this.getDataGeter().consumeItems(playerId, Arrays.asList(consumeItems), 0, Action.BEST_PRIZE_END_CONSUME);
		}
	}

	/**
	 * 活动结束清空抽奖券
	 * @param playerId
	 */
	private void clearDrawConsumeItems(String playerId) {
		if(this.getDataGeter().isPlayerCrossIngorePlayerObj(playerId)) {
			return;
		}
		int itemCount = getDataGeter().getItemNum(playerId, BestPrizeKVCfg.getInstance().getDrawItemId());
		if (itemCount <= 0) {
			return;
		}
		
		RewardItem.Builder consumeItems = RewardHelper.toRewardItem(30000, BestPrizeKVCfg.getInstance().getDrawItemId(), itemCount);
		this.getDataGeter().consumeItems(playerId, Arrays.asList(consumeItems), 0, Action.BEST_PRIZE_END_CONSUME);
		RewardItem.Builder changeItem = RewardHelper.toRewardItem(BestPrizeKVCfg.getInstance().getDrawItemChange());
		changeItem.setItemCount(changeItem.getItemCount() * itemCount);
		//发邮件
		Object[] content = new Object[1];
		content[0] = itemCount;
		getDataGeter().sendMail(playerId, MailId.BEST_PRIZE_END_CLEAR, null, null, content, Arrays.asList(changeItem), false);
	}

	/***
	 * 初始化成就  
	 * @param playerId
	 * @return
	 */
	private void initAchieveItems(String playerId) {
		Optional<BestPrizeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		BestPrizeEntity entity = opEntity.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		entity.setDayTime(HawkTime.getMillisecond());
		ConfigIterator<BestPrizeAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(BestPrizeAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while (configIterator.hasNext()) {
			BestPrizeAchieveCfg cfg = configIterator.next();				
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());				
			itemList.add(item);
		}
		entity.setItemList(itemList);
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, 1, this.providerActivityId()));
	}
	
	/**
	 * 奖池抽奖
	 * @param playerId
	 * @param bigPool
	 * @param smallPool
	 * @param times
	 * @return
	 */
	public int onPoolDraw(String playerId, BestPrizeDrawReq req) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		int bigPool = req.getBigPoolId(), smallPool = req.getSmallPoolId(), times = req.getTimes();
		BestPrizePoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(BestPrizePoolCfg.class, bigPool);
		if (poolCfg == null) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		if (times <= 0) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		long now = HawkTime.getMillisecond();
		if (!BestPrizeKVCfg.getInstance().checkDrawTimeRange(now)) {
			return Status.Error.BEST_PRIZE_DRAW_TIME_ERROR_VALUE;
		}
		
		Optional<BestPrizeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		BestPrizeEntity entity = opEntity.get();
		if (poolCfg.getMaxGetLimit() > 0 && entity.getBigPoolDrawCount(bigPool) >= poolCfg.getMaxGetLimit()) {
			return Status.Error.BEST_PRIZE_BIG_POOL_LIMIT_VALUE;
		}
		
		String poolLockKey = poolDrawLockKey(bigPool);
		long hsetResult = getRedis().hSetNx(poolLockKey, String.valueOf(smallPool), playerId + ":" + now);
		//设置失败，有人正在抽奖
		if (hsetResult <= 0) {
			return Status.Error.BEST_PRIZE_POOL_DRAWING_VALUE;
		}
		
		try {
			String poolInfoKey = smallPoolInfoKey(bigPool);
			String poolInfo = getRedis().hGet(poolInfoKey, String.valueOf(smallPool));
			if (HawkOSOperator.isEmptyString(poolInfo)) {
				HawkLog.errPrintln("BestPrizeActivity draw over break, playerId: {}, bigPool: {}, smallPool: {}, poolInfo: {}", playerId, bigPool, smallPool, poolInfo);
				syncBigPoolInfo(playerId, bigPool);
				return Status.Error.BEST_PRIZE_POOL_FINISH_VALUE; //该奖池已抽完
			}
			
			SmallPoolInfo poolObj = JSONObject.parseObject(poolInfo, SmallPoolInfo.class);
			int poolLimitTimes = BestPrizePoolAwardCfg.getlimitTimesByPoolId(bigPool);
			if (poolObj.getDrawTimesTotal() >= poolLimitTimes) {
				HawkLog.errPrintln("BestPrizeActivity draw over break, playerId: {}, bigPool: {}, smallPool: {}, drawTimes: {}", playerId, bigPool, smallPool, poolObj.getDrawTimesTotal());
				syncBigPoolInfo(playerId, bigPool);
				return Status.Error.BEST_PRIZE_POOL_FINISH_VALUE; //该奖池已抽完
			}
			
			//从剩余的奖励中抽取
			int result = drawSingle(poolObj, entity, bigPool, times, req.getChangeRewardIdList());
			if (result != 0) {
				return result;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			getRedis().hDel(poolLockKey, String.valueOf(smallPool));
		}
		
        syncActivityInfo(playerId); 
		return 0;
	}
	
	/**
	 * 获取抽奖消耗
	 * @param playerId
	 * @param poolCfg
	 * @param realTimes
	 * @return
	 */
	private List<RewardItem.Builder> getDrawConsume(String playerId, BestPrizePoolCfg poolCfg, int consumeCount) {
		List<RewardItem.Builder> consumeList = new ArrayList<>();
		//优先消耗活动结束后要兑换回收的那种道具
		RewardItem.Builder consume1 = RewardHelper.toRewardItem(poolCfg.getDrawConsume());
		RewardItem.Builder consume2 = RewardHelper.toRewardItem(poolCfg.getDrawReserveConsume());
		int haveNum = this.getDataGeter().getItemNum(playerId, consume1.getItemId());
		if (haveNum <= 0) { //没有那种道具
			consume2.setItemCount(consumeCount);
			consumeList.add(consume2);
		} else if (haveNum < consumeCount) { //那种道具的数量不够
			consume1.setItemCount(haveNum);
			consumeList.add(consume1);
			consume2.setItemCount(consumeCount - haveNum);
			consumeList.add(consume2);
		} else { //那种道具的数量足够
			consume1.setItemCount(consumeCount);
			consumeList.add(consume1);
		}
		return consumeList;
	}
	
	/**
	 * 互斥抽奖（一个小奖池同时只能被一个玩家抽取）
	 * @param poolInfoStr
	 * @param entity
	 * @param bigPool   所属大奖池id
	 * @param smallPool 所属小奖池id
	 * @param times     抽奖次数
	 * @param changeRewardList 需要转换成积分的奖励列表
	 */
	private int drawSingle(SmallPoolInfo poolObj, BestPrizeEntity entity, int bigPool, int times, List<Integer> changeRewardList) {
		String playerId = entity.getPlayerId();
		int smallPool = poolObj.getPoolId();
		//该奖池总共能抽多少次
		int poolLimitTimes = BestPrizePoolAwardCfg.getlimitTimesByPoolId(bigPool);
		//已经抽过多少次了
		int drawTimes = poolObj.getDrawTimesTotal();
		//剩余可抽奖次数
		int remainTimes = poolLimitTimes - drawTimes;
		//实际抽奖次数
		int realTimes = Math.min(remainTimes, times);
		
		//消耗奖券
		BestPrizePoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(BestPrizePoolCfg.class, bigPool);
		int consumeCount = poolCfg.getItemNeedValue() * realTimes; //消耗总数
		List<RewardItem.Builder> consumeList = getDrawConsume(playerId, poolCfg, consumeCount);
        boolean cost = this.getDataGeter().cost(playerId, consumeList, 1, Action.BEST_PRIZE_POOL_DRAW, true);
        if (!cost) {
        	HawkLog.errPrintln("BestPrizeActivity draw consume failed, playerId: {}, count: {}, bigPool: {}, smallPool: {}", playerId, consumeCount, bigPool, smallPool);
        	return Status.Error.BEST_PRIZE_DRAW_CONSUME_ERROR_VALUE;
        }
		
		entity.setDrawConsume(entity.getDrawConsume() + consumeCount);
		ActivityManager.getInstance().postEvent(new BestPrizeDrawEvent(playerId, entity.getDrawConsume()));
		String poolInfoKey = smallPoolInfoKey(bigPool);
		//将剩余的奖励全部发放下去
		if (realTimes >= remainTimes) {
			drawReward(entity, poolCfg, Collections.emptyList(), poolObj, changeRewardList);
			getRedis().hDel(poolInfoKey, String.valueOf(smallPool));
			refreshPoolCountLocal(null);
			HawkLog.logPrintln("BestPrizeActivity draw over success, playerId: {}, bigPool: {}, smallPool: {}, drawTimes: {}, realTimes: {}, client reqTimes: {}", playerId, bigPool, smallPool, drawTimes, realTimes, times);
			return 0;
		}
		
		Map<Integer, Integer> rewardMap = poolCfg.getRewardWeightMap();
		List<Integer> rewardList = new ArrayList<>();
		List<Integer> weightList = new ArrayList<>();
		for (Entry<Integer, Integer> entry : rewardMap.entrySet()) {
			int cfgId = entry.getKey(), weight = entry.getValue();
			int rewardDrawTimes = poolObj.getAwardDrawMap().getOrDefault(cfgId, 0);
			int rewardRemainTimes = BestPrizePoolAwardCfg.getLimit(cfgId) - rewardDrawTimes;
			while (rewardRemainTimes > 0) {
				rewardRemainTimes--;
				rewardList.add(cfgId);
				weightList.add(weight);
			}
		}

		//发奖
		List<Integer> randomList = HawkRand.randomWeightObject(rewardList, weightList, realTimes);
		if (randomList.isEmpty()) {
			HawkLog.errPrintln("BestPrizeActivity drawSingle error, playerId: {}, bigPool: {}, smallPool: {}, realDrawTimes: {}", playerId, bigPool, smallPool, realTimes);
			throw new RuntimeException("draw reward error");
		}
		
		drawReward(entity, poolCfg, randomList, poolObj, changeRewardList);
		for (int cfgId : randomList) {
			int newTimes = poolObj.getAwardDrawMap().getOrDefault(cfgId, 0) + 1;
			poolObj.getAwardDrawMap().put(cfgId, newTimes);
			BestPrizePoolAwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BestPrizePoolAwardCfg.class, cfgId);
			if (cfg.getBigPrize() > 0 && poolObj.getRemainRewardA() > 0) {
				poolObj.setRemainRewardA(poolObj.getRemainRewardA() - 1);
			}
		}
		poolObj.setDrawTimesTotal(drawTimes + realTimes);
		getRedis().hSet(poolInfoKey, String.valueOf(smallPool), JSONObject.toJSONString(poolObj), getExpireSeconds());
		HawkLog.logPrintln("BestPrizeActivity draw success, playerId: {}, bigPool: {}, smallPool: {}, drawTimes: {}, realTimes: {}, client reqTimes: {}, remainTimes: {}, randomList: {}", 
				playerId, bigPool, smallPool, drawTimes, realTimes, times, remainTimes, randomList);
		return 0;
	}
	
	/**
	 * 奖池抽奖发奖逻辑
	 * @param playerId
	 * @param poolCfg
	 * @param rewardIds
	 * @param poolObj
	 */
	private void drawReward(BestPrizeEntity entity, BestPrizePoolCfg poolCfg, List<Integer> rewardIds, SmallPoolInfo poolObj, List<Integer> changeRewardList) {
		String playerId = entity.getPlayerId();
		List<RewardItem.Builder> initialRewards = new ArrayList<>();
		List<RewardItem.Builder> rewardItems = new ArrayList<>();
		boolean lastReward = false;
		long changedPoints = 0;
		if (rewardIds.isEmpty()) {
			lastReward = true;
			rewardIds = new ArrayList<>();
			for (Entry<Integer, Integer> entry : poolCfg.getRewardWeightMap().entrySet()) {
				int cfgId = entry.getKey();
				int drawTimes = poolObj.getAwardDrawMap().getOrDefault(cfgId, 0);
				int remainTimes = BestPrizePoolAwardCfg.getLimit(cfgId) - drawTimes;
				while (remainTimes > 0) {
					remainTimes--;
					rewardIds.add(cfgId);
				}
			}
			
			ImmutableList<RewardItem.Builder> listRewardsItems = RewardHelper.toRewardItemImmutableList(poolCfg.getLastRewards()); 
			initialRewards.addAll(listRewardsItems);
			rewardItems.addAll(listRewardsItems);
		}
		
		HawkLog.logPrintln("BestPrizeActivity draw reward change list, playerId: {}, bigPool: {}, smallPool: {}, list: {}", playerId, poolCfg.getId(), poolObj.getPoolId(), changeRewardList);
		
		String serverId = this.getDataGeter().getServerId();
		String playerName = this.getDataGeter().getPlayerName(playerId);
		boolean bigPrize = false;
		int bigPrizeItemId = 0, bigPrizeItemCount = 0;
		List<Integer> changeList = new ArrayList<>();
		Map<Integer, Integer> map = new HashMap<>();
		for (int cfgId : rewardIds) {
			int count = map.getOrDefault(cfgId, 0);
			map.put(cfgId, count + 1);
			BestPrizePoolAwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BestPrizePoolAwardCfg.class, cfgId);
			RewardItem.Builder reward = RewardHelper.toRewardItem(cfg.getRewards());
			initialRewards.add(reward);
			if (changeRewardList.contains(cfgId)) {
				changeList.add(cfgId);
				RewardItem.Builder points = RewardHelper.toRewardItem(cfg.getChangePoints());
				rewardItems.add(points);
				changedPoints += points.getItemCount();
			} else {
				rewardItems.add(reward);
			}
			if (cfg.getBigPrize() > 0) {
				bigPrize = true;
				bigPrizeItemId = reward.getItemId();
				bigPrizeItemCount += reward.getItemCount();
				DrawRecord record = new DrawRecord(poolCfg.getId(), poolObj.getPoolId(), 1, serverId, playerName, HawkTime.getMillisecond());
				drawRecordSave(record);
			}
		}
		
		initialRewards = RewardHelper.mergeRewardItem(initialRewards);
		rewardItems = RewardHelper.mergeRewardItem(rewardItems);
		this.getDataGeter().takeReward(playerId, rewardItems, 1, Action.BEST_PRIZE_POOL_DRAW, false, RewardOrginType.ACTIVITY_REWARD);
		
		//公告A奖显示方式：【xx区】【玩家昵称】运气逆天抽到【大奖池名字】【小奖池编号】箱，A奖【奖励道具*数量】
		if (bigPrize) {
			entity.addBigPoolDrawCount(poolCfg.getId(), 1);
			sendBroadcast(Const.NoticeCfgId.BEST_PRIZE_BIG_AWARD, null, serverId, playerName, poolCfg.getId(), poolObj.getPoolId(), bigPrizeItemId, bigPrizeItemCount);
		}
		//公告最终奖显示方式：【xx区】【玩家昵称】眼疾手快获得【大奖池名字】【小奖池编号】箱，最终奖【奖励道具*数量】
		if (lastReward) {
			entity.addBigPoolDrawCount(poolCfg.getId(), 1);
			DrawRecord record = new DrawRecord(poolCfg.getId(), poolObj.getPoolId(), 0, serverId, playerName, HawkTime.getMillisecond());
			drawRecordSave(record);
			RewardItem.Builder builder = RewardHelper.toRewardItem(poolCfg.getLastRewards());
			sendBroadcast(Const.NoticeCfgId.BEST_PRIZE_LAST_AWARD, null, serverId, playerName, poolCfg.getId(), poolObj.getPoolId(), builder.getItemId(), builder.getItemCount());
		}
		
		//抽奖人次增加
		String key = getDrawPlayerTimesKey();
		getRedis().increaseBy(key, 1, getExpireSeconds());
		
		StringJoiner sj = new StringJoiner(",");
		StringJoiner sj2 = new StringJoiner(",");
		//返回response
		BestPrizeDrawResp.Builder builder = BestPrizeDrawResp.newBuilder();
		builder.setBigPoolId(poolCfg.getId());
		builder.setSmallPoolId(poolObj.getPoolId());
		initialRewards.forEach(e -> builder.addReward(e));
		rewardItems.forEach(e -> builder.addFinalReward(e));
		builder.setChangePoints((int)changedPoints);
		for(Entry<Integer, Integer> entry : map.entrySet()) {
			DrawRewardPB.Builder drBuilder = DrawRewardPB.newBuilder();
			drBuilder.setCfgId(entry.getKey());
			drBuilder.setCount(entry.getValue());
			builder.addDrawReward(drBuilder);
			sj.add(entry.getKey() + "_" + entry.getValue());
			if (changeList.contains(entry.getKey())) {
				sj2.add(entry.getKey() + "_" + entry.getValue());
			}
		}
		pushToPlayer(playerId, HP.code2.BEST_PRIZE_DRAW_RESP_VALUE, builder);
		
		Map<String, Object> param = new HashMap<>();
		param.put("termId", getActivityTermId());
		param.put("groupId", groupId);
        param.put("bigPool", poolCfg.getId());             //大奖池ID
        param.put("smallPool", poolObj.getPoolId());       //小奖池ID
        param.put("lastReward", lastReward ? 1 : 0);       //本次是否得最终奖
        param.put("bigPrize", bigPrize ? 1 : 0);           //是否抽得大奖
        param.put("drawRewards", sj.toString());           //本次抽中的奖励ID和数量
        param.put("changeList", sj2.toString());    //本次抽中的奖励中被兑换的奖励
        getDataGeter().logActivityCommon(playerId, LogInfoType.best_prize_draw, param);
	}
	
	/**
	 * 抽奖记录存储
	 * @param record
	 */
	private void drawRecordSave(DrawRecord record) {
		try {
			String recordKey = poolDrawRecordKey();
			long length = getRedis().lPush(recordKey, getExpireSeconds(), record.toString());
			int maxLen = BestPrizeKVCfg.getInstance().getSaveDataLimit();
			if (maxLen > 0 && length > maxLen + 100) {
				getRedis().lTrim(recordKey, 0, maxLen);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 从时间上判断是否可以购买券
	 * @return
	 */
	private boolean canBuy() {
		long now = HawkTime.getMillisecond();
		if (BestPrizeKVCfg.getInstance().checkDrawTimeRange(now)) {
			return true;
		}
		int termId = getActivityTermId();
		BestPrizeTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(BestPrizeTimeCfg.class, termId);
		if (timeCfg == null) {
			return true;
		}
		
		long endDayZeroTime = HawkTime.getAM0Date(new Date(timeCfg.getEndTimeValue())).getTime();
		List<long[]> drawTimeArrList = BestPrizeKVCfg.getInstance().getDrawTimeArrList();
		long[] msArr = drawTimeArrList.get(drawTimeArrList.size() - 1);
		if (now < endDayZeroTime + msArr[1]) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 直购判断
	 * @param playerId
	 * @param goodsId
	 * @return
	 */
	public int buyItemCheck(String playerId, String goodsId) {
		BestPrizeShopCfg cfg = BestPrizeShopCfg.getConfig(goodsId);
		if (cfg == null) {
			HawkLog.errPrintln("BestPrizeActivity shop buyItem check error, shop config match null, playerId: {}, goodsId: {}", playerId, goodsId);
			return Status.Error.BEST_PRIZE_PAYGIFT_EMPTY_VALUE;
		}
		
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		if (!canBuy()) {
			return Status.Error.BEST_PRIZE_BUY_TIME_ERROR_VALUE;
		}
		
		Optional<BestPrizeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		BestPrizeEntity entity = opEntity.get();
		int boughtCount = entity.getShopItemMap().getOrDefault(cfg.getId(), 0);
		int newCount = boughtCount + 1;
		if (newCount > cfg.getTimes()) {
			HawkLog.errPrintln("BestPrizeActivity shop buyItem check limit error, playerId: {}, goodsId: {}, shopId: {}, oldCount: {}", playerId, goodsId, cfg.getId(), boughtCount);
			return Status.Error.BEST_PRIZE_SHOP_LIMIT_VALUE;
		}
		
		return 0;
	}
	
	/***
	 * 购买事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		String goodsId = event.getGiftId();
		BestPrizeShopCfg cfg = BestPrizeShopCfg.getConfig(goodsId);
		if (cfg == null) {
			HawkLog.errPrintln("BestPrizeActivity payGift callback error,  shop config match null, playerId: {}, goodsId: {}", playerId, goodsId);
			return;
		}
		
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<BestPrizeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		BestPrizeEntity entity = opEntity.get();
		shopBuyItem(playerId, entity, cfg, 1);
		//发收据邮件
		getDataGeter().sendMail(playerId, MailId.BEST_PRIZE_PURCHASE, null, null, null, RewardHelper.toRewardItemImmutableList(cfg.getGetItem()), true);
		
		HawkLog.logPrintln("BestPrizeActivity payGift finish, playerId: {}, goodsId: {}, shopId: {}", playerId, goodsId, cfg.getId());
		syncActivityInfo(playerId);
	}
	
	/**
	 * 商店购买
	 * @param playerId
	 */
	public int onShopBuy(String playerId, int shopId, int count) {
		if (count <= 0) {
			HawkLog.errPrintln("BestPrizeActivity shop buy param error, playerId: {}, count: {}", playerId, count);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		BestPrizeShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BestPrizeShopCfg.class, shopId);
		if (cfg == null) {
			HawkLog.errPrintln("BestPrizeActivity shop buy config error, playerId: {}, shopId: {}", playerId, shopId);
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		//需要通过直购付费的形式购买
		if (cfg.getShopItemType() == 1) {
			HawkLog.errPrintln("BestPrizeActivity shop buy type error, playerId: {}, shopId: {}, shopItemType: {}", playerId, shopId, cfg.getShopItemType());
			return Status.Error.BEST_PRIZE_SHOP_BUY_ERROR_VALUE;
		}
		
		Optional<BestPrizeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		BestPrizeEntity entity = opEntity.get();
		int boughtCount = entity.getShopItemMap().getOrDefault(shopId, 0);
		int newCount = boughtCount + count;
		if (newCount > cfg.getTimes()) {
			HawkLog.errPrintln("BestPrizeActivity shop buy limit error, playerId: {}, shopId: {}, oldCount: {}, newCount: {}", playerId, shopId, boughtCount, newCount);
			return Status.Error.BEST_PRIZE_SHOP_LIMIT_VALUE;
		}
		
		// 判断道具足够否
		if (!HawkOSOperator.isEmptyString(cfg.getPayItem())) {
			if (!canBuy()) {
				return Status.Error.BEST_PRIZE_BUY_TIME_ERROR_VALUE;
			}
			List<RewardItem.Builder> consumeItems = RewardHelper.toRewardItemImmutableList(cfg.getPayItem());
			boolean flag = this.getDataGeter().cost(playerId, consumeItems, count, Action.BEST_PRIZE_SHOP_BUY, false);
			if (!flag) {
				HawkLog.errPrintln("BestPrizeActivity shop buy consume error, playerId: {}, shopId: {}", playerId, shopId);
				return Status.Error.ITEM_NOT_ENOUGH_VALUE;
			}
		}
		
		shopBuyItem(playerId, entity, cfg, count);
		syncActivityInfo(playerId);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 商店够买
	 * @param playerId
	 * @param entity
	 * @param cfg
	 * @param count
	 */
	private void shopBuyItem(String playerId, BestPrizeEntity entity, BestPrizeShopCfg cfg, int count) {
		int shopId = cfg.getId();
		int boughtCount = entity.getShopItemMap().getOrDefault(shopId, 0);
		List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemImmutableList(cfg.getGetItem());
		this.getDataGeter().takeReward(playerId, rewardItems, count, Action.BEST_PRIZE_SHOP_BUY, true, RewardOrginType.ACTIVITY_REWARD);
		entity.getShopItemMap().put(shopId, boughtCount + count);
		entity.notifyUpdate();
		HawkLog.logPrintln("BestPrizeActivity shop buy success, playerId: {}, shopId: {}, old boughtCount: {}, count: {}", playerId, shopId, boughtCount, count);
	}
	
	/**
	 * 兑换商店积分兑换
	 * @param playerId
	 */
	public int onPointExchange(String playerId, int shopId, int count) {
		if (count <= 0) {
			HawkLog.errPrintln("BestPrizeActivity exchange param error, playerId: {}, count: {}", playerId, count);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		BestPrizeExchangeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BestPrizeExchangeCfg.class, shopId);
		if (cfg == null) {
			HawkLog.errPrintln("BestPrizeActivity exchange config error, playerId: {}, shopId: {}", playerId, shopId);
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		Optional<BestPrizeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		BestPrizeEntity entity = opEntity.get();
		int boughtCount = entity.getExchangeItemMap().getOrDefault(shopId, 0);
		int newCount = boughtCount + count;
		if (newCount > cfg.getTimes()) {
			HawkLog.errPrintln("BestPrizeActivity exchange limit error, playerId: {}, shopId: {}, oldCount: {}, newCount: {}", playerId, shopId, boughtCount, newCount);
			return Status.Error.BEST_PRIZE_EXCHANGE_LIMIT_VALUE;
		}
		
		List<RewardItem.Builder> consumeItems = RewardHelper.toRewardItemImmutableList(cfg.getNeedItem());
		boolean flag = this.getDataGeter().cost(playerId, consumeItems, count, Action.BEST_PRIZE_EXCHANGE, false);
		if (!flag) {
			HawkLog.errPrintln("BestPrizeActivity exchange consume error, playerId: {}, shopId: {}", playerId, shopId);
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}
		
		shopId = cfg.getId();
		boughtCount = entity.getExchangeItemMap().getOrDefault(shopId, 0);
		List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemImmutableList(cfg.getGainItem());
		this.getDataGeter().takeReward(playerId, rewardItems, count, Action.BEST_PRIZE_EXCHANGE, true, RewardOrginType.ACTIVITY_REWARD);
		entity.getExchangeItemMap().put(shopId, boughtCount + count);
		entity.notifyUpdate();
		HawkLog.logPrintln("BestPrizeActivity exchange success, playerId: {}, shopId: {}, old boughtCount: {}, count: {}", playerId, shopId, boughtCount, count);
		
		syncActivityInfo(playerId);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 单个大奖池信息
	 * @param playerId
	 * @param poolId
	 */
	public void syncBigPoolInfo(String playerId, int poolId) {
		if (!isOpening(playerId)) {
			return;
		}
		BestPrizePoolCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BestPrizePoolCfg.class, poolId);
		if (cfg == null) {
			return;
		}
		
		BigPoolInfoPB.Builder bigPoolInfo = buildBigPool(cfg);
		BestPrizeBigPoolResp.Builder resp = BestPrizeBigPoolResp.newBuilder();
		resp.setPoolInfo(bigPoolInfo);
		pushToPlayer(playerId, HP.code2.BEST_PRIZE_BIGPOOL_RESP_VALUE, resp);
	}
	
	/**
	 * 获取大奖池下的小奖池个数数据
	 * @return
	 */
	private Map<String, String> getPoolSizeMap() {
		String poolSizeKey = smallPoolSizeKey();
		Map<String, String> poolSizeMap = getRedis().hGetAll(poolSizeKey);
		return poolSizeMap;
	}
	
	/**
	 * 同步活动信息
	 */
	public void syncActivityDataInfo(String playerId){
		syncActivityInfo(playerId);
	}

	/**
	 * 同步活动信息
	 * 
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId){
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<BestPrizeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		BestPrizeEntity entity = opEntity.get();
		PBBestPrizeInfo.Builder info = PBBestPrizeInfo.newBuilder();
		for (Entry<Integer, Integer> entry : entity.getShopItemMap().entrySet()) {
			BestPrizeShopInfo.Builder builder = BestPrizeShopInfo.newBuilder();
			builder.setShopId(entry.getKey());
			builder.setCount(entry.getValue());
			info.addShopInfo(builder);
		}
		for (Entry<Integer, Integer> entry : entity.getExchangeItemMap().entrySet()) {
			BestPrizeExchangeInfo.Builder builder = BestPrizeExchangeInfo.newBuilder();
			builder.setExchangeId(entry.getKey());
			builder.setCount(entry.getValue());
			info.addExchangeInfo(builder);
		}
		
		for (Entry<Integer, Integer> entry : entity.getBigPoolDrawMap().entrySet()) {
			BigPoolBigAwardPB.Builder builder = BigPoolBigAwardPB.newBuilder();
			builder.setBigPoolId(entry.getKey());
			builder.setBigAwardTimes(entry.getValue());
			info.addBigAwardInfo(builder);
		}
		
		info.setTermId(this.getActivityTermId());
		info.addAllTips(getTips(BestPrizeExchangeCfg.class, entity.getTipSet()));
		info.setDrawConsume(entity.getDrawConsume());
		info.setGroupFinishTime(getGroupEndTime());
		
		if (groupId > 0) {
			info.setGroupUnfinish(0);
			info.addAllServerId(serverList);
			String drawPlayerTimes = getRedis().getString(getDrawPlayerTimesKey());
			int playerCount = HawkOSOperator.isEmptyString(drawPlayerTimes) ? 0 : Integer.parseInt(drawPlayerTimes);
			info.setPlayerCount(playerCount);
//			refreshAddPoolTime(HawkApp.getInstance().getCurrentTime());
//			info.setNextAddPoolTime(lastAddPoolTime.get() + BestPrizeKVCfg.getInstance().getDailyRefreshTime() + 2000L); //这里多加几秒，是为了防止卡时间点卡得太紧，客户端请求过来时，补箱子还没补完
			
			ConfigIterator<BestPrizePoolCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BestPrizePoolCfg.class);
			while(iterator.hasNext()) {
				BestPrizePoolCfg cfg = iterator.next();
				BigPoolInfoPB.Builder builder = buildBigPool(cfg);
				info.addBigPoolInfo(builder);
			}
			
			List<String> records = getRedis().lRange(poolDrawRecordKey(), 0, BestPrizeKVCfg.getInstance().getSaveDataLimit(), 0);
			for (String record : records) {
				DrawRecord obj = DrawRecord.toObject(record);
				if (obj != null) {
					info.addDrawRecord(obj.toBuilder());
				}
			}
		} else {
			info.setGroupUnfinish(1);
		}
		
		pushToPlayer(playerId, HP.code2.BEST_PRIZE_INFO_SYNC_VALUE, info);
	}

	/**
	 * 获取分组时间范围的结束时间
	 * @return
	 */
	private long getGroupEndTime() {
		return getTimeControl().getStartTimeByTermId(getActivityTermId()) + BestPrizeKVCfg.getInstance().getGroupingTime();
	}
	
	/**
	 * 构建奖池信息
	 * @param cfg
	 * @param poolSizeMap
	 * @return
	 */
	private BigPoolInfoPB.Builder buildBigPool(BestPrizePoolCfg cfg) {
		int bigPool = cfg.getId();
		String poolInfoKey = smallPoolInfoKey(bigPool);
		Map<String, String> map = getRedis().hGetAll(poolInfoKey);
		BigPoolInfoPB.Builder builder = BigPoolInfoPB.newBuilder();
		builder.setBigPoolId(bigPool);
		for (Entry<String,String> entry : map.entrySet()) {
			SmallPoolInfo poolObj = JSONObject.parseObject(entry.getValue(), SmallPoolInfo.class);
			SmallPoolInfoPB.Builder smallBuilder = SmallPoolInfoPB.newBuilder();
			smallBuilder.setSmallPoolId(poolObj.getPoolId());
			smallBuilder.setTotalDrawTimes(poolObj.getDrawTimesTotal());
			for (Entry<Integer, Integer> awardEntry : poolObj.getAwardDrawMap().entrySet()) {
				PoolAwardInfoPB.Builder awardBuilder = PoolAwardInfoPB.newBuilder();
				awardBuilder.setCfgId(awardEntry.getKey());
				awardBuilder.setDrawTimes(awardEntry.getValue());
				smallBuilder.addAwardInfo(awardBuilder);
			}
			builder.addSmallPool(smallBuilder);
		}
		
		int poolSize = poolCountMap.getOrDefault(bigPool, 0);
		builder.setSmallPoolCnt(poolSize);
		//已抽空的池子
		for (int poolId = 1; poolId <= poolSize; poolId++) {
			if (!map.containsKey(String.valueOf(poolId))) {
				SmallPoolInfoPB.Builder smallBuilder = SmallPoolInfoPB.newBuilder();
				smallBuilder.setSmallPoolId(poolId);
				smallBuilder.setTotalDrawTimes(BestPrizePoolAwardCfg.getlimitTimesByPoolId(bigPool));
				builder.addSmallPool(smallBuilder);
			}
		}
		
		return builder;
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
		if (!isOpening(playerId)) {
			return Optional.empty();
		}
		Optional<BestPrizeEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		BestPrizeEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(BestPrizeAchieveCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.BEST_PRIZE_ACHIEVE; 
	}
	
	/**
	 * 区服分组信息
	 * @return
	 */
	private String groupKey() {
		return BestPrizeRedisKey.SERVER_GROUP_KEY + ":" + this.getActivityTermId();
	}
	
	/**
	 * 添加奖池的锁
	 * @return
	 */
	private String addPoolLockKey() {
		if (groupId < 0) {
			throw new RuntimeException("groupId invalid");
		}
		return BestPrizeRedisKey.POOL_ADD_LOCK_KEY + ":" + this.getActivityTermId() + ":" + groupId;
	}
	/**
	 * 小奖池抽奖锁
	 * @param bigPool
	 * @param smallPool
	 * @return
	 */
	private String poolDrawLockKey(int bigPool) {
		if (groupId < 0) {
			throw new RuntimeException("groupId invalid");
		}
		return BestPrizeRedisKey.POOL_DRAW_LOCK_KEY + ":" + this.getActivityTermId() + ":" + groupId + ":" + bigPool;
	}
	
	/**
	 * 小奖池信息
	 * @param bigPool
	 * @return
	 */
	private String smallPoolInfoKey(int bigPool) {
		if (groupId < 0) {
			throw new RuntimeException("groupId invalid");
		}
		return BestPrizeRedisKey.POOL_INFO_KEY + ":" + this.getActivityTermId() + ":" + groupId + ":" + bigPool;
	}
	
	/**
	 * 小奖池数量
	 * @return
	 */
	private String smallPoolSizeKey() {
		if (groupId < 0) {
			throw new RuntimeException("groupId invalid");
		}
		return BestPrizeRedisKey.SMALL_POOL_COUNT_KEY + ":" + this.getActivityTermId() + ":" + groupId;
	}
	
	/**
	 * 大奖池每日添加小奖池数量
	 * @param bigPool
	 * @return
	 */
	private String smallPoolDailyAddKey(int bigPool) {
		if (groupId < 0) {
			throw new RuntimeException("groupId invalid");
		}
		return BestPrizeRedisKey.SMALL_POOL_DAILY_KEY + ":" + this.getActivityTermId() + ":" + groupId + ":" + bigPool;
	}
	
	/**
	 * 获取抽奖记录的key
	 * @return
	 */
	private String poolDrawRecordKey() {
		if (groupId < 0) {
			throw new RuntimeException("groupId invalid");
		}
		return BestPrizeRedisKey.POOL_DRAW_RECORD_KEY + ":" + this.getActivityTermId() + ":" + groupId;
	}
	
	/**
	 * 抽奖人次
	 * @return
	 */
	private String getDrawPlayerTimesKey() {
		return BestPrizeRedisKey.DRAW_TIMES_KEY + ":" + this.getActivityTermId() + ":" + groupId;
	}
	
	/**
	 * 获取上次补箱子的时间
	 * @return
	 */
	private String getAddPoolTimeKey() {
		return BestPrizeRedisKey.ADD_POOL_TIME_KEY + ":" + this.getActivityTermId() + ":" + groupId;
	}
	
	/**
	 * 此活动redis数据过期时间
	 * @return
	 */
	private int getExpireSeconds() {
		return 3600 * 24 * 30;
	}

	private HawkRedisSession getRedis() {
		return ActivityGlobalRedis.getInstance().getRedisSession();
	}
	
	/**
	 * redis访问内部接口，用于容错
	 * @return
	 */
	private String getString(String key, int expireSeconds) {
		try (Jedis jedis = getRedis().getJedis(); Pipeline pipeline = jedis.pipelined()) {
			Response<String> resp = pipeline.get(key);
			if (expireSeconds > 0) {
				pipeline.expire(key, expireSeconds);
			}
			pipeline.sync();
			return resp.get();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		throw new RuntimeException(key);
	}
}
