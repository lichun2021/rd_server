package com.hawk.activity.type.impl.redrecharge;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.HappyRedRechargeScoreEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.redrecharge.cfg.HappyRedRechargeAwardCfg;
import com.hawk.activity.type.impl.redrecharge.cfg.HappyRedRechargeKVConfig;
import com.hawk.activity.type.impl.redrecharge.cfg.HappyRedRechargeScoreCfg;
import com.hawk.activity.type.impl.redrecharge.entity.HappyRedRechargeEntity;
import com.hawk.activity.type.impl.redrecharge.entity.HappyRedRechargeItem;
import com.hawk.game.protocol.Activity.RedRechargeActivityPB;
import com.hawk.game.protocol.Activity.RedRechargeItemPB;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 欢乐限购（红包）活动
 * 
 * @author lating
 */
public class HappyRedRechargeActivity extends ActivityBase implements AchieveProvider {

	public HappyRedRechargeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.RED_RECHARGE_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.RED_RECHARGE_BOX_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		HappyRedRechargeActivity activity = new HappyRedRechargeActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<HappyRedRechargeEntity> queryList = HawkDBManager.getInstance()
				.query("from HappyRedRechargeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			HappyRedRechargeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		HappyRedRechargeEntity entity = new HappyRedRechargeEntity(playerId, termId);
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
		Optional<HappyRedRechargeEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		HappyRedRechargeEntity playerDataEntity = opPlayerDataEntity.get();
		initAchieveItems(playerDataEntity);
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
	
	/**
	 * 判断是否是活动最后一天
	 * 
	 * @return
	 */
	private boolean isLastday(String playerId) {
		int termId = getTimeControl().getActivityTermId(HawkTime.getMillisecond());
		long endTime = getTimeControl().getEndTimeByTermId(termId, playerId);
		long now = HawkTime.getMillisecond();
		return !HawkTime.isCrossDay(now, endTime, 0);
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.RED_RECHARGE_INIT, ()-> {
				Optional<HappyRedRechargeEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return;
				}
				HappyRedRechargeEntity entity = opEntity.get();
				initAchieveItems(entity);
				entity.recordLoginDay();
				syncActivityInfo(entity);
			});
		}
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
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
		Optional<HappyRedRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		HappyRedRechargeEntity entity = opEntity.get();
		if (event.isCrossDay() || !entity.getLoginDaysList().contains(HawkTime.getYyyyMMddIntVal())) {
			for (HappyRedRechargeItem rechargeItem : entity.getRechargeItems()) {
				rechargeItem.setBuyCountToday(0);
			}
		}
		
		entity.recordLoginDay();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(entity);
		}
		
		syncActivityInfo(entity);
	}

	/**
	 * 初始化成就配置项
     *
	 * @param entity
	 */
	private void initAchieveItems(HappyRedRechargeEntity entity) {
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		List<AchieveItem> items = new ArrayList<>();
		ConfigIterator<HappyRedRechargeScoreCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(HappyRedRechargeScoreCfg.class);
		while (configIterator.hasNext()) {
			HappyRedRechargeScoreCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			items.add(item);
		}
		
		entity.resetItemList(items);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), entity.getItemList()), true);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(HappyRedRechargeScoreCfg.class, achieveId);
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		HappyRedRechargeScoreCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(HappyRedRechargeScoreCfg.class, achieveId);
		if (achieveCfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	/***
	 * 购买事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<HappyRedRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("HappyRedRechargeActivity purchase event handle failed, entity data not exist, playerId: {}", playerId);
			return;
		}
		
		HappyRedRechargeEntity entity = opEntity.get();
		String payGiftId = event.getGiftId();
		int cfgId = HappyRedRechargeAwardCfg.getGiftId(payGiftId);
		HappyRedRechargeAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(HappyRedRechargeAwardCfg.class, cfgId);
		if(config == null){
			HawkLog.errPrintln("HappyRedRechargeActivity purchase event match failed, playerId: {}, payGiftId: {}, cfgId: {}", playerId, payGiftId, cfgId);
		    return;
		}
		
		int totalScore = entity.getScore() + config.getIntegral();
		//给宝箱奖励
		HawkLog.logPrintln("HappyRedRechargeActivity purchase success, playerId: {}, payGiftId: {}, cfgId: {}, score: {}, totalScore: {}", 
				playerId, payGiftId, cfgId, config.getIntegral(), totalScore);
		
		// 基础奖励 + 红包
		String itemInfo = this.getDataGeter().sendAwardFromAwardCfg(config.getAwardId(), 1, playerId, true, Action.RED_RECHARGE_AWARD, RewardOrginType.ACTIVITY_REWARD);
		// 积分
		ActivityManager.getInstance().postEvent(new HappyRedRechargeScoreEvent(playerId, config.getIntegral()), true);
		entity.setScore(totalScore);
		// 更新数据到数据库
		HappyRedRechargeItem item = entity.getRechargeItem(cfgId);
		if (item == null) {
			item = HappyRedRechargeItem.valueOf(cfgId);
			entity.addRechargeItem(item);
		} else {
			long now = HawkTime.getMillisecond();
			item.setBuyCount(item.getBuyCount() + 1);
			item.setBuyCountToday(HawkTime.isCrossDay(now, item.getLatestBuyTime(), 0) ? 1 : item.getBuyCountToday() + 1);
			item.setLatestBuyTime(now);
			entity.notifyUpdate();
		}
		
		//同步界面信息
		syncActivityInfo(entity);
		
		// 收据邮件
		List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(itemInfo);
        this.getDataGeter().sendMail(playerId, MailId.HAPPY_RED_RECHARGE_REWARD,
                 new Object[]{this.getActivityCfg().getActivityName()},
                 new Object[]{this.getActivityCfg().getActivityName()},
                 null,
                 rewardList, true);
	}
	
	/**
	 * 判断是否可以购买
	 * 
	 * @param payGiftId
	 * @return
	 */
	public boolean buyGiftCheck(String playerId, String payGiftId) {
		int cfgId = HappyRedRechargeAwardCfg.getGiftId(payGiftId);
		HappyRedRechargeAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(HappyRedRechargeAwardCfg.class, cfgId);
		if(config == null) {
			HawkLog.errPrintln("HappyRedRechargeAwardCfg error, playerId: {}, payGiftId: {},  cfgId: {}", playerId, payGiftId, cfgId);
		    return false;
		}
		
		if (!isOpening(playerId)) {
			return false;
		}
		
		Optional<HappyRedRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("HappyRedRechargeActivity purchase check failed, data entity not exist, playerId: {}", playerId);
			return false;
		}
		
		HappyRedRechargeEntity entity = opEntity.get();
		HappyRedRechargeItem item = entity.getRechargeItem(cfgId);
		if(item == null) {
			return true;
		}
		
		HappyRedRechargeKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(HappyRedRechargeKVConfig.class);
		boolean lastday = isLastday(playerId);
		if (lastday && cfg.isSummation()) {
			int day = getCurrentDays();
			return item.getBuyCount() < day;
		}
		
		boolean crossDay = HawkTime.isCrossDay(HawkTime.getMillisecond(), item.getLatestBuyTime(), 0);
		int limit = cfg.getLimit();
		int remainCount = crossDay ? limit : limit - item.getBuyCountToday();
		return remainCount > 0;
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
	private void syncActivityInfo(HappyRedRechargeEntity entity) {
		RedRechargeActivityPB.Builder builder = RedRechargeActivityPB.newBuilder();
		int day = getCurrentDays();
		builder.setDay(day);
		builder.setScore(entity.getScore());
		long now = HawkTime.getMillisecond();
		boolean lastday = isLastday(entity.getPlayerId());
		HappyRedRechargeKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(HappyRedRechargeKVConfig.class);
		lastday = lastday && cfg.isSummation();
		int limit = cfg.getLimit();
		ConfigIterator<HappyRedRechargeAwardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(HappyRedRechargeAwardCfg.class);
		while(iterator.hasNext()){
			HappyRedRechargeAwardCfg config = iterator.next();
			HappyRedRechargeItem rechargeItem = entity.getRechargeItem(config.getId());
			RedRechargeItemPB.Builder item = RedRechargeItemPB.newBuilder();
			item.setCfgId(config.getId());
			if (rechargeItem == null) {
				int count = lastday ? day : limit;
				item.setRemainCount(count);
				item.setTotalCountToday(count);
			} else {
				boolean crossDay = HawkTime.isCrossDay(now, rechargeItem.getLatestBuyTime(), 0);
				int remainCount = day - rechargeItem.getBuyCount();
				remainCount = lastday ? remainCount : (crossDay ? limit : limit - rechargeItem.getBuyCountToday());
				item.setRemainCount(remainCount);
				int totalCount = lastday ? remainCount + rechargeItem.getBuyCountToday() : limit;
				item.setTotalCountToday(totalCount);
			}
			builder.addRechargeItem(item);
		}
		
		//同步信息
		PlayerPushHelper.getInstance().pushToPlayer(entity.getPlayerId(), HawkProtocol.valueOf(HP.code2.RED_RECHARGE_ACTIVITY_INFO_SYNC, builder));
	}
	
}
