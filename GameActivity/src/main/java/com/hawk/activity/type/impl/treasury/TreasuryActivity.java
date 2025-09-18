package com.hawk.activity.type.impl.treasury;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ConsumeMoneyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.treasury.cfg.TreasuryCfg;
import com.hawk.activity.type.impl.treasury.cfg.TreasuryRateCfg;
import com.hawk.activity.type.impl.treasury.entity.TreasuryEntity;
import com.hawk.game.protocol.Activity.TreasuryInfo;
import com.hawk.game.protocol.Activity.TreasuryMsg;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

public class TreasuryActivity extends ActivityBase {

	public TreasuryActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.TREASURY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new TreasuryActivity(config.getActivityId(), activityEntity); 
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<TreasuryEntity> queryList = HawkDBManager.getInstance().query("from TreasuryEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		TreasuryEntity te = new TreasuryEntity();
		te.setPlayerId(playerId);
		te.setTermId(termId);
		te.setStorageInfoMap(new HashMap<>());
		te.setReceivedInfoMap(new HashMap<>());
		te.setCostInfoMap(new HashMap<>());
		
		return te;
	}
	
	public int onReceiveReward(String playerId, Integer treasuryId) {
		if (!isAllowOprate(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		TreasuryCfg treasuryCfg = HawkConfigManager.getInstance().getConfigByKey(TreasuryCfg.class, treasuryId);
		if (treasuryCfg == null) {
			logger.warn("treasuryId:{} is invalid playerId:{}", treasuryId, playerId);
			
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		int openDay = this.getActivityOpenDay();
		if (treasuryCfg.getReceiveTime() > openDay) {
			logger.warn("treasury time deficiency openDay:{}, receiveTime:{}  playerId:{}", openDay, treasuryCfg.getReceiveTime(), playerId);
			
			return Status.Error.TREASURY_TIME_DEFICIENCY_VALUE;
		}		
		
		Optional<TreasuryEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			logger.warn("treasuryEntity is null  playerId:{}", playerId);
			
			return Status.SysError.DATA_ERROR_VALUE;
		}
		
		TreasuryEntity treasuryEntity = optional.get();
		Integer storageNum = treasuryEntity.getStorageInfoMap().get(treasuryId);
		if (storageNum == null || storageNum.intValue() <= 0) {
			logger.warn("storage num is null  playerId:{}", playerId);
			
			return Status.Error.TREASURY_NOT_HAVE_VALUE;
		}
		
		Integer receivedFlag = treasuryEntity.getReceivedInfoMap().get(treasuryId);
		if (receivedFlag != null && receivedFlag.intValue() != 0) {
			logger.warn("treasuryId :{} already received playerId:{} ", treasuryId, playerId);
			
			return Status.Error.TREASURY_ALREADY_RECEIVED_VALUE; 
		}		
		
		treasuryEntity.addReceivedInfo(treasuryId, 1);		
		RewardItem.Builder rewardItemBuilder = RewardItem.newBuilder(); 
		rewardItemBuilder.setItemType(GameConst.ITEM_TYPE_BASE);
		rewardItemBuilder.setItemId(PlayerAttr.GOLD_VALUE);
		rewardItemBuilder.setItemCount(storageNum);		
		this.getDataGeter().takeReward(playerId, Arrays.asList(rewardItemBuilder), Action.TREASURY_RECEIVED, true);
		
		return 0;
		
	}
	/**
	 * 消耗金条
	 */
	@Subscribe
	public void onConsumeGlodBar(ConsumeMoneyEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		if (event.getResType() != PlayerAttr.GOLD_VALUE) {
			return;
		}
		
		Optional<TreasuryEntity> optional = this.getPlayerDataEntity(event.getPlayerId());
		if (!optional.isPresent()) {
			logger.error("treasury entity is null:{}", event.getPlayerId());
			
			return;
		}		
		TreasuryEntity treasuryEntity = optional.get();
		//获取当前的宝藏ID
		Integer treasuryId = this.getCurrentTreasuryId();
		if (treasuryId.intValue() <= 0 ) {
			return ;
		}
		Integer oldStorage = treasuryEntity.getStorageInfoMap().get(treasuryId);
		if (oldStorage == null) {
			oldStorage = 0;
		}
		Integer oldCost = treasuryEntity.getCostInfoMap().get(treasuryId);
		if (oldCost == null) {
			oldCost = Integer.valueOf(0);
		}
		int newStorage = this.calcStorage(treasuryId, oldStorage, oldCost, (int)event.getNum());
		treasuryEntity.addCostInfo(treasuryId, oldCost + (int)(event.getNum()));
		if (oldStorage != newStorage) {
			treasuryEntity.addStorageInfo(treasuryId, newStorage);			
		}
		
	}
	
	public int getActivityOpenDay() {
		long startTime = this.getTimeControl().getStartTimeByTermId(this.getActivityTermId());
		int difDay = difDay(new Date(startTime), new Date());
		
		return difDay;
	}
	
	private int getCurrentTreasuryId() {
		int difDay = getActivityOpenDay();
		ConfigIterator<TreasuryCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(TreasuryCfg.class);
		TreasuryCfg treasuryCfg = null;
		while (configIterator.hasNext()) {
			treasuryCfg = configIterator.next();
			if (treasuryCfg.getStorageTime() == difDay) {
				return treasuryCfg.getId();
			}
		}
		
		return 0;
	}
	
	public  int difDay(Date startDate, Date endTime) {
		return (int)((HawkTime.getAM0Date(endTime).getTime() / 1000 - HawkTime.getAM0Date(startDate).getTime() / 1000) / (24 * 60 * 60) + 1);
	}
	
	private int calcStorage(Integer treasuryId, int oldStorage, Integer oldCost, int add) {
		TreasuryCfg treasury = HawkConfigManager.getInstance().getConfigByKey(TreasuryCfg.class, treasuryId);
		if (treasury == null) {
			logger.error("treasury is null id:{}", treasuryId);
			
			return oldStorage;
		}		
		if (oldStorage >= treasury.getStorageMax()) {
			return oldStorage;
		}		
		//概率
		ConfigIterator<TreasuryRateCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(TreasuryRateCfg.class);
		TreasuryRateCfg treasuryRateCfg = null;
		int storageAdd = 0;
		while(configIterator.hasNext()) {
			treasuryRateCfg = configIterator.next();
			if (oldCost >= treasuryRateCfg.getMax()) {
				continue;
			}
			if (oldCost + add <= treasuryRateCfg.getMin()) {
				break;
			}
			int max = oldCost + add > treasuryRateCfg.getMax() ? treasuryRateCfg.getMax() : oldCost + add;
			int min = treasuryRateCfg.getMin() > oldCost ? treasuryRateCfg.getMin() : oldCost;
			int curAdd = Math.round((max - min ) * treasuryRateCfg.getRate() / 10000.0f);
			storageAdd = storageAdd + curAdd;
		}
		
		return oldStorage + storageAdd > treasury.getStorageMax() ? treasury.getStorageMax() : oldStorage + storageAdd;
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<TreasuryEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		synTreasuryEntityInfo(playerId, optional.get());
	}
	
	private void synTreasuryEntityInfo(String playerId, TreasuryEntity entity) {
		TreasuryInfo.Builder treasuryInfo = TreasuryInfo.newBuilder();
		TreasuryMsg.Builder treasuryMsg = null;
		Map<Integer, Integer> storageMap = entity.getStorageInfoMap();
		Map<Integer, Integer> costMap = entity.getCostInfoMap();
		Integer storageNum = null;
		for (Entry<Integer, Integer> entry : costMap.entrySet()) {
			treasuryMsg = TreasuryMsg.newBuilder();
			treasuryMsg.setTreasuryId(entry.getKey());			
			storageNum = storageMap.get(entry.getKey());
			storageNum = storageNum == null ? Integer.valueOf(0) : storageNum;
			treasuryMsg.setStorageNum(storageNum);
			treasuryMsg.setCostNum(entry.getValue());
			Integer receive = entity.getReceivedInfoMap().get(entry.getKey());
			treasuryMsg.setReceivedFlag(receive == null ? 0 : receive);
			
			treasuryInfo.addTreasuries(treasuryMsg);
		}
		
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.TREASURY_INFO_RESP_VALUE, treasuryInfo);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, hawkProtocol);
	}
	
}
