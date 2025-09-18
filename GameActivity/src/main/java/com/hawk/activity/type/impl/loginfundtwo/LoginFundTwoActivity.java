package com.hawk.activity.type.impl.loginfundtwo;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.BuyFundEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayFundTwoEvent;
import com.hawk.activity.event.impl.LoginFundBuyEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.loginfundtwo.cfg.LoginFundActivityTwoAchieveCfg;
import com.hawk.activity.type.impl.loginfundtwo.cfg.LoginFundActivityTwoKVCfg;
import com.hawk.activity.type.impl.loginfundtwo.entity.LoginFundTwoEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.result.Result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 登录基金2活动
 * @author hf
 */
public class LoginFundTwoActivity extends ActivityBase implements AchieveProvider {
	public LoginFundTwoActivity(int activityId, ActivityEntity activityEntity) {
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
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<LoginFundTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		LoginFundTwoEntity entity = opEntity.get();
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.LOGIN_FUND_TWO_ACTIVITY;
	}

	@Override
	public Action takeRewardAction() {
		return Action.ACTIVITY_LOGIN_FUND_TWO_AWARD;
	}


	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		LoginFundTwoActivity activity = new LoginFundTwoActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<LoginFundTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from LoginFundTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			LoginFundTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		LoginFundTwoEntity entity = new LoginFundTwoEntity(playerId, termId);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<LoginFundTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		LoginFundTwoEntity entity = opEntity.get();
		Activity.LoginFundTwoInfoSync.Builder builder = Activity.LoginFundTwoInfoSync.newBuilder();
		builder.addAllHasBuyType(entity.getBuyInfoMap().keySet());
		pushToPlayer(playerId, HP.code.PUSH_LOGINFUND_TWO_INFO_SYNC_S_VALUE, builder);
	}



	/**
	 * 购买登录基金
	 * @return
	 */
	@Subscribe
	public void onLoginFundBuyEvent(LoginFundBuyEvent event) {
		String giftId = event.getGiftId();
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			super.logger.info("LoginFundTwoActivity onLoginFundBuyEvent activity is not open, playerId: {}, giftId:{}",playerId, giftId);
			return;
		}
		LoginFundActivityTwoKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(LoginFundActivityTwoKVCfg.class);
		if (kvConfig == null) {
			super.logger.info("LoginFundTwoActivity onLoginFundBuyEvent LoginFundActivityTwoKVCfg is not exist, playerId: {}, giftId:{}",playerId, giftId);
			return;
		}
		int type = kvConfig.getBuyType(giftId);
		ActivityDataProxy dataGeter = getDataGeter();
		//vip等级限制
		int vipLevel = dataGeter.getVipLevel(playerId);
		if (vipLevel < kvConfig.getLimitVipLevelByType(type)) {
			super.logger.info("LoginFundTwoActivity onLoginFundBuyEvent vipLevel is limit, playerId: {}, giftId:{},vipLevel:{}",playerId, giftId, vipLevel);
			return;
		}
		//主堡等级限制
		int facLv = dataGeter.getConstructionFactoryLevel(playerId);
		if (facLv < kvConfig.getBuyCityLimitByType(type)) {
			super.logger.info("LoginFundTwoActivity onLoginFundBuyEvent facLv is limit, playerId: {}, giftId:{},facLv:{}",playerId, giftId, facLv);
			return;
		}

		Optional<LoginFundTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			super.logger.info("LoginFundTwoActivity onLoginFundBuyEvent LoginFundTwoEntity is not exist, playerId: {}, giftId:{}",playerId, giftId);
			return;
		}
		LoginFundTwoEntity entity = opEntity.get();
		if (entity.isHasBuy(type)) {
			super.logger.info("LoginFundTwoActivity onLoginFundBuyEvent fund type is has buy, playerId: {}, giftId:{}, type:{}",playerId, giftId, type);
			return;
		}
		//记录该类型基金购买数据
		entity.addBuyType(type);
		// 添加该类型对应的成就数据项
		ConfigIterator<LoginFundActivityTwoAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(LoginFundActivityTwoAchieveCfg.class);
		List<AchieveItem> items = new ArrayList<>();
		while (configIterator.hasNext()) {
			LoginFundActivityTwoAchieveCfg config = configIterator.next();
			//找到对应此类型基金的任务成就
			if (config.getFundType() == type){
				AchieveItem item = AchieveItem.valueOf(config.getAchieveId());
				entity.addItem(item);
				items.add(item);
			}
		}
		
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, items), true);
		//购买之后开始记录天数
		entity.recordLoginDay();
		ActivityManager.getInstance().postEvent(new LoginDayFundTwoEvent(playerId), true);

		ActivityManager.getInstance().postEvent(new BuyFundEvent(playerId,this.getActivityType().intValue(), type));
		syncActivityDataInfo(playerId);
		// 流水记录
		getDataGeter().buyFundRecord(playerId, getActivityType());
		//此基金有三种类型,,单独记录一份tlog
		getDataGeter().buyLoginFundTwoRecord(playerId, getActivityTermId(), type);
	}

	/**
	 * 玩家是否购买登录基金
	 * @param playerId
	 * @return
	 */
	public boolean isHasBuyAnyType(String playerId){
		Optional<LoginFundTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return false;
		}
		LoginFundTwoEntity entity = opEntity.get();
		return entity.isHasBuyAnyType();
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<LoginFundTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		LoginFundTwoEntity entity = opEntity.get();
		entity.recordLoginDay();
		boolean isBuy = entity.isHasBuyAnyType();
		if (isBuy) {
			ActivityManager.getInstance().postEvent(new LoginDayFundTwoEvent(playerId), true);
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, GameConst.MsgId.LOGIN_FUND_DATA_INIT, () -> {
				Optional<LoginFundTwoEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					return;
				}
				LoginFundTwoEntity entity = opEntity.get();
				entity.recordLoginDay();
				boolean isRecord = recordOpenFacLv(playerId);
				if (isRecord){
					syncActivityDataInfo(playerId);
				}
			});
		}
	}


	@Override
	public void onPlayerLogin(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		// 登录检测
		recordOpenFacLv(playerId);
	}

	@Subscribe
	public void onEvent(BuildingLevelUpEvent event) {
		//非主堡不处理
		if(event.getBuildType() != Const.BuildingType.CONSTRUCTION_FACTORY_VALUE){
			return;
		}
		String playerId = event.getPlayerId();
		// 活动开启,记录开启初始化的大本等级, 如果首次,则同步活动消息
		if (!isOpening(playerId)) {
			return;
		}
		boolean isRecord = recordOpenFacLv(playerId);
		if (isRecord){
			this.syncActivityStateInfo(playerId);
			this.syncActivityDataInfo(playerId);
		}
	}

	/**
	 * 记录开启活动时,记录的大本等级
	 * @param playerId
	 */
	public boolean recordOpenFacLv(String playerId){
		if (!isOpening(playerId)) {
			return false;
		}
		Optional<LoginFundTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		LoginFundTwoEntity entity = opEntity.get();
		if (entity.getFacLv() <= 0){
			int facLv = this.getDataGeter().getConstructionFactoryLevel(playerId);
			entity.setFacLv(facLv);
			return true;
		}
		return false;
	}
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(LoginFundActivityTwoAchieveCfg.class, achieveId);
	}

	@Override
	public boolean isActivityClose(String playerId) {
		//大本等级 优先判断
		int facLv = this.getDataGeter().getConstructionFactoryLevel(playerId);
		LoginFundActivityTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginFundActivityTwoKVCfg.class);
		//小于大本等级限制,不开活动
		if (facLv < cfg.getOpenCtiyLimt()) {
			return true;
		}
		Optional<LoginFundTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		LoginFundTwoEntity entity = opEntity.get();
		//判断三个基金是否都买了,,有没买的就不关活动
		if (!entity.isHasAllBuy()) {
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


}
