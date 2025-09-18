package com.hawk.game.recharge.impl;

import com.hawk.game.GsApp;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.module.dayazhizhan.playerteam.msg.DYZZSeasonOrderAdvanceBuyMsg;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 达雅之战赛季战力进阶直购
 * 
 * @author lating
 *
 */
public class DyzzOrderGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		return DYZZSeasonService.getInstance().canBuyDYZZSeasonOrderAdvacne(player);
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		GsApp.getInstance().postMsg(player, new DYZZSeasonOrderAdvanceBuyMsg(giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.DYZZ_SEASON_ORDER_ADVANCE;
	}

}
