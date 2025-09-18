package com.hawk.activity.type.impl.monthcard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.hawk.game.protocol.Const;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.BuyMonthCardEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.MonthCardPriceCutItemAddEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityCfg;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardCustomRewardCfg;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardShopCfg;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.activity.type.impl.monthcard.entity.CustomItem;
import com.hawk.activity.type.impl.monthcard.entity.MonthCardItem;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MonthCard.ExchangeItem;
import com.hawk.game.protocol.MonthCard.HPMonthCardSync;
import com.hawk.game.protocol.MonthCard.MonthCardActiveResp;
import com.hawk.game.protocol.MonthCard.MonthCardPB;
import com.hawk.game.protocol.MonthCard.MonthCardPriceCutInfo;
import com.hawk.game.protocol.MonthCard.MonthCardRenewNotifyPB;
import com.hawk.game.protocol.MonthCard.MonthCardState;
import com.hawk.game.protocol.MonthCard.PBShopItemTip;
import com.hawk.game.protocol.MonthCard.PBShopItemTipsResp;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 月卡周卡（特权礼包）
 * @author lating
 *
 */
public class MonthCardActivity extends ActivityBase {
	
	public MonthCardActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.MONTHCARD_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		MonthCardActivity activity = new MonthCardActivity(config.getActivityId(), activityEntity);
		return activity;
	}
	
	@Override
	public void onPlayerLogout(String playerId) {
	}
	

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityMonthCardEntity> queryList = HawkDBManager.getInstance()
				.query("from ActivityMonthCardEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ActivityMonthCardEntity entity = queryList.get(0);
			return entity;
		}
		
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityMonthCardEntity entity = new ActivityMonthCardEntity(playerId, termId);
		entity.setLastRefreshTime(HawkTime.getAM0Date().getTime());
		return entity;
	}
	
	@Override
	public void onShow() {
	}

	@Override
	public void onPlayerLogin(String playerId) {
		loginUpdateAndSyncCardInfo(playerId);
		checkPriceCut(playerId);
	}
	
	private void checkPriceCut(String playerId) {
		int validTime = this.getDataGeter().checkMonthCardPriceCut(playerId);
		MonthCardPriceCutInfo.Builder builder = MonthCardPriceCutInfo.newBuilder();
		builder.setPricecut(validTime >= 0);
		if (validTime > 0) {
			builder.setRemindTimeEnd(validTime);
		}
		
		pushToPlayer(playerId, HP.code.MONTHCARD_PRICECUT_SYNC_S_VALUE, builder);
	}
	
	/**
	 * 更新和推送月卡状态（登录时检测）
	 * @param playerId
	 */
	private void loginUpdateAndSyncCardInfo(String playerId) {
		Optional<ActivityMonthCardEntity> opDataEntity = getPlayerDataEntity(playerId);
		if(!opDataEntity.isPresent()){
			return;
		}
		
		ActivityMonthCardEntity dataEntity = opDataEntity.get();
		long now = HawkTime.getMillisecond();
		// &&后面的判断是为了防止跨天时系统时间纠正导致的时间回调，对比时长不一定就是1小时
		boolean otherDay = !HawkTime.isSameDay(dataEntity.getLastRefreshTime(), now) && now - dataEntity.getLastRefreshTime() > HawkTime.HOUR_MILLI_SECONDS;

		// 用于记录已购买过，但续费提醒期已过，重新回到未购买状态的月卡
		List<MonthCardItem> removeCards = new ArrayList<MonthCardItem>();
		// 所有月卡的原始ID
		List<Integer> initialCards = new ArrayList<Integer>();
		initialCards.addAll(MonthCardActivityCfg.getInitialCards());
		
		int autoRenewType = 0;
		HPMonthCardSync.Builder builder = HPMonthCardSync.newBuilder();
		MonthCardRenewNotifyPB.Builder notifyCardBuilder = MonthCardRenewNotifyPB.newBuilder();
		for (MonthCardItem cardItem : dataEntity.getCardList()) {
			Integer initialCardId = MonthCardActivityCfg.getInitialCard(cardItem.getCardId()); 
			initialCards.remove(initialCardId);
			// 未购买状态的月卡（续费提醒期已过的月卡）
			if (cardItem.getState() == MonthCardState.UNPURCHASED_VALUE) {
				addMonthCardPB(builder, cardItem, dataEntity);
				removeCards.add(cardItem);
				continue;
			}
			
			MonthCardActivityCfg cardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, cardItem.getCardId());
			// 跨天考虑要不要补发奖励邮件
			if (otherDay) {
				sendMonthCardAwardCheck(playerId, dataEntity, cardCfg, cardItem);
			}
			
			// 月卡续费期限已过,重新回到未购买状态
			if (cardCfg.getRenewEndTime(cardItem.getPucharseTime()) <= now) {
				removeCustomItem(dataEntity, cardCfg.getCardId());
				cardItem.setState(MonthCardState.UNPURCHASED_VALUE);
				cardItem.setCardId(MonthCardActivityCfg.getInitialCardByType(cardCfg.getType()));
				addMonthCardPB(builder, cardItem, dataEntity);
				removeCards.add(cardItem);
				continue;
			}
			
			// 月卡有效期已过，对于可续费的月卡进入续费提醒状态，不可续费月卡直接进入未购买状态
			if (cardCfg.getValidEndTime(cardItem.getPucharseTime()) <= now) {
				if (!cardCfg.isRenewable()) {  // 不可续费
					cardItem.setState(MonthCardState.UNPURCHASED_VALUE);
					cardItem.setCardId(MonthCardActivityCfg.getInitialCardByType(cardCfg.getType()));
					removeCards.add(cardItem);
				} else if (cardItem.getState() != MonthCardState.TO_RENEW_VALUE) {    // 可续费
					removeCustomItem(dataEntity, cardCfg.getCardId());
					sendRenewNoticeMail(playerId, initialCardId);
					cardItem.setState(MonthCardState.TO_RENEW_VALUE);
					notifyCardBuilder.addCardId(initialCardId);
				}
				
				if (cardItem.getReady() > 0) {
					autoRenewType = MonthCardActivityCfg.getMonthCardType(cardItem.getCardId());
					cardItem.setReady(0);
				}
				addMonthCardPB(builder, cardItem, dataEntity);
				continue;
			}
			
			// 月卡还在有效期内，跨天了，要改变领取状态
			if (otherDay && cardItem.getState() == MonthCardState.RECEIVED_VALUE) {
				cardItem.setState(MonthCardState.TO_RECEIVE_VALUE);
			} 
			
			addMonthCardPB(builder, cardItem, dataEntity);
		}
		
		// 跨天了，把月卡entity的刷新时间更新到今日零点
		if (otherDay) {
			dataEntity.setLastRefreshTime(HawkTime.getAM0Date().getTime());
		}
		
		// 未购买过的月卡，也要给客户端推送信息
		for (int initialCardId : initialCards) {
			MonthCardPB.Builder monthCardPB = MonthCardItem.getBuilder(initialCardId);
			if (monthCardPB != null) {
				addCustomItem(dataEntity, monthCardPB);
				builder.addMonthCardInfo(monthCardPB);
			}
		}
		
		// 删除已购买过，但续费提醒期已过，重新回到未购买状态的月卡
		if (!removeCards.isEmpty()) {
			dataEntity.removeMultiCard(removeCards);
			dataEntity.notifyUpdate();
		} else if (builder.getMonthCardInfoCount() > 0) {
			dataEntity.notifyUpdate();
		}
		
		builderDataChange(playerId, dataEntity, builder, true);
		pushToPlayer(playerId, HP.code.MONTHCARD_INFO_SYNC_S_VALUE, builder);
		
		// 可续费提醒 
		if (notifyCardBuilder.getCardIdCount() > 0) {
			pushToPlayer(playerId, HP.code.MONTHCARD_RENEW_NOTIFY_S_VALUE, notifyCardBuilder);
		}
		
		// 自动续费
		if (autoRenewType > 0) {
			HawkLog.logPrintln("player month card auto renew login, playerId: {}, type: {}", playerId, autoRenewType);
			buyMonthCard(playerId, autoRenewType, dataEntity);
		}
	}
	
	/**
	 * 定制特权卡添加定制奖励物品
	 * @param dataEntity
	 * @param monthCardPB
	 */
	private void addCustomItem(ActivityMonthCardEntity dataEntity, MonthCardPB.Builder monthCardPB) {
		MonthCardActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, monthCardPB.getCardId());
		if (cfg.isCustomType()) {
			CustomItem customItem = dataEntity.getCustomItem(monthCardPB.getCardId());
			if (customItem != null) {
				monthCardPB.addAllSelectedItem(customItem.getRewardIdList());
			}
		}
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
	}
	
	/**
	 * 登录时检测月卡每日奖励补发数
	 * 
	 * @param playerId
	 * @param dataEntity
	 * @param cardCfg
	 * @param cardItem
	 */
	private void sendMonthCardAwardCheck(String playerId, ActivityMonthCardEntity dataEntity, MonthCardActivityCfg cardCfg, MonthCardItem cardItem) {
		if (cardItem.getState() != MonthCardState.TO_RECEIVE_VALUE && cardItem.getState() != MonthCardState.RECEIVED_VALUE) {
			return;
		}
		
		long endtime = Math.min(HawkApp.getInstance().getCurrentTime(), cardCfg.getValidEndTime(cardItem.getPucharseTime()));
		int days = HawkTime.getCrossDay(endtime, dataEntity.getLastRefreshTime(), 0);
		if (cardItem.getState() == MonthCardState.RECEIVED_VALUE) {
			days--;
		}
		
		// 最多补发7天的奖励
		if (days > 7) {
			days = 7;
		}
		
		while (days > 0) {
			sendDailyAwardMail(playerId, dataEntity, cardCfg);
			logger.info("monthCard daily award reissue, playerId: {}, cardId: {}, day: {}, lastRefreshTime: {}, buyTime: {}", 
					playerId, cardItem.getCardId(), days, dataEntity.getLastRefreshTime(), cardItem.getPucharseTime());
			days --;
		}
	}
	
	/**
	 * 添加月卡项信息
	 * @param builder
	 * @param cardItem
	 */
	private void addMonthCardPB(HPMonthCardSync.Builder builder, MonthCardItem cardItem, ActivityMonthCardEntity dataEntity) {
		MonthCardPB.Builder monthCardPB = cardItem.toBuilder();
		if (monthCardPB == null) {
			return;
		}
		MonthCardActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, cardItem.getCardId());
		if (cfg.isCustomType()) {
			CustomItem customItem = dataEntity.getCustomItem(cardItem.getCardId());
			if (customItem == null && cardItem.getCardId() != cfg.getNextCard() && cardItem.getState() == MonthCardState.TO_RENEW_VALUE) {
				customItem = dataEntity.getCustomItem(cfg.getNextCard()); 
			}
			if (customItem != null) {
				monthCardPB.addAllSelectedItem(customItem.getRewardIdList());
			}
		}
		builder.addMonthCardInfo(monthCardPB);
	}
	
	@Subscribe
	public void priceCutItemAdd(MonthCardPriceCutItemAddEvent event) {
		checkPriceCut(event.getPlayerId());
	}
	
	/**
	 * 购买月卡
	 * @param playerId
	 * @param cardId
	 * @return
	 */
	@Subscribe
	public Result<?> buyMonthCard(BuyMonthCardEvent event) {		
		String playerId = event.getPlayerId();
		int type = event.getCardId();
		Optional<ActivityMonthCardEntity> opDataEntity = getPlayerDataEntity(playerId);
		if(!opDataEntity.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		ActivityMonthCardEntity dataEntity = opDataEntity.get();
		if (event.getReady() > 0) {
			MonthCardItem cardItem = dataEntity.getEfficientCard(type);
			cardItem.setReady(1);
			dataEntity.notifyUpdate();
			return Result.success();
		}
		
		return buyMonthCard(playerId, type, dataEntity);
	}
	
	/**
	 * 购买月卡处理
	 * @param playerId
	 * @param type
	 * @param dataEntity
	 * @return
	 */
	private Result<?> buyMonthCard(String playerId, int type, ActivityMonthCardEntity dataEntity) {
		return buyMonthCard(playerId, type, dataEntity, 0);
	}
	
	/**
	 * 购买月卡内部处理类
	 * @param playerId
	 * @param type
	 * @param dataEntity
	 * @return
	 */
	private Result<?> buyMonthCard(String playerId, int type, ActivityMonthCardEntity dataEntity, int itemId) {
		// 获取玩家身上此类型有效月卡（包括处于可续费期的月卡）
		List<MonthCardItem> cardList =  dataEntity.getUnfinishedCards(type);
		// 已经存在生效月卡或处于续费期的月卡，下面进行续费逻辑处理
		if (!cardList.isEmpty()) {
			return renewMonthCard(playerId, type, cardList, itemId);
		}
		
		int initialCardId = MonthCardActivityCfg.getInitialCardByType(type);
		MonthCardActivityCfg cardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, initialCardId);
		if (cardCfg == null) {
			logger.error("monthCard purchase failed, card config error, playerId: {}, type: {}, cardId: {}", playerId, type, initialCardId);
			return Result.fail(Status.Error.ACTIVITY_MONTHCARD_CONFIG_ERROR_VALUE);
		}
		
		MonthCardItem card = dataEntity.getCard(cardCfg.getCardId());
		if (card == null) {
			card = MonthCardItem.valueOf(cardCfg.getCardId(), MonthCardState.TO_RECEIVE_VALUE);
			dataEntity.addCard(card);
		} else {
			card.setState(MonthCardState.TO_RECEIVE_VALUE);
		}
		
		// 因为有效期是按24点来算的，这里将购买时间记成是当日0点，方便后面计算有效期
		long purchaseTime = HawkTime.getAM0Date().getTime();
		card.setPucharseTime(purchaseTime);
		dataEntity.notifyUpdate();
		
		// 发放月卡购买奖励
		deliverMonthCardReward(playerId, cardCfg, purchaseTime, false, itemId);
		// 推送数据变化
		pushDataChange(playerId, card, dataEntity);
		
		logger.info("monthCard pucharse success, playerId: {}, cardId: {}", playerId, cardCfg.getCardId());
		return Result.success();
	}
	
	/**
	 * 通过使用道具来激活月卡
	 * 
	 * @param playerId
	 * @param itemId
	 * @return
	 */
	public Result<?> activeMonthCardByItem(String playerId, int itemId) {
		Optional<ActivityMonthCardEntity> opDataEntity = getPlayerDataEntity(playerId);
		if(!opDataEntity.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		// 找不到对应的cardId
		int cardId = MonthCardActivityCfg.getCardIdByItem(itemId);
		if (cardId <= 0) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		
		// 玩家身上没有此类道具
		int count = this.getDataGeter().getItemNum(playerId, itemId);
		if (count <= 0) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		
		ActivityMonthCardEntity dataEntity = opDataEntity.get();
		return activeMonthCardByItem(playerId, cardId, dataEntity);
	}
	
	/**
	 * 通过使用道具来激活月卡
	 * 
	 * @param playerId
	 * @param type
	 * @param dataEntity
	 * @return
	 */
	private Result<?> activeMonthCardByItem(String playerId, int cardId, ActivityMonthCardEntity dataEntity) {
		MonthCardActivityCfg cardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, cardId);
		if (cardCfg == null) {
			logger.error("monthCard active failed, card config error, playerId: {}, cardId: {}", playerId, cardId);
			return Result.fail(Status.Error.ACTIVITY_MONTHCARD_CONFIG_ERROR_VALUE);
		}
		
		int type = cardCfg.getType();
		if (!this.getDataGeter().monthCardFrontBuildCheck(playerId, type)) {
			logger.error("monthCard active failed, monthCard condition not match, playerId: {}, type: {}", playerId, type);
			return Result.fail(Status.Error.MONTHCARD_BUILD_LEVEL_ERROR_VALUE);
		}
		
		List<MonthCardItem> list = dataEntity.getEfficientCardList(type);
		if (!list.isEmpty()) {
			logger.error("monthCard active failed, efficient card not empty, playerId: {}, cardId: {}", playerId, cardId);
			return Result.fail(Status.Error.ACTIVITY_MONTHCARD_ACTIVATED_VALUE); // 此类月卡处于激活状态
		}
		
		if (cardCfg.isCustomType()) {
			CustomItem customItem = dataEntity.getCustomItem(cardCfg.getCardId());
			if (customItem == null || customItem.getRewardIdList().size() != cardCfg.getMadeRewardValue()) {
				HawkLog.errPrintln("monthcard active check failed, select reward count break, playerId: {}, type: {}", playerId, type);
				return Result.fail(Status.Error.MONTHCARD_CUSTOM_PAY_FAILED_VALUE); //定制类特权卡还未选择定制奖励，不能购买
			}
		}
		
		List<RewardItem.Builder> consumeList = RewardHelper.toRewardItemImmutableList(cardCfg.getCardItem());
		boolean consumeResult = getDataGeter().consumeItems(playerId, consumeList, HP.code2.MONTH_CARD_ACTIVE_REQ_VALUE, Action.MONTHCARD_ACTIVE_COST_ITEM); 
		if (!consumeResult) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		
		list = new ArrayList<MonthCardItem>();
		for (MonthCardItem card : dataEntity.getCardList()) {
			MonthCardActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, card.getCardId());
			if (cfg.getType() == type) {
				list.add(card);
			}
		}
		
		if (!list.isEmpty()) {
			dataEntity.removeMultiCard(list);
		}
		
		MonthCardItem monthCard = MonthCardItem.valueOf(cardCfg.getCardId(), MonthCardState.TO_RECEIVE_VALUE);
		// 因为有效期是按24点来算的，这里将购买时间记成是当日0点，方便后面计算有效期
		long purchaseTime = HawkTime.getAM0Date().getTime();
		monthCard.setPucharseTime(purchaseTime);
		dataEntity.addCard(monthCard);
		dataEntity.notifyUpdate();
		
		// 发放月卡购买奖励
		deliverMonthCardReward(playerId, cardCfg, purchaseTime, false, consumeList.get(0).getItemId());
		// 推送数据变化
		pushDataChange(playerId, monthCard, dataEntity);
		
		MonthCardActiveResp.Builder builder = MonthCardActiveResp.newBuilder();
		builder.setCardId(cardId);
		pushToPlayer(playerId, HP.code2.MONTH_CARD_ACTIVE_RESP_VALUE, builder);
		
		logger.info("monthCard active by item success, playerId: {}, cardId: {}", playerId, cardCfg.getCardId());
		return Result.success();
	}
	
	/**
	 * 领取月卡奖励
	 * @param playerId
	 * @param cardId
	 * @return
	 */
	public Result<?> receiveMonthCardAward(String playerId, int cardId) {
		Optional<ActivityMonthCardEntity> opDataEntity = getPlayerDataEntity(playerId);
		if(!opDataEntity.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		ActivityMonthCardEntity dataEntity = opDataEntity.get();
		MonthCardItem card = dataEntity.getCard(cardId);
		if (card == null || card.getState() != MonthCardState.TO_RECEIVE_VALUE) {
			logger.error("monthCard award received failed, playerId: {}, cardId: {}, card state: {}", playerId, cardId, card == null ? "" : card.getState());
			return Result.fail(Status.Error.ACTIVITY_MONTHCARD_CANNOT_RECEIVE_VALUE);
		}
		
		MonthCardActivityCfg cardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, cardId);
		if (cardCfg == null) {
			logger.error("monthCard award received failed, card config error, playerId: {}, cardId: {}", playerId, cardId);
			return Result.fail(Status.Error.ACTIVITY_MONTHCARD_CONFIG_ERROR_VALUE);
		}
		
		// 月卡有效期判断
		long cardValidEndTime = cardCfg.getValidEndTime(card.getPucharseTime()); 
		if (cardValidEndTime <= HawkTime.getMillisecond()) {
			removeCustomItem(dataEntity, cardId);
			updateMonthCardState(dataEntity, card, cardCfg);
			logger.error("monthCard award received failed, card valid endtime passed, playerId: {}, cardId: {}", playerId, cardId);
			return Result.fail(Status.Error.ACTIVITY_MONTHCARD_CANNOT_RECEIVE_VALUE);
		}
		
		// 发放奖励
		List<RewardItem.Builder> rewardList = this.getReward(playerId, dataEntity, cardCfg);
		if (rewardList.isEmpty()) {
			return Result.fail(Status.SysError.DATA_ERROR_VALUE);
		}
		
		Action action = cardCfg.isFreeCard() ? Action.MONTH_CARD_FREE_REWARD : Action.ACTIVITY_MONTHCARD_REWARD;
		ActivityReward reward = new ActivityReward(rewardList, action);
		reward.setOrginType(RewardOrginType.ACTIVITY_REWARD, getActivityId());
		reward.setAlert(true);
		postReward(playerId, reward);
		// 记录月卡活动点击事件流水日志
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), cardId);

		// 更新月卡状态，如果是在有效期内最后一次领取，领取完就将月卡状态改为可续费期
		if (cardValidEndTime <= HawkTime.getNextAM0Date() && cardCfg.isRenewable()) {
			removeCustomItem(dataEntity, cardId);
			updateMonthCardState(dataEntity, card, cardCfg);
		} else {
			card.setState(MonthCardState.RECEIVED_VALUE);
			dataEntity.notifyUpdate();
			pushDataChange(playerId, card, dataEntity);
		}
		
		logger.info("monthCard daily award receive, playerId: {}, cardId: {}", playerId, cardId);
		return Result.success();
	}
	
	/**
	 * 获取奖励内容
	 * @param playerId
	 * @param dataEntity
	 * @param cardId
	 * @return
	 */
	private List<RewardItem.Builder> getReward(String playerId, ActivityMonthCardEntity dataEntity, MonthCardActivityCfg cardCfg) {
		List<RewardItem.Builder> rewardList = getInitialDailyReward(playerId, dataEntity, cardCfg);
		if (!rewardList.isEmpty()) {
			int cardId = cardCfg.getCardId();
			double eff641 = this.getDataGeter().getBuff(playerId, Const.EffType.LIFE_TIME_CARD_641) * 0.0001 + 1;
			int ratioBuff = this.getDataGeter().getBuff(playerId, Const.EffType.MONTHCARD_REWARD_DOUBLE_910);
			int randInt = HawkRand.randInt(10000);
			boolean rewardDouble = cardCfg.getDouble() > 0 && randInt < ratioBuff;
			HawkLog.logPrintln("monthcard getReward, playerId: {}, cardId: {}, buff641: {}, buff910: {}, random number: {}, double config: {}", playerId, cardId, (eff641-1)*1000, ratioBuff, randInt, cardCfg.getDouble());
			for (RewardItem.Builder item : rewardList) {
				int newCount = (int) Math.ceil(item.getItemCount() * eff641);
				if (rewardDouble) {
					newCount += item.getItemCount();
				}
				item.setItemCount(newCount);
			}
		}
		
		return rewardList;
	}
	
	/**
	 * 获取初始化每日奖励
	 * @param playerId
	 * @param dataEntity
	 * @param cardCfg
	 * @return
	 */
	public List<RewardItem.Builder> getInitialDailyReward(String playerId, ActivityMonthCardEntity dataEntity, MonthCardActivityCfg cardCfg) {
		int cardId = cardCfg.getCardId();
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		if (!cardCfg.isCustomType()) {
			rewardList = RewardHelper.toRewardItemImmutableList(cardCfg.getDailyAward());
		} else {
			//定制类特权卡
			CustomItem customItem = dataEntity.getCustomItem(cardId);
			if (customItem == null) {
				customItem = dataEntity.getCustomLatest(cardCfg.getType());
				if (customItem == null) {
					logger.error("monthCard award received failed, customItem data error, playerId: {}, cardId: {}", playerId, cardId);
					return Collections.emptyList();
				} else {
					dataEntity.addCustomItem(customItem);
				} 
			}
			for (int rewardId : customItem.getRewardIdList()) {
				MonthCardCustomRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardCustomRewardCfg.class, rewardId);
				rewardList.addAll(RewardHelper.toRewardItemImmutableList(cfg.getReward()));
			}
		}
		
		return rewardList;
	}
	
	/**
	 * 更新月卡状态
	 * 
	 * @param dataEntity
	 * @param card
	 * @param cardCfg
	 */
	private void updateMonthCardState(ActivityMonthCardEntity dataEntity, MonthCardItem card, MonthCardActivityCfg cardCfg) {
		if (cardCfg.isRenewable()) {  // 可续费
			card.setState(MonthCardState.TO_RENEW_VALUE);
		} else {    // 不可续费
			card.setState(MonthCardState.UNPURCHASED_VALUE);
			card.setCardId(MonthCardActivityCfg.getInitialCardByType(cardCfg.getType()));
			dataEntity.removeCard(card);
		}
		
		dataEntity.notifyUpdate();
		pushDataChange(dataEntity.getPlayerId(), card, dataEntity);
	}
	
	/**
	 * 月卡续费
	 * 
	 * @param playerId
	 * @param cardId
	 * @return
	 */
	private Result<?> renewMonthCard(String playerId, int type, List<MonthCardItem> cardList, int itemId) {
		long now = HawkTime.getMillisecond();
		MonthCardActivityCfg cardCfg = null;
		// 找出处于可续费期的月卡
		for (MonthCardItem card : cardList) {
			MonthCardActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, card.getCardId());
			long validEndTime = cfg.getValidEndTime(card.getPucharseTime()); 
			if (validEndTime <= now || (validEndTime <= HawkTime.getNextAM0Date() && card.getState() == MonthCardState.TO_RENEW_VALUE)) {
				cardCfg = cfg;
				break;
			}
		}
		
		// 没有找到可续费的月卡
		if (cardCfg == null) {
			logger.error("monthCard renew failed, no card to renew, playerId: {}, card type: {}", playerId, type);
			return Result.fail(Status.Error.ACTIVITY_MONTHCARD_CANNOT_RENEW_VALUE);
		}
		
		// 获取新的月卡配置信息
		MonthCardActivityCfg newCardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, cardCfg.getNextCard());
		if (newCardCfg == null) {
			logger.error("monthCard renew failed, new card config error, playerId: {}, cardId:{}", playerId, cardCfg.getNextCard());
			return Result.fail(Status.Error.ACTIVITY_MONTHCARD_CONFIG_ERROR_VALUE);
		}

		// 如果原来的月卡还未失效，购买时间设置为第二天0点，否则设置为当日0点
		Optional<ActivityMonthCardEntity> opDataEntity = getPlayerDataEntity(playerId);
		if(!opDataEntity.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		ActivityMonthCardEntity dataEntity = opDataEntity.get();
		long purchaseTime = HawkTime.getNextAM0Date();
		MonthCardItem oldCard = dataEntity.getCard(cardCfg.getCardId());
		if (cardCfg.getValidEndTime(oldCard.getPucharseTime()) <= now) {
			purchaseTime -= HawkTime.DAY_MILLI_SECONDS;
		}
		
		MonthCardItem newCard = dataEntity.getCard(newCardCfg.getCardId());
		if (newCard == null) {
			newCard = MonthCardItem.valueOf(newCardCfg.getCardId(), purchaseTime < now ? MonthCardState.TO_RECEIVE_VALUE : MonthCardState.RECEIVED_VALUE);
			dataEntity.addCard(newCard);
		} else {
			newCard.setState(purchaseTime < now ? MonthCardState.TO_RECEIVE_VALUE : MonthCardState.RECEIVED_VALUE);
		}
		
		// 添加新的月卡后，将老的月卡删除(当续费后的月卡和老月卡一样时，不能删除)
		if (newCard.getCardId() != oldCard.getCardId()) {
			dataEntity.removeCard(oldCard);		
		}
		
		newCard.setPucharseTime(purchaseTime);
		dataEntity.notifyUpdate();
		
		// 发放月卡奖励
		deliverMonthCardReward(playerId, newCardCfg, purchaseTime, true, itemId);
		// 推送数据变化
		pushDataChange(playerId, newCard, dataEntity);
	
		logger.info("monthCard renew success, playerId: {}, cardId: {}", playerId, cardCfg.getCardId());
		return Result.success();
	}
	
	/**
	 * 发放月卡购买奖励
	 * 
	 * @param playerId
	 * @param cardCfg
	 * @param purchaseTime
	 */
	private void deliverMonthCardReward(String playerId, MonthCardActivityCfg cardCfg, long purchaseTime, boolean isRenew, int renewCardItemId) {
		int coinCount = getExchangeCoinCount(playerId);
		int coinCountAdd = 0;
		// 发放月卡购买相关的一次性奖励
		List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemImmutableList(cardCfg.getOneOffAward());
		if (!rewardList.isEmpty()) {
			ActivityReward reward = new ActivityReward(rewardList, Action.RECHARGE_BUY_MONTHCARD);
			reward.setOrginType(RewardOrginType.ACTIVITY_REWARD, getActivityId());
			reward.setAlert(true);
			reward.setAwardReason("recharge_activity");
			postReward(playerId, reward);
			coinCountAdd = (int) rewardList.stream().filter(e -> e.getItemId() == MonthCardShopCfg.getPayItemId()).mapToLong(e -> e.getItemCount()).sum();
		}
		
		// 月卡购买收据邮件
		int initialCardId = MonthCardActivityCfg.getInitialCardByType(cardCfg.getType());
		Object[] cardObject = new Object[]{ initialCardId };
		sendMailToPlayer(playerId, MailId.MONTHCARD_BUY, null, cardObject, cardObject, rewardList, true);
		
		// 作用号生效    月卡续费时 新的月卡作用号覆盖原来的月卡作用号，生效时间叠加
		long endTime = cardCfg.getValidEndTime(purchaseTime);
		List<Integer> buffList = cardCfg.getBuffList();
		if (!buffList.isEmpty()) {
			buffList.stream().forEach(e -> this.getDataGeter().addBuff(playerId, e, endTime));
		}
		
		int newCoinCount = coinCount + coinCountAdd;
		
		//记录特权卡购买（续费或激活）同时记录获得了多少特权兑换币, 续费的时候记录消耗的续费卡
		Map<String, Object> param = new HashMap<>();
        param.put("monthCardId", initialCardId); //特权卡ID
        param.put("renewBuy", isRenew ? 1 : 0);  //是否是续费，1:是，0:否
        param.put("validEndTime", cardCfg.getValidEndTime(purchaseTime) / 1000); //有效期结束时间
        param.put("cardType", cardCfg.getType());   //特权卡类型
        param.put("exchangeCoinAdd", coinCountAdd); //特权商店兑换积分数增加值
        param.put("exchangeCoinCount", newCoinCount); //特权商店兑换积分最新值
        param.put("renewCardItem", renewCardItemId);  //特权卡续费消耗的续费卡道具ID 
        getDataGeter().logActivityCommon(playerId, LogInfoType.buy_month_card, param);
		
		//记录月卡购买流水日志
		//this.getDataGeter().buyMonthCardRecord(playerId, initialCardId, isRenew, cardCfg.getValidEndTime(purchaseTime)); //不想再去改老的接口，所以注掉这一行，改用上面的logActivityCommon来记录打点
		// 刷新战力
		this.getDataGeter().refreshPower(playerId, PowerChangeReason.BUY_MONTHCARD);
	}
	
	/**
	 * 是否可以购买定制特权卡
	 * @param playerId
	 * @param type
	 * @return
	 */
	public int canPurchaseCustomCard(String playerId, int type) {
		if (!MonthCardActivityCfg.isCustomTypeCard(type)) {
			throw new RuntimeException("not support type - " + type);
		}
		
		Optional<ActivityMonthCardEntity> opDataEntity = getPlayerDataEntity(playerId);
		ActivityMonthCardEntity dataEntity = opDataEntity.get();
		List<MonthCardItem> cardList = dataEntity.getUnfinishedCards(type);
		MonthCardActivityCfg cardCfg = null;
		if (cardList.isEmpty()) {
			int initialCardId = MonthCardActivityCfg.getInitialCardByType(type);
			cardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, initialCardId);
		} else {
			long now = HawkTime.getMillisecond();
			//找出处于可续费期的月卡
			for (MonthCardItem card : cardList) {
				MonthCardActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, card.getCardId());
				long validEndTime = cfg.getValidEndTime(card.getPucharseTime()); 
				if (validEndTime <= now || (validEndTime <= HawkTime.getNextAM0Date() && card.getState() == MonthCardState.TO_RENEW_VALUE)) {
					cardCfg = cfg;
					break;
				}
			}
			
			//获取新的月卡配置信息
			if (cardCfg != null) {
				cardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, cardCfg.getNextCard());
			}
		}
		
		if (cardCfg == null) {
			HawkLog.errPrintln("monthcard canPurchaseCustomCard check failed, playerId: {}, type: {}", playerId, type);
			return Status.Error.MONTHCARD_CUSTOM_PAY_FAILED_VALUE;
		}
		
		CustomItem customItem = dataEntity.getCustomItem(cardCfg.getCardId());
		if (customItem == null || customItem.getRewardIdList().size() != cardCfg.getMadeRewardValue()) {
			HawkLog.errPrintln("monthcard canPurchaseCustomCard check failed, select reward count break, playerId: {}, type: {}", playerId, type);
			return Status.Error.MONTHCARD_CUSTOM_PAY_FAILED_VALUE;
		}
		
		return 0;
	}
	
	/**
	 * 定时检测月卡状态，状态变化是需要主动给前端发邮件
	 * @param playerId
	 */
	public void onTick(String playerId) {
		try {
			checkMonthCardAndPush(playerId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 月卡状态检测（客户端主动请求刷新月卡数据）
	 * @param playerId
	 * @return
	 */
	public void checkMonthCardState(String playerId) {
		checkMonthCardAndPush(playerId);
	}
	
	/**
	 * 跨天刷新月卡状态
	 * @param event
	 */
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		Optional<ActivityMonthCardEntity> opDataEntity = getPlayerDataEntity(playerId);
		if(!opDataEntity.isPresent()){
			return;
		}
		
		ActivityMonthCardEntity dataEntity = opDataEntity.get();
		long now = HawkTime.getMillisecond();
		if (dataEntity.getExchangeRefreshTime() == 0) {
			dataEntity.setExchangeRefreshTime(now);
			if(event.isCrossDay()){
				checkMonthCardAndPush(event.getPlayerId());
			}
		} else if (!HawkTime.isSameDay(now, dataEntity.getExchangeRefreshTime())) {
			clearExchangeInfoCrossWeek(dataEntity);
			dataEntity.setExchangeRefreshTime(now);
			checkMonthCardAndPush(event.getPlayerId());
		}
	}
	
	/**
	 * 跨周清除相关兑换信息
	 * @param dataEntity
	 */
	private void clearExchangeInfoCrossWeek(ActivityMonthCardEntity dataEntity) {
		try {
			if (HawkTime.isSameWeek(dataEntity.getExchangeRefreshTime(), HawkTime.getMillisecond())) {
				return;
			}
			
			boolean change = false;
			Iterator<Entry<Integer, Integer>> iterator = dataEntity.getExchangeMap().entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, Integer> entry = iterator.next();
				MonthCardShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardShopCfg.class, entry.getKey());
				if (cfg == null || cfg.isWeekLimit()) {
					iterator.remove();
					change = true;
				}
			}
			
			if (change) {
				dataEntity.notifyUpdate();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 检测月卡状态并推送
	 * 
	 * @param playerId
	 */
	private void checkMonthCardAndPush(String playerId) {
		Optional<ActivityMonthCardEntity> opDataEntity = getPlayerDataEntity(playerId);
		if(!opDataEntity.isPresent()){
			return;
		}
		
		ActivityMonthCardEntity dataEntity = opDataEntity.get();
		long now = HawkTime.getMillisecond();
		// &&后面的判断是为了防止跨天时系统时间纠正导致的时间回调，对比时长不一定就是1小时
		boolean otherDay = !HawkTime.isSameDay(dataEntity.getLastRefreshTime(), now) && now - dataEntity.getLastRefreshTime() > HawkTime.HOUR_MILLI_SECONDS;
		
		// 用于记录已购买过，但续费提醒期已过，重新回到未购买状态的月卡
		List<MonthCardItem> removeCards = new ArrayList<MonthCardItem>();
		int autoRenewType = 0;
		HPMonthCardSync.Builder builder = HPMonthCardSync.newBuilder();
		MonthCardRenewNotifyPB.Builder notifyCardBuilder = MonthCardRenewNotifyPB.newBuilder();
		for (MonthCardItem cardItem : dataEntity.getCardList()) {
			MonthCardActivityCfg cardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, cardItem.getCardId());
			if (cardCfg == null) {
				HawkLog.errPrintln("cross day refresh monthCard failed, config error, playerId: {}, cardId: {}", playerId, cardItem.getCardId());
				continue;
			}
			
			// 跨天考虑要不要补发奖励邮件
			if (otherDay) {
				sendMonthCardAwardCheck(playerId, dataEntity, cardCfg, cardItem);
			}
			
			// 处于续费提醒状态的月卡，判断续费提醒时间是否结束
			if (cardItem.getState() == MonthCardState.TO_RENEW_VALUE) {
				if (cardCfg.getRenewEndTime(cardItem.getPucharseTime()) <= now) {
					removeCustomItem(dataEntity, cardCfg.getCardId());
					cardItem.setState(MonthCardState.UNPURCHASED_VALUE);
					cardItem.setCardId(MonthCardActivityCfg.getInitialCardByType(cardCfg.getType()));
					addMonthCardPB(builder, cardItem, dataEntity);
					removeCards.add(cardItem);
				}
				if (cardItem.getReady() > 0) {
					autoRenewType = MonthCardActivityCfg.getMonthCardType(cardItem.getCardId());
					cardItem.setReady(0);
				}
				continue;
			}
			
			// 有效期结束，还未更新到续费提醒状态的月卡
			if (cardItem.getState() != MonthCardState.UNPURCHASED_VALUE && cardCfg.getValidEndTime(cardItem.getPucharseTime()) <= now) {
				// 可续费且可续费期未结束的月卡，将状态更新到续费提醒状态
				if (cardCfg.isRenewable() && cardCfg.getRenewEndTime(cardItem.getPucharseTime()) > now) {
					if (cardItem.getState() != MonthCardState.TO_RENEW_VALUE) {
						removeCustomItem(dataEntity, cardCfg.getCardId());
					}
					cardItem.setState(MonthCardState.TO_RENEW_VALUE);
					int initialCardId = MonthCardActivityCfg.getInitialCardByType(cardCfg.getType());
					sendRenewNoticeMail(playerId, initialCardId);
					notifyCardBuilder.addCardId(initialCardId);
				} else { // 不可续费或可续费期已结束，将月卡状态更新到未购买状态
					cardItem.setState(MonthCardState.UNPURCHASED_VALUE);
					cardItem.setCardId(MonthCardActivityCfg.getInitialCardByType(cardCfg.getType()));
					removeCards.add(cardItem);
				}
				
				if (cardItem.getReady() > 0) {
					autoRenewType = MonthCardActivityCfg.getMonthCardType(cardItem.getCardId());
					cardItem.setReady(0);
				}
				addMonthCardPB(builder, cardItem, dataEntity);
				continue;
			} 
			
			// 处于有效状态的月卡，跨天了，要更新领取状态
			if (otherDay && cardItem.getState() == MonthCardState.RECEIVED_VALUE) {
				cardItem.setState(MonthCardState.TO_RECEIVE_VALUE);
				addMonthCardPB(builder, cardItem, dataEntity);
			}
		}
		
		// 跨天了，把月卡entity的刷新时间更新到今日零点
		if (otherDay) {
			dataEntity.setLastRefreshTime(HawkTime.getAM0Date().getTime());
		}
		
		// 有状态发生改变的月卡，及时推给前端
		if (builder.getMonthCardInfoCount() > 0) {
			if (!removeCards.isEmpty()) {
				dataEntity.removeMultiCard(removeCards);
			}
			
			dataEntity.notifyUpdate();
			builderDataChange(playerId, dataEntity, builder, true);
			pushToPlayer(playerId, HP.code.PUSH_MONTHCARD_CHANGE_S_VALUE, builder);
			this.getDataGeter().refreshPower(playerId, PowerChangeReason.MONTHCARD_CHANGE);
		}
		
		// 续费提醒
		if (notifyCardBuilder.getCardIdCount() > 0) {
			pushToPlayer(playerId, HP.code.MONTHCARD_RENEW_NOTIFY_S_VALUE, notifyCardBuilder);
		}
		
		// 自动续费
		if (autoRenewType > 0) {
			HawkLog.logPrintln("player month card auto renew, playerId: {}, type: {}", playerId, autoRenewType);
			buyMonthCard(playerId, autoRenewType, dataEntity);
		}
	}
	
	/**
	 * 在前一天没有领取的情况下，补发月卡的每日奖励
	 *
	 * @param playerId
	 * @param cardCfg
	 */
	private void sendDailyAwardMail(String playerId, ActivityMonthCardEntity dataEntity, MonthCardActivityCfg cardCfg) {
		List<RewardItem.Builder> rewardList = this.getReward(playerId, dataEntity, cardCfg);
		if (rewardList.isEmpty()) {
			logger.info("sendDailyAwardMail failed, rewardList empty, playerId: {}, cardId: {}", playerId, cardCfg.getCardId());
			return;
		}
		int initialCardId = MonthCardActivityCfg.getInitialCardByType(cardCfg.getType());
		Object[] cardObject = new Object[]{ initialCardId };
		sendMailToPlayer(playerId, MailId.MONTHCARD_DAILY_AWARD_REFRESH, null, cardObject, cardObject, rewardList, false);
	}
	
	/**
	 * 发送月卡续费通知邮件
	 * 
	 * @param playerId
	 */
	private void sendRenewNoticeMail(String playerId, int cardId) {
		Object[] cardObject = new Object[]{cardId};
		sendMailToPlayer(playerId, MailId.MONTHCARD_RENEW_NOTICE, null, cardObject, cardObject, Collections.emptyList(), true);
	}
	
	/**
	 * 向玩家推送数据变更
	 * 
	 * @param playerId
	 * @param updateList
	 */
	private void pushDataChange(String playerId, MonthCardItem cardItem, ActivityMonthCardEntity entity) {
		HPMonthCardSync.Builder builder = HPMonthCardSync.newBuilder();
		addMonthCardPB(builder, cardItem, entity);
		builderDataChange(playerId, entity, builder, false);
		logger.info("monthCard change data push, playerId: {} ,cardItem: {}", playerId, cardItem);
		pushToPlayer(playerId, HP.code.PUSH_MONTHCARD_CHANGE_S_VALUE, builder);
	}
	
	/**
	 * 信息变化推送
	 * @param playerId
	 */
	private void pushDataChange(String playerId, ActivityMonthCardEntity entity) {
		HPMonthCardSync.Builder builder = HPMonthCardSync.newBuilder();
		builderDataChange(playerId, entity, builder, false);
		pushToPlayer(playerId, HP.code.PUSH_MONTHCARD_CHANGE_S_VALUE, builder);
	}
	
	/**
	 * 构建周卡月卡相关变化信息
	 * @param playerId
	 * @param entity
	 * @param builder
	 */
	private void builderDataChange(String playerId, ActivityMonthCardEntity entity, HPMonthCardSync.Builder builder, boolean customSync) {
		try {
			int coinCount = getExchangeCoinCount(playerId);
			builder.setExchangeCoins(coinCount);
			long endTime1 = this.getDataGeter().getBuffEndTime(playerId, Const.EffType.LIFE_TIME_CARD_641);
			builder.setEff641EndTime(endTime1);
			long endTime2 = this.getDataGeter().getBuffEndTime(playerId, Const.EffType.MONTHCARD_REWARD_DOUBLE_910);
			builder.setEff910EndTime(endTime2);
			builder.addAllTips(entity.getPlayerPoints());
			for (Entry<Integer, Integer> entry : entity.getExchangeMap().entrySet()) {
				ExchangeItem.Builder itemBuilder = ExchangeItem.newBuilder();
				itemBuilder.setExchangeId(entry.getKey());
				itemBuilder.setCount(entry.getValue());
				builder.addExchangeItem(itemBuilder);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	
	/**
	 * 获取兑换币数量
	 * @param playerId
	 * @return
	 */
	private int getExchangeCoinCount(String playerId) {
		int coins = this.getDataGeter().getItemNum(playerId, MonthCardShopCfg.getPayItemId());
		return coins;
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
	/**
	 * 激活免费特权卡
	 */
	public Result<?> activeFreeCard(String playerId) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		int freeType = MonthCardActivityCfg.getFreeType();
		if (freeType <= 0) {
			HawkLog.errPrintln("monthcard activeFreeCard failed, no free card exist, playerId: {}, freeType: {}", playerId, freeType);
			return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
		}
		
		Optional<ActivityMonthCardEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("monthcard activeFreeCard failed, entity data not exist, playerId: {}", playerId);
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		ActivityMonthCardEntity entity = opEntity.get();
		Result<?> result = buyMonthCard(playerId, freeType, entity);
		HawkLog.errPrintln("monthcard activeFreeCard over, playerId: {}, freeType: {}, result: {}", playerId, freeType, result.getStatus());
		return result;
	}

	/**
	 * 选择定制奖励
	 * @param playerId
	 * @param cardId
	 * @param rewardIds
	 */
	public int selectCustomReward(String playerId, int cardId, List<Integer> rewardIds) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<ActivityMonthCardEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("monthcard select custom reward failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		ActivityMonthCardEntity entity = opEntity.get();
		MonthCardItem cardItem = entity.getCard(cardId);
		if (cardItem != null && cardItem.getState() != MonthCardState.UNPURCHASED_VALUE && cardItem.getState() != MonthCardState.TO_RENEW_VALUE) {
			HawkLog.errPrintln("monthcard select custom reward failed, cardItem status not support, playerId: {}, cardId: {}, cardstatus: {}", playerId, cardId, cardItem.getState());
			return Status.Error.MONTHCARD_STATUS_UNMATCH_VALUE;
		}
		
		int cardStatus = MonthCardState.UNPURCHASED_VALUE;
		MonthCardActivityCfg customCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, cardId);
		if (cardItem != null) {
			cardStatus = cardItem.getState();
			if (cardItem.getState() == MonthCardState.TO_RENEW_VALUE && cardId != customCfg.getNextCard()) {
				cardId = customCfg.getNextCard();
				customCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, cardId);
			}
		}
		
		Set<Integer> rewardIdSet = new HashSet<>();
		rewardIdSet.addAll(rewardIds);
		if (customCfg == null || rewardIdSet.size() > customCfg.getMadeRewardValue()) {
			int cfgCount = customCfg == null ? 0 : customCfg.getMadeRewardValue();
			HawkLog.errPrintln("monthcard select custom reward failed, count not match, playerId: {}, cardId: {}, cfg count: {}, real count: {}", playerId, cardId, cfgCount, rewardIdSet.size());
			return Status.Error.MONTHCARD_CUSTOM_COUNT_ERROR_VALUE;
		}
		
		for (int rewardId : rewardIdSet) {
			MonthCardCustomRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardCustomRewardCfg.class, rewardId);
			if (cfg == null || cfg.getCardId() != cardId) {
				int cfgCardId = cfg == null ? 0 : cfg.getCardId();
				HawkLog.errPrintln("monthcard select custom reward failed, cardId not match, playerId: {}, rewardId: {}, cfg cardId: {}, real cardId: {}", playerId, rewardId, cfgCardId, cardId);
				return Status.Error.MONTHCARD_CUSTOM_REWARD_ERROR_VALUE;
			}
		}
		
		CustomItem item = entity.getCustomItem(cardId);
		if (item == null) {
			item = CustomItem.valueOf(cardId);
			item.setRewardIdList(rewardIdSet);
			entity.addCustomItem(item);
		} else {
			item.setRewardIdList(rewardIdSet);
			entity.notifyUpdate();
		}
		
		entity.updateCustomLatest(customCfg.getType(), item);
		if (cardItem == null) {
			cardItem = MonthCardItem.valueOf(cardId, MonthCardState.UNPURCHASED_VALUE);
		}
		//发送返回信息
		pushDataChange(playerId, cardItem, entity);
		
		//定制特权卡选择物品
		Map<String, Object> param = new HashMap<>();
        param.put("cardId", cardId);                  //特权卡ID
        param.put("cardType", customCfg.getType());   //特权卡类型
        param.put("cardStatus", cardStatus);          //特权卡状态
        param.put("selectCount", rewardIdSet.size()); //选择定制奖励种类数
        param.put("selectRewards", SerializeHelper.collectionToString(rewardIdSet, ",")); //选择定制奖励ID
        getDataGeter().logActivityCommon(playerId, LogInfoType.month_card_custom, param);
		        
		HawkLog.logPrintln("monthcard select custom reward success, playerId: {}, cardId: {}, rewardIds: {}", playerId, cardId, rewardIds);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 特权兑换商店兑换
	 * @param playerId
	 * @param exchangeId
	 * @param count
	 * @return
	 */
	public int exchange(String playerId, int exchangeId, int count) {
		if (count <= 0) {
			HawkLog.errPrintln("monthcard exchange failed, playerId: {}, count: {}", playerId, count);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		MonthCardShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardShopCfg.class, exchangeId);
		if (cfg == null) {
			HawkLog.errPrintln("monthcard exchange failed, playerId: {}, exchangeId: {}", playerId, exchangeId);
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		Optional<ActivityMonthCardEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("monthcard exchange failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		ActivityMonthCardEntity entity = opEntity.get();
		int exhangeCount = entity.getExchangeMap().getOrDefault(exchangeId, 0);
		int newCount = exhangeCount + count;
		//限购
		if (cfg.isLimitType() && newCount > cfg.getShopQuota()) {
			HawkLog.errPrintln("monthcard exchange failed, playerId: {}, exchangeId: {}, oldCount: {}, newCount: {}", playerId, exchangeId, exhangeCount, newCount);
			return Status.Error.MONTHCARD_EXCHANGE_LIMIT_VALUE;
		}
		
		List<RewardItem.Builder> consumeItems = RewardHelper.toRewardItemImmutableList(cfg.getPayItem());
		// 判断道具足够否
		boolean flag = this.getDataGeter().cost(playerId, consumeItems, count, Action.MONTH_CARD_EXCHANGE, false);
		if (!flag) {
			HawkLog.errPrintln("monthcard exchange failed, playerId: {}, exchangeId: {}, count: {}, coinCount remain: {}", playerId, exchangeId, count, getExchangeCoinCount(playerId));
			return Status.Error.MONTHCARD_COIN_NOT_ENOUGH_VALUE;
		}
		
		List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemImmutableList(cfg.getGetItem());
		this.getDataGeter().takeReward(playerId, rewardItems, count, Action.MONTH_CARD_EXCHANGE, true, RewardOrginType.ACTIVITY_REWARD);
		entity.getExchangeMap().put(exchangeId, newCount);
		entity.notifyUpdate();
		
		//发送返回信息
		pushDataChange(playerId, entity);
		
		//记录特权兑换商店兑换数据：花了多少兑换币兑换了多少什么东西
		Map<String, Object> param = new HashMap<>();
        param.put("exchangeId", exchangeId); //兑换商品ID
        param.put("exchangeCount", count);   //兑换商品数量
        param.put("cost", consumeItems.get(0).getItemCount() * count); //消耗兑换币数量
        param.put("itemId", rewardItems.get(0).getItemId());           //兑换获得的物品ID
        param.put("itemCount", rewardItems.get(0).getItemCount() * count); //兑换获得的物品数量
        getDataGeter().logActivityCommon(playerId, LogInfoType.month_card_exchange, param);
         
		HawkLog.logPrintln("monthcard exchange success, playerId: {}, exchangeId: {}, old exchangeCount: {}, count: {}", playerId, exchangeId, exhangeCount, count);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	 /**
     * 玩家勾选信息处理
     * @param playerId
     * @param tipsList
     */
    public void updateShopItemTips(String playerId, List<PBShopItemTip> tipsList) {
    	if (tipsList.isEmpty()) {
    		HawkLog.errPrintln("monthcard updateShopItemTips failed, param error, playerId: {}", playerId);
    		return;
    	}
    	if (!isOpening(playerId)) {
			return;
		}
        Optional<ActivityMonthCardEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("monthcard updateShopItemTips failed, entity data not exist, playerId: {}", playerId);
			return;
		}
		
		ActivityMonthCardEntity entity = opEntity.get();
        for (PBShopItemTip tip : tipsList) {
            updateOneTip(entity, tip.getId(), tip.getTip());
        }

        PBShopItemTipsResp.Builder builder = PBShopItemTipsResp.newBuilder();
        builder.addAllTips(entity.getPlayerPoints());
        pushToPlayer(playerId, HP.code2.MONTH_CARD_TIP_RESP_VALUE, builder);
    }

    private void updateOneTip(ActivityMonthCardEntity entity, int id, boolean isSelected) {
    	MonthCardShopCfg config = HawkConfigManager.getInstance().getConfigByKey(MonthCardShopCfg.class, id);
        if (config == null) {
            return;
        }
        
        if (isSelected) {
            entity.removeTips(id);
        } else {
            entity.addTips(id);
        }
    }
    
    private void removeCustomItem(ActivityMonthCardEntity entity, int cardId) {
    	MonthCardActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, cardId);
    	if (!cfg.isCustomType()) {
    		return;
    	}
    	
    	int count = 0;
    	int curCardId = MonthCardActivityCfg.getInitialCard(cardId);
    	do {
    		entity.removeCustomItem(curCardId);
    		cfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, curCardId);
    		count++;
    		if (cfg.getNextCard() == curCardId || count >= 10) {
    			break;
    		}
    		curCardId = cfg.getNextCard();
    	} while(true);
    }
    
    /**
     * 周卡月卡购买或续费检测
     * @return
     */
    public int buyMonthCardCheck(String playerId, int cardType, boolean paySuccess, String payGiftId) {
    	return buyMonthCardCheck(playerId, cardType, paySuccess, payGiftId, false);
    }
    
    /**
     * 周卡月卡购买或续费检测
     * @param playerId
     * @param cardType
     * @param paySuccess
     * @param payGiftId
     * @param byItem 是否是通过道具激活或免费提升
     * @return
     */
    private int buyMonthCardCheck(String playerId, int cardType, boolean paySuccess, String payGiftId, boolean byItem) {
		boolean isSell = MonthCardActivityCfg.inSell(cardType, HawkTime.getMillisecond());
		if(!isSell){
			HawkLog.errPrintln("MSDK buy item failed, the type monthCard not in sell, playerId: {}, type: {}", playerId, cardType);
			return Status.Error.MONTHCARD_NOT_IN_SELL_VALUE;
		}
		
		if (!this.getDataGeter().monthCardFrontBuildCheck(playerId, cardType)) {
			logger.error("monthCard active failed, monthCard condition not match, playerId: {}, type: {}", playerId, cardType);
			return Status.Error.MONTHCARD_BUILD_LEVEL_ERROR_VALUE;
		}
		
		Optional<ActivityMonthCardEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		ActivityMonthCardEntity dataEntity = opEntity.get();
		List<MonthCardItem> list = dataEntity.getEfficientCardList(cardType);
		// 月卡生效期间无须购买再购买同类月卡
		if (!list.isEmpty()) {
			HawkLog.errPrintln("MSDK buy item failed, the type monthCard has already bought, playerId: {}, type: {}", playerId, cardType);
			return byItem ? Status.Error.MONTHCARD_ACTIVE_BY_ITEM_ERROR_VALUE : Status.Error.ACTIVITY_MONTHCARD_ACTIVATED_VALUE;
		}

		// 半价检测
		if (!HawkOSOperator.isEmptyString(payGiftId)) {
			int code = this.getDataGeter().monthCardGoldPrivilegeCheck(playerId, cardType, payGiftId, paySuccess);
			if (code != 0) {
				return code;
			}
		}
		
		//定制类特权卡，单独判断
		if (MonthCardActivityCfg.isCustomTypeCard(cardType)) {
			int code = this.canPurchaseCustomCard(playerId, cardType);
			if (code != 0) {
				return code;
			}
		}
		
		return 0;
	}
    
}
