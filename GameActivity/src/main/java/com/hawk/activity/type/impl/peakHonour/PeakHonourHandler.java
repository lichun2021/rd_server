package com.hawk.activity.type.impl.peakHonour;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.ActivityPeakHonour.PeakHonourGetPointReward;
import com.hawk.game.protocol.HP;

/**
 * 巅峰荣耀
 * @author Golden
 *
 */
public class PeakHonourHandler extends ActivityProtocolHandler{
	/**
	 * 请求界面信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.ACTIVITY_PEAK_HONOUR_PAGE_REQ_VALUE)
	public void pageInfo(HawkProtocol protocol, String playerId){
		PeakHonourActivity activity = getActivity(ActivityType.PEAK_HONOUR);
		activity.pushPageInfo(playerId);
	}
	
	/**
	 * 领取个人奖励
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.ACTIVITY_PEAK_HONOUR_OWN_POINT_REWARD_VALUE)
	public void ownAward(HawkProtocol protocol, String playerId){
		PeakHonourGetPointReward req = protocol.parseProtocol(PeakHonourGetPointReward.getDefaultInstance());
		PeakHonourActivity activity = getActivity(ActivityType.PEAK_HONOUR);
		activity.getOwnAward(playerId, req.getId());
	}
}
