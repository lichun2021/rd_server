package com.hawk.robot.action.march.type.impl;

import java.util.List;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.march.March;
import com.hawk.robot.action.march.type.MarchType;
import com.hawk.robot.annotation.RobotMarch;
import com.hawk.robot.util.WorldUtil;

/**
 * 机器人行军 - 攻击玩家
 * @author golden
 *
 */
@RobotMarch(marchType = "ATTACK_PLAYER")
public class AttackPlayer implements March {
	

	@Override
	public void startMarch(GameRobotEntity robot) {
		WorldPointSync resp = robot.getWorldData().getWorldPointSync();
		if(resp == null){
			WorldUtil.enterWorldMap(robot); //进入世界地图
			WorldUtil.move(robot); //移动(通过传回point list, 查找可攻击玩家)
			return;
		}
		
		//遍历所有点攻击玩家
		List<WorldPointPB> worldPointList = resp.getPointsList();
		for (WorldPointPB wp : worldPointList) {
			if (wp.getPlayerId() == null || !wp.getPointType().equals(WorldPointType.PLAYER)) {
				continue;
			}
			// 攻打玩家
			if(!attackPlayer(robot, wp)) {
				continue;
			}
			break;
		}
	}

	@Override
	public int getMarchType() {
		return MarchType.ATTACK_PLAYER.intVal();
	}

	/**
	 * 攻打玩家
	 * @param robotEntity
	 * @param wp
	 * @param massEnable
	 * @return
	 */
	public boolean attackPlayer(GameRobotEntity robot, WorldPointPB wp) {
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, wp.getPointX(), wp.getPointY(), null, true);
		if (builder == null) {
			return false;
		}
		
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_ATTACK_PLAYER_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), MarchType.ATTACK_PLAYER.name());
		return true;
	}
}
