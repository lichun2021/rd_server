package com.hawk.activity.type.impl.starInvest;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBStarInvestBuySpeedItemReq;
import com.hawk.game.protocol.Activity.PBStarInvestExploreSpeedReq;
import com.hawk.game.protocol.Activity.PBStarInvestExploreStartReq;
import com.hawk.game.protocol.Activity.PBStarInvestExploreTakeReq;
import com.hawk.game.protocol.Activity.PBStarInvestExploreUseAllSpeedReq;
import com.hawk.game.protocol.Activity.PBStarInvestTakeBuyRewardReq;
import com.hawk.game.protocol.HP;

/**勋章基金活动消息处理
 * @author Winder
 */
public class StarInvestHandler extends ActivityProtocolHandler {
	
	
	//领取奖励
	@ProtocolHandler(code = HP.code2.STAR_INVEST_TAKE_BUY_REWARD_REQ_VALUE)
	public void getStarInvestRechargeGiftReward(HawkProtocol protocol, String playerId){
		PBStarInvestTakeBuyRewardReq req =  protocol.parseProtocol(PBStarInvestTakeBuyRewardReq.getDefaultInstance());
		StarInvestActivity activity = getActivity(ActivityType.STAR_INVEST);
		Result<?> result = activity.getStarInvestReward(playerId, req.getId(), protocol.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	
	//领取免费奖励
	@ProtocolHandler(code = HP.code2.STAR_INVEST_TAKE_FREE_REWARD_REQ_VALUE)
	public void getStarInvestFreeGiftReward(HawkProtocol protocol, String playerId){
		PBStarInvestTakeBuyRewardReq req =  protocol.parseProtocol(PBStarInvestTakeBuyRewardReq.getDefaultInstance());
		StarInvestActivity activity = getActivity(ActivityType.STAR_INVEST);
		Result<?> result = activity.getStarInvestFreeReward(playerId, req.getId(), protocol.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	
	//探索开始
	@ProtocolHandler(code = HP.code2.STAR_INVEST_EXPLORE_START_REQ_VALUE)
	public void starInvestExploreStart(HawkProtocol protocol, String playerId){
		PBStarInvestExploreStartReq req =  protocol.parseProtocol(PBStarInvestExploreStartReq.getDefaultInstance());
		StarInvestActivity activity = getActivity(ActivityType.STAR_INVEST);
		Result<?> result = activity.exploreRewardStart(playerId, req.getId(), protocol.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	//探索奖励
	@ProtocolHandler(code = HP.code2.STAR_INVEST_EXPLORE_TAKE_REQ_VALUE)
	public void starInvestExploreRewardAchieve(HawkProtocol protocol, String playerId){
		PBStarInvestExploreTakeReq req =  protocol.parseProtocol(PBStarInvestExploreTakeReq.getDefaultInstance());
		StarInvestActivity activity = getActivity(ActivityType.STAR_INVEST);
		Result<?> result = activity.exploreRewardAchieve(playerId, req.getId(), protocol.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	//探索加速
	@ProtocolHandler(code = HP.code2.STAR_INVEST_EXPLORE_SPEED_REQ_VALUE)
	public void starInvestExploreSpeed(HawkProtocol protocol, String playerId){
		PBStarInvestExploreSpeedReq req =  protocol.parseProtocol(PBStarInvestExploreSpeedReq.getDefaultInstance());
		StarInvestActivity activity = getActivity(ActivityType.STAR_INVEST);
		Result<?> result = activity.exploreSpeed(playerId, req.getId(), protocol.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	//探索加速使用剩余道具
	@ProtocolHandler(code = HP.code2.STAR_INVEST_EXPLORE_SPEED_ALL_REQ_VALUE)
	public void starInvestExploreAllSpeed(HawkProtocol protocol, String playerId){
		PBStarInvestExploreUseAllSpeedReq req =  protocol.parseProtocol(PBStarInvestExploreUseAllSpeedReq.getDefaultInstance());
		StarInvestActivity activity = getActivity(ActivityType.STAR_INVEST);
		Result<?> result = activity.exploreSpeedAllLastItem(playerId, req.getId(), protocol.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}else{
			responseSuccess(playerId, protocol.getType());
		}
	}
	
	//购买加速道具
	@ProtocolHandler(code = HP.code2.STAR_INVEST_EXPLORE_BUY_SPEED_ITEM_REQ_VALUE)
	public void starInvestExploreBuySpeedItem(HawkProtocol protocol, String playerId){
		PBStarInvestBuySpeedItemReq req =  protocol.parseProtocol(PBStarInvestBuySpeedItemReq.getDefaultInstance());
		StarInvestActivity activity = getActivity(ActivityType.STAR_INVEST);
		Result<?> result = activity.exploreBuySpeedItem(playerId, req.getCount(), protocol.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	//探索历史
	@ProtocolHandler(code = HP.code2.STAR_INVEST_EXPLORE_RECORD_REQ_VALUE)
	public void starInvestExploreRecords(HawkProtocol protocol, String playerId){
		StarInvestActivity activity = getActivity(ActivityType.STAR_INVEST);
		Result<?> result = activity.getExploreRecords(playerId, protocol.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	
	//探索历史
	@ProtocolHandler(code = HP.code2.STAR_INVEST_PAGE_INFO_REQ_VALUE)
	public void starInvestInfo(HawkProtocol protocol, String playerId){
		StarInvestActivity activity = getActivity(ActivityType.STAR_INVEST);
		activity.syncActivityDataInfo(playerId);
		
	}
}
