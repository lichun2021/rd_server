package com.hawk.activity.type.impl.presentrebate;

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
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.presentrebate.cfg.PresentRebateAchieveCfg;
import com.hawk.activity.type.impl.presentrebate.cfg.PresentRebateActivityKVCfg;
import com.hawk.activity.type.impl.presentrebate.entity.PresentRebateEntity;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 礼包返利活动
 * 
 * @author lating
 *
 */
public class PresentRebateActivity extends ActivityBase implements AchieveProvider {

	public PresentRebateActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return !isHidden(playerId);
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<PresentRebateEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			
			PresentRebateEntity entity = opEntity.get();
			if (entity.getItemList().isEmpty()) {
				initAchieveInfo(playerId);
			}
		}
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
		Optional<PresentRebateEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		PresentRebateEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
				
		// 初始添加成就项
		ConfigIterator<PresentRebateAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PresentRebateAchieveCfg.class);
		while (configIterator.hasNext()) {
			PresentRebateAchieveCfg achieveCfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			entity.addItem(item);
		}

		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);		
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<PresentRebateEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		
		PresentRebateEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public PresentRebateAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(PresentRebateAchieveCfg.class, achieveId);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PRESENT_REBATE;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_PRESENT_REBATE_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PresentRebateActivity activity = new PresentRebateActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PresentRebateEntity> queryList = HawkDBManager.getInstance()
				.query("from PresentRebateEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PresentRebateEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PresentRebateEntity entity = new PresentRebateEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<PresentRebateEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		PresentRebateEntity entity = opEntity.get();
		if (event.isCrossDay() || entity.getLoginDays() == 0) {
			entity.setLoginDays(entity.getLoginDays() + 1);
		}
		
		PresentRebateActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PresentRebateActivityKVCfg.class);
		if(kvCfg.isDailyRefresh()){
			if (!event.isCrossDay()) {
				return;
			}
			
			List<AchieveItem> items = new ArrayList<>();
			ConfigIterator<PresentRebateAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(PresentRebateAchieveCfg.class);
			while (achieveIterator.hasNext()) {
				PresentRebateAchieveCfg cfg = achieveIterator.next();
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				items.add(item);
			}
			entity.resetItemList(items);
			// 推送给客户端
			AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
		}
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public void onTakeRewardSuccess(String playerId) {
		checkActivityClose(playerId);
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
}
