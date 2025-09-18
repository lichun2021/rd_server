package com.hawk.robot.response.building;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Building.UnlockedAreaPB;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.UNLOCKED_AREA_PUSH_VALUE)
public class UnlockedAreaResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		UnlockedAreaPB unlockPB = protocol.parseProtocol(UnlockedAreaPB.getDefaultInstance());
		robotEntity.getCityData().setUnlockedAreas(unlockPB.getUnlockedAreaList());
	}

}
