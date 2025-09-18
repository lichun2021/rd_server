package com.hawk.robot.response.hero;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.data.GameRobotData;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.UPDATE_HERO_INFO_VALUE)
public class HeroUpdateResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		GameRobotData robotData = robotEntity.getData();
		PBHeroInfo info = protocol.parseProtocol(PBHeroInfo.getDefaultInstance());
		robotData.getBasicData().addHeroInfo(info);
	}

}
