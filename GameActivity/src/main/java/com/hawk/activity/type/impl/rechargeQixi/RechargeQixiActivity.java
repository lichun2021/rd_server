package com.hawk.activity.type.impl.rechargeQixi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
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
import com.hawk.activity.event.impl.ShareProsperityEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.rechargeQixi.cfg.RechargeQixiAchieveCfg;
import com.hawk.activity.type.impl.rechargeQixi.cfg.RechargeQixiLimitAchieveCfg;
import com.hawk.activity.type.impl.rechargeQixi.entity.RechargeQixiEntity;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 七夕充值活动
 * hf
 */
public class RechargeQixiActivity extends ActivityBase implements AchieveProvider {
	public final Logger logger = LoggerFactory.getLogger("Server");
	
	
	public RechargeQixiActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}


	@Override
	public ActivityType getActivityType() {
		return ActivityType.RECHARGE_QIXI_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<RechargeQixiEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
		}
	}
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.RECHARGE_QIXI_INIT, () -> {
				initAchieve(playerId);
			});
		}
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RechargeQixiActivity activity = new RechargeQixiActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RechargeQixiEntity> queryList = HawkDBManager.getInstance()
				.query("from RechargeQixiEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			RechargeQixiEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RechargeQixiEntity entity = new RechargeQixiEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}


	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isShow(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<RechargeQixiEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		RechargeQixiEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg =  HawkConfigManager.getInstance().getConfigByKey(RechargeQixiAchieveCfg.class, achieveId);
		if (cfg == null) {
			RechargeQixiLimitAchieveCfg limitCfg = HawkConfigManager.getInstance().getConfigByKey(RechargeQixiLimitAchieveCfg.class, achieveId);
			if (limitCfg == null) {
				return null;
			}
			if (!isOverTimeLimit(limitCfg)) {
				cfg = limitCfg;
			}
		}
		return cfg;
	}
	
	
	/** 任务是否超过限时时间
	 * @param cfg
	 * @return
	 */
	public boolean isOverTimeLimit(RechargeQixiLimitAchieveCfg cfg){
		long nowTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		long openTime = getTimeControl().getStartTimeByTermId(termId);
		//limitTime 已活动开启时间为准
		if (openTime + cfg.getTimelimit() < nowTime) {
			return true;
		}
		return false;
	}
	
	
	@Override
	public Action takeRewardAction() {
		return Action.RECHARGE_QIXI_REWARD;
	}
	//初始化成就
	private void initAchieve(String playerId){
		Optional<RechargeQixiEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		RechargeQixiEntity entity = optional.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<RechargeQixiAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(RechargeQixiAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			RechargeQixiAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		//限时成就
		ConfigIterator<RechargeQixiLimitAchieveCfg> limitConfigIterator = HawkConfigManager.getInstance().getConfigIterator(RechargeQixiLimitAchieveCfg.class);
		while(limitConfigIterator.hasNext()){
			RechargeQixiLimitAchieveCfg cfg = limitConfigIterator.next();
			//限时成就是否过期
			if (isOverTimeLimit(cfg)) {
				continue;
			}
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		return Result.success();
	}
	
	
	@Subscribe
	public void onEvent(ShareProsperityEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<RechargeQixiEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		RechargeQixiEntity entity = opEntity.get();
		AchieveManager.getInstance().onSpecialAchieve(this, playerId, entity.getItemList(), AchieveType.ACCUMULATE_DIAMOND_RECHARGE, event.getDiamondNum());
		entity.notifyUpdate();
	}
	
}
