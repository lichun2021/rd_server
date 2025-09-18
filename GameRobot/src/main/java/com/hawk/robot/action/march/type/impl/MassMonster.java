package com.hawk.robot.action.march.type.impl;

import java.util.List;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Const.BuildingType;
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
 * 机器人行军 - 集结攻击野怪
 * @author golden
 *
 */
@RobotMarch(marchType = "MASS_MONSTER")
public class MassMonster implements March {
	
	@Override
	public void startMarch(GameRobotEntity robot) {
		WorldPointSync resp = robot.getWorldData().getWorldPointSync();
		if(resp == null){
			WorldUtil.enterWorldMap(robot); //进入世界地图
			WorldUtil.move(robot); //移动(通过传回point list, 查找可攻击玩家)
			return;
		}
		List<WorldPointPB> worldPointList = resp.getPointsList();
		boolean massEnable = robot.getBuildingMaxLevel(BuildingType.SATELLITE_COMMUNICATIONS_VALUE) > 0;
		if(!massEnable){
			RobotLog.worldErrPrintln("start march failed, mass monster march need satellite_communications_building, playerId: {}", robot.getPlayerId());
			return;
		}
		
		for (WorldPointPB wp : worldPointList) {
			if (wp.getPlayerId() == null || !wp.getPointType().equals(WorldPointType.MONSTER)) {
				continue;
			}
			int monsterId = wp.getMonsterId();
			
			// 精英野怪id以6开始 测试方便，这里不读表了。
			if (monsterId/100000 != 6) {
				continue;
			}
			// 攻打野怪
			if(!massAttackMonster(robot, wp, massEnable)) {
				continue;
			} else {
				return;
			}
		}
		// 没有找到点
		WorldUtil.reqMonsterPoint(robot);
	}

	@Override
	public int getMarchType() {
		return MarchType.MASS_MONSTER.intVal();
	}
	
	/**
	 * 攻打玩家
	 * @param robotEntity
	 * @param wp
	 * @param massEnable
	 * @return
	 */
	public boolean massAttackMonster(GameRobotEntity robot, WorldPointPB wp, boolean massEnable) {
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, wp.getPointX(), wp.getPointY(), null, true);
		if (builder == null) {
			return false;
		}
		
		builder.setMassTime(600);
		builder.setType(WorldMarchType.MONSTER_MASS);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MASS_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), MarchType.MASS_MONSTER.name());
		return true;
	}

}