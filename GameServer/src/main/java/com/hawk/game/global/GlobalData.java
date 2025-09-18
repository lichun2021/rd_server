package com.hawk.game.global;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.SerializeField;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.callback.HawkCallback;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.collection.ConcurrentHashTable;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.game.aoi.HawkAOIObj;
import org.hawk.log.HawkLog;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tickable.HawkTickable;
import org.hawk.tickable.HawkTickableContainer;
import org.hawk.util.HawkNumberConvert;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Table;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.MergeServerTimeCfg;
import com.hawk.activity.helper.PlayerActivityData;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.ServerInfo;
import com.hawk.common.ServerStatus;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.MergeServerGroupCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.model.CsPlayerData;
import com.hawk.game.data.PfOnlineInfo;
import com.hawk.game.data.ServerSettingData;
import com.hawk.game.entity.GuildInfoEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.PlayerRelationEntity;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengcyb.CYBORGRoomManager;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.lianmengjunyan.LMJYRoomManager;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.PlayerAccountCancellationModule;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZState;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLConst.FGYLState;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZConst.XHJZState;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager;
import com.hawk.game.msg.PlayerUnlockImageMsg;
import com.hawk.game.msg.PlayerUnlockImageMsg.PLAYERSTAT_PARAM;
import com.hawk.game.msg.PlayerUnlockImageMsg.UnlockType;
import com.hawk.game.msg.RemoveArmourMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.playercopy.PlayerCopyService;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Chat.HPPushChat;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Questionnaire.PushSurverInfo;
import com.hawk.game.protocol.Questionnaire.SurveyInfo;
import com.hawk.game.protocol.Status;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.queryentity.BuildingLevelCount;
import com.hawk.game.queryentity.ServerPlayerStat;
import com.hawk.game.queryentity.SuperArmourInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GlobalMail;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.ImmgrationService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.SearchService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.DailyInfoField;
import com.hawk.game.util.GsConst.GlobalControlType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.Predicates;
import com.hawk.game.world.WorldScene;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldRobotService;
import com.hawk.health.HealthGameManager;
import com.hawk.health.entity.FetchHealthGameConfResult;
import com.hawk.health.entity.UpdateUserInfo;
import com.hawk.health.entity.UpdateUserInfoResult;
import com.hawk.log.LogConst.Platform;
import com.hawk.sdk.SDKConst.UserType;
import com.hawk.sdk.config.HealthCfg;
import com.hawk.sdk.config.TencentCfg;
import com.hawk.zoninesdk.ZonineSDK;
import com.hawk.zoninesdk.datamanager.OpDataType;

public class GlobalData extends HawkTickable {
	/**
	 * 
	 */
	private static Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 在线用户缓存
	 */
	private Map<String, Player> activePlayer;
	/**
	 * puid+serverId 得出playerId.
	 */
	private Map<String, String> onlinePuidPlayerId;
	/**
	 * puid + serverId 对应的账号数据
	 */
	public Map<String, AccountInfo> puidAccountData;
	/**
	 * 已重置的账号角色Id
	 */
	private Set<String> resetAccountRoleIds;
	
	/**
	 * playerId, puid+serverId
	 */
	private Map<String, String> playerPuidData;
	/**
	 * 用户数据缓存
	 */
	private LoadingCache<String, PlayerData> playerDataCache;
	/**
	 * 新用户的Entity缓存
	 */
	private LoadingCache<String, PlayerEntity> playerEntityCache;
	/**
	 * 大本等级数量分布
	 */
	private Map<Integer, Integer> mainBuildingLevels;
	/**
	 * 数据定时更新容器
	 */
	private HawkTickableContainer tickableContainer;
	/**
	 * 在线且没有联盟的玩家ID集合
	 */
	private Set<String> outGuildPlayerIds;
	/**
	 * 全服邮件
	 */
	private List<GlobalMail> globalMails;
	/**
	 * 世界聊天
	 */
	private Deque<ChatMsg> worldMsgs;
	/**
	 * 联盟聊天
	 */
	private Map<String, Deque<ChatMsg>> guildsMsgs;
	/**
	 * 在线用户数
	 */
	private Map<String, PfOnlineInfo> onlineInfo;
	/**
	 * 服务器初始化时获取数据库里面总的注册人数
	 */
	private AtomicInteger registerCount;
	/**
	 * 本服联盟名字
	 */
	private Set<String> guildNames;
	/**
	 * 本服联盟简称
	 */
	private Set<String> guildTags;
	/**
	 * 调查问卷
	 */
	private List<SurveyInfo.Builder> globalQuestionnaire;
	
	/**
	 * 联盟封禁信息<联盟ID, 封禁类型, 封禁具体信息>
	 */
	private Table<String, String, String> guildBanInfos;
	/**
	 * 日充值玩家
	 */
	private Set<String> dailyRechargePlayers;
	/**
	 * 上次充值时间
	 */
	private AtomicLong lastRechargeTime;
	/**
	 * 服务器控制变量
	 */
	private ServerSettingData serverSettingData;
	/**
	 * 健康游戏配置参数
	 */
	private FetchHealthGameConfResult healthGameConf;
	/**
	 * {playerid, AccountRoleInfo>
	 */
	private Map<String, AccountRoleInfo> playerIdAccountRoleMap;
	/**
	 * 记录平台头像被屏蔽的账号<openid, 结束时间>
	 */
	private Map<String, Long> banPortraitAccounts;
	
	/**
	 * 上一次上报实时在线数据的时间，单位分钟
	 */
	private int lastLogOnlineMinute;

	/**
	 * 全服保护状态结束时间
	 */
	private long globalProtectEndTime;

	/**
	 * 打破全服保护状态晚间
	 */
	private Set<String> brokenProtectPlayer;
	/**
	 * 玩家名字自增索引
	 */
	private AtomicInteger randomPlyaerNameIndex;
	/**
	 * 全服控制参数
	 */
	private Map<Integer, String> globalControlBanTypes;
	/**
	 * 健康引导开关
	 */
	private volatile boolean healthGameEnable;
	/**
	 * 神器结束时间
	 */
	private Set<SuperArmourInfo> superArmourEndTime;
	/**
	 * 账号注销信息
	 */
	private Map<String, Long> accountCancellationMap;
	/**
	 * 玩家平台授权信息
	 */
	private Map<String, Set<Integer>> playerPfAuthInfoMap;
	/**
	 * 起服存一份serverMap
	 */
	private Map<String, ServerInfo> serverMap;
	/**
	 * 迁入玩家列表
	 */
	private Map<String, Long> immgrationInPlayerIds;
	/**
	 * 服务器开服时间：主要用于跨服的玩家获取
	 */
	private Map<String, Long> serverOpenTimeMap;
	/**
	 * 红点开关集合
	 */
	private Map<Integer, Long> redDotSwitchMap;
	
	/**
	 * 大本排名等级达成时间
	 */
	private Map<String, Long> cityRankTime;
	
	/**
	 * 玩家迁服记录<playerId:tarServer, sourceServer>
	 */
	private Map<String, String> playerImmgrationDataMap;
	/**
	 * 本地拉取puidProfile信息的白名单账号
	 */
	private Set<String> puidProfileCtrlSet;
	
	/** 禁止发放的邮件id */
	private Set<Integer> forbiddenMailIdSet = new ConcurrentHashSet<>();
	
	/**
	 * 全局实例对象
	 */
	private static GlobalData instance = null;	

	/**
	 * 获取实例对象
	 *
	 * @return
	 */
	public static GlobalData getInstance() {
		if (instance == null) {
			instance = new GlobalData();
		}
		return instance;
	}

	/**
	 * 构造
	 */
	private GlobalData() {
		puidAccountData = new ConcurrentHashMap<String, AccountInfo>();
		playerPuidData = new ConcurrentHashMap<String, String>();
		activePlayer = new ConcurrentHashMap<String, Player>();
		outGuildPlayerIds = new ConcurrentHashSet<String>();
		tickableContainer = new HawkTickableContainer();
		mainBuildingLevels = new ConcurrentHashMap<Integer, Integer>();
		onlineInfo = new ConcurrentHashMap<String, PfOnlineInfo>();
		guildsMsgs = new ConcurrentHashMap<String, Deque<ChatMsg>>();
		guildNames = new ConcurrentHashSet<String>();
		guildTags = new ConcurrentHashSet<String>();
		registerCount = new AtomicInteger();
		guildBanInfos = ConcurrentHashTable.create();
		dailyRechargePlayers = new ConcurrentHashSet<String>();
		lastRechargeTime = new AtomicLong(HawkApp.getInstance().getCurrentTime());
		resetAccountRoleIds = new ConcurrentHashSet<String>();
		playerIdAccountRoleMap = new ConcurrentHashMap<>();
		brokenProtectPlayer = new ConcurrentHashSet<String>();
		banPortraitAccounts = new ConcurrentHashMap<String, Long>();
		onlinePuidPlayerId = new ConcurrentHashMap<>();
		superArmourEndTime = new ConcurrentHashSet<>();
		playerPfAuthInfoMap = new ConcurrentHashMap<>();
		immgrationInPlayerIds = new ConcurrentHashMap<>();
		globalControlBanTypes = new ConcurrentHashMap<>();
		serverOpenTimeMap = new ConcurrentHashMap<>();
		redDotSwitchMap = new ConcurrentHashMap<Integer, Long>();
		cityRankTime = new ConcurrentHashMap<>();
		playerImmgrationDataMap = new HashMap<>();
		puidProfileCtrlSet = new ConcurrentHashSet<>();
		GsApp.getInstance().addTickable(this);
	}

	/**
	 * 初始化全局数据
	 *
	 * @return
	 */
	public boolean init() {
		//账号复制相关初始化
		List<String> copySuccPlayerIds = playerCopyInit();
		// 初始化玩家数据缓存
		if (!initPlayerDataCache()) {
			return false;
		}

		// 初始化账号数据
		if (!initAccountData()) {
			return false;
		}

		//全服大本等级分布、神奇过期时间、本服已注册人数等数据初始化
		initGameServerData();
		
		// 获取平台头像被屏蔽的账号
		Map<String, Long> openidMap = RedisProxy.getInstance().getAllBanPortraitAccount();
		banPortraitAccounts.putAll(openidMap);

		// 充值服务器状态
		resetServerStatus(GsConfig.getInstance().getServerId(), getRegisterCount(), 0, HawkTime.getSeconds());

		// 初始化全局邮件
		if (!initGlobalMails()) {
			return false;
		}

		// 初始化聊天数据
		if (!initChatMsgs()) {
			return false;
		}

		// 初始化联盟信息
		if (!initGuildInfos()) {
			return false;
		}

		// 初始化调查问卷信息
		if (!initGlobalQuestionnaire()) {
			return false;
		}
		
		initAccountCancel();
		initServerControlData();
		initHealthGame();
		// 注册更新对象
		registerTickable();

		// 全服保护结束时间
		long protectCoverAddTime = WorldMapConstProperty.getInstance().getProtectCoverAddTime();
		if (protectCoverAddTime > 0) {
			globalProtectEndTime = HawkTime.getMillisecond() + WorldMapConstProperty.getInstance().getProtectCoverAddTime();
		}
		
		globalControlBanTypes.putAll(LocalRedis.getInstance().getAllGlobalControlBanType());
		
		initCrossPlayerMirror();
		
		serverMap = new HashMap<>();
		List<ServerInfo> serverList = RedisProxy.getInstance().getServerList();
		for (ServerInfo serverInfo : serverList) {
			serverMap.put(serverInfo.getId(), serverInfo);
		}
		
		Map<Integer, Long> map = RedisProxy.getInstance().getAllRedDotSwitch();
		redDotSwitchMap.putAll(map);
		
		if (!copySuccPlayerIds.isEmpty()) {
			PlayerCopyService.getInstance().copySuccPlayerRefresh(copySuccPlayerIds);
		}
		
		// 初始化玩家迁服记录
		initPlayerImmgrationData();
		
		Set<Integer> mailIds = RedisProxy.getInstance().getForBiddenMailIds();
		if (!mailIds.isEmpty()) {
			forbiddenMailIdSet.addAll(mailIds);
		}
		
		return true;
	}
	

	/**
	 * 账号注销相关初始化
	 */
	private void initAccountCancel() {
		//2025-02-13 23:00:00
		if (HawkTime.getMillisecond() > 1739458800000L) {
			accountCancellationMap = new ConcurrentHashMap<>();
			Map<String, Long> map = RedisProxy.getInstance().getAllAccountCancellationInfo("0");
			for (Entry<String, Long> entry : map.entrySet()) {
				if (this.getAccountInfoByPlayerId(entry.getKey()) != null) {
					accountCancellationMap.put(entry.getKey(), entry.getValue());
				}
			}
		} else {
			String serverId = GsConfig.getInstance().getServerId();
			accountCancellationMap = RedisProxy.getInstance().getAllAccountCancellationInfo(serverId);
			accountCancelMerge();
			if (!accountCancellationMap.isEmpty()) {
				Map<String, String> map = new HashMap<>();
				accountCancellationMap.entrySet().forEach(e -> map.put(e.getKey(), String.valueOf(e.getValue())));
				try {
					String key = RedisProxy.ACCOUNT_CANCELLATION_BEGIN + ":0";
					RedisProxy.getInstance().getRedisSession().hmSet(key, map, 0);
					key = RedisProxy.ACCOUNT_CANCELLATION_BEGIN + ":" + serverId;
					RedisProxy.getInstance().getRedisSession().expire(key, GsConst.DAY_SECONDS * 15);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}
	
	/**
	 * 注销账号合服相关处理
	 */
	private void accountCancelMerge() {
		String serverId = GsConfig.getInstance().getServerId();
		ConfigIterator<MergeServerTimeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(MergeServerTimeCfg.class);
		while (iterator.hasNext()) {
			try {
				MergeServerTimeCfg timeCfg = iterator.next();
				if (!serverId.equals(timeCfg.getMasterServer())) {
					continue;
				}
				if (accountCancellationMap.isEmpty()) {
					accountCancellationMap = new HashMap<>();
				}
				for(String slaveId : timeCfg.getSlaveServerIdList()) {
					Map<String, Long> slaveInfoMap = RedisProxy.getInstance().getAllAccountCancellationInfo(slaveId);
					if (!slaveInfoMap.isEmpty()) {
						accountCancellationMap.putAll(slaveInfoMap);
						String key = RedisProxy.ACCOUNT_CANCELLATION_BEGIN + ":" + slaveId;
						RedisProxy.getInstance().getRedisSession().expire(key, GsConst.DAY_SECONDS * 15);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 健康游戏相关初始化
	 */
	private void initHealthGame() {
		setHealthGameEnable(HealthCfg.getInstance().isHealthGameEnable());
		GameUtil.postHealthGameTask(new HawkCallback() {
			@Override
			public int invoke(Object args) {
				Map<String, Object> paramMap = GameUtil.getHealthReqParam(null);
				healthGameConf = HealthGameManager.getInstance().getHealthGameConf(paramMap);
				if (healthGameConf == null) {
					healthGameConf = new FetchHealthGameConfResult();
					healthGameConf.init();
					HawkLog.logPrintln("health game conf fetch failed");
				} else {
					HawkLog.logPrintln("health game conf fetch success: {}", healthGameConf.toString());
				}
				
				return 0;
			}
		});
	}
	
	private void initCrossPlayerMirror() {
		
	}
	
	/**
	 * 初始化服务器控制参数
	 */
	private void initServerControlData() {
		int maxOnlineCnt = GsConfig.getInstance().getSessionMaxSize();
		int maxRegisterCnt = GsConfig.getInstance().getRegisterMaxNum();
		int maxWaitCnt = GsConfig.getInstance().getLoginWaitMaxNum();
		serverSettingData = RedisProxy.getInstance().getServerControlData();
		if (serverSettingData == null) {
			serverSettingData = new ServerSettingData(maxOnlineCnt, maxRegisterCnt, maxWaitCnt);
			serverSettingData.setCfgMaxOnlineCount(maxOnlineCnt);
			serverSettingData.setCfgMaxRegisterCount(maxRegisterCnt);
			serverSettingData.setCfgMaxWaitCount(maxWaitCnt);
		} else {
			GameUtil.updateServerControlData();
		}
	}
	
	/**
	 * 账号复制相关初始化
	 */
	private List<String> playerCopyInit() {
		List<String> copySuccPlayerIds = new ArrayList<>();
		if (GsConfig.getInstance().isDebug()) {
			try {
				if (PlayerCopyService.getInstance().init()) {
					copySuccPlayerIds = PlayerCopyService.getInstance().copyPlayer();
					PlayerCopyService.getInstance().removePlayerData();
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return copySuccPlayerIds;
	}
	
	private void initGameServerData() {
		// 获取全服大本等级分布
		int buildingType = BuildingType.CONSTRUCTION_FACTORY_VALUE;
		String sql = String.format(
				"select buildingCfgId %% 100 as level, count(buildingCfgId) as count from building where type = %d group by buildingCfgId",
				buildingType);
		List<BuildingLevelCount> levelCountList = HawkDBManager.getInstance().executeQuery(sql,
				BuildingLevelCount.class);
		
		if (levelCountList != null) {
			for (BuildingLevelCount levelCount : levelCountList) {
				mainBuildingLevels.put(levelCount.getLevel(), levelCount.getCount());
			}
		}
		
		//神器过期时间
		superArmourEndTime = new ConcurrentHashSet<>();
		String armourSql = String.format("select id, playerId, endTime from armour where isSuper = 1");
		List<SuperArmourInfo> superArmourInfo = HawkDBManager.getInstance().executeQuery(armourSql, SuperArmourInfo.class);
		if (superArmourInfo != null) {
			for (SuperArmourInfo info : superArmourInfo) {
				superArmourEndTime.add(info);
			}
		}
		
		// 获取本服已注册人数
		String registerSql = String.format("select channel, count(id) as register from player group by channel");
		List<ServerPlayerStat> registerList = HawkDBManager.getInstance().executeQuery(registerSql, ServerPlayerStat.class);
		if (registerList != null) {
			int total = 0;
			for (ServerPlayerStat stat : registerList) {
				total += stat.getRegister();
				addRegister(stat.getChannel(), stat.getRegister());
			}
			
			registerCount.set(total);
		}
	}
	
	/**
	 * 获取服务器控制变量
	 * 
	 * @return
	 */
	public ServerSettingData getServerSettingData() {
		return serverSettingData;
	}
	
	/**
	 * 添加指定渠道的注册数
	 * 
	 * @param channel
	 * @param count
	 */
	public void addRegister(String channel, int count) {
		PfOnlineInfo pfOnlineInfo = getPfOnlineInfo(channel);
		pfOnlineInfo.addRegister(count);
	}
	
	/**
	 * 添加排队等待人数
	 * @param channel
	 * @param count
	 */
	public void addLoginWait(String channel, int count) {
		if (HawkOSOperator.isEmptyString(channel)) {
			HawkLog.logPrintln("add login wait failed, channel: {}", channel);
			return;
		}
		
		PfOnlineInfo pfOnlineInfo = getPfOnlineInfo(channel);
		pfOnlineInfo.waitLoginAdd(count);
	}
	
	/**
	 * 排队等待人数减少
	 * @param channel
	 * @param count
	 */
	public void decLoginWait(String channel, int count) {
		if (HawkOSOperator.isEmptyString(channel)) {
			HawkLog.logPrintln("dec login wait failed, channel: {}", channel);
			return;
		}
		
		PfOnlineInfo pfOnlineInfo = getPfOnlineInfo(channel);
		pfOnlineInfo.waitLoginSub(count);
	}
	
	/**
	 * 获取PfOnlineInfo对象
	 * @param channel
	 * @return
	 */
	private PfOnlineInfo getPfOnlineInfo(String channel) {
		PfOnlineInfo pfOnlineInfo = onlineInfo.get(channel);
		if (pfOnlineInfo == null) {
			onlineInfo.putIfAbsent(channel, new PfOnlineInfo());
			pfOnlineInfo = onlineInfo.get(channel);
		}
		
		return pfOnlineInfo;
	}

	public FetchHealthGameConfResult getHealthGameConf() {
		return healthGameConf;
	}

	/**
	 * 初始化玩家数据缓存
	 * 
	 * @return
	 */
	private boolean initPlayerDataCache() {
		// 新建玩家的entity对象缓存
		playerEntityCache = CacheBuilder.newBuilder().recordStats().maximumSize(4096).initialCapacity(32).expireAfterAccess(60, TimeUnit.SECONDS)
				.build(new CacheLoader<String, PlayerEntity>() {
					@Override
					public PlayerEntity load(String playerId) {
						return null;
					}
				});
		
		// 玩家数据缓存
		playerDataCache = CacheBuilder.newBuilder().recordStats().maximumSize(GsConfig.getInstance().getCacheMaxSize())
				.initialCapacity(GsConfig.getInstance().getCacheInitSize())
				.expireAfterAccess(GsConfig.getInstance().getCacheExpireTime(), TimeUnit.MILLISECONDS)
				.removalListener(new RemovalListener<String, PlayerData>() {
					@Override
					public void onRemoval(RemovalNotification<String, PlayerData> notification) {
						final PlayerData playerData = notification.getValue();

						// 记录日志
						HawkLog.logPrintln("cache remove player data, playerId: {}, puid: {}, deviceId: {}, reason: {}",
								playerData.getPlayerEntity().getId(), playerData.getPlayerEntity().getPuid(),
								playerData.getPlayerEntity().getDeviceId(), notification.getCause().name());

						// 玩家是否在线判断
						HawkXID playerXid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerData.getPlayerEntity().getId());
						HawkObjBase<HawkXID, HawkAppObj> playerObj = GsApp.getInstance().queryObject(playerXid);
						if (playerObj != null) {
							Player player = (Player) playerObj.getImpl();
							if (player.isActiveOnline()) {
								// 严重错误
								HawkLog.errPrintln("cache remove player data illegality, playerId: {}, online: true", playerXid.getUUID());
							} else {
								// 非法状态
								HawkLog.errPrintln("cache remove player data illegality, playerId: {}, online: false", playerXid.getUUID());
							}
						}
					}
				}).build(new CacheLoader<String, PlayerData>() {
					@Override
					public PlayerData load(String playerId) {
						try {
							if (isResetAccount(playerId)) {
								HawkLog.errPrintln("playerdata load failed, player is removed player, playerId: {}", playerId);
								return null;
							}
							PlayerData playerData = null;
							
							HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
							HawkObjBase<HawkXID, HawkAppObj> objBase = GsApp.getInstance().queryObject(xid);
							if (objBase != null) {
								Player player = ((Player)objBase.getImpl());
								if (player.getData() != null) {
									playerData = player.getData();
								}
							}
							
							if (playerData == null) {
								//如果是跨服玩家则创建CsPlayerData();
								if (CrossService.getInstance().isImmigrationPlayer(playerId)) {
									playerData = new CsPlayerData();
								} else {
									playerData = new PlayerData();								
								}
								if (!playerData.loadPlayerData(playerId)) {
									HawkLog.errPrintln("cache load player data failed, playerId: {}", playerId);
									return null;
								}
							}

							HawkLog.logPrintln("cache load player data success, playerId: {}, puid: {}, deviceId: {}",
									playerId, playerData.getPlayerEntity().getPuid(),
									playerData.getPlayerEntity().getDeviceId());

							return playerData;
						} catch (Exception e) {
							HawkException.catchException(e);
						}
						return null;
					}
				});
		
		return true;
	}

	/**
 	 * 加载player表中所有的severId.
	 * @return
	 */
	private List<String> loadServerIdsFromDB() {
		String sql = "select distinct(serverId)  from player";
		List<String> serverList = HawkDBManager.getInstance().executeQuery(sql, null);
		
		return serverList;
	}
	
	/**
	 * 根据player表加载所有区服
	 * 合服之后会提前起服,所以用合服配置做加载是有问题的，只能是用player表加载.
	 * @return
	 */
	private List<AccountInfo> loadAccountInfo() {
		List<AccountInfo> rltList = new ArrayList<>();
		List<String> serverIdList = this.loadServerIdsFromDB();
		
		// 这是加载失败异常，如果是db为空的，那么应该是empty的list
		if (serverIdList == null) {
			// 重试三次
			int tryTimes = 0;
			do {
				HawkOSOperator.osSleep(3000);
				
				tryTimes ++;
				serverIdList = this.loadServerIdsFromDB();
			} while (serverIdList == null && tryTimes < 3);
			
			if (serverIdList == null) {
				HawkLog.errPrintln("load account disticnt server ids failed");
				return null;
			}
		}
		
		if (CollectionUtils.isEmpty(serverIdList)) {
			serverIdList = new ArrayList<>(Arrays.asList(GsConfig.getInstance().getServerId()));
		}
		
		for (String serverId : serverIdList) {
			StringBuilder sb = new StringBuilder("select id as playerId, puid, serverId, forbidenTime, logoutTime, updateTime, isActive, "
					+ "name as playerName from player where ");
			sb.append("serverId='").append(serverId).append("' order by createTime");			
			List<AccountInfo> accountInfoList = HawkDBManager.getInstance().executeQuery(
					sb.toString(), AccountInfo.class);
			
			if (accountInfoList == null) {
				HawkLog.errPrintln("load account info failed, serverId: {}", serverId);
				return null;
			}
			
			if(!CollectionUtils.isEmpty(accountInfoList)) {
				rltList.addAll(accountInfoList);
			}
		}		
		
		return rltList;
	}
	/**
	 * 初始化账号数据
	 *
	 * @return
	 */
	private boolean initAccountData() {
		// 从db拉取玩家puid和id的映射表
		try {
			long beginTime = HawkTime.getMillisecond();
			HawkLog.logPrintln("load account info from db......");
			List<AccountInfo> accountInfoList = this.loadAccountInfo();
			
			// 这是加载失败, 如果db为空，应该是个empty的list
			if (accountInfoList == null) {
				HawkLog.errPrintln("init account data failed");
				return false;
			}
			
			int accountSize = accountInfoList.size();
			randomPlyaerNameIndex = new AtomicInteger(accountSize + 1);
			if (accountInfoList != null) {			
				int cacheCount = 0;
				HawkLog.logPrintln("loaded account count: {}", accountInfoList.size());
				for (int i = 0; i < accountInfoList.size(); i++) {
					AccountInfo accountInfo = accountInfoList.get(i);
					if (!accountInfo.isActive()) {
						resetAccountRoleIds.add(accountInfo.getPlayerId());
						continue;
					}									
					
					// 添加对应的各种映射信息
					String dataKey = accountInfo.getPuid() + "#" + accountInfo.getServerId();
					playerPuidData.put(accountInfo.getPlayerId(), dataKey);
					
					if (puidAccountData.putIfAbsent(dataKey, accountInfo) != null) {
						HawkLog.errPrintln("account info duplicate, dataKey: {}", dataKey);
						continue;
					}								
					
					// 添加玩家信息
					SearchService.getInstance().addPlayerInfo(accountInfo.getPlayerName(), accountInfo.getPlayerId(), false);
					SearchService.getInstance().addPlayerNameLow(accountInfo.getPlayerName(), accountInfo.getPlayerId());
				}
				
				if (!GsConfig.getInstance().isDebug()) {
					accountInfoList.sort(new Comparator<AccountInfo>() {
						@Override
						public int compare(AccountInfo o1, AccountInfo o2) {
							return Long.valueOf(o2.getUpdateTime()).compareTo(Long.valueOf(o1.getUpdateTime()));
						}
					});
					
					for (int i = 0; i < accountInfoList.size(); i++) {
						AccountInfo accountInfo = accountInfoList.get(i);
						if (!accountInfo.isActive()) {
							continue;
						}
						
						// 预加载配置最小数量的玩家数据
						if (i >= GsConfig.getInstance().getCacheInitSize()) {
							break;
						}
						
						PlayerData playerData = getPlayerData(accountInfo.getPlayerId(), true);
						if (playerData != null) {
							playerData.loadStart();
							cacheCount ++;
						} else {
							HawkLog.errPrintln("cache load player data failed, playerId: {}", accountInfo.getPlayerId());
							if (GsConfig.getInstance().isDebug()) {
								return false;
							}
						}
					}
				}
				
				HawkLog.logPrintln("cached player data count: {}, costtime: {}(ms)", cacheCount,
						HawkTime.getMillisecond() - beginTime);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	/**
	 * 初始化全服邮件
	 * 
	 * @return
	 */
	private boolean initGlobalMails() {
		// 初始化全服邮件
		globalMails = LocalRedis.getInstance().getAllGlobalMails();
		HawkLog.logPrintln("load global mails v2 success, count: {}", globalMails.size());
		return true;
	}
	
	
	/**
	 * 无效玩家的playerData,主要用于跨服.
	 * @param playerId
	 */
	public void invalidatePlayerData(String playerId) {
		playerDataCache.invalidate(playerId);
	}
	
	/**
	 * 用于跨服
	 * @param playerId
	 * @param playerData
	 */
	public void addPlayerDataToCache(String playerId, PlayerData playerData) {
		playerDataCache.put(playerId, playerData);
	}
	
	/**
	 * 更新缓存的过期时间
	 * 
	 * @param expireTime
	 */
	public boolean updateCacheExpire(int expireTime) {
		try {
			Field localCacheField = HawkOSOperator.getClassField(playerDataCache, "localCache");
			
			Field expireField = HawkOSOperator.getClassField(localCacheField.get(playerDataCache), "expireAfterAccessNanos");
			expireField.setAccessible(true);
			final Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(expireField, expireField.getModifiers() & ~Modifier.FINAL);
			
			long expireAfterAccessNanos = TimeUnit.MILLISECONDS.toNanos(expireTime);
			expireField.set(localCacheField.get(playerDataCache), expireAfterAccessNanos);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 初始化聊天信息
	 * 
	 * @return
	 */
	private boolean initChatMsgs() {
		long curr_time = HawkTime.getMillisecond();
		LinkedList<ChatMsg> msgs = new LinkedList<>();
		worldMsgs = new ConcurrentLinkedDeque<>();
		HPPushChat worldMsg_cach = LocalRedis.getInstance().getChatMsg(null);
		if (worldMsg_cach == null) {
			return true;
		}
		List<ChatMsg> worldMsgList = worldMsg_cach.getChatMsgList();
		if (worldMsgList == null || worldMsgList.size() == 0) {
			return true;
		}
		int cacheSize = worldMsgList.size();
		int limit = GsConst.Alliance.PUSH_CHAT_COUNT;
		int size = limit > cacheSize ? cacheSize : limit;
		int msgCount = 0;
		for (ChatMsg msg : worldMsgList) {
			msgs.add(msg);
			msgCount++;
			if (msgCount >= size) {
				break;
			}
		}
		worldMsgs.addAll(msgs);
		HawkLog.logPrintln("load world chat msgs success count: {}, costtime: {}", worldMsgs.size(),
				HawkTime.getMillisecond() - curr_time);
		return true;
	}

	/**
	 * 初始化联盟信息
	 * 
	 * @return
	 */
	private boolean initGuildInfos() {
		long curr_time = HawkTime.getMillisecond();
		List<GuildInfoEntity> guildInfos = HawkDBManager.getInstance().query("from GuildInfoEntity");
		if (guildInfos != null) {
			for (GuildInfoEntity entity : guildInfos) {
				guildNames.add(entity.getName());
				guildTags.add(entity.getTag());
			}
			HawkLog.logPrintln("load guild info success count: {}, costtime: {}", guildInfos.size(),
					HawkTime.getMillisecond() - curr_time);
		}
		return true;
	}

	/**
	 * 初始化调查问卷
	 * 
	 * @return
	 */
	private boolean initGlobalQuestionnaire() {
		globalQuestionnaire = new ArrayList<SurveyInfo.Builder>();

		// 初始化问卷推送
		PushSurverInfo pushSurverInfo = LocalRedis.getInstance().getAllGlobalQuestionnaires();
		if (pushSurverInfo == null) {
			return true;
		}

		List<SurveyInfo> surveyInfos = pushSurverInfo.getSurveyInfoList();
		if (surveyInfos == null || surveyInfos.size() == 0) {
			return true;
		}

		for (SurveyInfo info : surveyInfos) {
			globalQuestionnaire.add(info.toBuilder());
		}

		HawkLog.logPrintln("load global questionnaire success, count: {}", globalQuestionnaire.size());
		return true;
	}

	/**
	 * 注册可更新对象
	 */
	private void registerTickable() {
		// 周期性显示cache的状态
		int showPlayerCachePeriod = GameConstCfg.getInstance().getShowPlayerCachePeriod();
		tickableContainer.addTickable(new HawkPeriodTickable(showPlayerCachePeriod) {
			@Override
			public void onPeriodTick() {
				HawkLog.logMonitor("cache state: {}, cache count: {}", getCacheState(), getCacheCount());
			}
		});

		// 10分钟打印一次日统计信息
		tickableContainer.addTickable(new HawkPeriodTickable(600000) {
			@Override
			public void onPeriodTick() {
				HawkLog.logPrintln("server daily info: {}", getServerDailyAccInfo(null).toJSONString());
			}
		});

		/**
		 * TLOG实时上报
		 */
		tickableContainer.addTickable(new HawkPeriodTickable(TencentCfg.getInstance().getTlogRealtimeFrequency()) {
			@Override
			public void onPeriodTick() {
				reportActiveCountTlog();
			}
		});

		// 初始化统计数据
		LoginStatis.getInstance().init();
		
		// 显示在线玩家
		int showActivePlayerPeriod = GameConstCfg.getInstance().getShowActivePlayerPeriod();
		tickableContainer.addTickable(new HawkPeriodTickable(showActivePlayerPeriod) {
			@Override
			public void onPeriodTick() {
				HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
					@Override
					public Object run() {
						refreshServerState();
						return null;
					}
				});
			}
		});

		// 5分钟一次的tlog记录在线玩家数
		tickableContainer.addTickable(new HawkPeriodTickable(300000) {
			@Override
			public void onPeriodTick() {
				LogUtil.logHeart(activePlayer.size());
			}
		});

		// 检查全服邮件是否过期
		int checkGlobalMailPeriod = GameConstCfg.getInstance().getCheckGlobalMailTime();
		tickableContainer.addTickable(new HawkPeriodTickable(checkGlobalMailPeriod) {
			@Override
			public void onPeriodTick() {
				checkGlobalMails();
			}
		});

		// 监控检查
		long healthGameCheckUpdatePeriod = HealthCfg.getInstance().getHealthGameUpdatePeriod() * 1000L;
		tickableContainer.addTickable(new HawkPeriodTickable(healthGameCheckUpdatePeriod, healthGameCheckUpdatePeriod) {
			@Override
			public void onPeriodTick() {
				updateOnlineUserHealthGameInfo();
			}
		});

		// 主播
		tickableContainer.addTickable(new HawkPeriodTickable(3000, 3000) {
			@Override
			public void onPeriodTick() {
				HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
					@Override
					public Object run() {
						sendAnchorInfo();
						return null;
					}
				});
			}
		});
		
		// 神器
		tickableContainer.addTickable(new HawkPeriodTickable(GsConst.SUPER_ARMOUR_ENDTIME_TICK) {
			@Override
			public void onPeriodTick() {
				superArmourEndCheck();
			}
		});
		
		//活动事件统计
		tickableContainer.addTickable(new HawkPeriodTickable(60000) {
			@Override
			public void onPeriodTick() {
				ActivityManager.getInstance().activityEventStat();
			}
		});

		// 计算账号注销
		tickableContainer.addTickable(new HawkPeriodTickable(600000) {
			@Override
			public void onPeriodTick() {
				accountCancelCheck();
			}
		});
		
		// 世界上的玩家数据记录
		if (GsConfig.getInstance().isDebug()) {
			tickableContainer.addTickable(new HawkPeriodTickable(GsConst.COUNT_INT_WORLD_LOG_TICK) {
				@Override
				public void onPeriodTick() {
					int countInTheWorld = 0;
					for (Player player : activePlayer.values()) {
						HawkAOIObj aoiObj = WorldScene.getInstance().getObjById(player.getAoiObjId());
						if (aoiObj == null) {
							continue;
						}
						countInTheWorld++;
					}
					logger.info("player count in the world : {}", countInTheWorld);
				}
			});
		}
	}
	
	/**
	 * tlog实时上报在线人数
	 */
	private void reportActiveCountTlog() {
		int minute = (int) (HawkApp.getInstance().getCurrentTime() / 60000);
		if (minute == lastLogOnlineMinute) {
			return;
		}
		
		lastLogOnlineMinute = minute;
		PfOnlineInfo wxOnline = onlineInfo.get("wx");
		if (wxOnline != null && wxOnline.getRegisterCount() > 0) {
			LogUtil.logOnlinecnt("wx", wxOnline.getIosOnlineCnt(), wxOnline.getAndroidOnlineCnt(),
					wxOnline.getRegisterCount(), wxOnline.getWaitLoginCount());
		}

		PfOnlineInfo qqOnline = onlineInfo.get("qq");
		if (qqOnline != null && qqOnline.getRegisterCount() > 0) {
			LogUtil.logOnlinecnt("qq", qqOnline.getIosOnlineCnt(), qqOnline.getAndroidOnlineCnt(),
					qqOnline.getRegisterCount(), qqOnline.getWaitLoginCount());
		}

		PfOnlineInfo guestOnline = onlineInfo.get("guest");
		if (guestOnline != null && guestOnline.getRegisterCount() > 0) {
			LogUtil.logOnlinecnt("guest", guestOnline.getIosOnlineCnt(), guestOnline.getAndroidOnlineCnt(),
					guestOnline.getRegisterCount(), guestOnline.getWaitLoginCount());
		}
	}
	
	/**
	 * 刷新服务器状态
	 */
	private void refreshServerState() {
		// 活跃玩家检测
		try {
			int sessionMaxSize = getServerSettingData().getMaxOnlineCount();
			if (sessionMaxSize > 0 && getOnlineUserCount() >= sessionMaxSize / 2) {
				Set<Player> playerSet = getOnlinePlayers();
				for (Player player : playerSet) {
					if (!player.isSessionActive()) {
						removeActivePlayer(player.getId());
					}
				}
			} else {
				Iterator<Entry<String, Player>> iterator = activePlayer.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, Player> entry = iterator.next();
					if (entry.getValue() == null) {
						HawkLog.logPrintln("globaldata refreshServerState remove active player null, playerId: {}", entry.getKey());
						iterator.remove();
						continue;
					}
					
					Player player = entry.getValue();
					if (player.isCsPlayer() && !player.isSessionActive()) {
						HawkLog.logPrintln("globaldata refreshServerState remove active player, playerId: {}", entry.getKey());
						iterator.remove();
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 定时写入当前的已注册数目和在线数目
		resetServerStatus(GsConfig.getInstance().getServerId(), registerCount.get(), activePlayer.size(),
				HawkTime.getSeconds());
		
		LoginStatis.getInstance().logStatus();
		
		//刷新区服的灰度.
		GsApp.getInstance().flushVersionInfo();
	}
	
	/**
	 * 给在线玩家发送主播相关
	 */
	private void sendAnchorInfo() {
		// 这里是给当前所有玩家推送主播开播和关播消息， 如果之后加关注了，应该是只给对应粉丝推送
		int anchorId = ChatService.getInstance().getLiveAnchorId();
		int currentAnchorId = ChatService.getInstance().getCurrentAnchorId();
		if (anchorId != currentAnchorId) {
			JSONObject anchorJson = RedisProxy.getInstance().getAnchorInfo(String.valueOf(anchorId));
			ChatService.getInstance().setCurrentAnchorId(anchorJson);
			Set<Player> players = GlobalData.getInstance().getOnlinePlayers();
			for (Player player : players) {
				ChatService.getInstance().sendAnchorInfo(player, anchorJson);
			}

			if (anchorId > 0) {
				// 查找此主播的关注列表并推送给玩家
				ChatService.getInstance().pushMsgToFans(anchorJson);
				logger.info("anchor {} is login to live, current anchor is {}  !!", anchorId, currentAnchorId);
			} else {
				logger.info("anchor {} is loginout to live, current anchor is {}  !!", currentAnchorId, anchorId);
			}
		}
	}
	
	/**
	 * 神器结束检测
	 */
	private void superArmourEndCheck() {
		long currentTime = HawkTime.getMillisecond();
		
		Iterator<SuperArmourInfo> iterator = superArmourEndTime.iterator();
		
		while(iterator.hasNext()) {
			
			SuperArmourInfo info = iterator.next();
			
			if (info.getEndTime() > currentTime) {
				continue;
			}
			
			Player player = makesurePlayer(info.getPlayerId());
			
			HawkTaskManager.getInstance().postMsg(player.getXid(), new RemoveArmourMsg(info));
			
			iterator.remove();
		}
	}
	
	/**
	 * 账号注销检测
	 */
	private void accountCancelCheck() {
		List<String> playerIdList = new ArrayList<>();
		long continueTime = ConstProperty.getInstance().getAccountCancContinue();
		for (Entry<String, Long> info : accountCancellationMap.entrySet()) {
			if (info.getValue() + continueTime < HawkTime.getMillisecond()) {
				playerIdList.add(info.getKey());
				logger.info("account cancellation check success, playerId:{}, time:{}, continueTime:{}", info.getKey(), info.getValue(), continueTime);
			}
		}
		
		for (String playerId : playerIdList) {
			try {
				rmAccountCancellationInfo(playerId);
				Player player = makesurePlayer(playerId);
				PlayerAccountCancellationModule module = player.getModule(GsConst.ModuleType.ACCOUNT_CANCELLATION);
				module.realCancellation();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 游戏停服时检测实时在线数据
	 */
	public void checkOnlineInfo() {
		PfOnlineInfo wxOnline = onlineInfo.get("wx");
		if (wxOnline != null && wxOnline.getRegisterCount() > 0) {
			LogUtil.logOnlinecnt("wx", 0, 0, 0, 0);
		}

		PfOnlineInfo qqOnline = onlineInfo.get("qq");
		if (qqOnline != null && qqOnline.getRegisterCount() > 0) {
			LogUtil.logOnlinecnt("qq", 0, 0, 0, 0);
		}

		PfOnlineInfo guestOnline = onlineInfo.get("guest");
		if (guestOnline != null && guestOnline.getRegisterCount() > 0) {
			LogUtil.logOnlinecnt("guest", 0, 0, 0, 0);
		}
	}

	/**
	 * 更新在线玩家的健康游戏信息
	 */
	private void updateOnlineUserHealthGameInfo() {
		if (!isHealthGameEnable()) {
			return;
		}

		long timeNow = HawkApp.getInstance().getCurrentTime();
		List<UpdateUserInfo> qqAndroidUpdateInfos = new ArrayList<UpdateUserInfo>();
		List<UpdateUserInfo> wxAndroidUpdateInfos = new ArrayList<UpdateUserInfo>();
		List<UpdateUserInfo> qqIOSUpdateInfos = new ArrayList<UpdateUserInfo>();
		List<UpdateUserInfo> wxIOSUpdateInfos = new ArrayList<UpdateUserInfo>();
		int count = 0;
		
		for (Player player : activePlayer.values()) {
			int periodTime = (int) ((timeNow - player.getHealthGameUpdateTime()) / 1000);
			if (periodTime <= 0) {
				continue;
			}

			UpdateUserInfo updateInfo =  new UpdateUserInfo(player.getOpenId(), player.getId(), periodTime);
			count++;
			int userType = UserType.getByChannel(player.getChannel());
			if (userType == UserType.WX) {
				if (player.getPlatform().equalsIgnoreCase("ios")) {
					wxIOSUpdateInfos.add(updateInfo);
				} else {
					wxAndroidUpdateInfos.add(updateInfo);
				}
			} else {
				if (player.getPlatform().equalsIgnoreCase("ios")) {
					qqIOSUpdateInfos.add(updateInfo);
				} else {
					qqAndroidUpdateInfos.add(updateInfo);
				}
			}
		}

		HawkLog.logPrintln("health game report player online info count: {}", count);
		if (count == 0) {
			return;
		}

		GameUtil.postHealthGameTask(new HawkCallback() {
			@Override
			public int invoke(Object args) {
				reportUserInfoBatch("QQ", "android", qqAndroidUpdateInfos, timeNow);
				reportUserInfoBatch("WX", "android", wxAndroidUpdateInfos, timeNow);
				reportUserInfoBatch("QQ", "ios", qqIOSUpdateInfos, timeNow);
				reportUserInfoBatch("WX", "ios", wxIOSUpdateInfos, timeNow);
				return 0;
			}
		});
	}
	
	/**
	 * 批量上报在线时长信息
	 * 
	 * @param channel
	 * @param UpdateInfos
	 * @param time
	 */
	private void reportUserInfoBatch(String channel, String platform, List<UpdateUserInfo> updateInfos, long time) {
		if (updateInfos.isEmpty()) {
			return;
		}
		
		Map<String, Object> paramMap = GameUtil.getHealthReqParam(null);
		paramMap.put("channel", channel);
		paramMap.put("platform", platform);
		List<UpdateUserInfoResult> updateUserInfoResults = HealthGameManager.getInstance().updateUserInfoBatch(updateInfos, paramMap);
		if (updateUserInfoResults.isEmpty()) {
			HawkLog.logPrintln("health game report player online info, response back empty");
		}
		
		for (UpdateUserInfoResult userInfo : updateUserInfoResults) {
			HawkLog.logPrintln("health game report player online info, response: {}", userInfo.toString());
			Player player = activePlayer.get(userInfo.getCharacter_id());
			if (player == null) {
				continue;
			}
			
			player.setHealthGameUpdateTime(time);
			player.healthGameRemind(userInfo);
		}
	}

	/**
	 * 重置服务器状态
	 * 
	 * @param serverId
	 * @param register
	 * @param online
	 * @return
	 */
	public boolean resetServerStatus(String serverId, int register, int online, int activeTime) {
		if (activeTime <= 0) {
			activeTime = HawkTime.getSeconds();
		}
		
		
		//获取出来的列表包括主区.
		List<String> mergedList = AssembleDataManager.getInstance().getMergedServerList(GsConfig.getInstance().getServerId());
		if (!CollectionUtils.isEmpty(mergedList)) {
			for (String slaveId : mergedList) {
				try {
					ServerStatus salveServerStatus = new ServerStatus();
					salveServerStatus.setOnline(online);
					salveServerStatus.setRegister(register);
					salveServerStatus.setServerId(slaveId);
					salveServerStatus.setActiveTime(activeTime);
					salveServerStatus.setServerType(GsConfig.getInstance().getServerType());
					
					RedisProxy.getInstance().updateServerStatus(salveServerStatus);
										
				} catch (Exception e) {
					HawkException.catchException(e);
				} 
			}
			
			return true;
		} else {
			ServerStatus serverStatus = new ServerStatus();
			serverStatus.setOnline(online);
			serverStatus.setRegister(register);
			serverStatus.setServerId(serverId);
			serverStatus.setActiveTime(activeTime);
			serverStatus.setServerType(GsConfig.getInstance().getServerType());
			
			return RedisProxy.getInstance().updateServerStatus(serverStatus);
		}
				
	}

	/**
	 * 退出时存储全服数据
	 */
	public void saveGlobalData() {
		try {
			// 更新聊天信息redis缓存
			updateChatCache();
			
			RankService.getInstance().updateRedisPowerRank();
			
			// 保存战旗资源数据
			WarFlagService.getInstance().savePlayerResource();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 显示缓存状态
	 */
	public String getCacheState() {
		if (playerDataCache != null) {
			return playerDataCache.stats().toString();
		}
		return null;
	}

	/**
	 * 缓存PlayerData数量
	 * 
	 * @return
	 */
	public long getCacheCount() {
		return playerDataCache.size();
	}

	/**
	 * 获取玩家数据
	 * 
	 * @param puid
	 * @param autoLoad
	 * @return
	 */
	public PlayerData getPlayerData(String playerId, boolean autoLoad) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return null;
		}
		
		if (isResetAccount(playerId)) {
			HawkLog.logPrintln("global getPlayerData null, player is removed player, playerId: {}", playerId);
			return null;
		}

		try {
			PlayerData playerData = playerDataCache.getIfPresent(playerId);
			if (playerData == null && autoLoad) {
				playerData = playerDataCache.get(playerId);
			}
			return playerData;
		} catch (Exception e) {
			HawkLog.errPrintln("LoadingCache get playerData failed, playerId: {}", playerId);
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 通知玩家数据访问
	 * 
	 * @param playerData
	 */
	public void notifyPlayerDataAccess(PlayerData playerData) {
		if (playerData != null) {
			// 对象不存在的情况下, 缓存对象
			PlayerEntity entity = playerData.getPlayerEntity();
			if (entity != null) {
				playerDataCache.getIfPresent(entity.getId());
			}
		}
	}

	/**
	 * 明确删除缓存对象
	 * 
	 * @param playerId
	 */
	public void uncachePlayerData(String playerId) {
		playerDataCache.invalidate(playerId);
	}

	/**
	 * 缓存玩家实体对象
	 * 
	 * @param entity
	 */
	public void cachePlayerEntity(PlayerEntity entity) {
		playerEntityCache.put(entity.getId(), entity);
	}
	
	/**
	 * 获取缓存的玩家实体对象
	 * 
	 * @param playerId
	 * @return
	 */
	public PlayerEntity getCacheEntity(String playerId) {
		return playerEntityCache.getIfPresent(playerId);
	}
	
	/**
	 * 移除玩家实体数据对象
	 * 
	 * @param playerId
	 */
	public void removeCacheEntity(String playerId) {
		playerEntityCache.invalidate(playerId);
	}
	/**
	 * 添加在线玩家
	 * 
	 * @param player
	 */
	public void addActivePlayer(Player player) {
		activePlayer.put(player.getId(), player);
		//如果是跨服的玩家就不触发添加在线人数
		if (!player.isCsPlayer()) {
			changePfOnlineCnt(player, true);
		}		
		if (!player.hasGuild()) {
			outGuildPlayerIds.add(player.getId());
		}
		
		ZonineSDK.getInstance().opDataReport(OpDataType.ONLINE_USER, player.getOpenId(), 1);
		
		//不同区服的人来到本服
		String key = player.getPuid() + ":" + GsConfig.getInstance().getServerId();
		onlinePuidPlayerId.put(key, player.getId());
	}

	/**
	 * 移除在线玩家
	 * 
	 * @param xid
	 */
	public void removeActivePlayer(String playerId) {
		Player player = activePlayer.remove(playerId);
		//跨服玩家不参与统计.
		if (player != null && (!player.isCsPlayer())) {
			changePfOnlineCnt(player, false);
			
			//不同区服的人来到本服
			String key = player.getPuid() + ":" + GsConfig.getInstance().getServerId();
			onlinePuidPlayerId.remove(key);
		}

		//这里不移除了，策划需要24小时离线之内的无盟玩家列表
		//outGuildPlayerIds.remove(playerId);

		long timeNow = HawkTime.getMillisecond();
		if (player == null) {
			player = makesurePlayer(playerId);
		}
		player.getEntity().setLogoutTime(timeNow);
		
		AccountInfo accountInfo = getAccountInfoByPlayerId(playerId);
		if (accountInfo != null) {
			accountInfo.setLogoutTime(timeNow);
		}
		
		ZonineSDK.getInstance().opDataReport(OpDataType.ONLINE_USER, player.getOpenId(), -1);
		
		if (GsConfig.getInstance().isLoginWaitQueue()) {
			GsApp.getInstance().notifyLogin(playerId);
		}
	}

	/**
	 * 改变平台在线人数
	 * 
	 * @param player
	 * @param add
	 */
	public void changePfOnlineCnt(Player player, boolean add) {
		if (player == null || HawkOSOperator.isEmptyString(player.getChannel())) {
			return;
		}

		String channel = player.getChannel().toLowerCase();
		PfOnlineInfo pfOnlineInfo = getPfOnlineInfo(channel.toLowerCase());

		HawkLog.logPrintln("change online count, playerId: {}, operation: {}", player.getId(), add ? "add" : "remove");
		if (add) {
			if (player.getPlatform().equalsIgnoreCase(Platform.IOS.strVal())) {
				pfOnlineInfo.addIosOnline();
			} else {
				pfOnlineInfo.addAndroidOnline();
			}
		} else {
			if (player.getPlatform().equalsIgnoreCase(Platform.IOS.strVal())) {
				pfOnlineInfo.removeIosOnline();
			} else {
				pfOnlineInfo.removeAndroidOnline();
			}
		}
	}
	
	/**
	 * 添加充值玩家
	 * 
	 * @param playerId
	 */
	public void addRechargePlayer(String playerId) {
		long now = HawkApp.getInstance().getCurrentTime();
		// 如果当前时间和上一次充值时间不是同一天，先将集合清空
		if (!HawkTime.isSameDay(lastRechargeTime.get(), now)) {
			dailyRechargePlayers.clear();
		}

		// 更新最后一次充值时间
		lastRechargeTime.set(now);

		if (!dailyRechargePlayers.contains(playerId)) {
			dailyRechargePlayers.add(playerId);
			RedisProxy.getInstance().incServerDailyInfo(DailyInfoField.DAY_RECHARGE_PLAYER, 1);
			RedisProxy.getInstance().incGlobalStatInfo(DailyInfoField.DAY_RECHARGE_PLAYER, 1);
		}
	}

	/**
	 * 获取当日累计信息
	 * 
	 * @param time
	 */
	public JSONObject getServerDailyAccInfo(String dayTime) {
		if (HawkOSOperator.isEmptyString(dayTime)) {
			dayTime = HawkTime.formatTime(HawkApp.getInstance().getCurrentTime(), HawkTime.FORMAT_YMD);
		}

		Map<String, String> infoMap = RedisProxy.getInstance().getServerDailyInfoMap(dayTime);
		JSONObject json = new JSONObject();
		for (Entry<String, String> entry : infoMap.entrySet()) {
			json.put(entry.getKey(), entry.getValue());
		}

		return json;
	}

	/**
	 * 获得在线玩家
	 * 
	 * @param id
	 * @return
	 */
	public Player getActivePlayer(String playerId) {
		return activePlayer.get(playerId);
	}

	/**
	 * 获取在线玩家数量
	 * 
	 * @return
	 */
	public int getOnlineUserCount() {
		return activePlayer.size();
	}

	/**
	 * 获取在线玩家的id列表
	 * 
	 * @return
	 */
	public Set<String> getOnlinePlayerIds() {
		Set<String> playerIds = new HashSet<String>(activePlayer.size());
		playerIds.addAll(activePlayer.keySet());
		return playerIds;
	}

	public Set<Player> getOnlinePlayers() {
		Set<Player> players = new HashSet<Player>(activePlayer.size());
		players.addAll(activePlayer.values());
		return players;
	}
	
	/**
	 * 跨服那边需要处理已经跨出去的玩家,给个提示.
	 * 只查询已有的, 不做创建.
	 * @param playerId
	 * @return
	 */
	public Player queryPlayer(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return null;
		}
		
		HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
		HawkObjBase<HawkXID, HawkAppObj> objBase = GsApp.getInstance().queryObject(xid);
		if (objBase != null) {
			return (Player)objBase.getImpl();
		} 
		
		return null;
	}
	/**
	 * 确认玩家对象存在管理器中, 这里返回的玩家对象不一定是在线的, 只是在对象管理器存在
	 * 
	 * @param playerId
	 * @return
	 */
	public Player makesurePlayer(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return null;
		}
		
		if (isResetAccount(playerId)) {
			HawkLog.logPrintln("makesurePlayer failed, player is removed player, playerId: {}", playerId);
			return null;
		}
		
		if(playerId.startsWith(WorldRobotService.ROBOT_PRE)){
			return WorldRobotService.getInstance().makesurePlayer(playerId);
		}
		
		if(playerId.startsWith(BattleService.NPC_ID)){
			return LMJYRoomManager.getInstance().makesurePlayer(playerId);
		}

		// 未被初始化的玩家不能被动创建
		AccountInfo accountInfo = null;
		if (!ImmgrationService.getInstance().isPlayerImmigrating(playerId)) {
			accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
		}
		
		if (accountInfo == null) {
			// 真正无账号, 跨服的玩家是不在AccountInfo里面的,
			if (!resetAccountRoleIds.contains(playerId) && !CrossService.getInstance().isImmigrationPlayer(playerId)) {
				HawkLog.errPrintln("player not exist, playerId: {}", playerId);
				return null;
			}
		} else {
			// 创建过程中
			if (accountInfo.isInBorn()) {			
				HawkLog.errPrintln("player in borning, playerId: {}", playerId);
				return null;
			}
		}
		
		Player player = null;
		HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId);
		try {
			HawkObjBase<HawkXID, HawkAppObj> objBase = GsApp.getInstance().queryObject(xid);
			// 对象不存在即创建
			if (objBase == null || !objBase.isObjValid()) {
				objBase = GsApp.getInstance().createObj(xid);

				// 二次check, 解决并发问题
				if (objBase == null) {
					objBase = GsApp.getInstance().queryObject(xid);
				}
			}

			if (objBase != null) {
				// 设置访问时间并获取玩家对象
				objBase.setVisitTime(GsApp.getInstance().getCurrentTime());
				player = (Player) objBase.getImpl();
				
				// 玩家数据不存在, 加载
				if (player.getData() == null) {
					// 获取玩家数据, 检测数据
					PlayerData playerData = getPlayerData(playerId, true);
					if (playerData == null) {
						return null;
					}

					player.updateData(playerData);
					
					// 初始化计算玩家战力
					if (GsApp.getInstance().isInitOK()) {
						// 流失玩家不计算战力
						if (getPlayerLossDays(playerId) < GsConfig.getInstance().getLossCacheDays()) {
							playerData.getPowerElectric().calcElectricBeforeChange();
						}
					}
				} else {
					if (!player.isActiveOnline()) {
						notifyPlayerDataAccess(player.getData());
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		if(player.getLmjyState() == PState.GAMEING){// 如果玩家正在副本中玩耍
			return LMJYRoomManager.getInstance().makesurePlayer(playerId);
		}
		if(player.getTBLYState() == TBLYState.GAMEING){// 如果玩家正在副本中玩耍
			return TBLYRoomManager.getInstance().makesurePlayer(playerId);
		}
		if(player.getCYBORGState() == CYBORGState.GAMEING){// 如果玩家正在副本中玩耍
			return CYBORGRoomManager.getInstance().makesurePlayer(playerId);
		}
		if(player.getDYZZState() == DYZZState.GAMEING){// 如果玩家正在副本中玩耍
			return DYZZRoomManager.getInstance().makesurePlayer(playerId);
		}
		if(player.getYQZZState() == YQZZState.GAMEING){// 如果玩家正在副本中玩耍
			return YQZZRoomManager.getInstance().makesurePlayer(playerId);
		}
		if(player.getXhjzState() == XHJZState.GAMEING){// 如果玩家正在副本中玩耍
			return XHJZRoomManager.getInstance().makesurePlayer(playerId);
		}
		if(player.getFgylState() == FGYLState.GAMEING){// 如果玩家正在副本中玩耍
			return FGYLRoomManager.getInstance().makesurePlayer(playerId);
		}
		
		return player;
	}
	
	/**
	 * 获取玩家快照信息(由配置文件决定是二进制还是文本格式)
	 * 
	 * @param playerIds
	 * @return
	 */
	public Map<String, Player> getPlayerMap(String... playerIds) {
		Map<String, Player> map = new HashMap<>();
		for(String playerId : playerIds){
			Player player = this.makesurePlayer(playerId);
			if (player == null) {
				continue;
			}
			map.put(playerId, player);
		}
		return map;
	}

	/**
	 * 脚本中创建玩家对象的接口
	 * 
	 * @param scriptParams
	 * @return
	 */
	public Player scriptMakesurePlayer(Map<String, String> scriptParams) {
		String playerId = scriptParams.get("playerId");
		String playerName = scriptParams.get("playerName");

		// 获取玩家的数据对象
		if (HawkOSOperator.isEmptyString(playerId) && !HawkOSOperator.isEmptyString(playerName)) {
			playerId = GameUtil.getPlayerIdByName(playerName);
		}

		return scriptMakesurePlayer(playerId);
	}

	/**
	 * 脚本中创建玩家对象的接口
	 * 
	 * @param scriptParams
	 * @return
	 */
	public Player scriptMakesurePlayer(String playerId) {
		// 从本服跨服出去的玩家
		if (CrossService.getInstance().isEmigrationPlayer(playerId)) {
			return null;
		}
		return GlobalData.getInstance().makesurePlayer(playerId);
	}
	
	/**
	 * 随机出一个玩家名字
	 *
	 * @param puid
	 * @return
	 */
	public String randomPlayerName() {
		int count = getServerPlayerNameIncrIndex();
		//后缀, 自增 + 区服ID
		String suffix = HawkNumberConvert.convertString(count) + HawkNumberConvert.convertString(Integer.parseInt(GsConfig.getInstance().getServerId()));
		//测试了一下玩家取名字最多 指挥官+6个字符,所以拼到7位字符就不会有重名的问题.
		suffix = StringUtils.rightPad(suffix, 7, "0");
		String playerName = String.format("%s%s", GameConstCfg.getInstance().getNamePrefix(), suffix);

		// 规则生成检测不合法的时候, 随机字符串生成
		if (GameUtil.checkPlayerNameCode(playerName) != Status.SysError.SUCCESS_OK_VALUE) {
			int randLen = ConstProperty.getInstance().getPlayerNameMax()
					- GameConstCfg.getInstance().getNamePrefix().length();
			if (randLen <= 0) {
				throw new RuntimeException("name prefix lenth unmatch max limit");
			}
			randLen = HawkRand.randInt(1, randLen);
			playerName = GameConstCfg.getInstance().getNamePrefix() + HawkOSOperator.randomString(randLen);
		}
		return playerName;
	}

	/**
	 * 随机生成puid
	 * 
	 * @return
	 */
	public String randomPlatformPuid(String platform) {
		int count = getServerPlayerNameIncrIndex();
		return String.format("%s_0%d%s", platform, count, GsConfig.getInstance().getServerId());
	}

	/**
	 * 获取账号信息
	 *
	 * @param puid
	 * @return
	 */
	public AccountInfo getAccountInfo(String puid, String serverId) {
		if (HawkOSOperator.isEmptyString(puid)) {
			return null;
		}

		if (HawkOSOperator.isEmptyString(serverId)) {
			serverId = GsConfig.getInstance().getServerId();
		}

		if (!HawkOSOperator.isEmptyString(serverId)) {
			String dataKey = puid + "#" + serverId;
			return puidAccountData.get(dataKey);
		}
		return null;
	}

	/**
	 * 根据opendId获取玩家id列表
	 * 
	 * @param openId
	 * @return
	 */
	public List<String> getPlayerIdsByOpenid(String openid) {
		String sql = String.format("select id from player where openid = '%s' order by updateTime desc", openid);
		List<String> idList = HawkDBManager.getInstance().executeQuery(sql, null);
		return idList == null ? Collections.emptyList() : idList;
	}

	/**
	 * 删除账号信息
	 * 
	 * @param puid
	 * @param serverId
	 * @return
	 */
	public boolean removeAccountInfo(String playerId) {
		if (!removeAccountInfoOnly(playerId)) {
			return false;
		}
		resetAccountRoleIds.add(playerId);
		return true;
	}
	
	/**
	 * 退出跨服的时候清理掉部分信息
	 * @param playerId
	 * @return
	 */
	public boolean removeAccountInfoOnExitCross(String playerId) {
		return removeAccountInfoOnly(playerId);
	}
	
	/**
	 * 删除账号信息
	 * @param playerId
	 * @return
	 */
	public boolean removeAccountInfoOnly(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}
		
		String dataKey = playerPuidData.remove(playerId);
		if (HawkOSOperator.isEmptyString(dataKey)) {
			logger.error("remove accountInfo error, playerId:{}, dataKey:{}", playerId, dataKey);
			return false;
		}

		puidAccountData.remove(dataKey);
		return true;
	}

	/**
	 * 判断是否为新玩家
	 * id取不到账号信息则表明账号是被封了
	 * @param playerId
	 * @return
	 */
	public boolean isNewlyPlayer(String playerId) {
		AccountInfo accountInfo = getAccountInfoByPlayerId(playerId);
		if (accountInfo != null && accountInfo.isNewly()) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 通过角色id获取账号信息
	 * 
	 * @param playerId
	 * @return
	 */
	public AccountInfo getAccountInfoByPlayerId(String playerId) {
 		String puidKey = playerPuidData.get(playerId);
		if (!HawkOSOperator.isEmptyString(puidKey)) {
			return puidAccountData.get(puidKey);
		}
		return null;
	}

	/**
	 * 通过玩家角色id获取角色名字
	 * 
	 * @param playerId
	 * @return
	 */
	public String getPlayerNameById(String playerId) {
		AccountInfo accountInfo = getAccountInfoByPlayerId(playerId);
		String playerName = "anonymous";
		if (accountInfo != null) {
			playerName = accountInfo.getPlayerName();
		}

		return playerName;
	}

	/**
	 * 更新账号信息, 不存在即添加
	 *
	 * @param puid
	 * @param serverId
	 * @param playerId
	 * @param forbidenTime
	 * @param playerName
	 * @return
	 */
	public AccountInfo updateAccountInfo(String puid, String serverId, String playerId, long forbidenTime,
			String playerName) {
		return updateAccountInfo(puid, serverId, playerId, forbidenTime, playerName, false);
	}
	
	public AccountInfo updateAccountInfo(String puid, String serverId, String playerId, long forbidenTime, String playerName, boolean addSearch) {
		AccountInfo accountInfo = getAccountInfo(puid, serverId);
		if (accountInfo == null) {
			accountInfo = new AccountInfo();
			accountInfo.setPuid(puid);
			accountInfo.setServerId(serverId);
		}
		
		accountInfo.setPlayerId(playerId);
		accountInfo.setPlayerName(playerName);
		accountInfo.setForbidenTime(forbidenTime);
		String dataKey = puid + "#" + serverId;
		puidAccountData.put(dataKey, accountInfo);
		playerPuidData.put(playerId, dataKey);
		//是否需要添加到搜索里面去,为跨服做一个兼容.
		if (addSearch) {
			SearchService.getInstance().addPlayerInfo(playerName, playerId, true);
			SearchService.getInstance().addPlayerNameLow(playerName, playerId);
		}			 
		
		return accountInfo;
	}

	/**
	 * 获取账号列表
	 * 
	 * @param accountList
	 * @return
	 */
	public int getAccountList(List<AccountInfo> accountList) {
		accountList.addAll(puidAccountData.values());
		return accountList.size();
	}

	/**
	 * 定时更新状态
	 */
	@Override
	public void onTick() {
		if (tickableContainer != null) {
			tickableContainer.onTick();
		}
	}

	/**
	 * 获取本服务器历史以来的达到制定等级级的总大本数
	 * 
	 * @return
	 */
	public int getMainBuildingCountByLevel(int level) {
		int count = 0;
		for (int i = level; i <= GsConst.BUILDING_MAX_LEVEL; i++) {
			if (mainBuildingLevels.containsKey(i)) {
				count += mainBuildingLevels.get(i);
			}
		}
		return count;
	}

	/**
	 * 通知大本建筑升级
	 * 
	 * @param curLevel
	 * @param nextLevel
	 */
	public void notifyMainBuildingLevelup(int curLevel, int nextLevel) {
		Integer count = mainBuildingLevels.get(nextLevel);
		if (count == null) {
			mainBuildingLevels.put(nextLevel, 1);
		} else {
			mainBuildingLevels.put(nextLevel, count + 1);
		}

		count = mainBuildingLevels.get(curLevel);
		if (count != null && count > 0) {
			mainBuildingLevels.put(curLevel, count - 1);
		}
	}

	/**
	 * 移除没有联盟的玩家
	 * 
	 * @param playerId
	 */
	public void removeNoGuildPlayer(String playerId) {
		outGuildPlayerIds.remove(playerId);
	}

	/**
	 * 退出联盟
	 * 
	 * @param playerId
	 */
	public void quitGuild(String playerId, String guildId) {
		outGuildPlayerIds.add(playerId);
	}

	/**
	 * 获得推荐加入联盟玩家
	 * 
	 * @param page(从1开始)
	 * @return
	 */
	public List<String> getRecommendInvitePlayers() {
		List<String> playerIds = new LinkedList<String>();
		playerIds.addAll(outGuildPlayerIds);
		return playerIds;
	}

	/**
	 * 检查全服邮件是否过期
	 */
	private void checkGlobalMails() {
		if (globalMails == null || globalMails.size() == 0) {
			return;
		}

		long currentTime = HawkTime.getMillisecond();
		long expireTime = GameConstCfg.getInstance().getSystemMailExpire();

		int index = 0;
		for (; index < globalMails.size(); index++) {
			// 找到第一个过期的
			if (currentTime > globalMails.get(index).getCreateTime() + expireTime) {
				break;
			}
		}

		// 删除过期的邮件
		if (index < globalMails.size()) {
			LocalRedis.getInstance().delGlobalMail(index);
			globalMails.subList(index, globalMails.size()).clear();
		}
	}
	
	/**
	 * 删除全服邮件
	 * 
	 * @param mailUuid
	 */
	public void deleteGlobalMail(String mailUuid) {
		if (globalMails == null || globalMails.size() == 0) {
			return;
		}
		
		int index = 0;
		for (; index < globalMails.size(); index++) {
			if (globalMails.get(index).getUuid().equals(mailUuid)) {
				break;
			}
		}

		if (index < globalMails.size()) {
			LocalRedis.getInstance().removeGlobalMail(index);
			globalMails.remove(index);
		}
	}

	/**
	 * 获取新邮件
	 */
	public List<GlobalMail> getNewGlobalMails(Player player) {
		if (globalMails == null || globalMails.size() == 0 || player == null) {
			return null;
		}

		long curTime = HawkTime.getMillisecond();
		List<GlobalMail> newMails = new ArrayList<GlobalMail>();
		for (int i = 0; i < globalMails.size(); i++) {
			GlobalMail mail = globalMails.get(i);
			// 不在生效的时间段内
			if ((mail.getStartTime() > 0 && curTime < mail.getStartTime())
					|| (mail.getEndTime() > 0 && curTime > mail.getEndTime())) {
				continue;
			}
			// 角色创建之前的全服邮件，或已经收取的系统邮件
			if (mail.getCreateTime() < player.getCreateTime()
					|| mail.getCreateTime() <= player.getEntity().getLastGmailCtime()) {
				continue;
			}
			newMails.add(mail);
		}
		return newMails;
	}

	/**
	 * 获取所有的全服邮件
	 */
	public List<GlobalMail> getAllGlobalMail() {
		return Collections.unmodifiableList(globalMails);
	}

	/**
	 * 增加全服邮件
	 */
	public void addGlobalMail(GlobalMail mail) {
		globalMails.add(0, mail);
	}

	/**
	 * 验证全服邮件是否存在
	 */
	public boolean globalMailExist(String mailId) {
		List<GlobalMail> mails = getAllGlobalMail();
		for (GlobalMail mail : mails) {
			if (mail.getUuid().equals(mailId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 更新聊天信息redis缓存 . 只在停服时调用
	 */
	private void updateChatCache() {
		// 更新周期内有新消息,则进行更新
		if (worldMsgs != null && worldMsgs.size() > 0) {
			HPPushChat.Builder builder = HPPushChat.newBuilder();
			builder.addAllChatMsg(worldMsgs);
			LocalRedis.getInstance().updateChatMsgCache(builder.build(), null);
		}
		// 联盟聊天
		for (Entry<String, Deque<ChatMsg>> entry : guildsMsgs.entrySet()) {
			String guildId = entry.getKey();
			Deque<ChatMsg> guildMsgs = entry.getValue();
			// 更新周期内有新消息,则进行更新
			if (guildMsgs != null && guildMsgs.size() > 0) {
				HPPushChat.Builder builder = HPPushChat.newBuilder();
				builder.addAllChatMsg(guildMsgs);
				LocalRedis.getInstance().updateChatMsgCache(builder.build(), guildId);
			}
		}
	}

	/**
	 * 添加一条聊天消息
	 * 
	 * @param chatMsg
	 */
	public void addChatMsg(ChatMsg chatMsg) {
		int limit = GsConst.Alliance.PUSH_CHAT_COUNT;
		if (ChatService.getInstance().isGuildMsg(chatMsg)) {
			String guildId = chatMsg.getAllianceId();
			if (HawkOSOperator.isEmptyString(guildId)) {
				return;
			}
			Deque<ChatMsg> guildMsgs = guildsMsgs.get(guildId);
			if (guildMsgs == null) {
				guildMsgs = new ConcurrentLinkedDeque<>();
				guildsMsgs.put(guildId, guildMsgs);
			}
			guildMsgs.addFirst(chatMsg);
			if (guildMsgs.size() > limit) {
				guildMsgs.removeLast();
			}
		} else if (ChatService.getInstance().isWorldMsg(chatMsg)) {
			worldMsgs.addFirst(chatMsg);
			if (worldMsgs.size() > limit) {
				worldMsgs.removeLast();
			}
		}
	}

	/**
	 * 获取最近的20条信息
	 * 
	 * @param guildId
	 * @param msgMinTime
	 * @param type
	 * @return
	 */
	public List<ChatMsg> getChatMsgCache(String guildId, String playerId, long msgMinTime, Const.ChatType type) {
		Deque<ChatMsg> msgs = new ConcurrentLinkedDeque<>();
		int limit = GsConst.Alliance.GET_CHAT_MST_COUNT;
		if (type == Const.ChatType.CHAT_ALLIANCE) {
			msgs = getGuildChatMsgCache(guildId);
			limit = GsConst.Alliance.GET_CHAT_MST_COUNT;
		} else if (type == Const.ChatType.CHAT_WORLD) {
			msgs.addAll(worldMsgs);
			limit = GsConst.Alliance.GET_CHAT_WORLD_COUNT;
		}
		List<ChatMsg> builders = new ArrayList<>(limit);
		if (msgs == null || msgs.size() == 0) {
			return builders;
		}
		RelationService relationInstance = RelationService.getInstance();
		int count = 0;
		for (ChatMsg builder : msgs) {
			if (relationInstance.isBlacklist(playerId, builder.getPlayerId())) {
				continue;
			}
			if (builder.getMsgTime() < msgMinTime) {
				builders.add(builder);
				count++;
			}
			if (count >= limit) {
				break;
			}
		}
		return builders;

	}

	/**
	 * 获取指定联盟的聊天缓存,不存在则从redis中拉取
	 * 
	 * @param guildId
	 * @return
	 */
	private Deque<ChatMsg> getGuildChatMsgCache(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return null;
		}
		Deque<ChatMsg> msgs = new ConcurrentLinkedDeque<>();
		// 若该联盟聊天记录已从redis载入缓存,则直接返回缓存信息
		if (guildsMsgs.get(guildId) != null) {
			msgs.addAll(guildsMsgs.get(guildId));
			return msgs;
		}

		HPPushChat guildMsgCache = LocalRedis.getInstance().getChatMsg(guildId);
		if (guildMsgCache == null) {
			return null;
		}

		List<ChatMsg> msgList = guildMsgCache.getChatMsgList();
		if (msgList == null || msgList.size() == 0) {
			return null;
		}
		msgs.addAll(msgList);

		guildsMsgs.put(guildId, msgs);
		return msgs;
	}

	/**
	 * 清楚玩家聊天
	 */
	public void clearPlayerChat(Player player) {
		try {
			Predicate<ChatMsg> notPlayerChat = Predicates.of(o -> !Objects.equal(o.getPlayerId(), player.getId()));
			worldMsgs = worldMsgs.stream().filter(notPlayerChat)
					.collect(Collectors.toCollection(ConcurrentLinkedDeque::new));

			Deque<ChatMsg> guildMsgs = guildsMsgs.getOrDefault(player.getGuildId(), new ConcurrentLinkedDeque<>())
					.stream().filter(notPlayerChat).collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
			guildsMsgs.put(player.getGuildId(), guildMsgs);

			this.updateChatCache();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 清除联盟聊天缓存
	 * 
	 * @param guildId
	 */
	public void deleteGuildMsgCache(String guildId) {
		if (guildsMsgs.containsKey(guildId)) {
			guildsMsgs.remove(guildId);
		}
		LocalRedis.getInstance().deleteGuildMsgCache(guildId);

	}

	/**
	 * 新增一个注册用户
	 */
	public void addUserRegister(PlayerEntity playerEntity) {
		registerCount.incrementAndGet();
		addRegister(playerEntity.getChannel(), 1);
	}

	/**
	 * 尝试占用玩家名字
	 * 
	 * @param playerName
	 * @return
	 */
	public boolean tryOccupyOrUpdatePlayerName(String playerId, String playerName) {
		return RedisProxy.getInstance().updatePlayerName(playerName, playerId);
	}

	/**
	 * 移除玩家名信息
	 * 
	 * @param name
	 */
	public void removePlayerNameInfo(String playerName) {
		if (HawkOSOperator.isEmptyString(playerName)) {
			return;
		}
		RedisProxy.getInstance().deletePlayerName(playerName);
	}

	/**
	 * 根据玩家名字获取playerId
	 * 
	 * @param playerName
	 * @return
	 */
	public String getPlayerIdByName(String playerName) {
		if (playerName == null) {
			return null;
		}
		return RedisProxy.getInstance().getPlayerIdByName(playerName);
	}

	/**
	 * 判断联盟名称是否已存在
	 * 
	 * @param guildName
	 * @return
	 */
	public boolean isGuildNameExist(String guildName) {
		return guildNames.contains(guildName);
	}

	/**
	 * 修改联盟名称
	 * 
	 * @param oldName
	 * @param newName
	 */
	public void changeGuildName(String oldName, String newName) {
		if (!HawkOSOperator.isEmptyString(oldName)) {
			guildNames.remove(oldName);
		}
		guildNames.add(newName);
	}

	/**
	 * 解散联盟移除联盟名称和简称
	 * 
	 * @param guildName
	 */
	public void onDismissGuild(String guildName, String guildTag) {
		if (!HawkOSOperator.isEmptyString(guildName)) {
			guildNames.remove(guildName);
		}
		if (!HawkOSOperator.isEmptyString(guildTag)) {
			guildTags.remove(guildTag);
		}
	}

	/**
	 * 判断联盟简称是否已存在
	 * 
	 * @param guildTag
	 * @return
	 */
	public boolean isGuildTagExist(String guildTag) {
		return guildTags.contains(guildTag);
	}

	/**
	 * 修改联盟简称
	 * 
	 * @param oldTag
	 * @param newTag
	 */
	public void changeGuildTag(String oldTag, String newTag) {
		if (!HawkOSOperator.isEmptyString(oldTag)) {
			guildTags.remove(oldTag);
		}
		guildTags.add(newTag);
	}

	/**
	 * 增加全服调查问卷
	 */
	public void addGlobalQuestionnaire(SurveyInfo.Builder surveyInfo) {
		PushSurverInfo.Builder pushInfo = PushSurverInfo.newBuilder();
		boolean exist = false;
		if (globalQuestionnaire.size() > 0) {
			for (int i = 0; i < globalQuestionnaire.size(); i++) {
				SurveyInfo.Builder question = globalQuestionnaire.get(i);
				// 若添加的问卷已存在,则刷新问卷的过期时间
				if (question.getSurveyId() == surveyInfo.getSurveyId()) {
					question.setCreateTime(surveyInfo.getCreateTime());
					exist = true;
				}
				pushInfo.addSurveyInfo(question);
			}
		}
		if (!exist) {
			globalQuestionnaire.add(surveyInfo);
			pushInfo.addSurveyInfo(surveyInfo);
		}
		LocalRedis.getInstance().updateGlobalQuestionnaire(pushInfo.build());
	}
	
	/**
	 * 下架调查问卷
	 * @param surveyId
	 * @return 是否存在此全服问卷
	 */
	
	public boolean removeGlobalQuestionaire(int surveyId) {
		PushSurverInfo.Builder pushInfo = PushSurverInfo.newBuilder();
		boolean exist = false;
		SurveyInfo.Builder needRemove = null;
		if (globalQuestionnaire.size() > 0) {
			for (int i = 0; i < globalQuestionnaire.size(); i++) {
				SurveyInfo.Builder question = globalQuestionnaire.get(i);
				if (question.getSurveyId() == surveyId) {
					needRemove = question;
					break;
				}
				pushInfo.addSurveyInfo(question);
			}
		}
		// 如果当前存在该id的问卷,则刷新问卷列表,更新问卷version
		if (needRemove != null) {
			globalQuestionnaire.remove(needRemove);
			LocalRedis.getInstance().updateGlobalQuestionnaire(pushInfo.build());
		}
		return exist;
	}

	/**
	 * 获取所有的全服问卷
	 */
	public List<SurveyInfo.Builder> getAllGlobalQuestionnaire() {
		return Collections.unmodifiableList(globalQuestionnaire);
	}

	/**
	 * 单服注册名额是否已满
	 * 
	 * @return
	 */
	public boolean isRegisterFull() {
		int limitCount = getServerSettingData().getMaxRegisterCount();
		return limitCount <= 0 || limitCount <= registerCount.intValue();
	}

	/**
	 * 获取已注册玩家的数量
	 * 
	 * @return
	 */
	public int getRegisterCount() {
		return registerCount.intValue();
	}

	/**
	 * 获取全服所有玩家的playerId
	 * 
	 * @return
	 */
	public Set<String> getAllPlayerIds() {
		return playerPuidData.keySet();
	}

	/**
	 * 添加排行榜单封禁信息
	 * 
	 * @param targetId
	 * @param rankType
	 * @param banInfo
	 */
	public void addBanRankInfo(String targetId, String rankType, String banInfo, long banEndTime) {
		if (banEndTime > 0) {
			banInfo = new StringBuilder().append(HawkTime.getMillisecond()).append(":")
					.append(banEndTime).append(":")
					.append(banInfo).append(":")
					.append(rankType).toString();
			RedisProxy.getInstance().addBanRankInfo(targetId, rankType, banInfo);
		}

		if (GuildService.getInstance().isGuildExist(targetId)) {
			guildBanInfos.put(targetId, rankType, banInfo);
		} else {
			Player player = makesurePlayer(targetId);
			if (player != null) {
				player.getData().addBanRankInfo(rankType, banInfo);
			}
		}
	}

	/**
	 * 移除排行榜单封禁信息
	 * 
	 * @param targetId
	 * @param rankType
	 */
	public void removeBanRankInfo(String targetId, String rankType) {
		RedisProxy.getInstance().delBanRankInfo(targetId, rankType);
		if (GuildService.getInstance().isGuildExist(targetId)) {
			guildBanInfos.remove(targetId, rankType);
		} else {
			Player player = makesurePlayer(targetId);
			if (player != null) {
				player.getData().removeBanRankInfo(rankType);
			}
		}
	}

	/**
	 * 获取排行榜单封禁信息
	 * 
	 * @param targetId
	 * @param rankType
	 * @return
	 */
	public String getBanRankInfo(String targetId, String rankType) {
		String banInfo = null;
		if (GuildService.getInstance().isGuildExist(targetId)) {
			banInfo = guildBanInfos.get(targetId, rankType);
		} else {
			Player player = makesurePlayer(targetId);
			if (player != null) {
				banInfo = player.getData().getBanRankInfo(rankType);
			}
		}

		return banInfo;
	}

	/**
	 * 玩家ID是否有效，即存在账号
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean isExistPlayerId(String playerId) {
		return this.getAccountInfoByPlayerId(playerId) == null ? false : true;
	}

	/**
	 * 玩家是否在线
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean isOnline(String playerId) {
		Player player = getActivePlayer(playerId);
		if (player == null || !player.isActiveOnline()) {
			return false;
		}
		return true;
	}

	/**
	 * 落地所有的entity list
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean savePlayerAllDbEntities(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}

		if (!this.isExistPlayerId(playerId)) {
			logger.error("not exist playerId: {}", playerId);
			return false;
		}

		List<HawkDBEntity> entityList = getPlayerAllDbEntityList(playerId);
		if (entityList.isEmpty()) {
			logger.warn("empty entity list playerId:{}", playerId);
			return false;
		}

		for (HawkDBEntity entity : entityList) {
			entity.notifyUpdate();
		}

		return true;
	}

	/**
	 * 获取玩家数据中所有的dbEntity
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<HawkDBEntity> getPlayerAllDbEntityList(String playerId) {
		List<HawkDBEntity> entityList = new ArrayList<HawkDBEntity>();

		// 获取对应玩家的数据对象
		PlayerData playerData = playerDataCache.getIfPresent(playerId);
		if (playerData == null) {
			return entityList;
		}

		Field[] playerDataFields = playerData.getClass().getDeclaredFields();
		for (Field field : playerDataFields) {
			if (field.getAnnotation(SerializeField.class) == null) {
				continue;
			}

			Object fieldValue = null;
			try {
				field.setAccessible(true);
				fieldValue = field.get(playerData);
			} catch (Exception e) {
				HawkException.catchException(e);
			}

			if (fieldValue == null) {
				continue;
			}

			if (fieldValue instanceof HawkDBEntity) {
				entityList.add((HawkDBEntity) fieldValue);
			} else if (fieldValue instanceof List) {
				entityList.addAll((List<HawkDBEntity>) fieldValue);
			} else if (fieldValue instanceof Map) {
				Map<?, ? extends HawkDBEntity> map = (Map<?, HawkDBEntity>) fieldValue;
				entityList.addAll(map.values());
			}
		}

		// 好友相关. 朋友和黑名单
		RelationService relationService = RelationService.getInstance();
		Map<String, PlayerRelationEntity> relationMap = relationService.getPlayerRelationMap(playerId);
		entityList.addAll(relationMap.values());

		// 活动数据
		PlayerActivityData activityData = PlayerDataHelper.getInstance().getPlayerData(playerId, false);
		if (activityData != null) {
			entityList.addAll(activityData.getDataMap().values());
			entityList.addAll(activityData.getPlayerActivityEntityMap().values());
		}

		return entityList;
	}

	/**
	 * 刷新保存所有玩家数据实体对象
	 * 
	 * @return
	 */
	public boolean saveAllPlayerDbEntities() {
		Map<String, PlayerData> playerDataMap = playerDataCache.asMap();
		boolean result = true;
		for (String playerId : playerDataMap.keySet()) {
			boolean succ = savePlayerAllDbEntities(playerId);
			if (!succ) {
				logger.error("save player entities failed, playerId: {}", playerId);
				result = false;
			}
		}
		return result;
	}

	/**
	 * 保存所有的全局信息
	 * 
	 */
	public boolean saveAllGlobalEntities() {
		boolean rlt = true;
		// 国王战官职
		try {
			PresidentOfficier.getInstance().notifySaveEntity();
		} catch (Exception e) {
			rlt = false;
			HawkException.catchException(e);
		}

		// 活动全局数据
		try {
			ActivityManager.getInstance().notifySaveEntity();
		} catch (Exception e) {
			rlt = false;
			HawkException.catchException(e);
		}

		// 世界点的数据
		try {
			WorldPointService.getInstance().notifySaveEntity();
		} catch (Exception e) {
			rlt = false;
			HawkException.catchException(e);
		}

		return rlt;
	}
	
	public void updateAccountRolePfIconInfo(String playerId, String pfIcon) {
		AccountRoleInfo accountRoleInfo = getAccountRoleInfo(playerId);
		if (accountRoleInfo != null) {
			accountRoleInfo.setPfIcon(pfIcon);
			RedisProxy.getInstance().addAccountRole(accountRoleInfo);
		}
	}
	
	public void addOrUpdateAccountRoleInfo(AccountRoleInfo accountRoleInfo) {
		if (accountRoleInfo == null) {
			return;
		}
		
		try {
			if(accountRoleInfo.getQqSVIPLevel() > 0){
				Player player = this.makesurePlayer(accountRoleInfo.getPlayerId());
				HawkTaskManager.getInstance().postMsg(player.getXid(), PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.QQSVIP));
			}
			this.playerIdAccountRoleMap.put(accountRoleInfo.getPlayerId(), accountRoleInfo);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		RedisProxy.getInstance().addAccountRole(accountRoleInfo);
	}
	
	public void removeAccountRoleInfo(String playerId) {
		AccountRoleInfo accountRoleInfo = this.playerIdAccountRoleMap.remove(playerId);
		if (accountRoleInfo == null) {
			return;
		}
		
		RedisProxy.getInstance().removeAccountRole(accountRoleInfo.getOpenId(), accountRoleInfo.getServerId(), accountRoleInfo.getPlatform());
	}
	
	@SuppressWarnings("deprecation")
	public AccountRoleInfo getAccountRoleInfo(String playerId) {
		if(WorldRobotService.getInstance().isRobotId(playerId)){
			return WorldRobotService.getInstance().getAccountRoleInfo(playerId);
		}
		
		AccountRoleInfo accountRoleInfo = this.playerIdAccountRoleMap.get(playerId);
		if (accountRoleInfo != null) {
			return accountRoleInfo;
		}
		
		AccountInfo accountInfo = this.getAccountInfoByPlayerId(playerId);
		if (accountInfo != null) {
			String[] strs = accountInfo.getPuid().split("#");
			if (strs.length != 2) {
				return accountRoleInfo;
			}
			
			String openId = strs[0];
			String platform = strs[1];
			accountRoleInfo = RedisProxy.getInstance().getAccountRole(accountInfo.getServerId(), platform, openId);
			if (accountRoleInfo != null) {
				this.playerIdAccountRoleMap.put(playerId, accountRoleInfo);
			} else if (GameConstCfg.getInstance().isTestEnv()) {
				// 导出线上数据进行合服测试的时候，会走到此处，因为导数据一般不会导redis数据；而在起服加载排行榜数据的时候，会很频繁地调到这个接口
				Player player = makesurePlayer(playerId);
				if (player != null) {
					accountRoleInfo = AccountRoleInfo.newInstance().openId(player.getOpenId()).playerId(player.getId())
							.serverId(player.getServerId()).platform(player.getPlatform()).registerTime(player.getCreateTime())
							.playerName(player.getName()).playerLevel(player.getLevel()).cityLevel(player.getCityLevel())
							.vipLevel(player.getVipLevel()).battlePoint(player.getPower()).activeServer(GsConfig.getInstance().getServerId())
							.icon(player.getIcon()).loginWay(player.getEntity().getLoginWay()).loginTime(player.getLoginTime())
							.logoutTime(player.getLogoutTime());
					addOrUpdateAccountRoleInfo(accountRoleInfo);
				}
			}
		}
		
		if (accountRoleInfo == null) {
			HawkLog.errPrintln("fetch accountRoleInfo failed, playerId: {}", playerId);
		}
		
		return accountRoleInfo;
	}

	/**
	 * 获取全服保护结束时间
	 * @return
	 */
	public long getGlobalProtectEndTime() {
		return globalProtectEndTime;
	}
	
	/**
	 * 设置全服保护结束时间
	 * @param globalProtectEndTime
	 */
	public void setGlobalProtectEndTime(long globalProtectEndTime) {
		this.globalProtectEndTime = globalProtectEndTime;
	}
	
	/**
	 * 添加破除全服保护玩家id
	 * @param playerId
	 */
	public void addBrokenProtectPlayer(String playerId) {
		brokenProtectPlayer.add(playerId);
	}
	
	public void clearBrokenProtectPlayer() {
		brokenProtectPlayer.clear();
	}
	
	/**
	 * 是否破除全服保护
	 * @param playerId
	 * @return
	 */
	public boolean isBrokenProtect(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return true;
		}
		return brokenProtectPlayer.contains(playerId);
	}
	
	/**
	 * 获取服务器的自增索引
	 * 
	 * @return
	 */
	public int getServerPlayerNameIncrIndex() {
		return randomPlyaerNameIndex.incrementAndGet();
	}
	
	/**
	 * 添加平台头像被屏蔽的账号
	 * @param openid
	 */
	public void addBanPortraitAccount(String openid, long endTime) {
		banPortraitAccounts.put(openid, endTime);
	}
	
	/**
	 * 移除平台头像被屏蔽的账号
	 * @param openid
	 */
	public void removeBanPortraitAccount(String openid) {
		banPortraitAccounts.remove(openid);
	}
	
	/**
	 * 判断一个账号是否时平台头像被屏蔽的账号
	 * @param openid
	 * @return
	 */
	public boolean isBanPortraitAccount(String openid) {
		long banEndTime = banPortraitAccounts.getOrDefault(openid, 0L);
		return banEndTime > HawkTime.getMillisecond();
	}
	
	/**
	 *  通过判断accontInfo里面的serverId
	 *  暂时只给跨服使用,非通用.
	 * @return
	 */
	public boolean isLocalPlayer(String playerId) {
		AccountInfo accountInfo = this.getAccountInfoByPlayerId(playerId);
		if (accountInfo == null) {
			return false;
		}
		
		// 判断是否为迁入的玩家
		if (immgrationInPlayerIds.containsKey(playerId)) {
			return true;
		}
		
		return this.isLocalServer(accountInfo.getServerId()); 
	}
	
	/**
	 * {puid:serverid, playerId}
	 * @return
	 */
	public Map<String, String> getOnlinePuidPlayerId() {
		return onlinePuidPlayerId;
	}
	
	/**
	 *{puid:serverid, playerId}
	 * @param puid
	 * @return
	 */
	public String  getOnlinePlayerByPuid(String puid) {
		String key = puid + ":" + GsConfig.getInstance().getServerId();
		return onlinePuidPlayerId.get(key);
	}
	
	public Map<String,PlayerData> getAllPlayerData() {
		return playerDataCache.asMap();
	}
	
	/**
	 * 该serverId 是否是本服,主要涉及到合服问题.
	 * @param serverId
	 * @return
	 */
	public boolean isLocalServer(String serverId) {
		//本服.
		if (serverId.equals(GsConfig.getInstance().getServerId())) {
			return true;
		}
		
		String mainServerId = this.getMainServerId(serverId);
		if (mainServerId.equals(GsConfig.getInstance().getServerId())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 合服之后取主服的，没合服取原服的.
	 * @param serverId
	 * @return
	 */
	public String getMainServerId(String serverId) {
		//mainServerId为空，则说明没有合服,还有一种情况是合服之后的主服,mainServerId也为空,但是else返回也没有问题.
		String mainServerId = AssembleDataManager.getInstance().getMainServerId(serverId);
		if (HawkOSOperator.isEmptyString(mainServerId)) {
			return serverId;
		} else {
			return mainServerId;
		}
	}
	
	/**
	 * 判断区服是不是主服
	 * @param serverId
	 * @return
	 */
	public boolean isMainServer(String serverId) {
		String mainServerId = this.getMainServerId(serverId);
		
		return mainServerId.equals(serverId);
	}
	
	/**
	 * 两个服是不是一个服
	 * @param serverId
	 * @param serverIdOne
	 * @return
	 */
	public boolean isSameServer(String serverId, String serverIdOne) {
		return this.getMainServerId(serverId).equals(this.getMainServerId(serverIdOne));
	}
	
	/**
	 * 不管是主服还是从服 需要判空
	 * @param serverId
	 * @return
	 */
	public List<String> getMergeServerList(String serverId) {
		return AssembleDataManager.getInstance().getMergedServerList(serverId); 
	}
	
	/**
	 * 该服是否已经合服
	 * @return
	 */
	public boolean isMerged() {
		return AssembleDataManager.getInstance().isMergedServer();
	}
	
	public void addGlobalBanType(GlobalControlType type, String reason) {
		globalControlBanTypes.put(type.intVal(), reason);
	}
	
	public boolean isGlobalBan(GlobalControlType type) {
		return globalControlBanTypes.containsKey(type.intVal());
	}
	
	public String getGlobalBanReason(GlobalControlType type) {
		return globalControlBanTypes.getOrDefault(type.intVal(), "");
	}
	
	public void cancelGlobalBan(GlobalControlType type) {
		globalControlBanTypes.remove(Integer.valueOf(type.intVal()));
	}

	public boolean isHealthGameEnable() {
		return healthGameEnable;
	}

	public void setHealthGameEnable(boolean healthGameEnable) {
		this.healthGameEnable = healthGameEnable;
	}
	
	public void addSuperArmourInfo(String armourId, String playerId, long endTime) {
		SuperArmourInfo info = new SuperArmourInfo();
		info.setPlayerId(playerId);
		info.setId(armourId);
		info.setEndTime(endTime);
		this.superArmourEndTime.add(info);
	}
	
	/**
	 * 移除账号注销信息
	 */
	public void rmAccountCancellationInfo(String playerId) {
		if (accountCancellationMap.containsKey(playerId)) {
			accountCancellationMap.remove(playerId);
			RedisProxy.getInstance().rmAccountCancellationInfo("0", playerId);
		}
	}
	
	/**
	 * 更新账号注销信息
	 * @param playerId
	 */
	public void updateAccountCancellationInfo(String playerId) {
		accountCancellationMap.put(playerId, HawkTime.getMillisecond());
		RedisProxy.getInstance().updateAccountCancellationInfo("0", playerId);
	}
	
	/**
	 * 判断玩家是否处于注销状态
	 * @param playerId
	 * @return
	 */
	public boolean isPlayerInAccountCancelState(String playerId) {
		return accountCancellationMap.containsKey(playerId);
	}
	
	/**
	 * 更新玩家的平台授权信息
	 * 
	 * @param playerId
	 * @param array
	 */
	public void updatePlayerPfAuthInfo(String openId, JSONArray array) {
		Set<Integer> set = playerPfAuthInfoMap.get(openId);
		if (set == null || set.isEmpty()) {
			set = new ConcurrentHashSet<>();
			playerPfAuthInfoMap.put(openId, set);
		}

		for (int i = 0; i < array.size(); i++) {
			int event = array.getIntValue(i);
			if (event < 0) {
				set.add(event);
			} else {
				set.remove(0 - event);
			}
		}
	}
	
	/**
	 * 是否解除全部授权
	 * @param playerId
	 * @return
	 */
	public boolean isPfAuthCancel(String openId) {
		if (!playerPfAuthInfoMap.containsKey(openId)) {
			return false;
		}
		
		Set<Integer> set = playerPfAuthInfoMap.get(openId);
		return set.contains(GsConst.EVENT_CANCEL_PFAUTH);
	}
	
	/**
	 * 是否解除个人信息
	 * @param playerId
	 * @return
	 */
	public boolean isPfPersonInfoCancel(String openId) {
		if (!playerPfAuthInfoMap.containsKey(openId)) {
			return false;
		}
		
		Set<Integer> set = playerPfAuthInfoMap.get(openId);
		return set.contains(GsConst.EVENT_CANCEL_PFAUTH) || set.contains(GsConst.EVENT_CANCEL_PERSONINFO);
	}
	
	/**
	 * 解除平台好友关系链
	 * @param playerId
	 * @return
	 */
	public boolean isPfRelationCancel(String openId) {
		if (!playerPfAuthInfoMap.containsKey(openId)) {
			return false;
		}
		
		Set<Integer> set = playerPfAuthInfoMap.get(openId);
		return set.contains(GsConst.EVENT_CANCEL_PFAUTH) || set.contains(GsConst.EVENT_CANCEL_RELATION);
	}
	
	/**
	 * 获取玩家授权状态
	 * @param playerId
	 * @return
	 */
	public String getPfAuthStatus(String openId) {
		JSONObject json = new JSONObject();
		Set<Integer> set = playerPfAuthInfoMap.get(openId);
		if (set != null && set.contains(GsConst.EVENT_CANCEL_PFAUTH)) {
			json.put("1", 0);
		} else {
			json.put("1", 1);
		}
		
		if (set != null && set.contains(GsConst.EVENT_CANCEL_PERSONINFO)) {
			json.put("2", 0);
		} else {
			json.put("2", 1);
		}
		
		if (set != null && set.contains(GsConst.EVENT_CANCEL_RELATION)) {
			json.put("3", 0);
		} else {
			json.put("3", 1);
		}
		
		return json.toJSONString();
	}
	
	public void clearPfAuthStatus(String openId) {
		playerPfAuthInfoMap.remove(openId);
	}

	public void addImmgrationInPlayerIds(String playerId) {
		immgrationInPlayerIds.put(playerId, HawkTime.getMillisecond());
	}
	
	public Map<String, ServerInfo> getServerMap() {
		return serverMap;
	}
	
	public ServerInfo getServerInfo(String serverId) {
		return serverMap.get(serverId);
	}
	
	/**
	 * 获取玩家所有同平台帐号列表
	 * @param openId
	 * @return
	 */
	public Map<String, AccountRoleInfo> getPlayerAccountInfos(String openId) {
		Map<String, String> map = RedisProxy.getInstance().getAccountRole(openId);
		if (map == null) {
			return null;
		}
		
		Map<String, AccountRoleInfo> retMap = new HashMap<>();
		for (String value : map.values()) {
			AccountRoleInfo roleInfoObj = JSONObject.parseObject(value, AccountRoleInfo.class);
			retMap.put(roleInfoObj.getServerId(), roleInfoObj);
		}
		return retMap;
	}
	
	/**
	 * 是否是重置了的账号
	 * @param playerId
	 * @return
	 */
	public boolean isResetAccount(String playerId) {
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
		if (accountInfo == null && resetAccountRoleIds.contains(playerId) ) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取区服对应的开服时间
	 * 
	 * @param serverId
	 * @return
	 */
	public long getServerOpenTime(String serverId) {
		if (GsConfig.getInstance().getServerId().equals(serverId)) {
			return GameUtil.getServerOpenTime();
		}
		
		long serverOpenTime = serverOpenTimeMap.getOrDefault(serverId, 0L);
		if (serverOpenTime > 0) {
			return serverOpenTime;
		}
		
		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(serverId);
		if (serverInfo != null && !HawkOSOperator.isEmptyString(serverInfo.getOpenTime())) {
			serverOpenTime = HawkTime.parseTime(serverInfo.getOpenTime());
			serverOpenTimeMap.put(serverId, serverOpenTime);
		} else {
			serverOpenTime = GameUtil.getServerOpenTime();
		}
		
		return serverOpenTime;
	}
	
	/**
	 * 获取本服开服天数
	 * @return
	 */
	public int getServerOpenDays() {
		long serverOpenTime = getServerOpenTime(GsConfig.getInstance().getServerId());
		long openTimeLong = HawkTime.getMillisecond() - serverOpenTime;
		long day = openTimeLong / HawkTime.DAY_MILLI_SECONDS;
		if (openTimeLong % HawkTime.DAY_MILLI_SECONDS > 0) {
			day += 1;
		}
		
		return (int)day;
	}
	
	/**
	 * 添加红点开关
	 * @param type
	 */
	public void addRedDotSwitch(int type) {
		redDotSwitchMap.put(type, HawkTime.getMillisecond());
	}
	
	/**
	 * 移除红点开关
	 * @param type
	 */
	public void delRedDotSwitch(int type) {
		redDotSwitchMap.remove(type);
	}
	
	/**
	 * 判断是否存在红点开关
	 * @param type
	 * @return
	 */
	public long getRedDotSwitchOpenTime(int type) {
		return redDotSwitchMap.getOrDefault(type, 0L);
	}
	
	/**
	 * 获取所有的红点开关
	 * @return
	 */
	public Map<Integer, Long> getAllRedDotSwitch() {
		return redDotSwitchMap;
	}
	
	/**
	 * 获取玩家流失天数
	 * @param playerId
	 * @return
	 */
	public int getPlayerLossDays(String playerId) {
		// 从accountInfo取登录登出时间,避免调用makesurePlayer()方法加载玩家数据
		AccountInfo account = getAccountInfoByPlayerId(playerId);
		if (account == null) {
			return 0;
		}
		// 玩家在线
		if (isOnline(playerId)) {
			return 0;
		}
		long logoutDuration = HawkTime.getMillisecond() - account.getLogoutTime();
		int lossDays = (int)(logoutDuration / GsConst.DAY_MILLI_SECONDS);
		return lossDays;
	}
	
	/**
	 * 获取大本等级达成时间(排行榜用,只有40级以上才记录)
	 * @param playerId
	 * @return
	 */
	public long getCityRankTime(String playerId) {
		if (cityRankTime.containsKey(playerId)) {
			return cityRankTime.get(playerId);
		}
		long time = RedisProxy.getInstance().getCityRankTime(playerId);
		cityRankTime.put(playerId, time);
		return time;
	}
	
	/**
	 * 更新大本等级达成时间
	 * @param playerId
	 * @return
	 */
	public void updateCityRankTime(String playerId) {
		cityRankTime.put(playerId, HawkTime.getMillisecond());
		RedisProxy.getInstance().updateCityRankTime(playerId);
	}
	
	/**
	 * 更新大本等级达成时间(指定时间)
	 * @param playerId
	 * @param time
	 */
	public void updateCityRankTime(String playerId, long time) {
		cityRankTime.put(playerId, time);
		RedisProxy.getInstance().updateCityRankTime(playerId, time);
	}
	
	/**
	 * 初始化迁服记录数据
	 */
	private void initPlayerImmgrationData() {
		List<String> textList = new ArrayList<String>();
		try {
			HawkOSOperator.readTextFileLines("cfg/immgrationlog/global.txt", textList);
			for (String line : textList) {
				try {
					JSONObject json = JSONObject.parseObject(line);
					String key = json.getString("playerId") + ":" + json.getString("tarServer");
					String value = json.getString("fromServer");
					playerImmgrationDataMap.put(key, value);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 取迁服玩家的原始服
	 * @param playerId
	 * @param tarServer
	 * @return
	 */
	public String getImmgrationSource(String playerId, String tarServer) {
		String key = playerId + ":" + tarServer;
		return playerImmgrationDataMap.get(key);
	}
	
	/**
	 * 慎用！！！！先搞清楚逻辑
	 * @param openId
	 * @param platform
	 * @param serverId
	 */
	public void removePuidAccountData(String openId, String platform, String serverId) {
		String puid = GameUtil.getPuidByPlatform(openId, platform);
		String dataKey = puid + "#" + serverId;
		puidAccountData.remove(dataKey);
	}
	
	/**
	 * 获取本服所属合服区服列表中的从服区服ID
	 * @return
	 */
	public List<String> getSlaveServerList() {
		return getSlaveServerList(GsConfig.getInstance().getServerId());
	}
	
	/**
	 * 获取指定区服ID所属合服区服列表中的从服区服ID
	 * @param serverId
	 * @return
	 */
	public List<String> getSlaveServerList(String serverId) {
		List<String> serverList = AssembleDataManager.getInstance().getMergedServerList(serverId);
		if (serverList != null && !serverList.isEmpty()) {
			String mainServer = serverList.get(0);
			return MergeServerGroupCfg.getSlaveServerIds(mainServer);
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * 判断是否是，本地拉取puidProfile信息的白名单账号
	 * @param openid
	 * @return
	 */
	public boolean isPuidProfileCtrlExist(String openid) {
		return puidProfileCtrlSet.contains(openid);
	}
	
	public Set<String> getPuidProfileCtrlSet() {
		return puidProfileCtrlSet;
	}
	
	/**
	 * 添加本地拉取puidProfile信息的白名单账号
	 * @param openid
	 */
	public void addPuidProfileCtrl(String openid) {
		puidProfileCtrlSet.add(openid);
	}
	
	public Set<Integer> getForbiddenMailIds() {
		return forbiddenMailIdSet;
	}

	public void addForbiddenMailId(int mailId) {
		if (MailConst.MailId.valueOf(mailId) != null) {
			forbiddenMailIdSet.add(mailId);
			RedisProxy.getInstance().addForBiddenMailId(mailId);
			HawkLog.logPrintln("global addForbiddenMailId: {}, after: {}", mailId, forbiddenMailIdSet);
		}
	}
	
	public void removeForbiddenMailId(int mailId) {
		forbiddenMailIdSet.remove(mailId);
		RedisProxy.getInstance().removeForBiddenMailId(mailId);
		HawkLog.logPrintln("global removeForbiddenMailId: {}, after: {}", mailId, forbiddenMailIdSet);
	}
	
}
