package com.hawk.robot.action.march.type.impl;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.SearchType;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldSearchReq;
import com.hawk.game.protocol.World.WorldSearchResp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.march.March;
import com.hawk.robot.action.march.type.MarchType;
import com.hawk.robot.annotation.RobotMarch;
import com.hawk.robot.util.WorldUtil;

/**
 * 据点行军
 * @author golden
 *
 */
@RobotMarch(marchType = "STRONGPOINT")
public class Strongpoint implements March {

	@Override
	public void startMarch(GameRobotEntity robot) {
		WorldSearchReq.Builder req = WorldSearchReq.newBuilder();
		req.setType(SearchType.SEARCH_STRONGPOINT);
		req.setId(HawkRand.randInt(30));
		req.setIndex(HawkRand.randInt(20));
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SEARCH_C_VALUE, req));
	}

	@Override
	public int getMarchType() {
		return MarchType.STRONGPOINT.intVal();
	}
	
	public static void startMarch(GameRobotEntity robot, WorldSearchResp resp) {
		if(resp == null){
			return;
		}
		int x = resp.getTargetX();
		int y = resp.getTargetY();
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, x, y, null, true);
		if (builder == null) {
			return;
		}
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.STRONGPOINT_MARCH_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), MarchType.STRONGPOINT.name());
	}
}