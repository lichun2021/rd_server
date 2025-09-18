package com.hawk.robot.response.player;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.Consume.HPConsumeInfo;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.PLAYER_CONSUME_S_VALUE)
public class PlayerConsumeSyncponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPConsumeInfo consumeInfo = protocol.parseProtocol(HPConsumeInfo.getDefaultInstance());
		if (consumeInfo != null) {
			robotEntity.getBasicData().consumeItems(consumeInfo.getConsumeItemList());
			robotEntity.getBasicData().updatePlayerInfo(consumeInfo.getAttrInfo());
		}
	}

}
