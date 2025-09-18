package com.hawk.activity.type.impl.coreplate;

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
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.RandomHeroEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.coreplate.cfg.CoreplateAchieveCfg;
import com.hawk.activity.type.impl.coreplate.cfg.CoreplateActivityKVConfig;
import com.hawk.activity.type.impl.coreplate.entity.CoreplateActivityEntity;
import com.hawk.game.protocol.Activity.CorePlateActivityInfoResp;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class CoreplateActivity extends ActivityBase implements AchieveProvider{
	public final Logger logger = LoggerFactory.getLogger("Server");
	
	public CoreplateActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.COREPLATE_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		CoreplateActivity activity = new CoreplateActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<CoreplateActivityEntity> queryList = HawkDBManager.getInstance().query("from CoreplateActivityEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0 ) {
			CoreplateActivityEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}
	

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_COREPLATE, ()-> {
				boolean rlt = this.initData(playerId);
				if(rlt){
					this.syncActivityDataInfo(playerId);
				}
			});
		}
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		CoreplateActivityEntity entity = new CoreplateActivityEntity(playerId, termId);
		return entity;
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
		Optional<CoreplateActivityEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		CoreplateActivityEntity entity = optional.get();
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	
	@Override
	public void onPlayerLogin(String playerId) {
		this.initData(playerId);
	}
	
	/**初始化成就
	 * @param playerId
	 */
	private boolean initData(String playerId) {
		Optional<CoreplateActivityEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return false;
		}
		CoreplateActivityEntity entity = optional.get();
		if(entity.getCityLevel() > 0){
			return false;
		}
		CoreplateActivityKVConfig kvConfig = HawkConfigManager.getInstance().getKVInstance(CoreplateActivityKVConfig.class);
		int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
		if(cityLevel < kvConfig.getBuildLevelLimit()){
			return false;
		}
		entity.setCityLevel(cityLevel);
		ConfigIterator<CoreplateAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(CoreplateAchieveCfg.class);
		List<AchieveItem> list = new ArrayList<>();
		while (iterator.hasNext()) {
			CoreplateAchieveCfg cfg = iterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			list.add(item);
		}
		entity.setItemList(list);
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
		return true;
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		CoreplateAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CoreplateAchieveCfg.class, achieveId);
		return cfg;
	}
	
	@Override
	public boolean isActivityClose(String playerId) {
		CoreplateActivityKVConfig kvConfig = HawkConfigManager.getInstance().getKVInstance(CoreplateActivityKVConfig.class);
		int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
		if(cityLevel < kvConfig.getBuildLevelLimit()){
			return true;
		}
		return false;
	}

	
	@Override
	public boolean isHidden(String playerId) {
		return super.isHidden(playerId);
	}
	
	@Override
	public Action takeRewardAction() {
		return Action.COREPLATE_TASK_REWARD;
	}
	
	@Subscribe
	public void onCityLevelUp(BuildingLevelUpEvent event){
		//不是主城不管
		if(event.getBuildType() != BuildingType.CONSTRUCTION_FACTORY_VALUE){
			return;
		}
		boolean init = this.initData(event.getPlayerId());
		if(init){
			PlayerPushHelper.getInstance().syncActivityStateInfo(event.getPlayerId(), this);
			this.syncActivityDataInfo(event.getPlayerId());
		}
	}
	
	
	/**
	 * 处理芯片抽取
	 * @param event
	 */
	@Subscribe
	public void onCoreRandom(RandomHeroEvent event){
		if(event.getGachaType() == GachaType.SKILL_ONE_VALUE ||
				event.getGachaType() == GachaType.SKILL_TEN_VALUE){
			if(this.isHidden(event.getPlayerId())){
				return;
			}
			Optional<CoreplateActivityEntity> optional = this.getPlayerDataEntity(event.getPlayerId());
			if (!optional.isPresent()) {
				return;
			}
			CoreplateActivityEntity entity = optional.get();
			if(entity.getCityLevel() <= 0){
				return;
			}
			int count = entity.getCoreplateTimes() + event.getCount();
			entity.setCoreplateTimes(count);
			this.syncActivityDataInfo(entity);
		}
	}
	
	/**
	 * 开箱子
	 */
	public void achiveBox(String playerId){
		if(this.isHidden(playerId)){
			return;
		}
		Optional<CoreplateActivityEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		CoreplateActivityEntity entity = optional.get();
		if(entity.getCityLevel() <= 0){
			return;
		}
		CoreplateActivityKVConfig kvConfig = HawkConfigManager.getInstance().getKVInstance(CoreplateActivityKVConfig.class);
		int score = this.getScore(entity);
		if(score < kvConfig.getBoxScore()){
			return;
		}
		//记录+1
		int boxTime = entity.getBoxAchieveTimes() + 1;
		entity.setBoxAchieveTimes(boxTime);
		//奖励
		List<RewardItem.Builder> signRewards = this.getBoxRewardItems();
		this.getDataGeter().takeReward(playerId, signRewards, 1,
				Action.COREPLATE_BOX_REWARD,true, RewardOrginType.ACTIVITY_REWARD);
		//同步
		this.syncActivityDataInfo(entity);
		//Tlog
		this.getDataGeter().logCoreplateBox(playerId, entity.getTermId(), entity.getBoxAchieveTimes());
	}
	
	/**
	 * 箱子奖励
	 * @return
	 */
	public List<RewardItem.Builder> getBoxRewardItems(){
		CoreplateActivityKVConfig kvConfig = HawkConfigManager.getInstance().getKVInstance(CoreplateActivityKVConfig.class);
		List<String> rewardList = this.getDataGeter().getAwardFromAwardCfg(kvConfig.getBoxAwardId());
		List<RewardItem.Builder> rewardItemList = new ArrayList<>();
		for (String rewardStr : rewardList) {
			List<RewardItem.Builder> rewardBuilders = RewardHelper.toRewardItemImmutableList(rewardStr);
			rewardItemList.addAll(rewardBuilders);
		}
		return rewardItemList;
	}
	
	
	/**
	 * 计算积分
	 * @param entity
	 * @return
	 */
	public int getScore(CoreplateActivityEntity entity){
		CoreplateActivityKVConfig kvConfig = HawkConfigManager.getInstance().getKVInstance(CoreplateActivityKVConfig.class);
		int score = entity.getCoreplateTimes() * kvConfig.getCoreplateSocre() 
				- entity.getBoxAchieveTimes() * Math.max(1, kvConfig.getBoxScore());
		return Math.max(0,score);
	}
	
	
	/**
	 * 获取剩余抽取次数
	 * @param entity
	 * @return
	 */
	public int getAchiveBoxCount(CoreplateActivityEntity entity){
		CoreplateActivityKVConfig kvConfig = HawkConfigManager.getInstance().getKVInstance(CoreplateActivityKVConfig.class);
		int score = this.getScore(entity);
		int count = score / Math.max(1, kvConfig.getBoxScore());
		return count;
	}
	
	/**
	 * 同步信息
	 * @param entity
	 */
	private void syncActivityDataInfo(CoreplateActivityEntity entity){
		CorePlateActivityInfoResp.Builder builder = CorePlateActivityInfoResp.newBuilder();
		builder.setLotteryTimes(this.getAchiveBoxCount(entity));
		builder.setCoreRandomTimes(entity.getCoreplateTimes());
		pushToPlayer(entity.getPlayerId(), HP.code.COREPLATE_ACTIVITY_INFO_RESP_VALUE, builder);
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<CoreplateActivityEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		CoreplateActivityEntity entity = optional.get();
		this.syncActivityDataInfo(entity);
	}
	
}
