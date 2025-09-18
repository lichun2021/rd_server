package com.hawk.activity.type.impl.playerteamback;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.PlayerTeamBackH5Event;
import com.hawk.game.protocol.Activity.H5TeamMemberInfo;
import com.hawk.game.protocol.Activity.PlayerTeamBackActivityInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.Platform;
import com.hawk.activity.type.ActivityType;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.playerteamback.cfg.PlayerTeamBackAchieveCfg;
import com.hawk.activity.type.impl.playerteamback.entity.MemberInfo;
import com.hawk.activity.type.impl.playerteamback.entity.PlayerTeamBackEntity;
import com.hawk.common.AccountRoleInfo;

/**
 * 玩家回流H5活动
 * 
 * @author lating
 *
 */
public class PlayerTeamBackActivity extends ActivityBase implements AchieveProvider {

	public PlayerTeamBackActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PLAYER_TEAM_BACK_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PlayerTeamBackActivity activity = new PlayerTeamBackActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PlayerTeamBackEntity> queryList = HawkDBManager.getInstance()
				.query("from PlayerTeamBackEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PlayerTeamBackEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PlayerTeamBackEntity entity = new PlayerTeamBackEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return true;
	}

	@Override
	public Action takeRewardAction() {
		return Action.PLAYER_TEAM_BACK_AWARD_TAKE;
	}

	@Override
	public void onPlayerLogin(String playerId) {
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.PLAYER_TEAM_BACK_INIT, () -> {
				Optional<PlayerTeamBackEntity> opEntity = getPlayerDataEntity(playerId);
				if (opEntity.isPresent()) {
					initAchieveInfo(playerId);
				}
			});
		}
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<PlayerTeamBackEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		PlayerTeamBackEntity entity = optional.get();
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
		Optional<PlayerTeamBackEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		PlayerTeamBackEntity entity = optional.get();
		//成就是否已经初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		List<AchieveItem> items = new CopyOnWriteArrayList<AchieveItem>();
		//初始化成就项
		ConfigIterator<PlayerTeamBackAchieveCfg> cIterator = HawkConfigManager.getInstance().getConfigIterator(PlayerTeamBackAchieveCfg.class);
		while (cIterator.hasNext()) {
			PlayerTeamBackAchieveCfg next = cIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			items.add(item);
		}
		entity.resetItemList(items);
		entity.setRefreshTime(HawkTime.getMillisecond());
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(PlayerTeamBackAchieveCfg.class, achieveId);
		return config;
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		Optional<PlayerTeamBackEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_AVAILABLE_VALUE);
		}
		
		PlayerTeamBackEntity entity = optional.get();
		if (entity.getTeamId() <= 0) {
			return Result.fail(Status.Error.ACTIVITY_CAN_NOT_TAKE_REWARD_VALUE);
		}
		
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
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
		Optional<PlayerTeamBackEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		PlayerTeamBackEntity entity = opEntity.get();
		syncActivityInfo(entity);

		if (!event.isCrossDay() && HawkTime.isSameDay(entity.getRefreshTime(), now)) {
			return;
		}
		
		entity.setRefreshTime(now);
		List<AchieveItem> items = new CopyOnWriteArrayList<AchieveItem>();
		ConfigIterator<PlayerTeamBackAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(PlayerTeamBackAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			PlayerTeamBackAchieveCfg cfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			items.add(item);
		}
		entity.resetItemList(items);
		entity.notifyUpdate();
		// 推送给客户端
		AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
	}
	
	@Subscribe
	public void onH5InfoChangeEvent(PlayerTeamBackH5Event event){
		HawkLog.logPrintln("PlayerTeamBackH5Event touch, playerId: {}", event.getPlayerId());
		Optional<PlayerTeamBackEntity> optional = getPlayerDataEntity(event.getPlayerId());
		if (!optional.isPresent()) {
			return;
		}
		PlayerTeamBackEntity entity = optional.get();
		List<Integer> rewardList = event.getRewardList();
		List<MemberInfo> memberList = event.getMemberList();
		// 奖励信息
		for (int rewardId : rewardList) {
			if (!entity.getRewardList().contains(rewardId)) {
				entity.getRewardList().add(rewardId);
			}
		}
		
		// 战队成员信息
		List<MemberInfo> oldMembers = entity.getMemberList();
		for (MemberInfo member : memberList) {
			boolean exist = false;
			for (MemberInfo mem : oldMembers) {
				if (mem.getOpenId().equals(member.getOpenId())) {
					exist = true;
					break;
				}
			}
			
			if (!exist) {
				oldMembers.add(member);
			}
		}
		
		entity.setStarNum(event.getStarNum());
		entity.setTeamId(event.getTeamId());
		syncActivityInfo(entity);
		
		HawkLog.logPrintln("PlayerTeamBackH5Event touch finish, playerId: {}", event.getPlayerId());
	}

	/**
	 * 同步活动信息
	 * 
	 * @param entity
	 */
	private void syncActivityInfo(PlayerTeamBackEntity entity) {
		PlayerTeamBackActivityInfo.Builder activityBuilder = PlayerTeamBackActivityInfo.newBuilder();
		activityBuilder.setStarNum(entity.getStarNum() > 0 ? entity.getStarNum() : -1);
		activityBuilder.setTeamId(entity.getTeamId() > 0 ? entity.getTeamId() : -1);
		activityBuilder.addAllRewardInfo(entity.getRewardList());
		for (MemberInfo member : entity.getMemberList()) {
			try {
				String platform = Platform.valueOf(member.getPlatId()).name().toLowerCase();
				AccountRoleInfo roleInfo = getDataGeter().getAccountRole(member.getServerId(), platform, member.getOpenId());
				H5TeamMemberInfo.Builder memBuilder = H5TeamMemberInfo.newBuilder();
				memBuilder.setBack(member.getBackFlag());
				memBuilder.setGroupTs(member.getGroupTs());
				memBuilder.setServerId(member.getServerId());
				memBuilder.setPlayerId(roleInfo.getPlayerId());
				memBuilder.setPlayerName(roleInfo.getPlayerName());
				if (roleInfo.getPfIcon() != null) {
					memBuilder.setPfInfo(roleInfo.getPfIcon());
				}
				memBuilder.setIcon(roleInfo.getIcon());
				activityBuilder.addMember(memBuilder);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		pushToPlayer(entity.getPlayerId(), HP.code.PLAYER_TEAMBACK_ACTIVITY_SYNC_VALUE, activityBuilder);
	}
	
}
