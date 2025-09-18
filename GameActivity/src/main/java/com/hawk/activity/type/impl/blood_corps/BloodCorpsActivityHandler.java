package com.hawk.activity.type.impl.blood_corps;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 铁血军团活动网络消息接收句柄
 * 
 * @author Jesse
 *
 */
public class BloodCorpsActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 获取榜单信息
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_BLOOD_RANK_LIST_REQ_VALUE)
	public boolean onPullActivityInfo(HawkProtocol protocol, String playerId) {
		BloodCorpsActivity activity = getActivity(ActivityType.BLOOD_CORPS_ACTIVITY);
		activity.pullInfo(playerId);
		return true;
	}
}
