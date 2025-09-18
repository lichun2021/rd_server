package com.hawk.activity.type.impl.spaceguard;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 机甲玩法活动
 * 
 * @author lating
 */
public class SpaceGuardHandler extends ActivityProtocolHandler {
	
	/**
	 * 请求界面信息
	 */
	@ProtocolHandler(code = HP.code2.SPACE_MACHINE_GUARD_INFO_REQ_VALUE)
	public void pageInfo(HawkProtocol protocol, String playerId) {
		SpaceGuardActivity activity = this.getActivity(ActivityType.SPACE_GUARD_ACTIVITY);
		activity.pushPageInfo(playerId);
	}
}
