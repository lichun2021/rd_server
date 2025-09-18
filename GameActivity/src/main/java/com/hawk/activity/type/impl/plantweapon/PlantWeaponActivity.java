package com.hawk.activity.type.impl.plantweapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.impl.PlantWeaponDrawTimesEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.plantweapon.cfg.PlantWeaponAchieveCfg;
import com.hawk.activity.type.impl.plantweapon.cfg.PlantWeaponKVCfg;
import com.hawk.activity.type.impl.plantweapon.cfg.PlantWeaponPoolCfg;
import com.hawk.activity.type.impl.plantweapon.cfg.PlantWeaponRewardCfg;
import com.hawk.activity.type.impl.plantweapon.cfg.PlantWeaponShopCfg;
import com.hawk.activity.type.impl.plantweapon.entity.PlantWeaponEntity;
import com.hawk.game.protocol.Activity.PBPlantWeaponActivityInfo;
import com.hawk.game.protocol.Activity.PBPlantWeaponShopItem;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.serialize.string.SerializeHelper;

/***
 * 泰能超武投放活动
 * 
 * @author lating
 */
public class PlantWeaponActivity extends ActivityBase implements AchieveProvider{
	
	/**
	 * 1.高级奖池，2.低级奖池
	 */
	static final int HIGH_POOL = 1;
	static final int LOW_POOL = 2;
	
	public PlantWeaponActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PLANT_WEAPON_355;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PlantWeaponActivity activity = new PlantWeaponActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PlantWeaponEntity> queryList = HawkDBManager.getInstance().query("from PlantWeaponEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PlantWeaponEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PlantWeaponEntity entity = new PlantWeaponEntity(playerId, termId);
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
		Optional<PlantWeaponEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		PlantWeaponEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(PlantWeaponAchieveCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.PLANT_WEAPON_ACHIEVE_REWARD;
	}
	
	@Override
	public void onOpen() {
		Set<String> onlinePlayers = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayers){
			if (!isOpening(playerId)) {
				continue;
			}
			callBack(playerId, GameConst.MsgId.INIT_PLANT_WEAPON_INIT, ()->{
				initAchieveItems(playerId);
				syncActivityInfo(playerId);
			});
		}
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		String playerId = event.getPlayerId();
		pushActivityInfo(playerId);
	}

	/**
	 * 同步活动信息
	 * @param playerId
	 */
	public void pushActivityInfo(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<PlantWeaponEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		long now = HawkTime.getMillisecond();
		PlantWeaponEntity entity = opEntity.get();
		if (!HawkTime.isSameDay(entity.getDayTime(), now)) {
			entity.setDayTime(now);
			if (entity.getItemList().isEmpty()) {
				initAchieveItems(playerId);
			}
		}
		
		syncActivityInfo(playerId);
	}
	
	
	/***
	 * 初始化成就
	 * @param playerId
	 * @return
	 */
	public void initAchieveItems(String playerId) {
		Optional<PlantWeaponEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		PlantWeaponEntity entity = opEntity.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		ConfigIterator<PlantWeaponAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PlantWeaponAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while (configIterator.hasNext()) {
			PlantWeaponAchieveCfg cfg = configIterator.next();				
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());				
			itemList.add(item);
		}
		entity.setItemList(itemList);
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}
	

	/**
	 * 研究方向（随机奖励）：分别从高级池子随出一个奖励物品、低级池子中随机出2个奖励物品
	 * @param playerId
	 * @param entity
	 * @param researchTimes 研究次数，表示第几次研究之前进行随机
	 */
	private void randomAward(String playerId, PlantWeaponEntity entity, int researchTimes) {
		PlantWeaponKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlantWeaponKVCfg.class);
		if (researchTimes > kvCfg.getMaxExtract()) {
			return;
		}
		PlantWeaponPoolCfg highPoolCfg = PlantWeaponPoolCfg.getHighPoolConfig(researchTimes);
		PlantWeaponPoolCfg lowPoolCfg = PlantWeaponPoolCfg.getLowPoolConfig(researchTimes);
		String highPoolReward = highPoolCfg.randomAward();
		//消耗道具数量达到xxx值时下次抽奖必得研究灵感
		if (entity.getConsumeItemCount() >= kvCfg.getNeedItemValue() && entity.getTouchCount() < 1) {
			highPoolReward = String.format("30000_%d_1", kvCfg.getAddOneItem());
		}
		String lowPoolReward = lowPoolCfg.randomAward();
		String lowPoolReward2 = "";
		int count = 0;
		do {
			lowPoolReward2 = lowPoolCfg.randomAward();
			count++;
			if (!lowPoolReward2.equals(lowPoolReward) || count > 10) { //限定循环次数，防止出现死循环
				break;
			}
		} while(true);
		
		PlantWeaponRewardCfg rewardCfg = PlantWeaponRewardCfg.getConfig(researchTimes);
		int disCount = rewardCfg.randomDisCount();
		entity.getAwardList().clear();
		entity.getAwardList().add(highPoolReward);
		entity.getAwardList().add(lowPoolReward);
		entity.getAwardList().add(lowPoolReward2);
		entity.setDisCount(disCount);
		entity.notifyUpdate();
		
		HawkLog.logPrintln("PlantWeaponActivity randomAward, playerId: {}, researchTimes: {}, highPoolReward: {}, lowPoolReward: {}, lowPoolReward2: {}, disCount: {}, loopTimes: {}", 
				playerId, researchTimes, highPoolReward, lowPoolReward, lowPoolReward2, disCount, count);
	}
	
	/**
	 * 点击研究方向，随机奖励
	 * @param playerId
	 */
	public int onAwardRandom(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<PlantWeaponEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		PlantWeaponEntity entity = opEntity.get();
		PlantWeaponKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlantWeaponKVCfg.class);
		int currDrawTimes = entity.getContinueDraws();
		if (entity.getContinueDraws() < kvCfg.getMaxExtract()) {
			currDrawTimes += 1; //如果还没有达到最大抽奖次数，当前随机奖励实际是给下一次付费展示用
		}
		
		randomAward(playerId, entity, currDrawTimes);
		syncActivityInfo(playerId);
		return 0;
	}
	
	/**
	 * 开始研究（继续研究）：消耗物品获得奖励，同时随机出下一次的奖励；第一次研究获得研究灵感道具，消耗满300获得研究灵感道具，获得特殊奖励得灵感道具
	 * @param playerId
	 */
	public int onResearch(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<PlantWeaponEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		PlantWeaponEntity entity = opEntity.get();
		PlantWeaponKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlantWeaponKVCfg.class);
		if (entity.getAwardList().isEmpty()) {
			HawkLog.errPrintln("PlantWeaponActivity research error, random award empty, playerId: {}", playerId);
			return Status.Error.PLANT_WEAPON_RANDON_EMPTY_VALUE; //还未随机出奖励
		}
		
		if (entity.getInspireProgress() >= kvCfg.getInspireMaxVal()) {
			 HawkLog.logPrintln("PlantWeaponActivity research inspireProgress full break, playerId: {}", playerId);
			 return 0;
		}
		
		double disCount = entity.getDisCount() * 1d / 10000;
		int currDrawTimes = entity.getContinueDraws() + 1;
		currDrawTimes = Math.min(currDrawTimes, kvCfg.getMaxExtract());
		PlantWeaponRewardCfg rewardCfg = PlantWeaponRewardCfg.getConfig(currDrawTimes);
		//研究消耗
        List<RewardItem.Builder> consume = RewardHelper.toRewardItemImmutableList(rewardCfg.getCostItem());
        consume.forEach(e -> e.setItemCount((long) Math.ceil(e.getItemCount() * disCount)));
        boolean cost = this.getDataGeter().cost(playerId, consume, 1, Action.PLANT_WEAPON_RESEARCH, false);
        if (!cost) {
        	HawkLog.errPrintln("PlantWeaponActivity research consume error, playerId: {}", playerId);
        	return Status.Error.ITEM_NOT_ENOUGH_VALUE;
        }
        
        //研究获得
        String awardItems = SerializeHelper.collectionToString(entity.getAwardList(), SerializeHelper.BETWEEN_ITEMS);
        if (!HawkOSOperator.isEmptyString(kvCfg.getExtReward())) {
        	awardItems = String.format("%s,%s", awardItems, kvCfg.getExtReward());
        }
        List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemImmutableList(awardItems);
        this.getDataGeter().takeReward(playerId, rewardItems, 1, Action.PLANT_WEAPON_RESEARCH, true, RewardOrginType.ACTIVITY_REWARD);
        
		//累计研究次数
        entity.continueDrawsAdd(1);
        //各种灵感进度增加条件判断
        inspireProgressAdd(entity, rewardItems, consume);
        
        ActivityManager.getInstance().postEvent(new PlantWeaponDrawTimesEvent(playerId, 1));
        HawkLog.logPrintln("PlantWeaponActivity research finish, playerId: {}, turn: {}, researchTimes: {}, consumeTotal: {}, inspireProgress: {}", 
        		playerId, entity.getTurnCount(), entity.getContinueDraws(), entity.getConsumeItemCount(), entity.getInspireProgress());

        //随机下一次的奖励
        randomAward(playerId, entity, Math.min(entity.getContinueDraws() + 1, kvCfg.getMaxExtract()));
        
        syncActivityInfo(playerId);
        return 0;
	}
	
	/**
	 * 灵感进度增加
	 * @param entity
	 * @param rewardItems
	 * @param consume
	 */
	private void inspireProgressAdd(PlantWeaponEntity entity, List<RewardItem.Builder> rewardItems, List<RewardItem.Builder> consume) {
		//累计消耗
        int oldConsumeTotal = entity.getConsumeItemCount();
        int newConsumeCount = (int)consume.get(0).getItemCount();
        entity.consumeAdd(newConsumeCount);
        
        PlantWeaponKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlantWeaponKVCfg.class);
        //消耗道具数量达到xxx值时下次抽奖必得研究灵感
        if (oldConsumeTotal >= kvCfg.getNeedItemValue() && entity.getTouchCount() < 1) {
        	entity.touchCountAdd(1);
        	entity.setConsumeItemCount(newConsumeCount);
        	HawkLog.logPrintln("PlantWeaponActivity research inspireProgressAdd by consume, playerId: {}, turn: {}, researchTimes: {}, consumeTotal: {}, inspireProgress: {}", 
            		entity.getPlayerId(), entity.getTurnCount(), entity.getContinueDraws(), entity.getConsumeItemCount(), entity.getInspireProgress());
        }
        
        //获得特殊奖励得灵感道具
        Optional<RewardItem.Builder> op = rewardItems.stream().filter(e -> e.getItemId() == kvCfg.getAddOneItem()).findAny();
        if (op.isPresent()) {
        	entity.inspireProgressAdd(1);
        	specialConsume(entity.getPlayerId(), kvCfg.getAddOneItem()); //获得的道具随即自动消耗掉
        	HawkLog.logPrintln("PlantWeaponActivity research inspireProgressAdd by addOneItem, playerId: {}, turn: {}, researchTimes: {}, consumeTotal: {}, inspireProgress: {}", 
        			entity.getPlayerId(), entity.getTurnCount(), entity.getContinueDraws(), entity.getConsumeItemCount(), entity.getInspireProgress());
        }
        Optional<RewardItem.Builder> opThree = rewardItems.stream().filter(e -> e.getItemId() == kvCfg.getAddThreeItem()).findAny();
        if (opThree.isPresent()) {
        	entity.inspireProgressAdd(3);
        	specialConsume(entity.getPlayerId(), kvCfg.getAddThreeItem()); //获得的道具随即自动消耗掉
        	HawkLog.logPrintln("PlantWeaponActivity research inspireProgressAdd by addThreeItem, playerId: {}, turn: {}, researchTimes: {}, consumeTotal: {}, inspireProgress: {}", 
        			entity.getPlayerId(), entity.getTurnCount(), entity.getContinueDraws(), entity.getConsumeItemCount(), entity.getInspireProgress());
        }
        
        //灵感达到满值了
        if (entity.getInspireProgress() >= kvCfg.getInspireMaxVal()) {
        	entity.setInspireProgress(kvCfg.getInspireMaxVal());
        	String unlockItem = kvCfg.getPlantWeaponUnlockItem(entity.getChoosePlantWeapon()); 
        	List<RewardItem.Builder> unlockItemAward = RewardHelper.toRewardItemImmutableList(unlockItem);
            this.getDataGeter().takeReward(entity.getPlayerId(), unlockItemAward, 1, Action.PLANT_WEAPON_RESEARCH, true, RewardOrginType.ACTIVITY_BIG_REWEARD);
            HawkLog.logPrintln("PlantWeaponActivity research inspireProgress full, playerId: {}, turn: {}, researchTimes: {}, consumeTotal: {}, inspireProgress: {}", 
            		entity.getPlayerId(), entity.getTurnCount(), entity.getContinueDraws(), entity.getConsumeItemCount(), entity.getInspireProgress());
        }
	}
	
	/**
	 * 特殊道具自动消耗
	 * @param playerId
	 * @param itemId
	 */
	private void specialConsume(String playerId, int itemId) {
		int itemNum = this.getDataGeter().getItemNum(playerId, itemId);
    	RewardItem.Builder builder = RewardHelper.toRewardItem(ItemType.TOOL_VALUE * 10000, itemId, itemNum);
    	List<RewardItem.Builder> specialConsume = new ArrayList<>();
    	specialConsume.add(builder);
    	this.getDataGeter().cost(playerId, specialConsume, 1, Action.PLANT_WEAPON_SPECIAL_CONSUME, false);
	}
	
	/**
	 * 选择超武解锁道具奖励（就是【研究灵感】进度达满值时送的超武激活道具）
	 * @param playerId
	 * @param swId 超武ID
	 * @return
	 */
	public int choosePlantWeapon(String playerId, int swId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<PlantWeaponEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		PlantWeaponEntity entity = opEntity.get();
		PlantWeaponKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlantWeaponKVCfg.class);
		//灵感值未满
		if (entity.getInspireProgress() < kvCfg.getInspireMaxVal()) {
			HawkLog.errPrintln("PlantWeaponActivity choosePlantWeapon error, playerId: {}, continueDraws: {}, maxVal config: {}", playerId, entity.getContinueDraws(), kvCfg.getMaxExtract());
			return Status.Error.PLANT_WEAPON_INSPIRE_LIMIT_VALUE;
		}
		
		if (entity.getTurnCount() >= kvCfg.getTurnMaxVal()) {
			HawkLog.errPrintln("PlantWeaponActivity choosePlantWeapon error, playerId: {}, turn now: {}, turnMax config: {}", playerId, entity.getTurnCount(), kvCfg.getTurnMaxVal());
			return Status.Error.PLANT_WEAPON_TURN_MAX_VALUE;
		}
		
		String configUnlockItem = kvCfg.getPlantWeaponUnlockItem(swId);
		if (HawkOSOperator.isEmptyString(configUnlockItem)) {
			HawkLog.errPrintln("PlantWeaponActivity choosePlantWeapon error, unlockItem config empty, playerId: {}, swId: {}", playerId, swId);
			return Status.Error.PLANT_WEAPON_CONFIG_ERROR_VALUE;
		}
		
		//所选解锁道具对应的超武已经解锁了，或解锁道具已经足够解锁了，不让选
		if (getDataGeter().isPlantWeaponUnlocked(playerId, swId)) {
			HawkLog.errPrintln("PlantWeaponActivity choosePlantWeapon error, plant weapon unlocked, playerId: {}, swId: {}", playerId, swId);
			return Status.Error.PLANT_WEAPON_CHOOSE_UNLOCKED_VALUE;
		}
		
		if (getDataGeter().isPlantWeaponUnlockItemEnough(playerId, swId)) {
			HawkLog.errPrintln("PlantWeaponActivity choosePlantWeapon error, plant weapon unlock item enough, playerId: {}, swId: {}", playerId, swId);
			return Status.Error.PLANT_WEAPON_CHOOSE_ENOUGH_VALUE;
		}
		
		entity.turnCountAdd(1);
		entity.setChoosePlantWeapon(swId);
		
		//数据重置
		entity.setContinueGiveups(0);
    	entity.setCooldownTime(0);
    	entity.setContinueDraws(0);
		entity.setInspireProgress(0);
		entity.setTouchCount(0);
		entity.setConsumeItemCount(0);
    	entity.getAwardList().clear();
		entity.setDisCount(0);
		entity.notifyUpdate();
		HawkLog.logPrintln("PlantWeaponActivity choosePlantWeapon, playerId: {}, turn: {}, swId: {}", playerId, entity.getTurnCount(), swId);
		
		syncActivityInfo(playerId);
        return 0;
	}
	
	/**
	 * 放弃研究：连续多次放弃，会进入冷却期；连续研究次数清零；研究灵感清零（如果玩家灵感进度为满值则后续点击【放弃研究】不会清空研究灵感进度）
	 * @param playerId
	 */
	public int onGiveup(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<PlantWeaponEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		PlantWeaponEntity entity = opEntity.get();
		if (entity.getAwardList().isEmpty()) {
			HawkLog.errPrintln("PlantWeaponActivity give up error, random award empty, playerId: {}", playerId);
			return 0;
		}
		
		PlantWeaponKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlantWeaponKVCfg.class);
		//处于冷却期未结束
		long cooldownTime = entity.getCooldownTime();
		if (cooldownTime > 0 && cooldownTime + kvCfg.getContinuityWaiveCoolDownTime() > HawkTime.getMillisecond()) {
			HawkLog.errPrintln("PlantWeaponActivity give up error, playerId: {}, cooldown startTime: {}, continueTime: {}", playerId, HawkTime.formatTime(cooldownTime), kvCfg.getContinuityWaiveCoolDownTime());
			return Status.Error.PLANT_WEAPON_GIVEUP_COOL_VALUE;
		}
		
		 //冷却cd结束后，之前累计的连续放弃次数也要清零了
        if (cooldownTime > 0) {
        	entity.setContinueGiveups(0);
        	entity.setCooldownTime(0);
        }
		
		entity.setContinueDraws(0);
		//研究灵感未达到满值也需要清零
		if (entity.getInspireProgress() < kvCfg.getInspireMaxVal()) {
			entity.setInspireProgress(0);
			entity.setTouchCount(0);
		}
		//连续放弃次数加1
		entity.continueGiveupsAdd(1);
		//连续放弃次数达到阈值，进入冷却期
		if (entity.getContinueGiveups() >= kvCfg.getContinuityWaiveTimes()) {
			entity.setCooldownTime(HawkTime.getMillisecond());
		}
		
		//之前随出的奖励清空
		entity.getAwardList().clear();
		entity.setDisCount(0);
		entity.notifyUpdate();
		
		HawkLog.logPrintln("PlantWeaponActivity giveup, playerId: {}, turn: {}, researchTimes: {}, giveupTimes: {}, inspireProgress: {}, cooldownTime: {}", 
				playerId, entity.getTurnCount(), entity.getContinueDraws(), entity.getContinueGiveups(), entity.getInspireProgress(), entity.getCooldownTime());
		
		syncActivityInfo(playerId);
		return 0;
	}
	
	/**
	 * 领取免费奖励 
	 * @return
	 */
	public int recieveDailyReward(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		String freeAward = PlantWeaponShopCfg.getFreeAward();
		if (HawkOSOperator.isEmptyString(freeAward)) {
			HawkLog.errPrintln("PlantWeaponActivity recieveDailyReward error, freeAward empty, playerId: {}", playerId);
			return Status.Error.PLANT_WEAPON_FREE_EMPTY_VALUE;
		}
		
		Optional<PlantWeaponEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		PlantWeaponEntity entity = opEntity.get();
		long now = HawkTime.getMillisecond();
		if (HawkTime.isSameDay(entity.getDailyRecieveTime(), now)) {
			HawkLog.errPrintln("PlantWeaponActivity recieveDailyReward error, recieved finish today, playerId: {}", playerId);
			return Status.Error.PLANT_WEAPON_FREE_RECIEVED_VALUE;
		}
		
		entity.setDailyRecieveTime(now);
		List<RewardItem.Builder> freeAwardItem = RewardHelper.toRewardItemImmutableList(freeAward);
        this.getDataGeter().takeReward(playerId, freeAwardItem, 1, Action.PLANT_WEAPON_FREE_AWARD, true, RewardOrginType.ACTIVITY_REWARD);
        HawkLog.logPrintln("PlantWeaponActivity recieve free award, playerId: {}", playerId);
        
        syncActivityInfo(playerId); 
		return 0;
	}
	
	/**
	 * 直购判断
	 * 
	 * @param playerId
	 * @param goodsId
	 * @return
	 */
	public int buyItemCheck(String playerId, String goodsId) {
		PlantWeaponShopCfg cfg = PlantWeaponShopCfg.getConfig(goodsId);
		if (cfg == null) {
			HawkLog.errPrintln("PlantWeaponActivity shop buyItem check error, shop config match null, playerId: {}, goodsId: {}", playerId, goodsId);
			return Status.Error.PLANT_WEAPON_PAYGIFT_EMPTY_VALUE;
		}
		
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<PlantWeaponEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		PlantWeaponEntity entity = opEntity.get();
		
		int boughtCount = entity.getShopItemMap().getOrDefault(cfg.getId(), 0);
		int newCount = boughtCount + 1;
		//限购
		if (newCount > cfg.getTimes()) {
			HawkLog.errPrintln("PlantWeaponActivity shop buyItem check error, playerId: {}, goodsId: {}, shopId: {}, oldCount: {}", playerId, goodsId, cfg.getId(), boughtCount);
			return Status.Error.PLANT_WEAPON_SHOP_LIMIT_VALUE;
		}
		
		return 0;
	}
	
	/***
	 * 购买事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		String goodsId = event.getGiftId();
		PlantWeaponShopCfg cfg = PlantWeaponShopCfg.getConfig(goodsId);
		if (cfg == null) {
			HawkLog.errPrintln("PlantWeaponActivity payGift callback error,  shop config match null, playerId: {}, goodsId: {}", playerId, goodsId);
			return;
		}
		
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<PlantWeaponEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		PlantWeaponEntity entity = opEntity.get();
		shopBuyItem(playerId, entity, cfg, 1);
		
		HawkLog.logPrintln("PlantWeaponActivity payGift finish, playerId: {}, turn: {}, goodsId: {}, shopId: {}", playerId, entity.getTurnCount(), goodsId, cfg.getId());
		syncActivityInfo(playerId);
	}
	
	/**
	 * 商店购买
	 * @param playerId
	 */
	public int onShopBuy(String playerId, int shopId, int count) {
		if (count <= 0) {
			HawkLog.errPrintln("PlantWeaponActivity shop buy error, playerId: {}, count: {}", playerId, count);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		PlantWeaponShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantWeaponShopCfg.class, shopId);
		if (cfg == null) {
			HawkLog.errPrintln("PlantWeaponActivity shop buy error of config, playerId: {}, shopId: {}", playerId, shopId);
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		//需要通过直购付费的形式购买
		if (cfg.getShopItemType() == 1) {
			HawkLog.errPrintln("PlantWeaponActivity shop buy error, playerId: {}, shopId: {}, shopItemType: {}", playerId, shopId, cfg.getShopItemType());
			return Status.Error.PLANT_WEAPON_SHOP_BUY_ERROR_VALUE;
		}
		
		Optional<PlantWeaponEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		PlantWeaponEntity entity = opEntity.get();
		
		int boughtCount = entity.getShopItemMap().getOrDefault(shopId, 0);
		int newCount = boughtCount + count;
		//限购
		if (newCount > cfg.getTimes()) {
			HawkLog.errPrintln("PlantWeaponActivity shop buy error, playerId: {}, shopId: {}, oldCount: {}, newCount: {}", playerId, shopId, boughtCount, newCount);
			return Status.Error.PLANT_WEAPON_SHOP_LIMIT_VALUE;
		}
		
		List<RewardItem.Builder> consumeItems = RewardHelper.toRewardItemImmutableList(cfg.getPayItem());
		// 判断道具足够否
		boolean flag = this.getDataGeter().cost(playerId, consumeItems, count, Action.PLANT_WEAPON_SHOP_BUY, false);
		if (!flag) {
			HawkLog.errPrintln("PlantWeaponActivity shop buy error of consume, playerId: {}, shopId: {}", playerId, shopId);
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
	private void shopBuyItem(String playerId, PlantWeaponEntity entity, PlantWeaponShopCfg cfg, int count) {
		int shopId = cfg.getId();
		int boughtCount = entity.getShopItemMap().getOrDefault(shopId, 0);
		List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemImmutableList(cfg.getGetItem());
		this.getDataGeter().takeReward(playerId, rewardItems, count, Action.PLANT_WEAPON_SHOP_BUY, true, RewardOrginType.ACTIVITY_REWARD);
		entity.getShopItemMap().put(shopId, boughtCount + count);
		entity.notifyUpdate();
		
		Map<String, Object> param = new HashMap<>();
        param.put("shopId", shopId); //购买商品ID
        param.put("buyCount", count);   //购买商品数量
        param.put("itemId", rewardItems.get(0).getItemId());  //购买获得的物品ID
        param.put("itemCount", rewardItems.get(0).getItemCount() * count); //购买获得的物品数量
        getDataGeter().logActivityCommon(playerId, LogInfoType.plant_weapon_shop_buy, param);
         
		HawkLog.logPrintln("plantweapon shop buy success, playerId: {}, shopId: {}, old boughtCount: {}, count: {}", playerId, shopId, boughtCount, count);
	}
	
	/**
	 * 同步活动信息
	 * 
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId){
		Optional<PlantWeaponEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		PlantWeaponEntity entity = opEntity.get();
		PBPlantWeaponActivityInfo.Builder info = PBPlantWeaponActivityInfo.newBuilder();
		info.setTurnCount(entity.getTurnCount());
		info.setDrawTimes(entity.getContinueDraws());
		info.setGiveupTimes(entity.getContinueGiveups());
		info.setCooldownTime(entity.getCooldownTime());
		info.setInspireProgress(entity.getInspireProgress());
		info.setConsumeCount(entity.getConsumeItemCount());
		info.addAllAwardItems(entity.getAwardList());
		info.setDisCount(entity.getDisCount());
		//当日免费奖励是否已领取、【研究灵感】进度满值送的超武激活道具
		info.setRecieveToday(HawkTime.isSameDay(entity.getDailyRecieveTime(), HawkTime.getMillisecond()) ? 1 : 0);
		info.setChoosePlantWeapon(entity.getChoosePlantWeapon());
		for (Entry<Integer, Integer> entry : entity.getShopItemMap().entrySet()) {
			PBPlantWeaponShopItem.Builder builder = PBPlantWeaponShopItem.newBuilder();
			builder.setShopId(entry.getKey());
			builder.setCount(entry.getValue());
			info.addShopItem(builder);
		}
		pushToPlayer(playerId, HP.code2.ACTIVITY_PLANT_WEAPON_INFO_SYNC_VALUE, info);
	}
	
	
	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

}
