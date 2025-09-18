package com.hawk.robot.action.march.type.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchType;
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
 * 随机宝箱
 * @author zhenyu.shang
 * @since 2017年10月16日
 */
@RobotMarch(marchType = "RANDOM_BOX")
public class RandomBox implements March{

	@Override
	public void startMarch(GameRobotEntity robot) {
		WorldPointSync resp = robot.getWorldData().getWorldPointSync();
		if(resp == null || resp.getPointsList().isEmpty()){
			WorldUtil.enterWorldMap(robot); //进入世界地图
			WorldUtil.move(robot); //移动(通过传回point list, 查找可攻击玩家)
			return;
		}
		
		//遍历所有尤里点
		List<WorldPointPB> worldPointList = resp.getPointsList();
		List<WorldPointPB> boxPoint = new ArrayList<WorldPointPB>();
		for (WorldPointPB wp : worldPointList) {
			if (wp.getPointType().equals(WorldPointType.BOX)) {
				boxPoint.add(wp);
			}
		}
		if(boxPoint.isEmpty()){
			WorldUtil.enterWorldMap(robot); //进入世界地图
			WorldUtil.move(robot); //移动(通过传回point list, 查找可攻击玩家)
			return;
		}
		Collections.shuffle(boxPoint);;
		exploreYuri(robot, boxPoint.get(0));
	}

	@Override
	public int getMarchType() {
		return MarchType.RANDOM_BOX.intVal();
	}

	/**
	 * 攻打玩家
	 * @param robotEntity
	 * @param wp
	 * @param massEnable
	 * @return
	 */
	public boolean exploreYuri(GameRobotEntity robot, WorldPointPB wp) {
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, wp.getPointX(), wp.getPointY(), WorldMarchType.RANDOM_BOX, false);
		if (builder == null) {
			return false;
		}
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_RANDOM_BOX_MARCH_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), MarchType.RANDOM_BOX.name());
		return true;
	}

}
