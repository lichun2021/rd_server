package com.hawk.activity.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.activity.event.MsgArgsCallBack;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 给玩家发放奖励
 * @author PhilChen
 *
 */
public class PlayerRewardByIdFromActivityMsg extends HawkMsg {
	/** */
	private int rewardId;
	/** */
	private Action behaviorAction;
	/** */
	private MsgArgsCallBack callBack;
	
	private PlayerRewardByIdFromActivityMsg() {
		super(MsgId.PLAYER_REWARD_BY_ID_FROM_ACTIVITY);
	}
	
	public static PlayerRewardByIdFromActivityMsg valueOf(int rewardId, Action behaviorAction, MsgArgsCallBack callBack) {
		PlayerRewardByIdFromActivityMsg msg = new PlayerRewardByIdFromActivityMsg();
		msg.rewardId = rewardId;
		msg.behaviorAction = behaviorAction;
		msg.callBack = callBack;
		return msg;
	}

	public int getRewardId() {
		return rewardId;
	}

	public Action getBehaviorAction() {
		return behaviorAction;
	}

	public MsgArgsCallBack getCallBack() {
		return callBack;
	}

}
