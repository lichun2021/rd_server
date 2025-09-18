package com.hawk.activity.type.impl.questTreasure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple3;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDaysActivityEvent;
import com.hawk.activity.event.impl.QuestTreasureBoxScoreEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.questTreasure.cfg.QuestTreasureAchieveCfg;
import com.hawk.activity.type.impl.questTreasure.cfg.QuestTreasureBoxCfg;
import com.hawk.activity.type.impl.questTreasure.cfg.QuestTreasureKVCfg;
import com.hawk.activity.type.impl.questTreasure.cfg.QuestTreasureShopCfg;
import com.hawk.activity.type.impl.questTreasure.entity.QuestTreasureAchieveItem;
import com.hawk.activity.type.impl.questTreasure.entity.QuestTreasureEntity;
import com.hawk.activity.type.impl.questTreasure.entity.QuestTreasureGame;
import com.hawk.game.protocol.Activity.QuestTreasureGameBox;
import com.hawk.game.protocol.Activity.QuestTreasureGameBoxAchieve;
import com.hawk.game.protocol.Activity.QuestTreasureGameData;
import com.hawk.game.protocol.Activity.QuestTreasureGamePos;
import com.hawk.game.protocol.Activity.QuestTreasureGameWalkRandomResp;
import com.hawk.game.protocol.Activity.QuestTreasureInfoSync;
import com.hawk.game.protocol.Activity.QuestTreasureShopData;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.serialize.string.SerializeHelper;

public class QuestTreasureActivity extends ActivityBase implements AchieveProvider,IExchangeTip<QuestTreasureShopCfg>{
	
	

	public QuestTreasureActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.QUEST_TREASURE;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		QuestTreasureActivity activity = new QuestTreasureActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<QuestTreasureEntity> queryList = HawkDBManager.getInstance()
				.query("from QuestTreasureEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			QuestTreasureEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		QuestTreasureEntity entity = new QuestTreasureEntity(playerId, termId);
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
		Optional<QuestTreasureEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		QuestTreasureEntity entity = opEntity.get();
		if (entity.getAchieveList().isEmpty()) {
			initActivityInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getAchieveList(), entity);
		return Optional.of(achieveItems);
	}
	
	
	

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(QuestTreasureAchieveCfg.class, achieveId);
		return config;
	}

	
	@Override
	public Action takeRewardAction() {
		return Action.QUEST_TREASURE_ACHIEVE_REWARD;
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		int termId = this.getActivityTermId();
		if(termId <= 0){
			//回收道具
	    	this.recoverItem(playerId);
		}
		this.initActivityInfo(playerId);
	}
	
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.QUEST_TREASURE_INIT, ()-> {
				initActivityInfo(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	
	
	/**
	 * 初始化活动数据
	 * @param playerId
	 */
	public void initActivityInfo(String playerId){
		int termId = this.getActivityTermId();
		if(termId <= 0){
			return;
		}
		Optional<QuestTreasureEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		QuestTreasureEntity entity = opEntity.get();
		if(entity.getInitTime() > 0){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		//记录初始化时间
		entity.setInitTime(curTime);
		//记录登录天数
		entity.recordLoginDay();
		//游戏初始
		QuestTreasureGame game = this.createNewGame();
		entity.setGame(game);
		//初始化任务
		updateAchieveData(entity);
		//抛每天登录事件
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, entity.getLoginDaysCount(), this.providerActivityId()), true);
        //回收道具
        this.recoverItem(playerId);
        HawkLog.logPrintln("QuestTreasureActivity,initActivityInfo,playerId:{}", playerId);
        
	}
	
	
	
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		String playerId = event.getPlayerId();
        //活动是否开启，没开不继续处理
        if (!isOpening(playerId)) {
            return;
        }
        //去当前期数
        int termId = getActivityTermId(playerId);
        //取当前期数结束时间
        long hiddenTime = getTimeControl().getHiddenTimeByTermId(termId, playerId);
        //取当前时间
        long now = HawkTime.getMillisecond();
        //如果当前时间大于当前期数结束时间，不继续处理
        if (now >= hiddenTime) {
            return;
        }
        Optional<QuestTreasureEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        QuestTreasureEntity entity = opEntity.get();
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
        // 成就已初始化
        updateAchieveData(entity);
        //给客户端同步数据
        this.syncActivityDataInfo(playerId);
        //抛每天登录事件
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, entity.getLoginDaysCount(), this.providerActivityId()), true);
        HawkLog.logPrintln("QuestTreasureActivity ContinueLoginEvent reset playerId:{}", playerId);
	}
	
	
	/**
	 * 新建游戏
	 * @return
	 */
	public QuestTreasureGame createNewGame(){
		QuestTreasureGame game = new QuestTreasureGame();
		game.init();
		return game;
	}
	
	/**
	 * 更新任务
	 * @param entity
	 */
	public void updateAchieveData(QuestTreasureEntity entity){
		// 成就已初始化
		long curTime = HawkTime.getMillisecond();
		List<QuestTreasureAchieveItem> itemList = new CopyOnWriteArrayList<>();
		Map<Integer,QuestTreasureAchieveItem> amap = new HashMap<>();
		List<AchieveItem> list = entity.getAchieveList();
		for(AchieveItem item : list){
			QuestTreasureAchieveItem qitem =  (QuestTreasureAchieveItem) item;
			QuestTreasureAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(QuestTreasureAchieveCfg.class, qitem.getAchieveId());
			//配置不在
			if(Objects.isNull(cfg)){
				continue;
			}
			int rday = cfg.getRefreshDays();
			int turn = this.getAchieveTrun(rday, curTime);
			//该刷新了
			if(turn != qitem.getTurn()){
				continue;
			}
			amap.put(qitem.getAchieveId(), qitem);
		}
		//初始添加成就项
		ConfigIterator<QuestTreasureAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(QuestTreasureAchieveCfg.class);
		while (configIterator.hasNext()) {
			QuestTreasureAchieveCfg next = configIterator.next();
			if(amap.containsKey(next.getAchieveId())){
				continue;
			}
			int rday = next.getRefreshDays();
			int turn = this.getAchieveTrun(rday, curTime);
			QuestTreasureAchieveItem item = QuestTreasureAchieveItem.valueOf(next.getAchieveId(),turn);
			amap.put(item.getAchieveId(), item);
		}
		itemList.addAll(amap.values());
		entity.setAchieveList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), entity.getAchieveList()), true);
		HawkLog.logPrintln("QuestTreasureActivity updateAchieveData playerId:{}", entity.getPlayerId(),SerializeHelper.collectionToString(itemList));
	}

	
	
	/**
	 * 获取界面信息
	 * @param playerId
	 * @param hp
	 */
	public void getPageInfo(String playerId,int hp){
		Optional<QuestTreasureEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        QuestTreasureEntity entity = opEntity.get();
        //重置游戏
        this.checkGameRest(entity);
        this.syncActivityDataInfo(playerId);
	}
	
	/**
	 * 设置路径点
	 * @param playerId
	 * @param plist
	 * @param hp
	 */
	public void setGameChoosePoints(String playerId,List<QuestTreasureGamePos> plist,int hp){
		Optional<QuestTreasureEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        QuestTreasureEntity entity = opEntity.get();
        QuestTreasureKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(QuestTreasureKVCfg.class);
        if(entity.getGameRefreshCount() > cfg.getGameRefreshCount()){
        	return;
		}
        QuestTreasureGame game =  entity.getGame();
        int rlt = game.addChoosePoints(plist);
        if(rlt != Status.SysError.SUCCESS_OK_VALUE){
        	PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,hp, rlt);
        	return;
        }
        PlayerPushHelper.getInstance().responseSuccess(playerId,hp);
        this.syncActivityDataInfo(playerId);
        this.logQuestTreasureGamePointChoose(entity.getPlayerId(), entity.getTermId(), game.getRolePos(),
        		JSON.toJSONString(game.getBoxMap()),JSON.toJSONString(game.getChooseList()));
	}
	
	/**
	 * 随机前进
	 * @param playerId
	 */
	public void randomWalk(String playerId,int hp){
		Optional<QuestTreasureEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        QuestTreasureEntity entity = opEntity.get();
        QuestTreasureGame game =  entity.getGame();
        boolean rlt = game.checkRandom();
        if(!rlt){
        	return;
        }
		QuestTreasureKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(QuestTreasureKVCfg.class);
		//扣费
		List<RewardItem.Builder> costList = new ArrayList<>();
		costList.add(cfg.getRandomItem(1));
        boolean success =this.getDataGeter().consumeItems(playerId, costList,hp, 
        		Action.QUEST_TREASURE_RANDOM_WALK_COST);
		if (!success) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hp, 
					Status.Error.ITEM_NOT_ENOUGH_VALUE);
		    return;
		}
		HawkTuple3<List<Integer>, Map<Integer,Integer>,Integer> randomRlt = game.randomWalk();
		if(Objects.isNull(randomRlt)){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hp, 
					Status.SysError.CONFIG_ERROR_VALUE);
			return;
		}
		List<Integer> wpoints = randomRlt.first;
		Map<Integer,Integer> box = randomRlt.second;
		int randomNum = randomRlt.third;
        int boxScoreToatalBef = entity.getBoxScore();
        int boxScoreToatal = entity.getBoxScore();
		List<RewardItem.Builder> rewardsAll = new ArrayList<>();
		
		QuestTreasureGameWalkRandomResp.Builder builder = QuestTreasureGameWalkRandomResp.newBuilder();
		//发奖
		for(int point : wpoints){
			QuestTreasureGamePos.Builder gpbuilder = QuestTreasureGame.genQuestTreasureGamePos(point);
			builder.addPassPos(gpbuilder);
		}
		for(Map.Entry<Integer, Integer> boxEntry : box.entrySet()){
			int pos = boxEntry.getKey();
			int boxId = boxEntry.getValue();
			QuestTreasureBoxCfg boxcfg = HawkConfigManager.getInstance().getConfigByKey(QuestTreasureBoxCfg.class, boxId);
			if(Objects.isNull(boxcfg)){
				continue;
			}
			QuestTreasureGameBoxAchieve.Builder achieveBuilder = QuestTreasureGameBoxAchieve.newBuilder();
			//宝箱信息
			QuestTreasureGameBox.Builder boxBuilder = QuestTreasureGameBox.newBuilder();
			QuestTreasureGamePos.Builder gpbuilder = QuestTreasureGame.genQuestTreasureGamePos(pos);
			boxBuilder.setPos(gpbuilder);
			boxBuilder.setBoxId(boxId);
			achieveBuilder.setBox(boxBuilder);
			//宝箱奖励
			List<String> randomAward = ActivityManager.getInstance().getDataGeter().getAwardFromAwardCfg(boxcfg.getReward());
			List<RewardItem.Builder> rewards = new ArrayList<>();
			for (String rewardStr : randomAward) {
	            List<RewardItem.Builder> rewardBuilders = RewardHelper.toRewardItemImmutableList(rewardStr);
	            rewards.addAll(rewardBuilders);
	        }
			for(RewardItem.Builder rb : rewards){
				achieveBuilder.addItems(rb);
				rewardsAll.add(rb.clone());
			}
			//积分情况
			boxScoreToatal += boxcfg.getBoxScore() ;
			achieveBuilder.setBoxScore(boxcfg.getBoxScore());
			achieveBuilder.setBoxScoreTotal(boxScoreToatal);
			builder.addBoxAchieves(achieveBuilder);
		}
		//是否结束
		boolean over = game.checkGameOver();
		builder.setGameOver(over);
		builder.setRandomNum(randomNum);
		//修改数据
		entity.setBoxScore(boxScoreToatal);
		//检查刷新
		this.checkGameRest(entity);
		//奖励
		ActivityManager.getInstance().getDataGeter().takeReward(playerId, rewardsAll, 1, Action.QUEST_TREASURE_RANDOM_WALK_REWARD, true,RewardOrginType.QUEST_TREASURE_RANDOM_WALK_REWARD);
		//返回
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.QUEST_TREASURE_GAME_WALK_RANDOM_RESP_VALUE, builder));
		//抛事件
		ActivityManager.getInstance().postEvent(new QuestTreasureBoxScoreEvent(playerId,boxScoreToatal), true);
		//日志
		this.logQuestTreasureGameRandomWalk(entity.getPlayerId(), entity.getTermId(), game.getRolePos(),
	        		JSON.toJSONString(game.getBoxMap()),JSON.toJSONString(game.getChooseList()),randomNum,
	        		JSON.toJSONString(wpoints),JSON.toJSONString(box),boxScoreToatalBef,boxScoreToatal);
	}
	
	
	/**
	 * 检查结束重置
	 * @param entity
	 * @param sync
	 */
	public void checkGameRest(QuestTreasureEntity entity){
		QuestTreasureGame game = entity.getGame();
		boolean over = game.checkGameOver();
		if(!over){
			return;
		}
		int rcount = entity.getGameRefreshCount();
		QuestTreasureGame newGame = this.createNewGame();
		entity.setGame(newGame);
		entity.setGameRefreshCount(rcount + 1);
		this.logQuestTreasureGameRefersh(entity.getPlayerId(), entity.getTermId(), newGame.getRolePos(),JSON.toJSONString(newGame.getBoxMap()));
	}
	
	
	
	
	
	/**
	 * 购买随机道具
	 * @param playerId
	 * @param count
	 * @param hp
	 */
	public void itemBuy(String playerId, int count,int hp){
		QuestTreasureKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(QuestTreasureKVCfg.class);
        Optional<QuestTreasureEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        QuestTreasureEntity entity = opDataEntity.get();
        //消耗兑换道具
        List<RewardItem.Builder> costItems = cfg.getDicePriceItem(count);
        boolean success = getDataGeter().consumeItems(playerId, costItems,hp, 
        		Action.QUEST_TREASURE_RANDOM_ITEM_BUY_COST);
		if (!success) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hp, 
					Status.Error.ITEM_NOT_ENOUGH_VALUE);
		    return;
		}
		
		List<RewardItem.Builder> gianList = new ArrayList<>();
		gianList.add(cfg.getRandomItem(count));
        //发奖励
        this.getDataGeter().takeReward(playerId,gianList, 1, Action.QUEST_TREASURE_RANDOM_ITEM_BUY_GET, true);
        PlayerPushHelper.getInstance().responseSuccess(playerId, hp);
        this.logQuestTreasureGameRandomItemBuy(playerId, entity.getTermId(), count);
        HawkLog.logPrintln("QuestTreasureActivity,itemBuy,sucess,playerId:{},count:{}", playerId, count);
	}
	
	
	
    /**
     * 商店物品兑换
     *
     * @param playerId
     * @param exchangeId
     * @param exchangeCount
     */
    public void itemExchange(String playerId, int exchangeId, int exchangeCount,int hp) {
    	QuestTreasureShopCfg exchangeConfig = HawkConfigManager.getInstance().
                getConfigByKey(QuestTreasureShopCfg.class, exchangeId);
        if (Objects.isNull(exchangeConfig)) {
            return;
        }
        
        Optional<QuestTreasureEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        QuestTreasureEntity entity = opDataEntity.get();
        int eCount = entity.getBuyCount(exchangeId);
        if (eCount + exchangeCount > exchangeConfig.getTimes()) {
            //错误码
            HawkLog.logPrintln("QuestTreasureActivity,itemExchange,fail,countless,playerId: " + "{},exchangeType:{},ecount:{}",
                    playerId, exchangeId, eCount);
            return;
        }
        //消耗兑换道具
        List<RewardItem.Builder> costItems = exchangeConfig.getNeedItemList();
        for(RewardItem.Builder citem : costItems){
        	long ncount = citem.getItemCount() * exchangeCount;
        	citem.setItemCount(ncount);
        }
        boolean success = getDataGeter().consumeItems(playerId, costItems,hp, 
        		Action.QUEST_TREASURE_SHOP_BUY_COST);
		if (!success) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hp, 
					Status.Error.ITEM_NOT_ENOUGH_VALUE);
		    return;    
		}
        //增加兑换次数
        entity.addBuyCount(exchangeId, exchangeCount);
        //发奖励
        this.getDataGeter().takeReward(playerId, exchangeConfig.getGainItemList(), exchangeCount, Action.QUEST_TREASURE_SHOP_BUY_GET, true);
        //同步
        this.syncActivityDataInfo(playerId);
        this.logQuestTreasureShopBuy(playerId,  entity.getTermId(), exchangeId, eCount);
        HawkLog.logPrintln("QuestTreasureActivity,itemExchange,sucess,playerId: " + "{},exchangeType:{},ecount:{}", playerId, exchangeId, eCount);
    }
	
    
	
	

    /**
     * 同步活动信息
     */
	@Override
	public void syncActivityDataInfo(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<QuestTreasureEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return ;
		}
		QuestTreasureEntity entity = opEntity.get();
		QuestTreasureInfoSync.Builder builder  = genQuestTreasureInfo(entity);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.QUEST_TREASURE_INFO_RESP_VALUE, builder));
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>"+JSON.toJSONString(entity.getGame()));
	}
	
	
	
	/**
	 * 生成页面
	 * @param entity
	 * @return
	 */
	public QuestTreasureInfoSync.Builder genQuestTreasureInfo(QuestTreasureEntity entity){
		QuestTreasureInfoSync.Builder builder = QuestTreasureInfoSync.newBuilder();
		//游戏数据
		QuestTreasureGame game = entity.getGame();
		QuestTreasureGameData.Builder gbuilder = game.genBuilder();
		builder.setGameData(gbuilder);
		//商店数据
        for(Map.Entry<Integer, Integer> shop : entity.getBuyInfoMap().entrySet()){
        	QuestTreasureShopData.Builder sbuilder = QuestTreasureShopData.newBuilder();
        	sbuilder.setShopId(shop.getKey());
        	sbuilder.setCount(shop.getValue());
        	builder.addShopDatas(sbuilder);
        }
        //关注
        builder.addAllTips(getTips(QuestTreasureShopCfg.class, entity.getTipSet()));
        //宝箱积分
        builder.setBoxScoreTotal(entity.getBoxScore());
        //完成次数
        builder.setFinishCount(entity.getGameRefreshCount());
		return builder;
	}
	
	
	
	/**
	 * 获取任务刷新轮次
	 * @param interval
	 * @param curTime
	 * @return
	 */
	public int getAchieveTrun(int interval,long curTime){
		int termId = this.getActivityTermId();
		if(termId <= 0){
			return 0;
		}
		long startTime = this.getTimeControl().getStartTimeByTermId(termId);
		if(startTime <= 0){
			return 0;
		}
		//活动开放第几天
		int day = HawkTime.calcBetweenDays(new Date(startTime), new Date(curTime)) + 1;
		int trun = day / interval;
		int add = day % interval;
		if(add >0){
			trun += 1;
		}
		return trun;
	}
	
	
	/** 发送奖励mail
	 */
	public void sendRewardByMail(String playerId, List<RewardItem.Builder> rewardList,int mailId){
		try {
			
			MailId mId = MailId.valueOf(mailId);
			// 邮件发送奖励
			Object[] content = new Object[0];
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			//发邮件
			sendMailToPlayer(playerId, mId, title, subTitle, content, rewardList, true);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	   
    /**
     * 回收道具
     * @param playerId
     */
    public void recoverItem(String playerId){
    	QuestTreasureKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(QuestTreasureKVCfg.class);
    	Map<String,String> rmap = cfg.getRecoverMap();
    	
    	for(Map.Entry<String, String> entry : rmap.entrySet()){
    		try {
    			String costItem = entry.getKey();
        		String gainItem = entry.getValue();
        		//活动结束时回收道具ID
        		RewardItem.Builder costItemBuilder = RewardHelper.toRewardItem(costItem);
        		if(Objects.isNull(costItemBuilder)){
        			continue;
        		}
        		//取玩家身上此道具的数量
        		int count = this.getDataGeter().getItemNum(playerId, costItemBuilder.getItemId());
        		if(count <= 0){
        			continue;
        		}
        		//扣除道具的数据准备
        		List<RewardItem.Builder> costList = new ArrayList<>();
        		RewardItem.Builder costBuilder = RewardItem.newBuilder();
        		//类型为道具
        		costBuilder.setItemType(ItemType.TOOL_VALUE);
        		//待扣除物品ID
        		costBuilder.setItemId(costItemBuilder.getItemId());
        		//待扣除的物品数量
        		costBuilder.setItemCount(count);
        		//把待扣除的物品数据加入参数容器
        		costList.add(costBuilder);
        		//注意这里先扣除源道具，如果失败，不给兑换后的道具
        		boolean cost = this.getDataGeter().cost(playerId,costList, 1, Action.QUEST_TREASURE_RECOVER_COST, true);
        		//扣除失败不继续处理
        		if (!cost) {
        			continue;
        		}
        		HawkLog.logPrintln("QuestTreasureActivity,recoverItem,cost,playerId{},itemId:{},count:{}", playerId,costBuilder.getItemId(),count);
        		if(!HawkOSOperator.isEmptyString(gainItem)){
        			RewardItem.Builder gainItemBuilder = RewardHelper.toRewardItem(gainItem);
        			long gcount = gainItemBuilder.getItemCount() * count;
        			gainItemBuilder.setItemCount(gcount);
        			List<RewardItem.Builder> gainList = new ArrayList<>();
        			gainList.add(gainItemBuilder);
        			Object[] content =  new Object[]{count};
        			Object[] title =  new Object[]{count};
        			Object[] subtitle =  new Object[]{count};
    				this.getDataGeter().sendMail(playerId, MailId.QUEST_TREASURE_RECOVER, title, subtitle, content,
    						gainList, false);
    				HawkLog.logPrintln("QuestTreasureActivity,recoverItem,gain,playerId{},itemId:{},count:{}", playerId,gainItemBuilder.getItemId(),gainItemBuilder.getItemCount());
        		}
			} catch (Exception e) {
				continue;
			}
    	}
	}

	


    /**
     * <!--秘境寻宝游戏刷新  -->
     * @param playerId
     * @param termId   期数
     * @param rolePos  角色初始位置
     * @param boxPos   宝箱散落位置
     */
    private void logQuestTreasureGameRefersh(String playerId,int termId,int rolePos, String boxPos){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("rolePos", rolePos);
        param.put("boxPos", boxPos);
        getDataGeter().logActivityCommon(playerId, LogInfoType.quest_treasure_game_refersh, param);
    }
    
    
    
    /**
     * <!--秘境寻宝游戏路径选择  -->
     * @param termId   期数
     * @param rolePos  角色初始位置
     * @param boxPos   宝箱散落位置
     * @param choosePos 行进路径
     */
    private void logQuestTreasureGamePointChoose(String playerId,int termId,int rolePos, String boxPos,String choosePos){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("rolePos", rolePos);
        param.put("boxPos", boxPos);
        param.put("choosePos", choosePos);
        getDataGeter().logActivityCommon(playerId, LogInfoType.quest_treasure_game_point_choose, param);
    }
    
    
    /**
     * <!--秘境寻宝游戏随机前进  -->
     * @param termId   期数
     * @param rolePos  角色初始位置
     * @param boxPos   宝箱散落位置
     * @param choosePos 行进路径
     * @param randomNum   随机前进步数
     * @param walkPos     前进路线
     * @param achieveBox  宝箱获得
     * @param scoreAdd  宝箱积分获得
     * @param scoreTotal 宝箱积分总计
     */
    private void logQuestTreasureGameRandomWalk(String playerId,int termId,int rolePos, String boxPos,String choosePos,int randomNum,
    		String walkPos,String achieveBox,int scoreAdd,int scoreTotal){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("rolePos", rolePos);
        param.put("boxPos", boxPos);
        param.put("choosePos", choosePos);
        param.put("randomNum", randomNum);
        param.put("walkPos", walkPos);
        param.put("achieveBox", achieveBox);
        param.put("scoreAdd", scoreAdd);
        param.put("scoreTotal", scoreTotal);
        getDataGeter().logActivityCommon(playerId, LogInfoType.quest_treasure_game_random_walk, param);
    }
    
    
    /**
     * <!--秘境寻宝随机道具购买  -->
     * @param playerId
     * @param termId
     * @param count 购买数量
     */
    private void logQuestTreasureGameRandomItemBuy(String playerId,int termId,int count){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("count", count);
        getDataGeter().logActivityCommon(playerId, LogInfoType.quest_treasure_game_random_item_buy, param);
    }
    
    
    /**
     * <!-- 秘境寻宝商店购买 -->
     * @param playerId
     * @param termId
     * @param giftId
     * @param score
     */
    private void logQuestTreasureShopBuy(String playerId,int termId,int shopId,int count){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("shopId", shopId);
        param.put("count", count);
        getDataGeter().logActivityCommon(playerId, LogInfoType.quest_treasure_shop_buy, param);
    }
}
