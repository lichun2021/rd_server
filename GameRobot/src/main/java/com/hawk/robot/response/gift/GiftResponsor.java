package com.hawk.robot.response.gift;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Item.HPSyncGiftInfoResp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code={HP.code.GIFT_SYNC_S_VALUE})
public class GiftResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		switch(protocol.getType()) {
		case HP.code.GIFT_SYNC_S_VALUE:
			doGiftSync(robotEntity, protocol);
			break;
		}
	}

	
	private void doGiftSync(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPSyncGiftInfoResp resp = protocol.parseProtocol(HPSyncGiftInfoResp.getDefaultInstance());
		robotEntity.getData().getBasicData().setGiftBuilder(resp.toBuilder());
	}

}
