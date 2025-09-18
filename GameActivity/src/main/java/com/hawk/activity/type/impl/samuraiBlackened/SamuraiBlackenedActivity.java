package com.hawk.activity.type.impl.samuraiBlackened;

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
import org.hawk.log.HawkLog;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.SamuraiBlackenedScoreEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.samuraiBlackened.cfg.SamuraiBlackenedAchieveCfg;
import com.hawk.activity.type.impl.samuraiBlackened.cfg.SamuraiBlackenedScoreCfg;
import com.hawk.activity.type.impl.samuraiBlackened.entity.SamuraiBlackenedEntity;
import com.hawk.game.protocol.Activity.SamuraiBlackenedInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 黑武士
 * @author jm
 *
 */
public class SamuraiBlackenedActivity extends ActivityBase implements AchieveProvider {

	public SamuraiBlackenedActivity(int activityId, ActivityEntity activityEntity) {
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
	public void onPlayerLogin(String playerId) {
		syncActivityDataInfo(playerId);
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_SAMURAI_BLACKENED, ()-> {
				initAchieveItems(playerId);
			});
		}
	}
	
	/**
	 * 初始化成就配置项
	 * @param playerId
	 * @param isLogin
	 * @param entity
	 */
	private void initAchieveItems(String playerId) {
		int currDay = getCurrentDays(playerId);
		Optional<SamuraiBlackenedEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SamuraiBlackenedEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}

		// 初始添加成就项
		ConfigIterator<SamuraiBlackenedAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SamuraiBlackenedAchieveCfg.class);
		for (SamuraiBlackenedAchieveCfg cfg : configIterator) {
			if (cfg.getDay() == currDay || cfg.getDay() == 0) {
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				entity.addItem(item);
			}
		}

		// 积分成就
		ConfigIterator<SamuraiBlackenedScoreCfg> scoreAchieveIt = HawkConfigManager.getInstance().getConfigIterator(SamuraiBlackenedScoreCfg.class);
		for (SamuraiBlackenedScoreCfg cfg : scoreAchieveIt) {
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			entity.addItem(item);
		}

		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}
	
	private int getCurrentDays(String playerId) {
		int termId = getTimeControl().getActivityTermId(HawkTime.getMillisecond());
		long openTime = getTimeControl().getStartTimeByTermId(termId); 
		int crossHour = getDataGeter().getCrossDayHour();
		int betweenDays = HawkTime.getCrossDay(HawkTime.getMillisecond(), openTime, crossHour);
		return betweenDays + 1;
	}
	
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<SamuraiBlackenedEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		SamuraiBlackenedEntity playerDataEntity = opPlayerDataEntity.get();
		initAchieveItems(playerId);
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(SamuraiBlackenedAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(SamuraiBlackenedScoreCfg.class, achieveId);
		}
		return config;
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SAMURAI_BLACKENED_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.SAMURAI_BALCKENED_ACHIEVE_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SamuraiBlackenedActivity activity = new SamuraiBlackenedActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SamuraiBlackenedEntity> queryList = HawkDBManager.getInstance()
				.query("from SamuraiBlackenedEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SamuraiBlackenedEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SamuraiBlackenedEntity entity = new SamuraiBlackenedEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		SamuraiBlackenedInfoResp.Builder builder = genPageInfo(playerId);
		if (builder != null) {
			pushToPlayer(playerId, HP.code.SAMURAI_BLACKENED_INFO_RESP_VALUE, builder);
		}
	}
	
	/**
	 * 构建主界面信息
	 * @param playerId
	 * @return
	 */
	public SamuraiBlackenedInfoResp.Builder genPageInfo(String playerId) {
		Optional<SamuraiBlackenedEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return null;
		}
		int currDay = getCurrentDays(playerId);
		SamuraiBlackenedEntity entity = opPlayerDataEntity.get();
		SamuraiBlackenedInfoResp.Builder builder = SamuraiBlackenedInfoResp.newBuilder();
		builder.setScore(entity.getScore());
		builder.setCurrDay(currDay);
		return builder;
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<SamuraiBlackenedEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		
		SamuraiBlackenedEntity entity = opPlayerDataEntity.get();
		List<AchieveItem> oldItems =entity.getItemList(); 
		
		
		// 积分成就/不重置任务的数据保留
		List<AchieveItem> retainList = new ArrayList<>();
		for(AchieveItem item : oldItems){
			SamuraiBlackenedScoreCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SamuraiBlackenedScoreCfg.class, item.getAchieveId());
			if(cfg != null){
				retainList.add(item);
			}
			else{
				SamuraiBlackenedAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(SamuraiBlackenedAchieveCfg.class, item.getAchieveId());
				if(achieveCfg !=null && achieveCfg.getDay() ==0){
					retainList.add(item);
				}
			}
		}
		
		// 如果没有积分/不重置成就数据,则进行初始化
		if(retainList.isEmpty()){
			ConfigIterator<SamuraiBlackenedScoreCfg> scoreAchieveIt = HawkConfigManager.getInstance().getConfigIterator(SamuraiBlackenedScoreCfg.class);
			while (scoreAchieveIt.hasNext()) {
				SamuraiBlackenedScoreCfg achieveCfg = scoreAchieveIt.next();
				AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
				retainList.add(item);
			}
			// 初始添加成就项
			ConfigIterator<SamuraiBlackenedAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SamuraiBlackenedAchieveCfg.class);
			for (SamuraiBlackenedAchieveCfg cfg : configIterator) {
				if (cfg.getDay() == 0) {
					AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
					retainList.add(item);
				}
			}
		}
		
		int currDay = getCurrentDays(playerId);
		// 需要刷新的普通任务列表
		List<AchieveItem> addList = new ArrayList<>();
		ConfigIterator<SamuraiBlackenedAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(SamuraiBlackenedAchieveCfg.class);
		for (SamuraiBlackenedAchieveCfg cfg : achieveIterator) {
			if (cfg.getDay() == currDay) {
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				addList.add(item);
			}
		}
		
		retainList.addAll(addList);
		entity.resetItemList(retainList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, addList), true);
		syncActivityDataInfo(playerId);
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		SamuraiBlackenedAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(SamuraiBlackenedAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			// 积分宝箱成就配置
			SamuraiBlackenedScoreCfg scoreCfg = HawkConfigManager.getInstance().getConfigByKey(SamuraiBlackenedScoreCfg.class, achieveId);
			if (scoreCfg == null) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
		} else {
			Optional<SamuraiBlackenedEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
			if (!opPlayerDataEntity.isPresent()) {
				return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			}
			SamuraiBlackenedEntity entity = opPlayerDataEntity.get();
			int addScore = achieveCfg.getScore();
			entity.setScore(entity.getScore() + addScore);
			ActivityManager.getInstance().postEvent(new SamuraiBlackenedScoreEvent(playerId, entity.getScore()));
		}
		syncActivityDataInfo(playerId);
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
