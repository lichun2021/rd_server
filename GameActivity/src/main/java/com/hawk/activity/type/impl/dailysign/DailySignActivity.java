package com.hawk.activity.type.impl.dailysign;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.eventbus.Subscribe;
import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.MonthCardSignOK;
import com.hawk.activity.event.impl.PlayerSignEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.dailysign.cfg.DailySignAchieveCfg;
import com.hawk.activity.type.impl.dailysign.cfg.DailySignActivityTimeCfg;
import com.hawk.activity.type.impl.dailysign.cfg.DailySignRetroactiveCfg;
import com.hawk.activity.type.impl.dailysign.cfg.DailySignRewardsCfg;
import com.hawk.activity.type.impl.dailysign.entity.DailySignEntity;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.game.protocol.Activity.PBDailySignInfoSync;
import com.hawk.game.protocol.Activity.PBDailySignResignReq;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class DailySignActivity extends ActivityBase implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public DailySignActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DAILY_SIGN_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<DailySignEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			// 刷成就
			if (opDataEntity.get().getItemList().isEmpty()) {
				initAchieveInfo(playerId);
			}
			if (opDataEntity.get().getTermRewardList().isEmpty()) {
				initCurTermRewards(playerId);
			}
		}
		
		// 第一次登录时取不到数据，所以要做延迟处理
		HawkTaskManager.getInstance().postTask( new HawkDelayTask(5000, 5000, 1) {
			@Override
			public Object run() {
				syncAchieveInfo(playerId);
				return null;
			}
		});
	}

	@Override
	public void onOpenForPlayer(String playerId) {
		callBack(playerId, MsgId.DAILY_SIGN_ACTIVITY_INIT, () -> {
			Optional<DailySignEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			// 刷成就
			if (opDataEntity.get().getItemList().isEmpty()) {
				initAchieveInfo(playerId);
			}
			// 刷奖励
			if (opDataEntity.get().getTermRewardList().isEmpty()) {
				initCurTermRewards(playerId);
			}
			this.syncActivityInfo(playerId, opDataEntity.get());
		});
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<DailySignEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	// 同步签到消息玩家
	public void syncActivityInfo(String playerId, DailySignEntity entity) {
		if (!isOpening(playerId)) {
			return;
		}

		PBDailySignInfoSync.Builder builder = PBDailySignInfoSync.newBuilder();
		builder.setCurDayIndex(getActivityOpenDays(playerId) + 1);
		builder.setCurTerm(entity.getCfgPoolId());
		builder.getInfoBuilder().setSignDays(entity.getSignDays());
		builder.getInfoBuilder().setResignDays(entity.getResignDays());
		builder.getInfoBuilder().setSignToday(entity.getSignToday());
		builder.getInfoBuilder().addAllRewards(entity.getTermRewardList());
		if(!entity.getTermRewardList().isEmpty()){
			if(0 == entity.getCfgPoolId()){
				builder.setCurTerm(1);	
			}
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.DAILY_SIGN_INFO_SYN_S, builder));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DailySignActivity activity = new DailySignActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DailySignEntity> queryList = HawkDBManager.getInstance()
				.query("from DailySignEntity where playerId = ? and termId = ? and invalid = 0 order by createTime desc limit 1", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DailySignEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DailySignEntity entity = new DailySignEntity(playerId, termId);
		// 活动开放或者活动新的一轮
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
		Optional<DailySignEntity> opEntity = getPlayerDataEntity(playerId);
		DailySignEntity entity = opEntity.get();
		if (event.isCrossDay()) {
			entity.setSignToday(0);
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

		Optional<DailySignEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		DailySignEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public Action takeRewardAction() {
		return Action.DAILY_SIGN_ACHIEVE_AWARD;
	}

	@Override
	public DailySignAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(DailySignAchieveCfg.class, achieveId);
	}

	private void initCurTermRewards(String playerId) {
		try {
			Optional<DailySignEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			DailySignEntity entity = opEntity.get();

			// 每日奖励
			if (!entity.getTermRewardList().isEmpty()) {
				return;
			}

			Map<Integer, DailySignRewardsCfg> termCfgAll = DailySignRewardsCfg.getTermCfg(entity.getTermId());
			if (null != termCfgAll) {
				for (Map.Entry<Integer, DailySignRewardsCfg> cfg : termCfgAll.entrySet()) {
					entity.addTermRewards(cfg.getValue().getRewardList().get(0).build());
					entity.setCfgPoolId(cfg.getValue().getPool());
				}
			}
			entity.notifyUpdate();
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	/**
	 * 初始化成就信息
	 * 
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<DailySignEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		DailySignEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<DailySignAchieveCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(DailySignAchieveCfg.class);
		while (configIterator.hasNext()) {
			DailySignAchieveCfg next = configIterator.next();
			int targetTerm = Math.min(entity.getTermId(), DailySignAchieveCfg.getMaxTermId());
			if (next.getPool() == targetTerm) {
				AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
				entity.addItem(item);
				entity.setCfgPoolId(targetTerm );
			}
		}
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		entity.notifyUpdate();
		logger.debug("dailysign_log refresh achieveitems :");
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		DailySignAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(DailySignAchieveCfg.class,
				achieveId);
		if (achieveCfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}

		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId,
				ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return AchieveProvider.super.onTakeReward(playerId, achieveId);
	}

	private List<RewardItem.Builder> getRewardItemsByDayIndex(DailySignEntity entity, int dayIndex) {
		RewardItem rewardItem = null;
		if (entity.getTermRewardList().size() > dayIndex) {
			rewardItem = entity.getTermRewardList().get(dayIndex);
		}
		if (null == rewardItem) {
			return null;
		}
		List<RewardItem.Builder> rewardItems = new ArrayList<RewardItem.Builder>();
		rewardItems.add(RewardItem.newBuilder(rewardItem));

		return rewardItems;
	}

	// 获取活动开启了几天
	private int getActivityOpenDays(String playerId) {

		Optional<DailySignEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return -1;
		}

		DailySignEntity entity = opEntity.get();

		DailySignActivityTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DailySignActivityTimeCfg.class,
				entity.getTermId());
		if (null == cfg) {
			return -1;
		}

		DailySignTimeController timeController = getActivityType().getTimeControl();

		int days = 0;
		long termOpenTime = 0;
		if (null != timeController) {
			termOpenTime = timeController.getShowTimeByTermId(entity.getTermId(), playerId);
			days = HawkTime.calcBetweenDays(new Date(termOpenTime), HawkTime.getAM0Date());
		}

		return days;
	}

	// 签到
	public boolean onPlayerSign(String playerId) {
		Optional<DailySignEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return false;
		}
		DailySignEntity entity = opDataEntity.get();

		// 判断今日是否已经签到
		if (0 != entity.getSignToday()) {
			logger.info("daily_sign error, playerId: {}, termId: {}, signToday: {}", playerId, entity.getTermId(), entity.getSignToday());
			return false;
		}
		// 判断今日是否可补签 1 ~ 30
		int days = getActivityOpenDays(playerId) + 1;
		if (days < 0) {
			logger.info("daily_sign error, playerId: {}, termId: {}, days: {}", playerId, entity.getTermId(), days);
			return false;
		}
		
		if (entity.getSignDays() + entity.getResignDays() > days) {
			logger.info("daily_sign error, playerId: {}, termId: {}, days: {}, signTotalDays: {}", playerId, entity.getTermId(), days, entity.getSignDays() + entity.getResignDays());
			return false;
		}
		// 总签到天数
		int signTotalDays = entity.getSignDays() + entity.getResignDays();

		List<RewardItem.Builder> rewardList = getRewardItemsByDayIndex(entity, signTotalDays);
		if (null == rewardList) {
			logger.info("daily_sign error, rewardList null, playerId: {}, termId: {}, signTotalDays: {}", playerId, entity.getTermId(), signTotalDays);
			return false;
		}

		// 签到奖励
		DailySignRewardsCfg rewardCfg = DailySignRewardsCfg.getCfg(entity.getTermId(), signTotalDays + 1);
		if (null == rewardCfg) {
			logger.info("daily_sign error, rewardCfg null, playerId: {}, termId: {}, signTotalDays: {}", playerId, entity.getTermId(), signTotalDays);
			return false;
		}

		int vipLv = getDataGeter().getVipLevel(playerId);
		int muti = 1;
		if (vipLv >= rewardCfg.getVip()) {
			muti = rewardCfg.getMutiple();
		}
		// 记录签到次数
		entity.setSignDays(entity.getSignDays() + 1);
		entity.setSignToday(entity.getSignToday() + 1);
		entity.notifyUpdate();
		// 发送奖励
		this.getDataGeter().takeReward(playerId, rewardList, muti, Action.DAILY_SIGN_AWARD, true, RewardOrginType.MONCARD_DAILY_SIGN);
		logger.info("daily_sign playerId: {}, termId: {}, day: {} num: {}", playerId, entity.getTermId(), rewardCfg.getDay(), muti);

		// 成就逻辑
		ActivityManager.getInstance().postEvent(MonthCardSignOK.valueOf(playerId));
		this.syncActivityInfo(playerId, entity);

		getDataGeter().dailySignRewardRecord(playerId, 1, entity.getTermId(), signTotalDays + 1, null);
		// 签到事件
		ActivityManager.getInstance().postEvent(new PlayerSignEvent(playerId));
		return true;
	}

	// 补签
	public boolean onPlayerResign(String playerId, PBDailySignResignReq req) {
		Optional<DailySignEntity> opDataEntity = getPlayerDataEntity(playerId);
		logger.debug("dailysign_log onPlayerResign player:{} req:{}", playerId, JsonFormat.printToString(req));

		if (!opDataEntity.isPresent()) {
			return false;
		}
		DailySignEntity entity = opDataEntity.get();

		// 判断今日是否可补签 1 ~ 30
		int days = getActivityOpenDays(playerId) + 1;
		if (days < 0) {
			logger.info("daily_resign error, playerId: {}, termId: {}, days: {}", playerId, entity.getTermId(), days);
			return false;
		}
		if (0 == entity.getSignToday()) {
			logger.info("daily_resign error, playerId: {}, termId: {}, signToday: {}", playerId, entity.getTermId(), entity.getSignToday());
			return false;
		}

		// 判断参数
		if (entity.getResignDays() > req.getResignDay() || entity.getSignDays() + req.getResignDay() > days) {
			logger.info("daily_resign error, playerId: {}, termId: {}, days:{}, signDays: {}, resignDays: {}, req resignDay: {}", playerId, entity.getTermId(), days,
					entity.getSignDays(), entity.getResignDays(), req.getResignDay());
			return false;
		}

		// 消费
		DailySignRetroactiveCfg costCfg = HawkConfigManager.getInstance().getConfigByKey(DailySignRetroactiveCfg.class,
				entity.getResignDays() + 1);
		if (null == costCfg) {
			logger.info("daily_resign error, playerId: {}, termId: {}, resignDays: {}", playerId, entity.getTermId(), entity.getResignDays());
			return false;
		}

		// 总签到天数
		int signTotalDays = entity.getSignDays() + entity.getResignDays();
		// 奖励
		DailySignRewardsCfg rewardCfg = DailySignRewardsCfg.getCfg(entity.getTermId(), signTotalDays + 1);
		if (null == rewardCfg) {
			logger.info("daily_resign error, rewardCfg null, playerId: {}, termId: {}, signTotalDays: {}", playerId, entity.getTermId(), signTotalDays);
			return false;
		}
		int vipLv = getDataGeter().getVipLevel(playerId);
		int muti = 1;
		if (vipLv >= rewardCfg.getVip()) {
			muti = rewardCfg.getMutiple();
		}

		List<RewardItem.Builder> rewardList = getRewardItemsByDayIndex(entity, signTotalDays);

		if (null == rewardList) {
			logger.info("daily_resign error, rewardList null, playerId: {}, termId: {}, signTotalDays: {}", playerId, entity.getTermId(), signTotalDays);
			return false;
		}

		// 扣钱
		boolean flag = this.getDataGeter().cost(playerId, costCfg.getCostList(), 1, Action.DAILY_SIGN_RESIGN_COST, true);
		if (!flag) {
			return false;
		}
		entity.setResignDays(entity.getResignDays() + 1);
		// 记录签到次数
		entity.notifyUpdate();
		// 发送奖励
		this.getDataGeter().takeReward(playerId, rewardList, muti, Action.DAILY_SIGN_RESIGN_AWARD, true, RewardOrginType.MONCARD_DAILY_SIGN);
		logger.info("daily_sign resign playerId: {}, termId: {}, day: {} num: {}", playerId, entity.getTermId(), rewardCfg.getDay(), muti);

		// 成就逻辑
		ActivityManager.getInstance().postEvent(MonthCardSignOK.valueOf(playerId));

		this.syncActivityInfo(playerId, entity);
		getDataGeter().dailySignRewardRecord(playerId, 2, entity.getTermId(), signTotalDays + 1, costCfg.getRetroactiveCost());
		return true;
	}
	
	private void syncAchieveInfo(String playerId) {
		try {
			Optional<AchieveItems> achieveItems = getAchieveItems(playerId);
			if (achieveItems.isPresent()) {
				AchievePushHelper.pushAchieveInfo(playerId, achieveItems.get().getItems());
			}	
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
