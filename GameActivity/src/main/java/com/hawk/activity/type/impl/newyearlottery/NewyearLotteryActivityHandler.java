package com.hawk.activity.type.impl.newyearlottery;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.NewyearLotteryAchieveRewardReq;
import com.hawk.game.protocol.Activity.NewyearLotteryGiftAwardSelectReq;
import com.hawk.game.protocol.Activity.NewyearLotteryReq;
import com.hawk.game.protocol.HP;

public class NewyearLotteryActivityHandler extends ActivityProtocolHandler {

	/**
	 * 请求活动页面信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.NEWYEAR_LOTTERY_ACTIVITY_INFO_REQ_VALUE)
	public void onActivityInfoReq(HawkProtocol protocol, String playerId) {
		NewyearLotteryActivity activity = getActivity(ActivityType.NEWYEAR_LOTTERY_ACTIVITY);
		Result<?> result = activity.syncActivityInfo(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	/**
	 * 请求自选奖励
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.NEWYEAR_LOTTERY_GIFT_AWARD_SELECT_VALUE)
	public void onGiftSelectRewardReq(HawkProtocol protocol, String playerId) {
		NewyearLotteryActivity activity = getActivity(ActivityType.NEWYEAR_LOTTERY_ACTIVITY);
		NewyearLotteryGiftAwardSelectReq req = protocol.parseProtocol(NewyearLotteryGiftAwardSelectReq.getDefaultInstance());
		Result<?> result = activity.selectGiftAward(playerId, req.getId());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		} else {
			responseSuccess(playerId, protocol.getType());
		}
	}
	
	/**
	 * 请求抽奖
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.NEWYEAR_LOTTERY_REQ_VALUE)
	public void onLotteryReq(HawkProtocol protocol, String playerId) {
		NewyearLotteryActivity activity = getActivity(ActivityType.NEWYEAR_LOTTERY_ACTIVITY);
		NewyearLotteryReq req = protocol.parseProtocol(NewyearLotteryReq.getDefaultInstance());
		Result<?> result = activity.lottery(playerId, req.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		} else {
			responseSuccess(playerId, protocol.getType());
		}
	}
	
	/**
	 * 请求领取成就任务
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.NEWYEAR_LOTTERY_ACHIEVE_REWARD_REQ_VALUE)
	public void onAchieveRewardReq(HawkProtocol protocol, String playerId) {
		NewyearLotteryActivity activity = getActivity(ActivityType.NEWYEAR_LOTTERY_ACTIVITY);
		NewyearLotteryAchieveRewardReq req = protocol.parseProtocol(NewyearLotteryAchieveRewardReq.getDefaultInstance());
		Result<?> result = activity.onTakeAchieveReward(playerId, req.getId());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		} else {
			responseSuccess(playerId, protocol.getType());
		}
	}

}
