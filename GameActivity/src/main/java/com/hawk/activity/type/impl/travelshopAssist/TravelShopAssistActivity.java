package com.hawk.activity.type.impl.travelshopAssist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.TravelShopAssistAchieveFinishEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.travelshopAssist.cfg.TravelShopAssistAchieveCfg;
import com.hawk.activity.type.impl.travelshopAssist.cfg.TravelShopAssistKVCfg;
import com.hawk.activity.type.impl.travelshopAssist.cfg.TravelShopAssistScoreCfg;
import com.hawk.activity.type.impl.travelshopAssist.entity.TravelShopAssistEntity;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 特惠商人助力庆典
 * @author che
 *
 */
public class TravelShopAssistActivity extends ActivityBase implements AchieveProvider {

	public TravelShopAssistActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
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
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACTIVITY_TRAVEL_SHOP_ASSIST_INIT, ()-> {
				initAchieveInfo(playerId);
				this.getDataGeter().travelShopAssistRefresh(playerId, false);
			});
		}
	}
	
	@Override
	public void onEnd() {
		super.onEnd();
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACTIVITY_TRAVEL_SHOP_ASSIST_CLOSE, ()-> {
				this.getDataGeter().travelShopAssistRefresh(playerId, false);
			});
		}
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<TravelShopAssistEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		TravelShopAssistEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		//此处需要清空下缓存 
		this.getDataGeter().travelShopAssistRefresh(playerId, true);
		// 初始添加成就项
		ConfigIterator<TravelShopAssistAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(TravelShopAssistAchieveCfg.class);
		while (configIterator.hasNext()) {
			TravelShopAssistAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		// 积分成就
		ConfigIterator<TravelShopAssistScoreCfg> scoreAchieveIt = HawkConfigManager.getInstance().getConfigIterator(TravelShopAssistScoreCfg.class);
		while (scoreAchieveIt.hasNext()) {
			TravelShopAssistScoreCfg next = scoreAchieveIt.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<TravelShopAssistEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		TravelShopAssistEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(TravelShopAssistAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(TravelShopAssistScoreCfg.class, achieveId);
		}
		return config;
	}
	
	
	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		AchieveConfig achieveConfig = this.getAchieveCfg(achieveItem.getAchieveId());
		if(achieveConfig.getAchieveType().getValue() != 
				AchieveType.TRAVEL_SHOP_ASSIST_ASSIST_ACHIEVE_FINISH.getValue()){
			ActivityManager.getInstance().postEvent(new TravelShopAssistAchieveFinishEvent(playerId, 1));
		}
		//任务完成打点
		this.getDataGeter().logTravelShopAssistAchieveFinish(playerId, achieveConfig.getAchieveId());
		return Result.success();
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.TRAVEL_SHOP_ASSIST_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_TRAVEL_SHOP_ASSIST_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		TravelShopAssistActivity activity = new TravelShopAssistActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<TravelShopAssistEntity> queryList = HawkDBManager.getInstance()
				.query("from TravelShopAssistEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			TravelShopAssistEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		TravelShopAssistEntity entity = new TravelShopAssistEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	
	/**
	 * 跨天事件
	 * @param event
	 */
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		TravelShopAssistKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(TravelShopAssistKVCfg.class);
		if (!kvCfg.isDailyReset()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<TravelShopAssistEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		
		TravelShopAssistEntity entity = opPlayerDataEntity.get();
		List<AchieveItem> addList = new ArrayList<AchieveItem>();
		ConfigIterator<TravelShopAssistScoreCfg> scoreAchieveIt = HawkConfigManager.getInstance()
				.getConfigIterator(TravelShopAssistScoreCfg.class);
		while (scoreAchieveIt.hasNext()) {
			TravelShopAssistScoreCfg achieveCfg = scoreAchieveIt.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			addList.add(item);
		}
		// 需要刷新的普通任务列表
		ConfigIterator<TravelShopAssistAchieveCfg> achieveIterator = HawkConfigManager.getInstance()
				.getConfigIterator(TravelShopAssistAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			TravelShopAssistAchieveCfg achieveCfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			addList.add(item);
		}
		entity.resetItemList(addList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, addList), true);
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

}
