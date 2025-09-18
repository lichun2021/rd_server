package com.hawk.activity.type.impl.dailyrecharge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.game.protocol.Activity.DailyRechargeAccBoughtInfo;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.dailyrecharge.cfg.RechargeBuyBoxCfg;
import com.hawk.activity.type.impl.dailyrecharge.entity.DailyRechargeEntity;
import com.hawk.activity.type.impl.dailyrecharge.cfg.RechargeBuyAchieveCfg;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 今日累充活动
 * 
 * @author lating
 *
 */
public class DailyRechargeActivity extends ActivityBase implements AchieveProvider {

	public DailyRechargeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.DAILY_RECHARGE_ACC_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.DAILY_RECHARGE_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DailyRechargeActivity activity = new DailyRechargeActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DailyRechargeEntity> queryList = HawkDBManager.getInstance()
				.query("from DailyRechargeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DailyRechargeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DailyRechargeEntity entity = new DailyRechargeEntity(playerId, termId);
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
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_DAILY_RECHARGE, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	/**
	 * 初始化成就信息
	 * 
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<DailyRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		DailyRechargeEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		// 初始添加成就项
		List<AchieveItem> itemList = entity.getItemList();
		ConfigIterator<RechargeBuyAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(RechargeBuyAchieveCfg.class);
		while (configIterator.hasNext()) {
			RechargeBuyAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			itemList.add(item);
		}
		
		entity.setRefreshTime(HawkTime.getMillisecond());
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		String playerId = event.getPlayerId();
		Optional<DailyRechargeEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		
		long now = HawkTime.getMillisecond();
		DailyRechargeEntity dataEntity = opDataEntity.get();
		if (event.isCrossDay() || !HawkTime.isSameDay(now, dataEntity.getRefreshTime())) {
			List<AchieveItem> items = new ArrayList<>();
			ConfigIterator<RechargeBuyAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(RechargeBuyAchieveCfg.class);
			while (achieveIterator.hasNext()) {
				RechargeBuyAchieveCfg cfg = achieveIterator.next();
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				items.add(item);
			}
			
			dataEntity.resetItemList(items);
			dataEntity.resetBuyItemList();
			dataEntity.setRefreshTime(now);
			// 推送给客户端
			AchievePushHelper.pushAchieveUpdate(playerId, dataEntity.getItemList());
			syncBoughtBoxInfo(playerId);
		}
	}
	
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<DailyRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		
		DailyRechargeEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public RechargeBuyAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(RechargeBuyAchieveCfg.class, achieveId);
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		this.getDataGeter().logDailyRecharge(playerId, achieveId, false);
		return Result.success();
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		
	}

	/**
	 * 同步已购买的宝箱信息
	 * 
	 * @param playerId
	 */
	public int syncBoughtBoxInfo(String playerId) {
		if (!isOpening(playerId)) {
			HawkLog.errPrintln("DailyRechargeActivity syncBoughtBoxInfo failed, errCode: {}, playerId: {}", Status.Error.ACTIVITY_NOT_OPEN_VALUE, playerId);
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<DailyRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("DailyRechargeActivity syncBoughtBoxInfo failed, errCode: {}, playerId: {}", Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE, playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		DailyRechargeEntity entity = opEntity.get();
		DailyRechargeAccBoughtInfo.Builder builder = DailyRechargeAccBoughtInfo.newBuilder();
		builder.addAllBoxId(entity.getBuyItemList());
		pushToPlayer(playerId, HP.code.DAILY_RECHARGE_BOUGHT_PUSH_VALUE, builder);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 购买宝箱请求
	 * 
	 * @param playerId
	 * @param boxId
	 */
	public int buyRewardBox(String playerId, int boxId) {
		if (!isOpening(playerId)) {
			HawkLog.errPrintln("DailyRechargeActivity buy failed, errCode: {}, playerId: {}, boxId: {}", Status.Error.ACTIVITY_NOT_OPEN_VALUE, playerId, boxId);
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		RechargeBuyBoxCfg boxCfg = HawkConfigManager.getInstance().getConfigByKey(RechargeBuyBoxCfg.class, boxId);
		if (boxCfg == null) {
			HawkLog.errPrintln("DailyRechargeActivity buy failed, errCode: {}, playerId: {}, boxId: {}", Status.SysError.PARAMS_INVALID_VALUE, playerId, boxId);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		Optional<DailyRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("DailyRechargeActivity buy failed, errCode: {}, playerId: {}, boxId: {}", Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE, playerId, boxId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		DailyRechargeEntity entity = opEntity.get();
		if (entity.getBuyItemList().contains(boxId)) {
			HawkLog.errPrintln("DailyRechargeActivity buy failed, errCode: {}, playerId: {}, boxId: {}", Status.Error.DAILY_RECHARGE_ACC_BOX_BOUGHT_VALUE, playerId, boxId);
			return Status.Error.DAILY_RECHARGE_ACC_BOX_BOUGHT_VALUE;
		}
		
		AchieveItem achieveItem = null;
		for (AchieveItem item : entity.getItemList()) {
			if (item.getAchieveId() == boxCfg.getAchieveId()) {
				achieveItem = item;
				break;
			}
		}
		
		if (achieveItem == null || achieveItem.getState() == AchieveState.NOT_ACHIEVE_VALUE) {
			HawkLog.errPrintln("DailyRechargeActivity buy failed, errCode: {}, playerId: {}, boxId: {}", Status.Error.DAILY_RECHARGE_ACC_CONDITION_ERROR_VALUE, playerId, boxId);
			return Status.Error.DAILY_RECHARGE_ACC_CONDITION_ERROR_VALUE;
		}
		
		boolean success = getDataGeter().consumeItems(playerId, boxCfg.getPrice(), HP.code.DAILY_RECHARGE_BUY_REQ_VALUE, Action.DAILY_RECHARGE_BUY_CONSUME);
		if (!success) {
			HawkLog.errPrintln("DailyRechargeActivity buy failed, consumeItems not enought, playerId: {}, boxId: {}", playerId, boxId);
			return 0; // 这里不要去假设消耗的时什么东西，不足时在掉消耗接口时已经返回错误提示了，所以这里可以不用去管到底是什么不足的问题
		}
		
		entity.addBuyItem(boxId);
		getDataGeter().takeReward(playerId, boxCfg.getRewardList(), Action.DAILY_RECHARGE_BUY_REWARD, true);
		
		syncBoughtBoxInfo(playerId);
		
		// 记录tlog
		getDataGeter().logDailyRecharge(playerId, boxId, true);
		
		HawkLog.logPrintln("DailyRechargeActivity buy success, playerId: {}, boxId: {}", playerId, boxId);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
}
