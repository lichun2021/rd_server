package com.hawk.activity.type.impl.dailyrechargenew;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringJoiner;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.ShareProsperityEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.game.protocol.Activity.RechargeBuyGiftList;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.dailyrechargenew.cfg.RechargeBuyNewAchieveCfg;
import com.hawk.activity.type.impl.dailyrechargenew.cfg.RechargeBuyNewGiftCfg;
import com.hawk.activity.type.impl.dailyrechargenew.cfg.RechargeBuyNewGiftRewardCfg;
import com.hawk.activity.type.impl.dailyrechargenew.entity.DailyRechargeNewEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 今日累充活动（新版）
 * 
 * @author lating
 *
 */
public class DailyRechargeNewActivity extends ActivityBase implements AchieveProvider {

	public DailyRechargeNewActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.DAILY_RECHARGE_NEW_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.DAILY_RECHARGE_NEW_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DailyRechargeNewActivity activity = new DailyRechargeNewActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DailyRechargeNewEntity> queryList = HawkDBManager.getInstance()
				.query("from DailyRechargeNewEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DailyRechargeNewEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DailyRechargeNewEntity entity = new DailyRechargeNewEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return !isHidden(playerId);
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_DAILY_RECHARGE_NEW, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	/**
	 * 初始化成就信息
	 * 
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<DailyRechargeNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		DailyRechargeNewEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		// 初始添加成就项
		List<AchieveItem> items = new ArrayList<AchieveItem>();
		ConfigIterator<RechargeBuyNewAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(RechargeBuyNewAchieveCfg.class);
		while (configIterator.hasNext()) {
			RechargeBuyNewAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			items.add(item);
		}
		
		entity.resetItemList(items);
		entity.setRefreshTime(HawkTime.getMillisecond());
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}
	
	public void onPlayerLogin(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<DailyRechargeNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		DailyRechargeNewEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		
		syncGiftItemInfo(playerId);
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		String playerId = event.getPlayerId();
		Optional<DailyRechargeNewEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		
		long now = HawkTime.getMillisecond();
		DailyRechargeNewEntity dataEntity = opDataEntity.get();
		if (event.isCrossDay() || !HawkTime.isSameDay(now, dataEntity.getRefreshTime())) {
			List<AchieveItem> items = new ArrayList<AchieveItem>();
			ConfigIterator<RechargeBuyNewAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(RechargeBuyNewAchieveCfg.class);
			while (achieveIterator.hasNext()) {
				RechargeBuyNewAchieveCfg cfg = achieveIterator.next();
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				items.add(item);
			}
			
			dataEntity.resetItemList(items);
			dataEntity.resetGiftItem();
			dataEntity.setRefreshTime(now);
			// 推送给客户端
			AchievePushHelper.pushAchieveUpdate(playerId, dataEntity.getItemList());
			syncGiftItemInfo(playerId);
		}
	}
	
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<DailyRechargeNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		
		DailyRechargeNewEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public RechargeBuyNewAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(RechargeBuyNewAchieveCfg.class, achieveId);
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		
	}
	
	/**
	 * 同步特价礼包信息
	 * 
	 * @param playerId
	 * @return
	 */
	public int syncGiftItemInfo(String playerId) {
		if (!isOpening(playerId)) {
			HawkLog.errPrintln("DailyRechargeNewActivity syncGiftItemInfo failed, errCode: {}, playerId: {}", Status.Error.ACTIVITY_NOT_OPEN_VALUE, playerId);
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<DailyRechargeNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("DailyRechargeNewActivity syncGiftItemInfo failed, errCode: {}, playerId: {}", Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE, playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		DailyRechargeNewEntity entity = opEntity.get();
		RechargeBuyGiftList.Builder builder = RechargeBuyGiftList.newBuilder();
		for (GiftItem giftItem : entity.getGiftItemList()) {
			builder.addGift(giftItem.toBuilder());
		}
		
		pushToPlayer(playerId, HP.code.RECHARGE_BUY_GIFG_INFO_SYNC_VALUE, builder);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 选择定制奖励
	 * 
	 * @param playerId
	 * @param giftId
	 * @param rewardIds
	 */
	public int selectGiftReward(String playerId, int giftId, List<Integer> rewardIds) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<DailyRechargeNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("selectGiftReward failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		DailyRechargeNewEntity entity = opEntity.get();
		RechargeBuyNewGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(RechargeBuyNewGiftCfg.class, giftId);
		if (giftCfg == null) {
			HawkLog.errPrintln("selectGiftReward failed, gift config error, playerId: {}, giftId: {}", playerId, giftId);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		if (giftCfg.getChooseRewardCount() != rewardIds.size()) {
			HawkLog.errPrintln("selectGiftReward failed, choose count not match, playerId: {}, giftId: {}, choose count: {}", playerId, giftId, rewardIds.size());
			return Status.Error.SELECT_REWARD_COUNT_ERROR_VALUE;
		}
		
		Map<Integer, Integer> chooseRewardMap = new HashMap<Integer, Integer>();
		for (int rewardId : rewardIds) {
			RechargeBuyNewGiftRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RechargeBuyNewGiftRewardCfg.class, rewardId);
			if (cfg == null) {
				HawkLog.errPrintln("selectGiftReward failed, reward config error, playerId: {}, giftId: {}, rewardId: {}", playerId, giftId, rewardId);
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
			
			int count = chooseRewardMap.getOrDefault(cfg.getRewardType(), 0);
			chooseRewardMap.put(cfg.getRewardType(), count + 1);
		}
		
		for (Entry<Integer, Integer> entry : chooseRewardMap.entrySet()) {
			if (giftCfg.getChooseIdCount(entry.getKey()) != entry.getValue()) {
				HawkLog.errPrintln("selectGiftReward failed, reward count not match, playerId: {}, giftId: {}, chooseId: {}, count: {}", playerId, giftId, entry.getKey(), entry.getValue());
				return Status.Error.SELECT_REWARD_COUNT_ERROR_VALUE;
			}
		}
		
		GiftItem item = entity.getGiftItem(giftId);
		if (item == null) {
			item = new GiftItem();
			item.setGiftId(giftId);
			item.setRewardIdList(rewardIds);
			entity.addGiftItem(item);
		} else {
			item.setRewardIdList(rewardIds);
			entity.notifyUpdate();
		}
		
		syncGiftItemInfo(playerId);
		
		HawkLog.logPrintln("selectGiftReward success, playerId: {}, giftId: {}, rewardIds: {}", playerId, giftId, rewardIds);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 购买特价礼包
	 * 
	 * @param playerId
	 * @param giftId
	 */
	public int buyGift(String playerId, int giftId, int protocol) {
		if (!isOpening(playerId)) {
			HawkLog.errPrintln("buyGift failed, errCode: {}, playerId: {}, giftId: {}", Status.Error.ACTIVITY_NOT_OPEN_VALUE, playerId, giftId);
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		RechargeBuyNewGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(RechargeBuyNewGiftCfg.class, giftId);
		if (giftCfg == null) {
			HawkLog.errPrintln("buyGift failed, gift config error, playerId: {}, giftId: {}", playerId, giftId);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		Optional<DailyRechargeNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("buyGift failed, errCode: {}, playerId: {}, giftId: {}", Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE, playerId, giftId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		DailyRechargeNewEntity entity = opEntity.get();
		GiftItem giftItem = entity.getGiftItem(giftId);
		if (giftItem == null) {
			HawkLog.errPrintln("buyGift failed, gift reward not select, playerId: {}, giftId: {}", playerId, giftId);
			return Status.Error.RECHARGE_BUY_GIFT_NOT_SELECT_VALUE;
		}
		
		if (giftItem.getPurchaseTime() > 0) {
			HawkLog.errPrintln("buyGift failed, gift item bought, playerId: {}, giftId: {}, buyTime: {}", playerId, giftId, giftItem == null ? 0 : giftItem.getPurchaseTime());
			return Status.Error.RECHARGE_BUY_GIFT_BOUGHT_VALUE;
		}
		
		if (giftItem.getRewardIdList().size() != giftCfg.getChooseRewardCount()) {
			HawkLog.errPrintln("buyGift failed, gift select reward error, playerId: {}, giftId: {}, selected: {}", playerId, giftId, giftItem.getRewardIdList());
			return Status.Error.SELECT_REWARD_COUNT_ERROR_VALUE;
		}
		
		AchieveItem achieveItem = null;
		for (AchieveItem item : entity.getItemList()) {
			if (item.getAchieveId() == giftCfg.getAchieveId()) {
				achieveItem = item;
				break;
			}
		}
		
		if (achieveItem == null || achieveItem.getState() == AchieveState.NOT_ACHIEVE_VALUE) {
			HawkLog.errPrintln("buyGift failed, achieve condition error, playerId: {}, giftId: {}", playerId, giftId);
			return Status.Error.RECHARGE_BUY_GIFT_COND_ERROR_VALUE;
		}
		
		boolean success = getDataGeter().consumeItems(playerId, giftCfg.getCostItems(), protocol, Action.DAILY_RECHARGE_BUY_CONSUME);
		if (!success) {
			HawkLog.errPrintln("buyGift failed, consumeItems not enought, playerId: {}, giftId: {}", playerId, giftId);
			return 0; // 这里不要去假设消耗的时什么东西，不足时在掉消耗接口时已经返回错误提示了，所以这里可以不用去管到底是什么不足的问题
		}
		
		giftItem.setPurchaseTime(HawkTime.getMillisecond());
		entity.notifyUpdate();
		List<RewardItem.Builder> rewardList = giftCfg.getRewardList();
		StringJoiner sj = new StringJoiner(",");
		
		for (int rewardId : giftItem.getRewardIdList()) {
			RechargeBuyNewGiftRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RechargeBuyNewGiftRewardCfg.class, rewardId);
			if (cfg == null) {
				continue;
			}
			
			sj.add(String.valueOf(rewardId));
			rewardList.addAll(cfg.getRewardList());
		}
		
		getDataGeter().takeReward(playerId, rewardList, Action.DAILY_RECHARGE_BUY_REWARD, true);
		
		syncGiftItemInfo(playerId);
		
		// 记录tlog
		getDataGeter().logDailyRechargeNew(playerId, giftId, sj.toString());
		
		HawkLog.logPrintln("DailyRechargeNewActivity buy gift success, playerId: {}, giftId: {}", playerId, giftId);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
	@Subscribe
	public void onEvent(ShareProsperityEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<DailyRechargeNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		DailyRechargeNewEntity entity = opEntity.get();
		AchieveManager.getInstance().onSpecialAchieve(this, playerId, entity.getItemList(), AchieveType.ACCUMULATE_DIAMOND_RECHARGE, event.getDiamondNum());
		entity.notifyUpdate();
	}
	
}
