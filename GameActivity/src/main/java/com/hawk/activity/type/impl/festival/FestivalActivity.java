package com.hawk.activity.type.impl.festival;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.AddScoreEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayFestivalEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.festival.cfg.FestivalAchieveCfg;
import com.hawk.activity.type.impl.festival.cfg.FestivalScoreCfg;
import com.hawk.activity.type.impl.festival.entity.FestivalEntity;
import com.hawk.game.protocol.Activity.FestivalInfoSync;
import com.hawk.game.protocol.Activity.ScoreRewardTimeLimit;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

/**
 * 八日盛典
 * @author PhilChen
 *
 */
public class FestivalActivity extends ActivityBase implements AchieveProvider {

	public FestivalActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.FESTIVAL_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_FESTIVAL_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		FestivalActivity activity = new FestivalActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<FestivalEntity> queryList = HawkDBManager.getInstance()
				.query("from FestivalEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			FestivalEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		FestivalEntity entity = new FestivalEntity(playerId, termId);
		return entity;
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
		Optional<FestivalEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		FestivalEntity playerDataEntity = opPlayerDataEntity.get();
		initAchieveItems(playerId, false, playerDataEntity);
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}
	
	private int getCurrentDays(String playerId) {
		int termId = getTimeControl().getActivityTermId(HawkTime.getMillisecond(), playerId);
		long openTime = getTimeControl().getStartTimeByTermId(termId, playerId); 
		int crossHour = getDataGeter().getCrossDayHour();
		int betweenDays = HawkTime.getCrossDay(HawkTime.getMillisecond(), openTime, crossHour);
		return betweenDays + 1;
	}
	
	@Override
	public void onOpenForPlayer(String playerId) {
		Optional<FestivalEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		initAchieveItems(playerId, false, opEntity.get());
		ActivityManager.getInstance().postEvent(new LoginDayFestivalEvent(playerId, 1), true);
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		int betweenDays = getCurrentDays(playerId);
		FestivalInfoSync.Builder builder = FestivalInfoSync.newBuilder();
		builder.setCurDays(betweenDays);
		long startTime = getTimeControl().getStartTimeByTermId(getPlayerActivityEntity(playerId).getTermId(), playerId);
		ConfigIterator<FestivalScoreCfg> its = HawkConfigManager.getInstance().getConfigIterator(FestivalScoreCfg.class);
		for (FestivalScoreCfg cfg : its) {
			ScoreRewardTimeLimit.Builder limit = ScoreRewardTimeLimit.newBuilder();
			limit.setAchieveId(cfg.getAchieveId());
			limit.setTimeLimit(startTime + cfg.getTimeLimit());
			builder.addLimit(limit);
		}
		pushToPlayer(playerId, HP.code.PUSH_FESTIVAL_INFO_SYNC_S_VALUE, builder);
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		boolean isLogin = event.isLogin();
		if (!isOpening(playerId)) {
			return;
		}
		int termId = getActivityTermId(playerId);
		long endTime = getTimeControl().getEndTimeByTermId(termId, playerId);
		long now = HawkTime.getMillisecond();
		if (now >= endTime) {
			return;
		}
		Optional<FestivalEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		FestivalEntity entity = opEntity.get();
		if (event.isCrossDay() && !HawkTime.isSameDay(entity.getRefreshTime(), now)) {
			entity.setLoginDays(entity.getLoginDays() + 1);
			entity.setRefreshTime(now);
		}
		initAchieveItems(playerId, isLogin, entity);
		ActivityManager.getInstance().postEvent(new LoginDayFestivalEvent(playerId, entity.getLoginDays()), true);
	}

	/**
	 * 初始化成就配置项
	 * @param playerId
	 * @param isLogin
	 * @param entity
	 */
	private void initAchieveItems(String playerId, boolean isLogin, FestivalEntity entity) {
		int betweenDays = getCurrentDays(playerId);
		// 已经初始化过的成就配置不再处理
		if (betweenDays <= entity.getInitDays()) {
			return;
		}
		List<AchieveItem> items = new ArrayList<>();
		// 首次初始化成就数据时,初始化积分成就项
		if(entity.getInitDays() == 0){
			ConfigIterator<FestivalScoreCfg> scoreIterator = HawkConfigManager.getInstance().getConfigIterator(FestivalScoreCfg.class);
			while (scoreIterator.hasNext()) {
				FestivalScoreCfg cfg = scoreIterator.next();
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				entity.addItem(item);
				items.add(item);
			}
		}
		
		for (int i = entity.getInitDays() + 1; i <= betweenDays; i++) {
			ConfigIterator<FestivalAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(FestivalAchieveCfg.class);
			while (configIterator.hasNext()) {
				FestivalAchieveCfg cfg = configIterator.next();
				if (cfg.getDay() != i) {
					continue;
				}
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				entity.addItem(item);
				items.add(item);
			}
		}
		if (items.size() > 0) {
			ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, items), true);
			entity.setInitDays(betweenDays);
			if (isLogin == false) {
				syncActivityDataInfo(playerId);
			}
		}
	}
	

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(FestivalAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(FestivalScoreCfg.class, achieveId);
		}
		return config;
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		FestivalAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(FestivalAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			// 积分宝箱成就配置
			FestivalScoreCfg scoreCfg = HawkConfigManager.getInstance().getConfigByKey(FestivalScoreCfg.class, achieveId);
			if (scoreCfg == null) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
			long startTime = getTimeControl().getStartTimeByTermId(getPlayerActivityEntity(playerId).getTermId(), playerId);
			if (scoreCfg.getAchieveType() == AchieveType.FESTIVAL_SCORE && HawkTime.getMillisecond() < scoreCfg.getTimeLimit() + startTime) {
				return Result.fail(Status.Error.ACTIVITY_CAN_NOT_TAKE_REWARD_VALUE);
			}
		} else {
			ActivityManager.getInstance().postEvent(new AddScoreEvent(playerId, achieveCfg.getScore()));
		}

		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
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
