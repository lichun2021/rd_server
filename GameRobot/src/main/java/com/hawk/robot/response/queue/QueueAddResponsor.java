package com.hawk.robot.response.queue;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.queue.PlayerQueueAction;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = { HP.code.QUEUE_ADD_PUSH_VALUE, HP.code.QUEUE_UPDATE_PUSH_VALUE })
public class QueueAddResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		QueuePB addQueueInfo = protocol.parseProtocol(QueuePB.getDefaultInstance());
		robotEntity.getBasicData().refreshQueueData(addQueueInfo);
		RobotLog.cityPrintln("queue info from server, playerId: {}, protocol: {}, type: {}, startTime: {}, endTime: {}, total: {}, total reduce: {}", 
				robotEntity.getPlayerId(), protocol.getType(), addQueueInfo.getQueueType(), 
				addQueueInfo.getStartTime(), addQueueInfo.getEndTime(), addQueueInfo.getTotalQueueTime(), addQueueInfo.getTotalReduceTime());
		// 新建的队列还没加速过，现在加速
		if(addQueueInfo.getTotalReduceTime() <= 0 && !PlayerQueueAction.checkQueueRemainTimeFree(robotEntity, addQueueInfo)) {
			GameRobotApp.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					PlayerQueueAction.doQueueSpeedAction(robotEntity);
				}
			});
		}
	}

}
