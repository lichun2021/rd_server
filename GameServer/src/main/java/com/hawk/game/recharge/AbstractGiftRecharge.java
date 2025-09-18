package com.hawk.game.recharge;

import java.util.Set;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.impl.RechargeAllRmbEvent;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.config.ServerAwardCfg;
import com.hawk.game.entity.RechargeDailyEntity;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Recharge.RechargeBuyItemCode;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GsConst.DailyInfoField;
import com.hawk.game.util.GsConst.DiamondPresentReason;
import com.hawk.game.recharge.RechargeType;
import com.hawk.log.Action;

public abstract class AbstractGiftRecharge {
	/**
	 * 礼包购买发货，所有礼包通用的
	 * 
	 * @param player
	 * @param giftCfg
	 * @param rechargeEntity
	 */
	public void deliverGoodsPub(Player player, PayGiftCfg giftCfg, RechargeDailyEntity rechargeEntity) {
		if (giftCfg.isMonthCard()) {
			return;
		}
		
		int RMBToDaimonds = giftCfg.getPayRMB()/10;  // getPayRMB单位是分，除以100就是元，再乘以10就是等额钻石数，合并一下就是除以10
		// 添加道具直购的付费统计
		RedisProxy.getInstance().incServerDailyInfo(DailyInfoField.DAY_PAYITEM, RMBToDaimonds);
		RedisProxy.getInstance().incGlobalStatInfo(DailyInfoField.DAY_PAYITEM, RMBToDaimonds);
		
		// 钻石奖励不再走AwardItems体系，单独添加
		if (giftCfg.getGainDia() > 0) {
			// 赠送原因 recharge_activity
			player.increaseDiamond(giftCfg.getGainDia(), Action.RECHARGE_BUY_GIFT, null, DiamondPresentReason.RECHATGE);
		}
		
		try {
			// 发货
			ServerAwardCfg cfg = AssembleDataManager.getInstance().getServerAwardByAwardId(giftCfg.getServerAwardId());
			if (cfg != null) {
				AwardItems awardItems = cfg.getAwardItems();
				awardItems.rewardTakeAffectAndPush(player, Action.RECHARGE_BUY_GIFT, RewardOrginType.SHOPPING_GIFT);
				// 存储发货内容
				rechargeEntity.setAwardItems(cfg.getReward());
				
				// 发送邮件---礼包购买后发放的邮件
				MailId mid = MailId.PAY;
				if(giftCfg.getGiftType() == RechargeType.EQUIP_BLACK_MARKET){
					mid = MailId.EQUIP_BLACK_MARKET_PACKAGE;
				}
				if (giftCfg.getGiftType() == RechargeType.LIFETIME_COMMON_CARD){
					mid = MailId.LIFE_TIME_CARD_COMMON;
				}
				if (giftCfg.getGiftType() == RechargeType.LIFETIME_ADVANCED_CARD){
					mid = MailId.LIFE_TIME_CARD_ADVANCE;
				}
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(mid)
						.addSubTitles(giftCfg.getNameData())
						.addContents(giftCfg.getNameData(), cfg.getReward())
						.setRewards(cfg.getReward())
						.build());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		//直购礼包充值事件
		ActivityManager.getInstance().postEvent(new PayGiftBuyEvent(player.getId(), giftCfg.getId(), giftCfg.getPayRMB()/100,giftCfg.getPayRMB()/10));
		ActivityManager.getInstance().postEvent(new RechargeAllRmbEvent(player.getId(), giftCfg.getPayRMB()/100));
	}
	
	/**
	 * 购买条件判断（所有礼包通用）
	 * @return
	 */
	public boolean giftBuyCheckPub(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Set<String> goodsIds = RedisProxy.getInstance().getAllUnfinishedRechargeGoods(player.getId());
		if (goodsIds.contains(giftCfg.getId())) {
			if (req.hasResultCode() && req.getResultCode() == RechargeBuyItemCode.PAYRESULT_SUCC_VALUE) {
				player.sendError(protocol, Status.Error.PAY_GIFT_LAST_UNFINISH, 0);
				HawkLog.errPrintln("MSDK buy item failed, callback unreached, playerId: {}, giftId: {}",  player.getId(), giftCfg.getId());
				return false;
			}
			
			HawkLog.logPrintln("MSDK buy item repeated, playerId: {}, giftId: {}, resultCode: {}",  player.getId(), giftCfg.getId(), req.hasResultCode() ? req.getResultCode() : -100001);
			RedisProxy.getInstance().removeUnfinishedRechargeGoods(player.getId(), giftCfg.getId());
		}
		
		if (giftCfg.isMonthCard()) {
			return true;
		}
		
		// 礼包购买次数已满
		int rechargeTimesToday = player.getData().getRechargeTimesToday(RechargeType.GIFT, giftCfg.getId());
		if (giftCfg.getPayCount() > 0 && rechargeTimesToday >= giftCfg.getPayCount()) {
			player.sendError(protocol, Status.Error.PAY_GIFT_BUY_FULL_TODAY, 0);
			HawkLog.errPrintln("MSDK buy item failed, pay gift full today, playerId: {}, openId: {}, giftId: {}",  player.getId(), player.getOpenId(), giftCfg.getId());
			return false;
		}
		
		return true;
	}
	
	/**
	 * 购买条件判断（具体的礼包）
	 * @return
	 */
	public abstract boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol);
	
	/**
	 * 礼包购买发货（具体的礼包）
	 * 
	 * @return
	 */
	public abstract boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity);
	
	/**
	 * 礼包类型
	 * @return
	 */
	public abstract int getGiftType();
}
