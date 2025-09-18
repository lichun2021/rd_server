package com.hawk.robot.response;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.robot.GameRobotEntity;

public abstract class RobotResponsor {
	
	public abstract void response(GameRobotEntity robotEntity, HawkProtocol protocol);
	
}
