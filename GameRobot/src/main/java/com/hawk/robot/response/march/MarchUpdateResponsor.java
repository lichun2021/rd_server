package com.hawk.robot.response.march;

import com.hawk.robot.annotation.RobotResponse;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

/**
 * 添加行军、行军信息变更处理
 * 
 * @author lating
 *
 */
@RobotResponse(code = {HP.code.WORLD_MARCH_UPDATE_PUSH_VALUE, HP.code.WORLD_MARCH_ADD_PUSH_VALUE})
public class MarchUpdateResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		WorldMarchPB worldMarch = protocol.parseProtocol(WorldMarchPB.getDefaultInstance());
		if(worldMarch == null) {
			HawkLog.logMonitor("push world march failed, action: add");
			return;
		}
		
		robotEntity.getWorldData().refreshWorldMarch(robotEntity, worldMarch);
	}

}
