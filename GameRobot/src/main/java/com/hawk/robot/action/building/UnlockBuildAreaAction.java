package com.hawk.robot.action.building;

import java.util.List;
import java.util.Optional;
import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.config.BuildAreaCfg;
import com.hawk.robot.util.ClientUtil;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Building.BuildingAreaUnlockReq;

/**
 * 
 * 解锁地块
 * 
 * @author lating
 *
 */
@RobotAction(valid = true)
public class UnlockBuildAreaAction extends HawkRobotAction {
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		unlockArea(gameRobotEntity);
	}
	
	/**
	 * 解锁地块
	 * 
	 * @param robot
	 */
	public static synchronized void unlockArea(GameRobotEntity robot) {
		if (!ClientUtil.isExecuteAllowed(robot, UnlockBuildAreaAction.class.getSimpleName(), 60000)) {
			return;
		}
		
		List<Integer> areaList = BuildAreaCfg.getUnlockedArea(robot.getMilitaryLevel());
		if (areaList == null) {
			return;
		}
		
		List<Integer> unlockedAreas = robot.getCityData().getUnlockedAreas();
		Optional<Integer> op = areaList.stream().filter(e -> !unlockedAreas.contains(e)).findAny();
		if(!op.isPresent()) {
			return;
		}
		
		int areaId = op.get();
		BuildingAreaUnlockReq.Builder builder = BuildingAreaUnlockReq.newBuilder();
		builder.setAreaId(areaId);
		builder.setImmediate(HawkRand.randPercentRate(50));
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.BUILDING_AREA_UNLOCK_C_VALUE, builder));
		robot.getCityData().getLastExecuteTime().put(UnlockBuildAreaAction.class.getSimpleName(), HawkTime.getMillisecond());
		RobotLog.cityDebugPrintln("unlock area, playerId: {}, areaId: {}", robot.getPlayerId(), areaId);
	}
	
}
