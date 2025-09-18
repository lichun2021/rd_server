package com.hawk.game.recharge.impl;

import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.LoginFundBuyEvent;
import com.hawk.activity.type.impl.loginfundtwo.LoginFundTwoActivity;
import com.hawk.activity.type.impl.loginfundtwo.cfg.LoginFundActivityTwoKVCfg;
import com.hawk.activity.type.impl.loginfundtwo.entity.LoginFundTwoEntity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 登录基金礼包
 * 
 * @author lating
 *
 */
public class LoginFundGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		return loginFundBuyCheck(player, giftCfg.getId());
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new LoginFundBuyEvent(player.getId(), giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.LOGIN_FUND;
	}

	/**
	 * 购买登录基金礼包
	 * @return
	 */
	private boolean loginFundBuyCheck(Player player, String giftId) {
		LoginFundActivityTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginFundActivityTwoKVCfg.class);
		if (cfg == null) {
			HawkLog.errPrintln("PlayerRechargeModule LoginFundBuyCheck LoginFundActivityTwoKVCfg is error, playerId: {}, giftId:{}", player.getId(), giftId);
			return false;
		}
		if(!cfg.isCheckValid(giftId)){
			HawkLog.errPrintln("PlayerRechargeModule LoginFundBuyCheck error giftId is no valid, playerId: {}, giftId:{}", player.getId(), giftId);
			return false;
		}
		Optional<LoginFundTwoActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.LOGIN_FUND_TWO_VALUE);
		if (!opActivity.isPresent()) {
			HawkLog.errPrintln("PlayerRechargeModule LoginFundBuyCheck !opActivity.isPresent(), playerId: {}, giftId:{}", player.getId(), giftId);
			return false;
		}
		LoginFundTwoActivity activity = opActivity.get();
		if (!activity.isOpening(player.getId())) {
			HawkLog.errPrintln("PlayerRechargeModule LoginFundBuyCheck activity is not opening, playerId: {}, giftId:{}", player.getId(), giftId);
			return false;
		}
		Optional<LoginFundTwoEntity> opDataEntity = activity.getPlayerDataEntity(player.getId());
		if (!opDataEntity.isPresent()) {
			HawkLog.errPrintln("PlayerRechargeModule LoginFundBuyCheckLogin FundTwoEntity is not exist, playerId: {}, giftId:{}", player.getId(), giftId);
			return false;
		}
		int type = cfg.getBuyType(giftId);
		//vip等级限制
		int vipLevel = player.getVipLevel();
		if (vipLevel < cfg.getLimitVipLevelByType(type)) {
			HawkLog.errPrintln("PlayerRechargeModule LoginFundBuyCheck vipLevel is limit, playerId: {}, giftId:{}, vipLv:{}", player.getId(), giftId, vipLevel);
			return false;
		}
		//主堡等级限制
		int facLv = player.getData().getConstructionFactoryLevel();
		if (facLv < cfg.getBuyCityLimitByType(type)) {
			HawkLog.errPrintln("PlayerRechargeModule LoginFundBuyCheck facLv is limit, playerId: {}, giftId:{}, facLv:{}", player.getId(), giftId, facLv);
			return false;
		}
		int buyType = cfg.getBuyType(giftId);
		boolean isBuy = opDataEntity.get().isHasBuy(buyType);
		if (isBuy) {
			HawkLog.errPrintln("PlayerRechargeModule LoginFundBuyCheck error giftId is has buy, playerId:{},giftId:{},buyType:{}", player.getId(), giftId, buyType);
			return false;
		}
		return true;
	}
	
}
