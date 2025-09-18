package com.hawk.robot.response.world;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.WORLD_POINT_SYNC_VALUE)
public class WorldPointSyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		// 玩家视野移动
		WorldPointSync resp = protocol.parseProtocol(WorldPointSync.getDefaultInstance());
		robotEntity.getWorldData().setWorldPointSync(resp);
	}
}
