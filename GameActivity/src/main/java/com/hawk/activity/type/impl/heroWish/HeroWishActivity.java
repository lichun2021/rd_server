package com.hawk.activity.type.impl.heroWish;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayHeroWishEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.heroWish.cfg.HeroWishAchieveCfg;
import com.hawk.activity.type.impl.heroWish.cfg.HeroWishKVCfg;
import com.hawk.activity.type.impl.heroWish.cfg.HeroWishRewardCfg;
import com.hawk.activity.type.impl.heroWish.cfg.HeroWishTimeCfg;
import com.hawk.activity.type.impl.heroWish.entity.HeroWishEntity;
import com.hawk.game.protocol.Activity.PBHeroWishInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 7夕相遇
 * @author che
 *
 */
public class HeroWishActivity extends ActivityBase  implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public HeroWishActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.HERO_WISH_ACTIVITY;
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		HeroWishActivity activity = new HeroWishActivity(
				config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<HeroWishEntity> queryList = HawkDBManager.getInstance()
				.query("from HeroWishEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			HeroWishEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		HeroWishEntity entity = new HeroWishEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public boolean isProviderActive(String playerId) {
		boolean open =  isOpening(playerId);
		if(!open){
			return open;
		}
		if(this.inAchieveTime()){
			return false;
		}
		return true;
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isShow(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<HeroWishEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		HeroWishEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId,entity);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	
	//初始化成就
	private void initAchieve(String playerId,HeroWishEntity entity){
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<HeroWishAchieveCfg> configIterator = HawkConfigManager.getInstance().
				getConfigIterator(HeroWishAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			HeroWishAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
	}
		
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.HERO_WISH_INIT, () -> {
				Optional<HeroWishEntity>  optional = this.getPlayerDataEntity(playerId);
				if (!optional.isPresent()) {
					return;
				}
				HeroWishEntity entity = optional.get();
				this.initAchieve(playerId,entity);
				syncActivityDataInfo(playerId);
				//登陆天数
				entity.recordLoginDay();
				ActivityManager.getInstance().postEvent(new LoginDayHeroWishEvent(playerId, entity.getLoginDaysCount()), true);
			});
		}
	}
	
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
        if (!isOpening(playerId)) {
            return;
        }
        //活动数据不存在
        Optional<HeroWishEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        HeroWishEntity entity =  opEntity.get();
        entity.recordLoginDay();
        ActivityManager.getInstance().postEvent(new LoginDayHeroWishEvent(playerId, entity.getLoginDaysCount()), true);
	}
	
	
	/**
	 * 选择
	 * @param playerId
	 */
	public void chooseHero(String playerId,int chooseId){
		Optional<HeroWishEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		HeroWishEntity entity = optional.get();
		if (entity.getChooseId() > 0) {
			return;
		}
		HeroWishRewardCfg cfg = HawkConfigManager.getInstance()
				.getConfigByKey(HeroWishRewardCfg.class, chooseId);
		if(cfg == null){
			return;
		}
		entity.setAddCount(cfg.getChooseAdd());
		entity.setChooseId(chooseId);
		int termId = this.getActivityTermId();
		this.getDataGeter().logHeroWishChoose(playerId, termId, chooseId);
		this.syncActivityInfo(playerId, entity);
		logger.info("HeroWishActivity chooseHero, playerId: {},chooseId:{}",playerId,chooseId);
	}
	
	
	/**
	 * 祈福
	 * @param playerId
	 * @param questionId
	 * @param answerId
	 */
	public void wishHero(String playerId){
		Optional<HeroWishEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		HeroWishEntity entity = optional.get();
		if (entity.getChooseId() <= 0) {
			return;
		}
		int chooseId = entity.getChooseId();
		HeroWishRewardCfg heroCfg = HawkConfigManager.getInstance()
				.getConfigByKey(HeroWishRewardCfg.class, chooseId);
		if(heroCfg == null){
			return;
		}
		HeroWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HeroWishKVCfg.class);
		List<RewardItem.Builder> costItemList = cfg.getWishCostItemList();
		int wishCount = -1;
		for(RewardItem.Builder costItem:costItemList){
			int itemCount = this.getDataGeter().getItemNum(playerId, costItem.getItemId());
			int tempCount = (int) (itemCount / costItem.getItemCount());
			if(wishCount < 0){
				wishCount = tempCount; 
			}
			if(tempCount < wishCount){
				wishCount = tempCount;
			}
		}
		if(wishCount <= 0){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					HP.code2.HERO_WISH_ADD_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		for(RewardItem.Builder costItem:costItemList){
			costItem.setItemCount(costItem.getItemCount() * wishCount);
		}
		boolean cost = this.getDataGeter().cost(playerId,costItemList, 1, Action.HERO_WISH_COST,false);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					HP.code2.HERO_WISH_ADD_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		int curCount = entity.getAddCount();
		int count =  curCount+ wishCount * heroCfg.getWishAdd();
		entity.setAddCount(count);
		this.syncActivityInfo(playerId, entity);
		logger.info("HeroWishActivity wishHero, playerId: {},chooseId:{},addWish:{},before:{},after:{}",
				playerId,chooseId,wishCount,curCount,count);
	}
	
	
	
	/**
	 * 收获
	 * @param acfg
	 * @param favor
	 * @return
	 */
	public void achieveHero(String playerId){
		Optional<HeroWishEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		HeroWishEntity entity = optional.get();
		if (entity.getChooseId() <= 0) {
			return;
		}
		int chooseId = entity.getChooseId();
		HeroWishRewardCfg heroCfg = HawkConfigManager.getInstance()
				.getConfigByKey(HeroWishRewardCfg.class, chooseId);
		if(heroCfg == null){
			return;
		}
		if(!this.inAchieveTime()){
			return;
		}
		int count = entity.getAddCount();
		if(count <= 0){
			return;
		}
		List<RewardItem.Builder> rewardItemList = heroCfg.getRewardItemList();
		for(RewardItem.Builder rewardItem:rewardItemList){
			long rewardCount = rewardItem.getItemCount() * count;
			rewardItem.setItemCount(rewardCount);
		}
		int achieveCount = entity.getAchieveCount();
		entity.setAchieveCount(achieveCount + count);
		entity.setAddCount(0);
		//发奖励
		this.getDataGeter().takeReward(playerId, rewardItemList, 
				1, Action.HERO_WISH_REWARD, true);
		this.syncActivityInfo(playerId, entity);
		logger.info("HeroWishActivity achieveHero, playerId: {},chooseId:{},achieveCount:{}",
				playerId,chooseId,count);
	}
	
	/**
	 * 获取活动阶段
	 * @return
	 */
	public boolean inAchieveTime(){
		long curTime  =HawkTime.getMillisecond();
		long achieveTime = this.getActivityAchieveTime();
		if(curTime > achieveTime){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 获得收获时间点
	 * @return
	 */
	private long getActivityAchieveTime(){
		int termId = this.getActivityTermId();
		HeroWishTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(HeroWishTimeCfg.class, termId);
		return timeCfg.getAchieveTimeValue();
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<HeroWishEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId,HeroWishEntity entity){
		long achieveTime = this.getActivityAchieveTime();
		PBHeroWishInfoResp.Builder builder =PBHeroWishInfoResp.newBuilder();
		builder.setChooseId(entity.getChooseId());
		builder.setWishCount(entity.getAddCount());
		builder.setAchieve(0);
		builder.setAchieveTime(achieveTime);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.HERO_WISH_INFO_RESP,builder));
	}
	
	
	

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg =  HawkConfigManager.getInstance().
				getConfigByKey(HeroWishAchieveCfg.class, achieveId);
		return cfg;
	}

	@Override
	public Action takeRewardAction() {
		return Action.HERO_WISH_TASK_REWARD;
	}
	
	
}
