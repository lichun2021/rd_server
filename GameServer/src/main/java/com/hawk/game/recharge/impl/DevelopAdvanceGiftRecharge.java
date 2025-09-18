package com.hawk.game.recharge.impl;

import java.util.Optional;

import org.hawk.log.HawkLog;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.DevelopSpurtAdvancedUnlockEvent;
import com.hawk.activity.type.impl.backFlow.developSput.DevelopSpurtActivity;
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
 * 发展冲刺进阶礼包
 * 
 * @author lating
 *
 */
public class DevelopAdvanceGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.DEVELOP_SPURT_VALUE);
		if (!opActivity.isPresent()) {
			player.sendError(protocol, Status.Error.ACTIVITY_NOT_OPEN_VALUE, 0);
			HawkLog.errPrintln("developSpurtActivity not exist, playerId: {}", player.getId());
			return false;
		}
		
		DevelopSpurtActivity activity = (DevelopSpurtActivity) opActivity.get();
		int result = activity.checkAdvancedAwardUnlockedStatus(player.getId(), giftCfg.getId());
		if (result != 0) {
			player.sendError(protocol, result, 0);
			HawkLog.errPrintln("developSpurtActivity anAdvancedCheck failed, playerId: {}, result: {}", player.getId(), result);
			return false;
		}
		
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new DevelopSpurtAdvancedUnlockEvent(player.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.DEVELOP_SPURT_ADVANCED;
	}

}
