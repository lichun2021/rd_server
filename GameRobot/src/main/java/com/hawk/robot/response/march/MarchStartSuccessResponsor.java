package com.hawk.robot.response.march;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.StatisticHelper;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.response.RobotResponsor;

/**
 * 行军消息发送成功
 * 
 * @author lating
 *
 */
@RobotResponse(code = {HP.code.WORLD_ASSISTANCE_RES_S_VALUE, 
 HP.code.WORLD_ASSISTANCE_S_VALUE, 
 HP.code.WORLD_FIGHTMONSTER_S_VALUE,
 HP.code.WORLD_ATTACK_PLAYER_S_VALUE,
 HP.code.WORLD_COLLECTRESOURCE_S_VALUE,
 HP.code.GUILD_MANOR_MARCH_S_VALUE,
 HP.code.WORLD_MASS_JOIN_S_VALUE,
 HP.code.WORLD_MASS_S_VALUE,
 HP.code.WORLD_QUARTERED_S_VALUE,
 HP.code.WORLD_SPY_S_VALUE,
 HP.code.WORLD_YURI_EXPLORE_S_VALUE,
 HP.code.WARE_HOUSE_STORE_MARCH_C_VALUE,
 HP.code.WARE_HOUSE_TAKE_MARCH_C_VALUE})
public class MarchStartSuccessResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		WorldDataManager.getInstance().addSuccessMarchCount();
		StatisticHelper.incSuccessProtocolCnt(protocol.getType());
	}
	
}
