package com.hawk.robot.response.hero;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.PBHeroListPush;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.data.GameRobotData;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.PUSH_ALL_HERO_VALUE)
public class HeroListResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		GameRobotData robotData = robotEntity.getData();
		PBHeroListPush heroList = protocol.parseProtocol(PBHeroListPush.getDefaultInstance());
		heroList.getHerosList().forEach(info -> robotData.getBasicData().addHeroInfo(info));
	}

}
