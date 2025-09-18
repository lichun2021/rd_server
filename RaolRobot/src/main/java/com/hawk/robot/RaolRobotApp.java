package com.hawk.robot;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigStorage;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocolManager;
import org.hawk.os.HawkException;
import org.hawk.robot.HawkRobotApp;

import com.hawk.robot.action.RobotAlarmAction;
import com.hawk.robot.action.RobotArmyAction;
import com.hawk.robot.action.RobotCommonAction;
import com.hawk.robot.action.RobotLoginAction;
import com.hawk.robot.action.RobotMonsterAction;
import com.hawk.robot.action.RobotResourceAction;
import com.hawk.robot.action.RobotWelfareAction;
import com.hawk.robot.config.RobotConfig;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class RaolRobotApp extends HawkRobotApp {
	/**
	 * io事件处理器
	 */
	private Bootstrap bootstrap;
	
	/**
	 * 全局静态对象
	 */
	private static RaolRobotApp instance = null;

	/**
	 * 获取全局静态对象
	 * 
	 * @return
	 */
	public static RaolRobotApp getInstance() {
		return instance;
	}

	/**
	 * 构造函数
	 */
	public RaolRobotApp() {
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
		RaolRobotCfg appCfg = null;
		try {
			HawkConfigStorage cfgStorage = new HawkConfigStorage(RaolRobotCfg.class);
			appCfg = (RaolRobotCfg) cfgStorage.getConfigByIndex(0);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}

		if (!super.init(xmlCfg)) {
			HawkLog.errPrintln("robot init config.xml failed");
			return false;
		}

		if(!HawkConfigManager.getInstance().init(appCfg.getConfigPackages())) {
			return false;
		}
		
		HawkProtocolManager.getInstance().setProtocolIdentify(0x00EFBBBF);
		
		if (!initBootstrap(appCfg.getConnectTimeout())) {
			HawkLog.errPrintln("robot init config.xml failed");
			return false;
		}
		
		initRobotActions();
		return true;
	}
	
	/**
	 * 获取事件处理器
	 * 
	 * @return
	 */
	public Bootstrap getBootstrap() {
		return bootstrap;
	}
	
	/**
	 * 初始化事件处理器
	 * 
	 * @param connectTimeout
	 * @return
	 */
	private boolean initBootstrap(long timeout) {
		if (bootstrap == null) {
			bootstrap = new Bootstrap();
			NioEventLoopGroup eventGroup = new NioEventLoopGroup();
			bootstrap.group(eventGroup).channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true);
			
			// 设置连接超时
			if (timeout > 0) {
				bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeout);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 添加机器人的可用行为
	 */
	private void initRobotActions() {
		// 添加机器人可用行为
		addAction(new RobotLoginAction());
		addAction(new RobotWelfareAction());
		addAction(new RobotAlarmAction());
		addAction(new RobotArmyAction());
		addAction(new RobotMonsterAction());
		addAction(new RobotResourceAction());
		addAction(new RobotCommonAction());
	}
	
	/**
	 * 应用程序开始运行
	 */
	protected void onAppRunning() {
		// 开启机器人
		ConfigIterator<RobotConfig> it = HawkConfigManager.getInstance().getConfigIterator(RobotConfig.class);
		while (it.hasNext()) {
			RobotConfig robotCfg = it.next();
			initRobot(robotCfg);
		}
	}

	/**
	 * 初始化机器人
	 * 
	 * @param robotCfg
	 */
	private void initRobot(RobotConfig robotCfg) {
		RaolRobotEntity robotEntity = new RaolRobotEntity();
		if (robotEntity.init(robotCfg)) {
			robotEntity.addAction(RobotLoginAction.class);
			robotEntity.addAction(RobotWelfareAction.class);
			robotEntity.addAction(RobotAlarmAction.class);
			robotEntity.addAction(RobotArmyAction.class);
			robotEntity.addAction(RobotMonsterAction.class);
			robotEntity.addAction(RobotResourceAction.class);
			robotEntity.addAction(RobotCommonAction.class);
			
			RaolRobotApp.getInstance().addRobot(robotEntity);
		}
	}
}
