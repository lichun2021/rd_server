package com.hawk.robot.action.march;

import java.util.List;

import org.hawk.annotation.RobotAction;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotAppConfig;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.RobotLog;
import com.hawk.robot.util.WorldUtil;

@RobotAction(valid = true)
public class PlayerMarchAction extends HawkRobotAction {
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		reqTarget(robotEntity);
	}
	
	/**
	 * 请求目标点id
	 */
	private void reqTarget(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		// 判断是否可切世界
		if (!WorldUtil.checkSwitchIntoWorld(robot)) {
			return;
		}
		
		worldMarchCallback(robot);
		if (robot.getWorldData().getMarchCount() > RobotAppConfig.getInstance().getMarchUpLimit()) {
			RobotLog.worldErrPrintln("start march failed, player march count touch limit, playerId: {}", robot.getPlayerId());
			return;
		}
		
 		boolean simpleMarch = HawkOSOperator.isEmptyString(robot.getGuildId());
		March currentMarch = MarchFactory.getInstance().randomMarch(simpleMarch);
		if (currentMarch != null) {
			currentMarch.startMarch(robot);
		}
	}
	
	/**
	 * 行军遣返
	 * @param robot
	 */
	public static void worldMarchCallback(GameRobotEntity robot) {
		List<String> marchIdList = robot.getWorldData().getMarchIdList();
		for (String marchId : marchIdList) {
			WorldMarchPB worldMarch = WorldDataManager.getInstance().getMarch(marchId);
			if (worldMarch == null) {
				continue;
			}
			long startTime = worldMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT ?  
					worldMarch.getResStartTime() : worldMarch.getStartTime();
			// 停留类行军超过半小时遣返
			int marchAlive = GameRobotApp.getInstance().getConfig().getInt("marchAlive");
			if (WorldUtil.isStopMarch(worldMarch.getMarchType()) && HawkTime.getMillisecond() - startTime > marchAlive * 1000) {
				WorldMarchCallbackAction.marchCallBack(robot, worldMarch);
			}
		}
	}
}
