package com.hawk.robot;

import org.hawk.app.HawkAppCfg;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(files = { "cfg/app.cfg" })
public class RaolRobotCfg extends HawkAppCfg {
	/**
	 * 网络连接超时
	 */
	private final int connectTimeout;
	/**
	 * 机器人心跳
	 */
	private final int heartbeatPeriod;
	
	/**
	 * 全局静态对象
	 */
	protected static RaolRobotCfg instance = null;

	/**
	 * 获取全局静态对象
	 *
	 * @return
	 */
	public static RaolRobotCfg getInstance() {
		return instance;
	}

	public RaolRobotCfg() {
		connectTimeout = 3000;
		heartbeatPeriod = 10000;
		instance = this;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public int getHeartbeatPeriod() {
		return heartbeatPeriod;
	}
}
