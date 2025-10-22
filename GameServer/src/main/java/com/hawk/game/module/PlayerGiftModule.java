package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.hawk.game.config.*;
import com.hawk.game.protocol.Const;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.GiftPurchasePriceEvent;
import com.hawk.activity.event.impl.PackageBuyEvent;
import com.hawk.activity.type.impl.shareGlory.ShareGloryActivity;
import com.hawk.game.GsApp;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.PlayerGiftEntity;
import com.hawk.game.entity.PlayerResourceGiftEntity;
import com.hawk.game.entity.item.GiftAdviceItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.msg.BuildingLevelUpMsg;
import com.hawk.game.msg.CommanderLevlUpMsg;
import com.hawk.game.msg.CrossDayBeforeZeroMsg;
import com.hawk.game.msg.MarchEmoticonBagUnlockMsg;
import com.hawk.game.msg.SecondBuildGiftBuyMsg;
import com.hawk.game.msg.SuperGiftDirectBuyMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.vipsuper.PlayerSuperVipInfo;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Item.GiftRecommend;
import com.hawk.game.protocol.Item.GiftRecommendInfo;
import com.hawk.game.protocol.Item.HPBuyGiftReq;
import com.hawk.game.protocol.Item.HPBuyGiftResp;
import com.hawk.game.protocol.Item.HPSyncGiftInfoResp;
import com.hawk.game.protocol.Item.MilitaryShopBuyReq;
import com.hawk.game.protocol.Item.MilitaryShopInfo;
import com.hawk.game.protocol.Item.MilitaryShopItem;
import com.hawk.game.protocol.Item.RecieveSueprVipGift;
import com.hawk.game.protocol.Item.RecieveSueprVipScoreReq;
import com.hawk.game.protocol.Item.ResourceGiftBuyC;
import com.hawk.game.protocol.Item.ResourceGiftBuyS;
import com.hawk.game.protocol.Item.ResourceGiftMsg;
import com.hawk.game.protocol.Item.ResourceGiftSyn;
import com.hawk.game.protocol.Item.SkinEffActivateReq;
import com.hawk.game.protocol.Item.SuperVipActiveReq;
import com.hawk.game.protocol.Item.TakeBenefitBoxReq;
import com.hawk.game.protocol.Item.VipExclusiveBoxBuyReq;
import com.hawk.game.protocol.Item.VipShopBuyReq;
import com.hawk.game.protocol.Item.VipShopItem;
import com.hawk.game.protocol.Item.VipShopItemInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.PlayerFlagPosition;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.TavernService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.GsConst.VipRelatedDateType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MapUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GiftType;
import com.hawk.sdk.msdk.entity.PayItemInfo;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 礼包模块（超值礼包、vip贵族商城礼包）
 *
 * @author lating
 */
public class PlayerGiftModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * vip商城商品id
	 */
	private List<Integer> vipShopIds;
	/**
	 * vip商城刷新时间
	 */
	private long vipShopRefreshTime;
	/**
	 * tick间隔
	 */
	private int tickInterval = 3000;
	/**
	 * 上一次的tick时间
	 */
	private long lastTickTime = 0;

	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerGiftModule(Player player) {
		super(player);
	}

	/**
	 * 更新
	 *
	 * @return
	 */
	@Override
	public boolean onTick() {
		long curTime = HawkApp.getInstance().getCurrentTime();
		if (curTime < lastTickTime + tickInterval) {
			return true;
		}
		this.lastTickTime = curTime;
		try {
			onSuperGiftTick();
		} catch (Exception e) {
			HawkException.catchException(e, "tick superGift exception");
		}
		
		try {
			onResourceGiftTick(false);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 判断至尊vip激活周期跨自然月, 或跨天
		if (player.getSuperVipObject().checkSuperVipCrossMonth() || player.getSuperVipObject().checkCrossDay(curTime)) {
			player.getSuperVipObject().checkMissedSuperVipDailyGift();
			player.getSuperVipObject().syncSuperVipInfo(false);
		}
		
		try {
			// 检测荣耀礼包清除
			if (!isShareGloryActivityOpen()) {
				clearShareGloryGift();
				clearShareGloryGiftBuyNum();
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}	

	@Override
	protected boolean onPlayerAssemble() {
		try {
            fixSecondBuildGiftData();
			assembleSuperGift();
		} catch (Exception e) {
			HawkException.catchException(e, "assemble super gift");
		}		
		assembleResGift();
		
		return true;
	}
	
	/**
	 * 玩家登陆处理(数据同步)
	 */
	@Override
	protected boolean onPlayerLogin() {
		boolean isNewly = false;
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
		if (accountInfo !=null && accountInfo.isNewly()) {
			isNewly = true;
		}
		
		vipShopIds = new ArrayList<>();
		syncVipShopItems(isNewly);
		syncGiftInfo();
		checkMissedVipBenefitBox(isNewly);
		player.getPush().syncAllVipBoxStatus(true, isNewly);
		this.synResGiftInfo();
		this.syncMilitayShopItems(true, isNewly);
		this.syncGiftRecommendInfo();
		// 至尊vip信息
		player.getSuperVipObject().superVipLevelUp(); // 1. 功能上线后，玩家首次登录，满足条件自动升级；2. 如果存在数据问题，也可用作登录自动修复
		player.getSuperVipObject().syncSuperVipInfo(true);
		player.getSuperVipObject().checkMissedSuperVipDailyGift();
		clearMonthGift();

		return true;
	}
	
	/**
	 * 修复第二城建队列礼包数据
	 */
	private void fixSecondBuildGiftData() {
		try {
			CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.SECOND_BUILD_OPEN_KEY);
			//还没有购买过第二城建队列礼包
			if (customData == null || customData.getValue() != 1) {
				return;
			}
			
			int giftId = 666551;
			PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
			Map<Integer, Integer> map = giftEntity.getBuyNumsMap();
			if (map != null && !map.containsKey(giftId)) {
				giftEntity.addBuyNum(giftId, 1);
				HawkLog.logPrintln("playerGiftModule fix secondBuild giftData, playerId: {}", player.getId());
			} 
		} catch (Exception e) {
			HawkException.catchException(e, player.getId());
		}
	}
	
	/**
	 * 激活皮肤特效
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.SUPER_VIP_EFF_ACTIVATE_VALUE)
	private boolean onSkinEffActivate(HawkProtocol protocol) {
		if (!player.getSuperVipObject().isSuperVipOpen()) {
			sendError(protocol.getType(), Status.Error.SUPER_VIP_NO_TOUCH);
			return false;
		}
		
		if (player.getSuperVipObject().checkSuperVipCrossMonth()) {
			HawkLog.logPrintln("onSkinEffActivate check super vip cross month, playerId: {}", player.getId());
			player.getSuperVipObject().syncSuperVipInfo(false);
			return false;
		}
		
		if (!player.getSuperVipObject().isSuperVipActivated()) {
			sendError(protocol.getType(), Status.Error.SUPER_VIP_NOT_ACTIVATED);
			player.getSuperVipObject().syncSuperVipInfo(false);
			HawkLog.logPrintln("onSkinEffActivate failed, super vip not activated, playerId: {}", player.getId());
			return false;
		}
		
		long timeNow = HawkTime.getMillisecond();
		PlayerSuperVipInfo superVipInfo = player.getSuperVipObject().getSuperVipInfo();
		long lastTime = player.getSuperVipObject().getLastSkinActivateClickTime();
		if (timeNow - lastTime < 5000) {
			sendError(protocol.getType(), Status.Error.SUPER_VIP_EFF_ACTIVATE_FREQUENCY);
			return false;
		}
		
		SkinEffActivateReq req = protocol.parseProtocol(SkinEffActivateReq.getDefaultInstance());
		if (req.getLevel() < 0 || req.getLevel() > superVipInfo.getActualLevel()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		player.getSuperVipObject().setLastSkinActivateClickTime(timeNow);
		
		int oldLevel = superVipInfo.getSkinEffActivated();
		if (req.getLevel() != oldLevel) {
			superVipInfo.setSkinEffActivated(req.getLevel()); // >0 表示激活，0表示取消激活
			player.getSuperVipObject().updateSuperVipInfo();
		}
		player.getSuperVipObject().syncSuperVipInfo(false);
		
		HawkLog.logPrintln("SkinEffActivate success, playerId: {}, activated level: {}, oldLevel: {}, newLevel: {}", player.getId(), superVipInfo.getActivatedLevel(), oldLevel, superVipInfo.getSkinEffActivated());
		return true;
	}
	
	/**
	 * 领取至尊vip礼包
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.SUPER_VIP_GIFT_RECIEVE_VALUE)
	private boolean onSuperVipGiftReq(HawkProtocol protocol) {
		if (!player.getSuperVipObject().isSuperVipOpen()) {
			sendError(protocol.getType(), Status.Error.SUPER_VIP_NO_TOUCH);
			return false;
		}
		
		if (player.getSuperVipObject().checkSuperVipCrossMonth()) {
			HawkLog.logPrintln("onSuperVipGiftReq check super vip cross month, playerId: {}", player.getId());
			player.getSuperVipObject().syncSuperVipInfo(false);
			return false;
		}
		
		if (!player.getSuperVipObject().isSuperVipActivated()) {
			sendError(protocol.getType(), Status.Error.SUPER_VIP_NOT_ACTIVATED);
			player.getSuperVipObject().syncSuperVipInfo(false);
			HawkLog.logPrintln("onSuperVipGiftReq failed, not activated, playerId: {}", player.getId());
			return false;
		}
		
		PlayerSuperVipInfo superVipInfo = player.getSuperVipObject().getSuperVipInfo();
		RecieveSueprVipGift req = protocol.parseProtocol(RecieveSueprVipGift.getDefaultInstance());
		int giftType = req.getGiftType();
		// 月度礼包
		if (giftType == 0) {
			if (!superVipInfo.getMonthGiftRecieved().isEmpty()) {
				sendError(protocol.getType(), Status.Error.SUPER_VIP_MONTH_GIFT_RECIEVED);
				HawkLog.logPrintln("player recieve super vip month gift repeated, playerId: {}, super vipLevel: {}", player.getId(), superVipInfo.getActivatedLevel());
				return false;
			}
			
			VipSuperCfg cfg = HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, superVipInfo.getActivatedLevel());
			superVipInfo.getMonthGiftRecieved().add(superVipInfo.getActivatedLevel());
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItemInfos(cfg.getMonthGiftItems());
			awardItems.rewardTakeAffectAndPush(player, Action.SUPER_VIP_MONTH_GIFT_AWARD, true);
			player.getSuperVipObject().updateSuperVipInfo();
			player.getSuperVipObject().syncSuperVipInfo(false);
			HawkLog.logPrintln("player recieve super vip month gift, playerId: {}, super vipLevel: {}", player.getId(), superVipInfo.getActivatedLevel());
			return true;
		}
		
		long timeNow = HawkTime.getMillisecond();
		// 每日礼包
		if (!HawkTime.isCrossDay(timeNow, superVipInfo.getDailyGiftRecieveTime(), 0)) {
			sendError(protocol.getType(), Status.Error.SUPER_VIP_DAILY_GIFT_ERROR);
			HawkLog.logPrintln("player recieve super vip daily gift failed, time error, playerId: {}, activated level: {}", player.getId(), superVipInfo.getActivatedLevel());
			return false;
		}
		
		VipSuperCfg cfg = HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, superVipInfo.getActivatedLevel());
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(cfg.getVipBenefitItems());
		awardItems.rewardTakeAffectAndPush(player, Action.SUPER_VIP_DAILY_GIFT_AWARD, true);
		
		superVipInfo.setDailyGiftRecieveTime(timeNow);
		player.getSuperVipObject().updateSuperVipInfo();
		player.getSuperVipObject().syncSuperVipInfo(false);
		
		HawkLog.logPrintln("player recieve super vip daily gift, playerId: {}, activated level: {}", player.getId(), superVipInfo.getActivatedLevel());
		return true;
	}
	
	/**
	 * 领取至尊vip奖励积分
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.SUPER_VIP_SCORE_RECEIVE_VALUE)
	private boolean onSuperVipScoreReq(HawkProtocol protocol) {
		if (!player.getSuperVipObject().isSuperVipOpen()) {
			sendError(protocol.getType(), Status.Error.SUPER_VIP_NO_TOUCH);
			return false;
		}
		
		long timeNow = HawkTime.getMillisecond();
		if (player.getSuperVipObject().checkSuperVipCrossMonth() || player.getSuperVipObject().checkCrossDay(timeNow)) {
			HawkLog.logPrintln("onSuperVipScoreReq check super vip cross month, playerId: {}", player.getId());
			player.getSuperVipObject().syncSuperVipInfo(false);
			return false;
		}
		
		PlayerSuperVipInfo superVipInfo = player.getSuperVipObject().getSuperVipInfo();
		// 改版后不管是否激活，都可以领取每日积分 
//		if (!player.getSuperVipObject().isSuperVipActivated()) {
//			sendError(protocol.getType(), Status.Error.SUPER_VIP_NOT_ACTIVATED);
//			player.getSuperVipObject().syncSuperVipInfo(false);
//			HawkLog.logPrintln("onSuperVipGiftReq failed, not activated, playerId: {}", player.getId());
//			return false;
//		}
		
		RecieveSueprVipScoreReq req = protocol.parseProtocol(RecieveSueprVipScoreReq.getDefaultInstance());
		int scoreType = req.getScoreType();
		// 登录奖励积分
		if (scoreType == 0) {
			int crossDay = HawkTime.getCrossDay(timeNow, superVipInfo.getDailyLoginRecieveTime(), 0);
			if (crossDay == 0) {
				sendError(protocol.getType(), Status.Error.SUPER_VIP_DAILY_SCORE_ERROR);
				HawkLog.logPrintln("player recieve super vip login score failed, time error, playerId: {}, super vipLevel: {}, activated level: {}", player.getId(), superVipInfo.getActualLevel(), superVipInfo.getActivatedLevel());
				return false;
			}
			
			int loginScore = superVipInfo.getLastLoginScore();
			if (loginScore <= 0) {
				loginScore = ConstProperty.getInstance().getSuperVipDailyLoginScore();
				superVipInfo.setLastLoginScore(loginScore);
			}
			
			superVipInfo.setDailyLoginRecieveTime(timeNow);
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addVipExp(loginScore);
			awardItems.rewardTakeAffectAndPush(player, Action.SUPER_VIP_LOGIN_AWARD);
			
			HawkLog.logPrintln("player recieve super vip login score, playerId: {}, super vipLevel: {}, activated level: {}, loginScore: {}", player.getId(), superVipInfo.getActualLevel(), superVipInfo.getActivatedLevel(), loginScore);
			return true;
		} 
		
		// 活跃奖励积分
		if (!HawkTime.isCrossDay(timeNow, superVipInfo.getDailyActiveRecieveTime(), 0)) {
			sendError(protocol.getType(), Status.Error.SUPER_VIP_DAILY_SCORE_ERROR);
			HawkLog.logPrintln("player recieve super vip active score failed, time error, playerId: {}, super vipLevel: {}, activated level: {}", player.getId(), superVipInfo.getActualLevel(), superVipInfo.getActivatedLevel());
			return false;
		}
		
		int toatalActiveScore = TavernService.getInstance().getTotalScore(player);
		int scoreLimit = ConstProperty.getInstance().getIntegral();
		if (toatalActiveScore < scoreLimit) {
			sendError(protocol.getType(), Status.Error.SUPER_VIP_DAILY_ACTIVE_LIMIT);
			HawkLog.logPrintln("player recieve super vip active score failed, playerId: {}, super vipLevel: {}, activated level: {}, toatalActiveScore: {}", player.getId(), superVipInfo.getActualLevel(), superVipInfo.getActivatedLevel(), toatalActiveScore);
			return false;
		}
		
		superVipInfo.setDailyActiveRecieveTime(timeNow);
		int addScore = ConstProperty.getInstance().getRewardExperience();
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addVipExp(addScore);
		awardItems.rewardTakeAffectAndPush(player, Action.SUPER_VIP_ACTIVE_AWARD);
		
		HawkLog.logPrintln("player recieve super vip active score, playerId: {}, super vipLevel: {}, activated level: {}", player.getId(), superVipInfo.getActualLevel(), superVipInfo.getActivatedLevel());
		return true;
	}
	
	/**
	 * 手动激活至尊vip
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.SUPER_VIP_ACTIVE_C_VALUE)
	private boolean onSuperVipActiveReq(HawkProtocol protocol) {
		// 未达到至尊vip阶段
		if (!player.getSuperVipObject().isSuperVipOpen()) {
			sendError(protocol.getType(), Status.Error.SUPER_VIP_NO_TOUCH);
			return false;
		}
		
		long timeNow = HawkTime.getMillisecond();
		if (player.getSuperVipObject().checkSuperVipCrossMonth() || player.getSuperVipObject().checkCrossDay(timeNow)) {
			HawkLog.logPrintln("onSuperVipActiveReq check super vip cross month, playerId: {}", player.getId());
			player.getSuperVipObject().syncSuperVipInfo(false);
			return false;
		}
		
		// 自动激活的，当月不需要再手动激活了
		if (player.getSuperVipObject().isAutoActivated()) {
			sendError(protocol.getType(), Status.Error.SUPER_VIP_AUTO_ACTIVATED);
			return false;
		}
		
		PlayerSuperVipInfo superVipInfo = player.getSuperVipObject().getSuperVipInfo();
		// 第一次不需要判断时间
		if (superVipInfo.getActiveEndTime() > 0 && GameUtil.isCrossMonth(timeNow, superVipInfo.getActiveEndTime())) {
			sendError(protocol.getType(), Status.Error.SUPER_VIP_ACTIVATED_ENOUGH);
			return false;
		}
		
		SuperVipActiveReq req = protocol.parseProtocol(SuperVipActiveReq.getDefaultInstance());
		int cfgId = req.getCfgId();
		VipTemprorySuperCfg cfg = HawkConfigManager.getInstance().getConfigByKey(VipTemprorySuperCfg.class, cfgId);
		if (cfg == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(cfg.getActivationCostItems(), false);
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}
		consume.consumeAndPush(player, Action.SUPER_VIP_ACTIVE);
		
		superVipInfo.setActualLevel(VipSuperCfg.getVipSuperLevel(player.getVipExp()));
		superVipInfo.setActivatedPeriodMonth(HawkTime.getYyyyMMddIntVal() / 100);
		superVipInfo.setLoginTime(timeNow);
		superVipInfo.setActivatedLevel(superVipInfo.getActualLevel());
		
		long lastRecieveTime = superVipInfo.getDailyGiftRecieveTime(), lastEndTime = superVipInfo.getActiveEndTime();
		// 最后一次领取礼包后超过一天才激活的，鉴于dailyGiftRecieveTime可能在登录补发的时候设置成昨天的一个时间，不能只靠这个来判断
		if (HawkTime.getCrossDay(timeNow, lastRecieveTime, 0) > 1 || lastRecieveTime > lastEndTime) {
			superVipInfo.setLoginDays(1);
			superVipInfo.setLastLoginScore(ConstProperty.getInstance().getSuperVipDailyLoginScore());
			superVipInfo.setDailyGiftRecieveTime(timeNow - HawkTime.DAY_MILLI_SECONDS);
		}
		lastEndTime = Math.max(timeNow, lastEndTime);
		superVipInfo.setActiveEndTime(lastEndTime + cfg.getActivationTimes() * HawkTime.DAY_MILLI_SECONDS);
		
		player.getSuperVipObject().updateSuperVipInfo();
		player.getSuperVipObject().syncSuperVipInfo(false);
		LogUtil.logSuperVipActive(player, 0, superVipInfo.getActualLevel(), 2, cfgId, superVipInfo.getActiveEndTime());
		
		return true;
	}
	
	/**
	 * vip商城数据请求
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.VIP_SHOP_ITEM_REQ_C_VALUE)
	private boolean onVipShopItemReq(HawkProtocol protocol) {
		// 判断当前时间是否是新的一周
		if (vipShopRefreshTime == 0 || !HawkTime.isSameWeek(vipShopRefreshTime, HawkApp.getInstance().getCurrentTime())) {
			pushNewVipShopItem();
		}

		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 跨周时推送vip商城最新数据
	 */
	private void pushNewVipShopItem() {
		VipShopItemInfo.Builder vipShopItemBuilder = VipShopItemInfo.newBuilder();
		vipShopRefresh(vipShopItemBuilder);
		vipShopItemBuilder.setRefreshAll(true);
		// 同步vip商城购买信息
		player.sendProtocol(HawkProtocol.valueOf(HP.code.VIP_SHOP_ITEM_SYNC_S, vipShopItemBuilder));
	}

	/**
	 * vip商城购买
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.VIP_SHOP_BUY_C_VALUE)
	private boolean onVipShopItemBuy(HawkProtocol protocol) {		
		VipShopBuyReq req = protocol.parseProtocol(VipShopBuyReq.getDefaultInstance());
		final int shopId = req.getShopId();
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.VIP_SHOP, shopId)) {
			logger.error("vip shop item closed, playerId: {}, shopId: {}", player.getId(), shopId);
			sendError(protocol.getType(), Status.SysError.VIP_SHOP_GOODS_OFF);
			return false;
		}

		if (!HawkTime.isSameWeek(vipShopRefreshTime, HawkApp.getInstance().getCurrentTime())) {
			logger.error("vip shop goods valid time passed, playerId: {}", player.getId());
			pushNewVipShopItem();
			sendError(protocol.getType(), Status.Error.VIP_SHOP_OUT_OF_DATE);
			return false;
		}

		int count = req.getCount();
		HawkAssert.checkPositive(count);
		if (!vipShopIds.contains(shopId)) {
			logger.error("vip shop buy, params error, playerId: {}, cfgId: {}", player.getId(), shopId);
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		VipShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(VipShopCfg.class, shopId);
		if (cfg == null) {
			logger.error("vip shop buy, vipShop config error, playerId: {}, cfgId: {}", player.getId(), shopId);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}

		if (player.getVipLevel() < cfg.getVipLevel()) {
			logger.error("vip shop buy error, player vipLevel lower, playerId: {}, cfgId: {}, player vipLevel: {}",
					player.getId(), shopId, player.getVipLevel());
			sendError(protocol.getType(), Status.Error.VIP_LEVEL_LOWER);
			return false;
		}

		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, cfg.getHoldItem().getItemId());
		if (itemCfg == null) {
			logger.error("vip shop buy, item config error, playerId: {}, cfgId: {}", player.getId(), cfg.getHoldItem().getItemId());
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}

		int boughtTimes = RedisProxy.getInstance().getVipShopBuyTimes(player.getId(), shopId);
		if (boughtTimes + count > cfg.getNum()) {
			logger.error(
					"vip shop buy count error, playerId: {}, cfgId: {}, request count: {}, bought times: {}, config num: {}",
					player.getId(), cfg.getHoldItem().getItemId(), count, boughtTimes, cfg.getNum());
			sendError(protocol.getType(), Status.Error.VIP_GOODS_PURCHASE_TIME_OVERLIMIT);
			return false;
		}

		ConsumeItems consume = ConsumeItems.valueOf();
		ItemInfo price = cfg.getPriceItem();
		price.setCount(price.getCount() * count);//份数价格
		consume.addConsumeInfo(price, false);
		int consumeMoney = (int) price.getCount();
		if (!consume.checkConsume(player, protocol.getType())) {
			logger.error(
					"vip shop buy, consume error, playerId: {}, shopId: {}, cost: {}, playerGold: {}, requst shop goods count: {}",
					player.getId(), shopId, cfg.getPrice(), player.getGold(), count);
			return false;
		}

		if (consume.getBuilder().hasAttrInfo() && consume.getBuilder().getAttrInfo().getDiamond() > 0) {
			consume.addPayItemInfo(new PayItemInfo(String.valueOf(itemCfg.getId()), itemCfg.getSellPrice(), count));
		}
		consume.consumeAndPush(player, Action.VIP_SHOP_BUY);

		AwardItems award = AwardItems.valueOf();
		//将一份道具修改为多个  itemId="30000_810000_10" num="200"  num=份,itemId=单份数量 总价=itemId.count*count
		long maxCount = count*cfg.getHoldItem().getCount();
		award.addItem(cfg.getHoldItem().getType(), itemCfg.getId(), maxCount);
		award.rewardTakeAffectAndPush(player, Action.VIP_SHOP_BUY, true);
		RedisProxy.getInstance().updateVipShopBuyTimes(player.getId(), shopId, boughtTimes + count);
		player.getPush().syncVipShopItemInfo(shopId, boughtTimes + count);
		player.responseSuccess(protocol.getType());
		// 贵族商城道具购买打点日志
		LogUtil.logGiftBagFlow(player, GiftType.VIP_STORE_ITEM, String.valueOf(shopId), consumeMoney, price.getItemId(), itemCfg.getItemType(), (int)maxCount);

		return true;
	}

	/**
	 * 购买vip专属礼包
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.VIP_EXCLUSIVE_BOX_BUY_C_VALUE)
	private boolean onExclusiveBoxBuy(HawkProtocol protocol) {
		VipExclusiveBoxBuyReq req = protocol.parseProtocol(VipExclusiveBoxBuyReq.getDefaultInstance());
		int level = req.getVipLevel();
		HawkAssert.checkNonNegative(level);

		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.VIP_GIFT, level)) {
			logger.error("vip exclusive box closed, playerId: {}, giftId: {}", player.getId(), level);
			sendError(protocol.getType(), Status.SysError.VIP_GIFT_OFF);
			return false;
		}

		if (level > player.getVipLevel()) {
			logger.error("vipExclusiveBox buy, params error, playerId: {}, req level: {}, player vipLevel: {}",
					player.getId(), level, player.getVipLevel());
			sendError(protocol.getType(), Status.Error.VIP_LEVEL_LOWER);
			return false;
		}

		boolean bought = RedisProxy.getInstance().getVipBoxStatus(player.getId(), level);
		if (bought) {
			logger.error("vipExclusiveBox already bought, playerId: {}, req level: {}", player.getId(), level);
			sendError(protocol.getType(), Status.Error.VIP_EXCLUSIVE_BOUGHT);
			return false;
		}

		VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, level);
		if (vipCfg == null) {
			logger.error("vipExclusiveBox buy, vipCfg error, playerId: {}, req level: {}", player.getId(), level);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}

		List<ItemInfo> vipExclusiveItems = vipCfg.getVipExclusiveItems();
		if (vipExclusiveItems.isEmpty()) {
			logger.error("vipExclusiveBox buy, vipCfg gift items null, playerId: {}, req level: {}", player.getId(),
					level);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}

		ConsumeItems consume = ConsumeItems.valueOf();
		ItemInfo itemInfo = vipCfg.getVipExclusiveNowPrice();
		int cost = (int) itemInfo.getCount();
		consume.addConsumeInfo(itemInfo, false);
		if (!consume.checkConsume(player, protocol.getType())) {
			logger.error("vipExclusiveBox buy, consume error, playerId: {}, vipLevel: {}, cost: {}, playerGold: {}",
					player.getId(), player.getVipLevel(), vipCfg.getVipExclusiveNowPrice(), player.getGold());
			return false;
		}

		for (ItemInfo info : vipExclusiveItems) {
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, info.getItemId());
			if (itemCfg != null) {
				consume.addPayItemInfo(new PayItemInfo(String.valueOf(info.getItemId()), itemCfg.getSellPrice(), (int)info.getCount()));
			}
		}
		consume.consumeAndPush(player, Action.VIP_EXCLUSIVE_BOX_BUY);

		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(vipExclusiveItems);
		award.rewardTakeAffectAndPush(player, Action.VIP_EXCLUSIVE_BOX_BUY, true, RewardOrginType.SHOPPING_GIFT);

		RedisProxy.getInstance().updateVipBoxStatus(player.getId(), level, true);
		player.getPush().syncVipBoxStatus(level, true);

		MailParames.Builder mailParames = MailParames.newBuilder().setMailId(MailId.VIP_EXCLUSIVE_BOX_BOUGHT)
				.setPlayerId(player.getId()).setRewards(vipExclusiveItems);
		SystemMailService.getInstance().sendMail(mailParames.build());

		// vip贵族专属礼包购买打点日志
		LogUtil.logGiftBagFlow(player, GiftType.VIP_EXCLUSIVE_GIFT, String.valueOf(level), cost, itemInfo.getItemId(), 0);

		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 领取vip福利礼包
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.VIP_BENEFIT_BOX_TAKE_C_VALUE)
	private boolean onTakeBenefitBox(HawkProtocol protocol) {		
		TakeBenefitBoxReq req = protocol.parseProtocol(TakeBenefitBoxReq.getDefaultInstance());
		int vipLevel = req.getVipLevel();
		HawkAssert.checkNonNegative(vipLevel);
		int curLevel = player.getVipLevel();
		if (vipLevel > curLevel) {
			logger.error("vip level error, playerId: {}, current level: {}, req level: {}", player.getId(), curLevel, vipLevel);
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		} else if (curLevel == vipLevel) {
			boolean taken = RedisProxy.getInstance().getVipBoxStatus(player.getId(), 0);
			if (taken) {
				logger.error("vipBenefitBox already taken, playerId: {}, req level: {}", player.getId(), vipLevel);
				sendError(protocol.getType(), Status.Error.VIP_BENEFIT_TAKEN_TODAY);
				return false;
			}
		} else {
			List<String> unreceivedBoxes = RedisProxy.getInstance().getUnreceivedBenefitBox(player.getId());
			if (!unreceivedBoxes.contains(String.valueOf(vipLevel))) {
				logger.error("vip level error, playerId: {}, unreceivedBoxes: {}, req level: {}", player.getId(),
						unreceivedBoxes, vipLevel);
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
				return false;
			}
		}

		VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, vipLevel);
		if (vipCfg == null) {
			logger.error("vipBenefitBox take, vipCfg error, playerId: {}, req level: {}", player.getId(), vipLevel);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}

		List<ItemInfo> vipBenefitItems = vipCfg.getVipBenefitItems();
		if (vipBenefitItems.isEmpty()) {
			logger.error("vipBenefitBox take, vipBenefitBox items null, playerId: {}, req level: {}", player.getId(),
					vipLevel);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}

		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(vipBenefitItems);
		award.rewardTakeAffectAndPush(player, Action.TAKE_VIP_BENEFIT_BOX, true, RewardOrginType.SHOPPING_GIFT);

		logger.info("vipBenefitBox take success, playerId: {}, vipLevel: {}", player.getId(), player.getVipLevel());
		
		if (curLevel == vipLevel) {
			RedisProxy.getInstance().updateVipBoxStatus(player.getId(), 0, true);
			RedisProxy.getInstance().vipGiftRefresh(player.getId(), VipRelatedDateType.VIP_BENEFIT_TAKEN, HawkTime.getMillisecond());
			player.getPush().syncVipBoxStatus(0, true);
		} else {
			RedisProxy.getInstance().removeUnreceivedBenefitBox(player.getId(), String.valueOf(vipLevel));
			player.getPush().syncAllVipBoxStatus(false, false);
		}

		MailParames.Builder mailParames = MailParames.newBuilder().setMailId(MailId.VIP_BENEFIT_BOX_TAKEN)
				.setPlayerId(player.getId()).setRewards(vipBenefitItems);
		SystemMailService.getInstance().sendMail(mailParames.build());

		player.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 * 请求军演商店信息
	 * @return
	 */
	@ProtocolHandler(code = HP.code.MITITARY_SHOP_INFO_C_VALUE)
	private boolean onMilitaryShopInfoReq(HawkProtocol protocol) {
		syncMilitayShopItems(false, false);
		return true;
	}
	
	/**
	 * 购买联盟军演商店物品
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.MILITARY_SHOP_BUY_C_VALUE)
	private boolean buyMilitayShopItem(HawkProtocol protocol) {
		// 跨周了，先刷新数据
		long militaryShopRefreshTime = RedisProxy.getInstance().getMilitaryShopRefreshTime(player.getId());
		if (!HawkTime.isSameWeek(militaryShopRefreshTime, HawkApp.getInstance().getCurrentTime())) {
			syncMilitayShopItems(false, false);
			return false;
		}
		
		MilitaryShopBuyReq req = protocol.parseProtocol(MilitaryShopBuyReq.getDefaultInstance());
		int shopId = req.getShopId();
		int count = req.getCount();
		MilitaryExerciseShopCfg config = HawkConfigManager.getInstance().getConfigByKey(MilitaryExerciseShopCfg.class, shopId);
		if (config == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			logger.error("buyMilitayShopItem failed, config error, playerId: {}, shopId: {}", player.getId(), shopId);
			return false;
		}
		
		// 有没有到解锁时间
		long serverOpenTime = GsApp.getInstance().getServerOpenTime();
		if (HawkApp.getInstance().getCurrentTime() - serverOpenTime < config.getUnlockTime() * 1000L) {
			sendError(protocol.getType(), Status.Error.MILITARY_ITEM_LOCKED);
			logger.error("buyMilitayShopItem failed, shopItem not unlocked, playerId: {}, shopId: {}, count: {}", player.getId(), shopId);
			return false;
		}

		// 已买过多少次
		int alreadyBuyCount = RedisProxy.getInstance().getMilitaryShopItemBuyCount(player.getId(), shopId);
		int converTUpLimit = config.getConvertUpLimit();
		double eff645 = player.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_645) * GsConst.EFF_PER + 1;
		converTUpLimit = (int)(converTUpLimit * eff645);
		if (alreadyBuyCount + count > converTUpLimit) {
			sendError(protocol.getType(), Status.Error.MILITARY_ITEM_BUY_COUNT_LIMIT);
			logger.error("buyMilitayShopItem failed, buy count error, playerId: {}, shopId: {}, count: {}, alreadyBuyCount: {}", player.getId(), shopId, count, alreadyBuyCount);
			return false;
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		List<ItemInfo> consumeItems = config.getConsumeItems();
		consumeItems.stream().forEach(e -> e.setCount(e.getCount() * count));
		consume.addConsumeInfo(consumeItems);
		if (!consume.checkConsume(player, protocol.getType())) {
			logger.error("buyMilitayShopItem failed, consume error, playerId: {}, shopId: {}, count: {}", player.getId(), shopId, count);
			return false;
		}
		
		List<ItemInfo> shopItems = config.getShopItems();
		if (consume.getBuilder().hasAttrInfo() && consume.getBuilder().getAttrInfo().getDiamond() > 0) {
			for (ItemInfo info : shopItems) {
				ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, info.getItemId());
				if (itemCfg != null) {
					consume.addPayItemInfo(new PayItemInfo(String.valueOf(info.getItemId()), itemCfg.getSellPrice(), (int)info.getCount()));
				}
			}
		}
		consume.consumeAndPush(player, Action.BUY_MILITARY_ITEM);
		AwardItems awardItems = AwardItems.valueOf();
		shopItems.stream().forEach(e -> e.setCount(e.getCount() * count));
		awardItems.addItemInfos(shopItems);
		awardItems.rewardTakeAffectAndPush(player, Action.BUY_MILITARY_ITEM, true);

		// 更新购买次数
		RedisProxy.getInstance().incrMilitaryShopItemBuyCount(player.getId(), shopId, count);
		
		syncMilitayShopItems(false, false);
		
		return true;
	}
	
	/**
	 * 同步军演商店物品刷新时间
	 * 
	 * @param isNewly
	 */
	private void syncMilitayShopItems(boolean isLogin, boolean isNewly) {
		try {
			long militaryShopRefreshTime = RedisProxy.getInstance().getMilitaryShopRefreshTime(player.getId());
			if (!HawkTime.isSameWeek(militaryShopRefreshTime, HawkApp.getInstance().getCurrentTime())) {
				RedisProxy.getInstance().clearMilitaryShopItemBuyCount(player.getId());
				militaryShopRefreshTime = HawkApp.getInstance().getCurrentTime();
				RedisProxy.getInstance().updateMilitaryShopRefreshTime(player.getId(), militaryShopRefreshTime);
				HawkLog.logPrintln("syncMilitayShopItems cross week, playerId: {}, isLogin: {}", player.getId(), isLogin);
			}

			MilitaryShopInfo.Builder builder = MilitaryShopInfo.newBuilder();
			Map<Integer, Integer> buyCountMap = RedisProxy.getInstance().getMilitaryShopItemBuyCount(player.getId());
			for (Entry<Integer, Integer> entry : buyCountMap.entrySet()) {
				MilitaryShopItem.Builder item = MilitaryShopItem.newBuilder();
				item.setShopId(entry.getKey());
				item.setBuyCount(entry.getValue());
				MilitaryExerciseShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MilitaryExerciseShopCfg.class, entry.getKey());
				int convertUpLimit = cfg.getConvertUpLimit();
				double eff645 = player.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_645) * 0.0001 + 1;
				convertUpLimit = (int)(convertUpLimit * eff645);
				item.setTotalCount(convertUpLimit);
				builder.addBuyItems(item);
			}

			// 同步购买次数数据
			builder.setServerOpenTime(GsApp.getInstance().getServerOpenTime());
			builder.setMilitaryScore(player.getPlayerBaseEntity().getGuildMilitaryScore());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.MITITARY_SHOP_INFO_PUSH, builder));

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 请求可用礼包
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GIFT_SYNC_C_VALUE)
	private boolean onSyncGiftInfo(HawkProtocol protocol) {
		syncGiftInfo();
		return true;
	}

	/**
	 * 同步礼包信息
	 */
	public void syncGiftInfo() {
		HPSyncGiftInfoResp.Builder resp = createGiftInfoPB(player);
        try {
            logger.info("gift sync size, playerId:{}, groupCount:{}", player.getId(), resp.getGiftsOnSellCount());
        } catch (Exception ignore) {}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GIFT_SYNC_S, resp));
	}

	private HPSyncGiftInfoResp.Builder createGiftInfoPB(Player player) {
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		List<Integer> giftGroupIdList = giftEntity.getGiftGroupIdsList();
		GiftGroupCfg groupCfg = null;
		int level = 0;
		HPSyncGiftInfoResp.Builder sbuilder = HPSyncGiftInfoResp.newBuilder();
		AssembleDataManager dataManager = AssembleDataManager.getInstance(); 
		for (Integer groupId : giftGroupIdList) {
			groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, groupId);
			if (groupCfg.getOnSale() == GsConst.INT_TRUE) {
				level = giftEntity.getBuyLevel(groupCfg.getId());
				GiftCfg giftCfg = dataManager.getNextGiftCfg(groupCfg.getId(), level);
				if (giftCfg == null) {
					logger.error("createGiftInfoPB playerId:{} giftCfg id:{},level:{} can not find nextLevel", player.getId(), groupCfg.getId(), level);
				} else {
					sbuilder.addGiftsOnSell(giftCfg.getId());
				}
			}			
		}
		sbuilder.setCountDown(giftEntity.getLastRefreshTime() + ConstProperty.getInstance().getGiftResetTimeInterval() * 1000);
		
		return sbuilder;
	}
	
	/**
	 * 解锁行军表情包
	 * 
	 * @param msg
	 */
	@MessageHandler
	private void onMarchEmoticonBagUnlock(MarchEmoticonBagUnlockMsg msg) {
		String payGiftId = msg.getPayGiftId();
		int emoticonBag = MarchEmoticonProperty.getInstance().getEmoticonBagByPayGift(payGiftId);
		if (emoticonBag == 0) {
			HawkLog.errPrintln("unlock march emoticon bag failed, emoticonBag config not match, playerId: {}, payGiftId: {}", player.getId(), payGiftId);
			return;
		}
		
		CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.MARCH_EMOTICON_BAG);
		if (customData == null) {
			customData = player.getData().createCustomDataEntity(GsConst.MARCH_EMOTICON_BAG, 0, String.valueOf(emoticonBag));
		} else {
			customData.setArg(String.format("%s,%d", customData.getArg(), emoticonBag));
		}
		
		List<ItemInfo> itemList = MarchEmoticonProperty.getInstance().getPayReward(payGiftId);
		if (itemList.isEmpty()) {
			HawkLog.errPrintln("unlock march emoticon bag, reward not exist, playerId: {}, payGiftId: {}", player.getId(), payGiftId);
		} else {
			AwardItems awardItem = AwardItems.valueOf();
			awardItem.addItemInfos(itemList);
			awardItem.rewardTakeAffectAndPush(player, Action.MARCH_EMOTICON_BUY, RewardOrginType.MONCARD_DAILY_SIGN);
		}
		
		HawkLog.logPrintln("unlock march emoticon bag, playerId: {}, payGiftId: {}, emoticonBag: {}", player.getId(), payGiftId, emoticonBag);
		
		// 通知客户端表情包解锁了
		player.getPush().syncMarchEmoticon();
	}
	
	/**
	 * 超值礼包直购.
	 * @param msg
	 */
	@MessageHandler
	private void onDeliverGift(SuperGiftDirectBuyMsg msg) {
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		List<Integer> giftIdList = AssembleDataManager.getInstance().getSuperGiftIdListByPayGiftId(msg.getPayGiftId());
		if (giftIdList == null) {
			logger.error("playerId:{} deliver gift fail can not find giftId payGiftId:{}", player.getId(), msg.getPayGiftId());
			return ;
		}
		Integer giftId = null;
		Optional<Integer> op = giftIdList.stream().filter(id->{
			GiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(GiftCfg.class, id);
			if (giftCfg == null) {
				return false;
			}
			return giftEntity.getGiftGroupIdsList().contains(giftCfg.getGroupId()) && giftEntity.getBuyNum(id) == 0;
		}).findFirst();
		
		if (!op.isPresent()) {
			logger.error("playerId:{} deliver gift fail can find giftCfg in player group list payGiftId:{}, ", player.getId(), msg.getPayGiftId());
			return;
		}
		
		giftId = op.get();
		GiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(GiftCfg.class, giftId);		
		if (giftCfg == null ) {
			logger.error("playerId:{} deliver gift fail can not find giftCfg payGiftId:{}, giftId:{}", player.getId(), msg.getPayGiftId(), giftId);
			return ;
		}
		
		GiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, giftCfg.getGroupId());
		
		//发奖.
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(giftCfg.getCrystalRewardList());
		awardItem.addItemInfos(giftCfg.getAwardSpecialItemsList());
		awardItem.addItemInfos(giftCfg.getAwardItemsList());
		awardItem.rewardTakeAffectAndPush(player, Action.BUY_GIFT, RewardOrginType.SHOPPING_GIFT);
		
		if (groupCfg.getLimitType() != GsConst.GiftConst.LIMIT_NONE) {
			giftEntity.addBuyNum(giftCfg.getId(), 1);				
		}
				
		if (giftCfg.getIsMaxLevel() == GsConst.INT_TRUE) {			
			giftEntity.removeBuyLevel(giftCfg.getGroupId());			
			if (groupCfg.getType() != GsConst.GiftConst.GROUP_TYPE_POST) {
				//加入一个根引用这样在判定是否可以刷新的时候会过滤掉当前的根礼包
				giftEntity.addRootGroupIdRef(giftCfg.getGroupId(), 0);
			}			
			giftEntity.removeGiftGroupId(groupCfg.getId());
			giftEntity.removeGiftAdvice(groupCfg.getId());
			Integer poolId = AssembleDataManager.getInstance().getGiftPoolIdByGroupId(groupCfg.getId());
			int id = 0;
			//pooolId找不到说明是后置礼包.
			if (poolId != null) {
				GiftSysPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(GiftSysPoolCfg.class, poolId);
				//特殊礼包是从礼包池里面去随机,所以需要判断礼包池是否出售.
				if (poolCfg.getType() == GsConst.GiftConst.POOL_TYPE_SPECIAL) {
					if (poolCfg.getIsSale() == GsConst.INT_TRUE) {
						List<Integer> idList = randomGroupIdByPool(poolCfg, 1);
						if (!idList.isEmpty()) {
							id = idList.get(0);
						}
					}						
				} else {
					id = randomPostGroupId(groupCfg);
				}				
			} else {
				 id = randomPostGroupId(groupCfg);
			}
						
			if (id != 0) {
				this.addGiftGroupId(id);
				GiftGroupCfg giftGroupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, id);
				//加入了一个特殊礼包,它刷新出来的礼包并不是后置礼包，当做后置礼包处理的话会出现礼包重复刷新的问题.
				if (giftGroupCfg.getType() == GsConst.GiftConst.GROUP_TYPE_POST) {
					Optional<Entry<Integer, Integer>> optional = giftEntity.getRootGroupIdRefRecordsMap().entrySet().stream().filter(entry->{
						return entry.getKey().equals(giftCfg.getGroupId()) || entry.getValue().equals(giftCfg.getGroupId());
					}).findAny();
					if (optional.isPresent()) {
						giftEntity.addRootGroupIdRef(optional.get().getKey(), id);
					}
				}				
			}			
			logger.info("giftModule playerId:{}, postGiftId:{}", player.getId(), id);
		} else {
			giftEntity.addBuyLevel(giftCfg.getGroupId(), giftCfg.getLevel());
			addGiftAdviceBuyTimes(giftCfg.getGroupId());
			GiftCfg nextGiftCfg = AssembleDataManager.getInstance().getNextGiftCfg(giftCfg.getGroupId(), giftCfg.getLevel());
			LogUtil.logGiftRefresh(player, nextGiftCfg.getId());
		}
		syncGiftInfo();
		
		List<ItemInfo> itemInfoList = new ArrayList<>();
		itemInfoList.addAll(giftCfg.getAwardSpecialItemsList());
		itemInfoList.addAll(giftCfg.getAwardItemsList());
		itemInfoList.addAll(giftCfg.getCrystalRewardList());
		// 新手邮件改变为奖励已发状态. 奖励现由英雄升级给出. 奖励一至性由策划保证
		SystemMailService.getInstance()
				.sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.PAY)
						.addSubTitles(groupCfg.getGroupName()).addContents(groupCfg.getGroupName())
						.addRewards(itemInfoList).build());
		
		// 联盟成员发放礼物
		int allianceGift = giftCfg.getAllianceGift();
		if(!player.isCsPlayer() && player.hasGuild() && allianceGift>0){
			GuildService.getInstance().bigGift(player.getGuildId()).addSmailGift(allianceGift, false);
		}
		
		//LogUtil.logGiftBagFlow(player, GiftType.OVERBALANCED_GIFT, String.valueOf(giftId), costDiamond, costDiamond > 0 ? PlayerAttr.DIAMOND_VALUE : PlayerAttr.GOLD_VALUE, giftCfg.getGroupId());
		logger.info("player buy gift, playerId: {}, giftId: {}, cityLvl: {}, exp: {}, vipLvl: {}", player.getId(),
				giftId , player.getCityLevel(), player.getExp(), player.getVipLevel());
	}
	
	/**
	 * 购买礼包 (超值礼包)
	 */
	@ProtocolHandler(code = HP.code.BUY_GIFT_C_VALUE)
	private boolean onBuyGift(HawkProtocol protocol) {
		HPBuyGiftReq req = protocol.parseProtocol(HPBuyGiftReq.getDefaultInstance());
		final int giftId = req.getGiftId();
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		// 超值礼包关闭
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.PREMIUM_GIFT, giftId)) {
			logger.warn("premium gift closed, giftId: {}", giftId);
			sendError(protocol.getType(), Status.SysError.PREMIUM_GIFT_OFF);			
			return false;
		}

		GiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(GiftCfg.class, giftId);
		if (Objects.isNull(giftCfg) || giftCfg.getPriceList().isEmpty()) {
			sendError(protocol.getType(), Status.Error.ITEM_TYPE_ERROR);
			logger.warn("giftModule playerId: {}, giftId: {} is invalid", player.getId(), giftId);			
			return false;
		}
				
		GiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, giftCfg.getGroupId());
		if (groupCfg.getOnSale() != GsConst.INT_TRUE) {
			logger.warn("giftModule gift not sell  giftId:{}, sell:{}", player.getId(), giftCfg.getId(), groupCfg.getOnSale());
			sendError(protocol.getType(), Status.SysError.GIFT_NOT_SALE_VALUE);
			giftEntity.removeGiftGroupId(groupCfg.getId());
			giftEntity.removeGiftAdvice(groupCfg.getId());
			this.syncGiftInfo();
			return false;
		}
		//类型
		int voucherId = req.getVoucherId();
		//是否使用代金券
		boolean isUseVoucher = false;
		int voucherValue = 0;
		List<ItemInfo> lastItemInfos = new ArrayList<>();
		if (voucherId > 0) {
			int voucherType = groupCfg.getVoucherType();
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, voucherId);
			List<Integer> voucherList = SerializeHelper.cfgStr2List(itemCfg.getVoucherUse());
			long giftPrice = giftCfg.getPriceByType(PlayerAttr.DIAMOND_VALUE);
			long voucherPrice = itemCfg.getVoucherLimitByType(PlayerAttr.DIAMOND_VALUE);
			if (!voucherList.contains(voucherType) || giftPrice < voucherPrice) {
				sendError(protocol.getType(), Status.Error.VOUCHER_UNAVAILABLE_VALUE);
				logger.warn("giftModule playerId: {}, giftId: {} voucherId: {} giftPrice:{}, vocherPrice:{} is invalid",
						player.getId(), giftId, voucherId, giftPrice, voucherPrice);			
				return false;
			}
			if (HawkTime.getMillisecond() > HawkTime.parseTime(itemCfg.getVoucherTime())) {
				sendError(protocol.getType(),  Status.Error.VOUCHER_OVERTIME_VALUE);
				logger.warn("giftModule playerId: {}, giftId: {},  voucherId: {}, vocherTime:{}  is overTime", player.getId(), giftId, voucherId, itemCfg.getVoucherTime());			
				return false;
			}
			
			List<ItemInfo> newItemInfos = new ArrayList<>();
			newItemInfos.addAll(giftCfg.getCopyPriceList());
			for (ItemInfo itemInfo : newItemInfos) {
				if (itemInfo.getItemId() == PlayerAttr.DIAMOND_VALUE) {
					long value = (itemInfo.getCount() - itemCfg.getNum()) > 0 ? (itemInfo.getCount() - itemCfg.getNum()) : 0;
					itemInfo.setCount(value);
					lastItemInfos.add(new ItemInfo(ItemType.TOOL_VALUE, itemCfg.getId(), 1));
					isUseVoucher = true;
					voucherValue = itemCfg.getNum();
					break;
				}
			}
			//抵扣后的消耗数据(减了金条，加了代金券)
			lastItemInfos.addAll(newItemInfos);
		}
		if (!this.checkSell(giftCfg.getGroupId(), giftId)) {
			logger.warn("giftModule gift  limit buy num playerId:{}, giftId:{}, sell:{}", player.getId(), giftCfg.getId(), groupCfg.getOnSale());
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);		
			return false;
		}
							
		if (!giftEntity.getGiftGroupIdsList().contains(groupCfg.getId())){
			logger.warn("giftModul groupId not in playerGroupIdList playerId:{}. buyGroupId:{}, playerGroupIdList",
					player.getId(), groupCfg.getId(), giftEntity.getGiftGroupIdsList());
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);			
			return false;
		}
		
		int curLevel = giftEntity.getBuyLevel(giftCfg.getGroupId());
		if (curLevel + 1 != giftCfg.getLevel()) {			  
			logger.warn("giftModul buy sequence incorrect. playerId:{}, buyGroupId:{}, curLevel:{}, buyLevel:{}",
					player.getId(), groupCfg.getId(), curLevel, giftCfg.getLevel());
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);			
			return false;
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		List<ItemInfo> itemInfos = lastItemInfos.isEmpty() ? giftCfg.getPriceList() :lastItemInfos;
		consume.addConsumeInfo(itemInfos, false);
		if (!consume.checkConsume(player, protocol.getType())) {			
			return false;
		}
		
		if (consume.getBuilder().hasAttrInfo() && consume.getBuilder().getAttrInfo().getDiamond() > 0) {
			try {
				if (!giftCfg.getCrystalRewardList().isEmpty()) {
					ItemInfo itemInfo = giftCfg.getCrystalRewardList().get(0);
					consume.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), 1, (int)itemInfo.getCount()));
				}
				
				if (!giftCfg.getAwardSpecialItemsList().isEmpty()) {
					for (ItemInfo itemInfo : giftCfg.getAwardSpecialItemsList()) {
						ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
						consume.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), itemCfg.getSellPrice(), (int)itemInfo.getCount()));
					}
				}
				
				if (!giftCfg.getAwardItemsList().isEmpty()) {
					for (ItemInfo itemInfo : giftCfg.getAwardItemsList()) {
						ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
						consume.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), itemCfg.getSellPrice(), (int)itemInfo.getCount()));
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		consume.consumeAndPush(player, Action.BUY_GIFT);

		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(giftCfg.getCrystalRewardList());
		awardItem.addItemInfos(giftCfg.getAwardSpecialItemsList());
		awardItem.addItemInfos(giftCfg.getAwardItemsList());
		awardItem.rewardTakeAffectAndPush(player, Action.BUY_GIFT, RewardOrginType.SHOPPING_GIFT);
		
		boolean overCycle = false;
		if (groupCfg.getLimitType() != GsConst.GiftConst.LIMIT_NONE) {
			giftEntity.addBuyNum(giftCfg.getId(), 1);				
		}
				
		if (giftCfg.getIsMaxLevel() == GsConst.INT_TRUE) {			
			giftEntity.removeBuyLevel(giftCfg.getGroupId());			
			if (groupCfg.getType() != GsConst.GiftConst.GROUP_TYPE_POST) {
				//加入一个根引用这样在判定是否可以刷新的时候会过滤掉当前的根礼包
				giftEntity.addRootGroupIdRef(giftCfg.getGroupId(), 0);
			}			
			giftEntity.removeGiftGroupId(groupCfg.getId());
			giftEntity.removeGiftAdvice(groupCfg.getId());
			Integer poolId = AssembleDataManager.getInstance().getGiftPoolIdByGroupId(groupCfg.getId());
			int id = 0;
			//pooolId找不到说明是后置礼包.
			if (poolId != null) {
				GiftSysPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(GiftSysPoolCfg.class, poolId);
				//特殊礼包是从礼包池里面去随机,所以需要判断礼包池是否出售.
				if (poolCfg.getType() == GsConst.GiftConst.POOL_TYPE_SPECIAL) {
					if (poolCfg.getIsSale() == GsConst.INT_TRUE) {
						List<Integer> idList = randomGroupIdByPool(poolCfg, 1);
						if (!idList.isEmpty()) {
							id = idList.get(0);
						}
					}						
				} else {
					id = randomPostGroupId(groupCfg);
				}				
			} else {
				 id = randomPostGroupId(groupCfg);
			}
						
			if (id != 0) {
				this.addGiftGroupId(id);
				GiftGroupCfg giftGroupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, id);
				//加入了一个特殊礼包,它刷新出来的礼包并不是后置礼包，当做后置礼包处理的话会出现礼包重复刷新的问题.
				if (giftGroupCfg.getType() == GsConst.GiftConst.GROUP_TYPE_POST) {
					Optional<Entry<Integer, Integer>> optional = giftEntity.getRootGroupIdRefRecordsMap().entrySet().stream().filter(entry->{
						return entry.getKey().equals(giftCfg.getGroupId()) || entry.getValue().equals(giftCfg.getGroupId());
					}).findAny();
					if (optional.isPresent()) {
						giftEntity.addRootGroupIdRef(optional.get().getKey(), id);
					}
				}				
			}			
			overCycle = true;
			logger.info("giftModule playerId:{}, postGiftId:{}", player.getId(), id);
		} else {
			giftEntity.addBuyLevel(giftCfg.getGroupId(), giftCfg.getLevel());
			addGiftAdviceBuyTimes(giftCfg.getGroupId());
			GiftCfg nextGiftCfg = AssembleDataManager.getInstance().getNextGiftCfg(giftCfg.getGroupId(), giftCfg.getLevel());
			LogUtil.logGiftRefresh(player, nextGiftCfg.getId());
		}
		
		syncGiftInfo();
		if (overCycle && giftCfg.getGroupId() == ConstProperty.getInstance().getBreakIceHeroPackageId()) {
			GameUtil.setFlagAndPush(player, PlayerFlagPosition.BREAK_ICE, 1);
		}
		HPBuyGiftResp.Builder resp = HPBuyGiftResp.newBuilder();
		resp.setGiftId(giftId);
		resp.setSuccess(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.BUY_GIFT_S_VALUE, resp));
		player.responseSuccess(protocol.getType());
		
		// 联盟成员发放礼物
		int allianceGift = giftCfg.getAllianceGift();
		if(!player.isCsPlayer() && player.hasGuild() && allianceGift > 0){
			GuildService.getInstance().bigGift(player.getGuildId()).addSmailGift(allianceGift, false);
		}

		// 触发购买礼包事件
		int costDiamond = GameUtil.getItemNumByItemId(giftCfg.getPriceList(), ItemType.PLAYER_ATTR,
				PlayerAttr.DIAMOND_VALUE);
		if (costDiamond > 0) {
			PackageBuyEvent packageBuyEvent = PackageBuyEvent.valueOf(player.getId(), costDiamond);
			ActivityManager.getInstance().postEvent(packageBuyEvent);
			ActivityManager.getInstance().postEvent(new GiftPurchasePriceEvent(player.getId(), costDiamond, 1));
		}
		
		List<ItemInfo> itemInfoList = new ArrayList<>();
		itemInfoList.addAll(giftCfg.getAwardSpecialItemsList());
		itemInfoList.addAll(giftCfg.getAwardItemsList());
		itemInfoList.addAll(giftCfg.getCrystalRewardList());
		// 新手邮件改变为奖励已发状态. 奖励现由英雄升级给出. 奖励一至性由策划保证
		SystemMailService.getInstance()
				.sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.PAY)
						.addSubTitles(groupCfg.getGroupName()).addContents(groupCfg.getGroupName())
						.addRewards(itemInfoList).build());
		// 超值礼包购买打点日志
		LogUtil.logGiftBagFlow(player, GiftType.OVERBALANCED_GIFT, String.valueOf(giftId), costDiamond, costDiamond > 0 ? PlayerAttr.DIAMOND_VALUE : PlayerAttr.GOLD_VALUE, giftCfg.getGroupId());
		//用代金券打点日志
		if (isUseVoucher) {
			LogUtil.logGiftByVoucher(player, GiftType.OVERBALANCED_GIFT, String.valueOf(giftId), costDiamond,
					costDiamond > 0 ? PlayerAttr.DIAMOND_VALUE : PlayerAttr.GOLD_VALUE, giftCfg.getGroupId(), voucherId, voucherValue);
		}
		logger.info("player buy gift, playerId: {}, giftId: {}, cost: {}, cityLvl: {}, exp: {}, vipLvl: {}", player.getId(),
				giftId, costDiamond, player.getCityLevel(), player.getExp(), player.getVipLevel());		
		return true;
	}

	private List<Integer> randomGroupIdByPool(GiftSysPoolCfg poolCfg, int i) {		
		List<Integer> idList = new ArrayList<>();
		List<Integer> weightList = new ArrayList<>();
		poolCfg.getValueMap().entrySet().stream().forEach(entry->{
			if (this.canRefresh(entry.getKey())) {
				idList.add(entry.getKey());
				weightList.add(entry.getValue());
			}
		});
		
		if (!idList.isEmpty()) {
			i = weightList.size() >= i ? i : weightList.size();
			List<Integer> randomIdList = HawkRand.randomWeightObject(idList, weightList, i);
			
			return randomIdList;
		} else {
			return new ArrayList<>();
		}
	}

	/**
	 * 同步vip商城物品信息
	 */
	private void syncVipShopItems(boolean isNewly) {
		if (!isNewly) {
			vipShopRefreshTime = RedisProxy.getInstance().getVipGiftRefreshDate(player.getId(), VipRelatedDateType.VIP_SHOP_REFRESH);
		}
		
		VipShopItemInfo.Builder vipShopItemBuilder = VipShopItemInfo.newBuilder();

		// 时间跨周了，需要刷新数据
		if (vipShopRefreshTime == 0 || !HawkTime.isSameWeek(vipShopRefreshTime, HawkApp.getInstance().getCurrentTime())) {
			vipShopRefresh(vipShopItemBuilder);
		} else {
			vipShopBuilder(vipShopItemBuilder);
		}

		vipShopItemBuilder.setRefreshAll(true);
		// 同步vip商城购买信息
		player.sendProtocol(HawkProtocol.valueOf(HP.code.VIP_SHOP_ITEM_SYNC_S, vipShopItemBuilder));
		
		// 贵族商城红点标识
		if (!HawkTime.isSameWeek(HawkTime.getMillisecond(), player.getLogoutTime())) {
			player.setVipShopRedPoint(true);
			LocalRedis.getInstance().addVipShopRedPoint(player.getId());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.VIPSHOP_RED_POINT_SYNC));
		} else {
			player.setVipShopRedPoint(false);
			int redPointFlag = LocalRedis.getInstance().getVipShopRedPoint(player.getId());
			if (redPointFlag > 0) {
				player.setVipShopRedPoint(true);
				player.sendProtocol(HawkProtocol.valueOf(HP.code.VIPSHOP_RED_POINT_SYNC));
			}
		}
	}

	/**
	 * 检查离线期间错过领取的vip福利礼包，有邮件的形式给玩家补发
	 */
	private void checkMissedVipBenefitBox(boolean isNewly) {
		long lastTakenDate = 0;
		int vipLevel = player.getVipLevel();
		if (vipLevel > 0) {
			lastTakenDate = RedisProxy.getInstance().getVipGiftRefreshDate(player.getId(), VipRelatedDateType.VIP_BENEFIT_TAKEN);
		}
		
		if (lastTakenDate <= 0) {
			if (vipLevel > 0) {
				RedisProxy.getInstance().updateVipBoxStatus(player.getId(), 0, false);
				RedisProxy.getInstance().vipGiftRefresh(player.getId(), VipRelatedDateType.VIP_BENEFIT_TAKEN,
						GsApp.getInstance().getCurrentTime() - GsConst.DAY_MILLI_SECONDS);
			}
			logger.info("checkMissedVipBenefitBox, playerId: {}, vipLevel: {}, lastTakenDate: {}", player.getId(), vipLevel, lastTakenDate);
			return;
		}
		
		if (!HawkTime.isSameDay(lastTakenDate, HawkTime.getMillisecond())) {
			logger.info("checkMissedVipBenefitBox, playerId: {}, vipLevel: {}, lastTakenDate: {}", player.getId(), vipLevel, lastTakenDate);
			RedisProxy.getInstance().updateVipBoxStatus(player.getId(), 0, false);
		}

		int between = HawkTime.getCrossDay(lastTakenDate, GsApp.getInstance().getCurrentTime(), 0);
		if (between <= 1) {
			return;
		}

		VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, vipLevel);
		if (vipCfg == null) {
			return;
		}

		between = between > 7 ? 7 : between;
		while (between > 1) {
			MailParames.Builder mailParames = MailParames.newBuilder().setMailId(MailId.VIP_BENEFIT_BOX_REFRESH)
					.setPlayerId(player.getId()).setRewards(vipCfg.getVipBenefitItems())
					.setAwardStatus(MailRewardStatus.NOT_GET);
			SystemMailService.getInstance().sendMail(mailParames.build());
			HawkLog.logPrintln("vip daily award reissue, playerId: {}, vipLevel: {}, day: {}, lastTime: {}", player.getId(), vipLevel, between, lastTakenDate);
			between--;
		}

		RedisProxy.getInstance().vipGiftRefresh(player.getId(), VipRelatedDateType.VIP_BENEFIT_TAKEN,
				GsApp.getInstance().getCurrentTime() - GsConst.DAY_MILLI_SECONDS);
		logger.info("vipBenefitBox reissue, playerId: {}, vipLevel: {}", player.getId(), vipLevel);
	}

	/**
	 * 跨周了，刷新vip商城物品信息
	 * 
	 * @param vipShopItemBuilder
	 */
	private void vipShopRefresh(VipShopItemInfo.Builder vipShopItemBuilder) {
		// 刷新礼包
		vipShopRefreshTime = GsApp.getInstance().getCurrentTime();
		
		Map<Integer, List<VipShopCfg>> cfgMap = AssembleDataManager.getInstance().getVipShopCfgListMap();
		StringBuilder sb = new StringBuilder();
		int randomCfgCount = 0;
		//刷新的时候清理
		vipShopIds.clear();
		for (Entry<Integer, List<VipShopCfg>> entry : cfgMap.entrySet()) {
			int vipLevel = entry.getKey();
			int shopItemCount = ConstProperty.getInstance().getVipShopItemCount(vipLevel);
			if (shopItemCount == 0) {
				continue;
			}

			List<VipShopCfg> cfgList = entry.getValue();
			List<VipShopCfg> selObjList = new ArrayList<>();
			selObjList.addAll(cfgList);
			List<Integer> weights = cfgList.stream().map(cfg->cfg.getWeight()).collect(Collectors.toList());
			List<VipShopCfg> refreshObjList = HawkRand.randomWeightObject(cfgList, weights, shopItemCount);
			Collections.sort(refreshObjList);
			for (VipShopCfg cfg : refreshObjList) {
				vipShopIds.add(cfg.getId());
				vipShopItemBuilder.addVipShopItems(BuilderUtil.getVipShopItemBuilder(cfg));
				sb.append(cfg.getId()).append("_").append(cfg.getVipLevel()).append(",");
				randomCfgCount++;
			}
		}

		// 批量更新
		RedisProxy.getInstance().batchUpdateVipShopRefreshInfo(player.getId(), vipShopRefreshTime, sb.toString());
		
		HawkLog.logPrintln("player vipShopRefresh, playerId: {}, cfgMap size: {}, random cfg count: {}", player.getId(), cfgMap.size(), randomCfgCount);
	}

	/**
	 * vip商城数据未刷新情况下（时间未到），根据最新配置重新数据组装
	 * 
	 * @param vipShopItemBuilder
	 */
	private void vipShopBuilder(VipShopItemInfo.Builder vipShopItemBuilder) {
		String shopIds = RedisProxy.getInstance().getVipShopIds(player.getId());
		if (HawkOSOperator.isEmptyString(shopIds)) {
			HawkLog.logPrintln("player vipShopBuilder build faild, vipShopIds empty, playerId: {}", player.getId());
			return;
		}

		Map<Integer, Integer> shopItemBuyTimes = RedisProxy.getInstance().getPlayerVipShopAllBuyTimes(player.getId());
		String[] cfgIds = shopIds.split(",");
		for (int i = 0; i < cfgIds.length; i++) {
			String[] shopIdLevel = cfgIds[i].split("_");
			int shopId = Integer.valueOf(shopIdLevel[0]);
			VipShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(VipShopCfg.class, shopId);
			if (cfg != null) {
				VipShopItem.Builder buider = BuilderUtil.getVipShopItemBuilder(cfg);
				if (shopItemBuyTimes.containsKey(shopId)) {
					buider.setRemainBuyTimes(cfg.getNum() - shopItemBuyTimes.get(shopId));
				}				
				vipShopItemBuilder.addVipShopItems(buider);
				vipShopIds.add(shopId);
			} 
		}

	}

	private void refresh(boolean isLogin) {
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		ConstProperty constProperty = ConstProperty.getInstance();
		long curTime = HawkTime.getMillisecond();			
		long openServerTime = GsApp.getInstance().getServerOpenAM0Time();
		long times = ((curTime - openServerTime) / (constProperty.getGiftResetTimeInterval() * 1000));
		long curOpenTime = times * constProperty.getGiftResetTimeInterval() * 1000 + openServerTime;
		giftEntity.setLastRefreshTime(curOpenTime);
		
		//系统刷新的时候清理部分数据.		
		giftEntity.getGiftGroupIdsList().clear();
				
		List<Integer> groupIdList = this.randomPoolGroupIdList();
		List<Integer> newIdList = new ArrayList<>();
		//必须走一次否则会有问题.
		List<Integer> conditionList = this.findConditionGroupIdList();
		newIdList.addAll(conditionList);
		newIdList.addAll(groupIdList);
		this.addGiftGroupIdList(newIdList);
		
		if (!isLogin) {
			this.syncGiftInfo();
		}
		
		logger.info("giftModule playerId:{}, refresh groupIdList: {}", player.getId(), newIdList);
	}
	
	private void addGiftGroupId(Integer groupId) {
		Integer poolId = AssembleDataManager.getInstance().getGiftPoolIdByGroupId(groupId);
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		if (poolId != null) {
			int curTime = HawkTime.getSeconds();
			giftEntity.addPoolResetTimeIfAbsent(poolId, curTime);
		}
		
		giftEntity.addGiftGroupId(groupId);
		
		//批量很难做查询统计
		Integer buyLevel = MapUtil.getIntValue(giftEntity.getBuyLevelsMap(), groupId);
		GiftCfg giftCfg = AssembleDataManager.getInstance().getNextGiftCfg(groupId, buyLevel);
		if (giftCfg != null) {
			LogUtil.logGiftRefresh(player, giftCfg.getId());
		} else {
			logger.error("add giftId not find next level playerId:{}, groupId:{}, buyLevel:{}", player.getId(), groupId, buyLevel);
		}		 
		
	}
	
	private void addGiftGroupIdList(List<Integer> idList) {
		for (Integer id : idList) {
			this.addGiftGroupId(id);
		}
	}

	/**
	 * 获取所有条件开启的礼包
	 * @return
	 */
	private List<Integer> findConditionGroupIdList() {
		ConfigIterator<GiftSysPoolCfg> groupCfgIterator = HawkConfigManager.getInstance().getConfigIterator(GiftSysPoolCfg.class);
		GiftSysPoolCfg poolCfg = null;
		List<Integer> idList = new ArrayList<>();
		while(groupCfgIterator.hasNext()) {
			poolCfg = groupCfgIterator.next();
			if (poolCfg.getActivateType() == GsConst.GiftConst.POOL_ACTIVATE_TYPE_CONDITION && poolCfg.getIsSale() == GsConst.INT_TRUE) {
				idList.addAll(this.findConditionGroupIdList(poolCfg.getId()));				
			}
		}
		
		return idList;
	}
	
	/**
	 * @param poolId
	 * @return
	 */
	private List<Integer> findConditionGroupIdList(Integer poolId) {
		
		GiftSysPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(GiftSysPoolCfg.class, poolId);
		if (poolCfg.getIsSale() == GsConst.INT_FALSE) {
			return new ArrayList<>();
		}
		
		List<Integer> idList = new ArrayList<>();
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		poolCfg.getValueMap().entrySet().stream().forEach(entry->{
			//刷新的时候如果已经通过根礼包刷新出了后置礼包则以后置礼包为刷新
			//系统重置的时候根礼包和后置礼包的引用关系已经删除,所以这逻辑没有问题
			Integer groupId = entry.getKey();
			Integer refId = giftEntity.getRootGroupIdRef(groupId);
			if (refId != null && refId.intValue() != 0) {
				groupId = refId;
			}				
			if (this.canRefresh(groupId) && !idList.contains(groupId)) {
				idList.add(groupId);
			}
		});
						
		return idList;
	}
	
	/**
	 * 随机一个后置礼包
	 * @param groupCfg
	 * @return
	 */
	private int randomPostGroupId(GiftGroupCfg groupCfg) {
		List<Integer> weightList = new ArrayList<>();
		List<Integer> groupIdList = new ArrayList<>();
		
		groupCfg.getPostGiftMap().entrySet().stream().forEach(entry->{
			if (this.canRefresh(entry.getKey())){
				groupIdList.add(entry.getKey());
				weightList.add(entry.getValue());
			}
		});
		
		if (weightList.isEmpty()) {
			return 0;
		} else {
			return HawkRand.randomWeightObject(groupIdList, weightList);
		}
		
	}
	
	/**
	 * 从指定的pool中随机出group id list
	 * 
	 * @param poolId
	 * @return
	 */
	private List<Integer> randomPoolGroupIdList(Integer poolId) {		
		GiftSysPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(GiftSysPoolCfg.class, poolId);
		if (poolCfg.getIsSale() == GsConst.INT_FALSE) {
			return new ArrayList<>();
		}
		
		List<Integer> groupIdList = new ArrayList<>();			
		List<Integer> weightList = new ArrayList<>();
		List<Integer> poolGroupIdList = new ArrayList<>();
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		poolCfg.getValueMap().entrySet().stream().forEach(entry->{
			//刷新的时候如果已经通过根礼包刷新出了后置礼包则以后置礼包为刷新
			//系统重置的时候根礼包和后置礼包的引用关系已经删除,所以这逻辑没有问题
			Integer refId = giftEntity.getRootGroupIdRef(entry.getKey());
			Integer refreshId = entry.getKey();
			if (refId != null && refId.intValue() != 0) {
				refreshId = refId;
			}
			
			if (canRefresh(refreshId) && !poolGroupIdList.contains(refreshId)) {
				poolGroupIdList.add(refreshId);
				weightList.add(entry.getValue());
			}
		});
	
		int num = poolCfg.getCnt() >= poolGroupIdList.size() ? poolGroupIdList.size() : poolCfg.getCnt();
		if (num > 0) {
			List<Integer> randIdList = HawkRand.randomWeightObject(poolGroupIdList, weightList, num);
			groupIdList.addAll(randIdList);
		}

		return groupIdList;
	}
	/**
	 * 系统刷新,从所有的系统刷新池子里面找到符合条件的group id list.
	 * 
	 * @return
	 */
	private List<Integer> randomPoolGroupIdList() {
		List<Integer> groupIdList = new ArrayList<>();
		ConfigIterator<GiftSysPoolCfg> giftSysPoolIterator = HawkConfigManager.getInstance().getConfigIterator(GiftSysPoolCfg.class);
		GiftSysPoolCfg poolCfg = null;
		while (giftSysPoolIterator.hasNext()) {
			poolCfg = giftSysPoolIterator.next();
			if (poolCfg.getActivateType() != GsConst.GiftConst.POOL_ACTIVATE_TYPE_NORMAL || poolCfg.getIsSale() == GsConst.INT_FALSE) {
				continue;
			}
			groupIdList.addAll(this.randomPoolGroupIdList(poolCfg.getId()));
		}

		return groupIdList;
	}
	
	//检测所有的条件开启,因为开服时间和国王战本身就是要通过,tick去做的。所以干脆所有的逻辑否放这里了
	private void checkRefreshAllCondtionGift() {		
		List<Integer> conditionList = this.findConditionGroupIdList();		
		if (!conditionList.isEmpty()) {
			this.addGiftGroupIdList(conditionList);
			this.syncGiftInfo();			
			logger.info("condition open idList:{}", player.getData().getPlayerGiftEntity().getGiftGroupIdsList());
		}		
	}

	/**
	 * 检测礼包是否可以购买
	 * @param groupId
	 * @param giftId  添加了一个giftId参数
	 * @return
	 */
	public boolean checkSell(int groupId, int giftId) {
		GiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, groupId);
		if (groupCfg.getLimitType() != GsConst.GiftConst.LIMIT_NONE) {
			PlayerGiftEntity playerGiftEntity = player.getData().getPlayerGiftEntity();
			int buyNum = playerGiftEntity.getBuyNum(giftId);
			if (groupCfg.getLimitCnt() <= buyNum) {
				return false;
			}
		}

		return true;
	}
	
	@SuppressWarnings("deprecation")
	private boolean canRefresh(Integer groupId) {
		GiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, groupId);
		if (groupCfg.getOnSale() != GsConst.INT_TRUE) {
			return false;
		}
		
		PlayerGiftEntity playerGiftEntity = player.getData().getPlayerGiftEntity();
		//是否已经刷新
		if (playerGiftEntity.getGiftGroupIdsList().contains(groupId) ) {
			return false;
		} else {
			//是否是根礼包 已经通过根礼包刷新出了后置礼包也不能刷新
			Map<Integer, Integer> rootMap = playerGiftEntity.getRootGroupIdRefRecordsMap();
			/**
			 * 在后置礼包不可以无限循环的情况下，会有问题.
			 * 因为后置礼包会丢失掉所有的信息,无法记录是否已经刷新完了.
			 */
			if (rootMap.containsKey(groupId)) {
				return false;
			}
			
		}
		//次数限制
		if (groupCfg.getLimitType() != GsConst.GiftConst.LIMIT_NONE) {
			GiftCfg giftCfg = AssembleDataManager.getInstance().getGroupMaxGiftCfgMap().get(groupId);
			int buyNum = playerGiftEntity.getBuyNum(giftCfg.getId());
			if (groupCfg.getLimitCnt() <= buyNum) {
				return false;
			}
		}	
		//条件限制
		GiftGroupCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, groupId);
		boolean conditionCheck = cfg.checkUnlockConValue(player);
		if (conditionCheck && ConstProperty.getInstance().getSecondBuildGiftGroupId() == groupId) {
			long serverOpenTime = GlobalData.getInstance().getServerOpenTime(player.getServerId());
			boolean pass = serverOpenTime > ConstProperty.getInstance().getSecondBuildDefaultOpenTimeLong();
			return pass;
		}
		return conditionCheck;
	}
	
	private boolean checkRefresh(boolean isLogin) {
		long curTime = HawkTime.getMillisecond();
		PlayerGiftEntity playerGiftEntity = player.getData().getPlayerGiftEntity();
		ConstProperty constProperty = ConstProperty.getInstance();
		if (playerGiftEntity.getLastRefreshTime() == 0 || (playerGiftEntity.getLastRefreshTime() + constProperty.getGiftResetTimeInterval() * 1000) < curTime) {
			refresh(isLogin);
			return true;
		} else {
			return false;
		}		
	}
	
	@MessageHandler
	private void onBuildingLevelUpMsg(BuildingLevelUpMsg msg) {
		this.checkRefreshAllCondtionGift();
	}
	
	@MessageHandler
	private void onCommanderLevelUpMsg(CommanderLevlUpMsg msg) {
		this.checkRefreshAllCondtionGift();
	}
	
	@MessageHandler
	private void onCrossDayBeforeZeroMsg(CrossDayBeforeZeroMsg msg) {
		clearGiftData();
	}	
	
	private void clearGiftData() {
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		List<Integer> removeKey = new ArrayList<>();
		for (Entry<Integer, Integer> entry : giftEntity.getBuyNumsMap().entrySet()) {
			GiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(GiftCfg.class, entry.getKey());
			GiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, giftCfg.getGroupId());
			if (groupCfg.getLimitType() == GsConst.GiftConst.LIMIT_DAILY) {
				removeKey.add(entry.getKey());
			}
		}
				
		giftEntity.setResetTime(HawkTime.getMillisecond());
		for (Integer removeId : removeKey) {
			giftEntity.removeBuyNum(removeId);
		}
	}
	
	/**
	 * 重置
	 * @return 是否有新的加入
	 */
	private boolean reset(Integer poolId) {
		//重置pool
		clearGroupId(poolId);
		GiftSysPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(GiftSysPoolCfg.class, poolId);
		List<Integer> idList = null;
		if (poolCfg.getActivateType() == GsConst.GiftConst.POOL_ACTIVATE_TYPE_NORMAL) {
			idList = this.randomPoolGroupIdList(poolId);
		} else {
			idList = this.findConditionGroupIdList(poolId);
		}
		
		this.addGiftGroupIdList(idList);
		logger.info("giftreset playerId:{},poolId:{}, newIdList:{}", player.getId(), poolId, idList);
		return !idList.isEmpty();
	}
	
	private void clearGroupId(Integer poolId) {
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		List<Integer> removeIdList = new ArrayList<>();
		for (Integer id : giftEntity.getGiftGroupIdsList()) {
			Integer tmpPoolId = AssembleDataManager.getInstance().getGiftPoolIdByGroupId(id);
			if (tmpPoolId == null) {
				continue;
			}else if (tmpPoolId.equals(poolId)){
				removeIdList.add(id);
			}
		}
		
		//根和后置礼包
		Iterator<Entry<Integer, Integer>> entryIterator = giftEntity.getRootGroupIdRefRecordsMap().entrySet().iterator();
		Set<Integer> rootSet = new HashSet<>();
		while (entryIterator.hasNext()) {
			Entry<Integer, Integer> entry = entryIterator.next();
			Integer tmpPoolId = AssembleDataManager.getInstance().getGiftPoolIdByGroupId(entry.getKey());
			if (tmpPoolId == null) {
				continue;
			} else if (tmpPoolId.equals(poolId)) {
				removeIdList.add(entry.getValue());
				rootSet.add(entry.getKey());
				entryIterator.remove();
			}
		}
		
		Iterator<Entry<Integer, Integer>> buyLevelIterator = giftEntity.getBuyLevelsMap().entrySet().iterator();
		Entry<Integer, Integer> buyLevelInterEntry;
		while(buyLevelIterator.hasNext()) {
			buyLevelInterEntry = buyLevelIterator.next();
			GiftGroupCfg giftGroupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, buyLevelInterEntry.getKey());
			//不限购并且在删除列表里面才清楚购买信息.
			if (giftGroupCfg.getLimitType() == GsConst.GiftConst.LIMIT_NONE 
					&& (removeIdList.contains(giftGroupCfg.getId()) || rootSet.contains(giftGroupCfg.getId()))) {
				buyLevelIterator.remove();
			}
		}
		
		giftEntity.removeGiftGroupIds(removeIdList);
		logger.info("clearPool playerId:{}, poolId:{}, groupId:{}", player.getId(), poolId, removeIdList);
	}
	
	/**
	 * 客户端点击贵族商城红点
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.VIPSHOP_RED_POINT_CLICK_C_VALUE)
	private void onClickVipShopRedPoint(HawkProtocol protocol){
		if (player.isVipShopRedPoint()) {
			player.setVipShopRedPoint(false);
			LocalRedis.getInstance().removeVipShopRedPoint(player.getId());
		}
		
		player.responseSuccess(protocol.getType());
	}
	
	@ProtocolHandler(code = { HP.code.RESOURCE_GIFT_BUY_C_VALUE })
	private void onResGiftBuy(HawkProtocol protocol){
		ResourceGiftBuyC buyParam = protocol.parseProtocol(ResourceGiftBuyC.getDefaultInstance());
		PlayerResourceGiftEntity giftEntity = player.getData().getPlayerResourceGiftEntity();
		HawkConfigManager configManager = HawkConfigManager.getInstance();
		AssembleDataManager assembleDataManager = AssembleDataManager.getInstance(); 
		Integer buyId = buyParam.getId();
		if (!giftEntity.getBoughtInfoMap().containsKey(buyId)) {
			this.respResGiftBuy(Status.Error.RESOURCE_GIFT_NOT_EXIST_VALUE);
			return;
		}
		
		ResGiftLevelCfg levelCfg = configManager.getConfigByKey(ResGiftLevelCfg.class, buyId);
		ResGiftGroupCfg groupCfg = configManager.getConfigByKey(ResGiftGroupCfg.class, levelCfg.getResType());
		if (groupCfg.getOnSale() == GsConst.INT_FALSE) {
			this.respResGiftBuy(Status.Error.RESOURCE_GIFT_OFF_VALUE);
			return;
		}
		
		HawkTuple2<Integer, ConsumeItems> rltTuple = ConsumeItems.checkConsumeAndGetResult(player, levelCfg.getPriceList());
		if (rltTuple.first != Status.SysError.SUCCESS_OK_VALUE) {
			this.respResGiftBuy(rltTuple.first);
			return;
		}
		
		ConsumeItems consume = rltTuple.second;
		if (consume.getBuilder().hasAttrInfo() && consume.getBuilder().getAttrInfo().getDiamond() > 0) {
			try {
				if (!levelCfg.getCrystalRewardList().isEmpty()) {
					ItemInfo itemInfo = levelCfg.getCrystalRewardList().get(0);
					consume.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), 1, (int)itemInfo.getCount()));
				}
				
				if (!levelCfg.getSpecialRewardList().isEmpty()) {
					for (ItemInfo itemInfo : levelCfg.getSpecialRewardList()) {
						ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
						consume.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), itemCfg.getSellPrice(), (int)itemInfo.getCount()));
					}
				}
				
				if (!levelCfg.getOrdinaryRewardList().isEmpty()) {
					for (ItemInfo itemInfo : levelCfg.getOrdinaryRewardList()) {
						ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
						consume.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), itemCfg.getSellPrice(), (int)itemInfo.getCount()));
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		consume.consumeAndPush(player, Action.RESOURCE_GIFT_BUY);
		AwardItems.rewardTaskAffectAndPush(player, Action.RESOURCE_GIFT_BUY, RewardOrginType.SHOPPING_GIFT, levelCfg.getCrystalRewardList(), levelCfg.getSpecialRewardList(), levelCfg.getOrdinaryRewardList());
		Integer time = giftEntity.getBoughtInfoMap().get(buyId);
		giftEntity.removeBoughtInfo(buyId);
		if (levelCfg.getIsMaxLevel() == GsConst.INT_TRUE) {
			levelCfg = assembleDataManager.getNextgResGiftLevelCfg(groupCfg.getResType(), 0);
		} else {
			levelCfg = assembleDataManager.getNextgResGiftLevelCfg(groupCfg.getResType(), levelCfg.getLevel());
		}
		giftEntity.addBoughtInfo(levelCfg.getId(), time);
		
		List<ItemInfo> itemInfoList = new ArrayList<>();
		itemInfoList.addAll(levelCfg.getCrystalRewardList());
		itemInfoList.addAll(levelCfg.getSpecialRewardList());
		itemInfoList.addAll(levelCfg.getOrdinaryRewardList());
		// 新手邮件改变为奖励已发状态. 奖励现由英雄升级给出. 奖励一至性由策划保证
		SystemMailService.getInstance()
				.sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.RESOURCE_GIFT)
						.addRewards(itemInfoList).build());
		// 联盟成员发放礼物
		int allianceGift = levelCfg.getAllianceGift();
		if(!player.isCsPlayer() && player.hasGuild() && allianceGift>0){
			GuildService.getInstance().bigGift(player.getGuildId()).addSmailGift(allianceGift, false);
		}
		
		logger.info("resGift playerId:{}, buy:{}", player.getId(), buyId);
		this.respResGiftBuy(Status.SysError.SUCCESS_OK_VALUE);
		
	}
	
	private void respResGiftBuy(int code) {
		ResourceGiftBuyS.Builder sbuilder = ResourceGiftBuyS.newBuilder();
		sbuilder.setRlt(code);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.RESOURCE_GIFT_BUY_S_VALUE, sbuilder));
		
		if (code == Status.SysError.SUCCESS_OK_VALUE) {
			this.synResGiftInfo();
		}
	}
	
	/*
	 * 超值礼包
	 */
	private void onSuperGiftTick() {
		//重置
		checkReset(false);
		//系统刷新
		checkRefresh(false);
		//条件刷新.
		this.checkRefreshAllCondtionGift();
	}
	
	private boolean checkReset(boolean isLogin) {
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		int curTime = HawkTime.getSeconds();
		boolean isReset = false;
		List<Integer> removePoolIdList = new ArrayList<>();
		for (Entry<Integer, Integer> entry : giftEntity.getPoolResetTimesMap().entrySet()) {
			GiftSysPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(GiftSysPoolCfg.class, entry.getKey());
			//<data id="5" type="1" activateType="1" cnt="8" value="60005_100,60010_100,60055_100" resetTime="0_12_20" isSale="1" />
			if (poolCfg.getResetTimeList().isEmpty()) {
				continue;
			}
			for (Integer resetClock : poolCfg.getResetTimeList()) {
				long poolResetTime = HawkTime.getHourOfDayTime(HawkTime.getMillisecond(), resetClock);
				int intPoolResetTime = (int)(poolResetTime / 1000);
				//已经重置了, 还有一种情况就是 停服关服导致上一次时间没有清理
				if (entry.getValue() >= intPoolResetTime || (curTime < intPoolResetTime && intPoolResetTime - 24 * 60 * 60 < entry.getValue())) {
					continue;
				}
								
				boolean newList = this.reset(entry.getKey());
				if (newList) {
					entry.setValue(curTime);
				} else {
					removePoolIdList.add(entry.getKey());
				}
				isReset = true;
			}
		}

		//那些到期了，没有新的礼包的清理掉
		removePoolIdList.stream().forEach(removePoolId->giftEntity.getPoolResetTimesMap().remove(removePoolId));
		if (isReset && !isLogin) {
			this.syncGiftInfo();
		}
		
		return isReset;
	}
	
	private void assembleResGift() {
		PlayerResourceGiftEntity giftEntity = player.getData().getPlayerResourceGiftEntity(); 
		if (giftEntity.getBoughtInfoMap().isEmpty()) {
			 initResGift();
		} else {
			onResourceGiftTick(true);
		}
	}	
	public void onResourceGiftTick(boolean isLogin) {
		PlayerResourceGiftEntity giftEntity = player.getData().getPlayerResourceGiftEntity();
		int curTime = HawkTime.getSeconds();
		List<Integer> removeId = new ArrayList<>(giftEntity.getBoughtInfoMap().size());
		AssembleDataManager assembleDataManager = AssembleDataManager.getInstance();
		HawkConfigManager configManager = HawkConfigManager.getInstance(); 
		for (Entry<Integer, Integer> entry : giftEntity.getBoughtInfoMap().entrySet()) {
			if (entry.getValue() <= curTime) {
				removeId.add(entry.getKey()); 
			}
		}
		
		if (!removeId.isEmpty()) {
			ResGiftLevelCfg resGiftLevelCfg = null;
			ResGiftGroupCfg groupCfg = null;
			for (Integer id : removeId) {
				giftEntity.removeBoughtInfo(id);
				resGiftLevelCfg = configManager.getConfigByKey(ResGiftLevelCfg.class, id);
				resGiftLevelCfg = assembleDataManager.getNextgResGiftLevelCfg(resGiftLevelCfg.getResType(), 0);
				groupCfg = configManager.getConfigByKey(ResGiftGroupCfg.class, resGiftLevelCfg.getResType());
				giftEntity.addBoughtInfo(resGiftLevelCfg.getId(), curTime + groupCfg.getResetTime());
				
			}
			
			if (!isLogin) {
				this.synResGiftInfo();
			}
		}
	}
	private void initResGift() {
		PlayerResourceGiftEntity resEntity = player.getData().getPlayerResourceGiftEntity();
		AssembleDataManager assembleDataManager = AssembleDataManager.getInstance();
		HawkConfigManager configManager = HawkConfigManager.getInstance();
		int curTime = HawkTime.getSeconds();
		ConfigIterator<ResGiftGroupCfg> groupIterator = configManager.getConfigIterator(ResGiftGroupCfg.class);
		ResGiftLevelCfg giftLevelCfg = null;
		for (ResGiftGroupCfg groupCfg : groupIterator) {
			giftLevelCfg = assembleDataManager.getNextgResGiftLevelCfg(groupCfg.getResType(), 0);
			resEntity.addBoughtInfo(giftLevelCfg.getId(), curTime + groupCfg.getResetTime());
		}
	}

	private void synResGiftInfo() {
		ResourceGiftSyn.Builder resGiftSyn = ResourceGiftSyn.newBuilder();
		PlayerResourceGiftEntity giftEntity = player.getData().getPlayerResourceGiftEntity();
		ResourceGiftMsg.Builder resBuilder = null;
		for (Entry<Integer, Integer> entry : giftEntity.getBoughtInfoMap().entrySet()) {
			resBuilder = ResourceGiftMsg.newBuilder();
			resBuilder.setId(entry.getKey());
			resBuilder.setEndTime(entry.getValue());
			
			resGiftSyn.addResGiftMsgs(resBuilder.build());
		}
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.RESOURCE_GIFT_SYN_VALUE, resGiftSyn);
		player.sendProtocol(protocol);
	}

	private void assembleSuperGift() {
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		if (!HawkTime.isSameDay(giftEntity.getResetTime(), HawkTime.getMillisecond())) {
			this.clearGiftData();
		} else {
			Calendar calendar = HawkTime.getCalendar(true);
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MINUTE, 55);
			
			long now = HawkTime.getMillisecond();
			//玩家重置时间小于固定重置时间,并且当前时间大于固定重置时间
			if (now > calendar.getTimeInMillis() && giftEntity.getResetTime() < calendar.getTimeInMillis()) {
				this.clearGiftData();
			}			
		}
		
		this.checkReset(true);
		this.checkRefresh(true);		
	}

	/**
	 * 礼包推荐信息
	 */
	@ProtocolHandler(code = HP.code.GIFT_RECOMMEND_INFO_REQ_VALUE)
	private boolean onGiftRecommendInfo(HawkProtocol protocol) {
		syncGiftRecommendInfo();
		return true;
	}

	/**
	 * 礼包推荐
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GIFT_RECOMMEND_VALUE)
	private boolean onGiftRecommend(HawkProtocol protocol) {
		GiftRecommend req = protocol.parseProtocol(GiftRecommend.getDefaultInstance());
		int giftGroupCount = req.getGiftGroupIdCount();
		// 防止篡改协议乱搞
		if (giftGroupCount > 100) {
			return false;
		}
		
		List<Integer> giftGroupIdList = req.getGiftGroupIdList();
		for (int giftGroupId : giftGroupIdList) {
			// 没有这个礼包
			GiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, giftGroupId);
			if (groupCfg == null) {
				continue;
			}
			
			PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
			GiftAdviceItem giftAdviceInfo = giftEntity.getGiftAdviceInfo(giftGroupId);
			if (giftAdviceInfo == null) {
				giftAdviceInfo = new GiftAdviceItem();
				giftAdviceInfo.setGiftGroupId(giftGroupId);
				giftEntity.addGiftAdviceInfo(giftAdviceInfo);
			}
			
			giftAdviceInfo.addAllAdviceCount();
			giftAdviceInfo.addDayAdviceCount();
			giftAdviceInfo.setLastAdviceTime(HawkTime.getMillisecond());
			giftEntity.notifyUpdate();
		}
		
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	private void syncGiftRecommendInfo() {
		GiftRecommendInfo.Builder builder = GiftRecommendInfo.newBuilder();
		for (int groupId : getGiftAdviceIds()) {
			builder.addGiftGroupId(groupId);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GIFT_RECOMMEND_INFO_RESP, builder));
	}
	
	/**
	 * 获取推荐礼包groupId列表
	 * @return
	 */
	private List<Integer> getGiftAdviceIds() {
		List<Integer> adviceGiftGroupIds = new ArrayList<>();
		
		long currentTime = HawkTime.getMillisecond();
		
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();

		// 遍历所有礼包
		for (Integer groupId : giftEntity.getGiftGroupIdsList()) {
			
			// 不可售卖
			GiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, groupId);
			if (groupCfg.getOnSale() != GsConst.INT_TRUE) {
				continue;
			}
			
			// 礼包配置不存在
			int level = giftEntity.getBuyLevel(groupCfg.getId());
			GiftCfg giftCfg = AssembleDataManager.getInstance().getNextGiftCfg(groupCfg.getId(), level);
			if (giftCfg == null) {
				continue;
			}
			
			// 不需要推荐
			if (groupCfg.getRecommend() <= 0) {
				continue;
			}
			
			// vip等级不符合要求
			int vipLevel = player.getVipLevel();
			if (vipLevel < groupCfg.getVipLevelMin() || vipLevel > groupCfg.getBuildLevelMax()) {
				continue;
			}
			
			// 大本等级不符合要求
			int cityLevel = player.getCityLevel();
			if (cityLevel < groupCfg.getBuildLevelMin() || cityLevel > groupCfg.getBuildLevelMax()) {
				continue;
			}
			
			// 推荐信息
			GiftAdviceItem giftAdviceInfo = giftEntity.getGiftAdviceInfo(groupId);
			if (giftAdviceInfo != null) {
				// 每日推荐次数达到最大
				int dayAdviceCount = giftAdviceInfo.getDayAdviceCount();
				if (dayAdviceCount >= groupCfg.getDayRecommendTimes()) {
					continue;
				}
				
				// 总推荐次数达到最大
				int allAdviceCount = giftAdviceInfo.getAllAdviceCount();
				if (allAdviceCount >= groupCfg.getTotalRecommendTimes()) {
					continue;
				}
				
				// 推荐cd
				long lastAdviceTime = giftAdviceInfo.getLastAdviceTime();
				if (currentTime - lastAdviceTime < groupCfg.getIntervalTime()) {
					continue;
				}
			}
			
			adviceGiftGroupIds.add(groupId);
		}
		return adviceGiftGroupIds;
	}
	
	/**
	 * 添加推荐礼包购买次数
	 * @param giftGroupId
	 */
	private void addGiftAdviceBuyTimes(int giftGroupId) {
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		GiftAdviceItem giftAdviceInfo = giftEntity.getGiftAdviceInfo(giftGroupId);
		if (giftAdviceInfo == null) {
			giftAdviceInfo = new GiftAdviceItem();
			giftAdviceInfo.setGiftGroupId(giftGroupId);
			giftEntity.addGiftAdviceInfo(giftAdviceInfo);
		}
		// 添加购买次数
		giftAdviceInfo.addBuyTimes();
		giftEntity.notifyUpdate();
	}
	
	/**
	 * 购买第二建造队列礼包
	 * 
	 * @param msg
	 */
	@MessageHandler
	public void buySecondBuildGift(SecondBuildGiftBuyMsg msg){
		CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.SECOND_BUILD_OPEN_KEY);
		if (customData != null) {
			HawkLog.logPrintln("buy second build gift repeated, playerId: {}", player.getId());
			customData.setValue(1);
		} else {
			customData = player.getData().createCustomDataEntity(GsConst.SECOND_BUILD_OPEN_KEY, 1, "");
		}
		
		player.getPush().syncSecondaryBuildQueue();
		HawkLog.logPrintln("buy second build gift success, playerId: {}", player.getId());
	}
	
	public void clearMonthGift() {
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		List<Integer> removeKey = new ArrayList<>();
		// 每月限购礼包次数重置
		int monthMark = RedisProxy.getInstance().getSuperGiftMonthMark(player.getId());
		int currentMonth = HawkTime.getCalendar(false).get(Calendar.MONTH);
		boolean monthClear = (monthMark != currentMonth);
		if (monthClear) {
			RedisProxy.getInstance().updateSuperGiftMonthMark(player.getId());
		}
		
		long now = HawkTime.getMillisecond();
		boolean crossWeek = !HawkTime.isSameWeek(giftEntity.getWeekResetTime(), now);
		if (crossWeek) {
			giftEntity.setWeekResetTime(now);
		}
		
		Set<Integer> addGroupIdSet = new HashSet<>();
		Set<Integer> weekGroupIdSet = new HashSet<>();
		for (Entry<Integer, Integer> entry : giftEntity.getBuyNumsMap().entrySet() ) {
			GiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(GiftCfg.class, entry.getKey());
			GiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, giftCfg.getGroupId());
			if (groupCfg.getLimitType() == GsConst.GiftConst.LIMIT_MONTH && monthClear) {
				removeKey.add(entry.getKey());
				addGroupIdSet.add(groupCfg.getId());
			}
			if (groupCfg.getLimitType() == GsConst.GiftConst.LIMIT_WEEK && crossWeek) {
				removeKey.add(entry.getKey());
				weekGroupIdSet.add(groupCfg.getId());
			}
		}
		
		for (Integer groupgId : addGroupIdSet) {
			giftEntity.getGiftGroupIdsList().removeIf(e -> e == groupgId);
			giftEntity.addGiftGroupId(groupgId);
		}
		
		for (Integer groupgId : weekGroupIdSet) {
			giftEntity.removeBuyLevel(groupgId);
			giftEntity.getGiftGroupIdsList().removeIf(e -> e == groupgId);
			giftEntity.addGiftGroupId(groupgId);
		}
				
		for (Integer removeId : removeKey) {
			giftEntity.removeBuyNum(removeId);
			HawkLog.logPrintln("player supergift buyNums data month clear, playerId: {}, giftId: {}", player.getId(), removeId);
		}
		
		if (monthClear) {
			syncGiftInfo();
		}
	}
	
	/**
	 * 清除荣耀同享礼包
	 */
	private void clearShareGloryGift() {
		List<Integer> removePoolIdList = new ArrayList<>();
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		for (Entry<Integer, Integer> entry : giftEntity.getPoolResetTimesMap().entrySet()) {
			int poolId = entry.getKey();
			GiftSysPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(GiftSysPoolCfg.class, poolId);
			if (poolCfg.getType() == GsConst.GiftConst.POOL_SHARE_GLORY_GIFT) {
				reset(poolId);
				removePoolIdList.add(poolId);
				logger.info("reset share glory gift, playerId:{}, id:{}", player.getId(), poolId);
			}
		}
		removePoolIdList.stream().forEach(removePoolId->giftEntity.getPoolResetTimesMap().remove(removePoolId));
	}

	/**
	 * 清除荣耀同享礼包
	 */
	private void clearShareGloryGiftBuyNum() {
		PlayerGiftEntity giftEntity = player.getData().getPlayerGiftEntity();
		if (giftEntity == null) {
			return;
		}
		
		Map<Integer, Integer> buyNumsMap = giftEntity.getBuyNumsMap();
		if (buyNumsMap == null || buyNumsMap.isEmpty()) {
			return;
		}
		List<Integer> clearIds = new ArrayList<>();
		List<Integer> clearGroupIds = new ArrayList<>();
		for (Entry<Integer, Integer> entry : buyNumsMap.entrySet()) {
			if (entry.getValue() == 0) {
				continue;
			}
			GiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GiftCfg.class, entry.getKey());
			if (cfg == null) {
				continue;
			}
			GiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, cfg.getGroupId());
			if (groupCfg == null) {
				continue;
			}
			List<int[]> unlockConValueList = groupCfg.getUnlockConValueList();
			if (unlockConValueList == null || unlockConValueList.size() <= 0) {
				continue;
			}
			int[] is = unlockConValueList.get(0);
			if (is.length <= 0) {
				continue;
			}
			int condition = is[0];
			if (condition == GsConst.GiftConst.SHARE_GLORY_GIFT) {
				clearIds.add(entry.getKey());
				clearGroupIds.add(groupCfg.getId());
			}
		}
		for (int id : clearIds) {
			giftEntity.removeBuyNum(id);
			logger.info("clear share glory gift buy num, playerId:{}, id:{}", player.getId(), id);
		}
		for (int id : clearGroupIds) {
			giftEntity.removeBuyLevel(id);
			logger.info("clear share glory gift buy level, playerId:{}, id:{}", player.getId(), id);
		}
	}
	
	/**
	 * 荣耀同享活动开着没
	 */
	private boolean isShareGloryActivityOpen() {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(ActivityType.SHARE_GLORY_VALUE);
		if (!activityOp.isPresent()) {
			return false;
		}
		ShareGloryActivity activity = (ShareGloryActivity)activityOp.get();
		if (activity == null) {
			return false;
		}
		if (!activity.isHidden(player.getId())) {
			return true;
		}
		return false;
	}
}
