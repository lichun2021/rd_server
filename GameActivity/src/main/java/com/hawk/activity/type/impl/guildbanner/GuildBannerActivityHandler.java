package com.hawk.activity.type.impl.guildbanner;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 插旗（联盟排行榜）活动
 * 
 * @author lating
 *
 */
public class GuildBannerActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 请求活动排行信息
	 * 
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.GUILD_BANNER_RANK_REQ_VALUE)
	public void onAcitivityRankReq(HawkProtocol protocol, String playerId){
		GuildBannerActivity bannerActivity = this.getActivity(ActivityType.GUILD_BANNER_ACTIVITY);
		if (bannerActivity.isAllowOprate(playerId)) {
			bannerActivity.pushRankInfo(playerId);
		}
	}
	
	@ProtocolHandler(code = HP.code.GUILD_BANNER_PAGE_INFO_REQ_VALUE)
	public void onAcitivityPageInfoReq(HawkProtocol protocol, String playerId){
		GuildBannerActivity bannerActivity = this.getActivity(ActivityType.GUILD_BANNER_ACTIVITY);
		if (bannerActivity.isAllowOprate(playerId)) {
			bannerActivity.pushActivityPageInfo(playerId);
		}
	}
}