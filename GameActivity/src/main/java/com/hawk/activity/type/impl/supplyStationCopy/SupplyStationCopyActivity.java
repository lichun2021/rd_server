package com.hawk.activity.type.impl.supplyStationCopy;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.HappyGiftPurchaseEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.supplyStationCopy.cfg.SupplyStationChestCopyCfg;
import com.hawk.activity.type.impl.supplyStationCopy.cfg.SupplyStationKVCopyConfig;
import com.hawk.activity.type.impl.supplyStationCopy.entity.SupplyStationCopyEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.SupplyStationInfo;
import com.hawk.game.protocol.Activity.SupplyStationItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

public class SupplyStationCopyActivity extends ActivityBase {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public SupplyStationCopyActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.HAPPY_GIFT;
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SupplyStationCopyActivity activity = new SupplyStationCopyActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SupplyStationCopyEntity> queryList = HawkDBManager.getInstance()
				.query("from SupplyStationCopyEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SupplyStationCopyEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SupplyStationCopyEntity entity = new SupplyStationCopyEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			syncActivityInfo(playerId);
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayerIds){
			callBack(playerId, GameConst.MsgId.HAPPY_GIFT_INIT, () -> {
				syncActivityInfo(playerId);
			});
		}
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<SupplyStationCopyEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		if(!event.isCrossDay()){
			return;
		}
		SupplyStationKVCopyConfig config = SupplyStationKVCopyConfig.getInstance();
		if(!config.isReset()){
			return;
		}
		SupplyStationCopyEntity entity = opEntity.get();
		
		//记录一下清空之前的购买记录
		logger.info("SupplyStationCopyActivity before clear entity msg: {}", entity);
		
		entity.crossDay();
		entity.notifyUpdate();
		syncActivityInfo(event.getPlayerId());
	}
	
	/***
	 * 购买事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(HappyGiftPurchaseEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<SupplyStationCopyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("HappyGiftPurchaseEvent handle failed, entity data not exist, playerId: {}", playerId);
			return;
		}
		
		SupplyStationCopyEntity entity = opEntity.get();
		String payGiftId = event.getPayGiftId();
		int chestId = SupplyStationChestCopyCfg.getGiftId(payGiftId);
		
		SupplyStationChestCopyCfg chestCfg = HawkConfigManager.getInstance().getConfigByKey(SupplyStationChestCopyCfg.class, chestId);
		if(chestCfg == null){
			logger.error("SupplyStationCopyActivity send error chestId: {}", chestId);
		    return;
		}
		
		//给宝箱奖励
		logger.info("SupplyStationCopyActivity playerId: {}, buy chest, chestId: {}", playerId, chestId);
		
		this.getDataGeter().sendAwardFromAwardCfg(chestCfg.getAwardId(), 1, playerId, true, Action.SUPPLY_STATION_REWARD, RewardOrginType.SHOPPING_GIFT);
		//更新数据到数据库
		entity.onPlayerBuy(chestId, 1);
		entity.notifyUpdate();
		//同步界面信息
		syncActivityInfo(playerId);
	}
	
	/**
	 * 判断直购项对应的奖励是否存在
	 * 
	 * @param payGiftId
	 * @return
	 */
	public boolean isPayGiftExist(String payGiftId) {
		int chestId = SupplyStationChestCopyCfg.getGiftId(payGiftId);
		SupplyStationChestCopyCfg chestCfg = HawkConfigManager.getInstance().getConfigByKey(SupplyStationChestCopyCfg.class, chestId);
		if(chestCfg == null) {
			logger.error("SupplyStationCopyActivity ChestCfg error, payGiftId: {},  chestId: {}", payGiftId, chestId);
		    return false;
		}
		
		return true;
	}

	public void syncActivityInfo(String playerId){
		if(!isOpening(playerId)){
			return;
		}
		
		Optional<SupplyStationCopyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("SupplyStationCopyEntity init error, can't init entity, playerId: {}", playerId);
			return;
		}
		
		SupplyStationCopyEntity entity = opEntity.get();
		SupplyStationInfo.Builder build = SupplyStationInfo.newBuilder();
		ConfigIterator<SupplyStationChestCopyCfg> ite = HawkConfigManager.getInstance().getConfigIterator(SupplyStationChestCopyCfg.class);
		while(ite.hasNext()){
			SupplyStationChestCopyCfg config = ite.next();
			SupplyStationItem.Builder item = SupplyStationItem.newBuilder();
			item.setId(config.getId());
			item.setBuyCnt(entity.getBuyCnt(config.getId()));
			build.addItems(item);
		}
		
		//同步信息
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.SUPPLY_STATION_COPY_INFO_S, build));
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

}
