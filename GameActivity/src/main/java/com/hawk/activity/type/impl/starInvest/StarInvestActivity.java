package com.hawk.activity.type.impl.starInvest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.AddTavernScoreEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.StarInvestGiftBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.starInvest.cfg.StarInvestAchieveCfg;
import com.hawk.activity.type.impl.starInvest.cfg.StarInvestExploreBox;
import com.hawk.activity.type.impl.starInvest.cfg.StarInvestExploreCellCfg;
import com.hawk.activity.type.impl.starInvest.cfg.StarInvestGiftCfg;
import com.hawk.activity.type.impl.starInvest.cfg.StarInvestKVCfg;
import com.hawk.activity.type.impl.starInvest.cfg.StarInvestMenuCfg;
import com.hawk.activity.type.impl.starInvest.cfg.StarInvestRewardCfg;
import com.hawk.activity.type.impl.starInvest.cfg.StarInvestTimeCfg;
import com.hawk.activity.type.impl.starInvest.entity.StarInvestEntity;
import com.hawk.game.protocol.Activity.PBStarInvestBufGift;
import com.hawk.game.protocol.Activity.PBStarInvestBuyGiftState;
import com.hawk.game.protocol.Activity.PBStarInvestExploreCell;
import com.hawk.game.protocol.Activity.PBStarInvestExploreRecord;
import com.hawk.game.protocol.Activity.PBStarInvestExploreRecordResp;
import com.hawk.game.protocol.Activity.PBStarInvestFreeGfitState;
import com.hawk.game.protocol.Activity.PBStarInvestFreeGift;
import com.hawk.game.protocol.Activity.PBStarInvestPageInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.serialize.string.SerializeHelper;

public class StarInvestActivity extends ActivityBase implements AchieveProvider{

	public StarInvestActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.STAR_INVEST;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		StarInvestActivity activity = new StarInvestActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<StarInvestEntity> queryList = HawkDBManager.getInstance()
				.query("from StarInvestEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			StarInvestEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		StarInvestEntity entity = new StarInvestEntity(playerId, termId);
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
		Optional<StarInvestEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		StarInvestEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initActivityInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(StarInvestAchieveCfg.class, achieveId);
		return config;
	}

	@Override
	public Action takeRewardAction() {
		return Action.STAR_INVEST_ACHIEVE_REWARD;
	}

	@Override
	public int providerActivityId() {
		return 0;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		this.initActivityInfo(playerId);
	}
	
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.STAR_INVEST, ()-> {
				initActivityInfo(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}

	public void initActivityInfo(String playerId){
		int termId = this.getActivityTermId();
		if(termId <= 0){
			return;
		}
		
		Optional<StarInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		StarInvestEntity entity = opEntity.get();
		if(entity.getInitTime() > 0){
			return;
		}
		// 成就已初始化
		List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<StarInvestAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(StarInvestAchieveCfg.class);
		while (configIterator.hasNext()) {
			StarInvestAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			itemList.add(item);
		}
		long curTime = HawkTime.getMillisecond();
		entity.setInitTime(curTime);
		entity.setItemList(itemList);
		entity.recordLoginDay();
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), entity.getItemList()), true);
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
		Optional<StarInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		int giftId = StarInvestMenuCfg.getGiftId(payforId);
		StarInvestMenuCfg medalFundGiftCfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestMenuCfg.class, giftId);
		if (medalFundGiftCfg == null) {
			return false;
		}
		StarInvestEntity entity = opEntity.get();
		Map<Integer, Integer> buyInfoMap = entity.getBuyInfoMap();
		//已经购买
		if (buyInfoMap.containsKey(giftId)) {
			return false;
		}
		return true;
	}
	
	/** 开始探索
	 * @param playerId
	 */
	public Result<?> exploreRewardStart(String playerId, int cellId, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		//是否是活动期内
		if (this.getActivityEntity().getState() != ActivityState.OPEN.intValue()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		StarInvestExploreCellCfg cellCfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestExploreCellCfg.class, cellId);
		if(Objects.isNull(cellCfg)){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		Optional<StarInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		StarInvestEntity entity = opEntity.get();
		int unlock = this.cellUnlock(cellId, entity);
		if(unlock <= 0){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		Map<Integer, StarInvestExploreCell> cellMap = entity.getCellMap();
		StarInvestExploreCell cell = cellMap.get(cellId);
		long startTime =0;
		if(Objects.nonNull(cell)){
			startTime = cell.getStartTime();
		}
		if(startTime > 0){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		long curTime = HawkTime.getMillisecond();
		if(Objects.isNull(cell)){
			cell = new StarInvestExploreCell();
			cell.setCellId(cellId);
			cellMap.put(cell.getCellId(), cell);
		}
		//设置领取时间
		cell.setStartTime(curTime);
		entity.notifyUpdate();
		//push
		syncActivityDataInfo(playerId);
		//Tlog
		int termId = this.getActivityTermId();
		int advance = this.cellAdvance(entity);
		this.logStarInvestExploreStart(playerId, termId, cellId, advance);
		return Result.success();
	}
	
	/**
	 * 收货探索奖励
	 * @param playerId
	 * @param cellId
	 * @param protoType
	 * @return
	 */
	public Result<?> exploreRewardAchieve(String playerId, int cellId, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		StarInvestExploreCellCfg cellCfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestExploreCellCfg.class, cellId);
		if(Objects.isNull(cellCfg)){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		Optional<StarInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		StarInvestEntity entity = opEntity.get();
		int unlock = this.cellUnlock(cellId, entity);
		if(unlock <= 0){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		this.checExplorekData(entity);
		Map<Integer, StarInvestExploreCell> freeInfoMap = entity.getCellMap();
		StarInvestExploreCell cell = freeInfoMap.get(cellId);
		if(Objects.isNull(cell)){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		if(cell.getStartTime() <= 0){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		long curTime = HawkTime.getMillisecond();
		long endTime = this.getExploreEndTime(cell, entity);
		if(curTime < endTime){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		int boxId = cell.getBoxId();
		if(boxId <= 0){
			boxId = this.randomExploreReward(entity, cellCfg);
		}
		//设置领取时间
		cell.setStartTime(0);
		cell.setBoxId(0);
		cell.setBoxAdvance(0);
		cell.setSpeed(0);
		entity.notifyUpdate();
		
		StarInvestExploreBox boxCfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestExploreBox.class, boxId);
		HawkTuple2<Integer,List<RewardItem.Builder>> rewardTupe = boxCfg.getRewardList();
		int rewardIndex = 0;
		if(Objects.nonNull(rewardTupe)){
			rewardIndex = rewardTupe.first;
			List<RewardItem.Builder> rewardItemList = rewardTupe.second;
			this.getDataGeter().takeReward(playerId, rewardItemList, Action.STAR_INVEST_EXPLORE_REWARD, true);
		}
		int termId = this.getActivityTermId();
		int advance = this.cellAdvance(entity);
		this.addExploreRecord(playerId, curTime, boxId,rewardIndex, advance);
		//push
		syncActivityDataInfo(playerId);
		//Tlog
		this.logStarInvestExploreReward(playerId, termId, cellId, advance, boxId);
		return Result.success();
	}
	
	/**
	 * 探索加速
	 * @param playerId
	 * @param cellId
	 * @param protoType
	 * @return
	 */
	public Result<?> exploreSpeed(String playerId, int cellId, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		StarInvestExploreCellCfg cellCfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestExploreCellCfg.class, cellId);
		if(Objects.isNull(cellCfg)){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		Optional<StarInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		StarInvestEntity entity = opEntity.get();
		int unlock = this.cellUnlock(cellId, entity);
		if(unlock <= 0){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		long curTime = HawkTime.getMillisecond();
		Map<Integer, StarInvestExploreCell> cellInfoMap = entity.getCellMap();
		StarInvestExploreCell cell = cellInfoMap.get(cellId);
		if(Objects.isNull(cell)){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		//没有开始探索
		long startTime = cell.getStartTime();
		if(startTime <= 0){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		//先检查
		this.checExplorekData(entity);
		//已经完成
		long endTime = this.getExploreEndTime(cell, entity);
		if(curTime >= endTime){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		//消耗
		StarInvestKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(StarInvestKVCfg.class);
		long speedTime = (endTime - curTime) /1000;
		long needCount = speedTime / cfg.getSpeedTime();
		long needCountAdd = speedTime % cfg.getSpeedTime();
		if(needCountAdd > 0){
			needCount += 1;
		}
		
		RewardItem.Builder sitem = cfg.getSpeedItem();
		long hasNum = this.getDataGeter().getItemNum(playerId,sitem.getItemId());
		int needBuyNum = 0;
		List<RewardItem.Builder> costList = new ArrayList<>();
		if(hasNum >= needCount){
			sitem.setItemCount(needCount);
			costList.add(sitem);
		}else{
			sitem.setItemCount(hasNum);
			costList.add(sitem);
			int buyNum = (int) (needCount - hasNum);
			
			int limitCount = cfg.getCount();
			int alreadyCount = entity.getSpeedItemBuyCount();
			if(alreadyCount + buyNum > limitCount){
				return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
			}
			needBuyNum = buyNum;
			List<RewardItem.Builder> buyList = cfg.getSpeedItemBuyCostList();
			buyList.forEach(re->re.setItemCount(re.getItemCount() * buyNum));
			costList.addAll(buyList);
		}
		boolean sucess = this.getDataGeter().cost(playerId, costList,  Action.STAR_INVEST_EXPLORE_SPEED_COST);
		if(!sucess){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.STAR_INVEST_EXPLORE_SPEED_REQ_VALUE, 
					Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//设置完成
		cell.setStartTime(1);
		int advace = this.cellAdvance(entity);
		int boxId = this.randomExploreReward(entity, cellCfg);
		cell.setBoxId(boxId);
		cell.setBoxAdvance(advace);
		cell.addSpeed((int)needCount);
		//累计购买
		if(needBuyNum > 0){
			int num = entity.getSpeedItemBuyCount() + needBuyNum;
			entity.setSpeedItemBuyCount(num);
		}
		entity.notifyUpdate();
		//push
		syncActivityDataInfo(playerId);
		//Tlog
		int termId = this.getActivityTermId();
		
		this.logStarInvestExploreSpeed(playerId, termId, cellId, advace, (int) needCount);
		return Result.success();
	}
	
	
	/**
	 * 探索加速剩余道具
	 * @param playerId
	 * @param cellId
	 * @param protoType
	 * @return
	 */
	public Result<?> exploreSpeedAllLastItem(String playerId, int cellId, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		StarInvestExploreCellCfg cellCfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestExploreCellCfg.class, cellId);
		if(Objects.isNull(cellCfg)){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		Optional<StarInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		StarInvestEntity entity = opEntity.get();
		int unlock = this.cellUnlock(cellId, entity);
		if(unlock <= 0){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		long curTime = HawkTime.getMillisecond();
		Map<Integer, StarInvestExploreCell> cellInfoMap = entity.getCellMap();
		StarInvestExploreCell cell = cellInfoMap.get(cellId);
		if(Objects.isNull(cell)){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		//没有开始探索
		long startTime = cell.getStartTime();
		if(startTime <= 0){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		//先检查
		this.checExplorekData(entity);
		//已经完成
		long endTime = this.getExploreEndTime(cell, entity);
		if(curTime >= endTime){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		//消耗
		StarInvestKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(StarInvestKVCfg.class);
		long speedTime = (endTime - curTime) /1000;
		long needCount = speedTime / cfg.getSpeedTime();
		long needCountAdd = speedTime % cfg.getSpeedTime();
		if(needCountAdd > 0){
			needCount += 1;
		}
		RewardItem.Builder sitem = cfg.getSpeedItem();
		long hasNum = this.getDataGeter().getItemNum(playerId,sitem.getItemId());
		if(hasNum >= needCount){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		List<RewardItem.Builder> costList = new ArrayList<>();
		sitem.setItemCount(hasNum);
		costList.add(sitem);
		boolean sucess = this.getDataGeter().cost(playerId, costList,  Action.STAR_INVEST_EXPLORE_SPEED_COST);
		if(!sucess){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.STAR_INVEST_EXPLORE_SPEED_REQ_VALUE, 
					Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//设置完成
		int advace = this.cellAdvance(entity);
		cell.addSpeed((int)hasNum);
		entity.notifyUpdate();
		//push
		syncActivityDataInfo(playerId);
		//Tlog
		int termId = this.getActivityTermId();
		
		this.logStarInvestExploreSpeed(playerId, termId, cellId, advace, (int) hasNum);
		return Result.success();
	}
	
	
	public Result<?> exploreBuySpeedItem(String playerId, int count, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		if(count <= 0){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<StarInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		StarInvestEntity entity = opEntity.get();
		//消耗
		StarInvestKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(StarInvestKVCfg.class);
		int limitCount = cfg.getCount();
		int alreadyCount = entity.getSpeedItemBuyCount();
		if(alreadyCount + count > limitCount){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		List<RewardItem.Builder> buyList = new ArrayList<>();
		RewardItem.Builder sitem = cfg.getSpeedItem();
		sitem.setItemCount(sitem.getItemCount() * count);
		buyList.add(sitem);
		
		List<RewardItem.Builder> costList = cfg.getSpeedItemBuyCostList();
		costList.forEach(re->re.setItemCount(re.getItemCount() * count));
		
		boolean sucess = this.getDataGeter().cost(playerId, costList,  Action.STAR_INVEST_EXPLORE_SPEED_BUY_COST);
		if(!sucess){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.STAR_INVEST_EXPLORE_SPEED_REQ_VALUE, 
					Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		entity.setSpeedItemBuyCount(alreadyCount + count);
		this.getDataGeter().takeReward(playerId, buyList, 
				Action.STAR_INVEST_EXPLORE_SPEED_BUY_ACHIEVE, true);
		this.syncActivityDataInfo(playerId);
		return Result.success();
	}
	
	
	public Result<?> getExploreRecords(String playerId,int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		int termId = this.getActivityTermId();
		String key = String.format(ActivityRedisKey.STAR_INVEST_EXPLORE_RECORD, termId,playerId);
		Map<String,String> rmap = ActivityGlobalRedis.getInstance().hgetAll(key);
		PBStarInvestExploreRecordResp.Builder builder = PBStarInvestExploreRecordResp.newBuilder();
		for(Map.Entry<String, String> entry : rmap.entrySet()){
			String reStr = entry.getValue();
			StarInvestExploreRecord record = new StarInvestExploreRecord();
			record.mergeFrom(reStr);
			PBStarInvestExploreRecord.Builder rbuilder = PBStarInvestExploreRecord.newBuilder();
			rbuilder.setBoxId(record.getBox());
			rbuilder.setTime(record.getRefreshTime());
			rbuilder.setAdvance(record.getAdvance());
			rbuilder.setRewardIndex(record.getRewardIndx());
			builder.addRecords(rbuilder);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.STAR_INVEST_EXPLORE_RECORD_RESP, builder));
		return Result.success();
	}
	
	
	
	
	/**
	 * 检查升级
	 * @param bef
	 * @param entity
	 */
	public void onExploreCellAdvance(int termId,StarInvestEntity entity){
		Map<Integer,StarInvestExploreCell> cellMap = entity.getCellMap();
		for(Map.Entry<Integer,StarInvestExploreCell> entry : cellMap.entrySet()){
			//升级了,探索中的  直接完成
			int cellId = entry.getKey();
			StarInvestExploreCell cell = entry.getValue();
			StarInvestExploreCellCfg cellCfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestExploreCellCfg.class, cellId);
			long starTime = cell.getStartTime();
			if(starTime <= 0){
				//没有 开始探索的不管
				continue;
			}
			if(cell.getBoxId() > 0){
				//已经完成了
				continue;
			}
			//探索中 并且栏位升级了，直接完成
			int advacne = this.cellAdvance(entity);
			int boxId = this.randomExploreReward(entity, cellCfg);
			cell.setStartTime(1);
			cell.setBoxId(boxId);
			cell.setBoxAdvance(advacne);
			entity.notifyUpdate();
			this.logStarInvestExploreSpeed(entity.getPlayerId(), termId, cellId, advacne, 0);
		}
	}
	
	
	public int randomExploreReward(StarInvestEntity entity,StarInvestExploreCellCfg cellCfg){
		int advacne = this.cellAdvance(entity);
		Map<StarInvestExploreBox,Integer> weightMap = new HashMap<>();
		ConfigIterator<StarInvestExploreBox> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(StarInvestExploreBox.class);
		while (configIterator.hasNext()) {
			StarInvestExploreBox boxCfg = configIterator.next();
			if(advacne == 0){
				weightMap.put(boxCfg, boxCfg.getNormalWeight());
			}
			if(advacne == 1){
				weightMap.put(boxCfg, boxCfg.getUpWeight());
			}
		}
		
		StarInvestExploreBox box = HawkRand.randomWeightObject(weightMap);
		return box.getId();
	}
	
	
	/**收割期领取奖励
	 * @param playerId
	 */
	public Result<?> getStarInvestFreeReward(String playerId, int giftId, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<StarInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		StarInvestGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestGiftCfg.class, giftId);
		if(Objects.isNull(giftCfg)){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		long curTime = HawkTime.getMillisecond();
		StarInvestEntity entity = opEntity.get();
		Map<Integer, Long> freeInfoMap = entity.getFreeInfoMap();
		long rewardTime = freeInfoMap.getOrDefault(giftCfg.getId(), 0l);
		if(HawkTime.isSameDay(curTime, rewardTime)){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		//设置领取时间
		entity.setFreeGiftRewardTime(giftCfg.getId(), curTime);
		entity.notifyUpdate();
		//发奖
		List<RewardItem.Builder> rewardItemList = giftCfg.getRewardList();
		this.getDataGeter().takeReward(playerId, rewardItemList, Action.STAR_INVEST_FREE_REWARD_ACHIEVE, true);
		int termId = getActivityTermId();
		//push
		syncActivityDataInfo(playerId);
		//Tlog
		this.logStarInvestFreeReward(playerId, termId, giftId);
		return Result.success();
	}
	
	/**收割期领取奖励
	 * @param playerId
	 */
	public Result<?> getStarInvestReward(String playerId, int giftId, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		//是否是收割期
		if (this.getActivityEntity().getState() != ActivityState.END.intValue()) {
			return Result.fail(Status.Error.STAR_INVEST_NO_TIME_LIMIT_VALUE);
		}
		Optional<StarInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		StarInvestEntity entity = opEntity.get();
		Map<Integer, Integer> buyInfoMap = entity.getBuyInfoMap();
		//未购买
		if (!buyInfoMap.containsKey(giftId)) {
			return Result.fail(Status.Error.STAR_INVEST_NO_BUY_VALUE);
		}
		//已经领过
		if (buyInfoMap.get(giftId) != PBStarInvestBuyGiftState.GITF_BUY_VALUE) {
			return Result.fail(Status.Error.STAR_INVEST_HAVE_RECEIVED_VALUE);
		}
		//奖励验证
		List<RewardItem.Builder> rewardItemList = this.getStarInvestRewardByGift(entity, giftId);
		if(rewardItemList.isEmpty()){
			return Result.fail(Status.Error.STAR_INVEST_NO_REWARD_FOR_SCORE_VALUE);
		}
		//发奖
		this.getDataGeter().takeReward(playerId, rewardItemList, Action.STAR_INVEST_RECHARGE_REWARD_ACHIEVE, true);
		buyInfoMap.put(giftId, PBStarInvestBuyGiftState.GIFT_REWARD_VALUE);
		entity.notifyUpdate();
		//push
		syncActivityDataInfo(playerId);
		//Tlog
		int termId = getActivityTermId();
		this.logStarInvestRechargeReward(playerId, termId, giftId, SerializeHelper.mapToString(entity.getDaliyTaskMap()));
		return Result.success();
	}
	
	
	@Override
	public boolean isOpening(String playerId) {
		int termId = getActivityTermId(playerId);
		long startTime = getTimeControl().getStartTimeByTermId(termId);
		long hiddenTime = getTimeControl().getHiddenTimeByTermId(termId);
		long now = HawkTime.getMillisecond();
		if (startTime > now || now >= hiddenTime) {
			return false;
		}
		return true;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<StarInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return ;
		}
		StarInvestEntity entity = opEntity.get();
		//检查一下状态
		this.checExplorekData(entity);
		PBStarInvestPageInfo.Builder builder  = genStarInvestPageInfo(entity);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.STAR_INVEST_PAGE_INFO_RESP, builder));
		
	}
	
	
	public long getExploreEndTime(StarInvestExploreCell cell,StarInvestEntity entity){
		if(Objects.isNull(cell)){
			return Long.MAX_VALUE;
		}
		//没开放
		int unlock = this.cellUnlock(cell.getCellId(), entity);
		if(unlock <= 0){
			return Long.MAX_VALUE;
		}
		//没开始
		if(cell.getStartTime() <= 0){
			return Long.MAX_VALUE;
		}
		StarInvestKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(StarInvestKVCfg.class);
		StarInvestExploreCellCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestExploreCellCfg.class, cell.getCellId());
		int advance = this.cellAdvance(entity);
		long eTime = cfg.getCommonTime() * 1000;
		if(advance >= 1){
			eTime = cfg.getAdvancedTime() * 1000;
		}
		long endTime = cell.getStartTime() + eTime - cell.getSpeed() * kvcfg.getSpeedTime() * 1000;
		return endTime;
	}
	
	
	/**
	 * 检查探索任务
	 * @param entity
	 */
	public void checExplorekData(StarInvestEntity entity){
		long curTime = HawkTime.getMillisecond();
		Map<Integer, StarInvestExploreCell> cellMap = entity.getCellMap();
		boolean update = false;
		for(StarInvestExploreCell cell : cellMap.values()){
			//没开始探测
			if(cell.getStartTime() <=0){
				continue;
			}
			StarInvestExploreCellCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestExploreCellCfg.class, cell.getCellId());
			long endTime = this.getExploreEndTime(cell, entity);
			if(curTime >= endTime && cell.getBoxId() <=0){
				int advacne = this.cellAdvance(entity);
				int boxId = this.randomExploreReward(entity, cfg);
				cell.setBoxId(boxId);
				cell.setBoxAdvance(advacne);
				update = true;
			}
		}
		if(update){
			entity.notifyUpdate();
		}
	}
	
	
	/**生成协议
	 * @param entity
	 * @return
	 */
	public PBStarInvestPageInfo.Builder genStarInvestPageInfo(StarInvestEntity entity){
		long curTime = HawkTime.getMillisecond();
		PBStarInvestPageInfo.Builder builder  = PBStarInvestPageInfo.newBuilder();
		int termId = getActivityTermId();
		StarInvestTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestTimeCfg.class, termId);
		builder.setBuyEndTime(cfg.getEndTimeValue());
		builder.setSpeedItemBuyCount(entity.getSpeedItemBuyCount());
		//每日积分
		int openDays = getActivityBetweenDays();
		for (int i = 0; i < openDays; i++) {
			int score = getScoreDays(entity, i);
			builder.addDayScore(score);
		}
		//购买和每日任务积分信息
		Map<Integer, Integer> buyInfoMap = entity.getBuyInfoMap();
		for (Entry<Integer, Integer> entry : buyInfoMap.entrySet()) {
			PBStarInvestBufGift.Builder buyFundInfo = PBStarInvestBufGift.newBuilder();
			buyFundInfo.setBuyId(entry.getKey());
			buyFundInfo.setBuyState(PBStarInvestBuyGiftState.valueOf(entry.getValue()));
			builder.addBuyGifts(buyFundInfo);
		}
		//免费礼包
		Map<Integer, Long> freeInfoMap = entity.getFreeInfoMap();
		ConfigIterator<StarInvestGiftCfg> giftIterator = HawkConfigManager.getInstance().
				getConfigIterator(StarInvestGiftCfg.class);
		while (giftIterator.hasNext()) {
			StarInvestGiftCfg giftCfg = giftIterator.next();
			int gid = giftCfg.getId();
			long rewadTime = freeInfoMap.getOrDefault(gid, 0l);
			
			PBStarInvestFreeGift.Builder fbuilder = PBStarInvestFreeGift.newBuilder();
			fbuilder.setGiftId(giftCfg.getId());
			if(HawkTime.isSameDay(curTime, rewadTime)){
				fbuilder.setBuyState(PBStarInvestFreeGfitState.FREE_TAKE);
			}else{
				fbuilder.setBuyState(PBStarInvestFreeGfitState.FREE_CAN_TAKE);
			}
			builder.addFreeGifts(fbuilder);
		}
		//探索栏位
		int advance = this.cellAdvance(entity);
		Map<Integer, StarInvestExploreCell> cellMap = entity.getCellMap();
		ConfigIterator<StarInvestExploreCellCfg> cellIterator = HawkConfigManager.getInstance().
				getConfigIterator(StarInvestExploreCellCfg.class);
		while (cellIterator.hasNext()) {
			StarInvestExploreCellCfg cellCfg = cellIterator.next();
			PBStarInvestExploreCell.Builder ebuilder = PBStarInvestExploreCell.newBuilder();
			StarInvestExploreCell cell = cellMap.get(cellCfg.getId());
			long startTime = 0;
			int boxId = 0;
			long endTime = 0;
			int boxAdvance = 0;
			int unlock = this.cellUnlock(cellCfg.getId(), entity);
			if(unlock > 0 && Objects.nonNull(cell)){
				startTime = cell.getStartTime();
				boxId = cell.getBoxId();
				boxAdvance = cell.getBoxAdvance();
				endTime = this.getExploreEndTime(cell, entity);
			}
			ebuilder.setCellId(cellCfg.getId());
			ebuilder.setLevel(unlock);
			ebuilder.setStartTime(startTime);
			ebuilder.setEndTime(endTime);
			ebuilder.setBoxId(boxId);
			ebuilder.setAdvance(boxAdvance);
			builder.addCells(ebuilder);
		}
		//栏位是否是进阶
		builder.setCellAdvance(advance);
		return builder;
	}
	
	
	public int cellAdvance(StarInvestEntity entity){
		StarInvestKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(StarInvestKVCfg.class);
		int recharge = entity.getRechargeCount();
		if(recharge >= cfg.getUpLimit()){
			return 1;
		}
		return 0;
	}
	
	public int cellUnlock(int cellId,StarInvestEntity entity){
		StarInvestExploreCellCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestExploreCellCfg.class, cellId);
		if(Objects.isNull(cfg)){
			return 0;
		}
		int buyCount = entity.getBuyInfoMap().size();
		if(buyCount >= cfg.getOpenLimit()){
			return 1;
		}
		return 0;
	}
	
	
	/**获取奖励
	 * @param entity
	 * @param giftId
	 * @return
	 */
	public List<RewardItem.Builder> getStarInvestRewardByGift(StarInvestEntity entity, int giftId){
		StarInvestMenuCfg menuCfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestMenuCfg.class, giftId);
		List<Integer> socreList = menuCfg.getDailyScoreList();
		List<RewardItem.Builder> allRewardList = new ArrayList<>();
		
		ConfigIterator<StarInvestRewardCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(StarInvestRewardCfg.class);
		while (achieveIterator.hasNext()) {
			StarInvestRewardCfg cfg = achieveIterator.next();
			if (cfg.getPool() == giftId) {
				//判断三个档位的条件
				int conditionScore = socreList.get(0); //100
				int conditionScore1 = socreList.get(1);//300
				int conditionScore2 = socreList.get(2);//500
				int score = getScoreDays(entity, cfg.getDay() - 1); //库里存的0-6 表配的1-7
				if (score >= conditionScore) {
					allRewardList.addAll(cfg.getRewardFirst());
				}
				if(score >= conditionScore1){
					allRewardList.addAll(cfg.getRewardSecond());
				} 
				if(score >= conditionScore2){
					allRewardList.addAll(cfg.getRewardThird());
				}
			}
		}
		return allRewardList;
	}
	
	
	/**获取改档位积分满足的天数
	 * @param entity
	 * @param conditionScore
	 * @return
	 */
	public int getScoreDays(StarInvestEntity entity, int dayth){
		Map<Integer,Integer> daliyMap = entity.getDaliyTaskMap();
		if (!daliyMap.containsKey(dayth)) {
			return 0;
		}
		return daliyMap.get(dayth);
	}
	
	/**获取活动时间天数(积分结算期)
	 * @return
	 */
	public int getActivityBetweenDays(){
		int termId = getActivityTermId();
		StarInvestTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestTimeCfg.class, termId);
		long startTime = cfg.getStartTimeValue();
		long endTime = cfg.getEndTimeValue();
		//活动开几天
		int betweenDays = HawkTime.calcBetweenDays(new Date(startTime), new Date(endTime)) + 1;
		return betweenDays;
	}
	
	
	/**
	 * 直购礼包支付完成
	 * @param event
	 */
	@Subscribe
	public void onStarInvestGiftBuy(StarInvestGiftBuyEvent event){
		int termId = this.getActivityTermId();
		if(termId <= 0){
			return;
		}
		String playerId = event.getPlayerId();
		String payforId = event.getGiftId();
		int giftId = StarInvestMenuCfg.getGiftId(payforId);
		StarInvestMenuCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(StarInvestMenuCfg.class, giftId);
		if(giftCfg == null){
			return;
		}
		Optional<StarInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		StarInvestEntity entity = opEntity.get();
		//检查下探索
		this.checExplorekData(entity);
		int rechargeCount = entity.getRechargeCount() + event.getRmb();
		int advanceBef = this.cellAdvance(entity);
		//修改数据
		entity.getBuyInfoMap().put(giftId, PBStarInvestBuyGiftState.GITF_BUY_VALUE);
		entity.setRechargeCount(rechargeCount);
		entity.notifyUpdate();
		//发送奖励
		List<RewardItem.Builder> rewardList = giftCfg.getBuyAwardList();
		this.getDataGeter().takeReward(playerId, rewardList, Action.STAR_INVEST_RECHARGE_GIFT, true);
		//发送奖励收据
		sendRewardByMail(playerId, rewardList,giftCfg.getMailId());
		//检查栏位等级变化
		int advanceAft = this.cellAdvance(entity);
		if(advanceAft > advanceBef){
			this.onExploreCellAdvance(termId, entity);
		}
		//同步
		this.syncActivityDataInfo(playerId);
		HawkLog.logPrintln("onStarInvestGiftBuy success playerId:{},payforId:{},giftId:{}", playerId,payforId,giftId);
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
	
	
	@Subscribe
	public void onEvent(AddTavernScoreEvent event){
		String playerId = event.getPlayerId();
		if (!super.isOpening(playerId)) {
			return;
		}
		//是否是活动期内
		if (this.getActivityEntity().getState() != ActivityState.OPEN.intValue()) {
			return;
		}
		Optional<StarInvestEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		StarInvestEntity entity = optional.get();
		int totalScore = event.getTotalScore(); 
		//更新每日任务积分
		Map<Integer,Integer> daliyMap = entity.getDaliyTaskMap();
		//活动开启的第几天
		long termOpenTime = getActivityType().getTimeControl().getShowTimeByTermId(entity.getTermId(), playerId);
		int dayth = HawkTime.calcBetweenDays(new Date(termOpenTime), new Date(HawkTime.getMillisecond()));
		daliyMap.put(dayth, totalScore);
		entity.notifyUpdate();
		//push
		syncActivityDataInfo(playerId);
		
		HawkLog.logPrintln("starInvestActivity AddTavernScoreEvent playerId:{}, dayth:{}, totalScore:{}", playerId, dayth, totalScore);
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
        Optional<StarInvestEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        StarInvestEntity entity = opEntity.get();
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
 		List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
 		// 初始添加成就项
 		ConfigIterator<StarInvestAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(StarInvestAchieveCfg.class);
 		while (configIterator.hasNext()) {
 			StarInvestAchieveCfg next = configIterator.next();
 			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
 			itemList.add(item);
 		}
 		entity.setItemList(itemList);
 		// 初始化成就数据
 		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), entity.getItemList()), true);
        //给客户端同步数据
        this.syncActivityDataInfo(playerId);
        HawkLog.logPrintln("onStarInvestContinueLoginEvent reset playerId:{}", playerId);
	}
	

	
	public void addExploreRecord(String playerId,long time,int boxId,int rewardIndex,int advance){
		int termId = this.getActivityTermId();
		String key = String.format(ActivityRedisKey.STAR_INVEST_EXPLORE_RECORD, termId,playerId);
		StarInvestExploreRecord record = StarInvestExploreRecord.valueOf(boxId, rewardIndex, advance, time);
		ActivityGlobalRedis.getInstance().hset(key, record.getId(), record.serializ(), (int)TimeUnit.DAYS.toSeconds(30));
	}
	 
    /**
     * 星海投资-投资充值奖励领取
     * @param playerId
     * @param termId
     * @param giftId
     * @param score
     */
    private void logStarInvestRechargeReward(String playerId,int termId,int giftId, String score){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId); //期数
        param.put("giftId", giftId); //礼包ID
        param.put("score", score);   //积分情况
        getDataGeter().logActivityCommon(playerId, LogInfoType.star_invest_recharge_reward, param);
    }
    
    /**
     * 星海投资-投资免费奖励领取
     * @param playerId
     * @param termId
     * @param giftId
     * @param score
     */
    private void logStarInvestFreeReward(String playerId,int termId,int giftId){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId); //期数
        param.put("giftId", giftId); //礼包ID
        getDataGeter().logActivityCommon(playerId, LogInfoType.star_invest_free_reward, param);
    }
    
    
    /**
     * 星海投资-探测开始
     * @param playerId
     * @param termId
     * @param giftId
     * @param score
     */
    private void logStarInvestExploreStart(String playerId,int termId,int cellId,int cellLevel){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId); //期数
        param.put("cellId", cellId); //栏位ID
        param.put("cellLevel", cellLevel); //栏位等级
        getDataGeter().logActivityCommon(playerId, LogInfoType.star_invest_explore_start, param);
    }
    
    
    /**
     * 星海投资-探索加速
     * @param playerId
     * @param termId
     * @param giftId
     * @param score
     */
    private void logStarInvestExploreSpeed(String playerId,int termId,int cellId,int cellLevel,int action){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId); //期数
        param.put("cellId", cellId); //栏位ID
        param.put("cellLevel", cellLevel); //栏位等级
        param.put("action", action); //原因 0栏位升级    n使用道具 
        getDataGeter().logActivityCommon(playerId, LogInfoType.star_invest_explore_speed, param);
    }
    
	
    
    
    /**
     * 星海投资-探索领奖
     * @param playerId
     * @param termId
     * @param giftId
     * @param score
     */
    private void logStarInvestExploreReward(String playerId,int termId,int cellId,int cellLevel,int rewardId){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId); //期数
        param.put("cellId", cellId); //栏位ID
        param.put("cellLevel", cellLevel); //栏位等级
        param.put("rewardId", rewardId); //原因 1使用道具  2栏位升级
        getDataGeter().logActivityCommon(playerId, LogInfoType.star_invest_explore_reward, param);
    }
}
