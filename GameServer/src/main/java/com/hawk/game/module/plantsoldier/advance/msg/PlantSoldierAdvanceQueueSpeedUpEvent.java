package com.hawk.game.module.plantsoldier.advance.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

public class PlantSoldierAdvanceQueueSpeedUpEvent extends HawkMsg {

	/** 加速时间(毫秒)*/
	private long upTime;

	public PlantSoldierAdvanceQueueSpeedUpEvent(long upTime) {
		super(MsgId.PLANT_ADVANCE_SPEEDUP);
		this.upTime = upTime;
	}

	public long getUpTime() {
		return upTime;
	}
}
