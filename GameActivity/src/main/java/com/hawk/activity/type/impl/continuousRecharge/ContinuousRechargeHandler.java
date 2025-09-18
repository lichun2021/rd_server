package com.hawk.activity.type.impl.continuousRecharge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.continuousRecharge.cfg.ContinuousRechargeCfg;
import com.hawk.activity.type.impl.continuousRecharge.entity.ContinuousRechargeEntity;
import com.hawk.activity.type.impl.continuousRecharge.item.ContinuousRechargeItem;
import com.hawk.game.protocol.Activity.ContinuousRechargeRewardReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/**
 * 连续充值
 * @author golden
 *
 */
public class ContinuousRechargeHandler extends ActivityProtocolHandler {

	/**
	 * 领奖
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.CONTINUOUS_RECHARGE_REWARD_REQ_VALUE)
	public void getReward(HawkProtocol protocol, String playerId) {
		
		ContinuousRechargeRewardReq brokenExchangeReq = protocol.parseProtocol(ContinuousRechargeRewardReq.getDefaultInstance());
		int day = brokenExchangeReq.getDay();
		int rechargeGrade = brokenExchangeReq.getGrade();
		
		ContinuousRechargeActivity activity = getActivity(ActivityType.CONTINUOUS_RECHARGE_ACTIVITY);
		Optional<ContinuousRechargeEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("continue recharge reward failed, dContinuousRechargeEntity not exist, playerId: {}", playerId);
			return;
		}
		ContinuousRechargeEntity entity = opEntity.get();
		
		int currentDay = entity.getCurrent().getDay();
		if (day > currentDay || day <= 0) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("continue recharge reward failed, day param error, playerId: {}, day: {}", entity.getPlayerId(), day);
			return;
		}
		ContinuousRechargeCfg cfg = ContinuousRechargeCfg.getConfig(day, rechargeGrade);
		if (cfg == null) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("continue recharge reward failed, data error, playerId: {}, day: {}, grade: {}, config grade: {}", entity.getPlayerId(), day, rechargeGrade, cfg == null ? 0: cfg.getCount());
			return;
		}
		
		List<ContinuousRechargeItem> itemList = new ArrayList<ContinuousRechargeItem>(entity.getHistory());
		itemList.add(entity.getCurrent());
		int gradeCnt = 0; //该档充值满足次数
		for(ContinuousRechargeItem item : itemList){
			if(item.getCount() >= rechargeGrade){
				gradeCnt ++;
			}
			if(item.getDay() == day){
				if(item.getReceivedGrade().contains(rechargeGrade)){
					// 已领取过奖励，不能重复领取
					PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), Status.Error.CONTINUE_RECHARGE_AWARD_RECEIVED_VALUE);
					HawkLog.errPrintln("continue recharge reward failed, reward second times, playerId: {}, day: {}, grade: {}, received grade: {}", entity.getPlayerId(), day, rechargeGrade, entity.getCurrent().getReceivedGrade());
					return;
				}
			}
		}
		if(day > gradeCnt){ //领取次数大于充值挡满足次数
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), Status.Error.CONTINUE_RECHARGE_AWARD_RECEIVED_VALUE);
			HawkLog.errPrintln("continue recharge reward failed, reward second times, playerId: {}, day: {}, grade: {}, received grade: {}", entity.getPlayerId(), day, rechargeGrade, entity.getCurrent().getReceivedGrade());
			return;
		}
		//给奖励，记录领奖
		activity.getDataGeter().takeReward(playerId, cfg.getRewardList(), 1, Action.CONTINUOUS_RECHARGE, true, RewardOrginType.FIRST_RECHARGE_REWARD);
		for(ContinuousRechargeItem item : itemList){
			if(item.getDay() == day){
				item.addReceivedGrade(rechargeGrade);
				break;
			}
		}
		entity.notifyUpdate();
		
		PlayerPushHelper.getInstance().responseSuccess(playerId, protocol.getType());
		activity.syncActivityDataInfo(playerId);
		activity.checkActivityClose(playerId);
	}
}
