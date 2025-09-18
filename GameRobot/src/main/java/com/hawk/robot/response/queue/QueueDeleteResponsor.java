package com.hawk.robot.response.queue;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Queue.QueuePBSimple;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = { HP.code.QUEUE_DELETE_PUSH_VALUE, HP.code.QUEUE_CANCEL_PUSH_VALUE })
public class QueueDeleteResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		QueuePBSimple delete = protocol.parseProtocol(QueuePBSimple.getDefaultInstance());
		robotEntity.getBasicData().deleteQueue(delete.getId());
	}

}
