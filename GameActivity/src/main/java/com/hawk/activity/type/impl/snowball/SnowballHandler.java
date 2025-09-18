package com.hawk.activity.type.impl.snowball;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.SnowballGetBoxRewardReq;
import com.hawk.game.protocol.Activity.SnowballRankReq;
import com.hawk.game.protocol.Activity.SnowballRankType;
import com.hawk.game.protocol.HP;

/**
 * 雪球大战
 * @author golden
 *
 */
public class SnowballHandler extends ActivityProtocolHandler {
	
	/**
	 * 请求界面信息
	 */
	@ProtocolHandler(code = HP.code.SNOWBALL_PAGE_INFO_REQ_VALUE)
	public void pageInfo(HawkProtocol protocol, String playerId) {
		SnowballActivity activity = this.getActivity(ActivityType.SNOWBALL);
		activity.pushPageInfo(playerId);
	}
	
	/**
	 * 获取宝箱奖励
	 */
	@ProtocolHandler(code = HP.code.SNOWBALL_GET_BOX_REWARD_REQ_VALUE)
	public void getBoxReward(HawkProtocol protocol, String playerId) {
		SnowballActivity activity = this.getActivity(ActivityType.SNOWBALL);
		if (!activity.isOpening(playerId)) {
			return;
		}
		
		SnowballGetBoxRewardReq req = protocol.parseProtocol(SnowballGetBoxRewardReq.getDefaultInstance());
		activity.getBoxReward(playerId, req.getCfgId());
		activity.pushPageInfo(playerId);
	}
	
	/**
	 * 获取排行榜单信息
	 */
	@ProtocolHandler(code = HP.code.SNOWBALL_GET_RANK_INFO_REQ_VALUE)
	public boolean onGetHistoryRankList(HawkProtocol protocol, String playerId) {
		SnowballRankReq req = protocol.parseProtocol(SnowballRankReq.getDefaultInstance());
		SnowballRankType rankType = req.getRankType();
		SnowballActivity activity = getActivity(ActivityType.SNOWBALL);
		activity.pullRankInfo(playerId, rankType);
		return true;
	}
}
