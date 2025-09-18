package com.hawk.activity.type.impl.rewardOrder;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.recieveOrder;
import com.hawk.game.protocol.Activity.rewardOrderInfo;
import com.hawk.game.protocol.HP;

public class RewardOrderHandler extends ActivityProtocolHandler {
	
	/***
	 * 查看信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.REQ_REWARD_ORDER_INFO_VALUE)
	public void reqOrderInfo(HawkProtocol protocol, String playerId){
		RewardOrderActivity activity = getActivity(ActivityType.REWARD_ORDER);
		Result<?> result = activity.reqRewardOrderInfo(playerId);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
		rewardOrderInfo.Builder build = (rewardOrderInfo.Builder)result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.REQ_REWARD_ORDER_INFO_VALUE, build));
	}
	
	/***
	 * 刷新悬赏令
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.REFRESH_REWARD_ORDER_VALUE)
	public void refreshOrder(HawkProtocol protocol, String playerId){
		RewardOrderActivity activity = getActivity(ActivityType.REWARD_ORDER);
		Result<?> result = activity.refreshRewardOrder(playerId);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
		rewardOrderInfo.Builder build = (rewardOrderInfo.Builder)result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.REQ_REWARD_ORDER_INFO_VALUE, build));
	}
	
	/***
	 * 领取悬赏令
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.RECEIVE_REWARD_ORDER_VALUE)
	public void receiveOrder(HawkProtocol protocol, String playerId){
		recieveOrder req = protocol.parseProtocol(recieveOrder.getDefaultInstance());
		RewardOrderActivity activity = getActivity(ActivityType.REWARD_ORDER);
		Result<?> result = activity.receiveOrder(playerId, req.getOrderId());
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
		rewardOrderInfo.Builder build = (rewardOrderInfo.Builder)result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.REQ_REWARD_ORDER_INFO_VALUE, build));
	}
	
	/***
	 * 放弃悬赏令
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.GIVE_UP_REWARD_ORDER_VALUE)
	public void giveupOrder(HawkProtocol protocol, String playerId){
		RewardOrderActivity activity = getActivity(ActivityType.REWARD_ORDER);
		Result<?> result = activity.giveUpOrder(playerId);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
		rewardOrderInfo.Builder build = (rewardOrderInfo.Builder)result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.REQ_REWARD_ORDER_INFO_VALUE, build));
	}
	
	/***
	 * 获取悬赏令奖励
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.GET_REWARD_ORDER_REWARD_VALUE)
	public void getOrderReward(HawkProtocol protocol, String playerId){
		RewardOrderActivity activity = getActivity(ActivityType.REWARD_ORDER);
		Result<?> result = activity.takeOrderReward(playerId);
		if(result != null && result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
		if(result == null){
			return;
		}
		rewardOrderInfo.Builder build = (rewardOrderInfo.Builder)result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.REQ_REWARD_ORDER_INFO_VALUE, build));
	}
}
