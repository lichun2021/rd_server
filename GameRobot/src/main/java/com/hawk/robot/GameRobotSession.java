package com.hawk.robot;

import org.hawk.log.HawkLog;
import org.hawk.net.client.HawkClientSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.robot.HawkRobotAction;

import com.hawk.robot.GameRobotEntity.RobotState;
import com.hawk.robot.response.RobotResponseManager;

import io.netty.bootstrap.Bootstrap;

public class GameRobotSession extends HawkClientSession {
	/**
	 * 机器人对象
	 */
	private GameRobotEntity gameRobotEntity;
	
	/**
	 * 构造机器人会话
	 * 
	 * @param gameRobotEntity
	 */
	public GameRobotSession(GameRobotEntity gameRobotEntity, Bootstrap bootstrap) {
		super(bootstrap);
		this.gameRobotEntity = gameRobotEntity;
	}
	
	/**
	 * 获取机器人实体对象
	 * 
	 * @return
	 */
	public GameRobotEntity getGameRobotEntity() {
		return gameRobotEntity;
	}

	/**
	 * 会话开启
	 */
	@Override
	public void onSessionOpened() {
		gameRobotEntity.setState(RobotState.CONNECTED);
		RobotAppHelper.getInstance().userLogin(gameRobotEntity);
		HawkLog.logPrintln("robot session open, puid: {}", gameRobotEntity.getPuid());
	}

	/**
	 * 会话关闭
	 */
	@Override
	protected void onSessionClosed() {
		gameRobotEntity.doLogout();
	}

	/**
	 * 发送协议
	 */
	@Override
	protected void onSessionProtocol(HawkProtocol protocol) {
		HawkLog.debugPrintln("robotId: {}, type: {}, size: {}", gameRobotEntity.getPlayerId(), protocol.getType(), protocol.getSize());
		try {
			RobotResponseManager.getInstance().response(gameRobotEntity, protocol);
		} catch(Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 发送心跳
	 */
	protected void onSessionHeartbeat() {
		HawkRobotAction action = RobotAppHelper.getInstance().getHeartBeatAction();
		if(action != null) {
			action.doAction(gameRobotEntity);
		}
	}
}
