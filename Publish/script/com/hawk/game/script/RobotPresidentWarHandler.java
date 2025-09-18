package com.hawk.game.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.hawk.app.HawkApp;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.ActiveSkinEvent;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ArmourPoolCfg;
import com.hawk.game.config.BuildAreaCfg;
import com.hawk.game.config.BuildLimitCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.DressCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.HeroCfg;
import com.hawk.game.config.StoryMissionCfg;
import com.hawk.game.config.StoryMissionChaptCfg;
import com.hawk.game.config.SuperSoldierBuildTaskCfg;
import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.config.SuperSoldierStarLevelCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.StoryMissionEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.GuildCreateObj;
import com.hawk.game.invoker.GuildCreateRpcInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.MarchSpeedItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.PlayerHeroModule;
import com.hawk.game.msg.AcceptGuildApplyMsg;
import com.hawk.game.msg.SuperSoldierTriggeTaskMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.PlayerSuperSoldierModule;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.supersoldier.SuperSoldierSkillSlot;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Mail.PBGundamStartUp;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierListPush;
import com.hawk.game.protocol.SuperSoldier.SupersoldierTaskType;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.BuildingService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.StoryMissionService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGainItem;
import com.hawk.game.service.mssion.event.EventMechaAdvance;
import com.hawk.game.service.mssion.event.EventMechaPartLvUp;
import com.hawk.game.service.mssion.event.EventMechaPartRepair;
import com.hawk.game.service.mssion.event.EventUnlockGround;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.game.util.GsConst.PresidentTowerPointId;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.Source;
import com.hawk.log.LogConst.ChapterMissionOperType;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.LogConst.TaskType;

/**
 * 机器人王战开启
 *
 * localhost:8080/script/robotPresidentWar?count=100&distance=200&guildCount=16&close=0&towerMarchEnable=false
 *
 * count: 开启机器人的数量，默认200
 * marchLimit: 机器人行军数量上限
 * distance: 距离王座中心点的距离范围，默认200
 * guildCount: 联盟个数，默认10个
 * close: 是否关闭机器人王战：1关闭，0开启 (只是关闭机器人自动发行军，王战周期本身不影响)，默认为0
 * towerMarchEnable: 是否开箭塔行军，true开启，false不开启，默认为false
 * speed: 是否开启加速，1开启加速，0关闭加速
 *
 * @author lating
 *
 */
public class RobotPresidentWarHandler extends HawkScript {
	/**
	 * 自动行军的距离
	 */
	private static int DISTANCE_TO_CENTER = 200;
	/**
	 * 开启行军机器人个数
	 */
	private static int robotCount = 200;
	/**
	 * 联盟个数
	 */
	private static int guildCount = 10;
	private static Set<String> guildLeaders = new ConcurrentHashSet<>();
	/**
	 * 是否开箭塔的行军
	 */
	private static AtomicBoolean towerMarchEnable = new AtomicBoolean(false);
	/**
	 * 是否支持加速
	 */
	private boolean speedEnable = true;
	/**
	 * 机器人行军数量上限
	 */
	private static int marchLimit = 500;
	
	/**
	 * 机器人openid前缀
	 */
	private static String openid_prefix = "robot_puid_";
	
	RobotPresidentWarTickable tickable = null;
	private static AtomicBoolean tickableRunning = new AtomicBoolean(false);
	
	private static Set<String> playerIdSet = new ConcurrentHashSet<>();


	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(SCRIPT_ERROR, "");
		}
		
		int close = 0;
		if (params.containsKey("close")) {
			close = Integer.valueOf(params.get("close"));
		}
		// 关闭机器人王战
		if (close == 1) {
			tickableRunning.set(false);
			return HawkScript.successResponse("机器人国王战已关闭（国王战还处于有效期，只是机器人不再发行军）");
		}
		
		if (params.containsKey("speed")) {
			int speed = Integer.valueOf(params.get("speed"));
			speedEnable = speed != 0;
			// 已经开了，就不要重复开
			if (tickableRunning.get() && !params.containsKey("marchLimit")) {
				return HawkScript.successResponse("speed参数设置成功！");
			}
		}
		
		if (params.containsKey("marchLimit")) {
			marchLimit = Integer.valueOf(params.get("marchLimit"));
			// 已经开了，就不要重复开
			if (tickableRunning.get()) {
				String msg = params.containsKey("speed") ? "speed、marchLimit参数设置成功！" : "marchLimit参数设置成功！";
				return HawkScript.successResponse(msg);
			}
		}
		
		// 不能重复开启，如果已经开启了要先关闭后才能再开启
		if (tickableRunning.get()) {
			return HawkScript.failedResponse(SCRIPT_ERROR, "机器人国王战正在进行中，要关闭后才能重新开启！");
		}
		
		if (params.containsKey("count")) {
			robotCount = Integer.valueOf(params.get("count"));
		}
		if (params.containsKey("guildCount")) {
			guildCount = Integer.valueOf(params.get("guildCount"));
		}
		if (params.containsKey("distance")) {
			DISTANCE_TO_CENTER = Integer.valueOf(params.get("distance"));
		}
		
		if (params.containsKey("towerMarchEnable")) {
			towerMarchEnable.set(params.get("towerMarchEnable").equalsIgnoreCase("true"));
		}
		
		// 防止异步任务执行对结果造成影响，所以一些相关的返回信息参数在异步任务发起前先提前计算
		boolean opened = PresidentFightService.getInstance().isFightPeriod() && tickable != null;
		int count = 0;
		if (!opened) {
			List<AccountInfo> accountList = getExistRobotAccount();
			count = accountList.size();
			count = Math.max(robotCount - count - 10, 0) / 30 + 1;
		}
		
		// 首次初始化机器人时比较耗时，所以这里做成异步任务
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				doAction();
				return null;
			}
		});
		
		if (opened) {
			return HawkScript.successResponse("机器人国王战已开启！");
		}
		
		return HawkScript.successResponse("机器人国王战将于" + count + "分钟后开启！");
	}
	
	/**
	 * 开启机器人王战
	 */
	private void doAction() {
		List<AccountInfo> accountList = getExistRobotAccount();
		Collections.shuffle(accountList);
		int robotTotal = accountList.size();
		// 实际存在可用的机器人数量大于要求数量
		if (robotCount <= accountList.size()) {
			accountList.subList(robotCount, accountList.size()).clear();
		} else {
			// 可用的机器人不足时，生成新的机器人
			int max = robotTotal + robotCount - accountList.size();
			for(int i= robotTotal + 1; i <= max; i++) {
				AccountInfo accountInfo = genRobot(openid_prefix + i);
				if(accountInfo != null) {
					accountList.add(accountInfo);
				}
			}
		}
		
		int count = Math.max(robotCount - robotTotal, 0);
		// 开启国王战
		startPresidentWar(count);
		// 添加定时器
		tickableRunning.set(true);
		if (tickable == null) {
			tickable = new RobotPresidentWarTickable(accountList);
			GsApp.getInstance().addTickable(tickable);
		}
	}
	
	/**
	 * 开启国王战（针对王战周期）
	 */
	private String startPresidentWar(int count) {
		if (PresidentFightService.getInstance().isFightPeriod()) {
			return "already fight yet";
		}
		
		long oldStartTime = PresidentFightService.getInstance().getPresidentCity().getStartTime();
		count = Math.max(count - 10, 0) / 30 + 1;
		long newStartTime = HawkTime.getMillisecond() + count * 60000L;
		PresidentFightService.getInstance().getPresidentCity().setStartTime(Math.min(oldStartTime, newStartTime));
		return "president war is going to start after 1 minute";
	}
	
	/**
	 * 获取已存在的机器人账号
	 * @return
	 */
	private List<AccountInfo> getExistRobotAccount() {
		List<AccountInfo> accountList = new LinkedList<AccountInfo>();
		GlobalData.getInstance().getAccountList(accountList);
		Iterator<AccountInfo> iterator = accountList.iterator();
		while (iterator.hasNext()) {
			AccountInfo accountInfo = iterator.next();
			if (accountInfo.getPuid().indexOf("robot") < 0) {
				iterator.remove();
			}
		}
		
		return accountList;
	}
	
	/**
	 * 自动生成机器人
	 * @param puid
	 */
	public AccountInfo genRobot(String openid) {
		try {
			// 构造登录协议对象
			HPLogin.Builder builder = HPLogin.newBuilder();
			builder.setCountry("cn");
			builder.setChannel("guest");
			builder.setLang("zh-CN");
			builder.setPlatform("android");
			builder.setVersion("1.0.0.0");
			builder.setPfToken("da870ef7cf996eb6");
			builder.setPhoneInfo("{\"deviceMode\":\"win32\",\"mobileNetISP\":\"0\",\"mobileNetType\":\"0\"}\n");
			builder.setPuid(openid);
			builder.setServerId(GsConfig.getInstance().getServerId());
			builder.setDeviceId(openid);
			
			HawkSession session = new HawkSession(null);
			session.setAppObject(new Player(null));
			
			if (GsApp.getInstance().doLoginProcess(session, HawkProtocol.valueOf(HP.code.LOGIN_C_VALUE, builder), HawkTime.getMillisecond())) {
				AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(openid +"#android", GsConfig.getInstance().getServerId());
				if (accountInfo != null) {
					// 加载数据
					playerIdSet.add(accountInfo.getPlayerId());
					accountInfo.setInBorn(false);
					HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, accountInfo.getPlayerId());
					Player player = (Player) GsApp.getInstance().queryObject(xid).getImpl();
					PlayerData playerData = GlobalData.getInstance().getPlayerData(accountInfo.getPlayerId(), true);
					player.updateData(playerData);
					
					// 投递消息
					//HawkApp.getInstance().postMsg(player, PlayerAssembleMsg.valueOf(builder.build(), session));
					
					int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
					HawkTaskManager.getInstance().postTask(new HawkTask() {
						@Override
						public Object run() {
							player.onRobotAssembleMsg();
							player.onRobotLoginMsg();
							extraEnhance(player);
							return null;
						}
					}, threadIdx);
					
					return accountInfo;
				} else {
					return null;
				}
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return null;
	}
	
	/**
	 * 给玩家发兵
	 * @param player
	 */
	private void awardSoldier(Player player) {
		String items = "70000_100205_300000,70000_100106_200000,70000_100206_300000,70000_100107_200000,70000_100207_200000,70000_100405_300000,"
				+ "70000_100306_200000,70000_100406_300000,70000_100407_200000,70000_100605_300000,70000_100506_400000,70000_100606_200000,70000_100507_300000,70000_100607_200000,70000_100805_200000,70000_100706_200000,70000_100806_200000";
		AwardItems awardItems = AwardItems.valueOf(items);
		awardItems.rewardTakeAffectAndPush(player, Action.GM_AWARD);
	}
	
	private void extraEnhance(Player player) {
		// 贵族经验、部队
		String items = "10000_1005_30000,70000_100205_300000,70000_100106_200000,70000_100206_300000,70000_100107_200000,70000_100207_200000,70000_100405_300000,"
				+ "70000_100306_200000,70000_100406_300000,70000_100407_200000,70000_100605_300000,70000_100506_400000,70000_100606_200000,70000_100507_300000,70000_100607_200000,70000_100805_200000,70000_100706_200000,70000_100806_200000";
		AwardItems awardItems = AwardItems.valueOf(items);
		awardItems.rewardTakeAffectAndPush(player, Action.GM_AWARD);
		
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("playerId", player.getId());
			params.put("level", "30");
			BuildingUpGradeToMaxTask.doAction(params); // 建筑满级
			
			FirstSuperSoldierUnlockTask.doAction(params); // 解锁机甲
			
			UpgradeAllPreSuperSoldierTask.doAction(params); // 一代机甲
			
			UnlockAllSuperSoldierTask.doAction(params); // 二代机甲
			
			UnlockArmourTask.doAction(params); // 添加装备
			
			params.put("cityControl", "0");
			for (int i = 1; i <= 13; i++) {
				params.put("chapter", String.valueOf(i));
				StoryMissionTask.doAction(params); // 章节任务
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 解锁英雄
		ConfigIterator<HeroCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(HeroCfg.class);
		PlayerHeroModule heroModule = player.getModule(GsConst.ModuleType.HERO);
		while (iterator.hasNext()) {
			HeroCfg cfg = iterator.next();
			try {
				heroModule.unLockHero(cfg.getHeroId());
			} catch (Exception e) {
			}
		}
		
		// 装扮
		String openid = player.getOpenId();
		String subStr = openid.substring(openid.indexOf(openid_prefix) + openid_prefix.length());
		doDress(player, Integer.parseInt(subStr));
		
		// 加入联盟
		if (!player.hasGuild()) {
			joinGuild(player);
		}
		
		playerIdSet.remove(player.getId());
	}
	
	/**
	 * 装扮
	 * @param player
	 * @param robotId
	 */
	protected void doDress(Player player, int robotId) {
		long timeLong = HawkTime.DAY_MILLI_SECONDS * 360;
		List<DressCfg> cfgList = new ArrayList<>(HawkConfigManager.getInstance().getConfigIterator(DressCfg.class).toList());
		for (int i = 0; i < 6; i++) {
			int dressType = i + 1;
			OptionalInt optionalInt = cfgList.stream().filter(e -> e.getDressType() == dressType).mapToInt(e -> e.getModelType()).max();
			if (!optionalInt.isPresent()) {
				continue;
			}
			
			int max = optionalInt.getAsInt();
			int modleType = robotId % max + 1;
			Optional<DressCfg> optional = cfgList.stream().filter(e -> e.getDressType() == dressType && e.getModelType() == modleType).findAny();
			if (optional.isPresent()) {
				DressCfg dressCfg = optional.get();
				DressEntity dressEntity = player.getData().getDressEntity();
				dressEntity.addOrUpdateDressInfo(dressCfg.getDressType(), dressCfg.getModelType(), timeLong);
				WorldPointService.getInstance().updateShowDress(player.getId(), dressCfg.getDressType(), dressEntity.getDressInfo(dressCfg.getDressType(), dressCfg.getModelType()));
				ActivityManager.getInstance().postEvent(new ActiveSkinEvent(player.getId(), dressCfg.getDressType(), dressCfg.getModelType(), (int) (timeLong / 1000)));
				player.getPush().syncDressInfo();
				player.getEffect().resetEffectDress(player);
				
				WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
				if (worldPoint == null) {
					return;
				}
				WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
			}
		}
	}
	
	private synchronized void joinGuild(Player player) {
		int guildCnt = guildLeaders.size();
		if (guildCnt < guildCount) {
			guildLeaders.add(player.getId());
			// 先迁城再创建联盟
			guildLeaderMoveCity(player);
			String guildName = GlobalData.getInstance().randomPlayerName().replaceFirst("指挥官", "");
			String tag = guildName.substring(0, 3);
			GuildCreateObj obj = new GuildCreateObj(guildName, tag, HP.code.GUILDMANAGER_CREATE_C_VALUE, ConsumeItems.valueOf());
			obj.randomTag();
			player.rpcCall(MsgId.GUILD_CREATE, GuildService.getInstance(), new GuildCreateRpcInvoker(player, obj));
		} else {
			// 此处不能加入联盟，因为上面创建联盟是异步的，可能还没创建完
		}
	}
	
	/**
	 * 个人迁城（先判断范围，后随机点）
	 * @param player
	 * @param targetPos
	 * @param distance
	 */
	private boolean randomMoveCity(Player player, int[] targetPos, int distance) {
		try {
			int[] selfPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
			if (Math.abs(selfPos[0] - targetPos[0]) > distance || Math.abs(selfPos[1] - targetPos[1]) > distance) {
				// 在给定范围内随机一个迁城坐标
				int[] pos = randomPoint(player, targetPos, distance);
				if(pos != null) {
					 WorldPlayerService.getInstance().mantualSettleCity(player, pos[0], pos[1], 0);
					 return true;
				}
			} else {
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
	}
	
	/**
	 * 个人迁城（不管范围直接随机点）
	 * @param player
	 * @param targetPos
	 * @param distance
	 * @return
	 */
	private boolean randomMoveCityDirect(Player player, int[] targetPos, int distance) {
		try {
			int[] pos = randomPoint(player, targetPos, distance);
			if(pos != null) {
				WorldPlayerService.getInstance().mantualSettleCity(player, pos[0], pos[1], 0);
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
	}
	
	/**
	 * 随机一个城点
	 * @param player
	 * @param targetPos
	 * @return
	 */
	private int[] randomPoint(Player player, int[] targetPos, int distance) {
		int aroundDistance = 0;
		do {
			aroundDistance++;
			Map<Integer, Point> aroundPoints = WorldPointService.getInstance().getAroundPoints(targetPos[0], targetPos[1], aroundDistance, aroundDistance);
			List<Point> points = new ArrayList<>(aroundPoints.values());
			Collections.shuffle(points);
			for (Point validPoint : points) {
				if(((validPoint.getX() + validPoint.getY()) % 2 == 1) && WorldPlayerService.getInstance().checkPlayerCanOccupy(player, validPoint.getX(), validPoint.getY())) {
					return new int[]{validPoint.getX(), validPoint.getY()};
				}
			}
		} while (aroundDistance <= distance);
		
		return null;
	}
	
	/**
	 * 盟主迁城
	 * @param player
	 */
	private void guildLeaderMoveCity(Player player) {
		int pointId = WorldMapConstProperty.getInstance().getCenterPointId();  // 380_760
		int[] kingPos = GameUtil.splitXAndY(pointId);
		boolean success = false;
		int ratio = 5;
		while (!success && ratio > 0) {
			success = randomMoveCityDirect(player, kingPos, DISTANCE_TO_CENTER);
			ratio--;
		}
		
		int[] pos = player.getPosXY();
		HawkLog.logPrintln("robot president war, guild leader random moveCity {}, openid: {}, pos: {},{}", success ? "success" : "failed", player.getOpenId(), pos[0], pos[1]);
	}
	
	/**
	 * 联盟成员迁城
	 * @param player
	 */
	private void guildMemberMoveCity(Player player) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		
		String leaderId = GuildService.getInstance().getGuildLeaderId(guildId);
		int[] leaderPos = WorldPlayerService.getInstance().getPlayerPosXY(leaderId);
		boolean success = false;
		int ratio = 4;
		while (!success && ratio > 0) {
			success = randomMoveCityDirect(player, leaderPos, DISTANCE_TO_CENTER / ratio);
			ratio--;
		}
		
		int pointId = WorldMapConstProperty.getInstance().getCenterPointId();
		int[] kingPos = GameUtil.splitXAndY(pointId);
		if (!success) {
			success = randomMoveCity(player, kingPos, DISTANCE_TO_CENTER);
			HawkLog.logPrintln("robot president war, guild member random moveCity second time {}, openid: {}", success ? "success" : "failed", player.getOpenId());
			return;
		}
		
		int[] pos = player.getPosXY();
		HawkLog.logPrintln("robot president war, guild member random moveCity success, openid: {}, pos: {},{}", player.getOpenId(), pos[0], pos[1]);
		if (Math.abs(pos[0] - kingPos[0]) > DISTANCE_TO_CENTER || Math.abs(pos[1] - kingPos[1]) > DISTANCE_TO_CENTER) {
			success = randomMoveCityDirect(player, kingPos, DISTANCE_TO_CENTER);
			int[] newPos = player.getPosXY();
			HawkLog.logPrintln("robot president war, guild member fix random moveCity {}, openid: {}, new pos: {},{}", success ? "success" : "failed", player.getOpenId(), newPos[0], newPos[1]);
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////   以下是机器人检测行军的Tickable     //////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * 内部类 ：机器人定时检测发行军的定时器
	 */
	class RobotPresidentWarTickable extends HawkPeriodTickable {
		private List<AccountInfo> accountList;

		public RobotPresidentWarTickable(List<AccountInfo> accountList) {
			super(10000, 10000);
			this.accountList = accountList;
		}

		@Override
		public void onPeriodTick() {
			// 机器人检测行军的总开关
			if (!tickableRunning.get()) {
				return;
			}
			// 王战战争期还没开启，不发行军了
			if (!PresidentFightService.getInstance().isFightPeriod()) {
				return;
			}
			// 联盟还没有创建完，不开启。这里为了防止出错造成卡死，设置数值为1的容错范围
			if (GuildService.getInstance().getGuildCount() < guildCount - 1) {
				return;
			}
			
			for (int i = 0; accountList.size() > 1 && i < accountList.size(); i++) {
				startMarch(accountList.get(i));
			}
		}
		
		/**
		 * 机器人发行军总入口
		 * 
		 * @param accountInfo
		 */
		private void startMarch(AccountInfo accountInfo) {
			String playerId = accountInfo.getPlayerId();
			// 个人还没有初始化完
			if (playerIdSet.contains(playerId)) {
				return;
			}
			
			Player player = GlobalData.getInstance().scriptMakesurePlayer(playerId);
			if (player == null) {
				return;
			}
			
			// 没有联盟的，先考虑加入联盟
			if (!player.hasGuild()) {
				// 盟主正在创建联盟的过程中，就不需要加入了
				if (!guildLeaders.contains(playerId)) {
					List<String> guildIds = GuildService.getInstance().getGuildIds();
					String guildId = guildIds.get(HawkRand.randInt(0, guildIds.size() - 1));
					GuildInfoObject entity = GuildService.getInstance().getGuildInfoObject(guildId);
					int result = GuildService.getInstance().onRobotJoinGuild(player.getId(), guildId, player.getName(), player.getPower(), Const.GuildAuthority.L1_VALUE);
					if (result == Status.SysError.SUCCESS_OK_VALUE) {
						HawkApp.getInstance().postMsg(player.getXid(), AcceptGuildApplyMsg.valueOf(entity.getId()));
						// 迁城
						guildMemberMoveCity(player);
					}
				}
				return;
			}
			
			startMarch(player);
		}
		
		/**
		 * 前置条件校验通过，可以准备发行军了
		 * @param player
		 */
		private void startMarch(Player player) {
			String playerId = player.getId();
			int marchCount = WorldMarchService.getInstance().getPlayerMarchCount(playerId);
			if (marchCount > 0) {
				marchSpeed(playerId);
				if (marchCount >= 4) {
					return;
				}
			}

			String guildId = player.getGuildId();
			if (HawkOSOperator.isEmptyString(guildId)) {
				return;
			}
			
			List<IWorldMarch> robotMarchs = WorldMarchService.getInstance().getMarchsValue().stream().filter(e -> e.getPlayer() != null && e.getPlayer().getOpenId().startsWith(openid_prefix)).collect(Collectors.toList());
			if (robotMarchs.size() >= marchLimit) {
				return;
			}
			
			int marchType = WorldMarchType.PRESIDENT_SINGLE_VALUE;
			String targetId = "";
			
			IWorldMarch massMarch = null;
			List<IWorldMarch> guildMassMarchList = WorldMarchService.getInstance().getGuildMarchs(guildId).stream().filter(e -> e.getMarchType() == WorldMarchType.PRESIDENT_MASS).collect(Collectors.toList());
			List<IWorldMarch> selfMassMarchList = WorldMarchService.getInstance().getPlayerMarch(playerId, WorldMarchType.PRESIDENT_MASS_VALUE);
			if (guildMassMarchList.size() < 4 && selfMassMarchList.isEmpty()) {
				marchType = WorldMarchType.PRESIDENT_MASS_VALUE;
				if (!HawkOSOperator.isEmptyString(PresidentFightService.getInstance().getCurrentGuildId())) {
					targetId = PresidentFightService.getInstance().getCurrentGuildId();
				}
			} else {
				List<IWorldMarch> list = WorldMarchService.getInstance().getPlayerMarch(playerId, WorldMarchType.PRESIDENT_SINGLE_VALUE);
				if (!list.isEmpty()) {
					massMarch = findMassMarch(playerId, guildMassMarchList);
					if (massMarch != null) {
						marchType = WorldMarchType.PRESIDENT_MASS_JOIN_VALUE;
					}
				}
			}
			
			if (marchType == WorldMarchType.PRESIDENT_SINGLE_VALUE && towerMarchEnable.get()) {
				List<IWorldMarch> list = WorldMarchService.getInstance().getPlayerMarch(playerId, WorldMarchType.PRESIDENT_TOWER_SINGLE_VALUE);
				if (list.isEmpty()) {
					marchType = WorldMarchType.PRESIDENT_TOWER_SINGLE_VALUE;
				}
			}

			startMarch(player, marchType, targetId, massMarch);
		}
		
		/**
		 * 准备好相关参数了，直接发行军
		 * 
		 * @param player
		 * @param marchType
		 * @param targetId
		 * @param massMarch
		 */
		private void startMarch(Player player, int marchType, String targetId, IWorldMarch massMarch) {
			// 检测机器人城点是否被清理了
			int[] posInfo = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
			if (posInfo[0] <= 0 || posInfo[1] <= 0) {
				WorldPlayerService.getInstance().randomSettlePoint(player, false);
			}

			int ratio = marchType == WorldMarchType.PRESIDENT_SINGLE_VALUE ? HawkRand.randInt(4, 10) : HawkRand.randInt(1, 4);
			int amount = 101600 * ratio;
			// 出征部队
			List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
			List<ArmyEntity> entityList = player.getData().getArmyEntities();
			Collections.shuffle(entityList);
			int total = 0;
			for (ArmyEntity entity : entityList) {
				if (entity.getFree() <= 0) {
					continue;
				}
				
				int min = Math.min(amount, entity.getFree());
				total += min;
				amount -= min;
				armyList.add(new ArmyInfo(entity.getArmyId(), min));
				if (amount <= 0) {
					break;
				}
			}
			
			if (total <= 0) {
				awardSoldier(player);
				HawkLog.logPrintln("robot president war start march failed, playerId: {}, marchType: {}, totalAmry: {}", player.getId(), marchType, total);
				return;
			}
			
			WorldMarchReq.Builder req = WorldMarchReq.newBuilder();
			// 出征英雄
			List<PlayerHero> heroList = player.getAllHero();
			if (!heroList.isEmpty()) {
				Collections.shuffle(heroList);
				req.addHeroId(heroList.get(0).getHeroEntity().getHeroId());
				if (heroList.size() > 1) {
					req.addHeroId(heroList.get(1).getHeroEntity().getHeroId());
				}
			}
			// 出征机甲
			List<SuperSoldier> superSoldierList = player.getAllSuperSoldier();
			if (!superSoldierList.isEmpty()) {
				Collections.shuffle(superSoldierList);
				req.setSuperSoldierId(superSoldierList.get(0).getCfgId());
			}
			
			// 扣兵
			if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			    HawkLog.errPrintln("robot president war check army failed, playerId: {}", player.getId());
				return;
			}

			// 开启行军
			int pointId = WorldMapConstProperty.getInstance().getCenterPointId();
			int[] kingPos = GameUtil.splitXAndY(pointId);
			req.setPosX(kingPos[0]);
			req.setPosY(kingPos[1]);
			
			if (marchType == WorldMarchType.PRESIDENT_SINGLE_VALUE) {
				startSingleMarch(player, pointId, req, armyList);
			} else if (marchType == WorldMarchType.PRESIDENT_MASS_VALUE) {
				startMassMarch(player, targetId, pointId, req, armyList);
			} else if (marchType == WorldMarchType.PRESIDENT_MASS_JOIN_VALUE) {
				startMassJoinMarch(player, massMarch, req, armyList);
			} else if (marchType == WorldMarchType.PRESIDENT_TOWER_SINGLE_VALUE) {
				starTowertSingleMarch(player, req, armyList);
			}
		}
		
		/**
		 * 查找集结行军
		 * 
		 * @param playerId
		 * @param guildMassMarchList
		 * @return
		 */
		private IWorldMarch findMassMarch(String playerId, List<IWorldMarch> guildMassMarchList) {
			Optional<IWorldMarch> optional = guildMassMarchList.stream().filter(e -> !e.getPlayerId().equals(playerId) && e.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE).findAny();
			if (!optional.isPresent()) {
				return null;
			}
			
			IWorldMarch massMarch = optional.get();
			List<IWorldMarch> joinMarchList = WorldMarchService.getInstance().getPlayerMarch(playerId, WorldMarchType.PRESIDENT_MASS_JOIN_VALUE);
			if (joinMarchList.isEmpty()) {
				return massMarch;
			}
			
			if (joinMarchList.size() > 1) { // 参与集结行军最多只能发两条
				return null;
			}
			
			String mPlayerId = massMarch.getPlayerId();
			String leaderPlayerId = joinMarchList.get(0).getMarchEntity().getLeaderPlayerId();
			if (!mPlayerId.equals(leaderPlayerId) && leaderPlayerId != null) {
				return massMarch;
			}
			
			Optional<IWorldMarch> op = guildMassMarchList.stream().filter(e -> !e.getPlayerId().equals(playerId) && 
					!e.getPlayerId().equals(mPlayerId) &&
					e.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE).findAny();
			return op.isPresent() ? op.get() : null;
		}
		
		/**
		 * 王城单人行军
		 */
		private void startSingleMarch(Player player, int pointId, WorldMarchReq.Builder req, List<ArmyInfo> armyList) {
			IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.PRESIDENT_SINGLE_VALUE, pointId, "", 
					null, 0, new EffectParams(req.build(), armyList));
			if (march == null) {
				HawkLog.logPrintln("robot president war start single march failed, playerId: {}, openid: {}", player.getId(), player.getOpenId());
				return;
			}
			
			HawkLog.logPrintln("robot president war start single march, playerId: {}, openid: {}", player.getId(), player.getOpenId());
			BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_PRESIDENT_SINGLE_MARCH, Params.valueOf("marchData", march));
			int count = HawkRand.randInt(1, 4);
			marchSpeedRandom(player.getId(), march, count);
		}
		
		/**
		 * 王城集结行军
		 */
		@SuppressWarnings("deprecation")
		private void startMassMarch(Player player, String targetId, int pointId, WorldMarchReq.Builder req, List<ArmyInfo> armyList) {
			// 开始行军
			int waitTime = 60; //HawkRand.randInt(60, 180);
			IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.PRESIDENT_MASS_VALUE, pointId, 
					targetId, null, waitTime, new EffectParams(req.build(), armyList));
			if (march == null) {
				HawkLog.logPrintln("robot president war start mass march failed, playerId: {}, openid: {}", player.getId(), player.getOpenId());
				return;
			}
			
			HawkLog.logPrintln("robot president war start mass march, playerId: {}, openid: {}", player.getId(), player.getOpenId());
			BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_MASS, Params.valueOf("marchData", march));
			
			int[] leaderPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
			String leaderPosX = String.valueOf(leaderPos[0]);
			String leaderPosY = String.valueOf(leaderPos[1]);
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.PRESIDENT_MASS_ATTACK, player, 
					WorldPointType.KING_PALACE_VALUE, march.getMarchId(), leaderPosX, leaderPosY);
			
			List<String> members = new ArrayList<String>(GuildService.getInstance().getGuildMembers(player.getGuildId()));
			Collections.shuffle(members);
			int count = HawkRand.randInt(2, 5);
			for (String memberId : members) {
				if (memberId.equals(player.getId())) {
					continue;
				}
				
				count--;
				if (count < 0) {
					break;
				}
				
				Player member = GlobalData.getInstance().scriptMakesurePlayer(memberId);
				if (member != null && member.getOpenId().startsWith(openid_prefix)) {
					startMarch(member, WorldMarchType.PRESIDENT_MASS_JOIN_VALUE, "", march);
				} else {
					count++;
				}
			}
		}
		
		/**
		 * 王城参与集结行军
		 */
		private void startMassJoinMarch(Player player, IWorldMarch massMarch, WorldMarchReq.Builder req, List<ArmyInfo> armyList) {
			if (massMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
				return;
			}
			long now = HawkTime.getMillisecond();
			// 时间太短了，不发行军
			if (massMarch.getStartTime() - now <= 5000) {
				return;
			}
			
			int[] leaderPos = WorldPlayerService.getInstance().getPlayerPosXY(massMarch.getPlayerId());
			WorldPoint point = WorldPointService.getInstance().getWorldPoint(leaderPos[0], leaderPos[1]);
			IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.PRESIDENT_MASS_JOIN_VALUE, point.getId(), massMarch.getMarchId(), 
					null, 0, new EffectParams(req.build(), armyList));
			if (march == null) {
				HawkLog.logPrintln("robot president war start mass join march failed, playerId: {}, openid: {}", player.getId(), player.getOpenId());
				return;
			}
			
			HawkLog.logPrintln("robot president war start mass join march, playerId: {}, openid: {}", player.getId(), player.getOpenId());
			if (march.getEndTime() <= 0) {
				march.getMarchEntity().setEndTime(now + 1800000L);
			}
			
			long waitRemainTime = massMarch.getStartTime() - now;
			long joinRemainTime = march.getEndTime() - now;
			HawkLog.logPrintln("robot president war before mass join march speed, openId: {}, waitRemainTime: {}, joinRemainTime: {}, mass startTime: {}, join endTime: {}", 
					player.getOpenId(), waitRemainTime, joinRemainTime, massMarch.getStartTime(), march.getEndTime());
			int count = 1;
			while (waitRemainTime > 0 && joinRemainTime > 0 && joinRemainTime < 3600000L && joinRemainTime > waitRemainTime) {
				count++;
				joinRemainTime = joinRemainTime / 2;
			}
			count++;
			count = Math.min(count, 5);
			String playerId = player.getId();
			while (count > 0) {
				count--;
				marchSpeed(playerId, march);
			}
		}
		
		/**
		 * 箭塔单人行军
		 */
		private void starTowertSingleMarch(Player player, WorldMarchReq.Builder req, List<ArmyInfo> armyList) {
			PresidentTowerPointId[] pointIds = { PresidentTowerPointId.ONE, PresidentTowerPointId.TWO, PresidentTowerPointId.THREE, PresidentTowerPointId.FOUR };
			int index = HawkRand.randInt(0, pointIds.length - 1);
			PresidentTowerPointId pointId = pointIds[index];
			IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.PRESIDENT_TOWER_SINGLE_VALUE, pointId.getPointId(), "", 
					null, 0, new EffectParams(req.build(), armyList));
			if (march == null) {
				HawkLog.logPrintln("robot president war start single march failed, playerId: {}, openid: {}", player.getId(), player.getOpenId());
				return;
			}
			
			HawkLog.logPrintln("robot president war start tower single march, playerId: {}, openid: {}", player.getId(), player.getOpenId());
			BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_PRESIDENT_TOWER_SINGLE_MARCH, Params.valueOf("marchData", march));
			int count = HawkRand.randInt(1, 4);
			marchSpeedRandom(player.getId(), march, count);
		}
		
		/**
		 * 行军加速
		 */
		private void marchSpeed(String playerId) {
			List<IWorldMarch> list = WorldMarchService.getInstance().getPlayerMarch(playerId, WorldMarchType.PRESIDENT_SINGLE_VALUE);
			for (IWorldMarch march : list) {
				if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || 
						march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
					int count = HawkRand.randInt(1, 4);
					marchSpeedRandom(playerId, march, count);
				}
			}
			
			List<IWorldMarch> towerMarchList = WorldMarchService.getInstance().getPlayerMarch(playerId, WorldMarchType.PRESIDENT_TOWER_SINGLE_VALUE);
			for (IWorldMarch march : towerMarchList) {
				if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || 
						march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
					int count = HawkRand.randInt(1, 4);
					marchSpeedRandom(playerId, march, count);
				}
			}
			
			List<IWorldMarch> massMarchlist = WorldMarchService.getInstance().getPlayerMarch(playerId, WorldMarchType.PRESIDENT_MASS_VALUE);
			for (IWorldMarch march : massMarchlist) {
				if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || 
						march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
					int count = HawkRand.randInt(2, 5);
					marchSpeedRandom(playerId, march, count);
				}
			}
			
			List<IWorldMarch> massJoinList = WorldMarchService.getInstance().getPlayerMarch(playerId, WorldMarchType.PRESIDENT_MASS_JOIN_VALUE);
			for (IWorldMarch march : massJoinList) {
				if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
					marchSpeedRandom(playerId, march, 10);
				}
			}
		}
		
		/**
		 * 行军加速
		 * @param playerId
		 * @param march
		 * @param max
		 */
		private void marchSpeedRandom(String playerId, IWorldMarch march, int max) {
			long now = HawkTime.getMillisecond();
			int count = 1;
			long remainTime = march.getEndTime() - now;
			long fixTime = HawkRand.randInt(3, 10) * 1000L;
			while (remainTime > fixTime && count < max) {
				remainTime = remainTime / 2;
				count ++;
			}
			
			while (count > 0) {
				count--;
				marchSpeed(playerId, march);
			}
		}
		
		/**
		 * 行军加速
		 * @param playerId
		 * @param march
		 */
		private void marchSpeed(String playerId, IWorldMarch march) {
			if (!speedEnable) {
				return;
			}
			WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.WORLD_MARCH_SPEED) {
				@Override
				public boolean onInvoke() {
					List<MarchSpeedItem> speedConsume = new ArrayList<MarchSpeedItem>();
					speedConsume.add(new MarchSpeedItem(ItemType.TOOL_VALUE, 820002, 1, playerId));
					WorldMarchService.getInstance().marchSpeedUp(march.getMarchId(), 50, 2000, speedConsume, playerId);
					HawkLog.logPrintln("robot president war march sppeed, marchType: {}, marchStatus: {}, playerId: {}", march.getMarchType(), WorldMarchStatus.valueOf(march.getMarchStatus()), playerId);
					return false;
				}
			});
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////   以下是模拟的脚本功能      //////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * 章节任务完成脚本 
	 */
	static class StoryMissionTask {
		public static String doAction(Map<String, String> params) {
			if (!GsConfig.getInstance().isDebug()) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
			}
			
			if (!params.containsKey("chapter")) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "chapter param need");
			}
			
			try {
				Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
				if (player == null) {
					return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
				}
				
				int chapter = Integer.parseInt(params.get("chapter"));
				if (chapter <= 0) {
					return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "chapter param error");
				}
				
				if (player.isActiveOnline()) {
					int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
					HawkTaskManager.getInstance().postTask(new HawkTask() {
						@Override
						public Object run() {
							finishChapterTask(player, chapter, Integer.parseInt(params.getOrDefault("cityControl", "1")));
							return null;
						}
					}, threadIdx);
				} else {
					finishChapterTask(player, chapter, Integer.parseInt(params.getOrDefault("cityControl", "1")));
				}
				
				return HawkScript.successResponse("ok");
			} catch (Exception e) {
				HawkException.catchException(e);
				return HawkException.formatStackMsg(e);
			}
		}
		
		private static void finishChapterTask(Player player, int chapter, int cityControl) {
			StoryMissionEntity entity = player.getData().getStoryMissionEntity();
			if (entity.getChapterId() > chapter) {
				HawkLog.logPrintln("storyMissionFinish handler, playerId: {}, chapter {} already finished, latest chapter: {}", player.getId(), chapter, entity.getChapterId());
				return;
			}
			
			int maxChapterId = HawkConfigManager.getInstance().getConfigSize(StoryMissionChaptCfg.class);
			int count = 0;
			while (entity.getChapterId() <= chapter && count <= maxChapterId) {
				try {
					List<MissionEntityItem> itemList = new ArrayList<MissionEntityItem>();
					itemList.addAll(entity.getMissionItems());
					for (MissionEntityItem item : itemList) {
						if (item.getState() == GsConst.MissionState.STATE_BONUS) {
							continue;
						}
						
						StoryMissionService.getInstance().pushMissionReward(player, entity.getChapterId(), item.getCfgId());
						entity.changeMissionState(item.getCfgId(), GsConst.MissionState.STATE_BONUS);
						StoryMissionCfg cfg = AssembleDataManager.getInstance().getStoryMissionCfg(entity.getChapterId(), item.getCfgId());
						StoryMissionService.getInstance().logTaskFlow(player, cfg, MissionState.STATE_BONUS);
					}
					
					if (maxChapterId == entity.getChapterId() && entity.getChapterState() == GsConst.MissionState.STORY_MISSION_COMPLETE) {
						player.getPush().syncStoryMissionInfo();
						return;
					}
					
					StoryMissionService.getInstance().pushChapterReward(player, entity.getChapterId());
					// 领取章节任务奖励打点记录
					LogUtil.logChapterMissionFlow(player, TaskType.STORY_MISSION, entity.getChapterId(), ChapterMissionOperType.COMPLETE_AWARD_TAKEN);
					if (cityControl == 0) {
						refreshChapterMission(player);
					} else {
						StoryMissionService.getInstance().refreshChapterMission(player);
					}
				} catch (Exception e) {
					HawkException.catchException(e);
					player.getPush().syncStoryMissionInfo();
					return;
				}
				
				count++;
			}
			
			player.getPush().syncStoryMissionInfo();
		}
		
		private static void refreshChapterMission(Player player) {
			StoryMissionEntity entity = player.getData().getStoryMissionEntity();

			int chapterId = entity.getChapterId();
			int maxChapterId = HawkConfigManager.getInstance().getConfigSize(StoryMissionChaptCfg.class);
			if (chapterId >= maxChapterId) {
				entity.setChapterState(GsConst.MissionState.STORY_MISSION_COMPLETE);
				return;
			}

			int nextChapterId = chapterId + 1;
			StoryMissionChaptCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(StoryMissionChaptCfg.class, nextChapterId);
			
			// 等级是否可开启下一章节
			if (nextCfg != null && nextCfg.isOpen()) {
				entity.setChapterId(nextChapterId);
				entity.setMissionItems(StoryMissionService.getInstance().generateChapterMission(player, nextChapterId));

				boolean chapterComp = true;
				for (MissionEntityItem mission : entity.getMissionItems()) {
					if (mission.getState() == GsConst.MissionState.STATE_NOT_FINISH) {
						chapterComp = false;
					}
				}

				int chapterState = chapterComp ? GsConst.MissionState.STATE_FINISH : GsConst.MissionState.STATE_NOT_FINISH;
				entity.setChapterState(chapterState);
				LogUtil.logChapterMissionFlow(player, TaskType.STORY_MISSION, nextChapterId, ChapterMissionOperType.MISSION_REFRESH);
			} else {
				entity.setChapterState(GsConst.MissionState.STATE_BONUS);
				List<MissionEntityItem> missionItems = entity.getMissionItems();
				for (MissionEntityItem missionItem : missionItems) {
					if (missionItem.getState() != GsConst.MissionState.STATE_BONUS) {
						missionItem.setState(GsConst.MissionState.STATE_BONUS);
						StoryMissionCfg cfg = AssembleDataManager.getInstance().getStoryMissionCfg(chapterId, missionItem.getCfgId());
						StoryMissionService.getInstance().logTaskFlow(player, cfg, missionItem.getState());
					}
				}
				entity.setMissionItems(missionItems);
			}
		}
	}
	
	/**
	 * 添加装备脚本
	 */
	static class UnlockArmourTask {
		public static String doAction(Map<String, String> params) {
			try {
				if (!GsConfig.getInstance().isDebug()) {
					return null;
				}
				Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
				if (player == null) {
					return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
				}
				
				ConfigIterator<ArmourPoolCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ArmourPoolCfg.class);
				while(iterator.hasNext()) {
					ArmourPoolCfg cfg = iterator.next();
					player.addArmour(cfg.getId());
				}
				
				return HawkScript.successResponse("SUCC");
				
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
			
		}
	}
	
	/**
	 * 二代机甲解锁脚本
	 */
	static class UnlockAllSuperSoldierTask {
		public static String doAction(Map<String, String> params) {
			if (!GsConfig.getInstance().isDebug()) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
			}
			
			try {
				Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
				if (player == null) {
					return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
				}
				
				if (player.isActiveOnline()) {
					int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
					HawkTaskManager.getInstance().postTask(new HawkTask() {
						@Override
						public Object run() {
							if (player.getAllSuperSoldier().isEmpty()) {
								unlockFirstSuperSoldier(player);
							}
							upgradeAllPreSuperSoldier(player);
							return null;
						}
					}, threadIdx);
				} else {
					if (player.getAllSuperSoldier().isEmpty()) {
						unlockFirstSuperSoldier(player);
					}
					upgradeAllPreSuperSoldier(player);
				}
				
				
				return HawkScript.successResponse("ok");
			} catch (Exception e) {
				HawkException.catchException(e);
				return HawkException.formatStackMsg(e);
			}
		}
		
		/**
		 * 解锁所有的一代机甲
		 * 
		 * @param player
		 */
		private static void upgradeAllPreSuperSoldier(Player player) {
			PlayerSuperSoldierModule module = player.getModule(GsConst.ModuleType.SUPER_SOLDIER);
			ConfigIterator<SuperSoldierCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierCfg.class);
			while (iterator.hasNext()) {
				SuperSoldierCfg cfg = iterator.next();
				if (cfg.getPreSupersoldierId() > 0) {
					continue;
				}
				
				int count = 0;
				while (true) {
					if (!superSoldierUnlockAndStarUp(player, cfg.getSupersoldierId(), module)) {
						break;
					}
					
					count++;
					if (count > 10) {
						break;
					}
				}
			}
			
			ConfigIterator<SuperSoldierCfg> iterator2 = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierCfg.class);
			while (iterator2.hasNext()) {
				SuperSoldierCfg cfg = iterator2.next();
				if (cfg.getPreSupersoldierId() <= 0) {
					continue;
				}
				
				int superSoldierId = cfg.getSupersoldierId();
				Optional<SuperSoldier> ssoldier = player.getSuperSoldierByCfgId(superSoldierId);
				if (!ssoldier.isPresent()) {
					SuperSoldier superSoldier = module.unLockSuperSoldier(superSoldierId);
					superSoldier.notifyChange();
					MissionManager.getInstance().postMsg(player, new EventMechaPartLvUp(superSoldierId));
				}
			}
		}
		
		/**
		 * 机甲解锁和进阶
		 * 
		 * @param player
		 * @param soldierId
		 * @param module
		 */
		private static boolean superSoldierUnlockAndStarUp(Player player, int soldierId, PlayerSuperSoldierModule module) {
			int toStep = 0;
			SuperSoldier superSoldier = null;
			Optional<SuperSoldier> ssoldier = player.getSuperSoldierByCfgId(soldierId);
			if (!ssoldier.isPresent()) {
				superSoldier = module.unLockSuperSoldier(soldierId);
				superSoldier.notifyChange();
				MissionManager.getInstance().postMsg(player, new EventMechaPartLvUp(soldierId));
			} else {
				superSoldier = ssoldier.get();
			}
			
			int toStar = superSoldier.getStar() + 1;
			SuperSoldierStarLevelCfg toStarLevelCfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, soldierId, toStar, toStep);
			if (Objects.isNull(toStarLevelCfg)) {
				return false;
			}
			SuperSoldierStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, soldierId, superSoldier.getStar(), superSoldier.getStep());
			if (toStarLevelCfg.getId() <= starLevelCfg.getId()) {
				return false;
			}
			
			for (SuperSoldierSkillSlot skillSlot : superSoldier.getSkillSlots()) {
				if (skillSlot.getSkill().isMaxLevel()) {
					continue;
				}
				
				skillSlot.getSkill().addExp(9999999);
				superSoldier.notifyChange();
				MissionManager.getInstance().postMsg(player, new EventMechaPartLvUp(soldierId));
			}
			
			superSoldier.starUp(toStar, toStep);
			MissionManager.getInstance().postMsg(player, new EventMechaAdvance(soldierId, toStep));
			GuildMailService.getInstance().sendMail(MailParames.newBuilder()
	                .setPlayerId(player.getId())
	                .setMailId(MailId.GUNDAM_START_UP)
	                .addContents(PBGundamStartUp.newBuilder().setSsoldier(superSoldier.toPBobj()))
	                .addSubTitles(soldierId,superSoldier.getStar())
	                .addTips(soldierId,superSoldier.getStar())
	                .build());
			return true;
		}
		
		/**
		 * 解锁第一个机甲
		 * 
		 * @param player
		 */
		private static void unlockFirstSuperSoldier(Player player) {
			BuildingBaseEntity building = player.getData().getBuildingEntityByType(BuildingType.SUER_SOLDIER_BUILD);
			if (building == null) {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (BuildingType.SUER_SOLDIER_BUILD_VALUE * 100) + 1);
				building = player.getData().createBuildingEntity(buildingCfg, "1", false);
				BuildingService.getInstance().createBuildingFinish(player, building, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
				player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
			}
			
			PlayerSuperSoldierModule module = player.getModule(GsConst.ModuleType.SUPER_SOLDIER);
			module.unLockSuperSoldier(GameConstCfg.getInstance().getSuperSoldierId());
			updateCustomData(player);
			
			// 关联的任务自动完成：任务类型 = 270（获取机甲图纸）
			for (int itemId : ConstProperty.getInstance().getRecordItemList()) {
				RedisProxy.getInstance().getRedisSession().hIncrBy("GainItemTotal:" + player.getId(), String.valueOf(itemId), 100);
				MissionManager.getInstance().postMsg(player, new EventGainItem(itemId, 100));
			}
			
			ConfigIterator<SuperSoldierBuildTaskCfg> taskCfgInterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierBuildTaskCfg.class);
			while (taskCfgInterator.hasNext()) {
				SuperSoldierBuildTaskCfg taskCfg = taskCfgInterator.next();
				MissionManager.getInstance().postMsg(player, new EventMechaPartRepair(taskCfg.getId()));
			}
			
			player.getPush().syncCustomData();
			
			List<SuperSoldier> soldiers = player.getAllSuperSoldier();
			PBSuperSoldierListPush.Builder resp = PBSuperSoldierListPush.newBuilder();
			soldiers.forEach(sd -> resp.addSuperSoldiers(sd.toPBobj()));
			player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_ALL_SUPER_SOLDIER, resp));
		}
		
		private static void updateCustomData(Player player) {
			String customKey = GameConstCfg.getInstance().getSuperSoldierTutorialKey();
			CustomDataEntity entity = player.getData().getCustomDataEntity(customKey);
			if (entity == null) {
				// 这里不调player.getData().createCustomDataEntity接口，是为了防止db容错的一个缺陷（数据落地失败但又触发了容错机制，导致内存跟db不一致）而客户端又强依赖这个数据
				entity = new CustomDataEntity();
				entity.setPlayerId(player.getId());
				entity.setType(customKey);
				entity.setValue(1);
				entity.setArg("");
				entity.setId(HawkOSOperator.randomUUID());
				entity.create(true);
				player.getData().getCustomDataEntities().add(entity);
			} else {
				entity.setValue(1);
			}
			
			// 和前端对齐后，两种情况下调用这个接口都不用同步了
			//player.getPush().syncCustomData();
		}
	}
	
	/**
	 * 一代机甲解锁脚本
	 */
	static class UpgradeAllPreSuperSoldierTask {
		public static String doAction(Map<String, String> params) {
			if (!GsConfig.getInstance().isDebug()) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
			}
			
			try {
				Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
				if (player == null) {
					return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
				}
				
				if (player.isActiveOnline()) {
					int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
					HawkTaskManager.getInstance().postTask(new HawkTask() {
						@Override
						public Object run() {
							if (player.getAllSuperSoldier().isEmpty()) {
								unlockFirstSuperSoldier(player);
							}
							upgradeAllPreSuperSoldier(player);
							return null;
						}
					}, threadIdx);
				} else {
					if (player.getAllSuperSoldier().isEmpty()) {
						unlockFirstSuperSoldier(player);
					}
					upgradeAllPreSuperSoldier(player);
				}
				
				return HawkScript.successResponse("ok");
			} catch (Exception e) {
				HawkException.catchException(e);
				return HawkException.formatStackMsg(e);
			}
		}
		
		/**
		 * 解锁所有的一代机甲
		 * 
		 * @param player
		 */
		private static void upgradeAllPreSuperSoldier(Player player) {
			PlayerSuperSoldierModule module = player.getModule(GsConst.ModuleType.SUPER_SOLDIER);
			ConfigIterator<SuperSoldierCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierCfg.class);
			while (iterator.hasNext()) {
				SuperSoldierCfg cfg = iterator.next();
				if (cfg.getPreSupersoldierId() > 0) {
					continue;
				}
				
				int count = 0;
				while (true) {
					if (!superSoldierUnlockAndStarUp(player, cfg.getSupersoldierId(), module)) {
						break;
					}
					
					count++;
					if (count > 10) {
						break;
					}
				}
			}
		}
		
		/**
		 * 机甲解锁和进阶
		 * 
		 * @param player
		 * @param soldierId
		 * @param module
		 */
		private static boolean superSoldierUnlockAndStarUp(Player player, int soldierId, PlayerSuperSoldierModule module) {
			int toStep = 0;
			SuperSoldier superSoldier = null;
			Optional<SuperSoldier> ssoldier = player.getSuperSoldierByCfgId(soldierId);
			if (!ssoldier.isPresent()) {
				superSoldier = module.unLockSuperSoldier(soldierId);
				superSoldier.notifyChange();
				MissionManager.getInstance().postMsg(player, new EventMechaPartLvUp(soldierId));
			} else {
				superSoldier = ssoldier.get();
			}
			
			int toStar = superSoldier.getStar() + 1;
			SuperSoldierStarLevelCfg toStarLevelCfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, soldierId, toStar, toStep);
			if (Objects.isNull(toStarLevelCfg)) {
				return false;
			}
			SuperSoldierStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, soldierId, superSoldier.getStar(), superSoldier.getStep());
			if (toStarLevelCfg.getId() <= starLevelCfg.getId()) {
				return false;
			}
			
			for (SuperSoldierSkillSlot skillSlot : superSoldier.getSkillSlots()) {
				if (skillSlot.getSkill().isMaxLevel()) {
					continue;
				}
				
				skillSlot.getSkill().addExp(9999999);
				superSoldier.notifyChange();
				MissionManager.getInstance().postMsg(player, new EventMechaPartLvUp(soldierId));
			}
			
			superSoldier.starUp(toStar, toStep);
			MissionManager.getInstance().postMsg(player, new EventMechaAdvance(soldierId, toStep));
			GuildMailService.getInstance().sendMail(MailParames.newBuilder()
	                .setPlayerId(player.getId())
	                .setMailId(MailId.GUNDAM_START_UP)
	                .addContents(PBGundamStartUp.newBuilder().setSsoldier(superSoldier.toPBobj()))
	                .addSubTitles(soldierId,superSoldier.getStar())
	                .addTips(soldierId,superSoldier.getStar())
	                .build());
			return true;
		}
		
		/**
		 * 解锁第一个机甲
		 * 
		 * @param player
		 */
		private static void unlockFirstSuperSoldier(Player player) {
			BuildingBaseEntity building = player.getData().getBuildingEntityByType(BuildingType.SUER_SOLDIER_BUILD);
			if (building == null) {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (BuildingType.SUER_SOLDIER_BUILD_VALUE * 100) + 1);
				building = player.getData().createBuildingEntity(buildingCfg, "1", false);
				BuildingService.getInstance().createBuildingFinish(player, building, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
				player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
			}
			
			PlayerSuperSoldierModule module = player.getModule(GsConst.ModuleType.SUPER_SOLDIER);
			module.unLockSuperSoldier(GameConstCfg.getInstance().getSuperSoldierId());
			updateCustomData(player);
			
			// 关联的任务自动完成：任务类型 = 270（获取机甲图纸）
			for (int itemId : ConstProperty.getInstance().getRecordItemList()) {
				RedisProxy.getInstance().getRedisSession().hIncrBy("GainItemTotal:" + player.getId(), String.valueOf(itemId), 100);
				MissionManager.getInstance().postMsg(player, new EventGainItem(itemId, 100));
			}
			
			ConfigIterator<SuperSoldierBuildTaskCfg> taskCfgInterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierBuildTaskCfg.class);
			while (taskCfgInterator.hasNext()) {
				SuperSoldierBuildTaskCfg taskCfg = taskCfgInterator.next();
				MissionManager.getInstance().postMsg(player, new EventMechaPartRepair(taskCfg.getId()));
			}
			
			player.getPush().syncCustomData();
			
			List<SuperSoldier> soldiers = player.getAllSuperSoldier();
			PBSuperSoldierListPush.Builder resp = PBSuperSoldierListPush.newBuilder();
			soldiers.forEach(sd -> resp.addSuperSoldiers(sd.toPBobj()));
			player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_ALL_SUPER_SOLDIER, resp));
		}
		
		private static void updateCustomData(Player player) {
			String customKey = GameConstCfg.getInstance().getSuperSoldierTutorialKey();
			CustomDataEntity entity = player.getData().getCustomDataEntity(customKey);
			if (entity == null) {
				// 这里不调player.getData().createCustomDataEntity接口，是为了防止db容错的一个缺陷（数据落地失败但又触发了容错机制，导致内存跟db不一致）而客户端又强依赖这个数据
				entity = new CustomDataEntity();
				entity.setPlayerId(player.getId());
				entity.setType(customKey);
				entity.setValue(1);
				entity.setArg("");
				entity.setId(HawkOSOperator.randomUUID());
				entity.create(true);
				player.getData().getCustomDataEntities().add(entity);
			} else {
				entity.setValue(1);
			}
			
			// 和前端对齐后，两种情况下调用这个接口都不用同步了
			//player.getPush().syncCustomData();
		}
	}

	/**
	 * 解锁首个机甲的脚本
	 */
	static class FirstSuperSoldierUnlockTask {
		public static String doAction(Map<String, String> params) {
			if (!GsConfig.getInstance().isDebug()) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
			}
			
			try {
				Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
				if (player == null) {
					return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
				}
				
				if (!player.getAllSuperSoldier().isEmpty()) {
					return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player mecha not empty");
				}
				
				if (player.isActiveOnline()) {
					int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
					HawkTaskManager.getInstance().postTask(new HawkTask() {
						@Override
						public Object run() {
							unlockSuperSoldier(player);
							return null;
						}
					}, threadIdx);
				} else {
					unlockSuperSoldier(player);
				}
				
				return HawkScript.successResponse("ok");
			} catch (Exception e) {
				HawkException.catchException(e);
				return HawkException.formatStackMsg(e);
			}
		}
		
		private static void unlockSuperSoldier(Player player) {
			BuildingBaseEntity building = player.getData().getBuildingEntityByType(BuildingType.SUER_SOLDIER_BUILD);
			if (building == null) {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (BuildingType.SUER_SOLDIER_BUILD_VALUE * 100) + 1);
				building = player.getData().createBuildingEntity(buildingCfg, "1", false);
				BuildingService.getInstance().createBuildingFinish(player, building, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
				player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
			}
			
			PlayerSuperSoldierModule module = player.getModule(GsConst.ModuleType.SUPER_SOLDIER);
			module.unLockSuperSoldier(GameConstCfg.getInstance().getSuperSoldierId());
			updateCustomData(player);
			
			// 关联的任务自动完成：任务类型 = 270（获取机甲图纸）
			for (int itemId : ConstProperty.getInstance().getRecordItemList()) {
				RedisProxy.getInstance().getRedisSession().hIncrBy("GainItemTotal:" + player.getId(), String.valueOf(itemId), 100);
				MissionManager.getInstance().postMsg(player, new EventGainItem(itemId, 100));
			}
			
			ConfigIterator<SuperSoldierBuildTaskCfg> taskCfgInterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierBuildTaskCfg.class);
			while (taskCfgInterator.hasNext()) {
				SuperSoldierBuildTaskCfg taskCfg = taskCfgInterator.next();
				MissionManager.getInstance().postMsg(player, new EventMechaPartRepair(taskCfg.getId()));
			}
			
			player.getPush().syncCustomData();
			
			List<SuperSoldier> soldiers = player.getAllSuperSoldier();
			PBSuperSoldierListPush.Builder resp = PBSuperSoldierListPush.newBuilder();
			soldiers.forEach(sd -> resp.addSuperSoldiers(sd.toPBobj()));
			player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_ALL_SUPER_SOLDIER, resp));
		}
		
		private static void updateCustomData(Player player) {
			String customKey = GameConstCfg.getInstance().getSuperSoldierTutorialKey();
			CustomDataEntity entity = player.getData().getCustomDataEntity(customKey);
			if (entity == null) {
				// 这里不调player.getData().createCustomDataEntity接口，是为了防止db容错的一个缺陷（数据落地失败但又触发了容错机制，导致内存跟db不一致）而客户端又强依赖这个数据
				entity = new CustomDataEntity();
				entity.setPlayerId(player.getId());
				entity.setType(customKey);
				entity.setValue(1);
				entity.setArg("");
				entity.setId(HawkOSOperator.randomUUID());
				entity.create(true);
				player.getData().getCustomDataEntities().add(entity);
			} else {
				entity.setValue(1);
			}
			
			// 和前端对齐后，两种情况下调用这个接口都不用同步了
			//player.getPush().syncCustomData();
		}
	}
	
	/**
	 * 建筑一键满级脚本
	 * @author Admin
	 *
	 */
	static class BuildingUpGradeToMaxTask {
		public static String doAction(Map<String, String> params) {
			if (!GsConfig.getInstance().isDebug()) {
				return HawkScript.failedResponse(SCRIPT_ERROR, "");
			}
			
			try {
				if (!GsConfig.getInstance().isDebug()) {
					return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
				}
				
				Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
				if (player == null) {
					return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
				}
				
				unlockArea(player);
				
				AtomicInteger indexObj = new AtomicInteger(1);
				for (int buildType : getBuildTypeSet()) {
					if (buildType == BuildingType.PRISM_TOWER_VALUE) {
						specialBuildLevelUp(player);
						specialBuildLevelUp(player);
						continue;
					}
					
					if (BuildAreaCfg.isShareBlockBuildType(buildType)) {
						unlockShareBlockBuild(player, buildType, indexObj);
						continue;
					}
					
					BuildingBaseEntity buildingEntity = getBuildingBaseEntity(player, buildType);
					if (buildingEntity == null) {
						BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (buildType * 100) + 1);
						buildingEntity = player.getData().createBuildingEntity(buildingCfg, "1", false);
						BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
					}
					
					BuildingCfg oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
					BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
					while (buildingCfg != null && oldBuildCfg.getLevel() < 30 && buildingCfg.getBuildType() == buildType) {
						BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
						oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
						buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
					}
				}
				player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
				return HawkScript.successResponse("ok");
			} catch (Exception e) {
				HawkException.catchException(e);
				return HawkException.formatStackMsg(e);
			}
		}
		
		private static void specialBuildLevelUp(Player player) {
			BuildingBaseEntity buildingEntity = getBuildingBaseEntity(player, BuildingType.PRISM_TOWER_VALUE);
			if (buildingEntity == null) {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (BuildingType.PRISM_TOWER_VALUE * 100) + 1);
				buildingEntity = player.getData().createBuildingEntity(buildingCfg, "1", false);
				BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
			} else {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (BuildingType.PRISM_TOWER_VALUE * 100) + 1);
				buildingEntity = player.getData().createBuildingEntity(buildingCfg, "2", false);
				BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
			}
			
			BuildingCfg oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
			while (buildingCfg != null && oldBuildCfg.getLevel() < 30 && buildingCfg.getBuildType() == BuildingType.PRISM_TOWER_VALUE) {
				BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
				oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
				buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
			}
		}
		
		private static void unlockShareBlockBuild(Player player, int buildType, AtomicInteger indexObj) {
			int count = player.getData().getBuildCount(BuildingType.valueOf(buildType));
			while (count < 5) {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (buildType * 100) + 1);
				BuildingBaseEntity buildingEntity = player.getData().createBuildingEntity(buildingCfg, String.valueOf(indexObj.get()), false);
				indexObj.addAndGet(1);
				count++;
				BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
			}
			
			List<BuildingBaseEntity> buildingList = player.getData().getBuildingListByType(BuildingType.valueOf(buildType));
			for (BuildingBaseEntity buildingEntity : buildingList) {
				BuildingCfg oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
				BuildingCfg newBuildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
				while (newBuildingCfg != null && oldBuildCfg.getLevel() < 30 && newBuildingCfg.getBuildType() == buildType) {
					BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
					oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
					newBuildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
				}
			}
		}
		
		private static void unlockArea(Player player) {
			try {
				Set<Integer> unlockedAreas = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
				ConfigIterator<BuildAreaCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BuildAreaCfg.class);
				while (iterator.hasNext()) {
					BuildAreaCfg cfg = iterator.next();
					int areaId = cfg.getId();
					if (unlockedAreas.contains(areaId)) {
						continue;
					}
					
					player.unlockArea(areaId);			
					// 解锁地块任务
					MissionManager.getInstance().postMsg(player, new EventUnlockGround(areaId));
					MissionManager.getInstance().postSuperSoldierTaskMsg(player, new SuperSoldierTriggeTaskMsg(SupersoldierTaskType.UNLOCK_AREA_TASK, 1));
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			// 推送建筑信息
			player.getPush().synUnlockedArea();
		}
		
		/**
		 * 根据建筑cfgId获取建筑实体
		 * @param id
		 */
		public static BuildingBaseEntity getBuildingBaseEntity(Player player, int buildCfgId) {
			Optional<BuildingBaseEntity> op = player.getData().getBuildingEntities().stream()
					.filter(e -> e.getStatus() != BuildingStatus.BUILDING_CREATING_VALUE)
					.filter(e -> e.getBuildingCfgId() / 100 == buildCfgId)
					.findAny();
			if(op.isPresent()) {
				return op.get();
			}
			return null;
		}
		
		
		/**
		 * 获取需要升级至满级的建筑列表
		 * @return
		 */
		private static Set<Integer> getBuildTypeSet() {
			Set<Integer> set = new HashSet<>();
			
			ConfigIterator<BuildingCfg> buildCfgIterator = HawkConfigManager.getInstance().getConfigIterator(BuildingCfg.class);
			while (buildCfgIterator.hasNext()) {
				BuildingCfg buildCfg = buildCfgIterator.next();
				if (buildCfg.getLevel() > 1) {
					continue;
				}
				BuildLimitCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildLimitCfg.class, buildCfg.getLimitType());
				if (cfg == null) {
					continue;
				}
				set.add(buildCfg.getBuildType());
			}
			return set;
		}
	}
	
}