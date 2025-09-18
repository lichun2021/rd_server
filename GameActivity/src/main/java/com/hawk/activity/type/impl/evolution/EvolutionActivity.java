package com.hawk.activity.type.impl.evolution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.speciality.EvolutionEvent;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.evolution.cfg.EvolutionExchangeCfg;
import com.hawk.activity.type.impl.evolution.cfg.EvolutionLevelCfg;
import com.hawk.activity.type.impl.evolution.cfg.EvolutionTaskCfg;
import com.hawk.activity.type.impl.evolution.entity.ActivityEvolutionEntity;
import com.hawk.activity.type.impl.evolution.entity.TaskItem;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskContext;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskParser;
import com.hawk.game.protocol.Activity.EvolutionBaseInfoPB;
import com.hawk.game.protocol.Activity.EvolutionPageInfoSync;
import com.hawk.game.protocol.Activity.EvolutionTaskInfoSync;
import com.hawk.game.protocol.Activity.EvolutionTaskPB;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 英雄进化之路活动
 * 
 * @author lating
 *
 */
public class EvolutionActivity extends ActivityBase {

	public EvolutionActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.EVOLUTION_ACTIVITY;
	}
	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		EvolutionActivity activity = new EvolutionActivity(config.getActivityId(), activityEntity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityEvolutionEntity> queryList = HawkDBManager.getInstance()
				.query("from ActivityEvolutionEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ActivityEvolutionEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityEvolutionEntity entity = new ActivityEvolutionEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACTIVITY_EVOLUTION_INIT, () -> {
				Optional<ActivityEvolutionEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return;
				}

				initTask(playerId, opEntity.get());
			});
		}
	}
	
	/**
	 * 初始化任务数据
	 * 
	 * @param playerId
	 * @param dataEntity
	 */
	private void initTask(String playerId, ActivityEvolutionEntity dataEntity) {
		if (!isOpening(playerId)) {
			return;
		}

		if (!dataEntity.getTaskList().isEmpty()) {
			return;
		}
		
		dataEntity.setLevel(1);
		List<TaskItem> taskList = new ArrayList<TaskItem>();
		ConfigIterator<EvolutionTaskCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(EvolutionTaskCfg.class);
		while (iterator.hasNext()) {
			EvolutionTaskCfg cfg = iterator.next();
			TaskItem item = TaskItem.valueOf(cfg.getId());
			taskList.add(item);
		}
		
		dataEntity.resetTaskList(taskList);
		syncActivityDataInfo(playerId);
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<ActivityEvolutionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		EvolutionPageInfoSync.Builder builder = genPageInfoBuilder(playerId);
		pushToPlayer(playerId, HP.code.EVOLUTION_ACTIVITY_PAGE_INFO_S_VALUE, builder);
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<ActivityEvolutionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		ActivityEvolutionEntity dataEntity = opEntity.get();
		List<TaskItem> taskList = dataEntity.getTaskList();
		if (taskList.isEmpty()) {
			initTask(playerId, dataEntity);
		}
		
		syncActivityDataInfo(playerId);
	}
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		
		Optional<ActivityEvolutionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		ActivityEvolutionEntity dataEntity = opEntity.get();
		if (dataEntity.getTaskList().isEmpty()) {
			initTask(playerId, dataEntity);
		}
		
		if (event.isCrossDay()) {
			List<TaskItem> taskList = dataEntity.getTaskList();
			for (TaskItem task : taskList) {
				EvolutionTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EvolutionTaskCfg.class, task.getTaskId());
				if (cfg != null && cfg.getTime() > 0) {
					task.reset();
				}
			}
			
			dataEntity.notifyUpdate();
			syncActivityDataInfo(playerId);
		}
		
	}
	
	@Subscribe
	public void onEvent(EvolutionEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		long now = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		// 已过活动开启时间,不再接受活动积分时间,积分不再增长
		if (now >= endTime) {
			return;
		}
		
		Optional<ActivityEvolutionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}

		ActivityEvolutionEntity dataEntity = opEntity.get();
		List<EvolutionTaskParser<?>> parsers = EvolutionTaskContext.getParser(event.getClass());
		if (parsers == null) {
			logger.info("EvolutionTaskParser not found, eventClass: {}", event.getClass().getName());
			return;
		}

		List<TaskItem> taskList = dataEntity.getTaskList();
		boolean update = false;
		List<TaskItem> changeList = new ArrayList<TaskItem>();
		for (EvolutionTaskParser<?> parser : parsers) {
			for (TaskItem taskItem : taskList) {
				EvolutionTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EvolutionTaskCfg.class, taskItem.getTaskId());
				if (cfg == null) {
					continue;
				}

				// 判定任务类型是否一致
				if (!cfg.getEvolutionTaskType().equals(parser.getTaskType())) {
					continue;
				}

				// 完全完成的任务不做处理
				if (parser.finished(taskItem, cfg)) {
					continue;
				}
				
				if (parser.onEventUpdate(dataEntity, cfg, taskItem, event.convert())) {
					changeList.add(taskItem);
					update = true;
				}
			}
		}

		if (update) {
			dataEntity.notifyUpdate();
			EvolutionTaskInfoSync.Builder builder = EvolutionTaskInfoSync.newBuilder();
			builder.addAllTask(genTaskItemList(changeList));
			pushToPlayer(playerId, HP.code.EVOLUTION_TASK_INFO_CHANGE_SYNC_VALUE, builder);
			EvolutionBaseInfoPB.Builder baseInfoBuilder = genBaseInfoBuilder(playerId);
			pushToPlayer(playerId, HP.code.EVOLUTION_BASE_INFO_SYNC_VALUE, baseInfoBuilder);
		}
	}
	
	/**
	 * 领取奖池等级奖励
	 * 
	 * @param playerId
	 */
	public int onRecPoolLevelAward(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<ActivityEvolutionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		ActivityEvolutionEntity dataEntity = opEntity.get();
		int status = dataEntity.getStatus();
		// 当前等级的等级奖励已领取过了
		if (status > 0) {
			HawkLog.errPrintln("EvolutionActivity level award rec failed, has received, playerId: {}, lvl: {}", playerId, dataEntity.getLevel());
			return Status.Error.CUR_LEVEL_AWARD_RECIEVED_VALUE;
		}
		
		List<Integer> exchangeList = EvolutionExchangeCfg.getExchangeList(dataEntity.getLevel());
		List<Integer> playerExchangeList = dataEntity.getExchangeList();
		// 奖池中的物品还没有兑换完，不能领取等级奖励
		if (exchangeList.size() > playerExchangeList.size()) {
			HawkLog.errPrintln("EvolutionActivity level award rec failed, exchange pool remain goods, playerId: {}, lv: {}, exchangeList: {}, playerExchangeList: {}", 
					playerId, dataEntity.getLevel(), exchangeList, playerExchangeList);
			return Status.Error.EXCHANGE_POOL_GOODS_REMAIN_VALUE;
		}
		
		for (Integer exchangeId : exchangeList) {
			// 奖池中的物品还没有兑换完，不能领取等级奖励
			if (!playerExchangeList.contains(exchangeId)) {
				HawkLog.errPrintln("EvolutionActivity level award rec failed, playerExchangeList not contain, playerId: {}, lv: {}, exhangeId: {}, playerExchangeList: {}", 
						playerId, dataEntity.getLevel(), exchangeId, playerExchangeList);
				return Status.Error.EXCHANGE_POOL_GOODS_REMAIN_VALUE;
			}
		}
		
		EvolutionLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(EvolutionLevelCfg.class, dataEntity.getLevel());
		// 配置不存在
		if (levelCfg == null) {
			HawkLog.errPrintln("EvolutionActivity level award rec failed, config not exist, playerId: {}, lvl: {}", playerId, dataEntity.getLevel());
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		List<RewardItem.Builder> rewardList = levelCfg.getNormalRewardList();
		if (!rewardList.isEmpty()) {
			ActivityReward reward = new ActivityReward(rewardList, Action.EVOLUTION_LEVEL_AWARD);
			reward.setAlert(true);
			postReward(playerId, reward, false);
		}
		
		dataEntity.setStatus(1);
		
		HawkLog.logPrintln("EvolutionActivity level award rec success, playerId: {}, lvl: {}", playerId, dataEntity.getLevel());
		
		EvolutionLevelCfg nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(EvolutionLevelCfg.class, dataEntity.getLevel() + 1);
		if (nextLevelCfg != null) {
			dataEntity.setLevel(nextLevelCfg.getLevel());
			dataEntity.getExchangeList().clear();
			dataEntity.setStatus(0);
		}
		
		EvolutionBaseInfoPB.Builder builder = genBaseInfoBuilder(playerId);
		pushToPlayer(playerId, HP.code.EVOLUTION_BASE_INFO_SYNC_VALUE, builder);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 奖池兑换
	 * 
	 * @param playerId
	 * @param exchangeId
	 * @return
	 */
	public int onPoolExchange(String playerId, int exchangeId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<ActivityEvolutionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		ActivityEvolutionEntity dataEntity = opEntity.get();
		List<Integer> playerExchangeList = dataEntity.getExchangeList();
		// 已兑换过
		if (playerExchangeList.contains(exchangeId)) {
			HawkLog.errPrintln("EvolutionActivity pool exchange failed, has exchanged, playerId: {}, exhangeId: {}, exchangeList: {}", playerId, exchangeId, playerExchangeList);
			return Status.Error.POOL_GOODS_EXCHANGED_VALUE;
		}
		
		List<Integer> exchangeList = EvolutionExchangeCfg.getExchangeList(dataEntity.getLevel());
		// 不是当前等级奖池中的物品不能兑换
		if (!exchangeList.contains(exchangeId)) {
			HawkLog.errPrintln("EvolutionActivity pool exchange failed, not curLevel goods, playerId: {}, exhangeId: {}, curLevel exchangeList: {}", playerId, exchangeId, exchangeList);
			return Status.Error.NOT_CUR_LEVEL_POOL_GOODS_VALUE;
		}
		
		EvolutionExchangeCfg exchangeCfg = HawkConfigManager.getInstance().getConfigByKey(EvolutionExchangeCfg.class, exchangeId);
		// 配置不存在
		if (exchangeCfg == null) {
			HawkLog.errPrintln("EvolutionActivity pool exchange failed, config not exist, playerId: {}, exhangeId: {}", playerId, exchangeId);
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		// 积分不足
		if (dataEntity.getExp() < exchangeCfg.getExp()) {
			HawkLog.errPrintln("EvolutionActivity pool exchange failed, exp not enough, playerId: {}, exhangeId: {}, player exp: {}", playerId, exchangeId, dataEntity.getExp());
			return Status.Error.EXCHANGE_CONSUME_NOT_ENOUGH_VALUE;
		}
		
		dataEntity.setExp(dataEntity.getExp() - exchangeCfg.getExp());
		dataEntity.getExchangeList().add(exchangeId);
		
		List<RewardItem.Builder> rewardList = exchangeCfg.getNormalRewardList();
		if (!rewardList.isEmpty()) {
			ActivityReward reward = new ActivityReward(rewardList, Action.EVOLUTION_EXCHANGE_AWARD);
			reward.setAlert(true);
			postReward(playerId, reward, false);
		}
		
		EvolutionBaseInfoPB.Builder builder = genBaseInfoBuilder(playerId);
		pushToPlayer(playerId, HP.code.EVOLUTION_BASE_INFO_SYNC_VALUE, builder);
		
		// 积分消耗打点记录
		getDataGeter().logEvolutionExpChange(playerId, exchangeCfg.getExp(), false, exchangeId);
		// 奖池兑换打点记录
		getDataGeter().logEvolutionExchange(playerId, dataEntity.getLevel(), exchangeId);
		
		HawkLog.logPrintln("EvolutionActivity level award rec success, playerId: {}, lvl: {}, exhangeId: {}", playerId, dataEntity.getLevel(), exchangeId);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 构建当前活动界面信息PB
	 * @param playerId
	 */
	private EvolutionPageInfoSync.Builder genPageInfoBuilder(String playerId) {
		Optional<ActivityEvolutionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		ActivityEvolutionEntity dataEntity = opEntity.get();

		EvolutionPageInfoSync.Builder builder = EvolutionPageInfoSync.newBuilder();
		builder.setBaseInfo(genBaseInfoBuilder(playerId));
		builder.addAllTask(genTaskItemList(dataEntity.getTaskList()));
		return builder;
	}
	
	/**
	 * 构建活动任务列表PB
	 * 
	 * @param taskList
	 * @return
	 */
	private List<EvolutionTaskPB> genTaskItemList(List<TaskItem> taskList) {
		List<EvolutionTaskPB> list = new ArrayList<EvolutionTaskPB>();
		for(TaskItem item : taskList){
			list.add(item.build());
		}
		return list;
	}

	/**
	 * 构建活动基础信息PB
	 * @param playerId
	 * @return
	 */
	private EvolutionBaseInfoPB.Builder genBaseInfoBuilder(String playerId) {
		Optional<ActivityEvolutionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		
		ActivityEvolutionEntity dataEntity = opEntity.get();
		EvolutionBaseInfoPB.Builder builder = EvolutionBaseInfoPB.newBuilder();
		builder.setLevel(dataEntity.getLevel());
		builder.setExp(dataEntity.getExp());
		builder.setAwardStatus(dataEntity.getStatus());
		List<Integer> exchangeList = dataEntity.getExchangeList();
		if (!exchangeList.isEmpty()) {
			builder.addAllExchanged(exchangeList);
		}
		return builder;
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
}
