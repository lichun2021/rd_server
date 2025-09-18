package com.hawk.robot;

import org.hawk.log.HawkLog;
import org.hawk.net.client.HawkClientSession;
import org.hawk.net.protocol.HawkProtocol;

import io.netty.bootstrap.Bootstrap;

public class RaolRobotSession extends HawkClientSession {
	/**
	 * 机器人对象
	 */
	private RaolRobotEntity robotEntity;
	
	/**
	 * 构造机器人会话
	 * 
	 * @param gameRobotEntity
	 */
	public RaolRobotSession(RaolRobotEntity robotEntity, Bootstrap bootstrap) {
		super(bootstrap);
		this.robotEntity = robotEntity;
	}
	
	/**
	 * 获取机器人实体对象
	 * 
	 * @return
	 */
	public RaolRobotEntity getRobotEntity() {
		return robotEntity;
	}

	/**
	 * 会话开启
	 */
	@Override
	public void onSessionOpened() {
		robotEntity.setConnectOk(true);
	}

	/**
	 * 会话关闭
	 */
	@Override
	protected void onSessionClosed() {
		robotEntity.setConnectOk(false);
	}

	/**
	 * 发送协议
	 */
	@Override
	protected void onSessionProtocol(HawkProtocol protocol) {
		HawkLog.debugPrintln("robotId: {}, type: {}, size: {}", 
				robotEntity.getPlayerId(), protocol.getType(), protocol.getSize());
		
		robotEntity.onProtocol(protocol);		
	}
}
