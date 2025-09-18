package com.hawk.robot.response.army;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.StatisticHelper;
import com.hawk.robot.response.RobotResponsor;

/**
 * 收兵响应
 * 
 * @author lating
 *
 */
@RobotResponse(code = HP.code.COLLECT_SOLDIER_S_VALUE)
public class CollectSoldierResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		StatisticHelper.incSuccessProtocolCnt(protocol.getType());
	}

}
