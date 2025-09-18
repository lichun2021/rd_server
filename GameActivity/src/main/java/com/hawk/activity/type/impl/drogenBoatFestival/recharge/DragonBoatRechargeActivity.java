package com.hawk.activity.type.impl.drogenBoatFestival.recharge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
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
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DragonBoatAchieveFinishEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.drogenBoatFestival.recharge.cfg.DragonBoatRechargeAchieveCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.recharge.cfg.DragonBoatRechargeDaysCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.recharge.cfg.DragonBoatRechargeKVCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.recharge.entity.DragonBoatRechargeEntity;
import com.hawk.game.protocol.Activity.DragonBoatRechargeInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 端午庆典
 * @author che
 *
 */
public class DragonBoatRechargeActivity extends ActivityBase implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	public DragonBoatRechargeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DRAGON_BOAT_RECHARGE_ACTIVITY;
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
			callBack(playerId, MsgId.DRAGON_BOAT_RECHARGE_INIT, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<DragonBoatRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		this.syncActivityDataInfo(playerId, opEntity.get());
	}

	
	public void syncActivityDataInfo(String playerId,DragonBoatRechargeEntity entity){
		DragonBoatRechargeInfoResp.Builder builder = DragonBoatRechargeInfoResp.newBuilder();
		builder.setChargeDays(entity.getRechargeDays());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.DRAGON_BOAT_RECHAGE_RESP, builder));
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<DragonBoatRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		DragonBoatRechargeEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		List<AchieveItem> updateList = new ArrayList<>();
		List<AchieveItem> itemListDay = new CopyOnWriteArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<DragonBoatRechargeAchieveCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(DragonBoatRechargeAchieveCfg.class);
		while (configIterator.hasNext()) {
			DragonBoatRechargeAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			itemListDay.add(item);
			updateList.add(item);
		}
		entity.resetItemListDay(itemListDay);
		// 积分成就
		List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
		ConfigIterator<DragonBoatRechargeDaysCfg> scoreAchieveIt = HawkConfigManager.getInstance()
				.getConfigIterator(DragonBoatRechargeDaysCfg.class);
		while (scoreAchieveIt.hasNext()) {
			DragonBoatRechargeDaysCfg next = scoreAchieveIt.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			itemList.add(item);
			updateList.add(item);
		}
		entity.resetItemList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, updateList), true);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<DragonBoatRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		DragonBoatRechargeEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getAchieveList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(DragonBoatRechargeAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(DragonBoatRechargeDaysCfg.class, achieveId);
		}
		return config;
	}
	
	

	
	public Action takeRewardAction() {
		return Action.DRAGON_BOAT_RECHARGE_ACHIEVE_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DragonBoatRechargeActivity activity = new DragonBoatRechargeActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DragonBoatRechargeEntity> queryList = HawkDBManager.getInstance()
				.query("from DragonBoatRechargeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DragonBoatRechargeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DragonBoatRechargeEntity entity = new DragonBoatRechargeEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		logger.info("DragonBoatRecharge,onAchieveFinished,playerId: "
				+ "{},achieveId:{}", playerId,achieveItem.getAchieveId());
		DragonBoatRechargeKVCfg cfg = HawkConfigManager.getInstance().
				getKVInstance(DragonBoatRechargeKVCfg.class);
		if(achieveItem.getAchieveId() != cfg.getFinishId()){
			return Result.success();
		}
		
		Optional<DragonBoatRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.success();
		}
		DragonBoatRechargeEntity entity = opEntity.get();
		long curTime = HawkTime.getMillisecond();
		entity.setRechargeDays(entity.getRechargeDays() + 1);
		entity.setLastRechargeTime(curTime);
		entity.notifyUpdate();
		this.syncActivityDataInfo(playerId,entity);
		int termId = this.getActivityTermId();
		this.getDataGeter().logDragonBoatRechargeDays(playerId, termId, entity.getRechargeDays());
		ActivityManager.getInstance().postEvent(new DragonBoatAchieveFinishEvent(playerId, achieveItem.getAchieveId(),entity.getRechargeDays()));
		return Result.success();
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		logger.info("DragonBoatRecharge,onTakeReward,playerId: "
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
		logger.info("DragonBoatRecharge,onContinueLogin,playerId:{}", event.getPlayerId());
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<DragonBoatRechargeEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		DragonBoatRechargeEntity entity = opPlayerDataEntity.get();
		List<AchieveItem> addList = new CopyOnWriteArrayList<AchieveItem>();
		ConfigIterator<DragonBoatRechargeAchieveCfg> achieveIt = HawkConfigManager.getInstance()
				.getConfigIterator(DragonBoatRechargeAchieveCfg.class);
		while (achieveIt.hasNext()) {
			DragonBoatRechargeAchieveCfg achieveCfg = achieveIt.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			addList.add(item);
		}
		entity.resetItemListDay(addList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, addList), true);
		logger.info("DragonBoatRecharge,onContinueLogin,finish,playerId:{},listSize:{}", playerId,addList.size());
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

}
