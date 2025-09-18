package com.hawk.activity.type.impl.greetings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
import com.hawk.activity.event.impl.LoginDayGreetingsEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.greetings.cfg.GreetingsAchieveCfg;
import com.hawk.activity.type.impl.greetings.entity.GreetingsEntity;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 周年庆祝福语活动
 * hf
 */
public class GreetingsActivity extends ActivityBase implements AchieveProvider {
	public final Logger logger = LoggerFactory.getLogger("Server");
	
	public GreetingsActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GREESTINGS_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.GREETINGS_INIT, () -> {
				initAchieve(playerId);
			});
		}
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		GreetingsActivity activity = new GreetingsActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GreetingsEntity> queryList = HawkDBManager.getInstance()
				.query("from GreetingsEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GreetingsEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GreetingsEntity entity = new GreetingsEntity(playerId, termId);
		return entity;
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<GreetingsEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		GreetingsEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	
	//初始化成就
	private void initAchieve(String playerId){
		Optional<GreetingsEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		GreetingsEntity entity = optional.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<GreetingsAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(GreetingsAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			GreetingsAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
		//跨天登录
		ActivityManager.getInstance().postEvent(new LoginDayGreetingsEvent(playerId, 1), true);
	}
	
	
	
	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<GreetingsEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		GreetingsEntity entity  = opEntity.get();
		
		/**
		 * 成就处理
		 */
		List<AchieveItem> oldItems = entity.getItemList();
		//成就中,登录不重置任务数据
		List<AchieveItem> retainList = new ArrayList<>();
		for (AchieveItem item : oldItems) {
			GreetingsAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(GreetingsAchieveCfg.class, item.getAchieveId());
			if (achieveCfg != null && achieveCfg.getIsReset() == 0) {
				retainList.add(item);
			}
		}
		//如果为空，初始化
		boolean idRetainEmpty = retainList.isEmpty();
		ConfigIterator<GreetingsAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(GreetingsAchieveCfg.class);
		while (configIterator.hasNext()) {
			GreetingsAchieveCfg cfg = configIterator.next();
			if (!idRetainEmpty && cfg.getIsReset() == 0) {
				continue;
			}
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			retainList.add(item);
		}
		// 初始化成就数据
		entity.resetItemList(retainList);
		//相关次数重置
		
		entity.notifyUpdate();
		//跨天登录
		ActivityManager.getInstance().postEvent(new LoginDayGreetingsEvent(playerId, 1), true);
		//push
		AchievePushHelper.pushAchieveUpdate(playerId, retainList);
		logger.info("GreetingsActivity onContinueLoginEvent,finish,playerId:{},isCross:{}",event.getPlayerId(),event.isCrossDay());
	}
	
	
	
	
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg =  HawkConfigManager.getInstance().getConfigByKey(GreetingsAchieveCfg.class, achieveId);
		return cfg;
	}
	
	@Override
	public Action takeRewardAction() {
		return Action.GREETINGS_REWARD;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		return Result.success();
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
}
