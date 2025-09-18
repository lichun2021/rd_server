package com.hawk.robot.response.building;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Building.CityDefPB;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.action.building.BuildingOutFireAction;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.CITY_DEF_PUSH_VALUE)
public class CityDefResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		CityDefPB cityDef = protocol.parseProtocol(CityDefPB.getDefaultInstance());
		robotEntity.getCityData().updateCityDef(cityDef);
		GameRobotApp.getInstance().executeTask(new Runnable() {
			@Override
			public void run() {
				BuildingOutFireAction.outFire(robotEntity);
			}
		});
	}

}
