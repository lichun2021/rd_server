package com.hawk.game.recharge.impl;

import java.util.Optional;
import org.hawk.log.HawkLog;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CustomGiftPurchaseEvent;
import com.hawk.activity.type.impl.customgift.CustomGiftActivity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 定制礼包
 * 
 * @author lating
 *
 */
public class CustomGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.CUSTOM_MADE_GIFT_VALUE);
		if (!opActivity.isPresent()) {
			player.sendError(protocol, Status.Error.CUSTOM_GIFT_ACTIVITY_NOT_OPEN_VALUE, 0);
			HawkLog.errPrintln("CustomGiftActivity not exist, playerId: {}, openId: {}", player.getId(), player.getOpenId());
			return false;
		}
		
		CustomGiftActivity activity = (CustomGiftActivity)opActivity.get();
		if (!activity.isOpening(player.getId())) {
			player.sendError(protocol, Status.Error.CUSTOM_GIFT_ACTIVITY_NOT_OPEN_VALUE, 0);
			HawkLog.errPrintln("CustomGiftActivity not open, playerId: {}, openId: {}", player.getId(), player.getOpenId());
			return false;
		}
		
		// 定制礼包选择的奖励数量不对
		if (!activity.isGiftRewardSelectedFull(player.getId(), giftCfg.getId())) {
			player.sendError(protocol, Status.Error.GIFT_CHOOSEITEM_NOT_MATCH_VALUE, 0);
			HawkLog.errPrintln("CustomGift reward count error, playerId: {}, openId: {}, giftId: {}", player.getId(), player.getOpenId(), giftCfg.getId());
			return false;
		}
		
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new CustomGiftPurchaseEvent(player.getId(), giftCfg.getId()));
//		logCustomGift(player, giftCfg.getId());
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.CUSTOM_GIFT;
	}
	
//	/**
//	 * 私人定制礼包购买打点记录
//	 * 
//	 * @param payGifgId
//	 */
//	private void logCustomGift(Player player, String payGifgId) {
//		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.CUSTOM_MADE_GIFT_VALUE);
//		if (opActivity.isPresent()) {
//			CustomGiftActivity activity = (CustomGiftActivity) opActivity.get();
//			List<Integer> rewardIds = activity.getGiftRewardIds(player.getId(), payGifgId);
//			StringBuilder sb = new StringBuilder();
//			for (int rewardId : rewardIds) {
//				sb.append(rewardId).append(",");
//			}
//			
//			if (sb.indexOf(",") > 0) {
//				sb.deleteCharAt(sb.length() - 1);
//			}
//			LogUtil.logCustomGiftPurchase(player, payGifgId, sb.toString());
//		}
//	}

}
