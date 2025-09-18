package com.hawk.robot.action.march.type.impl;

import java.util.Random;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldInfoPush;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.march.March;
import com.hawk.robot.action.march.type.MarchType;
import com.hawk.robot.annotation.RobotMarch;
import com.hawk.robot.util.WorldUtil;

/**
 * 机器人行军 - 驻扎
 * @author golden
 *
 */
@RobotMarch(marchType = "QUARTERED")
public class Quartered implements March {

	/** 搜索半径 */
	private final int searchRadius = 50;
	
	@Override
	public void startMarch(GameRobotEntity robot) {
		Random random = new Random();
		WorldInfoPush robotWorldInfo = robot.getWorldData().getWorldInfo();
		if(robotWorldInfo == null) {
			RobotLog.worldErrPrintln("req target point failed, playerId: {}, position info: {}", robot.getPlayerId(), robotWorldInfo);
			return;
		}
		int x = robotWorldInfo.getTargetX();
		int y = robotWorldInfo.getTargetY();
		
		int targetX = x + (random.nextInt(searchRadius) * (random.nextBoolean() ? 1 : -1));
		int targetY = y + (random.nextInt(searchRadius) * (random.nextBoolean() ? 1 : -1));
		
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, targetX, targetY, null, true);
		if (builder == null) {
			return;
		}
		
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_QUARTERED_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), MarchType.QUARTERED.name());
	}

	@Override
	public int getMarchType() {
		return MarchType.QUARTERED.intVal();
	}

}
