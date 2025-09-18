package com.hawk.robot.response.item;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Item.HPSyncGiftInfoResp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = { HP.code.PUSH_GIFT_SYNC_S_VALUE, HP.code.GIFT_SYNC_S_VALUE })
public class GiftInfoResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPSyncGiftInfoResp giftInfo = protocol.parseProtocol(HPSyncGiftInfoResp.getDefaultInstance());
		robotEntity.getBasicData().refreshGiftInfo(giftInfo);
	}

}
