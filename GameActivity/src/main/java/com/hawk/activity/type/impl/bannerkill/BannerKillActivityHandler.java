package com.hawk.activity.type.impl.bannerkill;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.BannerKillRewardGetReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 战神降临活动
 * 
 * @author lating
 *
 */
public class BannerKillActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 请求活动信息
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.BANNER_KILL_INFO_C_VALUE)
	public void onGetActivityInfo(HawkProtocol hawkProtocol, String playerId) {
		BannerKillActivity bannerKillActivity = this.getActivity(ActivityType.BANNER_KILL_ACTIVITY);
		bannerKillActivity.syncActivityDataInfo(playerId);
	}
	
	/**
	 * 领取活动目标奖励
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.BANNER_KILL_REWARD_REQ_VALUE) 
	public void onReceiveTargetAward(HawkProtocol hawkProtocol, String playerId){
		BannerKillActivity bannerKillActivity = this.getActivity(ActivityType.BANNER_KILL_ACTIVITY);
		BannerKillRewardGetReq cparam = hawkProtocol.parseProtocol(BannerKillRewardGetReq.getDefaultInstance()); 
		int result = bannerKillActivity.receive(playerId, cparam.getTargetId());
		
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			this.responseSuccess(playerId, hawkProtocol.getType());
		} else {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
	/**
	 * 请求活动排行信息
	 * 
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.BANNER_KILL_RANK_REQ_VALUE)
	public void onActivityRankReq(HawkProtocol protocol, String playerId){
		BannerKillActivity bannerKillActivity = this.getActivity(ActivityType.BANNER_KILL_ACTIVITY);
		if (bannerKillActivity.isAllowOprate(playerId)) {
			bannerKillActivity.pushRankInfo(playerId);
		}
	}
}