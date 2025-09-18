package com.hawk.robot;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.config.RobotConfig;

public class RaolRobotEntity extends HawkRobotEntity {
	/**
	 * 玩家平台id
	 */
	private String puid;
	/**
	 * 会话
	 */
	protected RaolRobotSession session;
	/**
	 * 连接正常
	 */
	protected boolean connectOk;
	
	/**
	 * 构造
	 */
	public RaolRobotEntity() {
		session = new RaolRobotSession(this, RaolRobotApp.getInstance().getBootstrap());
	}
	
	/**
	 * 开启服务器连接
	 */
	public boolean init(RobotConfig robotCfg) {
		// 初始化连接
		puid = robotCfg.getOpenId() + "#" + robotCfg.getPlatform();
		
		// 初始化连接
		int connectTimeout = RaolRobotCfg.getInstance().getConnectTimeout();
		int heartbeatPeriod = RaolRobotCfg.getInstance().getHeartbeatPeriod();
		if (!session.init(robotCfg.getIp(), robotCfg.getPort(), connectTimeout, heartbeatPeriod)) {
			HawkLog.errPrintln("robot connect server failed, ip: {}, port: {}", robotCfg.getIp(), robotCfg.getPort());
			return false;
		}
		return true;
	}

	public boolean isConnectOk() {
		return connectOk;
	}

	public void setConnectOk(boolean connectOk) {
		this.connectOk = connectOk;
	}

	@Override
	public String getPuid() {
		return puid;
	}

	@Override
	public String getPlayerId() {
		return null;
	}

	@Override
	public boolean isOnline() {
		if (session != null) {
			return session.isActive() && this.connectOk;
		}
		return false;
	}

	@Override
	public boolean closeSession() {
		// 关闭会话
		if (session != null) {
			session.close();
		}
		return true;
	}

	@Override
	public boolean sendProtocol(HawkProtocol protocol) {
		if (session != null && session.isActive()) {
			session.sendProtocol(protocol);
			return true;
		}
		return false;
	}

	public void onProtocol(HawkProtocol protocol) {
		
	}
}
