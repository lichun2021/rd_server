package com.hawk.activity.type.impl.dressuptwo.christmasrecharge;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.DiamondRechargeEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.dressuptwo.christmasrecharge.cfg.ChristmasRechargeAchieveCfg;
import com.hawk.activity.type.impl.dressuptwo.christmasrecharge.entity.ChristmasRechargeEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 累积充值活动
 * @author Jesse
 *
 */
public class ChristmasRechargeActivity extends ActivityBase implements AchieveProvider {
	private static final Logger logger = LoggerFactory.getLogger("Server");
	public ChristmasRechargeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
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
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_CHRISTMAS_RECHARGE, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<ChristmasRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ChristmasRechargeEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<ChristmasRechargeAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ChristmasRechargeAchieveCfg.class);
		while (configIterator.hasNext()) {
			ChristmasRechargeAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}

		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<ChristmasRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		ChristmasRechargeEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public ChristmasRechargeAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(ChristmasRechargeAchieveCfg.class, achieveId);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.CHRISTMAS_RECHARGE_ACTIVITY;
	}
	
	@Override
	public Action takeRewardAction() {
		return Action.CHRISTMAS_RECHARGE_REWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ChristmasRechargeActivity activity = new ChristmasRechargeActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ChristmasRechargeEntity> queryList = HawkDBManager.getInstance()
				.query("from ChristmasRechargeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ChristmasRechargeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ChristmasRechargeEntity entity = new ChristmasRechargeEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<ChristmasRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ChristmasRechargeEntity entity = opEntity.get();
		syncActivityDataInfo(playerId, entity);
	}

	@Subscribe
	public void onDiamondRechargeEvent(DiamondRechargeEvent event) {
		String playerId = event.getPlayerId();
		Optional<ChristmasRechargeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ChristmasRechargeEntity entity = opEntity.get();
		int rechargeDiamond = event.getDiamondNum();
		entity.addRechargeDiamond(rechargeDiamond);

		logger.info("ChristmasRechargeActivity,onDiamondRechargeEvent,playerId:{},goodId:{},rechargeDiamond:{}, totalRechargeDiamond:{}",
				playerId, event.getGoodsId(), rechargeDiamond, entity.getRechargeDiamond());

		this.syncActivityDataInfo(playerId,entity);
		int termId = this.getActivityTermId();
		//log 打点
		this.getDataGeter().logChristmasRechargeDiamond(playerId, termId, rechargeDiamond, entity.getRechargeDiamond());

	}

	/**
	 * 活动消息同步
	 * @param playerId
	 * @param entity
	 */
	public void syncActivityDataInfo(String playerId, ChristmasRechargeEntity entity){
		Activity.ChristmasRechargeInfoSync.Builder builder = Activity.ChristmasRechargeInfoSync.newBuilder();
		int rechargeDiamond = entity.getRechargeDiamond();
		builder.setRechargeDiamond(rechargeDiamond);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.CHRISTMAS_RECHARGE_INFO_SYNC, builder));
	}
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
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

	@Override
	public List<Builder> getRewardList(String playerId, AchieveConfig achieveConfig) {
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		
		ChristmasRechargeAchieveCfg cfg = (ChristmasRechargeAchieveCfg)achieveConfig;
		RewardItem.Builder goldItem = RewardItem.newBuilder();
		goldItem.setItemType(ItemType.PLAYER_ATTR_VALUE * GameConst.ITEM_TYPE_BASE);
		goldItem.setItemId(PlayerAttr.DIAMOND_VALUE);
		int count = cfg.randDiamod();
		goldItem.setItemCount(count);
		rewardList.add(goldItem);
		
		rewardList.addAll(achieveConfig.getRewardList());
		
		getDataGeter().sendMail(playerId, MailId.TIME_LIMIT_RECHARGE_GOLD, null, null, new Object[] { count }, rewardList, true);
		return rewardList;
	}
}
