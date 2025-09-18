package com.hawk.activity.type.impl.continuousRecharge;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.RechargeMoneyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.continuousRecharge.cfg.ContinuousRechargeCfg;
import com.hawk.activity.type.impl.continuousRecharge.entity.ContinuousRechargeEntity;
import com.hawk.activity.type.impl.continuousRecharge.item.ContinuousRechargeItem;
import com.hawk.game.protocol.Activity.ContinuousRechargeInfo;
import com.hawk.game.protocol.Activity.ContinuousRechargePage;
import com.hawk.game.protocol.HP;

/**
 * 连续充值活动
 * @author golden
 *
 */
public class ContinuousRechargeActivity extends ActivityBase {

	public ContinuousRechargeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.CONTINUOUS_RECHARGE_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new ContinuousRechargeActivity(config.getActivityId(), activityEntity);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ContinuousRechargeEntity> queryList = HawkDBManager.getInstance().query("from ContinuousRechargeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ContinuousRechargeEntity entity = new ContinuousRechargeEntity(playerId, termId);
		return entity;
	}

	/**
	 * 充值
	 * @param event
	 */
	@Subscribe
	public void onEvent(RechargeMoneyEvent event) {
		
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		Optional<ContinuousRechargeEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		
		// 添加
		opEntity.get().addCurrentNum(event.getMoney());;
		
		// 同步
		syncActivityDataInfo(event.getPlayerId());
	}
	
	/**
	 * 跨天
	 * @param event
	 */
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		Optional<ContinuousRechargeEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		
		if(!event.isCrossDay()){
			return;
		}
		
		ContinuousRechargeEntity entity = opEntity.get();
		
		// 添加到历史
		ContinuousRechargeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ContinuousRechargeCfg.class,  entity.getCurrent().getDay());
		if (cfg == null) {
			return;
		}
		
		if (entity.getCurrent().getCount() >= cfg.getCount()) {
			entity.addHistory(entity.getCurrent());
		}
		
		// 重置当前
		entity.resetCurrent();
		
		// 同步
		if (!event.isLogin()) {
			syncActivityDataInfo(event.getPlayerId());
		}
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		ContinuousRechargeEntity entity = (ContinuousRechargeEntity) getPlayerDataEntity(playerId).get();
		ContinuousRechargePage.Builder builder = ContinuousRechargePage.newBuilder();
		
		for (ContinuousRechargeItem history : entity.getHistory()) {
			ContinuousRechargeInfo.Builder info = ContinuousRechargeInfo.newBuilder();
			info.setDay(history.getDay());
			info.setCount(history.getCount());
		    for (Integer received : history.getReceivedGrade()) {
		    	info.addRewardedGrade(received);
		    }
			builder.addRechargeInfo(info);
		}
		
		ContinuousRechargeInfo.Builder info = ContinuousRechargeInfo.newBuilder();
		info.setDay(entity.getCurrent().getDay());
		info.setCount(entity.getCurrent().getCount());
		for (Integer received : entity.getCurrent().getReceivedGrade()) {
		   	info.addRewardedGrade(received);
		}
		builder.addRechargeInfo(info);
		
		//builder.setCurrentDay(entity.getCurrent().getDay());
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.CONTINUOUS_RECHARGE_PAGE_INFO, builder));
	}

	@Override
	public boolean isActivityClose(String playerId) {
		Optional<ContinuousRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return true;
		}
		
		ContinuousRechargeEntity entity = opEntity.get();
		int dayCount = HawkConfigManager.getInstance().getConfigSize(ContinuousRechargeCfg.class);
		
		if (entity.getCurrent().getDay() < dayCount) {
			return false;
		} else {
			Set<Integer> receivedGrades = entity.getCurrent().getReceivedGrade();
			int rechargeCount = entity.getCurrent().getCount();
			for (ContinuousRechargeCfg cfg : ContinuousRechargeCfg.getDayConfig(entity.getCurrent().getDay()).values()) {
				if (!receivedGrades.contains(cfg.getCount()) && rechargeCount >= cfg.getCount()) {
					return false;
				}
			}
		}
		
		for (ContinuousRechargeItem history : entity.getHistory()) {
			if (history.getDay() > dayCount) {
				return true;
			}
			
			Set<Integer> receivedGrades = history.getReceivedGrade();
			int rechargeCount = history.getCount();
			for (ContinuousRechargeCfg cfg : ContinuousRechargeCfg.getDayConfig(history.getDay()).values()) {
				if (!receivedGrades.contains(cfg.getCount()) && rechargeCount >= cfg.getCount()) {
					return false;
				}
			}
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
