package com.hawk.robot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigStorage;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotApp;
import org.hawk.robot.HawkRobotEntity;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.robot.GameRobotEntity.RobotState;
import com.hawk.robot.action.anchor.AnchorChatAction;
import com.hawk.robot.config.MapBlock;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class GameRobotApp extends HawkRobotApp {
	/**
	 * io事件处理器
	 */
	private Bootstrap bootstrap;
	
	private Bootstrap anchorBootstrap;
	/**
	 * 是否 要检测机器人在线时长，需要检测时, 机器人在线达到一定时长时将下线，其它离线机器人或新的机器人登录来补充
	 */
	private boolean detectEnable = false;
	/**
	 * 全局静态对象
	 */
	private static GameRobotApp instance = null;
	
	/**
	 * 获取全局静态对象
	 * 
	 * @return
	 */
	public static GameRobotApp getInstance() {
		return instance;
	}

	/**
	 * 构造函数
	 */
	public GameRobotApp() {
		super();

		if (instance == null) {
			instance = this;
		}
	}

	/**
	 * 使用配置文件初始化
	 * 
	 * @param cfgFile
	 * @return
	 */
	@Override
	public boolean init(String xmlCfg) {
		RobotAppConfig appCfg = null;
		try {
			HawkConfigStorage cfgStorage = new HawkConfigStorage(RobotAppConfig.class);
			appCfg = (RobotAppConfig) cfgStorage.getConfigByIndex(0);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		
		if (!super.init(xmlCfg)) {
			HawkLog.errPrintln("robot init config.xml failed");
			return false;
		}
		//加载地图阻挡信息
		MapBlock.getInstance().init();
		
		if(!HawkConfigManager.getInstance().init(appCfg.getConfigPath())) {
			HawkLog.errPrintln("robot init xml config failed");
			return false;
		}
		
		if (!initBootstrap(appCfg.getTimeout())) {
			HawkLog.errPrintln("robot init bootstrap failed");
			return false;
		}
		
		if (!initAnchorBootstrap(appCfg.getTimeout())) {
			HawkLog.errPrintln("robot init anchor bootstrap failed");
			return false;
		}
		
		HawkUUIDGenerator.prepareUUIDGenerator(appCfg.getServerId());
		
		if (!RobotAppHelper.getInstance().init(getConfig())) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 随机一个机器人
	 * 
	 * @return
	 */
	public HawkRobotEntity randRobotEntity(String robotId) {
		Set<HawkRobotEntity> robotEntities = getRobotEntities();
		int index = HawkRand.randInt(robotEntities.size() - 1);
		for (HawkRobotEntity robotEntity : robotEntities) {
			index --;
			if (index <= 5 && !HawkOSOperator.isEmptyString(robotEntity.getPlayerId()) && !robotEntity.getPlayerId().equals(robotId)) {
				return robotEntity;
			}
		}
		return null;
	}
	
	
	@Override
	public void run() {
		int detectType = getConfig().getInt("detectType");
		// 老版检测机制：不限时注册
		if (detectType == 0) {
			onTick();
		} else { 
			// 限时注册机制
			onTimeLimitTick();
		}
	}
	
	/**
	 * 老版检测机制： 不限时注册
	 */
	private void onTick() {
		// 在线机器人数量上限
		final int count = RobotAppConfig.getInstance().getRobotOnlineCnt();
		// 添加机器人
		RobotAppHelper.getInstance().addRobot(count);

		// 在线机器人检测时间间隔
		final long detectDuration = getConfig().getInt("detectGap") * 1000;
		// 下一次检测时间
		long detectTime = HawkTime.getMillisecond() + detectDuration/2;
		// 添加机器人的时间
		long detectAddTime = 0;
		// 移除的机器人数量
		int removeCount = 0;
		// 可以接受的登录失败玩家个数，登录失败的玩家数量小于等于此值开启检测，否则不开启
		int loginFailedCount = getConfig().getInt("loginFailedCount");
		// 第一次检测
		boolean firstDetect = true;
		
		while (true) {
			try {
				// 隔多长时间检测一次在线机器人
				long now = HawkTime.getMillisecond();
				if(now >= detectTime) {
					int onlineCnt = StatisticHelper.getOnlineRobotCnt();
					int loginCnt = StatisticHelper.getLoginRobotCnt();
					if (!detectEnable && (count - onlineCnt < loginFailedCount || !firstDetect)) {
						detectEnable = getConfig().getBoolean("detect");
					}
					
					firstDetect = false;
					removeCount = RobotAppHelper.getInstance().robotDetect(detectEnable);
					HawkLog.logPrintln("robot offline count: {}, onlineCnt: {}, loginCount: {}, lowLevel offline robot count: {}", 
							removeCount, onlineCnt, loginCnt, StatisticHelper.getOfflineLowLevelRobotCnt());
					// 机器人注册满了后，走正常时间检测，否则检测时间间隔加快
					if (RobotAppHelper.getInstance().isFullRegister()) {
						detectTime = now + detectDuration;
						detectAddTime = now + detectDuration / 2;
					} else {
						detectTime = now + detectDuration / 2;
						detectAddTime = now + detectDuration / 4;
					}
					
					Set<HawkRobotEntity> robotEntities = getRobotEntities();
					for (HawkRobotEntity robotEntity : robotEntities) {
						if (!robotEntity.isOnline() && robotEntity.getLoginTime() > 0 && now - robotEntity.getLoginTime() > 120000) {
							robotEntity.closeSession();
						}
					}
				}
				
				
				// 在线机器人执行action
				super.run();
				
				// 当前在线机器人数量小于在线数量上限时，按一定的几率确定是否要添加机器人
				if(detectEnable && removeCount > 0 && now > detectAddTime) {
					int addCount = count - StatisticHelper.getLoginRobotCnt();
					// 下线的机器人数分两次补加回来，但这两次不一定都能真正加上，还得按概率来决定要不要添加
					if(detectTime - detectAddTime <= detectDuration / 4) {
						RobotAppHelper.getInstance().detectAddRobot(addCount);
						removeCount = 0;
					} else {
						RobotAppHelper.getInstance().detectAddRobot(addCount/2);
						detectAddTime += detectDuration / 4;
					}
				}
			} catch(Exception e) {
				HawkLog.logPrintln("GameRobotApp run exception");
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 限时tick，限定时间内必须注册满
	 */
	private void onTimeLimitTick() {
		// 上一次检测登录时间
		long lastLoginTickTime = 0;
		// 上一次检测协议action的时间
		long lastActionTickTime = 0;
		// 上一次检测登录失败的时间
		long lastRegisterTickTime = 0;
		
		// 注册总人数上限
		int registerCountLimit = RobotAppConfig.getInstance().getRobotRegisterCnt();
		// 限定时间内导入玩家，单位分钟
		int minuteLimit = RobotAppConfig.getInstance().getRegisterTimeLimit();
		// 平均每分钟导入多少
		int loginPerMin = registerCountLimit / minuteLimit;
		// 登录检测时间间隔
		long loginPeriod = HawkTime.MINUTE_MILLI_SECONDS;
		
		long actionTickPeriod = RobotAppConfig.getInstance().getActionTickPeriod();
		
		while (true) {
			
			long tickTime = HawkTime.getMillisecond();
			
			// 定期检测登录
			if (tickTime - lastLoginTickTime > loginPeriod) {
				lastLoginTickTime = tickTime;
				// 第一次需要赋值
				if (lastActionTickTime == 0) {
					lastActionTickTime = tickTime;
					lastRegisterTickTime = tickTime;
				}
				
				// 还未注册满的情况
				if (StatisticHelper.getRegisterSuccCnt() < registerCountLimit) {
					RobotAppHelper.getInstance().detectAddRobot(loginPerMin);
				} else {
					// 注册满后第一次会出现这种情况
					if (loginPeriod == HawkTime.MINUTE_MILLI_SECONDS) {
						loginPeriod = getConfig().getInt("detectGap") * 1000;
						loginPerMin = getConfig().getInt("removeCount");
					} else {
						detectLogin(loginPerMin);
					}
				}
			}
			
			// 定期执行action
			if (tickTime - lastActionTickTime > actionTickPeriod) {
				lastActionTickTime = tickTime;
				intervelLogout();
				super.run();
				
			}
			
			// 定期检测注册失败的玩家，重新注册
			if (tickTime - lastRegisterTickTime > HawkTime.MINUTE_MILLI_SECONDS) {
				lastRegisterTickTime = tickTime;
				int registerSuccCnt = StatisticHelper.getRegisterSuccCnt();
				if (registerCountLimit > registerSuccCnt) {
					HawkLog.logPrintln("game robot app tick, register succ count: {}, failed count: {}", registerSuccCnt, StatisticHelper.getRegisterFailedCnt());
					RobotAppHelper.getInstance().registerAgain();
				}
			}
			
			// cpu睡眠一定时间
			HawkOSOperator.sleep();
		}
	}
	
	/**
	 * 间隔登出
	 * @param logoutCnt
	 */
	private void intervelLogout() {
		int logoutCnt = RobotAppConfig.getInstance().getLogoutCount();
		if (logoutCnt == 0) {
			return;
		}
		
		Set<HawkRobotEntity> robotEntries = HawkRobotApp.getInstance().getRobotEntities();
		List<HawkRobotEntity> logoutList = new ArrayList<>(logoutCnt);
		for(HawkRobotEntity robotEntity : robotEntries) {
			GameRobotEntity entity = (GameRobotEntity) robotEntity;
			// 只踢出在线的玩家
			if (entity.getState() != RobotState.ASSEMBLE_FINISH) {
				continue;
			}
			
			logoutList.add(robotEntity);
			// 退出一定数量的机器人
			if (--logoutCnt == 0) {
				break;
			}
		}
		
		// 从现有机器人里面移除已退出的机器人
		robotEntries.removeAll(logoutList);
		
		new Thread() {
			public void run () {
				try {
					long logoutInterval = RobotAppConfig.getInstance().getLogoutInterval();
					for(HawkRobotEntity robotEntity : robotEntries) {
						GameRobotEntity entity = (GameRobotEntity) robotEntity;
						// 在线机器人下线
						entity.doLogout();
						Thread.sleep(logoutInterval);
					}
				} catch (InterruptedException e) {
					HawkException.catchException(e);
				}
			}
		}.start();
	}
	
	/**
	 * 登录检测
	 * 
	 * @param loginCount
	 */
	private void detectLogin(int loginCount) {
		// 在线总人数上限
		int onlineCountLimit = RobotAppConfig.getInstance().getRobotOnlineCnt();
		
		int freeCount = onlineCountLimit - StatisticHelper.getOnlineRobotCnt();
		// 在线人数没满时，空缺人数小于每分钟登录人数，需要踢出部分机器人
		if (freeCount < loginCount) {
			// 现有在线的机器人
			Set<HawkRobotEntity> robotEntries = HawkRobotApp.getInstance().getRobotEntities();
			int logoutCnt = loginCount - freeCount;
			List<HawkRobotEntity> logoutList = new ArrayList<>(logoutCnt);
			for(HawkRobotEntity robotEntity : robotEntries) {
				GameRobotEntity entity = (GameRobotEntity) robotEntity;
				// 只踢出在线的玩家
				if (entity.getState() != RobotState.ASSEMBLE_FINISH) {
					continue;
				}
				
				// 在线机器人下线
				entity.doLogout();
				logoutList.add(robotEntity);
				// 退出一定数量的机器人
				if (--logoutCnt == 0) {
					break;
				}
			}
			
			// 从现有机器人里面移除已退出的机器人
			robotEntries.removeAll(logoutList);
		}
		
		// 机器人登录
		RobotAppHelper.getInstance().detectAddRobot(loginCount);
	}
	
	/**
	 * 获取事件处理器
	 * 
	 * @return
	 */
	public Bootstrap getBootstrap() {
		return bootstrap;
	}
	
	public Bootstrap getAnchorBootstrap() {
		return anchorBootstrap;
	}

	/**
	 * 初始化事件处理器
	 * 
	 * @param connectTimeout
	 * @return
	 */
	private boolean initBootstrap(long connectTimeout) {
		if (bootstrap == null) {
			bootstrap = new Bootstrap();
			NioEventLoopGroup eventGroup = new NioEventLoopGroup();
			bootstrap.group(eventGroup).channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true);
			
			// 设置连接超时
			if (connectTimeout > 0) {
				bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 初始化事件处理器
	 * 
	 * @param connectTimeout
	 * @return
	 */
	private boolean initAnchorBootstrap(long connectTimeout) {
		if (anchorBootstrap == null) {
			anchorBootstrap = new Bootstrap();
			NioEventLoopGroup eventGroup = new NioEventLoopGroup();
			anchorBootstrap.group(eventGroup).channel(NioSocketChannel.class);
			anchorBootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true);
			
			// 设置连接超时
			if (connectTimeout > 0) {
				anchorBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void removeRobot(HawkRobotEntity robotEntity) {
		super.removeRobot(robotEntity);
		// 不是在线玩家，又不是正在登录中的玩家
		if (!robotEntity.isOnline() && !StatisticHelper.isLoginAccount(robotEntity.getPuid())) {
			return;
		}
		
		// 从正在登录的玩家列表中移除
		StatisticHelper.removeLogin(robotEntity.getPuid());

		if(robotEntity.isOnline()) {
			// 记录该机器人退出的时间
			StatisticHelper.robotOffline(robotEntity);
		} else if (!StatisticHelper.isRegisterSucc(robotEntity.getPuid())) {
			StatisticHelper.addRegisterFailed(robotEntity.getPuid());
		}
	}
	
	/**
	 * 投递任务
	 * @param task
	 */
	public void executeTask(Runnable task) {
		RobotAppHelper.getInstance().executeTask(task);
	}
	
	/**
	 * 添加action对象
	 */
	public void addAction(HawkRobotAction action) {
		actions.add(action);
	}
	
	/**
	 * 为机器人随机action
	 * @param robotEntity
	 */
	public void randRobotActions(GameRobotEntity robotEntity) {
		int count = 0;
		if (!GameRobotApp.getInstance().getConfig().getBoolean("auto")) {
			// 增加常规的Action
			for (HawkRobotAction action : actions) { 
				RobotAction annot = action.getClass().getAnnotation(RobotAction.class);
				if(annot.valid()) {
					robotEntity.addAction(action.getClass());
					count++;
				}
			}
		} else {
			// 随机Action的概率
			int probability = GameRobotApp.getInstance().getConfig().getInt("robot.probability");
			for (HawkRobotAction action : actions) {
				if(action instanceof AnchorChatAction){
					continue;
				}
				// 先判断是否命中
				if(!action.isRandom() || HawkRand.randPercentRate(probability)){
					robotEntity.addAction(action.getClass());
					count++;
				}
			}
		}
		
		HawkLog.logPrintln("robot {} add Action total count : {}", robotEntity.getPuid(), count);
	}
}
