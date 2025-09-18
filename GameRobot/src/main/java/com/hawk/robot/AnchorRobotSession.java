package com.hawk.robot;

import org.hawk.log.HawkLog;
import org.hawk.net.client.HawkClientSession;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.robot.response.RobotResponseManager;

import io.netty.bootstrap.Bootstrap;

/**
 *
 * @author zhenyu.shang
 * @since 2018年4月4日
 */
public class AnchorRobotSession extends HawkClientSession {
	/**
	 * 机器人对象
	 */
	private GameRobotEntity gameRobotEntity;
	
	/**
	 * 构造机器人会话
	 * 
	 * @param gameRobotEntity
	 */
	public AnchorRobotSession(GameRobotEntity gameRobotEntity, Bootstrap bootstrap) {
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
		HawkLog.logPrintln("robot session open, puid: {}", gameRobotEntity.getPuid());
	}

	/**
	 * 会话关闭
	 */
	@Override
	protected void onSessionClosed() {
		gameRobotEntity.getAnchorData().clearOffline();
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
}
