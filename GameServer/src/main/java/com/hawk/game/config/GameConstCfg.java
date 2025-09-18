package com.hawk.game.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import com.hawk.game.GsConfig;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.world.object.Point;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 系统基础配置
 *
 * @author hawk
 *
 */
@HawkConfigManager.KVResource(file = "cfg/gameConst.cfg")
public class GameConstCfg extends HawkConfigBase {
	/**
	 * 默认名字前缀
	 */
	protected final String namePrefix;
	/**
	 * 默认头像
	 */
	protected final int defaultIcon;
	/**
	 * 默认的平台头像
	 */
	protected final String defaultPfIcon;
	/**
	 * 聊天文字长度
	 */
	protected final int chatMsgMaxLen;
	/**
	 * 玩家视野X半径
	 */
	protected final int viewXRadius;
	/**
	 * 玩家视野Y半径
	 */
	protected final int viewYRadius;
	/**
	 * 世界同步搜索半径
	 */
	protected final int syncSearchRadius;
	/**
	 * 移动速率同步因子
	 */
	protected final float moveSyncFactor;
	/**
	 * 地图区块刷新周期
	 */
	protected final int areaUpdatePeriod;
	/**
	 * 地图怪和资源重刷的延迟时间, 避免开服的拥堵
	 */
	protected final int areaUpdateDelayPeriod;
	/**
	 * 活动检测的更新周期
	 */
	protected final int activityPeriod;

	/**
	 * 领地生效，领地切换更新周期
	 */
	protected final int manorEffectPeriod;

	/**
	 * 释放指挥官的更新周期
	 */
	protected final int commanderRelaeasePeriod;

	/**
	 * 联盟tick周期
	 */
	protected final int guildTickPeriod;

	/**
	 * 新手专属数据的有效期
	 */
	protected final int newlyPeriod;
	/**
	 * 一次最大增加道具数目（已废弃，改成配置在const.xml表中，由策划去维护）
	 */
	protected final int maxAddItemNum;
	/**
	 * 人民币充值渠道
	 */
	protected final String usdChannels;
	/**
	 * 全服系统邮件过期时间（天）
	 */
	protected final long systemMailExpire;
	/**
	 * 默认出生地
	 */
	protected final String defaultBornPos;
	/**
	 * 默认加入联盟
	 */
	protected final String defaultGuildId;
	/**
	 * 默认出生地
	 */
	protected final String randMinMaxPt;
	/**
	 * 警报检测周期
	 */
	protected final int alarmPeriod;

	/**
	 * 战斗数据缓存有效期
	 */
	protected final long battleCachePeriod;

	/**
	 * 读取邮件间隔(毫秒)
	 */
	private final int readMailInterval;

	/**
	 * 快照更新周期(毫秒)
	 */
	private final int snapshotPeriod;

	/**
	 * 快照更新延迟时间(毫秒)
	 */
	private final int snapshotDelay;
	
	/**
	 * 快照每一批次更新数量
	 */
	private final int snapshotBatchCnt;

	/**
	 * 聊天缓存更新周期
	 */
	private final int chatUpdatePeriod;

	/**
	 * 刷新redis中聊天信息的缓存
	 */
	private final int chatCacheTime;

	/**
	 * 检查全服邮件频率(毫秒)
	 */
	private final int checkGlobalMailTime;

	/**
	 * 日志记录在线玩家频率(毫秒)
	 */
	private final int showActivePlayerPeriod;

	/**
	 * 日志记录玩家cache状态频率(毫秒)
	 */
	private final int showPlayerCachePeriod;

	/**
	 * 全服调查问卷推送过期检测周期(毫秒)
	 */
	private final int checkQuestionnaireTime;

	/**
	 * 全服问卷读取间隔(毫秒)
	 */
	private final int questionnaireInterval;

	/**
	 * 是否关闭pve副本结果服务器校验
	 */
	private final int closePveResultServerCheck;

	/**
	 * 客户端日志记录包体限制(字节)
	 */
	private final int clientLogRecordSizeLimit;

	/**
	 * 尤里复仇活动数据缓存有效期(毫秒)
	 */
	private final long yuriRevengeCachePeriod;
	/**
	 * 榜单封禁检测周期(毫秒)
	 */
	private final int checkRankBanPeriod;
	
	/**
	 * 战力排行更新存储周期(ms)
	 */
	private final int updatePowerRankPeriod;
	
	/**
	 * 可重复使用CDK类型
	 */
	private final String cdkType;

	/**
	 * 联盟战争储存条数上限
	 */
	private final int recordCount;

	/**
	 * 机器人模式下行军数量上限
	 */
	private final int marchCountLimit;
	
	/**
	 * 机器人模式下加vip经验的大本等级阶梯
	 */
	private final String cityLevelStep;

	/**
	 * 邮件缓存天
	 */
	private final int mailExpireSecond;
	
	/**
	 * 超级武器信息缓存天
	 */
	private final int superWeaponExpireSecond;
	
	/**
	 * 最大邮件数
	 */
	private final int mailMaxCount;
	/**
	 * 单次清理预留空间
	 */
	private final int mailClearOnce;
	
	/**
	 * 聊天频率秒
	 */
	private final int chatTimeInterval;

	/**
	 * 客户端事件上报size限制
	 */
	private final int clientEventSizeLimit;
	
	/**
	 * 问卷调查回调开启
	 */
	private final int surveyNotify;
	/**
	 * 推送开关关闭值
	 */
	private final int pushSwitchCloseValue;
	/**
	 * 调用支付相关接口返回表示登录校验失败的错误码
	 */
	private final int tokenExpiredCode;
	/**
	 * 是否立即发送聊天消息.(不合并发送, 会影响双端效率)
	 */
	private final int sendChatNow;
	
	/**
	 * 是否记录战斗日志
	 */
	private final int keepBattleLog;
	/**
	 * 自定义数据存储长度限制
	 */
	private final int customDataLimit;
	
	/**
	 * 安全idip接口货币单次添加上限
	 */
	private final int moneyCntAddLimit;
	
	/**
	 * 安全idip接口普通资源单次添加上限
	 */
	private final int resCntAddLimit;
	
	/**
	 * 战力变更活动事件延迟投递开启条件(同时在线人数超过设定值时进行延迟投递)
	 */
	private final long powerChangeEventPostDelayCondition;

	/**
	 * 战力变更活动事件投递频率(ms)
	 */
	private final long powerChangeEventPostPeriod;
	
	/**
	 * 是否开启国王战(机器人模式下生效）
	 */
	private final boolean openPresidentFight;
	
	/**
	 * 起服时开启玩家部队检测
	 */
	private final boolean openArmyCheck;
	
	/**
	 * 野怪刷新延迟时间(20ms)
	 */
	private final long monsterRefreshDelay;
	
	/**
	 * 资源刷新延迟时间(1500ms)
	 */
	private final long resourceRefreshDelay;
	
	/**
	 * 据点刷新延迟时间(1500ms)
	 */
	private final long strongpointRefreshDelay;
	
	/**
	 * 是否debug控制世界资源刷新 
	 */
	private final boolean debugControlWorldRefresh;
	/**
	 * 是否开启活动野怪
	 */
	private final boolean openMonsterActivity;
	
	/**
	 * 是否开启据点活动
	 */
	private final boolean openStrongpointActivity;
	
	/**
	 * qq积分上报，上报战力变化的大本起始等级
	 */
	private final int powerScoreBatchLv;
	/**
	 * 手q积分上报最大缓存任务数
	 */
	private final int scoreBatchMaxWait;
	
	/**
	 * 在线标识存储过期时间: 单位s 
	 */
	private final int onlineFlagExpire;
	/**
	 * 在线标识tick时间：单位ms
	 */
	private final int onlineFlagTickPeriod;
	/**
	 * army修正因子
	 */
	private final int armyFixFactor;
	/**
	 * 被攻击计算时间
	 */
	private final long beAttackedCalcTime;
	/**
	 * 是否需要记录跨服的entityLog
	 */
	private final boolean crossEntityLog;
	
	/**
	 * 年兽优化
	 */
	private final  boolean nianOptimize;
	/**
	 * 跨服缓存过期时间.
	 */
	private final int crossCacheExpireTime;	
	/**
	 * 拆服补偿道具
	 */
	private final String separateGuardItem;
	
	/**
	 * 默认的出生中心点
	 */
	private Point defaultBornPoint = null;
	
	/**
	 * 随机城点最小位置
	 */
	private Point randMinPt = null;
	/**
	 * 随机城点最大位置
	 */
	private Point randMaxPt = null;
	/**
	 * 平台图表列表
	 */
	private List<String> pfIconList = new LinkedList<String>();
	
	/**
	 * 可重复使用的cdk类型
	 */
	private static Map<String, String> cdkTypes = new HashMap<String, String>();
	/**
	 * 充值渠道表
	 */
	private static Map<String, String> usdChannelMap = new HashMap<String, String>();
	
	/**
	 * 机器人模式下添加vip经验的大本等级阶梯
	 */
	private static Map<Integer, Integer> cityLevelVipExpMap;
	/**
	 * 好友申请过期时间
	 */
	private  final int  relaitonApplyExpireTime;
	/**
	 * 退出等待最小时间
	 */
	private final int exitCrossMinWaitTime;
	/**
	 * 退出等待最大时间
	 */
	private final int exitCrossMaxWaitTime;
	/**
	 * 检测周期毫秒,秒的差距太大了.
	 */
	private final int exitCrossPeriodTime;
	
	// 超级武器时间控制，只有debug robot模式下启用
	private final long superBarrackPaceTime;
	private final long superBarrackSignTime;
	private final long superBarrackWarfreTime;
	private final long superBarrackControlTime;
	private final long superBarrackOccupyTime;
	
	/**
	 * 跨服使用pipeline的单次数据长度
	 */
	private final int crossDataPipelineMaxLength;
	
	/**
	 * 服务器是否活着误差时间.
	 */
	private final int serverActiveFixTime;
	
	/**
	 * 跨服协议的有效时间.  单位秒.
	 */
	private final int crossProtocolValidTime;
	/**
	 *	游戏内领完微券后等待多少毫秒后再请求微券信息 
	 */
	private final long sendCouponWaitMs;
	/**
	 * 是否是apple登录审核环境
	 */
	private final boolean appleLoginVerifyEnv;
	/**
	 * 针对特定情况的测试环境
	 */
	private final boolean testEnv;
	
	// 机甲优化功能用到的自动解锁机甲ID、机甲引导customeKey等参数
	private final int superSoldierId;
	private final String superSoldierTutorialKey;
	/**
	 * 拉取指定玩家的信息
	 */
	private final String motherPlayerIds;
	private List<String> motherPlayerIdList;
	private final String motherPlayer;
	private final String sonPlayerIds;
	private List<String> sonPlayerIdList;
	
	private final boolean playerCopySkip;
	
	private final String removePlayerIds;
	private List<String> removePlayerIdList;
	
	//母号的一些基本信息：包括openid,角色名字,角色id,渠道,平台,区服id
	private final String motherRoleInfo;
	
	// 用于记录协议打点日志耗时的玩家
	private final String secOperSpecialPlayerIds;
	private List<String> secOperSpecialPlayerIdList;
	
	/**
	 * 军魂传承活动测试
	 */
	private final String inheritTestServers;
	private List<String> inheritTestServerList;
	
	/**
	 * 问卷推送白名单服务器
	 */
	private final String pushSurveyGrayServers;
	
	// 检测过期城点周期(s)
	private final int checkTimeOutCityPeroid;
	// 检测过期城点起始时间
	private final int checkTimeOutCityHourA;
	// 检测过期城点结束时间
	private final int checkTimeOutCityHourB;
	// 检测过期城点数量
	private final int checkTimeOutCityCount;
	// 联盟盟主检测
	private final boolean guildLeaderCheck;
	/**
	 * 是否拉取新的荣耀赛季数据
	 */
	private final boolean seasonHonorNew;
	/**
	 * 道具直购请求areaId传参控制开关
	 */
	private final int payItemsAreaSwith;
	
	private List<String> pushSurveyGrayServerList;
	
	// ip转换参数
	private final String ipConvertParams;
	private int ip_convert_modid;
	private int ip_convert_cmdid;
	private String ip_convert_suburl;
	private int ip_convert_appid;
	private String ip_convert_token;
	private String ip_convert_echo;
	private String ip_convert_randstr;
	private int ip_convert_command;
	
	
	/**
	 * 全局静态对象
	 */
	private static GameConstCfg instance = null;

	/**
	 * 获取全局静态对象
	 *
	 * @return
	 */
	public static GameConstCfg getInstance() {
		return instance;
	}

	public GameConstCfg() {
		namePrefix = "";
		defaultIcon = 10;
		defaultPfIcon = null;
		chatMsgMaxLen = 0;
		viewXRadius = 0;
		viewYRadius = 0;
		syncSearchRadius = 0;
		moveSyncFactor = 1.0f;
		areaUpdatePeriod = 0;
		areaUpdateDelayPeriod = 0;
		activityPeriod = 10000;
		newlyPeriod = 3600000;
		maxAddItemNum = 0;
		usdChannels = null;
		systemMailExpire = 0;
		defaultBornPos = null;
		defaultGuildId = null;
		randMinMaxPt = null;
		alarmPeriod = 1000;
		readMailInterval = 5000;
		manorEffectPeriod = 5000;
		commanderRelaeasePeriod = 5000;
		guildTickPeriod = 5000;
		snapshotPeriod = 5000;
		snapshotDelay = 60000;
		snapshotBatchCnt = 1;
		chatUpdatePeriod = 1800000;
		chatCacheTime = 1800000;
		checkGlobalMailTime = 60000;
		showActivePlayerPeriod = 10000;
		showPlayerCachePeriod = 300000;
		cdkType = null;
		battleCachePeriod = 604800000;
		checkQuestionnaireTime = 60000;
		questionnaireInterval = 10000;
		closePveResultServerCheck = 0;
		clientLogRecordSizeLimit = 204800;
		checkRankBanPeriod = 5000;
		updatePowerRankPeriod = 60000;
		yuriRevengeCachePeriod = 1296000000;
		recordCount = 50;
		marchCountLimit = 5000;
		mailExpireSecond = 1296000;
		mailMaxCount = 999;
		mailClearOnce = 200;
		chatTimeInterval = 2;
		clientEventSizeLimit = 0;
		surveyNotify = 0;
		pushSwitchCloseValue = -1;
		tokenExpiredCode = 1018;
		sendChatNow = 0;
		keepBattleLog = 0;
		customDataLimit = 1024;
		moneyCntAddLimit = 5000;
		resCntAddLimit = 100000;
		cityLevelStep = "";
		powerChangeEventPostDelayCondition = 100000;
		powerChangeEventPostPeriod = 0;
		relaitonApplyExpireTime = 172000;
		openPresidentFight = false;
		openArmyCheck = true;
		superBarrackPaceTime = 300 * 1000L;
		superBarrackSignTime = 300 * 1000L;
		superBarrackWarfreTime = 300 * 1000L;
		superBarrackControlTime = 300 * 1000L;
		superBarrackOccupyTime = 300 * 1000L;
		monsterRefreshDelay = 20L;
		resourceRefreshDelay = 1500L;
		strongpointRefreshDelay = 1500L;
		openMonsterActivity = false;
		openStrongpointActivity = false;
		debugControlWorldRefresh = false;
		superWeaponExpireSecond = 1296000;
		powerScoreBatchLv = 5;
		scoreBatchMaxWait = 30000;
		onlineFlagExpire = 60;
		onlineFlagTickPeriod = 25000;
		armyFixFactor = 0;
		exitCrossPeriodTime = 300;
		exitCrossMinWaitTime = 3000;
		exitCrossMaxWaitTime = 10000;
		beAttackedCalcTime = 2000L;
		crossEntityLog = true;
		serverActiveFixTime = 3000;
		crossDataPipelineMaxLength = 3000;
		crossProtocolValidTime = 10;
		nianOptimize = false;
		sendCouponWaitMs = 1000L;
		crossCacheExpireTime = 3 * 24 * 3600;
		appleLoginVerifyEnv = false;
		testEnv = false;
		superSoldierId = 1001;
		superSoldierTutorialKey = "IsBuildClick";
		motherPlayerIds = "";
		motherPlayer = "";
		sonPlayerIds = "";
		removePlayerIds = "";
		motherRoleInfo = "";
		playerCopySkip = false;
		pushSurveyGrayServers = "90001";
		ipConvertParams = "192000065;83172;blackcentre.query/query_var;20247;OrZ6lQv6Qayn$VBR;100;hello;100";
		checkTimeOutCityPeroid = 180;
		checkTimeOutCityHourA = 2;
		checkTimeOutCityHourB = 7;
		checkTimeOutCityCount = 30;
		separateGuardItem = "30000_21063027_1";
		guildLeaderCheck = false;
		secOperSpecialPlayerIds = "852-23kxn7-d,851-232bri-4";
		inheritTestServers = "";
		seasonHonorNew = true;
		payItemsAreaSwith = 0;
	}
	
	public int getPayItemsAreaSwith() {
		return payItemsAreaSwith;
	}

	public boolean isSeasonHonorNew() {
		return seasonHonorNew;
	}

	public String getMotherRoleInfo() {
		return motherRoleInfo;
	}
	
	public boolean guildLeaderCheck() {
		return guildLeaderCheck;
	}

	public boolean isPlayerCopySkip() {
		return playerCopySkip;
	}

	public int getMoneyCntAddLimit() {
		return moneyCntAddLimit;
	}

	public int getResCntAddLimit() {
		return resCntAddLimit;
	}

	public int getTokenExpiredCode() {
		return tokenExpiredCode;
	}
	
	public String getMotherPlayer() {
		return motherPlayer;
	}

	/**
	 * 默认名字前缀
	 */
	public String getNamePrefix() {
		return namePrefix;
	}

	/**
	 * 默认头像
	 */
	public int getDefaultIcon() {
		return defaultIcon;
	}

	/**
	 * 聊天文字长度
	 */
	public int getChatMsgMaxLen() {
		return chatMsgMaxLen;
	}

	/**
	 * 获取地图的视野X半径
	 * 
	 * @return
	 */
	public int getViewXRadius() {
		return viewXRadius;
	}

	/**
	 * 获取地图的视野Y半径
	 * 
	 * @return
	 */
	public int getViewYRadius() {
		return viewYRadius;
	}

	public int getSyncSearchRadius() {
		return syncSearchRadius;
	}

	/**
	 * 获取移动速率同步因子
	 * 
	 * @return
	 */
	public float getMoveSyncFactor() {
		return moveSyncFactor;
	}

	/**
	 * 获取地图区块的更新周期
	 * 
	 * @return
	 */
	public int getAreaUpdatePeriod() {
		return areaUpdatePeriod;
	}

	/**
	 * 获取区域更新的延迟时间
	 * 
	 * @return
	 */
	public int getAreaUpdateDelayPeriod() {
		return areaUpdateDelayPeriod;
	}

	/**
	 * 获取活动更新周期
	 * 
	 * @return
	 */
	public int getActivityPeriod() {
		return activityPeriod;
	}

	/**
	 * 获取新手数据的有效期
	 * 
	 * @return
	 */
	public int getNewlyPeriod() {
		return newlyPeriod;
	}

	/**
	 * 获取系统邮件的有效期
	 * 
	 * @return
	 */
	public long getSystemMailExpire() {
		return systemMailExpire;
	}

	/**
	 * 是否为USD支付渠道
	 * 
	 * @param channel
	 * @return
	 */
	public static boolean isUSDChannel(String channel) {
		return usdChannelMap.containsKey(channel.toLowerCase().trim());
	}

	/**
	 * 获取默认的出生中心点
	 * 
	 * @return
	 */
	public Point getDefaultBornPoint() {
		return defaultBornPoint;
	}

	/**
	 * 获取默认的联盟id
	 * 
	 * @return
	 */
	public String getDefaultGuildId() {
		return defaultGuildId;
	}

	/**
	 * 获取最小的出生点
	 * 
	 * @return
	 */
	public Point getRandMinPt() {
		return randMinPt;
	}

	/**
	 * 获取随机的最大点
	 * 
	 * @return
	 */
	public Point getRandMaxPt() {
		return randMaxPt;
	}

	/**
	 * 获取警报周期
	 * 
	 * @return
	 */
	public int getAlarmPeriod() {
		return alarmPeriod;
	}

	public int getReadMailInterval() {
		return readMailInterval;
	}

	/**
	 * 获得领地切换，领地生效更新周期
	 * 
	 * @return
	 */
	public int getManorEffectPeriod() {
		return manorEffectPeriod;
	}

	/**
	 * 获得指挥官释放的更新周期
	 * 
	 * @return
	 */
	public int getCommanderRelaeasePeriod() {
		return commanderRelaeasePeriod;
	}

	/**
	 * 获得联盟商店刷新的更新周期
	 * 
	 * @return
	 */
	public int getGuildTickPeriod() {
		return guildTickPeriod;
	}

	/**
	 * 获得快照更新周期(毫秒)
	 * 
	 * @return
	 */
	public int getSnapshotPeriod() {
		return snapshotPeriod;
	}

	/**
	 * 获取快照更新延迟(毫秒)
	 * 
	 * @return
	 */
	public int getSnapshotDelay() {
		return snapshotDelay;
	}
	
	/**
	 * 获取快照每一批次更新数量
	 * @return
	 */
	public int getSnapshotBatchCnt() {
		return snapshotBatchCnt;
	}

	/**
	 * 聊天缓存更新周期
	 * 
	 * @return
	 */
	public int getChatUpdatePeriod() {
		return chatUpdatePeriod;
	}

	/**
	 * 刷新redis中聊天信息的缓存
	 * 
	 * @return
	 */
	public int getChatCacheTime() {
		return chatCacheTime;
	}

	/**
	 * 检查全服邮件频率(毫秒)
	 */
	public int getCheckGlobalMailTime() {
		return checkGlobalMailTime;
	}

	/**
	 * 日志记录在线玩家频率(毫秒)
	 */
	public int getShowActivePlayerPeriod() {
		return showActivePlayerPeriod;
	}

	/**
	 * 日志记录玩家cache状态频率(毫秒)
	 */
	public int getShowPlayerCachePeriod() {
		return showPlayerCachePeriod;
	}

	/**
	 * 战斗数据缓存有效期
	 * 
	 * @return
	 */
	public long getBattleCachePeriod() {
		return battleCachePeriod;
	}

	/**
	 * 全服调查问卷推送过期检测周期(毫秒)
	 * 
	 * @return
	 */
	public int getCheckQuestionnaireTime() {
		return checkQuestionnaireTime;
	}

	/**
	 * 全服调查问卷读取间隔(毫秒)
	 * 
	 * @return
	 */
	public int getQuestionnaireInterval() {
		return questionnaireInterval;
	}

	/**
	 * 是否关闭pve副本结果服务器校验
	 * 
	 * @return
	 */
	public boolean isPveResultServerCheckClosed() {
		return closePveResultServerCheck == 1;
	}

	/**
	 * 客户端日志记录包体大小限制(字节)
	 * 
	 * @return
	 */
	public int getClientLogRecordSizeLimit() {
		return clientLogRecordSizeLimit;
	}

	/**
	 * 榜单封禁检测周期(毫秒)
	 * 
	 * @return
	 */
	public int getCheckRankBanPeriod() {
		return checkRankBanPeriod;
	}
	
	/**
	 * 战力排行更新存储周期(ms)
	 * @return
	 */
	public int getUpdatePowerRankPeriod() {
		return updatePowerRankPeriod;
	}

	/**
	 * 尤里复仇活动数据缓存有效期(s)
	 * 
	 * @return
	 */
	public int getYuriRevengeCachePeriod() {
		return (int) (yuriRevengeCachePeriod / 1000);
	}

	/**
	 * 判断cdk类型是否可循环使用
	 * 
	 * @param cdkType
	 * @return
	 */
	public boolean isCycleCdkType(String cdkType) {
		return cdkTypes.containsKey(cdkType);
	}

	/**
	 * 联盟战争储存条数上限
	 * 
	 * @return
	 */
	public int getRecordCount() {
		return recordCount;
	}

	public int getMailExpireSecond() {
		return mailExpireSecond;
	}

	public int getMailMaxCount() {
		return mailMaxCount;
	}

	public int getMailClearOnce() {
		return mailClearOnce;
	}

	public int getMarchCountLimit() {
		return marchCountLimit;
	}

	public int getChatTimeInterval() {
		return chatTimeInterval;
	}
	
	public int getClientEventSizeLimit() {
		return clientEventSizeLimit;
	}
	
	public boolean isSurveyNotifyOpen() {
		return surveyNotify == 1;
	}
	
	public int getPushSwitchCloseValue() {
		return pushSwitchCloseValue;
	}

	/**
	 * 数据组装
	 */
	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(usdChannels)) {
			String[] channels = usdChannels.split(",");
			for (String channel : channels) {
				channel = channel.toLowerCase().trim();
				usdChannelMap.put(channel, channel);
			}
		}

		if (!HawkOSOperator.isEmptyString(cdkType)) {
			String[] types = cdkType.split(",");
			for (String type : types) {
				cdkTypes.put(type, type);
			}
		}

		if (!HawkOSOperator.isEmptyString(defaultBornPos)) {
			String[] items = defaultBornPos.split("_");
			int x = Integer.valueOf(items[0]);
			int y = Integer.valueOf(items[1]);
			defaultBornPoint = new Point(x, y);
		}

		if (!HawkOSOperator.isEmptyString(randMinMaxPt)) {
			String[] items = randMinMaxPt.split(",");
			if (items.length >= 2) {
				String[] minItems = items[0].split("_");
				String[] maxItems = items[1].split("_");
				randMinPt = new Point(Integer.valueOf(minItems[0].trim()), Integer.valueOf(minItems[1].trim()));
				randMaxPt = new Point(Integer.valueOf(maxItems[0].trim()), Integer.valueOf(maxItems[1].trim()));
			}
		}

		if (!HawkOSOperator.isEmptyString(defaultPfIcon)) {
			pfIconList.clear();
			String[] items = defaultPfIcon.split(",");
			for (String item : items) {
				pfIconList.add(item.trim());
			}
		}
		
		if (!HawkOSOperator.isEmptyString(cityLevelStep)) {
			cityLevelVipExpMap = new HashMap<Integer, Integer>();
			String[] cityLevelStepArr = cityLevelStep.split(",");
			for (String step : cityLevelStepArr) {
				String[] arr = step.split("-");
				cityLevelVipExpMap.put(Integer.valueOf(arr[0]), Integer.valueOf(arr[1]));
			}
		}
		
		motherPlayerIdList = SerializeHelper.stringToList(String.class, motherPlayerIds, ",");
		sonPlayerIdList = SerializeHelper.stringToList(String.class, sonPlayerIds, ",");
		removePlayerIdList = SerializeHelper.stringToList(String.class, removePlayerIds, ",");
		
		pushSurveyGrayServerList = SerializeHelper.stringToList(String.class, pushSurveyGrayServers, ",");
		secOperSpecialPlayerIdList = SerializeHelper.stringToList(String.class, secOperSpecialPlayerIds, ",");
		
		inheritTestServerList = SerializeHelper.stringToList(String.class, inheritTestServers, ",");
		
		String[] paramArray = ipConvertParams.split(";");
		ip_convert_modid = Integer.parseInt(paramArray[0]);
		ip_convert_cmdid = Integer.parseInt(paramArray[1]);
		ip_convert_suburl = paramArray[2];
		ip_convert_appid = Integer.parseInt(paramArray[3]);
		ip_convert_token = paramArray[4];
		ip_convert_echo = paramArray[5];
		ip_convert_randstr = paramArray[6];
		ip_convert_command = Integer.parseInt(paramArray[7]);
		
		instance = this;
		return true;
	}
	
	public List<String> getInheritTestServerList() {
		return inheritTestServerList;
	}
	
	public boolean isSecOperSpecialPlayer(String playerId) {
		return secOperSpecialPlayerIdList.contains(playerId);
	}
	
	public List<String> getPushSurveyGrayServerList() {
		return pushSurveyGrayServerList;
	}

	public List<String> getSonPlayerIdList() {
		return sonPlayerIdList;
	}
	
	public List<String> getMotherPlayerIdList() {
		return motherPlayerIdList;
	}
	
	public List<String> getRemovePlayerIdList() {
		return removePlayerIdList;
	}
	
	public int getVipExpByCityLevel(Integer cityLevel) {
		if (cityLevelVipExpMap == null) {
			return 0;
		}
		
		if (cityLevelVipExpMap.containsKey(cityLevel)) {
			return cityLevelVipExpMap.get(cityLevel);
		}
		
		return 0;
	}

	public int getSendChatNow() {
		return sendChatNow;
	}

	public String randomPfIcon() {
		if (pfIconList.size() > 0) {
			return pfIconList.get(HawkRand.randInt(pfIconList.size()-1));
		}
		return null;
	}

	public int getKeepBattleLog() {
		// 测试环境看配置，正式环境直接关闭
		return GsConfig.getInstance().isDebug() ? keepBattleLog : 0;
	}

	public int getCustomDataLimit() {
		return customDataLimit;
	}

	public long getPowerChangeEventPostDelayCondition() {
		return powerChangeEventPostDelayCondition;
	}

	public long getPowerChangeEventPostPeriod() {
		return powerChangeEventPostPeriod;
	}

	public int getRelaitonApplyExpireTime() {
		return relaitonApplyExpireTime;
	}

	public boolean isOpenPresidentFight() {
		return openPresidentFight;
	}

	public boolean isOpenArmyCheck() {
		return openArmyCheck;
	}

	public long getSuperBarrackPaceTime() {
		return superBarrackPaceTime;
	}

	public long getSuperBarrackSignTime() {
		return superBarrackSignTime;
	}

	public long getSuperBarrackWarfreTime() {
		return superBarrackWarfreTime;
	}

	public long getSuperBarrackControlTime() {
		return superBarrackControlTime;
	}

	public long getSuperBarrackOccupyTime() {
		return superBarrackOccupyTime;
	}

	public long getMonsterRefreshDelay() {
		return monsterRefreshDelay;
	}

	public boolean isOpenMonsterActivity() {
		return openMonsterActivity;
	}

	public long getResourceRefreshDelay() {
		return resourceRefreshDelay;
	}

	public long getStrongpointRefreshDelay() {
		return strongpointRefreshDelay;
	}

	public boolean isOpenStrongpointActivity() {
		return openStrongpointActivity;
	}

	public boolean isDebugControlWorldRefresh() {
		return debugControlWorldRefresh;
	}

	public int getSuperWeaponExpireSecond() {
		return superWeaponExpireSecond;
	}

	public int getPowerScoreBatchLv() {
		return powerScoreBatchLv;
	}

	public int getScoreBatchMaxWait() {
		return scoreBatchMaxWait;
	}

	public int getOnlineFlagExpire() {
		return onlineFlagExpire;
	}

	public int getOnlineFlagTickPeriod() {
		return onlineFlagTickPeriod;
	}

	public int getArmyFixFactor() {
		return armyFixFactor;
	}

	public long getBeAttackedCalcTime() {
		return beAttackedCalcTime;
	}
	public int getExitCrossPeriodTime() {
		return exitCrossPeriodTime;
	}

	public int getExitCrossMinWaitTime() {
		return exitCrossMinWaitTime;
	}

	public int getExitCrossMaxWaitTime() {
		return exitCrossMaxWaitTime;
	}

	public boolean isCrossEntityLog() {
		return crossEntityLog;
	}

	public int getServerActiveFixTime() {
		return serverActiveFixTime;
	}

	public int getCrossDataPipelineMaxLength() {
		return crossDataPipelineMaxLength;
	}

	public int getCrossProtocolValidTime() {
		return crossProtocolValidTime;
	}

	public boolean isNianOptimize() {
		return nianOptimize;
	}

	public long getSendCouponWaitMs() {
		return sendCouponWaitMs;
	}
	
	public int getCrossCacheExpireTime() {
		return crossCacheExpireTime;
	}

	public boolean isAppleLoginVerifyEnv() {
		return appleLoginVerifyEnv;
	}

	public boolean isTestEnv() {
		return testEnv;
	}

	public int getSuperSoldierId() {
		return superSoldierId;
	}

	public String getSuperSoldierTutorialKey() {
		return superSoldierTutorialKey;
	}

	public int getIp_convert_modid() {
		return ip_convert_modid;
	}

	public int getIp_convert_cmdid() {
		return ip_convert_cmdid;
	}

	public String getIp_convert_suburl() {
		return ip_convert_suburl;
	}

	public int getIp_convert_appid() {
		return ip_convert_appid;
	}

	public String getIp_convert_token() {
		return ip_convert_token;
	}

	public String getIp_convert_echo() {
		return ip_convert_echo;
	}

	public String getIp_convert_randstr() {
		return ip_convert_randstr;
	}

	public int getIp_convert_command() {
		return ip_convert_command;
	}

	public long getCheckTimeOutCityPeroid() {
		return checkTimeOutCityPeroid * 1000;
	}

	public int getCheckTimeOutCityHourA() {
		return checkTimeOutCityHourA;
	}

	public int getCheckTimeOutCityHourB() {
		return checkTimeOutCityHourB;
	}

	public int getCheckTimeOutCityCount() {
		return checkTimeOutCityCount;
	}

	public ItemInfo getSeparateGuardItem() {
		return ItemInfo.valueOf(separateGuardItem);
	}

}
