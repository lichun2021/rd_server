package com.hawk.game.recharge.impl;

import java.util.Optional;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BuyMonthCardEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.impl.RechargeAllRmbEvent;
import com.hawk.activity.type.impl.monthcard.MonthCardActivity;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityCfg;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeDailyEntity;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.msg.BuyMonthCardMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Recharge.RechargeBuyItemCode;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.DailyInfoField;
import com.hawk.game.recharge.RechargeType;
import com.hawk.log.Action;

public class MonthCardRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		boolean paySuccess = req.hasResultCode() && req.getResultCode() == RechargeBuyItemCode.PAYRESULT_SUCC_VALUE;
		return buyMonthCard(player, giftCfg, paySuccess, protocol);
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		int RMBToDaimonds = giftCfg.getPayRMB()/10;  // getPayRMB单位是分，除以100就是元，再乘以10就是等额钻石数，合并一下就是除以10
		// 添加月卡的付费统计
		RedisProxy.getInstance().incServerDailyInfo(DailyInfoField.DAY_MONTHCARD, RMBToDaimonds);
		RedisProxy.getInstance().incGlobalStatInfo(DailyInfoField.DAY_MONTHCARD, RMBToDaimonds);
		
		ActivityManager.getInstance().postEvent(new BuyMonthCardEvent(player.getId(), giftCfg.getMonthCardType(), giftCfg.getPayRMB() / 100));
		HawkApp.getInstance().postMsg(player.getXid(), BuyMonthCardMsg.valueOf(giftCfg.getMonthCardType()));
		//直购礼包充值事件
		ActivityManager.getInstance().postEvent(new PayGiftBuyEvent(player.getId(), giftCfg.getId(),giftCfg.getPayRMB()/100, giftCfg.getPayRMB()/10));
		ActivityManager.getInstance().postEvent(new RechargeAllRmbEvent(player.getId(), giftCfg.getPayRMB()/100));
		rechargeEntity.setAwardItems("monthcard-" + giftCfg.getMonthCardType());
		RechargeDailyEntity entity = player.getData().getPlayerRechargeDailyEntity(rechargeEntity.getBillno());
		if (entity != null) {
			entity.setAwardItems(rechargeEntity.getAwardItems());
		}
		
		if (giftCfg.getMonthCardType() == ConstProperty.getInstance().getGoldPrivilegeType()) {
			try {
				ConsumeItems consume = ConsumeItems.valueOf();
				consume.addItemConsume(ConstProperty.getInstance().getGoldPrivilegeDiscountItem(), 1);
				if (consume.checkConsume(player)) {
					consume.consumeAndPush(player, Action.MONTH_CARD_PRICE_CUT);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.MONTH_CARD;
	}

	private boolean buyMonthCard(Player player, PayGiftCfg giftCfg, boolean paySuccess, int protocol) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.MONTHCARD_VALUE);
		MonthCardActivity activity = (MonthCardActivity) opActivity.get();
		int result = activity.buyMonthCardCheck(player.getId(), giftCfg.getMonthCardType(), paySuccess, giftCfg.getId());
		if (result != 0) {
			player.sendError(protocol, result, 0);
			return false;
		}
		return true;
	}
	
	protected boolean buyMonthCardOld(Player player, PayGiftCfg giftCfg, boolean paySuccess, int protocol) {
		int type = giftCfg.getMonthCardType();
		boolean isSell = MonthCardActivityCfg.inSell(type, HawkTime.getMillisecond());
		if(!isSell){
			player.sendError(protocol, Status.Error.MONTHCARD_NOT_IN_SELL, 0);
			HawkLog.errPrintln("MSDK buy item failed, the type monthCard not in sell, playerId: {}, type: {}", player.getId(), giftCfg.getMonthCardType());
			return false;
		}
		
		int frontBuildId = MonthCardActivityCfg.getFrontBuildId(type);
		if (frontBuildId > 0) {
			BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, frontBuildId);
			int maxCfgId = player.getData().getMaxLevelBuildingCfg(cfg != null ? cfg.getBuildType() : 0);
			if (maxCfgId < frontBuildId) {
				HawkLog.errPrintln("MSDK buy item failed, monthCard condition not match, playerId: {}, type: {}", player.getId(), giftCfg.getMonthCardType());
				return false;
			}
		}
		
		ActivityMonthCardEntity entity = GameUtil.getMonthCardEntity(player.getId());
		// 月卡生效期间无须购买再购买同类月卡
		if (entity != null && !entity.getEfficientCardList(giftCfg.getMonthCardType()).isEmpty()) {
			HawkLog.errPrintln("MSDK buy item failed, the type monthCard has already bought, playerId: {}, type: {}", player.getId(), giftCfg.getMonthCardType());
			return false;
		}

		// 半价
		if (type == ConstProperty.getInstance().getGoldPrivilegeType()) {
			int itemCount = player.getData().getItemNumByItemId(ConstProperty.getInstance().getGoldPrivilegeDiscountItem());
			String androidPriceCutId = ConstProperty.getInstance().getGoldPrivilegePayGiftIdAndroid();
			String iosPriceCutId = ConstProperty.getInstance().getGoldPrivilegePayGiftIdIos();
			if (itemCount > 0 && !giftCfg.getId().equals(androidPriceCutId) && !giftCfg.getId().equals(iosPriceCutId)) {
				String cutPriceGiftId = !player.getPlatform().equalsIgnoreCase("ios") ? androidPriceCutId : iosPriceCutId;
				PayGiftCfg tmpCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, cutPriceGiftId);
				giftCfg = tmpCfg == null ? giftCfg : tmpCfg;
			}
			
			Set<String> goodsIds = RedisProxy.getInstance().getAllUnfinishedRechargeGoods(player.getId());
			if (goodsIds.contains(giftCfg.getId())) {
				if (paySuccess) {
					player.sendError(protocol, Status.Error.PAY_GIFT_LAST_UNFINISH, 0);
					HawkLog.errPrintln("MSDK buy item failed, callback unreached, playerId: {}, giftId: {}",  player.getId(), giftCfg.getId());
					return false;
				}
				
				RedisProxy.getInstance().removeUnfinishedRechargeGoods(player.getId(), giftCfg.getId());
			}
		}
		
		//定制类特权卡，单独判断
		if (MonthCardActivityCfg.isCustomTypeCard(type)) {
			Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.MONTHCARD_VALUE);
			MonthCardActivity monthCardActivity = (MonthCardActivity) opActivity.get();
			int result = monthCardActivity.canPurchaseCustomCard(player.getId(), type);
			if (result != 0) {
				player.sendError(protocol, result, 0);
				return false;
			}
		}
		
		return true;
	}
	
}
