package com.hawk.robot.action.march.type.impl;

import java.util.List;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.march.March;
import com.hawk.robot.action.march.type.MarchType;
import com.hawk.robot.annotation.RobotMarch;
import com.hawk.robot.util.WorldUtil;

/**
 * 机器人行军 - 集结攻击玩家
 * @author golden
 *
 */
@RobotMarch(marchType = "MASS")
public class Mass implements March {
	
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
			RobotLog.worldErrPrintln("start march failed, mass march need satellite_communications_building, playerId: {}", robot.getPlayerId());
			return;
		}
		//调整视野
		boolean changeReview = true;
		String guildId = robot.getGuildId();
		for (WorldPointPB wp : worldPointList) {
			if (wp.getPlayerId() == null || !wp.getPointType().equals(WorldPointType.PLAYER)) {
				continue;
			}
			// 攻打玩家
			if(!massAttackPlayer(robot, guildId, wp, massEnable)) {
				continue;
			}
			changeReview = false;
			break;
		}
		if(changeReview){
			WorldUtil.enterWorldMap(robot); //进入世界地图
			WorldUtil.move(robot); //移动(通过传回point list, 查找可攻击玩家)
		}
	}

	@Override
	public int getMarchType() {
		return MarchType.MASS.intVal();
	}
	
	/**
	 * 攻打玩家
	 * @param robotEntity
	 * @param wp
	 * @param massEnable
	 * @return
	 */
	public boolean massAttackPlayer(GameRobotEntity robot, String guildId, WorldPointPB wp, boolean massEnable) {
		HawkRobotEntity defRobotEntity = GameRobotApp.getInstance().getRobotEntity(wp.getPlayerId());
		if(defRobotEntity == null) {
			RobotLog.worldErrPrintln("start march failed, mass march to player null, playerId: {}, defPlayerId: {}", robot.getPlayerId(), wp.getPlayerId());
			return false;
		}
		
		GameRobotEntity defRobot = (GameRobotEntity) defRobotEntity;
		if(guildId.equals(defRobot.getGuildId())){
			RobotLog.worldErrPrintln("start march failed, mass march to player in same guild, playerId: {}, defPlayerId", robot.getPlayerId(), defRobot.getPlayerId());
			return false;
		} else {
			WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, wp.getPointX(), wp.getPointY(), null, true);
			if (builder == null) {
				return false;
			}
			builder.setMassTime(600);
			builder.setType(WorldMarchType.MASS);
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MASS_C_VALUE, builder));
			RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), MarchType.MASS.name());
			return true;
		}
	}

}
