package com.hawk.activity.type.impl.heroSkin;

import java.util.ArrayList;
import java.util.Collection;
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
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DiamondRechargeEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.heroSkin.cfg.HeroSkinAchieveCfg;
import com.hawk.activity.type.impl.heroSkin.cfg.HeroSkinActivityKVCfg;
import com.hawk.activity.type.impl.heroSkin.entity.HeroSkinEntity;
import com.hawk.game.protocol.Activity.PBHeroSkinInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 英雄皮肤活动
 * @author golden
 *
 */
public class HeroSkinActivity extends ActivityBase implements AchieveProvider {

	public final Logger logger = LoggerFactory.getLogger("Server");
	
	public HeroSkinActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.HERO_SKIN;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		HeroSkinActivity activity = new HeroSkinActivity(config.getActivityId(), activityEntity);
		// 加入成就管理器
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		sync(playerId);
	}
	
	/** 跨天事件
	 * 
	 * @param event */
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<HeroSkinEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		HeroSkinEntity entity = opPlayerDataEntity.get();
		if (entity.hasFinally()) {
			return;
		}
		entity.setRefreshTimes(0);
	}

	public void sync(String playerId) {
		Optional<HeroSkinEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		HeroSkinEntity entity = opEntity.get();
		int openCount = (int) entity.getItemsList().stream().filter(i -> i > 0).count();
		HeroSkinActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HeroSkinActivityKVCfg.class);
		PBHeroSkinInfo.Builder resp = PBHeroSkinInfo.newBuilder();
		resp.setPool(entity.getPool());
		resp.addAllRewardId(entity.getItemsList());
		resp.setRefreshTimes(entity.getRefreshTimes());
		resp.setMaxRefresh(kvCfg.getMaxRefresh());
		if (openCount < 9) {
			resp.setOpenCost(kvCfg.getTreasureCost(openCount));
		}
		if (entity.getRefreshTimes() < kvCfg.getMaxRefresh()) {
			resp.setRefreshCost(kvCfg.getRefreshCost(entity.getRefreshTimes()));
		}
		resp.setHasFinally(entity.hasFinally());
		pushToPlayer(playerId, HP.code.HERO_SKIN_INFO_S_VALUE, resp);

	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<HeroSkinEntity> queryList = HawkDBManager.getInstance()
				.query("from HeroSkinEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			HeroSkinEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		HeroSkinEntity entity = new HeroSkinEntity(playerId, termId);
		entity.resetItems();
		return entity;
	}

	/**
	 * 充值事件，活动期间充值，1元给一个物品
	 * @param event
	 */
	@Subscribe
	public void onEvent(DiamondRechargeEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<HeroSkinEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		HeroSkinActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HeroSkinActivityKVCfg.class);
		if (cfg == null) { 
			return;
		}
		
		RewardItem.Builder cfgReward = cfg.getGetItemList();
		int itemId = cfgReward.getItemId();
		int num = event.getDiamondNum() / 10;
		long itemNum = cfgReward.getItemCount() * num;
		RewardItem.Builder reward = RewardItem.newBuilder();
		reward.setItemId(itemId);
		reward.setItemCount(itemNum);
		reward.setItemType(cfgReward.getItemType());
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		rewardList.add(reward);
		// 邮件发送奖励
		Object[] content;
		content = new Object[1];
		content[0] = getActivityCfg().getActivityName();
		Object[] title = new Object[0];
		Object[] subTitle = new Object[0];
		//发奖
		this.getDataGeter().takeReward(playerId, rewardList, 1,  Action.HERO_SKIN_RECHARGE, false, RewardOrginType.HERO_SKIN_RECHARGE_REWARD);
		//发邮件
		sendMailToPlayer(playerId, MailConst.MailId.HERO_SKIN_RECHARGE, title, subTitle, content, rewardList, true);
		logger.info("HeroSkinActivity sendMail addItems from DiamondRechargeEvent ItemId:{}, num:{}", itemId, itemNum);
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
	}

	@Override
	public void onTick() {
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

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
		Optional<HeroSkinEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		HeroSkinEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(HeroSkinAchieveCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.HERO_ACHIEVE_AWARD;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_HERO_SKIN_ACHIEVE, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}

	/**
	 * 初始化成就信息
	 */
	private void initAchieveInfo(String playerId) {
		Optional<HeroSkinEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		HeroSkinEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<HeroSkinAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(HeroSkinAchieveCfg.class);
		while (configIterator.hasNext()) {
			HeroSkinAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}

		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}
}
