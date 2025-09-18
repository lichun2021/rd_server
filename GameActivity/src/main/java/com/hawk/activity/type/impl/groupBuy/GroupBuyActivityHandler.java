package com.hawk.activity.type.impl.groupBuy;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.DiscoutCheckReq;
import com.hawk.game.protocol.Activity.GroupBuyRecordPageInfo;
import com.hawk.game.protocol.Activity.GroupBuyReq;
import com.hawk.game.protocol.Activity.HotSellFreePointReq;
import com.hawk.game.protocol.Activity.TopDiscountRewardGetReq;
import com.hawk.game.protocol.HP;

public class GroupBuyActivityHandler extends ActivityProtocolHandler {
	
	/***
	 * 查看界面
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.GROUP_BUY_PAGE_INFO_REQ_VALUE)
	public void getGroupBuyPageInfo(HawkProtocol protocol, String playerId){
		GroupBuyActivity activity = getActivity(ActivityType.GROUP_BUY_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
	}
	
	@ProtocolHandler(code = HP.code.GROUP_BUY_RECORD_REQ_VALUE)
	public void getGroupBuyRecordInfo(HawkProtocol protocol, String playerId){
		GroupBuyActivity activity = getActivity(ActivityType.GROUP_BUY_ACTIVITY);
		GroupBuyRecordPageInfo.Builder builder = activity.getGroupBuyRecord(playerId);
		if (builder != null) {
			sendProtocol(playerId, HawkProtocol.valueOf(HP.code.GROUP_BUY_RECORD_RESP_VALUE, builder));
		}
	}
	
	@ProtocolHandler(code = HP.code.GROUP_BUY_REQ_VALUE)
	public void groupBuyReq(HawkProtocol protocol, String playerId){
		GroupBuyReq req = protocol.parseProtocol(GroupBuyReq.getDefaultInstance());
		GroupBuyActivity activity = getActivity(ActivityType.GROUP_BUY_ACTIVITY);
		Result<?> result = activity.groupBuyGift(playerId, req.getId(), req.getNum(), protocol.getType());
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
	}
	
	@ProtocolHandler(code = HP.code.GROUP_BUY_DISCOUNT_CHECK_REQ_VALUE)
	public void groupBuyDiscountCheckReq(HawkProtocol protocol, String playerId){
		DiscoutCheckReq req = protocol.parseProtocol(DiscoutCheckReq.getDefaultInstance());
		GroupBuyActivity activity = getActivity(ActivityType.GROUP_BUY_ACTIVITY);
		Result<?> result = activity.groupBuyDiscountCheck(playerId, req.getGiftId(), req.getCfgId());
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}else{
			responseSuccess(playerId, protocol.getType());
		}
	}
	
	/**
	 * 热销商品免费积分领取
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.GROUP_BUY_HOTSELL_FREEPOINT_C_VALUE)
	public void hotSellFreePointReq(HawkProtocol protocol, String playerId){
		HotSellFreePointReq req = protocol.parseProtocol(HotSellFreePointReq.getDefaultInstance());
		GroupBuyActivity activity = getActivity(ActivityType.GROUP_BUY_ACTIVITY);
		Result<?> result = activity.hotSellFreePointGet(playerId, req.getGiftId());
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		} else {
			responseSuccess(playerId, protocol.getType());
		}
	}
	
	/**
	 * 最高折扣奖励领取
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.GROUP_BUY_TOPDISCOUNT_REWARD_C_VALUE)
	public void topDiscountRewardGetReq(HawkProtocol protocol, String playerId){
		TopDiscountRewardGetReq req = protocol.parseProtocol(TopDiscountRewardGetReq.getDefaultInstance());
		GroupBuyActivity activity = getActivity(ActivityType.GROUP_BUY_ACTIVITY);
		Result<?> result = activity.topDiscountRewardGet(playerId, req.getGiftId(), req.getBuyTimesItem());
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		} else {
			responseSuccess(playerId, protocol.getType());
		}
	}

}
