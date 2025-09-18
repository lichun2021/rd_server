package com.hawk.activity.type.impl.inherit;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 军魂承接
 * @author Jesse
 *
 */
public class InheritHandler extends ActivityProtocolHandler {

	/**
	 * 获取活动界面信息
	 * @param playerId
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_INHERIT_PAGE_INFO_C_VALUE)
	public boolean getPageInfo(HawkProtocol protocol, String playerId) {
		InheritActivity activity = getActivity(ActivityType.MACHINE_AWAKE_ACTIVITY);
		activity.syncPageInfo(playerId);
		return true;
	}
	
	/**
	 * 激活承接
	 * @param playerId
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.INHERIT_ACTIVE_C_VALUE)
	public boolean activeInherit(HawkProtocol protocol, String playerId) {
		InheritActivity activity = getActivity(ActivityType.INHERITE);
		activity.onActiveInherit(playerId);
		return true;
	}
}
