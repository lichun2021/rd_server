package com.hawk.activity.type.impl.jigsawconnect326;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
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
import com.hawk.activity.event.impl.AddJigsawConnectScoreEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayJigsawConnectEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.jigsawconnect326.cfg.JXJigsawConnectAchieveCfg;
import com.hawk.activity.type.impl.jigsawconnect326.cfg.JXJigsawConnectChestCfg;
import com.hawk.activity.type.impl.jigsawconnect326.cfg.JXJigsawConnectComboCfg;
import com.hawk.activity.type.impl.jigsawconnect326.entity.JXJigsawConnectEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 双十一拼图活动
 * 16个任务组成4 * 4的格子,四点连线完成成就
 * @author hf
 *
 */
public class JXJigsawConnectActivity extends ActivityBase implements AchieveProvider{

	public final Logger logger = LoggerFactory.getLogger("Server");
	
	public JXJigsawConnectActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	@Override
	public ActivityType getActivityType() {
		return ActivityType.JXJIGSAW_CONNECT_ACTIVITY;
	}
	
	@Override
	public Action takeRewardAction() {
		return Action.ACTIVITY_JXJIGSAW_CONNECT_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		JXJigsawConnectActivity activity = new JXJigsawConnectActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_JIGSAW_CONNECT, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<JXJigsawConnectEntity> queryList = HawkDBManager.getInstance()
				.query("from JXJigsawConnectEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			JXJigsawConnectEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		JXJigsawConnectEntity entity = new JXJigsawConnectEntity(playerId, termId);
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
		Optional<JXJigsawConnectEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		JXJigsawConnectEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	public void initAchieveInfo(String playerId){
		Optional<JXJigsawConnectEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		JXJigsawConnectEntity entity = optional.get();
		//成就是否已经初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		//积分成就
		ConfigIterator<JXJigsawConnectChestCfg> scoreIterator = HawkConfigManager.getInstance().getConfigIterator(JXJigsawConnectChestCfg.class);
		while(scoreIterator.hasNext()){
			JXJigsawConnectChestCfg nextScore = scoreIterator.next();
			AchieveItem item = AchieveItem.valueOf(nextScore.getAchieveId());
			entity.addItem(item);
		}
		//初始化成就项
		ConfigIterator<JXJigsawConnectAchieveCfg> cIterator = HawkConfigManager.getInstance().getConfigIterator(JXJigsawConnectAchieveCfg.class);
		while (cIterator.hasNext()) {
			JXJigsawConnectAchieveCfg next = cIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		ActivityManager.getInstance().postEvent(new LoginDayJigsawConnectEvent(playerId, 1), true);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(JXJigsawConnectAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(JXJigsawConnectChestCfg.class, achieveId);
		}
		return config;
	}


	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		JXJigsawConnectAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(JXJigsawConnectAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			JXJigsawConnectChestCfg scoreCfg = HawkConfigManager.getInstance().getConfigByKey(JXJigsawConnectChestCfg.class, achieveId);
			if (scoreCfg == null) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
		}
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public void onTakeRewardSuccess(String playerId) {
		/**领奖之后,计算四点连线完成个数  刷成就事件*/
		int connectLineNum = connectFinishNum(playerId);
		if (connectLineNum > 0){
			ActivityManager.getInstance().postEvent(new AddJigsawConnectScoreEvent(playerId, connectLineNum));
		}
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		int termId = getActivityTermId(playerId);
		long endTime = getTimeControl().getEndTimeByTermId(termId, playerId);
		long now = HawkTime.getMillisecond();
		if (now >= endTime) {
			return;
		}
		Optional<JXJigsawConnectEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		JXJigsawConnectEntity entity = opEntity.get();
		if (!HawkTime.isSameDay(entity.getRefreshTime(), now)) {
			entity.setLoginDays(entity.getLoginDays() + 1);
			entity.setRefreshTime(now);
			entity.notifyUpdate();
		}
		ActivityManager.getInstance().postEvent(new LoginDayJigsawConnectEvent(playerId, entity.getLoginDays()), true);
	}
	/**
	 * 计算所有已完成的4点连线的个数
	 * @param playerId
	 * @return
	 */
	public int connectFinishNum(String playerId){
		Optional<JXJigsawConnectEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return 0;
		}
		JXJigsawConnectEntity entity = optional.get();
		//所有涉及4连的线
		List<JXJigsawConnectComboCfg> connectLineList = HawkConfigManager.getInstance().getConfigIterator(JXJigsawConnectComboCfg.class).toList();
		if (connectLineList == null){
			return 0;
		}
		//一共几组4连的线
		int lineNum = 0;
		for (JXJigsawConnectComboCfg cfg: connectLineList) {
			boolean complete = true;
			List<Integer> lineConnectList = cfg.getAchieveConnectList();
			for (Integer id: lineConnectList) {
				if (entity.getAchieveItem(id) == null || entity.getAchieveItem(id).getState()!= Activity.AchieveState.TOOK_VALUE){
					complete = false;
					break;
				}
			}
			if (complete){
				lineNum ++;
			}
		}
		return lineNum;
	}

	public AchieveItem getAchieveItemById(List<AchieveItem> achieveItemList, int achieveId){
		for (AchieveItem achieveItem: achieveItemList) {
			if (achieveItem.getAchieveId() == achieveId){
				return achieveItem;
			}
		}
		return null;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

}
