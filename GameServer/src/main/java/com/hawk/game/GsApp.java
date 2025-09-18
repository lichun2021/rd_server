package com.hawk.game;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.hawk.game.module.homeland.map.HomeLandMapService;
import com.hawk.game.module.homeland.rank.HomeLandService;
import com.hawk.game.service.commonMatch.CMWService;
import com.hawk.game.service.guildTeam.GuildTeamService;
import com.hawk.game.service.shop.ShopService;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.xhjzWar.XHJZWarService;
import com.hawk.game.service.xqhxWar.XQHXWarService;
import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppCfg;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigStorage;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.net.HawkIptablesManager;
import org.hawk.net.HawkNetworkManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.obj.HawkObjManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkShutdownHook;
import org.hawk.os.HawkTime;
import org.hawk.pool.HawkObjectPool;
import org.hawk.profiler.HawkSysProfiler;
import org.hawk.redis.HawkRedisSession;
import org.hawk.security.HawkPPSSecurity;
import org.hawk.task.HawkTaskManager;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.util.HawkWordFilter;
import org.hawk.util.service.HawkCdkService;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ProtocolMessageEnum;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.common.CommonConst.ServerType;
import com.hawk.common.ServerInfo;
import com.hawk.common.service.BillboardService;
import com.hawk.game.activity.ActivityConfigChecker;
import com.hawk.game.activity.ActivityConfigLoader;
import com.hawk.game.activity.GameActivityDataProxy;
import com.hawk.game.activity.impl.backflow.BackFlowService;
import com.hawk.game.activity.impl.inherit.InheritNewService;
import com.hawk.game.activity.impl.inherit.InheritService;
import com.hawk.game.activity.impl.yurirevenge.YuriRevengeService;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.effect.CheckerFactory;
import com.hawk.game.callback.ShutdownCallback;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.BlockWordCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.IpCfg;
import com.hawk.game.config.MergeServerConfig;
import com.hawk.game.config.ProtoElapseCfg;
import com.hawk.game.config.SecProtoCfg;
import com.hawk.game.config.SeparateActivityCfg;
import com.hawk.game.config.ServerGroupCfg;
import com.hawk.game.config.SilenceWordCfg;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossactivity.season.CrossActivitySeasonService;
import com.hawk.game.crossfortress.CrossFortressService;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.CrossSkillService;
import com.hawk.game.crossproxy.callback.CrossPlayerLoginCallback;
import com.hawk.game.data.ServerSettingData;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.ServerIdentifyEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.guild.championship.ChampionshipService;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.heroTrial.HeroTrialCheckerFactory;
import com.hawk.game.idipscript.online.UpdateInfoAuthorizationHandler;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.CYBORGRoomManager;
import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.lianmengjunyan.LMJYRoomManager;
import com.hawk.game.lianmengstarwars.SWBattleRoom;
import com.hawk.game.lianmengstarwars.SWRoomManager;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.autologic.service.GuildAutoMarchService;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager;
import com.hawk.game.module.dayazhizhan.marchserver.service.DYZZMatchService;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonService;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.module.lianmengXianquhx.XQHXBattleRoom;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLMatchService;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZBattleRoom;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager;
import com.hawk.game.module.material.MeterialTransportService;
import com.hawk.game.module.obelisk.service.ObeliskService;
import com.hawk.game.module.schedule.ScheduleService;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.toucai.MedalFactoryService;
import com.hawk.game.msg.RemoveObjectMsg;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.nation.NationService;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerFactory;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.player.item.ItemService;
import com.hawk.game.player.roleexchange.RoleExchangeService;
import com.hawk.game.player.roleexchange.XinyueConst.ServerState;
import com.hawk.game.player.skill.talent.TalentSkillContext;
import com.hawk.game.player.strength.PlayerStrengthFactory;
import com.hawk.game.playercopy.PlayerCopyService;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.profiler.ProfilerAnalyzer;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Const.LoginFlag;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Login.HPLoginRet;
import com.hawk.game.protocol.Login.HPWaitLogin;
import com.hawk.game.protocol.ScriptProxy.script;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.SysError;
import com.hawk.game.protocol.Status.WaitLoginCode;
import com.hawk.game.protocol.SysProtocol.HPErrorCode;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.recharge.RechargeManager;
import com.hawk.game.scriptproxy.ScriptProxy;
import com.hawk.game.serverproxy.ServerProxyManager;
import com.hawk.game.service.ActivityService;
import com.hawk.game.service.BuffService;
import com.hawk.game.service.GlobalTimerService;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.ImmgrationService;
import com.hawk.game.service.PlayerAchieveService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.QQScoreBatch;
import com.hawk.game.service.QuestionnaireService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.SearchService;
import com.hawk.game.service.StoryMissionService;
import com.hawk.game.service.SysOpService;
import com.hawk.game.service.TavernService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.college.CollegeService;
import com.hawk.game.service.cyborgWar.CyborgLeaguaWarService;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.service.mail.MailManager;
import com.hawk.game.service.mssion.MissionContext;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.pushgift.PushGiftManager;
import com.hawk.game.service.simulatewar.SimulateWarService;
import com.hawk.game.service.starwars.StarWarsActivityService;
import com.hawk.game.service.tiberium.TiberiumLeagueWarService;
import com.hawk.game.service.tiberium.TiberiumWarService;
import com.hawk.game.strengthenguide.StrengthenGuideManager;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.task.PlayerLoginTask;
import com.hawk.game.trustee.TrusteeService;
import com.hawk.game.tsssdk.GameTssSdk;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.AsmCheckUtil;
import com.hawk.game.util.DBUtil;
import com.hawk.game.util.ExceptionReportUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.DailyInfoField;
import com.hawk.game.util.GsConst.PlayerCrossStatus;
import com.hawk.game.util.GsConst.WaitLoginState;
import com.hawk.game.util.LoginUtil;
import com.hawk.game.util.ProtoUtil;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.service.WorldChristmasWarService;
import com.hawk.game.world.service.WorldFoggyFortressService;
import com.hawk.game.world.service.WorldGundamService;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldNianService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldPylonService;
import com.hawk.game.world.service.WorldResTreasurePointService;
import com.hawk.game.world.service.WorldResourceService;
import com.hawk.game.world.service.WorldRobotService;
import com.hawk.game.world.service.WorldSnowballService;
import com.hawk.game.world.service.WorldStrongPointService;
import com.hawk.game.world.service.WorldTreasureHuntService;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.activity.ConfigChecker;
import com.hawk.gamelog.GameLog;
import com.hawk.log.Action;
import com.hawk.log.LogConst;
import com.hawk.sdk.SDKConst;
import com.hawk.sdk.SDKManager;
import com.hawk.sdk.config.HealthCfg;
import com.hawk.sdk.config.PlatformConstCfg;
import com.hawk.tsssdk.manager.TssSdkManager;
import com.hawk.zoninesdk.ZonineSDK;

/**
 * 游戏应用
 * 
 * @author hawk
 * 
 */
public class GsApp extends HawkApp {
	/**
	 * playerId登录时间间隔控制
	 */
	private Map<String, Long> puidLoginTime;
	/**
	 * 屏蔽词过滤器
	 */
	private HawkWordFilter wordFilter;

	/**
	 * 沉默词语过滤器
	 */
	private HawkWordFilter silenceWordFilter;
	/**
	 * 服务器开服时间
	 */
	private volatile long serverOpenTime = 0;
	/**
	 * 服务器开服时间当天时间的凌晨0点.
	 */
	private long serverOpenAM0Time = 0;
	/**
	 * 初始化完成
	 */
	private boolean initOK = false;

	/**
	 * 服务器标识
	 */
	public ServerIdentifyEntity serverIdentifyEntity;
	
	public String mergeNotChangeIdentify;
	/**
	 * redis是否异常中
	 */
	public volatile boolean redisInException;
	/**
	 * 处于排队等待中的session集合
	 */
	private Queue<HawkSession> waitSessionQueue;
	/**
	 * 批量状态
	 */
	private int batchStatus = 0;
	
	/**
	 * 全局静态对象
	 */
	private static GsApp instance = null;

	/**
	 * 获取全局静态对象
	 * 
	 * @return
	 */
	public static GsApp getInstance() {
		return instance;
	}

	/**
	 * 构造函数
	 */
	public GsApp() {
		super(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.APP));

		if (instance == null) {
			instance = this;
		}

		// 初始化对象
		redisInException = false;
		wordFilter = new HawkWordFilter();
		silenceWordFilter = new HawkWordFilter();
		puidLoginTime = new ConcurrentHashMap<String, Long>();
	}

	/**
	 * 从配置文件初始化
	 * 
	 * @param cfg
	 * @return
	 */
	public boolean init(String cfg) {
		
		// 应用程序主体配置
		GsConfig appCfg = null;
		try {
			HawkConfigStorage cfgStorage = new HawkConfigStorage(GsConfig.class);
			appCfg = (GsConfig) cfgStorage.getConfigByIndex(0);
			HawkTime.setMsOffset(appCfg.getTsOffset() * 1000L);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}

		// 服务器分组配置
		try {
			HawkConfigStorage cfgStorage = new HawkConfigStorage(ServerGroupCfg.class);
			ServerGroupCfg groupCfg = (ServerGroupCfg) cfgStorage.getConfigByIndex(0);
			if (groupCfg != null) {
				String groupId = ServerGroupCfg.getServerGroupId(appCfg.getServerId());
				if (!HawkOSOperator.isEmptyString(groupId)) {
					HawkConfigManager.getInstance().setConfigRoot("groupData/" + groupId);
					HawkLog.logPrintln("server group info, serverId: {}, groupId: {}", appCfg.getServerId(), groupId);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		
		// 设置路径
		if (!HawkOSOperator.installLibPath()) {
			return false;
		}

		// 打印打包进去的svn信息
		try {
			InputStream is = this.getClass().getResourceAsStream("/svn_info.txt");
			byte[] content = new byte[is.available()];
			is.read(content);
			is.close();
			HawkLog.logPrintln("SVNInfo: {}", new String(content, "GB2312"));
		} catch (Exception e) {
			HawkLog.logPrintln("SVNInfo Miss");
		}

		// 注册活动配置检测
		ConfigChecker.setDefaultChecker(new ActivityConfigChecker());

		// 玩家数据序列化对象的检测
		if (!PlayerDataSerializer.checkSerilizeData()) {
			return false;
		}
		
		// Action重复性校验
		if (!Action.checkRepeated()) {
			HawkLog.errPrintln("action define repeated");
			return false;
		}
		
		// 活动成就id重复校验
		if (!AchieveType.checkRepeated()) {
			return false;
		}
		
		// 父类初始化
		if (!super.init(appCfg)) {
			return false;
		}
		
		this.currentTime = HawkTime.getMillisecond();
		
		// 开启线程池
		HawkTaskManager.getInstance().startThreadPool("task", GsConfig.getInstance().getExtraThreads(), true);
		
		// 尝试修复sql
		if (appCfg.isDebug()) {
			if (appCfg.isUseCodeUpdateDb()) {
				DBUtil.useCodeUpdateDb();
			}
			//检测是否在ActivivityBase中调用了其它方法.
			AsmCheckUtil.checkActivityCreateEntity();
			if (!AsmCheckUtil.checkActivityDBEntity()) {
				return false;
			}
			if (!AsmCheckUtil.checkJedisUnclose(Arrays.asList("com.hawk.game.global.LocalRedis", "com.hawk.game.global.RedisProxy"))) {
				return false;
			}
		}
		
		// 初始化对象池
		if (appCfg.isObjectPool()) {
			HawkObjectPool.getInstance().scanObjectPool("com.hawk.game.msg");
			HawkObjectPool.getInstance().scanObjectPool("com.hawk.activity.msg");
		}

		// 统计初始化
		StatisManager.getInstance().init();
		CheckerFactory.getInstance().init();
		// 初始化redis代理
		if (!RedisProxy.getInstance().init()) {
			return false;
		}
		
		// 初始化服务器标识
		if (!initServerIdentify()) {
			return false;
		}
		
		// 初始化本地redis操作对象
		if (!LocalRedis.getInstance().init()) {
			return false;
		}
		
		// 分散全服同步起服的数据读写压力
		waitBatchStatus();
				
		// 获取服务器时间
		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(appCfg.getServerId());
		if (serverInfo != null && !HawkOSOperator.isEmptyString(serverInfo.getOpenTime())) {
			serverOpenTime = HawkTime.parseTime(serverInfo.getOpenTime());
			serverOpenAM0Time = HawkTime.getAM0Date(new Date(serverOpenTime)).getTime();
		}

		if (serverOpenTime > 0) {
			HawkLog.logPrintln("server open time: {}", HawkTime.formatTime(serverOpenTime));
		} else {
			HawkLog.logPrintln("server open time: 0000-00-00 00:00:00");
		}

		// 没有开服的情况下, 一直获取数据
		while (serverOpenTime <= 0) {
			serverInfo = RedisProxy.getInstance().getServerInfo(GsConfig.getInstance().getServerId());
			if (serverInfo != null && !HawkOSOperator.isEmptyString(serverInfo.getOpenTime())) {
				serverOpenTime = HawkTime.parseTime(serverInfo.getOpenTime());
				serverOpenAM0Time = HawkTime.getAM0Date(new Date(serverOpenTime)).getTime();
				break;
			}

			HawkLog.logPrintln("waitting server open ......");
			HawkOSOperator.osSleep(5000);
		}
		
		if (serverIdentifyEntity.getServerOpenTime() == 0) {
			serverIdentifyEntity.setServerOpenTime(serverOpenTime);
		} else if (serverIdentifyEntity.getServerOpenTime() != serverOpenTime && !GsConfig.getInstance().isDebug()) {
			HawkLog.logPrintln("serverOpenTime error.");
			return false;
		}
		
		// 初始化sdk服务
		SDKManager.initInstance(SDKConst.SDKType.MSDK, appCfg.getHttpUrlTimeout(), GsConfig.getInstance().getServerId());
		// 初始化---安全SDK
		if (appCfg.isTssSdkEnable()) {
			int instanceId = 0;
			String serverId = appCfg.getServerId();
			if (!HawkOSOperator.isEmptyString(serverId)) {
				instanceId = Integer.parseInt(serverId);
			}

			GameTssService.getInstance().init();
			TssSdkManager.getInstance().initTssSdk(GameTssSdk.class, instanceId, "./cfg/tss", appCfg.getTssInterval());
		}
				
		// 初始化游戏日志对象
		if (appCfg.isTlogEnable() && !GameLog.getInstance().init()) {
			HawkLog.errPrintln("tlog init failed !!!!");
			return false;
		}
		
		// 初始化组装数据管理器
		if (!AssembleDataManager.getInstance().init()) {
			return false;
		}

		// 检测活跃
		HawkDBManager.getInstance().setCheckAliveSql("SELECT serverIdentify from server_identify");
				
		// 初始化系统控制器
		SystemControler.getInstance().init();

		// 初始化对象管理区
		if (!initAppObjMan()) {
			return false;
		}
		
		// 在初始化GlobalData之前执行
		try {
			new HawkConfigStorage(HealthCfg.class);
			new HawkConfigStorage(PlatformConstCfg.class);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 重设配置路径
		ActivityConfigLoader.getInstance().init();
		if (!ActivityConfigLoader.getInstance().resetConfigFilePath()) {
			return false;
		}
		
		// 手动装载配置
		if (appCfg.getConfigPackages() != null && appCfg.getConfigPackages().length() > 0) {
			if (!HawkConfigManager.getInstance().init(appCfg.getConfigPackages(), false)) {
				System.err.println("----------------------------------------------------------------------");
				System.err.println("-------------config crashed, take weapon to fuck designer-------------");
				System.err.println("----------------------------------------------------------------------");
				return false;
			}
		}
		
		String serverId = GsConfig.getInstance().getServerId();
		String mainServerId = GlobalData.getInstance().getMainServerId(serverId);
		// 合完服后从服不让起服
		if (!HawkOSOperator.isEmptyString(mainServerId) && !mainServerId.equals(serverId)) {
			HawkLog.errPrintln("slave server cannot startup after merge");
			return false;
		}
		
		Long mergeTime = AssembleDataManager.getInstance().getServerMergeTime(serverId);
		if (mergeTime != null && HawkTime.isToday(mergeTime) && mergeTime > HawkTime.getMillisecond()) {
			HawkLog.errPrintln("server cannot startup before config mergeTime in merge day");
			return false;
		}
		
		// 初始化活动管理器
		if (!ActivityManager.getInstance().init(GameConstCfg.getInstance().getActivityPeriod())) {
			HawkLog.errPrintln("init activity service failed");
			return false;
		}
		
		// 全局数据初始化
		if (!GlobalData.getInstance().init()) {
			return false;
		}

		// 初始化性能分析器
		ProfilerAnalyzer.getInstance().init();

		// 战力
		if (!PlayerStrengthFactory.getInstance().init()) {
			return false;
		}
		
		// 英雄试炼
		HeroTrialCheckerFactory.getInstance().init();
		
		//加载匹配列表 这里CrossActivityService 还没有初始化.
		CrossActivityService.getInstance().loadCrossServerList();
		
		// 跨服服务初始化
		if (!CrossService.getInstance().init()) {
			HawkLog.errPrintln("cross service init failed");
			return false;
		}
		
		// 初始化联盟管理器
		if (!GuildService.getInstance().initGuildData()) {
			return false;
		}
		
		// 邮件
		if(!MailManager.getInstance().init()){
			return false;
		}
		
		if(!MedalFactoryService.getInstance().init()){
			return false;
		}

		// 初始化世界基础点管理器
		if (!WorldPointService.getInstance().init()) {
			return false;
		}

		// 初始化世界玩家点管理器
		if (!WorldPlayerService.getInstance().init()) {
			return false;
		}

		// 初始化机器人玩家点管理器
		if (!WorldRobotService.getInstance().init()) {
			return false;
		}
		
		// 初始化世界怪点管理器
		if (!WorldMonsterService.getInstance().init()) {
			return false;
		}
		
		// 初始化据点管理器
		if (!WorldStrongPointService.getInstance().init()) {
			return false;
		}

		// 初始化能量塔管理器
		if (!WorldPylonService.getInstance().init()) {
			return false;
		}
		
		// 初始化雪球管理器
		if (!WorldSnowballService.getInstance().init()) {
			return false;
		}

		// 初始化世界资源管理器
		if (!WorldResourceService.getInstance().init()) {
			return false;
		}
		
		// 初始化世界迷雾点管理器
		if (!WorldFoggyFortressService.getInstance().init()) {
			return false;
		}

		// 初始化世界机甲管理器
		if (!WorldGundamService.getInstance().init()) {
			return false;
		}
		
		// 初始化世界年兽管理器
		if (!WorldNianService.getInstance().init()) {
			return false;
		}
		
		// 初始寻宝管理器
		if (!WorldTreasureHuntService.getInstance().init()) {
			return false;
		}
		
		// 战地之王管理器
		if (!WarFlagService.getInstance().init()) {
			return false;
		}
		
		// 星甲召唤
		if (!SpaceMechaService.getInstance().init()) {
			return false;
		}
		
		// 国王战管理器
		if (!PresidentFightService.getInstance().init()) {
			return false;
		}
		
		// 名城(超级武器)管理器
		if (!SuperWeaponService.getInstance().init()) {
			return false;
		}
		
		// 初始化世界行军管理器
		if (!WorldMarchService.getInstance().init()) {
			return false;
		}

		// 初始化世界城点管理器
		if (!CityManager.getInstance().init()) {
			return false;
		}
		
		// 要塞管理器
		if (!CrossFortressService.getInstance().init()) {
			return false;
		}
		
		//初始化好友模块,主要是因为加了守护功能.
		if (!RelationService.getInstance().init()) {
			return false; 
		}
		
		// 排行管理器
		if (!RankService.getInstance().init()) {
			return false;
		}
		
		// 充值管理器
		if (!RechargeManager.getInstance().init()) {
			return false;
		}
		
		// 道具管理
		if (!ItemService.getInstance().init()) {
			return false;
		}
		
		
		//赛季- 跨服服务初始化
		if(!CrossActivitySeasonService.getInstance().init()){
			return false;
		}
		
		// 跨服服务初始化
		if (!CrossActivityService.getInstance().init()) {
			HawkLog.errPrintln("cross activity service init failed");
			return false;
		}
		
		// 司令技能
		if (!CrossSkillService.getInstance().init()) {
			return false;
		}
		
		// 初始化联盟领地管理器
		if (!GuildManorService.getInstance().init()) {
			return false;
		}

		// 搜索服务
		if (!SearchService.getInstance().init()) {
			return false;
		}
		
		// 初始化军事学院管理器
		if (!CollegeService.getInstance().init()) {
			return false;
		}

		// 问卷系统管理器
		if (!QuestionnaireService.getInstance().init()) {
			return false;
		}

		// 尤里复仇活动管理器
		if (!YuriRevengeService.getInstance().init()) {
			return false;
		}
		
		// 军魂承接管理器
		if (!InheritService.getInstance().init()) {
			return false;
		}
		
		// 军魂承接管理器(新)
		if (!InheritNewService.getInstance().init()) {
			return false;
		}
	 
		//老玩家活动数据管理器
		if(!BackFlowService.getInstance().init()){
			return false;
		}
		//联盟排行榜管理器
		if(!GuildRankMgr.getInstance().init()){
			return false;
		}
		
		// 我要变强
		if(!StrengthenGuideManager.getInstance().init()){
			return false;
		}
		// 小战区管理器
		if (!XZQService.getInstance().init()) {
			return false;
		}
		
		//方尖碑初始化
		if(!ObeliskService.getInstance().init()){
			return false;
		}
		
		//达雅之战初始化
		if(!DYZZService.getInstance().init()){
			return false;
		}
		//达雅之战匹配初始化
		if(!DYZZMatchService.getInstance().init()){
			return false;
		}
		//达雅之战匹配初始化
		if(!DYZZSeasonService.getInstance().init()){
			return false;
		}
		
		if(!NationService.getInstance().init()){
			return false;
		}
		if(!YQZZMatchService.getInstance().init()){
			return false;
		}
		if(!FGYLMatchService.getInstance().init()){
			return false;
		}
		if(!MeterialTransportService.getInstance().init()){
			return false;
		}
		
		// 初始化屏蔽字库
		if (!updateWords()) {
			return false;
		}

		// 初始化高级屏蔽字库
		if (!updateSlienceWords()) {
			return false;
		}

		// 服务器代理器初始化
		int connTimeout = appCfg.getProxyConnectTimeout();
		int idleTimeout = appCfg.getProxyIdleTimeout();
		if (!ServerProxyManager.getInstance().init(connTimeout, idleTimeout)) {
			return false;
		}
		
		// 任务管理器
		if (!MissionManager.getInstance().init()) {
			return false;
		}

		// 任务上下文
		MissionContext.getInstance().init();

		// 剧情任务服务类
		if (!StoryMissionService.getInstance().init()) {
			return false;
		}

		// 天赋技能
		TalentSkillContext.getInstance();

		// 每日任务(酒馆)
		TavernService.getInstance();
		
		// 成就
		PlayerAchieveService.getInstance().init();
		
		// 泰伯利亚之战服务初始化
		if (!TBLYWarService.getInstance().init()) {
			HawkLog.errPrintln("TiberiumWarService init failed");
			return false;
		}
		
		// 泰伯利亚联赛服务初始化
		if (!TBLYSeasonService.getInstance().init()) {
			HawkLog.errPrintln("TiberiumLeagueWarService init failed");
			return false;
		}
		
		// 星球大战服务初始化
		if (!StarWarsActivityService.getInstance().init()) {
			HawkLog.errPrintln("StarWarsActivityService init failed");
			return false;
		}		
		
		// 联盟锦标赛服务初始化
		if (!ChampionshipService.getInstance().init()) {
			HawkLog.errPrintln("GuildChampionshipService init failed");
			return false;
		}
		
		if (!SimulateWarService.getInstance().init()) {
			HawkLog.errPrintln("SimulateWarService init failed");
			return false;
		}
		
		if (!WorldChristmasWarService.getInstance().init()) {
			HawkLog.errPrintln("christmas war service init fail");
			
			return false;
		}

		if (!ShopService.getInstance().init()) {
			HawkLog.errPrintln("ShopService init failed");
			return false;
		}

		// 赛博联赛服务初始化
		if (!CyborgLeaguaWarService.getInstance().init()) {
			HawkLog.errPrintln("CyborgLeaguaWarService init failed");
			return false;
		}

		// 赛博之战服务初始化
		if (!CyborgWarService.getInstance().init()) {
			HawkLog.errPrintln("CyborgWarService init failed");
			return false;
		}

		// 泰伯利亚联赛服务初始化
		if (!XHJZWarService.getInstance().init()) {
			HawkLog.errPrintln("XHJZWarService init failed");
			return false;
		}

		if (!GuildTeamService.getInstance().init()) {
			HawkLog.errPrintln("GuildTeamService init failed");
			return false;
		}

		if (!CMWService.getInstance().init()) {
			HawkLog.errPrintln("CMWService init failed");
			return false;
		}

		if (!XQHXWarService.getInstance().init()) {
			HawkLog.errPrintln("XQHXWarService init failed");
			return false;
		}
		if (!HomeLandService.getInstance().init()) {
			HawkLog.errPrintln("HomeLandRank init failed");
			return false;
		}
		if (!HomeLandMapService.getInstance().init()) {
			HawkLog.errPrintln("HomeLandMap init failed");
			return false;
		}

		// CDK初始化
		if (!HawkCdkService.getInstance().install(appCfg.getGameId(), appCfg.getServerId(), appCfg.getCdkHost(), appCfg.getHttpUrlTimeout())) {
			return false;
		}

		// 设置关服回调
		HawkShutdownHook.getInstance().setCallback(new ShutdownCallback());

		// 初始化公告服务
		BillboardService.getInstance().init(RedisProxy.getInstance().getRedisSession());

		// 设置ip控制
		if (!updateIpControl()) {
			return false;
		}

		// 添加安全协议
		updateSecProto();
		
		// 接入ZonineSDK
		if (!GsConfig.getInstance().isDebug() && GsConfig.getInstance().isZonineEnable()) {
			if (!ZonineSDK.getInstance().init(appCfg.getGameId(), appCfg.getAreaId(), appCfg.getServerId(), appCfg.getZonineAddrList())) {
				return false;
			}
			
			ZonineSDK.getInstance().addMergeServers(GlobalData.getInstance().getMergeServerList(GsConfig.getInstance().getServerId()));
		}
		
		
		// 定时事件初始化
		GlobalTimerService.getInstance().init();
		
		//初始化推送礼包
		PushGiftManager.getInstance().init();
		
		// 登录排队
		if (GsConfig.getInstance().isLoginWaitQueue()) {
			waitSessionQueue = new LinkedBlockingQueue<HawkSession>();
		}
		
		// 积分上报
		QQScoreBatch.getInstance().init();
		
		// 策略初始化
		TrusteeService.getInstance().init();

		// 刷新写出当前版本号
		flushVersionInfo();
		
		//将拆服、合服涉及的活动表刷新到redis中
		flushActivityTable();
				
		// 注册tick信息
		registerTickable();
		
		//待办事项管理器初始化
		ScheduleService.getInstance().init();
		
		// db信息写入redis
		dbInfoToRedis();
		
		// redis信息写入redis
		rsInfoToRedis();
		
		// log信息写入redis
		logInfoToRedis();
		
		// 初始化世界资源管理器
		if (!WorldResourceService.getInstance().init2()) {
			return false;
		}

		// 迁服服务类
		if (!ImmgrationService.getInstance().init()) {
			return false;
		}
		
		this.addTickable(new HawkPeriodTickable(10000l) {
			
			@Override
			public void onPeriodTick() {
				try {
					adaptDbRiskCheck();
				} catch (Exception e) {
					HawkException.catchException(e);
				}				
			}
		});
		
		// 查询玩家数据
		if (PlayerCopyService.getInstance().init()) {
			PlayerCopyService.getInstance().selectMotherPlayer();
		}
		
		initOK = true;
		
		//修复指挥官等级排行榜数据
		SysOpService.getInstance().fixCommanderLevelRank();
		return true;
	}
	
	/**
	 * 23:55- 00:15 这段时间关闭
	 */
	private void adaptDbRiskCheck() {
		List<Integer> notCheckTime = GsConfig.getInstance().getAdaptDbRiskTimeList();
		if (CollectionUtils.isEmpty(notCheckTime)) {
			return;
		}
		long beforeZeorTime = notCheckTime.get(0) * 1000;
		long afterZeroTime = notCheckTime.get(1) * 1000;
		long curTime = HawkTime.getMillisecond();
		long curDayZeroTime = HawkTime.getAM0Date().getTime();
		long nextDayZeroTime = HawkTime.getNextAM0Date();
		if (curTime < curDayZeroTime + afterZeroTime) {
			HawkDBManager.getInstance().setRiskCheckEnable(false);
		} else if (curTime + beforeZeorTime > nextDayZeroTime) {
			HawkDBManager.getInstance().setRiskCheckEnable(false);
		} else {
			HawkDBManager.getInstance().setRiskCheckEnable(true);
		}
		
	}
	/**
	 * 等待批量状态
	 */
	private void waitBatchStatus() {
		if (GsConfig.getInstance().getMaxBatchServerCount() < 0) {
			return;
		}
		
		long startTime = HawkTime.getMillisecond();
		long count = 0;
		do {
			count = RedisProxy.getInstance().getRedisSession().hLen("server_batch_status", false);
			if (count < GsConfig.getInstance().getMaxBatchServerCount()) {
				batchStatus = HawkTime.getSeconds();
				RedisProxy.getInstance().getRedisSession().hSet("server_batch_status", appCfg.getServerId(), "" + batchStatus, 300);				
				return;
			}
			HawkOSOperator.osSleep(5000);
			HawkLog.logPrintln("wait for server batch status, current count {}", count);
		} while (HawkTime.getMillisecond() - startTime < 600000);
		HawkLog.logPrintln("wait timeout for server batch status, final count {}", count);
	}

	/**
	 * 初始化完成的通知
	 */
	public void onInitFinish() {
		try {
			if (batchStatus > 0) {
				batchStatus = 0;
				RedisProxy.getInstance().getRedisSession().hDel("server_batch_status", appCfg.getServerId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 配置重新加载
	 */
	public void onCfgReload(List<String> reloadFiles) {
		// 更新黑白名单
		updateIpControl();
		
		// 更新安全协议
		updateSecProto();
		
		// 重新初始化屏蔽字库
		updateWords();
		updateSlienceWords();
	}
	
	/**
	 * 写出版本信息
	 * 
	 * @return
	 */
	public boolean flushVersionInfo() {
		try {
			// 构造状态信息
			JSONObject verJson = new JSONObject();
			verJson.put("version", GsConfig.getInstance().getGsVersion());
			verJson.put("grayState", GsConfig.getInstance().getGrayState());
			verJson.put("condVersion", GsConfig.getInstance().getCondVersion());
			
			if (GsConfig.getInstance().isPuidCtrl()) {
				verJson.put("puidCtrl", 1);
			} else {
				verJson.put("puidCtrl", 0);
			}
			
			String newPackVersion = GsConfig.getInstance().getNewPackVersion();
			if (!HawkOSOperator.isEmptyString(newPackVersion)) {
				verJson.put("newPackVersion", newPackVersion);
			}
			
			// 加载成功即赋值
			HawkLog.logPrintln("server version status: {}", verJson.toJSONString());

			
			//把从区的信息也写入到这里面
			List<String> mergedServerList = AssembleDataManager.getInstance().getMergedServerList(appCfg.getServerId());
			if (!CollectionUtils.isEmpty(mergedServerList)) {
				for (String mergeServerId : mergedServerList) {
					RedisProxy.getInstance().updateServerCondCfg(mergeServerId, verJson);
				}
			} else {
				// 把服务器条件配置存储到缓存中
				RedisProxy.getInstance().updateServerCondCfg(appCfg.getServerId(), verJson);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return false;
	}
	
	/**
	 * 将拆服、合服涉及的活动表刷新到redis中
	 */
	public void flushActivityTable() {
		try {
			//拆服表
			ConfigIterator<SeparateActivityCfg> iter = HawkConfigManager.getInstance().getConfigIterator(SeparateActivityCfg.class);
			String[] separateTables = new String[iter.size()];
			int index = 0;
			while (iter.hasNext()) {
				SeparateActivityCfg cfg = iter.next();
				separateTables[index++] = cfg.getActivity();
			}
			RedisProxy.getInstance().getRedisSession().sAdd("separate_activity_tables", 0, separateTables);
			
			//合服表
			List<String> mergeTables = MergeServerConfig.getInstance().getSaveActivityTableList();
			RedisProxy.getInstance().getRedisSession().sAdd("merge_activity_tables", 0, mergeTables.toArray(new String[mergeTables.size()]));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 初始化应用对象管理器
	 * 
	 * @return
	 */
	private boolean initAppObjMan() {
		HawkObjManager<HawkXID, HawkAppObj> objMan = null;

		// 创建玩家管理器
		objMan = createObjMan(GsConst.ObjType.PLAYER, true, GsConfig.getInstance().getPlayerCacheTime());

		// 创建全局管理器, 并注册应用对象
		objMan = createObjMan(GsConst.ObjType.MANAGER, true);
		objMan.allocObject(getXid(), this);

		// 联盟军演管理器
		createObjMan(GsConst.ObjType.LMJYAOGUAN_ROOM, true, TimeUnit.HOURS.toMillis(1));
		
		// 联盟军演管理器
		createObjMan(GsConst.ObjType.TBLYAOGUAN_ROOM, true, TimeUnit.HOURS.toMillis(2));
		createObjMan(GsConst.ObjType.SWAOGUAN_ROOM, true, TimeUnit.HOURS.toMillis(5));
		createObjMan(GsConst.ObjType.CYBORGAOGUAN_ROOM, true, TimeUnit.HOURS.toMillis(2));
		createObjMan(GsConst.ObjType.DYZZAOGUAN_ROOM, true, TimeUnit.HOURS.toMillis(2));
		createObjMan(GsConst.ObjType.YQZZAOGUAN_ROOM, true, TimeUnit.HOURS.toMillis(48));
		createObjMan(GsConst.ObjType.XHJZAOGUAN_ROOM, true, TimeUnit.HOURS.toMillis(2));
		createObjMan(GsConst.ObjType.FGYLAOGUAN_ROOM, true, TimeUnit.HOURS.toMillis(1));
		createObjMan(GsConst.ObjType.XQHXAOGUAN_ROOM, true, TimeUnit.HOURS.toMillis(1));
		
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.HLG_ROOMMANAGER));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.TBLY_ROOMMANAGER));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.SW_ROOMMANAGER));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.CYBORG_ROOMMANAGER));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.DYZZ_ROOMMANAGER));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.YQZZ_ROOMMANAGER));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.XHJZ_ROOMMANAGER));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.FGYL_ROOMMANAGER));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.XQHX_ROOMMANAGER));
		// 充值管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.RECHARGE));
		// 道具管理
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.ITEM));

		// 创建聊天管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.CHAT));

		// 创建联盟管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.GUILD));
		
		// 创建邮件管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.MAILMANAGER));

		// 世界点管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WORLDPOINT));

		// 世界玩家点管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WORLDPLAYER));

		// 世界怪物点管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WORLDMONSTER));

		// 世界资源点管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WORLDRESOURCE));

		// 世界据点管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WORLD_STRONGPOINT));
		
		// 世界迷雾点管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WORLD_FOGGY));

		// 世界机甲管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WORLDGUNDAM));
		
		// 年兽管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WORLDNIAN));
		
		// 寻宝管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.TREASURE_HUNT));
		
		// 战地之王管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WAR_FLAG));

		// 世界行军管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WORLDMARCH));

		// 领地管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.MANOR));

		// 活动管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.ACTIVITY));

		// 国王战管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.PRESIDENT));
		
		// 国王战管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.SUPER_WEAPON));

		// 战斗管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.BATTLE));

		// 推送服务
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.PUSHER));

		// 搜索服务
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.SEARCHER));

		// 排行管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.RANK));


		// 问卷系统管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.QUESTIONNAIRE));

		// 尤里复仇活动管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.YURI_REVENGE));

		// 活动服务类
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.SERVER_ACTIVITY));

		// 好友系统
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.RELATION));

		// 全服buff
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.GLOBAL_BUFF));

		// 定时服务
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.TIMER));

		// 跨服处理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.CROSS_SERVER));
		
		// 跨服处理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.CROSS_ACTIVITY));
				
		//聯盟戰爭副本
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WAR_COLLEGE));
		
		// 军魂承接管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.INHERIT));
		// 军魂承接(新)管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.INHERIT_NEW));
		// 资源宝库点管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.RES_TREASURE));
		
		// 军事学院管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.MILITARY_COLLEGE));
		
		// 泰伯利亚之战管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.TIBERIUM_WAR));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.TBLY_WAR));
		// 泰伯利亚联赛管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.TIBERIUM_LEAGUA_WAR));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.TBLY_SEASON));
		// 星球大战阶段管理器
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.STAR_WARS_ACTIVITY));
		// 要塞
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.CROSS_FORTRESS));
		// 联盟锦标赛
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.CHAMPIONSHIP));
		
		// 机器人
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WORLD_ROBOT));
		
		// 能量塔
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WORLD_PYLON));
		
		//攻防模拟战.
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.SIMULATE_WAR));
		//圣诞大战
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.CHRISMAS_WAR));
		
		// 雪球
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.WORLD_SNOWBALL));
		// 赛博之战
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.CYBORG_WAR));
		// 赛博赛季
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.CYBORG_LEAGUA));
		
		// 老玩家数据
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.BACK_FLOW));
		// 司令技能
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.CROSS_SKILL));
		//小站区
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.XIAO_ZHAN_QU));

		//方尖碑
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.OBELISK));
		
		// 国家
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.NATIONAL));
		
		//达雅之战
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.DYZZ_WAR));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.DYZZ_MATCH));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.DYZZ_SEASON));
		//月球之战
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.YQZZ_MATCH));
		//星海激战
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.XHJZ_WAR));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.SHOP));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.GUILD_TEAM));
		
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.FGYL_WAR));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.CMW));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.XQHX_WAR));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.AUTO_MASS_JON));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.METERIAL_TRAN));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.CROSS_ACTIVITY_SEASON));
		createObj(HawkXID.valueOf(GsConst.ObjType.MANAGER, GsConst.ObjId.HOME_LAND));
		return true;
	}

	/**
	 * 更新ip控制
	 */
	public boolean updateIpControl() {
		// 黑白名单功能
		try {
			HawkIptablesManager.getInstance().setIpUsage(appCfg.getIptablesUsage());

			HawkIptablesManager.getInstance().clearWhiteIp();
			HawkIptablesManager.getInstance().clearBlackIp();

			int size = HawkConfigManager.getInstance().getConfigSize(IpCfg.class);
			for (int i = 0; i < size; i++) {
				IpCfg ipCfg = HawkConfigManager.getInstance().getConfigByIndex(IpCfg.class, i);
				if (ipCfg.getValue() > 0) {
					HawkIptablesManager.getInstance().addWhiteIp(ipCfg.getIp());
				} else {
					HawkIptablesManager.getInstance().addBlackIp(ipCfg.getIp());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);

			return false;
		}

		return true;
	}

	/**
	 * 更新安全协议
	 */
	public void updateSecProto() {
		try {
			HawkPPSSecurity.clearSecurityProtoId();
			
			ConfigIterator<SecProtoCfg> cfgIt = HawkConfigManager.getInstance().getConfigIterator(SecProtoCfg.class);
			for (SecProtoCfg cfg : cfgIt) {
				HawkPPSSecurity.addSecurityProtoId(cfg.getId(), cfg.getPps());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 初始化屏蔽字库
	 */
	public boolean updateWords() {
		try {
			wordFilter.clearWords();
			ConfigIterator<BlockWordCfg> cfgIt = HawkConfigManager.getInstance().getConfigIterator(BlockWordCfg.class);
			for (BlockWordCfg cfg : cfgIt) {
				wordFilter.addWord(cfg.getName());
			}
		} catch (Exception e) {
			HawkException.catchException(e);

			return false;
		}

		return true;
	}

	/**
	 * 初始化高级屏蔽字库
	 */
	public boolean updateSlienceWords() {
		try {
			silenceWordFilter.clearWords();
			ConfigIterator<SilenceWordCfg> cfgIt = HawkConfigManager.getInstance().getConfigIterator(SilenceWordCfg.class);
			for (SilenceWordCfg cfg : cfgIt) {
				silenceWordFilter.addWord(cfg.getName());
			}
		} catch (Exception e) {
			HawkException.catchException(e);

			return false;
		}

		return true;
	}

	/**
	 * 获取屏蔽词过滤器
	 * 
	 * @return
	 */
	public HawkWordFilter getWordFilter() {
		return wordFilter;
	}

	/**
	 * 获取高级屏蔽词过滤器
	 * 
	 * @return
	 */
	public HawkWordFilter getSilenceWordFilter() {
		return silenceWordFilter;
	}

	/**
	 * 获取开服时间
	 * 
	 * @return
	 */
	public long getServerOpenTime() {
		return serverOpenTime;
	}

	/**
	 * 是否初始化完成
	 * 
	 * @return
	 */
	public boolean isInitOK() {
		return initOK;
	}

	/**
	 * 报告异常信息(主要通过邮件)
	 * 
	 * @param e
	 */
	@Override
	public void reportException(Throwable throwable, Object... params) {
		if (GsConfig.getInstance().isDebug()) {
			if (GsConfig.getInstance().isReportException2Feishu()) {
				ExceptionReportUtil.reportException2Feishu(GsConfig.getInstance().getTargetQAUrl(), throwable, params);
				return;
			} else if (!initOK && GsConfig.getInstance().isSpecialServer()) {
				ExceptionReportUtil.reportException2Feishu(GsConfig.getInstance().getTargetCehuaUrl(), throwable, params);
				return;
			}
		} 
		
		ExceptionReportUtil.reportException2Mail(throwable, params);
	}
	
	/**
	 * 帧更新
	 */
	@Override
	public boolean onTick() {	
		if (super.onTick()) {
			return true;
		}
		return false;
	}

	/**
	 * 主循环启动前的回调, 主要用来初始化功能逻辑线程
	 */
	@Override
	protected void onAppRunning() {
		// 初始化世界线程
		WorldThreadScheduler.getInstance().startWorldThread(appCfg.getTickPeriod());

		// 通知应用程序启动
		super.onAppRunning();
	}

	/**
	 * 停服关闭
	 */
	@Override
	protected void onClosed() {
		try {
			// 世界点刷新出去
			WorldPointProxy.getInstance().flush();
			
			// 关闭活跃玩家会话
			Set<Player> activePlayers = GlobalData.getInstance().getOnlinePlayers();
			for (Player player : activePlayers) {				
				try {
					player.notifyPlayerKickout(Status.SysError.SERVER_CLOSE_VALUE, "");
					postMsg(player, SessionClosedMsg.valueOf());
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			
			//服务器停服标识（0: 未停服 1: 已停服）
			RoleExchangeService.getInstance().updateServerState(ServerState.SERVER_STOPPED);
			
			//跨服出去的玩家如果session还在的话说明是在跨服的状态, 发一条退出的协议,然后跨服.
			Set<String> emigrationPlayerIds = CrossService.getInstance().getEmigrationPlayers().keySet();
			for (String playerId : emigrationPlayerIds) {
				Player player = GlobalData.getInstance().queryPlayer(playerId);
				if (player != null && player.getSession() != null) {
					player.notifyPlayerKickout(Status.SysError.SERVER_CLOSE_VALUE, "");
					//设置session是防止极限情况下, 所跨的那个服也发了一个服务器关闭的协议过来.
					player.setSession(null);
				}				
			}
			
			// 关闭积分上报
			QQScoreBatch.getInstance().close();
			
			// 等一会客户端处理踢玩家的协议
			HawkOSOperator.osSleep(3000);
			
			// 存储全局数据
			GlobalData.getInstance().saveGlobalData();
			
			//联盟帮助
			GuildService.getInstance().saveGuildHelpInfoTable();
			
			// 联盟任务
			GuildService.getInstance().updateGuildTaskList();
			
			ActivityManager.getInstance().shutdown();
			
			// 泰伯利亚-缓存变更数据写入redis
			TiberiumWarService.getInstance().updateGuildTeamChanges();
			
			// 跨服活动
			CrossActivityService.getInstance().shutdown();
			//方尖碑缓存更新到redis中
			ObeliskService.getInstance().onClose();

			// 定时写入当前的已注册数目和在线数目
			int registerCount = GlobalData.getInstance().getRegisterCount();
			GlobalData.getInstance().resetServerStatus(GsConfig.getInstance().getServerId(), registerCount, 0, HawkTime.getSeconds());

			// 清理tlog的在线数据
			GlobalData.getInstance().checkOnlineInfo();

			// 关闭日志
			GameLog.getInstance().close();

			// 安全SDK卸载
			TssSdkManager.getInstance().tssSdkUnLoad();

			// 关闭世界线程
			WorldThreadScheduler.getInstance().close();
			
			if (GsConfig.getInstance().isZonineEnable()) {
				ZonineSDK.getInstance().close();
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		super.onClosed();
	}

	/**
	 * 服务关闭前各个业务逻辑的关闭前操作
	 */
	@Override
	public void onShutdown() {
		try {
			// 联盟领地操作
			if (GuildManorService.getInstance() != null) {
				GuildManorService.getInstance().beforeServerShutDown();
			}
			
			if (MailManager.getInstance() != null) {
				MailManager.getInstance().reissueMail();
			}
			
			// 国家服务停服处理
			NationService.getInstance().onShutdown();
			
			// 星甲召唤停服处理
			SpaceMechaService.getInstance().onShutdown();
			
			// 账号复制服务停服关闭
			PlayerCopyService.getInstance().shutdown();
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		super.onShutdown();
	}

	/**
	 * 创建应用对象
	 */
	@Override
	protected HawkAppObj onCreateObj(HawkXID xid) {
		HawkAppObj appObj = null;
		if (xid.getType() == GsConst.ObjType.PLAYER) {
			appObj = PlayerFactory.getInstance().newPlayer(xid);
		} else if (xid.getType() == GsConst.ObjType.LMJYAOGUAN_ROOM){
			appObj = new LMJYBattleRoom(xid);
		} else if (xid.getType() == GsConst.ObjType.TBLYAOGUAN_ROOM){
			appObj = new TBLYBattleRoom(xid);
		}else if (xid.getType() == GsConst.ObjType.XHJZAOGUAN_ROOM){
			appObj = new XHJZBattleRoom(xid);
		}else if (xid.getType() == GsConst.ObjType.FGYLAOGUAN_ROOM){
			appObj = new FGYLBattleRoom(xid);
		}else if (xid.getType() == GsConst.ObjType.XQHXAOGUAN_ROOM){
			appObj = new XQHXBattleRoom(xid);
		} else if (xid.getType() == GsConst.ObjType.SWAOGUAN_ROOM){
			appObj = new SWBattleRoom(xid);
		} else if (xid.getType() == GsConst.ObjType.CYBORGAOGUAN_ROOM){
			appObj = new CYBORGBattleRoom(xid);
		} else if (xid.getType() == GsConst.ObjType.DYZZAOGUAN_ROOM){
			appObj = new DYZZBattleRoom(xid);
		}else if (xid.getType() == GsConst.ObjType.YQZZAOGUAN_ROOM){
			appObj = new YQZZBattleRoom(xid);
		} else if (xid.getType() == GsConst.ObjType.MANAGER) {
			if (xid.getId() == GsConst.ObjId.CHAT) {
				appObj = new ChatService(xid);
			} else if (xid.getId() == GsConst.ObjId.GUILD) {
				appObj = new GuildService(xid);
			} else if (xid.getId() == GsConst.ObjId.WORLDPOINT) {
				appObj = new WorldPointService(xid);
			} else if (xid.getId() == GsConst.ObjId.WORLDPLAYER) {
				appObj = new WorldPlayerService(xid);
			} else if (xid.getId() == GsConst.ObjId.WORLDMONSTER) {
				appObj = new WorldMonsterService(xid);
			} else if (xid.getId() == GsConst.ObjId.WORLDRESOURCE) {
				appObj = new WorldResourceService(xid);
			} else if (xid.getId() == GsConst.ObjId.WORLD_STRONGPOINT) {
				appObj = new WorldStrongPointService(xid);
			} else if (xid.getId() == GsConst.ObjId.WORLD_FOGGY) {
				appObj = new WorldFoggyFortressService(xid);
			} else if (xid.getId() == GsConst.ObjId.WORLDMARCH) {
				appObj = new WorldMarchService(xid);
			} else if (xid.getId() == GsConst.ObjId.RECHARGE) {
				appObj = new RechargeManager(xid);
			} else if (xid.getId() == GsConst.ObjId.ITEM) {
				appObj = new ItemService(xid);
			} else if (xid.getId() == GsConst.ObjId.PUSHER) {
				appObj = new PushService(xid);
			} else if (xid.getId() == GsConst.ObjId.SEARCHER) {
				appObj = new SearchService(xid);
			} else if (xid.getId() == GsConst.ObjId.RANK) {
				appObj = new RankService(xid);
			} else if (xid.getId() == GsConst.ObjId.MANOR) {
				appObj = new GuildManorService(xid);
			} else if (xid.getId() == GsConst.ObjId.ACTIVITY) {
				HawkRedisSession localRedisSession = LocalRedis.getInstance().getRedisSession();
				HawkRedisSession globalRedisSession= RedisProxy.getInstance().getRedisSession();
				appObj = new ActivityManager(xid, localRedisSession, globalRedisSession, new GameActivityDataProxy());
			} else if (xid.getId() == GsConst.ObjId.PRESIDENT) {
				appObj = new PresidentFightService(xid);
			} else if (xid.getId() == GsConst.ObjId.BATTLE) {
				appObj = new BattleService(xid);
			} else if (xid.getId() == GsConst.ObjId.QUESTIONNAIRE) {
				appObj = new QuestionnaireService(xid);
			} else if (xid.getId() == GsConst.ObjId.SERVER_ACTIVITY) {
				appObj = new ActivityService(xid);
			} else if (xid.getId() == GsConst.ObjId.YURI_REVENGE) {
				appObj = new YuriRevengeService(xid);
			} else if (xid.getId() == GsConst.ObjId.RELATION) {
				appObj = new RelationService(xid);
			} else if (xid.getId() == GsConst.ObjId.GLOBAL_BUFF) {
				appObj = new BuffService(xid);
			} else if (xid.getId() == GsConst.ObjId.TIMER) {
				appObj = new GlobalTimerService(xid);
			} else if (xid.getId() == GsConst.ObjId.SUPER_WEAPON) {
				appObj = new SuperWeaponService(xid);
			} else if (xid.getId() == GsConst.ObjId.MAILMANAGER) {
				appObj = new MailManager(xid);
			} else if (xid.getId() == GsConst.ObjId.WAR_COLLEGE) {
				appObj = new WarCollegeInstanceService(xid);
			} else if (xid.getId() == GsConst.ObjId.HLG_ROOMMANAGER) {
				appObj = new LMJYRoomManager(xid);
			} else if (xid.getId() == GsConst.ObjId.TBLY_ROOMMANAGER) {
				appObj = new TBLYRoomManager(xid);
			} else if (xid.getId() == GsConst.ObjId.XHJZ_ROOMMANAGER) {
				appObj = new XHJZRoomManager(xid);
			} else if (xid.getId() == GsConst.ObjId.FGYL_ROOMMANAGER) {
				appObj = new FGYLRoomManager(xid);
			} else if (xid.getId() == GsConst.ObjId.XQHX_ROOMMANAGER) {
				appObj = new XQHXRoomManager(xid);
			} else if (xid.getId() == GsConst.ObjId.CYBORG_ROOMMANAGER) {
				appObj = new CYBORGRoomManager(xid);
			} else if (xid.getId() == GsConst.ObjId.SW_ROOMMANAGER) {
				appObj = new SWRoomManager(xid);
			} else if (xid.getId() == GsConst.ObjId.DYZZ_ROOMMANAGER) {
				appObj = new DYZZRoomManager(xid);
			} else if (xid.getId() == GsConst.ObjId.YQZZ_ROOMMANAGER) {
				appObj = new YQZZRoomManager(xid);
			} else if (xid.getId() == GsConst.ObjId.WORLDGUNDAM) {
				appObj = new WorldGundamService(xid);
			} else if (xid.getId() == GsConst.ObjId.CROSS_SERVER) {
				appObj = new CrossService(xid);
			} else if (xid.getId() == GsConst.ObjId.CROSS_ACTIVITY) {
				appObj = new CrossActivityService(xid);
			} else if (xid.getId() == GsConst.ObjId.WORLDNIAN) {
				appObj = new WorldNianService(xid);
			} else if (xid.getId() == GsConst.ObjId.TREASURE_HUNT) {
				appObj = new WorldTreasureHuntService(xid);
			}  else if (xid.getId() == GsConst.ObjId.CROSS_SKILL) {
				appObj = new CrossSkillService(xid);
			} else if (xid.getId() == GsConst.ObjId.INHERIT) {
				appObj = new InheritService(xid);
			} else if (xid.getId() == GsConst.ObjId.WAR_FLAG) {
				appObj = new WarFlagService(xid);
			} else if (xid.getId() == GsConst.ObjId.RES_TREASURE) {
				appObj = new WorldResTreasurePointService(xid);
			} else if (xid.getId() == GsConst.ObjId.MILITARY_COLLEGE) {
				appObj = new CollegeService(xid);
			}else if (xid.getId() == GsConst.ObjId.TIBERIUM_WAR) {
				appObj = new TiberiumWarService(xid);
			}else if (xid.getId() == GsConst.ObjId.TIBERIUM_LEAGUA_WAR) {
				appObj = new TiberiumLeagueWarService(xid);
			}else if (xid.getId() == GsConst.ObjId.STAR_WARS_ACTIVITY) {
				appObj = new StarWarsActivityService(xid);
			} else if(xid.getId() == GsConst.ObjId.CROSS_FORTRESS) {
				appObj = new CrossFortressService(xid);
			} else if(xid.getId() == GsConst.ObjId.CHAMPIONSHIP) {
				appObj = new ChampionshipService(xid);
			} else if(xid.getId() == GsConst.ObjId.WORLD_ROBOT) {
				appObj = new WorldRobotService(xid);
			} else if(xid.getId() == GsConst.ObjId.WORLD_PYLON) {
				appObj = new WorldPylonService(xid);
			} else if (xid.getId() == GsConst.ObjId.SIMULATE_WAR) {
				appObj = new SimulateWarService(xid);
			} else if (xid.getId() == GsConst.ObjId.CHRISMAS_WAR) {
				appObj = new WorldChristmasWarService(xid);
			}  else if(xid.getId() == GsConst.ObjId.WORLD_SNOWBALL) {
				appObj = new WorldSnowballService(xid);
			}  else if(xid.getId() == GsConst.ObjId.CYBORG_WAR) {
				appObj = new CyborgWarService(xid);
			}  else if(xid.getId() == GsConst.ObjId.CYBORG_LEAGUA) {
				appObj = new CyborgLeaguaWarService(xid);
			} else if(xid.getId() == GsConst.ObjId.BACK_FLOW) {
				appObj = new BackFlowService(xid);
			}  else if (xid.getId() == GsConst.ObjId.INHERIT_NEW) {
				appObj = new InheritNewService(xid);
			} else if(xid.getId() == GsConst.ObjId.XIAO_ZHAN_QU){
				appObj = new XZQService(xid);
			} else if (xid.getId() == GsConst.ObjId.OBELISK) {
				appObj = new ObeliskService(xid);
			} else if (xid.getId() == GsConst.ObjId.NATIONAL) {
				appObj = new NationService(xid);
			} else if(xid.getId() == GsConst.ObjId.DYZZ_WAR){
				appObj = new DYZZService(xid);
			} else if(xid.getId() == GsConst.ObjId.DYZZ_MATCH){
				appObj = new DYZZMatchService(xid);
			}else if(xid.getId() == GsConst.ObjId.DYZZ_SEASON){
				appObj = new DYZZSeasonService(xid);
			}else if(xid.getId() == GsConst.ObjId.YQZZ_MATCH){
				appObj = new YQZZMatchService(xid);
			}else if(xid.getId() == GsConst.ObjId.XHJZ_WAR){
				appObj = new XHJZWarService(xid);
			}else if(xid.getId() == GsConst.ObjId.SHOP){
				appObj = new ShopService(xid);
			}else if(xid.getId() == GsConst.ObjId.GUILD_TEAM){
				appObj = new GuildTeamService(xid);
			}else if(xid.getId() == GsConst.ObjId.TBLY_WAR){
				appObj = new TBLYWarService(xid);
			}else if(xid.getId() == GsConst.ObjId.FGYL_WAR){
				appObj = new FGYLMatchService(xid);
			}else if(xid.getId() == GsConst.ObjId.TBLY_SEASON){
				appObj = new TBLYSeasonService(xid);
			}else if(xid.getId() == GsConst.ObjId.CMW){
				appObj = new CMWService(xid);
			}else if(xid.getId() == GsConst.ObjId.XQHX_WAR){
				appObj = new XQHXWarService(xid);
			}else if(xid.getId() == GsConst.ObjId.AUTO_MASS_JON){
				appObj = new GuildAutoMarchService(xid);
			}else if(xid.getId() == GsConst.ObjId.METERIAL_TRAN){
				appObj = new MeterialTransportService(xid);
			}else if(xid.getId() == GsConst.ObjId.CROSS_ACTIVITY_SEASON){
				appObj = new CrossActivitySeasonService(xid);
			}else if(xid.getId() == GsConst.ObjId.HOME_LAND){
				appObj = new HomeLandService(xid);
			}
			
		}

		if (appObj == null) {
			HawkLog.errPrintln("create obj failed: {}", xid);
		}
		return appObj;
	}

	/**
	 * 配置文件的整理组装, 主要用来组装关联数据构成
	 * 
	 * @return
	 */
	@Override
	public boolean assembleConfigData() {
		
		// 先清理已组装的数据
		AssembleDataManager.getInstance().clearData();
		
		// 重新组装新数据
		AssembleDataManager.getInstance().doAssemble();
		if(!AssembleDataManager.getInstance().checkConfigData()){
			return false;
		}
		
		// 添加协议时间间隔
		ConfigIterator<ProtoElapseCfg> peIter = HawkConfigManager.getInstance().getConfigIterator(ProtoElapseCfg.class);
		while(peIter.hasNext()) {
			ProtoElapseCfg cfg = peIter.next();
			HawkNetworkManager.getInstance().addProtoElapse(cfg.getProtoId(), cfg.getElapse());
		}
		
		return super.assembleConfigData();
	}

	/**
	 * 配置重加载成功
	 * 
	 * @param storage
	 * @return
	 */
	public boolean onConfigReload(HawkConfigStorage storage) {
		return super.onConfigReload(storage);
	}

	/**
	 * 服务器是否开启
	 * 
	 * @return
	 */
	public boolean isServerOpened() {
		if (serverOpenTime > 0 && currentTime >= serverOpenTime) {
			return true;
		}
		return false;
	}

	/**
	 * 回话开启的处理, io线程直接调用
	 */
	@Override
	public boolean onSessionOpened(HawkSession session) {
		if (!isServerOpened()) {
			// 记录未开服
			HawkLog.logPrintln("server not opened, server will open at: {}", HawkTime.formatTime(GameUtil.getServerOpenTime()));

			// 发送错误码
			HPErrorCode.Builder builder = HPErrorCode.newBuilder();
			builder.setHpCode(HP.code.LOGIN_C_VALUE);
			builder.setErrCode(Status.SysError.SERVER_NOT_OPENED_VALUE);
			builder.setErrFlag(1);
			session.sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder));

			// 延迟关闭
			final HawkSession closeSession = session;
			this.addDelayAction(2000, new HawkDelayAction() {
				@Override
				protected void doAction() {
					closeSession.close();
				}
			});
			
			return false;
		}

		// 检测最大会话数
		if (!checkSessionMaxSize(session)) {
			return false;
		}
				
		return super.onSessionOpened(session);
	}

	/**
	 * 
	 */
	private void registerTickable() {
		int redisCheckPeriod = HawkAppCfg.getInstance().getDbRiskCheckPeriod();
		addTickable(new HawkPeriodTickable(redisCheckPeriod) {
			@Override
			public void onPeriodTick() {
				String serverId = GsConfig.getInstance().getServerId();
				long retCode = RedisProxy.getInstance().getRedisSession().hSet("server_identify", serverId, getServerIdentify());
				redisInException = retCode < 0 ? true : false;
			}
		});
		
		addTickable(new HawkPeriodTickable(5000) {
			@Override
			public void onPeriodTick() {
				// 开启排队登录session队列检测
				if (!GsConfig.getInstance().isLoginWaitQueue()) {
					return;
				}
				
				int onlineCount = GlobalData.getInstance().getOnlineUserCount();
				int maxOnlineCnt = GlobalData.getInstance().getServerSettingData().getMaxOnlineCount();
				if (onlineCount < maxOnlineCnt && !waitSessionQueue.isEmpty()) {
					int diff = maxOnlineCnt - onlineCount;
					diff = Math.min(10, diff);
					synchronized (waitSessionQueue) {
						diff = Math.min(waitSessionQueue.size(), diff);
						while (diff-- > 0) {
							HawkSession waitingSession = waitSessionQueue.poll();
							if (waitingSession != null) {
								// 通知前端可以登录了
								waitingSession.setUserObject(GsConst.LOGIN_WAIT_KEY, WaitLoginState.ALLOW_LOGIN);
								HPWaitLogin.Builder builder = HPWaitLogin.newBuilder();
								builder.setWaitLoginCode(WaitLoginCode.ALLOW_LOGIN_VALUE);
								waitingSession.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_PREPARE_SYNC_S, builder));
								GlobalData.getInstance().decLoginWait(waitingSession.getUserObject("channel"), 1);
								waitingSession.clearUserObject("channel");
								waitingSession.setUserObject("allowLoginTs", HawkApp.getInstance().getCurrentTime());
								
								HawkLog.logPrintln("waitlogin status, openid: {}, allow login, session: {}, onlineCnt: {}", 
										waitingSession.getUserObject("openid"), waitingSession, GlobalData.getInstance().getOnlineUserCount());
							}
						}
					}
				}
			}
		});
	}
	
	/**
	 * 检测最大的会话在线数
	 * 
	 * @return
	 */
	public boolean checkSessionMaxSize(HawkSession session) {
		try {
			// 当前最大协议数判断
			int maxSessionSize = GlobalData.getInstance().getServerSettingData().getMaxOnlineCount();
			if (maxSessionSize <= 0) {
				return true;
			}
			
			if (GlobalData.getInstance().getOnlineUserCount() < maxSessionSize) {
				return true;
			}
			
			if (GsConfig.getInstance().isLoginWaitQueue()) {
				return true;
			}

			// 记录日志
			HawkLog.logPrintln("connection refused by pcu limit, sessionSize: {}, activePlayer: {}", 
					HawkNetworkManager.getInstance().getSessionCount(), GlobalData.getInstance().getOnlineUserCount());
			
			// 发送通知
			HawkProtocol protocol = onRefuseByOverload(session);
			if (protocol != null) {
				session.sendProtocol(protocol);
			}
			
			// 延迟关闭
			final HawkSession closeSession = session;
			this.addDelayAction(2000, new HawkDelayAction() {
				@Override
				protected void doAction() {
					closeSession.close();
				}
			});
			
			return false;
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return true;
	}

	/**
	 * 回话开启被ip控制器限制的处理, io线程直接调用
	 */
	@Override
	public HawkProtocol onRefuseByIptables(String ip, int usage) {
		HPErrorCode.Builder builder = HPErrorCode.newBuilder();
		builder.setHpCode(HP.code.LOGIN_C_VALUE);
		builder.setErrCode(Status.SysError.SERVER_GRAY_STATE_VALUE);
		builder.setErrFlag(1);
		return HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder);
	}

	/**
	 * 会话关闭处理, io线程直接调用
	 */
	@Override
	public void onSessionClosed(HawkSession session) {
		if (this.isWaitingClose() || !isServerOpened()) {
			return;
		}
		
		if (GsConfig.getInstance().isLoginWaitQueue()) {
			synchronized (waitSessionQueue) {
				if (waitSessionQueue.contains(session)) {
					waitSessionQueue.remove(session);
					GlobalData.getInstance().decLoginWait(session.getUserObject("channel"), 1);
					HawkLog.logPrintln("waitlogin status, wait session remove, openid: {}, session: {}, onlineCnt: {}", 
							session.getUserObject("openid"), session, GlobalData.getInstance().getOnlineUserCount());
				}
			}
		}
		
		Player player = (Player)session.getAppObject();
		if (session != null && player != null && player.getData() != null) {			
			String toCrossServerId = CrossService.getInstance().getEmigrationPlayerServerId(player.getId());
			//防止主动发起关闭的时候, 再次触发.
			if (!HawkOSOperator.isEmptyString(toCrossServerId)) {
				//暂时找不到更好的方法了,只能是在这几处手动调用增删
				GlobalData.getInstance().changePfOnlineCnt(player, false);
				if (player.getSession() == session) {
					//在跨服的时候抛出一个
					CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(CHP.code.INNER_LOGOUT_VALUE), toCrossServerId, player.getId());				
					//在跨服的时候退出由跨过去的服务器处理,需要在这里再清理一次session.
					player.setSession(null);
				}				
			} else {
				postMsg(session.getAppObject(), SessionClosedMsg.valueOf());
			}			
		}
		
		super.onSessionClosed(session);
	}
	
	/**
	 * 会话协议回调, 由IO线程直接调用, 非线程安全
	 */
	@Override
	public boolean onSessionProtocol(HawkSession session, HawkProtocol protocol) {
		// 判断系统或协议是否已被关闭
		if (isProtocolClosed(protocol.getType())) {
			HPErrorCode.Builder builder = HPErrorCode.newBuilder();
			builder.setHpCode(protocol.getType());
			builder.setErrCode(Status.SysError.MODULE_CLOSED_VALUE);
			builder.setErrFlag(0);
			session.sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder));
			
			HawkLog.debugPrintln("protocol has been closed, protocol: {}", protocol.getType());
			return true;
		}

		if (protocol.checkType(script.REQUEST_VALUE) && GsConfig.getInstance().getServerType() != ServerType.NORMAL) {
			return ScriptProxy.onProtocol(session, protocol);
		}
		
		// 服务器未开启的处理
		if (!isServerOpened()) {
			return true;
		}

		// 玩家协议解密
		protocol = ProtoUtil.decryptionProtocol(session, protocol);
		if (protocol == null) {
			return false;
		}

		long protoTime = HawkTime.getMillisecond();
		try {
			// db异常情况下直接回复心跳
			if (protocol.checkType(HP.sys.HEART_BEAT_VALUE)) {
				if (HawkDBManager.getInstance().isDbInException() || redisInException) {
					LoginUtil.onPlayerHeartBeat(protocol);
					return true;
				}
			}
			
			// 绑定对象的会话, 直接交由基类框架进行协议处理
			if (session.getAppObject() != null) {				
				return dispatchProtocol(protocol, session);
			}
			
			// 设备激活协议
			if (protocol.checkType(HP.code.DEVICE_ACTIVE_C)) {
				LoginUtil.doDeviceActive(protocol, session);
				return true;
			}

			// 非登陆协议不予以处理
			if (!protocol.checkType(HP.code.LOGIN_C) && !protocol.checkType(HP.code.LOGIN_WAIT_C)) {
				HawkLog.errPrintln("session appobj null cannot process unlogin protocol: {}", protocol.getType());
				return false;
			}
			
			HPLogin cmd = null;
			// 这里只有是login_c协议才解析
			if (protocol.checkType(HP.code.LOGIN_C)) {
				cmd = protocol.parseProtocol(HPLogin.getDefaultInstance());
				HawkLog.logPrintln("on session protocol, new serverId: {}", cmd.getServerId());
				boolean alreadyHandle = doLoginProtocol(session, protocol, cmd);
				if (alreadyHandle) {
					return true;
				}
			}
			
			// 最大会话数检测
			if (!checkSessionMaxSize(session)) {
				return true;
			}
			
			// 登录排队检测
			if (!waitLoginCheck(session, protocol)) {
				return true;
			}
			
			// LOGIN_WAIT_C协议时，排队登录检测通过了，就直接让玩家登录
			if (protocol.checkType(HP.code.LOGIN_WAIT_C)) {
				waitSessionQueue.remove(session);
				HPWaitLogin.Builder builder = HPWaitLogin.newBuilder();
				builder.setWaitLoginCode(WaitLoginCode.ALLOW_LOGIN_VALUE);
				session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_PREPARE_SYNC_S, builder));
				GlobalData.getInstance().decLoginWait(session.getUserObject("channel"), 1);
				return true;
			}
			
			String openid = session.getUserObject("openid");
			if (HawkOSOperator.isEmptyString(openid)) {
				HawkLog.errPrintln("openid null cannot process login protocol: {}", protocol.getType());
				HPLoginRet.Builder builder = HPLoginRet.newBuilder();
				builder.setErrCode(Status.SysError.OPENID_INVALID_VALUE);
				protocol.response(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
				return false;
			}
			
			String puid = GameUtil.getPuidByPlatform(openid, cmd.getPlatform());

			// 登录控制: puid白名单没校验过的时候, 进行设备激活校验, 若都未通过, 提示puid限制登录
			if (!puidLoginCheck(session, protocol, cmd)) {
				return false;
			}
			
			// playerId登录频率控制(3s一次)
			int loginElapse = GsConfig.getInstance().getLoginElapse();
			if (loginElapse > 0 && !HawkOSOperator.isEmptyString(puid)) {
				if (puidLoginTime.containsKey(puid) && protoTime <= puidLoginTime.get(puid) + loginElapse) {
					HPLoginRet.Builder builder = HPLoginRet.newBuilder();
					builder.setErrCode(Status.SysError.PLAYER_FREQUENT_LOGIN_VALUE);
					protocol.response(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
					return true;
				}
				puidLoginTime.put(puid, protoTime);
			}

			// 判断是否把登录投递到额外线程进行处理, 避免IO阻塞
			
			if (!GsConfig.getInstance().isLoginAsync()) {
				return doLoginProcess(session, protocol, protoTime);
			} else {				
				int threadIndex = Math.abs(puid.hashCode() % HawkTaskManager.getInstance().getExtraThreadNum());
				HawkTaskManager.getInstance().postExtraTask(new PlayerLoginTask(session, protocol), threadIndex);
			}
			return true;

		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		} finally {
			protoTime = HawkTime.getMillisecond() - protoTime;
			if (protoTime >= GsConfig.getInstance().getProtoTimeout()) {
				HawkLog.logPrintln("session protocol timeout, protocolId: {}, costtime: {}", protocol.getType(), protoTime);
			}
		}
	}
	
	
	/**
	 * 登录白名单校验.
	 * @param session
	 * @param protocol
	 * @param cmd
	 * @return
	 */
	private boolean puidLoginCheck(HawkSession session, HawkProtocol protocol, HPLogin cmd) {
		if (LoginUtil.isPuidCtrl()) {
			// openId白名单校验
			if (!LoginUtil.checkPuidCtrl(cmd.getPuid())) {
				// 是否开启设备激活
				if (GsConfig.getInstance().isDeviceNeedActive()) {
					// 设备激活校验
					if (!LoginUtil.checkDeviceActive(cmd, session)) {
						HPLoginRet.Builder builder = HPLoginRet.newBuilder();
						builder.setErrCode(Status.DeviceError.DEVICE_NOT_ACTIVE_VALUE);
						protocol.response(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
						return false;
					}
				} else {
					// 不在白名单之内不可登录
					HPLoginRet.Builder builder = HPLoginRet.newBuilder();
					builder.setErrCode(Status.SysError.PUID_CTRL_DISABLE_VALUE);
					protocol.response(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
					return false;
				}
			}
		} else {
			// 设备激活登录控制
			if (!LoginUtil.checkDeviceActive(cmd, session)) {
				HPLoginRet.Builder builder = HPLoginRet.newBuilder();
				builder.setErrCode(Status.DeviceError.DEVICE_NOT_ACTIVE_VALUE);
				protocol.response(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
				return false;
			}
		}
		
		return true;
	} 
	
	/**
	 * 处理登录协议
	 * @param protocol
	 * @param cmd
	 * @return
	 */
	private boolean doLoginProtocol(HawkSession session, HawkProtocol protocol, HPLogin cmd) {
		session.setUserObject("openid", cmd.getPuid());
		session.setUserObject("channel", cmd.getChannel().toLowerCase());
		String puid = GameUtil.getPuidByPlatform(cmd.getPuid(), cmd.getPlatform()); 
		String serverId = cmd.getServerId();
		List<String> serverList = GlobalData.getInstance().getMergeServerList(GsConfig.getInstance().getServerId());
		//没有合服 或者不在合服列表里面. 取本服.
		if (CollectionUtils.isEmpty(serverList) || !serverList.contains(serverId)) {
			serverId = GsConfig.getInstance().getServerId();
			//虽然传进来的没有改，是因为下面走doLoginProcess的时候依然会去修改HPLogin中的serverId
			cmd = cmd.toBuilder().setServerId(serverId).build();
		}		
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(puid, serverId);
		if (accountInfo != null) {
			//找到玩家是否跨服了
			String toCrossServerId = CrossService.getInstance().getEmigrationPlayerServerId(accountInfo.getPlayerId());
			if (!HawkOSOperator.isEmptyString(toCrossServerId)) {			
				//跨服玩家校验一下白名单 检查不通过的话,不让登录跨服.
				if (!puidLoginCheck(session, protocol, cmd)) {
					return true;
				}				
				
				long protoTime = HawkTime.getMillisecond();
				// playerId登录频率控制(3s一次)
				int loginElapse = GsConfig.getInstance().getLoginElapse();
				if (loginElapse > 0 && !HawkOSOperator.isEmptyString(puid)) {
					if ((puidLoginTime.containsKey(puid) && protoTime <= puidLoginTime.get(puid) + loginElapse)) {
						HPLoginRet.Builder builder = HPLoginRet.newBuilder();
						builder.setErrCode(Status.SysError.PLAYER_FREQUENT_LOGIN_VALUE);
						protocol.response(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
						return true;
					}
					puidLoginTime.put(puid, protoTime);
				}
				
				//修复 player的预跨服状态
				Player player = GlobalData.getInstance().queryPlayer(accountInfo.getPlayerId());
				if (player != null && player.getCrossStatus() == PlayerCrossStatus.PREPARE_CROSS) {
					player.setCrossStatus(PlayerCrossStatus.NOTHING);
				}
				
				//尝试修复玩家的状态. 修复成功代表修复了, 那么玩家
				String realServerId = GlobalData.getInstance().getMainServerId(accountInfo.getServerId());
				int crossType = RedisProxy.getInstance().getPlayerCrossType(realServerId, accountInfo.getPlayerId());
				boolean fixResult = false;
				if (crossType == CrossType.CROSS_VALUE){					
					//判断一下服务器是否关闭
					if (!CrossService.getInstance().isServerOpen(toCrossServerId)) {
						// 发送错误码
						HPErrorCode.Builder builder = HPErrorCode.newBuilder();
						builder.setHpCode(HP.code.LOGIN_C_VALUE);
						builder.setErrCode(Status.SysError.SERVER_NOT_OPENED_VALUE);
						builder.setErrFlag(1);
						session.sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder));
						
						return true;
					}
					
					fixResult = CrossService.getInstance().loginFixCrossPlayer(realServerId, accountInfo.getPlayerId());
				} else if (crossType == CrossType.TIBERIUM_VALUE){
					fixResult = CrossService.getInstance().loginFixTiberiumPlayer(realServerId, accountInfo.getPlayerId());
				} else if (crossType == CrossType.STAR_WARS_VALUE) {
					fixResult = CrossService.getInstance().loginFixStarWarsPlayer(realServerId, accountInfo.getPlayerId());
				} else if (crossType == CrossType.CYBORG_VALUE) {
					fixResult = CrossService.getInstance().loginFixCyborgPlayer(realServerId, accountInfo.getPlayerId());
				}else if(crossType == CrossType.DYZZ_VALUE){
					fixResult = CrossService.getInstance().loginFixDyzzPlayer(realServerId, accountInfo.getPlayerId());
				}else if(crossType == CrossType.YQZZ_VALUE){
					fixResult = CrossService.getInstance().loginFixYqzzPlayer(realServerId, accountInfo.getPlayerId());
				}else if(crossType == CrossType.XQHX_VALUE){
					fixResult = CrossService.getInstance().loginFixXqhxPlayer(realServerId, accountInfo.getPlayerId());
				}
				
				if (!fixResult) {							
					//这里还需要把Player初始化出来.因为要放一些特殊的协议过去.
					player = GlobalData.getInstance().makesurePlayer(accountInfo.getPlayerId());
					session.setAppObject(player);
					player.setHpLogin(cmd.toBuilder());
					
					//调用次数.
					GlobalData.getInstance().changePfOnlineCnt(player, true);
					//先把Session存起来.
					CrossService.getInstance().addPlayerIdSession(accountInfo.getPlayerId(), session);
					
					//CrossProxy.getInstance().sendNotify(protocol, toCrossServerId, accountInfo.getPlayerId());
					CrossProxy.getInstance().rpcRequest(protocol, new CrossPlayerLoginCallback(player), 
							toCrossServerId, accountInfo.getPlayerId(), accountInfo.getPlayerId());
					
					return true;
				} else {
					//因为在外面还有一次登录时间的校验,所以修复之后就吧登录时间去掉.防止后面的校验失败,不敢挪动原来代码的位置.
					if (loginElapse > 0 && !HawkOSOperator.isEmptyString(puid)) {
						puidLoginTime.remove(puid);
					}									
				}
				
			}
		}
		
		return false;
	}
	/**
	 * session绑定了对象之后.
	 * @param protocol
	 * @return
	 */
	private boolean dispatchProtocol(HawkProtocol protocol, HawkSession session) {
		Player player = (Player)session.getAppObject();
		if (player == null || player.getData() == null) {
			HawkLog.errPrintln("dispatchProtocol failed, player or playerdata empty, protocol: {}", protocol.getType());
			return false;
		}
		String targetServerId = CrossService.getInstance().getEmigrationPlayerServerId(player.getId());
		//这里还要过滤一些特殊的协议放给本服处理.				
		if (HawkOSOperator.isEmptyString(targetServerId) ) {
			return super.onSessionProtocol(session, protocol);										 
		} else {
			AssembleDataManager assembleDataManager = AssembleDataManager.getInstance();
			//跨服状态下原服处理的协议
			if (assembleDataManager.isCrossLocalProtocol(protocol.getType())) {
				return super.onSessionProtocol(session, protocol);
			} else if (assembleDataManager.isCrossShieldProtocl(protocol.getType())) {
				player.sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD_VALUE, 0);
				
				return true;
			} else {
				return CrossProxy.getInstance().sendNotify(protocol, targetServerId, player.getId());
			}
												
		}							
	}
	
	/**
	 * 是否为系统对象
	 */
	@Override
	public boolean isSystemAppObj(HawkXID xid) {
		if (xid.getType() == GsConst.ObjType.MANAGER) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 排队等待登录检测
	 * @param session
	 * @param protocol
	 * @return true可直接登录，false排队等待
	 */
	private boolean waitLoginCheck(HawkSession session, HawkProtocol protocol) {
		// 中开关
		ServerSettingData serverSetting = GlobalData.getInstance().getServerSettingData();
		if (serverSetting.getMaxOnlineCount() <= 0 ||
			!GsConfig.getInstance().isLoginWaitQueue() || 
			serverSetting.getMaxWaitCount() <= 0) {
			return true;
		}
		
		synchronized (waitSessionQueue) {
			// 对于排过队轮到自己的，可直接登录
			if (session.getUserObject(GsConst.LOGIN_WAIT_KEY) == WaitLoginState.ALLOW_LOGIN) {
				if (HawkApp.getInstance().getCurrentTime() - (long)session.getUserObject("allowLoginTs") <= 10000) {
					session.clearUserObject(GsConst.LOGIN_WAIT_KEY);
					return true;
				}
				
				HPLoginRet.Builder builder = HPLoginRet.newBuilder();
				builder.setErrCode(WaitLoginCode.LOGIN_TIMEOUT_VALUE);
				session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
				GsApp.getInstance().addDelayAction(1000, new HawkDelayAction() {
					@Override
					protected void doAction() {
						session.close();
					}
				});
				return false;
			}
			
			// 在线人数未达到上限, 且没有人在排队，可直接登录
			if(GlobalData.getInstance().getOnlineUserCount() < serverSetting.getMaxOnlineCount() && waitSessionQueue.isEmpty()) {
				return true;
			}
			
			// 排队人数已达上限, 且是LOGIN_C协议，直接关闭session
			if (waitSessionQueue.size() >= serverSetting.getMaxWaitCount() && protocol.checkType(HP.code.LOGIN_C)) {
				HPLoginRet.Builder builder = HPLoginRet.newBuilder();
				builder.setErrCode(WaitLoginCode.WAIT_LOGIN_UPPER_LIMIT_VALUE);
				session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
				
				HawkLog.logPrintln("waitlogin status, openid: {}, wait login upper limit, sessionState: {}, session: {}, onlineCnt: {}", 
						session.getUserObject("openid"), session.getUserObject(GsConst.LOGIN_WAIT_KEY), GlobalData.getInstance().getOnlineUserCount());
			
			} else if (protocol.checkType(HP.code.LOGIN_C)) {
				if (!waitSessionQueue.contains(session)) {
					waitSessionQueue.add(session);
					GlobalData.getInstance().addLoginWait(session.getUserObject("channel"), 1);
				}
				
				int waitCnt = waitSessionQueue.size();
				int waitIndex = getSessionWaitIndex(session);
				HPLoginRet.Builder builder = HPLoginRet.newBuilder();
				builder.setErrCode(WaitLoginCode.WAITING_LOGIN_VALUE);
				builder.setWaitingNum(waitCnt);
				builder.setWaitingIndex(waitIndex);
				session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
				
				HawkLog.logPrintln("waitlogin status, openid: {}, sync protocol: LOGIN_S, wait count: {}, self index: {}, session: {}, onlineCnt: {}", 
						session.getUserObject("openid"), waitCnt, waitIndex, session, GlobalData.getInstance().getOnlineUserCount());
				
			} else {
				syncLoginWaitInfo(session, HP.code.LOGIN_PREPARE_SYNC_S);
			}
		}

		return false;
	}
	
	/**
	 * 同步排队信息
	 * 
	 * @param session
	 * @param type
	 */
	private void syncLoginWaitInfo(HawkSession session, ProtocolMessageEnum type) {
		int count = waitSessionQueue.size();
		int index = getSessionWaitIndex(session);
		session.setUserObject(GsConst.LOGIN_WAIT_KEY, WaitLoginState.WAIT_LOGIN);
		
		HPWaitLogin.Builder builder = HPWaitLogin.newBuilder();
		builder.setWaitLoginCode(WaitLoginCode.WAITING_LOGIN_VALUE);
		builder.setWaitingNum(count);
		builder.setWaitingIndex(index);
		session.sendProtocol(HawkProtocol.valueOf(type, builder));
	}
	
	/**
	 * 获取session在所有排队等待中的session的位次
	 * 
	 * @param session
	 * @return
	 */
	private int getSessionWaitIndex(HawkSession session) {
		Iterator<HawkSession> iterator = waitSessionQueue.iterator();
		int index = 1;
		while (iterator.hasNext()) {
			if (session == iterator.next()) {
				return index;
			}
			
			index++;
		}
		
		return -1;
	}
	
	/**
	 * 在线session关闭，通知排队session登录
	 * 
	 */
	public void notifyLogin(String playerId) {
		synchronized (waitSessionQueue) {
			int maxOnlineCnt = GlobalData.getInstance().getServerSettingData().getMaxOnlineCount();
			if(maxOnlineCnt > 0 && GlobalData.getInstance().getOnlineUserCount() >= maxOnlineCnt) {
				return;
			}
			
			HawkSession waitingSession = waitSessionQueue.poll();
			if (waitingSession != null) {
				// 通知前端可以登录了
				waitingSession.setUserObject(GsConst.LOGIN_WAIT_KEY, WaitLoginState.ALLOW_LOGIN);
				HPWaitLogin.Builder builder = HPWaitLogin.newBuilder();
				builder.setWaitLoginCode(WaitLoginCode.ALLOW_LOGIN_VALUE);
				waitingSession.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_PREPARE_SYNC_S, builder));
				GlobalData.getInstance().decLoginWait(waitingSession.getUserObject("channel"), 1);
				waitingSession.clearUserObject("channel");
				waitingSession.setUserObject("allowLoginTs", HawkApp.getInstance().getCurrentTime());
				HawkLog.logPrintln("waitlogin status, openid: {}, allow login, session: {}, logout player: {}, onlineCnt: {}", 
						waitingSession.getUserObject("openid"), waitingSession, playerId, GlobalData.getInstance().getOnlineUserCount());
			}
		}
	}
	
	/**
	 * 准备puid对应的会话
	 * 
	 * @param puid
	 * @return
	 */
	public boolean doLoginProcess(HawkSession session, HawkProtocol protocol, long timestamp) {
		// 协议解析
		HPLogin cmd = protocol.parseProtocol(HPLogin.getDefaultInstance());
		String serverId = cmd.getServerId();
		String platform = cmd.getPlatform();
		List<String> serverList = GlobalData.getInstance().getMergeServerList(GsConfig.getInstance().getServerId());
		HawkLog.logPrintln("on session protocol doLoginProcess, serverId: {}, serverList: {}", serverId, serverList);
		//没有合服 或者不在合服列表里面. 取本服.
		if (CollectionUtils.isEmpty(serverList) || !serverList.contains(serverId)) {
			serverId = GsConfig.getInstance().getServerId();
			cmd = cmd.toBuilder().setServerId(serverId).build();
		}		
		// 同一个第三方账号在不同平台（android，ios）下是不同账号
		String openid = cmd.getPuid();
		String puid = GameUtil.getPuidByPlatform(openid, platform);

		try {
			String result = RedisProxy.getInstance().getRedisSession().getString(UpdateInfoAuthorizationHandler.PFAUTH_CANCEL_STATUS_KEY + cmd.getPuid());
			if (!HawkOSOperator.isEmptyString(result)) {
				RedisProxy.getInstance().getRedisSession().del(UpdateInfoAuthorizationHandler.PFAUTH_CANCEL_STATUS_KEY + cmd.getPuid());
				if (cmd.getFlag() == LoginFlag.BROKEN_CONNECT_VALUE) {
					HPLoginRet.Builder builder = HPLoginRet.newBuilder();
					builder.setErrCode(Status.IdipMsgCode.IDIP_AUTH_RELEASE_OFFLINE_VALUE);
					session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
					HawkLog.errPrintln("platform auth check failed - pfAuthCancelEvent, platform: {}, channel: {}, puid: {}", cmd.getPlatform(), cmd.getChannel(), puid);
					return false;
				}
			} 
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 登录鉴权
		if (!LoginUtil.platformAuthCheck(cmd, session)) {
			HPLoginRet.Builder builder = HPLoginRet.newBuilder();
			builder.setErrCode(Status.SysError.AUTH_CHECK_FAILED_VALUE);
			session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
			HawkLog.errPrintln("platform auth check failed, platform: {}, channel: {}, puid: {}", cmd.getPlatform(), cmd.getChannel(), puid);
			return false;
		}
		
		// 获取迁服后禁止登录
		try {
			JSONObject immgrationBanLogin = RedisProxy.getInstance().getImmgrationBanLogin(cmd.getPuid());
			if (immgrationBanLogin != null) {
				String currServerId = GsConfig.getInstance().getServerId();
				String banServerId = GlobalData.getInstance().getMainServerId(immgrationBanLogin.getString("serverId"));
				String banTarServerId = GlobalData.getInstance().getMainServerId(immgrationBanLogin.getString("tarServerId"));
				long banBeginTime = immgrationBanLogin.getLong("time");
				// 区服被禁止登录
				if (currServerId.equals(banServerId) || currServerId.equals(banTarServerId)) {
					// 在封禁时间内
					long banEndTime = banBeginTime + (GsConfig.getInstance().getImmgrationBanLogin() * 1000L);
					if (HawkTime.getMillisecond() < banEndTime) {
						HPLoginRet.Builder builder = HPLoginRet.newBuilder();
						builder.setErrCode(Status.SysError.IMMGRATION_BAN_LOGIN_VALUE);
						builder.setZkMsg(String.valueOf(banEndTime));
						session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
						return false;	
					}
				}
			}	
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 区服和puid校验
		if (!LoginUtil.checkLoginServerAndPuid(serverId, puid, session)) {
			HPLoginRet.Builder builder = HPLoginRet.newBuilder();
			builder.setErrCode(Status.SysError.FETCH_PUID_FAILED_VALUE);
			session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, builder));
			return false;
		}

		// 获取账号信息
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(puid, serverId);

		// 机器人模式下的puid生成
		if (accountInfo == null && HawkOSOperator.isEmptyString(puid) && GsConfig.getInstance().isRobotMode()) {
			puid = GlobalData.getInstance().randomPlatformPuid(cmd.getPlatform());
			while (GlobalData.getInstance().getAccountInfo(puid, serverId) != null) {
				puid = GlobalData.getInstance().randomPlatformPuid(cmd.getPlatform());
			}
		}

		// 创建新账号
		int loginType = LogConst.LoginType.NO_FIRST_LOGIN;
		if (accountInfo == null) {
			// 判断账号是否被禁止创建角色
			if (LoginUtil.forbidCreateRole(session, openid)) {
				HawkLog.logPrintln("account forbid create role, openid: {}", openid);
				return false;
			}
			
			// 注册白名单玩家不受注册上限限制
			if (GlobalData.getInstance().isRegisterFull() && !GameUtil.isRegisterPuidCtrlPlayer(openid)) {
				HPLoginRet.Builder response = HPLoginRet.newBuilder();
				response.setErrCode(Status.SysError.SERVER_REGISTER_FULL_VALUE);
				session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, response));
				HawkLog.logPrintln("server register full, cannnot create new role, openid: {}", openid);
				return false;
			}

			String playerId = HawkUUIDGenerator.genUUID();
			// 预设登录名
			String playerName = LoginUtil.getLoginNameByPuid(playerId, puid, cmd.getPfToken(), cmd.getChannel(), session, openid);
			// 创建玩家实体对象
			PlayerEntity playerEntity = LoginUtil.createNewPlayer(playerId, openid, puid, playerName, serverId, cmd);
			if (playerEntity == null) {
				HPLoginRet.Builder response = HPLoginRet.newBuilder();
				response.setErrCode(Status.SysError.PLAYER_CREATE_FAILED_VALUE);
				session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, response));
				return false;
			}
			
			// 特殊的机器人角色
			if (GsConfig.getInstance().isDebug() && cmd.getRobot() > 0) {
				LocalRedis.getInstance().addRobotRole(playerId);
			}
			
			// 缓存玩家实体对象
			GlobalData.getInstance().cachePlayerEntity(playerEntity);
			
			// 登录名
			GameUtil.updateNewPlayerName(playerEntity.getId(), playerName);

			loginType = LogConst.LoginType.FIRST_LOGIN;
			// 记录log信息
			HawkLog.logPrintln("create player entity success, playerId: {}, puid: {}, deviceId: {}, serverId: {}, channel: {}", playerId, puid, cmd.getDeviceId(), GsConfig.getInstance().getServerId(), cmd.getChannel());

			accountInfo = GlobalData.getInstance().updateAccountInfo(playerEntity.getPuid(), playerEntity.getServerId(), playerEntity.getId(), 0, playerEntity.getName());
			accountInfo.setNewly(true);
			accountInfo.setInBorn(true);
			accountInfo.setLoginTime(timestamp);
			
			// 记录玩家的名字到缓存中
			SearchService.getInstance().addPlayerNameLow(playerName, playerId);

			// 新增注册用户
			GlobalData.getInstance().addUserRegister(playerEntity);
			// 玩家注册相关信息存储
			LoginUtil.playerRegisterSuccess(playerEntity, cmd);
			
		} else {
			if (RedisProxy.getInstance().isRemovePlayer(accountInfo.getPlayerId())) {
				HawkLog.errPrintln("player is been removing, cannot login game, playerId: {}, openid: {}", accountInfo.getPlayerId(), accountInfo.getPuid());
				HPLoginRet.Builder response = HPLoginRet.newBuilder();
				response.setErrCode(Status.SysError.PLAYER_INIT_FAILED_VALUE);
				session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, response));
				return false;
			}
			
			//心悦角色交易限制检测
			if (!RoleExchangeService.getInstance().loginCheck(openid, accountInfo.getPlayerId())) {
				HPLoginRet.Builder response = HPLoginRet.newBuilder();
				response.setErrCode(Status.SysError.ROLE_EXCHANGE_FORBIDDEN_VALUE);
				session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, response));
				return false;
			}
			
			//第二次登录需要设置一下
			accountInfo.setNewly(false);
			accountInfo.setLoginTime(timestamp);
		}
			
		// 账号被封号
		if (LoginUtil.forbidAccount(session, accountInfo, openid)) {
			return false;
		}

		// 初始化玩家对象
		if (!LoginUtil.initLoginPlayer(accountInfo, loginType, cmd, session)) {
			HPLoginRet.Builder response = HPLoginRet.newBuilder();
			response.setErrCode(Status.SysError.PLAYER_INIT_FAILED_VALUE);
			session.sendProtocol(HawkProtocol.valueOf(HP.code.LOGIN_S, response));
			HawkLog.errPrintln("init login player failed, puid: {}, playerId: {}, serverId: {}, channel: {}", accountInfo.getPuid(), accountInfo.getPlayerId(), serverId, cmd.getChannel());
			return false;
		}

		// 日志记录
		HawkLog.logPrintln("login prepare success, playerId: {}, puid: {}, serverId: {}, channel: {}", accountInfo.getPlayerId(), accountInfo.getPuid(), serverId, cmd.getChannel());
		Player player = GlobalData.getInstance().queryPlayer(accountInfo.getPlayerId());
		if (player != null && player.getCrossStatus() != PlayerCrossStatus.NOTHING) {
			//走到这里都是在原服登录的玩家.
			//如果状态是20也就是跨服完成的状态,出现这条打印是正常的,不用担心
			HawkLog.errPrintln("fix player crosss status playerId:{}, status:{}", accountInfo.getPlayerId(), player.getCrossStatus());
			player.setCrossStatus(PlayerCrossStatus.NOTHING);
		}
		// 重新投递登录协议
		HawkTaskManager.getInstance().postProtocol(session.getAppObject().getXid(), protocol, 1);
		return true;
	}

	/**
	 * 通知从管理器删除对象
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	private boolean onRemoveObjectMsg(RemoveObjectMsg msg) {
		try {
			HawkXID xid = msg.getRemoveXid();
			if (xid == null || !xid.isValid()) {
				return false;
			}

			HawkAppObj appObj = removeObj(xid);
			if (xid.getType() == GsConst.ObjType.PLAYER) {
				Player player = (Player) appObj;
				if (player != null) {
					player.clearData(true);
					HawkLog.logPrintln("app remove player obj,  xid: {}", xid);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}

	/**
	 * 移除超时的玩家对象
	 */
	public void onRemoveTimeoutObj(HawkAppObj appObj) {
		HawkSession session = appObj.getSession();
		if (session != null && session.isActive()) {
			appObj.getSession().close();
		}
	}

	/**
	 * 获取应用层的状态信息
	 * 
	 * @param justApp
	 *            只获取应用级状态信息，不获取系统级信息
	 * @return
	 */
	public JSONObject getAppStatusInfo(boolean justApp) {
		JSONObject status = null;
		if (!justApp) {
			status = HawkSysProfiler.getInstance().getProfilerStatus();
		}

		if (status == null) {
			status = new JSONObject();
		}

		status.put("registerCount", GlobalData.getInstance().getRegisterCount());
		status.put("onlineCount", GlobalData.getInstance().getOnlineUserCount()); // 在线人数
		status.put("activePlayerCount", RedisProxy.getInstance().getServerDailyInfo(DailyInfoField.DAY_LOGIN)); // 日活跃玩家数
		status.put("registerAddCount", RedisProxy.getInstance().getServerDailyInfo(DailyInfoField.DAY_REGISTER)); // 日新增注册数
		status.put("dataCache", GlobalData.getInstance().getCacheState());
		status.put("playerDataCount", GlobalData.getInstance().getCacheCount());
		status.put("playerCache", GsApp.getInstance().getObjMan(GsConst.ObjType.PLAYER).getObjCount());
		status.put("exceptionCount", HawkException.getExceptionCount());

		JSONObject worldState = new JSONObject();
		worldState.put("areaCount", WorldPointService.getInstance().getAreaSize());
		worldState.put("pointCount", WorldPointService.getInstance().getWorldPointSize());
		worldState.put("marchCount", WorldMarchService.getInstance().getMarchsSize());
		status.put("worldState", worldState);

		status.put("worldThreadState", WorldThreadScheduler.getInstance().getThreadState());
		return status;
	}

	/**
	 * 性能分析器, 注册在ProfilerAnalyzer里面, 调用analyzerWrite进行性能数据格式化存储
	 */
	@Override
	public void profilerAnalyze() {
		if (this.isInitOK()) {
			HawkLog.logMonitor(getAppStatusInfo(true).toJSONString());
		}
		ProfilerAnalyzer.getInstance().doAnalyze();
	}

	public String getProtoName(int protoId) {
		HP.code code = HP.code.valueOf(protoId);
		if (code != null) {
			return code.name().toLowerCase();
		} else if (protoId == HP.sys.HEART_BEAT_VALUE) {
			return HP.sys.HEART_BEAT.name().toLowerCase();
		} else {
			HP.code2 code2 = HP.code2.valueOf(protoId);
			if (code2 != null) {
				return code2.name().toLowerCase();
			}
		}

		return null;
	}

	/**
	 * 分析器写数据
	 * 
	 */
	@Override
	public void analyzerWrite(String key, String value, String type) {
		if (!GsConfig.getInstance().isAnalyzerEnable() || GsConfig.getInstance().isDebug()) {
			return;
		}

		// 记录性能日志
		HawkLog.debugMonitor("monitor: {}, data: {}", key, value);
		
		// 超时统计的数据要做特殊存储
		if (GsConfig.getInstance().getTimeoutStatKeyList().contains(key)) {
			writeTimeoutMonitorInfo(key, value);
			return;
		}

		//此代码不能删除，libra读取的线上监控数据，就是从这里写入的 ！！！
		RedisProxy.getInstance().writeMonitorInfo(4320, type, key, value);
		if (GsConfig.getInstance().isZonineEnable()) {
			ZonineSDK.getInstance().singleMonitorReport(key, value);
		}
	}
	
	/**
	 * 存储超时数据
	 * 
	 * @param key
	 * @param value
	 */
	private void writeTimeoutMonitorInfo(String key, String value) {
		if (HawkOSOperator.isEmptyString(value)) {
			return;
		}
		
		try {
			if (GsConfig.getInstance().isZonineEnable()) {
				ZonineSDK.getInstance().statDataReport(key, value);
			}
			
			String dayHour = HawkTime.formatNowTime("yyyyMMddHH");
			String serverId = GsConfig.getInstance().getServerId();
			// 超时数据按小时存储list
			String monitorKey = String.format("monitor:%s:%s:%s", key, serverId, dayHour);
			//此代码不能删除，libra读取的线上监控数据，就是从这里写入的 ！！！
			RedisProxy.getInstance().getRedisSession().lPush(monitorKey, GsConfig.getInstance().getStatExpireTime(), value);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 读取分析器数据
	 * 
	 * @param analyzerWrite
	 * @param param
	 */
	@Override
	public List<String> analyzerRead(String serverId, String key, int start, int count, String type) {
		if (key.indexOf("script:") >= 0) {
			return readScriptInvokeInfo(serverId, key, start, count);
		}
		
		if (key.indexOf("anchor") >= 0) {
			serverId = "";
		}
		
		return RedisProxy.getInstance().readMonitorInfo(serverId, key, start, count, type);
	}

	/**
	 * 读取脚本调用统计信息
	 * 
	 * @param serverId
	 * @param key
	 * @return
	 */
	private List<String> readScriptInvokeInfo(String serverId, String key, int start, int count) {
		String cKey =  String.format("%s:%s:%s", key, HawkTime.formatNowTime("yyyy-MM-dd"), "count");
		Map<String, String> countData = RedisProxy.getInstance().getRedisSession().hGetAll(cKey);
		if (countData.isEmpty()) {
			return Collections.emptyList();
		}
		
		String tKey =  String.format("%s:%s:%s", key, HawkTime.formatNowTime("yyyy-MM-dd"), "costtime");
		Map<String, String> costtimeData = RedisProxy.getInstance().getRedisSession().hGetAll(tKey);
		if (costtimeData.isEmpty()) {
			return Collections.emptyList();
		}
		
		String mKey =  String.format("script:%s:authMass", HawkTime.formatNowTime("yyyy-MM-dd"));
		Map<String, String> taskMassData = RedisProxy.getInstance().getRedisSession().hGetAll(mKey);
		
		String dKey =  String.format("script:%s:authDiscard", HawkTime.formatNowTime("yyyy-MM-dd"));
		Map<String, String> discardData = RedisProxy.getInstance().getRedisSession().hGetAll(dKey);
		
		List<JSONObject> dataList = new LinkedList<JSONObject>();
		try {
			for (Entry<String, String> entry : countData.entrySet()) {
				JSONObject statisInfo = new JSONObject();
				
				String minTs = entry.getKey();
				int times = Integer.valueOf(entry.getValue());
				int costtime = Integer.valueOf(costtimeData.get(minTs));
				int avg = times == 0 ? 0 : costtime / times;
				
				statisInfo.put("ts", minTs);
				statisInfo.put("访问次数", times);
				statisInfo.put("平均耗时", avg);
				
				if (taskMassData.containsKey(minTs)) {
					int taskMass = Integer.valueOf(taskMassData.get(minTs));
					statisInfo.put("任务堆压", taskMass);
				} else {
					statisInfo.put("任务堆压", 0);
				}
				
				if (discardData.containsKey(minTs)) {
					int discardCount = Integer.valueOf(discardData.get(minTs));
					statisInfo.put("任务丢弃", discardCount);
				} else {
					statisInfo.put("任务丢弃", 0);
				}
				
				dataList.add(statisInfo);
			}
			
			Collections.sort(dataList, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					return o1.getString("ts").compareTo(o2.getString("ts"));
				}
			});
			
			int length = dataList.size();
			int fromIndex = length - start - count - 1;
			int toIndex = length - start;
			fromIndex = Math.max(fromIndex, 0);
			toIndex = Math.max(toIndex, 0);
			if (fromIndex >= toIndex) {
				fromIndex = 0;
				toIndex = length;
			}
			
			dataList = dataList.subList(fromIndex, toIndex);
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		return dataList.stream().map(e -> e.toJSONString()).collect(Collectors.toList());
	}
	
	/**
	 * 判断消息是否已被关闭
	 * 
	 * @return
	 */
	public boolean isMsgClosed(int msgId, String msgName) {
		if (SystemControler.getInstance().isAllSystemClosed() || SystemControler.getInstance().isMsgClosed(msgId)) {
			return true;
		}
		return false;
	}

	/**
	 * 协议是否关闭
	 * 
	 * @param protoType
	 * @return
	 */
	public boolean isProtocolClosed(int protoType) {
		// 判断系统或协议是否已被关闭
		if (SystemControler.getInstance().isAllSystemClosed() || SystemControler.getInstance().isProtocolClosed(protoType)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 初始化服务器标识
	 * @return
	 */
	private boolean initServerIdentify() {
		{

			List<ServerIdentifyEntity> entityList = HawkDBManager.getInstance().query("from ServerIdentifyEntity where invalid = 0");
			if (entityList.size() > 0) {
				serverIdentifyEntity = entityList.get(0);
			} else {
				serverIdentifyEntity = new ServerIdentifyEntity();
				if (!HawkDBManager.getInstance().create(serverIdentifyEntity)) {
					HawkLog.logPrintln("initServerIdentify error.");
					return false;
				}
			}

			String serverIdentify = serverIdentifyEntity.getServerIdentify();
			RedisProxy.getInstance().updateServerIdentify(GsConfig.getInstance().getServerId(), serverIdentify);
		}

		// 合服之后原服不改变的ServerIdentify
		{
			String mark = "mergeNotChangeIdentify:" + GsConfig.getInstance().getServerId();
			List<CustomDataEntity> entityList = HawkDBManager.getInstance().query("from CustomDataEntity where id = ? ", mark);
			if (entityList.size() > 0) {
				mergeNotChangeIdentify = entityList.get(0).getArg();
			} else {
				mergeNotChangeIdentify = HawkUUIDGenerator.genUUID();
				CustomDataEntity entity = new CustomDataEntity();
				entity.setId(mark);
				entity.setArg(mergeNotChangeIdentify);
				if (!HawkDBManager.getInstance().create(entity)) {
					HawkLog.logPrintln("init mergeNotChangeIdentify error.");
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 获取服务器标识 
	 * 合服后, 原服也会丢弃
	 * @return
	 */
	public String getServerIdentify() {
		return serverIdentifyEntity == null ? null : serverIdentifyEntity.getServerIdentify();
	}
	
	/**
	 * 获取服务器标识 
	 * 合服后, 原服不会丢弃
	 * @return
	 */
	public String getMergeNotChangeIdentify() {
		return mergeNotChangeIdentify;
	}

	/**
	 * 配置检测
	 * 
	 * @param cfg
	 * @return
	 */
	public boolean checkConfig() {
		GsConfig appCfg = null;
		try {
			HawkConfigStorage cfgStorage = new HawkConfigStorage(GsConfig.class);
			appCfg = (GsConfig) cfgStorage.getConfigByIndex(0);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}

		// 设置路径
		if (!HawkOSOperator.installLibPath()) {
			return false;
		}
		
		String configPackages = appCfg.getConfigPackages();
		if (!HawkOSOperator.isEmptyString(configPackages)) {
			if (!HawkConfigManager.getInstance().init(configPackages, true)) {
				System.err.println("----------------------------------------------------------------------");
				System.err.println("-------------config crashed, take weapon to fuck designer-------------");
				System.err.println("----------------------------------------------------------------------");
				return false;
			}
			
			// 初始化redis代理
			if (!RedisProxy.getInstance().init()) {
				return false;
			}
			// 重设配置路径
			ActivityConfigLoader.getInstance().init();
			if (!ActivityConfigLoader.getInstance().resetConfigFilePath()) {
				return false;
			}
			
			if (!HawkConfigManager.getInstance().init(configPackages, false)) {
				System.err.println("----------------------------------------------------------------------");
				System.err.println("-------------config crashed, take weapon to fuck designer-------------");
				System.err.println("----------------------------------------------------------------------");
				return false;
			}
		}
		
		HawkLog.logPrintln("config check success ...");
		return true;
	}

	public long getServerOpenAM0Time() {
		return serverOpenAM0Time;
	}
	
	/**
	 * 连接速度限制回调协议
	 * 
	 * @param session
	 * @return
	 */
	public HawkProtocol onRefuseByConnectSpeed() {
		HPErrorCode.Builder builder = HPErrorCode.newBuilder();
		builder.setHpCode(HP.code.LOGIN_C_VALUE);
		builder.setErrCode(SysError.SERVER_BUSY_LIMIT_VALUE);
		builder.setErrFlag(0);
		return HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder);
	}
	
	/**
	 * 达到上限
	 */
	public HawkProtocol onRefuseByOverload(HawkSession session) {
		HPErrorCode.Builder builder = HPErrorCode.newBuilder();
		builder.setHpCode(HP.code.LOGIN_C_VALUE);
		builder.setErrCode(SysError.SERVER_BUSY_LIMIT_VALUE);
		builder.setErrFlag(0);
		return HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder);
	}
	
	/**
	 * 把db信息写到redis
	 */
	public void dbInfoToRedis() {
		try {
			String gameId = GsConfig.getInstance().getGameId();
			String areaId = GsConfig.getInstance().getAreaId();
			String serverId = GsConfig.getInstance().getServerId();
			String mainServerId = GlobalData.getInstance().getMainServerId(serverId);
			
			String dbConnUrl = GsConfig.getInstance().getDbConnUrl();
			String dbUserName = GsConfig.getInstance().getDbUserName();
			String dbPassWord = GsConfig.getInstance().getDbPassWord();
			
			JSONObject json = new JSONObject();
			json.put("dbConnUrl", dbConnUrl);
			json.put("dbUserName", dbUserName);
			json.put("dbPassWord", dbPassWord);
			json.put("mainServerId", mainServerId);
			
			RedisProxy.getInstance().updateDBInfo(gameId, areaId, serverId, json.toJSONString());	
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 把redis信息写到redis
	 */
	public void rsInfoToRedis() {
		try {
			String gameId = GsConfig.getInstance().getGameId();
			String areaId = GsConfig.getInstance().getAreaId();
			String serverId = GsConfig.getInstance().getServerId();
			String mainServerId = GlobalData.getInstance().getMainServerId(serverId);
			
			String url = GsConfig.getInstance().getGlobalRedis();
			String passWord = GsConfig.getInstance().getGlobalRedisAuth();
			
			JSONObject json = new JSONObject();
			json.put("url", url);
			json.put("passWord", passWord);
			json.put("mainServerId", mainServerId);
			
			RedisProxy.getInstance().updateRedisInfo(gameId, areaId, serverId, json.toJSONString());	
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * log信息写到redis
	 */
	public void logInfoToRedis() {
		try {
			String log4jXmlPath  = GsMain.isExistDebugLog4jFile() ? GsMain.DEBUG_LOG4J_XML_PATH : GsMain.LOG4J_XML_PATH;
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dombuilder = domFactory.newDocumentBuilder();
			Document doc = dombuilder.parse(new FileInputStream(log4jXmlPath));
			NodeList property = doc.getElementsByTagName("Property");
			String logFilePath = property.item(0).getTextContent();
			
			Path path = FileSystems.getDefault().getPath(logFilePath);
			if (!path.isAbsolute()) {
				logFilePath = HawkOSOperator.getWorkPath() + logFilePath ;
			}
			
			String gameId = GsConfig.getInstance().getGameId();
			String areaId = GsConfig.getInstance().getAreaId();
			String serverId = GsConfig.getInstance().getServerId();
			String mainServerId = GlobalData.getInstance().getMainServerId(serverId);
			
			JSONObject json = new JSONObject();
			json.put("logPath", logFilePath);
			json.put("mainServerId", mainServerId);
			
			RedisProxy.getInstance().updateLogInfo(gameId, areaId, serverId, json.toJSONString());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
}
