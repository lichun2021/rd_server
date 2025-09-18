package com.hawk.activity.type.impl.luckyWelfare;

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
import com.hawk.activity.event.impl.LoginDayLuckyWelfareEvent;
import com.hawk.activity.event.impl.ShareProsperityEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.luckyWelfare.cfg.LuckyWelfareAchieveCfg;
import com.hawk.activity.type.impl.luckyWelfare.cfg.LuckyWelfareActivityKVCfg;
import com.hawk.activity.type.impl.luckyWelfare.entity.LuckyWelfareEntity;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 幸运福利
 * @author RickMei 
 *
 */
public class LuckyWelfareActivity extends ActivityBase implements AchieveProvider {

	public LuckyWelfareActivity(int activityId, ActivityEntity activityEntity) {
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
			callBack(playerId, MsgId.LUCKY_WELFARE_ACTIVITY_INIT, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<LuckyWelfareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		LuckyWelfareEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<LuckyWelfareAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(LuckyWelfareAchieveCfg.class);
		while (configIterator.hasNext()) {
			LuckyWelfareAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}

		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		ActivityManager.getInstance().postEvent(new LoginDayLuckyWelfareEvent(playerId, 1), true);
	}
	
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<LuckyWelfareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		LuckyWelfareEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public LuckyWelfareAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(LuckyWelfareAchieveCfg.class, achieveId);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.LUCKY_WELFARE_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_LUCKY_WELFARE_ACHIEVE_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		LuckyWelfareActivity activity = new LuckyWelfareActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<LuckyWelfareEntity> queryList = HawkDBManager.getInstance()
				.query("from LuckyWelfareEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			LuckyWelfareEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		LuckyWelfareEntity entity = new LuckyWelfareEntity(playerId, termId);
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
		Optional<LuckyWelfareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		LuckyWelfareEntity entity = opEntity.get();
		if (event.isCrossDay() || entity.getLoginDays() == 0) {
			entity.setLoginDays(entity.getLoginDays() + 1);
			ActivityManager.getInstance().postEvent(new LoginDayLuckyWelfareEvent(playerId, entity.getLoginDays()), true);
		}
		LuckyWelfareActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LuckyWelfareActivityKVCfg.class);
		if(kvCfg.isDailyRefresh()){
			if (!event.isCrossDay()) {
				return;
			}
			List<AchieveItem> items = new ArrayList<>();
			ConfigIterator<LuckyWelfareAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(LuckyWelfareAchieveCfg.class);
			while (achieveIterator.hasNext()) {
				LuckyWelfareAchieveCfg cfg = achieveIterator.next();
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
		Optional<LuckyWelfareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		LuckyWelfareEntity entity = opEntity.get();
		AchieveManager.getInstance().onSpecialAchieve(this, playerId, entity.getItemList(), AchieveType.ACCUMULATE_DIAMOND_RECHARGE, event.getDiamondNum());
		entity.notifyUpdate();
	}
	
}
