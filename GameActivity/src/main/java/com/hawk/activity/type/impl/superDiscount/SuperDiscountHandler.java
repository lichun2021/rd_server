package com.hawk.activity.type.impl.superDiscount;

import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.HPSuperDiscountBuyReq;
import com.hawk.game.protocol.HP;

public class SuperDiscountHandler extends ActivityProtocolHandler {

	/***
	 * 幸运折扣抽奖
	 * 
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.SUPER_DISCOUNT_DRAW_REQ_VALUE)
	public void onSuperDiscountActivityLottery(HawkProtocol protocol, String playerId) {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.SUPER_DISCOUNT_VALUE);
		if(activityOp.isPresent()){
			SuperDiscountActivity activity = (SuperDiscountActivity) activityOp.get();
			activity.onProtocolActivityDrawReq(protocol.getType(), playerId);
		}
	}

	/***
	 * 幸运折扣商品购买
	 * 
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.SUPER_DISCOUNT_BUY_REQ_VALUE)
	public void onSupeDiscountActivityBuy(HawkProtocol protocol, String playerId) {
		HPSuperDiscountBuyReq msg = protocol.parseProtocol(HPSuperDiscountBuyReq.getDefaultInstance());
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.SUPER_DISCOUNT_VALUE);
		if(activityOp.isPresent()){
			int voucherId = 0;
			if(msg.hasVoucherId()){
				voucherId = msg.getVoucherId();
			}
			SuperDiscountActivity activity = (SuperDiscountActivity) activityOp.get();
			activity.onProtocolActivityBuyReq(protocol.getType(), playerId, msg.getCfgId(), msg.getCount() <= 0 ? 1 : msg.getCount(),voucherId);
		}
	}
	
	/***
	 * 请求活动信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.SUPER_DISCOUNT_INFO_REQ_VALUE)
	public void onSupeDiscountActivityInfoReq(HawkProtocol protocol, String playerId) {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.SUPER_DISCOUNT_VALUE);
		if(activityOp.isPresent()){
			SuperDiscountActivity activity = (SuperDiscountActivity) activityOp.get();
			if (null != activity && activity.isOpening(playerId)) {
				activity.syncActivityDataInfo(playerId);
			}
		}
	}
}
