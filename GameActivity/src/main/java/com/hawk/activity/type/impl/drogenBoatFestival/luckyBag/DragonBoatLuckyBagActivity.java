package com.hawk.activity.type.impl.drogenBoatFestival.luckyBag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DragonBoatLuckyBagOpenEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.drogenBoatFestival.luckyBag.cfg.DragonBoatLuckBagExchangeCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.luckyBag.cfg.DragonBoatLuckyBagAchieveCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.luckyBag.cfg.DragonBoatLuckyBagKVCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.luckyBag.entity.DragonBoatLuckyBagEntity;
import com.hawk.game.protocol.Activity.DragonBoatLuckyBagResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 端午兑换
 * @author che
 *
 */
public class DragonBoatLuckyBagActivity extends ActivityBase  implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public DragonBoatLuckyBagActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DRAGON_BOAT_LUCKY_BAG_ACTIVITY;
	}

	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DragonBoatLuckyBagActivity activity = new DragonBoatLuckyBagActivity(
				config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DragonBoatLuckyBagEntity> queryList = HawkDBManager.getInstance()
				.query("from DragonBoatLuckyBagEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DragonBoatLuckyBagEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DragonBoatLuckyBagEntity entity = new DragonBoatLuckyBagEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.DRAGON_BOAT_LUCKY_BAG_INIT, () -> {
				this.initAchieveInfo(playerId);
				this.syncActivityDataInfo(playerId);
			});
		}
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<DragonBoatLuckyBagEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<DragonBoatLuckyBagEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		DragonBoatLuckyBagEntity entity = opPlayerDataEntity.get();
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		//重置分享记录
		entity.setOpenCount(0);
		List<AchieveItem> addList = new CopyOnWriteArrayList<AchieveItem>();
		ConfigIterator<DragonBoatLuckyBagAchieveCfg> achieveIt = HawkConfigManager.getInstance()
				.getConfigIterator(DragonBoatLuckyBagAchieveCfg.class);
		while (achieveIt.hasNext()) {
			DragonBoatLuckyBagAchieveCfg achieveCfg = achieveIt.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			addList.add(item);
		}
		entity.resetItemList(addList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, addList), true);
		//推送新数据
		syncActivityInfo(playerId, entity);
		logger.info("DragonBoatLuckBagActivity onContinueLogin resetOpenCount playerId:{}",playerId);
	}
	
	
	/**
	 * 道具兑换
	 * @param playerId
	 * @param protolType
	 */
	public void openLuckBag(String playerId,int openCount,int protocolType){
		DragonBoatLuckyBagKVCfg config = HawkConfigManager.getInstance().
				getKVInstance(DragonBoatLuckyBagKVCfg.class);
		if (config == null) {
			return;
		}
		Optional<DragonBoatLuckyBagEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		DragonBoatLuckyBagEntity entity = opDataEntity.get();
		int count = entity.getOpenCount() + openCount;
		if(count > config.getBuyLimit()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.Error.DRAGON_BOAT_LUCKY_BAG_OPEN_LIMIT_VALUE);
			logger.info("DragonBoatLuckyBag,openLuckBag,fail,countless,playerId:{},openCount{},count:{}",
					playerId,openCount,entity.getOpenCount());
			return;
		}
		List<RewardItem.Builder> sourceList = RewardHelper.toRewardItemList(config.getCost());
		//扣金条
		boolean flag = this.getDataGeter().cost(playerId, sourceList, openCount,  Action.DRAGON_BOAT_LCUK_BAG_OPEN_COST, true);
		if (!flag) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		List<RewardItem.Builder> list =new ArrayList<RewardItem.Builder>();
		for(int i=0;i<openCount;i++){
			int awardCount = this.getRandomCount();	
			RewardItem.Builder award = RewardHelper.toRewardItem(config.getGain());
			if(award == null){
				return;
			}
			award.setItemCount(awardCount);
			list.add(award);
		}
		//增加次数
		entity.addOpenCount(openCount);
		//发奖励
		if (!list.isEmpty()) {
			ActivityReward reward = new ActivityReward(list, Action.DRAGON_BOAT_LCUK_BAG_OPEN_AWARD);
			reward.setOrginType(RewardOrginType.DRAGON_BOAT_LUCKY_BAG_OPEN_REWARD, getActivityId());
			reward.setAlert(true);
			postReward(playerId, reward, false);
		}
		//抛出事件
		ActivityManager.getInstance().postEvent(new DragonBoatLuckyBagOpenEvent(playerId, openCount));
		//同步
		this.syncActivityInfo(playerId,entity);
		//日志记录
		int termId = this.getActivityTermId();
		this.getDataGeter().logDragonBoatLuckyBagOpen(playerId, termId, openCount);
		logger.info("DragonBoatLuckyBag,openLuckBag,sucess,playerId:{},openCount{},count:{}",
				playerId,openCount,entity.getOpenCount());
		
	}
	
	
	/***
	 * 随机数量
	 * @param ita
	 * @return
	 */
	private int getRandomCount(){
		ConfigIterator<DragonBoatLuckBagExchangeCfg> configItrator = HawkConfigManager.
				getInstance().getConfigIterator(DragonBoatLuckBagExchangeCfg.class);
		Map<DragonBoatLuckBagExchangeCfg, Integer> map = new HashMap<>();
		while(configItrator.hasNext()){
			DragonBoatLuckBagExchangeCfg cfg = configItrator.next();
			map.put(cfg, cfg.getRate());
		}
		DragonBoatLuckBagExchangeCfg chose = HawkRand.randomWeightObject(map);
		if(chose == null){
			throw new RuntimeException("can not found SuperGoldCfg:" + map);
		}
		int low = chose.getLowLimit();
		int hight = chose.getHighLimit();
		try {
			int value = HawkRand.randInt(low,hight);
			return value;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return low;
	}


	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<DragonBoatLuckyBagEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		DragonBoatLuckyBagEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		Iterator<DragonBoatLuckyBagAchieveCfg> ite = HawkConfigManager.getInstance().getConfigIterator(DragonBoatLuckyBagAchieveCfg.class);
		List<AchieveItem> alist = new CopyOnWriteArrayList<AchieveItem>();
		while(ite.hasNext()){
			DragonBoatLuckyBagAchieveCfg config = ite.next();
			AchieveItem item = AchieveItem.valueOf(config.getAchieveId());
			alist.add(item);
			
		}
		entity.resetItemList(alist);
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), entity.getItemList()), true);
	}
	
	
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId,DragonBoatLuckyBagEntity entity){
		int openCount = entity.getOpenCount();
		DragonBoatLuckyBagResp.Builder builder = DragonBoatLuckyBagResp.newBuilder();
		builder.setOpenCount(openCount);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.DRAGON_BOAT_LUCKY_BAG_INFO_RESP, builder));
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
		Optional<DragonBoatLuckyBagEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		DragonBoatLuckyBagEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().
				getConfigByKey(DragonBoatLuckyBagAchieveCfg.class, achieveId);
		return config;
	}

	@Override
	public Action takeRewardAction() {
		return Action.DRAGON_BOAT_LCUK_BAG_ACHIEVE_AWARD;
	}
	
	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		logger.info("DragonBoatLuckyBag,onAchieveFinished playerId:{},achieveId{}",
				playerId,achieveItem.getAchieveId());
		return AchieveProvider.super.onAchieveFinished(playerId, achieveItem);
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		logger.info("DragonBoatLuckyBag,onTakeReward,playerId: "
				+ "{},achieveId:{}", playerId,achieveId);
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
}
