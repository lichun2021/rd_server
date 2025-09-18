package com.hawk.activity.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 
 * @author jesse
 */
public class ActivityDoubleRechargeMsg extends HawkMsg{
	/**
	 * 玩家id
	 */
	private String playerId;

	public String getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	public ActivityDoubleRechargeMsg() {
		super(MsgId.ACTIVITY_DOUBLE_RECHARGE);
	}
	
	public static ActivityDoubleRechargeMsg valueOf(String playerId){
		ActivityDoubleRechargeMsg msg = new ActivityDoubleRechargeMsg();
		msg.playerId = playerId;
		return msg;
	}
}
