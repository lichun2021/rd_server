package com.hawk.activity.type.impl.onermbpurchase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.OneRMBPurchaseEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.activity.type.impl.onermbpurchase.cfg.OneRMBPurchaseActivityKVCfg;
import com.hawk.activity.type.impl.onermbpurchase.cfg.OneRMBPurchaseCfg;
import com.hawk.activity.type.impl.onermbpurchase.entity.OneRMBPurchaseEntity;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Activity.OneRMBPurchaseInfo;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;

/**
 * 一元购活动
 * 
 * @author lating
 *
 */
public class OneRMBPurchaseActivity extends ActivityBase {

	public OneRMBPurchaseActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.ONE_RMB_PURCHARSE_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		OneRMBPurchaseActivity activity = new OneRMBPurchaseActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<OneRMBPurchaseEntity> queryList = HawkDBManager.getInstance()
				.query("from OneRMBPurchaseEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			OneRMBPurchaseEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		OneRMBPurchaseEntity entity = new OneRMBPurchaseEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		
		pushToPlayer(playerId, HP.code.ONE_RMB_PURCHASE_INFO_PUSH_VALUE, getOneRMBPurchaseInfoBuilder());
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ONE_RMB_PURCHASE_INIT, ()-> {
				Optional<OneRMBPurchaseEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return;
				}
				
				pushToPlayer(playerId, HP.code.ONE_RMB_PURCHASE_INFO_PUSH_VALUE, getOneRMBPurchaseInfoBuilder());
			});
		}
	}
	
	@Override
	public boolean isOpening(String playerId) {
		if (!super.isOpening(playerId)) {
			return false;
		}
		
		int termId = getActivityTermId(playerId);
		long endTime = getTimeControl().getEndTimeByTermId(termId, playerId);
		long now = HawkTime.getMillisecond();
		if (now >= endTime) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 跨天事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<OneRMBPurchaseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		long now = HawkTime.getMillisecond();
		OneRMBPurchaseEntity entity = opEntity.get();
		if (event.isCrossDay() && !HawkTime.isSameDay(entity.getRefreshTime(), now)) {
			entity.setRefreshTime(now);
		}
		
		pushToPlayer(event.getPlayerId(), HP.code.ONE_RMB_PURCHASE_INFO_PUSH_VALUE, getOneRMBPurchaseInfoBuilder());
	}
	
	/***
	 * 购买事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(OneRMBPurchaseEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		Optional<OneRMBPurchaseEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		
		String playerId = event.getPlayerId();
		int currentDay = getCurrentDays();
		OneRMBPurchaseCfg awardCfg = OneRMBPurchaseCfg.getCfgByDay(currentDay);
		if (awardCfg == null) {
			HawkLog.errPrintln("OneRMBPurchase config error, playerId: {}, day: {}", playerId, currentDay);
			return;
		}
		
		
		// 发奖
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		rewardList.addAll(awardCfg.getRewardList());
		Map<RewardItem.Builder, Integer> randomRewardMap = awardCfg.getRandomRewardMap();
		rewardList.addAll(randomRewardMap.keySet());
		
		OneRMBPurchaseActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OneRMBPurchaseActivityKVCfg.class);
		String firstReward = cfg.getFirstBuyRewards();
		if (!HawkOSOperator.isEmptyString(firstReward)) {
			// 防止redis异常阻断正常流程，所以这里要加try catch
			try {
				String key = getRedisKey() + ":" + playerId;
				String extraReward = ActivityLocalRedis.getInstance().get(key);
				if (HawkOSOperator.isEmptyString(extraReward)) {
					List<RewardItem.Builder> rewardBuilder = RewardHelper.toRewardItemList(firstReward);
					rewardList.addAll(rewardBuilder);
					long expireTime = getTimeControl().getEndTimeByTermId(getActivityTermId(playerId), playerId) - HawkTime.getMillisecond();
					ActivityLocalRedis.getInstance().set(key, firstReward, (int) expireTime / 1000);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (!rewardList.isEmpty()) {
			ActivityReward reward = new ActivityReward(rewardList, Action.ONE_RMB_PURCHASE);
			reward.setOrginType(RewardOrginType.SHOPPING_GIFT, getActivityId());
			reward.setAlert(true);
			postReward(playerId, reward, false);
		}
		
		// 收据邮件
		sendMailToPlayer(playerId, MailId.ONE_RMB_PURCHASE, null, null, null, rewardList, true);
		
		// 稀有道具走马灯
		for (Entry<RewardItem.Builder, Integer> entry : randomRewardMap.entrySet()) {
			if (entry.getValue() == 1) {
				String playerName = ActivityManager.getInstance().getDataGeter().getPlayerName(playerId);
				RewardItem.Builder rewardItem = entry.getKey();
				int itemType = rewardItem.getItemType() < 10000 ? rewardItem.getItemType() * 10000 : rewardItem.getItemType();
				String itemInfoStr = String.format("%d_%d_%d", itemType, rewardItem.getItemId(), rewardItem.getItemCount());
				sendBroadcast(Const.NoticeCfgId.ONE_RMB_PURCHASE, null, playerName, itemInfoStr);
			}
		}
		
		pushToPlayer(playerId, HP.code.ONE_RMB_PURCHASE_INFO_PUSH_VALUE, getOneRMBPurchaseInfoBuilder());
	}
	
	/**
	 * 活动开始后当前天数
	 * 
	 * @return
	 */
	private int getCurrentDays() {
		int termId = getTimeControl().getActivityTermId(HawkTime.getMillisecond());
		long openTime = getTimeControl().getStartTimeByTermId(termId); 
		int crossHour = getDataGeter().getCrossDayHour();
		int betweenDays = HawkTime.getCrossDay(HawkTime.getMillisecond(), openTime, crossHour);
		return betweenDays + 1;
	}
	
	private OneRMBPurchaseInfo.Builder getOneRMBPurchaseInfoBuilder() {
		OneRMBPurchaseInfo.Builder builder = OneRMBPurchaseInfo.newBuilder();
		builder.setDay(getCurrentDays());
		return builder;
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
	/**
	 * 获取redis key
	 * @return
	 */
	private String getRedisKey() {
		 return ActivityRedisKey.ONE_RMB_EXTRA_REWARD + ":" + String.valueOf(this.getActivityTermId());
	}

}
