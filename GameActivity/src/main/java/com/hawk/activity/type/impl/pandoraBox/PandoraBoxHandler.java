package com.hawk.activity.type.impl.pandoraBox;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.helper.HawkAssert;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PandoraExchangeReq;
import com.hawk.game.protocol.Activity.PandoraLotteryReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class PandoraBoxHandler extends ActivityProtocolHandler {
	
	/****
	 * 潘朵拉抽奖
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.PANDORA_LOTTERY_REQ_VALUE)
	public void pandoraLottery(HawkProtocol protocol, String playerId){
		PandoraLotteryReq proto = protocol.parseProtocol(PandoraLotteryReq.getDefaultInstance());
		int count = proto.getCount();
		HawkAssert.checkPositive(count);
		PandoraBoxActivity activity = getActivity(ActivityType.PANDORA_BOX);
		if (activity == null) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		Result<?> result = activity.pandoraBoxLottery(playerId, count);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());			
		} else {			
			responseSuccess(playerId, protocol.getType());
		}
	}
	
	@ProtocolHandler(code = HP.code.PANDORA_LOTTERY_INFO_REQ_VALUE)
	public void pandoraLotteryInfoReq(HawkProtocol protocol, String playerId) {
		PandoraBoxActivity activity = getActivity(ActivityType.PANDORA_BOX);
		if (activity == null) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		
		activity.synPandoraLotteryInfo(playerId);
	}
	/****
	 * 潘朵拉兑换商城奖励
	 * @param protocol
	 * @param playerId
	 */
	@SuppressWarnings("unchecked")
	@ProtocolHandler(code=HP.code.PANDORA_EXCHANGE_REQ_VALUE)
	public void pandoraExchange(HawkProtocol protocol, String playerId){
		PandoraExchangeReq proto = protocol.parseProtocol(PandoraExchangeReq.getDefaultInstance());
		int count = proto.getCount();
		int configId = proto.getCfgId();
		PandoraBoxActivity activity = getActivity(ActivityType.PANDORA_BOX);
		if (activity == null) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		Result<Integer> result = (Result<Integer>) activity.pandoraBoxExchange(playerId, configId, count);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		} else {
			responseSuccess(playerId, protocol.getType());
		}
	}
	
	/***
	 * 潘朵拉宝盒商城信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.PANDORA_STORE_INFO_REQ_VALUE)
	public void reqStoreInfo(HawkProtocol protocol, String playerId){
		PandoraBoxActivity activity = getActivity(ActivityType.PANDORA_BOX);
		if (activity == null) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		activity.synPandoraStoreInfo(playerId);
	}
}
