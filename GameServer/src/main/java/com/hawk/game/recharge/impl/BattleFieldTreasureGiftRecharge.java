package com.hawk.game.recharge.impl;

import java.util.Optional;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BattleFieldPurchaseEvent;
import com.hawk.activity.type.impl.battlefield.BattleFieldActivity;
import com.hawk.activity.type.impl.battlefield.entity.BattleFieldEntity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 战地寻宝直购礼包
 * 
 * @author lating
 *
 */
public class BattleFieldTreasureGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<BattleFieldActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.BATTLE_FIELD_TREASURE_VALUE);
		if (!opActivity.isPresent()) {
			return false;
		}
		
		BattleFieldActivity activity = opActivity.get();
		if (!activity.isOpening(player.getId())) {
			return false;
		}
		
		Optional<BattleFieldEntity> opDataEntity = activity.getPlayerDataEntity(player.getId());
		if (!opDataEntity.isPresent()) {
			return false;
		}
		
		// 已购买生效
		if (opDataEntity.get().getBuyTime() > 0) {
			return false;
		}
		
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new BattleFieldPurchaseEvent(player.getId(), giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.BATTLE_FIELD_TREASURE;
	}

}
