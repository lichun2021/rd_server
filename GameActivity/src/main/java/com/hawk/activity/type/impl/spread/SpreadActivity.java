package com.hawk.activity.type.impl.spread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BuildingLevelUpSpreadEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.VipLevelupEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.spread.cfg.SpreadExchangeCfg;
import com.hawk.activity.type.impl.spread.cfg.SpreadHiddenAchieveCfg;
import com.hawk.activity.type.impl.spread.cfg.SpreadKVCfg;
import com.hawk.activity.type.impl.spread.cfg.SpreadNewAchieveCfg;
import com.hawk.activity.type.impl.spread.cfg.SpreadOldAchieveCfg;
import com.hawk.activity.type.impl.spread.entity.SpreadEntity;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.HPSpreadFriendInfoResp;
import com.hawk.game.protocol.Activity.HPSpreadInfoSync;
import com.hawk.game.protocol.Activity.PBSpreadBindRoleInfo;
import com.hawk.game.protocol.Activity.PBSpreadFriendInfo;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class SpreadActivity extends ActivityBase implements AchieveProvider, IExchangeTip<SpreadExchangeCfg> {
	private static final Logger logger = LoggerFactory.getLogger("Server");

	public SpreadActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SPREAD_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<SpreadEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			String openId = this.getDataGeter().getOpenId(playerId);
			String serverId = this.getDataGeter().getServerId();
			if (!HawkOSOperator.isEmptyString(openId) && !HawkOSOperator.isEmptyString(serverId)) {
				// 设置玩家的推广码 和推广码绑定的 openid 和 serverid
				this.getDataGeter().setPlayerSpreadInfo(playerId, openId, serverId);
			}

			// 刷成就
			if (opDataEntity.get().getItemList().isEmpty()) {
				initAchieveInfo(playerId);
			}
			SpreadEntity entity = opDataEntity.get();

			if (!entity.getIsBindCode() || HawkOSOperator.isEmptyString(entity.getBindCode())) {
				return;
			}
			updateNewFriendInfo(playerId);
			int recharge = this.getDataGeter().getRechargeCount(playerId);
			this.getDataGeter().logPlayerSpreadLogin(playerId, openId, entity.getBindCode(), recharge, 0, 0);

		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, GameConst.MsgId.SPREAD_ACTIVITY_INIT, () -> {
				Optional<SpreadEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					logger.error("spreadactivity_log error, no entity created:" + playerId);
				}
				callBack(playerId, MsgId.SPREAD_ACTIVITY_INIT, () -> {
					initAchieveInfo(playerId);
					this.syncActivityInfo(playerId, opEntity.get());
				});
			});
		}
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<SpreadEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	public void onProtocolSendFriendInfo(String playerId) {
		try {
			// 获取好友的ActivityAccountInfo
			Optional<SpreadEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			HPSpreadFriendInfoResp.Builder builder = HPSpreadFriendInfoResp.newBuilder();
			builder.addAllFriends(getAllFriends(playerId));

			PlayerPushHelper.getInstance().pushToPlayer(playerId,
					HawkProtocol.valueOf(HP.code.HP_SPREAD_FRIEND_INFO_RESP_S, builder));
			SpreadEntity entity = opDataEntity.get();
			entity.setFriends(JsonFormat.printToString(builder.build()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private List<PBSpreadFriendInfo> getAllFriends(String playerId) {
		List<PBSpreadFriendInfo> list = new ArrayList<>();
		Map<byte[], byte[]> friends = ActivityGlobalRedis.getInstance().getRedisSession().hGetAllBytes(bindCodeKey(playerId).getBytes());
		for (byte[] bytes : friends.values()) {
			try {
				PBSpreadFriendInfo.Builder roleInfoBuilder = PBSpreadFriendInfo.newBuilder();
				roleInfoBuilder.mergeFrom(bytes);
				list.add(roleInfoBuilder.build());
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public void onProtocolPlayerBindCode(int protocolType, String playerId, String code) {
		try {
			List<AccountRoleInfo> accounts = getDataGeter().getPlayerAccountInfosNew(playerId);
			AccountRoleInfo firstRole = accounts.stream().sorted(Comparator.comparingLong(AccountRoleInfo::getRegisterTime))
					.findFirst().get();
			if (!Objects.equals(playerId, firstRole.getPlayerId())) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_NOT_FIRST_VALUE);
				return;
			}

			if (HawkOSOperator.isEmptyString(code) || code.length() > 20 || code.length() < 10) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_BIND_OPENID_VALUE);
				return;
			}
			if (playerId.equals(code)) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_BIND_SELF_VALUE);
				return;
			}
			Optional<SpreadEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			SpreadEntity entity = opDataEntity.get();

			if (entity.getIsBindCode()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_BINDED_VALUE);
				return;
			}

			// 满足绑定条件
			SpreadKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpreadKVCfg.class);
			if (null == cfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_DAILY_AWARDED_VALUE);
				return;
			}

			if (cfg.getBaseRequest() <= this.getDataGeter().getConstructionFactoryLevel(playerId)) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_BIND_LV_VALUE);
				return;
			}

			if (this.getDataGeter().getPlayerCreateTime(playerId) + cfg.getLogRequestMs() < HawkTime.getMillisecond()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_BIND_LOG_VALUE);
				return;
			}

			// 查询能否根据激活码索引到对方玩家的openid
			PBSpreadBindRoleInfo spreadRoleInfo = this.getDataGeter().getPlayerSpreadInfo(code);
			if (null == spreadRoleInfo || HawkOSOperator.isEmptyString(spreadRoleInfo.getServerId())
					|| HawkOSOperator.isEmptyString(spreadRoleInfo.getPlayerId())
					|| HawkOSOperator.isEmptyString(spreadRoleInfo.getOpenId())) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_BIND_OPENID_VALUE);
				return;
			}
			// 查询我的openid
			String openId = this.getDataGeter().getOpenId(playerId);

			if (openId.equals(spreadRoleInfo.getOpenId())) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_BIND_SELF_VALUE);
				return;
			}

			// 查询该玩家账号有没有在其他服务器绑定
			// 如果被绑定过
			if (this.getDataGeter().getIsSpreadOpenidBindFlag(openId)) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_BINDED_VALUE);
				return;
			}

			entity.setIsBindCode(1);
			// 这里的数据已经经过了调换
			entity.setBindCode(code);

			// 抛个事件，vip升级 登录抛事件 一直登录一直抛事件
			int vipLevel = this.getDataGeter().getVipLevel(playerId);
			VipLevelupEvent vipLevelupEvent = new VipLevelupEvent(playerId, 0, vipLevel);
			ActivityManager.getInstance().postEvent(vipLevelupEvent);
			// 抛个事件,建筑等级提升 登录抛事件 一直登录一直抛事件
			int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
			BuildingLevelUpSpreadEvent cityLevelEvent = new BuildingLevelUpSpreadEvent(playerId,
					Const.BuildingType.CONSTRUCTION_FACTORY_VALUE, cityLevel);
			ActivityManager.getInstance().postEvent(cityLevelEvent);

			updateNewFriendInfo(playerId);

			// 发奖励
			this.getDataGeter().takeReward(playerId, cfg.getNewBandAwardItems(), 1, Action.SPREAD_ACTIVITY_NEW_BAND, true,
					RewardOrginType.SPREAD_ACTIVITY);
			// 打操作日志
			logger.info("speadactivity_log bindsucc,player=%s,code=%s,serverId=%s", playerId, code,
					spreadRoleInfo.getServerId());

			this.syncActivityInfo(playerId, entity);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	public void onProtocolPlayerExchange(int protocolType, String playerId, int cfgId, int count) {
		try {
			if (0 == count) {
				count = 1;
			}
			Optional<SpreadEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			SpreadEntity entity = opDataEntity.get();

			SpreadExchangeCfg exchangeCfg = HawkConfigManager.getInstance().getConfigByKey(SpreadExchangeCfg.class,
					cfgId);
			if (null == exchangeCfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_EXCHANGE_CFG_VALUE);
				return;
			}
			SpreadKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpreadKVCfg.class);
			if (null == cfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_DAILY_AWARDED_VALUE);
				return;
			}

			int boughtCount = entity.getShopItemVal(cfgId);
			if (boughtCount + count > exchangeCfg.getTimes()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_EXCHANGE_TIMES_VALUE);
				return;
			}

			boolean flag = this.getDataGeter().cost(playerId, exchangeCfg.getNeedItemList(), count,
					Action.SPREAD_ACTIVITY_SHOP_COST, false);
			if (!flag) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_EXCHANGE_ITEMS_VALUE);
				return;
			}

			// 发奖
			this.getDataGeter().takeReward(playerId, exchangeCfg.getGainItemList(), count,
					Action.SPREAD_ACTIVITY_SHOP_GAIN, true, RewardOrginType.SPREAD_ACTIVITY);

			entity.addShopItems(cfgId, count);

			entity.notifyUpdate();
			this.syncActivityInfo(playerId, entity);
			PlayerPushHelper.getInstance().responseSuccess(playerId, protocolType);

			// 记录日志
			String openId = this.getDataGeter().getOpenId(playerId);
			int recharge = this.getDataGeter().getRechargeCount(playerId);
			this.getDataGeter().logPlayerSpreadLogin(playerId, openId, entity.getBindCode(), recharge, cfgId, count);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	public void onProtocolPlayerRewardDaily(int protocolType, String playerId) {
		try {
			Optional<SpreadEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			SpreadEntity entity = opDataEntity.get();
			if (entity.getDayReward() != 0) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_DAILY_AWARDED_VALUE);
				return;
			}

			SpreadKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpreadKVCfg.class);
			if (null == cfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_DAILY_AWARDED_VALUE);
				return;
			}

			entity.setDayReward(1);
			entity.notifyUpdate();
			// 发奖励
			this.getDataGeter().takeReward(playerId, cfg.getGiftItems(), 1, Action.SPREAD_ACTIVITY_DAILY, true,
					RewardOrginType.SPREAD_ACTIVITY);
			logger.info("spread_activity daily sign playerId:{}, termId:{}, num:{}", playerId, entity.getTermId(), 1);
			PlayerPushHelper.getInstance().responseSuccess(playerId, protocolType);
			syncActivityInfo(playerId, entity);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public void onProtocolPlayerRewardAchieve(int protocolType, String playerId, int cfgId) {
		try {
			Optional<SpreadEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			SpreadEntity entity = opDataEntity.get();

			SpreadOldAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SpreadOldAchieveCfg.class, cfgId);
			if (null == cfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_ACHIEVE_CFG_VALUE);
				return;
			}

			int canRewardTimes = getAchieveRewardTimes(entity, getAllFriends(entity.getPlayerId())).getOrDefault(cfgId, 0);
			if (canRewardTimes == 0) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SPREAD_ACTIVITY_AWARD_TIMES_VALUE);
				return;
			}
			// 扣次数,给奖励
			entity.addAchieveRewardedTimes(cfgId, 1);
			this.getDataGeter().takeReward(playerId, cfg.getRewardList(), 1, Action.SPREAD_ACTIVITY_OLD_ACHIEVE, true,
					RewardOrginType.SPREAD_ACTIVITY);

			entity.notifyUpdate();

			this.syncActivityInfo(playerId, entity);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	// 同步消息给玩家
	public void syncActivityInfo(String playerId, SpreadEntity entity) {
		if (!isOpening(playerId)) {
			return;
		}

		HPSpreadInfoSync.Builder builder = HPSpreadInfoSync.newBuilder();
		builder.setDayReward(entity.getDayReward());
		builder.setBindCode(entity.getIsBindCode() ? 1 : 0);
		for (Map.Entry<Integer, Integer> entry : entity.getShopItems().entrySet()) {
			KeyValuePairInt.Builder newBuidler = KeyValuePairInt.newBuilder();
			newBuidler.setKey(entry.getKey());
			newBuidler.setVal(entry.getValue());
			builder.addExchangeInfos(newBuidler);
		}

		List<PBSpreadFriendInfo> friends = getAllFriends(entity.getPlayerId());
		for (Map.Entry<Integer, Integer> entry : getAchieveRewardTimes(entity, friends).entrySet()) {
			KeyValuePairInt.Builder newBuidler = KeyValuePairInt.newBuilder();
			newBuidler.setKey(entry.getKey());
			newBuidler.setVal(entry.getValue());
			builder.addCanRewardTimes(newBuidler);
		}

		for (Map.Entry<Integer, Integer> entry : entity.getAchieveRewardedTimes().entrySet()) {
			KeyValuePairInt.Builder newBuidler = KeyValuePairInt.newBuilder();
			newBuidler.setKey(entry.getKey());
			newBuidler.setVal(entry.getValue());
			builder.addRewardedTimes(newBuidler);
		}
		builder.setFriendCnt(friends.size());
		builder.addAllTip(getTips(SpreadExchangeCfg.class, entity.getTipSet()));
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.HP_SPREAD_INFO_SYNC_S, builder));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SpreadActivity activity = new SpreadActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SpreadEntity> queryList = HawkDBManager.getInstance()
				.query("from SpreadEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SpreadEntity entity = queryList.get(0);

			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SpreadEntity entity = new SpreadEntity(playerId, termId);
		// 初始化玩家的 playerId:openId

		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		if (event.isCrossDay()) {
			Optional<SpreadEntity> opEntity = this.getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			SpreadEntity entity = opEntity.get();
			entity.setDayReward(0);
			entity.notifyUpdate();
			this.syncActivityDataInfo(playerId);
		}
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
		Optional<SpreadEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		SpreadEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public Action takeRewardAction() {
		return Action.SPREAD_ACTIVITY_ACHIEVE;
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg = HawkConfigManager.getInstance().getConfigByKey(SpreadOldAchieveCfg.class, achieveId);
		if (null == cfg) {
			cfg = HawkConfigManager.getInstance().getConfigByKey(SpreadNewAchieveCfg.class, achieveId);
		}
		if (null == cfg) {
			return HawkConfigManager.getInstance().getConfigByKey(SpreadHiddenAchieveCfg.class, achieveId);
		}
		return cfg;
	}

	/**
	 * 初始化成就信息
	 * 
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<SpreadEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SpreadEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始化完成成就的成就
		ConfigIterator<SpreadOldAchieveCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(SpreadOldAchieveCfg.class);
		while (configIterator.hasNext()) {
			SpreadOldAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}

		// 初始化隐藏成就
		ConfigIterator<SpreadHiddenAchieveCfg> hiddenConfigIterator = HawkConfigManager.getInstance()
				.getConfigIterator(SpreadHiddenAchieveCfg.class);
		while (hiddenConfigIterator.hasNext()) {
			SpreadHiddenAchieveCfg next = hiddenConfigIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}

		// 初始 新兵成就
		ConfigIterator<SpreadNewAchieveCfg> newConfigIterator = HawkConfigManager.getInstance()
				.getConfigIterator(SpreadNewAchieveCfg.class);
		while (newConfigIterator.hasNext()) {
			SpreadNewAchieveCfg next = newConfigIterator.next();
			// 如果没有找到相同的成就
			if (!entity.getItemList().stream().anyMatch(e -> e.getAchieveId() == next.getAchieveId())) {
				AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
				entity.addItem(item);
			}
		}
		entity.notifyUpdate();
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);

		logger.debug("spread_log refresh achieveitems ok");
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		Result<?> res = AchieveProvider.super.onTakeReward(playerId, achieveId);
		AchieveConfig achieveCfg = getAchieveCfg(achieveId);
		if (null != achieveCfg && (achieveCfg instanceof SpreadOldAchieveCfg)) {
			// 此种成就完成之后设置成 未完成 继续走update
			Optional<SpreadEntity> opEntity = getPlayerDataEntity(playerId);
			if (opEntity.isPresent()) {
				SpreadEntity entity = opEntity.get();
				List<AchieveItem> achieveItems = entity.getItemList().stream()
						.filter(e -> e.getAchieveId() == achieveId).collect(Collectors.toList());
				if (!achieveItems.isEmpty()) {
					AchieveItem achieveItem = achieveItems.get(0);
					int val = achieveItem.getValue(0);
					achieveItem.setValue(0, val > 0 ? val - 1 : 0);
					achieveItem.setState(AchieveState.NOT_ACHIEVE_VALUE);
					entity.notifyUpdate();
					ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()),
							true);
				}
			}
		}
		return res;
	}

	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		Result<?> res = AchieveProvider.super.onAchieveFinished(playerId, achieveItem);
		updateNewFriendInfo(playerId);

		return res;
	}

	private void updateNewFriendInfo(String playerId) {
		Optional<SpreadEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SpreadEntity entity = opEntity.get();
		if (!entity.getIsBindCode() || HawkOSOperator.isEmptyString(entity.getBindCode())) {
			return;
		}

		PBSpreadFriendInfo.Builder roleInfoBuilder = PBSpreadFriendInfo.newBuilder();
		roleInfoBuilder.setPlayerId(playerId).setPlayerName(getDataGeter().getPlayerName(playerId))
				.setPfIcon(getDataGeter().getPfIcon(playerId)).setIcon(getDataGeter().getIcon(playerId)).setViplevel(getDataGeter().getVipLevel(playerId))
				.setCityLevel(getDataGeter().getConstructionFactoryLevel(playerId)).setServerId(getDataGeter().getServerId());

		for (AchieveItem achieveItem : entity.getItemList()) {
			if (achieveItem.getState() == AchieveState.NOT_ACHIEVE_VALUE) {
				continue;
			}
			AchieveConfig achieveCfg = getAchieveCfg(achieveItem.getAchieveId());
			if (achieveCfg instanceof SpreadHiddenAchieveCfg) {
				roleInfoBuilder.addFinishOldAchieve(((SpreadHiddenAchieveCfg) achieveCfg).getFinishAchieveId());
			}
		}

		ActivityGlobalRedis.getInstance().getRedisSession().hSetBytes(bindCodeKey(entity.getBindCode()), playerId, roleInfoBuilder.build().toByteArray());
	}

	private String bindCodeKey(String bindCode) {
		return "SPREAD_ACTIVITY:" + getActivityTermId() + ":" + bindCode;
	}

	/**剩余可以领取的次数*/
	public Map<Integer, Integer> getAchieveRewardTimes(SpreadEntity entity, List<PBSpreadFriendInfo> friends) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (PBSpreadFriendInfo info : friends) {
			for (Integer aid : info.getFinishOldAchieveList()) {
				int val = map.getOrDefault(aid, 0);
				map.put(aid, (val + 1));
			}
		}

		for (Entry<Integer, Integer> ent : map.entrySet()) {
			int aid = ent.getKey();
			int val = ent.getValue();
			SpreadOldAchieveCfg oldCfg = HawkConfigManager.getInstance().getConfigByKey(SpreadOldAchieveCfg.class, aid);
			val = val / oldCfg.getGroup();
			val = Math.min(oldCfg.getMaxTimes(), val);
			ent.setValue(val);
		}

		for (Integer key : map.keySet()) {
			map.merge(key, entity.getAchieveRewardedTimeById(key), (v1, v2) -> v1 - v2);
		}

		return map;
	}
}
