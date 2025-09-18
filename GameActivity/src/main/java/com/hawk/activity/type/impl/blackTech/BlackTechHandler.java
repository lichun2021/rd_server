package com.hawk.activity.type.impl.blackTech;

import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.HPBlackTechActivityBuy;
import com.hawk.game.protocol.HP;

public class BlackTechHandler extends ActivityProtocolHandler {

	@ProtocolHandler(code = HP.code.HP_BLACK_TECH_REFRESH_C_VALUE)
	public void onActivityRefresh(HawkProtocol protocol, String playerId) {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance()
				.getActivity(Activity.ActivityType.BLACK_TECH_VALUE);
		if (activityOp.isPresent()) {
			BlackTechActivity activity = (BlackTechActivity) activityOp.get();
			if (activity.isOpening(playerId)) {
				Result<?> result = activity.onProtocolActivityDrawReq(HP.code.HP_BLACK_TECH_REFRESH_C_VALUE, playerId);
				if (result.isFail()) {
					sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
				}				
			}
		}
	}

	@ProtocolHandler(code = HP.code.HP_BLACK_TECH_ACTIVE_BUFF_C_VALUE)
	public void onActivityActiveBuff(HawkProtocol protocol, String playerId) {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance()
				.getActivity(Activity.ActivityType.BLACK_TECH_VALUE);
		if (activityOp.isPresent()) {
			BlackTechActivity activity = (BlackTechActivity) activityOp.get();
			if (activity.isOpening(playerId)) {
				Result<?> result = activity.onProtocolActivityActiveReq(HP.code.HP_BLACK_TECH_ACTIVE_BUFF_C_VALUE,
						playerId);
				if (result.isFail()) {
					sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
				}
			}

		}
	}

	@ProtocolHandler(code = HP.code.HP_BLACK_TECH_BUY_C_VALUE)
	public void onActivityBuy(HawkProtocol protocol, String playerId) {
		HPBlackTechActivityBuy msg = protocol.parseProtocol(HPBlackTechActivityBuy.getDefaultInstance());
		Optional<ActivityBase> activityOp = ActivityManager.getInstance()
				.getActivity(Activity.ActivityType.BLACK_TECH_VALUE);
		if (activityOp.isPresent()) {
			BlackTechActivity activity = (BlackTechActivity) activityOp.get();
			if (activity.isOpening(playerId)) {
				Result<?> result = activity.onProtocolActivityBuyReq(HP.code.HP_BLACK_TECH_BUY_C_VALUE, playerId,
						msg.getCfgId(), msg.getCount());
				if (result.isFail()) {
					sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
				}				
			}
		}
	}
}
