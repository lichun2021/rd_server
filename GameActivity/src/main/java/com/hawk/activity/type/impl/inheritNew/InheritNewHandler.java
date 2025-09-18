package com.hawk.activity.type.impl.inheritNew;


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
public class InheritNewHandler extends ActivityProtocolHandler {

	/**
	 * 获取活动界面信息
	 * @param playerId
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_INHERIT_NEW_PAGE_INFO_C_VALUE)
	public boolean getPageInfo(HawkProtocol protocol, String playerId) {
		InheritNewActivity activity = getActivity(ActivityType.INHERITE_NEW);
		activity.syncPageInfo(playerId);
		return true;
	}
	
	/**
	 * 激活承接
	 * @param playerId
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.INHERIT_NEW_ACTIVE_C_VALUE)
	public boolean activeInherit(HawkProtocol protocol, String playerId) {
		InheritNewActivity activity = getActivity(ActivityType.INHERITE_NEW);
		activity.onActiveInherit(playerId);
		return true;
	}
}
