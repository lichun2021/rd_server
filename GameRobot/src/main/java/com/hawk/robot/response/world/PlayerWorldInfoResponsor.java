package com.hawk.robot.response.world;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldInfoPush;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.WORLD_PLAYER_WORLD_INFO_PUSH_VALUE)
public class PlayerWorldInfoResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		// 玩家城点
		WorldInfoPush worldInfo = protocol.parseProtocol(WorldInfoPush.getDefaultInstance());
		robotEntity.getWorldData().setWorldInfo(worldInfo);
	}

}
