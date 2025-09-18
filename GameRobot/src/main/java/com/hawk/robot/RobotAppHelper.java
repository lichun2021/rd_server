package com.hawk.robot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkXmlCfg;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotApp;
import org.hawk.robot.HawkRobotEntity;
import org.hawk.util.HawkClassScaner;

import com.hawk.robot.action.PlayerHeartBeatAction;
import com.hawk.robot.config.ConstProperty;
import com.hawk.robot.response.RobotResponseManager;
import com.hawk.robot.util.NamedThreadFactory;

/**
 * GameRobotApp帮助类
 * 
 * @author lating
 *
 */
public class RobotAppHelper {
	/**
	 * 登录线程池
	 */
	private ExecutorService loginPool;
	/**
	 * 业务线程池
	 */
	private ScheduledExecutorService taskPool;
	/**
	 * 机器人起始id
	 */
	private int startId = 0;
	/**
	 * 
	 */
	private int robotMaxId = 0;
	/**
	 * 心跳Action
	 */
	private HawkRobotAction heartBeatAction;
	
	/**
	 * 单例对象
	 */
	private static RobotAppHelper instance;
	
	/**
	 * 私有构造
	 */
	private RobotAppHelper() {}
	
	
	public static RobotAppHelper getInstance() {
		if (instance == null) {
			instance = new RobotAppHelper();
		}
		
		return instance;
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public boolean init(HawkXmlCfg cfg) {
		try {
			// 初始化登录线程池
			loginPool = Executors.newFixedThreadPool(cfg.getInt("robot.loginThreads"), new NamedThreadFactory("Login"));
			// 初始化业务线程池
			taskPool = Executors.newScheduledThreadPool(cfg.getInt("robot.taskThreads"), new NamedThreadFactory("TaskExecutor"));
			
			// 初始化action
			initRobotActions();
			// 扫描服务器响应处理器
			RobotResponseManager.getInstance().scanRobotResponsor();
			// 添加统计日志线程
			StatisticHelper.startStatisticTask(60);
			
			startId = RobotAppConfig.getInstance().getRobotStartId();
			robotMaxId = startId + RobotAppConfig.getInstance().getRobotRegisterCnt();
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
		
	}
	
	/**
	 * 用户登录
	 * @param puid
	 */
	public void userLogin(GameRobotEntity robotEntity){
		this.loginPool.execute(new Runnable() {
			@Override
			public void run() {
				robotEntity.doLogin();
				try {
					Thread.sleep(GameRobotApp.getInstance().getConfig().getInt("robot.loginGap"));
				} catch (InterruptedException e) {
					HawkException.catchException(e);
				}
			}
		});
	}
	
	/**
	 * 投递任务
	 * @param task
	 */
	public void executeTask(Runnable task) {
		this.taskPool.execute(task);
	}
	
	/**
	 * 投递延时任务
	 * @param task
	 * @param delay 延时时间，单位ms
	 */
	public void executeDelayTask(Runnable task, long delay) {
		this.taskPool.schedule(task, delay, TimeUnit.MILLISECONDS);
	}
	
	public HawkRobotAction getHeartBeatAction() {
		return heartBeatAction;
	}

	public void setHeartBeatAction(HawkRobotAction heartBeatAction) {
		this.heartBeatAction = heartBeatAction;
	}
	
	public boolean isFullRegister() {
		return StatisticHelper.getRegisterSuccCnt() >= RobotAppConfig.getInstance().getRobotRegisterCnt();
	}
	
	/**
	 * 在线机器人检测, 判断各在线玩家是否已到退出时间
	 * @return
	 */
	public int robotDetect(boolean detectEnable) {
		if(!detectEnable) {
			return 0;
		}
		// 现有在线的机器人
		Set<HawkRobotEntity> robotEntries = HawkRobotApp.getInstance().getRobotEntities();
		
		int removeCount = GameRobotApp.getInstance().getConfig().getInt("removeCount");
		// 将要移除的机器人
		List<GameRobotEntity> removeEntities = null;
		// 注册人数没满时，为了加快注册数进度，移除数量加倍, 同时, 不管有没有达到下线时间，都加入移除列表
		if (!isFullRegister()) {
			removeCount *= 2;
			removeEntities = new ArrayList<>(removeCount);
			for (HawkRobotEntity robotEntity : robotEntries) {
				GameRobotEntity robot = (GameRobotEntity) robotEntity;
				removeEntities.add(robot);
				if (removeEntities.size() >= removeCount) {
					break;
				}
			}
		} else {
			removeEntities = new ArrayList<>();
			for (HawkRobotEntity robotEntity : robotEntries) {
				GameRobotEntity robot = (GameRobotEntity) robotEntity;
				// 自动退出时间还没到或没有设置退出时间
				if(robot.getOfflineTime() > HawkTime.getMillisecond() || robot.getOfflineTime() <= 0) {
					continue;
				}
				
				removeEntities.add(robot);
			}
		}
		
		if (removeEntities.isEmpty()) {
			return 0;
		}
		
		removeCount = removeEntities.size() > removeCount ? removeCount : removeEntities.size();
		Collections.shuffle(removeEntities);
		
		if (PlayerHeartBeatAction.checkResetAccount() && heartBeatAction != null) {
			GameRobotEntity robot = removeEntities.get(HawkRand.randInt(removeEntities.size() -1));
			heartBeatAction.doAction(robot);
		}
		
		List<GameRobotEntity> subList = removeEntities.subList(0, removeCount);
		int lowLevelTotal = StatisticHelper.getOfflineLowLevelRobotCnt();
		int lowLevelAdd = 0;
		int lowLevelAddTotal = GameRobotApp.getInstance().getConfig().getInt("robot.offlineCount");
		int lowLevelAddOnce = GameRobotApp.getInstance().getConfig().getInt("robot.addCountOnce");
		for(GameRobotEntity robotEntity : subList) {
			robotEntity.doLogout();
			if (lowLevelAdd < lowLevelAddOnce && lowLevelTotal < lowLevelAddTotal 
					&& robotEntity.getCityLevel() < ConstProperty.getInstance().getRebuildLevel()) {
				lowLevelAdd ++;
				lowLevelTotal ++;
				StatisticHelper.addLowLevelRobot(robotEntity.getPuid());
			}
			
			HawkLog.logPrintln("robot session closed offline, puid: {}", robotEntity.getPuid());
		}
		
		// 从现有机器人里面移除已退出的机器人
		robotEntries.removeAll(subList);
		return subList.size();
	}
	
	/**
	 * 对注册失败的额玩家进行重新注册
	 */
	public void registerAgain() {
		Set<String> registerFailed = StatisticHelper.getRegisterFailed();
		if (registerFailed.isEmpty()) {
			HawkLog.logPrintln("register again failed, there is no account to add");
			return;
		}
		
		Set<String> addSet = new HashSet<>(registerFailed);
		addOldRobot(addSet.size() > 50 ? 50 : addSet.size(), addSet);
	}
	
	/**
	 * 在线机器人检测后，补充机器人
	 * @param addCount
	 */
	public void detectAddRobot(int addCount) {
		final long offlineTimeLong = GameRobotApp.getInstance().getConfig().getInt("robot.offlineDuration") * 1000L;
		final long loginAgainTimeLong = GameRobotApp.getInstance().getConfig().getInt("robot.loginAgain") * 1000L;
		
		if(addRobot(addCount) > 0) {
			int registerCnt = StatisticHelper.getRegisterSuccCnt();
			int onlineCnt = StatisticHelper.getOnlineRobotCnt();
			HawkLog.logPrintln("add robot new: {}, onlineCnt: {}, registerCount: {}, max puid: {}", addCount, onlineCnt, registerCnt, startId);
			return;
		} 
		
		// 检索退出时长已达到offlineTimeLong的机器人
		List<String> puids = StatisticHelper.getOfflineLongEnoughRobots(offlineTimeLong, loginAgainTimeLong);
		addOldRobot(addCount, puids);
		
	}
	
	/**
	 * 从已下线的机器人中选取登录
	 * @param addCount
	 * @param puids
	 */
	private void addOldRobot(int addCount, Collection<String> puids) {
		int add = addCount;
		robotConnect(puids, addCount);
		if(addCount > puids.size()) {
			int addNew = add - puids.size(); 
			addRobot(addNew);
			add = puids.size();
		}
		
		int registerCnt = StatisticHelper.getRegisterSuccCnt();
		int onlineCnt = StatisticHelper.getOnlineRobotCnt();
		HawkLog.logPrintln("add robot old: {}, onlineCnt: {}, registerCount: {}, max puid: {}", add, onlineCnt, registerCnt, startId);
	}

	/**
	 * 添加新的机器人
	 * @param count
	 * @return
	 */
	public int addRobot(int count) {
		if(startId >= robotMaxId) {
			return 0;
		}
		
		int maxRobotId = startId + count;
		if(maxRobotId > robotMaxId) {
			maxRobotId = robotMaxId;
			count = maxRobotId - startId;
		}
		
		robotConnect(new ArrayList<String>(), count);
		startId = maxRobotId;
		
		return count;
	}
	
	/**
	 * 机器人连接
	 * @param puids
	 * @param count
	 */
	private void robotConnect(final Collection<String> puids, final int count) {
		if(puids.isEmpty()) {
			for (int i = startId; i < startId + count; i++) {
				puids.add(RobotAppConfig.getInstance().getRobotPuidPrefix() + (i + 1));
			}
		}
		
		new Thread() {
			public void run () {
				int changeCount = count;
				for (String puid : puids) {
					robotConnect(puid);
					if(--changeCount < 0) {
						break;
					}
					
					HawkOSOperator.sleep();
				}
			}
		}.start();
	}
	
	/**
	 * 机器人异常断线后重新连接
	 * @param puid
	 */
	public void robotReconnect(String puid) {
		this.loginPool.execute(new Runnable() {
			@Override
			public void run() {
				robotConnect(puid);
			}
		});
	}
	
	/**
	 * 机器人主动连接
	 * @param puid
	 */
	private void robotConnect(String puid) {
		GameRobotEntity robotEntity = new GameRobotEntity();
		robotEntity.init(puid);
		robotEntity.connect();
	}
	
	/**
	 * 注册机器人action
	 * 当参数auto配置为false时，表示手动针对Action测试使用， true表示大量机器人连续自动测试
	 */
	private void initRobotActions() {
		// 每个Action执行周期的偏移量随机值
		int delayRandomOffset = GameRobotApp.getInstance().getConfig().getInt("robot.offset");
		List<Class<?>> classList = HawkClassScaner.scanClassesFilter(RobotAppConfig.getInstance().getActionClassPath(), RobotAction.class);
		if(!GameRobotApp.getInstance().getConfig().getBoolean("debug")) {
			Optional<Class<?>> op = classList.stream().filter(e -> e.getSimpleName().equals("PlayerHeartBeatAction")).findAny();
			if(op.isPresent()) {
				try {
					setHeartBeatAction((HawkRobotAction) op.get().newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					HawkException.catchException(e);
				}
			}
		}
		
		String manualAction = GameRobotApp.getInstance().getConfig().getString("manualAction");
		List<String> names = null;
		if(!HawkOSOperator.isEmptyString(manualAction)){
			HawkLog.logPrintln("load manualAction config: {}", manualAction);
			names = Arrays.asList(manualAction.replace(" ", "").split(";"));
		} else {
			names = new ArrayList<String>();
		}
		
		// 非自动测试时仅加载需要的Action
		if(!GameRobotApp.getInstance().getConfig().getBoolean("auto")) {
			initManualRobotActions(classList, names);
		} else { // 自动时加载所有过滤后的Action
			for (Class<?> clzz : classList) {
				RobotAction annot = clzz.getAnnotation(RobotAction.class);
				if (annot == null) {
					continue;
				}
				
				try {
					String paramKey = "actionPeriod." + clzz.getSimpleName();
					if(!GameRobotApp.getInstance().getConfig().containsKey(paramKey)) {
						continue;
					}
					
					int period = GameRobotApp.getInstance().getConfig().getInt(paramKey);
					if(period <= 0) {
						continue;
					}
					
					HawkRobotAction action = (HawkRobotAction) clzz.newInstance();
					if (!names.contains(clzz.getSimpleName())) {
						action.setRandom(true);
					}
					action.setActionPeriod(period * 1000);
					action.setRandOffset(delayRandomOffset * 1000);
					GameRobotApp.getInstance().addAction(action);
					HawkLog.logPrintln("load Action : {}", clzz.getSimpleName());
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}
	
	/**
	 * 加载手动Action
	 * @param classList
	 * @param names
	 */
	private void initManualRobotActions(List<Class<?>> classList, List<String> names){
		// 每个Action执行周期的偏移量随机值
		int delayRandomOffset = GameRobotApp.getInstance().getConfig().getInt("robot.offset");
		for (Class<?> clzz : classList) {
			RobotAction annot = clzz.getAnnotation(RobotAction.class);
			if (annot == null) {
				continue;
			}
			
			for (String name : names) {
				if(!name.trim().equals(clzz.getSimpleName())){
					continue;
				}
				
				try {
					String paramKey = "actionPeriod." + clzz.getSimpleName();
					if(!GameRobotApp.getInstance().getConfig().containsKey(paramKey)) {
						continue;
					}
					int period = GameRobotApp.getInstance().getConfig().getInt(paramKey);
					if(period <= 0) {
						continue;
					}
					HawkRobotAction action = (HawkRobotAction) clzz.newInstance();
					action.setActionPeriod(period * 1000);
					action.setRandOffset(delayRandomOffset * 1000);
					GameRobotApp.getInstance().addAction(action);
					HawkLog.logPrintln("load Action : {}", clzz.getSimpleName());
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}

}
