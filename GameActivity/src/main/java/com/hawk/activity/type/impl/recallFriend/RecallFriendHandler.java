package com.hawk.activity.type.impl.recallFriend;

import com.hawk.game.protocol.Activity;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.RecallFriendReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import org.hawk.result.Result;

public class RecallFriendHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.RECALL_FRIEND_REQ_VALUE)
	public void onRecallFriendReq(HawkProtocol protocol, String playerId) {
		RecallFriendReq req = protocol.parseProtocol(RecallFriendReq.getDefaultInstance()); 
		RecallFriendActivity activity = getActivity(ActivityType.RECALL_FRIEND_ACTIVITY);
		if (activity.isAllowOprate(playerId)) {
			int result = activity.recallFriendReq(playerId, req.getOpenId());
			if (result != Status.SysError.SUCCESS_OK_VALUE) {						
				this.sendErrorAndBreak(playerId, protocol.getType(), result);
			}
		}
	}
	
	@ProtocolHandler(code = HP.code.RECALL_FRIEND_INFO_REQ_VALUE)
	public void onRecallFriendInfoReq(HawkProtocol protocol, String playerId) {
		RecallFriendActivity activity = getActivity(ActivityType.RECALL_FRIEND_ACTIVITY);
		if (activity.isAllowOprate(playerId)) {
			activity.recallFriendInfo(playerId);
		}
	}


	/**
	 * 联盟召回回流玩家
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.RECALL_GUILD_BACKFLOW_REQ_VALUE)
	public void onRecallFriendGuildReq(HawkProtocol protocol, String playerId) {
		Activity.RecallGuildBackFlowReq req = protocol.parseProtocol(Activity.RecallGuildBackFlowReq.getDefaultInstance());
		RecallFriendActivity activity = getActivity(ActivityType.RECALL_FRIEND_ACTIVITY);
		if (activity.isAllowOprate(playerId)) {
			Result<?> result = activity.recallGuildBackFlowPlayerReq(playerId, req.getPlayerId());
			if (result.isFail()) {
				this.sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			}else {
				responseSuccess(playerId, protocol.getType());
			}
		}
	}

	/**
	 * 联盟召回回流玩家的列表
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.RECALL_GUILD_BACKFLOW_INFO_REQ_VALUE)
	public void onRecallFriendGuildInfoReq(HawkProtocol protocol, String playerId) {
		RecallFriendActivity activity = getActivity(ActivityType.RECALL_FRIEND_ACTIVITY);
		if (activity.isAllowOprate(playerId)) {
			activity.getGuildBackFlowPlayerInfoReq(playerId);
		}
	}
}
 