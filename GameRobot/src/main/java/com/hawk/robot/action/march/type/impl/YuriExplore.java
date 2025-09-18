package com.hawk.robot.action.march.type.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;

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
 * 尤里探索行军
 * @author zhenyu.shang
 * @since 2017年10月16日
 */
@RobotMarch(marchType = "YURI_EXPLORE")
public class YuriExplore implements March{
	
	
	private int[] exploreTime = new int[]{900, 3600, 14400, 28800};

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
		List<WorldPointPB> yuriPoint = new ArrayList<WorldPointPB>();
		for (WorldPointPB wp : worldPointList) {
			if (wp.getPointType().equals(WorldPointType.YURI_FACTORY)) {
				yuriPoint.add(wp);
			}
		}
		if(yuriPoint.isEmpty()){
			WorldUtil.enterWorldMap(robot); //进入世界地图
			WorldUtil.move(robot); //移动(通过传回point list, 查找可攻击玩家)
			return;
		}
		Collections.shuffle(yuriPoint);;
		exploreYuri(robot, yuriPoint.get(0));
	}

	@Override
	public int getMarchType() {
		return MarchType.YURI_EXPLORE.intVal();
	}

	/**
	 * 探索尤里矿
	 * @param robotEntity
	 * @param wp
	 * @param massEnable
	 * @return
	 */
	public boolean exploreYuri(GameRobotEntity robot, WorldPointPB wp) {
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, wp.getPointX(), wp.getPointY(), WorldMarchType.YURI_EXPLORE, true);
		if (builder == null) {
			return false;
		}
		int etime = exploreTime[HawkRand.randInt(0, 3)];
		builder.setMassTime(etime);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_YURI_EXPLORE_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), MarchType.YURI_EXPLORE.name());
		return true;
	}
}
