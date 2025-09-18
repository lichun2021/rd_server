package com.hawk.activity.type.impl.submarineWar;

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
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.eventbus.Subscribe;
import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDaysActivityEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.impl.SubmarineWarScoreEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarAchieveCfg;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarKVCfg;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarOrderLevelCfg;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarPassAuthorityCfg;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarShopCfg;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarSkillItemCfg;
import com.hawk.activity.type.impl.submarineWar.entity.SubmarineWarEntity;
import com.hawk.activity.type.impl.submarineWar.entity.SubmarineWarGame;
import com.hawk.activity.type.impl.submarineWar.entity.SubmarineWarOrder;
import com.hawk.activity.type.impl.submarineWar.rank.SubmarineWarRank;
import com.hawk.activity.type.impl.submarineWar.rank.SubmarineWarRankGroup;
import com.hawk.game.protocol.Activity.PBSubmarineWarRank;
import com.hawk.game.protocol.Activity.SubmarineWarBarrageResp;
import com.hawk.game.protocol.Activity.SubmarineWarGameData;
import com.hawk.game.protocol.Activity.SubmarineWarGameStage;
import com.hawk.game.protocol.Activity.SubmarineWarGameStagePassResp;
import com.hawk.game.protocol.Activity.SubmarineWarGameStartResp;
import com.hawk.game.protocol.Activity.SubmarineWarInfoSync;
import com.hawk.game.protocol.Activity.SubmarineWarRankResp;
import com.hawk.game.protocol.Activity.SubmarineWarShopData;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.serialize.string.SerializeHelper;

public class SubmarineWarActivity extends ActivityBase implements AchieveProvider,IExchangeTip<SubmarineWarShopCfg>{
	
	
	public SubmarineWarRank rank = new SubmarineWarRank();
	
	public SubmarineWarActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SUBMARINE_WAR;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SubmarineWarActivity activity = new SubmarineWarActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SubmarineWarEntity> queryList = HawkDBManager.getInstance()
				.query("from SubmarineWarEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SubmarineWarEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SubmarineWarEntity entity = new SubmarineWarEntity(playerId, termId);
		return entity;
	}
	

	@Override
	public boolean isProviderActive(String playerId) {
		// 活动是否失效
		if (isInvalid()) {
			return false;
		}
		ActivityState state = getIActivityEntity(playerId).getActivityState();
		if (state != ActivityState.OPEN) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		if (isInvalid()) {
			return false;
		}
		ActivityState state = getIActivityEntity(playerId).getActivityState();
		if (state != ActivityState.OPEN && state != ActivityState.END) {
			return false;
		}
		return true;
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<SubmarineWarEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		SubmarineWarEntity entity = opEntity.get();
		if (entity.getAchieveList().isEmpty()) {
			initActivityInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getAchieveList(), entity);
		return Optional.of(achieveItems);
	}
	
	@Override
	public List<Builder> getRewardList(String playerId, AchieveConfig achieveConfig) {
		List<RewardItem.Builder> norlist = AchieveProvider.super.getRewardList(playerId, achieveConfig);
		List<RewardItem.Builder> rlt = new ArrayList<>();
		rlt.addAll(norlist);
		if(achieveConfig instanceof SubmarineWarAchieveCfg){
			SubmarineWarAchieveCfg submarineWarAchieveCfg = (SubmarineWarAchieveCfg) achieveConfig;
			Optional<SubmarineWarEntity> opEntity = this.getPlayerDataEntity(playerId);
			if (opEntity.isPresent()) {
				SubmarineWarEntity entity = opEntity.get();
				SubmarineWarOrder order = entity.getOrder();
				if (Objects.nonNull(order) && order.getAdvance() > 0) {
					rlt.addAll(submarineWarAchieveCfg.getPassExtraRewardList());
					HawkLog.logPrintln("SubmarineWarActivity,achieve,getRewardList,extReward,playerId:{},achieveId:{}", 
							playerId,achieveConfig.getAchieveId());
				}
			}
		}
		return rlt;
	}

	
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarAchieveCfg.class, achieveId);
		return config;
	}

	
	@Override
	public Action takeRewardAction() {
		return Action.SUBMARINE_WAR_ACHIEVE_REWARD;
	}

	
	@Override
	public void onTakeRewardSuccessAfter(String playerId, List<RewardItem.Builder> rewardList, int achieveId) {
		Optional<SubmarineWarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SubmarineWarEntity entity = opEntity.get();
		SubmarineWarOrder order = entity.getOrder();
		if(Objects.isNull(order)){
			return;
		}
		SubmarineWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
		RewardItem.Builder score = cfg.getScoreItem(1);
		long gameScore = 0l;
		for(RewardItem.Builder builder : rewardList){
			if(builder.getItemId() == score.getItemId()){
				gameScore += builder.getItemCount();
			}
		}
		float addExp = gameScore * cfg.getBaseTransRate();
		if(order.getAdvance() > 0){
			addExp = gameScore * cfg.getHighTransRate();
		}
		int orderExp = (int) addExp;
		if(orderExp > 0){
			this.addOrderExp(entity, orderExp, 4);
			this.syncActivityDataInfo(playerId);
		}
	}
	
	
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	
	
	@Override
	public boolean isActivityClose(String playerId) {
		SubmarineWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
		int cityLvl = getDataGeter().getConstructionFactoryLevel(playerId);
		if (cityLvl < cfg.getBaseLimit()) {
			return true;
		}
		return false;
	}

	@Subscribe
	public void onEvent(BuildingLevelUpEvent event) {
		// 大本升级,检测活动是否关闭
		if(event.getBuildType() == BuildingType.CONSTRUCTION_FACTORY_VALUE){
			this.syncActivityStateInfo(event.getPlayerId());
			this.syncActivityDataInfo(event.getPlayerId());
		}
	}
	
	
	
	@Override
	public void onShow() {
		this.rank = new SubmarineWarRank();
	}
	
	
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.SUBMARINE_WAR_INIT, ()-> {
				initActivityInfo(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		int termId = this.getActivityTermId();
		if(termId <= 0){
			//回收道具
	    	this.recoverItem(playerId);
		}
		this.initActivityInfo(playerId);
		this.reAddRank(playerId);
	}
	
	@Override
	public void onTick() {
		long curTime = HawkTime.getMillisecond();
		this.rank.ontick(curTime);
	}
	
	/**
	 * 初始化活动数据
	 * @param playerId
	 */
	public void initActivityInfo(String playerId){
		HawkLog.logPrintln("SubmarineWarActivity,initActivityInfo,playerId:{}", playerId);
		int termId = this.getActivityTermId();
		if(termId <= 0){
			return;
		}
		if (this.getActivityEntity().getActivityState() ==
        		com.hawk.activity.type.ActivityState.SHOW) {
			return;
		}
		//活动是否开启，没开不继续处理
		Optional<SubmarineWarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SubmarineWarEntity entity = opEntity.get();
		if(entity.getInitTime() > 0){
			return;
		}
		SubmarineWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
		long curTime = HawkTime.getMillisecond();
		//记录初始化时间
		entity.setInitTime(curTime);
		//记录登录天数
		entity.recordLoginDay();
		entity.setGameCount(cfg.getGameCountLimit());
		//游戏初始
		SubmarineWarGame game = new SubmarineWarGame();
		entity.setGame(game);
		//战令初始化
		SubmarineWarOrder order = new SubmarineWarOrder();
		HawkTuple2<Integer, Integer> lvs = this.calcLevel(order.getExpTotal());
		order.setLevel(lvs.first);
		order.setExp(lvs.second);
		entity.setOrder(order);
		//初始化任务
		this.initAchieveData(entity);
		//抛每天登录事件
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, entity.getLoginDaysCount(), this.providerActivityId()), false);
        //回收道具
        this.recoverItem(playerId);
        HawkLog.logPrintln("SubmarineWarActivity,initActivityInfo,over,playerId:{}", playerId);
        
	}
	
	public void reAddRank(String playerId){
		int termId = this.getActivityTermId();
		if(termId <= 0){
			return;
		}
		//活动是否开启，没开不继续处理
        if (!isOpening(playerId)) {
            return;
        }
		Optional<SubmarineWarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		SubmarineWarEntity entity = opEntity.get();
		if(entity.getGameScoreMax() > 0 && entity.getGameScoreMaxTime() > 0){
			PBSubmarineWarRank.Builder rank = this.rank.getCurSubmarineWarRank(playerId);
			if(rank.getRank() <= 0){
				this.rank.addPlayerScore(playerId, entity.getGameScoreMax(), (int)entity.getGameScoreMaxTime());
			}
		}
		//抛每天登录事件
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, entity.getLoginDaysCount(), this.providerActivityId()), false);
       
	}
	
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		HawkLog.logPrintln("SubmarineWarActivity,ContinueLoginEvent,playerId:{}", event.getPlayerId());
		String playerId = event.getPlayerId();
        SubmarineWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
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
        if (this.getActivityEntity().getActivityState() !=
        		com.hawk.activity.type.ActivityState.OPEN) {
			return;
		}
        Optional<SubmarineWarEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        SubmarineWarEntity entity = opEntity.get();
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
        if(cfg.getGameCountLimit() > entity.getGameCount()){
        	  entity.setGameCount(cfg.getGameCountLimit());
        }
        //重置购买次数
        entity.setBuyGameCount(0);
        //重置技能购买
        entity.clearSkillItembuyInfoMap();
        // 成就已初始化
        updateAchieveData(entity);
        //抛每天登录事件
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, entity.getLoginDaysCount(), this.providerActivityId()), false);
        //给客户端同步数据
        this.syncActivityDataInfo(playerId);
        HawkLog.logPrintln("SubmarineWarActivity ContinueLoginEvent reset playerId:{}", playerId);
	}
	
	
	
	
	/**
	 * 更新任务
	 * @param entity
	 */
	public void initAchieveData(SubmarineWarEntity entity){
		List<AchieveItem> itemList = new CopyOnWriteArrayList<>();
		//初始添加成就项
		ConfigIterator<SubmarineWarAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SubmarineWarAchieveCfg.class);
		while (configIterator.hasNext()) {
			SubmarineWarAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			itemList.add(item);
		}
		entity.setAchieveList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), entity.getAchieveList()), true);
		HawkLog.logPrintln("SubmarineWarActivity updateAchieveData playerId:{}", entity.getPlayerId(),SerializeHelper.collectionToString(itemList));
	}
	
	/**
	 * 更新任务
	 * @param entity
	 */
	public void updateAchieveData(SubmarineWarEntity entity){
		List<AchieveItem> itemList = new CopyOnWriteArrayList<>();
		Map<Integer,AchieveItem> amap = new HashMap<>();
		List<AchieveItem> list = entity.getAchieveList();
		for(AchieveItem item : list){
			SubmarineWarAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarAchieveCfg.class, item.getAchieveId());
			//配置不在
			if(Objects.isNull(cfg)){
				continue;
			}
			//留下周期更新的任务
			int rt = cfg.getRefreshDays();
			if(rt <= 0){
				amap.put(item.getAchieveId(), item);
			}
		}
		//初始添加成就项
		ConfigIterator<SubmarineWarAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SubmarineWarAchieveCfg.class);
		while (configIterator.hasNext()) {
			SubmarineWarAchieveCfg next = configIterator.next();
			int rt = next.getRefreshDays();
			//每天刷新的换新
			if(rt >= 1){
				AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
				amap.put(item.getAchieveId(), item);
			}
			
		}
		itemList.addAll(amap.values());
		entity.setAchieveList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), entity.getAchieveList()), true);
		HawkLog.logPrintln("SubmarineWarActivity updateAchieveData playerId:{}", entity.getPlayerId(),SerializeHelper.collectionToString(itemList));
	}

	
	
	/**
	 * 获取界面信息
	 * @param playerId
	 * @param hp
	 */
	public void getPageInfo(String playerId,int hp){
        this.syncActivityDataInfo(playerId);
	}
	

	/**
	 * 活动开始
	 * @param type
	 * @param hp
	 */
	public void gameStart(String playerId,int type,int skillItemId,int hp){
		//活动是否开启，没开不继续处理
        if (!isOpening(playerId)) {
            return;
        }
		Optional<SubmarineWarEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        SubmarineWarEntity entity = opEntity.get();
        SubmarineWarGame game = entity.getGame();
        if(game.getStartTime() > 0){
        	return;
        }
        //手动开始
        if(type != SubmarineWarGame.PASS_TYPE_PLAY &&
        		type != SubmarineWarGame.PASS_TYPE_FAST){
        	return;
        }
        SubmarineWarSkillItemCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarSkillItemCfg.class, skillItemId);
        if(skillItemId > 0 && Objects.isNull(skillCfg)){
        	return;
        }
        int gameCount = entity.getGameCount() -1;
        if(gameCount < 0){
        	return;
        }
        entity.setGameCount(gameCount);
        long curTime = HawkTime.getMillisecond();
        int startLevel = entity.getGameLevelMax();
        if(type == SubmarineWarGame.PASS_TYPE_PLAY){
        	game.start(curTime,skillItemId);
        }
        if(type == SubmarineWarGame.PASS_TYPE_FAST){
        	SubmarineWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
        	startLevel -=(cfg.getSweepLevel() + 1);
        	startLevel = Math.max(0, startLevel);
    		game.startFast(curTime, startLevel,skillItemId);
        }
        SubmarineWarGameStartResp.Builder resp = SubmarineWarGameStartResp.newBuilder();
        resp.setGame(game.genBuilder());
        PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.SUBMARINE_WAR_START_RESP_VALUE, resp));
        //TLOG
        int termId = this.getActivityTermId();
        this.logSubmarineWarGameStart(playerId, termId, gameCount, type, startLevel, skillItemId);
	}
	
	/**
	 * 通过关卡
	 * @param stage
	 */
	public void gameStagePass(String playerId,SubmarineWarGameStage stage){
		if (!isOpening(playerId)) {
	        return;
	    }
		Optional<SubmarineWarEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        SubmarineWarEntity entity = opEntity.get();
        SubmarineWarGame game = entity.getGame();
        if(game.getStartTime() <= 0){
        	return;
        }
        long curTime = HawkTime.getMillisecond();
        boolean addRlt = game.stagePass(playerId,stage,curTime);
        if(!addRlt){
        	HawkLog.logPrintln("SubmarineWarActivity,stagePass,addERR,playerId:{},stage:{}",
                    playerId, JsonFormat.printToString(stage));
        	return;
        }
        int termId = this.getActivityTermId();
        SubmarineWarGameStagePassResp.Builder builder = SubmarineWarGameStagePassResp.newBuilder();
        boolean over = game.gameOver();
        if(!over){
        	entity.notifyUpdate();
        	builder.setOver(false);
        	builder.setOverStage(stage.getStageId());
        	builder.setNextStage(game.getNextStage());
        	builder.setScore(stage.getScore());
        	PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.SUBMARINE_WAR_STAGE_PASS_RESP, builder));
            this.syncActivityDataInfo(playerId);
            //TLOG
            this.logSubmarineWarGamePass(playerId, termId, stage.getStageId(), stage.getScore(), stage.getUseItemCount());
            return;
        }
    	int curGameScore = game.getScore();
    	int scoreTotal = entity.getGameScore() + curGameScore;
    	int maxStage = game.getPassMax();
    	int rankTime = this.getRankTime();
    	boolean updateRecord = entity.updateMaxLevelRecord(maxStage, curGameScore, rankTime);
    	if(updateRecord){
        	 this.rank.addPlayerScore(playerId, game.getScore(), rankTime);
        }
    	//添加战令经验
    	this.addOrderExpOnGameOver(curGameScore, entity);
    	entity.setGameScore(scoreTotal);
    	entity.notifyUpdate();
    	//回消息，发奖励
    	SubmarineWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
    	PBSubmarineWarRank.Builder rank = this.rank.getCurSubmarineWarRank(playerId);
    	builder.setOver(true);
    	builder.setWin(game.getPass());
    	builder.setOverStage(game.getFinalStage());
        builder.setScore(game.getScore());
        builder.setRank(rank.getRank());
    	RewardItem.Builder item = cfg.getScoreItem(game.getScore());
    	List<RewardItem.Builder> list = new ArrayList<>();
    	list.add(item);
    	ActivityManager.getInstance().getDataGeter().takeReward(playerId, list, 1, Action.SUBMARINE_WAR_GAME_OVER_REWARD, false,RewardOrginType.ACTIVITY_REWARD);
        PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.SUBMARINE_WAR_STAGE_PASS_RESP, builder));
        ActivityManager.getInstance().postEvent(new SubmarineWarScoreEvent(playerId, scoreTotal,curGameScore,game.getPassMax(),game.getMonsterKill()), true);
        //TLOG
        this.logSubmarineWarGameOver(playerId, termId, curGameScore, game.getFinalStage(), rank.getRank(), scoreTotal, game.getPass());
        this.syncActivityDataInfo(playerId);
	}
	
	

	
	/**
	 * 购买技能道具
	 * @param playerId
	 * @param count
	 * @param hp
	 */
	public void skillItemBuy(String playerId, int itemId,int count,int hp){
		//活动是否开启，没开不继续处理
        if (!isOpening(playerId)) {
        	return;
        }
		SubmarineWarSkillItemCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarSkillItemCfg.class, itemId);
		if(Objects.isNull(cfg)){
			return;
		}
        Optional<SubmarineWarEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        SubmarineWarEntity entity = opDataEntity.get();
        int eCount = entity.getSkillItemBuyCount(itemId);
        if (eCount + count > cfg.getBuyLimit()) {
            //错误码
            HawkLog.logPrintln("SubmarineWarActivity,itemExchange,fail,countless,playerId: " + "{},exchangeType:{},ecount:{}",
                    playerId, itemId, eCount);
            return;
        }
        
        //消耗兑换道具
        List<RewardItem.Builder> costItems = cfg.getBuyCostItem(count);
        boolean success = getDataGeter().consumeItems(playerId, costItems,hp, 
        		Action.SUBMARINE_WAR_SKILL_ITEM_BUY_COST);
		if (!success) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hp, 
					Status.Error.ITEM_NOT_ENOUGH_VALUE);
		    return;
		}
		//记录购买
		entity.addSkillItemBuyCount(itemId, count);
        //发奖励
		List<RewardItem.Builder> gianList = cfg.getGainItem(count);
        this.getDataGeter().takeReward(playerId, gianList, 1, Action.SUBMARINE_WAR_SKILL_ITEM_BUY_GET, true);
        PlayerPushHelper.getInstance().responseSuccess(playerId, hp);
        this.syncActivityDataInfo(playerId);
        //TLOG
        int termId = this.getActivityTermId();
        this.logSubmarineWarShopBuy(playerId, termId, 2, itemId, eCount);
        HawkLog.logPrintln("SubmarineWarActivity,itemBuy,sucess,playerId:{},count:{}", playerId, count);
	}
	
	
	
    /**
     * 商店物品兑换
     *
     * @param playerId
     * @param exchangeId
     * @param exchangeCount
     */
    public void itemExchange(String playerId, int exchangeId, int exchangeCount,int hp) {
    	SubmarineWarShopCfg exchangeConfig = HawkConfigManager.getInstance().
                getConfigByKey(SubmarineWarShopCfg.class, exchangeId);
        if (Objects.isNull(exchangeConfig)) {
            return;
        }
        
        Optional<SubmarineWarEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        SubmarineWarEntity entity = opDataEntity.get();
        int eCount = entity.getShopBuyCount(exchangeId);
        if (eCount + exchangeCount > exchangeConfig.getTimes()) {
            //错误码
            HawkLog.logPrintln("SubmarineWarActivity,itemExchange,fail,countless,playerId: " + "{},exchangeType:{},ecount:{}",
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
        		Action.SUBMARINE_WAR_SHOP_ITEM_BUY_COST);
		if (!success) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hp, 
					Status.Error.ITEM_NOT_ENOUGH_VALUE);
		    return;    
		}
        //增加兑换次数
        entity.addShopBuyCount(exchangeId, exchangeCount);
        //发奖励
        this.getDataGeter().takeReward(playerId, exchangeConfig.getGainItemList(), exchangeCount, Action.SUBMARINE_WAR_SHOP_ITEM_BUY_GET, true);
        //同步
        this.syncActivityDataInfo(playerId);
        //TLOG
        int termId = this.getActivityTermId();
        this.logSubmarineWarShopBuy(playerId, termId, 2, exchangeId, eCount);
        HawkLog.logPrintln("SubmarineWarActivity,itemExchange,sucess,playerId:{},exchangeType:{},ecount:{}", playerId, exchangeId, eCount);
    }
	
    
    /**
     * 购买游戏次数
     * @param playerId
     * @param count
     * @param hp
     */
    public void gameCountBuy(String playerId, int count,int hp) {
    	//活动是否开启，没开不继续处理
        if (!isOpening(playerId)) {
        	return;
        }
    	SubmarineWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
    	Optional<SubmarineWarEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        SubmarineWarEntity entity = opDataEntity.get();
        int curCount = entity.getBuyGameCount();
        int aftCount = curCount + 1;
        if (curCount >= cfg.getBuyGameTimes()) {
            //错误码
            HawkLog.logPrintln("SubmarineWarActivity,gameCountBuy,fail,countless,playerId: " + "{},curCount:{},ecount:{}",
                    playerId,  curCount);
            return;
        }
        //消耗兑换道具
        List<RewardItem.Builder> costItems = cfg.getBuyGameCountCost(aftCount);
        boolean success = getDataGeter().consumeItems(playerId, costItems,hp, 
        		Action.SUBMARINE_WAR_GAME_COUNT_BUY_COST);
		if (!success) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hp, 
					Status.Error.ITEM_NOT_ENOUGH_VALUE);
		    return;
		}
		entity.setGameCount(entity.getGameCount() + count);
		entity.setBuyGameCount(entity.getBuyGameCount() + count);
        //发奖励
        PlayerPushHelper.getInstance().responseSuccess(playerId, hp);
        this.syncActivityDataInfo(playerId);
        //Tlog
        int termId = this.getActivityTermId();
        this.logSubmarineWarGameCountBuy(playerId, termId, count, aftCount);
        HawkLog.logPrintln("SubmarineWarActivity,gameCountBuy,sucess,playerId:{},count:{},curCount:{}", playerId, count,entity.getGameCount());
    }
    
    
    
    
    /**
	 * 战令领奖
	 */
	public Result<?> orderReward(String playerId,int level){
		if (isHidden(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<SubmarineWarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		SubmarineWarEntity dataEntity = opEntity.get();
		SubmarineWarOrder orderData = dataEntity.getOrder();
		if(Objects.isNull(orderData)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		boolean canRewardNormal = orderData.canRewardNormal(level);
		boolean canRewardAdvance = orderData.canRewardAdvance(level);
		if(!canRewardNormal && !canRewardAdvance){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		long curTime = HawkTime.getMillisecond();
		SubmarineWarOrderLevelCfg levelCfg =  HawkConfigManager.getInstance().getConfigByKey(SubmarineWarOrderLevelCfg.class, level);
		//正常奖励
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		if(canRewardNormal){
			orderData.addAchieveRewardNormal(level, curTime);
			rewardList.addAll(levelCfg.getNormalRewardList());
			this.logSubmarineWarOrderReward(playerId, dataEntity.getTermId(), level, 0);
		}
		if(canRewardAdvance){
			orderData.addAchieveRewardAdvance(level, curTime);
			rewardList.addAll(levelCfg.getAdvRewardList());
			this.logSubmarineWarOrderReward(playerId, dataEntity.getTermId(), level, 1);
		}
		dataEntity.notifyUpdate();
		//发奖
		this.getDataGeter().takeReward(playerId, rewardList, 1, Action.SUBMARINE_WAR_ORDER_ACHIEVE, true, RewardOrginType.ACTIVITY_REWARD);
		this.syncActivityDataInfo(playerId);
		return Result.success();
		
	}
    
    /**
	 * 一键领奖
	 */
	public Result<?> orderRewardAll(String playerId){
		if (isHidden(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<SubmarineWarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		SubmarineWarEntity dataEntity = opEntity.get();
		SubmarineWarOrder orderData = dataEntity.getOrder();
		if(Objects.isNull(orderData)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		//正常奖励
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		int level = orderData.getLevel();
		for(int i=1;i<= level;i++){
			boolean canRewardNormal = orderData.canRewardNormal(i);
			boolean canRewardAdvance = orderData.canRewardAdvance(i);
			if(!canRewardNormal && !canRewardAdvance){
				continue;
			}
			long curTime = HawkTime.getMillisecond();
			SubmarineWarOrderLevelCfg levelCfg =  HawkConfigManager.getInstance().getConfigByKey(SubmarineWarOrderLevelCfg.class, i);
			if(canRewardNormal){
				orderData.addAchieveRewardNormal(i, curTime);
				rewardList.addAll(levelCfg.getNormalRewardList());
				this.logSubmarineWarOrderReward(playerId, dataEntity.getTermId(), level, 0);
			}
			if(canRewardAdvance){
				orderData.addAchieveRewardAdvance(i, curTime);
				rewardList.addAll(levelCfg.getAdvRewardList());
				this.logSubmarineWarOrderReward(playerId, dataEntity.getTermId(), level, 1);
			}
		}
		dataEntity.notifyUpdate();
		//发奖
		this.getDataGeter().takeReward(playerId, rewardList, 1, Action.SUBMARINE_WAR_ORDER_ACHIEVE, true, RewardOrginType.ACTIVITY_REWARD);
		this.syncActivityDataInfo(playerId);
		return Result.success();
	}
	
	
	/**
	 * 购买战令等级
	 * @param playerId
	 * @param toLevel
	 * @return
	 */
	public Result<?> buyAuthLvlMultiple(String playerId, int toLevel) {
		//活动是否开启，没开不继续处理
        if (!isOpening(playerId)) {
        	return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
		Optional<SubmarineWarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		SubmarineWarEntity dataEntity = opEntity.get();
		SubmarineWarOrder orderData = dataEntity.getOrder();
		if(Objects.isNull(orderData)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		SubmarineWarKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
		if (orderData.getLevel() >= toLevel) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		
		int unlockLevel = toLevel - orderData.getLevel();
		if(orderData.getBuyExpCount() + unlockLevel > kvCfg.getGoldTimeLimit()){
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		int curLevel = orderData.getLevel();
		for(int i= curLevel+1; i<= toLevel; i++){
			SubmarineWarOrderLevelCfg lvlCfg = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarOrderLevelCfg.class, i);
			if (lvlCfg == null) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
		}
		SubmarineWarOrderLevelCfg toLvlCfg = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarOrderLevelCfg.class, toLevel);
		int addExp = toLvlCfg.getLevelUpExp() - orderData.getExpTotal();
		if(addExp <= 0){
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		
		RewardItem.Builder price = RewardHelper.toRewardItem(kvCfg.getGoldPrice());
		boolean flag = getDataGeter().cost(playerId, Arrays.asList(price), 1, Action.SUBMARINE_WAR_ORDER_LVL_BUY, true);
		if (!flag) {
			return Result.fail(Status.Error.DIAMONDS_NOT_ENOUGH_VALUE);
		}
		// 加经验
		addOrderExp(dataEntity, addExp, 2);
		dataEntity.notifyUpdate();
		// 流水记录
		HawkLog.logPrintln("SubmarineWarActivity buyOrderExp success, playerId: {}, currLvl: {}, addExp: {}, level: {}", playerId, curLevel, addExp, orderData.getLevel());
		this.syncActivityDataInfo(playerId);
		return Result.success();
	}
    
	
	/**
	 * 游戏结束添加战令经验
	 * @param score
	 * @param dataEntity
	 */
	public void addOrderExpOnGameOver(int score,SubmarineWarEntity dataEntity){
		SubmarineWarKVCfg config = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
		SubmarineWarOrder order = dataEntity.getOrder();
		if(Objects.isNull(order)){
			return;
		}
		float addExp = score * config.getBaseTransRate();
		if(order.getAdvance() > 0){
			addExp = score * config.getHighTransRate();
		}
		this.addOrderExp(dataEntity, (int)addExp, 1);
		
	}
	/**
	 * 增加战令经验
	 * @param dataEntity
	 * @param addExp
	 */
	public void addOrderExp(SubmarineWarEntity dataEntity, int addExp, int reason) {
		String playerId = dataEntity.getPlayerId();
		if (addExp < 0) {
			return;
		}
		SubmarineWarOrder orderData = dataEntity.getOrder();
		int oldLvl = orderData.getLevel();
		int oldExp = orderData.getExp();
		int oldExpTotal = orderData.getExpTotal();
		int newExpTotal = oldExpTotal + addExp;
		HawkTuple2<Integer, Integer> levels = calcLevel(newExpTotal);
		orderData.setExpTotal(newExpTotal);
		orderData.setLevel(levels.first);
		orderData.setExp(levels.second);
		dataEntity.notifyUpdate();
		// 流水记录
		this.logSubmarineWarOrderExpAdd(playerId, dataEntity.getTermId(), addExp,oldExpTotal, newExpTotal, oldLvl, oldExp, levels.first, levels.second,reason);
		HawkLog.logPrintln("SubmarineWarActivity  expAdd, playerId: {}, expBef: {}, expAft: {}, lvlBef: {}, lvlAft: {}, reason: {}",
				playerId, oldExpTotal, newExpTotal, oldLvl, levels.first, reason);
	}
	
	
	/**
	 * 计算等级
	 * @param exp
	 * @return
	 */
	private HawkTuple2<Integer, Integer> calcLevel(int expTotal) {
		int level = 0;
		int exp = 0;
		SubmarineWarOrderLevelCfg curCfg = null;
		ConfigIterator<SubmarineWarOrderLevelCfg> its = HawkConfigManager.getInstance().getConfigIterator(SubmarineWarOrderLevelCfg.class);
		for (SubmarineWarOrderLevelCfg cfg : its) {
			if (expTotal < cfg.getLevelUpExp()) {
				continue;
			}
			if(Objects.isNull(curCfg)){
				curCfg = cfg;
				continue;
			}
			if(cfg.getLevel() > curCfg.getLevel()){
				curCfg = cfg;
			}
		}
		if(Objects.nonNull(curCfg)){
			level = curCfg.getLevel();
			exp = expTotal - curCfg.getLevelUpExp();
		}
		return HawkTuples.tuple(level, exp);
	}


    /**
     * 同步活动信息
     */
	@Override
	public void syncActivityDataInfo(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<SubmarineWarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return ;
		}
		SubmarineWarEntity entity = opEntity.get();
        SubmarineWarGame game = entity.getGame();
        game.checkRest(playerId);
		try {
			SubmarineWarInfoSync.Builder builder  = genSubmarineWarInfo(entity);
			PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.SUBMARINE_WAR_INFO_RESP, builder));
		} catch (Exception e) {
			 HawkException.catchException(e);
		}
		
	}
	
	/**
	 * 玩家迁出，清除排行榜数据
	 */
	@Override
	public void onPlayerMigrate(String playerId) {
		this.rank.delPlayerScore(playerId);
	}
	
	@Override
	public void removePlayerRank(String playerId) {
		this.rank.delPlayerScore(playerId);
	}
	
	/**
	 * 生成页面
	 * @param entity
	 * @return
	 */
	public SubmarineWarInfoSync.Builder genSubmarineWarInfo(SubmarineWarEntity entity){
		SubmarineWarInfoSync.Builder builder = SubmarineWarInfoSync.newBuilder();
		//游戏数据
		SubmarineWarGame game = entity.getGame();
		if(game.getStartTime() > 0){
			SubmarineWarGameData.Builder gbuilder = game.genBuilder();
			builder.setCurGame(gbuilder);
		}
		//商店数据
        for(Map.Entry<Integer, Integer> shop : entity.getShopBuyInfoMap().entrySet()){
        	SubmarineWarShopData.Builder sbuilder = SubmarineWarShopData.newBuilder();
        	sbuilder.setShopId(shop.getKey());
        	sbuilder.setCount(shop.getValue());
        	builder.addExchangeShopDatas(sbuilder);
        }
        //技能道具
        for(Map.Entry<Integer, Integer> shop : entity.getSkillItembuyInfoMap().entrySet()){
        	SubmarineWarShopData.Builder sbuilder = SubmarineWarShopData.newBuilder();
        	sbuilder.setShopId(shop.getKey());
        	sbuilder.setCount(shop.getValue());
        	builder.addSkillShopDatas(sbuilder);
        }
        //关注
        builder.addAllTips(getTips(SubmarineWarShopCfg.class, entity.getTipSet()));
        //最大通过关卡
        builder.setPassLevel(entity.getGameLevelMax());
        //总分
        builder.setScoreTotal(entity.getGameScore());
        //剩余游戏次数
        builder.setGameCount(entity.getGameCount());
        //购买次数
        builder.setBuyCount(entity.getBuyGameCount());
        //最大通关数
        builder.setGameMaxLevel(entity.getGameLevelMax());
        //最大积分
        builder.setGameMaxScore(entity.getGameScoreMax());
        //服务器分组
        SubmarineWarRankGroup group = this.rank.getServerGroup();
        if(Objects.nonNull(group)){
        	builder.addAllServers(group.getServers());
        }
        //战令
        SubmarineWarOrder order = entity.getOrder();
        if(Objects.nonNull(order)){
        	builder.setOrderData(order.genBuilder());
        }
		return builder;
	}
	
	
    /**
     * 获取排行榜信息
     * @param playerId
     */
    public void getRankInfo(String playerId){
    	SubmarineWarKVCfg config = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
    	SubmarineWarRankResp.Builder builder = SubmarineWarRankResp.newBuilder();
    	builder.setSelf(this.getCurRank(playerId));
    	List<PBSubmarineWarRank> personList = this.rank.getShowRankList();
		int rankSize = personList.size()-1;
		int showSize = config.getRankSize();
		for(int i=0;i<showSize;i++){
			if(i>rankSize){
				break;
			}
			builder.addRanks(personList.get(i));
		}
    	pushToPlayer(playerId, HP.code2.SUBMARINE_WAR_RANK_RESP_VALUE, builder);
    }
    
    private PBSubmarineWarRank getCurRank(String playerId){
    	PBSubmarineWarRank.Builder sbuilder = this.rank.getCurSubmarineWarRank(playerId);
		return sbuilder.build();
	}
 
	
    /**
     * 弹幕
     * @param playerId
     */
    public void getBarrageInfo(String playerId){
    	SubmarineWarKVCfg config = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
    	SubmarineWarBarrageResp.Builder builder = SubmarineWarBarrageResp.newBuilder();
    	int barrageSize = config.getBarrageSize();
    	List<PBSubmarineWarRank> personList = this.rank.getShowRankList();
    	int rankSize = personList.size()-1;
		for(int i=0;i<barrageSize;i++){
			if(i>rankSize){
				break;
			}
			builder.addBarrages(personList.get(i));
		}
    	pushToPlayer(playerId, HP.code2.SUBMARINE_WAR_BARRAGE_RESP_VALUE, builder);
    }
	
	


	public int getRankTime(){
		int termId = this.getActivityTermId();
		long startTime = this.getTimeControl().getStartTimeByTermId(termId);
		long curTime = HawkTime.getMillisecond();
		int time = (int) ((curTime - startTime)/1000);
		return Math.max(0, time);
	}

	
    /**
     * 回收道具
     * @param playerId
     */
    public void recoverItem(String playerId){
    	SubmarineWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
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
        		boolean cost = this.getDataGeter().cost(playerId,costList, 1, Action.SUBMARINE_WAR_RECOVER_COST, true);
        		//扣除失败不继续处理
        		if (!cost) {
        			continue;
        		}
        		HawkLog.logPrintln("SubmarineWarActivity,recoverItem,cost,playerId{},itemId:{},count:{}", playerId,costBuilder.getItemId(),count);
        		if(!HawkOSOperator.isEmptyString(gainItem)){
        			RewardItem.Builder gainItemBuilder = RewardHelper.toRewardItem(gainItem);
        			long gcount = gainItemBuilder.getItemCount() * count;
        			gainItemBuilder.setItemCount(gcount);
        			List<RewardItem.Builder> gainList = new ArrayList<>();
        			gainList.add(gainItemBuilder);
        			Object[] content =  new Object[]{count};
        			Object[] title =  new Object[]{count};
        			Object[] subtitle =  new Object[]{count};
    				this.getDataGeter().sendMail(playerId, MailId.SUBMARINE_WAR_RECOVER_COST, title, subtitle, content,
    						gainList, false);
    				HawkLog.logPrintln("SubmarineWarActivity,recoverItem,gain,playerId{},itemId:{},count:{}", playerId,gainItemBuilder.getItemId(),gainItemBuilder.getItemCount());
        		}
			} catch (Exception e) {
				continue;
			}
    	}
	}
    
    
  /**
   * 直购礼包
   * @param event
   */
    @Subscribe
	public void onGiftBuyEvent(PayGiftBuyEvent event) {
	  	String giftId = event.getGiftId();
	  	int configId = SubmarineWarPassAuthorityCfg.getGiftId(giftId);
	  	SubmarineWarPassAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarPassAuthorityCfg.class, configId);
	  	if(Objects.isNull(cfg)){
	  		return;
	  	}
	  	Optional<SubmarineWarEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
	    if (!opEntity.isPresent()) {
	    	return;
	    }
	    SubmarineWarEntity entity = opEntity.get();
	    SubmarineWarOrder order = entity.getOrder();
		if (order == null) {
			return;
		}
		order.setAdvance(configId);
		// 增加经验
		addOrderExp(entity, cfg.getExp(), 3);
		this.syncActivityDataInfo(event.getPlayerId());
  }
    
	
	/**检查是否可以购买礼包
	 * @param playerId
	 * @param payforId
	 * @return
	 */
	public boolean canPayforGift(String playerId, String payforId) {
		if(!this.isOpening(playerId)){
			return false;
		}
		//是否是活动期内
		if (this.getActivityEntity().getState() 
				!= ActivityState.OPEN.intValue()) {
			return false;
		}
		Optional<SubmarineWarEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		int giftId = SubmarineWarPassAuthorityCfg.getGiftId(payforId);
		SubmarineWarPassAuthorityCfg authorityCfg = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarPassAuthorityCfg.class, giftId);
		if (authorityCfg == null) {
			return false;
		}
		SubmarineWarEntity entity = opEntity.get();
		SubmarineWarOrder order = entity.getOrder();
		if (order == null) {
			return false;
		}
		int advanceId = order.getAdvance();
		if(advanceId > 0){
			return false;
		}
		return true;
	}
    

   
    
    /**
     *  <!-- 潜艇大战游戏开始 -->
     * @param playerId
     * @param termId
     * @param gameCount 游戏次数
     * @param startType 开始方式
     * @param startLevel 开始关卡
     * @param skillItem 技能道具ID
     */
    private void logSubmarineWarGameStart(String playerId, int termId, int gameCount, int startType,int startLevel,int skillItem){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("gameCount", gameCount);
        param.put("startType", startType);
    	param.put("startLevel", startLevel);
        param.put("skillItem", skillItem);
        getDataGeter().logActivityCommon(playerId, LogInfoType.submarine_war_game_start, param);
    }
    
    
    /**
     * <!-- 潜艇大战游戏过关 -->
     * @param playerId
     * @param termId
     * @param stageId 关卡ID
     * @param stageScore 关卡积分
     * @param skillCount 技能使用次数
     */
    private void logSubmarineWarGamePass(String playerId, int termId, int stageId, int stageScore, int skillCount){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("stageId", stageId);
        param.put("stageScore", stageScore);
    	param.put("skillCount", skillCount);
        getDataGeter().logActivityCommon(playerId, LogInfoType.submarine_war_game_pass, param);
    }

   
    
    /**
     *  <!-- 潜艇大战游戏结束 -->
     * @param playerId
     * @param termId
     * @param gameScore 游戏积分
     * @param overStage 结束关卡
     * @param rankIndex 当前排行
     * @param totalScore 当前总积分
     * @param winRlt 胜利结果 1 胜利  2失败
     */
    private void logSubmarineWarGameOver(String playerId, int termId, int gameScore, int overStage, int rankIndex, int totalScore, int winRlt){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("gameScore", gameScore);
        param.put("overStage", overStage);
    	param.put("rankIndex", rankIndex);
    	param.put("totalScore", totalScore);
    	param.put("winRlt", winRlt);
        getDataGeter().logActivityCommon(playerId, LogInfoType.submarine_war_game_over, param);
    }
    
    
    /**
     * <!-- 潜艇大战商店购买 -->
     * @param playerId
     * @param termId
     * @param shopType 商店类型 1兑换商店  2技能道具商店
     * @param shopId 商品ID
     * @param count 个数
     */
    private void logSubmarineWarShopBuy(String playerId, int termId,int shopType,int shopId,int count){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("shopType", shopType);
        param.put("shopId", shopId);
    	param.put("count", count);
        getDataGeter().logActivityCommon(playerId, LogInfoType.submarine_war_shop_buy, param);
    }
    
    
    /**
     * <!-- 潜艇大战游戏次数购买 -->
     * @param playerId
     * @param termId
     * @param buyCount  购买次数
     * @param aftCount  当前次数
     */
    private void logSubmarineWarGameCountBuy(String playerId, int termId,int buyCount,int aftCount){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("buyCount", buyCount);
        param.put("aftCount", aftCount);
        getDataGeter().logActivityCommon(playerId, LogInfoType.submarine_war_game_count_buy, param);
    }
    
    /**
     * 战令经验增加
     * @param playerId
     * @param termId 期数
     * @param addExp 经验增加数量
     * @param expBef 经验增加前数量
     * @param expAft 经验增加后数量
     * @param lvBef  增加前等级
     * @param lvexpBef 增加前等级经验
     * @param lvAft  增加后等级
     * @param lvexpAft 增加后等级经验
     * @param reason  原因
     */
    private void logSubmarineWarOrderExpAdd(String playerId, int termId,int addExp,int expBef,int expAft,int lvBef,int lvexpBef,int lvAft,int lvexpAft,int reason){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
    	param.put("addExp", addExp);
        param.put("expBef", expBef);
        param.put("expAft", expAft);
        param.put("lvBef", lvBef);
        param.put("lvexpBef", lvexpBef);
        param.put("lvAft", lvAft);
        param.put("lvexpAft", lvexpAft);
        param.put("reason", reason);
        getDataGeter().logActivityCommon(playerId, LogInfoType.submarine_war_order_exp_add, param);
    }
    
    
    /**
     * 战令等级奖励
     * @param playerId
     * @param termId 期数
     * @param level 等级
     * @param advance 是否进阶
     */
    private void logSubmarineWarOrderReward(String playerId, int termId,int level,int advance){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("level", level);
        param.put("advance", advance);
        getDataGeter().logActivityCommon(playerId, LogInfoType.submarine_war_order_reward, param);
    }
    
    
}
