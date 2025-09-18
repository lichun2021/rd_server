package com.hawk.activity.type.impl.fullyArmed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayFullyArmedEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.fullyArmed.cfg.FullyArmedAchieveCfg;
import com.hawk.activity.type.impl.fullyArmed.cfg.FullyArmedPackageCfg;
import com.hawk.activity.type.impl.fullyArmed.cfg.FullyArmedTreasure;
import com.hawk.activity.type.impl.fullyArmed.entity.FullyArmedEntity;
import com.hawk.game.protocol.Activity.HPFullyArmedActivityInfoSync;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class FullyArmedActivity extends ActivityBase implements AchieveProvider {
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<FullyArmedEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	public FullyArmedActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.FULLY_ARMED_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}


	@Override
	public void onPlayerLogin(String playerId) {
		try {
			if (isOpening(playerId)) {
				Optional<FullyArmedEntity> opDataEntity = this.getPlayerDataEntity(playerId);
				if (!opDataEntity.isPresent()) {
					return;
				}
				ActivityManager.getInstance().postEvent(new LoginDayFullyArmedEvent(playerId, 1));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void initAchieveInfo(String playerId) {
		Optional<FullyArmedEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		FullyArmedEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始化完成成就的成就
		ConfigIterator<FullyArmedAchieveCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(FullyArmedAchieveCfg.class);
		while (configIterator.hasNext()) {
			FullyArmedAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		//跨天登录
		ActivityManager.getInstance().postEvent(new LoginDayFullyArmedEvent(playerId, 1));
	}

	private void syncActivityInfo(String playerId, FullyArmedEntity entity) {
		HPFullyArmedActivityInfoSync.Builder builder = HPFullyArmedActivityInfoSync.newBuilder();
		builder.setCurIndex(entity.getSearchId());
		for (Map.Entry<Integer, Integer> entry : entity.getBuyRecordMap().entrySet()) {
			KeyValuePairInt.Builder pair = KeyValuePairInt.newBuilder();
			pair.setKey(entry.getKey());
			pair.setVal(entry.getValue());
			builder.addGoods(pair);
		}

		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.HP_FULLY_ARMED_INFO_SYNC_S, builder));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		FullyArmedActivity activity = new FullyArmedActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<FullyArmedEntity> queryList = HawkDBManager.getInstance()
				.query("from FullyArmedEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			FullyArmedEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		FullyArmedEntity entity = new FullyArmedEntity(playerId, termId);
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
		if (event.isCrossDay()) {
			Optional<FullyArmedEntity> opEntity = this.getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}

			FullyArmedEntity entity = opEntity.get();

			// 刷新免费次数
			entity.setSearchId(0);
			entity.getBuyRecordMap().clear();

			List<AchieveItem> items = new ArrayList<>();
			ConfigIterator<FullyArmedAchieveCfg> achieveIterator = HawkConfigManager.getInstance()
					.getConfigIterator(FullyArmedAchieveCfg.class);
			while (achieveIterator.hasNext()) {
				FullyArmedAchieveCfg cfg = achieveIterator.next();
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				items.add(item);
			}
			entity.resetItemList(items);
			//跨天登录
			ActivityManager.getInstance().postEvent(new LoginDayFullyArmedEvent(playerId, 1));
			entity.notifyUpdate();
			this.syncActivityDataInfo(playerId);
			// 推送给客户端
			AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
		}
	}

	public Result<?> onProtocolActivityBuyReq(int protocolType, String playerId, int cfgId, int count) {
		try {
			if (count <= 0 || count > 2000) {
				return Result.fail(Status.Error.FULLY_ARMED_UNKNOW_ERR_VALUE);
			}
			Optional<FullyArmedEntity> opDataEntity = getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return Result.fail(Status.Error.FULLY_ARMED_NOT_OPEN_VALUE);
			}
			FullyArmedEntity entity = opDataEntity.get();

			FullyArmedPackageCfg packageCfg = HawkConfigManager.getInstance().getConfigByKey(FullyArmedPackageCfg.class,
					cfgId);

			if (null == packageCfg) {
				return Result.fail(Status.Error.FULLY_ARMED_ITEM_BUY_CFG_VALUE);
			}

			// 验证购买限制
			if (entity.getBuyTimes(cfgId) + count > packageCfg.getLimit()) {
				return Result.fail(Status.Error.FULLY_ARMED_ITEM_BUY_TIMES_VALUE);
			}

			// 判断道具足够否
			boolean flag = this.getDataGeter().cost(playerId, packageCfg.getPriceList(), count,
					Action.FULLY_ARMED_BUY_COST, false);
			if (!flag) {
				return Result.fail(Status.Error.FULLY_ARMED_SEARCH_ITEM_BUY_VALUE);
			}

			// 设置entity数据
			entity.addBuyTimes(cfgId, count);
			entity.notifyUpdate();
			// 发道具
			this.getDataGeter().sendAwardFromAwardCfg(packageCfg.getAwardId(), count, playerId, true,
					Action.FULLY_ARMED_BUY_GAIN, RewardOrginType.ACTIVITY_REWARD);
			// 发消息
			this.syncActivityInfo(playerId, entity);
			// 打点
			this.getDataGeter().logFullArmedBuy(playerId, cfgId, count);
		} catch (Exception e) {
			return Result.fail(Status.Error.FULLY_ARMED_UNKNOW_ERR_VALUE);
		}
		return Result.success();
	}

	public Result<?> onProtocolActivitySearchReq(int protocolType, String playerId) {
		try {
			Optional<FullyArmedEntity> opDataEntity = getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return Result.fail(Status.Error.FULLY_ARMED_NOT_OPEN_VALUE);
			}
			FullyArmedEntity entity = opDataEntity.get();
			// 是否已经探索完
			if (entity.getSearchId() >= FullyArmedTreasure.getMaxId()) {
				return Result.fail(Status.Error.FULLY_ARMED_SEARCH_ALL_VALUE);
			}
			// 配置表
			FullyArmedTreasure cfg = HawkConfigManager.getInstance().getConfigByKey(FullyArmedTreasure.class,
					entity.getSearchId() + 1);
			if (null == cfg) {
				return Result.fail(Status.Error.FULLY_ARMED_SEARCH_ALL_VALUE);
			}
			//扣除道具
			// 判断道具足够否
			boolean flag = this.getDataGeter().cost(playerId, cfg.getCostList(), 1,
					Action.FULLY_ARMED_BUY_COST, false);
			if(!flag){
				return Result.fail(Status.Error.FULLY_ARMED_SEARCH_ITEM_VALUE);
			}
			entity.setSearchId(entity.getSearchId() + 1);
			entity.notifyUpdate();
			// 发道具
			this.getDataGeter().sendAwardFromAwardCfg(cfg.getAwardId(), 1, playerId, true,
					Action.FULLY_ARMED_SEARCH_GAIN, RewardOrginType.FULLY_ARMED_EXPLORE);
			// 给客户端同步
			this.syncActivityInfo(playerId, entity);
			// 打点
			this.getDataGeter().logFullArmedSearch(playerId, entity.getSearchId());
		} catch (Exception e) {
			return Result.fail(Status.Error.FULLY_ARMED_UNKNOW_ERR_VALUE);
		}
		return Result.success();
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<FullyArmedEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		FullyArmedEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(FullyArmedAchieveCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.FULLY_ARMED_ACHIEVE_GAIN;
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
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, GameConst.MsgId.FULLY_ARMED_ACTIVITY_INIT, () -> {
				Optional<FullyArmedEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return ;
				}
				callBack(playerId, MsgId.FULLY_ARMED_ACTIVITY_INIT, () -> {
					initAchieveInfo(playerId);
				});
			});
		}
	}
}
