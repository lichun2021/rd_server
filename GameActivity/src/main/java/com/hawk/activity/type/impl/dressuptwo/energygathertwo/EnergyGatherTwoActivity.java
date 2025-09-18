package com.hawk.activity.type.impl.dressuptwo.energygathertwo;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayEnergyGatherTwoEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.dressuptwo.energygathertwo.cfg.EnergyGatherTwoAchieveCfg;
import com.hawk.activity.type.impl.dressuptwo.energygathertwo.entity.EnergyGatherTwoEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 圣诞节系列活动一:冰雪计划活动
 * @author hf
 */
public class EnergyGatherTwoActivity extends ActivityBase implements AchieveProvider {

	public EnergyGatherTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.ENERGY_GATHER_TWO_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ENERGY_GATHER_TWO_AWARD;
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		EnergyGatherTwoActivity activity = new EnergyGatherTwoActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<EnergyGatherTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from EnergyGatherTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			EnergyGatherTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		EnergyGatherTwoEntity entity = new EnergyGatherTwoEntity(playerId, termId);
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
		Optional<EnergyGatherTwoEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		EnergyGatherTwoEntity playerDataEntity = opPlayerDataEntity.get();
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
			callBack(playerId, MsgId.ACHIEVE_INIT_ENERGY_GATHER_TWO, ()-> {
				Optional<EnergyGatherTwoEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return;
				}
				initAchieveItems(playerId, false, opEntity.get());
				EnergyGatherTwoEntity entity = opEntity.get();
				//优化版,,登录就记录天数次数据已经排除重复
				entity.recordLoginDay();
				ActivityManager.getInstance().postEvent(new LoginDayEnergyGatherTwoEvent(playerId, entity.getLoginDaysCount()), true);
			});
		}
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		int betweenDays = getCurrentDays();
		Activity.EnergyGatherInfoSync.Builder builder = Activity.EnergyGatherInfoSync.newBuilder();
		builder.setCurDays(betweenDays);
		pushToPlayer(playerId, HP.code.ENERGY_GATHER_TWO_INFO_SYNC_VALUE, builder);
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
		Optional<EnergyGatherTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		EnergyGatherTwoEntity entity = opEntity.get();
		//优化版,,登录就记录天数次数据已经排除重复20211116
		entity.recordLoginDay();
		initAchieveItems(playerId, isLogin, entity);
		ActivityManager.getInstance().postEvent(new LoginDayEnergyGatherTwoEvent(playerId, entity.getLoginDaysCount()), true);
	}

	/**
	 * 初始化成就配置项
	 * @param playerId
	 * @param isLogin
	 * @param entity
	 */
	private void initAchieveItems(String playerId, boolean isLogin, EnergyGatherTwoEntity entity) {
		int betweenDays = getCurrentDays();
		// 已经初始化过的成就配置不再处理
		if (betweenDays <= entity.getInitDays()) {
			return;
		}
		List<AchieveItem> items = new ArrayList<>();
		for (int i = entity.getInitDays() + 1; i <= betweenDays; i++) {
			ConfigIterator<EnergyGatherTwoAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(EnergyGatherTwoAchieveCfg.class);
			while (configIterator.hasNext()) {
				EnergyGatherTwoAchieveCfg cfg = configIterator.next();
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
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(EnergyGatherTwoAchieveCfg.class, achieveId);
		return config;
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		EnergyGatherTwoAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(EnergyGatherTwoAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
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
