package com.hawk.activity.type.impl.machineSell;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.HPMachineSellLotteryReq;
import com.hawk.game.protocol.HP;

public class MachineSellHandler extends ActivityProtocolHandler {
	// 机甲破世，请求购买碎片
	@ProtocolHandler(code = HP.code.MACHINE_SELL_LOTTERY_REQ_VALUE)
	public void onPlayerLotteryReq(HawkProtocol protocol, String playerId) {
		HPMachineSellLotteryReq msg = protocol.parseProtocol(HPMachineSellLotteryReq.getDefaultInstance());
		MachineSellActivity activity = getActivity(ActivityType.MACHINE_SELL_ACTIVITY);
		if (null == activity) {
			LoggerFactory.getLogger("Server").info("machinesell_log QuestionShare activity not open");
			return;
		}
		if (!activity.isOpening(playerId)) {
			LoggerFactory.getLogger("Server").info("machinesell_log QuestionShare activity not open for player:{}",
					playerId);
			return;
		}
		
		Result<?> result = activity.onLottery(playerId, msg);
		if(null != result && result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
		activity.syncActivityDataInfo(playerId);
	}
}
