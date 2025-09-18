package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 限时商店触发信息
 * 
 * @author lating
 *
 */
public class TimeLimitStoreTriggerMsg extends HawkMsg {
	/**
	 * 触发类型
	 */
	private int triggerType;
	/**
	 * 触发数量
	 */
	private int triggerNum;
	
	public TimeLimitStoreTriggerMsg(int triggerType, int triggerNum) {
		super(MsgId.TIMELIMIT_STORE_TRIGGER);
		this.triggerType = triggerType;
		this.triggerNum = triggerNum;
	}

	public int getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(int triggerType) {
		this.triggerType = triggerType;
	}

	public int getTriggerNum() {
		return triggerNum;
	}

	public void setTriggerNum(int triggerNum) {
		this.triggerNum = triggerNum;
	}

}
