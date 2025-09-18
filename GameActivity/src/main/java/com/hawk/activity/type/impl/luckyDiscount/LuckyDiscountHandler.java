package com.hawk.activity.type.impl.luckyDiscount;

import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.HPLuckyDiscountBuy;

public class LuckyDiscountHandler extends ActivityProtocolHandler {

	/***
	 * 幸运折扣抽奖
	 * 
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.HP_LUCKY_DISCOUNT_DRAW_REQ_C_VALUE)
	public void onLuckyDiscountActivityLottery(HawkProtocol protocol, String playerId) {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.LUCKY_DISCOUNT_VALUE);
		if(activityOp.isPresent()){
			LuckyDiscountActivity activity = (LuckyDiscountActivity) activityOp.get();
			activity.onProtocolActivityDrawReq(protocol.getType(), playerId);
		}
	}

	/***
	 * 幸运折扣商品购买
	 * 
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.HP_LUCKY_DISCOUNT_BUY_REQ_C_VALUE)
	public void onLuckyDiscountActivityBuy(HawkProtocol protocol, String playerId) {
		HPLuckyDiscountBuy msg = protocol.parseProtocol(HPLuckyDiscountBuy.getDefaultInstance());
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.LUCKY_DISCOUNT_VALUE);
		if(activityOp.isPresent()){
			LuckyDiscountActivity activity = (LuckyDiscountActivity) activityOp.get();
			activity.onProtocolActivityBuyReq(protocol.getType(), playerId, msg.getCfgId(), msg.getCount() <= 0 ? 1 : msg.getCount());
		}
	}
	
	/***
	 * 请求活动信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.HP_LUCKY_DISCOUNT_INFO_SYNC_C_VALUE)
	public void onLuckyDiscountActivityInfoReq(HawkProtocol protocol, String playerId) {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.LUCKY_DISCOUNT_VALUE);
		if(activityOp.isPresent()){
			LuckyDiscountActivity activity = (LuckyDiscountActivity) activityOp.get();
			if (null != activity && activity.isOpening(playerId)) {
				activity.syncActivityDataInfo(playerId);
			}
		}
	}
}
