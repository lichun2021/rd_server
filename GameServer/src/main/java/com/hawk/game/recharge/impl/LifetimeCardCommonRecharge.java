package com.hawk.game.recharge.impl;

import org.hawk.os.HawkTime;

import com.hawk.game.config.LifetimeCardCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.LifetimeCardEntity;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.module.PlayerLifetimeCardModule;
import com.hawk.game.module.agency.PlayerAgencyModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.util.GsConst;

/**
 * 终身卡直购
 * @author Golden
 *
 */
public class LifetimeCardCommonRecharge extends AbstractGiftRecharge {

	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		// 大本等级限制
		int unlockCityLevel = LifetimeCardCfg.getInstance().getUnlockCityLevel();
		if (player.getCityLevel() < unlockCityLevel) {
			return false;
		}
		// 终身卡已经解锁了
		LifetimeCardEntity lifetimeCardEntity = player.getData().getLifetimeCardEntity();
		if (lifetimeCardEntity.isCommonUnlock()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		LifetimeCardEntity lifetimeCardEntity = player.getData().getLifetimeCardEntity();
		lifetimeCardEntity.setCommonUnlockTime(HawkTime.getMillisecond());
		
		// 刷新终身卡作用号
		player.getEffect().resetLifeTimeCard(player);
		
		// 同步终身卡界面
		PlayerLifetimeCardModule module = player.getModule(GsConst.ModuleType.LIFETIME_CARD);
		module.syncLifetimeCardInfo();
		
		// 刷新军情任务
		PlayerAgencyModule agencyModule = player.getModule(GsConst.ModuleType.AGENCY_MODULE);
		agencyModule.addMission();
		agencyModule.pushPageInfo();
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.LIFETIME_COMMON_CARD;
	}

}
