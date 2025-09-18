package com.hawk.game.recharge.impl;

import java.util.Optional;

import org.hawk.log.HawkLog;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.EquipBlackMarketBuyEvent;
import com.hawk.activity.type.impl.equipBlackMarket.EquipBlackMarketActivity;
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
 * 黑市装备礼包
 * 
 * @author lating
 *
 */
public class EquipBlackMarketGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		return equipBlackMarketGiftBuyCheck(player, giftCfg.getId(), protocol);
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new EquipBlackMarketBuyEvent(player.getId(), giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.EQUIP_BLACK_MARKET;
	}
	
	public boolean equipBlackMarketGiftBuyCheck(Player player, String giftId,int protoType){
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.EQUIP_BLACK_MARKET_VALUE);
		if (!opActivity.isPresent()) {
			player.sendError(protoType, Status.Error.ACTIVITY_NOT_OPEN_VALUE, 0);
			HawkLog.errPrintln("EquipBlackMarketActivity not exist, playerId: {}", player.getId());
			return false;
		}
		
		EquipBlackMarketActivity activity = (EquipBlackMarketActivity)opActivity.get();
		if (!activity.isOpening(player.getId())) {
			player.sendError(protoType, Status.Error.ACTIVITY_NOT_OPEN_VALUE, 0);
			HawkLog.errPrintln("EquipBlackMarketActivity not opening, playerId: {}", player.getId());
			return false;
		}
		
		if (!activity.canBuyGift(player.getId(), giftId)) {
			player.sendError(protoType, Status.Error.PAY_GOODS_CANNOT_SALE, 0);
			HawkLog.errPrintln("EquipBlackMarketActivity reward config error, playerId: {}", player.getId());
			return false;
		}
		
		return true;
	}

}
