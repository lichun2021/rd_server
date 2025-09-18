package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 任务消息体
 * 
 * @author golden
 *
 */
@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class MissionMsg extends HawkMsg {

	private MissionEvent event;

	public MissionMsg() {
		super(MsgId.MISSION_TRIGGERED);
	}

	public MissionEvent getEvent() {
		return event;
	}

	public void setEvent(MissionEvent event) {
		this.event = event;
	}

	public static MissionMsg valueOf(MissionEvent event) {
		MissionMsg msg = HawkObjectPool.getInstance().borrowObject(MissionMsg.class);
		msg.event = event;
		return msg;
	}
}
