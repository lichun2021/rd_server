package com.hawk.game.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CostDiamondBuyGiftEvent;
import com.hawk.activity.event.impl.TravelShopBuyBlackGoldPackageEvent;
import com.hawk.activity.event.impl.TravelShopPurchaseEvent;
import com.hawk.activity.type.impl.drogenBoatFestival.benefit.cfg.DragonBoatBenefitKVCfg;
import com.hawk.activity.type.impl.travelshopAssist.cfg.TravelShopAssistKVCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.DragonBoatBenefitShopCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.TimeLimitStoreCfg;
import com.hawk.game.config.TimeLimitStoreTypeCfg;
import com.hawk.game.config.TravelShopAssistCfg;
import com.hawk.game.config.TravelShopCfg;
import com.hawk.game.config.TravelShopFriendlyAwardCfg;
import com.hawk.game.config.TravelShopGiftCfg;
import com.hawk.game.config.TravelShopPoolCfg;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.data.TimeLimitStoreConditionInfo;
import com.hawk.game.data.TimeLimitStoreConditionInfo.ConditionState;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.travelshop.TravelShopFriendly;
import com.hawk.game.msg.TimeLimitStoreTriggerMsg;
import com.hawk.game.msg.TravelShopBuildingFinishMsg;
import com.hawk.game.msg.TravelShopFriendlyCardBuyMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.TravelShop.RateChangeParam;
import com.hawk.game.protocol.TravelShop.TimeLimitStoreBuyReq;
import com.hawk.game.protocol.TravelShop.TimeLimitStoreGoodsBuyInfo;
import com.hawk.game.protocol.TravelShop.TimeLimitStoreGroupInfo;
import com.hawk.game.protocol.TravelShop.TravelGiftBuyC;
import com.hawk.game.protocol.TravelShop.TravelGiftBuyS;
import com.hawk.game.protocol.TravelShop.TravelGiftItemMsg;
import com.hawk.game.protocol.TravelShop.TravelShopBuyReq;
import com.hawk.game.protocol.TravelShop.TravelShopBuyResp;
import com.hawk.game.protocol.TravelShop.TravelShopFriendlyAddPush;
import com.hawk.game.protocol.TravelShop.TravelShopFriendlyAwardAchieveReq;
import com.hawk.game.protocol.TravelShop.TravelShopFriendlyAwardGroupSetReq;
import com.hawk.game.protocol.TravelShop.TravelShopFriendlyAwardGroupSetResp;
import com.hawk.game.protocol.TravelShop.TravelShopInfoSync;
import com.hawk.game.protocol.TravelShop.TravelShopItemMsg;
import com.hawk.game.protocol.TravelShop.TravelShopRefreshResp;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.RandomUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GiftType;
import com.hawk.sdk.msdk.entity.PayItemInfo;

/**
 * 黑市商人模块
 * 
 * @author admin
 *
 */
public class PlayerTravelShopModule extends PlayerModule {
	static Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 是否解锁了该模块
	 */
	private boolean isUnlock = false;
	/**
	 * 上一次tick限时商店的时刻
	 */
	private long lastTickTime;
	/**
	 * 当天触发开启限时商店次数
	 */
	private int timeLimitStoreOpenTimesDay;
	/**
	 * 当天内限时商店开启时间（实际就是timeLimitStoreOpenTimesDay变量的初始化时间，在检测到跨天时请求redis初始化，减少查redis次数，减轻压力）
	 */
	private long timeLimitStoreOpenTime;
	/**
	 * 限时商店购买信息
	 */
	private Map<Integer, Integer> timeLimitBoughtInfo;

	/**
	 * 上次tick的小时
	 */
	private int hourFlag;
	
	public PlayerTravelShopModule(Player player) {
		super(player);
	}

	@Override
	public boolean onPlayerLogin() {
		if (isUnlock && !SystemControler.getInstance().isSystemItemsClosed(ControlerModule.INDEPENDENT_ARMS)) {
			this.travelShopFriendlyUpdate();
			long nextRefreshTime = this.checkNeedRefresh();
			if (nextRefreshTime > 0) {
				this.refreshGoods(true, nextRefreshTime, true, null);
			} else {
				this.synGoodsInfo();
			}
			// 初始化已上架的商品库信息
			initTimeLimitOnSaleStore();
			hourFlag = HawkTime.getHour();
		}
		return false;
	}

	@Override
	public boolean onPlayerAssemble() {
		calcUnlocked();
		return true;
	}
	
	 /**
     * 退出游戏处理
     */
    protected boolean onPlayerLogout() {
    	// 考虑到跨服，玩家下线时需要将数据重置
    	timeLimitStoreOpenTimesDay = 0;
    	timeLimitStoreOpenTime = 0L;
    	timeLimitBoughtInfo = null;
    	player.resetTimeLimitStoreCondition();
    	return super.onPlayerLogout();
    }
	
    @MessageHandler
   	public void travelShopOpenEvent(TravelShopBuildingFinishMsg buildingMsg) {
   		if (buildingMsg.getCfgType() == BuildingType.ARMS_DEALER_VALUE) {
   			logger.info("open travel shop id:{}", player.getId());
   			long nextRefreshTime = this.findNextRefreshTime();
   			this.refreshGoods(true, nextRefreshTime, true, null);
   			this.isUnlock = true;
   		}
   	}
       
    /**
     * 特惠商人助力庆典活动,刷新新的商品给玩家，并重新设置刷新时间
     */
    public void onTravelShopAssistActivityChange(boolean rateParamClear){
    	try {
    		//新一期活动开启那刻，玩家不在线，等后面玩家上线时需要清理数据
    		if (rateParamClear) { 
    			TravelShopInfoSync.Builder shopBuilder = RedisProxy.getInstance().getTravelShopInfo(player.getId());
    			if (shopBuilder != null && !shopBuilder.getRateParamBuilderList().isEmpty()) {
    				TravelShopInfoSync.Builder newBuilder = cloneTravelShopInfo(shopBuilder);
    				RedisProxy.getInstance().addOrUpdateTravelShop(player.getId(), newBuilder);
    			}
    		} else {
    			long nextRefreshTime = this.findNextRefreshTime();
    			this.refreshGoods(true, nextRefreshTime, TravelShopInfoSync.newBuilder()); //这个传一个空的TravelShopInfoSync而不是null，相当于是清空历史浮动rate记录
    		}
    	} catch (Exception e) {
    		HawkException.catchException(e);
    	}
    }
    
    /**
     * shopBuilder克隆（清除rateParam信息）
     * @param shopBuilder
     * @return
     */
    private TravelShopInfoSync.Builder cloneTravelShopInfo(TravelShopInfoSync.Builder shopBuilder) {
    	TravelShopInfoSync.Builder newBuilder = TravelShopInfoSync.newBuilder();
    	newBuilder.setNextRefreshTime(shopBuilder.getNextRefreshTime());
    	if (!shopBuilder.getItemsList().isEmpty()) {
    		newBuilder.addAllItems(shopBuilder.getItemsList());
    	}
    	newBuilder.setRefreshCount(shopBuilder.getRefreshCount());
    	newBuilder.setIsActivity(shopBuilder.getIsActivity());
    	newBuilder.setActivityId(shopBuilder.getActivityId());
    	newBuilder.setFriendly(shopBuilder.getFriendly());
    	newBuilder.setFriendlyCardBuyTime(shopBuilder.getFriendlyCardBuyTime());
    	newBuilder.setFriendlyCommAwardCount(shopBuilder.getFriendlyCommAwardCount());
    	newBuilder.setFriendlyPrivilegeAwardCount(shopBuilder.getFriendlyPrivilegeAwardCount());
    	newBuilder.setFriendlyPrivilegeGroup(shopBuilder.getFriendlyPrivilegeGroup());
    	return newBuilder;
    }
    
    public void travelShopItemRefresh(){
    	int curHour = HawkTime.getHour();
		int[] timeArr = null;
    	if(this.hourFlag != curHour){
    		this.hourFlag = curHour;
    		int type = this.getCurType();
    		if(type == 0){
    			timeArr = ConstProperty.getInstance().getTravelShopRefreshTimeArray(); 
    		}else if(type == ActivityType.TRAVEL_SHOP_ASSIST_VALUE){
    			TravelShopAssistKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(TravelShopAssistKVCfg.class);
    			timeArr = kvCfg.getTravelShopRefreshTimeArr();
    		}else if(type == ActivityType.DRAGON_BOAT_BENEFIT_VALUE){
    			DragonBoatBenefitKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatBenefitKVCfg.class);
    			timeArr = kvCfg.getTravelShopRefreshTimeArr();
    		}
    		boolean has = false;
    		for(int time : timeArr ){
    			if(time == curHour){
    				has = true;
    				break;
    			}
    		}
    		if(has){
    			long nextRefreshTime = this.findNextRefreshTime();
    			this.refreshGoods(true, nextRefreshTime, true, null);
    		}
    	}
    }
    
	/**
	 * 在线tick
	 */
	public boolean onTick() {
		travelShopItemRefresh();
		timelimitStoreTick();
		return true;
	}
	
	/**
	 * 限时商店tick
	 */
	private void timelimitStoreTick() {
		if (!isUnlock || SystemControler.getInstance().isSystemItemsClosed(ControlerModule.INDEPENDENT_ARMS)) {
			return;
		}
		if (HawkApp.getInstance().getCurrentTime() - lastTickTime < 3000L) {
			return;
		}
		
		lastTickTime = HawkApp.getInstance().getCurrentTime();
		TimeLimitStoreConditionInfo onSaleStore = player.getOnSellStoreCondition();
		if (onSaleStore == null) {
			return;
		}
		if (!isTimeLimitStoreEnd(onSaleStore)) {
			return;
		}
		
		HawkLog.logPrintln("timeLimitStore tick end, playerId: {}, triggerType: {}, startTime: {}", player.getId(), onSaleStore.getTriggerType(), onSaleStore.getStartTime());
		// 在线tick到限时商品出售倒计时结束
		onStoreEnd();
		
	}
	
	/**
	 * 限时商店初始化
	 */
	protected void initTimeLimitOnSaleStore() {
		TimeLimitStoreConditionInfo onSaleStore = player.getOnSellStoreCondition();
		if (onSaleStore != null && !isTimeLimitStoreEnd(onSaleStore)) {
			pushTimeLimitStoreInfo();
			return;
		}
		
		Map<Integer, TimeLimitStoreConditionInfo> storeMap = player.getTimeLimitStoreConditionMap();
		if (storeMap.isEmpty()) {
			BuildingBaseEntity entity = this.getTravelShopBuilding();
			if (entity != null && entity.getStatus() == BuildingStatus.TIME_LIMIT_STORE_VALUE) {
				this.setBuildingStateAndPush(BuildingStatus.TRAVEL_SHOP_REFRESH);
			}
			
			HawkLog.logPrintln("initTimeLimitOnSaleStore empty, playerId: {}", player.getId());
			return;
		}
		
		for (TimeLimitStoreConditionInfo store : storeMap.values()) {
			if (!isTimeLimitStoreEnd(store)) {
				// 商店商品上架出售初始化，出售倒计时开始
				player.setOnSellStoreCondition(store);
				HawkLog.logPrintln("initTimeLimitOnSaleStore success, playerId: {}, triggerType: {}, startTime: {}", 
						player.getId(), store.getTriggerType(), store.getStartTime());
				break;
			}
		}
		
		onSaleStore = player.getOnSellStoreCondition();
		if (onSaleStore != null) {
			storeMap.clear();
			timeLimitBoughtInfo = RedisProxy.getInstance().getTimeLimitStoreBoughtInfo(player.getId());
			pushTimeLimitStoreInfo();
		} else {
			BuildingBaseEntity entity = this.getTravelShopBuilding();
			if (entity != null && entity.getStatus() == BuildingStatus.TIME_LIMIT_STORE_VALUE) {
				this.setBuildingStateAndPush(BuildingStatus.TRAVEL_SHOP_REFRESH);
			}
			
			HawkLog.logPrintln("initTimeLimitOnSaleStore finish, onsell store not exist, playerId: {}", player.getId());
		}
	}

	/**
	 * 是否已经解锁
	 */
	private void calcUnlocked() {
		if (getTravelShopBuilding() != null) {
			isUnlock = true;
		} else {
			if (isUnlock) {
				logger.error("travel shop switch is open");
			}
			isUnlock = false;
		}
	}

	private BuildingBaseEntity getTravelShopBuilding() {
		List<BuildingBaseEntity> entityList = player.getData().getBuildingListByType(BuildingType.ARMS_DEALER);
		if (entityList != null && !entityList.isEmpty()) {
			return entityList.get(0);
		}
		return null;
	}

	private void refreshGoods(boolean syn, long nextRefreshTime, boolean clearRefreshTime, TravelShopInfoSync.Builder oldBuilder) {
		TravelShopInfoSync.Builder builder = TravelShopInfoSync.newBuilder();
		builder.setNextRefreshTime(nextRefreshTime);
		Map<Integer, List<TravelShopCfg>> shopCfgListMap = AssembleDataManager.getInstance().getTravelShopCfgListMap();
		int cfgType= this.getCurType();
		if(cfgType == ActivityType.TRAVEL_SHOP_ASSIST_VALUE){
			shopCfgListMap = AssembleDataManager.getInstance().getTravelShopAssistCfgListMap();
			if (oldBuilder == null) {
				oldBuilder = RedisProxy.getInstance().getTravelShopInfo(player.getId());
			}
		}else if(cfgType == ActivityType.DRAGON_BOAT_BENEFIT_VALUE){
			shopCfgListMap = AssembleDataManager.getInstance().getDragonBoatBenefitShopCfgListMap();
		}
		
		int buildLevel = player.getCityLevel();
		int randCount = GsConst.TravelShopConstant.GROUP_ITEM_NUM;
		for (Entry<Integer, List<TravelShopCfg>> entry : shopCfgListMap.entrySet()) {
			TravelShopPoolCfg travelShopPoolCfg = HawkConfigManager.getInstance().getConfigByKey(TravelShopPoolCfg.class, entry.getKey());
			boolean empty = travelShopPoolCfg.getGiftIdList().isEmpty();
			EffType effType = Const.EffType.valueOf(travelShopPoolCfg.getUnlockEffectId());
			//在没有开启月卡专属兰的时候，必刷一个黑市礼包.
			if (!empty && (travelShopPoolCfg.getUnlockEffectId() > 0 && player.getData().getEffVal(effType) <= 0)) {
				List<Integer> idList = HawkRand.randomWeightObject(travelShopPoolCfg.getGiftIdList(), travelShopPoolCfg.getGiftRateList(), randCount);
				for (Integer id : idList) {
					TravelShopGiftCfg travelShopGiftCfg = HawkConfigManager.getInstance().getConfigByKey(TravelShopGiftCfg.class, id);
					TravelShopItemMsg.Builder itemBuilder = TravelShopItemMsg.newBuilder();
					itemBuilder.setCfgId(0);
					itemBuilder.setDiscount(0);
					itemBuilder.setBought(1);
					itemBuilder.setNum(0);
					TravelGiftItemMsg.Builder travelGiftItem = buildTravelGiftItemMsgBuilder(travelShopGiftCfg);
					itemBuilder.setGiftMsg(travelGiftItem);
					builder.addItems(itemBuilder);
				}
			} else {
				List<TravelShopCfg> levelList = entry.getValue().stream().filter(cfg -> {
					return cfg.getLvMin() <= buildLevel && cfg.getLvMax() >= buildLevel;
				}).collect(Collectors.toList());
				List<Integer> rateList = new ArrayList<>(levelList.size());
				if(cfgType == ActivityType.TRAVEL_SHOP_ASSIST_VALUE && oldBuilder != null){
					List<RateChangeParam.Builder> paramsList = oldBuilder.getRateParamBuilderList();
					Map<Integer, Integer> rateMap = new HashMap<>();
					paramsList.forEach(e -> rateMap.put(e.getCfgId(), e.getNewRate()));
					StringBuilder sb = new StringBuilder();
					for (TravelShopCfg cfg : levelList) {
						int rate = rateMap.getOrDefault(cfg.getId(), cfg.getRate());
						rateList.add(rate);
						if (rate != cfg.getRate()) {
							sb.append(cfg.getId()).append(":").append(rate).append(",");
						}
					}
					if (sb.length() > 0) {
						//HawkLog.logPrintln("travelShop refreshGoods, playerId: {}, poolId: {}, by rates: {}", player.getId(), entry.getKey(), sb.toString());
					}
				} else {
					levelList.forEach(cfg -> rateList.add(cfg.getRate()));
				}
				List<TravelShopCfg> randomList = HawkRand.randomWeightObject(levelList, rateList, randCount);				
				for (TravelShopCfg tsc : randomList) {
					TravelShopItemMsg.Builder itemBuilder = TravelShopItemMsg.newBuilder();
					itemBuilder.setBought(0);
					itemBuilder.setCfgId(tsc.getId());
					itemBuilder.setDiscount(tsc.getDiscount());
					itemBuilder.setNum(tsc.getNum());
					builder.addItems(itemBuilder);
				}
			}
		}

		DailyDataEntity dailyDataEntity = player.getData().getDailyDataEntity();
		if (clearRefreshTime) {
			dailyDataEntity.setTravelShopRefreshTimes(0);
			 BuildingBaseEntity entity = this.getTravelShopBuilding();
			 if (entity != null && entity.getStatus() != BuildingStatus.TIME_LIMIT_STORE_VALUE) {
				 this.setBuildingStateAndPush(BuildingStatus.TRAVEL_SHOP_REFRESH);
			 }
		}	
		if(cfgType == ActivityType.TRAVEL_SHOP_ASSIST_VALUE && oldBuilder != null){
			oldBuilder.getRateParamBuilderList().forEach(e -> builder.addRateParam(e));
		}
		builder.setRefreshCount(dailyDataEntity.getTravelShopRefreshTimes());
		builder.setIsActivity(cfgType == 0 ? 0 : 1);
		builder.setActivityId(cfgType);
		RedisProxy.getInstance().addOrUpdateTravelShop(player.getId(), builder);		
		if (syn) {
			this.synGoodsInfo(builder);
		}
	}
	
	
	/**
	 * 获取当前商店需要的配置类型
	 * @return
	 */
	public int getCurType(){
		Optional<ActivityBase> opActivity  = ActivityManager.getInstance().getActivity(ActivityType.TRAVEL_SHOP_ASSIST_VALUE);
		if (opActivity.isPresent() && opActivity.get().isOpening(player.getId())) {
			return ActivityType.TRAVEL_SHOP_ASSIST_VALUE;
		}
		Optional<ActivityBase> dbbActivity  = ActivityManager.getInstance().getActivity(ActivityType.DRAGON_BOAT_BENEFIT_VALUE);
		if (dbbActivity.isPresent() && dbbActivity.get().isOpening(player.getId())) {
			return ActivityType.DRAGON_BOAT_BENEFIT_VALUE;
		}
		return 0;
	}

	/**
	 * 刷新出物品 重置一下redis的数据
	 * 
	 * @param syn
	 */
	private void refreshGoods(boolean syn, long nextRefreshTime, TravelShopInfoSync.Builder builder) {
		refreshGoods(syn, nextRefreshTime, false, builder);
	}

	private void synGoodsInfo() {
		TravelShopInfoSync.Builder shopInfoBuilder = RedisProxy.getInstance().getTravelShopInfo(player.getId());
		if (shopInfoBuilder == null) {
			// 猜测是挤号出现
			logger.error("null TravelShopInfoSync id:{}", player.getId());
			BuildingBaseEntity building = this.getTravelShopBuilding();
			if (building != null) {
				logger.error("travelShop buildId:{}, playerId:{}, cfgId:{}, invalid:{}", building.getId(), building.getPlayerId(), building.getBuildingCfgId(), building.isInvalid());
			}
			long nextRefreshTime = this.findNextRefreshTime();
			this.refreshGoods(true, nextRefreshTime, shopInfoBuilder);
			return;
		}
		
		if (needRefreshGoods(shopInfoBuilder)) {
			this.refreshGoods(true, shopInfoBuilder.getNextRefreshTime(), shopInfoBuilder);
		} else {
			synGoodsInfo(shopInfoBuilder);
		}			
	}
	
	private boolean needRefreshGoods(TravelShopInfoSync.Builder shopInfoBuilder) {
		List<TravelShopItemMsg> itemList = shopInfoBuilder.getItemsList();
		HawkConfigManager configManager = HawkConfigManager.getInstance();
		TravelShopCfg travelShopCfg = null;
		TravelShopGiftCfg travelShopGiftCfg = null;
		int type = this.getCurType();
		for (TravelShopItemMsg item : itemList) {
			if (item.getCfgId() == 0) {
				continue;
			} 
			if(type == 0){
				travelShopCfg = configManager.getConfigByKey(TravelShopCfg.class, item.getCfgId());
			}else if(type == ActivityType.TRAVEL_SHOP_ASSIST_VALUE){
				travelShopCfg = configManager.getConfigByKey(TravelShopAssistCfg.class, item.getCfgId());
			}else if(type == ActivityType.DRAGON_BOAT_BENEFIT_VALUE){
				travelShopCfg = configManager.getConfigByKey(DragonBoatBenefitShopCfg.class, item.getCfgId());
			}
			
			if (travelShopCfg == null) {
				return true;
			}
			if (item.hasGiftMsg()) {
				travelShopGiftCfg = configManager.getConfigByKey(TravelShopGiftCfg.class, item.getGiftMsg().getTravelShopGiftId());
				if (travelShopGiftCfg == null) {
					return true;
				}
			}			
		}
		
		return false;
	}

	// 能省一点是一点
	private void synGoodsInfo(TravelShopInfoSync.Builder shopInfoBuilder) {
		DailyDataEntity dailyDataEntity = player.getData().getDailyDataEntity();
		shopInfoBuilder.setRefreshCount(dailyDataEntity.getTravelShopRefreshTimes());
		TravelShopFriendly friendlyInfo = RedisProxy.getInstance().getTravelShopFriendlyInfo(player.getId());
		shopInfoBuilder.setFriendly(friendlyInfo.getFriendly());
		shopInfoBuilder.setFriendlyCardBuyTime(friendlyInfo.getPrivilegeStartTime());
		shopInfoBuilder.setFriendlyCommAwardCount(friendlyInfo.getFriendlyCommAwardCount());
		shopInfoBuilder.setFriendlyPrivilegeAwardCount(friendlyInfo.getFriendlyPrivilegeAwardCount());
		shopInfoBuilder.setFriendlyPrivilegeGroup(friendlyInfo.getPrivilegeAwardPoolChoose());
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.TRAVEL_SHOP_INFO_SYNC_S_VALUE, shopInfoBuilder);
		player.sendProtocol(protocol);
	}

	@ProtocolHandler(code = HP.code.TRAVEL_SHOP_REFRESH_C_VALUE)
	public void travelShopRefresh(HawkProtocol protocol) {
		if (!isUnlock) {
			return;
		}
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.INDEPENDENT_ARMS)) {
			this.respRefreshProtocol(Status.SysError.INDEPENDENT_ARMS_SYSTEM_CLOSED_VALUE);
			return;
		}

		ConstProperty constProperty = ConstProperty.getInstance();
		TravelShopInfoSync.Builder sbuilder = RedisProxy.getInstance().getTravelShopInfo(player.getId());
		DailyDataEntity dailyData = player.getData().getDailyDataEntity();
		int freeTimes = constProperty.getTravelShopFreeRefreshTimes() + player.getEffect().getEffVal(EffType.TRAVEL_SHOP_REFRESH_TIMES_ADD);
		int[] costArray = constProperty.getTravelShopCrystalRefreshCostArray();
		int shopType = this.getCurType();
		if(shopType == ActivityType.TRAVEL_SHOP_ASSIST_VALUE){
			TravelShopAssistKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(TravelShopAssistKVCfg.class);
			freeTimes =kvCfg.getFreeRefreshTimes()+ player.getEffect().getEffVal(EffType.TRAVEL_SHOP_REFRESH_TIMES_ADD);
			costArray = kvCfg.getTravelShopCrystalRefreshCostArr();
		}else if(shopType == ActivityType.DRAGON_BOAT_BENEFIT_VALUE ){
			DragonBoatBenefitKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatBenefitKVCfg.class);
			freeTimes =kvCfg.getFreeRefreshTimes()+ player.getEffect().getEffVal(EffType.TRAVEL_SHOP_REFRESH_TIMES_ADD);
			costArray = kvCfg.getTravelShopCrystalRefreshCostArr();
		}
		
		int maxTimes = freeTimes + costArray.length;
		if (dailyData.getTravelShopRefreshTimes() >= maxTimes) {
			respRefreshProtocol(Status.Error.TRAVEL_SHOP_REFREASH_MAX_VALUE);
			return;
		}
		int costRecord = 0;
		// 免费的次数用完之后就用钻石
		if (dailyData.getTravelShopRefreshTimes() >= freeTimes) {
			int index = dailyData.getTravelShopRefreshTimes() - freeTimes;
			if(index >=costArray.length ){
				index = costArray.length -1;
			}
			int costGold = costArray[index];
			costRecord = costGold;
			ConsumeItems cost = ConsumeItems.valueOf();
			cost.addConsumeInfo(PlayerAttr.valueOf(PlayerAttr.GOLD_VALUE), costGold);
			if (!cost.checkConsume(player, protocol.getType())) {
				return;
			}
			cost.consumeAndPush(player, Action.TRAVEL_SHOP_REFRESH);
		}
		dailyData.setTravelShopRefreshTimes(dailyData.getTravelShopRefreshTimes() + 1);
		this.refreshGoods(true, sbuilder.getNextRefreshTime(), sbuilder);
		this.respRefreshProtocol(Status.SysError.SUCCESS_OK_VALUE);
		LogUtil.logTravelShopRefreshCost(player, dailyData.getTravelShopRefreshTimes(), PlayerAttr.GOLD_VALUE,costRecord);
	}

	private void respRefreshProtocol(int errorCode) {
		TravelShopRefreshResp.Builder sbuilder = TravelShopRefreshResp.newBuilder();
		sbuilder.setRlt(errorCode);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TRAVEL_SHOP_REFRESH_S_VALUE, sbuilder));
	}

	/**
	 * 普通礼包的购买.
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TRAVEL_SHOP_BUY_C_VALUE)
	public void onTravelShopBuy(HawkProtocol protocol) {
		if (!isUnlock) {
			return;
		}
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.INDEPENDENT_ARMS)) {
			this.respBuyProtocol(Status.SysError.INDEPENDENT_ARMS_SYSTEM_CLOSED_VALUE);
			return;
		}

		TravelShopBuyReq cparam = protocol.parseProtocol(TravelShopBuyReq.getDefaultInstance());
		HawkTuple3<TravelShopCfg, TravelShopInfoSync.Builder, TravelShopItemMsg.Builder> tuple = travelShopBuyCheck(cparam);
		if (tuple == null) {
			return;
		}
		TravelShopCfg travelShopCfg = tuple.first;
		TravelShopInfoSync.Builder shopBuilder = tuple.second;
		TravelShopItemMsg.Builder itemBuilder = tuple.third;
		
		TravelShopPoolCfg travelShopPoolCfg = HawkConfigManager.getInstance().getConfigByKey(TravelShopPoolCfg.class, travelShopCfg.getShopPool());
		ItemInfo priceItem = travelShopConsumeAndReward(travelShopCfg, protocol);
		if (priceItem == null) {
			return;
		}
		
		// 是否在活动期间,活动期间刷新黑金礼包逻辑有改动  modify by golden 20220227
		int type = this.getCurType();
		boolean isInActivity = (type == ActivityType.TRAVEL_SHOP_ASSIST_VALUE || type == ActivityType.DRAGON_BOAT_BENEFIT_VALUE);
		// 刷新黑市礼包
		if (travelShopPoolCfg.getType() == GsConst.TravelShopConstant.POOL_TYPE_NORMAL) {
			this.triggerTravelGift(shopBuilder, cparam.getCfgId(), isInActivity); //普通池
		} else {
			this.triggerVipTravelGift(shopBuilder, cparam.getCfgId(), isInActivity); //vip池
		}

		itemBuilder.setBought(1);
		//计算浮动rate
		if (type == ActivityType.TRAVEL_SHOP_ASSIST_VALUE) {
			goodsRateChange(cparam.getCfgId(), shopBuilder);
		}
		RedisProxy.getInstance().addOrUpdateTravelShop(player.getId(), shopBuilder);
		this.respBuyProtocol(Status.SysError.SUCCESS_OK_VALUE, itemBuilder);

		// 特惠商人购买道具打点记录
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, travelShopCfg.getItemId());
		int giftItemType = itemCfg == null ? 0 : itemCfg.getItemType();
		LogUtil.logGiftBagFlow(player, GiftType.TRAVEL_SHOP_ITEM, String.valueOf(travelShopCfg.getId()), (int)priceItem.getCount(), priceItem.getItemId(), giftItemType);

		//活动：超时空研究所(黑市商人/旅行商人)购买x次商品
		//202408~09 优化by lating -- 无论是普通栏位和月卡高级栏位，只要在特惠商店花费金币都计入消耗金条任务 https://project.feishu.cn/hjol_ruyi/story/detail/4543630297
		ActivityManager.getInstance().postEvent(new TravelShopPurchaseEvent(player.getId(), priceItem.getItemId(), (int)priceItem.getCount(), isInActivity, true));
		// 友好度增加
		travelShopFriendlyAddPush(travelShopCfg);
		
		/**
		if(isInActivity){
			//ActivityManager.getInstance().postEvent(new TravelShopCommonBuyCostEvent(player.getId(), priceItem.getItemId(), priceItem.getCount())); //活动特惠商人助力庆典，特惠商人普通专区购买消耗
			//ActivityManager.getInstance().postEvent(new TravelShopCommonBuyEvent(player.getId())); //特惠商店购买事件
			//ActivityManager.getInstance().postEvent(new TravelShopGoldAreaCostEvent(player.getId(), priceItem.getItemId(), priceItem.getCount())); //活动特惠商人助力庆典，特惠商人金币专区购买消耗
			//ActivityManager.getInstance().postEvent(new TravelShopTimeLimitCostEvent(player.getId(), priceItem.getItemId(),priceItem.getCount())); //特惠商人助力庆典，金条消耗
		}
		*/
	}
	
	/**
	 * 购买判断
	 * @param protocol
	 * @return
	 */
	private HawkTuple3<TravelShopCfg, TravelShopInfoSync.Builder, TravelShopItemMsg.Builder> travelShopBuyCheck(TravelShopBuyReq cparam) {
		TravelShopCfg travelShopCfg = null;
		int type = this.getCurType();
		if(type == 0){
			travelShopCfg = HawkConfigManager.getInstance().getConfigByKey(TravelShopCfg.class, cparam.getCfgId());
		}else if(type == ActivityType.TRAVEL_SHOP_ASSIST_VALUE){
			//对应活动配置：activity/travel_shop_assist/travel_shop_assist_pool.xml
			travelShopCfg = HawkConfigManager.getInstance().getConfigByKey(TravelShopAssistCfg.class, cparam.getCfgId());
		}else if(type == ActivityType.DRAGON_BOAT_BENEFIT_VALUE){
			//对应活动配置：activity/dw_shop/dw_shop_pool.xml
			travelShopCfg = HawkConfigManager.getInstance().getConfigByKey(DragonBoatBenefitShopCfg.class, cparam.getCfgId());
		}
		
		if (travelShopCfg == null) {
			logger.error("error cfgId:{} playerId:{}", cparam.getCfgId(), player.getId());
			this.respBuyProtocol(Status.SysError.PARAMS_INVALID_VALUE);
			return null;
		}
		
		TravelShopPoolCfg travelShopPoolCfg = HawkConfigManager.getInstance().getConfigByKey(TravelShopPoolCfg.class, travelShopCfg.getShopPool());
		if (travelShopPoolCfg.getUnlockEffectId() > 0 && player.getData().getEffVal(Const.EffType.valueOf(travelShopPoolCfg.getUnlockEffectId())) <= 0) {
			logger.warn("trvvelShopBuy playerId:{} has no unlockeffectId:{}", player.getId(),travelShopPoolCfg.getUnlockEffectId());
			this.respBuyProtocol(Status.SysError.PARAMS_INVALID_VALUE);
			return null;
		}

		TravelShopInfoSync.Builder shopBuilder = RedisProxy.getInstance().getTravelShopInfo(player.getId());
		Optional<TravelShopItemMsg.Builder> findItemOptional = shopBuilder.getItemsBuilderList().stream().filter(shopMsg -> shopMsg.getCfgId() == cparam.getCfgId()).findAny();
		if (!findItemOptional.isPresent()) {
			logger.error("travelShop cfgId:{} not find playerId:{}, ", cparam.getCfgId(), player.getId());
			this.respBuyProtocol(Status.Error.TRAVEL_SHOP_GOODS_NOT_EXIST_VALUE);
			return null;
		}

		TravelShopItemMsg.Builder itemBuilder = findItemOptional.get();
		if (itemBuilder.getBought() > 0) {
			logger.error("travelShop cfgId:{}, already bought  playerId:{}, ", cparam.getCfgId(), player.getId());
			this.respBuyProtocol(Status.Error.TRAVEL_SHOP_GOODS_ALREADY_BOUGHT_VALUE);
			return null;
		}
		
		return new HawkTuple3<TravelShopCfg, TravelShopInfoSync.Builder, TravelShopItemMsg.Builder>(travelShopCfg, shopBuilder, itemBuilder);
	}
	
	/**
	 * 特惠商店购买消耗、获取奖励
	 * @param travelShopCfg
	 * @param protocol
	 * @return
	 */
	private ItemInfo travelShopConsumeAndReward(TravelShopCfg travelShopCfg, HawkProtocol protocol) {
		ItemInfo priceItem = travelShopCfg.getItemPrice();
		ConsumeItems consume = ConsumeItems.valueOf();
		ItemInfo item = new ItemInfo();
		item.setType(priceItem.getType());
		item.setItemId(priceItem.getItemId());
		item.setCount(priceItem.getCount());
		consume.addConsumeInfo(item, false);
		if (!consume.checkConsume(player, protocol.getType())) {
			logger.error("travel shop buy wealth not enough, playerId: {}, cfgId: {}", player.getId(), travelShopCfg.getId());
			return null;
		}
		
		if (consume.getBuilder().hasAttrInfo() && consume.getBuilder().getAttrInfo().getDiamond() > 0) {
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, travelShopCfg.getItemId());
			if (itemCfg != null) {
				consume.addPayItemInfo(new PayItemInfo(String.valueOf(travelShopCfg.getItemId()), itemCfg.getSellPrice(), travelShopCfg.getNum()));
			}
		}
		consume.consumeAndPush(player, Action.TRAVEL_SHOP_BUY);
		AwardItems awardItem = AwardItems.valueOf();
		ItemInfo addItemInfo = new ItemInfo(Const.ItemType.TOOL_VALUE, travelShopCfg.getItemId(), travelShopCfg.getNum());
		awardItem.addItem(addItemInfo);
		awardItem.rewardTakeAffectAndPush(player, Action.TRAVEL_SHOP_BUY, true, RewardOrginType.TRAVEL_SHOPPING);
		return priceItem;
	}

	
	/**
	 * 物品刷出权重变更（149活动开启时，每购买一次物品，都需要计算一下这个物品下次刷出的权重）
	 * 
	 * 权重 = 原权重 *（1 + min（此商品购买次数*单次提升%，此商品最大提升%，全局最大提升%））
	 *   
	 * @param cfgId
	 * @param shopBuilder
	 */
	private void goodsRateChange(final int cfgId, TravelShopInfoSync.Builder shopBuilder) {
		try {
			Optional<RateChangeParam.Builder> optional = shopBuilder.getRateParamBuilderList().stream().filter(e -> e.getCfgId() == cfgId).findAny();
			int times = 1;
			RateChangeParam.Builder builder = RateChangeParam.newBuilder();
			if (!optional.isPresent()) {
				builder.setCfgId(cfgId);
			} else {
				builder = optional.get();
				times = builder.getBuyTimes() + 1;
			}
			
			TravelShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TravelShopAssistCfg.class, cfgId);
			int oldRate = times == 1 ? cfg.getRate() : builder.getNewRate();
			int riseMax = Math.min(cfg.getRiseMax(), ConstProperty.getInstance().getTravelShopAssistRateRise());
			int ratio = Math.min(cfg.getRise() * times, riseMax);
			double rateDouble = cfg.getRate() * (1 +  ratio * 1D / 10000);
			int newRate = (int) Math.floor(rateDouble);
			builder.setBuyTimes(times);
			builder.setNewRate(newRate);
			if (!optional.isPresent()) {
				shopBuilder.addRateParam(builder);
			}
			HawkLog.logPrintln("travelShop buy goods rate change, openid: {}, playerId: {}, goodsId: {}, shopPool: {}, buyTimes: {}, init rate: {}, old rate: {}, new rate: {}", 
					player.getOpenId(), player.getId(), cfgId, cfg.getShopPool(), times, cfg.getRate(), oldRate, newRate);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	private void respBuyProtocol(int errorCode, TravelShopItemMsg.Builder itemBuilder) {
		TravelShopBuyResp.Builder sbuilder = TravelShopBuyResp.newBuilder();
		sbuilder.setRlt(errorCode);
		if (itemBuilder != null) {
			sbuilder.setTravelShopItemMsg(itemBuilder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TRAVEL_SHOP_BUY_S_VALUE, sbuilder));
	}

	private void respBuyProtocol(int errorCode) {
		this.respBuyProtocol(errorCode, null);
	}

	/**
	 * 返回一个非0的时间需要刷新否则不需要
	 * 
	 * @return
	 */
	private long checkNeedRefresh() {
		TravelShopInfoSync.Builder shopInfoBuilder = RedisProxy.getInstance().getTravelShopInfo(player.getId());
		long nextRefreshTime = this.findNextRefreshTime();
		// 合服之后是有可能为空的
		if (shopInfoBuilder == null) {
			logger.warn("travelShopInfoBuilder is null playerId:{}", player.getId());
			return nextRefreshTime;
		}
		if (shopInfoBuilder.getNextRefreshTime() == nextRefreshTime) {
			return 0;
		}
		return nextRefreshTime;
	}

	/**
	 * 根据当前时间找到属于哪一个刷新节点.
	 * 
	 * @return
	 */
	private long findNextRefreshTime() {
		int clock = HawkTime.getHour();
		int type = this.getCurType();
		int[] refreshTimeArray = ConstProperty.getInstance().getTravelShopRefreshTimeArray();
		if(type == ActivityType.TRAVEL_SHOP_ASSIST_VALUE){
			TravelShopAssistKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(TravelShopAssistKVCfg.class);
			refreshTimeArray = kvCfg.getTravelShopRefreshTimeArr();
		}else if(type == ActivityType.DRAGON_BOAT_BENEFIT_VALUE){
			DragonBoatBenefitKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatBenefitKVCfg.class);
			refreshTimeArray = kvCfg.getTravelShopRefreshTimeArr();
		}
		int curClock = 0;
		for (int i = refreshTimeArray.length - 1; i >= 0; i--) {
			if (clock >= refreshTimeArray[i]) {
				curClock = refreshTimeArray[i];
				break;
			}
		}

		return findNextRefreshTime(curClock);
	}

	

	/**
	 * 根据当前的刷新时间, 找到下一个刷新时间点.
	 * 
	 * @param clock
	 * @return
	 */
	private long findNextRefreshTime(int clock) {
		int[] refreshTimeArray = ConstProperty.getInstance().getTravelShopRefreshTimeArray();
		int type = this.getCurType();
		if(type == ActivityType.TRAVEL_SHOP_ASSIST_VALUE){
			TravelShopAssistKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(TravelShopAssistKVCfg.class);
			refreshTimeArray = kvCfg.getTravelShopRefreshTimeArr();
		}else if(type == ActivityType.DRAGON_BOAT_BENEFIT_VALUE){
			DragonBoatBenefitKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatBenefitKVCfg.class);
			refreshTimeArray = kvCfg.getTravelShopRefreshTimeArr();
		}
		
		boolean tomorrow = false;
		int nextClock = 0;
		if (clock == 0 && refreshTimeArray[0] != 0) {
			nextClock = refreshTimeArray[0];
		} else {
			for (int i = 0; i < refreshTimeArray.length; i++) {
				if (refreshTimeArray[i] != clock) {
					continue;
				}
				if (i == refreshTimeArray.length - 1) {
					nextClock = refreshTimeArray[0];
					tomorrow = true;
				} else {
					nextClock = refreshTimeArray[i + 1];
				}
				break;
			}
		}

		long nextTime = HawkTime.getHourOfDayTime(HawkTime.getMillisecond(), nextClock);
		if (tomorrow) {
			nextTime += HawkTime.DAY_MILLI_SECONDS;
		}
		return nextTime;
	}
		
	private boolean triggerVipTravelGift(TravelShopInfoSync.Builder shopBuilder, int triggerTravelShopId, boolean isInActivity) {
		ConstProperty constProperty = ConstProperty.getInstance();
		constProperty.getTravelGiftBuyTimesLimit();
		DailyDataEntity dailyDataEntity = player.getData().getDailyDataEntity();
		if (dailyDataEntity.getVipTravelGiftBuyTimes() >= constProperty.getSpecialTravelGiftBuyTimesLimit()) {
			return false;
		}

		// 如果有则不刷新
		Optional<TravelShopItemMsg.Builder> giftOptional = shopBuilder.getItemsBuilderList().stream()
				.filter(itemBuilder -> {
					if (itemBuilder.hasGiftMsg()) {
						TravelShopGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(
								TravelShopGiftCfg.class, itemBuilder.getGiftMsg().getTravelShopGiftId());
						if (giftCfg.getType() == GsConst.TravelShopConstant.POOL_TYPE_VIP) {
							return true;
						} else {
							return false;
						}
					} else {
						return false;
					}
				}).findAny();
		if (giftOptional.isPresent()) {
			return false;
		}
		ConfigIterator<TravelShopGiftCfg> giftCfgIter = HawkConfigManager.getInstance().getConfigIterator(TravelShopGiftCfg.class);
		List<TravelShopGiftCfg> travelShopGiftCfgs = new ArrayList<>();
		List<Integer> rateList = new ArrayList<>();
		Map<Integer, Integer> dailyTravelBuyMap = player.getData().getDailyDataEntity().getTravelShopInfoMap();
		while (giftCfgIter.hasNext()) {
			TravelShopGiftCfg tsgc = giftCfgIter.next();
			if (tsgc.getType() == GsConst.TravelShopConstant.POOL_TYPE_VIP) {
				int boughtNum = dailyTravelBuyMap.getOrDefault(tsgc.getId(), 0);
				int boughtNumLimit = tsgc.getDailyBuyTimes();
				if (isInActivity) {
					boughtNumLimit = tsgc.getLinLangBuyTimes();
				}
				if (tsgc.getDailyBuyTimes() > 0 && boughtNum >= boughtNumLimit) {
					continue;
				}
				travelShopGiftCfgs.add(tsgc);
				rateList.add(tsgc.getWeight());
			}
			
		}
		if (travelShopGiftCfgs.isEmpty()) {
			return false;
		}
		int num = RedisProxy.getInstance().getVipTravelGiftProb(player.getId());
		int curProb = constProperty.getSpecialTravelGiftActivateAddProb() * num
				+ constProperty.getSpecialTravelGiftActivateInitProb();
		int randNum = HawkRand.randInt(0, 10000);
		if (curProb >= randNum) {
			TravelShopGiftCfg travelShopGiftCfg = HawkRand.randomWeightObject(travelShopGiftCfgs, rateList);

			TravelGiftItemMsg.Builder travelGiftItem = buildTravelGiftItemMsgBuilder(travelShopGiftCfg);
			shopBuilder.getItemsBuilderList().stream().forEach(itemBuilder -> {
				if (itemBuilder.getCfgId() == triggerTravelShopId) {
					itemBuilder.setGiftMsg(travelGiftItem);
				}
			});

			RedisProxy.getInstance().addOrUpdateVipTravelGiftProb(player.getId(), 0);
			LogUtil.logTravelShopGiftRefresh(player, travelShopGiftCfg.getId());
			logger.info("travelGiftTrigger playerId: {}, travelGiftItemId: {}", player.getId(), travelShopGiftCfg.getId());
			return true;
		} else {
			RedisProxy.getInstance().addOrUpdateVipTravelGiftProb(player.getId(), num + 1);
			return false;
		}
	}

	private boolean triggerTravelGift(TravelShopInfoSync.Builder shopBuilder, int triggerTravelShopId, boolean isInActivity) {
		DailyDataEntity dailyDataEntity = player.getData().getDailyDataEntity();
		if (dailyDataEntity.getTravelGiftBuyTimes() >= ConstProperty.getInstance().getTravelGiftBuyTimesLimit()) {
			return false;
		}
		// 如果有则不清理
		Optional<TravelShopItemMsg.Builder> giftOptional = shopBuilder.getItemsBuilderList().stream().filter(itemBuilder -> {
				if (!itemBuilder.hasGiftMsg()) {
					return false;
				}
				TravelShopGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(TravelShopGiftCfg.class, itemBuilder.getGiftMsg().getTravelShopGiftId());
				return giftCfg.getType() == GsConst.TravelShopConstant.POOL_TYPE_NORMAL;
			}).findAny();
		if (giftOptional.isPresent()) {
			return false;
		}
		
		ConfigIterator<TravelShopGiftCfg> giftCfgIter = HawkConfigManager.getInstance().getConfigIterator(TravelShopGiftCfg.class);
		List<TravelShopGiftCfg> travelShopGiftCfgs = new ArrayList<>();
		List<Integer> rateList = new ArrayList<>();
		Map<Integer, Integer> dailyTravelBuyMap = player.getData().getDailyDataEntity().getTravelShopInfoMap();
		while (giftCfgIter.hasNext()) {
			TravelShopGiftCfg tsgc = giftCfgIter.next();
			if (tsgc.getType() == GsConst.TravelShopConstant.POOL_TYPE_NORMAL) {
				int boughtNum = dailyTravelBuyMap.getOrDefault(tsgc.getId(), 0);
				int boughtNumLimit = tsgc.getDailyBuyTimes();
				if (isInActivity) {
					boughtNumLimit = tsgc.getLinLangBuyTimes();
				}
				if (tsgc.getDailyBuyTimes() > 0 && boughtNum >= boughtNumLimit) {
					continue;
				}		
				if (tsgc.getWeight() <= 0) {
					continue;
				}
				travelShopGiftCfgs.add(tsgc);
				rateList.add(tsgc.getWeight());
			}
		}
		
		if (travelShopGiftCfgs.isEmpty()) {
			return false;
		}
		int num = RedisProxy.getInstance().getTravelGiftProb(player.getId());
		int curProb = ConstProperty.getInstance().getTravelGiftActivateAddProb() * num + ConstProperty.getInstance().getTravelGiftActivateInitProb();
		int randNum = HawkRand.randInt(0, 10000);
		if (curProb >= randNum) {
			TravelShopGiftCfg travelShopGiftCfg = HawkRand.randomWeightObject(travelShopGiftCfgs, rateList);
			TravelGiftItemMsg.Builder travelGiftItem = buildTravelGiftItemMsgBuilder(travelShopGiftCfg);
			shopBuilder.getItemsBuilderList().stream().forEach(itemBuilder -> {
				if (itemBuilder.getCfgId() == triggerTravelShopId) {
					itemBuilder.setGiftMsg(travelGiftItem);
				}
			});

			RedisProxy.getInstance().addOrUpdateTravelGiftProb(player.getId(), 0);
			LogUtil.logTravelShopGiftRefresh(player, travelShopGiftCfg.getId());
			logger.info("travelGiftTrigger playerId: {}, travelGiftItemId: {}", player.getId(), travelShopGiftCfg.getId());
			return true;
		}
		
		RedisProxy.getInstance().addOrUpdateTravelGiftProb(player.getId(), num + 1);
		return false;
	}

	private TravelGiftItemMsg.Builder buildTravelGiftItemMsgBuilder(TravelShopGiftCfg travelShopGiftCfg) {
		TravelGiftItemMsg.Builder travelGiftItemMsgBuilder = TravelGiftItemMsg.newBuilder();
		travelGiftItemMsgBuilder.setBuyNum(0);
		travelGiftItemMsgBuilder.setTravelShopGiftId(travelShopGiftCfg.getId());
		return travelGiftItemMsgBuilder;
	}
	
	/**
	 * 黑金礼包的购买.
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.TRAVEL_GIFT_BUY_C_VALUE)
	private void onTravelGiftBuy(HawkProtocol protocol) {
		TravelGiftBuyC cparam = protocol.parseProtocol(TravelGiftBuyC.getDefaultInstance());
		TravelShopInfoSync.Builder shopBuilder = RedisProxy.getInstance().getTravelShopInfo(player.getId());
		TravelGiftItemMsg.Builder giftBuilder = travelGiftBuyCheck(shopBuilder, cparam);
		if (giftBuilder == null) {
			return;
		}
		
		int giftId = cparam.getTravelShopGiftId();
		TravelShopGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(TravelShopGiftCfg.class, giftId);
		if (!travelGiftConsumeAndReward(giftCfg)) {
			return;
		}
		
		List<ItemInfo> priceItems = giftCfg.getPriceList();
		int costMoney = (int)priceItems.get(0).getCount();
		int moneyType = priceItems.get(0).getItemId();
		// 特惠商人购买黑金礼包打点记录
		LogUtil.logGiftBagFlow(player, GiftType.TRAVEL_SHOP_GIFT, String.valueOf(giftId), costMoney, moneyType, 0);
		if (giftCfg.getDailyBuyTimes() > 0) {
			player.getData().getDailyDataEntity().addTravelShopBoughtInfo(giftId);
		}
		
		// 联盟成员发放礼物
		int allianceGift = giftCfg.getAllianceGift();
		if (!player.isCsPlayer() && player.hasGuild() && allianceGift > 0) {
			GuildService.getInstance().bigGift(player.getGuildId()).addSmailGift(allianceGift, false);
		}
		
		giftBuilder.setBuyNum(giftBuilder.getBuyNum() + 1);
		RedisProxy.getInstance().addOrUpdateTravelShop(player.getId(), shopBuilder);
		DailyDataEntity dailyDataEntity = player.getData().getDailyDataEntity();
		if (giftCfg.getType() == GsConst.TravelShopConstant.POOL_TYPE_NORMAL) {
			dailyDataEntity.setTravelGiftBuyTimes(dailyDataEntity.getTravelGiftBuyTimes() + 1);
		} else {
			dailyDataEntity.setVipTravelGiftBuyTimes(dailyDataEntity.getVipTravelGiftBuyTimes() + 1);
		}		 

		logger.info("travelGiftBuy playerId: {}, giftId: {}", player.getId(), cparam.getTravelShopGiftId());

		this.respTravelGiftBuy(Status.SysError.SUCCESS_OK_VALUE, giftBuilder);
		//特惠商人助力庆典活动
		ActivityManager.getInstance().postEvent(new TravelShopBuyBlackGoldPackageEvent(player.getId(), 1));
		int costDiamond = GameUtil.getItemNumByItemId(giftCfg.getPriceList(), ItemType.PLAYER_ATTR, PlayerAttr.DIAMOND_VALUE);
		if (costDiamond > 0) {
			ActivityManager.getInstance().postEvent(new CostDiamondBuyGiftEvent(player.getId(), costDiamond));
		}

		//特惠商人助力庆典，金条消耗
		int curType = this.getCurType();
		if(curType == ActivityType.TRAVEL_SHOP_ASSIST_VALUE || curType == ActivityType.DRAGON_BOAT_BENEFIT_VALUE ){
			//ActivityManager.getInstance().postEvent(new TravelShopTimeLimitCostEvent(player.getId(),priceItems.get(0).getItemId(),priceItems.get(0).getCount()));
			ActivityManager.getInstance().postEvent(new TravelShopPurchaseEvent(player.getId(), priceItems.get(0).getItemId(), (int)priceItems.get(0).getCount(), true, false));
		}
	}
	
	/**
	 *  黑市礼包购买条件判断
	 * @param shopBuilder
	 * @param cparam
	 * @return
	 */
	private TravelGiftItemMsg.Builder travelGiftBuyCheck(TravelShopInfoSync.Builder shopBuilder, TravelGiftBuyC cparam) {
		int giftId = cparam.getTravelShopGiftId();
		TravelShopGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(TravelShopGiftCfg.class, giftId);
		if (giftCfg == null) {
			respTravelGiftBuy(Status.Error.TRAVEL_GIFT_NOT_EXIST_VALUE);
			logger.warn("travelGift cftId not exist playerId:{}, giftId:{}", player.getId(), cparam.getTravelShopGiftId());
			return null;
		}

		TravelGiftItemMsg.Builder giftBuilder = null;
		for (TravelShopItemMsg.Builder itemBuilder : shopBuilder.getItemsBuilderList()) {
			if (itemBuilder.hasGiftMsg() && itemBuilder.getGiftMsg().getTravelShopGiftId() == cparam.getTravelShopGiftId()) {
				giftBuilder = itemBuilder.getGiftMsgBuilder();
				break;
			}
		}

		if (giftBuilder == null) {
			respTravelGiftBuy(Status.Error.TRAVEL_GIFT_NOT_EXIST_VALUE);
			logger.warn("travelGift id not refresh playerId:{}, giftId:{}", player.getId(), cparam.getTravelShopGiftId());
			return null;
		}

		if (giftBuilder.getBuyNum() > 0) {
			respTravelGiftBuy(Status.Error.TRAVEL_GIFT_ALREADY_BOUGHT_VALUE);
			logger.warn("travelGift already bought playerId:{}", player.getId());
			return null;
		}
		
		return giftBuilder;
	}
	
	/**
	 * 黑市礼包购买消耗、获取奖励
	 * @param giftCfg
	 */
	private boolean travelGiftConsumeAndReward(TravelShopGiftCfg giftCfg) {
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		List<ItemInfo> priceItems = giftCfg.getPriceList();
		consumeItems.addConsumeInfo(priceItems);
		int rlt = consumeItems.checkConsumeAndGetResult(player);
		if (rlt != Status.SysError.SUCCESS_OK_VALUE) {
			this.respTravelGiftBuy(rlt);
			return false;
		}
		
		if (consumeItems.getBuilder().hasAttrInfo() && consumeItems.getBuilder().getAttrInfo().getDiamond() > 0) {
			try {
				if (!giftCfg.getCrystalRewardList().isEmpty()) {
					ItemInfo itemInfo = giftCfg.getCrystalRewardList().get(0);
					consumeItems.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), 1, (int)itemInfo.getCount()));
				}
				
				if (!giftCfg.getSpecialRewardList().isEmpty()) {
					for (ItemInfo itemInfo : giftCfg.getSpecialRewardList()) {
						ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
						consumeItems.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), itemCfg.getSellPrice(), (int)itemInfo.getCount()));
					}
				}
				
				if (!giftCfg.getOrdinaryRewardList().isEmpty()) {
					for (ItemInfo itemInfo : giftCfg.getOrdinaryRewardList()) {
						ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
						consumeItems.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), itemCfg.getSellPrice(), (int)itemInfo.getCount()));
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		consumeItems.consumeAndPush(player, Action.TRAVEL_GIFT_BUY);

		SystemMailService.getInstance()
				.sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.TRAVLE_SHOP_GIFT)
						.addRewards(giftCfg.getCrystalRewardList()).addRewards(giftCfg.getSpecialRewardList())
						.addRewards(giftCfg.getOrdinaryRewardList()).build());

		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(giftCfg.getCrystalRewardList());
		awardItems.addItemInfos(giftCfg.getSpecialRewardList());
		awardItems.addItemInfos(giftCfg.getOrdinaryRewardList());
		awardItems.rewardTakeAffectAndPush(player, Action.TRAVEL_GIFT_BUY, true, RewardOrginType.SHOPPING_GIFT);
		return true;
	}

	private void respTravelGiftBuy(int errorCode, TravelGiftItemMsg.Builder giftItemMsg) {
		TravelGiftBuyS.Builder sbuilder = TravelGiftBuyS.newBuilder();
		sbuilder.setRlt(errorCode);
		if (giftItemMsg != null) {
			sbuilder.setGiftItemMsg(giftItemMsg);
		}

		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.TRAVEL_GIFT_BUY_S_VALUE, sbuilder);
		player.sendProtocol(protocol);
	}

	private void respTravelGiftBuy(int errorCode) {
		TravelGiftBuyS.Builder sbuilder = TravelGiftBuyS.newBuilder();
		sbuilder.setRlt(errorCode);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.TRAVEL_GIFT_BUY_S_VALUE, sbuilder);
		player.sendProtocol(protocol);
	}

	@ProtocolHandler(code = { HP.code.TRAVEL_SHOP_INFO_C_VALUE })
	private void onTravleShopInfoC(HawkProtocol protocol) {
		logger.warn("traleShop playerId:{}, isUnlock:{}", player.getId(), this.isUnlock);
		if (this.isUnlock) {
			this.synGoodsInfo();
		}
	}
	
	public void setBuildingStateAndPush(BuildingStatus state) {
		BuildingBaseEntity entity = this.getTravelShopBuilding();
		if (entity == null) {
			logger.error("playerId:{} travel building entity is null", player.getId());
			return;
		}
		
		if (entity.getStatus() != state.getNumber()) {
			player.getPush().pushBuildingStatus(entity, state);
		}
	}
	
	@ProtocolHandler(code = HP.code.TRAVEL_SHOP_OPEN_REQ_VALUE)
	private void onTravelShopOpenReq(HawkProtocol protocol) {
		BuildingBaseEntity entity = this.getTravelShopBuilding();
		// 非限时商店气泡，直接改状态
		if (entity.getStatus() != BuildingStatus.TIME_LIMIT_STORE_VALUE) {
			this.setBuildingStateAndPush(BuildingStatus.COMMON);
			return;
		}
		
		// 限时商店还没结束，但商品已经售完了（没有售完不改状态）
		if (timeLimitStoreSellOut()) {
			this.setBuildingStateAndPush(BuildingStatus.TRAVEL_SHOP_REFRESH);
		}
	}
	
	/**
	 * 判断限时商店是否已售完
	 * 
	 * @return
	 */
	private boolean timeLimitStoreSellOut() {
		TimeLimitStoreConditionInfo onSaleStore = player.getOnSellStoreCondition();
		if (onSaleStore == null || isTimeLimitStoreEnd(onSaleStore)) {
			return true;
		}
		
		ConfigIterator<TimeLimitStoreCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(TimeLimitStoreCfg.class);
		while (iterator.hasNext()) {
			TimeLimitStoreCfg cfg = iterator.next();
			if (onSaleStore.getTriggerType() != cfg.getTriggerType()) {
				continue;
			}
			
			Integer alreadyBuy = timeLimitBoughtInfo.get(cfg.getId());
			if (alreadyBuy == null || alreadyBuy < cfg.getLimitNum()) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
     * 限时商店触发信息
     * @param msg
     * 
     * @return
     */
    @MessageHandler
    private boolean onTriggerTimeLimitStore(TimeLimitStoreTriggerMsg msg) {
    	if (!isUnlock || SystemControler.getInstance().isSystemItemsClosed(ControlerModule.INDEPENDENT_ARMS)) {
    		return false;
    	}
    	
    	// 因为在下线时timeLimitStoreOpenTime会清零，所以上限后第一次触发，无论如何不会时同一天，故而从redis取最新数据
    	if (!HawkTime.isSameDay(HawkApp.getInstance().getCurrentTime(), timeLimitStoreOpenTime)) {
    		timeLimitStoreOpenTime = HawkApp.getInstance().getCurrentTime();
    		timeLimitStoreOpenTimesDay = RedisProxy.getInstance().getTimeLimitStoreDayOpenTimes(player.getId());
    	}
    	
    	if (timeLimitStoreOpenTimesDay >= ConstProperty.getInstance().getTimeLimitShopTriggerTimes()) {
    		HawkLog.logPrintln("triggerTimeLimitStore failed, day trigger times exceed, playerId: {}, openTimes: {}", player.getId(), timeLimitStoreOpenTimesDay);
    		return false;
    	}
    	
    	final int triggerType = msg.getTriggerType();
    	final int triggerNum = msg.getTriggerNum();
    	if (triggerNum <= 0 || !TimeLimitStoreTypeCfg.checkTriggerType(triggerType)) {
    		HawkLog.logPrintln("triggerTimeLimitStore failed, playerId: {}, triggerType: {}, triggerNum: {}", player.getId(), triggerType, triggerNum);
    		return false;
    	}
    	
    	
    	TimeLimitStoreConditionInfo onSaleStore = player.getOnSellStoreCondition();
    	// 玩家不在线时，如果触发了，要先进行初始化
    	if (onSaleStore == null && !player.isActiveOnline()) {
    		Map<Integer, TimeLimitStoreConditionInfo> storeMap = player.getTimeLimitStoreConditionMap();
    		if (!storeMap.isEmpty()) {
    			for (TimeLimitStoreConditionInfo store : storeMap.values()) {
    				if (!isTimeLimitStoreEnd(store)) {
    					player.setOnSellStoreCondition(store);
    					HawkLog.logPrintln("trigger initTimeLimitOnSaleStore success, playerId: {}, triggerType: {}, startTime: {}", 
    							player.getId(), store.getTriggerType(), store.getStartTime());
    					break;
    				}
    			}
    			
    			onSaleStore = player.getOnSellStoreCondition();
    			if (onSaleStore != null) {
    				storeMap.clear();
    				timeLimitBoughtInfo = RedisProxy.getInstance().getTimeLimitStoreBoughtInfo(player.getId());
    			}
    		}
    	}
    	
		if (onSaleStore != null) {
			
			HawkLog.logPrintln("triggerTimeLimitStore, onSaleStore exist, playerId: {}, triggerType: {}, state: {}, startTime: {}", player.getId(), 
					onSaleStore.getTriggerType(), onSaleStore.getState(), onSaleStore.getStartTime());
			
			if (!isTimeLimitStoreEnd(onSaleStore)) {
				return false;
			}
			
			// 本期商店出售倒计时结束
			onStoreEnd();
		}
    	
    	long timeNow = HawkTime.getMillisecond();
    	TimeLimitStoreConditionInfo conditionInfo = player.getTimeLimitStoreCondition(triggerType);
    	if (conditionInfo == null || conditionInfo.getState() == ConditionState.SELL) {
    		player.getTimeLimitStoreConditionMap().remove(triggerType);
    		conditionInfo = new TimeLimitStoreConditionInfo(player.getId(), triggerType);
    		conditionInfo.setStartTime(timeNow);
    		player.addTimeLimitStoreCondition(conditionInfo);
    		
    		HawkLog.logPrintln("triggerTimeLimitStore, add condition store, playerId: {}, triggerType: {}", player.getId(), triggerType);
    	}
    	
    	if (conditionInfo.getState() != ConditionState.INIT) {
    		HawkLog.logPrintln("triggerTimeLimitStore failed, playerId: {}, triggerType: {}, condition store state: {}", player.getId(), triggerType, conditionInfo.getState());
    		return true;
    	}
    	
    	TimeLimitStoreTypeCfg storeTypeCfg = HawkConfigManager.getInstance().getConfigByKey(TimeLimitStoreTypeCfg.class, triggerType);
    	long startTime = timeNow;
    	Map<Long, Integer> timeFinishCount = conditionInfo.getTimeFinishCount();
    	// 第一次触发这个类型，getStartTime结果为0，必然走这个逻辑
    	if (timeNow - conditionInfo.getStartTime() > storeTypeCfg.getTriggerDuration() * 1000L) {
    		if (timeFinishCount.isEmpty()) {
    			conditionInfo.setStartTime(startTime);
    			conditionInfo.setTotalCount(0);
    		} else {
    			// 删除已过时的信息
    			Set<Long> timeSet = new HashSet<Long>(timeFinishCount.keySet());
    			for (Long time : timeSet) {
    				if (timeNow - time >= storeTypeCfg.getTriggerDuration() * 1000L) {
    					conditionInfo.setTotalCount(conditionInfo.getTotalCount() - timeFinishCount.get(time));
    					timeFinishCount.remove(time);
    				} else if (time < startTime) {
    					startTime = time;
    				}
    			}
    			
    			conditionInfo.setStartTime(startTime);
    		}
    		
    		HawkLog.logPrintln("triggerTimeLimitStore, remove timeout trigger count, playerId: {}, triggerType: {}, total: {}, startTime: {}", 
    				player.getId(), triggerType, conditionInfo.getTotalCount(), conditionInfo.getStartTime());
    	}
    	
    	timeFinishCount.put(timeNow, triggerNum);
    	conditionInfo.setTotalCount(conditionInfo.getTotalCount() + triggerNum);
    	
    	HawkLog.logPrintln("triggerTimeLimitStore success, playerId: {}, triggerType: {}, total: {}, need num: {}", player.getId(), triggerType, 
    			conditionInfo.getTotalCount(), storeTypeCfg.getNum());
    	
    	if (conditionInfo.getTotalCount() < storeTypeCfg.getNum()) {
    		RedisProxy.getInstance().updateTimeLimitStoreCondition(conditionInfo, storeTypeCfg.getTriggerDuration());
    	} else {
    		// 商店有新的商品上架出售，出售倒计时开始
    		conditionInfo.setStartTime(timeNow);
    		conditionInfo.setState(ConditionState.SELL);
    		conditionInfo.getTimeFinishCount().clear();
    		player.setOnSellStoreCondition(conditionInfo);
    		player.getTimeLimitStoreConditionMap().clear();
    		if (timeLimitBoughtInfo != null) {
    			timeLimitBoughtInfo.clear();
    		}
    		
    		timeLimitStoreOpenTimesDay += 1;
    		RedisProxy.getInstance().updateTimeLimitStoreDayOpenTimes(player.getId(), timeLimitStoreOpenTimesDay);
    		RedisProxy.getInstance().removeAllTimeLimitStoreBoughtInfo(player.getId());
    		RedisProxy.getInstance().removeAllTimeLimitStoreCondition(player.getId());
    		RedisProxy.getInstance().updateTimeLimitStoreCondition(conditionInfo, TimeLimitStoreTypeCfg.getShopDurationByType(conditionInfo.getTriggerType()));
    		
    		pushTimeLimitStoreInfo();
    		this.setBuildingStateAndPush(BuildingStatus.TIME_LIMIT_STORE);
			 
    		// 打点日志
    		LogUtil.logTimeLimitStoreOnSell(player, triggerType);
    	}
    	
    	return true;
    }
    
    /**
     * 购买限时商店物品
     * 
     * @param protocol
     */
    @ProtocolHandler(code = HP.code.TIMELIMIT_STORE_BUY_C_VALUE)
	private boolean onTimeLimitItemBuy(HawkProtocol protocol) {
    	TimeLimitStoreConditionInfo onSaleStore = player.getOnSellStoreCondition();
    	if (!timeLimitBuyCheck(onSaleStore, protocol)) {
    		return false;
    	}
    	
    	TimeLimitStoreBuyReq req = protocol.parseProtocol(TimeLimitStoreBuyReq.getDefaultInstance());
    	int shopCfgId = req.getShopCfgId();
    	TimeLimitStoreCfg storeCfg = HawkConfigManager.getInstance().getConfigByKey(TimeLimitStoreCfg.class, shopCfgId);
    	int buyCount = req.getBuyCount();
    	ItemInfo consumeItem = timeLimitConsumeAndReward(storeCfg, buyCount, protocol);
    	if (consumeItem == null) {
    		return false;
    	}
		
    	int count = timeLimitBoughtInfo.containsKey(shopCfgId) ? timeLimitBoughtInfo.get(shopCfgId) : 0;
    	count += buyCount;
		timeLimitBoughtInfo.put(shopCfgId, count);
		long startTime = onSaleStore.getStartTime() > 0 ? onSaleStore.getStartTime() : HawkApp.getInstance().getCurrentTime();
		long expireTime = startTime + TimeLimitStoreTypeCfg.getShopDurationByType(onSaleStore.getTriggerType()) * 1000L - HawkApp.getInstance().getCurrentTime();
		RedisProxy.getInstance().updateTimeLimitStoreBoughtInfo(player.getId(), shopCfgId, count, (int) (expireTime / 1000));
		
		// 给客户端同步信息
		pushTimeLimitStoreInfo();
		
		// 判断商品有没有售完
		if (timeLimitStoreSellOut()) {
			this.setBuildingStateAndPush(BuildingStatus.TRAVEL_SHOP_REFRESH);
		}
		
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, storeCfg.getGoodsItem().getItemId());
		int itemType = itemCfg != null ? itemCfg.getItemType() : ItemType.TOOL_VALUE;
		int costMoney = (int) (storeCfg.getPriceItem().getCount() * buyCount);
		LogUtil.logGiftBagFlow(player, GiftType.TIMELIMIT_STORE_ITEM, String.valueOf(shopCfgId), costMoney, consumeItem.getItemId(), itemType, buyCount);
		
		//特惠商人助力庆典，限时专区购买消耗
		int curType = this.getCurType();
		if(curType == ActivityType.TRAVEL_SHOP_ASSIST_VALUE || curType == ActivityType.DRAGON_BOAT_BENEFIT_VALUE ){
			//ActivityManager.getInstance().postEvent(new TravelShopTimeLimitCostEvent(player.getId(),consumeItem.getItemId(),consumeItem.getCount()));
			ActivityManager.getInstance().postEvent(new TravelShopPurchaseEvent(player.getId(), consumeItem.getItemId(), (int)consumeItem.getCount(), true, false));
		}
		HawkLog.logPrintln("onTimeLimitItemBuy, playerId: {}, triggerType: {}, goodsId: {}, buyCount: {}", player.getId(), storeCfg.getTriggerType(), shopCfgId, buyCount);
		return true;
    }
    
    /**
     * 限时商店购买条件检测
     * @param onSaleStore
     * @param protocol
     * @return
     */
    private boolean timeLimitBuyCheck(TimeLimitStoreConditionInfo onSaleStore, HawkProtocol protocol) {
    	if (onSaleStore == null) {
			sendError(protocol.getType(), Status.Error.TIME_LIMIT_STORE_END_VALUE);
    		HawkLog.logPrintln("onTimeLimitItemBuy break, onSaleStore not exist, playerId: {}", player.getId());
			return false;
		}
		
		// 结束了
		if (isTimeLimitStoreEnd(onSaleStore)) {
			sendError(protocol.getType(), Status.Error.TIME_LIMIT_STORE_END_VALUE);
    		HawkLog.logPrintln("onTimeLimitItemBuy break, onSaleStore end, playerId: {}, state: {}, startTime: {}", player.getId(), onSaleStore.getState(), onSaleStore.getStartTime());
			return false;
		}
		
    	TimeLimitStoreBuyReq req = protocol.parseProtocol(TimeLimitStoreBuyReq.getDefaultInstance());
    	int shopCfgId = req.getShopCfgId();
    	// 商品库是否存在
    	TimeLimitStoreCfg storeCfg = HawkConfigManager.getInstance().getConfigByKey(TimeLimitStoreCfg.class, shopCfgId);
    	if (storeCfg == null) {
    		sendError(protocol.getType(), Status.SysError.CONFIG_ERROR_VALUE);
    		HawkLog.logPrintln("onTimeLimitItemBuy break, TimeLimitStoreCfg not exist, playerId: {}, shopCfgId: {}", player.getId(), shopCfgId);
    		return false;
    	}
    	
    	int triggerType = storeCfg.getTriggerType();
    	// 是否是当前可购买的商品库
    	if (onSaleStore.getTriggerType() != triggerType) {
    		sendError(protocol.getType(), Status.Error.TIME_LIMIT_STORE_NOT_MATCH_VALUE);
    		HawkLog.logPrintln("onTimeLimitItemBuy break, triggerType not match, playerId: {}, onSaleStore trigger: {}, cfg triggerType: {}", 
    				player.getId(), onSaleStore.getTriggerType(), triggerType);
    		return false;
    	}
    	
    	// 防止之前没有加载成功
    	if (timeLimitBoughtInfo == null) {
    		timeLimitBoughtInfo = RedisProxy.getInstance().getTimeLimitStoreBoughtInfo(player.getId());
    	}
    	
    	int buyCount = req.getBuyCount();
    	int count = timeLimitBoughtInfo.containsKey(shopCfgId) ? timeLimitBoughtInfo.get(shopCfgId) : 0;
    	count += buyCount;
    	// 已购买数量是否超上限
    	if (count > storeCfg.getLimitNum()) {
    		sendError(protocol.getType(), Status.Error.TIME_LIMIT_BUY_COUNT_EXCEED_VALUE);
    		HawkLog.logPrintln("onTimeLimitItemBuy break, item buy count exceed, playerId: {}, triggerType: {}, goodsId: {}, alreadyBuyCount: {}, buyCount: {}, limit: {}", 
    				player.getId(), triggerType, shopCfgId, count, buyCount, storeCfg.getLimitNum());
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * 显示商店购买消耗、获取奖励
     * @param storeCfg
     * @param buyCount
     * @param protocol
     * @return
     */
    private ItemInfo timeLimitConsumeAndReward(TimeLimitStoreCfg storeCfg, int buyCount, HawkProtocol protocol) {
    	ItemInfo consumeItem = storeCfg.getPriceItem();
    	long costMoney = consumeItem.getCount() * buyCount;
    	
    	consumeItem.setCount(costMoney);
    	ConsumeItems consume = ConsumeItems.valueOf();
    	consume.addConsumeInfo(consumeItem, false);
    	// 金钱是否充足
    	if (!consume.checkConsume(player, protocol.getType())) {
    		HawkLog.logPrintln("onTimeLimitItemBuy break, consume error, playerId: {}, triggerType: {}, goodsId: {}, buyCount: {}, costMoney: {}", 
    				player.getId(), storeCfg.getTriggerType(), storeCfg.getId(), buyCount, costMoney);
			return null;
		}
    	
    	ItemInfo goodsItem = storeCfg.getGoodsItem();
    	ItemCfg goodsItemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, goodsItem.getItemId());
    	if (goodsItemCfg != null) {
    		consume.addPayItemInfo(new PayItemInfo(String.valueOf(goodsItem.getItemId()), goodsItemCfg.getSellPrice(), (int)goodsItem.getCount()));
    	}
    	consume.consumeAndPush(player, Action.LIMITTIME_SHOP_BUY);
    	goodsItem.setCount(goodsItem.getCount() * buyCount);
    	AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItem(goodsItem);
		awardItem.rewardTakeAffectAndPush(player, Action.LIMITTIME_SHOP_BUY, true);
		return consumeItem;
    }
    
    /**
     * 限时商店结束
     */
    private void onStoreEnd() {
    	player.setOnSellStoreCondition(null);
		if (timeLimitBoughtInfo != null) {
			timeLimitBoughtInfo.clear();
		}
		
		// 通知客户端
		pushTimeLimitStoreInfo(true);
    }
    
    /**
     * 同步限时商店商品库信息
     */
    private void pushTimeLimitStoreInfo() {
    	pushTimeLimitStoreInfo(false);
    }
    
    /**
     * 同步限时商店商品库信息
     * 
     * @param end 是否是结束推送
     */
    private void pushTimeLimitStoreInfo(boolean end) {
    	TimeLimitStoreConditionInfo onsellStore = player.getOnSellStoreCondition();
    	if (!end && onsellStore == null) {
    		return;
    	}
    	
		 TimeLimitStoreGroupInfo.Builder builder = TimeLimitStoreGroupInfo.newBuilder();
		 builder.setNofityEnd(end);
		 if (end) {
			 builder.setGroupId(0);
			 builder.setEndTime(0);
			 BuildingBaseEntity entity = this.getTravelShopBuilding();
			 if (entity != null && entity.getStatus() == BuildingStatus.TIME_LIMIT_STORE_VALUE) {
				this.setBuildingStateAndPush(BuildingStatus.TRAVEL_SHOP_REFRESH);
			 }
		 } else {
			 if (timeLimitBoughtInfo == null) {
		    	timeLimitBoughtInfo = RedisProxy.getInstance().getTimeLimitStoreBoughtInfo(player.getId());
		     }
			 
			 builder.setGroupId(onsellStore.getTriggerType());
			 builder.setEndTime(onsellStore.getStartTime() + TimeLimitStoreTypeCfg.getShopDurationByType(onsellStore.getTriggerType()) * 1000L);
			 for (Entry<Integer, Integer> entry : timeLimitBoughtInfo.entrySet()) {
				 TimeLimitStoreGoodsBuyInfo.Builder goodsInfo = TimeLimitStoreGoodsBuyInfo.newBuilder();
				 goodsInfo.setGoodsId(entry.getKey());
				 goodsInfo.setBoughtCount(entry.getValue());
				 builder.addGoodsBoughtInfo(goodsInfo);
			 }
		 }
		 
		 sendProtocol(HawkProtocol.valueOf(HP.code.TIMELIMIT_STORE_SYNC_S, builder));
    }
	
    /**
     * 判断出售中的商品库是否已结束
     * 
     * @param store
     * @return
     */
    private boolean isTimeLimitStoreEnd(TimeLimitStoreConditionInfo store) {
    	if (store == null) {
    		return true;
    	}
    	
    	if (store.getState() != ConditionState.SELL) {
    		return true;
    	}
    	
    	int duration = TimeLimitStoreTypeCfg.getShopDurationByType(store.getTriggerType());
		if (HawkApp.getInstance().getCurrentTime() - store.getStartTime() >= duration * 1000L) {
			return true;
		}
		
		return false;
    }

	@MessageHandler
	private void onTravelShopFriendlyCardBuy(TravelShopFriendlyCardBuyMsg msg) {
		// 更新builder信息
		TravelShopFriendly friendlyInfo = RedisProxy.getInstance().getTravelShopFriendlyInfo(player.getId());
		long curTime = HawkTime.getMillisecond();
		friendlyInfo.setPrivilegeStartTime(curTime);
		RedisProxy.getInstance().updateTravelShopFriendlyInfo(player.getId(),friendlyInfo);
		
		TravelShopInfoSync.Builder travelShopInfoBuilder = RedisProxy.getInstance().getTravelShopInfo(player.getId());
		synGoodsInfo(travelShopInfoBuilder);
	}
	
	/**
	 * 黑市商店友好度增加推送
	 * @param addCount
	 * @param afterCount
	 */
	private void travelShopFriendlyAddPush(TravelShopCfg travelShopCfg) {
		TravelShopFriendly friendlyInfo = RedisProxy.getInstance().getTravelShopFriendlyInfo(player.getId());
		boolean cardOpen = friendlyInfo.privilegeEffect();
		// 更新builder信息
		int addFriendly = 0;
		if (cardOpen) {
			int addCount = ConstProperty.getInstance().getTravelShopFriendly(travelShopCfg.getItemPrice().getItemId());
			addFriendly = addCount * (1000 + ConstProperty.getInstance().getTravelShopFriendlyUpRate()) / 1000;
			
		}else{
			addFriendly = ConstProperty.getInstance().getTravelShopFriendly(travelShopCfg.getItemPrice().getItemId());
		}
		
		friendlyInfo.addFriendlyScore(addFriendly, cardOpen);
		RedisProxy.getInstance().updateTravelShopFriendlyInfo(player.getId(), friendlyInfo);
		// 同步好友度增加
		TravelShopFriendlyAddPush.Builder builder = TravelShopFriendlyAddPush.newBuilder();
		builder.setAddFriendly(addFriendly);
		builder.setAfterFriendly(friendlyInfo.getFriendly());
		builder.setFriendlyCommAwardCount(friendlyInfo.getFriendlyCommAwardCount());
		builder.setFriendlyPrivilegeAwardCount(friendlyInfo.getFriendlyPrivilegeAwardCount());
		sendProtocol(HawkProtocol.valueOf(HP.code2.TRAVEL_SHOP_FRIENDLY_ADD_PUSH, builder));
	}
	
	public void travelShopFriendlyAddPush(long addCount) {
		TravelShopFriendly friendlyInfo = RedisProxy.getInstance().getTravelShopFriendlyInfo(player.getId());
		boolean cardOpen = friendlyInfo.privilegeEffect();
		// 更新builder信息
		friendlyInfo.addFriendlyScore(addCount, cardOpen);
		RedisProxy.getInstance().updateTravelShopFriendlyInfo(player.getId(), friendlyInfo);
		// 同步好友度增加
		TravelShopFriendlyAddPush.Builder builder = TravelShopFriendlyAddPush.newBuilder();
		builder.setAddFriendly((int)addCount);
		builder.setAfterFriendly(friendlyInfo.getFriendly());
		builder.setFriendlyCommAwardCount(friendlyInfo.getFriendlyCommAwardCount());
		builder.setFriendlyPrivilegeAwardCount(friendlyInfo.getFriendlyPrivilegeAwardCount());
		sendProtocol(HawkProtocol.valueOf(HP.code2.TRAVEL_SHOP_FRIENDLY_ADD_PUSH, builder));
	}
	
	
	@ProtocolHandler(code = HP.code2.TRAVEL_SHOP_FRIENDLY_AWARD_GROUP_SET_REQ_VALUE)
	private boolean onFriendlyPrivilegeAwardGroupSetReq(HawkProtocol protocol) {
		TravelShopFriendlyAwardGroupSetReq req = protocol.parseProtocol(TravelShopFriendlyAwardGroupSetReq.getDefaultInstance());
		int group = req.getGroupId();
		List<TravelShopFriendlyAwardCfg> randAwardListCfg = new ArrayList<>();
    	ConfigIterator<TravelShopFriendlyAwardCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(TravelShopFriendlyAwardCfg.class);
    	while (cfgIter.hasNext()) {
    		TravelShopFriendlyAwardCfg cfg = cfgIter.next();
    		// 特权卡未开启，不随机特殊奖励
    		if (cfg.getType() == ConstProperty.getInstance().getTravelShopFriendlyAwardPrivilegeType() 
    				&& cfg.getGroup() == group) {
    			randAwardListCfg.add(cfg);
    		}
    	}
    	if(randAwardListCfg.size() <= 0){
    		return true;
    	}
    	
    	TravelShopFriendly friendlyInfo = RedisProxy.getInstance().getTravelShopFriendlyInfo(player.getId());
    	friendlyInfo.setPrivilegeAwardGroup(group);
    	RedisProxy.getInstance().updateTravelShopFriendlyInfo(player.getId(), friendlyInfo);
    	TravelShopFriendlyAwardGroupSetResp.Builder builder = TravelShopFriendlyAwardGroupSetResp.newBuilder();
    	builder.setGroupId(group);
    	sendProtocol(HawkProtocol.valueOf(HP.code2.TRAVEL_SHOP_FRIENDLY_AWARD_GROUP_SET_RESP, builder));
    	return true;
	}
	
    /**
     * 领取友好度奖励
     * 
     * @param protocol
     */
    @ProtocolHandler(code = HP.code2.TRAVEL_SHOP_FRIENDLY_AWARD_REQ_VALUE)
	private boolean onFriendlyAwardReq(HawkProtocol protocol) {
    	TravelShopFriendlyAwardAchieveReq req = protocol.parseProtocol(TravelShopFriendlyAwardAchieveReq.getDefaultInstance());
    	int type = req.getAwardType();
    	//领取普通奖励
    	if(type == 1){
    		// 购买前友好度
        	TravelShopFriendly friendlyInfo = RedisProxy.getInstance().getTravelShopFriendlyInfo(player.getId());
        	if (friendlyInfo.getFriendlyCommAwardCount() <= 0 ) {
        		return false;
        	}
        	friendlyInfo.costFriendlyAwardCount(1, false);
        	RedisProxy.getInstance().updateTravelShopFriendlyInfo(player.getId(), friendlyInfo);
        	List<ItemInfo> itemList = new ArrayList<>();
        	List<ItemInfo> awardItems = ConstProperty.getInstance().getTravelShoFriendlyCommonAward();
        	List<ItemInfo> randomAwards = this.getFriendlyRandomAward(
        			ConstProperty.getInstance().getTravelShopFriendlyAwardCommType(),1);
        	if(awardItems != null){
        		itemList.addAll(awardItems);
        	}
        	if(randomAwards != null){
        		itemList.addAll(randomAwards);
        	}
        	AwardItems awardItem = AwardItems.valueOf();
    		awardItem.addItemInfos(itemList);
    		awardItem.rewardTakeAffectAndPush(player, Action.TRAVEL_SHOP_FRIENDLY_AWARD, true, null);
    		
    		TravelShopInfoSync.Builder travelShopInfo = RedisProxy.getInstance().getTravelShopInfo(player.getId());
        	synGoodsInfo(travelShopInfo);
        	return true;
    	}
    	if(type == 2){
    		// 购买前友好度
        	TravelShopFriendly friendlyInfo = RedisProxy.getInstance().getTravelShopFriendlyInfo(player.getId());
        	if(!friendlyInfo.privilegeEffect()){
        		return true;
        	}
        	// 友好度不够
        	if (friendlyInfo.getFriendlyPrivilegeAwardCount() <= 0) {
        		return false;
        	}
        	friendlyInfo.costFriendlyAwardCount(1, true);
        	RedisProxy.getInstance().updateTravelShopFriendlyInfo(player.getId(), friendlyInfo);
        	List<ItemInfo> itemList = new ArrayList<>();
        	List<ItemInfo> awardItems = ConstProperty.getInstance().getTravelShoFriendlyCommonAward();
        	List<ItemInfo> randomAwards = this.getFriendlyRandomAward(
        			ConstProperty.getInstance().getTravelShopFriendlyAwardPrivilegeType(),
        			friendlyInfo.getPrivilegeAwardPoolChoose());
        	if(awardItems != null){
        		itemList.addAll(awardItems);
        	}
        	if(randomAwards != null){
        		itemList.addAll(randomAwards);
        	}
        	AwardItems awardItem = AwardItems.valueOf();
    		awardItem.addItemInfos(itemList);
    		awardItem.rewardTakeAffectAndPush(player, Action.TRAVEL_SHOP_FRIENDLY_AWARD, true, null);
    		
    		TravelShopInfoSync.Builder travelShopInfo = RedisProxy.getInstance().getTravelShopInfo(player.getId());
        	synGoodsInfo(travelShopInfo);
        	
    		return true;
    	}
    	return true;
    }
    
    @SuppressWarnings("deprecation")
    private void travelShopFriendlyUpdate(){
    	TravelShopFriendly friendlyInfo = RedisProxy.getInstance().getTravelShopFriendlyInfo(player.getId());
    	if(friendlyInfo.getUpdateFlag() > 0){
    		return;
    	}
    	//新得上线把原来的友好度都给特权加上
		long travelShopCardTime = RedisProxy.getInstance().getTravelShopCardTime(player.getId());
    	long frindly = RedisProxy.getInstance().getTravelShopFriendly(player.getId());
    	
    	if(friendlyInfo.getPrivilegeStartTime() < travelShopCardTime){
    		friendlyInfo.setPrivilegeStartTime(travelShopCardTime);
    	}
    	if(frindly > 0){
    		friendlyInfo.addFriendlyScore(frindly, true);
    	}
    	friendlyInfo.setUpdateFlag(HawkTime.getMillisecond());
    	RedisProxy.getInstance().updateTravelShopFriendlyInfo(player.getId(),friendlyInfo);
    }
    
    private List<ItemInfo> getFriendlyRandomAward(int type,int group){
    	List<TravelShopFriendlyAwardCfg> randAwardListCfg = new ArrayList<>();
    	ConfigIterator<TravelShopFriendlyAwardCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(TravelShopFriendlyAwardCfg.class);
    	while (cfgIter.hasNext()) {
    		TravelShopFriendlyAwardCfg cfg = cfgIter.next();
    		// 特权卡未开启，不随机特殊奖励
    		if (cfg.getType() == type && cfg.getGroup() == group) {
    			randAwardListCfg.add(cfg);
    		}
    	}
    	TravelShopFriendlyAwardCfg cfg = RandomUtil.random(randAwardListCfg);
    	if(cfg != null){
    		return cfg.getAward();
    	}
    	return null;
    }
    
}
