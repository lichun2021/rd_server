package com.hawk.robot.response.building;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMoveCityReq;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.MOVE_CITY_NOTIFY_PUSH_VALUE)
public class CityForceMoveResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		// TODO 强制迁城
		WorldMoveCityReq.Builder builder = WorldMoveCityReq.newBuilder();
		builder.setType(1);
		builder.setForce(true);
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MOVE_CITY_C_VALUE, builder));
		RobotLog.worldPrintln("city forced move action, playerId: {}", robotEntity.getPlayerId());
	}

}
