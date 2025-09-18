package com.hawk.activity.type.impl.dressup.energygather;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.EnergyGatherScoreEvent;
import com.hawk.activity.event.impl.LoginDayEnergyGatherEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.dressup.energygather.cfg.EnergyGatherAchieveCfg;
import com.hawk.activity.type.impl.dressup.energygather.cfg.EnergyGatherScoreCfg;
import com.hawk.activity.type.impl.dressup.energygather.entity.EnergyGatherEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 装扮投放系列活动二:能量聚集
 * (复制66号活动)
 * @author hf
 */
public class EnergyGatherActivity extends ActivityBase implements AchieveProvider {

	public EnergyGatherActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.ENERGY_GATHER_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ENERGY_GATHER_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		EnergyGatherActivity activity = new EnergyGatherActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<EnergyGatherEntity> queryList = HawkDBManager.getInstance()
				.query("from EnergyGatherEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			EnergyGatherEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		EnergyGatherEntity entity = new EnergyGatherEntity(playerId, termId);
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
		Optional<EnergyGatherEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		EnergyGatherEntity playerDataEntity = opPlayerDataEntity.get();
		initAchieveItems(playerId, false, playerDataEntity);
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	/**
	 * 当前是活动第几天
	 * @return
	 */
	private int getCurrentDays() {
		int termId = getTimeControl().getActivityTermId(HawkTime.getMillisecond());
		long openTime = getTimeControl().getStartTimeByTermId(termId); 
		int crossHour = getDataGeter().getCrossDayHour();
		int betweenDays = HawkTime.getCrossDay(HawkTime.getMillisecond(), openTime, crossHour);
		return betweenDays + 1;
	}
	

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_ENERGY_GATHER, ()-> {
				Optional<EnergyGatherEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return;
				}
				initAchieveItems(playerId, false, opEntity.get());
				EnergyGatherEntity entity = opEntity.get();
				//优化版,,登录就记录天数次数据已经排除重复
				entity.recordLoginDay();
				ActivityManager.getInstance().postEvent(new LoginDayEnergyGatherEvent(playerId, entity.getLoginDaysCount()), true);
			});
		}
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		int betweenDays = getCurrentDays();
		Activity.EnergyGatherInfoSync.Builder builder = Activity.EnergyGatherInfoSync.newBuilder();
		builder.setCurDays(betweenDays);
		pushToPlayer(playerId, HP.code.ENERGY_GATHER_INFO_SYNC_VALUE, builder);
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		boolean isLogin = event.isLogin();
		if (!isOpening(playerId)) {
			return;
		}
		int termId = getActivityTermId(playerId);
		long endTime = getTimeControl().getEndTimeByTermId(termId, playerId);
		long now = HawkTime.getMillisecond();
		if (now >= endTime) {
			return;
		}
		Optional<EnergyGatherEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		EnergyGatherEntity entity = opEntity.get();
		//优化版,,登录就记录天数次数据已经排除重复20211116
		entity.recordLoginDay();
		initAchieveItems(playerId, isLogin, entity);
		ActivityManager.getInstance().postEvent(new LoginDayEnergyGatherEvent(playerId, entity.getLoginDaysCount()), true);
		// 修正数据
		fixScore(entity);
	}
	
	/**
	 * 修正积分数据
	 * @param entity
	 */
	private void fixScore(EnergyGatherEntity entity) {
		int achieveTotal = 0, scoreTotal = 0;
		List<AchieveItem> scoreItems = new ArrayList<AchieveItem>();
		try {
			for (AchieveItem item : entity.getItemList()) {
				EnergyGatherAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(EnergyGatherAchieveCfg.class, item.getAchieveId());
				if (achieveCfg != null) {
					achieveTotal += item.getState() != AchieveState.TOOK_VALUE ? 0 : achieveCfg.getInTegral();
				} else if (item.getState() == AchieveState.NOT_ACHIEVE_VALUE) { // 只针对未达成条件的累计积分成就
					scoreTotal = Math.max(scoreTotal, item.getValue(0));
					scoreItems.add(item);
				}
			}
			
			// 成就任务积分总数和累计积分数一致时，或累计积分成就任务都达成时，不处理
			if (scoreItems.isEmpty() || scoreTotal == achieveTotal) {
				return;
			}
			
			HawkLog.logPrintln("EnergyGatherActivity score fix on login, playerId: {}, achieveTotal: {}, scoreTotal: {}", entity.getPlayerId(), achieveTotal, scoreTotal);
			for (AchieveItem item : scoreItems) {
				EnergyGatherScoreCfg config = HawkConfigManager.getInstance().getConfigByKey(EnergyGatherScoreCfg.class, item.getAchieveId());
				int configValue = config.getConditionValue(config.getConditionValues().size() - 1);
				item.setValue(0, Math.min(achieveTotal, configValue));
				if (achieveTotal >= configValue) {
					item.setState(AchieveState.NOT_REWARD_VALUE);
				}
			}
			// 数据落地
			entity.notifyUpdate();
			// 通知更新
			AchievePushHelper.pushAchieveUpdate(entity.getPlayerId(), scoreItems);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 初始化成就配置项
	 * @param playerId
	 * @param isLogin
	 * @param entity
	 */
	private void initAchieveItems(String playerId, boolean isLogin, EnergyGatherEntity entity) {
		int betweenDays = getCurrentDays();
		// 已经初始化过的成就配置不再处理
		if (betweenDays <= entity.getInitDays()) {
			return;
		}
		
		List<AchieveItem> items = new ArrayList<>();
		if (entity.getInitDays() == 0) {
			ConfigIterator<EnergyGatherScoreCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(EnergyGatherScoreCfg.class);
			while (configIterator.hasNext()) {
				EnergyGatherScoreCfg cfg = configIterator.next();
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				entity.addItem(item);
				items.add(item);
			}
		}
		
		for (int i = entity.getInitDays() + 1; i <= betweenDays; i++) {
			ConfigIterator<EnergyGatherAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(EnergyGatherAchieveCfg.class);
			while (configIterator.hasNext()) {
				EnergyGatherAchieveCfg cfg = configIterator.next();
				if (cfg.getDay() != i) {
					continue;
				}
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				entity.addItem(item);
				items.add(item);
			}
		}
		if (items.size() > 0) {
			ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, items), true);
			entity.setInitDays(betweenDays);
			if (isLogin == false) {
				syncActivityDataInfo(playerId);
			}
		}
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(EnergyGatherAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(EnergyGatherScoreCfg.class, achieveId);
		}
		return config;
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		EnergyGatherAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(EnergyGatherAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			EnergyGatherScoreCfg scoreCfg = HawkConfigManager.getInstance().getConfigByKey(EnergyGatherScoreCfg.class, achieveId);
			if (scoreCfg == null) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
		} else {
			ActivityManager.getInstance().postEvent(new EnergyGatherScoreEvent(playerId, achieveCfg.getInTegral()));
		}
		
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
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
