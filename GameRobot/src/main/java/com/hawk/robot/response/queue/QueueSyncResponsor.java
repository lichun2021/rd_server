package com.hawk.robot.response.queue;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Queue.HPQueueInfoSync;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.PLAYER_QUEUE_SYNC_S_VALUE)
public class QueueSyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPQueueInfoSync queueInfo = protocol.parseProtocol(HPQueueInfoSync.getDefaultInstance());
		robotEntity.getBasicData().refreshQueueData(queueInfo.getQueuesList().toArray(new QueuePB[0]));
	}

}
