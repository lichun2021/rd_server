package com.hawk.activity.type.impl.customgift;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.CustomGiftPurchaseEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.customgift.cfg.CustomGiftCfg;
import com.hawk.activity.type.impl.customgift.cfg.CustomGiftRewardCfg;
import com.hawk.activity.type.impl.customgift.entity.CustomGiftEntity;
import com.hawk.game.protocol.Activity.CustomGiftInfoList;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 定制礼包活动
 * 
 * @author lating
 *
 */
public class CustomGiftActivity extends ActivityBase {

	public CustomGiftActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.CUSTOM_GIFT_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		CustomGiftActivity activity = new CustomGiftActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<CustomGiftEntity> queryList = HawkDBManager.getInstance()
				.query("from CustomGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			CustomGiftEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		CustomGiftEntity entity = new CustomGiftEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.CUSTOM_GIFT_ACTIVITY_INIT, ()-> {
				Optional<CustomGiftEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return;
				}
				
				pushToPlayer(playerId, HP.code.CUSTOM_GIFT_PUSH_VALUE, CustomGiftInfoList.newBuilder().setCount(0));
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
		
		Optional<CustomGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		// 只有跨天才进行处理
		CustomGiftEntity entity = opEntity.get();
		if(HawkTime.isSameDay(entity.getResetTime(), now)) {
			return;
		}
		entity.setResetTime(now);
		entity.setCount(0);
		entity.getFreeGetMap().clear();
		CustomGiftInfoList.Builder listBuilder = CustomGiftInfoList.newBuilder();
		for (PurchaseItem item : entity.getItemList()) {
			if (item.getPurchaseTime() > 0) {
				item.setPurchaseTime(0);
			}
			listBuilder.addCustomGift(item.toBuilder(entity.getFreeGetMap()));
		}
		entity.notifyUpdate();
		if (!event.isLogin()) {
			pushToPlayer(event.getPlayerId(), HP.code.CUSTOM_GIFT_PUSH_VALUE, listBuilder.setCount(0));
		}
	}
	
	/***
	 * 购买事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(CustomGiftPurchaseEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<CustomGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("CustomGiftPurchaseEvent handle failed, entity data not exist, playerId: {}", playerId);
			return;
		}
		
		CustomGiftEntity entity = opEntity.get();
		String payGiftId = event.getPayGiftId();
		int giftId = CustomGiftCfg.getGiftId(payGiftId);
		PurchaseItem purchaseItem = entity.getPurchaseItem(giftId);
		if (purchaseItem == null || purchaseItem.getRewardIdList().isEmpty()) {
			HawkLog.errPrintln("CustomGiftPurchaseEvent handle failed, playerId: {}, purchaseItem: {}, payGiftId: {}, giftId: {}", 
					playerId, purchaseItem, payGiftId, giftId);
			return;
		}
		
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		for (int rewardId : purchaseItem.getRewardIdList()) {
			CustomGiftRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CustomGiftRewardCfg.class, rewardId);
			if (cfg == null) {
				HawkLog.errPrintln("CustomGiftPurchaseEvent handle failed, reward config error, playerId: {}, rewardId: {}, payGiftId: {}, giftId: {}", 
						playerId, rewardId, payGiftId, giftId);
				return;
			}
			
			rewardList.add(RewardHelper.toRewardItem(cfg.getReward()));
		}
		CustomGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(CustomGiftCfg.class, giftId);
		boolean moreTime = HawkRand.randInt(10000) <= giftCfg.getMoreTimeRate();
		if(moreTime){
			ActivityReward reward = new ActivityReward(rewardList, Action.CUSTOM_GIFT_REWARD);
			reward.setOrginType(RewardOrginType.CUSTOM_REWARD_FREE, getActivityId());
			reward.setAlert(true);
			postReward(playerId, reward);
			entity.setCount(entity.getCount() + 1);
		}else {
			ActivityReward reward = new ActivityReward(rewardList, Action.CUSTOM_GIFT_REWARD);
			reward.setOrginType(RewardOrginType.CUSTOM_REWARD, getActivityId());
			reward.setAlert(true);
			postReward(playerId, reward);
		}
		
		purchaseItem.setPurchaseTime(HawkTime.getMillisecond());
		entity.notifyUpdate();
		
		CustomGiftInfoList.Builder listBuilder = CustomGiftInfoList.newBuilder();
		listBuilder.addCustomGift(purchaseItem.toBuilder(entity.getFreeGetMap()));
		listBuilder.setCount(entity.getCount());
		pushToPlayer(playerId, HP.code.CUSTOM_GIFT_PUSH_VALUE, listBuilder);
		//  log
		logCustomGift(playerId, payGiftId,giftId,purchaseItem.getRewardIdList(), moreTime, entity.getCount());
		HawkLog.logPrintln("CustomGiftPurchaseEvent handle success, playerId: {}, payGiftId: {}, giftId: {}", 
				playerId, payGiftId, giftId);
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
	public int syncCustomGiftInfo(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<CustomGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("syncCustomGiftInfo failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		CustomGiftEntity entity = opEntity.get();
		CustomGiftInfoList.Builder listBuilder = CustomGiftInfoList.newBuilder();
		for (PurchaseItem item : entity.getItemList()) {
			listBuilder.addCustomGift(item.toBuilder(entity.getFreeGetMap()));
		}
		listBuilder.setCount(entity.getCount());
		pushToPlayer(playerId, HP.code.CUSTOM_GIFT_PUSH_VALUE, listBuilder);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 选择定制奖励
	 * 
	 * @param playerId
	 * @param giftId
	 * @param rewardIds
	 */
	public int selectCustomReward(String playerId, int giftId, List<Integer> rewardIds) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<CustomGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("selectCustomReward failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		CustomGiftEntity entity = opEntity.get();
		PurchaseItem item = entity.getPurchaseItem(giftId);
		CustomGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(CustomGiftCfg.class, giftId);
		if (giftCfg == null || giftCfg.getChooseItems() < rewardIds.size()) {
			HawkLog.errPrintln("selectCustomReward failed, gift chooseItem count not match, playerId: {}, cfg count: {}, real count: {}", 
					playerId, giftCfg == null ? 0 : giftCfg.getChooseItems(), rewardIds.size());
			return Status.Error.GIFT_CHOOSEITEM_NOT_MATCH_VALUE;
		}
		
		for (int rewardId : rewardIds) {
			CustomGiftRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CustomGiftRewardCfg.class, rewardId);
			if (cfg == null || cfg.getGiftId() != giftId) {
				HawkLog.errPrintln("selectCustomReward failed, giftId not match, playerId: {}, rewardId: {}, cfg giftId: {}, real giftId: {}", 
						playerId, rewardId, cfg == null ? 0 : cfg.getGiftId(), giftId);
				return Status.Error.GIFT_CHOOSEITEM_NOT_MATCH_VALUE;
			}
		}
		
		if (item == null) {
			item = PurchaseItem.valueOf(giftId);
			item.setRewardIdList(rewardIds);
			entity.addItem(item);
		} else {
			item.setRewardIdList(rewardIds);
			entity.notifyUpdate();
		}
		
		CustomGiftInfoList.Builder listBuilder = CustomGiftInfoList.newBuilder();
		listBuilder.addCustomGift(item.toBuilder(entity.getFreeGetMap()));
		listBuilder.setCount(entity.getCount());
		pushToPlayer(playerId, HP.code.CUSTOM_GIFT_PUSH_VALUE, listBuilder);
		
		HawkLog.logPrintln("selectCustomReward success, playerId: {}, giftId: {}, rewardIds: {}", 
				playerId, giftId, rewardIds);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}


	/**
	 * 免费领奖
	 * @param playerId
	 * @param giftId
	 * @return
	 */
	public int onGetCustomReward(String playerId, int giftId){
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}

		Optional<CustomGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("selectCustomReward failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}

		CustomGiftEntity entity = opEntity.get();
		if(entity.getCount() <= 0){
			return Status.Error.GIFT_CHOOSEITEM_NOT_MATCH_VALUE;
		}
		if(entity.getFreeGetMap().getOrDefault(giftId, 0L) > 0){
			return Status.Error.GIFT_CHOOSEITEM_NOT_MATCH_VALUE;
		}
		PurchaseItem item = entity.getPurchaseItem(giftId);
		CustomGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(CustomGiftCfg.class, giftId);
		if (item ==null || giftCfg == null || item.getRewardIdList().isEmpty()){
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		if(item.getPurchaseTime() <= 0){
			return Status.Error.GIFT_CHOOSEITEM_NOT_MATCH_VALUE;
		}
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		for (int rewardId : item.getRewardIdList()) {
			CustomGiftRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CustomGiftRewardCfg.class, rewardId);
			if (cfg == null) {
				HawkLog.errPrintln("CustomGiftPurchaseEvent handle failed, reward config error, playerId: {}, rewardId: {}, giftId: {}",
						playerId, rewardId, giftId);
				return Status.Error.GIFT_CHOOSEITEM_NOT_MATCH_VALUE;
			}
			rewardList.add(RewardHelper.toRewardItem(cfg.getReward()));
		}
		entity.setCount(entity.getCount() - 1);
		entity.getFreeGetMap().put(giftId, HawkTime.getMillisecond());
		entity.notifyUpdate();
		ActivityReward reward = new ActivityReward(rewardList, Action.CUSTOM_GIFT_REWARD);
		reward.setOrginType(RewardOrginType.SHOPPING_GIFT, getActivityId());
		reward.setAlert(true);
		postReward(playerId, reward);
		CustomGiftInfoList.Builder listBuilder = CustomGiftInfoList.newBuilder();
		listBuilder.addCustomGift(item.toBuilder(entity.getFreeGetMap()));
		listBuilder.setCount(entity.getCount());
		pushToPlayer(playerId, HP.code.CUSTOM_GIFT_PUSH_VALUE, listBuilder);
		logCustomGift(playerId,"free", giftId,item.getRewardIdList(), false, entity.getCount());
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 判断定制礼包奖励是否选满，没选满情况下不允许购买
	 * 
	 * @param playerId
	 * @param payGiftId
	 * @return
	 */
	public boolean isGiftRewardSelectedFull(String playerId, String payGiftId) {
		if (!isOpening(playerId)) {
			return false;
		}
		
		Optional<CustomGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("GiftRewardSelectedCount judge failed, entity data not exist, playerId: {}", playerId);
			return false;
		}
		
		CustomGiftEntity entity = opEntity.get();
		int giftId = CustomGiftCfg.getGiftId(payGiftId);
		PurchaseItem purchaseItem = entity.getPurchaseItem(giftId);
		CustomGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(CustomGiftCfg.class, giftId);
		// 礼包对应所选择的奖励数量不正确
		if (purchaseItem.getRewardIdList().size() != giftCfg.getChooseItems()) {
			HawkLog.errPrintln("GiftRewardSelectedCount judge failed, selected reward count not march config, playerId: {}, selected count: {}, config count: {}", 
					playerId, purchaseItem.getRewardIdList().size(), giftCfg.getChooseItems());
			return false;
		}
		
		return true;
	}
	
	/**
	 * 获取定制礼包下所选择的奖池物品奖励ID
	 * 
	 * @param playerId
	 * @param payGiftId
	 * @return
	 */
	public List<Integer> getGiftRewardIds(String playerId, String payGiftId) {
		if (!isOpening(playerId)) {
			return Collections.emptyList();
		}
		
		Optional<CustomGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Collections.emptyList();
		}
		
		CustomGiftEntity entity = opEntity.get();
		int giftId = CustomGiftCfg.getGiftId(payGiftId);
		PurchaseItem purchaseItem = entity.getPurchaseItem(giftId);
		
		return purchaseItem.getRewardIdList();
	}
	/**
	 * 私人定制礼包购买打点记录
	 * 
	 * @param payGifgId
	 */
	private void logCustomGift(String playerId, String payGifgId, int giftId,List<Integer> rewardIds ,boolean moreTime, int freeCnt) {
			StringBuilder sb = new StringBuilder();
			for (int rewardId : rewardIds) {
				sb.append(rewardId).append(",");
			}
			
			if (sb.indexOf(",") > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			Map<String, Object> param = new HashMap<>();
	    	param.put("giftId", payGifgId); //期数
	        param.put("rewardIds", sb.toString()); //邀请ID
	        param.put("giftIdInt", giftId); //配置id
	        param.put("moreTime", moreTime?1:0); //再领一次
	        param.put("freeCnt", freeCnt); //再领X次
	        
	        getDataGeter().logActivityCommon(playerId, LogInfoType.custom_gift, param);
	}
}
