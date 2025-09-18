package com.hawk.robot.action.march;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.PlayerLeaveWorld;
import com.hawk.robot.GameRobotEntity;

/**
 * 
 * 切成内外
 * 
 * @author lating
 *
 */
@RobotAction(valid = true)
public class ChangeInWorldAction extends HawkRobotAction {

	@Override
	public void doAction(HawkRobotEntity entity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) entity;
		if (gameRobotEntity.isInWorld() && HawkRand.randInt(10000) > 5000) {
			PlayerLeaveWorld.Builder enterWorld = PlayerLeaveWorld.newBuilder();
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_LEAVE_WORLD_VALUE, enterWorld));
			gameRobotEntity.setInWorld(false);
		}
	}
}
