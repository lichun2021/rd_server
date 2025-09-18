package com.hawk.robot.response.tech;

import java.util.List;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Technology.HPTechnologySync;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.PLAYER_TECHNOLOGY_S_VALUE)
public class PlayerTechResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPTechnologySync techInfo = protocol.parseProtocol(HPTechnologySync.getDefaultInstance());
		List<Integer> techList = techInfo.getTechIdList();
		robotEntity.getCityData().addTechIds(techList);
		robotEntity.getCityData().unlockTechByTech(techList);
	}
}


