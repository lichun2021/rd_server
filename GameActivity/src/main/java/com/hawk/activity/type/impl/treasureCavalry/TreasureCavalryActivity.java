package com.hawk.activity.type.impl.treasureCavalry;

import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.treasureCavalry.cfg.TreasureCavalryActivityKVCfg;
import com.hawk.activity.type.impl.treasureCavalry.entity.TreasureCavalryEntity;
import com.hawk.game.protocol.Activity.PBTreasuryInfo;
import com.hawk.game.protocol.HP;

public class TreasureCavalryActivity extends ActivityBase {

	public TreasureCavalryActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.TREASURE_CAVALRY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		TreasureCavalryActivity activity = new TreasureCavalryActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		sync(playerId);
	}
	
	/** 跨天事件
	 * 
	 * @param event */
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<TreasureCavalryEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		TreasureCavalryEntity entity = opPlayerDataEntity.get();
		entity.setRefreshTimes(0);
		entity.resetItems();
		
	}

	public void sync(String playerId) {
		Optional<TreasureCavalryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		TreasureCavalryEntity entity = opEntity.get();
		int openCount = (int) entity.getItemList().stream().filter(i -> i > 0).count();
		TreasureCavalryActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(TreasureCavalryActivityKVCfg.class);
		PBTreasuryInfo.Builder resp = PBTreasuryInfo.newBuilder();
		resp.setPool(entity.getPool());
		resp.addAllRewardId(entity.getItemList());
		resp.setRefreshTimes(entity.getRefreshTimes());
		resp.setMaxRefresh(kvCfg.getMaxRefresh());
		if (openCount < 9) {
			resp.setOpenCost(kvCfg.getTreasureCost(openCount));
		}
		if (entity.getRefreshTimes() < kvCfg.getMaxRefresh()) {
			resp.setRefreshCost(kvCfg.getRefreshCost(entity.getRefreshTimes()));
		}
		pushToPlayer(playerId, HP.code.TREASURE_CAVALRY_INFO_S_VALUE, resp);

	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<TreasureCavalryEntity> queryList = HawkDBManager.getInstance()
				.query("from TreasureCavalryEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			TreasureCavalryEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		TreasureCavalryEntity entity = new TreasureCavalryEntity(playerId, termId);
		entity.resetItems();
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
	}

	@Override
	public void onTick() {
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}

}
