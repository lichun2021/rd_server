package com.hawk.activity.type.impl.plantweaponback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.PlantWeaponBackDrawEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.plantweaponback.cfg.PlantWeaponBackAchieveCfg;
import com.hawk.activity.type.impl.plantweaponback.cfg.PlantWeaponBackKVCfg;
import com.hawk.activity.type.impl.plantweaponback.cfg.PlantWeaponBackPoolCfg;
import com.hawk.activity.type.impl.plantweaponback.cfg.PlantWeaponBackShopCfg;
import com.hawk.activity.type.impl.plantweaponback.entity.PlantWeaponBackEntity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.PBPlantWeaponBackInfo;
import com.hawk.game.protocol.Activity.PWBShopInfo;
import com.hawk.game.protocol.Activity.PlantWeaponBackDrawResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

/***
 * 泰能超武返场活动
 * 
 * @author lating
 */
public class PlantWeaponBackActivity extends ActivityBase implements AchieveProvider, IExchangeTip<PlantWeaponBackShopCfg> {
	
	public PlantWeaponBackActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PLANT_WEAPON_BACK_360;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PlantWeaponBackActivity activity = new PlantWeaponBackActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PlantWeaponBackEntity> queryList = HawkDBManager.getInstance().query("from PlantWeaponBackEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PlantWeaponBackEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PlantWeaponBackEntity entity = new PlantWeaponBackEntity(playerId, termId);
		return entity;
	}

	 /**
     * 判断活动是否开启
     */
    public boolean isOpening(String playerId) {
    	boolean unlocked = this.getDataGeter().isManhattanFuncUnlocked(playerId);
    	if (!unlocked) {
    		return false;
    	}
    	
    	return super.isOpening(playerId);
    }
    
    public boolean isHidden(String playerId) {
    	boolean unlocked = this.getDataGeter().isManhattanFuncUnlocked(playerId);
    	if (!unlocked) {
    		return true;
    	}
    	
    	return super.isHidden(playerId);
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
		if (!isOpening(playerId)) {
			return Optional.empty();
		}
		Optional<PlantWeaponBackEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		PlantWeaponBackEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(PlantWeaponBackAchieveCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.PLANT_WEAPON_BACK_ACHIEVE;
	}
	
	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		PlantWeaponBackAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantWeaponBackAchieveCfg.class, achieveItem.getAchieveId());
		if (cfg.getBuff() > 0) {
			achieveItem.setState(AchieveState.TOOK_VALUE);
			Optional<PlantWeaponBackEntity> opEntity = getPlayerDataEntity(playerId);
			PlantWeaponBackEntity entity = opEntity.get();
			entity.setBuff(cfg.getBuff());
		}
		return Result.success();
	}
	
	@Override
	public void onOpen() {
		Set<String> onlinePlayers = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayers){
			if (!isOpening(playerId)) {
				continue;
			}
			callBack(playerId, GameConst.MsgId.PLANT_WEAPON_BACK_INIT, ()->{
				initAchieveItems(playerId);
				Optional<PlantWeaponBackEntity> opEntity = getPlayerDataEntity(playerId);
				PlantWeaponBackEntity entity = opEntity.get();
				entity.setDayTime(HawkTime.getMillisecond());
				syncActivityInfo(playerId);
			});
		}
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<PlantWeaponBackEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		PlantWeaponBackEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(playerId);
		}
		
		//每天重置数据
		if (!HawkTime.isToday(entity.getDayTime())) {
			entity.setDayTime(HawkTime.getMillisecond());
			entity.setFreeTimes(0);
		}
		
		syncActivityInfo(playerId);
	}

	/***
	 * 初始化成就
	 * @param playerId
	 * @return
	 */
	public void initAchieveItems(String playerId) {
		Optional<PlantWeaponBackEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		PlantWeaponBackEntity entity = opEntity.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		ConfigIterator<PlantWeaponBackAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PlantWeaponBackAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while (configIterator.hasNext()) {
			PlantWeaponBackAchieveCfg cfg = configIterator.next();				
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());				
			itemList.add(item);
		}
		entity.setItemList(itemList);
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}
	
	/**
	 * 抽奖
	 * @param type
	 * @return
	 */
	public int onDraw(String playerId, int type) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<PlantWeaponBackEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		PlantWeaponBackEntity entity = opEntity.get();
		
		PlantWeaponBackKVCfg constCfg = HawkConfigManager.getInstance().getKVInstance(PlantWeaponBackKVCfg.class);
		if (entity.getDrawTimes() >= constCfg.getMaxLotteryTimes()) {
			return Status.Error.PLANT_WEAPON_BACK_DRAW_LIMIT_VALUE;
		}
		
		int times = type > 0 ? 10 : 1;
		times = Math.min(times, constCfg.getMaxLotteryTimes() - entity.getDrawTimes());
		
		List<RewardItem.Builder> rewardListAll = new ArrayList<>();
		if (times > 1 || entity.getFreeTimes() >= constCfg.getDailyFreeTimes()) {
			int result = consume(playerId, times, rewardListAll);
			if (result != 0) {
				return result;
			}
		}
		
		entity.addDrawTimes(times);
		if (times == 1 && entity.getFreeTimes() < constCfg.getDailyFreeTimes()) {
			entity.addFreeTimes(1);
		}
		
		int swId = constCfg.getPlantWeaponId();
		//对应超武已经解锁了，或解锁道具已经够解锁了
		boolean exchange = getDataGeter().isPlantWeaponUnlocked(playerId, swId) || getDataGeter().isPlantWeaponUnlockItemEnough(playerId, swId);
		int exchangeTimes = 0;
		List<RewardItem.Builder> rewardListA = new ArrayList<>();
		List<RewardItem.Builder> rewardListB = new ArrayList<>();
		for (int i=0; i<times; i++) {
			PlantWeaponBackPoolCfg rewardCfg = PlantWeaponBackPoolCfg.randomRewardCfg(entity.getBuff());
			RewardItem.Builder builder = RewardHelper.toRewardItem(rewardCfg.getRewards());
			rewardListA.add(builder);
			if (HawkOSOperator.isEmptyString(rewardCfg.getReplaceRewards())) {
				rewardListB.add(builder);
				continue;
			}
			//将抽到的超武碎片转换成商店代币
			if (exchange) {
				exchangeTimes += 1;
				rewardListB.add(RewardHelper.toRewardItem(rewardCfg.getReplaceRewards()));
				continue;
			}
			
			exchange = true;
			rewardListB.add(builder);
		}
		
		rewardListAll.addAll(rewardListB);
		this.getDataGeter().takeReward(playerId, rewardListAll, 1, Action.PLANT_WEAPON_BACK_DRAW, false, RewardOrginType.ACTIVITY_REWARD);
		ActivityManager.getInstance().postEvent(new PlantWeaponBackDrawEvent(playerId, entity.getDrawTimes()));
		
		PlantWeaponBackDrawResp.Builder builder = PlantWeaponBackDrawResp.newBuilder();
		builder.setType(type);
		builder.setExchanged(exchangeTimes > 0 ? 1 : 0);
		rewardListA.forEach(e -> builder.addReward(e));
		rewardListB.forEach(e -> builder.addFinalReward(e));
		pushToPlayer(playerId, HP.code2.PLANT_WEAPON_BACK_DRAW_S_VALUE, builder);
		
		syncActivityInfo(playerId);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 抽奖消耗
	 * @param playerId
	 * @param times
	 * @param rewardList
	 * @return
	 */
	private int consume(String playerId, int times, List<RewardItem.Builder> rewardList) {
		PlantWeaponBackKVCfg constCfg = HawkConfigManager.getInstance().getKVInstance(PlantWeaponBackKVCfg.class);
		List<RewardItem.Builder> consumes = new ArrayList<>();
		RewardItem.Builder consumeItem = RewardHelper.toRewardItem(constCfg.getExtractExpend());
		consumes.add(consumeItem);
		int itemNum = this.getDataGeter().getItemNum(playerId, consumeItem.getItemId());
		//消耗金条，发放奖励
		if (itemNum >= times) {
			consumeItem.setItemCount(consumeItem.getItemCount() * times);
		} else {
			int addNum = times - itemNum;
			consumeItem.setItemCount(consumeItem.getItemCount() * itemNum);
			RewardItem.Builder builder = RewardHelper.toRewardItem(constCfg.getExtractNeedGoldExpend());
			builder.setItemCount(builder.getItemCount() * addNum);
			consumes.add(builder);
			
			RewardItem.Builder rewardBuilder = RewardHelper.toRewardItem(constCfg.getBuyGiveItem());
			rewardBuilder.setItemCount(rewardBuilder.getItemCount() * addNum);
			rewardList.add(rewardBuilder);
		}
		
		boolean flag = this.getDataGeter().cost(playerId, consumes, 1, Action.PLANT_WEAPON_BACK_DRAW, false);
		if (!flag) {
			HawkLog.errPrintln("PlantWeaponBackActivity draw error of consume, playerId: {}, times: {}, itemNum: {}", playerId, times, itemNum);
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}
		
		return 0;
	}
	
	/**
	 * 商店购买
	 * @param playerId
	 */
	public int onShopBuy(String playerId, int shopId, int count) {
		if (count <= 0) {
			HawkLog.errPrintln("PlantWeaponBackActivity shop buy error, playerId: {}, count: {}", playerId, count);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		PlantWeaponBackShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantWeaponBackShopCfg.class, shopId);
		if (cfg == null) {
			HawkLog.errPrintln("PlantWeaponBackActivity shop buy error of config, playerId: {}, shopId: {}", playerId, shopId);
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		if (cfg.getNotExchange() > 0) {
			int swId = cfg.getManhattanId();
			//对应超武已经解锁了，或解锁道具已经够解锁了
			boolean enough = getDataGeter().isPlantWeaponUnlocked(playerId, swId) || getDataGeter().isPlantWeaponUnlockItemEnough(playerId, swId);
			if (enough) {
				return Status.Error.PW_BACK_EXCHANGE_LIMIT_VALUE;
			}
		}
		
		Optional<PlantWeaponBackEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		PlantWeaponBackEntity entity = opEntity.get();
		
		int boughtCount = entity.getShopItemMap().getOrDefault(shopId, 0);
		int newCount = boughtCount + count;
		//限购
		if (newCount > cfg.getTimes()) {
			HawkLog.errPrintln("PlantWeaponBackActivity shop buy error, playerId: {}, shopId: {}, oldCount: {}, newCount: {}", playerId, shopId, boughtCount, newCount);
			return Status.Error.PLANT_WEAPON_SHOP_LIMIT_VALUE;
		}
		
		List<RewardItem.Builder> consumeItems = RewardHelper.toRewardItemImmutableList(cfg.getNeedItem());
		boolean flag = this.getDataGeter().cost(playerId, consumeItems, count, Action.PLANT_WEAPON_BACK_SHOPBUY, false);
		if (!flag) {
			HawkLog.errPrintln("PlantWeaponBackActivity shop buy error of consume, playerId: {}, shopId: {}", playerId, shopId);
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}
		
		shopBuyItem(playerId, entity, cfg, count);
		syncActivityInfo(playerId);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 商店够买
	 * @param playerId
	 * @param entity
	 * @param cfg
	 * @param count
	 */
	private void shopBuyItem(String playerId, PlantWeaponBackEntity entity, PlantWeaponBackShopCfg cfg, int count) {
		int shopId = cfg.getId();
		int boughtCount = entity.getShopItemMap().getOrDefault(shopId, 0);
		List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemImmutableList(cfg.getGainItem());
		this.getDataGeter().takeReward(playerId, rewardItems, count, Action.PLANT_WEAPON_BACK_SHOPBUY, true, RewardOrginType.ACTIVITY_REWARD);
		entity.getShopItemMap().put(shopId, boughtCount + count);
		entity.notifyUpdate();
		HawkLog.logPrintln("plantweaponback shop buy success, playerId: {}, shopId: {}, old boughtCount: {}, count: {}", playerId, shopId, boughtCount, count);
	}
	
	/**
	 * 同步活动信息
	 * 
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId){
		Optional<PlantWeaponBackEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		PlantWeaponBackEntity entity = opEntity.get();
		PBPlantWeaponBackInfo.Builder info = PBPlantWeaponBackInfo.newBuilder();
		info.setDrawTimes(entity.getDrawTimes());
		info.setFreeTimes(entity.getFreeTimes());
		for (Entry<Integer, Integer> entry : entity.getShopItemMap().entrySet()) {
			PWBShopInfo.Builder builder = PWBShopInfo.newBuilder();
			builder.setShopId(entry.getKey());
			builder.setCount(entry.getValue());
			info.addShopInfo(builder);
		}
		info.addAllTips(getTips(PlantWeaponBackShopCfg.class, entity.getTipSet()));
		pushToPlayer(playerId, HP.code2.PLANT_WEAPON_BACK_INFO_SYNC_VALUE, info);
	}
	
	public void syncActivityDataInfo(String playerId) {
		syncActivityInfo(playerId);
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

}
