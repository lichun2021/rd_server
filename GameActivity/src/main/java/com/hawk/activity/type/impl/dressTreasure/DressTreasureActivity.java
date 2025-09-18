package com.hawk.activity.type.impl.dressTreasure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.dressTreasure.cfg.DressTreasureAwardCfg;
import com.hawk.activity.type.impl.dressTreasure.cfg.DressTreasureExchangeCfg;
import com.hawk.activity.type.impl.dressTreasure.cfg.DressTreasureKVCfg;
import com.hawk.activity.type.impl.dressTreasure.cfg.DressTreasureRandomRangeCfg;
import com.hawk.activity.type.impl.dressTreasure.entity.DressTreasureEntity;
import com.hawk.game.protocol.Activity.DressTreasureExchangeItem;
import com.hawk.game.protocol.Activity.DressTreasureInfo;
import com.hawk.game.protocol.Activity.DressTreasureInfoResp;
import com.hawk.game.protocol.Activity.DressTreasureRandomResp;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 端午兑换
 * @author che
 *
 */
public class DressTreasureActivity extends ActivityBase implements IExchangeTip<DressTreasureExchangeCfg> {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public DressTreasureActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DRESS_TREASURE_ACTIVITY;
	}

	
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DressTreasureActivity activity = new DressTreasureActivity(
				config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DressTreasureEntity> queryList = HawkDBManager.getInstance()
				.query("from DressTreasureEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DressTreasureEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DressTreasureEntity entity = new DressTreasureEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.DRESS_TREASURE_INIT, () -> {
				initData(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<DressTreasureEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	
	@Override
	public void onPlayerLogin(String playerId) {
		this.initData(playerId);
	}
	
	/**
	 * 初始化数据
	 * @param entity
	 */
	public void initData(String playerId){
		Optional<DressTreasureEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		DressTreasureEntity entity = opDataEntity.get();
		if(entity.getRandomId() == 0){
			this.resetRandomData(entity,true);
		}
	}
	
	
	/**
	 * 重置随机数据
	 * @param entity
	 */
	private void resetRandomData(DressTreasureEntity entity,boolean resetRandomId){
		int randomId = 0;
		if(resetRandomId){
			randomId =  1;
			entity.clearAwards();
		}else{
			randomId = this.getRandomCfgId(entity);
		}
		DressTreasureRandomRangeCfg kvCfg = HawkConfigManager.getInstance().
				getConfigByKey(DressTreasureRandomRangeCfg.class, randomId);
		entity.setRandomId(randomId);
		entity.setAwardScoreFrom(kvCfg.getRangeStart());
		entity.setAwardScoreTo(kvCfg.getRangeEnd());
		
	}
	
	
	/**
	 * 投色子
	 * @param playerId
	 */
	public void randomAward(String playerId){
		Optional<DressTreasureEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		DressTreasureEntity entity = opDataEntity.get();
		//如果已经结束
		if(this.randomAwardOver(entity)){
			return;
		}
		int termId = this.getActivityTermId();
		DressTreasureKVCfg kvCfg =  HawkConfigManager.getInstance().getKVInstance(DressTreasureKVCfg.class);
		DressTreasureRandomRangeCfg randomCfg =  HawkConfigManager.getInstance().
				getConfigByKey(DressTreasureRandomRangeCfg.class, entity.getRandomId());
		//检查消耗
		boolean costRlt = this.getDataGeter().consumeItems(playerId, randomCfg.getRandomCostList(), 1, Action.DRESS_TREASURE_RANDOM_COST);
		if(!costRlt){
			return;
		}
		int random1 = HawkRand.randInt(kvCfg.getRandomStart(), kvCfg.getRandomEnd());
		int random2 = HawkRand.randInt(kvCfg.getRandomStart(), kvCfg.getRandomEnd());
		
		int randmom = random1 + random2;
		int awardScoreFrom = entity.getAwardScoreFrom();
		int awardScoreTo = entity.getAwardScoreTo();
		boolean award = (awardScoreFrom <= randmom && randmom <= awardScoreTo);
		if(award){
			int awardId = this.randomAwardId(entity.getAwardList());
			DressTreasureAwardCfg cfg = HawkConfigManager.getInstance().
					getConfigByKey(DressTreasureAwardCfg.class, awardId);
			if(cfg == null){
				return;
			}
			//奖励
			List<RewardItem.Builder> awardList = new ArrayList<>();
			List<RewardItem.Builder> randomAwards = cfg.getAwardList();
			List<RewardItem.Builder> scoreAwards = this.getExchangeScoreItems(randomCfg.getRandomCostList());
			awardList.addAll(randomAwards);
			awardList.addAll(scoreAwards);
			//发奖
			this.getDataGeter().takeReward(playerId, awardList, Action.DRESS_TREASURE_RANDOM_AWARD, false);
			//记录奖励ID
			entity.addAwardId(cfg.getId());
			//重置奖池
			if(this.awardAchiveOver(entity)){
				//如果有次数就重置
				int resetCount = entity.getResetCount();
				if(resetCount < kvCfg.getResetCountLimit()){
					this.resetRandomData(entity,true);
					entity.setResetCount(resetCount + 1);
				}
			}else{
				this.resetRandomData(entity, false);
			}
			//同步
			DressTreasureRandomResp.Builder builder = DressTreasureRandomResp.newBuilder();
			builder.setAwardId(awardId);
			for(RewardItem.Builder awardItem : awardList){
				builder.addRewards(awardItem.clone());
			}
			builder.setRandomFirst(random1);
			builder.setRandomSecond(random2);
			DressTreasureInfo.Builder dtBuilder = this.createDressTreasureInfoBuilder(entity);
			builder.setTreasureInfo(dtBuilder);
			PlayerPushHelper.getInstance().pushToPlayer(playerId,
					HawkProtocol.valueOf(HP.code2.DRESS_TREASURE_RANDOM_RESP, builder));
			this.getDataGeter().logDressTreasureRandom(playerId, termId, random1, random2, 
					awardScoreFrom, awardScoreTo, randomCfg.getId(), awardId,randomCfg.getRandomCost());
			
		}else{
			//奖励
			List<RewardItem.Builder> scoreAwards = this.getExchangeScoreItems(randomCfg.getRandomCostList());
			//发奖
			this.getDataGeter().takeReward(playerId, scoreAwards, Action.DRESS_TREASURE_RANDOM_AWARD, false);
			//扩展中奖范围
			this.awardRandomExtend(entity);
			//同步
			DressTreasureRandomResp.Builder builder = DressTreasureRandomResp.newBuilder();
			builder.setAwardId(0);
			for(RewardItem.Builder awardItem : scoreAwards){
				builder.addRewards(awardItem.clone());
			}
			builder.setRandomFirst(random1);
			builder.setRandomSecond(random2);
			DressTreasureInfo.Builder dtBuilder = this.createDressTreasureInfoBuilder(entity);
			builder.setTreasureInfo(dtBuilder);
			PlayerPushHelper.getInstance().pushToPlayer(playerId,
					HawkProtocol.valueOf(HP.code2.DRESS_TREASURE_RANDOM_RESP, builder));
			this.getDataGeter().logDressTreasureRandom(playerId, termId, random1, random2, 
					awardScoreFrom, awardScoreTo, randomCfg.getId(), 0, randomCfg.getRandomCost());
			
		}
		
	}
	
	
	private List<RewardItem.Builder> getExchangeScoreItems(List<RewardItem.Builder> cost){
		int baseVal = 0;
		for (RewardItem.Builder costItem : cost) {
			if (costItem.getItemId() == PlayerAttr.DIAMOND_VALUE) {
				baseVal += costItem.getItemCount();
			}
		}
		DressTreasureKVCfg kvCfg =  HawkConfigManager.getInstance().getKVInstance(DressTreasureKVCfg.class);
		List<RewardItem.Builder> scoreItems = kvCfg.getScoreAddItemList();
		for (RewardItem.Builder scoreItem : scoreItems) {
			long count = scoreItem.getItemCount() * baseVal;
			scoreItem.setItemCount(count);
		}
		return scoreItems;
	}
	
	private int getRandomCfgId(DressTreasureEntity entity){
		int randomId = entity.getRandomId() + 1;
		int count = HawkConfigManager.getInstance().getConfigSize(DressTreasureRandomRangeCfg.class);
		if(randomId > count){
			randomId = 1;
		}
		return randomId;
	}
	/**
	 * 随机奖励
	 * @param ids
	 * @return
	 */
	private int randomAwardId(List<Integer> ids){
		List<DressTreasureAwardCfg> list = new ArrayList<>();
		List<DressTreasureAwardCfg> cfgs = HawkConfigManager.getInstance().
				getConfigIterator(DressTreasureAwardCfg.class).toList();
		for(DressTreasureAwardCfg cfg : cfgs){
			if(ids.contains(cfg.getId())){
				continue;
			}
			list.add(cfg);
		}
		DressTreasureAwardCfg award = HawkRand.randomWeightObject(list);
		return award.getId();
	}
	
	
	/**
	 * 扩展中奖范围
	 * @param entity
	 */
	private void awardRandomExtend(DressTreasureEntity entity){
		boolean extRight = false;
		DressTreasureKVCfg kvCfg =  HawkConfigManager.getInstance().getKVInstance(DressTreasureKVCfg.class);
		if(kvCfg.getAwardPoolRandomStart() < entity.getAwardScoreFrom() &&  entity.getAwardScoreTo() <
				kvCfg.getAwardPoolRandomEnd()){
			int extRandom = HawkRand.randInt(1, 100);
			if(extRandom > 50){
				extRight = true;
			}
		} else if(kvCfg.getAwardPoolRandomStart() >= entity.getAwardScoreFrom() &&  entity.getAwardScoreTo() <
				kvCfg.getAwardPoolRandomEnd()){
			extRight = true;
		}
		if(extRight){
			int to = entity.getAwardScoreTo() + kvCfg.getExtendCount();
			entity.setAwardScoreTo(Math.min(kvCfg.getAwardPoolRandomEnd(), to));
		}else{
			int from = entity.getAwardScoreFrom() - kvCfg.getExtendCount();
			entity.setAwardScoreFrom(Math.max(kvCfg.getAwardPoolRandomStart(), from));
		}
	}
	
	/**
	 * 是否已经拿完奖励
	 * @param entity
	 * @return
	 */
	private boolean awardAchiveOver(DressTreasureEntity entity){
		int size = HawkConfigManager.getInstance().getConfigSize(DressTreasureAwardCfg.class);
		if(entity.getAwardList().size() >= size){
			return true;
		}
		return false;
	}
	
	public boolean randomAwardOver(DressTreasureEntity entity){
		boolean awardOver = this.awardAchiveOver(entity);
		DressTreasureKVCfg kvCfg =  HawkConfigManager.getInstance().getKVInstance(DressTreasureKVCfg.class);
		int resetCount = entity.getResetCount();
		if(awardOver && resetCount >= kvCfg.getResetCountLimit()){
			return true;
		}
		return false;
	}
	
	/**
	 * 重置奖励池
	 * @param playerId
	 */
	public void restRadomAward(String playerId,int protoType){
		Optional<DressTreasureEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		DressTreasureEntity entity = opDataEntity.get();
		DressTreasureKVCfg kvCfg =  HawkConfigManager.getInstance().getKVInstance(DressTreasureKVCfg.class);
		int resetCount = entity.getResetCount();
		if(resetCount >= kvCfg.getResetCountLimit()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.DRESS_TREASURE_RESET_REQ_VALUE,
					Status.Error.DRESS_TREASURE_RERST_COUNT_LIMIT_VALUE);
			return;
		}
		int termId = this.getActivityTermId();
		if(entity.getAwardCount() <= 0){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.DRESS_TREASURE_RESET_REQ_VALUE,
					Status.Error.DRESS_TREASURE_RERST_LIMIT_VALUE);
			return;
		}
		//检查消耗
		boolean costRlt = this.getDataGeter().consumeItems(playerId, kvCfg.getResetCostItemList(), protoType, Action.DRESS_TREASURE_RESET_COST);
		if(!costRlt){
			return;
		}
		entity.setResetCount(resetCount + 1);
		this.resetRandomData(entity,true);
		
		this.syncActivityInfo(playerId, entity);
		this.getDataGeter().logDressTreasureRest(playerId, termId, entity.getRandomId(), entity.getAwardScoreFrom(), entity.getAwardScoreTo());
	}
	
	

	
	/**
	 * 道具兑换
	 * @param playerId
	 * @param protolType
	 */
	public void itemExchange(String playerId,int exchangeId,int exchangeCount,int protocolType){
		DressTreasureExchangeCfg config = HawkConfigManager.getInstance().
				getConfigByKey(DressTreasureExchangeCfg.class, exchangeId);
		if (config == null) {
			return;
		}
		Optional<DressTreasureEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		DressTreasureEntity entity = opDataEntity.get();
		int eCount = entity.getExchangeCount(exchangeId);
		if(eCount + exchangeCount > config.getTimes()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.Error.DRESS_TREASURE_EXCHANGE_COUNT_LIMIT_VALUE);
			logger.info("DressTreasureActivity,itemExchange,fail,countless,playerId: "
					+ "{},exchangeType:{},ecount:{}", playerId,exchangeId,eCount);
			return;
		}
		
		List<RewardItem.Builder> makeCost = config.getNeedItemList();
		boolean cost = this.getDataGeter().cost(playerId,makeCost, exchangeCount, Action.DRESS_TREASURE_EXCAHNGE_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		
		//增加兑换次数
		entity.addExchangeCount(exchangeId, exchangeCount);
		//发奖励
		this.getDataGeter().takeReward(playerId, config.getGainItemList(), 
				exchangeCount, Action.DRESS_TREASURE_EXCAHNGE_GAIN, true);
		//同步
		this.syncActivityInfo(playerId,entity);
		logger.info("DressTreasureActivity,itemExchange,sucess,playerId: "
				+ "{},exchangeType:{},ecount:{}", playerId,exchangeId,eCount);
		
	}
	
	
	
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId,DressTreasureEntity entity){
		
		DressTreasureInfoResp.Builder builder = DressTreasureInfoResp.newBuilder();
		DressTreasureInfo.Builder dtBuilder = this.createDressTreasureInfoBuilder(entity);
		builder.setTreasureInfo(dtBuilder);
		builder.addAllTips(getTips(DressTreasureExchangeCfg.class, entity.getTipSet()));
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.DRESS_TREASURE_INFO_RESP, builder));
		
	}
	
	/**
	 * 创建同步信息
	 * @param entity
	 * @return
	 */
	public DressTreasureInfo.Builder createDressTreasureInfoBuilder(DressTreasureEntity entity){
		DressTreasureInfo.Builder builder = DressTreasureInfo.newBuilder();
		DressTreasureRandomRangeCfg randomCfg =  HawkConfigManager.getInstance().
				getConfigByKey(DressTreasureRandomRangeCfg.class, entity.getRandomId());
		for(int awadId : entity.getAwardList()){
			builder.addAwardList(awadId);
		}
		builder.setAwardRandomStart(entity.getAwardScoreFrom());
		builder.setAwardRandomEnd(entity.getAwardScoreTo());
		builder.setRandomCost(randomCfg.getRandomCost());
		
		for(Map.Entry<Integer, Integer> entry : entity.getExchangeNumMap().entrySet()){
			DressTreasureExchangeItem.Builder kvBuilder = DressTreasureExchangeItem.newBuilder();
			kvBuilder.setEchangeId(entry.getKey());
			kvBuilder.setEchangeCount(entry.getValue());
			builder.addExhanges(kvBuilder);
		}
		builder.setResetCount(entity.getResetCount());
		return builder;
	}
	
}
