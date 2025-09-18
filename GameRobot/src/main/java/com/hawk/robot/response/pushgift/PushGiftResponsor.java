package com.hawk.robot.response.pushgift;

import java.util.Iterator;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PushGift.PushGiftMsg;
import com.hawk.game.protocol.PushGift.PushGiftOper;
import com.hawk.game.protocol.PushGift.PushGiftSynInfo;
import com.hawk.game.protocol.PushGift.PushGiftUpdate;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = {
		HP.code.PUSH_GIFT_SYN_INFO_VALUE,
		HP.code.PUSH_GIFT_UPDATE_VALUE
})
public class PushGiftResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		switch(protocol.getType()) {
		case HP.code.PUSH_GIFT_SYN_INFO_VALUE:
			doSynInfo(robotEntity, protocol);
			break;
		case HP.code.PUSH_GIFT_UPDATE_VALUE:
			doUpdate(robotEntity, protocol);
			break;
		}

	}

	private void doSynInfo(GameRobotEntity robotEntity, HawkProtocol protocol) {
		PushGiftSynInfo synInfo = protocol.parseProtocol(PushGiftSynInfo.getDefaultInstance());
		robotEntity.getBasicData().setPushGiftList(synInfo.getPushGiftsList());
	}

	private void doUpdate(GameRobotEntity robotEntity, HawkProtocol protocol) {
		PushGiftUpdate updateInfo = protocol.parseProtocol(PushGiftUpdate.getDefaultInstance());
		if (updateInfo.getOper() == PushGiftOper.ADD) {
			robotEntity.getBasicData().getPushGiftList().addAll(updateInfo.getPushGiftsList());
		} else if (updateInfo.getOper() == PushGiftOper.DELETE){
			Iterator<PushGiftMsg> msgIter = robotEntity.getBasicData().getPushGiftList().iterator();
			for (PushGiftMsg msg : updateInfo.getPushGiftsList()) {
				while (msgIter.hasNext()) {
					PushGiftMsg oldMsg = msgIter.next();
					if (msg.getGiftId() == oldMsg.getGiftId()) {
						msgIter.remove();
					}
				}
			}			
		}
	}
	
	
}
