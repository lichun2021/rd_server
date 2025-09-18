package com.hawk.robot.action.march.type.impl;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.SearchType;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldSearchReq;
import com.hawk.game.protocol.World.WorldSearchResp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.march.March;
import com.hawk.robot.action.march.type.MarchType;
import com.hawk.robot.annotation.RobotMarch;
import com.hawk.robot.util.WorldUtil;

/**
 * 机器人行军-集结迷雾要塞
 * @author golden
 *
 */
@RobotMarch(marchType = "MASS_FOGGY")
public class MassFoggy implements March {
	@Override
	public void startMarch(GameRobotEntity robot) {
		WorldSearchReq.Builder req = WorldSearchReq.newBuilder();
		req.setType(SearchType.SEARCH_FOGGY);
		int[] foggyId = {1001,1002,1003,2001,2002,2003,3001,3002,3003,
				4001,4002,4003,4004};
		int rndomIndex = HawkRand.randInt();
		req.setId(foggyId[rndomIndex % foggyId.length]);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SEARCH_C_VALUE, req));
	}

	@Override
	public int getMarchType() {
		return MarchType.MASS_FOGGY.intVal();
	}
	
	/**
	 * 进攻野怪
	 * @param robot
	 * @param resp
	 */
	public static void startMarch(GameRobotEntity robot, WorldSearchResp resp) {
		if(resp == null){
			return;
		}
		int x = resp.getTargetX();
		int y = resp.getTargetY();
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, x, y, WorldMarchType.FOGGY_FORTRESS_MASS, true);
		if (builder == null) {
			return;
		}
		builder.setMassTime(600);
		builder.setType(WorldMarchType.FOGGY_FORTRESS_MASS);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MASS_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), MarchType.MASS_FOGGY.name());
	}
}