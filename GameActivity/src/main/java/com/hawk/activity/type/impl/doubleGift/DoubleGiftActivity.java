package com.hawk.activity.type.impl.doubleGift;

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
import com.hawk.activity.event.impl.DoubleGiftDayBuyEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Activity.DoubleGiftActivityInfo;
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
import com.hawk.activity.type.impl.doubleGift.cfg.DoubleGiftAchieveCfg;
import com.hawk.activity.type.impl.doubleGift.cfg.DoubleGiftActivityLayoutCfg;
import com.hawk.activity.type.impl.doubleGift.cfg.DoubleGiftCfg;
import com.hawk.activity.type.impl.doubleGift.entity.DoubleGiftEntity;
import com.hawk.activity.type.impl.doubleGift.entity.DoubleGiftItem;

/**
 * 双享豪礼活动
 * hf
 */
public class DoubleGiftActivity extends ActivityBase implements AchieveProvider {

	public DoubleGiftActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DOUBLE_GIFT_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DoubleGiftActivity activity = new DoubleGiftActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DoubleGiftEntity> queryList = HawkDBManager.getInstance()
				.query("from DoubleGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DoubleGiftEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DoubleGiftEntity entity = new DoubleGiftEntity(playerId, termId);
		//初始化
		initDoubleGift(entity);
		return entity;
	}

	/**初始化礼包相关数据
	 * @param entity
	 */
	public void initDoubleGift(DoubleGiftEntity entity){
		ConfigIterator<DoubleGiftCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(DoubleGiftCfg.class);
		while (configIterator.hasNext()) {
			DoubleGiftCfg cfg =  configIterator.next();
			int giftId = cfg.getGiftId();
			DoubleGiftActivityLayoutCfg layoutCfg = getDefaultRewardCfg(giftId);
			if (layoutCfg == null) {
				continue;
			}
			int rewardId = layoutCfg.getId();
			DoubleGiftItem item = new DoubleGiftItem(giftId, rewardId);
			entity.addDoubleGift(item);
		}
	}
	
	@Override
	public void onPlayerLogin(String playerId) {

	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.DOUBLE_GIFT_INIT, () -> {
				Optional<DoubleGiftEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return;
				}
				initAchieveInfo(playerId);
				pushToPlayer(playerId, HP.code.DOUBLE_GIFT_INFO_PUSH_VALUE, getDoubleGiftActivityInfoBuilder(playerId));
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

		Optional<DoubleGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		DoubleGiftEntity entity = opEntity.get();
		// 在线跨天
		if (event.isCrossDay() || !HawkTime.isSameDay(entity.getLatestPurchaseTime(), now)) {
			Iterator<DoubleGiftItem> iterator = entity.getDoubleGiftList().iterator();
			while (iterator.hasNext()) {
				DoubleGiftItem item = iterator.next();
				if (item.getBuyTime() > 0) {
					item.setBuyTime(0);
				}
			}
			if(event.isCrossDay()){
				entity.setFreeTakenTime(0);
			}
			//updata 
			entity.notifyUpdate();
			
			if (event.isCrossDay()) {
				pushToPlayer(playerId, HP.code.DOUBLE_GIFT_INFO_PUSH_VALUE, getDoubleGiftActivityInfoBuilder(playerId));
			}
			
			return;
		}
	}

	/***
	 * 购买事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		String payforId = event.getGiftId();
		int giftId = DoubleGiftCfg.getGiftId(payforId);
		//不是该活动礼包
		if (giftId == 0) {
			return;
		}
		// 发货
		DoubleGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DoubleGiftCfg.class,giftId);
		if (cfg == null) {
			HawkLog.errPrintln("DoubleGiftActivity  PayGiftBuyEvent failed, cfg not exist, playerId: {}, giftId: {}",playerId, event.getGiftId());
			return;
		}
		if (!isOpening(playerId)) {
			return;
		}

		Optional<DoubleGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}

		DoubleGiftEntity entity = opEntity.get();
		DoubleGiftItem item = entity.getDoubleGiftItem(giftId);
		if (item == null) {
			DoubleGiftActivityLayoutCfg rewardCfg = getDefaultRewardCfg(giftId);
			int rewardId = rewardCfg.getId();
			//如果为空,则初始化默认选择的奖励Id
			item = new DoubleGiftItem(giftId, rewardId);
			entity.addDoubleGift(item);
			HawkLog.errPrintln("DoubleGiftActivity PayGiftBuyEvent item == null so create DoubleGiftItem playerId:{},giftId:{}", playerId, giftId);
		}

		long now = HawkTime.getMillisecond();
		// 同一天同一个礼包买两次的情况，是否要做特殊处理
		if (HawkTime.isSameDay(now, item.getBuyTime())) {
			HawkLog.errPrintln("DoubleGiftActivity PayGiftBuyEvent failed, item purchased today, playerId: {}, giftId: {}, time: {}",playerId,item.getGiftId(), item.getBuyTime());
			return;
		}
		//固定奖励
		List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(cfg.getFixedReward());
		//选择奖励
		int rewardId = item.getRewardId();
		DoubleGiftActivityLayoutCfg layoutCfg = HawkConfigManager.getInstance().getConfigByKey(DoubleGiftActivityLayoutCfg.class, rewardId);
		List<RewardItem.Builder> selectRewardList = RewardHelper.toRewardItemList(layoutCfg.getReward());
		rewardList.addAll(selectRewardList);
		//发送奖励
		if (!rewardList.isEmpty()) {
			ActivityReward reward = new ActivityReward(rewardList, Action.DOUBLE_GIFT_BUY);
			reward.setOrginType(RewardOrginType.SHOPPING_GIFT, getActivityId());
			reward.setAlert(true);
			postReward(playerId, reward, false);
		}
		//购买时间更新
		item.setBuyTime(now);
		//
		if (!HawkTime.isSameDay(now, entity.getLatestPurchaseTime())) {
			entity.setAccDay(entity.getAccDay() + 1);
			//更新购买次数成就
			ActivityManager.getInstance().postEvent(new DoubleGiftDayBuyEvent(playerId, entity.getAccDay()));
		}

		entity.setLatestPurchaseTime(now);
		entity.notifyUpdate();
		pushToPlayer(playerId, HP.code.DOUBLE_GIFT_INFO_PUSH_VALUE, getDoubleGiftActivityInfoBuilder(playerId));

		int termId = getTimeControl().getActivityTermId(now);
		// 打点
		getDataGeter().logDoubleGiftBuy(playerId, termId, item.getGiftId(), rewardId);

		HawkLog.logPrintln("DoubleGiftActivity PayGiftBuyEvent success, playerId:{},giftId:{},rewardId:{}", playerId, giftId,rewardId);
	}


	/**
	 * 同步礼包信息
	 * 
	 * @param playerId
	 */
	public int syncDoubleGiftInfo(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}

		Optional<DoubleGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("syncDoubleGiftInfo failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		pushToPlayer(playerId, HP.code.DOUBLE_GIFT_INFO_PUSH_VALUE, getDoubleGiftActivityInfoBuilder(playerId));

		return Status.SysError.SUCCESS_OK_VALUE;
	}
	

	@Override
	public void syncActivityDataInfo(String playerId) {
		try{
			if (!isOpening(playerId)) {
				return;
			}
			Optional<DoubleGiftEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			pushToPlayer(playerId, HP.code.DOUBLE_GIFT_INFO_PUSH_VALUE, getDoubleGiftActivityInfoBuilder(playerId));			
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
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}

		Optional<DoubleGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}

		DoubleGiftEntity entity = opEntity.get();
		long now = HawkTime.getMillisecond();
		//已经领取过免费奖励
		if (HawkTime.isSameDay(entity.getFreeTakenTime(), now)) {
			return Status.Error.DOUBLE_FREE_GIFT_TAKEN_VALUE;
		}
		
		DoubleGiftCfg cfg = getFreeDoubleGiftCfg();
		int freeGiftId = cfg.getGiftId();
		DoubleGiftItem item = entity.getDoubleGiftItem(freeGiftId);
		if (item == null) {
			DoubleGiftActivityLayoutCfg rewardCfg = getDefaultRewardCfg(freeGiftId);
			int rewardId = rewardCfg.getId();
			//如果为空,则初始化默认选择的奖励Id
			item = new DoubleGiftItem(freeGiftId, rewardId);
			entity.addDoubleGift(item);
			HawkLog.errPrintln("DoubleGiftActivity receiveFreeGift item == null so create DoubleGiftItem playerId:{},freeGiftId:{}", playerId, freeGiftId);
		}
		
		List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(cfg.getFixedReward());
		//选择奖励
		int rewardId = item.getRewardId();
		DoubleGiftActivityLayoutCfg layoutCfg = HawkConfigManager.getInstance().getConfigByKey(DoubleGiftActivityLayoutCfg.class, rewardId);
		List<RewardItem.Builder> selectRewardList = RewardHelper.toRewardItemList(layoutCfg.getReward());
		rewardList.addAll(selectRewardList);
		
		if (!rewardList.isEmpty()) {
			ActivityReward reward = new ActivityReward(rewardList, Action.DOUBLE_GIFT_FREE);
			reward.setOrginType(RewardOrginType.ACTIVITY_REWARD, getActivityId());
			reward.setAlert(true);
			postReward(playerId, reward, false);
		}
		//购买时间更新
		item.setBuyTime(now);
		entity.setFreeTakenTime(now);
		entity.notifyUpdate();
		pushToPlayer(playerId, HP.code.DOUBLE_GIFT_INFO_PUSH_VALUE, getDoubleGiftActivityInfoBuilder(playerId));

		// 打点
		int termId = getTimeControl().getActivityTermId(now);
		getDataGeter().logDoubleGiftBuy(playerId, termId, item.getGiftId(), rewardId);
		HawkLog.logPrintln("DoubleGiftActivity receiveFreeGift success, playerId: {}", playerId);

		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 选择礼包的奖励
	 */
	public int selectDoubleGiftReward(String playerId, int giftId, int rewardId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}

		Optional<DoubleGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		//判断giftId 是否存在
		DoubleGiftCfg layoutCfg = HawkConfigManager.getInstance().getConfigByKey(DoubleGiftCfg.class, giftId);
		if(layoutCfg == null) {
			HawkLog.errPrintln("selectDoubleGiftReward failed, giftCfg error, playerId: {}, giftId: {}, rewardId: {}", playerId, giftId, rewardId);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		//判断奖励是否存在
		List<Integer> rewardIdList = DoubleGiftActivityLayoutCfg.getGiftRewardMap().get(giftId);
		if (!rewardIdList.contains(rewardId)) {
			HawkLog.errPrintln("selectDoubleGiftReward failed, rewardConfig error, playerId: {},giftId:{},rewardId: {}", playerId, giftId, rewardId);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		DoubleGiftEntity entity = opEntity.get();
		DoubleGiftItem item = entity.getDoubleGiftItem(giftId);
		//购买过的礼包,不可以设置奖励
		if (item != null) {
			boolean isSameDay = HawkTime.isSameDay(item.getBuyTime(), HawkTime.getMillisecond());
			if (item != null && isSameDay) {
				HawkLog.logPrintln("selectDoubleGiftReward failed, has buyed today, playerId: {}, giftId: {}, rewardId: {}, time: {}",playerId, giftId, rewardId, item.getBuyTime());
				return Status.Error.DOUBLE_GIFT_BUYED_VALUE;
			}
		}else{
			item = new DoubleGiftItem(giftId, rewardId);
			entity.addDoubleGift(item);
			HawkLog.errPrintln("DoubleGiftActivity selectDoubleGiftReward item == null so create DoubleGiftItem playerId:{},freeGiftId:{}", playerId, giftId);

		}
		//更新奖励Id
		item.setRewardId(rewardId);
		
		entity.notifyUpdate();
		pushToPlayer(playerId, HP.code.DOUBLE_GIFT_INFO_PUSH_VALUE, getDoubleGiftActivityInfoBuilder(playerId));

		HawkLog.logPrintln("selectDoubleGift success, playerId: {}, giftId: {}, rewardId: {}", playerId, giftId, rewardId);

		return Status.SysError.SUCCESS_OK_VALUE;
	}


	/**
	 * 获取活动信息
	 * 
	 * @param playerId
	 * @return
	 */
	private DoubleGiftActivityInfo.Builder getDoubleGiftActivityInfoBuilder(String playerId) {
		DoubleGiftActivityInfo.Builder builder = DoubleGiftActivityInfo.newBuilder();
		Optional<DoubleGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("syncDoubleGiftInfo failed, entity data not exist, playerId: {}", playerId);
			return builder;
		}

		DoubleGiftEntity entity = opEntity.get();
		builder.setAccDay(entity.getAccDay());
		builder.setFreeGiftTaken(HawkTime.isSameDay(entity.getFreeTakenTime(), HawkApp.getInstance().getCurrentTime()));
		HawkLog.logPrintln("syncDoubleGiftInfo , isSameDay: {}", HawkTime.isSameDay(entity.getFreeTakenTime(), HawkApp.getInstance().getCurrentTime()));
		for (DoubleGiftItem item : entity.getDoubleGiftList()) {
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
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}

		Optional<DoubleGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("selectGiftCheck failed, entity data not exist, playerId: {}, type: {}", playerId, type);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}

		DoubleGiftEntity entity = opEntity.get();
		DoubleGiftItem item = entity.getDoubleGiftItem(type);
		if (item == null) {
			HawkLog.errPrintln("selectGiftCheck failed, DoubleGiftItem not selected, playerId: {}, type: {}", playerId,
					type);
			return Status.Error.DOUBLE_GIFT_NOT_SELECTED_VALUE;
		}

		if (HawkTime.isSameDay(item.getBuyTime(), HawkTime.getMillisecond())) {
			HawkLog.errPrintln("selectGiftCheck failed, item purchased today, playerId: {}, type: {}, purchaseTime: {}",
					playerId, type, item.getBuyTime());
			return Status.Error.DOUBLE_GIFT_BUYED_VALUE;
		}

		return Status.SysError.SUCCESS_OK_VALUE;
	}

	private void initAchieveInfo(String playerId) {
		Optional<DoubleGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		DoubleGiftEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始化完成成就的成就
		ConfigIterator<DoubleGiftAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(DoubleGiftAchieveCfg.class);
		while (configIterator.hasNext()) {
			DoubleGiftAchieveCfg next = configIterator.next();
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

		Optional<DoubleGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		DoubleGiftEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(DoubleGiftAchieveCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.DOUBLE_ACC_GIFT_TAKE;
	}

	
	/**获取免费的礼包信息
	 * @return
	 */
	public DoubleGiftCfg getFreeDoubleGiftCfg(){
		ConfigIterator<DoubleGiftCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(DoubleGiftCfg.class);
		while (configIterator.hasNext()) {
			DoubleGiftCfg cfg = configIterator.next();
			if (cfg.isFree()) {
				return cfg;
			}
		}
		return null;
	}
	
	/**获取免费的礼包信息
	 * @return
	 */
	public DoubleGiftActivityLayoutCfg getDefaultRewardCfg(int giftId){
		//奖励ID组合
		List<Integer> rewardIdList = DoubleGiftActivityLayoutCfg.getGiftRewardMap().get(giftId);
		for (Integer rewardId : rewardIdList) {
			DoubleGiftActivityLayoutCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DoubleGiftActivityLayoutCfg.class, rewardId);
			if (cfg.isDefaultChoose()) {
				return cfg;
			}
		}
		return null;
	}
	
	
	/**检查是否可以购买礼包
	 * @param playerId
	 * @param payforId
	 * @return
	 */
	public boolean canPayforGift(String playerId, String payforId) {
		int giftId = DoubleGiftCfg.getGiftId(payforId);
		// 发货
		DoubleGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DoubleGiftCfg.class,giftId);
		if (cfg == null) {
			return false;
		}
		if (!isOpening(playerId)) {
			return false;
		}
		Optional<DoubleGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		DoubleGiftEntity entity = opEntity.get();
		DoubleGiftItem item = entity.getDoubleGiftItem(giftId);
		if (item == null) {
			return false;
		}
		long now = HawkTime.getMillisecond();
		// 同一天同一个礼包买两次的情况，是否要做特殊处理
		if (HawkTime.isSameDay(now, item.getBuyTime())) {
			return false;
		}
		return true;
	}
	
	
	
	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}
}
