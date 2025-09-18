package com.hawk.activity.type.impl.shootingPractice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDaysActivityEvent;
import com.hawk.activity.event.impl.ShootingPracticeBuyTimesEvent;
import com.hawk.activity.event.impl.ShootingPracticeScoreEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.shootingPractice.cfg.ShootingPracticeAchieveCfg;
import com.hawk.activity.type.impl.shootingPractice.cfg.ShootingPracticeEnemyCfg;
import com.hawk.activity.type.impl.shootingPractice.cfg.ShootingPracticeExchangeCfg;
import com.hawk.activity.type.impl.shootingPractice.cfg.ShootingPracticeKVCfg;
import com.hawk.activity.type.impl.shootingPractice.cfg.ShootingPracticeRankCfg;
import com.hawk.activity.type.impl.shootingPractice.entity.ShootingPracticeEntity;
import com.hawk.activity.type.impl.shootingPractice.rank.ShootingPracticeRank;
import com.hawk.game.protocol.Activity.PBShootingPracticeAction;
import com.hawk.game.protocol.Activity.PBShootingPracticeBarrageResp;
import com.hawk.game.protocol.Activity.PBShootingPracticeItem;
import com.hawk.game.protocol.Activity.PBShootingPracticeOverReq;
import com.hawk.game.protocol.Activity.PBShootingPracticeOverResp;
import com.hawk.game.protocol.Activity.PBShootingPracticePageInfoResp;
import com.hawk.game.protocol.Activity.PBShootingPracticeRankResp;
import com.hawk.game.protocol.Activity.PBShootingPracticeScoreRank;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.serialize.string.SerializeHelper;

public class ShootingPracticeActivity extends ActivityBase implements AchieveProvider ,IExchangeTip<ShootingPracticeExchangeCfg>{
	

	private ShootingPracticeRank scoreRank = new ShootingPracticeRank();
	private long rankRefreshTime;
	
	
    public ShootingPracticeActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.SHOOTING_PRACTICE;
    }
    
    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
    	ShootingPracticeActivity activity = new ShootingPracticeActivity(config.getActivityId(), activityEntity);
        AchieveContext.registeProvider(activity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<ShootingPracticeEntity> queryList = HawkDBManager.getInstance()
                .query("from ShootingPracticeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
        	ShootingPracticeEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
    	ShootingPracticeEntity entity = new ShootingPracticeEntity(playerId, termId);
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
    	Optional<ShootingPracticeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		ShootingPracticeEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initActivityInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getAllAchieveList(), entity);
		return Optional.of(achieveItems);
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
    	AchieveConfig config = HawkConfigManager.getInstance()
    			.getConfigByKey(ShootingPracticeAchieveCfg.class, achieveId);
		return config;
    }
    

    @Override
    public Action takeRewardAction() {
        return Action.SHOOTING_PRACTICE_ACHIEVE_REWARD;
    }
    
    
    @Override
    public void onTick() {
    	long curTime = HawkTime.getMillisecond();
    	if(curTime - this.rankRefreshTime > 5 * 60 * 1000){
    		//排行榜5分钟 刷新一次
    		this.scoreRank.doRankSort();
    		this.rankRefreshTime = curTime;
    	}
    }

    
    @Override
    public void onShow() {
    	//show 阶段重置排行榜
    	this.rankRest();
    }
    
    
    @Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.SHOOTING_PRACTICE_INIT, ()-> {
				initActivityInfo(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}
    
    @Override
    public void onEnd() {
    	sendRankRewardMail();
    }
    
    @Override
	public void onPlayerLogin(String playerId) {
    	Optional<ShootingPracticeEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        ShootingPracticeEntity entity = opEntity.get();
        if(entity.getScoreMax() > 0){
        	PBShootingPracticeScoreRank rank = this.scoreRank.getPlayerCurRank(playerId);
        	if(Objects.nonNull(rank) && rank.getRank() <= 0){
        		this.scoreRank.addScore(entity.getScoreMax(), playerId);
        	}
        }
    	this.initActivityInfo(playerId);
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
        Optional<ShootingPracticeEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        
        ShootingPracticeEntity entity = opEntity.get();
        //如果没有初始化就先初始化
        if(entity.getInitTime() <= 0){
        	this.initActivityInfo(playerId);
        	this.syncActivityDataInfo(playerId);
        }
        //玩家数据里记录当前天的的日期，说明已经处理过每日登陆成就，不继续处理
        if (entity.getLoginDaysList().contains(HawkTime.getYyyyMMddIntVal())) {
            return;
        }
        //记录当天日期
        entity.recordLoginDay();
        //成就更新
        this.updateAchieveItemsDay(entity);
        //游戏次数更新
        this.updateGameCount(entity);
        //给客户端同步数据
        this.syncActivityDataInfo(playerId);
        //每天登录事件
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, entity.getLoginDaysCount(), this.providerActivityId()), true);
    }

    
    
    @Override
    public void syncActivityDataInfo(String playerId) {
    	Optional<ShootingPracticeEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        ShootingPracticeEntity entity = opEntity.get();
        int gameCount = this.getPlayerGameCount(entity);
    	PBShootingPracticePageInfoResp.Builder builder = PBShootingPracticePageInfoResp.newBuilder();
    	builder.setGameCount(gameCount);
    	builder.setScore(entity.getScoreTotal());
    	for(Map.Entry<Integer, Integer> exchange : entity.getExchangeNumMap().entrySet()){
    		PBShootingPracticeItem.Builder ebuilder = PBShootingPracticeItem.newBuilder();
        	ebuilder.setGoodsId(exchange.getKey());
        	ebuilder.setExhangeTimes(exchange.getValue());
        	builder.addExchangeItems(ebuilder);
        }
    	builder.addAllTips(getTips(ShootingPracticeExchangeCfg.class, entity.getTipSet()));
    	builder.setBuyGameDaily(entity.getBuyCountDaily());
        pushToPlayer(playerId, HP.code2.SHOOTING_PRACTICE_PAGE_INFO_RESP_VALUE, builder);
    }
    
    
    
    /**
     * 游戏结束 上报积分
     * @param playerId
     * @param rlt
     */
    public void onGameOver(String playerId,PBShootingPracticeOverReq rlt){
    	Optional<ShootingPracticeEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        ShootingPracticeEntity entity = opEntity.get();
        int gameCount = this.getPlayerGameCount(entity);
    	if(gameCount <= 0){
    		return;
    	}
    	int termId = this.getActivityTermId();
    	PBShootingPracticeGameRlt gameRlt = this.gameRltVerify(rlt, entity);
    	if(!gameRlt.isVerifyRlt()){
    		//记录
    		return;
    	}
    	ShootingPracticeKVCfg config = HawkConfigManager.getInstance().getKVInstance(ShootingPracticeKVCfg.class);
    	int score = gameRlt.getScore();
    	int total = entity.getScoreTotal() + score;
    	//最高分
    	if(score > entity.getScoreMax()){
    		entity.setScoreMax(score);
    		//刷榜
        	this.scoreRank.addScore(score, playerId);
    	}
    	//总分
    	entity.setScoreTotal(total);
    	//记录游戏次数
    	if(entity.getFreeCount() < config.getGameCountLimit()){
    		int gcount = entity.getFreeCount() + 1;
    		entity.setFreeCount(gcount);
    	}else{
    		int bgcount = entity.getBuyCount() -1;
    		entity.setBuyCount(bgcount);
    	}
    	//道具
    	RewardItem.Builder item = config.getScoreItem();
    	item.setItemCount(score);
		this.getDataGeter().takeReward(playerId, Arrays.asList(item), Action.SHOOTING_PRACTICE_GAME_REWARD, false);
		//事件
		ActivityManager.getInstance().postEvent(new ShootingPracticeScoreEvent(playerId, entity.getScoreTotal()));
		//返回消息
		int rank = 0;
		PBShootingPracticeScoreRank rankData = this.scoreRank.getPlayerCurRank(playerId);
		if(Objects.nonNull(rankData)){
			rank = rankData.getRank();
		}
		PBShootingPracticeOverResp.Builder builder = PBShootingPracticeOverResp.newBuilder();
		builder.setSuccess(true);
		builder.setStartTime(gameRlt.getStartTime());
		builder.setOverTime(gameRlt.getOverTime());
		builder.setScore(gameRlt.getScore());
		builder.setRank(rank);
		pushToPlayer(playerId, HP.code2.SHOOTING_PRACTICE_GAME_OVER_RESP_VALUE, builder);
    	this.syncActivityDataInfo(playerId);
    	this.logShootingPracticeGame(playerId, termId, score, entity.getScoreTotal(), entity.getScoreMax());
    }
    
    
    /**
     * 游戏结果验证
     */
    private PBShootingPracticeGameRlt gameRltVerify(PBShootingPracticeOverReq req,ShootingPracticeEntity entity){
    	String playerId = entity.getPlayerId();
    	ShootingPracticeKVCfg config = HawkConfigManager.getInstance().getKVInstance(ShootingPracticeKVCfg.class);
    	int hitBullet = config.getAtkValue();
    	int hitBulletAdd = 0;
    	int scoreTotal = 0;
    	PBShootingPracticeGameRlt gameRlt = new PBShootingPracticeGameRlt();
    	gameRlt.setVerifyRlt(false);
    	List<PBShootingPracticeAction> actions  = req.getActionsList();
    	List<Integer> hitsAll = new ArrayList<>();
    	for(PBShootingPracticeAction action : actions){
    		List<Integer> hits = action.getHitIdsList();
    		List<Integer> scores = action.getScoresList();
    		if(hits.size() <= 0 || scores.size() <= 0 || hits.size() > scores.size()){
    			return gameRlt;
    		}
    		int badd = 0;
    		
    		//双杀额外积分
    		if(hits.size() == 2){
    			scoreTotal += config.getDoubleKillValue();
    		}
    		//3杀额外积分
    		if(hits.size() >= 3){
    			scoreTotal += config.getTripleKillValue();
    		}
    		for(int i =0;i<hits.size();i++){
    			int hid = hits.get(i);
    			int scroe = scores.get(i);
    			ShootingPracticeEnemyCfg  ecfg = HawkConfigManager.getInstance()
            			.getConfigByKey(ShootingPracticeEnemyCfg.class, hid);
    			//命中积分是否超出范围
    			if(Objects.isNull(ecfg) || ecfg.getDoubleScore() < scroe){
    				HawkLog.logPrintln("ShootingPractice,gameRltVerify,fail,score err,playerId:{},score:{},configScore:{},hitId:{}",
    	                    playerId, scroe, ecfg.getDoubleScore(),hid);
        			return gameRlt;
    			}
    			scoreTotal += scroe;
    			badd += ecfg.getShootAddValue();
    		}
    		//增加子弹是否正确
    		if(badd != action.getHitBulletAdd()){
    			HawkLog.logPrintln("ShootingPractice,gameRltVerify,fail,BulletAdd err,playerId:{},add:{},configAdd:{},hitId:{}",
	                    playerId, action.getHitBulletAdd(), badd,SerializeHelper.collectionToString(hits));
    			return gameRlt;
    		}
    		hitBulletAdd += action.getHitBulletAdd();
    		hitsAll.addAll(hits);
    	}
    	//总体子弹个数 是否正确
    	int bulletCount = hitBulletAdd + hitBullet;
    	if(bulletCount < actions.size()){
    		HawkLog.logPrintln("ShootingPractice,gameRltVerify,fail,action size err,playerId:{},actionSize:{},configSize:{},hitIds:{}",
                    playerId, actions.size(), bulletCount,SerializeHelper.collectionToString(hitsAll));
    		return gameRlt;
    	}
    	
    	HawkLog.logPrintln("ShootingPractice,gameRltVerify,sucess,playerId:{},actionSize:{},configSize:{},addSize:{},hitIds:{}",
                playerId, actions.size(),hitBullet, hitBulletAdd,SerializeHelper.collectionToString(hitsAll));
    	gameRlt.setStartTime(req.getStartTime());
    	gameRlt.setOverTime(req.getOverTime());
    	gameRlt.setScore(scoreTotal);
    	gameRlt.setVerifyRlt(true);
    	return gameRlt;
    }
    
    
    
    /**
     * 购买次数
     * @param playerId
     * @param count
     */
    public void buyGameCount(String playerId, int buyCount){
    	Optional<ShootingPracticeEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }     
        ShootingPracticeEntity entity = opDataEntity.get();
        ShootingPracticeKVCfg config = HawkConfigManager.getInstance().getKVInstance(ShootingPracticeKVCfg.class);
        int gCount = entity.getBuyCountDaily();
        if (gCount + buyCount > config.getBuyGameTimes()) {
            //错误码
            HawkLog.logPrintln("ShootingPractice,buyGameCount,fail,countless,playerId:{},gcount:{},buyCount:{}",
                    playerId, gCount, buyCount);
            return;
        }
        int count = gCount +  buyCount;
        List<RewardItem.Builder> makeCost = new ArrayList<>();
        makeCost.add(config.getBuyGameCountCost(count));
    	boolean success = getDataGeter().consumeItems(playerId, makeCost, HP.code2.SHOOTING_PRACTICE_BUY_GAME_COUNT_REQ_VALUE, 
        		Action.SHOOTING_PRACTICE_BUY_COUNT_COST);
		if (!success) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.SHOOTING_PRACTICE_BUY_GAME_COUNT_REQ_VALUE, 
					Status.Error.ITEM_NOT_ENOUGH_VALUE);
		    return;
		}
        entity.setBuyCount(entity.getBuyCount() + buyCount);
        entity.setBuyCountDaily(count);
        //同步
        this.syncActivityDataInfo(playerId);
        ActivityManager.getInstance().postEvent(new ShootingPracticeBuyTimesEvent(playerId,buyCount));
        HawkLog.logPrintln("ShootingPractice,buyGameCount,sucess,playerId:{},buyCount:{},aftCount:{}", playerId, buyCount, count);
    }
    
    /**
     * 物品兑换
     *
     * @param playerId
     * @param exchangeId
     * @param exchangeCount
     */
    public void itemExchange(String playerId, int exchangeId, int exchangeCount) {
    	ShootingPracticeExchangeCfg exchangeConfig = HawkConfigManager.getInstance().
                getConfigByKey(ShootingPracticeExchangeCfg.class, exchangeId);
        if (Objects.isNull(exchangeConfig)) {
            return;
        }
        
        Optional<ShootingPracticeEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        ShootingPracticeEntity entity = opDataEntity.get();
        int eCount = entity.getExchangeCount(exchangeId);
        if (eCount + exchangeCount > exchangeConfig.getTimes()) {
            //错误码
            HawkLog.logPrintln("ShootingPractice,itemExchange,fail,countless,playerId: " + "{},exchangeType:{},ecount:{}",
                    playerId, exchangeId, eCount);
            return;
        }
        //消耗兑换道具
        List<RewardItem.Builder> costItems = exchangeConfig.getNeedItemList();
        for(RewardItem.Builder citem : costItems){
        	long ncount = citem.getItemCount() * exchangeCount;
        	citem.setItemCount(ncount);
        }
        boolean success = getDataGeter().consumeItems(playerId, costItems, HP.code2.SHOOTING_PRACTICE_EXCHANGE_ITEM_REQ_VALUE, 
        		Action.SHOOTING_PRACTICE_EXCHANGE_COST);
		if (!success) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.SHOOTING_PRACTICE_EXCHANGE_ITEM_REQ_VALUE, 
					Status.Error.ITEM_NOT_ENOUGH_VALUE);
		    return;    
		}
        //增加兑换次数
        entity.addExchangeCount(exchangeId, exchangeCount);
        //发奖励
        this.getDataGeter().takeReward(playerId, exchangeConfig.getGainItemList(), exchangeCount, Action.SHOOTING_PRACTICE_EXCHANGE_GET, true);
        //同步
        this.syncActivityDataInfo(playerId);
        HawkLog.logPrintln("ShootingPractice,itemExchange,sucess,playerId: " + "{},exchangeType:{},ecount:{}", playerId, exchangeId, eCount);
    }
    
    /**
     * 弹幕
     * @param playerId
     */
    public void getBarrageInfo(String playerId){
    	ShootingPracticeKVCfg config = HawkConfigManager.getInstance().getKVInstance(ShootingPracticeKVCfg.class);
    	PBShootingPracticeBarrageResp.Builder builder = PBShootingPracticeBarrageResp.newBuilder();
    	int barrageSize = config.getBarrageSize();
    	List<PBShootingPracticeScoreRank> list = this.getTotalRankList();
    	for(int i=0;i<barrageSize;i++){
    		if(i > list.size()-1){
    			break;
    		}
    		PBShootingPracticeScoreRank rank = list.get(i);
    		builder.addBarrages(rank);
    	}
    	pushToPlayer(playerId, HP.code2.SHOOTING_PRACTICE_BARRAGE_RESP_VALUE, builder);
    }
    
    /**
     * 获取排行榜信息
     * @param playerId
     */
    public void getRanKInfo(String playerId){
    	PBShootingPracticeRankResp.Builder builder = PBShootingPracticeRankResp.newBuilder();
    	builder.setSelf(this.getCurRank(playerId));
    	builder.addAllRanks(this.getTotalRankList());
    	pushToPlayer(playerId, HP.code2.SHOOTING_PRACTICE_RANK_RESP_VALUE, builder);
    }
    private PBShootingPracticeScoreRank getCurRank(String playerId){
		PBShootingPracticeScoreRank sbuilder = this.scoreRank.getPlayerCurRank(playerId);
		return sbuilder;
	}
    private List<PBShootingPracticeScoreRank> getTotalRankList(){
    	List<PBShootingPracticeScoreRank> ranks = new ArrayList<>();
		List<PBShootingPracticeScoreRank> personList = this.scoreRank.getRankList();
		if(personList == null){
			return ranks;
		}
		for(PBShootingPracticeScoreRank data : personList){
			try {
				PBShootingPracticeScoreRank.Builder personBuilder = PBShootingPracticeScoreRank.newBuilder();
				String playerName = getDataGeter().getPlayerName(data.getPlayerId());
				String guildName = getDataGeter().getGuildNameByByPlayerId(data.getPlayerId());
				String guildTag = getDataGeter().getGuildTagByPlayerId(data.getPlayerId());
				if(!HawkOSOperator.isEmptyString(playerName)){
					personBuilder.setPlayerName(playerName);
					personBuilder.setPlayerId(data.getPlayerId());
				}
				if(!HawkOSOperator.isEmptyString(guildName)){
					personBuilder.setGuildName(guildName);
				}
				if(!HawkOSOperator.isEmptyString(guildTag)){
					personBuilder.setGuildTag(guildTag);
				}
				personBuilder.setScore(data.getScore());
				personBuilder.setRank(data.getRank());
				ranks.add(personBuilder.build());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return ranks;
	}
    
    
    
    
    
    /**
     * 获取玩家持有的游戏币
     * @param entity
     * @return
     */
    private int getPlayerGameCount(ShootingPracticeEntity entity){
    	 ShootingPracticeKVCfg config = HawkConfigManager.getInstance().getKVInstance(ShootingPracticeKVCfg.class);
    	 int gameCount = Math.max(0, config.getGameCountLimit() - entity.getFreeCount()) + entity.getBuyCount();
    	 return gameCount;
    }
    
    
    
    
    
  
    /**
     *  初始化玩家数据
     * @param playerId
     */
    private void initActivityInfo(String playerId){
    	if(this.getActivityTermId() <=0){
    		return;
    	}
    	Optional<ShootingPracticeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.logPrintln("ShootingPracticeEntity,initData,fail1,playerId:{}",playerId);
			return;
		}
		ShootingPracticeEntity entity = opEntity.get();
		// 成就已初始化
		if (entity.getInitTime() > 0) {
			HawkLog.logPrintln("ShootingPracticeEntity,initData,fail2,playerId:{}",playerId);
			return;
		}
		//删除现有活动道具
		ShootingPracticeKVCfg config = HawkConfigManager.getInstance().getKVInstance(ShootingPracticeKVCfg.class);
		RewardItem.Builder recoverItem = config.getScoreItem();
		int rcount = this.getDataGeter().getItemNum(playerId, recoverItem.getItemId());
		if(rcount  > 0){
			recoverItem.setItemCount(rcount);
			getDataGeter().cost(playerId, Arrays.asList(recoverItem), Action.SHOOTING_PRACTICE_ITEM_RECOVER);
		};
    	
		//任务
		List<AchieveItem> updateList = new ArrayList<>();
		List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
		List<AchieveItem> itemListDay = new CopyOnWriteArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<ShootingPracticeAchieveCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(ShootingPracticeAchieveCfg.class);
		while (configIterator.hasNext()) {
			ShootingPracticeAchieveCfg next = configIterator.next();
			//每日任务
			if(next.getPage() == 1){
				AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
				itemListDay.add(item);
				updateList.add(item);
			}
			//积分任务
			if(next.getPage() == 2){
				AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
				itemList.add(item);
				updateList.add(item);
			}
			
		}
		long curTime = HawkTime.getMillisecond();
		entity.setInitTime(curTime);
		entity.recordLoginDay();
		entity.resetItemListDay(itemListDay);
		entity.resetItemList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, updateList), true);
		//抛每天登录事件
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, entity.getLoginDaysCount(), this.providerActivityId()), true);
    }
    
    
    /**
     * 排行榜重置
     */
    private void rankRest(){
    	this.scoreRank = new ShootingPracticeRank();
    }
    
    
    /**
     * 更新玩家游戏次数
     * @param entity
     */
    /**
     * @param entity
     */
    private void updateGameCount(ShootingPracticeEntity entity){
    	ShootingPracticeKVCfg config = HawkConfigManager.getInstance().getKVInstance(ShootingPracticeKVCfg.class);
    	//如果玩家现在持有的次数 大于 上限  不给补
    	int count = this.getPlayerGameCount(entity);
    	if(count >= config.getGameCountLimit()){
    		entity.setBuyCountDaily(0);
			return;
		}
    	int free = config.getGameCountLimit() - entity.getBuyCount();
    	int finish = config.getGameCountLimit() - free;
    	finish = Math.max(0, finish);
    	entity.setFreeCount(finish);
    	entity.setBuyCountDaily(0);
    }
    
    /**
     * 刷新每日任务
     * @param entity
     */
    private void updateAchieveItemsDay(ShootingPracticeEntity entity) {
        ConfigIterator<ShootingPracticeAchieveCfg> configIterator =
                HawkConfigManager.getInstance().getConfigIterator(ShootingPracticeAchieveCfg.class);
		//赋值使用的配置ID
        List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
        //遍历成就配置，向数据库对象添加成就项
        while (configIterator.hasNext()) {
        	ShootingPracticeAchieveCfg next = configIterator.next();
            if(next.getPage() == 1){
				AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
				itemList.add(item);
			}
        }
        entity.setItemListDay(itemList);
        AchievePushHelper.pushAchieveUpdate(entity.getPlayerId(), entity.getItemListDay());
       
    } 
    
    
    private void sendRankRewardMail(){
    	int termId = this.getActivityTermId();
		List<ShootingPracticeRankCfg> rankListReward = HawkConfigManager.getInstance()
				.getConfigIterator(ShootingPracticeRankCfg.class).toList();
		int high = getRankHasRewardMaxRank();
		if(high == 0){
			HawkLog.errPrintln("ShootingPractice sendRewardMail rankReward error, config error");
			return;
		}
		List<PBShootingPracticeScoreRank> rankPlayers = this.scoreRank.getHasRewardRankList(termId, high);
		for (PBShootingPracticeScoreRank member : rankPlayers) {
			long score = member.getScore();
			int rank = member.getRank();
			for (ShootingPracticeRankCfg rewardCfg : rankListReward) {
				if (rewardCfg.getRankLow() < rank || rewardCfg.getRankHigh() > rank) {
					continue;
				}
				try {
					Object[] content;
					content = new Object[2];
					content[0] = score;
					content[1] = rank;
					sendMailToPlayer(member.getPlayerId(), MailId.SHOOTING_PRACTICE_RANK_REWARD, new Object[0], new Object[0], content, rewardCfg.getRewardList());
					HawkLog.logPrintln("ShootingPractice send rankReward, playerId:{}, score: {}, rank:{}, cfgId:{}", member.getPlayerId(), score, rank, rewardCfg.getId());
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}
	
    private int getRankHasRewardMaxRank(){
		int max = 0;
		ConfigIterator<ShootingPracticeRankCfg> ite = HawkConfigManager.getInstance().getConfigIterator(ShootingPracticeRankCfg.class);
		while(ite.hasNext()){
			ShootingPracticeRankCfg config = ite.next();
			if(config.getRankLow() > max){
				max = config.getRankLow();
			}
		}
		return max;
	}
    
    
    
    /**
     * 游戏结束积分记录
     * @param playerId
     * @param termId
     * @param score
     * @param totalScore
     * @param maxScore
     */
    private void logShootingPracticeGame(String playerId,int termId,int score,int totalScore,int maxScore){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId); //期数
        param.put("score",score); //本局积分
        param.put("totalScore", totalScore); //总积分
        param.put("maxScore", maxScore); //单局最高积分
        getDataGeter().logActivityCommon(playerId, LogInfoType.shooting_practice_game, param);
    }
    
    @Override
    public void onPlayerMigrate(String playerId) {
    	int termId = this.getActivityTermId();
    	if(termId <= 0){
    		return;
    	}
    	this.scoreRank.delPlayerScore(playerId);
    }
    
    /**
	 * 删除排行榜数据
	 */
	public void removePlayerRank(String playerId) {
		if (this.getActivityEntity().getActivityState() == com.hawk.activity.type.ActivityState.HIDDEN) {
			return;
		}
		
		scoreRank.delPlayerScore(playerId);
		scoreRank.doRankSort();
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
}
