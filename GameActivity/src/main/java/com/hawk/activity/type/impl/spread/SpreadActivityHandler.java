package com.hawk.activity.type.impl.spread;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.HPBindSpreadCodeReq;
import com.hawk.game.protocol.Activity.HPSpreadRewardAchieveReq;
import com.hawk.game.protocol.Activity.HPSpreadStoreBuyReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class SpreadActivityHandler extends ActivityProtocolHandler {

	@ProtocolHandler(code = HP.code.HP_SPREAD_INFO_REQ_C_VALUE)
	public void onActivityInfoReq(HawkProtocol protocol, String playerId) {
		SpreadActivity activity = getActivity(ActivityType.SPREAD_ACTIVITY);
		if (null == activity) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.SPREAD_ACTIVITY_NOT_EXISTS_VALUE);
			return;
		}
		if (!activity.isOpening(playerId)) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.SPREAD_ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		activity.syncActivityDataInfo(playerId);
	}

	@ProtocolHandler(code = HP.code.HP_SPREAD_FRIEND_INFO_REQ_C_VALUE)
	public void onActivityFriendInfoReq(HawkProtocol protocol, String playerId) {
		SpreadActivity activity = getActivity(ActivityType.SPREAD_ACTIVITY);
		if (null == activity) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.SPREAD_ACTIVITY_NOT_EXISTS_VALUE);
			return;
		}
		if (!activity.isOpening(playerId)) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.SPREAD_ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		activity.onProtocolSendFriendInfo(playerId);
	}
	
	@ProtocolHandler(code = HP.code.HP_SPREAD_BIND_CODE_REQ_C_VALUE)
	public void onActivityBindCodeReq(HawkProtocol protocol, String playerId) {
		HPBindSpreadCodeReq msg = protocol.parseProtocol(HPBindSpreadCodeReq.getDefaultInstance());
		SpreadActivity activity = getActivity(ActivityType.SPREAD_ACTIVITY);
		if (null == activity) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.SPREAD_ACTIVITY_NOT_EXISTS_VALUE);
			return;
		}
		if (!activity.isOpening(playerId)) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.SPREAD_ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		activity.onProtocolPlayerBindCode(protocol.getType(), playerId, msg.getCode());
	}
	
	@ProtocolHandler(code = HP.code.HP_SPREAD_STORE_BUY_REQ_C_VALUE)
	public void onActivityStoreBuyReq(HawkProtocol protocol, String playerId) {
		HPSpreadStoreBuyReq msg = protocol.parseProtocol(HPSpreadStoreBuyReq.getDefaultInstance());
		SpreadActivity activity = getActivity(ActivityType.SPREAD_ACTIVITY);
		if (null == activity) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.SPREAD_ACTIVITY_NOT_EXISTS_VALUE);
			return;
		}
		if (!activity.isOpening(playerId)) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.SPREAD_ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		activity.onProtocolPlayerExchange(protocol.getType(), playerId, msg.getId(), msg.getCount());
	}
	
	@ProtocolHandler(code = HP.code.HP_SPREAD_DAY_REWARD_REQ_C_VALUE)
	public void onActivityRewardDailyReq(HawkProtocol protocol, String playerId) {
		SpreadActivity activity = getActivity(ActivityType.SPREAD_ACTIVITY);
		if (null == activity) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.SPREAD_ACTIVITY_NOT_EXISTS_VALUE);
			return;
		}
		if (!activity.isOpening(playerId)) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.SPREAD_ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		activity.onProtocolPlayerRewardDaily(protocol.getType() , playerId);
	}
	
	@ProtocolHandler(code = HP.code.HP_SPREAD_REWARD_ACHIEVE_C_VALUE)
	public void onActivityRewardAchieveReq(HawkProtocol protocol, String playerId) {
		HPSpreadRewardAchieveReq msg = protocol.parseProtocol(HPSpreadRewardAchieveReq.getDefaultInstance());
		SpreadActivity activity = getActivity(ActivityType.SPREAD_ACTIVITY);
		if (null == activity) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.SPREAD_ACTIVITY_NOT_EXISTS_VALUE);
			return;
		}
		if (!activity.isOpening(playerId)) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.SPREAD_ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		activity.onProtocolPlayerRewardAchieve(protocol.getType() , playerId, msg.getCfgId() );
	}
}
