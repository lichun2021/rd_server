package com.hawk.activity.type.impl.firstRecharge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.newFirstRecharge.cfg.NewFirstRechargeKVCfg;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.FirstRechargeEvent;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.firstRecharge.cfg.FirstRechargeRewardCfg;
import com.hawk.activity.type.impl.firstRecharge.enitiy.FirstRechargeEntity;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

/**
 * 首充
 * @author golden
 *
 */
public class FirstRechargeActivity extends ActivityBase {

	public FirstRechargeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.FIRST_RECHARGE_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new FirstRechargeActivity(config.getActivityId(), activityEntity);
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	@Override
	public boolean isActivityClose(String playerId) {
		//活动总配置
		NewFirstRechargeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(NewFirstRechargeKVCfg.class);
		if(cfg != null){
			//long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
			long serverOpenDate = getDataGeter().getServerOpenTime(playerId);
			if(serverOpenDate >= cfg.getServerOpenValue()){
				return true;
			}
		}
		Optional<FirstRechargeEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return false;
		}
		return opDataEntity.get().hasReceiveReward();
	}

	public void pushReward(String playerId) {
		boolean hasAlreadyFirstRecharge = PlayerDataHelper.getInstance().getDataGeter().hasAlreadyFirstRecharge(playerId);
		if (!hasAlreadyFirstRecharge) {
			return;
		}

		Optional<FirstRechargeEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		FirstRechargeEntity dataEntity = opDataEntity.get();
		if (dataEntity.hasReceiveReward()) {
			return;
		}
		boolean hasExtraAward = (dataEntity.getHasExtrAward() == 1);
		FirstRechargeRewardCfg cfg = HawkConfigManager.getInstance().getConfigByIndex(FirstRechargeRewardCfg.class, 0);
		List<RewardItem.Builder> awardList = new ArrayList<RewardItem.Builder>();
		for(RewardItem.Builder build : cfg.getCommonAwardList()){
			awardList.add(build);
		}
		if(hasExtraAward){
			for(RewardItem.Builder build : cfg.getExtrAwardList()){
				awardList.add(build);
			}
		}
		
		this.getDataGeter().takeReward(playerId, awardList, 1, Action.ACTIVITY_FIRST_RECHARGE, true, RewardOrginType.FIRST_RECHARGE_REWARD);
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), cfg.getId());
		logger.info("first recharge common reward, playerId:{}, reward:{}", playerId, cfg.getCommonAward());

		if (dataEntity.getHasExtrAward() == 1) {
			//getDataGeter().takeReward(playerId, cfg.getExtrAwardList(), Action.ACTIVITY_FIRST_RECHARGE, true);
			logger.info("first recharge extr reward, playerId:{}, reward:{}", playerId, cfg.getExtrAward());
		}

		dataEntity.setReceiveReward();
		checkActivityClose(playerId);
	}
	
	@Subscribe
	public void onEvent(FirstRechargeEvent event) {
		if(!isOpening(event.getPlayerId())){
			return;
		}
		Optional<FirstRechargeEntity> opDataEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opDataEntity.isPresent()) {
			return;
		}
		FirstRechargeEntity dataEntity = opDataEntity.get();
		int cityLvl = this.getDataGeter().getConstructionFactoryLevel(event.getPlayerId());
		FirstRechargeRewardCfg cfg = HawkConfigManager.getInstance().getConfigByIndex(FirstRechargeRewardCfg.class, 0);
		if (cityLvl < cfg.getCityLvlLimit()) {
			dataEntity.setHasExtrAward(1);
		}
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<FirstRechargeEntity> queryList = HawkDBManager.getInstance()
				.query("from FirstRechargeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			FirstRechargeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		FirstRechargeEntity entity = new FirstRechargeEntity(playerId, termId);
		return entity;
	}
}
