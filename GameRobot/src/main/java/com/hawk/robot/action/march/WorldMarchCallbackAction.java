package com.hawk.robot.action.march;

import java.util.List;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.RobotLog;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchServerCallBackReq;
import com.hawk.game.protocol.World.WorldMarchStatus;

/**
 * 
 * 行军召回
 * 
 * @author lating
 *
 */
@RobotAction(valid = false)
public class WorldMarchCallbackAction extends HawkRobotAction {

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		List<String> marchIdList = robot.getWorldData().getMarchIdList();
		if(marchIdList == null || marchIdList.size() <= 0) {
			return;
		}
		
		for(String marchId : marchIdList) {
			WorldMarchPB worldMarch = WorldDataManager.getInstance().getMarch(marchId);
			if (worldMarch == null) {
				RobotLog.worldErrPrintln("fetch wolrd march failed, playerId: {}, marchId: {}", robot.getPlayerId(), marchId);
				continue;
			}
			
			if(worldMarch.getRelation() != WorldMarchRelation.SELF) {
				continue;
			}
			
			// 非驻扎行军不予处理
			if(worldMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED) {
				continue;
			}
			
			if(HawkTime.getMillisecond() - worldMarch.getStartTime() <= 600000) {
				return;
			}
			
			marchCallBack(robot, worldMarch);
		}
	}
	
	/**
	 * 行军召回
	 * @param robot
	 * @param worldMarch
	 */
	public static void marchCallBack(GameRobotEntity robot, WorldMarchPB worldMarch) {
		WorldMarchServerCallBackReq.Builder builder = WorldMarchServerCallBackReq.newBuilder();
		builder.setMarchId(worldMarch.getMarchId());
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SERVER_CALLBACK_C_VALUE, builder));
		RobotLog.worldPrintln("worldMarch callback, playerId: {}, marchId: {}, marchStatus: {}", robot.getPlayerId(), worldMarch.getMarchId(), worldMarch.getMarchStatus());
	}
}
