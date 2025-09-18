package com.hawk.activity.type.impl.pioneergift;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.PioneerGifBuyTimesEvent;
import com.hawk.activity.event.impl.PioneerGiftPurchaseEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.game.protocol.Activity.PioneerGiftActivityInfo;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.activity.type.ActivityType;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.pioneergift.cfg.PioneerGiftAccRewardCfg;
import com.hawk.activity.type.impl.pioneergift.cfg.PioneerGiftActivityKVCfg;
import com.hawk.activity.type.impl.pioneergift.cfg.PioneerGiftActivityLayoutCfg;
import com.hawk.activity.type.impl.pioneergift.cfg.PioneerGiftRewardCfg;
import com.hawk.activity.type.impl.pioneergift.entity.PioneerGiftEntity;
import com.hawk.activity.type.impl.pioneergift.entity.PurchaseItem;

/**
 * 先锋豪礼活动
 * 
 * @author lating
 *
 */
public class PioneerGiftActivity extends ActivityBase implements AchieveProvider {

	public PioneerGiftActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PIONEER_GIFT_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PioneerGiftActivity activity = new PioneerGiftActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PioneerGiftEntity> queryList = HawkDBManager.getInstance()
				.query("from PioneerGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PioneerGiftEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PioneerGiftEntity entity = new PioneerGiftEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerLogin(String playerId) {

	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.PIONEER_GIFT_INIT, () -> {
				Optional<PioneerGiftEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return;
				}
				initAchieveInfo(playerId);
				pushToPlayer(playerId, HP.code.PIONEER_GIFT_INFO_PUSH_VALUE, getPioneerGiftInfoBuilder(playerId));
			});
		}
	}

	/**
	 * 跨天事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}

		int termId = getActivityTermId(playerId);
		long endTime = getTimeControl().getEndTimeByTermId(termId, playerId);
		long now = HawkTime.getMillisecond();
		if (now >= endTime) {
			return;
		}

		Optional<PioneerGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		PioneerGiftEntity entity = opEntity.get();
		// 在线跨天
		if (event.isCrossDay() || !HawkTime.isSameDay(entity.getLatestPurchaseTime(), now)) {
			Iterator<PurchaseItem> iterator = entity.getItemPurchaseList().iterator();
			while (iterator.hasNext()) {
				PurchaseItem item = iterator.next();
				if (item.getPurchaseTime() > 0) {
					iterator.remove();
				}
			}

			entity.notifyUpdate();
			
			if (event.isCrossDay()) {
				pushToPlayer(playerId, HP.code.PIONEER_GIFT_INFO_PUSH_VALUE, getPioneerGiftInfoBuilder(playerId));
			}
			
			return;
		}
	}

	/***
	 * 购买事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(PioneerGiftPurchaseEvent event) {
		String playerId = event.getPlayerId();
		int type = event.getType();
		if (!isOpening(playerId)) {
			HawkLog.errPrintln("PioneerGiftPurchaseEvent failed, activity closed, playerId: {}, type: {}", playerId,
					type);
			return;
		}

		Optional<PioneerGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("PioneerGiftPurchaseEvent failed, entity data not exist, playerId: {}, type: {}",
					playerId, type);
			return;
		}

		PioneerGiftEntity entity = opEntity.get();
		PurchaseItem item = entity.getPurchaseItem(type);
		if (item == null) {
			// 这里出了异常情况，要做人工处理
			HawkLog.errPrintln("PioneerGiftPurchaseEvent failed, item not selected, playerId: {}, type: {}", playerId,
					type);
			return;
		}

		long now = HawkTime.getMillisecond();
		// 同一天同一个礼包买两次的情况，是否要做特殊处理
		if (HawkTime.isSameDay(now, item.getPurchaseTime())) {
			HawkLog.errPrintln(
					"PioneerGiftPurchaseEvent failed, item purchased today, playerId: {}, type: {}, giftId: {}, time: {}",
					playerId, type, item.getGiftId(), item.getPurchaseTime());
			return;
		}

		// 发货
		PioneerGiftRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PioneerGiftRewardCfg.class,
				item.getGiftId());
		if (cfg == null) {
			HawkLog.errPrintln(
					"PioneerGiftPurchaseEvent failed, PioneerGiftRewardCfg not exist, playerId: {}, giftId: {}",
					playerId, item.getGiftId());
			return;
		}

		List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(cfg.getReward());
		if (!rewardList.isEmpty()) {
			ActivityReward reward = new ActivityReward(rewardList, Action.PIONEER_GIFT_BUY);
			reward.setOrginType(RewardOrginType.SHOPPING_GIFT, getActivityId());
			reward.setAlert(true);
			postReward(playerId, reward, false);
		}

		item.setPurchaseTime(now);
		if (!HawkTime.isSameDay(now, entity.getLatestPurchaseTime())) {
			entity.setAccDay(entity.getAccDay() + 1);
			ActivityManager.getInstance().postEvent(PioneerGifBuyTimesEvent.valueOf(playerId, cfg.getGiftId()));
		}

		entity.setLatestPurchaseTime(now);
		entity.notifyUpdate();
		pushToPlayer(playerId, HP.code.PIONEER_GIFT_INFO_PUSH_VALUE, getPioneerGiftInfoBuilder(playerId));

		int termId = getTimeControl().getActivityTermId(now);
		getDataGeter().logPioneerGiftBuy(playerId, termId, type, item.getGiftId());

		HawkLog.logPrintln("PioneerGiftPurchaseEvent success, playerId: {}, type: {}, giftId: {}", playerId, type,
				item.getGiftId());
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}

	/**
	 * 同步礼包信息
	 * 
	 * @param playerId
	 */
	public int syncPioneerGiftInfo(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.PIONEER_ACTIVITY_NOT_OPEN_VALUE;
		}

		Optional<PioneerGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("syncPioneerGiftInfo failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.PIONEER_DATA_ERROR_VALUE;
		}
		pushToPlayer(playerId, HP.code.PIONEER_GIFT_INFO_PUSH_VALUE, getPioneerGiftInfoBuilder(playerId));

		return Status.SysError.SUCCESS_OK_VALUE;
	}
	

	@Override
	public void syncActivityDataInfo(String playerId) {
		try{
			if (!isOpening(playerId)) {
				return;
			}
			Optional<PioneerGiftEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			pushToPlayer(playerId, HP.code.PIONEER_GIFT_INFO_PUSH_VALUE, getPioneerGiftInfoBuilder(playerId));			
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}
	/**
	 * 领取免费礼包
	 * 
	 * @param playerId
	 * @return
	 */
	public int receiveFreeGift(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.PIONEER_ACTIVITY_NOT_OPEN_VALUE;
		}

		Optional<PioneerGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("takeFreeReward failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.PIONEER_DATA_ERROR_VALUE;
		}

		PioneerGiftEntity entity = opEntity.get();
		long now = HawkTime.getMillisecond();
		if (HawkTime.isSameDay(entity.getFreeTakenTime(), now)) {
			HawkLog.errPrintln("takeFreeReward failed, today has received, playerId: {}, receive time: {}", playerId,
					entity.getFreeTakenTime());
			return Status.Error.PIONEER_FREE_GIFT_TAKEN_VALUE;
		}

		PioneerGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PioneerGiftActivityKVCfg.class);
		List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(cfg.getFreeGift());
		if (!rewardList.isEmpty()) {
			ActivityReward reward = new ActivityReward(rewardList, Action.PIONEER_GIFT_FREE);
			reward.setOrginType(RewardOrginType.ACTIVITY_REWARD, getActivityId());
			reward.setAlert(true);
			postReward(playerId, reward, false);
		}

		entity.setFreeTakenTime(now);
		entity.notifyUpdate();
		pushToPlayer(playerId, HP.code.PIONEER_GIFT_INFO_PUSH_VALUE, getPioneerGiftInfoBuilder(playerId));

		HawkLog.logPrintln("takeFreeReward success, playerId: {}", playerId);

		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 选择礼包
	 * 
	 * @param playerId
	 * @param type 在 pioneer_gift_layout.xml 取 id
	 *            礼包档次
	 * @param giftId 在pioneer_gift_reward.xml 取 giftId
	 *            礼包ID
	 */
	public int selectPioneerGift(String playerId, int type, int giftId) {
		if (!isOpening(playerId)) {
			return Status.Error.PIONEER_ACTIVITY_NOT_OPEN_VALUE;
		}

		Optional<PioneerGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("selectPioneerGift failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.PIONEER_DATA_ERROR_VALUE;
		}
		
		//判断pioneer_gift_reward.xml 里存在
		PioneerGiftRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PioneerGiftRewardCfg.class, giftId);
		if (cfg == null ) {
			HawkLog.errPrintln("selectPioneerGift failed, config error, playerId: {}, type: {}, giftId: {}", playerId,
					type, giftId);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		//判断pioneer_gift_layout.xml 里存在
		PioneerGiftActivityLayoutCfg layoutCfg = HawkConfigManager.getInstance().getConfigByKey(PioneerGiftActivityLayoutCfg.class, type);
		if(layoutCfg == null || !layoutCfg.getPackages().contains(giftId)) {
			HawkLog.errPrintln("selectPioneerGift failed, layoutCfg error, playerId: {}, type: {}, giftId: {}", playerId, type, giftId);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		PioneerGiftEntity entity = opEntity.get();
		PurchaseItem item = entity.getPurchaseItem(type);
		if (item == null) {
			item = PurchaseItem.valueOf(type, giftId);
			entity.addPurchaseItem(item);
		}

		if (HawkTime.isSameDay(item.getPurchaseTime(), HawkApp.getInstance().getCurrentTime())) {
			HawkLog.logPrintln(
					"selectPioneerGift failed, has purchased today, playerId: {}, giftId: {}, type: {}, time: {}",
					playerId, giftId, type, item.getPurchaseTime());
			return Status.Error.PIONEER_GIFT_PURCHASED_VALUE;
		}

		item.setGiftId(giftId);
		
		entity.notifyUpdate();
		pushToPlayer(playerId, HP.code.PIONEER_GIFT_INFO_PUSH_VALUE, getPioneerGiftInfoBuilder(playerId));

		HawkLog.logPrintln("selectPioneerGift success, playerId: {}, giftId: {}, type: {}", playerId, giftId, type);

		return Status.SysError.SUCCESS_OK_VALUE;
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

	/**
	 * 获取活动信息
	 * 
	 * @param playerId
	 * @return
	 */
	private PioneerGiftActivityInfo.Builder getPioneerGiftInfoBuilder(String playerId) {
		PioneerGiftActivityInfo.Builder builder = PioneerGiftActivityInfo.newBuilder();
		Optional<PioneerGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("synctPioneerGiftInfo failed, entity data not exist, playerId: {}", playerId);
			return builder;
		}

		PioneerGiftEntity entity = opEntity.get();
		builder.setDay(getCurrentDays());
		builder.setAccDay(entity.getAccDay());
		builder.setFreeGiftTaken(HawkTime.isSameDay(entity.getFreeTakenTime(), HawkApp.getInstance().getCurrentTime()));
		for (PurchaseItem item : entity.getItemPurchaseList()) {
			builder.addGiftInfo(item.toBuilder());
		}

		return builder;
	}

	/**
	 * 判断该档次是否已选择礼包
	 * 
	 * @param type
	 * @return
	 */
	public int selectGiftCheck(String playerId, int type) {
		if (!isOpening(playerId)) {
			HawkLog.errPrintln("selectGiftCheck failed, activity not open, playerId: {}, type: {}", playerId, type);
			return Status.Error.PIONEER_ACTIVITY_NOT_OPEN_VALUE;
		}

		Optional<PioneerGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("selectGiftCheck failed, entity data not exist, playerId: {}, type: {}", playerId, type);
			return Status.Error.PIONEER_DATA_ERROR_VALUE;
		}

		PioneerGiftEntity entity = opEntity.get();
		PurchaseItem item = entity.getPurchaseItem(type);
		if (item == null) {
			HawkLog.errPrintln("selectGiftCheck failed, PurchaseItem not selected, playerId: {}, type: {}", playerId,
					type);
			return Status.Error.PIONEER_GIFT_NOT_SELECTED_VALUE;
		}

		if (HawkTime.isSameDay(item.getPurchaseTime(), HawkTime.getMillisecond())) {
			HawkLog.errPrintln("selectGiftCheck failed, item purchased today, playerId: {}, type: {}, purchaseTime: {}",
					playerId, type, item.getPurchaseTime());
			return Status.Error.PIONEER_GIFT_PURCHASED_VALUE;
		}

		return Status.SysError.SUCCESS_OK_VALUE;
	}

	private void initAchieveInfo(String playerId) {
		Optional<PioneerGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		PioneerGiftEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始化完成成就的成就
		ConfigIterator<PioneerGiftAccRewardCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(PioneerGiftAccRewardCfg.class);
		while (configIterator.hasNext()) {
			PioneerGiftAccRewardCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		entity.notifyUpdate();
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return true;
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {

		Optional<PioneerGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		PioneerGiftEntity entity = opEntity.get();
		if (entity.getItemPurchaseList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(PioneerGiftAccRewardCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.PIONEER_ACC_GIFT_TAKE;
	}

}
