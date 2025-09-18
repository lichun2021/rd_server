package com.hawk.activity.type.impl.groupPurchase;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.event.impl.GroupPurchaseEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 跨服团购
 * @author Jesse
 */
public class GroupPurchaseActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 进入活动界面
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.REFRESH_GROUP_PURCHASE_ACHIEVE_INFO_C_VALUE)
	public boolean onEnterPage(HawkProtocol protocol, String playerId) {
		ActivityManager.getInstance().postEvent(new GroupPurchaseEvent(playerId));
		GroupPurchaseActivity activity = getActivity(ActivityType.GROUP_PURCHASE_ACTIVITY);
		if (activity.isOpening(playerId)) {
			activity.syncActivityDataInfo(playerId);
		}
		return true;
	}
	/**
	 * 进入活动界面
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GROUP_PURCHASE_DAILY_REWARD_REQ_VALUE)
	public boolean onDailyRewardReq(HawkProtocol protocol, String playerId) {
		GroupPurchaseActivity activity = getActivity(ActivityType.GROUP_PURCHASE_ACTIVITY);
		if (!activity.isOpening(playerId)) {
			return true;
		}
		activity.onDailyRewardReceiveReq(playerId);
		return true;
	}

}
