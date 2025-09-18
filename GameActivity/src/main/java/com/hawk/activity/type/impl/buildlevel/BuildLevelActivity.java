package com.hawk.activity.type.impl.buildlevel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.buildlevel.cfg.BuildLevelActivityCfg;
import com.hawk.activity.type.impl.buildlevel.entity.ActivityBuildLevelEntity;
import com.hawk.activity.type.impl.buildlevel.entity.BuildLevelItem;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.BuildLevelInfoSync;
import com.hawk.game.protocol.Activity.BuildLevelInfoSync.Builder;
import com.hawk.game.protocol.Activity.BuildLevelItemPB;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 建筑等级活动
 * 
 * @author PhilChen
 *
 */
public class BuildLevelActivity extends ActivityBase {

	public BuildLevelActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.BUILD_LEVEL_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new BuildLevelActivity(config.getActivityId(), activityEntity);
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_BUILD_LEVEL, () -> {
				Optional<ActivityBuildLevelEntity> opEntity = getPlayerDataEntity(playerId);
				if(opEntity.isPresent()){
					checkNewConfig(opEntity.get());
				}
			});
		}
	}

	@Subscribe
	public void onEvent(BuildingLevelUpEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		// 获取玩家活动数据
		String playerId = event.getPlayerId();
		Optional<ActivityBuildLevelEntity> opEntity = getPlayerDataEntity(playerId);
		if(opEntity.isPresent()){
			updateActivityData(opEntity.get());
		}
	}

	/**
	 * 向玩家推送数据变更
	 * @param playerId
	 * @param updateList
	 */
	private void pushDataChange(String playerId, List<BuildLevelItem> updateList) {
		Builder builder = BuildLevelInfoSync.newBuilder();
		for (BuildLevelItem item : updateList) {
			BuildLevelItemPB.Builder itemPB = BuildLevelItemPB.newBuilder();
			itemPB.setItemId(item.getItemId());
			itemPB.setLevel(item.getBuildLevel());
			itemPB.setState(item.getState());
			builder.addItem(itemPB);
		}
		logger.debug("push activity data change, playerId: {} ,updateList: {}", playerId, updateList);
		// 向玩家推送数据变更
		pushToPlayer(playerId, HP.code.PUSH_BUILD_LEVEL_CHANGE_S_VALUE, builder);
	}
	
	private void updateActivityData(ActivityBuildLevelEntity dataEntity) {
		List<BuildLevelItem> updateList = new ArrayList<>();
		String playerId = dataEntity.getPlayerId();
		List<BuildLevelItem> itemList = dataEntity.getItemList();
		for (BuildLevelItem item : itemList) {
			// 只有处于未达成状态才需要进行数据更新
			if (item.getState() != AchieveState.NOT_ACHIEVE_VALUE) {
				continue;
			}
			BuildLevelActivityCfg config = HawkConfigManager.getInstance().getConfigByKey(BuildLevelActivityCfg.class, item.getItemId());
			if (config == null) {
				logger.error("build level activity item contig not found! itemId: {}", item.getItemId());
				continue;
			}
			int buildLvl = getDataGeter().getBuildMaxLevel(playerId, config.getBuildType());
			item.setBuildLevel(buildLvl);
			if (item.getBuildLevel() >= config.getLevel()) {
				item.setBuildLevel(config.getLevel());
				// 判断建筑等级是否达到活动配置要求
				item.setState(AchieveState.NOT_REWARD_VALUE);
			}
			updateList.add(item);
		}
		if (updateList.size() > 0) {
			dataEntity.itemsToString();
		}
		if (!updateList.isEmpty()) {
			pushDataChange(playerId, updateList);
		}
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if(isInvalid()){
			return;
		}
		
		Optional<ActivityBuildLevelEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		
		if (isOpening(playerId)) {
			// 检查当前配置中是否存在新增的活动项
			checkNewConfig(opEntity.get());
		}
	}

	/**
	 * 同步数据至客户端
	 * @param playerId
	 * @param dataEntity
	 */
	private void syncActivityInfo(String playerId, ActivityBuildLevelEntity dataEntity) {
		// 玩家登录时同步数据至客户端
		Builder builder = BuildLevelInfoSync.newBuilder();
		for (BuildLevelItem item : dataEntity.getItemList()) {
			BuildLevelItemPB.Builder itemPB = BuildLevelItemPB.newBuilder();
			itemPB.setItemId(item.getItemId());
			itemPB.setLevel(item.getBuildLevel());
			itemPB.setState(item.getState());
			builder.addItem(itemPB);
		}
		pushToPlayer(playerId, HP.code.PUSH_BUILD_LEVEL_INFO_SYNC_S_VALUE, builder);
		logger.debug("sync activity info, playerId: {}, itemList: {}", playerId, dataEntity.getItemList());
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<ActivityBuildLevelEntity> opDataEntity = getPlayerDataEntity(playerId);
		if(opDataEntity.isPresent()){
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	/**
	 * 检查当前配置中是否存在新增的活动项
	 * @param dataEntity
	 */
	private void checkNewConfig(ActivityBuildLevelEntity dataEntity) {
		Set<Integer> itemIdSet = new HashSet<>();
		for (BuildLevelItem item : dataEntity.getItemList()) {
			itemIdSet.add(item.getItemId());
		}
		boolean change = false;
		ConfigIterator<BuildLevelActivityCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(BuildLevelActivityCfg.class);
		while (configIterator.hasNext()) {
			BuildLevelActivityCfg config = configIterator.next();
			if (itemIdSet.contains(config.getItemId())) {
				continue;
			}
			BuildLevelItem item = BuildLevelItem.valueOf(config.getItemId(), 0, AchieveState.NOT_ACHIEVE_VALUE);
			dataEntity.addItem(item);
			change = true;
		}
		if (change) {
			dataEntity.itemsToString();
		}
		updateActivityData(dataEntity);
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityBuildLevelEntity> queryList = HawkDBManager.getInstance()
				.query("from ActivityBuildLevelEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ActivityBuildLevelEntity entity = queryList.get(0);
			entity.stringToItems();
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityBuildLevelEntity entity = new ActivityBuildLevelEntity(playerId, termId);
		return entity;
	}

	@Override
	public boolean isActivityClose(String playerId) {
		Optional<ActivityBuildLevelEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return false;
		}
		ActivityBuildLevelEntity entity = opEntity.get();
		// 所有奖励已领取完,则活动隐藏
		List<BuildLevelItem> itemList = entity.getItemList();
		if(itemList == null || itemList.isEmpty()){
			return false;
		}
		
		for (BuildLevelItem item : entity.getItemList()) {
			if (item.getState() != AchieveState.TOOK_VALUE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 领取活动奖励
	 * @param playerId
	 * @param itemId
	 * @return
	 */
	public Result<?> takeRewards(String playerId, int itemId) {
		Optional<ActivityBuildLevelEntity> opDataEntity = getPlayerDataEntity(playerId);
		if(!opDataEntity.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		ActivityBuildLevelEntity dataEntity = opDataEntity.get();
		BuildLevelItem item = dataEntity.getItem(itemId);
		if (item == null) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		BuildLevelActivityCfg config = HawkConfigManager.getInstance().getConfigByKey(BuildLevelActivityCfg.class, itemId);
		if (config == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		
		// 未达成
		if(item.getState() == AchieveState.NOT_ACHIEVE_VALUE){
			return Result.fail(Status.Error.ACTIVITY_CAN_NOT_TAKE_REWARD_VALUE);
		}
		
		// 已领取
		if (item.getState() == AchieveState.TOOK_VALUE) {
			return Result.fail(Status.Error.ACTIVITY_REWARD_IS_TOOK_VALUE);
		}
		
		item.setState(AchieveState.TOOK_VALUE);
		dataEntity.itemsToString();

		ActivityReward reward = new ActivityReward(config.getRewardList(), Action.ACTIVITY_REWARD_BUILD_LEVEL);
		reward.setOrginType(RewardOrginType.ACTIVITY_REWARD, getActivityId());
		reward.setAlert(true);
		postReward(playerId, reward);
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), itemId);
		
		List<BuildLevelItem> itemList = new ArrayList<>();
		itemList.add(item);
		pushDataChange(playerId, itemList);
		checkActivityClose(playerId);
		logger.debug("[activity] build level activity take reward, playerId: {}, itemId: {}", playerId, itemId);
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
