package com.hawk.game;

import java.util.*;

import org.hawk.app.HawkAppCfg;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(files = { "cfg/app.cfg", "cfg/svr.cfg" })
public class GsConfig extends HawkAppCfg {
	/**
	 * 协议安全校验
	 */
	protected final boolean protocolSecure;
	/**
	 * 初始数据缓存
	 */
	protected final int cacheInitSize;
	/**
	 * 最大数据缓存
	 */
	protected final int cacheMaxSize;
	/**
	 * 数据缓存过期时间(ms)
	 */
	protected final int cacheExpireTime;
	/**
	 * 流失玩家缓存天数判定
	 */
	protected final int lossCacheDays;
	/**
	 * 流失玩家缓存过期时间(ms)
	 */
	protected final int lossCacheTime;
	/**
	 * 玩家对象缓存时间(需小于数据缓存时间)(s)
	 */
	protected final int playerCacheTime;
	/**
	 * 生成订单的有效期
	 */
	protected final int orderExpire;
	/**
	 * 登录鉴权
	 */
	protected final int authCheckLevel;
	/**
	 * 玩家数据是否压缩
	 */
	protected final boolean gzPlayerData;
	/**
	 * 迁服的数据保存时间
	 */
	protected final int migrateExpireTime;	
	/**
	 * 快照采用二进制模式
	 */
	protected final boolean binarySnapshot;
	/**
	 * 是否需要会话秘钥
	 */
	protected final boolean sessionToken;
	/**
	 * 是否开启协议序号校验
	 */
	protected final boolean protocolOrder;
	/**
	 * 开启压缩的协议大小阈值
	 */
	protected final int protocolCompressSize;
	/**
	 * 客户端性能数据分析是否开启
	 */
	protected final boolean clientAnalyzer;
	/**
	 * 同一个玩家的登录周期间隔
	 */
	protected final int loginElapse;
	/**
	 * redis主机地址
	 */
	protected final String globalRedis;
	/**
	 * redis密码
	 */
	protected final String globalRedisAuth;
	
	/**
	 * redis主机地址
	 */
	protected final String globalRedisQQ;
	/**
	 * redis密码
	 */
	protected final String globalRedisAuthQQ;
	/**
	 * local redis相关信息
	 */
	protected final String localRedis;
	/**
	 * local redis密码认证
	 */
	protected final String localRedisAuth;
	/**
	 * redis超时时间
	 */
	protected final int redisTimeout;
	/**
	 * redis可用连接实例最大数目
	 */
	protected final int redisMaxActive;
	/**
	 * redis最大空闲连接数
	 */
	protected final int redisMaxIdle;
	/**
	 * redis等待可用连接的最大时间
	 */
	protected final int redisMaxWait;
	/**
	 * 机器人协议标记
	 */
	protected final boolean robotMode;
	/**
	 * 是否开启puid白名单
	 */
	protected final boolean puidCtrl;
	/**
	 * 设备是否需要激活
	 */
	protected final boolean deviceNeedActive;
	/**
	 * 控制是否推送消息
	 */
	protected final boolean pushEnable;
	/**
	 * 腾讯安全SDK开关
	 */
	protected final boolean tssSdkEnable;
	/**
	 * 腾讯安全sdk uic控制开关
	 */
	protected final boolean tssSdkUicEnable;
	/**
	 * 推送tlog日志和实时在线数据开关
	 */
	protected final boolean tlogEnable;
	/**
	 * 安全tlog日志打印开关
	 */
	protected final boolean secTlogPrintEnable;
	/**
	 * 积分上报开关
	 */
	protected final boolean scoreBatchEnable;
	/**
	 * 腾讯安全SDK Proc调用时间间隔
	 */
	protected final int tssInterval;
	/**
	 * 通知用户邮箱地址
	 */
	protected final String alarmEmails;
	/**
	 * 短信通知组字段
	 */
	protected final String smsPhoneNums;
	/**
	 * CDK host
	 */
	protected final String cdkHost;
	/**
	 * 平台区分
	 */
	private final int pfDistinct;
	/**
	 * 单服最大注册人数
	 */
	private final int registerMaxNum;
	/**
	 * 聊天信息发送周期
	 */
	private final long chatMsgTickPeriod;
	/**
	 * 是否开启排队等待机制
	 */
	private final boolean loginWaitQueue;
	/**
	 * 单服最大排队等待人数
	 */
	private final int loginWaitMaxNum;
	/**
	 * 数据库对象同步模拟的异步周期
	 */
	private final int entitySyncPeriod;
	/**
	 * 配置自加载的周期
	 */
	private final int reloadPeriod;
	/**
	 * 登录异步线程
	 */
	private final boolean loginAsync;
	/**
	 * 灰度版本(五段式的版本号结构)
	 */
	protected final String gsVersion;
	/**
	 * 灰度状态
	 */
	protected final int grayState;
	/**
	 * 版本条件(五段式的版本号结构)
	 */
	protected final String condVersion;
	/**
	 * gm渠道
	 */
	protected final String gmChannel;
	
	/**
	 * 使用异步广播行军
	 */
	private final boolean useAsyncBroacastMarch;
	/**
	 * 代理连接超时时间
	 */
	protected final int proxyConnectTimeout;
	/**
	 * 代理连接的空闲时间
	 */
	protected final int proxyIdleTimeout;
	/**
	 * 代理rpc请求的超时时间
	 */
	protected final int proxyRpcTimeout;
	/**
	 * 自动根据代码更新数据库DB
	 */
	protected final boolean useCodeUpdateDb;
	/**
	 * 道具直购订单的有效期
	 */
	protected final int buyItemOrderExpire;
	
	/**
	 * 玩家redis数据过期时间 （单位：s）
	 */
	protected final int playerRedisExpire;
	/**
	 * 预批量创建玩家数据
	 */
	protected final boolean preparePlayerData;
	/**
	 * 玩家数据短暂缓存等级
	 */
	private final int transitoryCacheLevel;
	/**
	 * 短暂缓存时间
	 */
	private final int transitoryCacheTime;
	/**
	 * 异步创建初始化的玩家数据
	 */
	private final boolean asyncInitData;
	/**
	 * 世界点更新到redis周期
	 */
	private final int pointFlushPeriod;
	/**
	 * 世界点存储模式
	 */
	private final int worldPointProxy;
	/**
	 * 是否开启gm
	 */
	private final boolean openGm;
	/**
	 * gm端口
	 */
	private final int gmPort;
	/**
	 * 时戳偏移， 单位秒
	 */
	private final int tsOffset;
	/**
	 * 最大同时预加载进程数
	 */
	private final int maxPreloadProcess;
	/**
	 * 数据同步周期
	 */
	private final int csEntityPeriod;
	/**
	 * 合服之后加载离线多少天的玩家.
	 */
	private final int leaveDay;
	/**
	 * zmq的发送高水位
	 */
	private final int zmqSndHwm;
	/**
	 * zmq的接受高水位
	 */
	private final int zmqRcvHwm;
	/**
	 * zmq唯一标识模式
	 */
	private final int zmqIdentifyMode;
	/**
	 * zonineApp的请求地址（发送性能分析数据的目的地址）
	 */
	private final String zonineAddr;
	/**
	 * 是否接zonineSDK
	 */
	private final boolean zonineEnable;
	/**
	 * 同时批量最大服务器数
	 */
	private final int maxBatchServerCount;
	/**
	 * 超时统计的监控数据key
	 */
	private final String timeoutStatKey;
	/**
	 * 超时统计的监控数据存储过期时间：单位秒
	 */
	private final int statExpireTime;
	/**
	 * 调节db risk check 配置秒吧方便计算 300_1200
	 */
	private final String adaptDbRiskTime;
	/**
	 * 新包客户端patch的指定版本号
	 */
	private final String newPackVersion;

	/**
	 * 
	 */
	private final boolean checkPlayerVisitTime;

	/**
	 * 高配
	 */
	private final String goodServers;
	
	/**
	 * 迁服以后禁止登录时间
	 */
	private final int immgrationBanLogin;
	/**
	 * wx服的areaId
	 */
	private final String wxAreaIds;
	private final int qqAreaId;
	private final String qqServers;
	/**
	 * 大区互通时间
	 */
	private final String areaInterflowTime;
	/**
	 * 心悦角色交易相关idip接口返回信息编码开关
	 */
	private final boolean xinyueRoleEncode;
	/**
	 *  是否将异常信息推送到飞书
	 */
	private final boolean reportException2Feishu;
	/**
	 * 飞书机器人推送链接
	 */
	private final String targetQAUrl;
	private final String targetCehuaUrl;
	
	// 异常过滤器
	private final String exceptionFilter;
	private List<String> exceptionFilterList;
	// 指定服务器
	private final String specialServer;
	private Map<String, String> specialServerMap;
	// 区服所属同学的userid-区服对应关系
	private final String ownerIdServers;
	private Map<String, String> serverOnwerIdMap = new HashMap<String, String>();
	private Map<String, String> serverOnwerNameMap = new HashMap<String, String>();
	
	private final String serverSvnAddrs;
	private Map<String, String> serverSvnAddrMap = new HashMap<String, String>();
	private final String svnAuthorInfo;
	private Map<String, String> svnAuthorKeyIdMap = new HashMap<String, String>();
	private Map<String, String> svnAuthorKeyNameMap = new HashMap<String, String>();
	private final String svnAuth;
	private final String svnRevisionGap;
	private int[] svnRevisionGapArray = new int[2];
	
	/**
	 * 通过java启动参数设置的serverId，此id优先于svr.cfg中的serverId
	 */
	private static String argServerId;
	
	/**
	 * zonine数据上报地址列表
	 */
	private List<String> zonineAddrList = new ArrayList<String>();
	/**
	 * 邮件发送目标
	 */
	private List<String> emailAddrList = new LinkedList<String>();
	
	/**
	 * 短信通知组号码表
	 */
	private ArrayList<String> smsPhoneList = new ArrayList<String>();
	private List<String> timeoutStatKeyList = new ArrayList<>();
	/**
	 * {@link #adaptDbRiskTime}
	 */
	private List<Integer> adaptDbRiskTimeList = new ArrayList<>();

	private List<Integer> goodServerList = new ArrayList<>();
	private List<Integer> wxAreaIdList = new ArrayList<>();
	private List<String> qqServerList = new ArrayList<>();
	private long areaInterflowTimeValue;
	
	/**
	 * 全局静态对象
	 */
	protected static GsConfig instance = null;

	/**
	 * 获取全局静态对象
	 *
	 * @return
	 */
	public static GsConfig getInstance() {
		return instance;
	}

	/**
	 * 获取参数的serverId
	 * 
	 * @param serverId
	 */
	public static void setArgServerId(String serverId) {
		argServerId = serverId;
	}

	public GsConfig() {
		protocolSecure = true;
		robotMode = false;
		localRedis = null;
		localRedisAuth = null;
		cacheInitSize = 0;
		cacheMaxSize = 0;
		cacheExpireTime = 0;
		lossCacheDays = 15;
		lossCacheTime = 600000;
		playerCacheTime = 0;
		orderExpire = 0;
		authCheckLevel = 0;
		puidCtrl = false;
		gzPlayerData = true;
		sessionToken = false;
		protocolOrder = true;
		protocolCompressSize = 2048;
		deviceNeedActive = false;
		globalRedis = null;
		globalRedisAuth = null;
		globalRedisQQ = null;
		globalRedisAuthQQ = null;
		redisTimeout = 3000;
		redisMaxActive = 32;
		redisMaxIdle = 8;
		redisMaxWait = 500;
		loginElapse = 5000;
		binarySnapshot = true;
		alarmEmails = null;
		smsPhoneNums = null;
		cdkHost = null;
		pushEnable = false;
		tssSdkEnable = false;
		tssSdkUicEnable = false;
		tlogEnable = false;
		tssInterval = 0;
		pfDistinct = 1;
		registerMaxNum = 0;
		chatMsgTickPeriod = 0;
		loginWaitQueue = false;
		loginWaitMaxNum = 0;
		entitySyncPeriod = 0;
		reloadPeriod = 0;
		gsVersion = "0.0.0.0.0";
		grayState = 0;
		condVersion = null;
		clientAnalyzer = false;
		loginAsync = true;
		gmChannel = "hawk";
		migrateExpireTime = 0;
		useAsyncBroacastMarch = true;
		proxyConnectTimeout = 2000;
		proxyIdleTimeout = 300000;
		proxyRpcTimeout = 10000;
		useCodeUpdateDb = false;
		buyItemOrderExpire = 60;
		playerRedisExpire = 2592000;
		secTlogPrintEnable = false;
		scoreBatchEnable = false;
		preparePlayerData = true;
		asyncInitData = true;
		transitoryCacheLevel = 0;
		transitoryCacheTime = 0;
		pointFlushPeriod = 60000;
		worldPointProxy = 1;
		openGm = false;
		gmPort = 0;
		tsOffset = 0;
		maxPreloadProcess = 0;
		csEntityPeriod = 3000;
		leaveDay = 15;
		zmqSndHwm = 20480;
		zmqRcvHwm = 20480;
		zmqIdentifyMode = 0;
		zonineAddr = "";
		zonineEnable = false;
		maxBatchServerCount = 100;
		timeoutStatKey = "protoTimeoutStat,msgTimeoutStat,dbTimeoutStat";
		statExpireTime = 604800;
		adaptDbRiskTime = "300_1200";
		newPackVersion = "";
		checkPlayerVisitTime = true;
		goodServers = "10001,10002,10005,10009,10011,10022,10024,10026,10032,10036,10043,10045,10047,10072,10124,10140,10160,10166,10183,10222,10241,10267,10300,10326,10337,10347,10362,10401,10411,10441,10448,10459,10494,10512,20001,20006,20008,20018,20021,20032,20080,20107,20119,20124,20159,20171,20186,20207,20215,20225,20241,20255";
		immgrationBanLogin = 300;
		wxAreaIds = "1"; 
		qqAreaId = 2;
		qqServers = "";
		areaInterflowTime = "2024-04-11 06:00:00";
		xinyueRoleEncode = true; 
		reportException2Feishu = false;
		
		targetCehuaUrl = "https://open.feishu.cn/open-apis/bot/v2/hook/45ebd8ea-ae33-4a0e-8e65-dc1be1b16773";
		targetQAUrl = "https://open.feishu.cn/open-apis/bot/v2/hook/bc353d70-5a05-486f-834c-1d688d2ca163";
		exceptionFilter = "";
		specialServer = "50001-trunk服,50002-debug服,50003-milestone服,50009-策划服";
		ownerIdServers = "ou_06f9ce75ed1ec0b3d5fa61c3870d059d,马洪滨,80001,80004;ou_99a6f25f55fcca3dd0ef8055f52e57d7,裴敏,80027,80026,80025,80013,80010,80003,80038,80039;ou_80becff92e6905cf670ec3143f47bf54,刘嘉文,80022,80023,80046;ou_652820c2e5ce9979feb57f0c9b86b2d5,李梅梅,80037,80036,80033,80032,80051,80050,80049,80048;ou_29e4508459c49d29c8d1be133c2dc6ba,韩金龙,80030,80029,80012,80028,80014,80011";
		serverSvnAddrs = "50001-svn://192.168.50.81/redalert/Resource_Product/trunk/0.配置表/2.后台/,"
				+ "50002-svn://192.168.50.81/redalert/Resource_Product/debug/all_file/server_file/,"
				+ "50003-svn://192.168.50.81/redalert/Resource_Product/milestone/1.4.108/0.配置表/2.后台/,"
				+ "debug-svn://192.168.50.81/redalert/Code_Server/debug/,"
				+ "50009-svn://192.168.50.81/redalert/Resource_Product/trunk/0.配置表/2.后台/";
		svnAuthorInfo = "yutao,余焘,ou_f863c98d496910e4db0537b681a9e7f3;zhaoxiong,赵雄,ou_9d5f24dbd6a24bafe64c0c2511b4caeb;qinkun,秦坤,ou_e797d71f3ac074b259b73d18df4e8767;yinzhenqiang,尹振强,ou_23c13e33f3fc3357111d649a4ff19501;weishunbai,韦舜柏,ou_e323151b14efdcf393b44fdc100e338e;zhouyuyang,周宇洋,ou_f8e07dae238d49b99283125b354dd13f";
		svnAuth = "lijialiang:hg61lbzkxw";
		svnRevisionGap = "2000_10000";
	}
	
	public boolean isXinyueRoleEncode() {
		return xinyueRoleEncode;
	}

	public int[] getSvnRevisionGapArray() {
		return svnRevisionGapArray;
	}
	
	public boolean isReportException2Feishu() {
		return reportException2Feishu;
	}
	
	public String getTargetCehuaUrl() {
		return targetCehuaUrl;
	}
	
	public String getTargetQAUrl() {
		return targetQAUrl;
	}

	public boolean isScoreBatchEnable() {
		return scoreBatchEnable;
	}

	public boolean isPreparePlayerData() {
		return preparePlayerData;
	}

	public boolean isAsyncInitData() {
		return asyncInitData;
	}

	public boolean isSecTlogPrintEnable() {
		return secTlogPrintEnable;
	}

	public int getPlayerRedisExpire() {
		return playerRedisExpire;
	}

	public int getBuyItemOrderExpire() {
		return buyItemOrderExpire;
	}

	public boolean isTssSdkUicEnable() {
		return tssSdkUicEnable;
	}

	public boolean isProtocolSecure() {
		return protocolSecure;
	}

	public boolean isTlogEnable() {
		return tlogEnable;
	}

	public int getCacheInitSize() {
		return cacheInitSize;
	}

	public int getCacheMaxSize() {
		return cacheMaxSize;
	}

	public int getCacheExpireTime() {
		return cacheExpireTime;
	}

	public int getLossCacheDays() {
		return lossCacheDays;
	}

	public int getLossCacheTime() {
		return lossCacheTime;
	}

	public int getPlayerCacheTime() {
		return playerCacheTime;
	}

	public int getOrderExpire() {
		return orderExpire;
	}

	public int getAuthCheckLevel() {
		return authCheckLevel;
	}

	public boolean isGzPlayerData() {
		return gzPlayerData;
	}

	public boolean isSessionToken() {
		return sessionToken;
	}

	public boolean isProtocolOrder() {
		return protocolOrder;
	}

	public int getProtocolCompressSize() {
		return protocolCompressSize;
	}

	public boolean isBinarySnapshot() {
		return binarySnapshot;
	}

	public int getLoginElapse() {
		return loginElapse;
	}

	public String getGlobalRedis() {
		return globalRedis;
	}

	public String getGlobalRedisAuth() {
		return globalRedisAuth;
	}
	
	public String getGlobalRedisQQ() {
		return globalRedisQQ;
	}

	public String getGlobalRedisAuthQQ() {
		return globalRedisAuthQQ;
	}

	public boolean isClientAnalyzer() {
		return clientAnalyzer;
	}

	public String getLocalRedis() {
		return localRedis;
	}

	public String getLocalRedisAuth() {
		return localRedisAuth;
	}

	public int getRedisTimeout() {
		return redisTimeout;
	}

	public int getRedisMaxActive() {
		return redisMaxActive;
	}

	public int getRedisMaxIdle() {
		return redisMaxIdle;
	}

	public int getRedisMaxWait() {
		return redisMaxWait;
	}

	public boolean isRobotMode() {
		return robotMode;
	}

	public boolean isPuidCtrl() {
		return puidCtrl;
	}

	public boolean isDeviceNeedActive() {
		return deviceNeedActive;
	}

	public boolean isPushEnable() {
		return pushEnable;
	}

	public boolean isTssSdkEnable() {
		return tssSdkEnable;
	}

	public int getTssInterval() {
		return tssInterval;
	}

	public List<String> getAlarmEmails() {
		return emailAddrList;
	}

	public boolean isLoginAsync() {
		return loginAsync;
	}

	public ArrayList<String> getSmsPhoneList() {
		return smsPhoneList;
	}
	
	public String getCdkHost() {
		return cdkHost;
	}

	public int getPfDistinct() {
		return pfDistinct;
	}

	public int getRegisterMaxNum() {
		return registerMaxNum;
	}

	public long getChatMsgTickPeriod() {
		return chatMsgTickPeriod;
	}

	public boolean isLoginWaitQueue() {
		return loginWaitQueue;
	}

	public int getLoginWaitMaxNum() {
		return loginWaitMaxNum;
	}
	
	public int getEntitySyncPeriod() {
		return entitySyncPeriod;
	}

	public int getReloadPeriod() {
		return reloadPeriod;
	}

	public String getGsVersion() {
		return gsVersion;
	}

	public int getGrayState() {
		return grayState;
	}

	public String getCondVersion() {
		return condVersion;
	}
	
	public String getGmChannel() {
		return gmChannel;
	}

	public boolean isUseAsyncBroacastMarch() {
		return useAsyncBroacastMarch;
	}
	
	public int getProxyConnectTimeout() {
		return proxyConnectTimeout;
	}

	public int getProxyIdleTimeout() {
		return proxyIdleTimeout;
	}

	public int getProxyRpcTimeout() {
		return proxyRpcTimeout;
	}

	public int getMigrateExpireTime() {
		return migrateExpireTime;
	}
	
	public int getTransitoryCacheLevel() {
		return transitoryCacheLevel;
	}

	public int getTransitoryCacheTime() {
		return transitoryCacheTime;
	}

	public boolean isUseCodeUpdateDb() {
		return useCodeUpdateDb;
	}

	public int getPointFlushPeriod() {
		return pointFlushPeriod;
	}

	public int getWorldPointProxy() {
		return worldPointProxy;
	}
	
	public boolean isOpenGm() {
		return openGm;
	}

	public int getGmPort() {
		return gmPort;
	}

	public int getTsOffset() {
		return tsOffset;
	}

	public int getMaxPreloadProcess() {
		return maxPreloadProcess;
	}

	public int getCsEntityPeriod() {
		return csEntityPeriod;
	}

	public long getLeavDayTime() {
		return this.leaveDay * HawkTime.DAY_MILLI_SECONDS;
	}
	
	public int getZmqSndHwm() {
		return zmqSndHwm;
	}

	public int getZmqRcvHwm() {
		return zmqRcvHwm;
	}

	public int getZmqIdentifyMode() {
		return zmqIdentifyMode;
	}

	@Override
	public String getServerId() {
		if (!HawkOSOperator.isEmptyString(argServerId)) {
			return argServerId;
		}

		return super.getServerId();
	}
	
	@Override
	protected boolean assemble() {
		if(HawkOSOperator.isEmptyString(areaId)) {
			return false;
		}
		
		if (!HawkOSOperator.isEmptyString(smsPhoneNums)) {
			String[] strs = smsPhoneNums.split(",");
			for (String str : strs) {
				if (!smsPhoneList.contains(str.trim())) {
					smsPhoneList.add(str.trim());
				}
			}
		}
		
		if (!HawkOSOperator.isEmptyString(alarmEmails)) {
			String[] emailArray = alarmEmails.split(",");
			for (String emailAddr : emailArray) {
				emailAddrList.add(emailAddr.trim());
			}
		}
		if (!HawkOSOperator.isEmptyString(timeoutStatKey)) {
			String[] statKeyArray = timeoutStatKey.split(",");
			for (String statKey : statKeyArray) {
				timeoutStatKeyList.add(statKey.trim());
			}
		}
		if (!HawkOSOperator.isEmptyString(adaptDbRiskTime)) {
			String[] adaptDbRiskTimeArray = adaptDbRiskTime.split("_");
			for (String str : adaptDbRiskTimeArray) {
				adaptDbRiskTimeList.add(Integer.parseInt(str));
			}
		}
		
		if (!HawkOSOperator.isEmptyString(zonineAddr)) {
			String[] addrArray = zonineAddr.split(",");
			for (String str : addrArray) {
				zonineAddrList.add(str.trim());
			}
		}
		
		if (!HawkOSOperator.isEmptyString(gsVersion) && !checkVersionValid()) {
			HawkLog.errPrintln("check version params invalid");
			return false;
		}

		if (!HawkOSOperator.isEmptyString(goodServers)){
			String[] goodServerArray = goodServers.split(",");
			for (String str : goodServerArray) {
				goodServerList.add(Integer.parseInt(str));
			}
		}
		
		wxAreaIdList = SerializeHelper.stringToList(Integer.class, wxAreaIds, ",");
		qqServerList = SerializeHelper.stringToList(String.class, qqServers, ",");
		areaInterflowTimeValue = HawkOSOperator.isEmptyString(areaInterflowTime) ? 0 : HawkTime.parseTime(areaInterflowTime);
		
		exceptionFilterList = SerializeHelper.stringToList(String.class, exceptionFilter, ",");
		specialServerMap = SerializeHelper.stringToMap(specialServer, String.class, String.class, "-", ",");
		serverSvnAddrMap = SerializeHelper.stringToMap(serverSvnAddrs, String.class, String.class, "-", ",");
		
		String[] array = ownerIdServers.split(";");
		for (String info : array) {
			String[] ownerIdServerArray = info.split(",");
			for (int i = 2; i < ownerIdServerArray.length; i++) {
				serverOnwerIdMap.put(ownerIdServerArray[i], ownerIdServerArray[0]);
				serverOnwerNameMap.put(ownerIdServerArray[i], ownerIdServerArray[1]);
			}
		}
		
		String[] svnInfoArray = svnAuthorInfo.split(";");
		for (String info : svnInfoArray) {
			String[] svnAuthorInfoArr = info.split(",");
			svnAuthorKeyNameMap.put(svnAuthorInfoArr[0], svnAuthorInfoArr[1]);
			svnAuthorKeyIdMap.put(svnAuthorInfoArr[0], svnAuthorInfoArr[2]);
		}
		
		String[] revisionGapArr = svnRevisionGap.split("_");
		if (revisionGapArr.length >= 2) {
			svnRevisionGapArray[0] = Integer.parseInt(revisionGapArr[0]);
			svnRevisionGapArray[1] = Integer.parseInt(revisionGapArr[1]);
		} else {
			svnRevisionGapArray[0] = 2000;
			svnRevisionGapArray[1] = 10000;
		}
		
		instance = this;
		return super.assemble();
	}
	
	public List<Integer> getWxAreaIdList() {
		return wxAreaIdList;
	}
	
	public int getQQAreaId() {
		return qqAreaId;
	}
	
	public List<String> getQQServerList() {
		return qqServerList;
	}
	
	public long getAreaInterflowTimeValue() {
		return areaInterflowTimeValue;
	}
	
	/**
	 * 检测版本号信息
	 * @return
	 */
	private boolean checkVersionValid() {
		try {
			String[] gsVersionSplit = gsVersion.split("\\.");
			boolean needCheck = false;
			int first = 0, second = 0, third = 0;
			if (gsVersionSplit.length == 5) {
				first = Integer.parseInt(gsVersionSplit[0]);
				second = Integer.parseInt(gsVersionSplit[1]); 
				third = Integer.parseInt(gsVersionSplit[2]);
				int fourth = Integer.parseInt(gsVersionSplit[3]);
				int fifth = Integer.parseInt(gsVersionSplit[4]);
				// 大版本更新
				needCheck = first > 0 && second > 0 && third > 0 && fourth == 0 && fifth == 0;
			}
			
			if (!needCheck) {
				return true;
			}
			
			if (HawkOSOperator.isEmptyString(newPackVersion)) {
				return false;
			}
			
			String[] versionSplit = newPackVersion.split("\\.");
			if (versionSplit.length != 4) {
				return false;
			}
			
			int versionFirst = Integer.parseInt(versionSplit[0]);
			int versionSecond = Integer.parseInt(versionSplit[1]); 
			int versionThird = Integer.parseInt(versionSplit[2]);
			int versionFourth = Integer.parseInt(versionSplit[3]);
			// 大版本号不一致，或 patchNum小于 0
			if (first != versionFirst || second != versionSecond || third != versionThird || versionFourth < 0) {
				return false;
			}
			
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
	}
	
	public Map<String, String> specialServerMap() {
		return specialServerMap;
	}
	
	public List<String> getExceptionFilterList() {
		return exceptionFilterList;
	}
	
	public boolean isSpecialServer() {
		return specialServerMap.containsKey(this.getServerId());
	}
	
	public String getServerName() {
		return specialServerMap.getOrDefault(this.getServerId(), "");
	}
	
	public String getSvnAddr() {
		return serverSvnAddrMap.getOrDefault(this.getServerId(), "");
	}
	
	public String getDebugSvrSvnAddr() {
		return serverSvnAddrMap.getOrDefault("debug", "");
	}
	
	public String getSvnAuthorName(String key) {
		return svnAuthorKeyNameMap.get(key);
	}
	
	public String getSvnAuthorId(String key) {
		return svnAuthorKeyIdMap.get(key);
	}

	public String getZonineAddr() {
		return zonineAddr;
	}
	
	public List<String> getZonineAddrList() {
		return zonineAddrList;
	}

	public boolean isZonineEnable() {
		return zonineEnable;
	}

	public int getMaxBatchServerCount() {
		return maxBatchServerCount;
	}
	public String getTimeoutStatKey() {
		return timeoutStatKey;
	}
	
	public List<String> getTimeoutStatKeyList() {
		return timeoutStatKeyList;
	}
	
	public int getStatExpireTime() {
		return statExpireTime;
	}

	public List<Integer> getAdaptDbRiskTimeList() {
		return adaptDbRiskTimeList;
	}
	
	public String getNewPackVersion() {
		return newPackVersion;
	}

	public boolean isCheckPlayerVisitTime() {
		return checkPlayerVisitTime;
	}

	public List<Integer> getGoodServerList() {
		return goodServerList;
	}

	public int getImmgrationBanLogin() {
		return Math.max(immgrationBanLogin, 1800);
	}
	
	public String getServerOwnerId() {
		return serverOnwerIdMap.getOrDefault(serverId, "");
	}
	
	public String getServerOwnerName() {
		return serverOnwerNameMap.getOrDefault(serverId, "");
	}

	public String getSvnAuth() {
		return svnAuth == null ? "" : svnAuth;
	}
}
