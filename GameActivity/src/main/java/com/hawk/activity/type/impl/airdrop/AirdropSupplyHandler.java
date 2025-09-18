package com.hawk.activity.type.impl.airdrop;

import com.hawk.activity.ActivityProtocolHandler;
public class AirdropSupplyHandler extends ActivityProtocolHandler{

	/**
	 * 充值福利设置奖励
	 * @param protocol
	 * @param playerId
	 *//*
	@ProtocolHandler(code=HP.code.RECHARGE_WELFARE_SET_ITEM_REQ_VALUE)
	public void setRewardItemReq(HawkProtocol protocol, String playerId){
		AirdropSupplyActivity activity = getActivity(ActivityType.RECHARGE_WELFARE_ACTIVITY);
		if(null != activity && activity.isOpening(playerId)){
			RechargeWelfareItemSetReq proto = protocol.parseProtocol(RechargeWelfareItemSetReq.getDefaultInstance());
			Result<?> result = activity.rechargeWelfareSetRewardItem(playerId, proto);
			if(result.isFail()){
				sendErrorAndBreak(playerId, HP.code.RECHARGE_WELFARE_SET_ITEM_REQ_VALUE, result.getStatus());
			}else{
				responseSuccess(playerId, protocol.getType());
			}
		}
	}*/
}
