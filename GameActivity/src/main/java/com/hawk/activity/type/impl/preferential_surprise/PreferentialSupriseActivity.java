package com.hawk.activity.type.impl.preferential_surprise;

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
import com.hawk.activity.event.impl.ShareProsperityEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.preferential_surprise.cfg.PreferentialSupriseAchieveCfg;
import com.hawk.activity.type.impl.preferential_surprise.cfg.PreferentialSupriseKVCfg;
import com.hawk.activity.type.impl.preferential_surprise.cfg.PreferentialSupriseScoreCfg;
import com.hawk.activity.type.impl.preferential_surprise.entity.PreferentialSupriseEntity;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 特惠惊喜活动
 * @author Jesse
 *
 */
public class PreferentialSupriseActivity extends ActivityBase implements AchieveProvider {

	public PreferentialSupriseActivity(int activityId, ActivityEntity activityEntity) {
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
			callBack(playerId, MsgId.ACHIEVE_INIT_PRESENT_REBATE, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	

	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<PreferentialSupriseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		PreferentialSupriseEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<PreferentialSupriseAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PreferentialSupriseAchieveCfg.class);
		while (configIterator.hasNext()) {
			PreferentialSupriseAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		// 积分成就
		ConfigIterator<PreferentialSupriseScoreCfg> scoreAchieveIt = HawkConfigManager.getInstance().getConfigIterator(PreferentialSupriseScoreCfg.class);
		while (scoreAchieveIt.hasNext()) {
			PreferentialSupriseScoreCfg next = scoreAchieveIt.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<PreferentialSupriseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		PreferentialSupriseEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(PreferentialSupriseAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(PreferentialSupriseScoreCfg.class, achieveId);
		}
		return config;
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PREFERENTIAL_SURPRISE_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_HERO_ACHIEVE_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PreferentialSupriseActivity activity = new PreferentialSupriseActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PreferentialSupriseEntity> queryList = HawkDBManager.getInstance()
				.query("from PreferentialSupriseEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PreferentialSupriseEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PreferentialSupriseEntity entity = new PreferentialSupriseEntity(playerId, termId);
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
		PreferentialSupriseKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PreferentialSupriseKVCfg.class);
		if (!kvCfg.isDailyReset()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<PreferentialSupriseEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		
		PreferentialSupriseEntity entity = opPlayerDataEntity.get();
		List<AchieveItem> oldItems =entity.getItemList(); 
		
		
		// 积分成就的数据保留
		List<AchieveItem> retainList = new ArrayList<>();
		for(AchieveItem item : oldItems){
			PreferentialSupriseScoreCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PreferentialSupriseScoreCfg.class, item.getAchieveId());
			if(cfg != null){
				retainList.add(item);
			}
		}
		
		// 如果没有积分成就数据,则进行初始化
		if(retainList.isEmpty()){
			ConfigIterator<PreferentialSupriseScoreCfg> scoreAchieveIt = HawkConfigManager.getInstance().getConfigIterator(PreferentialSupriseScoreCfg.class);
			while (scoreAchieveIt.hasNext()) {
				PreferentialSupriseScoreCfg achieveCfg = scoreAchieveIt.next();
				AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
				retainList.add(item);
			}
		}
		
		// 需要刷新的普通任务列表
		List<AchieveItem> addList = new ArrayList<>();
		ConfigIterator<PreferentialSupriseAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(PreferentialSupriseAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			PreferentialSupriseAchieveCfg achieveCfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			addList.add(item);
		}
		
		retainList.addAll(addList);
		entity.resetItemList(retainList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, addList), true);
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// TODO Auto-generated method stub
		
	}

	@Subscribe
	public void onEvent(ShareProsperityEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<PreferentialSupriseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		PreferentialSupriseEntity entity = opEntity.get();
		AchieveManager.getInstance().onSpecialAchieve(this, playerId, entity.getItemList(), AchieveType.ACCUMULATE_DIAMOND_RECHARGE, event.getDiamondNum());
		AchieveManager.getInstance().onSpecialAchieve(this, playerId, entity.getItemList(), AchieveType.DIAMOND_RECHARGE_SCORE, event.getDiamondNum());
		entity.notifyUpdate();
	}
	
}
