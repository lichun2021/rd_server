package com.hawk.game.recharge.impl;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BuyItemConsumeEvent;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.module.PlayerDailyGiftBuyModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.util.GsConst;

/**
 * 每日必买礼包
 * 
 * @author lating
 *
 */
public class DailyGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		return daiyGiftMustBuyCheck(player, giftCfg.getId(), protocol);
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new BuyItemConsumeEvent(player.getId(), giftCfg.getId(), giftCfg.getGainDia() / 10));
		PlayerDailyGiftBuyModule module = player.getModule(GsConst.ModuleType.DAILY_GIFT_BUY_MOUDLE);
		module.onBuyGift(giftCfg);
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.GIFT;
	}
	
	private boolean daiyGiftMustBuyCheck(Player player, String giftId, int protoType) {
		final String android210001 = "210001";
		final String android210003 = "210003";
		final String android210006 = "210006";
		final String android210012 = "210012";
		final String android210030 = "210030";
		final String android210045 = "210045";
		final boolean isAndroid = giftId.equals(android210001) || giftId.equals(android210003)||
				giftId.equals(android210006)||giftId.equals(android210012)||
					giftId.equals(android210030)||giftId.equals(android210045);
		final String ios220001 = "220001";
		final String ios220003 = "220003";
		final String ios220006 = "220006";
		final String ios220012 = "220012";
		final String ios220030 = "220030";
		final String ios220045 = "220045";
		final boolean isIos = giftId.equals(ios220001) || giftId.equals(ios220003)||
				giftId.equals(ios220006)||giftId.equals(ios220012)||
					giftId.equals(ios220030)||giftId.equals(ios220045);
		if(isAndroid){
			int rechargeTimesToday1 = player.getData().getRechargeTimesToday(RechargeType.GIFT, android210001);
			int rechargeTimesToday3 = player.getData().getRechargeTimesToday(RechargeType.GIFT, android210003);
			int rechargeTimesToday6 = player.getData().getRechargeTimesToday(RechargeType.GIFT, android210006);
			int rechargeTimesToday12 = player.getData().getRechargeTimesToday(RechargeType.GIFT, android210012);
			int rechargeTimesToday30 = player.getData().getRechargeTimesToday(RechargeType.GIFT, android210030);
			int rechargeTimesToday45 = player.getData().getRechargeTimesToday(RechargeType.GIFT, android210045);
			if(giftId.equals(android210045) && (rechargeTimesToday1 + 
					rechargeTimesToday3 + rechargeTimesToday6 + rechargeTimesToday12+rechargeTimesToday30 ) == 0){
				return true;
			}
			if((giftId.equals(android210001) || giftId.equals(android210003) || 
					giftId.equals(android210006)||giftId.equals(android210012) || giftId.equals(android210030)) &&  
					rechargeTimesToday45 == 0){
				return true;
			}
		}
		if(isIos){
			int rechargeTimesToday1 = player.getData().getRechargeTimesToday(RechargeType.GIFT, ios220001);
			int rechargeTimesToday3 = player.getData().getRechargeTimesToday(RechargeType.GIFT, ios220003);
			int rechargeTimesToday6 = player.getData().getRechargeTimesToday(RechargeType.GIFT, ios220006);
			int rechargeTimesToday12 = player.getData().getRechargeTimesToday(RechargeType.GIFT,ios220012);
			int rechargeTimesToday30 = player.getData().getRechargeTimesToday(RechargeType.GIFT, ios220030);
			int rechargeTimesToday45 = player.getData().getRechargeTimesToday(RechargeType.GIFT, ios220045);
			if(giftId.equals(ios220045) && (rechargeTimesToday1 + 
					rechargeTimesToday3 + rechargeTimesToday6 + rechargeTimesToday12+rechargeTimesToday30 ) == 0){
				return true;
			}
			if((giftId.equals(ios220001) || giftId.equals(ios220003) || 
					giftId.equals(ios220006)||giftId.equals(ios220012) || giftId.equals(ios220030)) &&  
					rechargeTimesToday45 == 0){
				return true;
			}
		}
		return false;
	}

}
