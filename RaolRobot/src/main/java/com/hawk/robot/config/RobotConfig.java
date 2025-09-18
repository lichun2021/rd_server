package com.hawk.robot.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 *
 */
@HawkConfigManager.XmlResource(file = "cfg/robot.xml")
public class RobotConfig extends HawkConfigBase {
	@Id
	protected final String openId;
	// 平台
	protected final String platform;
	// 渠道
	protected final String channel;
	// 设备号
	protected final String deviceId;
	// ip
	protected final String ip;
	// 端口
	protected final int port;
	
	public RobotConfig() {
		openId = "";
		platform = "";
		channel = "";
		deviceId = "";
		ip = "";
		port = 0;
	}

	public String getOpenId() {
		return openId;
	}

	public String getPlatform() {
		return platform;
	}

	public String getChannel() {
		return channel;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}
}
