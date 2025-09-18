package com.hawk.robot;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;
import org.hawk.robot.HawkRobotAction.ActionStat;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.HP;

/**
 * 机器人运行数据统计类
 * 
 * @author lating
 *
 */
public class StatisticHelper {
	/**
	 * 已下线的玩家<下线时间，玩家puid>
	 */
	private static Map<String, Long> offlineRobots = new ConcurrentHashMap<>();
	/**
	 * 已下线的低等级玩家
	 */
	private static List<String> offlineLowLevelRobots = new CopyOnWriteArrayList<String>();
	/**
	 * 注册成功的玩家
	 */
	private static Set<String> registerSuccAccounts = new ConcurrentHashSet<>();
	/**
	 * 注册失败的玩家
	 */
	private static Set<String> registerFailedAccounts = new ConcurrentHashSet<>();
	/**
	 * 正在登录的玩家
	 */
	private static Set<String> loginingAccounts = new ConcurrentHashSet<>();
	
	/**
	 * 在线人数
	 */
	private static AtomicInteger onlineCount = new AtomicInteger(0);
	
	/**
	 * 统计任务对象
	 */
	private static GameRobotLogTask statisticTask;
	
	/**
	 * 协议发送数量
	 */
	private static Map<Integer, AtomicLong> sendProtocolCount;
	/**
	 * 执行成功的协议数量
	 */
	private static Map<Integer, AtomicLong> successProtocolCount;
	/**
	 * 执行失败的协议数量
	 */
	private static Map<Integer, AtomicLong> errorProtocolCount;
	
	
	/**
	 * 私有构造
	 */
	private StatisticHelper() {}
	
	////////////////// 注册成功相关     //////////////////
	public static void addRegisterSucc(String puid) {
		registerSuccAccounts.add(puid);
	}
	
	public static boolean isRegisterSucc(String puid) {
		return registerSuccAccounts.contains(puid);
	}
	
	public static int getRegisterSuccCnt() {
		return registerSuccAccounts.size();
	}
	
	//////////// 注册失败相关  ///////////////////////
	public static void addRegisterFailed(String puid) {
		registerFailedAccounts.add(puid);
	}
	
	public static boolean isRegisterFailed(String puid) {
		return registerFailedAccounts.contains(puid);
	}
	
	public static int getRegisterFailedCnt() {
		return registerFailedAccounts.size();
	}
	
	public static void removeRegisterFailed(String puid) {
		registerFailedAccounts.remove(puid);
	}
	
	public static Set<String> getRegisterFailed() {
		return registerFailedAccounts;
	}
	
	////////////////////// 正在登录的玩家处理   ////////////////////////
	
	public static void addLogin(String puid) {
		loginingAccounts.add(puid);
		
		offlineRobots.remove(puid);
		if (offlineLowLevelRobots.contains(puid)) {
			offlineLowLevelRobots.remove(puid);
		}
	}
	
	public static void removeLogin(String puid) {
		loginingAccounts.remove(puid);
	}
	
	public static boolean isLoginAccount(String puid) {
		return loginingAccounts.contains(puid);
	}
	
	/**
	 * 获取登录人数
	 * @return
	 */
	public static int getLoginRobotCnt() {
		return loginingAccounts.size();
	}
	
	/**
	 * 玩家下线
	 */
	public static void robotOffline(HawkRobotEntity robotEntity) {
		decOnlineCnt();
		offlineRobots.put(robotEntity.getPuid(), HawkTime.getMillisecond());
	}
	
	/**
	 * 获取不在线的玩家数量
	 * @return
	 */
	public static int getOfflineRobotCnt() {
		return offlineRobots.size();
	}
	
	/**
	 * 获取离线时长达到指定时长的玩家
	 * @param offlineTimeLong
	 * @param loginAgainTimeLong
	 * @return
	 */
	public static List<String> getOfflineLongEnoughRobots(long offlineTimeLong, long loginAgainTimeLong) {
		long now = HawkTime.getMillisecond();
		return offlineRobots.keySet().stream()
		.filter(e -> now - offlineRobots.get(e) > offlineTimeLong)
		.filter(e -> !offlineLowLevelRobots.contains(e) || now - offlineRobots.get(e) > loginAgainTimeLong)
		.collect(Collectors.toList());
	}
	
	/**
	 * 添加低等级玩家
	 * @param puid
	 */
	public static void addLowLevelRobot(String puid) {
		offlineLowLevelRobots.add(puid);
	}
	
	/**
	 * 获取不在线的低等级玩家数量
	 * @return
	 */
	public static int getOfflineLowLevelRobotCnt() {
		return offlineLowLevelRobots.size();
	}
	
	/**
	 * 增加实际在线人数
	 * @return
	 */
	public static int incrOnlineCnt() {
		return onlineCount.incrementAndGet();
	}
	
	/**
	 * 减少实际在线人数
	 * @return
	 */
	public static int decOnlineCnt() {
		return onlineCount.decrementAndGet();
	}
	
	/**
	 * 获取在线人数
	 * @return
	 */
	public static int getOnlineRobotCnt() {
		return onlineCount.intValue();
	}
	
	
	/////////////////////////////////////////////////////////
	             /// 协议相关统计  //////
	//////////////////////////////////////////////////////////
	
	
	/**
	 * 添加协议发送数量
	 * 
	 * @param protoType 协议号
	 * @return
	 */
	public static void incProtocolSendCnt(int protoType) {
		if (!RobotAppConfig.getInstance().isStatisticEnable()) {
			return;
		}
		
		AtomicLong countObj = sendProtocolCount.get(protoType);
		if (countObj == null) {
			sendProtocolCount.putIfAbsent(protoType, new AtomicLong(0));
			 countObj = sendProtocolCount.get(protoType);
		}
		
		countObj.incrementAndGet();
	}
	
	/**
	 * 添加协议发送数量
	 * @param protoType 协议号
	 * @return
	 */
	public static void incSuccessProtocolCnt(int protoType) {
		if (!RobotAppConfig.getInstance().isStatisticEnable()) {
			return;
		}
		
		AtomicLong countObj = successProtocolCount.get(protoType);
		if (countObj == null) {
			successProtocolCount.putIfAbsent(protoType, new AtomicLong(0));
			countObj = successProtocolCount.get(protoType);
		}
		
		countObj.incrementAndGet();
	}
	
	/**
	 * 获取指定协议的发送数量
	 * @param protoType 协议号
	 * @return
	 */
	public static void incErrorProtocolCnt(int protoType) {
		if (!RobotAppConfig.getInstance().isStatisticEnable()) {
			return;
		}
		
		AtomicLong countObj = errorProtocolCount.get(protoType);
		if (countObj == null) {
			errorProtocolCount.putIfAbsent(protoType, new AtomicLong(0));
			countObj = errorProtocolCount.get(protoType);
		}
		
		countObj.incrementAndGet();
	}
	
	/**
	 * 启动机器人测试相关统计任务
	 * 
	 * @param period 任务执行时间间隔， 单位second
	 */
	public static void startStatisticTask(int period) {
		if (!RobotAppConfig.getInstance().isStatisticEnable()) {
			return;
		}
		
		if (statisticTask != null || period <= 0) {
			return;
		}
		
		sendProtocolCount = new ConcurrentHashMap<>();
		successProtocolCount = new ConcurrentHashMap<>();
		errorProtocolCount = new ConcurrentHashMap<>();
		
		statisticTask = new GameRobotLogTask();
		ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();
		pool.scheduleAtFixedRate(statisticTask, 60, period, TimeUnit.SECONDS);
	}
	
	/**
	 * 定期输出日志
	 * 
	 * @author zhenyu.shang
	 * @since 2017年6月30日
	 */
	private static class GameRobotLogTask implements Runnable {
		/**
		 * 时间打印
		 */
		private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		@Override
		public void run() {
			Collection<HawkRobotEntity> entities = GameRobotApp.getInstance().getRobotEntities();
			HawkLog.logPrintln("robot entity num : {}", entities.size());
			for (HawkRobotEntity entity : entities) {
				StringBuffer sb = new StringBuffer("robot : " + entity.getPuid() + ", Action list: \r\n");
				for (Class<? extends HawkRobotAction> actionClz : entity.getActions()) {
					ActionStat actionStat = entity.getActionStat(actionClz);

					sb.append("[ActionState] ActionClass = " + actionClz.getSimpleName());
					sb.append(", TriggerTimes = " + actionStat.getTriggerTimes());
					sb.append(", ActiveTime = " + format.format(new Date(actionStat.getActiveTime())));
					sb.append(", LastTrigTime = " + format.format(new Date(actionStat.getLastTrigTime())));
					sb.append(", NextTrigTime = " + format.format(new Date(actionStat.getNextTrigTime())));
					sb.append("\r\n");
				}
				HawkLog.debugPrintln(sb.toString());
			}
			
			long successMarchCount = WorldDataManager.getInstance().getSuccessMarchCount();
			long delMarchCount = WorldDataManager.getInstance().getDelMarchCount();
			HawkLog.logPrintln("start march total: {}, success march total: {}, delete march total: {}, alive march count: {}, real march count: {}", 
					WorldDataManager.getInstance().getStartMarchCount(), successMarchCount,
					delMarchCount, successMarchCount - delMarchCount, WorldDataManager.getInstance().getWorldMarchCount());
			
			JSONObject json = new JSONObject();
			for (Entry<Integer, AtomicLong> entry : successProtocolCount.entrySet()) {
				AtomicLong sendCount = sendProtocolCount.get(entry.getKey());
				long count = sendCount != null ? sendCount.get() : 0;
				json.put(HP.code.valueOf(entry.getKey()).name(), count + ":" +entry.getValue().get());
			}

			HawkLog.logPrintln("success protocol info: {}", json.toJSONString());
			
			JSONObject sendProtocolJson = new JSONObject();
			for (Entry<Integer, AtomicLong> entry : sendProtocolCount.entrySet()) {
				HP.code code = HP.code.valueOf(entry.getKey());
				if (code != null) {
					sendProtocolJson.put(code.name(), entry.getValue().get());
				}
			}
			
			HawkLog.logPrintln("send protocol info: {}", sendProtocolJson.toJSONString());
			
			JSONObject errProtocolJson = new JSONObject();
			for (Entry<Integer, AtomicLong> entry : errorProtocolCount.entrySet()) {
				HP.code code = HP.code.valueOf(entry.getKey());
				if (code != null) {
					errProtocolJson.put(code.name(), entry.getValue().get());
				}
			}

			HawkLog.logPrintln("error protocol info: {}", errProtocolJson.toJSONString());
			
		}
	}

}
