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
 * 机器人行军-攻击新版野怪
 * @author golden
 *
 */
@RobotMarch(marchType = "ATTACK_NEW_MONSTER")
public class AttackNewMonster implements March {
	@Override
	public void startMarch(GameRobotEntity robot) {
		WorldSearchReq.Builder req = WorldSearchReq.newBuilder();
		req.setType(SearchType.SEARCH_NEW_MONSTER);
		req.setLevel(HawkRand.randInt(9));
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SEARCH_C_VALUE, req));
	}

	@Override
	public int getMarchType() {
		return MarchType.ATTACK_NEW_MONSTER.intVal();
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
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, x, y, null, true);
		if (builder == null) {
			return;
		}
		builder.setAttackModel(HawkRand.randInt() % 2);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_NEW_MONSTER_MARCH_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), MarchType.ATTACK_NEW_MONSTER.name());
	}
}
