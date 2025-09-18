package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.protocol.SuperSoldier.SupersoldierTaskType;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 机甲建筑任务触发消息
 * 
 * @author lating
 *
 */
public class SuperSoldierTriggeTaskMsg extends HawkMsg {

	private SupersoldierTaskType taskType;
	
	private int num;

	public SuperSoldierTriggeTaskMsg(SupersoldierTaskType type, int num) {
		super(MsgId.SUPER_SOLDIER_TASK);
		this.taskType = type;
		this.num = num;
	}

	public SupersoldierTaskType getTaskType() {
		return taskType;
	}

	public int getNum() {
		return num;
	}
	
}
