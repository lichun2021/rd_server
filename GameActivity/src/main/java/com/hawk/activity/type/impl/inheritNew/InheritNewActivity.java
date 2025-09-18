package com.hawk.activity.type.impl.inheritNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;

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
import com.hawk.activity.type.impl.inheritNew.cfg.InheritNewAchieveCfg;
import com.hawk.activity.type.impl.inheritNew.cfg.InheritNewKVCfg;
import com.hawk.activity.type.impl.inheritNew.entity.InheritNewEntity;
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
import com.hawk.log.LogConst.LogInfoType;

public class InheritNewActivity extends ActivityBase implements AchieveProvider {
	

	public InheritNewActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.INHERITE_NEW;
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
		InheritNewActivity activity = new InheritNewActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<InheritNewEntity> queryList = HawkDBManager.getInstance()
				.query("from InheritNewEntity where playerId = ? and termId = ? and invalid = 0 order by createTime desc limit 1", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			InheritNewEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		InheritNewEntity entity = new InheritNewEntity(playerId, termId);
		BackNewPlayerInfo info = getDataGeter().getBackPlayerInfoByIdNew(playerId);
		if(playerId.equals(info.getCurNewPlayer())){
			entity.setState(InheritStatus.NEED_ACTIVE_VALUE);
		} else {
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
		Optional<InheritNewEntity> opEntity = getPlayerDataEntity(playerId);
		BackNewPlayerInfo info = getDataGeter().getBackPlayerInfoByIdNew(playerId);
		// 不是回归新建角色,不用初始化成就信息
		if(info == null || !info.getCurNewPlayer().equals(playerId)){
			HawkLog.errPrintln("InheritNewActivity initAchieveInfo failed, playerId: {}, BackNewPlayerInfo null: {}", playerId, info == null);
			return;
		}
		
		if(!opEntity.isPresent()){
			return;
		}
		
		InheritNewEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		boolean isProprietary = isProprietary();
		// 初始添加成就项
		ConfigIterator<InheritNewAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(InheritNewAchieveCfg.class);
		while (configIterator.hasNext()) {
			InheritNewAchieveCfg next = configIterator.next();
			if(isProprietary){
				if(next.isService()){
					AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
					entity.addItem(item);
				}
			}else {
				if(!next.isService()){
					AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
					entity.addItem(item);
				}
			}
		}
		
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		syncPageInfo(playerId);
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
		Optional<InheritNewEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		InheritNewEntity playerDataEntity = opPlayerDataEntity.get();
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(InheritNewAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(InheritNewAchieveCfg.class, achieveId);
		}
		return config;
	}
	
	@Override
	public List<Builder> getRewardList(String playerId, AchieveConfig achieveConfig) {
		List<Builder> rewardList = achieveConfig.getRewardList();
		Optional<InheritNewEntity> opEntity = getPlayerDataEntity(playerId);
		InheritNewEntity entity = opEntity.get();
		//ConfigIterator<InheritNewAchieveCfg> its = HawkConfigManager.getInstance().getConfigIterator(InheritNewAchieveCfg.class);
		int shareCnt = Math.max(entity.getItemList().size(), 1);
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
		
		Optional<InheritNewEntity> opEntity = getPlayerDataEntity(playerId);
		InheritNewEntity entity = opEntity.get();
		int oldValue = entity.getLoginDays();
		//首次，上报军魂传承活动触发打点
		if (entity.getLoginDays() == 1 && (entity.getState() == InheritStatus.NEED_ACTIVE_VALUE || entity.getState() == InheritStatus.ACTIVED_VALUE)) {
			boolean succ = ActivityLocalRedis.getInstance().getRedisSession().setNx(getRedisKey(playerId), "1");
			if (succ) {
				InheritNewKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InheritNewKVCfg.class);
				ActivityLocalRedis.getInstance().getRedisSession().expire(getRedisKey(playerId), (int) (cfg.getLastTime()/1000));
				Map<String, Object> param = new HashMap<>();
		        param.put("startTime", HawkTime.formatTime(entity.getCreateTime()));
		        getDataGeter().logActivityCommon(playerId, LogInfoType.inherit_new_start, param);
			}
		}
		
		if (event.isCrossDay() && !(HawkTime.isSameDay(HawkTime.getMillisecond(), entity.getCreateTime()) || entity.getCreateTime() == 0)) {
			entity.setLoginDays(entity.getLoginDays() + 1);
		}
		
		// 当前为已激活状态,则推送累登活动事件
		if (entity.getState() == InheritStatus.ACTIVED_VALUE) {
			initAchieveInfo(playerId);
			if (entity.getLoginDays() != oldValue) {
				ActivityManager.getInstance().postEvent(new LoginDayInheritEvent(playerId, entity.getLoginDays()), true);
			}
		}
	}
	
	/**
	 * 获取redis信息存储key
	 * @param playerId
	 * @return
	 */
	private String getRedisKey(String playerId) {
		String key = String.format(ActivityRedisKey.INHERIT_NEW_START, playerId);
		return key;
	}

	public void syncPageInfo(String playerId) {
		Optional<InheritNewEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}

		Optional<InheritNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		BackNewPlayerInfo info = getDataGeter().getBackPlayerInfoByIdNew(playerId);
		if (info == null) {
			HawkLog.logPrintln("InheritNewActivity syncPageInfo break, BackNewPlayerInfo null, playerId: {}", playerId);
			return;
		}
		InheritNewEntity entity = opEntity.get();
		if (entity.getState() != InheritStatus.OLD_SERVER_VALUE && (playerId.equals(info.getCurOldPlayerId()) || playerId.equals(info.getCurrInheritPlayerId()))) {
			entity.setState(InheritStatus.OLD_SERVER_VALUE);
			entity.setLoginDays(1);
			entity.setItemList(new ArrayList<>());
		}

		InheritPageInfo.Builder builder = InheritPageInfo.newBuilder();
		InheritStatus status = InheritStatus.valueOf(entity.getState());
		builder.setStatus(status);
		switch (status) {
		case NEED_ACTIVE:
			// 找到合适的继承帐号
			AccountRoleInfo roleInfo = getDataGeter().getSuitInheritAccountNew(playerId);
			if (roleInfo != null) {
				PlayerRoleInfo.Builder infoBuilder = this.toBuilder(roleInfo);
				builder.setRoleInfo(infoBuilder);
			}
			break;
		case ACTIVED:
			//ConfigIterator<InheritNewAchieveCfg> its = HawkConfigManager.getInstance().getConfigIterator(InheritNewAchieveCfg.class);
			int shareCnt = Math.max(entity.getItemList().size(), 1);
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
		if(isProprietary()){
			builder.setIsProprietary(true);
		}else {
			builder.setIsProprietary(false);
		}
		
		List<AccountRoleInfo> inheritedInfo = new ArrayList<>();
		List<HawkTuple2<AccountRoleInfo, Integer>> notInheritedInfo = new ArrayList<>();
		getDataGeter().inheritDataCollect(playerId, inheritedInfo, notInheritedInfo);
		for (AccountRoleInfo roleInfo : inheritedInfo) {
			PlayerRoleInfo.Builder infoBuilder = this.toBuilder(roleInfo);
			if (status == InheritStatus.ACTIVED && roleInfo.getPlayerId().equals(entity.getSourcePlayerId())) {
				infoBuilder.setCurrentInherited(1);
			}
			builder.addInheritedRole(infoBuilder);
		}
		
		InheritNewKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InheritNewKVCfg.class);
		long goldRate = cfg.getGoldRate();
		int maxGift = cfg.getMaxGift();
		if(isProprietary()){
			goldRate = cfg.getSpecialGoldRate();
			maxGift = cfg.getSpecialMaxGift();
		}
		for (HawkTuple2<AccountRoleInfo, Integer> tuple : notInheritedInfo) {
			AccountRoleInfo roleInfo = tuple.first;
			// 充值金条数
			int diamonds = tuple.second;
			// 传承金条数量
			int rebetDiamonds = (int) (diamonds * goldRate / ActivityConst.MYRIABIT_BASE);
			rebetDiamonds = Math.min(maxGift, rebetDiamonds);
			int sumVipExp = Math.min(getDataGeter().getVipMaxExp(), diamonds);
			int rebetExp = (int) (sumVipExp * goldRate / ActivityConst.MYRIABIT_BASE);
			PlayerRoleInfo.Builder infoBuilder = this.toBuilder(roleInfo);
			infoBuilder.setVipLevel(roleInfo.getVipLevel());
			infoBuilder.setRechargeDiamonds(diamonds);
			infoBuilder.setRebetDiamonds(rebetDiamonds);
			infoBuilder.setRebetExp(rebetExp);
			builder.addNotInheritedRole(infoBuilder);
		}
		
		pushToPlayer(playerId, HP.code.GET_INHERIT_NEW_PAGE_INFO_S_VALUE, builder);
	}
	
	/**
	 * PlayerRoleInfo.Builder构建
	 * @param roleInfo
	 * @return
	 */
	private PlayerRoleInfo.Builder toBuilder(AccountRoleInfo roleInfo) {
		PlayerRoleInfo.Builder infoBuilder = PlayerRoleInfo.newBuilder();
		infoBuilder.setPlayerId(roleInfo.getPlayerId());
		infoBuilder.setOpenid(roleInfo.getOpenId());
		infoBuilder.setPlayerName(roleInfo.getPlayerName());
		infoBuilder.setIcon(roleInfo.getIcon());
		String pfIcon = roleInfo.getPfIcon();
		if (!HawkOSOperator.isEmptyString(pfIcon)) {
			infoBuilder.setPfIcon(pfIcon);
		}else{
			infoBuilder.setPfIcon("");
		}
		infoBuilder.setServerId(roleInfo.getServerId());
		return infoBuilder;
	}

	/**
	 * 激活传承
	 * @param playerId
	 */
	public Result<?> onActiveInherit(String playerId) {
		Optional<InheritNewEntity> opEntity = getPlayerDataEntity(playerId);
		InheritNewEntity entity = opEntity.get();
		// 已进行过承接
		if (entity.getState() != InheritStatus.NEED_ACTIVE_VALUE) {
			return Result.fail(Status.Error.ACTIVITY_ALREADY_INHERITED_VALUE);
		}
		BackNewPlayerInfo backInfo = getDataGeter().getBackPlayerInfoByIdNew(playerId);
		// 不是回归玩家,不能承接
		if (backInfo == null) {
			return Result.fail(Status.Error.ACTIVITY_NOT_BACK_PLAYER_VALUE);
		}

		AccountRoleInfo roleInfo = getDataGeter().getSuitInheritAccountNew(playerId);
		// 没有满足承接条件的帐号
		if (roleInfo == null) {
			return Result.fail(Status.Error.ACTIVITY_NO_ROLE_CAN_INHERIT_VALUE);
		}
		InheritNewKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InheritNewKVCfg.class);
		int sumGold = getDataGeter().getAccountRechargeNumAndExpNew(roleInfo);
		long goldRate = cfg.getGoldRate();
		int maxGift = cfg.getMaxGift();
		if(isProprietary()){
			goldRate = cfg.getSpecialGoldRate();
			maxGift = cfg.getSpecialMaxGift();
		}
		HawkLog.logPrintln("InheritNewActivity inherit rate, playerId:{}, maxGift:{}, goldRate:{}",playerId, maxGift, goldRate);
		// 返利金币额度
		int rebetGold = (int) (sumGold * goldRate / ActivityConst.MYRIABIT_BASE);
		rebetGold = Math.min(maxGift, rebetGold);
		int sumVipExp = Math.min(getDataGeter().getVipMaxExp(), sumGold);
		int rebetExp = (int) (sumVipExp * goldRate / ActivityConst.MYRIABIT_BASE);
		entity.setTotalGold(rebetGold);
		entity.setTotalVipExp(rebetExp);
		entity.setState(InheritStatus.ACTIVED_VALUE);
		entity.setSourcePlayerId(roleInfo.getPlayerId());
		backInfo.setInheritTime(HawkTime.getMillisecond());
		backInfo.setCurrInheritPlayerId(roleInfo.getPlayerId());
		backInfo.setCurrInheritServer(roleInfo.getServerId());
		// 更新回归信息
		getDataGeter().updateBackPlayerInfoNew(playerId, backInfo);
		// 记录被承接角色信息
		getDataGeter().addInheritedInfo(roleInfo, playerId, rebetGold);
		
		initAchieveInfo(playerId);
		int logindays = Math.max(entity.getLoginDays(), 1);
		ActivityManager.getInstance().postEvent(new LoginDayInheritEvent(playerId, logindays), true);
		syncPageInfo(playerId);
		HawkLog.logPrintln("InheritNewActivity inherit success, playerId: {}, rolePlayerId: {}, roleOpenid: {}, roleName: {}, roleServerId: {}, rolePlatform: {}, sumGold: {}, sumExp: {}",
				playerId, roleInfo.getPlayerId(), roleInfo.getOpenId(), roleInfo.getPlayerName(), roleInfo.getServerId(), roleInfo.getPlatform(), sumGold, sumVipExp);
		
		getDataGeter().logAccountInherit(playerId, entity.getTermId(), roleInfo.getPlayerId(), roleInfo.getServerId(), sumGold, rebetGold, sumVipExp, rebetExp);
		return Result.success();
	}
	
	private boolean isProprietary(){
		try {
			String proprietaryServer = ActivityGlobalRedis.getInstance().hget("PROPRIETARY_SERVER", getDataGeter().getServerId());
			if(HawkOSOperator.isEmptyString(proprietaryServer) || !"1".equals(proprietaryServer)){
				return false;
			}
			return true;
		}catch (Exception e){
			HawkException.catchException(e);
			return false;
		}
	}
}
