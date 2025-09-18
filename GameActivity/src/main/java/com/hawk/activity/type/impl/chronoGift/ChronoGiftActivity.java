package com.hawk.activity.type.impl.chronoGift;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ChronoGiftBuyEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayChronoGiftEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.chronoGift.cfg.ChronoGiftAchieveCfg;
import com.hawk.activity.type.impl.chronoGift.cfg.ChronoGiftActivityKVCfg;
import com.hawk.activity.type.impl.chronoGift.cfg.ChronoGiftBuyRewardCfg;
import com.hawk.activity.type.impl.chronoGift.cfg.ChronorGiftCfg;
import com.hawk.activity.type.impl.chronoGift.entity.ChronoGiftEntity;
import com.hawk.game.protocol.Activity.ChronoGiftInfoResp;
import com.hawk.game.protocol.Activity.ChronoGiftState;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 时空豪礼活动
 * @author che
 *
 */
public class ChronoGiftActivity extends ActivityBase implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public ChronoGiftActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.CHRONO_GIFT;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	
	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<ChronoGiftEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			//初始化成就数据
			if (opDataEntity.get().getAchieveList().isEmpty()) {
				initAchieveInfo(playerId);
			}
		}
	}
	
	

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		
		String playerId = event.getPlayerId();
		Optional<ChronoGiftEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		
		ChronoGiftEntity entity = opPlayerDataEntity.get();
		// 需要刷新的普通任务列表
		List<AchieveItem> addList = new CopyOnWriteArrayList<AchieveItem>();
		ConfigIterator<ChronoGiftAchieveCfg> achieveIt = HawkConfigManager.getInstance()
				.getConfigIterator(ChronoGiftAchieveCfg.class);
		while (achieveIt.hasNext()) {
			ChronoGiftAchieveCfg achieveCfg = achieveIt.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			addList.add(item);
		}
		//清除开门记录
		entity.clearDoors();
		//清除购买记录
		entity.setBuyNum(0);
		//重置任务列表
		entity.resetItemList(addList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, addList), true);
		//跨天登录
		ActivityManager.getInstance().postEvent(new LoginDayChronoGiftEvent(playerId, 1), true);
		//推动页面信息
		syncActivityInfo(playerId, entity);
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.CHRONO_GIFT_INIT, () -> {
				initAchieveInfo(playerId);
				this.syncActivityDataInfo(playerId);
			});
		}
	}
	
	

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<ChronoGiftEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ChronoGiftActivity activity = new ChronoGiftActivity(
				config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ChronoGiftEntity> queryList = HawkDBManager.getInstance()
				.query("from ChronoGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ChronoGiftEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	
	
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ChronoGiftEntity entity = new ChronoGiftEntity(playerId, termId);
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
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<ChronoGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		ChronoGiftEntity entity = opEntity.get();
		if(entity.getAchieveList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getAchieveList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		ChronoGiftAchieveCfg config = HawkConfigManager.getInstance().
				getConfigByKey(ChronoGiftAchieveCfg.class, achieveId);
		return config;
	}

	@Override
	public Action takeRewardAction() {
		return Action.CHRONO_GIFT_TASK_REWARD;
	}
	
	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		//记录打点
		int termId = this.getActivityTermId();
		this.getDataGeter().logChronoGiftTaskFinish(playerId, termId, achieveItem.getAchieveId());
		return AchieveProvider.super.onAchieveFinished(playerId, achieveItem);
	}
	
	/**
	 * 购买时空之钥
	 * @param playerId
	 * @param num
	 */
	public void buyChronoGiftKey(String playerId, int num,int protocolType){
		Optional<ChronoGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("buyChronoGiftKey failed, entity data not exist, playerId: "
					+ "{},num:{}", playerId,num);
			return;
		}
		ChronoGiftActivityKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(ChronoGiftActivityKVCfg.class);
		
		ChronoGiftEntity entity = opEntity.get();
		if(entity.getBuyNum() + num > kvConfig.getBuyLimit()){
			logger.error("buyChronoGiftKey failed, buyNum not enough, playerId: "
					+ "{},buyNum:{},curNum:{},limitNum:{}", playerId,num,
					entity.getBuyNum(),kvConfig.getBuyLimit());
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.Error.LUCKY_DISCOUNT_NOT_OPEN_VALUE);
		}
		
		Reward.RewardItem.Builder itemPrice = RewardHelper.toRewardItem(kvConfig.getItemPrice());
		itemPrice.setItemCount(itemPrice.getItemCount() * num);
		List<RewardItem.Builder> costList = new ArrayList<RewardItem.Builder>();
		costList.add(itemPrice);
		boolean consumeResult = this.getDataGeter().consumeItems
				(playerId, costList, HP.code.CHRONO_GIFT_KEY_BUY_REQ_VALUE, Action.CHRONO_GIFT_KEY_BUY);
		if(consumeResult){
			List<RewardItem.Builder> itemBuyList = new ArrayList<RewardItem.Builder>();
			Reward.RewardItem.Builder itemBuy = RewardHelper.toRewardItem(kvConfig.getOpenCost());
			itemBuy.setItemCount(num);
			itemBuyList.add(itemBuy);
			entity.addBuyKeyCount(num);
			this.getDataGeter().takeReward(playerId,itemBuyList, 1, Action.CHRONO_GIFT_KEY_BUY, true);
			this.syncActivityInfo(playerId, entity);
		}
		
	}
	/**
	 * 打开时空之门
	 * @param playerId
	 * @param giftId
	 * @return
	 */
	public void openChronoGift(String playerId, int giftId){
		ChronorGiftCfg giftCfg = HawkConfigManager.getInstance().
				getConfigByKey(ChronorGiftCfg.class, giftId);
		if(giftCfg == null){
			logger.error("openChronoGift failed, ChronorGiftCfg data not exist, playerId: "
					+ "{},giftId:{}", playerId,giftId);
			return;
		}
		Optional<ChronoGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("openChronoGift failed, entity data not exist, playerId: "
					+ "{},giftId:{}", playerId,giftId);
			return;
		}
		ChronoGiftEntity entity = opEntity.get();
		if(entity.getChronoDoor(giftId) != null){
			logger.error("openChronoGift failed, openChronoGift already open, playerId: "
					+ "{},giftId:{}", playerId,giftId);
			return;
		}
		ChronoGiftActivityKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(ChronoGiftActivityKVCfg.class);
		List<RewardItem.Builder> costList = new ArrayList<RewardItem.Builder>();
		Reward.RewardItem.Builder costItem = RewardHelper.toRewardItem(giftCfg.getUnlockItem());
		costList.add(costItem);
		int hasNum = this.getDataGeter().getItemNum(playerId, costItem.getItemId());
		long lackNum = costItem.getItemCount() - hasNum;
		if(lackNum > 0){
			Reward.RewardItem.Builder lackItem = RewardHelper.toRewardItem(kvConfig.getItemPrice());
			lackItem.setItemCount(lackItem.getItemCount() * lackNum);
		}
		boolean consumeResult = this.getDataGeter().consumeItems
				(playerId, costList, HP.code.CHRONO_GIFT_UNLOCK_REQ_VALUE, Action.CHRONO_GIFT_UNLOCK_COST);
		if(!consumeResult){
			return;
		}
		ChronoDoor door = ChronoDoor.valueOf(giftId);
		door.setOpenTime(HawkTime.getMillisecond());
		entity.addDoor(door);
		entity.notifyUpdate();
		this.syncActivityInfo(playerId,entity);
		int termId = this.getActivityTermId();
		this.getDataGeter().logChronoGiftUnlock(playerId, termId, giftId);
	}
	

	/**
	 * 选择定门内直购礼包奖励
	 * 
	 * @param playerId
	 * @param giftId
	 * @param rewardIds
	 */
	public void confirmGiftBuyRewards(String playerId, int giftId, List<Integer> rewardIds,int protocolType) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<ChronoGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("confirmGiftBuyRewards failed, entity data not exist, playerId: "
					+ "{},doorId:{},rewardIds :{}", playerId,giftId,rewardIds.toString());
			return;
		}
		
		ChronoGiftEntity entity = opEntity.get();
		ChronoDoor door = entity.getChronoDoor(giftId);
		if(door == null){
			logger.error("confirmGiftBuyRewards failed, ChronoDoor data not exist, playerId: "
					+ "{},doorId:{},rewardIds :{}", playerId,giftId,rewardIds.toString());
			return;
		}
		//已经购买过
		if(door.getBuyAwardState() == ChronoGiftState.ACHIEVE){
			logger.error("confirmGiftBuyRewards failed, ChronoDoor getBuyAwardState achieve, playerId: "
					+ "{},doorId:{},rewardIds :{}", playerId,giftId,rewardIds.toString());
			return;
		}
		for (int rewardId : rewardIds) {
			ChronoGiftBuyRewardCfg cfg = HawkConfigManager.getInstance().
					getConfigByKey(ChronoGiftBuyRewardCfg.class, rewardId);
			if (cfg == null || cfg.getGiftId() != giftId) {
				logger.error("selectCustomReward failed, giftId not match, playerId: {}, rewardId: {}, cfg giftId: {}, real giftId: {}", 
						playerId, rewardId, cfg == null ? 0 : cfg.getGiftId(), giftId);
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.CHRONO_GIFT_CHOOSE_NOT_MATCH_VALUE);
				return;
			}
		}
		door.AddRewardIdList(rewardIds);
		entity.notifyUpdate();
		this.syncActivityInfo(playerId, entity);
	}
	
	/**
	 * 直购礼包是否可以被购买
	 * @param playerId
	 * @param giftId
	 * @return
	 */
	public boolean canPayforGift(String playerId,String payforId){
		Optional<ChronoGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("canPayforGift false, ChronoGiftEntity data null, playerId: "
					+ "{},payforId:{}", playerId,payforId);
			return false;
		}
		
		int giftId = ChronorGiftCfg.getGiftId(payforId);
		ChronorGiftCfg giftCfg = HawkConfigManager.getInstance().
				getConfigByKey(ChronorGiftCfg.class, giftId);
		if(giftCfg == null){
			logger.error("canPayforGift false, ChronorGiftCfg data null, playerId: "
					+ "{},payforId:{}", playerId,payforId);
			return false;
		}
		ChronoGiftEntity entity = opEntity.get();
		ChronoDoor door = entity.getChronoDoor(giftId);
		if(door == null){
			logger.error("canPayforGift false, ChronoDoor entity null, playerId: "
					+ "{},payforId:{},giftId:{}", playerId,payforId,giftId);
			return false;
		}
		if(door.getBuyAwardState() == ChronoGiftState.ACHIEVE){
			logger.error("canPayforGift false, ChronoDoor getBuyAwardState achieve, playerId: "
					+ "{},payforId:{},giftId:{}", playerId,payforId,giftId);
			return false;
		}
		if(door.getRewardIdList() == null){
			logger.error("canPayforGift false, ChronoDoor getRewardIdList null, playerId: "
					+ "{},payforId:{},giftId:{}", playerId,payforId,giftId);
			return false;
		}
		if(door.getRewardIdList().size() != giftCfg.getChooseItems()){
			logger.error("canPayforGift false, ChronoDoor getRewardIdList size err, playerId: "
					+ "{},payforId:{},giftId:{},chooseSize:{},configSize:{}", playerId,payforId,giftId,
					door.getRewardIdList().size(),giftCfg.getChooseItems());
			return false;
		}
		return true;
	}
	
	
	/**
	 * 直购礼包支付完成
	 * @param event
	 */
	@Subscribe
	public void onChronoGiftBuy(ChronoGiftBuyEvent event){
		String playerId = event.getPlayerId();
		String payforId = event.getGiftId();
		int giftId = ChronorGiftCfg.getGiftId(payforId);
		logger.info("onChronoGiftBuy event come, playerId: "
				+ "{},payforId:{},giftId:{}", playerId,payforId,giftId);
		ChronorGiftCfg giftCfg = HawkConfigManager.getInstance().
				getConfigByKey(ChronorGiftCfg.class, giftId);
		if(giftCfg == null){
			logger.error("onChronoGiftBuy event come, ChronorGiftCfg data null,playerId: "
					+ "{},payforId:{},giftId:{}", playerId,payforId,giftId);
			return;
		}
		Optional<ChronoGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("onChronoGiftBuy event come, ChronoGiftEntity data null,playerId: "
					+ "{},payforId:{},giftId:{}", playerId,payforId,giftId);
			return;
		}
		ChronoGiftEntity entity = opEntity.get();
		ChronoDoor door = entity.getChronoDoor(giftId);
		if(door == null){
			logger.error("onChronoGiftBuy event come, ChronoDoor data null,playerId: "
					+ "{},payforId:{},giftId:{}", playerId,payforId,giftId);
			return;
		}
		if(door.getBuyAwardState() == ChronoGiftState.ACHIEVE){
			logger.error("onChronoGiftBuy event come, ChronoDoor getBuyAwardState achieve,playerId: "
					+ "{},payforId:{},giftId:{}", playerId,payforId,giftId);
			return;
		}
		if(door.getRewardIdList() == null){
			logger.info("onChronoGiftBuy event come, ChronoDoor getRewardIdList data null,playerId: "
					+ "{},payforId:{},giftId:{}", playerId,payforId,giftId);
			return;
		}
		if(door.getRewardIdList().size() != giftCfg.getChooseItems()){
			logger.error("onChronoGiftBuy event come, ChronoDoor getRewardIdList size err,playerId: "
					+ "{},payforId:{},giftId:{},chooseSize:{},configSize:{}", playerId,payforId,
					giftId,door.getRewardIdList().size(),giftCfg.getChooseItems());
			return;
		}
		door.setBuyPackageTime(HawkTime.getMillisecond());
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		for (int rewardId : door.getRewardIdList()) {
			ChronoGiftBuyRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ChronoGiftBuyRewardCfg.class, rewardId);
			if (cfg == null) {
				logger.error("onChronoGiftBuy event come, ChronoGiftBuyRewardCfg config error, playerId: {}, rewardId: {}, payGiftId: {}, giftId: {}", 
						playerId, rewardId, payforId, giftId);
				return;
			}
			rewardList.add(RewardHelper.toRewardItem(cfg.getReward()));
		}
		rewardList.add(RewardHelper.toRewardItem(giftCfg.getPurchaseVipExp()));
		entity.notifyUpdate();
		this.getDataGeter().takeReward(playerId, rewardList, Action.CHRONO_GIFT_FREE_AWARD_ACHIEVE, true);
		this.syncActivityInfo(playerId, entity);
		logger.info("onChronoGiftBuy event come, sendItem over,playerId: "
				+ "{},payforId:{},giftId:{}", playerId,payforId,giftId);
	}
	
	
	
	/**
	 * 获取门内免费礼品
	 * @param playerId
	 * @param doorId
	 */
	public void getChronoGiftFreeReward(String playerId, int giftId){
		if (!isOpening(playerId)) {
			logger.error("getChronoGiftFreeReward failed, isOpening fail, playerId: "
					+ "{},giftId:{}", playerId,giftId);
			return;
		}
		Optional<ChronoGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("getChronoGiftFreeReward failed, ChronoGiftEntity data not exist, playerId: "
					+ "{},giftId:{}", playerId,giftId);
			return;
		}
		ChronorGiftCfg giftCfg = HawkConfigManager.getInstance().
				getConfigByKey(ChronorGiftCfg.class, giftId);
		if (giftCfg == null) {
			logger.error("ChronorGiftCfg failed, ChronorGiftCfg null, playerId: "
					+ "{},giftId:{}", playerId,giftId);
			return;
		}
		ChronoGiftEntity entity = opEntity.get();
		ChronoDoor door = entity.getChronoDoor(giftId);
		if(door == null){
			logger.error("getChronoGiftFreeReward failed, ChronoDoor data not exist, playerId: "
					+ "{},giftId:{}", playerId,giftId);
			 return;
		}
		//已经领取过
		if(door.getFreeAwardState() == ChronoGiftState.ACHIEVE){
			logger.error("getChronoGiftFreeReward failed, getFreeAwardState  achieve, playerId: "
					+ "{},giftId:{}", playerId,giftId);
			return;
		}
		long curTime = HawkTime.getMillisecond();
		door.setFreeAwardTime(curTime);
		entity.notifyUpdate();
		List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemList(giftCfg.getFreeRewards());
		this.getDataGeter().takeReward(playerId,rewardItems, 1, Action.CHRONO_GIFT_FREE_AWARD_ACHIEVE, true);
		this.syncActivityInfo(playerId, entity);
		int termId = this.getActivityTermId();
		this.getDataGeter().logChronoGiftFreeAwardAchieve(playerId, termId, giftId);
	}
	
	/**
	 * 同步数据消息给玩家
	 * @param playerId
	 * @param entity
	 */
	public void syncActivityInfo(String playerId, ChronoGiftEntity entity) {
		if (!isOpening(playerId)) {
			return;
		}
		
		ChronoGiftInfoResp.Builder builder = ChronoGiftInfoResp.newBuilder();
		for(ChronoDoor door : entity.getChronoDoorList()){
			builder.addGifts(door.toBuilder());
		}
		builder.setBuyKeyCount(entity.getBuyNum());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.CHRONO_GIFT_INFO_RESP, builder));
		
	}
	
	
	
	
	
	/**
	 * 初始化成就信息
	 * 
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<ChronoGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ChronoGiftEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getAchieveList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		List<AchieveItem> addList = new CopyOnWriteArrayList<AchieveItem>();
		ConfigIterator<ChronoGiftAchieveCfg> achieveIt = HawkConfigManager.getInstance()
				.getConfigIterator(ChronoGiftAchieveCfg.class);
		while (achieveIt.hasNext()) {
			ChronoGiftAchieveCfg achieveCfg = achieveIt.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			addList.add(item);
		}
		entity.resetItemList(addList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getAchieveList()), true);
		//跨天登录
		ActivityManager.getInstance().postEvent(new LoginDayChronoGiftEvent(playerId, 1), true);
	}


	
	
	



}
