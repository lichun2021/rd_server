package com.hawk.activity.type.impl.spaceguard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.SpaceMachinePointAddEvent;
import com.hawk.activity.event.impl.SpaceMechaDailyLoginEvent;
import com.hawk.activity.event.impl.SpaceMechaPlaceEvent;
import com.hawk.activity.event.speciality.SpaceMechaEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardAchieveCfg;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardPointCfg;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardTimeCfg;
import com.hawk.activity.type.impl.spaceguard.entity.PointTaskItem;
import com.hawk.activity.type.impl.spaceguard.entity.SpaceGuardEntity;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskContext;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskParser;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.gamelib.GameConst;
import com.hawk.game.protocol.Activity.GuardRecordPB;
import com.hawk.game.protocol.Activity.GuildCoinTaskItemsInfoSync;
import com.hawk.game.protocol.Activity.SpaceMachineGuardActivityInfoPB;
import com.hawk.log.Action;

/**
 * 机甲舱体守护（机甲玩法）活动
 * 
 * @author lating
 *
 */
public class SpaceGuardActivity extends ActivityBase implements AchieveProvider {
	
	public SpaceGuardActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SPACE_GUARD_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SpaceGuardActivity activity = new SpaceGuardActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SpaceGuardEntity> queryList = HawkDBManager.getInstance().query("from SpaceGuardEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SpaceGuardEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SpaceGuardEntity entity = new SpaceGuardEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {		
		Set<String> onlinePlayers = this.getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayers) {
			this.callBack(playerId, GameConst.MsgId.SPACE_MACHA_ACTIVITY_OPEN, ()->{
				login(playerId);
			});
		}
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		login(event.getPlayerId());
	}
	
	/**
	 * 登录
	 * 
	 * @param playerId
	 */
	private void login(String playerId) {
		Optional<SpaceGuardEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		
		SpaceGuardEntity entity = optional.get();
		long lastLoginTime = entity.getLoginTime();
		entity.setLoginTime(HawkTime.getMillisecond());
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(entity);
		}
		if (entity.getTaskItemList().isEmpty()) {
			initPointTaskInfo(entity);
		}
		
		syncPointTaskInfo(entity);
		syncActivityInfo(entity);
		
		if (!HawkTime.isToday(lastLoginTime)) {
			ActivityManager.getInstance().postEvent(new SpaceMechaDailyLoginEvent(playerId));
		}
	}
	
	@Override
	public Action takeRewardAction() {
		return Action.SPACE_GUARD_ACHIEVE;
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
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(SpaceGuardAchieveCfg.class, achieveId);
		return config;
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<SpaceGuardEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		
		SpaceGuardEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(entity);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	private void initAchieveInfo(SpaceGuardEntity entity) {
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		List<AchieveItem> itemList = new ArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<SpaceGuardAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SpaceGuardAchieveCfg.class);
		while (configIterator.hasNext()) {
			SpaceGuardAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			itemList.add(item);
		}
		entity.resetItemList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), entity.getItemList()), true);
	}
	
	/**
	 * 初始出星币任务数据
	 * 
	 * @param entity
	 */
	private void initPointTaskInfo(SpaceGuardEntity entity) {
		// 成就已初始化
		if (!entity.getTaskItemList().isEmpty()) {
			return;
		}
		
		List<PointTaskItem> itemList = new ArrayList<PointTaskItem>();
		// 初始添加成就项
		ConfigIterator<SpaceGuardPointCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SpaceGuardPointCfg.class);
		while (configIterator.hasNext()) {
			SpaceGuardPointCfg next = configIterator.next();
			PointTaskItem item = PointTaskItem.valueOf(next.getId());
			itemList.add(item);
		}
		
		entity.resetTaskItemList(itemList);
	}
	
	/**
	 * 舱体放置事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(SpaceMechaPlaceEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}

		Optional<SpaceGuardEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SpaceGuardEntity entity = opEntity.get();
		syncActivityInfo(entity);
	}
	
	/**
	 * 任务事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(SpaceMechaEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}

		Optional<SpaceGuardEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SpaceGuardEntity dataEntity = opEntity.get();
		// 阶段检测
		List<SpaceMechaTaskParser<?>> parsers = SpaceMechaTaskContext.getParser(event.getClass());
		if (parsers == null) {
			HawkLog.logPrintln("PointTaskParser not found, eventClass: {}", event.getClass().getName());
			return;
		}

		List<PointTaskItem> taskItemList = dataEntity.getTaskItemList();
		boolean update = false;
		List<PointTaskItem> changeList = new ArrayList<>();
		for (SpaceMechaTaskParser<?> parser : parsers) {
			for (PointTaskItem taskItem : taskItemList) {
				SpaceGuardPointCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SpaceGuardPointCfg.class, taskItem.getTaskId());
				if (cfg == null) {
					continue;
				}
				// 判定任务类型是否一致
				if (!cfg.getTaskType().equals(parser.getTaskType())) {
					continue;
				}
				// 完全完成的任务不做处理
				if (parser.finished(taskItem, cfg)) {
					continue;
				}
				int oldPoints = taskItem.getPoints();
				if (parser.onEventUpdate(dataEntity, cfg, taskItem, event.convert())) {
					changeList.add(taskItem);
					update = true;
				}
				
				// 更新联盟代币点数、以及个人贡献代币点数总和
				int addPoints = taskItem.getPoints() - oldPoints;
				updatePoints(dataEntity, addPoints, cfg, taskItem.getPoints());
			}
		}
		
		if (update) {
			dataEntity.notifyUpdate();
			syncPointTaskInfo(dataEntity);
		}
	}
	
	/**
	 * 更新代币点数
	 * @param dataEntity
	 * @param addPoints
	 */
	private void updatePoints(SpaceGuardEntity dataEntity, int addPoints, SpaceGuardPointCfg cfg, int newPoints) {
		if (addPoints <= 0) {
			return;
		}
		
		dataEntity.addPoints(addPoints);
		String guildId = getDataGeter().getGuildId(dataEntity.getPlayerId());
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		
		long newGuildPoints = getDataGeter().getSpaceMechaPoint(guildId) + addPoints;
		getDataGeter().addSpaceMechaPoint(guildId, addPoints);
		String playerId = dataEntity.getPlayerId();
		ActivityManager.getInstance().postEvent(new SpaceMachinePointAddEvent(playerId, addPoints));
		// 星甲召唤：获得星币邮件凭证
		Object[] content = new Object[]{ addPoints };
		getDataGeter().sendMail(playerId, MailId.SPACE_MECHA_POINT_GOT, null, null, content, null, true);
		// 记录获得星币的打点日志
		getDataGeter().logSpaceMechaPointGet(playerId, dataEntity.getTermId(), cfg.getId(), addPoints, newGuildPoints, cfg.getExpLimit() - newPoints);
	}
	
	/**
	 * 获取联盟代币点数
	 * 
	 * @param playerId
	 * @return
	 */
	public long getGuildPoints(int termId, String playerId) {
		String guildId = getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return 0L;
		}
		
		return getDataGeter().getSpaceMechaPoint(guildId);
	}
	
	/**
	 * 同步代币获取任务信息
	 * 
	 * @param entity
	 */
	private void syncPointTaskInfo(SpaceGuardEntity entity) {
		GuildCoinTaskItemsInfoSync.Builder builder = GuildCoinTaskItemsInfoSync.newBuilder();
		for(PointTaskItem item : entity.getTaskItemList()){
			builder.addItem(item.build());
		}
		
		String playerId = entity.getPlayerId();
		builder.setGuildPoints(getGuildPoints(entity.getTermId(), playerId));  // 联盟代币总量
		builder.setPersonalPoints(entity.getPoints());     // 个人贡献代币点数
		pushToPlayer(playerId, HP.code2.SPACE_GUILDCOIN_TASK_INFO_SYNC_VALUE, builder);
	}
	
	/**
	 * 同步活动页面信息
	 * @param playerId
	 */
	public void pushPageInfo(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<SpaceGuardEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SpaceGuardEntity dataEntity = opEntity.get();
		syncActivityInfo(dataEntity);
	}
	
	/**
	 * 同步活动信息
	 * 
	 * @param playerId
	 */
	public void syncActivityInfo(SpaceGuardEntity entity) {
		String playerId = entity.getPlayerId();
		String guildId = getDataGeter().getGuildId(playerId);
		SpaceMachineGuardActivityInfoPB.Builder builder = getDataGeter().getSpaceMechaInfo(guildId);
		if (builder == null) {
			builder = SpaceMachineGuardActivityInfoPB.newBuilder();
			builder.setStage(SpaceMechaStage.SPACE_END_VALUE);
			builder.setSpaceLevelMax(1);  // 目前可达到的舱体最高等级
		} 
		
		builder.setGuildPoints(getGuildPoints(entity.getTermId(), playerId));  // 联盟代币总量
		builder.setPersonalPoints(entity.getPoints());     // 个人贡献代币点数
		builder.setCallMember(getDataGeter().spaceMechaGuildCall(playerId) ? 1 : 0); // 当日是否已号召过盟友
		List<GuardRecordPB.Builder> recordList = getDataGeter().getGuildSpaceRecord(guildId);
		if (recordList != null) {
			for (GuardRecordPB.Builder record : recordList) {
				builder.addGuardRecord(record);
			}
		}
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.SPACE_MACHINE_GUARD_INFO_SYNC_VALUE, builder));
	}
	
	/**
	 * 获取停止放置舱体的时间
	 * 
	 * @param playerId
	 * @return
	 */
	public SpaceGuardTimeCfg getTimeCfg() {
		int termId = getActivityTermId();
		SpaceGuardTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceGuardTimeCfg.class, termId);
		return timeCfg;
	}
	
}
