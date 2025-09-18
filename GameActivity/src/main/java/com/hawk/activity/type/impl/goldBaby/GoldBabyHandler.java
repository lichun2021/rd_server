package com.hawk.activity.type.impl.goldBaby;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.GoldBabyBuyTicketReq;
import com.hawk.game.protocol.Activity.GoldBabyFindReq;
import com.hawk.game.protocol.Activity.GoldBabyLockTopGradeReq;
import com.hawk.game.protocol.HP;

/**
 * 
 * @author Golden
 *
 */
public class GoldBabyHandler extends ActivityProtocolHandler {
	
	//活动信息请求
	@ProtocolHandler(code = HP.code2.GOLD_BABY_INFO_REQ_VALUE)
	public boolean onInfoReq(HawkProtocol protocol, String playerId) {
		GoldBabyActivity activity = getActivity(ActivityType.GOLD_BABY_ACTIVITY);
		Result<?> result = activity.onInfoReq(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		return true;
	}
	
	//搜寻
	@ProtocolHandler(code = HP.code2.GOLD_BABY_FIND_REQ_VALUE)
	public boolean onFindReward(HawkProtocol protocol, String playerId) {
		GoldBabyFindReq req = protocol.parseProtocol(GoldBabyFindReq.getDefaultInstance());
		GoldBabyActivity activity = getActivity(ActivityType.GOLD_BABY_ACTIVITY);
		int poolId = req.getPoolId();
		Result<?> result = activity.findReward(playerId, poolId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		return true;
	}
	
	//锁定最高档
	@ProtocolHandler(code = HP.code2.GOLD_BABY_LOCK_REQ_VALUE)
	public boolean onLockTopGrade(HawkProtocol protocol, String playerId) {
		GoldBabyLockTopGradeReq req = protocol.parseProtocol(GoldBabyLockTopGradeReq.getDefaultInstance());
		GoldBabyActivity activity = getActivity(ActivityType.GOLD_BABY_ACTIVITY);
		
		int poolId = req.getPoolId();
		Result<?> result = activity.lockTopGrade(playerId,poolId);
		
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		return true;
	}
	
	//购买道具请求
	@ProtocolHandler(code = HP.code2.GOLD_BABY_BUY_TICKET_REQ_VALUE)
	public boolean onBuyTicket(HawkProtocol protocol, String playerId) {
		GoldBabyBuyTicketReq req = protocol.parseProtocol(GoldBabyBuyTicketReq.getDefaultInstance());
		GoldBabyActivity activity = getActivity(ActivityType.GOLD_BABY_ACTIVITY);
		int count = req.getCount();
		Result<?> result = activity.buyTicket(playerId, count);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		return true;	
	}
}
