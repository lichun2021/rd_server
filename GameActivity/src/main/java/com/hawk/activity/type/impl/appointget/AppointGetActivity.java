package com.hawk.activity.type.impl.appointget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDaysActivityEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.appointget.cfg.AppointGetAchieveCfg;
import com.hawk.activity.type.impl.appointget.cfg.AppointGetBoxCfg;
import com.hawk.activity.type.impl.appointget.entity.AppointGetEntity;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.spread.cfg.SpreadExchangeCfg;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.PBAppointGetSync;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class AppointGetActivity extends ActivityBase implements AchieveProvider, IExchangeTip<SpreadExchangeCfg> {

	public AppointGetActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.APPOINT_GET;
	}

	public Action takeRewardAction() {
		return Action.APPOINT_GET;
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}

		if (!event.isCrossDay()) {
			return;
		}
		Optional<AppointGetEntity> opEntity = getPlayerDataEntity(playerId);
		AppointGetEntity entity = opEntity.get();

		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
			return;
		}

		List<AchieveItem> newItems = new ArrayList<AchieveItem>();
		for (AchieveItem item : entity.getItemList()) {
			AchieveConfig acfg = getAchieveCfg(item.getAchieveId());
			if (acfg instanceof AppointGetAchieveCfg && ((AppointGetAchieveCfg) acfg).getIsReset() == 1) {
				newItems.add(AchieveItem.valueOf(acfg.getAchieveId()));
			} else {
				newItems.add(item);
			}
		}

		entity.resetItemList(newItems);
		entity.notifyUpdate();

		// 推送给客户端
		AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
		ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, 1, this.providerActivityId()));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		AppointGetActivity activity = new AppointGetActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<AppointGetEntity> queryList = HawkDBManager.getInstance()
				.query("from AppointGetEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			AppointGetEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		AppointGetEntity entity = new AppointGetEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.SEND_FLOWER_INIT, () -> initAchieveInfo(playerId));
		}
	}

	/** 初始化成就信息
	 * 
	 * @param playerId */
	private void initAchieveInfo(String playerId) {
		Optional<AppointGetEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		AppointGetEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<AppointGetAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(AppointGetAchieveCfg.class);
		while (configIterator.hasNext()) {
			AppointGetAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		// 初始添加成就项
		ConfigIterator<AppointGetBoxCfg> boxconfigIterator = HawkConfigManager.getInstance().getConfigIterator(AppointGetBoxCfg.class);
		while (boxconfigIterator.hasNext()) {
			AppointGetBoxCfg next = boxconfigIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		entity.notifyUpdate();
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, 1, this.providerActivityId()));
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<AppointGetEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}

		int val1 = 0;
		int val2 = 0;
		int val3 = 0;
		AppointGetEntity playerDataEntity = opPlayerDataEntity.get();
		for (AchieveItem ait : playerDataEntity.getItemList()) {
			AchieveType atype = getAchieveCfg(ait.getAchieveId()).getAchieveType();
			switch (atype) {
			case Appoint_Get331001:
				val1 = Math.max(val1, ait.getValue(0));
				break;
			case Appoint_Get331002:
				val2 = Math.max(val2, ait.getValue(0));
				break;
			case Appoint_Get331003:
				val3 = Math.max(val3, ait.getValue(0));
				break;
			default:
				break;
			}
		}
		PBAppointGetSync.Builder resp = PBAppointGetSync.newBuilder();
		resp.setValue1(val1);
		resp.setValue2(val2);
		resp.setValue3(val3);
		resp.setTrainCnt(playerDataEntity.getTrainCnt());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.APPOINT_GET_SYNC, resp));
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
		Optional<AppointGetEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		AppointGetEntity playerDataEntity = opPlayerDataEntity.get();
		if (playerDataEntity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(AppointGetAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(AppointGetBoxCfg.class, achieveId);
		}
		return config;
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
}
