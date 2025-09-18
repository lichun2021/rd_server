package com.hawk.activity.type.impl.drogenBoatFestival.benefit;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DragonBoatBenefitAchieveFinishEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.drogenBoatFestival.benefit.cfg.DragonBoatBenefitAchieveCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.benefit.cfg.DragonBoatBenefitKVCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.benefit.cfg.DragonBoatBenefitScoreCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.benefit.entity.DragonBoatBenefitEntity;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 端午庆典
 * @author che
 *
 */
public class DragonBoatBenefitActivity extends ActivityBase implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	
	public DragonBoatBenefitActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
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
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.DRAGON_BOAT_BEBEFIT_INIT, ()-> {
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
			callBack(playerId, MsgId.DRAGON_BOAT_BEBEFIT_CLOSE, ()-> {
				this.getDataGeter().travelShopAssistRefresh(playerId, false);
			});
		}
	}
	
	

	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<DragonBoatBenefitEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		DragonBoatBenefitEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
		ConfigIterator<DragonBoatBenefitAchieveCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(DragonBoatBenefitAchieveCfg.class);
		while (configIterator.hasNext()) {
			DragonBoatBenefitAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			itemList.add(item);
		}
		// 积分成就
		ConfigIterator<DragonBoatBenefitScoreCfg> scoreAchieveIt = HawkConfigManager.getInstance()
				.getConfigIterator(DragonBoatBenefitScoreCfg.class);
		while (scoreAchieveIt.hasNext()) {
			DragonBoatBenefitScoreCfg next = scoreAchieveIt.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			itemList.add(item);
		}
		entity.resetItemList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<DragonBoatBenefitEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		DragonBoatBenefitEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(DragonBoatBenefitAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(DragonBoatBenefitScoreCfg.class, achieveId);
		}
		return config;
	}
	
	
	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		logger.info("DragonBoatBenefit,onAchieveFinished,playerId: "
				+ "{},achieveId:{}", playerId,achieveItem.getAchieveId());
		AchieveConfig achieveConfig = this.getAchieveCfg(achieveItem.getAchieveId());
		if(achieveConfig.getAchieveType().getValue() != 
				AchieveType.DRAGON_BOAT_BENEFIT_ACHIEVE_FINISH.getValue()){
			ActivityManager.getInstance().postEvent(new DragonBoatBenefitAchieveFinishEvent(playerId, 1));
		}
		return Result.success();
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DRAGON_BOAT_BENEFIT_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.DRAGON_BOAT_BENEFIT_ACHIEVE_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DragonBoatBenefitActivity activity = new DragonBoatBenefitActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DragonBoatBenefitEntity> queryList = HawkDBManager.getInstance()
				.query("from DragonBoatBenefitEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DragonBoatBenefitEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DragonBoatBenefitEntity entity = new DragonBoatBenefitEntity(playerId, termId);
		return entity;
	}
	
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		logger.info("DragonBoatBenefit,onTakeReward,playerId: "
				+ "{},achieveId:{}", playerId,achieveId);
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
		DragonBoatBenefitKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatBenefitKVCfg.class);
		if (!kvCfg.isDailyReset()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<DragonBoatBenefitEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		
		DragonBoatBenefitEntity entity = opPlayerDataEntity.get();
		List<AchieveItem> addList = new CopyOnWriteArrayList<AchieveItem>();
		ConfigIterator<DragonBoatBenefitScoreCfg> scoreAchieveIt = HawkConfigManager.getInstance()
				.getConfigIterator(DragonBoatBenefitScoreCfg.class);
		while (scoreAchieveIt.hasNext()) {
			DragonBoatBenefitScoreCfg achieveCfg = scoreAchieveIt.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			addList.add(item);
		}
		// 需要刷新的普通任务列表
		ConfigIterator<DragonBoatBenefitAchieveCfg> achieveIterator = HawkConfigManager.getInstance()
				.getConfigIterator(DragonBoatBenefitAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			DragonBoatBenefitAchieveCfg achieveCfg = achieveIterator.next();
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
