package com.hawk.activity.type.impl.logingift;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.LoginGiftRecieveReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 新版新手登录活动
 * 
 * @author lating
 *
 */
public class LoginGiftActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 获取活动界面信息
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.LOGIN_GIFT_ACTIVITY_INFO_REQ_VALUE)
	public boolean onGetPageInfo(HawkProtocol protocol, String playerId) {
		//ActivityManager.getInstance().postEvent(new GroupPurchaseEvent(playerId));
		LoginGiftActivity activity = getActivity(ActivityType.LOGIN_GIFT_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
		return true;
	}
	
	
	/**
	 * 领取奖励
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.LOGIN_GIFT_RECIEVE_REQ_VALUE)
	public boolean onRecieveLoginReward(HawkProtocol protocol, String playerId) {
		LoginGiftRecieveReq req = protocol.parseProtocol(LoginGiftRecieveReq.getDefaultInstance());
		LoginGiftActivity activity = getActivity(ActivityType.LOGIN_GIFT_ACTIVITY);
		int result = activity.onRecieveLoginReward(playerId, req.getDay(), req.getAdvance());
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			activity.syncActivityDataInfo(playerId);
		} else {
			this.sendErrorAndBreak(playerId, protocol.getType(), result);
		}
		return true;
	}
	
}
