package com.hawk.activity.type.impl.seasonpuzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import com.google.common.eventbus.Subscribe;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AddTavernScoreEvent;
import com.hawk.activity.event.impl.AttackFoggyEvent;
import com.hawk.activity.event.impl.ConsumeMoneyEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.event.impl.SeasonPuzzleRefreshEvent;
import com.hawk.activity.event.impl.SeasonPuzzleSendItemEvent;
import com.hawk.activity.event.impl.TrainSoldierCompleteEvent;
import com.hawk.activity.event.impl.TravelShopPurchaseEvent;
import com.hawk.activity.event.impl.TreatArmyEvent;
import com.hawk.activity.event.impl.UseItemSpeedUpEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.datatype.ListValueData;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.seasonpuzzle.cfg.SeasonPuzzleAchieveCfg;
import com.hawk.activity.type.impl.seasonpuzzle.cfg.SeasonPuzzleAwardCfg;
import com.hawk.activity.type.impl.seasonpuzzle.cfg.SeasonPuzzleConstCfg;
import com.hawk.activity.type.impl.seasonpuzzle.cfg.SeasonPuzzleTrayCfg;
import com.hawk.activity.type.impl.seasonpuzzle.entity.CallHelperInfo;
import com.hawk.activity.type.impl.seasonpuzzle.entity.SeasonPuzzleEntity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.CallHelpInfoPB;
import com.hawk.game.protocol.Activity.CallHelpInfoResp;
import com.hawk.game.protocol.Activity.CallHelpType;
import com.hawk.game.protocol.Activity.SeasonPuzzleActivityInfo;
import com.hawk.game.protocol.Activity.SeasonPuzzleMissionPB;
import com.hawk.game.protocol.Activity.SeasonPuzzleMissionSync;
import com.hawk.game.protocol.Activity.SendItemResp;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.serialize.string.SerializeHelper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;

/**
 * 赛季拼图373
 * 
 * @author lating
 */
public class SeasonPuzzleActivity extends ActivityBase implements AchieveProvider {
	/**
	 * 完成拼图数量
	 */
	private volatile AtomicInteger puzzleCompleteGlobal = new AtomicInteger(0);
	private volatile AtomicInteger puzzleCompleteLocal = new AtomicInteger(0);
	private int lastPuzzleCompleteLocal = -1;
	/**
	 * 注水逻辑
	 */
	SeasonPuzzleAutoLogic autoLogic;
	
	public SeasonPuzzleActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
		autoLogic = new SeasonPuzzleAutoLogic(this);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SEASON_PUZZLE_373;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SeasonPuzzleActivity activity = new SeasonPuzzleActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SeasonPuzzleEntity entity = new SeasonPuzzleEntity(playerId, termId);
		return entity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SeasonPuzzleEntity> queryList = HawkDBManager.getInstance()
				.query("from SeasonPuzzleEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.SEASON_PUZZLE_INIT, () -> {
				activityOpenInit(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isShow(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<SeasonPuzzleEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		SeasonPuzzleEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(entity);
		}
		
		List<AchieveItem> achieveItems = new ArrayList<>();
		for (AchieveItem achieveItem : entity.getItemList()) {
			SeasonPuzzleAchieveCfg achieveConfig = HawkConfigManager.getInstance().getConfigByKey(SeasonPuzzleAchieveCfg.class, achieveItem.getAchieveId());
			if (achieveConfig.getAchieveType() == AchieveType.SEASON_PUZZLE_COMPLETE_VALUE) {
				achieveItems.add(achieveItem);
			}
		}
		AchieveItems items = new AchieveItems(achieveItems, entity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(SeasonPuzzleAchieveCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.SEASON_PUZZLE_ACHIEVE;
	}

	private void activityOpenInit(String playerId) {
		Optional<SeasonPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		long time = HawkTime.getMillisecond();
		SeasonPuzzleEntity entity = opEntity.get();
		entity.setDayTime(time);
		initAchieveItems(entity);
		onEventProcess(playerId, AchieveType.LOGIN_DAYS_ACTIVITY, 1, 0, 0);
	}
	
	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			clearConsumeItems(playerId);
			return;
		}
		
		Optional<SeasonPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		long time = HawkTime.getMillisecond();
		SeasonPuzzleEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			entity.setDayTime(time);
			initAchieveItems(entity);
			onEventProcess(playerId, AchieveType.LOGIN_DAYS_ACTIVITY, 1, 0, 0);
		}
		
		if (HawkTime.isCrossDay(time, entity.getDayTime(), 0)) {
			entity.setDayTime(time);
			resetAchieveItems(entity);
			entity.setItemGetCount(0);
			entity.setItemSendCount(0);
			//拼图拼接数据每日重置
			if (SeasonPuzzleConstCfg.getInstance().getResetTray() > 0) {
				entity.getItemSetIndexList().clear();
			}
			//拼图碎片每日清空 
			if (SeasonPuzzleConstCfg.getInstance().getResetPuzzle() > 0) {
				clearConsumeItems(playerId);
			}
			onEventProcess(playerId, AchieveType.LOGIN_DAYS_ACTIVITY, 1, 0, 0);
		}
		
		refreshAchieveItems(entity, puzzleCompleteGlobal.get(), true);
		
		List<CallHelperInfo> expiredCallHelpList = new ArrayList<>();
		for (CallHelperInfo info : entity.getCallHelpInfoList()) {
			if (!HawkTime.isSameDay(time, info.getTime())) {
				expiredCallHelpList.add(info);
			}
		}
		if (!expiredCallHelpList.isEmpty()) {
			entity.getCallHelpInfoList().removeAll(expiredCallHelpList);
			entity.notifyUpdate();
		}
		
		syncActivityInfo(playerId, entity);
		
		syncMissionInfo(playerId, entity.getItemList(), true);
	}
	
	
	/**
	 * 全服拼图完成次数
	 * @param event
	 */
	@Subscribe
	public void onEvent(SeasonPuzzleRefreshEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		
		Optional<SeasonPuzzleEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		SeasonPuzzleEntity entity = optional.get();
		refreshAchieveItems(entity, event.getompleteVal(), true);
		syncActivityInfo(playerId, entity);
	}
	
	@Subscribe
	public void onEvent(SeasonPuzzleSendItemEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		
		Optional<SeasonPuzzleEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		String uuid = event.getCallHelpId();
		int itemId = event.getItemId();
		SeasonPuzzleEntity entity = optional.get();
		Optional<CallHelperInfo> callHelpOp = entity.getCallHelpInfoList().stream().filter(e -> e.getUuid().equals(uuid)).findAny();
		if (callHelpOp.isPresent()) {
			CallHelperInfo info = callHelpOp.get();
			if (info.getComplete() > 0) {
				HawkLog.logPrintln("activity373 get help repeated, playerId: {}, uuid: {}", playerId, uuid);
				return;
			}
			info.setComplete(1);
			entity.notifyUpdate();
		}
		
//		//确保当日获得碎片数量还未达到上限
//		if (entity.getItemGetCount() < SeasonPuzzleConstCfg.getInstance().getAchieveGetPuzzleTimesLimit()) {
//			entity.addItemGetCount(1);
//		}
		
		RewardItem.Builder reward = RewardHelper.toRewardItem(30000, itemId, 1);
		this.getDataGeter().takeReward(playerId, Arrays.asList(reward), 1, Action.SEASON_PUZZLE_REC_ITEM, false, RewardOrginType.ACTIVITY_REWARD);
		
		HawkLog.logPrintln("activity373 get help finish, playerId: {}, uuid: {}", playerId, uuid);
		syncActivityInfo(playerId, entity);
	}
	
	
	/**
	 * 初始化成就任务数据
	 * @param entity
	 */
	private void initAchieveItems(SeasonPuzzleEntity entity) {
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<SeasonPuzzleAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SeasonPuzzleAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while (configIterator.hasNext()) {
			SeasonPuzzleAchieveCfg cfg = configIterator.next();				
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());				
			itemList.add(item);
		}
		entity.setItemList(itemList);
		refreshAchieveItems(entity, puzzleCompleteGlobal.get(), false);
	}
	
	/**
	 * 重置任务数据
	 * @param entity
	 */
	private void resetAchieveItems(SeasonPuzzleEntity entity) {
		List<AchieveItem> itemList = new ArrayList<>();
		List<AchieveItem> removeList = new ArrayList<>();
		for (AchieveItem item : entity.getItemList()) {
			SeasonPuzzleAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeasonPuzzleAchieveCfg.class, item.getAchieveId());
			if (cfg.getReset() <= 0) {
				continue;
			}
			removeList.add(item);
			AchieveItem newItem = AchieveItem.valueOf(cfg.getAchieveId());				
			itemList.add(newItem);
		}
		entity.getItemList().removeAll(removeList);
		entity.getItemList().addAll(itemList);
		entity.notifyUpdate();
	}
	
	/**
	 * 刷新成就数据
	 * @param playerId
	 * @param homageVal
	 */
	private void refreshAchieveItems(SeasonPuzzleEntity entity, int puzzleCompleteVal, boolean pushUpdate) {
		try {
			List<AchieveItem> needPush = new ArrayList<>();
			for (AchieveItem achieveItem : entity.getItemList()) {
				SeasonPuzzleAchieveCfg config = HawkConfigManager.getInstance().getConfigByKey(SeasonPuzzleAchieveCfg.class, achieveItem.getAchieveId());
				if (config.getAchieveType() != AchieveType.SEASON_PUZZLE_COMPLETE_VALUE || achieveItem.getState() != AchieveState.NOT_ACHIEVE_VALUE) {
					continue;
				}
				
				needPush.add(achieveItem);
				int configValue = config.getConditionValue(0);
				if (puzzleCompleteVal < configValue) {
					achieveItem.setValue(0, puzzleCompleteVal);
					continue;
				}
				achieveItem.setValue(0, configValue);
				achieveItem.setState(AchieveState.NOT_REWARD_VALUE);
				HawkLog.logPrintln("SeasonPuzzleActivity achieve finish, playerId: {}, achieveId: {}", entity.getPlayerId(), config.getAchieveId());
			}
			
			if (!needPush.isEmpty() && pushUpdate) {
				entity.notifyUpdate();
				AchievePushHelper.pushAchieveUpdate(entity.getPlayerId(), needPush);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	@Override
	public void onEnd() {
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				Set<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
				for (String playerId : onlinePlayerIds) {
					clearConsumeItems(playerId);
				} 
				return null;
			}
		});
	}
	
	/**
	 * 活动结束清空道具
	 * @param playerId
	 */
	private void clearConsumeItems(String playerId) {
		try {
			if(this.getDataGeter().isPlayerCrossIngorePlayerObj(playerId)) {
				return;
			}
			
			List<RewardItem.Builder> consumeList = new ArrayList<>();
			for (int itemId : SeasonPuzzleTrayCfg.getAllPuzzleItemSet()) {
				int itemCount = getDataGeter().getItemNum(playerId, itemId);
				if (itemCount > 0) {
					RewardItem.Builder consumeItem = RewardHelper.toRewardItem(30000, itemId, itemCount);
					consumeList.add(consumeItem);
				}
			}
			
			if (!consumeList.isEmpty()) {
				this.getDataGeter().consumeItems(playerId, consumeList, 0, Action.SEASON_PUZZLE_SYS_CONSUME);
			}
		} catch (Exception e) {
			HawkException.catchException(e, playerId);
		}
	}
	
	
	@Override
	public void shutdown() {
		if (lastPuzzleCompleteLocal >= 0 && puzzleCompleteLocal.get() > lastPuzzleCompleteLocal) {
			int add = puzzleCompleteLocal.get() - lastPuzzleCompleteLocal;
			getRedis().increaseBy(getGlobalPuzzleKey(), add, getRedisExpire());
		}
	}
	
	public void onTick() {
		long currTime = HawkApp.getInstance().getCurrentTime();
		long endTime = this.getEndTime();
		if (currTime > endTime) {
			lastPuzzleCompleteLocal = -1;
			return;
		}
		
		//刚起服，初始化数据
		if (lastPuzzleCompleteLocal < 0) {
			lastPuzzleCompleteLocal = 0;
			checkMergeServer();
			autoLogic.setServerStartTime(currTime);
			puzzleCompleteGlobal.set(getGlobalPuzzleTimes());
			return;
		}
		
		//将本服新增的完成次数累加到全服累计计数上面
		if (puzzleCompleteLocal.get() > lastPuzzleCompleteLocal) {
			int newCountServer = puzzleCompleteLocal.get();
			int add = newCountServer - lastPuzzleCompleteLocal;
			getRedis().increaseBy(getGlobalPuzzleKey(), add, getRedisExpire());
			lastPuzzleCompleteLocal = newCountServer;
		}
		
		//注水
		autoLogic.autoAddPuzzle();
		int completeValGlobal = getGlobalPuzzleTimes();
		if (completeValGlobal > puzzleCompleteGlobal.get()) {
			puzzleCompleteGlobal.set(completeValGlobal);
			Set<String> onlinePlayers = this.getDataGeter().getOnlinePlayers();
			for(String playerId : onlinePlayers){
				ActivityManager.getInstance().postEvent(new SeasonPuzzleRefreshEvent(playerId, completeValGlobal));
			}
		}
	}
	
	/**
	 * 合服检测
	 */
	private void checkMergeServer() {
		try {
			long startTime = getTimeControl().getStartTimeByTermId(getActivityTermId());
			long endTime = getTimeControl().getEndTimeByTermId(getActivityTermId());
			Long serverMergeTime = getDataGeter().getServerMergeTime();
			if (serverMergeTime == null) {
				serverMergeTime = 0L;
			}
			
			List<String> slaveServerList = getDataGeter().getSlaveServerList();
			boolean mergeServer = !slaveServerList.isEmpty() && serverMergeTime >= startTime && serverMergeTime <= endTime;
			if (!mergeServer) {
				return;
			}
			String mainServerKey = getCallHelpZsetKey(CallHelpType.CALL_HELP_TO_WORLD_VALUE, this.getDataGeter().getServerId());
			for (String followServerId : slaveServerList) {
				String zsetKey = getCallHelpZsetKey(CallHelpType.CALL_HELP_TO_WORLD_VALUE, followServerId);
				Set<Tuple> set = getRedis().zRangeWithScores(zsetKey, 0, -1, 0);
				if (set.isEmpty()) {
					continue;
				}
				Map<String, Double> map = new HashMap<>();
				for (Tuple t : set) {
					map.put(t.getElement(), t.getScore());
				}
				getRedis().zAdd(mainServerKey, map, 86400);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取最新的全服拼图完成次数
	 * @return
	 */
	private int getGlobalPuzzleTimes() {
		String str = getRedis().getString(getGlobalPuzzleKey());
		return HawkOSOperator.isEmptyString(str) ? 0 : Integer.parseInt(str);
	}
	
	
	/**
	 * 求助
	 * @param playerId
	 * @param type
	 * @param itemId
	 * @return
	 */
	public int callHelp(String playerId, CallHelpType type, int itemId, String tarPlayerId) {
		if (!this.isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		SeasonPuzzleTrayCfg puzzleCfg = this.getPeriodConfig();
		if (puzzleCfg == null) {
			return Status.Error.SEASON_PUZZLE_PERIOD_EMPTY_VALUE;
		}
		List<SeasonPuzzleAwardCfg> configList = SeasonPuzzleAwardCfg.getConfigList(puzzleCfg.getPeriods());
		Optional<SeasonPuzzleAwardCfg> configOp = configList.stream().filter(e -> e.getItemId() == itemId).findAny();
		if (!configOp.isPresent()) {
			return Status.Error.SEASON_PUZZLE_ITEM_INVALID_VALUE;
		}
		Optional<SeasonPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		SeasonPuzzleEntity entity = opEntity.get();
		long time = HawkTime.getMillisecond();
		Optional<CallHelperInfo> infoOptional = entity.getCallHelpInfoList().stream().filter(e -> e.getType()== type.getNumber() && e.getItemId() == itemId).findAny();
		if (infoOptional.isPresent() && time - infoOptional.get().getTime() < SeasonPuzzleConstCfg.getInstance().getHelpCD()) {
			return Status.Error.SEASON_PUZZLE_CALLHELP_COOLING_VALUE;
		}
		
		if (type == CallHelpType.CALL_HELP_TO_PERSON) {
			if (HawkOSOperator.isEmptyString(tarPlayerId)) {
				return Status.Error.SEASON_PUZZLE_CALLHELP_PARAM_ERR_VALUE;
			}
			if (!this.getDataGeter().checkPlayerExist(tarPlayerId)) {
				return Status.Error.SEASON_PUZZLE_CALLHELP_PERSON_ERR_VALUE;
			}
		} else if (type == CallHelpType.CALL_HELP_TO_GUILD) {
			String guildId = this.getDataGeter().getGuildId(playerId);
			if (HawkOSOperator.isEmptyString(guildId)) {
				return Status.Error.SEASON_PUZZLE_CALLHELP_GUILD_ERR_VALUE;
			}
		}
		
		String playerName = this.getDataGeter().getPlayerName(playerId);
		int icon = this.getDataGeter().getIcon(playerId);
		String pfIcon = this.getDataGeter().getPfIcon(playerId);
		if (pfIcon == null) {
			pfIcon = "";
		}
		if (tarPlayerId == null) {
			tarPlayerId = "";
		}
		
		CallHelperInfo newInfo = new CallHelperInfo(type.getNumber(), time, itemId, tarPlayerId, playerName, icon, pfIcon, playerId); 
		entity.getCallHelpInfoList().add(newInfo);
		entity.notifyUpdate();
		
		String key = getCallHelpInfoKey(newInfo.getUuid());
		int second = (int) ((HawkTime.getNextAM0Date() - time) / 1000);
		getRedis().setBytes(key, newInfo.toBuilder().build().toByteArray(), second);
		
		String zsetKey = getCallHelpZsetKey(type.getNumber(), this.getDataGeter().getServerId());
		if (type == CallHelpType.CALL_HELP_TO_GUILD) {
			zsetKey = zsetKey + ":" + this.getDataGeter().getGuildId(playerId);
		} else if (type == CallHelpType.CALL_HELP_TO_PERSON) {
			zsetKey = zsetKey + ":" + tarPlayerId;
		}
		getRedis().zAdd(zsetKey, HawkTime.getMillisecond(), newInfo.getUuid(), second);
		
		//PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.SEASON_PUZZLE_CALL_HELP_S, newInfo.toBuilder()));
		
		HawkLog.logPrintln("activity373 callHelp end, playerId: {}, type: {}, itemId: {}, uuid: {}, tarPlayerId: {}", playerId, type, itemId, newInfo.getUuid(), tarPlayerId);
		syncActivityInfo(playerId, entity);
		return 0;
	}
	
	/**
	 * 赠送拼图碎片
	 * @param playerId
	 * @param setIndex
	 * @return
	 */
	public int sendPuzzleItem(String playerId, String uuid) {
		if (!this.isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		SeasonPuzzleTrayCfg puzzleCfg = this.getPeriodConfig();
		if (puzzleCfg == null) {
			return Status.Error.SEASON_PUZZLE_PERIOD_EMPTY_VALUE;
		}
		
		String key = getCallHelpInfoKey(uuid);
		byte[] val = getRedis().getBytes(key.getBytes());
		if (val == null) {
			return Status.Error.SEASON_PUZZLE_CALLHELP_EMPTY_VALUE;
		}
		
		CallHelpInfoPB.Builder builder = CallHelpInfoPB.newBuilder();
		try {
			builder.mergeFrom(val);
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
			return Status.Error.SEASON_PUZZLE_CALLHELP_EMPTY_VALUE;
		}
		
		if (!HawkTime.isToday(builder.getTime())) {
			return Status.Error.SEASON_PUZZLE_CALLHELP_EXPIRED_VALUE;
		}
		
		if (builder.getType() == CallHelpType.CALL_HELP_TO_PERSON && !playerId.equals(builder.getTarPlayer())) {
			return Status.Error.SEASON_PUZZLE_HELP_ERROR_VALUE;
		}
		if (playerId.equals(builder.getPlayerId())) {
			return Status.Error.SEASON_PUZZLE_HELP_ERROR_VALUE;
		}
		
		final int itemId = builder.getItemId();
		List<SeasonPuzzleAwardCfg> configList = SeasonPuzzleAwardCfg.getConfigList(puzzleCfg.getPeriods());
		Optional<SeasonPuzzleAwardCfg> configOp = configList.stream().filter(e -> e.getItemId() == itemId).findAny();
		if (!configOp.isPresent()) {
			return Status.Error.SEASON_PUZZLE_ITEM_INVALID_VALUE;
		}
		Optional<SeasonPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		int itemCount = this.getDataGeter().getItemNum(playerId, itemId);
		if (itemCount <= 0) {
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}
		
		String lockKey = getHelpLockKey(uuid);
		boolean succ = getRedis().setNx(lockKey, playerId);
		if (!succ) {
			return Status.Error.SEASON_PUZZLE_HELP_ONCE_VALUE;
		}
		
		getRedis().expire(lockKey, 86400);
		SeasonPuzzleEntity entity = opEntity.get();
		RewardItem.Builder consume = RewardHelper.toRewardItem(30000, itemId, 1);
		boolean flag = this.getDataGeter().cost(playerId, Arrays.asList(consume), 1, Action.SEASON_PUZZLE_SEND_ITEM, false);
		if (!flag) {
			getRedis().del(lockKey);
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}
		
		if (builder.getType() != CallHelpType.CALL_HELP_TO_PERSON) {
			String zsetKey = getCallHelpZsetKey(builder.getType().getNumber(), this.getDataGeter().getServerId());
			if (builder.getType() == CallHelpType.CALL_HELP_TO_GUILD) {
				zsetKey = zsetKey + ":" + this.getDataGeter().getGuildId(builder.getPlayerId());
			}
			getRedis().zRem(zsetKey, 0, builder.getUuid());
		}
		
		//给赠送对象玩家发邮件、发消息
		ActivityManager.getInstance().postEvent(new SeasonPuzzleSendItemEvent(builder.getPlayerId(), uuid, itemId));
		Object[] mailContent = new Object[3];
		mailContent[0] = this.getDataGeter().getPlayerName(playerId);
		mailContent[1] = itemId;
		mailContent[2] = playerId;
		sendMailToPlayer(builder.getPlayerId(), MailId.SEASON_PUZZLE_SEND_ITEM, null, null, mailContent, null);
		
		entity.addItemSendCount(1);
		if (entity.getItemSendCount() <= SeasonPuzzleConstCfg.getInstance().getGivePuzzleTimesLimit()) {
			List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemImmutableList(configOp.get().getGiveRewards());
			this.getDataGeter().takeReward(playerId, rewardItems, 1, Action.SEASON_PUZZLE_SEND_ITEM, true, RewardOrginType.ACTIVITY_REWARD);
		}
		
		SendItemResp.Builder respBuilder = SendItemResp.newBuilder();
		respBuilder.setUuid(builder.getUuid());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.SEASON_PUZZLE_SEND_ITEM_S, respBuilder));
		
		//赠送碎片tlog打点
		Map<String, Object> param = new HashMap<>();
        param.put("toPlayerId", builder.getPlayerId()); //给谁送
        param.put("sendItemId", itemId);      //送出的道具id
        param.put("callHelpId", uuid);        //求助的uuid
        param.put("sendTimes", entity.getItemSendCount()); //当天送出过多少次了
        getDataGeter().logActivityCommon(playerId, LogInfoType.season_puzzle_send, param);
		
		HawkLog.logPrintln("activity373 sendPuzzleItem end, playerId: {}, type: {}, itemId: {}, uuid: {}, toPlayer: {}", playerId, builder.getType(), itemId, builder.getUuid(), builder.getPlayerId());
		syncActivityInfo(playerId, entity);
		return 0;
	}
	
	/**
	 * 获取拼图期数配置
	 * @return
	 */
	private SeasonPuzzleTrayCfg getPeriodConfig() {
		long time = HawkTime.getMillisecond();
		ConfigIterator<SeasonPuzzleTrayCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SeasonPuzzleTrayCfg.class);
		while(iterator.hasNext()) {
			SeasonPuzzleTrayCfg cfg = iterator.next();
			if (time >= cfg.getOpenTimeValue() && time <= cfg.getEndTimeValue()) {
				return cfg;
			}
		}
		
		return null;
	}
	
	/**
	 * 放入碎片进行拼图
	 * @param playerId
	 * @param setIndex
	 * @return
	 */
	public int setPuzzleItem(String playerId, List<Integer> setIndexList) {
		if (!this.isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		SeasonPuzzleTrayCfg puzzleCfg = this.getPeriodConfig();
		if (puzzleCfg == null) {
			return Status.Error.SEASON_PUZZLE_PERIOD_EMPTY_VALUE;
		}
		if (setIndexList.isEmpty()) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		Optional<SeasonPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		SeasonPuzzleEntity entity = opEntity.get();
		Set<Integer> setIndexSet = new HashSet<>();
		setIndexSet.addAll(setIndexList);
		
		List<Integer> realAdd = new ArrayList<>();
		List<RewardItem.Builder> consumeItems = new ArrayList<>();
		List<RewardItem.Builder> rewardItems = new ArrayList<>();
		List<SeasonPuzzleAwardCfg> configList = SeasonPuzzleAwardCfg.getConfigList(puzzleCfg.getPeriods());
		for (int setIndex : setIndexSet) {
			Optional<SeasonPuzzleAwardCfg> configOp = configList.stream().filter(e -> e.getSlotMark() == setIndex).findAny();
			if (!configOp.isPresent()) {
				continue;
			}
			if (entity.getItemSetIndexList().contains(setIndex)) {
				continue;
			}
			realAdd.add(setIndex);
			SeasonPuzzleAwardCfg cfg = configOp.get();
			consumeItems.add(RewardHelper.toRewardItem(cfg.getItem()));
			rewardItems.addAll(RewardHelper.toRewardItemImmutableList(cfg.getRewards()));
		}
		
		if (realAdd.isEmpty()) {
			return Status.Error.SEASON_PUZZLE_SET_REPEATED_VALUE;
		}
		boolean flag = this.getDataGeter().cost(playerId, consumeItems, 1, Action.SEASON_PUZZLE_CONSUME, false);
		if (!flag) {
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}
		
		int complete = 0;
		entity.addItemSetIndexList(realAdd);
		if (entity.getItemSetIndexList().size() >= puzzleCfg.getPuzzleItemList().size()) {
			complete = 1;
			rewardItems.addAll(RewardHelper.toRewardItemImmutableList(puzzleCfg.getRewards()));
			puzzleCompleteLocal.addAndGet(1);
		}
		
		this.getDataGeter().takeReward(playerId, rewardItems, 1, Action.SEASON_PUZZLE_REWARD, true, RewardOrginType.ACTIVITY_REWARD);
		
		//放置拼图碎片tlog打点
		Map<String, Object> param = new HashMap<>();
        param.put("setIndex", SerializeHelper.collectionToString(setIndexSet, ",")); //请求放入的格子
        param.put("realSet", SerializeHelper.collectionToString(realAdd, ","));      //实际放入成功的格子
        param.put("complete", complete);    //是否完成拼图：1是0否
        getDataGeter().logActivityCommon(playerId, LogInfoType.season_puzzle_set, param);
		
		HawkLog.logPrintln("activity373 setPuzzleItem end, playerId: {}, setIndex: {}, realAdd: {}", playerId, setIndexSet, realAdd);
		syncActivityInfo(playerId, entity);
		return 0;
	}
	
	/**
	 * 查看求助信息
	 * @param playerId
	 * @param type
	 * @param page
	 * @return
	 */
	public int queryCallHelpInfo(String playerId, CallHelpType type, int page) {
		if (!this.isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		String zsetKey = getCallHelpZsetKey(type.getNumber(), this.getDataGeter().getServerId());
		if (type == CallHelpType.CALL_HELP_TO_GUILD) {
			zsetKey = zsetKey + ":" + this.getDataGeter().getGuildId(playerId);
		} else if (type == CallHelpType.CALL_HELP_TO_PERSON) {
			zsetKey = zsetKey + ":" + playerId;
		}
		
		if (page <= 0) {
			page = 1;
		}
		long start = (page - 1) * 100, end = page * 100;
		
//		long count = getRedis().zCount(zsetKey, 0, Long.MAX_VALUE);
//		while (start > count) {
//			start -= 100;
//			end -= 100;
//		}
//		if (start < 0) {
//			start = 0;
//			end = 100;
//		}
		
		Set<String> uuidSet = getRedis().zRange(zsetKey, start, end, 0);
		
		List<Object> returnObjs = Collections.emptyList();
		try (Jedis jedis = getRedis().getJedis(); Pipeline pip = jedis.pipelined()) {
			for (String uuid : uuidSet) {
				String key = getCallHelpInfoKey(uuid);
				pip.get(key.getBytes());
			}
			returnObjs = pip.syncAndReturnAll();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		CallHelpInfoResp.Builder respBuilder = CallHelpInfoResp.newBuilder();
		respBuilder.setType(type);
		for (Object obj : returnObjs) {
			if (obj == null) {
				continue;
			}
			byte[] val = (byte[]) obj;
			CallHelpInfoPB.Builder builder = CallHelpInfoPB.newBuilder();
			try {
				builder.mergeFrom(val);
				//排除自己
				if (builder.getPlayerId().equals(playerId)) {
					continue;
				}
				if (!HawkTime.isToday(builder.getTime())) {
					continue;
				}
				respBuilder.addCallHelpInfo(builder);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.SEASON_PUZZLE_CALL_INFO_S, respBuilder));
		return 0;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		if (!this.isOpening(playerId)) {
			return;
		}
		Optional<SeasonPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		this.syncActivityInfo(playerId, opEntity.get());
	}

	/**
	 * 同步界面信息
	 * @param playerId
	 * @param entity
	 */
	protected void syncActivityInfo(String playerId, SeasonPuzzleEntity entity) {
		if (!isOpening(playerId)) {
			return;
		}
		
		SeasonPuzzleActivityInfo.Builder builder = SeasonPuzzleActivityInfo.newBuilder();
		builder.setCompletePuzzleTimes(puzzleCompleteGlobal.get());
		builder.addAllPuzzleSetIndex(entity.getItemSetIndexList());
		builder.setSendItemCount(entity.getItemSendCount());
		builder.setItemGotCount(entity.getItemGetCount());
		for (CallHelperInfo info : entity.getCallHelpInfoList()) {
			builder.addCallHelpInfo(info.toBuilder());
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.SEASON_PUZZLE_ACT_INFO_S, builder));
	}
	
	/**
	 * 同步任务完成情况
	 * @param playerId
	 * @param entity
	 */
	protected void syncMissionInfo(String playerId, List<AchieveItem> achieveItems, boolean all) {
		SeasonPuzzleMissionSync.Builder builder = SeasonPuzzleMissionSync.newBuilder();
		builder.setAllMission(all ? 1 : 0);
		for (AchieveItem item : achieveItems) {
			int completeTimes = item.getValue(1);
			if (completeTimes <= 0) {
				continue;
			}
			SeasonPuzzleMissionPB.Builder mission = SeasonPuzzleMissionPB.newBuilder();
			mission.setAchieveId(item.getAchieveId());
			mission.setCompleteTimes(completeTimes);
			builder.addMission(mission);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.SEASON_PUZZLE_MISSION_SYNC, builder));
	}

	/**
	 * 获取活动的结束时间
	 * @return
	 */
	protected long getEndTime() {
		int termId = this.getActivityTermId();
		return this.getTimeControl().getEndTimeByTermId(termId);
	}
	
	
	/**
	 * 打野事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(MonsterAttackEvent event) {
		if (!event.isKill()) {
			return;
		}
		onEventProcess(event.getPlayerId(), AchieveType.MONSTER_KILL_NUM, event.getAtkTimes(), event.getMonsterId(), 1);
	}
	
	/**
	 * 日常任务积分事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(AddTavernScoreEvent event) {
		onEventProcess(event.getPlayerId(), AchieveType.TAVERN_SCORE, event.getScore(), 0, 0);
	}
	
	/**
	 * 攻打幽灵基地
	 * @param event
	 */
	@Subscribe
	public void onEvent(AttackFoggyEvent event) {
		if (event.isMass()) {
			onEventProcess(event.getPlayerId(), AchieveType.MASS_ATTACK_FOGGY, 1, 0, 0);
		}
	}
	
	/**
	 * 消耗货币
	 * @param event
	 */
	@Subscribe
	public void onEvent(ConsumeMoneyEvent event) {
		onEventProcess(event.getPlayerId(), AchieveType.CONSUME_MONEY, (int)event.getNum(), event.getResType(), 1);
	}
	
	/**
	 * 治疗伤兵
	 * @param event
	 */
	@Subscribe
	public void onEvent(TreatArmyEvent event) {
		onEventProcess(event.getPlayerId(), AchieveType.TREAT_ARMY, event.getCount(), 0, 0);
	}
	
	/**
	 * 使用道具加速x分钟
	 * @param event
	 */
	@Subscribe
	public void onEvent(UseItemSpeedUpEvent event) {
		onEventProcess(event.getPlayerId(), AchieveType.USE_ITEM_SPEED_UP, event.getMinute(), 0, 0);
	}
	
	/**
	 * 黑市商店购买次数
	 * @param event
	 */
	@Subscribe
	public void onEvent(TravelShopPurchaseEvent event) {
		if (!event.isCommonPool()) {
			return;
		}
		onEventProcess(event.getPlayerId(), AchieveType.TRAVEL_SHOP_PUCHASE_TIMES, 1, 0, 0);
	}
	
	/**
	 * 某个兵种训练（训练完成）多少个
	 * @param event
	 */
	@Subscribe
	public void onEvent(TrainSoldierCompleteEvent event) {
		onEventProcess(event.getPlayerId(), AchieveType.TRAIN_SOLDIER_COMPLETE_NUM, event.getNum(), event.getTrainId(), 1);
	}
	
	/**
	 * 采集某种资源达到多少
	 * @param event
	 */
	@Subscribe
	public void onEvent(ResourceCollectEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<SeasonPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SeasonPuzzleTrayCfg puzzleCfg = this.getPeriodConfig();
		if (puzzleCfg == null) {
			return;
		}
		
		SeasonPuzzleEntity entity = opEntity.get();
		int itemGotCount = entity.getItemGetCount();
		//碎片获得数量达到上限后，不再触发任务
		if (itemGotCount >= SeasonPuzzleConstCfg.getInstance().getAchieveGetPuzzleTimesLimit()) {
			return;
		}
		
		List<AchieveItem> itemList = new ArrayList<>();
		ListValueData listValueData = new ListValueData();
		for (AchieveItem item : entity.getItemList()) {
			try {
				SeasonPuzzleAchieveCfg achieveConfig = HawkConfigManager.getInstance().getConfigByKey(SeasonPuzzleAchieveCfg.class, item.getAchieveId());
				if (puzzleCfg.getPeriods() != achieveConfig.getPeriods()) {
					continue;
				}
				if (achieveConfig.getAchieveType() != AchieveType.RESOURCE_COLLECT || item.getState() != AchieveState.NOT_ACHIEVE_VALUE) {
					continue;
				}

				double addNum = 0;
				if (achieveConfig.getConditionValue(0) == 0) { //任意资源成就项
					Map<Integer, Double> collectMap = event.getCollectMap();
					for (Entry<Integer, Double> collectEntry: collectMap.entrySet()) {
						addNum = collectEntry.getValue() * event.getResWeight(collectEntry.getKey());
					}
				} else { //指定资源成就项
					List<Integer> conditionValues = achieveConfig.getConditionValues();
					Set<Integer> resourceTypes = event.getCollectMap().keySet();
					for (int resourceType : resourceTypes) {
						if (!listValueData.isInList(conditionValues, resourceType)) {
							break;
						}
						Double collectNum = event.getCollectNum(resourceType);
						if (collectNum == null || collectNum <= 0) {
							break;
						}
						addNum = event.getCollectNum(resourceType);
					}
				}
				int realAdd = (int)addNum;
				if (realAdd > 0) {
					int configNum = achieveConfig.getConditionValue(achieveConfig.getConditionValues().size() - 1);
					boolean complete = updateAchieveData(entity, item, achieveConfig, realAdd, configNum);
					if (complete) {
						itemList.add(item);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (!itemList.isEmpty()) {
			syncMissionInfo(playerId, itemList, false);
		}
		
		if (entity.getItemGetCount() > itemGotCount) {
			syncActivityInfo(playerId, entity);
		}
	}
	
	/**
	 * 任务事件处理
	 * @param playerId
	 * @param achieveType
	 * @param addCount
	 * @param eventParam
	 * @param configValIndex
	 * @param listValueData
	 */
	private void onEventProcess(String playerId, AchieveType achieveType, int addCount, int eventParam, int configValIndex) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<SeasonPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SeasonPuzzleTrayCfg puzzleCfg = this.getPeriodConfig();
		if (puzzleCfg == null) {
			return;
		}
		
		SeasonPuzzleEntity entity = opEntity.get();
		int itemGotCount = entity.getItemGetCount();
		//碎片获得数量达到上限后，不再触发任务
		if (itemGotCount >= SeasonPuzzleConstCfg.getInstance().getAchieveGetPuzzleTimesLimit()) {
			return;
		}
				
		List<AchieveItem> itemList = new ArrayList<>();
		for (AchieveItem achieveItem : entity.getItemList()) {
			try {
				SeasonPuzzleAchieveCfg achieveConfig = HawkConfigManager.getInstance().getConfigByKey(SeasonPuzzleAchieveCfg.class, achieveItem.getAchieveId());
				if (puzzleCfg.getPeriods() != achieveConfig.getPeriods()) {
					continue;
				}
				if (achieveConfig.getAchieveType() != achieveType || achieveItem.getState() != AchieveState.NOT_ACHIEVE_VALUE) {
					continue;
				}
				int conditionVal = achieveConfig.getConditionValue(0);
				if (eventParam > 0 && conditionVal != 0 && eventParam != conditionVal) {
					continue;
				}
				boolean complete = updateAchieveData(entity, achieveItem, achieveConfig, addCount, achieveConfig.getConditionValue(configValIndex));
				if (complete) {
					itemList.add(achieveItem);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (!itemList.isEmpty()) {
			syncMissionInfo(playerId, itemList, false);
		}
		if (entity.getItemGetCount() > itemGotCount) {
			syncActivityInfo(playerId, entity);
		}
	}
	
	/**
	 * 更新任务成就数据
	 * @param entity
	 * @param achieveType
	 * @param addValue
	 */
	private boolean updateAchieveData(SeasonPuzzleEntity entity, AchieveItem achieveItem, SeasonPuzzleAchieveCfg achieveConfig, int addValue, int configVal) {
		int oldValue = achieveItem.getValue(0), oldTimes = achieveItem.getValue(1);
		int newValue = oldValue + addValue;
		achieveItem.setValue(0, Math.min(newValue, configVal));
		boolean completeTimesChange = false;
		int puzzleGotLimit = SeasonPuzzleConstCfg.getInstance().getAchieveGetPuzzleTimesLimit();
		int addTimes = 0;
		if (newValue >= configVal) {
			completeTimesChange = true;
			int remainAdd = puzzleGotLimit - entity.getItemGetCount();
			addTimes = Math.min(remainAdd, newValue/configVal);
			achieveItem.setValue(1, oldTimes + addTimes);
			if (achieveItem.getValue(1) >= achieveConfig.getGetTimesLimit()) {
				addTimes = achieveConfig.getGetTimesLimit() - oldTimes;
				achieveItem.setValue(1, achieveConfig.getGetTimesLimit());
				achieveItem.setState(AchieveState.TOOK_VALUE);
			} else {
				achieveItem.setValue(0, newValue % configVal);
			}
		}
		
		entity.notifyUpdate();
		boolean complete = oldValue < configVal && configVal <= newValue;
		if (!complete) {
			return completeTimesChange;
		}
		
		//发奖
		int addCount = 0;
		List<RewardItem.Builder> builderList = new ArrayList<>();
		for (int i = 0; i < addTimes; i++) {
			String itemStr = HawkRand.randomWeightObject(achieveConfig.getRewardStrList(), achieveConfig.getRewardWeightList());
			RewardItem.Builder builder = RewardHelper.toRewardItem(itemStr);
			boolean contains = SeasonPuzzleAwardCfg.getPuzzleItemIds().contains(builder.getItemId());
			if (!contains) {
				builderList.add(builder);
			} else {
				if (entity.getItemGetCount() + addCount >= puzzleGotLimit) {
					break;
				}
				addCount++;
				builderList.add(builder);
			}
			
			//发奖励邮件凭证
			Object[] mailContent = new Object[2];
			mailContent[0] = achieveConfig.getAchieveId();
			mailContent[1] = builder.getItemId();  //道具id
			sendMailToPlayer(entity.getPlayerId(), MailId.SEASON_PUZZLE_ACHIEVE_ITEM, null, null, mailContent, Arrays.asList(builder), true);
		}
		
		if (addCount > 0) {
			entity.addItemGetCount(addCount);
		}
		
		if (!builderList.isEmpty()) {
			this.getDataGeter().takeReward(entity.getPlayerId(), builderList, 1, Action.SEASON_PUZZLE_ACHIEVE, false, RewardOrginType.ACTIVITY_REWARD);
		}

		return completeTimesChange;
	}
	
	
	/**
	 * 记录统计全服拼图完成次数的key
	 * @return
	 */
	public String getGlobalPuzzleKey(){
		return SeasonPuzzleConst.REDIS_KEY_PUZZLE_TIMES + getActivityTermId();
	}
	
	public String getCallHelpInfoKey(String uuid) {
		return SeasonPuzzleConst.REDIS_KEY_CALL_HELP + getActivityTermId() + ":" + uuid;
	}
	
	public String getCallHelpZsetKey(int type, String serverId) {
		String dayTime = HawkTime.formatNowTime("yyyyMMdd");
		return SeasonPuzzleConst.REDIS_KEY_CALL_HELP + getActivityTermId() + ":" + dayTime + ":" + serverId + ":" + type;
	}
	
	public String getHelpLockKey(String uuid) {
		return SeasonPuzzleConst.REDIS_KEY_HELP_LOCK + uuid;
	}
	
	public HawkRedisSession getRedis() {
		return ActivityGlobalRedis.getInstance().getRedisSession();
	}
	
	public int getRedisExpire() {
		return 86400 * 30;
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

}
