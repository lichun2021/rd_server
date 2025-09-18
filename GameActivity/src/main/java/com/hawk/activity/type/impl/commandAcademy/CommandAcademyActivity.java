package com.hawk.activity.type.impl.commandAcademy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.CommandAcademyGroupGiftEvent;
import com.hawk.activity.event.impl.EnergyCountEvent;
import com.hawk.activity.event.impl.EquipQualityAchiveEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.commandAcademy.CommandAcademyConst.RankType;
import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyAchieveCfg;
import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyBoxAchieveCfg;
import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyGiftCfg;
import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyKVCfg;
import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyRankScoreCfg;
import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyStageCfg;
import com.hawk.activity.type.impl.commandAcademy.entity.CommandAcademyEntity;
import com.hawk.activity.type.impl.commandAcademy.rank.CommandAcademyRank;
import com.hawk.activity.type.impl.commandAcademy.rank.CommandAcademyRankMember;
import com.hawk.activity.type.impl.commandAcademy.rank.FinalRankComparator;
import com.hawk.activity.type.impl.commandAcademy.rank.FinalRankMember;
import com.hawk.activity.type.impl.commandAcademy.rank.StageConfigComparator;
import com.hawk.game.protocol.Activity.CommandCollegeBuyPackageResp;
import com.hawk.game.protocol.Activity.CommandCollegeFinalRankResp;
import com.hawk.game.protocol.Activity.CommandCollegeInfoResp;
import com.hawk.game.protocol.Activity.CommandCollegePackageResp;
import com.hawk.game.protocol.Activity.CommandCollegeRankMember;
import com.hawk.game.protocol.Activity.CommandCollegeStageRank;
import com.hawk.game.protocol.Activity.CommandCollegeStageRankResp;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.gamelib.player.PowerChangeData;
import com.hawk.gamelib.player.PowerData;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;

import redis.clients.jedis.Tuple;
/**
 * 指挥官学院活动
 * @author che
 *
 */
public class CommandAcademyActivity extends ActivityBase implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	/** 排行列表*/
	private Map<Integer,CommandAcademyRank> ranks = new ConcurrentHashMap<Integer, CommandAcademyRank>();
	
	/** 当前阶段*/
	private int stageId;
	
	/** 团购人数缓存*/
	private Map<Integer,Integer> giftBuyCount = new ConcurrentHashMap<Integer,Integer>();
	
	/** 团购人数注水*/
	private Map<Integer,Integer> giftBuyCountAssist = new ConcurrentHashMap<Integer, Integer>();
	
	/** 上次注水时间*/
	private long giftBuyAssistUpdateTime;
	
	/** 阶段榜更新时间*/
	private long stageRankUpdateTime;
	
	/** 能源总量刷新事件更新时间*/
	private long energyEventUpdateTime;
	
	/** 团购礼包购买总量刷新时间*/
	private long giftBuyCountUpdateTime;
	
	
	//程序修正时间  redisKey 问题
	private final long fixTime = HawkTime.parseTime("2020-1-10 00:00:00");
	private int lastEventBuyCount;
	
	public CommandAcademyActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.COMMAND_ACADEMY_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<CommandAcademyEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			int curStage = this.getCommandAcademyStage();
			// 刷成就
			CommandAcademyEntity entity = opDataEntity.get();
			if (entity.getStage() != curStage) {
				initAchieveInfo(playerId);
			}
			//推动宝箱
			CommandAcademyGroupGiftEvent event = new 
					CommandAcademyGroupGiftEvent(playerId, this.getBuyCount(curStage),entity.isBuyGift());
			ActivityManager.getInstance().postEvent(event);
		}
	}

	@Override
	public void onShow() {
		String key = this.getSendAwardStageKey();
		if(!HawkOSOperator.isEmptyString(key)){
			ActivityLocalRedis.getInstance().del(key);
		}
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.COMMAND_ACADEMY_INIT, () -> {
				initAchieveInfo(playerId);
				this.syncActivityDataInfo(playerId);
			});
		}
	}
	
	@Override
	public void onEnd() {
		
		
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<CommandAcademyEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			this.initAchieveInfo(playerId);
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		CommandAcademyActivity activity = new CommandAcademyActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<CommandAcademyEntity> queryList = HawkDBManager.getInstance()
				.query("from CommandAcademyEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			CommandAcademyEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		CommandAcademyEntity entity = new CommandAcademyEntity(playerId, termId);
		return entity;
	}
	

	
	@Subscribe
	public void onBuidingLevelUpEvent(BuildingLevelUpEvent event) {
		if(isActivityClose(event.getPlayerId())){
			return;
		}
		//不是主城不管
		if(event.getBuildType() != BuildingType.CONSTRUCTION_FACTORY_VALUE){
			return;
		}
		int curStage = this.getCommandAcademyStage();
		
		CommandAcademyStageCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, curStage);
		if(cfg == null){
			return;
		}
		CommandAcademyConst.RankType rankType = CommandAcademyConst.RankType.
				getRankType(cfg.getFuncType());
		if(rankType == null){
			return;
		}
		if(rankType != CommandAcademyConst.RankType.BUILDING){
			return;
		}
		if(event.getLevel() <= 1){
			return;
		}
		String playerId = event.getPlayerId();
		Optional<CommandAcademyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		CommandAcademyEntity entity = opEntity.get();
		int maxRecored = entity.getStageParam(curStage);
		if(maxRecored < event.getLevel()){
			CommandAcademyRank rank = this.getRankObject(rankType.intValue());
			if(rank != null){
				entity.setStageParam(curStage, event.getLevel());
				CommandAcademyRankMember member = new CommandAcademyRankMember(playerId, 
						RankScoreHelper.calcSpecialRankScore(event.getLevel()));
				rank.insertRank(member);
				logger.info("CommandAcademyActivity rank update "+curStage+","+playerId+","+event.getLevel());
			}
		}
		
		
	}
	
	
	public void upateEneryCount(){
		long time = HawkTime.getMillisecond();
		if(time - this.energyEventUpdateTime < HawkTime.MINUTE_MILLI_SECONDS){
			return;
		}
		int curStage = this.getCommandAcademyStage();
		this.energyEventUpdateTime = time;
		CommandAcademyStageCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, curStage);
		if(cfg == null){
			return;
		}
		CommandAcademyConst.RankType rankType = CommandAcademyConst.RankType.
				getRankType(cfg.getFuncType());
		if(rankType == null){
			return;
		}
		if(rankType != CommandAcademyConst.RankType.ENERGY){
			return;
		}
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			int eCount = this.getDataGeter().getEnergyCount(playerId);
			EnergyCountEvent event = new EnergyCountEvent(playerId, eCount);
			ActivityManager.getInstance().postEvent(event);
		}
		
	}
	
	@Subscribe
	public void onEnergyCountEvent(EnergyCountEvent event) {
		if(isActivityClose(event.getPlayerId())){
			return;
		}
		//S装备榜 计 算增量
		int curStage = this.getCommandAcademyStage();
		CommandAcademyStageCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, curStage);
		if(cfg == null){
			return;
		}
		CommandAcademyConst.RankType rankType = CommandAcademyConst.RankType.
				getRankType(cfg.getFuncType());
		if(rankType == null){
			return;
		}
		if(rankType != CommandAcademyConst.RankType.ENERGY){
			return;
		}
		
		String playerId = event.getPlayerId();
		Optional<CommandAcademyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		CommandAcademyEntity entity = opEntity.get();
		int maxRecored = entity.getStageParam(curStage);
		if(maxRecored < event.getEnergy()){
			CommandAcademyRank rank = this.getRankObject(rankType.intValue());
			if(rank != null){
				entity.setStageParam(curStage, event.getEnergy());
				CommandAcademyRankMember member = new CommandAcademyRankMember(playerId, 
						RankScoreHelper.calcSpecialRankScore(event.getEnergy()));
				rank.insertRank(member);
				logger.info("CommandAcademyActivity rank update "+curStage+","+playerId+","+  event.getEnergy());
			}
		}
		
	}
	
	
	@Subscribe
	public void onEquipAchiveEvent(EquipQualityAchiveEvent event) {
		if(isActivityClose(event.getPlayerId())){
			return;
		}
		//S装备榜 计 算增量
		int curStage = this.getCommandAcademyStage();
		CommandAcademyStageCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, curStage);
		if(cfg == null){
			return;
		}
		CommandAcademyConst.RankType rankType = CommandAcademyConst.RankType.
				getRankType(cfg.getFuncType());
		if(rankType == null){
			return;
		}
		if(rankType != CommandAcademyConst.RankType.SEQUIP){
			return;
		}
		int achieveCount = event.equipQualityCount(3);
		if(achieveCount <=0){
			return;
		}
		String playerId = event.getPlayerId();
		Optional<CommandAcademyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		CommandAcademyEntity entity = opEntity.get();
		int count = entity.getStageParam(curStage);
		count += achieveCount;
		CommandAcademyRank rank = this.getRankObject(rankType.intValue());
		if(rank != null){
			entity.setStageParam(curStage, count);
			CommandAcademyRankMember member = new CommandAcademyRankMember(playerId,
					RankScoreHelper.calcSpecialRankScore(count) );
			rank.insertRank(member);
			logger.info("CommandAcademyActivity rank update "+curStage+","+playerId+","+ 1);
		}
	}
	
	@Subscribe
	public void onBattlePointChangeEvent(BattlePointChangeEvent event) {
		if(isActivityClose(event.getPlayerId())){
			return;
		}		
		String playerId = event.getPlayerId();
		PowerData data = event.getPowerData();
		PowerChangeData changeData = event.getChangeData();
		Optional<CommandAcademyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		int curStage = this.getCommandAcademyStage();
		CommandAcademyStageCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, curStage);
		if(cfg == null){
			return;
		}
		CommandAcademyConst.RankType rankType = CommandAcademyConst.RankType.
				getRankType(cfg.getFuncType());
		if(rankType == null){
			return;
		}
		CommandAcademyEntity entity = opEntity.get();
		int maxRecored = entity.getStageParam(curStage);
		boolean isUpdateRank = false;
		switch (rankType) {
		case TECHNOLOGY:
			if(data.getTechBattlePoint() > maxRecored){
				maxRecored = data.getTechBattlePoint();
				isUpdateRank = true;
			}
			break;
		case HERO:
			if(data.getHeroBattlePoint() > maxRecored){
				maxRecored = data.getHeroBattlePoint();
				isUpdateRank = true;
			}
			break;
		case ARMOR:
			if(data.getSuperSoldierBattlePoint() > maxRecored){
				maxRecored = data.getSuperSoldierBattlePoint();
				isUpdateRank = true;
			}
			break;
		case ARMY:
			PowerChangeReason reason = event.getReason();
			int armyAdd = changeData.getArmyBattleChange();
			// 除了初始化/奖励/训练/晋升士兵外,其他来源的士兵战力变化不计入分数
			if (armyAdd > 0 && (reason == PowerChangeReason.INIT_SOLDIER || 
					reason == PowerChangeReason.AWARD_SOLDIER || 
						reason == PowerChangeReason.TRAIN_SOLDIER)) {
				maxRecored += armyAdd;
				isUpdateRank = true;
			}
			break;
		default:
			return;
		}
		if(isUpdateRank){
			CommandAcademyRank rank = this.getRankObject(rankType.intValue());
			if(rank != null){
				entity.setStageParam(curStage, maxRecored);
				CommandAcademyRankMember member = new CommandAcademyRankMember(playerId,
						RankScoreHelper.calcSpecialRankScore(maxRecored) );
				rank.insertRank(member);
				logger.info("CommandAcademyActivity rank update "+curStage+","+playerId+","+ maxRecored);
			}
		}
	}
	
	
	@Override
	public void onTick() {
		long time = HawkTime.getMillisecond();
		if(this.stageId == 0){
			//初始化当前状态
			this.stageId = this.getCommandAcademyStage();
			return;
		}
		if(this.ranks.isEmpty()){
			//初始化排行榜数据
			this.initRanks();
			this.stageRankUpdateTime = time;
		}
		
		//状态更变
		this.stageCheck();
		//更新能源数量
		this.upateEneryCount();
		//更新团购礼包
		this.upateGroupGiftBuyCount(true,true);
		//更新阶段榜  3分钟
		this.updateStageRanks(this.stageId);
		//注水
		this.giftBuyCountAssistCheck(this.stageId);
		//发放阶段奖励
		this.sendStageAward();
		//发放总榜奖励
		this.sendFinalAward();
		
	}
	
	private void giftBuyCountAssistCheck(int stage) {
		if(this.stageId == CommandAcademyConst.STAGE_END){
			return;
		}
		CommandAcademyStageCfg stageCfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, stage);
		if(stageCfg == null){
			return;
		}
		List<int[]> assistList = stageCfg.getBuyCountAssistList();
		long startTime = this.getStageStartTime(stage);
		long curTime = HawkTime.getMillisecond();
		for(int[] times : assistList){
			int sH = times[0];
			int eH = times[1];
			int interval = times[2];
			int add = times[3];
			if((curTime >startTime+sH * 1000) &&
					(curTime <startTime + eH * 1000)&& 
						(curTime > (this.giftBuyAssistUpdateTime + interval * 1000))){
				String buyCountKey = this.getBuyCountKey(stage);
				String memberKey = this.getBuyCountAssistMemberKey();
				ActivityLocalRedis.getInstance().zIncrbyWithExpire(buyCountKey, memberKey, 
						add,(int)TimeUnit.DAYS.toSeconds(30));
				this.giftBuyAssistUpdateTime = curTime;
				return;
			}
		}
		
	}

	private void upateGroupGiftBuyCount(boolean sendEvent,boolean updateClient) {
		long time = HawkTime.getMillisecond();
		if(time - this.giftBuyCountUpdateTime < HawkTime.MINUTE_MILLI_SECONDS * 5){
			return;
		}
		int curStage = this.getCommandAcademyStage();
		if(curStage == CommandAcademyConst.STAGE_END){
			return;
		}
		String key = this.getBuyCountKey(curStage);
		if(HawkOSOperator.isEmptyString(key)){
			return;
		}
		this.giftBuyCountUpdateTime = time;
		Set<Tuple> set = ActivityLocalRedis.getInstance().zRevrangeWithScores(key);
		for(Tuple tuple : set){
			Double count = tuple.getScore();
			String countMember = tuple.getElement();
			if(countMember.equals(this.getBuyCountMemberKey())){
				this.giftBuyCount.put(curStage, count.intValue());
				continue;
			}
			if(countMember.equals(this.getBuyCountAssistMemberKey())){
				this.giftBuyCountAssist.put(curStage, count.intValue());
				continue;
			}
			
		}
		
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		int buyCount = this.getBuyCount(curStage);
		if(lastEventBuyCount== buyCount){
			return;
		}
		lastEventBuyCount = buyCount;
		
		for (String playerId : onlinePlayerIds) {
			Optional<CommandAcademyEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				continue;
			}
			CommandAcademyEntity entity = opEntity.get();
			if(sendEvent){
				CommandAcademyGroupGiftEvent event = new CommandAcademyGroupGiftEvent(playerId, buyCount,entity.isBuyGift());
				ActivityManager.getInstance().postEvent(event);
			}
			if(updateClient){
				this.updateBuyCount(buyCount, entity);
			}
		}
		
		int buyCountStage = 0;
		if(this.giftBuyCount.containsKey(curStage)){
			buyCountStage = this.giftBuyCount.get(curStage);
		}
		
		int termId = this.getActivityTermId();
		this.getDataGeter().logCommandAcademyBuyCount(termId, curStage, 
				buyCountStage, buyCount);
	}

	private String getBuyCountMemberKey(){
		return "buyCount";
	}
	
	private String getBuyCountAssistMemberKey(){
		return "buyCountAssist";
	} 
	private String getBuyCountKey(int stage) {
		int term = this.getActivityTermId();
		if(term == 0){
			return "";
		}
		String serverId = this.getDataGeter().getLocalIdentify();
		long openTime = this.getDataGeter().getServerOpenDate();
		if(openTime < this.fixTime){
			serverId = this.getDataGeter().getServerId();
		}
		StringBuilder keyStr = new StringBuilder();
		keyStr.append(serverId).append(":commandAcademyActivity:").append(term).
		append(":buyCount").append(":").append(stage);
		return keyStr.toString();
		
	}

	public void updateStageRanks(int stage){
		long time = HawkTime.getMillisecond();
		if(time - this.stageRankUpdateTime < HawkTime.MINUTE_MILLI_SECONDS * 3){
			return;
		}
		this.stageRankUpdateTime = time;
		CommandAcademyStageCfg stageCfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, stage);
		if(stageCfg == null){
			return;
		}
		CommandAcademyRank rank = this.getRankObject(stageCfg.getFuncType());
		if(rank == null){
			return;
		}
		rank.rankSort();
		//同时更新总榜积分
		this.updateFinalScore(stage);
	}
	
	
	/**
	 * 初始化排行榜数据
	 */
	public void initRanks(){
		for(CommandAcademyConst.RankType type :
				CommandAcademyConst.RankType.values()){
			CommandAcademyRank rank = new CommandAcademyRank(type);
			if(type != CommandAcademyConst.RankType.FINAL){
				rank.loadRank();
			}
			this.ranks.put(type.intValue(), rank);
		}
		int stageId = this.getCommandAcademyStage();
		this.updateFinalScore(stageId);
	}
	
	
	
	/**
	 * 状态变更
	 */
	public void stageCheck(){
		 int curStage =  this.getCommandAcademyStage();
		 if(this.stageId != curStage){
			 //刷新榜
			 int tempStage = this.stageId;
			 this.stageId = curStage;
			 CommandAcademyStageCfg stageCfg = HawkConfigManager.getInstance().
						getConfigByKey(CommandAcademyStageCfg.class, tempStage);
			 CommandAcademyRank rank =  this.getRankObject(stageCfg.getFuncType());
			 if(rank!= null){
				 rank.rankSort();
				 this.onOpen();
			 }
		 }
		 
	}
	
	private String getSendAwardStageKey() {
		int term = this.getActivityTermId();
		if(term == 0){
			return "";
		}
		
		String serverId = this.getDataGeter().getLocalIdentify();
		long openTime = this.getDataGeter().getServerOpenDate();
		if(openTime < this.fixTime){
			serverId = this.getDataGeter().getServerId();
		}
		StringBuilder keyStr = new StringBuilder();
		keyStr.append(serverId).append(":commandAcademyActivity:").append(term).append(":finsh");
		return keyStr.toString();
	}
	
	
	public void sendStageAward(){
		 String sendRecord = getSendAwardStageKey();
		 Set<String> sendSet = ActivityLocalRedis.getInstance().sMembers(sendRecord);
		 long time = HawkTime.getMillisecond();
		 List<CommandAcademyStageCfg> stageCfgs = HawkConfigManager.getInstance().
				 getConfigIterator(CommandAcademyStageCfg.class).toList();
		 for(CommandAcademyStageCfg scfg : stageCfgs){
			long stageEndTime = this.getStageEndTime(scfg.getStageId());
			if(time < stageEndTime){
				continue;
			}
			//如果已经结束，查看是否已经结算
			StringBuilder stageBuilder = new StringBuilder().append(scfg.getStageId());
			if(!sendSet.contains(stageBuilder.toString())){
				ActivityLocalRedis.getInstance().getRedisSession().sAdd(
						sendRecord, (int)TimeUnit.DAYS.toSeconds(30), stageBuilder.toString());
				//发送阶段奖励,增加评分进入总榜
				this.sendStageRankAward(scfg.getStageId());
			}
			
		 }
	}

	/**
	 * 发送阶段奖励
	 * @param stage
	 */
	public void sendStageRankAward(int stage){
		logger.info("sendStageRankAward:"+stage);
		CommandAcademyStageCfg stageCfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, stage);
		if(stageCfg == null){
			logger.info("sendStageRankAward stageCfg null:"+stage);
			return;
		}
		CommandAcademyConst.RankType rankType = CommandAcademyConst.
				RankType.getRankType(stageCfg.getFuncType());
		if(rankType == null){
			logger.info("sendStageRankAward RankType null:"+stage);
			return;
		}
		CommandAcademyRank rank = this.getRankObject(stageCfg.getFuncType());
		if(rank == null){
			logger.info("sendStageRankAward rank null:"+stage);
			return;
		}
		//需要重新拉取榜成员
		rank.rankSort();
		List<CommandAcademyRankScoreCfg>  scoreCfgs = this.getRankScoreCfgs(rankType);
		List<CommandAcademyRankMember> list = rank.getRankList();
		int termId = this.getActivityTermId();
		//需要给玩家评分增量
		for(CommandAcademyRankMember member : list){
			CommandAcademyRankScoreCfg cfg = this.getCommandAcademyRankScoreCfg(scoreCfgs, member);
			if(cfg == null){
				continue;
			}
			List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(cfg.getReward());
			Object[] content =  new Object[]{ActivityType.COMMAND_ACADEMY_ACTIVITY.intValue(),stage,member.getRank()};
			Object[] subtitle =  new Object[]{stage};
			this.getDataGeter().sendMail(member.getPlayerId(), MailId.COMMAND_ACADEMY_STAGE_REWARD, null, subtitle, content,
					rewardList, false);
			this.getDataGeter().logCommandAcademyRank(member.getPlayerId(), termId, stage, member.getRank());
			logger.info("sendStageRankAward:"+member.getPlayerId()+","+stage+","+member.getRank());
		}
		
	}
	

	public void updateFinalScore(int stage){
		if(stage == CommandAcademyConst.STAGE_END){
			stage = this.getCommandAcademyLastStage();
		}
		CommandAcademyStageCfg stageCfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, stage);
		if(stageCfg == null){
			return;
		}
		CommandAcademyRank rank = this.getRankObject(stageCfg.getFuncType()); 
		if(rank == null){
			return;
		}
		Map<String,FinalRankMember> scoreMap = new HashMap<String,FinalRankMember>();
		List<CommandAcademyStageCfg> configs = HawkConfigManager.getInstance().
				getConfigIterator(CommandAcademyStageCfg.class).toList();
		List<CommandAcademyStageCfg>  cfgs =new ArrayList<CommandAcademyStageCfg>();
		cfgs.addAll(configs);
		Collections.sort(cfgs, new StageConfigComparator());
		int param = 100000;
		for(CommandAcademyStageCfg cfg : cfgs){
			if(cfg.getOrder() > stageCfg.getOrder()){
				continue;
			}
			CommandAcademyRank stageRank = this.getRankObject(cfg.getFuncType()); 
			if(stageRank == null){
				continue;
			}
			List<CommandAcademyRankMember> members = stageRank.getRankList();
			List<CommandAcademyRankScoreCfg> list = this.getRankScoreCfgs(RankType.getRankType(cfg.getFuncType()));
			for(int i=members.size()-1;i>=0;i--){
				param --;
				CommandAcademyRankMember member  = members.get(i);
				CommandAcademyRankScoreCfg scorecfg = this.getCommandAcademyRankScoreCfg(list, member);
				double mscore = scorecfg.getRankScore();
				FinalRankMember fmember = scoreMap.get(member.getPlayerId());
				if(fmember != null){
					mscore += fmember.getScore();
				}
				if(cfg.getOrder() == stageCfg.getOrder()){
					fmember = new FinalRankMember(member.getPlayerId(), mscore,
							cfg.getOrder(), member.getRank());
				}else{
					fmember = new FinalRankMember(member.getPlayerId(), mscore,
							cfg.getOrder(), param);
				}
				scoreMap.put(member.getPlayerId(), fmember);
			}
		}
		List<FinalRankMember> fianlList = new ArrayList<FinalRankMember>(scoreMap.values());
		Collections.sort(fianlList, new FinalRankComparator());
		for(int i=0;i<fianlList.size();i++){
			if(i>= fianlList.size()){
				break;
			}
			FinalRankMember fmember = fianlList.get(i);
			fmember.setRank(i+1);
		}
		CommandAcademyRank finalRank = this.getRankObject(CommandAcademyConst.RankType.FINAL.intValue());
		if(finalRank != null){
			List<CommandAcademyRankMember> rankList = new ArrayList<CommandAcademyRankMember>();
			rankList.addAll(fianlList);
			finalRank.setRankList(rankList);
		}
	}
	
	
	/**
	 * 发送总榜奖励
	 */
	public void sendFinalAward(){
		if(this.stageId != CommandAcademyConst.STAGE_END){
			return;
		}
		//判断时间是否已经是最后阶段结束
		String sendRecord = getSendAwardStageKey();
		StringBuilder stageBuilder = new StringBuilder().append(CommandAcademyConst.STAGE_END);
		Set<String> sendSet = ActivityLocalRedis.getInstance().sMembers(sendRecord);
		if(sendSet.contains(stageBuilder.toString())){
			return;
		}
		logger.info("sendFinalAward");
		this.updateFinalScore(CommandAcademyConst.STAGE_END);
		CommandAcademyRank rank = this.getRankObject(CommandAcademyConst.RankType.FINAL.intValue());
		if(rank == null){
			logger.info("sendFinalAward rank null");
			return;
		}
		ActivityLocalRedis.getInstance().getRedisSession().sAdd(
				sendRecord, (int)TimeUnit.DAYS.toSeconds(30), stageBuilder.toString());
		List<CommandAcademyRankMember> list = rank.getRankList();
		List<String> awardList = new ArrayList<String>();
		List<CommandAcademyRankScoreCfg>  scoreCfgs = this.getRankScoreCfgs(CommandAcademyConst.RankType.FINAL);
		int termId = this.getActivityTermId();
		for(CommandAcademyRankMember member : list){
			CommandAcademyRankScoreCfg cfg = this.getCommandAcademyRankScoreCfg(scoreCfgs, member);
			if(cfg == null){
				continue;
			}
			awardList.add(member.toString());
			List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(cfg.getReward());
			Object[] content =  new Object[]{ActivityType.COMMAND_ACADEMY_ACTIVITY.intValue(),member.getRank()};
			this.getDataGeter().sendMail(member.getPlayerId(), MailId.COMMAND_ACADEMY_FINAL_REWARD, null, null, content,
					rewardList, false);
			this.getDataGeter().logCommandAcademyRank(member.getPlayerId(), termId, CommandAcademyConst.STAGE_END, member.getRank());
			logger.info("sendFinalAward:"+member.getPlayerId()+","+member.getRank());
		}
		
		if(awardList.size() > 0){
			String[] awardArr = new String[awardList.size()];
			awardList.toArray(awardArr);
			String finalRankKey = this.getRankRedisKey(CommandAcademyConst.RankType.FINAL);
			ActivityLocalRedis.getInstance().sadd(finalRankKey, (int)TimeUnit.DAYS.toSeconds(30),awardArr);
		}
	}

	public CommandAcademyRankScoreCfg getCommandAcademyRankScoreCfg(
			List<CommandAcademyRankScoreCfg>  scoreCfgs,CommandAcademyRankMember member){
		for(CommandAcademyRankScoreCfg cfg : scoreCfgs){
			if(member.getRank() >= cfg.getRankUpper() && 
					member.getRank() <= cfg.getRankLower()){
				return cfg;
			}
		}
		return null;
		
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
	public boolean isActivityClose(String playerId) {
		return !checkPlayerOpen(playerId);
	}
	
	/**
	 * @param playerId
	 * @return true=表示达成条件
	 */
	private boolean checkPlayerOpen(String playerId) {
		//判定playerId是否合法
		if (HawkOSOperator.isEmptyString(playerId)) {
			logger.info(this.getClass().getSimpleName()+" activity isPlayerOpen playerId : {} isNull", playerId);
			return false;
		}
		
		CommandAcademyKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(CommandAcademyKVCfg.class);
		if(cfg == null){
			logger.info(this.getClass().getSimpleName()+" activity isPlayerOpen CommandAcademySimplifyKVCfg not find");
			return false;
		}
		
		long serverOpenTime = this.getDataGeter().getServerOpenDate();
		//开服时间在配置时间内，当前堡等级大于配置等级
		return serverOpenTime >=cfg.getTimeStart() && serverOpenTime <= cfg.getTimeEnd();
	}		

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<CommandAcademyEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		CommandAcademyEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public Action takeRewardAction() {
		
		return Action.COMMAND_ACADEMY_ACHIVE_REWARD;
	}


	
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		int stage = this.getCommandAcademyStage();
		CommandAcademyStageCfg stageCfg = HawkConfigManager.
				getInstance().getConfigByKey(CommandAcademyStageCfg.class, stage);
		if(stageCfg == null ){
			return null;
		}
		CommandAcademyAchieveCfg achiveCfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyAchieveCfg.class, achieveId);
		if(achiveCfg!= null){
			if(achiveCfg.getStageId() == stage){
				return achiveCfg;
			}
			return null;
		}
		CommandAcademyBoxAchieveCfg boxAchiveCfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyBoxAchieveCfg.class, achieveId);
		
		if(boxAchiveCfg!= null && boxAchiveCfg.getStageId() == stage){
			return boxAchiveCfg;
		} 
		return null;
	}


	private void initAchieveInfo(String playerId) {
		Optional<CommandAcademyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		CommandAcademyEntity entity = opEntity.get();
		int stage = this.getCommandAcademyStage();
		if(stage == CommandAcademyConst.STAGE_END){
			return;
		}
		// 成就已初始化
		if (entity.getStage() == stage) {
			return;
		}
		//状态变换更新数据到当前榜
		this.updateStagePlayerScore(playerId, stage,entity);
		// 初始添加成就项
		List<AchieveItem> addList = new ArrayList<AchieveItem>();
		List<AchieveConfig> achieveCfgList = this.getAchieveConfigList(stage);
		for(AchieveConfig acfg : achieveCfgList){
			AchieveItem item = AchieveItem.valueOf(acfg.getAchieveId());
			addList.add(item);
		}
		//添加宝箱成就
		List<AchieveConfig> groupPurchaseAchieveCfgList = this.getGroupPurchaseAchieveConfigList(stage);
		for(AchieveConfig acfg : groupPurchaseAchieveCfgList){
			AchieveItem item = AchieveItem.valueOf(acfg.getAchieveId());
			addList.add(item);
		}
		entity.resetItemList(addList);
		entity.clearBuyList();
		entity.setStage(stage);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		
	}
	

	public void updateStagePlayerScore(String playerId,int stage,
			CommandAcademyEntity entity){
		CommandAcademyStageCfg scfg= HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, stage);
		if(scfg == null){
			return;
		}
		CommandAcademyRank rank = this.getRankObject(scfg.getFuncType());
		if(rank == null){
			return;
		}
		PowerData data = this.getDataGeter().getPowerData(playerId);
		if(data == null){
			return;
		}
		int score = 0;
		switch (rank.getRankType()) {
		case BUILDING:
			score = this.getDataGeter().getConstructionFactoryLevel(playerId);
			//主城初始为1级
			if(score == 1){
				score =0;
			}
			break;
		case TECHNOLOGY:
			score = data.getTechBattlePoint();
			break;
		case ENERGY:
			score = this.getDataGeter().getEnergyCount(playerId);
			break;
		case HERO:
			score = data.getHeroBattlePoint();
			break;
		case ARMOR:
			score = data.getSuperSoldierBattlePoint();
			break;
		case ARMY:
			//这个改为增量，不需要初始设置
			//score = data.getArmyBattlePoint();
			break;
		case SEQUIP:
			//这个改为增量，不需要初始设置
			//score = this.getDataGeter().getEquipNumByCondition(playerId, 0, 3);
			break;
		default:
			break;
		}
		if(score <= 0){
			return;
		}
		//初始化当前入榜值
		entity.setStageParam(stage, score);
		CommandAcademyRankMember member = new CommandAcademyRankMember(playerId, 
				RankScoreHelper.calcSpecialRankScore(score));
		rank.insertRank(member);
		
	}
	
	public long getStageStartTime(int stage){
		CommandAcademyStageCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, stage);
		if(cfg == null){
			return 0;
		}
		long endTime = this.getStageEndTime(stage);
		long startTime = endTime - cfg.getTime();
		return startTime;
		
	}
	public long getStageEndTime(int stage){
		int termId = this.getActivityTermId();
		long startTime = this.getTimeControl().getStartTimeByTermId(termId);
		List<CommandAcademyStageCfg> configs = HawkConfigManager.getInstance().
				getConfigIterator(CommandAcademyStageCfg.class).toList();
		List<CommandAcademyStageCfg>  cfgs =new ArrayList<CommandAcademyStageCfg>();
		cfgs.addAll(configs);
		Collections.sort(cfgs, new StageConfigComparator());
		for(CommandAcademyStageCfg config : cfgs){
			long stageEndTime = startTime + config.getTime();
			if(config.getStageId() == stage){
				return stageEndTime;
			}
			startTime = stageEndTime;
		}
		return 0;
	}
	
	public int getCommandAcademyLastStage(){
		List<CommandAcademyStageCfg> stages = HawkConfigManager.getInstance().
				getConfigIterator(CommandAcademyStageCfg.class).toList();
		List<CommandAcademyStageCfg>  cfgs =new ArrayList<CommandAcademyStageCfg>();
		cfgs.addAll(stages);
		Collections.sort(cfgs, new StageConfigComparator());
		int index = cfgs.size() -1;
		CommandAcademyStageCfg last = cfgs.get(index);
		return last.getStageId();
	}
	
	
	public int getCommandAcademyStage(){
		List<CommandAcademyStageCfg> stages = HawkConfigManager.getInstance().
				getConfigIterator(CommandAcademyStageCfg.class).toList();
		List<CommandAcademyStageCfg>  cfgs =new ArrayList<CommandAcademyStageCfg>();
		cfgs.addAll(stages);
		Collections.sort(cfgs, new StageConfigComparator());
		long curTime = HawkTime.getMillisecond();
		int termId = this.getActivityTermId();
		long startTime = this.getTimeControl().getStartTimeByTermId(termId);
		long stageEndTime = 0;
		for(CommandAcademyStageCfg stage : cfgs){
			stageEndTime = startTime + stage.getTime();
			if(curTime >startTime && curTime < stageEndTime){
				return stage.getStageId();
			}
			startTime = stageEndTime;
		}
		if(curTime >= stageEndTime){
			return CommandAcademyConst.STAGE_END;
		}
		return 0;
	}
	

	public List<AchieveConfig> getAchieveConfigList(int stage){
		List<AchieveConfig> achieveList = new ArrayList<AchieveConfig>();
		if(stage == CommandAcademyConst.STAGE_END){
			return achieveList;
		}
		List<CommandAcademyAchieveCfg> list = HawkConfigManager.getInstance().
				getConfigIterator(CommandAcademyAchieveCfg.class).toList();
		list.stream().forEach(cfg->{
			if(cfg.getStageId() == stage){
				achieveList.add(cfg);
			}
		});
		return achieveList;
	}
	
	
	public List<AchieveConfig> getGroupPurchaseAchieveConfigList(int stage){
		List<AchieveConfig> achieveList = new ArrayList<AchieveConfig>();
		if(stage == CommandAcademyConst.STAGE_END){
			return achieveList;
		}
		List<CommandAcademyBoxAchieveCfg> list = HawkConfigManager.getInstance().
				getConfigIterator(CommandAcademyBoxAchieveCfg.class).toList();
		list.stream().forEach(cfg->{
			if(cfg.getStageId() == stage){
				achieveList.add(cfg);
			}
		});
		return achieveList;
	}
	
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		
		CommandAcademyAchieveCfg achiveCfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyAchieveCfg.class, achieveId);
		if(achiveCfg != null){
			CommandAcademyStageCfg stageCfg = HawkConfigManager.
					getInstance().getConfigByKey(CommandAcademyStageCfg.class, stageId);
			if(stageCfg != null &&  stageCfg.getFuncType() == 
					CommandAcademyConst.RankType.ENERGY.intValue()){
				callBack(playerId, MsgId.COMMAND_ACADEMY_ACHIVE_PROCESS, () -> {
					callBack(playerId, MsgId.COMMAND_ACADEMY_ACHIVE_PROCESS, () -> {
						int eCount = this.getDataGeter().getEnergyCount(playerId);
						EnergyCountEvent event = new EnergyCountEvent(playerId, eCount);
						ActivityManager.getInstance().postEvent(event);
					});
				});
			}
			
		}
		return Result.success();
	}

	
	public String getRankRedisKey(CommandAcademyConst.RankType rank){
		int term = this.getActivityTermId();
		if(term == 0){
			return "";
		}
		
		String serverId = this.getDataGeter().getLocalIdentify();
		long openTime = this.getDataGeter().getServerOpenDate();
		if(openTime < this.fixTime){
			serverId = this.getDataGeter().getServerId();
		}
		StringBuilder keyStr = new StringBuilder();
		keyStr.append(serverId).append(":commandAcademyActivity:").
			append(term).append(":").append(rank.getRankName());
		return keyStr.toString();
	}
	
	
	public  CommandAcademyRank getRankObject(int rankType){
		return this.ranks.get(rankType);
	}

	public int getRankSize(RankType rankType) {
		List<CommandAcademyRankScoreCfg>  cfgs = this.getRankScoreCfgs(rankType);
		int rankSize = 0;
		for(CommandAcademyRankScoreCfg cfg : cfgs){
			if(cfg.getRankLower() > rankSize){
				rankSize = cfg.getRankLower();
			}
		}
		return rankSize;
	}
	
	
	public List<CommandAcademyRankScoreCfg> getRankScoreCfgs(RankType rankType){
		int rankId = 0;
		switch (rankType) {
		case FINAL:
			CommandAcademyKVCfg kvConfig = HawkConfigManager.getInstance().
			getKVInstance(CommandAcademyKVCfg.class);
			rankId = kvConfig.getCycleRankId();
			break;
		case BUILDING:
		case TECHNOLOGY:
		case ENERGY:
		case HERO:
		case ARMOR:
		case ARMY:
		case SEQUIP:
			CommandAcademyStageCfg cfg = this.getCommandAcademyStageCfg(rankType.intValue());
			rankId = cfg.getRankId();
			break;
		default:
			break;
		}
		List<CommandAcademyRankScoreCfg> list = this.getDataGeter().getCommandAcademyRankScoreCfg(rankId);
		return list;
	}
	
	
	public int getBuyCount(int stage){
		CommandAcademyStageCfg stageCfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, stage);
		int limit = 0;
		if(stageCfg != null){
			limit = stageCfg.getShowNumLimit();
		}
		int buyCount = 0;
		if(this.giftBuyCount.containsKey(stage)){
			buyCount = this.giftBuyCount.get(stage);
		}
		int buyCountAssist = 0;
		if(this.giftBuyCountAssist.containsKey(stage)){
			buyCountAssist = this.giftBuyCountAssist.get(stage);
		}
		int total = buyCount + buyCountAssist;
		return Math.min(total, limit);
	}
	
	
	public CommandAcademyStageCfg getCommandAcademyStageCfg(int rankType){
		List<CommandAcademyStageCfg> list = HawkConfigManager.getInstance().
				getConfigIterator(CommandAcademyStageCfg.class).toList();
		for(CommandAcademyStageCfg cfg : list){
			if(cfg.getFuncType() == rankType ){
				return cfg;
			}
		}
		return null;
	}
	
	
	
	public void updateBuyCount(int buyCount,CommandAcademyEntity entity){
		CommandCollegePackageResp.Builder builder = CommandCollegePackageResp.newBuilder();
		builder.setBuyCount(buyCount);
		for(Entry<Integer, Integer> entry : entity.getBuyGiftMap().entrySet()){
			KeyValuePairInt.Builder kbuilder = KeyValuePairInt.newBuilder();
			kbuilder.setKey(entry.getKey());
			kbuilder.setVal(entry.getValue());
			builder.addBuyList(kbuilder);
		}
		PlayerPushHelper.getInstance().pushToPlayer(entity.getPlayerId(),
				HawkProtocol.valueOf(HP.code.COMMAND_COLLEGE_PACKAGE_RESP, builder));
	}
	// 同步数据消息给玩家
	public void syncActivityInfo(String playerId, CommandAcademyEntity entity) {
		
		CommandCollegeInfoResp.Builder builder =  CommandCollegeInfoResp.newBuilder();
		int curStage = this.getCommandAcademyStage();
		int stageCfgId = curStage;
		if(curStage == CommandAcademyConst.STAGE_END){
			stageCfgId = this.getCommandAcademyLastStage();
		}
		CommandAcademyStageCfg stageCfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, stageCfgId);
		if(stageCfg == null){
			return;
		}
		CommandAcademyRank rank = this.getRankObject(stageCfg.getFuncType());
		if(rank == null){
			return;
		}
		List<CommandAcademyRankMember> rankMembers = rank.getClientShowList();
		for(CommandAcademyRankMember member : rankMembers){
			CommandCollegeRankMember.Builder mbuilder = this.getRankMemberBuilder(
					rank.getRankType(), member);
			builder.addMembers(mbuilder);
		}
		CommandAcademyRankMember self = rank.getRank(playerId);
		CommandCollegeRankMember.Builder sbuilder = this.getRankMemberBuilder(
				rank.getRankType(), self);
		builder.setSelfRank(sbuilder);
		long endTime = this.getStageEndTime(stageCfgId);
		if(curStage == CommandAcademyConst.STAGE_END){
			int termId = this.getActivityTermId();
			endTime = this.getTimeControl().getHiddenTimeByTermId(termId);
		}
		builder.setStage(stageCfg.getStageId());
		builder.setTimeLast(endTime);
		builder.setBuyCount(this.getBuyCount(stageCfg.getStageId()));
		for(Entry<Integer, Integer> entry : entity.getBuyGiftMap().entrySet()){
			KeyValuePairInt.Builder kbuilder = KeyValuePairInt.newBuilder();
			kbuilder.setKey(entry.getKey());
			kbuilder.setVal(entry.getValue());
			builder.addBuyList(kbuilder);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.COMMAND_COLLEGE_INFO_RESP_VALUE, builder));
	}
	
	public void buyPackage(String playerId, int packageId,int protoType) {
		CommandAcademyGiftCfg gcfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyGiftCfg.class, packageId);
		if(gcfg == null){
			return;
		}
		int stage = this.getCommandAcademyStage();
		CommandAcademyStageCfg stageCfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, stage);
		if(stageCfg == null){
			return;
		}
		if(gcfg.getStageId() != stage){
			return;
		}
		Optional<CommandAcademyEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		int termId = this.getActivityTermId();
		CommandAcademyEntity entity =  opEntity.get();
		int times = entity.getBuyTimes(packageId);
		if(times >= gcfg.getBuyLimit()){
			return;
		}
		// 扣费
		List<Reward.RewardItem.Builder> cost = RewardHelper.toRewardItemList(gcfg.getPrice());
		boolean consumeResult = this.getDataGeter().consumeItems(playerId, cost, protoType, Action.COMMAND_ACADEMY_BUY_COST);
		if (consumeResult == false) {
			return;
		}
		boolean isBuy = entity.isBuyGift();
		entity.addBuyGift(packageId);
		List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(gcfg.getAwards());
		this.getDataGeter().takeReward(playerId,rewardList, 1, Action.COMMAND_ACADEMY_BUY_REWARD, true,RewardOrginType.MONCARD_DAILY_SIGN_ACHIEVE);
		this.getDataGeter().sendAllianceGift(playerId, gcfg.getAllianceGift());
		CommandCollegeBuyPackageResp.Builder builder = CommandCollegeBuyPackageResp.newBuilder();
		builder.setPackageId(packageId);
		for(Entry<Integer, Integer> entry : entity.getBuyGiftMap().entrySet()){
			KeyValuePairInt.Builder kbuilder = KeyValuePairInt.newBuilder();
			kbuilder.setKey(entry.getKey());
			kbuilder.setVal(entry.getValue());
			builder.addBuyList(kbuilder);
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.COMMAND_COLLEGE_BUY_PACKAGE_RESP_VALUE, builder);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, protocol);
		//抛团购礼包事件
		CommandAcademyGroupGiftEvent event = new 
				CommandAcademyGroupGiftEvent(playerId, this.getBuyCount(stage),entity.isBuyGift());
		ActivityManager.getInstance().postEvent(event);
		//添加记录
		if(!isBuy){
			String buyCountKey = this.getBuyCountKey(stage);
			String memberKey = this.getBuyCountMemberKey();
			ActivityLocalRedis.getInstance().zIncrbyWithExpire(buyCountKey, memberKey, 
					1,(int)TimeUnit.DAYS.toSeconds(30));
		}
		if(stageCfg.getFuncType() == CommandAcademyConst.RankType.ENERGY.intValue()){
			callBack(playerId, MsgId.COMMAND_ACADEMY_ACHIVE_PROCESS, () -> {
				int eCount = this.getDataGeter().getEnergyCount(playerId);
				EnergyCountEvent eEvent = new EnergyCountEvent(playerId, eCount);
				ActivityManager.getInstance().postEvent(eEvent);
			});
		}
		
		this.getDataGeter().logCommandAcademyGiftBuy(playerId, termId, stage, packageId);
	}

	public void getFinalRankInfo(String playerId) {
		CommandAcademyRank rank = this.getRankObject(CommandAcademyConst.RankType.FINAL.intValue());
		if(rank == null){
			return;
		}
		CommandCollegeFinalRankResp.Builder builder = CommandCollegeFinalRankResp.newBuilder();
		List<CommandAcademyRankMember> rankMembers = rank.getClientShowList();
		for(CommandAcademyRankMember member : rankMembers){
			CommandCollegeRankMember.Builder mbuilder = this.getRankMemberBuilder(
					rank.getRankType(), member);
			builder.addMembers(mbuilder);
		}
		CommandAcademyRankMember self = rank.getCacheRank(playerId);
		if(self ==null){
			self = new CommandAcademyRankMember(playerId,0, 0);
		}
		CommandCollegeRankMember.Builder sbuilder = this.getRankMemberBuilder(
				rank.getRankType(), self);
		builder.setSelfRank(sbuilder);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.COMMAND_COLLEGE_FINAL_RANK_RESP_VALUE, builder);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, protocol);
		
	}

	public void getStageRankInfos(String playerId) {
		CommandCollegeStageRankResp.Builder builder =  CommandCollegeStageRankResp.newBuilder();
		int curStage = this.getCommandAcademyStage();
		int stageCfgId = curStage;
		if(curStage == CommandAcademyConst.STAGE_END){
			stageCfgId = this.getCommandAcademyLastStage();
		}
		CommandAcademyStageCfg stageCfg = HawkConfigManager.getInstance().
				getConfigByKey(CommandAcademyStageCfg.class, stageCfgId);
		if(stageCfg == null){
			return;
		}
		
		Optional<CommandAcademyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		logger.info("getStageRankInfos:"+playerId);
		CommandAcademyEntity entity = opEntity.get();
		List<CommandAcademyStageCfg> configs = HawkConfigManager.getInstance().
				getConfigIterator(CommandAcademyStageCfg.class).toList();
		List<CommandAcademyStageCfg>  cfgs =new ArrayList<CommandAcademyStageCfg>();
		cfgs.addAll(configs);
		Collections.sort(cfgs, new StageConfigComparator());
		for(int i= cfgs.size()-1;i>=0;i--){
			CommandAcademyStageCfg cfg = cfgs.get(i);
			if(cfg.getOrder() > stageCfg.getOrder()){
				continue;
			}
			CommandAcademyRank rank = this.getRankObject(cfg.getFuncType());
			if(rank == null){
				return;
			}
			logger.info("getStageRankInfos: rank:"+cfg.getStageId());
			CommandCollegeStageRank.Builder rbuilder = CommandCollegeStageRank.newBuilder();
			rbuilder.setStageId(cfg.getStageId());
			List<CommandAcademyRankMember> rankMembers = rank.getClientShowList();
			logger.info("getStageRankInfos: rankSize:"+rankMembers.size());
			for(CommandAcademyRankMember member : rankMembers){
				CommandCollegeRankMember.Builder mbuilder = this.getRankMemberBuilder(
						rank.getRankType(), member);
				rbuilder.addMembers(mbuilder);
			}
			builder.addRanks(rbuilder);
		}
		for(CommandAcademyStageCfg cfg : cfgs){
			if(cfg.getOrder() > stageCfg.getOrder()){
				continue;
			}
			int stageId = cfg.getStageId();
			Integer index = entity.getRankIndex(stageId);
			if(index == null){
				CommandAcademyRank stageRank = this.getRankObject(cfg.getFuncType());
				if(stageRank == null){
					index = 0;
				}else{
					CommandAcademyRankMember member = stageRank.getRank(playerId);
					index = member.getRank();
				}
				//如果不上当前阶段则存数据
				if(cfg.getStageId() != curStage){
					entity.setRankIndex(stageId, index);
				}
				
			}
			KeyValuePairInt.Builder kbuilder = KeyValuePairInt.newBuilder();
			kbuilder.setKey(stageId);
			kbuilder.setVal(index);
			logger.info("getStageRankInfos: rankIndex:"+stageId+"_"+index);
			builder.addStageRanks(kbuilder);
			
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.COMMAND_COLLEGE_STAGE_RANK_RESP_VALUE, builder);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, protocol);
	}
	
	
	public CommandCollegeRankMember.Builder getRankMemberBuilder(CommandAcademyConst.RankType rankType,
			CommandAcademyRankMember member){
		List<CommandAcademyRankScoreCfg>  scoreCfgs = this.getRankScoreCfgs(rankType);
		if(scoreCfgs == null){
			return null;
		}
		CommandCollegeRankMember.Builder builder = CommandCollegeRankMember.newBuilder();
		String playerName = this.getDataGeter().getPlayerName(member.getPlayerId());
		String guildName =  this.getDataGeter().getGuildNameByByPlayerId(member.getPlayerId());
		builder.setPlayerId(member.getPlayerId());
		builder.setRankIndex(member.getRank());
		builder.setPlayerName(playerName);
		builder.addAllPersonalProtectSwitch(this.getDataGeter().getPersonalProtectVals(member.getPlayerId()));
		if(!HawkOSOperator.isEmptyString(guildName)){
			builder.setGuildName(guildName);
		}
		Double rankValue = member.getScore();
		if(rankType == CommandAcademyConst.RankType.FINAL){
			builder.setStageParam(0);
			builder.setSocre(rankValue.intValue());
		}else{
			long realValue = RankScoreHelper.getRealScore(rankValue.longValue());
			builder.setStageParam((int)realValue);
			CommandAcademyRankScoreCfg cfg = this.getCommandAcademyRankScoreCfg(scoreCfgs, member);
			int score = 0;
			if(cfg != null){
				score = cfg.getRankScore();
			}
			builder.setSocre(score);
		}
		return builder;
	}

	
}
