package com.hawk.robot.action.march.type.impl;

import com.hawk.robot.annotation.RobotMarch;
import com.hawk.robot.util.WorldUtil;

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

/**
 * 机器人行军 - 侦查
 * @author golden
 *
 */
@RobotMarch(marchType = "INVESTIGATION")
public class Investigation implements March {

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
			if(!investPlayer(robot, wp)) {
				continue;
			}
			break;
		}
	}

	@Override
	public int getMarchType() {
		return MarchType.INVESTIGATION.intVal();
	}

	/**
	 * 侦查玩家
	 * @param robotEntity
	 * @param wp
	 * @param massEnable
	 * @return
	 */
	public boolean investPlayer(GameRobotEntity robot, WorldPointPB wp) {
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, wp.getPointX(), wp.getPointY(), null, false);
		if (builder == null) {
			return false;
		}
		
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SPY_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), MarchType.INVESTIGATION.name());
		return true;
	}
}
