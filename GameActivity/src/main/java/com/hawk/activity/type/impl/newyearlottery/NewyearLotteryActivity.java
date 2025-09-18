package com.hawk.activity.type.impl.newyearlottery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.hawk.collection.ConcurrentHashTable;
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
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.google.common.collect.Table;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.GuildDismissEvent;
import com.hawk.activity.event.impl.GuildPayGiftAchieveNotifyEvent;
import com.hawk.activity.event.impl.GuildQuiteEvent;
import com.hawk.activity.event.impl.JoinGuildEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.newyearlottery.cfg.NewyearLotteryAchieveCfg;
import com.hawk.activity.type.impl.newyearlottery.cfg.NewyearLotteryActivityKVCfg;
import com.hawk.activity.type.impl.newyearlottery.cfg.NewyearLotteryGiftCfg;
import com.hawk.activity.type.impl.newyearlottery.cfg.NewyearLotterySlotCfg;
import com.hawk.activity.type.impl.newyearlottery.data.GuildPayGiftInfoObject;
import com.hawk.activity.type.impl.newyearlottery.entity.NewyearLotteryAchieveItem;
import com.hawk.activity.type.impl.newyearlottery.entity.NewyearLotteryEntity;
import com.hawk.activity.type.impl.newyearlottery.entity.PayGiftInfoItem;
import com.hawk.game.protocol.Activity.GiftBuyAchieveState;
import com.hawk.game.protocol.Activity.NewyearLotteryGiftInfoPB;
import com.hawk.game.protocol.Activity.NewyearLotteryGiftType;
import com.hawk.game.protocol.Activity.NewyearLotteryInfoPB;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 
 * 双旦活动
 * @author lating
 *
 */
public class NewyearLotteryActivity extends ActivityBase {
	/**
	 * 联盟内礼包购买和抽奖数据
	 */
	private Map<String, GuildPayGiftInfoObject> guildPayGiftInfoMap = new ConcurrentHashMap<>();
	/**
	 * 记录哪些玩家领过哪些成就奖励，用于跨天重置时补发排重 <dayTime, playerId, ids>
	 */
	private Table<String, String, List<Integer>> memberAchieveListTable = ConcurrentHashTable.create();
	/**
	 * 上一次tick检测的时间
	 */
	private long lastTicktime;
	/**
	 * 补发流程状态标识
	 */
	private AtomicBoolean reissueStatus = new AtomicBoolean(false);
	
	/**
	 * 公共数据存储的rediskey
	 */
	private final String guildPayGiftInfoKey = "newyear_lottery_guild_payinfo";
	private final String guildPayGiftMemberKey = "newyear_lottery_guild_paymember";
	private final String guildLotteryMemberKey = "newyear_lottery_guild_lotterymember";
	private final String personalAchieveRewardKey = "newyear_lottery_personal_achieve";
	
	/**
	 * redi存储过期时间
	 */
	private final int REDIS_EXPIRE_SECOND = (int)(HawkTime.DAY_MILLI_SECONDS / 1000);

	
	public NewyearLotteryActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.NEWYEAR_LOTTERY_ACTIVITY;
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		NewyearLotteryActivity activity = new NewyearLotteryActivity(config.getActivityId(), activityEntity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		NewyearLotteryEntity entity = new NewyearLotteryEntity(playerId, termId);
		return entity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<NewyearLotteryEntity> queryList = HawkDBManager.getInstance().query("from NewyearLotteryEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			NewyearLotteryEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, GameConst.MsgId.NEWYEAR_LOTTERTY_INIT, () -> {
				Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					HawkLog.errPrintln("newyearLotteryActivity open, init entity error, no entity created: {}", playerId);
					return;
				}

				NewyearLotteryEntity entity = opEntity.get();
				checkCrossDay(entity);
				syncAcitivtyDataInfo(entity);
			});
		}
	}
	
	@Override
	public void onEnd() {
		boolean status = reissueStatus.getAndSet(true);
		// 别处已经在走奖励补发的流程了
		if (status) {
			HawkLog.errPrintln("newyearLotteryActivity end, reissueStatus is true, time: {}", HawkTime.formatTime(HawkTime.getMillisecond()));
			return;
		}
		
		long now = HawkTime.getMillisecond();
		// 这里往前推一个小时，是为了防止一种极端情况：系统出现了延迟，等跨天了才走进onEnd方法，往前推一点事件才能取到正确的dayTime
		String dayTime = HawkTime.formatTime(now - HawkTime.HOUR_MILLI_SECONDS, "yyyyMMdd");
		HawkLog.logPrintln("newyearLotteryActivity end, reissueStatus begin, dayTime: {}, now time: {}", dayTime, HawkTime.formatTime(now));
		
		crossDayReset(dayTime, true);
	}

	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		NewyearLotteryEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
				
		// 初始添加成就项
		List<NewyearLotteryAchieveItem> list = new ArrayList<NewyearLotteryAchieveItem>();
		ConfigIterator<NewyearLotteryAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(NewyearLotteryAchieveCfg.class);
		while (configIterator.hasNext()) {
			NewyearLotteryAchieveCfg achieveCfg = configIterator.next();
			NewyearLotteryAchieveItem item = NewyearLotteryAchieveItem.valueOf(achieveCfg.getId());
			if (item.getValue() >= achieveCfg.getValue()) {
				item.setValue(achieveCfg.getValue());
				item.setState(GiftBuyAchieveState.NEWYEAR_LOTTERY_NOT_REWARD_VALUE);
			}
			list.add(item);
		}

		entity.resetItemList(list);
	}
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (isHidden(playerId)) {
			clearLotteryTicket(playerId);
			return;
		}
		Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		NewyearLotteryEntity entity = opEntity.get();
		checkCrossDay(entity);
		refreshAchieveData(entity);
		syncAcitivtyDataInfo(entity);
	}
	
	/**
	 * 个人登录跨天检测
	 * @param entity
	 */
	private boolean checkCrossDay(NewyearLotteryEntity entity) {
		String dayTime = HawkTime.formatNowTime("yyyyMMdd");
		if (dayTime.equals(entity.getDayTime())) {
			return false;
		}
		
		// 跨天了
		entity.setDayTime(dayTime);
		resetPersonalActivityData(entity);
		clearLotteryTicket(entity.getPlayerId());
		return true;
	}
	
	/**
	 * 清除抽奖券
	 */
	private void clearLotteryTicket(String playerId) {
		NewyearLotteryActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(NewyearLotteryActivityKVCfg.class);
		for (int lotteryType : NewyearLotteryGiftCfg.getLotteryTypes()) {
			int itemId = kvCfg.getCostItemId(lotteryType);
			int itemCount = getDataGeter().getItemNum(playerId, itemId);
			if (itemCount > 0) {
				List<RewardItem.Builder> consumeItems = kvCfg.getLotteryCost(lotteryType);
				consumeItems.stream().forEach(e -> e.setItemCount(itemCount));
				this.getDataGeter().consumeItems(playerId, consumeItems, 0, Action.NEWYEAR_LOTTERY_ITEM_REMOVE_CROSSDAY);
			}
		}
	}
	
	/**
	 * 重置个人活动数据
	 * 
	 * @param entity
	 */
	private void resetPersonalActivityData(NewyearLotteryEntity entity) {
		try {
			entity.getItemList().clear();
			// 初始化成就数据
			initAchieveInfo(entity.getPlayerId());
			// 初始化礼包数据
			initLotteryGiftData(entity);
			entity.notifyUpdate();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 初始化礼包数据
	 * 
	 * @param entity
	 */
	private void initLotteryGiftData(NewyearLotteryEntity entity) {
		entity.getGiftItemList().clear();
		String guildId = getDataGeter().getGuildId(entity.getPlayerId());
		for (int lotteryType : NewyearLotteryGiftCfg.getLotteryTypes()) {
			NewyearLotteryGiftCfg cfg = NewyearLotteryGiftCfg.getDefaultRewardGiftCfg(lotteryType);
			String randomReward = HawkRand.randomWeightObject(cfg.getRandomRewardItems(), cfg.getRandomRewardWeight());
			PayGiftInfoItem item = PayGiftInfoItem.valueOf(lotteryType, cfg.getId(), randomReward);
			entity.getGiftItemList().add(item);
			if (!HawkOSOperator.isEmptyString(guildId)) {
				String guildType = getMapKey(guildId, lotteryType, entity.getDayTime());
				guildPayGiftInfoMap.putIfAbsent(guildType, GuildPayGiftInfoObject.valueOf(guildId, lotteryType, entity.getDayTime()));
			}
		}
	}
	
	/**
	 * 刷新成就数据
	 * 
	 * @param entity
	 */
	private void refreshAchieveData(NewyearLotteryEntity entity) {
		String guildId = getDataGeter().getGuildId(entity.getPlayerId());
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		
		List<PayGiftInfoItem> list = entity.getGiftItemList();
		boolean update = false;
		for (PayGiftInfoItem item : list) {
			GuildPayGiftInfoObject obj = getPayGiftCountObject(guildId, item.getLotteryType(), entity.getDayTime());
			if (obj == null) {
				continue;
			}
			
			if (item.getAchieveValue() == obj.getPayCount()) {
				continue;
			}
			
			update = true;
			item.setAchieveValue(obj.getPayCount());
			for (NewyearLotteryAchieveItem achieveItem : entity.getItemList()) {
				NewyearLotteryAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewyearLotteryAchieveCfg.class, achieveItem.getAchieveId());
				if (cfg.getLotteryType() != item.getLotteryType()) {
					continue;
				}
				
				if (achieveItem.getState() == GiftBuyAchieveState.NEWYEAR_LOTTERY_TOOK_VALUE) {
					continue;
				}
				int newVal = Math.min(cfg.getValue(), item.getAchieveValue());
				achieveItem.setValue(newVal);
				if (newVal < cfg.getValue()) {
					continue;
				}
				
				long achieveTime = obj.getAchieveTimeMap().getOrDefault(newVal, 0L);
				long joinTime = getJoinGuildTime(entity.getPlayerId());
				if (achieveTime == 0 || achieveTime > joinTime) {
					achieveItem.setState(GiftBuyAchieveState.NEWYEAR_LOTTERY_NOT_REWARD_VALUE);
				} else {
					achieveItem.setState(GiftBuyAchieveState.NEWYEAR_LOTTERY_CANNOT_REWARD_VALUE);
				}
			}
		}
		
		if (update) {
			entity.notifyUpdate();
			syncAcitivtyDataInfo(entity);
		}
	}
	
	/**
	 * 获取对象
	 * 
	 * @param guildId
	 * @param lotteryType
	 * @param dayTime
	 * @return
	 */
	private GuildPayGiftInfoObject getPayGiftCountObject(String guildId, int lotteryType, String dayTime) {
		String guildType = getMapKey(guildId, lotteryType, dayTime);
		GuildPayGiftInfoObject obj = guildPayGiftInfoMap.get(guildType);
		return obj;
	}

	@Override
	public void onTick() {
		// 判断活动是否开启
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN || this.isGmClose()) {
			return;
		}
		
		long now = HawkTime.getMillisecond();
		if (lastTicktime == 0) {
			lastTicktime = now;
			initGuildPayGiftInfo(now);
			return;
		}
		
		boolean sameday = HawkTime.isSameDay(lastTicktime, now);
		long tmpTime = lastTicktime;
		lastTicktime = now;
		// 跨天了，需要重置数据
		if (!sameday) {
			HawkLog.logPrintln("newyear lottery activity on tick cross day, time: {}", HawkTime.formatTime(now));
			String dayTime = HawkTime.formatTime(tmpTime, "yyyyMMdd");
			boolean status = reissueStatus.getAndSet(true);
			if (!status) {
				HawkLog.logPrintln("newyear lottery activity on tick cross day reset, time: {}", HawkTime.formatTime(now));
				crossDayReset(dayTime, false);
			}
		} else {
			notifyUpdateAchieveData(now);
		}
	}
	
	/**
	 * 通知玩家更新自己的成就数据
	 * 
	 * @param timeNow
	 */
	private void notifyUpdateAchieveData(long timeNow) {
		for (Entry<String, GuildPayGiftInfoObject> entry : guildPayGiftInfoMap.entrySet()) {
			try {
				GuildPayGiftInfoObject obj = entry.getValue();
				if (obj.getNextCheckPayCountAchieveTime() > timeNow) {
					continue;
				}
				
				obj.updateNextCheckPayCountAchieveTime();
				if (obj.getHistoryPayCount() == obj.getPayCount()) {
					continue;
				}
				
				obj.setHistoryPayCount(obj.getPayCount());
				List<String> onlineMembers = getDataGeter().getOnlineGuildMemberIds(obj.getGuildId());
				for (String playerId : onlineMembers) {
					ActivityManager.getInstance().postEvent(new GuildPayGiftAchieveNotifyEvent(playerId));
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 初始化联盟购买礼包信息公共数据
	 */
	private void initGuildPayGiftInfo(long now) {
		String dayTime = HawkTime.formatTime(now, "yyyyMMdd");
		String yestodayTime = HawkTime.formatTime(now - HawkTime.DAY_MILLI_SECONDS, "yyyyMMdd");
		// 当前服处理
		String serverId = this.getDataGeter().getServerId();
		initGuildPayGiftByServer(serverId, dayTime, yestodayTime);
		
		// 针对合服的处理
		List<String> serverList = this.getDataGeter().getMergeServerList();
		if (serverList == null || serverList.isEmpty()) {
			return;
		}
		
		for (String server : serverList) {
			if (serverId.equals(server)) {
				continue;
			}
			initGuildPayGiftByServer(server, dayTime, yestodayTime);
		}
	}
	
	/**
	 * 按区服初始化联盟购买礼包信息公共数据
	 * @param serverId
	 * @param dayTime
	 * @param yestodayTime
	 */
	private void initGuildPayGiftByServer(String serverId, String dayTime, String yestodayTime) {
		String redisKey = getGuildPayGiftInfoRedisKey(serverId); // subkey -> [guildId]:[lotteryType]:[yearDay]
		Map<String, String> map = ActivityGlobalRedis.getInstance().getRedisSession().hGetAll(redisKey);
		if (map.isEmpty()) {
			return;
		}
		
		String curServerId = this.getDataGeter().getServerId();
		List<String> delRedisKey = new ArrayList<>();
		List<GuildPayGiftInfoObject> reissueObjList = new ArrayList<>();
		
		// 判断时间，如果不是当天，要清理+补发
		for (Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			// 当天的数据直接加载到内存里面
			if (key.indexOf(dayTime) > 0) {
				if (!serverId.equals(curServerId)) {
					ActivityGlobalRedis.getInstance().getRedisSession().hDel(getGuildPayGiftInfoRedisKey(serverId), key);
					ActivityGlobalRedis.getInstance().getRedisSession().hSet(getGuildPayGiftInfoRedisKey(curServerId), key, value);
				}
				initGuildPayGiftInfoOfCurDay(dayTime, key, value);
				continue;
			}
			
			delRedisKey.add(key);  // 不是当天的，要将rediskey记下待后面统一删除
			if (key.indexOf(yestodayTime) > 0) {
				try {
					GuildPayGiftInfoObject obj = GuildPayGiftInfoObject.toObject(value);
					reissueObjList.add(obj);
				} catch (Exception e) {
					HawkLog.logPrintln("newyear lottery activity init lottery data, yestoday key: {}, val: {}", key, value);
					HawkException.catchException(e);
				}
			} else {
				// 超过一天的就不处理了，基本上是垃圾数据，直接输出日志打印就行了，如果是特殊情况根据日志再处理
				HawkLog.logPrintln("newyear lottery activity init lottery data, serverId: {}, key: {}, val: {}", getDataGeter().getServerId(), key, value);
			}
		}
		
		if (!delRedisKey.isEmpty()) {
			reissueOnInit(reissueObjList, delRedisKey, serverId);
		}
	}
	
	/**
	 * 初始化当天的联盟购买礼包信息数据
	 * @param dayTime
	 * @param dataKey
	 * @param dataVal
	 */
	private void initGuildPayGiftInfoOfCurDay(String dayTime, String dataKey, String dataVal) {
		try {
			GuildPayGiftInfoObject obj = GuildPayGiftInfoObject.toObject(dataVal);
			
			List<String> payMembers = getPayMemberListFromRedis(obj.getGuildId(), obj.getLotteryType(), dayTime);
			List<String> lotteryMembers = getLotteryMemberListFromRedis(obj.getGuildId(), obj.getLotteryType(), dayTime);
			obj.getPayMembers().addAll(payMembers);
			obj.getLotteryMembers().addAll(lotteryMembers);
			
			Map<String, String> memberAchieveIdsMap = getMemberAchieveIdsFromRedis(obj.getGuildId(), dayTime);
			for (Entry<String, String> entry : memberAchieveIdsMap.entrySet()) {
				String playerId = entry.getKey();
				if (HawkOSOperator.isEmptyString(entry.getValue())) {
					continue;
				}
				HawkLog.logPrintln("newyear lottery activity init achieve data, playerId: {}, guildId: {}, dayTime: {}, achieveIds: {}", playerId, obj.getGuildId(), dayTime, entry.getValue());
				String[] ids = entry.getValue().split(",");
				List<Integer> idList = Arrays.asList(ids).stream().map(e -> Integer.parseInt(e)).collect(Collectors.toList());
				List<Integer> achieveIds = memberAchieveListTable.get(dayTime, playerId);
				if (achieveIds == null) {
					memberAchieveListTable.put(dayTime, playerId, idList);
				} else {
					achieveIds.addAll(idList);
				}
			}
			
			String mapKey = getMapKey(obj.getGuildId(), obj.getLotteryType(), dayTime);
			guildPayGiftInfoMap.put(mapKey, obj);  
		} catch (Exception e) {
			HawkLog.errPrintln("newyear lottery activity init data failed, key: {}, data: {}", dataKey, dataVal);
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 起服初始化时补发奖励
	 * 
	 * @param reissueObjList
	 * @param delRedisKey
	 */
	private void reissueOnInit(List<GuildPayGiftInfoObject> reissueObjList, List<String> delRedisKey, String serverId) {
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				for (GuildPayGiftInfoObject obj : reissueObjList) {
					HawkLog.logPrintln("newyear lottery reissue on init, guildId: {}, lotteryType: {}, dayTime: {}", obj.getGuildId(), obj.getLotteryType(), obj.getDayTime());
					try {
						int maxConditionVal = NewyearLotteryAchieveCfg.getMaxConditionVal(obj.getLotteryType());
						// 满足解锁抽奖奖池的条件才需要加载相关数据
						if (obj.getPayCount() >= maxConditionVal) {
							List<String> payMembers = getPayMemberListFromRedis(obj.getGuildId(), obj.getLotteryType(), obj.getDayTime());
							List<String> lotteryMembers = getLotteryMemberListFromRedis(obj.getGuildId(), obj.getLotteryType(), obj.getDayTime());
							obj.getPayMembers().addAll(payMembers);
							obj.getLotteryMembers().addAll(lotteryMembers);
						}
						lotteryReissueCrossDay(obj);
						
						Map<String, String> map = getMemberAchieveIdsFromRedis(obj.getGuildId(), obj.getDayTime());
						for (Entry<String, String> entry : map.entrySet()) {
							String playerId = entry.getKey();
							if (HawkOSOperator.isEmptyString(entry.getValue())) {
								continue;
							}
							HawkLog.logPrintln("newyear lottery activity init achieve data, playerId: {}, guildId: {}, dayTime: {}, achieveIds: {}", playerId, obj.getGuildId(), obj.getDayTime(), entry.getValue());
							String[] ids = entry.getValue().split(",");
							List<Integer> idList = Arrays.asList(ids).stream().map(e -> Integer.parseInt(e)).collect(Collectors.toList());
							List<Integer> achieveIds = memberAchieveListTable.get(obj.getDayTime(), playerId);
							if (achieveIds == null) {
								memberAchieveListTable.put(obj.getDayTime(), playerId, idList);
							} else {
								achieveIds.addAll(idList);
							}
						}
						achieveRewardReissueCrossDay(obj);
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
				
				String[] array = delRedisKey.toArray(new String[delRedisKey.size()]);
				ActivityGlobalRedis.getInstance().getRedisSession().hDel(getGuildPayGiftInfoRedisKey(serverId), array);
				return null;
			}
		});
	}
	
	/**
	 * 公共数据跨天重置（前一天未领取奖励的在此时补发）
	 */
	private void crossDayReset(String dayTime, boolean end) {
		String today = HawkTime.formatNowTime("yyyyMMdd");
		Map<String, GuildPayGiftInfoObject> map = new HashMap<>(guildPayGiftInfoMap);
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				try {
					List<String> delRedisKey = new ArrayList<>();
					for (Entry<String, GuildPayGiftInfoObject> entry : map.entrySet()) {
						String mapKey = entry.getKey();
						// 非dayTime当天的数据
						if (mapKey.indexOf(dayTime) < 0) {
							continue;
						}
						
						HawkLog.logPrintln("newyear lottery crossDay reset, mapKey: {}", mapKey);
						GuildPayGiftInfoObject obj = entry.getValue();
						guildPayGiftInfoMap.remove(mapKey);
						if (!end) {
							String newMapKey = getMapKey(obj.getGuildId(), obj.getLotteryType(), today);
							guildPayGiftInfoMap.putIfAbsent(newMapKey, GuildPayGiftInfoObject.valueOf(obj.getGuildId(), obj.getLotteryType(), today));
						}
						
						delRedisKey.add(mapKey);
						lotteryReissueCrossDay(obj);
						achieveRewardReissueCrossDay(obj);
					}
					
					if (!delRedisKey.isEmpty()) {
						try {
							String[] array = delRedisKey.toArray(new String[delRedisKey.size()]);
							ActivityGlobalRedis.getInstance().getRedisSession().hDel(getGuildPayGiftInfoRedisKey(), array);
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
				// 最后要将补发流程的状态重置回来
				reissueStatus.set(false);
				
				return null;
			}
		});
	}
	
	/**
	 * 抽奖跨天补发：针对前一天满足条件但未抽奖的玩家
	 * 
	 * @param obj
	 */
	private void lotteryReissueCrossDay(GuildPayGiftInfoObject obj) {
		try {
			int maxConditionVal = NewyearLotteryAchieveCfg.getMaxConditionVal(obj.getLotteryType());
			// 不满足解锁抽奖奖池的条件
			if (obj.getPayCount() < maxConditionVal) {
				return;
			}
			
			Set<String> payMemberSet = obj.getPayMembers();
			Set<String> lottertMemberSet = obj.getLotteryMembers();
			payMemberSet.removeAll(lottertMemberSet);
			// 已买过该礼包的成员都抽过奖了
			if (payMemberSet.isEmpty()) {
				return;
			}
			
			HawkLog.logPrintln("newyear lottery crossDay reissue start, guildId: {}, lotteryType: {}, dayTime: {}", obj.getGuildId(), obj.getLotteryType(), obj.getDayTime());
			for (String playerId : payMemberSet) {
				if (obj.getDayTime().equals("20240428")) {
					Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(playerId);
					if (opEntity.isPresent() && opEntity.get().getGiftItemByType(obj.getLotteryType()).getLotteryAwardId() > 0) {
						continue;
					}
				}
				
				String playerGuildId = getDataGeter().getGuildId(playerId);
				NewyearLotterySlotCfg cfg = sendLotteryRewardMail(obj, playerId, playerGuildId, true);
				if (cfg!= null && cfg.getIsBigPrize() > 0) {
					sendBroadcast(obj.getLotteryType(), playerId, obj.getGuildId());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 抽奖奖励邮件发放
	 * 
	 * @param obj
	 * @param playerId
	 */
	private NewyearLotterySlotCfg sendLotteryRewardMail(GuildPayGiftInfoObject obj, String playerId, String playerGuildId, boolean mail) {
		if (!obj.getGuildId().equals(playerGuildId)) {
			return null;
		}
		
		int luckyNum = obj.getLotteryLuckyNum();
		obj.setLotteryCount(obj.getLotteryCount() + 1);
		obj.getLotteryMembers().add(playerId);
		HawkLog.logPrintln("newyear lottery reissue, guildId: {}, lotteryType: {}, playerId: {}, dayTime: {}", obj.getGuildId(), obj.getLotteryType(), playerId, obj.getDayTime());
		List<NewyearLotterySlotCfg> cfgList = NewyearLotterySlotCfg.getCfgList(obj.getLotteryType());
		NewyearLotterySlotCfg cfg = null;
		if (luckyNum == 0 && obj.getLotteryCount() >= obj.getLotteryLuckyMax()) {
			cfg = cfgList.stream().filter(e -> e.getIsBigPrize() > 0).findAny().get();
		} else {
			List<Integer> weightList = NewyearLotterySlotCfg.getCfgWeightList(obj.getLotteryType());
			cfg = HawkRand.randomWeightObject(cfgList, weightList);
		}
		
		if (cfg.getIsBigPrize() > 0 && luckyNum == 0) {
			obj.setLotteryLuckyNum(obj.getLotteryCount());
		}
		
		if (mail) {
			MailId mailId = obj.getLotteryType() == NewyearLotteryGiftType.LOTTERY_GIFT_COMMON_VALUE ? MailId.NEW_YEAR_LOTTERY_COMMON : MailId.NEW_YEAR_LOTTERY_ADVANCE;
			List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemImmutableList(cfg.getRewards());
			Object[] content = new Object[]{cfg.getId()};
			getDataGeter().sendMail(playerId, mailId, null, null, content, rewardList, false);
		} else {
			flushDataToRedis(obj);
			addLotteryMemberToRedis(obj.getGuildId(), obj.getLotteryType(), obj.getDayTime(), playerId);
		}
		
		return cfg;
	}
	
	/**
	 * 成就奖励跨天补发：针对前一天还有成就奖励未领取的玩家
	 * 
	 * @param obj
	 */
	private void achieveRewardReissueCrossDay(GuildPayGiftInfoObject obj) {
		Map<String, List<Integer>> rowMap = new HashMap<>(memberAchieveListTable.row(obj.getDayTime()));
		try {
			HawkLog.logPrintln("newyear lottery crossDay achieve reissue start, guildId: {}, lotteryType: {}, dayTime: {}", obj.getGuildId(), obj.getLotteryType(), obj.getDayTime());
			Collection<String> memberIds = getDataGeter().getGuildMemberIds(obj.getGuildId());
			for (String memberId : memberIds) {
				long joinTime = getJoinGuildTime(memberId);
				sendAchieveRewardMail(obj, memberId, joinTime, null);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		List<Integer> idList = HawkConfigManager.getInstance().getConfigIterator(NewyearLotteryAchieveCfg.class).stream().filter(e -> e.getLotteryType() == obj.getLotteryType()).map(e -> e.getId()).collect(Collectors.toList());
		// 确保内存数据清理，redis中的数据不用担心，有一天的过期时间
		for (String memberId : rowMap.keySet()) {
			String guildId = getDataGeter().getGuildId(memberId);
			if (!obj.getGuildId().equals(guildId)) {
				continue;
			}
			List<Integer> achieveIdList = memberAchieveListTable.get(obj.getDayTime(), memberId);
			if (achieveIdList != null && !achieveIdList.isEmpty()) {
				achieveIdList.removeAll(idList);
			} 
			
			if (achieveIdList == null || achieveIdList.isEmpty()) {
				memberAchieveListTable.remove(obj.getDayTime(), memberId);
			}
		}
	}
	
	/**
	 * 获取玩家加入联盟的时间
	 * 
	 * @param playerId
	 * @return
	 */
	private long getJoinGuildTime(String playerId) {
		long time = getDataGeter().getJoinGuildTime(playerId);
		if (time > 0) {
			return time;
		}
		
		String guildId = getDataGeter().getGuildId(playerId);
		String leaderId = getDataGeter().getGuildLeaderId(guildId);
		long now = HawkTime.getMillisecond();
		if (playerId.equals(leaderId)) {
			return now - HawkTime.DAY_MILLI_SECONDS;
		}
		
		return now;
	}
	
	/**
	 * 成就任务进度奖励邮件发放
	 * 
	 * @param obj
	 * @param memberId
	 * @param joinGuidTime
	 */
	private void sendAchieveRewardMail(GuildPayGiftInfoObject obj, String memberId, long joinGuidTime, NewyearLotteryEntity entity) {
		List<Integer> achieveIdList = memberAchieveListTable.get(obj.getDayTime(), memberId);
		if (achieveIdList == null) {
			achieveIdList = new ArrayList<>();
			memberAchieveListTable.put(obj.getDayTime(), memberId, achieveIdList);
		}
		
		Map<Integer, Long> achieveTimeMap = obj.getAchieveTimeMap();
		MailId mailId = obj.getLotteryType() == NewyearLotteryGiftType.LOTTERY_GIFT_COMMON_VALUE ? MailId.NEW_YEAR_LOTTERY_ACHIEVE_COMMON : MailId.NEW_YEAR_LOTTERY_ACHIEVE_ADVANCE;
		ConfigIterator<NewyearLotteryAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(NewyearLotteryAchieveCfg.class);
		while (iterator.hasNext()) {
			NewyearLotteryAchieveCfg achieveCfg = iterator.next();
			if (achieveCfg.getLotteryType() != obj.getLotteryType()) {
				continue;
			}
			
			if (achieveIdList.contains(achieveCfg.getId())) {
				continue;
			}
			
			long time = achieveTimeMap.getOrDefault(achieveCfg.getValue(), 0L);
			// 不是免费档次，且在联盟进度达成之后才入盟
			if (joinGuidTime > time && achieveCfg.getValue() > 0) {
				continue;
			}
			
			if (entity != null) {
				NewyearLotteryAchieveItem item = entity.getAchieveItem(achieveCfg.getId());
				if (item == null || item.getState() == GiftBuyAchieveState.NEWYEAR_LOTTERY_TOOK_VALUE) {
					continue;
				} else {
					item.setValue(achieveCfg.getValue());
					item.setState(GiftBuyAchieveState.NEWYEAR_LOTTERY_TOOK_VALUE);
					entity.notifyUpdate();
				}
			}
			
			achieveIdList.add(achieveCfg.getId());
			
			HawkLog.logPrintln("newyear lottery achieve reissue, playerId: {}, achieveId: {}, dayTime: {}", memberId, achieveCfg.getId(), obj.getDayTime());
			List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemImmutableList(achieveCfg.getRewards());
			if (achieveCfg.getValue() == 0) {
				MailId freeMailId = obj.getLotteryType() == NewyearLotteryGiftType.LOTTERY_GIFT_COMMON_VALUE ? MailId.NEW_YEAR_LOTTERY_ACHIEVE_COMMON_FREE : MailId.NEW_YEAR_LOTTERY_ACHIEVE_ADVANCE_FREE;
				getDataGeter().sendMail(memberId, freeMailId, null, null, null, rewardList, false);
			} else {
				Object[] content = new Object[]{achieveCfg.getValue()};
				getDataGeter().sendMail(memberId, mailId, null, null, content, rewardList, false);
			}
		}
	}
	
	/**
	 * 加入联盟
	 */
	@Subscribe
	public void onJoinGuild(JoinGuildEvent event) {
		String playerId = event.getPlayerId();
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		
		if (isHidden(event.getPlayerId())) {
			return;
		}
		
		Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		HawkLog.logPrintln("newyear lottery player join guild, playerId: {}, guildId: {}", playerId, guildId);
		NewyearLotteryEntity entity = opEntity.get();
		checkCrossDay(entity);
		
		for (int lotteryType : NewyearLotteryGiftCfg.getLotteryTypes()) {
			String guildType = getMapKey(guildId, lotteryType, entity.getDayTime());
			guildPayGiftInfoMap.putIfAbsent(guildType, GuildPayGiftInfoObject.valueOf(guildId, lotteryType, entity.getDayTime()));
		}
		
		List<Integer> achieveIdList = memberAchieveListTable.get(entity.getDayTime(), playerId);
		
		// 重新刷新成就
		List<PayGiftInfoItem> list = entity.getGiftItemList();
		for (PayGiftInfoItem item : list) {
			GuildPayGiftInfoObject obj = getPayGiftCountObject(guildId, item.getLotteryType(), entity.getDayTime());
			if (obj == null) {
				continue;
			}
			
			// 在本盟买的礼包，转到别的盟抽过奖，之后又转会本盟，记录该玩家已抽过奖
			if (obj.getPayMembers().contains(playerId) && item.getLotteryAwardId() > 0 && !obj.getLotteryMembers().contains(playerId)) {
				synchronized (obj) {
					obj.setLotteryCount(obj.getLotteryCount() + 1);
					obj.getLotteryMembers().add(playerId);
					flushDataToRedis(obj);
				}
				addLotteryMemberToRedis(guildId, item.getLotteryType(), obj.getDayTime(), playerId);
			}
			
			item.setAchieveValue(obj.getPayCount());
			for (NewyearLotteryAchieveItem achieveItem : entity.getItemList()) {
				NewyearLotteryAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewyearLotteryAchieveCfg.class, achieveItem.getAchieveId());
				if (cfg.getLotteryType() != item.getLotteryType()) {
					continue;
				}
				
				if (achieveItem.getState() == GiftBuyAchieveState.NEWYEAR_LOTTERY_TOOK_VALUE) {
					continue;
				}
				
				int newVal = Math.min(cfg.getValue(), item.getAchieveValue());
				achieveItem.setValue(newVal);
				if (cfg.getValue() == 0) {  // 免费
					achieveItem.setState(GiftBuyAchieveState.NEWYEAR_LOTTERY_NOT_REWARD_VALUE);
				} else if (newVal < cfg.getValue()) { // 未达成
					achieveItem.setState(GiftBuyAchieveState.NEWYEAR_LOTTERY_NOT_ACHIEVE_VALUE);
				} else if (achieveIdList != null && achieveIdList.contains(achieveItem.getAchieveId())) { //  已经领取过了
					achieveItem.setState(GiftBuyAchieveState.NEWYEAR_LOTTERY_TOOK_VALUE);
				} else { // 已达成不可领取
					achieveItem.setState(GiftBuyAchieveState.NEWYEAR_LOTTERY_CANNOT_REWARD_VALUE);
				}
			}
		}

		entity.notifyUpdate();
		int state = GiftBuyAchieveState.NEWYEAR_LOTTERY_TOOK_VALUE;
		// 对前面可能被剔出联盟或解散联盟的补救处理
		if (achieveIdList != null) {
			for (NewyearLotteryAchieveItem item : entity.getItemList()) {
				if (item.getState() != state && achieveIdList.contains(item.getAchieveId())) {
					NewyearLotteryAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewyearLotteryAchieveCfg.class, item.getAchieveId());
					item.setValue(cfg.getValue());
					item.setState(state);
					entity.notifyUpdate();
				}
			}
			
			List<Integer> lotteryAwardIds = achieveIdList.stream().filter(e -> e < 0).map(e -> Math.abs(e)).collect(Collectors.toList());
			for (int awardCfgId : lotteryAwardIds) {
				NewyearLotterySlotCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewyearLotterySlotCfg.class, awardCfgId);
				PayGiftInfoItem item = entity.getGiftItemByType(cfg.getLotteryType());
				item.setLotteryAwardId(cfg.getId());
				entity.notifyUpdate();
			}
		}
		
		achieveIdList = entity.getItemListByState(state).stream().map(e -> e.getAchieveId()).collect(Collectors.toList());
		memberAchieveListTable.put(entity.getDayTime(), playerId, achieveIdList);
		if (!achieveIdList.isEmpty()) {
			String achieveIds = SerializeHelper.collectionToString(achieveIdList, ",");
			HawkLog.logPrintln("newyear lottery join guild flush achieveIds, playerId: {}, guildId: {}, dayTime: {}, achieveIds: {}", playerId, guildId, entity.getDayTime(), achieveIds);
			flushAchieveDataToRedis(playerId, guildId, achieveIds, entity.getDayTime());
		}
		
		syncAcitivtyDataInfo(entity);
	}
	
	/**
	 * 联盟退出
	 * 
	 * @param event
	 */
	@Subscribe
	public void onGuildQuit(GuildQuiteEvent event){
		String playerId = event.getPlayerId();
		if (isHidden(playerId)) {
			return;
		}
		
		String guildId = event.getGuildId();
		HawkLog.logPrintln("newyear lottery player leave guild, playerId: {}, guildId: {}, joinTime: {}", playerId, guildId, event.getJoinGuildTime());
		long joinTime = event.getJoinGuildTime();
		if (joinTime <= 0) {
			joinTime = HawkTime.getMillisecond() - HawkTime.DAY_MILLI_SECONDS;
		}
		playerLeaveGuild(playerId, guildId, joinTime);
	}
	
	/**
	 * 联盟解散
	 * 
	 * @param event
	 */
	@Subscribe
	public void onGuildDismiss(GuildDismissEvent event) {
		String guildId = event.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		
		if (isHidden(event.getPlayerId())) {
			return;
		}
		
		// 解散联盟，只有在联盟成员只有盟主一个人的情况下才能解散联盟
		HawkLog.logPrintln("newyear lottery guild dismiss, playerId: {}, guildId: {}", event.getPlayerId(), guildId);
		playerLeaveGuild(event.getPlayerId(), guildId, HawkTime.getMillisecond() - HawkTime.DAY_MILLI_SECONDS);
		
		String dayTime = HawkTime.formatNowTime("yyyyMMdd");
		for (int lotteryType : NewyearLotteryGiftCfg.getLotteryTypes()) {
			String guildType = getMapKey(guildId, lotteryType, dayTime);
			guildPayGiftInfoMap.remove(guildType);
		}
	}
	
	/**
	 * 更新成就数据通知
	 * @param event
	 */
	@Subscribe
	public void onGuildPayGiftAchieveNotify(GuildPayGiftAchieveNotifyEvent event) {
		Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		
		NewyearLotteryEntity entity = opEntity.get();
		refreshAchieveData(entity);
	}
	
	/**
	 * 成员离开联盟
	 * 
	 * @param playerId
	 * @param guildId
	 */
	private void playerLeaveGuild(String playerId, String guildId, long joinGuidTime) {
		NewyearLotteryEntity entity = null;
		boolean crossServer = getDataGeter().isCrossPlayer(playerId);
		if (!crossServer) {
			Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			entity = opEntity.get();
			checkCrossDay(entity);
		}
		
		boolean handle = false;
		String dayTime = HawkTime.formatNowTime("yyyyMMdd");
		for (int lotteryType : NewyearLotteryGiftCfg.getLotteryTypes()) {
			GuildPayGiftInfoObject obj = getPayGiftCountObject(guildId, lotteryType, dayTime);
			if (obj == null) {
				continue;
			}
			handle = true;
			lotteryRewardPersonal(obj, playerId, guildId, entity);
			sendAchieveRewardMail(obj, playerId, joinGuidTime, entity);
		}
		
		if (handle) {
			String key = getAchieveRewardKey(guildId, dayTime);
			ActivityGlobalRedis.getInstance().getRedisSession().hDel(key, playerId);
		}
		
		if (entity != null) {
			syncAcitivtyDataInfo(entity);
		}
	}
	
	/**
	 * 给联盟成员发抽奖奖励
	 * 
	 * @param obj
	 * @param playerId
	 */
	private void lotteryRewardPersonal(GuildPayGiftInfoObject obj, String playerId, String playerGuildId, NewyearLotteryEntity entity) {
		int maxConditionVal = NewyearLotteryAchieveCfg.getMaxConditionVal(obj.getLotteryType());
		// 不满足解锁抽奖奖池的条件
		if (obj.getPayCount() < maxConditionVal) {
			return;
		}
		
		if (!obj.getPayMembers().contains(playerId)) {
			return;
		}
		
		if (obj.getLotteryMembers().contains(playerId)) {
			return;
		}
		
		NewyearLotteryActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(NewyearLotteryActivityKVCfg.class);
		int itemId = kvCfg.getCostItemId(obj.getLotteryType());
		int itemCount = getDataGeter().getItemNum(playerId, itemId);
		if (itemCount <= 0) {
			return;
		}
		
		if (entity != null) {
			PayGiftInfoItem item = entity.getGiftItemByType(obj.getLotteryType());
			if (item.getLotteryAwardId() > 0) {
				return;
			}
		}
		
		NewyearLotterySlotCfg cfg = null;
		synchronized (obj) {
			cfg = sendLotteryRewardMail(obj, playerId, playerGuildId, false);
		}
		
		if (cfg != null) {
			MailId mailId = obj.getLotteryType() == NewyearLotteryGiftType.LOTTERY_GIFT_COMMON_VALUE ? MailId.NEW_YEAR_LOTTERY_COMMON : MailId.NEW_YEAR_LOTTERY_ADVANCE;
			List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemImmutableList(cfg.getRewards());
			Object[] content = new Object[]{cfg.getId()};
			getDataGeter().sendMail(playerId, mailId, null, null, content, rewardList, false);
			if (cfg.getIsBigPrize() > 0) {
				sendBroadcast(obj.getLotteryType(), playerId, obj.getGuildId());
			}
			
			if (entity != null) {
				PayGiftInfoItem item = entity.getGiftItemByType(obj.getLotteryType());
				item.setLotteryAwardId(cfg.getId());
				entity.notifyUpdate();
			} else {
				// 被动退盟情况下的特殊处理
				List<Integer> achieveIdList = memberAchieveListTable.get(obj.getDayTime(), playerId);
				if (achieveIdList == null) {
					achieveIdList = new ArrayList<>();
					memberAchieveListTable.put(obj.getDayTime(), playerId, achieveIdList);
				}
				achieveIdList.add(0 - cfg.getId());
				HawkLog.logPrintln("newyear lottery auto special, playerId: {}, lotteryType: {}, cfgId: {}", playerId, obj.getLotteryType(), cfg.getId());
			}
			
			List<RewardItem.Builder> consumeItems = kvCfg.getLotteryCost(obj.getLotteryType());
			boolean cost = this.getDataGeter().consumeItems(playerId, consumeItems, 0, Action.NEWYEAR_LOTTERY_COST);
			if (!cost) {
				HawkLog.logPrintln("lotteryRewardPersonal cost item failed, playerId: {}, lotteryType: {}", playerId, obj.getLotteryType());
			}
		}
	}
	
	/**
	 * 选取直购礼包的自选奖励
	 * 
	 * @param playerId
	 * @param id
	 */
	public Result<?> selectGiftAward(String playerId, int id) {
		if (isHidden(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		NewyearLotteryGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewyearLotteryGiftCfg.class, id);
		if (cfg == null) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Result.fail(Status.Error.NEWYEAR_LOTTERY_OPER_NEED_GUILD_VALUE);
		}
		
		int lotteryType = cfg.getLotteryType();
		NewyearLotteryEntity entity = opEntity.get();
		PayGiftInfoItem item = entity.getGiftItemByType(lotteryType);
		if (HawkTime.isToday(item.getSelfPayTime())) {
			return Result.fail(Status.Error.NEWYEAR_LOTTERY_GIFT_PAIED_VALUE);
		}
		
		item.setSelectId(id);
		String randomReward = HawkRand.randomWeightObject(cfg.getRandomRewardItems(), cfg.getRandomRewardWeight());
		item.setRandomAward(randomReward);
		
		HawkLog.logPrintln("newyear lottery select reward, guildId: {}, lotteryType: {}, playerId: {}, selectId: {}, randomReward: {}", guildId, lotteryType, playerId, id, randomReward);
		syncAcitivtyDataInfo(entity);
		return Result.success();
	}
	
	/**
	 * 领取成就奖励
	 * 
	 * @param playerId
	 * @param achieveId
	 * @return
	 */
	public Result<?> onTakeAchieveReward(String playerId, int achieveId) {
		if (isHidden(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		NewyearLotteryAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(NewyearLotteryAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Result.fail(Status.Error.NEWYEAR_LOTTERY_OPER_NEED_GUILD_VALUE);
		}
		
		Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		NewyearLotteryEntity entity = opEntity.get();
		NewyearLotteryAchieveItem achieveItem = entity.getAchieveItem(achieveId);
		if (achieveItem == null) {
			HawkLog.logPrintln("newyear lottery take achieve reward failed, guildId: {}, playerId: {}, achieveId: {}, achieveItem null", 
					guildId, playerId, achieveId);
			return Result.fail(Status.SysError.DATA_ERROR_VALUE);
		}
		
		if (achieveItem.getState() != GiftBuyAchieveState.NEWYEAR_LOTTERY_NOT_REWARD_VALUE) {
			HawkLog.logPrintln("newyear lottery take achieve reward failed, guildId: {}, playerId: {}, achieveId: {}, state: {}", 
					guildId, playerId, achieveId, achieveItem.getState());
			return Result.fail(Status.Error.NEWYEAR_LOTTERY_ACHIEVE_STATUS_ERROR_VALUE);
		}
		
		List<Integer> achieveIdList = memberAchieveListTable.get(entity.getDayTime(), playerId);
		if (achieveIdList == null) {
			achieveIdList = new ArrayList<>();
			memberAchieveListTable.put(entity.getDayTime(), playerId, achieveIdList);
		}
		
		if (achieveIdList.contains(achieveId)) {
			HawkLog.logPrintln("newyear lottery take achieve reward failed - repeated, guildId: {}, playerId: {}, achieveId: {}, state: {}", 
					guildId, playerId, achieveId, achieveItem.getState());
			return Result.fail(Status.Error.NEWYEAR_LOTTERY_ACHIEVE_STATUS_ERROR_VALUE);
		} 
		
		achieveItem.setState(GiftBuyAchieveState.NEWYEAR_LOTTERY_TOOK_VALUE);
		entity.notifyUpdate();
		List<RewardItem.Builder> rewardItems = new ArrayList<RewardItem.Builder>();
		rewardItems.addAll(RewardHelper.toRewardItemImmutableList(achieveCfg.getRewards()));
		this.getDataGeter().takeReward(playerId, rewardItems, 1, Action.NEWYEAR_LOTTERY_ACHIEVE, true);
		
		achieveIdList.add(achieveId);
		String achieveIds = SerializeHelper.collectionToString(achieveIdList, ",");
		flushAchieveDataToRedis(playerId, guildId, achieveIds, entity.getDayTime());
		
		HawkLog.logPrintln("newyear lottery take achieve reward, guildId: {}, playerId: {}, achieveId: {}, dayTime: {}, achieveIds: {}", guildId, playerId, achieveId, entity.getDayTime(), achieveIds);
		
		syncAcitivtyDataInfo(entity);
		
		this.getDataGeter().logLotteryTakeAchieveReward(playerId, achieveCfg.getLotteryType(), achieveId);
		return Result.success();
	}
	
	/**
	 * 抽奖
	 * 
	 * @param playerId
	 * @param type
	 */
	public Result<?> lottery(String playerId, int lotteryType) {
		if (isHidden(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		if (!NewyearLotteryGiftCfg.getLotteryTypes().contains(lotteryType)) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		
		// 玩家未加入联盟
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Result.fail(Status.Error.NEWYEAR_LOTTERY_OPER_NEED_GUILD_VALUE);
		}
		
		Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		NewyearLotteryEntity entity = opEntity.get();
		GuildPayGiftInfoObject obj = getPayGiftCountObject(guildId, lotteryType, entity.getDayTime());
		if (obj == null || !HawkTime.formatNowTime("yyyyMMdd").equals(obj.getDayTime())) {
			HawkLog.logPrintln("newyear lottery handle faile, guildId: {}, playerId: {}, lotteryType: {}, entity dayTime: {}, obj dayTime: {}", 
					guildId, playerId, lotteryType, entity.getDayTime(), obj == null ? "0" : obj.getDayTime());
			return Result.fail(Status.Error.NEWYEAR_LOTTERY_CROSS_DAY_VALUE);
		}
		
		int maxConditionVal = NewyearLotteryAchieveCfg.getMaxConditionVal(lotteryType);
		int achieveCount = obj.getPayCount();
		if (achieveCount < maxConditionVal) {
			HawkLog.logPrintln("newyear lottery handle faile, guildId: {}, playerId: {}, lotteryType: {}, achieveCount: {}", 
					guildId, playerId, lotteryType, achieveCount);
			return Result.fail(Status.Error.NEWYEAR_LOTTERY_NOT_OPEN_VALUE);
		}
		
		NewyearLotteryActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(NewyearLotteryActivityKVCfg.class);
		List<RewardItem.Builder> consumeItems = kvCfg.getLotteryCost(lotteryType);
		boolean cost = this.getDataGeter().consumeItems(playerId, consumeItems, HP.code2.NEWYEAR_LOTTERY_REQ_VALUE, Action.NEWYEAR_LOTTERY_COST);
		if (!cost) {
			return Result.fail(Status.Error.NEWYEAR_LOTTERY_COST_ERROR_VALUE);
		}
		
		List<NewyearLotterySlotCfg> cfgList = NewyearLotterySlotCfg.getCfgList(lotteryType);
		List<Integer> weightList = NewyearLotterySlotCfg.getCfgWeightList(lotteryType);
		NewyearLotterySlotCfg cfg = HawkRand.randomWeightObject(cfgList, weightList);
		synchronized (obj) {
			obj.setLotteryCount(obj.getLotteryCount() + 1);
			obj.getLotteryMembers().add(playerId);
			if (obj.getLotteryLuckyNum() == 0 && cfg.getIsBigPrize() == 0 && obj.getLotteryCount() >= obj.getLotteryLuckyMax()) {
				cfg = cfgList.stream().filter(e -> e.getIsBigPrize() > 0).findAny().get();
			}
			if (cfg.getIsBigPrize() > 0 && obj.getLotteryLuckyNum() == 0) {
				obj.setLotteryLuckyNum(obj.getLotteryCount());
			}
			
			flushDataToRedis(obj);
		}
		
		addLotteryMemberToRedis(guildId, lotteryType, obj.getDayTime(), playerId);
		
		HawkLog.logPrintln("newyear lottery handle, guildId: {}, playerId: {}, lotteryType: {}, reward: {}", guildId, playerId, lotteryType, cfg.getRewards());
		
		List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemImmutableList(cfg.getRewards());
		this.getDataGeter().takeReward(playerId, rewardItems, 1, Action.NEWYEAR_LOTTERY_REWARD, true, RewardOrginType.NEWYEAR_LOTTERY_REWARD);
		PayGiftInfoItem item = entity.getGiftItemByType(lotteryType);
		item.setLotteryAwardId(cfg.getId());
		entity.notifyUpdate();
		
		syncAcitivtyDataInfo(entity);
		if (cfg.getIsBigPrize() > 0) {
			sendBroadcast(lotteryType, playerId, guildId);
		}
		
		this.getDataGeter().logLotteryInfo(playerId, lotteryType, cfg.getRewards());
		
		return Result.success();
	}
	
	private void sendBroadcast(int lotteryType, String playerId, String guildId) {
		String guildName = getDataGeter().getGuildName(guildId);
		String playerName = getDataGeter().getPlayerName(playerId);
		sendBroadcast(Const.NoticeCfgId.NEWYEAR_LOTTERY_BIG_AWARD, null, guildName, playerName, lotteryType);
	}
	
	/**
	 * 礼包购买检测
	 * 
	 * @param playerId
	 * @param giftId
	 * @return
	 */
	public int payGiftCheck(String playerId, String giftId) {
		if (isHidden(playerId)) {
			return Status.SysError.ACTIVITY_CLOSED_VALUE;
		}
		
		NewyearLotteryActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(NewyearLotteryActivityKVCfg.class);
		int lotteryType = cfg.getLotteryTypeByGiftId(giftId);
		// 直购ID跟活动礼包类型对不上
		if (lotteryType == 0) {
			HawkLog.errPrintln("newyear lottery gift not match, pay check failed, playerId: {}, giftId: {}", playerId, giftId);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		// 玩家未加入联盟
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.NEWYEAR_LOTTERY_GIFT_NEED_GUILD_VALUE;
		}
		
		Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.SysError.ACTIVITY_CLOSED_VALUE;
		}
		NewyearLotteryEntity entity = opEntity.get();
		PayGiftInfoItem item = entity.getGiftItemByType(lotteryType);
		
		// 当天已经购买过该种礼包了
		if (HawkTime.isToday(item.getSelfPayTime())) {
			return Status.Error.PAY_GIFT_BUY_FULL_TODAY_VALUE;
		}
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@Subscribe
	public void onGiftBuyEvent(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		String giftId = event.getGiftId();
		if (isHidden(playerId)) {
			HawkLog.errPrintln("newyear lottery activity hidden, payGift event handle failed, playerId: {}, giftId: {}", playerId, giftId);
			return;
		}
		
		NewyearLotteryActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(NewyearLotteryActivityKVCfg.class);
		int lotteryType = cfg.getLotteryTypeByGiftId(giftId);
		if (lotteryType == 0) {
			HawkLog.errPrintln("newyear lottery gift not match, payGift event handle failed, playerId: {}, giftId: {}", playerId, giftId);
			return;
		}
		
		Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("newyear lottery gift pay failed end, playerId: {}, giftId: {}", playerId, giftId);
			return;
		}
		NewyearLotteryEntity entity = opEntity.get();
		checkCrossDay(entity);
		PayGiftInfoItem item = entity.getGiftItemByType(lotteryType);
		long now = HawkTime.getMillisecond();
		item.setSelfPayTime(now);
		entity.notifyUpdate();
		try {
			//发奖
			List<RewardItem.Builder> rewardItems = new ArrayList<RewardItem.Builder>();
			rewardItems.addAll(item.getSelectAwardList());
			rewardItems.addAll(item.getRandomAwardList());
			rewardItems.addAll(item.getRegularRewardList());
			this.getDataGeter().takeReward(playerId, rewardItems, 1, Action.NEWYEAR_LOTTERY_GIFT_REWARD, true);
			MailId mailId = lotteryType == NewyearLotteryGiftType.LOTTERY_GIFT_COMMON_VALUE ? MailId.NEW_YEAR_LOTTERY_GIFT_COMMON : MailId.NEW_YEAR_LOTTERY_GIFT_ADVANCE;
			getDataGeter().sendMail(playerId, mailId, null, null, null, rewardItems, true);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		String guildId = getDataGeter().getGuildId(playerId);
		String key = getMapKey(guildId, lotteryType, entity.getDayTime());
		GuildPayGiftInfoObject obj = guildPayGiftInfoMap.putIfAbsent(key, GuildPayGiftInfoObject.valueOf(guildId, lotteryType, entity.getDayTime()));
		if (obj == null) {
			obj = guildPayGiftInfoMap.get(key);
		}
		
		// 可能出现同一个盟多个玩家同时购买礼包到此，因此需要同步执行此段逻辑
		synchronized (obj) {
			updatePayGiftCountInfo(playerId, obj, lotteryType, now);
		}
		
		addPayMemberToRedis(guildId, lotteryType, obj.getDayTime(), playerId);
		refreshAchieveData(entity);
		HawkLog.logPrintln("newyear lottery gift pay success end, playerId: {}, guildId: {}, giftId: {}, lotteryType: {}", playerId, guildId, giftId, lotteryType);
		syncAcitivtyDataInfo(entity);
		this.getDataGeter().logLotteryGiftPay(playerId, lotteryType, item.getSelectId(), item.getRandomAward());
	}
	
	/**
	 * 更新礼包购买人数信息
	 * 
	 * @param obj
	 * @param lotteryType
	 * @param time
	 */
	private void updatePayGiftCountInfo(String playerId, GuildPayGiftInfoObject obj, int lotteryType, long time) {
		try {
			int newCount = obj.getPayCount() + 1;
			obj.setPayCount(newCount);
			Set<String> payMemberSet = obj.getPayMembers();
			payMemberSet.add(playerId);

			NewyearLotteryAchieveCfg achieveCfg = NewyearLotteryAchieveCfg.getCfg(lotteryType, newCount);
			if (achieveCfg != null) {
				HawkLog.logPrintln("newyear lottery guild achieve touch, playerId: {}, guildId: {}, lotteryType: {}, achieveId: {}, value: {}", playerId, obj.getGuildId(), lotteryType, achieveCfg.getId(), achieveCfg.getValue());
				obj.getAchieveTimeMap().put(newCount, time);
				sendNotify(obj, playerId, newCount);
			}
			
			flushDataToRedis(obj);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 发联盟通知
	 * @param guildId
	 * @param lotteryType
	 * @param newCount
	 */
	private void sendNotify(GuildPayGiftInfoObject obj, String playerId, int newCount) {
		Const.NoticeCfgId noticeId = Const.NoticeCfgId.NEWYEAR_LOTTERY_GUILD_ACHIEVE_COMMON;
		try {
			int maxConditionVal = NewyearLotteryAchieveCfg.getMaxConditionVal(obj.getLotteryType());
			if (newCount == maxConditionVal) {
				NewyearLotteryActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(NewyearLotteryActivityKVCfg.class);
				obj.setLotteryLuckyMax(cfg.randomSlotGuaranteeNum());
				noticeId = obj.getLotteryType() == NewyearLotteryGiftType.LOTTERY_GIFT_COMMON_VALUE ? Const.NoticeCfgId.NEWYEAR_LOTTERY_COMMON_ENABLE : Const.NoticeCfgId.NEWYEAR_LOTTERY_ADVANCE_ENABLE;
			} else {
				noticeId = obj.getLotteryType() == NewyearLotteryGiftType.LOTTERY_GIFT_COMMON_VALUE ? Const.NoticeCfgId.NEWYEAR_LOTTERY_GUILD_ACHIEVE_COMMON : Const.NoticeCfgId.NEWYEAR_LOTTERY_GUILD_ACHIEVE_ADVANCE;
			}
			this.getDataGeter().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, obj.getGuildId(), noticeId, playerId, newCount);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 同步活动数据
	 * @param playerId
	 */
	public Result<?> syncActivityInfo(String playerId) {
		if (isHidden(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		Optional<NewyearLotteryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		NewyearLotteryEntity entity = opEntity.get();
		boolean crossDay = checkCrossDay(entity);
		if (!crossDay) {
			refreshAchieveData(entity);
		}
		
		syncAcitivtyDataInfo(entity);
		return Result.success();
	}
	
	/**
	 * 同步活动数据
	 * 
	 * @param entity
	 */
	private void syncAcitivtyDataInfo(NewyearLotteryEntity entity) {
		String playerId = entity.getPlayerId();
		String guildId = getDataGeter().getGuildId(playerId);
		NewyearLotteryInfoPB.Builder builder = NewyearLotteryInfoPB.newBuilder();
		if (!HawkOSOperator.isEmptyString(guildId)) {
			builder.setGuildId(guildId);
		}
		
		NewyearLotteryActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(NewyearLotteryActivityKVCfg.class);
		boolean update = false;
		for (PayGiftInfoItem item : entity.getGiftItemList()) {
			GuildPayGiftInfoObject obj = getPayGiftCountObject(guildId, item.getLotteryType(), entity.getDayTime());
			int oldCount = item.getPayCount();  // 上一次同步的进度值
			int newPayCount = obj == null ? 0 : obj.getPayCount();  // 最新的进度值
			item.setPayCount(newPayCount);
			if (oldCount != newPayCount) {
				update = true;
			}
			
			int itemId = kvCfg.getCostItemId(item.getLotteryType());
			int itemCount = getDataGeter().getItemNum(playerId, itemId);
			NewyearLotteryGiftInfoPB.Builder giftItem = item.toBuilder(entity.getItemList(item.getLotteryType()), oldCount, newPayCount, itemCount);
			builder.addGift(giftItem);
		}
		
		if (update) {
			entity.notifyUpdate();
		}
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.NEWYEAR_LOTTERY_ACTIVITY_INFO_SYNC_VALUE, builder));
	}
	
	/**
	 * 将 GuildPayGiftInfoObject 数据更新到redis
	 */
	private void flushDataToRedis(GuildPayGiftInfoObject obj) {
		String key = getGuildPayGiftInfoRedisKey();
		String subKey = getMapKey(obj.getGuildId(), obj.getLotteryType(), obj.getDayTime());
		ActivityGlobalRedis.getInstance().getRedisSession().hSet(key, subKey, obj.toJSONString());
	}
	
	/**
	 * 添加已付费的联盟成员数据 
	 */
	private void addPayMemberToRedis(String guildId, int lotteryType, String dayTime, String memberId) {
		String key = getPayMemberRedisKey(guildId, lotteryType, dayTime);
		ActivityGlobalRedis.getInstance().getRedisSession().lPush(key, REDIS_EXPIRE_SECOND * 2, memberId);
	}
	
	/**
	 * 获取已付费的联盟成员数据 
	 */
	private List<String> getPayMemberListFromRedis(String guildId, int lotteryType, String dayTime) {
		String key = getPayMemberRedisKey(guildId, lotteryType, dayTime);
		return ActivityGlobalRedis.getInstance().getRedisSession().lRange(key, 0, -1, 0);
	}
	
	/**
	 * 添加已抽奖的联盟成员数据
	 */
	private void addLotteryMemberToRedis(String guildId, int lotteryType, String dayTime, String memberId) {
		String key = getLotteryMemberRedisKey(guildId, lotteryType, dayTime);
		ActivityGlobalRedis.getInstance().getRedisSession().lPush(key, REDIS_EXPIRE_SECOND * 2, memberId);
	}
	
	/**
	 * 获取已抽奖的联盟成员数据
	 */
	private List<String> getLotteryMemberListFromRedis(String guildId, int lotteryType, String dayTime) {
		String key = getLotteryMemberRedisKey(guildId, lotteryType, dayTime);
		return ActivityGlobalRedis.getInstance().getRedisSession().lRange(key, 0, -1, 0);
	}
	
	/**
	 * 添加玩家领取成就奖励的成就配置ID数据 
	 */
	private void flushAchieveDataToRedis(String memberId, String guildId, String achieveIds, String dayTime) {
		String key = getAchieveRewardKey(guildId, dayTime);
		ActivityGlobalRedis.getInstance().getRedisSession().hSet(key, memberId, achieveIds, REDIS_EXPIRE_SECOND);
	}
	
	/**
	 * 获取玩家领取成就奖励的成就配置ID数据 
	 */
	private Map<String, String> getMemberAchieveIdsFromRedis(String guildId, String dayTime) {
		String key = getAchieveRewardKey(guildId, dayTime);
		return ActivityGlobalRedis.getInstance().getRedisSession().hGetAll(key);
	}
	
	/**
	 * 内存数据的map键
	 * @return
	 */
	private String getMapKey(String guildId, int lotteryType, String dayTime) {
		return guildId + ":" + lotteryType + ":" + dayTime;
	}
	
	private String getGuildPayGiftInfoRedisKey() {
		return guildPayGiftInfoKey + ":" + getDataGeter().getServerId();  // map存储
	}
	
	private String getGuildPayGiftInfoRedisKey(String serverId) {
		return guildPayGiftInfoKey + ":" + serverId;  // map存储
	}
	
	private String getPayMemberRedisKey(String guidId, int lotteryType, String dayTime) {
		return guildPayGiftMemberKey + ":" + guidId + ":" + lotteryType + ":" + dayTime;  // list存储
	}
	
	private String getLotteryMemberRedisKey(String guidId, int lotteryType, String dayTime) {
		return guildLotteryMemberKey + ":" + guidId + ":" + lotteryType + ":" + dayTime;  // list存储
	}
	
	private String getAchieveRewardKey(String guildId, String dayTime) {
		return personalAchieveRewardKey + ":" + guildId + ":" + dayTime; // list存储取的时候要访问redis次数太多，这里还是用map存储
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}
	
}
