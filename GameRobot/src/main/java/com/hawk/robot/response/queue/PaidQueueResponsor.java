package com.hawk.robot.response.queue;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Queue.PaidQueuePB;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.PAID_QUEUE_PUSH_VALUE)
public class PaidQueueResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		PaidQueuePB queueInfo = protocol.parseProtocol(PaidQueuePB.getDefaultInstance());
		long endTime = Long.MAX_VALUE;
		if (!queueInfo.getUnlockedPermanently()) {
			endTime = queueInfo.getEnableEndTime();
		}
		robotEntity.getBasicData().resetPaidQueueEndTime(endTime);
	}

}
