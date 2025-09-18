package com.hawk.activity.type.impl.loginfund;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BuyFundEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayFundEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.loginfund.cfg.LoginFundActivityCfg;
import com.hawk.activity.type.impl.loginfund.cfg.LoginFundActivityKVCfg;
import com.hawk.activity.type.impl.loginfund.entity.LoginFundEntity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.LoginFundInfoSync;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

/**
 * 登录基金活动
 * @author PhilChen
 *
 */
public class LoginFundActivity extends ActivityBase implements AchieveProvider {

	public LoginFundActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isShow(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<LoginFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		LoginFundEntity entity = opEntity.get();
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.LOGIN_FUND_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_LOGIN_FUND_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}


	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		LoginFundActivity activity = new LoginFundActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<LoginFundEntity> queryList = HawkDBManager.getInstance()
				.query("from LoginFundEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			LoginFundEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		LoginFundEntity entity = new LoginFundEntity(playerId, termId);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<LoginFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		LoginFundEntity entity = opEntity.get();
		LoginFundInfoSync.Builder builder = LoginFundInfoSync.newBuilder();
		builder.setIsBuy(entity.isBuy());
		builder.setIsNew(entity.isNew());
		builder.setLoginDay(Math.max(entity.getLoginDays(), 1));
		pushToPlayer(playerId, HP.code.PUSH_LOGINFUND_INFO_SYNC_S_VALUE, builder);
	}
	
	/**
	 * 进入活动页面
	 * @param playerId
	 */
	public void enterActivity(String playerId) {
		LoginFundActivityKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(LoginFundActivityKVCfg.class);
		Optional<LoginFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		LoginFundEntity entity = opEntity.get();
		int vipLevel = getDataGeter().getVipLevel(playerId);
		if (entity.isNew() && vipLevel >= kvConfig.getLimitVipLevel()) {
			entity.setIsNew(false);
		}
		syncActivityDataInfo(playerId);
	}
	
	/**
	 * 购买登录基金
	 * @param playerId
	 * @return
	 */
	public Result<?> buyLoginfund(String playerId) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		LoginFundActivityKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(LoginFundActivityKVCfg.class);
		if (kvConfig == null) {
			return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
		}
		ActivityDataProxy dataGeter = getDataGeter();
		int vipLevel = dataGeter.getVipLevel(playerId);
		if (vipLevel < kvConfig.getLimitVipLevel()) {
			return Result.fail(Status.Error.VIP_LEVEL_LOWER_VALUE);
		}
		
		Optional<LoginFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		LoginFundEntity entity = opEntity.get();
		if (entity.isBuy()) {
			return Result.fail(Status.Error.ACTIVITY_LOGINFUND_IS_BUY_VALUE);
		}
		
		List<RewardItem.Builder> itemList = new ArrayList<>();
		itemList.add(RewardHelper.toRewardItem(kvConfig.getPrice()));
		boolean consumeResult = getDataGeter().consumeItems(playerId, itemList, HP.code.LOGIN_FUND_ENTER_VALUE, Action.ACTIVITY_LOGIN_FUND_CONSUME);
		if (consumeResult == false) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		
		entity.setIsBuy(true);
		entity.setLoginDays(1);
		// 添加成就数据项
		ConfigIterator<LoginFundActivityCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(LoginFundActivityCfg.class);
		List<AchieveItem> items = new ArrayList<>();
		while (configIterator.hasNext()) {
			LoginFundActivityCfg config = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(config.getAchieveId());
			entity.addItem(item);
			items.add(item);
		}
		
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, items), true);
		ActivityManager.getInstance().postEvent(new LoginDayFundEvent(playerId, entity.getLoginDays()), true);
		ActivityManager.getInstance().postEvent(new BuyFundEvent(playerId,this.getActivityType().intValue(), 0));
		syncActivityDataInfo(playerId);
		// 流水记录
		getDataGeter().buyFundRecord(playerId, getActivityType());
		return Result.success();
	}
	
	/**
	 * 玩家是否购买登录基金
	 * @param playerId
	 * @return
	 */
	public boolean hasBuy(String playerId){
		Optional<LoginFundEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return false;
		}
		LoginFundEntity entity = opEntity.get();
		return entity.isBuy();
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<LoginFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		LoginFundEntity entity = opEntity.get();
		if (event.isCrossDay() && entity.isBuy()) {
			entity.setLoginDays(entity.getLoginDays() + 1);
		}

		if (entity.isBuy()) {
			ActivityManager.getInstance().postEvent(new LoginDayFundEvent(playerId, entity.getLoginDays()), true);
		}
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(LoginFundActivityCfg.class, achieveId);
	}

	@Override
	public boolean isActivityClose(String playerId) {
		Optional<LoginFundEntity> opEntity = getPlayerDataEntity(playerId);

		if (!opEntity.isPresent()) {
			return false;
		}
		LoginFundEntity entity = opEntity.get();
		if (!entity.isBuy()) {
			return false;
		}

		List<AchieveItem> itemList = entity.getItemList();
		if (itemList == null || itemList.isEmpty()) {
			return false;
		}

		for (AchieveItem item : entity.getItemList()) {
			if (item.getState() != AchieveState.TOOK_VALUE) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public void onTakeRewardSuccess(String playerId) {
		checkActivityClose(playerId);
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
