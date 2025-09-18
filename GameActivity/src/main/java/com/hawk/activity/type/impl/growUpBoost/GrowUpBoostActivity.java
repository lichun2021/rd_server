package com.hawk.activity.type.impl.growUpBoost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.GrowUpBoostAddScoreEvent;
import com.hawk.activity.event.impl.GrowUpBoostItemConsumeEvent;
import com.hawk.activity.event.impl.LoginDaysActivityEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.parser.GrowUoBoostScoreParser;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.growUpBoost.cfg.GrowUpBoostAchieveCfg;
import com.hawk.activity.type.impl.growUpBoost.cfg.GrowUpBoostExchangeCfg;
import com.hawk.activity.type.impl.growUpBoost.cfg.GrowUpBoostGoodsCfg;
import com.hawk.activity.type.impl.growUpBoost.cfg.GrowUpBoostKVCfg;
import com.hawk.activity.type.impl.growUpBoost.cfg.GrowUpBoostScoreCfg;
import com.hawk.activity.type.impl.growUpBoost.cfg.GrowUpBoostTimeCfg;
import com.hawk.activity.type.impl.growUpBoost.entity.GrowUpBoostEntity;
import com.hawk.activity.type.impl.growUpBoost.entity.GrowUpBoostScore;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.GrowUpBoostDailyScoreRecord;
import com.hawk.game.protocol.Activity.GrowUpBoostDailyScoreRecordResp;
import com.hawk.game.protocol.Activity.GrowUpBoostItemRecord;
import com.hawk.game.protocol.Activity.GrowUpBoostShopItem;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class GrowUpBoostActivity extends ActivityBase implements AchieveProvider ,IExchangeTip<GrowUpBoostExchangeCfg>{
	
	public static final String CUSTOM_KEY = "GrowUpBoostActivityItemRecord";
	
    public GrowUpBoostActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.GROW_UP_BOOST;
    }
    
    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        GrowUpBoostActivity activity = new GrowUpBoostActivity(config.getActivityId(), activityEntity);
        AchieveContext.registeProvider(activity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<GrowUpBoostEntity> queryList = HawkDBManager.getInstance()
                .query("from GrowUpBoostEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            GrowUpBoostEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        GrowUpBoostEntity entity = new GrowUpBoostEntity(playerId, termId);
        return entity;
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
    	Optional<GrowUpBoostEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		GrowUpBoostEntity entity = opEntity.get();
		if(entity.getAchieveList().isEmpty()){
			initActivityInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getAchieveList(), entity);
		return Optional.of(achieveItems);
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
    	AchieveConfig config = HawkConfigManager.getInstance()
    			.getConfigByKey(GrowUpBoostAchieveCfg.class, achieveId);
		return config;
    }
    
    @Override
    public Result<?> onTakeReward(String playerId, int achieveId) {
    	GrowUpBoostAchieveCfg config = HawkConfigManager.getInstance()
    			.getConfigByKey(GrowUpBoostAchieveCfg.class, achieveId);
		if(Objects.isNull(config)){
			return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
		}
		//每日任务不判定
		if(config.getType() == 1){
			return Result.success();
		}
		Optional<GrowUpBoostEntity> opEntity = getPlayerDataEntity(playerId);
		GrowUpBoostEntity entity = opEntity.get();
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		HawkTuple2<Integer, Integer> tuple = this.calFinishCountAndPage(entity);
		if(tuple.second != config.getPage()){
			return Result.fail(Status.GrowUpBoostErr.GROW_UP_BOOST_ACHIEVE_PAGE_LIMITE_VALUE);
		}
		return Result.success();
    }

    @Override
    public Action takeRewardAction() {
        return Action.GROW_UP_BOOST_ACHIEVE_REWARD;
    }
    
    @Override
    public void onTakeRewardSuccessAfter(String playerId, List<Builder> reweardList, int achieveId) {
    	GrowUpBoostAchieveCfg config = HawkConfigManager.getInstance()
    			.getConfigByKey(GrowUpBoostAchieveCfg.class, achieveId);
		if(Objects.isNull(config)){
			return;
		}
		GrowUpBoostKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(GrowUpBoostKVCfg.class);
		if (config == null) {
			return;
		}
		Optional<GrowUpBoostEntity> opEntity = getPlayerDataEntity(playerId);
		GrowUpBoostEntity entity = opEntity.get();
		if (!opEntity.isPresent()) {
			return;
		}
		int termId = this.getActivityTermId();
		//每日任务积分获取打点
		if(config.getType() == 1){
			int addScore = config.getScore();
			//是否达到每日最大积分限制
			int scoreTotalTody = entity.getScoreAchieveToday();
			int scoreTotalTodyAft = scoreTotalTody + addScore;
			if(scoreTotalTodyAft > kvConfig.getMaxDailyScoreLimit()){
				addScore = kvConfig.getMaxDailyScoreLimit() - scoreTotalTody;
			}
			//是否达到最大积分限制
			int befTotal = entity.getScoreTotal();
			int totalAftAdd = befTotal + addScore;
			if(totalAftAdd > kvConfig.getMaxScoreLimit()){
	        	addScore = kvConfig.getMaxScoreLimit() - befTotal;
	        }
			if(addScore > 0 && isOpening(playerId)) {
				entity.addAchieveScoreRecord(addScore);
				this.syncActivityDataInfo(playerId);
				//抛出积分变化事件
		        ActivityManager.getInstance().postEvent(new GrowUpBoostAddScoreEvent(entity.getPlayerId(), entity.getScoreTotal()), true);
			}
			//日志
			int aftTotal = entity.getScoreTotal();
	        this.getDataGeter().logGrowUpBoostAchieveScore(playerId, termId, achieveId, addScore, befTotal, aftTotal);
	        HawkLog.logPrintln("GrowUpBoostActivity,addAchieveScore,playerId:{},taskId:{},scoreToday:{},scoreAddBef:{},scoreAddAft:{},scoreAdd:{}",
	        		playerId,achieveId,scoreTotalTody,befTotal,addScore,aftTotal);
		}
		//积分任务领取
		if(config.getType() == 2){
			int aftTotal = entity.getScoreTotal();
			this.getDataGeter().logGrowUpBoostScoreAchieveRewardTake(playerId, termId, achieveId, aftTotal);
			//积分任务翻页
			HawkTuple2<Integer, Integer> tuple = this.calFinishCountAndPage(entity);
			if(tuple.second != config.getPage() || tuple.first == tuple.second){
				this.syncActivityDataInfo(playerId);
				this.getDataGeter().logGrowUpBoostScoreAchievePageChange(playerId, termId, tuple.first, tuple.second);
			}
		}
    }

    
    
    
    @Override
    public void syncActivityDataInfo(String playerId) {
    	GrowUpBoostKVCfg config = HawkConfigManager.getInstance().getKVInstance(GrowUpBoostKVCfg.class);
		if (config == null) {
			return;
		}
    	Optional<GrowUpBoostEntity> opEntity = getPlayerDataEntity(playerId);
		GrowUpBoostEntity entity = opEntity.get();
		if (!opEntity.isPresent()) {
			return;
		}
		GrowUpBoostTimeCfg timeCfg = this.getUseConfig();
		HawkTuple2<Integer, Integer> finishTuple = this.calFinishCountAndPage(entity);
        Activity.GrowUpBoostSync.Builder builder = Activity.GrowUpBoostSync.newBuilder();
        builder.setConfigUse(timeCfg.getConfigId());
        builder.setItemScore(entity.getScoreItem());
        builder.setTaskScore(entity.getScoreAchieveToday());
        builder.setTaskTotalScore(entity.getScoreAchieveTotal());
        builder.setTotalScore(entity.getScoreTotal());
        builder.setScoreAchievePage(finishTuple.second);
        builder.setScoreAchieveFinishTerm(finishTuple.first);
        for(Map.Entry<Integer, Integer> exchange : entity.getExchangeNumMap().entrySet()){
        	GrowUpBoostShopItem.Builder ebuilder = GrowUpBoostShopItem.newBuilder();
        	ebuilder.setGoodsId(exchange.getKey());
        	ebuilder.setExhangeTimes(exchange.getValue());
        	builder.addExchangeItems(ebuilder);
        }
        
        for(Map.Entry<Integer, Integer> buy : entity.getBuyNumMap().entrySet()){
        	GrowUpBoostShopItem.Builder bbuilder = GrowUpBoostShopItem.newBuilder();
        	bbuilder.setGoodsId(buy.getKey());
        	bbuilder.setExhangeTimes(buy.getValue());
        	builder.addGoods(bbuilder);
        }
        builder.addAllTips(getTips(GrowUpBoostExchangeCfg.class, entity.getTipSet()));
        
        Map<Integer,Long> itemRecordMap =this.getDataGeter().getGrowUpBoostItemRecord(playerId);
        for(Map.Entry<Integer,Long> record :itemRecordMap.entrySet()){
			int itemId = record.getKey();
			int count = record.getValue().intValue();
			GrowUpBoostItemRecord.Builder rbuilder = GrowUpBoostItemRecord.newBuilder();
			rbuilder.setItemId(itemId);
			rbuilder.setItemNum(count);
			builder.addRecords(rbuilder);
		}
        pushToPlayer(playerId, HP.code2.GROW_UP_BOOST_SYNC_VALUE, builder);
    }
    
    @Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.GROW_UP_BOOST_INIT, ()-> {
				initActivityInfo(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}
    
    
    @Override
	public void onPlayerLogin(String playerId) {
    	GrowUpBoostKVCfg config = HawkConfigManager.getInstance().getKVInstance(GrowUpBoostKVCfg.class);
		if (config == null) {
			return;
		}
		//取当前活动期数
		int termId = this.getActivityTermId();
		if(termId <= 0){
			//回收道具
	    	this.recoverItem(playerId,config.getExchangeItemId());
	    	this.recoverItem(playerId,config.getScoreItemId());
		}
    	//初始化数据
		initActivityInfo(playerId);
		//热更修复数据
		if(termId > 0){
			GrowUpBoostTimeCfg timeCfg = this.getUseConfig();
			if(Objects.isNull(timeCfg)){
				return;
			}
	    	Optional<GrowUpBoostEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			GrowUpBoostEntity entity = opEntity.get();
			// 成就已初始化
			if (!entity.getItemListScore().isEmpty()) {
				return;
			}
			List<AchieveItem> itemListScore = new CopyOnWriteArrayList<AchieveItem>();
			ConfigIterator<GrowUpBoostAchieveCfg> configIterator = HawkConfigManager.getInstance()
					.getConfigIterator(GrowUpBoostAchieveCfg.class);
			while (configIterator.hasNext()) {
				GrowUpBoostAchieveCfg next = configIterator.next();
				if(next.getConfigId() != timeCfg.getConfigId()){
					continue;
				}
				//积分任务
				if(next.getType() == 2){
					AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
					itemListScore.add(item);
				}
				
			}
			entity.resetItemListScore(itemListScore);
			// 初始化成就数据
			this.syncActivityDataInfo(playerId);
			ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemListScore), true);
			ActivityManager.getInstance().postEvent(new GrowUpBoostAddScoreEvent(entity.getPlayerId(), entity.getScoreTotal()), true);
		}
		
	}
    
	
    
    @Subscribe
    public void onContinueLogin(ContinueLoginEvent event) {
        String playerId = event.getPlayerId();
        //活动是否开启，没开不继续处理
        if (!isOpening(playerId)) {
            return;
        }
        //去当前期数
        int termId = getActivityTermId(playerId);
        //取当前期数结束时间
        long endTime = getTimeControl().getEndTimeByTermId(termId, playerId);
        //取当前时间
        long now = HawkTime.getMillisecond();
        //如果当前时间大于当前期数结束时间，不继续处理
        if (now >= endTime) {
            return;
        }
        Optional<GrowUpBoostEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }

        GrowUpBoostEntity entity = opEntity.get();
        //玩家数据里记录当前天的的日期，说明已经处理过每日登陆成就，不继续处理
        if (entity.getLoginDaysList().contains(HawkTime.getYyyyMMddIntVal())) {
            return;
        }
        //记录当天日期
        entity.recordLoginDay();
        this.updateAchieveItemsDay(entity);
        //给客户端同步数据
        this.syncActivityDataInfo(playerId);
        //每天登录事件
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, entity.getLoginDaysCount(), this.providerActivityId()), true);
        HawkLog.logPrintln("GrowUpBoostActivity,onContinueLogin,playerId:{},size1:{},size2:{}",playerId,
        		entity.getItemListDay().size(),entity.getItemListScore().size());
    }

    
    public void onActivityItemConsume(GrowUpBoostItemConsumeEvent event) {
    	//活动是否开启，没开不继续处理
    	GrowUpBoostKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(GrowUpBoostKVCfg.class);
        if (!isOpening(event.getPlayerId())) {
        	//活动没开始期间也要抵消配置物品数量
        	if(kvConfig.getDecomposeItemMap().containsKey(event.getItemId())){
        		Map<Integer,Long> recordMap = this.getDataGeter().getGrowUpBoostItemRecord(event.getPlayerId());
        		long recorNum = recordMap.getOrDefault(event.getItemId(), 0l);
        		if(recorNum > 0){
        			recorNum -= event.getNumber();
        			if(recorNum > 0){
        				recordMap.put(event.getItemId(), recorNum);
        			}else{
        				recordMap.remove(event.getItemId());
        			}
        			this.getDataGeter().updateGrowUpBoostItemRecord(event.getPlayerId(), recordMap);
        		}
        	}
            return;
        }
    	GrowUpBoostTimeCfg timeCfg = this.getUseConfig();
		if(Objects.isNull(timeCfg)){
			return;
		}
		//赋值使用的配置ID
		int useConfigId = timeCfg.getConfigId();
		GrowUpBoostScoreCfg scoreCfg = HawkConfigManager.getInstance()
				.getConfigByKey(GrowUpBoostScoreCfg.class, useConfigId);
		if(Objects.isNull(scoreCfg)){
			return;
		}
		
		int itemId = event.getItemId();
		long itemNum = event.getNumber();
		if(itemNum <= 0){
			return;
		}
		Map<Integer,Long> recordMap = this.getDataGeter().getGrowUpBoostItemRecord(event.getPlayerId());
		long recorNum = recordMap.getOrDefault(itemId, 0l);
		if(kvConfig.getDecomposeItemMap().containsKey(itemId) && recorNum > 0){
			if(recorNum >= itemNum){
				recorNum -= itemNum;
				recordMap.put(itemId, recorNum);
				itemNum = 0;
			}else{
				itemNum -= recorNum;
				recordMap.remove(itemId);
			}
			this.getDataGeter().updateGrowUpBoostItemRecord(event.getPlayerId(), recordMap);
		}
		//数量被抵消掉了，更新活动数据
		if(itemNum <= 0){
			this.syncActivityDataInfo(event.getPlayerId());
			return;
		}
		int itemScore = scoreCfg.getItemAddScore(itemId) ;
		int addScore = (int) (itemScore * itemNum);
		if(addScore <= 0){
			return;
		}
		Optional<GrowUpBoostEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
        if (!opEntity.isPresent()) {
            return;
        }
        GrowUpBoostEntity entity = opEntity.get();
        int scoreBef = entity.getScoreTotal();
        int total = scoreBef + addScore;
        if(total > kvConfig.getMaxScoreLimit()){
        	addScore = kvConfig.getMaxScoreLimit() - scoreBef;
        }
        if(addScore > 0){
        	entity.addItemConsumeScoreDetail(event.getItemId(), event.getNumber(), addScore);
            entity.addItemScoreRecord(addScore);
		}
        int scoreAft =  entity.getScoreTotal();
    	//抛出积分变化事件
        this.scoreAchieveProgress(event.getPlayerId());
        this.syncActivityDataInfo(event.getPlayerId());
        HawkLog.logPrintln("GrowUpBoostActivity,addItemConsumeScore,playerId:{},itemId:{},itemNuym:{},scoreAddBef:{},scoreAdd:{},scoreAft:{},action:{}",
        		event.getPlayerId(),event.getItemId(),event.getNumber(),scoreBef,addScore,scoreAft,event.getAction());
        //日志打点
        int termId = this.getActivityTermId();
        this.getDataGeter().logGrowUpBoostItemScore(entity.getPlayerId(), termId, event.getItemId(), event.getNumber(), addScore, scoreBef, scoreAft);
    }
    
    /**
     * 物品兑换
     *
     * @param playerId
     * @param exchangeId
     * @param exchangeCount
     */
    public void itemExchange(String playerId, int exchangeId, int exchangeCount) {
    	GrowUpBoostExchangeCfg exchangeConfig = HawkConfigManager.getInstance().
                getConfigByKey(GrowUpBoostExchangeCfg.class, exchangeId);

        if (Objects.isNull(exchangeConfig)) {
            return;
        }
        
        GrowUpBoostTimeCfg timeCfg = this.getUseConfig();
        if (Objects.isNull(timeCfg)) {
            return;
        }
        if(timeCfg.getConfigId() != exchangeConfig.getConfigId()){
        	return;
        }
        Optional<GrowUpBoostEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }     
        GrowUpBoostEntity entity = opDataEntity.get();
        if(this.exchangeInLock(exchangeConfig, entity)){
        	HawkLog.logPrintln("GrowUpBoostActivity,itemExchange,fail,exchangeInLock,playerId: " + "{},exchangeType:{},ecount:{}",
                    playerId, exchangeId, exchangeCount);
        	return;
        }
        int eCount = entity.getExchangeCount(exchangeId);
        if (eCount + exchangeCount > exchangeConfig.getTimes()) {
            //错误码
            HawkLog.logPrintln("GrowUpBoostActivity,itemExchange,fail,countless,playerId: " + "{},exchangeType:{},ecount:{}",
                    playerId, exchangeId, eCount);
            return;
        }

        List<RewardItem.Builder> makeCost = this.arrangeItemList(exchangeConfig.getNeedItemList());
        if(!makeCost.isEmpty()){
		    boolean cost = this.getDataGeter().cost(playerId, makeCost, exchangeCount,
		            Action.GROW_UP_BOOST_EXCHANGE_COST, true);
		    if (!cost) {
		        PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.HONOUR_HERO_RETURN_EXCHANGE_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
		        return;
		    }
        }
        int unlockMaxBef = this.getUnLockGroupMax(entity); 
        //增加兑换次数
        entity.addExchangeCount(exchangeId, exchangeCount);
        //奖励列表
        List<RewardItem.Builder> gianList = new ArrayList<>();
        gianList.addAll(exchangeConfig.getGainItemList());
        GrowUpBoostScoreCfg scoreCfg = HawkConfigManager.getInstance().
                getConfigByKey(GrowUpBoostScoreCfg.class, timeCfg.getConfigId());
        if(Objects.nonNull(scoreCfg)){
        	gianList.addAll(scoreCfg.getExchangeGainItem(exchangeConfig.getGroup()));
        }
        //发奖励
        this.getDataGeter().takeReward(playerId, gianList, exchangeCount, Action.GROW_UP_BOOST_EXCHANGE_GET, true);
        //同步
        this.syncActivityDataInfo(playerId);
        HawkLog.logPrintln("GrowUpBoostActivity,itemExchange,sucess,playerId: " + "{},exchangeType:{},ecount:{}", playerId, exchangeId, eCount);
        //兑换打点
        int termId = this.getActivityTermId();
        int unlockMaxAft = this.getUnLockGroupMax(entity); 
        this.getDataGeter().logGrowUpBoostExchangeGroup(playerId, termId, exchangeId, exchangeConfig.getGroup(), unlockMaxBef, unlockMaxAft);
    }
    
    
    
    /**
     * 物品兑换
     *
     * @param playerId
     * @param exchangeId
     * @param exchangeCount
     */
    public void itemBuy(String playerId, int buyId, int buyCount) {
    	GrowUpBoostGoodsCfg buyConfig = HawkConfigManager.getInstance().
                getConfigByKey(GrowUpBoostGoodsCfg.class, buyId);

        if (Objects.isNull(buyConfig)) {
            return;
        }
        GrowUpBoostTimeCfg timeCfg = this.getUseConfig();
        if (Objects.isNull(timeCfg)) {
            return;
        }
        if(timeCfg.getConfigId() != buyConfig.getConfigId()){
        	return;
        }
        Optional<GrowUpBoostEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }     
        GrowUpBoostEntity entity = opDataEntity.get();
        int bCount = entity.getBuyCount(buyId);
        if (bCount + buyCount > buyConfig.getBuyLimit()) {
            //错误码
            HawkLog.logPrintln("GrowUpBoostActivity,itemBuy,fail,countless,playerId: " + "{},exchangeType:{},ecount:{}",
                    playerId, buyId, buyCount);
            return;
        }
        List<RewardItem.Builder> makeCost = this.arrangeItemList(buyConfig.getNeedItemList());
        if(!makeCost.isEmpty()){
        	boolean cost = this.getDataGeter().cost(playerId, makeCost, buyCount,
                    Action.GROW_UP_BOOST_BUY_COST, true);
            if (!cost) {
                PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.GROW_UP_BOOST_EXCHANGE_REQ_VALUE,
                		Status.Error.ITEM_NOT_ENOUGH_VALUE);
                return;
            }
        }
        //增加兑换次数
        entity.addBuyCount(buyId, buyCount);
        //发奖励
        List<RewardItem.Builder> rewardItemList = new ArrayList<>();
        List<String> rewardList = this.getDataGeter().getAwardFromAwardCfg(buyConfig.getAwardId());
        for (String rewardStr : rewardList) {
            List<RewardItem.Builder> rewardBuilders = RewardHelper.toRewardItemImmutableList(rewardStr);
            rewardItemList.addAll(rewardBuilders);
        }
        this.getDataGeter().takeReward(playerId, rewardItemList, buyCount, Action.GROW_UP_BOOST_BUY_GET, true);
        //同步
        this.syncActivityDataInfo(playerId);
        HawkLog.logPrintln("GrowUpBoostActivity,itemBuy,sucess,playerId: " + "{},exchangeType:{},ecount:{}", playerId, buyId, buyCount);
        int termId = this.getActivityTermId();
        this.getDataGeter().logGrowUpBoostBuyGift(playerId, termId, buyId);
    }
    
    
    
    
    /**
     * 是否解锁兑换
     * @param exchangeConfig
     * @param entity
     * @return
     */
    private boolean exchangeInLock(GrowUpBoostExchangeCfg exchangeConfig,GrowUpBoostEntity entity){
    	int needGroup = exchangeConfig.getGroup() - 1;
    	if(needGroup <= 0){
    		return false;
    	}
    	Map<Integer, Integer> exchangeMap = entity.getExchangeNumMap();
    	for(Map.Entry<Integer, Integer> entry :exchangeMap.entrySet()){
    		int eId = entry.getKey();
    		GrowUpBoostExchangeCfg eConfig = HawkConfigManager.getInstance().
                    getConfigByKey(GrowUpBoostExchangeCfg.class, eId);
    		if(Objects.isNull(eConfig)){
    			continue;
    		}
    		if(eConfig.getGroup() >= needGroup){
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * 获取开放的最大层数
     * @param entity
     * @return
     */
    private int getUnLockGroupMax(GrowUpBoostEntity entity){
    	int max = 0;
    	Map<Integer, Integer> exchangeMap = entity.getExchangeNumMap();
    	for(Map.Entry<Integer, Integer> entry :exchangeMap.entrySet()){
    		int eId = entry.getKey();
    		GrowUpBoostExchangeCfg eConfig = HawkConfigManager.getInstance().
                    getConfigByKey(GrowUpBoostExchangeCfg.class, eId);
    		if(Objects.isNull(eConfig)){
    			continue;
    		}
    		if(eConfig.getGroup() > max){
    			max = eConfig.getGroup();
    		}
    	}
    	return max + 1;
    }
    
    
    
    
    /**
     * 每日获取积分记录
     * @param playerId
     */
    public void syncDailyScoreRecord(String playerId){
    	Optional<GrowUpBoostEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return;
		}
		GrowUpBoostEntity entity = opt.get();
		Map<Long, GrowUpBoostScore> scoreMap = entity.getScores();
		GrowUpBoostDailyScoreRecordResp.Builder respBuild = GrowUpBoostDailyScoreRecordResp.newBuilder();
		for(Map.Entry<Long, GrowUpBoostScore> entry : scoreMap.entrySet()){
			GrowUpBoostScore detail = entry.getValue();
			GrowUpBoostDailyScoreRecord.Builder srBuilder = GrowUpBoostDailyScoreRecord.newBuilder();
			srBuilder.setRecordTime(detail.getDayZero());
			srBuilder.setItemScore(detail.getItemScore());
			srBuilder.setTaskScore(detail.getAchieveScore());
			respBuild.addRecord(srBuilder);
		}
		
		pushToPlayer(playerId, HP.code2.GROW_UP_BOOST_DAILY_SCORE_RECORD_RESP_VALUE, respBuild);
		
    }
    
    
  
    /**
     *  初始化玩家数据
     * @param playerId
     */
    private void initActivityInfo(String playerId){
    	HawkLog.logPrintln("GrowUpBoostActivity,initData,fail4,playerId:{}",playerId);
    	GrowUpBoostTimeCfg timeCfg = this.getUseConfig();
		if(Objects.isNull(timeCfg)){
			HawkLog.logPrintln("GrowUpBoostActivity,initData,fail1,playerId:{}",playerId);
			return;
		}
    	Optional<GrowUpBoostEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.logPrintln("GrowUpBoostActivity,initData,fail2,playerId:{}",playerId);
			return;
		}
		GrowUpBoostEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemListScore().isEmpty()) {
			HawkLog.logPrintln("GrowUpBoostActivity,initData,fail3,playerId:{}",playerId);
			return;
		}
		//赋值使用的配置ID
		int useConfigId = timeCfg.getConfigId();
		entity.setUseConfig(useConfigId);
		//任务
		List<AchieveItem> updateList = new ArrayList<>();
		List<AchieveItem> itemListDay = new CopyOnWriteArrayList<AchieveItem>();
		List<AchieveItem> itemListScore = new CopyOnWriteArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<GrowUpBoostAchieveCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(GrowUpBoostAchieveCfg.class);
		while (configIterator.hasNext()) {
			GrowUpBoostAchieveCfg next = configIterator.next();
			if(next.getConfigId() != useConfigId){
				continue;
			}
			//每日任务
			if(next.getType() == 1){
				AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
				itemListDay.add(item);
				updateList.add(item);
			}
			//积分任务
			if(next.getType() == 2){
				AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
				itemListScore.add(item);
				updateList.add(item);
			}
			
		}
		entity.resetItemListDay(itemListDay);
		entity.resetItemListScore(itemListScore);
		entity.recordLoginDay();
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, updateList), true);
		//抛每天登录事件
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, entity.getLoginDaysCount(), this.providerActivityId()), true);
		//回收现有的道具
		GrowUpBoostKVCfg config = HawkConfigManager.getInstance().getKVInstance(GrowUpBoostKVCfg.class);
    	this.recoverItem(playerId,config.getExchangeItemId());
    	this.recoverItem(playerId,config.getScoreItemId());
    	HawkLog.logPrintln("GrowUpBoostActivity,initData,playerId:{}",playerId);
    	 HawkLog.logPrintln("GrowUpBoostActivity,initData,playerId:{},configId:{},size1:{},size2:{}",playerId,useConfigId,
         		entity.getItemListDay().size(),entity.getItemListScore().size());
    }
    
    /**
     * 刷新每日任务
     * @param entity
     */
    private void updateAchieveItemsDay(GrowUpBoostEntity entity) {
        ConfigIterator<GrowUpBoostAchieveCfg> configIterator =
                HawkConfigManager.getInstance().getConfigIterator(GrowUpBoostAchieveCfg.class);
        GrowUpBoostTimeCfg timeCfg = this.getUseConfig();
		if(Objects.isNull(timeCfg)){
			return;
		}
		//赋值使用的配置ID
		int useConfigId = timeCfg.getConfigId();
        List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
        //遍历成就配置，向数据库对象添加成就项
        while (configIterator.hasNext()) {
        	GrowUpBoostAchieveCfg next = configIterator.next();
        	if(next.getConfigId() != useConfigId){
				continue;
			}
            if(next.getType() == 1){
				AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
				itemList.add(item);
			}
        }
        entity.setItemListDay(itemList);
        AchievePushHelper.pushAchieveUpdate(entity.getPlayerId(), entity.getItemListDay());
       
    } 
    
    /**
     * 获取完成轮次和当前任务页
     * @param entity
     * @return
     */
    private HawkTuple2<Integer, Integer> calFinishCountAndPage(GrowUpBoostEntity entity){
    	int page = 0;
    	int maxPage = 0;
    	List<AchieveItem> list = entity.getItemListScore();
    	for(AchieveItem achive : list){
    		GrowUpBoostAchieveCfg config = HawkConfigManager.getInstance()
        			.getConfigByKey(GrowUpBoostAchieveCfg.class, achive.getAchieveId());
    		if(Objects.isNull(config)){
    			continue;
    		}
    		//找出最大页数
    		if(maxPage < config.getPage()){
    			maxPage = config.getPage();
    		}
    		
    		if(achive.getState() == Activity.AchieveState.TOOK_VALUE){
                continue;
            }
    		//初始化
    		if(page == 0){
    			page = config.getPage();
    			 continue;
    		}
    		//未完成的任务最小一页
    		if(page > config.getPage()){
    			page = config.getPage();
    			 continue;
    		}
    		
    	}
    	//全部完成了
    	if(page == 0){
    		return new HawkTuple2<Integer, Integer>(maxPage, maxPage);
    	}
    	return new HawkTuple2<Integer, Integer>(page-1, page);
    }
    
    /**
     * 获取适应配置
     * @return
     */
    private GrowUpBoostTimeCfg getUseConfig(){
    	int termId = getTimeControl().getActivityTermId(HawkTime.getMillisecond());
    	GrowUpBoostTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(GrowUpBoostTimeCfg.class, termId);
    	return timeCfg;
    }
    
    
    /**
     * 回收道具
     * @param playerId
     */
    public void recoverItem(String playerId,String recoverItem){
		//活动结束时回收道具ID
		RewardItem.Builder itemBuilder = RewardHelper.toRewardItem(recoverItem);
		if(Objects.isNull(itemBuilder)){
			return;
		}
		//取玩家身上此道具的数量
		int count = this.getDataGeter().getItemNum(playerId, itemBuilder.getItemId());
		if(count <= 0){
			return;
		}
		//扣除道具的数据准备
		List<RewardItem.Builder> costList = new ArrayList<>();
		RewardItem.Builder costBuilder = RewardItem.newBuilder();
		//类型为道具
		costBuilder.setItemType(ItemType.TOOL_VALUE);
		//待扣除物品ID
		costBuilder.setItemId(itemBuilder.getItemId());
		//待扣除的物品数量
		costBuilder.setItemCount(count);
		//把待扣除的物品数据加入参数容器
		costList.add(costBuilder);
		//注意这里先扣除源道具，如果失败，不给兑换后的道具
		boolean cost = this.getDataGeter().cost(playerId,costList, 1, Action.GROW_UP_BOOST_RECOVER_COST, true);
		//扣除失败不继续处理
		if (!cost) {
			return;
		}
		HawkLog.logPrintln("GrowUpBoostActivity,recoverItem,playerId{},itemId:{},count:{}", playerId,itemBuilder.getItemId(),count);
		int temId = this.getActivityTermId();
		this.getDataGeter().logGrowUpBoostItemRecover(playerId, temId, itemBuilder.getItemId(),count);
	}
    
    
    public List<RewardItem.Builder> arrangeItemList(List<RewardItem.Builder> list){
    	List<RewardItem.Builder> itemList = new ArrayList<>();
    	for(RewardItem.Builder item : list){
    		if(item.getItemCount() <= 0){
    			continue;
    		}
    		itemList.add(item);
    	}
    	return itemList;
    }

	

    
    
    
	public void scoreAchieveProgress(String playerId){
		Optional<AchieveItems> achieveItemsOpt = this.getAchieveItems(playerId);
		if(!achieveItemsOpt.isPresent()){
			return;
		}
		AchieveItems achieveItems = achieveItemsOpt.get();
		GrowUpBoostEntity entity = (GrowUpBoostEntity) achieveItems.getEntity();
		GrowUoBoostScoreParser parse = (GrowUoBoostScoreParser) AchieveContext.getParser(AchieveType.GROW_UP_BOOST_SCORE);
		GrowUpBoostAddScoreEvent event = new GrowUpBoostAddScoreEvent(playerId,entity.getScoreTotal());
		List<AchieveItem> update = new ArrayList<>();
		for(AchieveItem item : achieveItems.getItems()){
			GrowUpBoostAchieveCfg acfg = HawkConfigManager.getInstance()
					.getConfigByKey(GrowUpBoostAchieveCfg.class, item.getAchieveId());
			if(acfg.getAchieveType() != AchieveType.GROW_UP_BOOST_SCORE){
				continue;
			}
	  		parse.updateAchieveData(item, acfg, event, update);
		}
		entity.notifyUpdate();
		if(update.size() > 0){
			AchievePushHelper.pushAchieveUpdate(playerId, update);
		}
	}
	
	public static void onActivityItemConsumeEvnet(GrowUpBoostItemConsumeEvent event){
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.GROW_UP_BOOST_VALUE);
		if(!opActivity.isPresent()){
			return;
		}
		try {
			GrowUpBoostActivity activity = (GrowUpBoostActivity) opActivity.get();
			activity.onActivityItemConsume(event);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

}
