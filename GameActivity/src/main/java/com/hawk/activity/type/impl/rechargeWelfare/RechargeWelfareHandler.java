package com.hawk.activity.type.impl.rechargeWelfare;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.RechargeWelfareItemSetReq;
import com.hawk.game.protocol.Activity.RechargeWelfareLotteryReq;

public class RechargeWelfareHandler extends ActivityProtocolHandler{

	/**
	 * 充值福利设置奖励
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.RECHARGE_WELFARE_SET_ITEM_REQ_VALUE)
	public void setRewardItemReq(HawkProtocol protocol, String playerId){
		RechargeWelfareActivity activity = getActivity(ActivityType.RECHARGE_WELFARE_ACTIVITY);
		if(null != activity && activity.isOpening(playerId)){
			RechargeWelfareItemSetReq proto = protocol.parseProtocol(RechargeWelfareItemSetReq.getDefaultInstance());
			Result<?> result = activity.rechargeWelfareSetRewardItem(playerId, proto);
			if(result.isFail()){
				sendErrorAndBreak(playerId, HP.code.RECHARGE_WELFARE_SET_ITEM_REQ_VALUE, result.getStatus());
			}else{
				responseSuccess(playerId, protocol.getType());
			}
		}
	}
	
	@ProtocolHandler(code=HP.code.RECHARGE_WELFARE_LOTTERY_REQ_VALUE)
	public void lotteryRewardReq(HawkProtocol protocol, String playerId){
		RechargeWelfareActivity activity = getActivity(ActivityType.RECHARGE_WELFARE_ACTIVITY);
		if(null != activity && activity.isOpening(playerId)){
			RechargeWelfareLotteryReq proto = protocol.parseProtocol(RechargeWelfareLotteryReq.getDefaultInstance());
			Result<?> result = activity.rechargeWelfareLottery(playerId, proto, protocol.getType());
			if(result.isFail()){
				sendErrorAndBreak(playerId, HP.code.RECHARGE_WELFARE_LOTTERY_REQ_VALUE, result.getStatus());
			}else{
				responseSuccess(playerId, protocol.getType());
			}
		}
	}
	
	@ProtocolHandler(code=HP.code.RECHARGE_WELFARE_GET_COUPON_REQ_VALUE)
	public void receiveRechargeCouponReq(HawkProtocol protocol, String playerId){
		RechargeWelfareActivity activity = getActivity(ActivityType.RECHARGE_WELFARE_ACTIVITY);
		if(null != activity && activity.isOpening(playerId)){
			Result<?> result = activity.receiveRechargeCoupon(playerId, protocol.getType());
			if(result.isFail()){
				sendErrorAndBreak(playerId, HP.code.RECHARGE_WELFARE_GET_COUPON_REQ_VALUE, result.getStatus());
			}else{
				responseSuccess(playerId, protocol.getType());
			}
		}
	}
	
	@ProtocolHandler(code=HP.code.RECHARGE_WELFARE_GET_SCORE_COUPON_REQ_VALUE)
	public void receiveDailyScoreFreeCouponReq(HawkProtocol protocol, String playerId){
		RechargeWelfareActivity activity = getActivity(ActivityType.RECHARGE_WELFARE_ACTIVITY);
		if(null != activity && activity.isOpening(playerId)){
			Result<?> result = activity.receiveDailyScoreFreeCoupon(playerId, protocol.getType());
			if(result.isFail()){
				sendErrorAndBreak(playerId, HP.code.RECHARGE_WELFARE_GET_SCORE_COUPON_REQ_VALUE, result.getStatus());
			}else{
				responseSuccess(playerId, protocol.getType());
			}
		}
	}
}
