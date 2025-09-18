package com.hawk.activity.type.impl.yuriAchieveTwo;

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
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.yuriAchieveTwo.cfg.YuriAchieveTwoCfg;
import com.hawk.activity.type.impl.yuriAchieveTwo.entity.YuriAchieveTwoEntity;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 使命战争2(按日期配置开启)
 * @author Jesse
 *
 */
public class YuriAchieveTwoActivity extends ActivityBase implements AchieveProvider {

	public YuriAchieveTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.YURI_ACHIEVE_TWO_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_YURI_ACHIEVE_TWO_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		YuriAchieveTwoActivity activity = new YuriAchieveTwoActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<YuriAchieveTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from YuriAchieveTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			YuriAchieveTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		YuriAchieveTwoEntity entity = new YuriAchieveTwoEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_YURI_ACHIEVE_TWO, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<YuriAchieveTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		YuriAchieveTwoEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<YuriAchieveTwoCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(YuriAchieveTwoCfg.class);
		while (configIterator.hasNext()) {
			YuriAchieveTwoCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		entity.notifyUpdate();
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
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
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<YuriAchieveTwoEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		YuriAchieveTwoEntity playerDataEntity = opPlayerDataEntity.get();
		if(playerDataEntity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(YuriAchieveTwoCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(YuriAchieveTwoCfg.class, achieveId);
		}
		return config;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	@Override
	public void onTakeRewardSuccess(String playerId) {
		// TODO Auto-generated method stub
		
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<YuriAchieveTwoEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		YuriAchieveTwoEntity playerDataEntity = opPlayerDataEntity.get();
		List<AchieveItem> items = new ArrayList<>();
		ConfigIterator<YuriAchieveTwoCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(YuriAchieveTwoCfg.class);
		while (achieveIterator.hasNext()) {
			YuriAchieveTwoCfg cfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			items.add(item);
		}
		playerDataEntity.resetItemList(items);
		// 推送给客户端
		AchievePushHelper.pushAchieveUpdate(playerId, playerDataEntity.getItemList());
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// TODO Auto-generated method stub
		
	}
}
