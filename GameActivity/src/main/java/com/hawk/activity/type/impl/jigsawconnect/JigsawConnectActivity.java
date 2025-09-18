package com.hawk.activity.type.impl.jigsawconnect;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.jigsawconnect.cfg.JigsawConnectAchieveCfg;
import com.hawk.activity.type.impl.jigsawconnect.cfg.JigsawConnectChestCfg;
import com.hawk.activity.type.impl.jigsawconnect.cfg.JigsawConnectComboCfg;
import com.hawk.activity.type.impl.jigsawconnect.entity.JigsawConnectEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 双十一拼图活动
 * 16个任务组成4 * 4的格子,四点连线完成成就
 * @author hf
 *
 */
public class JigsawConnectActivity extends ActivityBase implements AchieveProvider{

	public final Logger logger = LoggerFactory.getLogger("Server");
	
	public JigsawConnectActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	@Override
	public ActivityType getActivityType() {
		return ActivityType.JIGSAW_CONNECT_ACTIVITY;
	}
	
	@Override
	public Action takeRewardAction() {
		return Action.ACTIVITY_JIGSAW_CONNECT_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		JigsawConnectActivity activity = new JigsawConnectActivity(config.getActivityId(), activityEntity);
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
		List<JigsawConnectEntity> queryList = HawkDBManager.getInstance()
				.query("from JigsawConnectEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			JigsawConnectEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		JigsawConnectEntity entity = new JigsawConnectEntity(playerId, termId);
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
		Optional<JigsawConnectEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		JigsawConnectEntity entity = optional.get();
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
		Optional<JigsawConnectEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		JigsawConnectEntity entity = optional.get();
		//成就是否已经初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		//积分成就
		ConfigIterator<JigsawConnectChestCfg> scoreIterator = HawkConfigManager.getInstance().getConfigIterator(JigsawConnectChestCfg.class);
		while(scoreIterator.hasNext()){
			JigsawConnectChestCfg nextScore = scoreIterator.next();
			AchieveItem item = AchieveItem.valueOf(nextScore.getAchieveId());
			entity.addItem(item);
		}
		//初始化成就项
		ConfigIterator<JigsawConnectAchieveCfg> cIterator = HawkConfigManager.getInstance().getConfigIterator(JigsawConnectAchieveCfg.class);
		while (cIterator.hasNext()) {
			JigsawConnectAchieveCfg next = cIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		ActivityManager.getInstance().postEvent(new LoginDayJigsawConnectEvent(playerId, 1), true);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(JigsawConnectAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(JigsawConnectChestCfg.class, achieveId);
		}
		return config;
	}


	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		JigsawConnectAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(JigsawConnectAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			JigsawConnectChestCfg scoreCfg = HawkConfigManager.getInstance().getConfigByKey(JigsawConnectChestCfg.class, achieveId);
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
		Optional<JigsawConnectEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		JigsawConnectEntity entity = opEntity.get();
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
		Optional<JigsawConnectEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return 0;
		}
		JigsawConnectEntity entity = optional.get();
		//所有涉及4连的线
		List<JigsawConnectComboCfg> connectLineList = HawkConfigManager.getInstance().getConfigIterator(JigsawConnectComboCfg.class).toList();
		if (connectLineList == null){
			return 0;
		}
		//一共几组4连的线
		int lineNum = 0;
		for (JigsawConnectComboCfg cfg: connectLineList) {
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
