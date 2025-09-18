package com.hawk.activity.type.impl.rechargeGift;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.RechargeMoneyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.rechargeGift.cfg.RechargeGiftAchieveCfg;
import com.hawk.activity.type.impl.rechargeGift.entity.RechargeGiftEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.RechargeGiftDataSync;
import com.hawk.log.Action;

/***
 * 充值豪礼
 * @author yang.rao
 *
 */
public class RechargeGiftActivity extends ActivityBase implements AchieveProvider{
	
	static Logger logger = LoggerFactory.getLogger("Server");

	public RechargeGiftActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.RECHARGE_GIFT_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<RechargeGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		RechargeGiftEntity entity = opEntity.get();
		syncActivityDataInfo(entity);
	}
	
	/***
	 * 监听玩家充值事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(RechargeMoneyEvent event){
		String playerId = event.getPlayerId();
		if(isInit(playerId)){
			initAchieveInfo(playerId);
			//同步活动开启的消息
			PlayerPushHelper.getInstance().syncActivityStateInfo(playerId, this);
		}
		
		updateAchieve(event);
		Optional<RechargeGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (opEntity.isPresent()) {
			RechargeGiftEntity entity = opEntity.get();
			entity.setRechargeTotal(entity.getRechargeTotal() + event.getMoney());
			syncActivityDataInfo(entity);
		}
	}
	
	/**
	 * 同步活动数据
	 * 
	 * @param entity
	 */
	private void syncActivityDataInfo(RechargeGiftEntity entity) {
		RechargeGiftDataSync.Builder builder = RechargeGiftDataSync.newBuilder();
		builder.setRechargeTotal(entity.getRechargeTotal() / 10);
		pushToPlayer(entity.getPlayerId(), HP.code.RECHARGE_GIFT_DATA_SYNC_S_VALUE, builder);
	}
	
	private void updateAchieve(RechargeMoneyEvent event){
		String playerId = event.getPlayerId();
		List<AchieveParser<?>> parsers = AchieveContext.getParser(event.getClass());
		if (parsers == null || parsers.isEmpty()) {
			return;
		}
		List<AchieveItem> needPush = new ArrayList<>();
		
		for (AchieveParser<?> parser : parsers) {
			List<AchieveProvider> providers = AchieveContext.getProviders();
			for (AchieveProvider provider : providers) {
				if(provider != this){
					continue;
				}
				Optional<AchieveItems> opAchieveItems = provider.getAchieveItems(playerId);
				if (!opAchieveItems.isPresent()) {
					continue;
				}
				AchieveItems achieveItems = opAchieveItems.get();
				if(achieveItems.getItems().isEmpty()){
					continue;
				}
				boolean update = false;
				for (AchieveItem achieveItem : achieveItems.getItems()) {
					// 更新具体成就数值和状态
					AchieveConfig achieveConfig = getAchieveConfig(achieveItem.getAchieveId());
					if (achieveConfig == null) {
						logger.error("achieve config not found, achieveId: {}", achieveItem.getAchieveId());
						continue;
					}
					if (achieveConfig.getAchieveType() != parser.geAchieveType()) {
						continue;
					}
					parser.updateAchieveData(achieveItem, achieveConfig, event, needPush);
					update = true;
				}
				if (update) {
					achieveItems.getEntity().notifyUpdate();
				}
			}
		}
		
		if (!needPush.isEmpty()) {
			AchievePushHelper.pushAchieveUpdate(playerId, needPush);
		}
	}
	
	private AchieveConfig getAchieveConfig(int achieveId) {
		List<AchieveProvider> providerList = AchieveContext.getProviders();
		for (AchieveProvider achieveProvider : providerList) {
			AchieveConfig config = achieveProvider.getAchieveCfg(achieveId);
			if (config != null) {
				return config;
			}
		}
		return null;
	}

	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<RechargeGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		RechargeGiftEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<RechargeGiftAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(RechargeGiftAchieveCfg.class);
		while (configIterator.hasNext()) {
			RechargeGiftAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RechargeGiftActivity activity =  new RechargeGiftActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RechargeGiftEntity> queryList = HawkDBManager.getInstance()
				.query("from RechargeGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			RechargeGiftEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RechargeGiftEntity entity = new RechargeGiftEntity(playerId, termId);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	@Override
	public boolean isProviderActive(String playerId) {
		//return isOpening(playerId);
		return false;
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return !isHidden(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<RechargeGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		RechargeGiftEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(RechargeGiftAchieveCfg.class, achieveId);
	}

	@Override
	public void onTakeRewardSuccess(String playerId) {
		checkActivityClose(playerId);
	}

	/***
	 * 是否可以初始化
	 * @param playerId
	 * @return true:可以初始化 false:不能初始化
	 */
	private boolean isInit(String playerId) {
		// 活动是否失效
		if(isInvalid()){
			return false;
		}
		
		Optional<RechargeGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		RechargeGiftEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isHidden(String playerId) {
		return isActivityClose(playerId);
	}

	@Override
	public boolean isActivityClose(String playerId) {		
		Optional<RechargeGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return true;
		}
		RechargeGiftEntity entity = opEntity.get();
		List<AchieveItem> itemList = entity.getItemList();
		if(itemList == null || itemList.isEmpty()){ //没有初始化，即表示活动没有开启
			return true;
		}
		boolean isInit = false; 
		boolean haveReward = false; //是否有奖励未被领取
		if (itemList != null && !itemList.isEmpty()) {
			isInit = true;
		}
		for (AchieveItem item : entity.getItemList()) {
			if (item.getState() != AchieveState.TOOK_VALUE) {
				haveReward = true;
			}
		}
		//活动关闭的条件为:被初始化过，并且奖励全部都被领取了。		
		return (isInit && !haveReward);
	}

	@Override
	public Action takeRewardAction() {
		return Action.RECHARGE_GIFT;
	}
}
