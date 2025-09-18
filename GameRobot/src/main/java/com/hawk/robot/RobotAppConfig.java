package com.hawk.robot;

import org.hawk.app.HawkAppCfg;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

@HawkConfigManager.KVResource(files = { "cfg/app.cfg" })
public class RobotAppConfig extends HawkAppCfg {
	/**
	 *  action所在package包
	 */
	private final String actionClassPath;
	/**
	 *  机器人puid统一前缀
	 */
	private final String robotPuidPrefix;
	/**
	 * 服务器ip和端口
	 */
	private final String server;
	/**
	 * 服务器ID
	 */
	private final String serverId;
	/**
	 * 超时时间
	 */
	private final long timeout;
	/**
	 * 发送心跳的间隔
	 */
	private final long heartbeat;
	/**
	 * 机器人测试相关配置文件路径
	 */
	private final String configPath;
	/**
	 * 机器人起始id
	 */
	private final int robotStartId;
	/**
	 * 机器人在线数量
	 */
	private final int robotOnlineCnt;
	/**
	 * 机器人注册总数
	 */
	private final int robotRegisterCnt;
	
	/**
	 * 机器人在线人数与在线人数上限的差值
	 */
	private final int robotOnlineCntGap;
	
	/**
	 * 联盟总数上限
	 */
	private final int guildNumLimit;
	
	/**
	 * 城堡升到多少级之后不让再升了
	 */
	private final int cityLevelUpLimit;
	/**
	 * 每个人行军数量上限
	 */
	private final int marchUpLimit;
	
	/**
	 * 限定时间内导入玩家，单位分钟
	 */
	private final int registerTimeLimit;
	
	/**
	 * 登出时间间隔
	 */
	private final long logoutInterval;
	/**
	 * 登出人数
	 */
	private final int logoutCount;
	/**
	 * 发送协议时间间隔
	 */
	private final long actionTickPeriod;
	
	/**
	 * 排队登录时发起登录的重复次数
	 */
	private final int waitReloginTimes;
	
	/**
	 * 军衔等级边界
	 */
	private final int militaryLevelBoundary;
	
	/**
	 * 统计日志开启开关
	 */
	private final boolean statisticEnable;
	
	
	///////////////////////////////////////////////
	
	/**
	 * 服务器ip
	 */
	private String serverIp;
	/**
	 * 服务器端口
	 */
	private int port;
	
	/**
	 * 全局静态对象
	 */
	protected static RobotAppConfig instance = null;

	/**
	 * 获取全局静态对象
	 *
	 * @return
	 */
	public static RobotAppConfig getInstance() {
		return instance;
	}

	public RobotAppConfig() {
		instance = this;
		actionClassPath = "";
		robotPuidPrefix = "robot_puid_";
		server = "";
		timeout= 0;
		heartbeat = 0;
		serverId = "";
		configPath = "";
		robotStartId = 0;
		robotOnlineCnt = 10;
		robotRegisterCnt = 10;
		guildNumLimit = 300;
		registerTimeLimit = 60;
		logoutInterval = 0;
		logoutCount = 0;
		actionTickPeriod = 5;
		waitReloginTimes = 10;
		militaryLevelBoundary = 0;  // 对应少校军衔等级
		cityLevelUpLimit = 0;
		marchUpLimit = 1;
		statisticEnable = false;
		robotOnlineCntGap = 10;
	}

	public String getActionClassPath() {
		return actionClassPath;
	}

	public String getRobotPuidPrefix() {
		return robotPuidPrefix;
	}

	public String getServerIp() {
		return serverIp;
	}

	public int getPort() {
		return port;
	}
	
	public String getServerId() {
		return serverId;
	}

	public long getTimeout() {
		return timeout;
	}

	public long getHeartbeat() {
		return heartbeat;
	}
	
	public String getConfigPath() {
		return configPath;
	}
	
	public int getRobotStartId() {
		return robotStartId;
	}

	public int getRobotOnlineCnt() {
		return robotOnlineCnt;
	}
	
	public int getRegisterTimeLimit() {
		return registerTimeLimit;
	}

	public int getRobotRegisterCnt() {
		return robotRegisterCnt;
	}

	public int getGuildNumLimit() {
		return guildNumLimit;
	}

	public boolean assemble() {
		if (HawkOSOperator.isEmptyString(server) 
				|| HawkOSOperator.isEmptyString(serverId)
				|| HawkOSOperator.isEmptyString(actionClassPath)
				|| HawkOSOperator.isEmptyString(configPath)) {
			throw new RuntimeException("config param empty error!!!");
		}
		
		String[] serverInfo = server.split(":");
		serverIp = serverInfo[0];
		port = Integer.parseInt(serverInfo[1]);
		
		return true;
	}

	public int getLogoutCount() {
		return logoutCount;
	}

	public long getLogoutInterval() {
		return logoutInterval;
	}

	public long getActionTickPeriod() {
		return actionTickPeriod;
	}

	public int getWaitReloginTimes() {
		return waitReloginTimes;
	}

	public int getMilitaryLevelBoundary() {
		return militaryLevelBoundary;
	}

	public int getCityLevelUpLimit() {
		return cityLevelUpLimit;
	}

	public int getMarchUpLimit() {
		return marchUpLimit;
	}

	public boolean isStatisticEnable() {
		return statisticEnable;
	}

	public int getRobotOnlineCntGap() {
		return robotOnlineCntGap;
	}
	
}
