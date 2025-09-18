package com.hawk.robot.response.march;

import com.hawk.robot.annotation.RobotResponse;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchDeletePush;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.response.RobotResponsor;

/**
 * 行军删除处理
 * @author lating
 *
 */
@RobotResponse(code = {HP.code.WORLD_MARCH_DELETE_PUSH_VALUE})
public class MarchRemoveResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		WorldMarchDeletePush marchDel = protocol.parseProtocol(WorldMarchDeletePush.getDefaultInstance());
		if(marchDel == null) {
			HawkLog.logMonitor("push world march failed, action: delete");
			return;
		}
		
		if (marchDel.getRelation() == WorldMarchRelation.SELF) {
			WorldDataManager.getInstance().addDelMarchCount();
		} else {
			HawkLog.logMonitor("push world march delete, relation: {}", marchDel.getRelation().name());
		}
		
		robotEntity.getWorldData().delWorldMarch(robotEntity, marchDel.getMarchId());
	}
	
}
