package com.hawk.game.recharge.impl;

import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.militaryprepare.MilitaryPrepareActivity;
import com.hawk.activity.type.impl.militaryprepare.cfg.MilitaryPrepareActivityKVCfg;
import com.hawk.activity.type.impl.militaryprepare.entity.MilitaryPrepareEntity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 军事备战进阶礼包
 * 
 * @author lating
 *
 */
public class MilitaryPrepareGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		String giftId = giftCfg.getId();
		MilitaryPrepareActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MilitaryPrepareActivityKVCfg.class);
		if (cfg == null) {
			return false;
		}
		if(!giftId.equals(cfg.getIosAdvance()) &&
				!giftId.equals(cfg.getAndroidAdvance())){
			return false;
		}
		Optional<MilitaryPrepareActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.MILITARY_PREPARE_VALUE);
		if (!opActivity.isPresent()) {
			return false;
		}
		MilitaryPrepareActivity activity = opActivity.get();
		if (!activity.isOpening(player.getId())) {
			return false;
		}
		Optional<MilitaryPrepareEntity> opDataEntity = activity.getPlayerDataEntity(player.getId());
		if (!opDataEntity.isPresent()) {
			return false;
		}
		int advanced = opDataEntity.get().getAdvanced();
		if (advanced > 0) {
			return false;
		}
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.MILITARYPREPARE;
	}

}
