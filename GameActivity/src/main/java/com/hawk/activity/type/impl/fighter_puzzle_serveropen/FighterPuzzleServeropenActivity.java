package com.hawk.activity.type.impl.fighter_puzzle_serveropen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkOSOperator;
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
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.FighterPuzzleServeropenScoreEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.fighter_puzzle_serveropen.cfg.FighterPuzzleServeropenAchieveCfg;
import com.hawk.activity.type.impl.fighter_puzzle_serveropen.cfg.FighterPuzzleServeropenActivityKVCfg;
import com.hawk.activity.type.impl.fighter_puzzle_serveropen.cfg.FighterPuzzleServeropenScoreCfg;
import com.hawk.activity.type.impl.fighter_puzzle_serveropen.entity.FighterPuzzleServeropenEntity;
import com.hawk.game.protocol.Activity.FightPuzzlePageInfo;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

/**
 * 武者拼图活动（新版：按开服时间配置）
 * 
 * @author huangfei -> lating
 *
 */
public class FighterPuzzleServeropenActivity extends ActivityBase implements AchieveProvider {
	public final Logger logger = LoggerFactory.getLogger("Server");
	
	public FighterPuzzleServeropenActivity(int activityId, ActivityEntity activityEntity) {
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
	public boolean isActivityClose(String playerId) {
		return !checkPlayerOpen(playerId);
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	/**
	 * @param playerId
	 * @return true=表示达成条件
	 */
	private boolean checkPlayerOpen(String playerId) {
		//判定playerId是否合法
		if (HawkOSOperator.isEmptyString(playerId)) {
			logger.info(this.getClass().getSimpleName()+" activity isPlayerOpen playerId : {} isNull", playerId);
			return false;
		}
		
		FighterPuzzleServeropenActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FighterPuzzleServeropenActivityKVCfg.class);
		if(cfg == null){
			logger.info(this.getClass().getSimpleName()+" activity isPlayerOpen FighterPuzzleServeropenActivityKVCfg not find");
			return false;
		}
		
		int cityLvl = this.getDataGeter().getConstructionFactoryLevel(playerId);
		//开服时间在配置时间内，当前堡等级大于配置等级
		return cityLvl >= cfg.getBuildLevelLimit();
	}
	
	@Subscribe
	public void onEvent(BuildingLevelUpEvent event) {
		FighterPuzzleServeropenActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FighterPuzzleServeropenActivityKVCfg.class);
		if(event.getLevel() < cfg.getBuildLevelLimit()){//未达到等级什么都不做
			return;
		}
		// 大本升级,检测活动是否关闭 如果是login触发的event直接跳过,重登login通过正常流程下发数据
		if(event.getBuildType() == BuildingType.CONSTRUCTION_FACTORY_VALUE && !event.isLogin()){
			if(checkPlayerOpen(event.getPlayerId()) && isOpening(event.getPlayerId())){
				PlayerPushHelper.getInstance().syncActivityStateInfo(event.getPlayerId(), this);//活动状态更新
				initAchieveItems(event.getPlayerId());//检查是否初始化成就列表
				syncActivityDataInfo(event.getPlayerId());//推送主界面信息
			}
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
		Optional<FighterPuzzleServeropenEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		FighterPuzzleServeropenEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}

		// 初始添加成就项
		ConfigIterator<FighterPuzzleServeropenAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(FighterPuzzleServeropenAchieveCfg.class);
		for (FighterPuzzleServeropenAchieveCfg cfg : configIterator) {
			if (cfg.getDay() == currDay || cfg.getDay() == 0) {
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				entity.addItem(item);
			}
		}

		// 积分成就
		ConfigIterator<FighterPuzzleServeropenScoreCfg> scoreAchieveIt = HawkConfigManager.getInstance().getConfigIterator(FighterPuzzleServeropenScoreCfg.class);
		for (FighterPuzzleServeropenScoreCfg cfg : scoreAchieveIt) {
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			entity.addItem(item);
		}
		
		entity.recordLoginDay();

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
		Optional<FighterPuzzleServeropenEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		FighterPuzzleServeropenEntity playerDataEntity = opPlayerDataEntity.get();
		initAchieveItems(playerId);
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(FighterPuzzleServeropenAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(FighterPuzzleServeropenScoreCfg.class, achieveId);
		}
		return config;
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.FIGHTER_PUZZLE_SERVEROPEN_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.FIGHTER_PUZZLE_SERVEROPEN_ACHIEVE_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		FighterPuzzleServeropenActivity activity = new FighterPuzzleServeropenActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<FighterPuzzleServeropenEntity> queryList = HawkDBManager.getInstance()
				.query("from FighterPuzzleServeropenEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			FighterPuzzleServeropenEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		FighterPuzzleServeropenEntity entity = new FighterPuzzleServeropenEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		FightPuzzlePageInfo.Builder builder = genPageInfo(playerId);
		if (builder != null) {
			pushToPlayer(playerId, HP.code.FIGHT_PUZZLE_SERVEROPEN_PAGE_INFO_SYNC_VALUE, builder);
		}
	}
	
	/**
	 * 构建主界面信息
	 * @param playerId
	 * @return
	 */
	public FightPuzzlePageInfo.Builder genPageInfo(String playerId) {
		Optional<FighterPuzzleServeropenEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return null;
		}
		int currDay = getCurrentDays(playerId);
		FighterPuzzleServeropenEntity entity = opPlayerDataEntity.get();
		FightPuzzlePageInfo.Builder builder = FightPuzzlePageInfo.newBuilder();
		builder.setScore(entity.getScore());
		builder.setCurrDay(currDay);
		return builder;
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		String playerId = event.getPlayerId();
		Optional<FighterPuzzleServeropenEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		
		FighterPuzzleServeropenEntity entity = opPlayerDataEntity.get();
		int val = HawkTime.getYyyyMMddIntVal();
		if (!event.isCrossDay() && entity.getLoginDaysList().contains(val)) {
			return;
		}
		
		List<AchieveItem> oldItems =entity.getItemList(); 
		entity.recordLoginDay();
		
		// 积分成就/不重置任务的数据保留
		List<AchieveItem> retainList = new ArrayList<>();
		for(AchieveItem item : oldItems) {
			FighterPuzzleServeropenScoreCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FighterPuzzleServeropenScoreCfg.class, item.getAchieveId());
			if(cfg != null) {
				retainList.add(item);
			} else {
				FighterPuzzleServeropenAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(FighterPuzzleServeropenAchieveCfg.class, item.getAchieveId());
				if(achieveCfg !=null && achieveCfg.getDay() ==0){
					retainList.add(item);
				}
			}
		}
		
		// 如果没有积分/不重置成就数据,则进行初始化
		if(retainList.isEmpty()){
			ConfigIterator<FighterPuzzleServeropenScoreCfg> scoreAchieveIt = HawkConfigManager.getInstance().getConfigIterator(FighterPuzzleServeropenScoreCfg.class);
			while (scoreAchieveIt.hasNext()) {
				FighterPuzzleServeropenScoreCfg achieveCfg = scoreAchieveIt.next();
				AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
				retainList.add(item);
			}
			// 初始添加成就项
			ConfigIterator<FighterPuzzleServeropenAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(FighterPuzzleServeropenAchieveCfg.class);
			for (FighterPuzzleServeropenAchieveCfg cfg : configIterator) {
				if (cfg.getDay() == 0) {
					AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
					retainList.add(item);
				}
			}
		}
		
		int currDay = getCurrentDays(playerId);
		// 需要刷新的普通任务列表
		List<AchieveItem> addList = new ArrayList<>();
		ConfigIterator<FighterPuzzleServeropenAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(FighterPuzzleServeropenAchieveCfg.class);
		for (FighterPuzzleServeropenAchieveCfg cfg : achieveIterator) {
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
		FighterPuzzleServeropenAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(FighterPuzzleServeropenAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			// 积分宝箱成就配置
			FighterPuzzleServeropenScoreCfg scoreCfg = HawkConfigManager.getInstance().getConfigByKey(FighterPuzzleServeropenScoreCfg.class, achieveId);
			if (scoreCfg == null) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
		} else {
			Optional<FighterPuzzleServeropenEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
			if (!opPlayerDataEntity.isPresent()) {
				return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			}
			FighterPuzzleServeropenEntity entity = opPlayerDataEntity.get();
			int addScore = achieveCfg.getScore();
			entity.setScore(entity.getScore() + addScore);
			ActivityManager.getInstance().postEvent(new FighterPuzzleServeropenScoreEvent(playerId, entity.getScore()));
		}
		syncActivityDataInfo(playerId);
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
}
