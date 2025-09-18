package com.hawk.activity.type.impl.inherit;

import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayInheritEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.inherit.cfg.InheritAchieveCfg;
import com.hawk.activity.type.impl.inherit.cfg.InheritKVCfg;
import com.hawk.activity.type.impl.inherit.entity.InheritEntity;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.protocol.Activity.InheritPageInfo;
import com.hawk.game.protocol.Activity.InheritStatus;
import com.hawk.game.protocol.Activity.PlayerRoleInfo;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

public class InheritActivity extends ActivityBase implements AchieveProvider {
	

	public InheritActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.INHERITE;
	}
	
	public Action takeRewardAction() {
		return Action.INHERIT_ACHIEVE_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		InheritActivity activity = new InheritActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<InheritEntity> queryList = HawkDBManager.getInstance()
				.query("from InheritEntity where playerId = ? and termId = ? and invalid = 0 order by createTime desc limit 1", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			InheritEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		InheritEntity entity = new InheritEntity(playerId, termId);
		BackPlayerInfo info = getDataGeter().getBackPlayerInfoById(playerId);
		if(playerId.equals(info.getCurNewPlayer())){
			entity.setState(InheritStatus.NEED_ACTIVE_VALUE);
		}
		else{
			entity.setState(InheritStatus.OLD_SERVER_VALUE);
		}
		return entity;
	}
	

	@Override
	public void onOpenForPlayer(String playerId) {
		syncActivityDataInfo(playerId);
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<InheritEntity> opEntity = getPlayerDataEntity(playerId);
		BackPlayerInfo info = getDataGeter().getBackPlayerInfoById(playerId);
		// 不是回归新建角色,不用初始化成就信息
		if(!info.getCurNewPlayer().equals(playerId)){
			return;
		}
		
		if(!opEntity.isPresent()){
			return;
		}
		
		InheritEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		// 初始添加成就项
		ConfigIterator<InheritAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(InheritAchieveCfg.class);
		while (configIterator.hasNext()) {
			InheritAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		//syncPageInfo(playerId);
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
		Optional<InheritEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		InheritEntity playerDataEntity = opPlayerDataEntity.get();
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(InheritAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(InheritAchieveCfg.class, achieveId);
		}
		return config;
	}
	
	
	
	@Override
	public List<Builder> getRewardList(String playerId, AchieveConfig achieveConfig) {
		List<Builder> rewardList = achieveConfig.getRewardList();
		Optional<InheritEntity> opEntity = getPlayerDataEntity(playerId);
		InheritEntity entity = opEntity.get();
		ConfigIterator<InheritAchieveCfg> its = HawkConfigManager.getInstance().getConfigIterator(InheritAchieveCfg.class);
		int shareCnt = Math.max(its.size(), 1);
		int gold = Math.max((int) Math.ceil(entity.getTotalGold() * 1d / shareCnt), 1);
		int exp = Math.max((int) Math.ceil(entity.getTotalVipExp() * 1d / shareCnt), 1);
		
		RewardItem.Builder goldItem = RewardItem.newBuilder();
		goldItem.setItemType(ItemType.PLAYER_ATTR_VALUE * GameConst.ITEM_TYPE_BASE);
		goldItem.setItemId(PlayerAttr.DIAMOND_VALUE);
		goldItem.setItemCount(gold);
		rewardList.add(goldItem);
		RewardItem.Builder expItem = RewardItem.newBuilder();
		expItem.setItemType(ItemType.PLAYER_ATTR_VALUE * GameConst.ITEM_TYPE_BASE);
		expItem.setItemId(PlayerAttr.VIP_POINT_VALUE);
		expItem.setItemCount(exp);
		rewardList.add(expItem);
		return rewardList;
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<InheritEntity> opEntity = getPlayerDataEntity(playerId);
		InheritEntity entity = opEntity.get();
		if (event.isCrossDay() && !(HawkTime.isSameDay(HawkTime.getMillisecond(), entity.getCreateTime()) || entity.getCreateTime() == 0)) {
			entity.setLoginDays(entity.getLoginDays() + 1);
		}
		// 当前为已激活状态,则推送累登活动事件
		if (entity.getState() == InheritStatus.ACTIVED_VALUE) {
			ActivityManager.getInstance().postEvent(new LoginDayInheritEvent(playerId, entity.getLoginDays()), true);
		}
		
	}

	public void syncPageInfo(String playerId) {
		Optional<InheritEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return;
		}
		InheritEntity entity = opPlayerDataEntity.get();
		InheritPageInfo.Builder builder = InheritPageInfo.newBuilder();
		InheritStatus status = InheritStatus.valueOf(entity.getState());
		builder.setStatus(status);
		switch (status) {
		case NEED_ACTIVE:
			// 找到合适的继承帐号
			AccountRoleInfo roleInfo = getDataGeter().getSuitInheritAccount(playerId);
			if (roleInfo != null) {
				PlayerRoleInfo.Builder infoBuilder = PlayerRoleInfo.newBuilder();
				infoBuilder.setPlayerId(roleInfo.getPlayerId());
				infoBuilder.setOpenid(roleInfo.getOpenId());
				infoBuilder.setPlayerName(roleInfo.getPlayerName());
				infoBuilder.setIcon(roleInfo.getIcon());
				String pfIcon = roleInfo.getPfIcon();
				if (!HawkOSOperator.isEmptyString(pfIcon)) {
					infoBuilder.setPfIcon(pfIcon);
				}
				infoBuilder.setServerId(roleInfo.getServerId());
				builder.setRoleInfo(infoBuilder);
			}
			break;
		case ACTIVED:
			ConfigIterator<InheritAchieveCfg> its = HawkConfigManager.getInstance().getConfigIterator(InheritAchieveCfg.class);
			int shareCnt = Math.max(its.size(), 1);
			int gold = Math.max((int) Math.ceil(entity.getTotalGold() * 1d / shareCnt), 1);
			int exp = Math.max((int) Math.ceil(entity.getTotalVipExp() * 1d / shareCnt), 1);
			
			// 金币奖励
			RewardItem.Builder goldItem = RewardItem.newBuilder();
			goldItem.setItemType(ItemType.PLAYER_ATTR_VALUE * GameConst.ITEM_TYPE_BASE);
			goldItem.setItemId(PlayerAttr.DIAMOND_VALUE);
			goldItem.setItemCount(gold);
			builder.addRewards(goldItem);
			
			// 经验奖励
			RewardItem.Builder expItem = RewardItem.newBuilder();
			expItem.setItemType(ItemType.PLAYER_ATTR_VALUE * GameConst.ITEM_TYPE_BASE);
			expItem.setItemId(PlayerAttr.VIP_POINT_VALUE);
			expItem.setItemCount(exp);
			builder.addRewards(expItem);
			break;
			
		default:
			break;
		}
		
		pushToPlayer(playerId, HP.code.GET_INHERIT_PAGE_INFO_S_VALUE, builder);
	}
	

	/**
	 * 激活传承
	 * @param playerId
	 */
	public Result<?> onActiveInherit(String playerId) {
		Optional<InheritEntity> opEntity = getPlayerDataEntity(playerId);
		InheritEntity entity = opEntity.get();
		// 已进行过承接
		if (entity.getState() != InheritStatus.NEED_ACTIVE_VALUE) {
			return Result.fail(Status.Error.ACTIVITY_ALREADY_INHERITED_VALUE);
		}
		BackPlayerInfo backInfo = getDataGeter().getBackPlayerInfoById(playerId);
		// 不是回归玩家,不能承接
		if (backInfo == null) {
			return Result.fail(Status.Error.ACTIVITY_NOT_BACK_PLAYER_VALUE);
		}

		AccountRoleInfo roleInfo = getDataGeter().getSuitInheritAccount(playerId);
		// 没有满足承接条件的帐号
		if (roleInfo == null) {
			return Result.fail(Status.Error.ACTIVITY_NO_ROLE_CAN_INHERIT_VALUE);
		}
		InheritKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InheritKVCfg.class);
		int sumGold = getDataGeter().getAccountRechargeNumAndExp(roleInfo);
		// 返利金币额度
		int rebetGold = (int) (sumGold * cfg.getGoldRate() / ActivityConst.MYRIABIT_BASE);
		rebetGold = Math.min(cfg.getMaxGift(), rebetGold);
		int sumVipExp = Math.min(getDataGeter().getVipMaxExp(), sumGold);
		int rebetExp = (int) (sumVipExp * cfg.getGoldRate() / ActivityConst.MYRIABIT_BASE);
		entity.setTotalGold(rebetGold);
		entity.setTotalVipExp(rebetExp);
		entity.setState(InheritStatus.ACTIVED_VALUE);
		backInfo.setInheritTime(HawkTime.getMillisecond());
		backInfo.setCurrInheritPlayerId(roleInfo.getPlayerId());
		backInfo.setCurrInheritServer(roleInfo.getServerId());
		// 更新回归信息
		getDataGeter().updateBackPlayerInfo(backInfo);
		// 记录被承接角色信息
		getDataGeter().addInheritedInfo(roleInfo, playerId, rebetGold);

		initAchieveInfo(playerId);
		int logindays = Math.max(entity.getLoginDays(), 1);
		ActivityManager.getInstance().postEvent(new LoginDayInheritEvent(playerId, logindays), true);
		syncPageInfo(playerId);
		HawkLog.logPrintln(
				"InheritActivity inherit success, playerId: {}, rolePlayerId: {}, roleOpenid: {}, roleName: {}, roleServerId: {}, rolePlatform: {}, sumGold: {}, sumExp: {}",
				playerId, roleInfo.getPlayerId(), roleInfo.getOpenId(), roleInfo.getPlayerName(), roleInfo.getServerId(), roleInfo.getPlatform(), sumGold, sumVipExp);
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
