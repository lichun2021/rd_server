package com.hawk.robot.response.army;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Army.HPArmyInfoSync;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

/**
 * 同步军队信息
 * 
 * @author lating
 *
 */
@RobotResponse(code = HP.code.PLAYER_ARMY_S_VALUE)
public class PlayerArmySyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPArmyInfoSync armyInfo = protocol.parseProtocol(HPArmyInfoSync.getDefaultInstance());
		robotEntity.getCityData().refreshArmyData(robotEntity, armyInfo);
	}

}
