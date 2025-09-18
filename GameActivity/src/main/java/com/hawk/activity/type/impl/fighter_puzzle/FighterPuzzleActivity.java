package com.hawk.activity.type.impl.fighter_puzzle;

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

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.FighterPuzzleScoreEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.fighter_puzzle.cfg.FighterPuzzleAchieveCfg;
import com.hawk.activity.type.impl.fighter_puzzle.cfg.FighterPuzzleScoreCfg;
import com.hawk.activity.type.impl.fighter_puzzle.entity.FighterPuzzleEntity;
import com.hawk.game.protocol.Activity.FightPuzzlePageInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 武者拼图活动
 * @author Jesse
 *
 */
public class FighterPuzzleActivity extends ActivityBase implements AchieveProvider {

	public FighterPuzzleActivity(int activityId, ActivityEntity activityEntity) {
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
	public void onPlayerLogin(String playerId) {
		syncActivityDataInfo(playerId);
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_FIGHTER_PUZZLE, ()-> {
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
		Optional<FighterPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		FighterPuzzleEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}

		// 初始添加成就项
		ConfigIterator<FighterPuzzleAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(FighterPuzzleAchieveCfg.class);
		for (FighterPuzzleAchieveCfg cfg : configIterator) {
			if (cfg.getDay() == currDay || cfg.getDay() == 0) {
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				entity.addItem(item);
			}
		}

		// 积分成就
		ConfigIterator<FighterPuzzleScoreCfg> scoreAchieveIt = HawkConfigManager.getInstance().getConfigIterator(FighterPuzzleScoreCfg.class);
		for (FighterPuzzleScoreCfg cfg : scoreAchieveIt) {
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
		Optional<FighterPuzzleEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		FighterPuzzleEntity playerDataEntity = opPlayerDataEntity.get();
		initAchieveItems(playerId);
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(FighterPuzzleAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(FighterPuzzleScoreCfg.class, achieveId);
		}
		return config;
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.FIGHTER_PUZZLE_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.FIGHTER_PUZZLE_ACHIEVE_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		FighterPuzzleActivity activity = new FighterPuzzleActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<FighterPuzzleEntity> queryList = HawkDBManager.getInstance()
				.query("from FighterPuzzleEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			FighterPuzzleEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		FighterPuzzleEntity entity = new FighterPuzzleEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		FightPuzzlePageInfo.Builder builder = genPageInfo(playerId);
		if (builder != null) {
			pushToPlayer(playerId, HP.code.FIGHT_PUZZLE_PAGE_INFO_SYNC_VALUE, builder);
		}
	}
	
	/**
	 * 构建主界面信息
	 * @param playerId
	 * @return
	 */
	public FightPuzzlePageInfo.Builder genPageInfo(String playerId) {
		Optional<FighterPuzzleEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return null;
		}
		int currDay = getCurrentDays(playerId);
		FighterPuzzleEntity entity = opPlayerDataEntity.get();
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
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<FighterPuzzleEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		
		FighterPuzzleEntity entity = opPlayerDataEntity.get();
		List<AchieveItem> oldItems =entity.getItemList(); 
		
		
		// 积分成就/不重置任务的数据保留
		List<AchieveItem> retainList = new ArrayList<>();
		for(AchieveItem item : oldItems){
			FighterPuzzleScoreCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FighterPuzzleScoreCfg.class, item.getAchieveId());
			if(cfg != null){
				retainList.add(item);
			}
			else{
				FighterPuzzleAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(FighterPuzzleAchieveCfg.class, item.getAchieveId());
				if(achieveCfg !=null && achieveCfg.getDay() ==0){
					retainList.add(item);
				}
			}
		}
		
		// 如果没有积分/不重置成就数据,则进行初始化
		if(retainList.isEmpty()){
			ConfigIterator<FighterPuzzleScoreCfg> scoreAchieveIt = HawkConfigManager.getInstance().getConfigIterator(FighterPuzzleScoreCfg.class);
			while (scoreAchieveIt.hasNext()) {
				FighterPuzzleScoreCfg achieveCfg = scoreAchieveIt.next();
				AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
				retainList.add(item);
			}
			// 初始添加成就项
			ConfigIterator<FighterPuzzleAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(FighterPuzzleAchieveCfg.class);
			for (FighterPuzzleAchieveCfg cfg : configIterator) {
				if (cfg.getDay() == 0) {
					AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
					retainList.add(item);
				}
			}
		}
		
		int currDay = getCurrentDays(playerId);
		// 需要刷新的普通任务列表
		List<AchieveItem> addList = new ArrayList<>();
		ConfigIterator<FighterPuzzleAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(FighterPuzzleAchieveCfg.class);
		for (FighterPuzzleAchieveCfg cfg : achieveIterator) {
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
		FighterPuzzleAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(FighterPuzzleAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			// 积分宝箱成就配置
			FighterPuzzleScoreCfg scoreCfg = HawkConfigManager.getInstance().getConfigByKey(FighterPuzzleScoreCfg.class, achieveId);
			if (scoreCfg == null) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
		} else {
			Optional<FighterPuzzleEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
			if (!opPlayerDataEntity.isPresent()) {
				return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			}
			FighterPuzzleEntity entity = opPlayerDataEntity.get();
			int addScore = achieveCfg.getScore();
			entity.setScore(entity.getScore() + addScore);
			ActivityManager.getInstance().postEvent(new FighterPuzzleScoreEvent(playerId, entity.getScore()));
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
