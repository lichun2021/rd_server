package com.hawk.activity;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.hawk.activity.type.impl.urlReward.IURLReward;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppCfg;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.profiler.HawkProfilerAnalyzer;
import org.hawk.redis.HawkRedisSession;
import org.hawk.task.HawkTaskManager;
import org.hawk.util.HawkClassScaner;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.configupdate.ActivityConfigUpdateManager;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.entity.ActivityPlayerEntity;
import com.hawk.activity.entity.IActivityEntity;
import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.ActivityEventSerialize;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.msg.AsyncActivityEventMsg;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.timeController.ITimeController;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskContext;
import com.hawk.activity.type.impl.order.task.OrderTaskContext;
import com.hawk.activity.type.impl.rank.ActivityRankContext;
import com.hawk.activity.type.impl.recallFriend.task.GuildAchieveContext;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskContext;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetContext;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ActivityInfoSync;
import com.hawk.game.protocol.Activity.ActivityPB.Builder;
import com.hawk.game.protocol.HP;

/**
 * 活动服务管理
 * 
 * @author PhilChen
 *
 */
public class ActivityManager extends HawkAppObj {
	/**
	 * 日志
	 */
	static final Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * <pre>
	 * 事件中心，对活动事件进行分发
	 * 如需使用事件，请在活动实现类型添加事件处理方法并标注注解：@EventHandler，方法参数为事件类名称
	 * </pre>
	 */
	private ActivityEventBusPool eventBus;

	/**
	 * 活动概要信息
	 */
	protected Map<Integer, ActivityEntity> activityInfos;
	/**
	 * 活动对象<activityId, Activity>
	 */
	protected Map<Integer, ActivityBase> activityMap;
	/**
	 * 游戏数据获取器
	 */
	protected ActivityDataProxy dataGeter;
	/**
	 * 调度器
	 */
	ScheduledExecutorService scheduled;
	
	/***
	 * 玩家注册时间map
	 */
	private Map<String, Long> playerRegistTimeMap = new HashMap<>();

	/**
	 * 活动联盟作用号
	 */
	private Map<Integer, Map<String, Map<Integer, Integer>>> guildEffMap = new HashMap<>();
	
	/**
	 * 活动事件统计
	 */
	private Map<String, AtomicLong> activityEventTimesMap = new ConcurrentHashMap<>();
	private Map<String, Long> activityEventStatMap = new HashMap<>();
	
	/**
	 * 单例对象
	 */
	private static ActivityManager instance;

	/**
	 * 获取全服单例对象
	 * 
	 * @return
	 */
	public static ActivityManager getInstance() {
		return instance;
	}

	/**
	 * 默认构造
	 * 
	 * @param xid
	 * @param serverId
	 * @param redisSession
	 */
	public ActivityManager(HawkXID xid, HawkRedisSession redisSession, HawkRedisSession globalRedisSession, ActivityDataProxy dataGeter) {
		super(xid);
		instance = this;
		this.dataGeter = dataGeter;
		PlayerDataHelper.getInstance().setDataGeter(dataGeter);
		ActivityRedisKey.init();
		ActivityLocalRedis.getInstance().init(redisSession);
		ActivityGlobalRedis.getInstance().init(redisSession);
	}

	public boolean init(int activityPeriod) {
		new ActivityHandler();
		eventBus = ActivityEventBusPool.create();
		AchieveContext.initParser();
		StrongestTargetContext.initParser();
		OrderTaskContext.initParser();
		EvolutionTaskContext.initParser();
		GuildAchieveContext.initParser();
		SpaceMechaTaskContext.initParser();

		// 活动配置更新
		ActivityConfigUpdateManager.getInstance().init();
		// 工厂初始化
		GameActivityFactory.getInstance().init();

		activityMap = new ConcurrentHashMap<>();
		// 加载活动概要信息
		activityInfos = new ConcurrentHashMap<>();

		// 初始化加载所有活动对象
		List<ActivityEntity> entityList = HawkDBManager.getInstance().query("from ActivityEntity where invalid = 0");
		
		Map<Integer, ActivityEntity> entityMap = new HashMap<>();
		for (ActivityEntity entity : entityList) {
			int activityId = entity.getActivityId();
			if (!entityMap.containsKey(activityId)) {
				entityMap.put(activityId, entity);
				continue;
			}
			// 如果有相同entity,取创建时间早的那个
			ActivityEntity beforeEntity = entityMap.get(activityId);
			if (beforeEntity.getCreateTime() > entity.getCreateTime()) {
				entityMap.put(activityId, entity);
				
				// 把错误数据删除
				beforeEntity.delete();
			} else {
				// 把错误数据删除
				entity.delete();
			}
		}
		
		for (ActivityEntity activityEntity : entityMap.values()) {
			activityInfos.put(activityEntity.getActivityId(), activityEntity);
			ActivityCfg config = HawkConfigManager.getInstance().getConfigByKey(ActivityCfg.class,
					activityEntity.getActivityId());
			if (config == null) {
				logger.error("activity config not found! activityId: {}", activityEntity.getActivityId());
				continue;
			}
			if (isActivityInvalid(config)){
				continue;
			}
			// 已到期关闭的活动仍然需要创建活动逻辑对象，为了某些活动逻辑在关闭后仍然需要在玩家登录时进行一些检测处理
			ActivityBase activity = GameActivityFactory.getInstance().buildActivity(config, activityEntity);
			eventBus.register(activity);
			activity.setDataGeter(dataGeter);
			activityMap.put(activityEntity.getActivityId(), activity);
			if (isActivityInvalid(config)) {
				continue;
			}
		}
		// 初始化成就事件监听
		eventBus.register(AchieveManager.getInstance());
		ActivityRankContext.init();

		// 检查新增活动
		checkNewActivity(false);
		
		// 添加调度器
		scheduled = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				thread.setName("ActivityUpdate");
				thread.setDaemon(true);
				return thread;
			
		}});
		
		scheduled.scheduleAtFixedRate(() -> {
			try {
				boolean isGsInit = getDataGeter().isGsInitFinish();
				if (isGsInit) {
					// 检查新增活动
					checkNewActivity(true);

					// 更新活动状态
					updateActivityState();
				}
			} catch (Exception e) {
				HawkException.catchException(e, "ThreadName:" + Thread.currentThread().getName() + ": scheduled update error!");
			}
		}, activityPeriod, activityPeriod, TimeUnit.MILLISECONDS);
		
		// 更新活动排行榜
		scheduled.scheduleAtFixedRate(()-> {
			try {
				boolean isGsInit = getDataGeter().isGsInitFinish();
				if (isGsInit) {
					ActivityRankContext.updateRankList();
				}
			} catch (Exception e) {
				HawkException.catchException(e, "ThreadName:" + Thread.currentThread().getName() + ": scheduled rankList error!");
			}
		}, ActivityConst.ACTIVITY_RANK_UPDATE_PERIOD, ActivityConst.ACTIVITY_RANK_UPDATE_PERIOD, TimeUnit.MILLISECONDS);
		
		// 快速tick 200ms
		scheduled.scheduleAtFixedRate(()-> {
			try {
				boolean isGsInit = getDataGeter().isGsInitFinish();
				if (isGsInit) {
					quickTick();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}, ActivityConst.QUICK_TICK_PEROID, ActivityConst.QUICK_TICK_PEROID, TimeUnit.MILLISECONDS);
		
		//Subscribe 注解检查
		if (!eventBusCheck()) {
			return false;
		}
		if (!activityEventCheck()) {
			return false;
		}
		
		logger.info("activity service init complete.");
		return true;
	}
	
	/** 活动事件必须有默认构造函数*/
	private boolean activityEventCheck() {
		List<Class<?>> clazzList = HawkClassScaner.getAllClasses("com.hawk.activity.event.impl");
		for (Class<?> cls : clazzList) {
			if (cls.getSuperclass() != ActivityEvent.class) {
				continue;
			}
			try {
				cls.newInstance();
			} catch (Exception e) {
				HawkException.catchException(e);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * eventBus接收者检测 , Subscribe标记的方法须为public
	 * @return
	 */
	private boolean eventBusCheck() {
		String packageName = this.getClass().getPackage().getName();
		ClassPath classPath;
		try {
			classPath = ClassPath.from(this.getClass().getClassLoader());
			ImmutableSet<ClassInfo> set = classPath.getTopLevelClassesRecursive(packageName);
			for (ClassInfo info : set) {
				Class<?> cls = info.load();
				Method[] methods = cls.getDeclaredMethods();
				for (Method method : methods) {
					if (method.isAnnotationPresent(Subscribe.class) && !Modifier.isPublic(method.getModifiers())) {
						HawkLog.errPrintln("activity eventBus check failed, Class: {} , method: {}", cls.getName(), method.getName());
						return false;
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 获取数据获取接口
	 * 
	 * @return
	 */
	public ActivityDataProxy getDataGeter() {
		return dataGeter;
	}

	/**
	 * 获取活动逻辑对象
	 * 
	 * @param activityId
	 * @return
	 */
	public Optional<ActivityBase> getActivity(int activityId) {
		ActivityBase gameActivity = activityMap.get(activityId);
		return Optional.ofNullable(gameActivity);
	}


	/**
	 * 检查新增活动配置
	 */
	private void checkNewActivity(boolean isPush) {
		ConfigIterator<ActivityCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ActivityCfg.class);
		while (configIterator.hasNext()) {
			ActivityCfg activityCfg = configIterator.next();
			if (isActivityInvalid(activityCfg)) {
				continue;
			}
			
			ActivityEntity activityEntity = null;
			if (activityInfos.containsKey(activityCfg.getActivityId())) {
				activityEntity = activityInfos.get(activityCfg.getActivityId());
			} else {
				activityEntity = new ActivityEntity();
				activityEntity.setActivityId(activityCfg.getActivityId());
				activityEntity.setTermId(0);
				activityEntity.setState(ActivityState.HIDDEN.intValue());
				HawkDBManager.getInstance().create(activityEntity);
				activityInfos.put(activityEntity.getActivityId(), activityEntity);
			}
			
			// 新增活动
			if (!activityMap.containsKey(activityEntity.getActivityId())) {
				addNewActivity(activityCfg, activityEntity, isPush);
			}
		}
	}

	/**
	 * 增加新的活动
	 * 
	 * @param activityCfg
	 */
	private void addNewActivity(ActivityCfg activityCfg, ActivityEntity activityEntity, boolean isPush) {
		// 创建新的活动逻辑对象
		ActivityBase activity = GameActivityFactory.getInstance().buildActivity(activityCfg, activityEntity);
		activity.setDataGeter(dataGeter);
		eventBus.register(activity);
		activityMap.put(activity.getActivityId(), activity);

		if (isPush) {
			// 新增活动推送给在线玩家
			PlayerPushHelper.getInstance().pushActivityState(activity);
			PlayerPushHelper.getInstance().pushActivityDataInfo(activity);
			// 如果该活动实现了成就接口,则需要进行成就数据推送
			if(activityEntity.getClass().isAssignableFrom(AchieveProvider.class)){
				PlayerPushHelper.getInstance().pushAchieveItemInfo((AchieveProvider) activity);
			}
		}
		
		logger.info("add new activity. activityId: {}, activityType: {}", activityCfg.getActivityId(),
				activityCfg.getType());
	}

	/**
	 * 更新活动状态
	 */
	protected boolean updateActivityState() {
		boolean isSuccess = true;
		List<Integer> invalidList = new ArrayList<>();
		for (ActivityBase activity : activityMap.values()) {
			try {
				ActivityCfg config = HawkConfigManager.getInstance().getConfigByKey(ActivityCfg.class,
						activity.getActivityId());
				if (config == null) {
					logger.error("activity config not exist, activityId: {}", activity.getActivityId());
					continue;
				}
				if (isActivityInvalid(config)) {
					PlayerPushHelper.getInstance().pushActivityHidden(activity);
					invalidList.add(config.getActivityId());
					continue;
				}
				ActivityEntity activityEntity = activityInfos.get(activity.getActivityId());
				if (activityEntity == null) {
					logger.error("activity entity not exist, activityId: {}", activity.getActivityId());
					continue;
				}
				ITimeController timeController = activity.getTimeControl();
				timeController.updateState(activity);
				ActivityState currentState = activityEntity.getActivityState();
				boolean isPlayerActivityTimeController = timeController.isPlayerActivityTimeController();
				if (currentState == ActivityState.SHOW || currentState == ActivityState.OPEN
						|| currentState == ActivityState.END || isPlayerActivityTimeController) {
					try {
						activity.onTick();
					} catch (Exception e) {
						HawkException.catchException(e, "activity onTick error, activityId: " + activity.getActivityId());
					}
				}

			} catch (Exception e) {
				HawkException.catchException(e, "activity update error! activityId={}", activity.getActivityId());
				isSuccess = false;
			}
		}
		for(int activityId : invalidList){
			ActivityBase activity = activityMap.remove(activityId);
			eventBus.unregister(activity);
		}
		return isSuccess;
	}

	/**
	 * 更新活动状态
	 */
	protected void quickTick() {
		for (ActivityBase activity : activityMap.values()) {
			try {
				ActivityCfg config = HawkConfigManager.getInstance().getConfigByKey(ActivityCfg.class, activity.getActivityId());
				if (isActivityInvalid(config)) {
					continue;
				}
				
				ActivityEntity activityEntity = activityInfos.get(activity.getActivityId());
				if (activityEntity == null || activityEntity.isInvalid()) {
					continue;
				}
				
				activity.onQuickTick();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 向玩家同步所有活动信息
	 * 
	 * @param player
	 */
	public void syncAllActivityInfo(String playerId) {
		ActivityInfoSync.Builder syncBuilder = Activity.ActivityInfoSync.newBuilder();
		List<ActivityBase> activityList = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for (ActivityEntity activityEntity : activityInfos.values()) {
			if (activityEntity.isInvalid()) {
				continue;
			}
			Optional<ActivityBase> opActivity = getActivity(activityEntity.getActivityId());
			if (!opActivity.isPresent()) {
				continue;
			}
			ActivityBase activity = opActivity.get();
			if (activity.isHidden(playerId)) {
				continue;
			}
			activityList.add(activity);
			IActivityEntity iActivityEntity = activity.getIActivityEntity(playerId);
			Builder builder = PlayerPushHelper.getInstance().buildActivityPB(playerId, activity, iActivityEntity);
			syncBuilder.addActivityInfos(builder);
			sb.append(builder.getActivityId()).append("_").append(builder.getStage()).append("_").append(builder.getState()).append(",");
		}
		// 推送活动状态信息
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PUSH_ALL_ACTIVITY_INFO_VALUE, syncBuilder));
		// 打印玩家活动状态信息
		HawkLog.logPrintln("syncAllActivityInfo, playerId:{}, activitys:{}", playerId, sb.toString());
		// 同步各个活动的数据
		for (ActivityBase activity : activityList) {
			activity.syncActivityDataInfo(playerId);
		}
		Map<String, String> urlRewardMap = ActivityGlobalRedis.getInstance().hgetAll(IURLReward.URL_REWARD + ":" + playerId);
		long now = HawkTime.getMillisecond();
		Activity.URLRewardSync.Builder builder = Activity.URLRewardSync.newBuilder();
		for (ActivityBase activity : activityList) {
			if(activity instanceof IURLReward && !HawkOSOperator.isEmptyString(((IURLReward<?>) activity).getURLRewardCfg())){
				String urlReward = urlRewardMap.getOrDefault(String.valueOf(activity.getActivityId()), "0");
				builder.addItems(Activity.URLRewardItem.newBuilder()
						.setActivityId(activity.getActivityId())
						.setCanGet(!HawkTime.isSameDay(now, Long.parseLong(urlReward))));
			}

		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.URL_REWARD_SYNC, builder));
	}
	
	/**
	 * 获取需要显示公告的活动id
	 * @param playerId
	 * @return
	 */
	public List<Integer> getBillBoardActivitys(String playerId) {
		List<Integer> activityIds = new ArrayList<>();
		List<ActivityBase> list = new ArrayList<>();
		for (ActivityEntity activityEntity : activityInfos.values()) {
			if (activityEntity.isInvalid()) {
				continue;
			}
			Optional<ActivityBase> opActivity = getActivity(activityEntity.getActivityId());
			if (!opActivity.isPresent()) {
				continue;
			}
			ActivityBase activity = opActivity.get();
			if (activity.isHidden(playerId)) {
				continue;
			}
			ActivityCfg cfg = activity.getActivityCfg();
			if (cfg.hasBillBoard()) {
				list.add(activity);
			}
		}
		activityIds = list.stream().sorted(new Comparator<ActivityBase>() {
			@Override
			public int compare(ActivityBase arg0, ActivityBase arg1) {
				int termId0 = arg0.getActivityTermId(playerId);
				long showTime0 = arg0.getTimeControl().getShowTimeByTermId(termId0, playerId);
				int termId1 = arg1.getActivityTermId(playerId);
				long showTime1 = arg1.getTimeControl().getShowTimeByTermId(termId1, playerId);
				if (showTime0 != showTime1) {
					return (int) (showTime0 - showTime1);
				} else {
					return arg0.getActivityId() - arg1.getActivityId();
				}
			}
		}).map(e -> e.getActivityId()).collect(Collectors.toList());
		return activityIds;
	}

	/**
	 * 准备新玩家活动数据
	 * 
	 * @param playerId
	 * @return
	 */
	public List<HawkDBEntity> prepareNewPlayerActivityEntity(String playerId) {
		List<HawkDBEntity> dbEntities = new LinkedList<HawkDBEntity>();
		
		for (ActivityEntity activityEntity : activityInfos.values()) {
			if (activityEntity.isInvalid()) {
				continue;
			}
			
			Optional<ActivityBase> opActivity = getActivity(activityEntity.getActivityId());
			if (!opActivity.isPresent()) {
				continue;
			}
			
			int termId = 0;
			ActivityBase activity = opActivity.get();
			ActivityType activityType = activity.getActivityType();
			
			// 玩家独立开启的活动
			if (activity.getTimeControl().isPlayerActivityTimeController()){
				ActivityPlayerEntity entity = activity.preparePlayerActivityEntity(playerId);
				if (entity != null) {
					dbEntities.add(entity);
					termId = activity.getTimeControl().getActivityTermId(HawkTime.getMillisecond(), playerId);
				}
			} else {
				// 非独立开启活动,读取公共活动期数
				termId = activity.getActivityTermId(playerId);
			}
			
			// 活动未开启, 不进行活动data创建
			if(termId == 0){
				continue;
			}
			
			// 缓存获取
			HawkDBEntity dataEntity = PlayerDataHelper.getInstance().getActivityDataEntity(playerId, activityType);
			if(dataEntity != null) {
				IActivityDataEntity iDataEntity = (IActivityDataEntity) dataEntity;
				if(iDataEntity.getTermId() == termId){
					continue;
				}
			}
			
			// 创建新对象
			dataEntity = activity.createDataEntity(playerId, termId);
			if (dataEntity != null) {
				// 避免出错
				if (dataEntity.getUpdateTime() > 0) {
					throw new RuntimeException("activity data entity create state error");
				}
				
				dbEntities.add(dataEntity);
				PlayerDataHelper.getInstance().putActivityDataEntity(playerId, activityType, dataEntity);
			}
		}
		
		return dbEntities;
	}
	
	/**
	 * 玩家登陆之后通知活动模块
	 * 
	 * @param activityData
	 * 
	 * @param player
	 */
	public boolean onPlayerLogin(String playerId) {
		// 向客户端同步活动配置文件校验码
		ActivityConfigUpdateManager.getInstance().pushCheckCodeToPlayer(playerId);

		AchieveManager.getInstance().onPlayerLogin(playerId);

		long startTime = HawkTime.getMillisecond();
		// 每个活动各自的玩家登录事件
		for (Entry<Integer, ActivityBase> entry : activityMap.entrySet()) {
			ActivityBase activity = entry.getValue();
			try {
				activity.refreshDataCache(playerId);
				activity.onPlayerLogin(playerId);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			long now = HawkTime.getMillisecond();
			long costtime = now - startTime;
			if (costtime > 50) {
				HawkLog.logPrintln("player activity onlogin, playerId: {}, activityId: {}, costtime: {}", playerId, activity.getActivityId(), costtime);
				HawkProfilerAnalyzer.getInstance().addMsgHandleInfo("activityLogin-" + activity.getActivityId(), costtime);
			}
			startTime = now;
		}

		// 同步活动通用数据
		syncAllActivityInfo(playerId);
		
		// 玩家首次登录,重发今日发生的特定活动事件
		if (this.getDataGeter().isDailyFirstLogin(playerId)) {
			String key = ActivityRedisKey.EVENT_RECORD + ":" + playerId;
			List<byte[]> result = ActivityLocalRedis.getInstance().lall(key.getBytes());
			if (result != null && !result.isEmpty()) {
				ActivityLocalRedis.getInstance().del(key.getBytes());
				for (byte[] eventBytes : result) {
					try {
						ActivityEvent event = ActivityEventSerialize.deSerialize(eventBytes);
						// 只处理今天发生的的事件
						if (event != null && event.getEventTime() >= HawkTime.getAM0Date().getTime()) {
							HawkLog.logPrintln("resent OfflineResentEvent, playerId:{}, eventName:{}", playerId, event.getClass().getName());
							this.postEvent(event);
						}
					} catch (Exception e) {
						HawkException.catchException(e);
					}
				}
			}
		}
		return true;
	}

	/**
	 * 玩家登出之后通知活动模块
	 * 
	 * @param player
	 */
	public boolean onPlayerLogout(String playerId) {
		for (Entry<Integer, ActivityBase> entry : activityMap.entrySet()) {
			ActivityBase activity = entry.getValue();
			if (activity.isInvalid()) {
				continue;
			}
			try {
				activity.onPlayerLogout(playerId);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return true;
	}

	/**
	 * 玩家tick
	 * @param playerId
	 * @return
	 */
	public boolean onPlayerTick(String playerId) {
		for (Entry<Integer, ActivityBase> entry : activityMap.entrySet()) {
			ActivityBase activity = entry.getValue();
			if (activity.isInvalid()) {
				continue;
			}
			try {
				activity.onPlayerTick(playerId);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return true;
	}

	/**
	 * 获取活动对象
	 * 
	 * @param activityType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends ActivityBase> Optional<T> getGameActivityByType(int activityType) {
		for (Entry<Integer, ActivityBase> entry : activityMap.entrySet()) {
			ActivityCfg config = HawkConfigManager.getInstance().getConfigByKey(ActivityCfg.class, entry.getKey());
			if(config == null){
				HawkLog.errPrintln("activity cfg not exist, activityType: {} ", activityType);
				return Optional.empty();
			}
			if (config.getType().intValue() == activityType) {
				ActivityBase gameActivity = activityMap.get(entry.getKey());
				if (gameActivity.isInvalid()) {
					// 如果活动已关闭，不返回活动逻辑对象。PS:为了已有的周环活动复用活动配置表的手动关闭功能
					return Optional.empty();
				}
				return Optional.ofNullable((T) gameActivity);
			}
		}
		return Optional.empty();
	}

	// 每日重置时间计算

	/**
	 * 异步消息处理
	 * 
	 * @param msg
	 * @param playerId
	 */
	public void onEventMsg(AsyncActivityEventMsg msg, String playerId) {
		try {
			long startTime = HawkTime.getMillisecond();
			
			ActivityEvent event = msg.getEvent();
			eventBus.post(event);
			
			long costTime = HawkTime.getMillisecond() - startTime;
			if(costTime >= HawkAppCfg.getInstance().getTaskTimeout()){
				HawkLog.logPrintln("async activity event timeout, event: {}, costtime: {}", event.getClass().getSimpleName(), costTime);
			}
			
		} catch (Exception e) {
			HawkException.catchException(e, "activity on event error!");
		}
	}

	/**
	 * 事件处理
	 * 
	 * @param event
	 * @param isSync
	 *            是否同步调用
	 */
	public void postEvent(ActivityEvent event, boolean isSync) {
		// 性能监测
		// HawkSysProfiler.getInstance().incMsgTask("AE-" + event.getClass().getSimpleName());
		String playerId = event.getPlayerId();
		
		// 涉及玩家的事件,判定对应玩家状态,是否进行事件处理
		if(!HawkOSOperator.isEmptyString(playerId)){
			// npc玩家不做处理
			if(this.getDataGeter().isNpcPlayer(playerId)){
				return;
			}
			
			// 联盟军演状态下的玩家
			if (this.getDataGeter().isInDungeonState(playerId) && !event.isSkip() && !isSync) {
				HawkLog.logPrintln("post event failed, player in event skip = false, playerId: {}", playerId);
				return;
			}
			//跨服出去的玩家本服不做处理
			if (this.getDataGeter().isCrossPlayer(playerId)) { // 如果是跨服玩家,让玩家自己决定如何处理.
				this.getDataGeter().corssPostEvent(playerId, event);
				return;
			}
			
			// 今天未登录的离线玩家,判定特定事件是否进行存储
			if (event.isOfflineResent() && this.getDataGeter().isDailyOffLine(playerId)) {
				String key = ActivityRedisKey.EVENT_RECORD + ":" + playerId;
				event.setEventTime(HawkTime.getMillisecond());
				byte[] eventBytes = ActivityEventSerialize.serialize(event);
				if (eventBytes != null) {
					ActivityLocalRedis.getInstance().lpush(key.getBytes(), 3 * 24 * 3600, eventBytes);
					HawkLog.logPrintln("dealwith OfflineResentEvent, playerId:{}, eventName:{} ", playerId, event.getClass().getName());
				}
				return;
			}
		}
		
		if (event instanceof ContinueLoginEvent) {
			final String notcrossKey = playerId + ":crossDay:" + HawkTime.getYyyyMMddIntVal();
			String val = ActivityGlobalRedis.getInstance().getRedisSession().getString(notcrossKey);
			if (Objects.equals(notcrossKey, val)) {
				((ContinueLoginEvent) event).setCrossDay(true);
				ActivityGlobalRedis.getInstance().getRedisSession().del(notcrossKey);
			}
			if ((((ContinueLoginEvent) event).isCrossDay())) {
				ActivityGlobalRedis.getInstance().getRedisSession().del(notcrossKey);
				final String notCrossKeyTomorry = playerId + ":crossDay:" + HawkTime.getYyyyMMddIntVal(1);
				ActivityGlobalRedis.getInstance().getRedisSession().setString(notCrossKeyTomorry, notCrossKeyTomorry, 2 * 24 * 60 * 60);
				this.getDataGeter().dungeonRedisLog(playerId, "ContinueLoginEvent  isActivityCross {} val {}", ((ContinueLoginEvent) event).isCrossDay(), val);
			}
		}
		
		if (isSync) {
			try {
				eventBus.post(event);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		} else {
			HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, event.getPlayerId());
			AsyncActivityEventMsg msg = AsyncActivityEventMsg.valueOf(event);
			HawkTaskManager.getInstance().postMsg(xid, msg);
		}
		
		activityEventTimesAdd(event);
	}
	
	/**
	 * 活动事件统计
	 * @param event
	 */
	private void activityEventTimesAdd(ActivityEvent event) {
		String eventName = event.getClass().getSimpleName();
		AtomicLong stat = activityEventTimesMap.get(eventName);
		if (stat == null) {
			activityEventTimesMap.putIfAbsent(eventName, new AtomicLong(0));
			stat = activityEventTimesMap.get(eventName);
		}
		stat.addAndGet(1);
	}
	
	/**
	 * 活动事件统计
	 */
	public void activityEventStat() {
        int newCount = 0;
		JSONObject activityEventStat = new JSONObject();
		for (Entry<String, AtomicLong> entry : activityEventTimesMap.entrySet()) {
			long count = activityEventStatMap.getOrDefault(entry.getKey(), 0L);
			long value = entry.getValue().get();
			long add = value - count;
			activityEventStatMap.put(entry.getKey(), value);
            if (add > 0) {
                newCount += 1;
                activityEventStat.put(entry.getKey(), add);
			}
		}

		if (newCount > 0) {
           activityEventStat.put("ts", HawkTime.formatNowTime());
		   HawkApp.getInstance().analyzerWrite("activityEventStat", activityEventStat.toJSONString(), "list");
		}
	}

	/**
	 * 事件处理
	 * 
	 * @param event
	 */
	public void postEvent(ActivityEvent event) {
		postEvent(event, false);
	}
	
	/**
	 * 玩家数据迁移,活动数据移除
	 * @param playerId
	 */
	public void onPlayerMigrate(String playerId) {
		for(ActivityBase activity : activityMap.values()){
			activity.onPlayerMigrate(playerId);
		}
	}
	
	/**
	 * 玩家数据迁移,活动数据插入
	 * @param playerId
	 */
	public void onImmigrateInPlayer(String playerId) {
		for(ActivityBase activity : activityMap.values()){
			activity.onImmigrateInPlayer(playerId);
		}
	}
	
	/**
	 * 非玩家数据强制落地
	 */
	public void notifySaveEntity() {
		for (ActivityEntity entity : this.activityInfos.values()) {
			entity.notifyUpdate();
		}
	}
	
	public long getPlayerRegistTime(String playerId){
		Long time = playerRegistTimeMap.get(playerId);
		if(time == null){
			time = getDataGeter().getPlayerCreateAM0Date(playerId);
			playerRegistTimeMap.put(playerId, time);
		}
		return time;
	}

	public void updateGuildEffect(int activityId, String guildId, Map<Integer, Integer> effMap){
		try {
			Map<String, Map<Integer, Integer>> guildMap = guildEffMap.get(activityId);
			if(guildMap == null){
				guildMap = guildEffMap.putIfAbsent(activityId, new ConcurrentHashMap<>());
				if(guildMap == null){
					guildMap = guildEffMap.get(activityId);
				}
			}
			guildMap.put(guildId, effMap);
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}

	public void cleanGuildEffect(int activityId){
		try {
			guildEffMap.remove(activityId);
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}

	public int getBuff(int activityId, String guildId, int effId){
		try {
			Map<String, Map<Integer, Integer>> guildMap = guildEffMap.get(activityId);
			if(guildMap == null){
				return 0;
			}
			Map<Integer, Integer> effMap = guildMap.get(guildId);
			if(effMap == null){
				return 0;
			}
			return effMap.getOrDefault(effId, 0);
		} catch (Exception e) {
			HawkException.catchException(e);
			return 0;
		}
	}

	
	/**
	 * 判定活动是否失效
	 * @param activityCfg
	 * @return
	 */
	public boolean isActivityInvalid(ActivityCfg activityCfg) {
		if(activityCfg == null){
			return true;
		}
		
		if (activityCfg.isInvalid()) {
			return true;
		}
		//判定活动在本区服是否失效
		String serverId = getDataGeter().getServerId();
		List<String> serverList = activityCfg.getServerList();
		return !(serverList.isEmpty() || serverList.contains(serverId));
	}
	
	/**
	 * 加载玩家活动数据到内存
	 * @param playerId
	 */
	public void loadPlayerActivityData(String playerId){
		for (ActivityEntity activityEntity : activityInfos.values()) {
			try {
				if (activityEntity.isInvalid()) {
					continue;
				}
				
				Optional<ActivityBase> opActivity = getActivity(activityEntity.getActivityId());
				if (!opActivity.isPresent()) {
					continue;
				}
				
				ActivityBase activity = opActivity.get();
				if (activity.isHidden(playerId)) {
					continue;
				}
				
				activity.getPlayerDataEntity(playerId, false);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	public Map<Integer, ActivityBase> getActivityMap() {
		return activityMap;
	}
	
	public void shutdown() {
		if (activityMap == null) {
			return;
		}
		
		for (ActivityBase activity : activityMap.values()) {
			try {
				activity.shutdown();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
}
