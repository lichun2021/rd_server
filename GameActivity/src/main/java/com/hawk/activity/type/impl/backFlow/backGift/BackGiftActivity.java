package com.hawk.activity.type.impl.backFlow.backGift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.backFlow.backGift.cfg.BackGfitDateCfg;
import com.hawk.activity.type.impl.backFlow.backGift.cfg.BackGiftKVCfg;
import com.hawk.activity.type.impl.backFlow.backGift.cfg.BackGiftLotteryCfg;
import com.hawk.activity.type.impl.backFlow.backGift.cfg.BackGiftLotteryWeightCfg;
import com.hawk.activity.type.impl.backFlow.backGift.cfg.BackGiftTimeCfg;
import com.hawk.activity.type.impl.backFlow.backGift.entity.BackGiftEntity;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.game.protocol.Activity.BackGiftInfoResp;
import com.hawk.game.protocol.Activity.BackGiftLotteryResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.log.Action;

/***
 * 回归大礼活动
 * @author che
 *
 */
public class BackGiftActivity extends ActivityBase {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	
	public BackGiftActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public void onPlayerLogin(String playerId) {
		
		Optional<BackGiftEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		BackGiftEntity entity = optional.get();
		BackFlowPlayer backFlowPlayer = this.getDataGeter().getBackFlowPlayer(playerId);
		if(backFlowPlayer == null){
			return;
		}
		//检查新开活动
		if(this.checkFitLostParams(backFlowPlayer,entity)){
			int backTimes = backFlowPlayer.getBackCount();
			BackGfitDateCfg dataCfg = this.getBackGfitDateCfg(backFlowPlayer);
			long startTime = HawkTime.getAM0Date(
					new Date(backFlowPlayer.getBackTimeStamp())).getTime();
			long continueTime = 0;
			int backType = 0;
			if(dataCfg != null){
				continueTime = dataCfg.getDuration() * HawkTime.DAY_MILLI_SECONDS - 1000;
				backType = dataCfg.getId();
			}
			long overTime = startTime + continueTime;
			entity.setBackCount(backTimes);
			entity.setBackType(backType);
			entity.setStartTime(startTime);
			entity.setOverTime(overTime);
			entity.setLossDays(backFlowPlayer.getLossDays());
			entity.setLossVip(backFlowPlayer.getVipLevel());
			initLottery(entity);
			entity.notifyUpdate();
			logger.info("onPlayerLogin  checkFitLostParams init sucess,  playerId: "+ 
					"{},backCount:{},backType:{},backTime:{},startTime:{}.overTime:{}", 
					playerId,backTimes,backType,backFlowPlayer.getBackTimeStamp(),startTime,overTime);
			return;
		}
	}
	
	
	/**
	 * 是否可以触发
	 * @return
	 */
	public boolean canTrigger(long backTime){
		int termId = this.getActivityTermId();
		BackGiftTimeCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(BackGiftTimeCfg.class, termId);
		if(cfg == null){
			return false;
		}
		if(backTime < cfg.getStartTimeValue()){
			return false;
		}
		if(backTime > cfg.getStopTriggerValue()){
			return false;
		}
		return true;
	}
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		if(this.isHidden(playerId)){
			return;
		}
		Optional<BackGiftEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		BackGiftEntity entity = optional.get();
		int totalCount = entity.getLotteryTotalCount();
		this.initLottery(entity);
		entity.setLotteryTotalCount(totalCount);
		//同步消息
		this.syncActivityDataInfo(playerId);
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<BackGiftEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		BackGiftEntity entity = opDataEntity.get();
		this.syncActivityInfo(playerId, entity);
	}
	
	
	/**
	 * 检查参数
	 * @param backFlowPlayer
	 * @param entity
	 * @return
	 */
	public boolean checkFitLostParams(BackFlowPlayer backFlowPlayer,BackGiftEntity entity) {
		if(backFlowPlayer.getBackCount() <= entity.getBackCount()){
			logger.info("checkFitLostParams failed, BackCount data fail , playerId: "
					+ "{},backCount:{},entityBackCount:{}", backFlowPlayer.getPlayerId(),
					backFlowPlayer.getBackCount(),entity.getBackCount());
			return false;
		}
		long backTime = backFlowPlayer.getBackTimeStamp();
		//如果在活动中，只更新期数，不更新其他数据
		if(backTime < entity.getOverTime() && backTime > entity.getStartTime()){
			entity.setBackCount(backFlowPlayer.getBackCount());
			entity.notifyUpdate();
			logger.info("checkFitLostParams failed,in activity, playerId: "
					+ "{},backCount:{},backTime:{}", entity.getPlayerId(),entity.getBackCount(),backTime);
			return false;
		}
		//停止触发，只更新期数，不更新其他数据
		if(!this.canTrigger(backTime)){
			entity.setBackCount(backFlowPlayer.getBackCount());
			entity.notifyUpdate();
			logger.info("checkFitLostParams failed,can not Trigger, playerId: "
					+ "{},backCount:{},backTime:{}", entity.getPlayerId(),entity.getBackCount(),backTime);
			return false;
		}
		int lossDays = backFlowPlayer.getLossDays();
		logger.info("checkFitLostParams sucess,playerId: "
				+ "{},loss:{}", backFlowPlayer.getPlayerId(),lossDays);
		return true;
	}
	
	/**
	 * 刷新转盘奖励
	 * @param playerId
	 */
	public void giftRefresh(String playerId){
		if(this.isHidden(playerId)){
			return;
		}
		Optional<BackGiftEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		BackGiftEntity entity = optional.get();
		int refreshCount = entity.getRefreshCount();
		BackGiftKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BackGiftKVCfg.class);
		if(refreshCount >= cfg.getResetLimit()){
			logger.info("giftRefresh failed, refreshCount not enough , playerId: "
					+ "{},refreshCount:{},entityRefreshCount{},confg:{}", playerId,
					refreshCount,entity.getRefreshCount(),cfg.getResetLimit());
			return;
		}
		long curTime = HawkTime.getMillisecond();
		long refreshTime = this.getRefreshCdTime(entity);
		if(refreshTime > 0){
			logger.info("giftRefresh failed, refresh in cd, playerId: "
					+ "{},curTime:{},refreshTime{},confg:{}", playerId,
					curTime,refreshTime,cfg.getResetLimitCd());
			return;
		}
		logger.info("giftRefresh befor, playerId: "+ "{},gift:{}", playerId,entity.getAwardList());
		//重置转盘奖励
		List<BackGift> giftList = this.randomGfit(entity);
		entity.setRefreshTime(curTime);
		entity.setRefreshCount(refreshCount + 1);
		entity.setAwardIndex(0);
		entity.resetAwardList(giftList);
		entity.notifyUpdate();
		this.syncActivityDataInfo(playerId);
		logger.info("giftRefresh after, playerId: "+ "{},gift:{}", playerId,entity.getAwardList());
		
	}
	
	
	
	/**
	 * 抽奖
	 * @param playerId
	 */
	public void lottery(String playerId){
		if(this.isHidden(playerId)){
			return;
		}
		Optional<BackGiftEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		BackGiftEntity entity = optional.get();
		BackGiftKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackGiftKVCfg.class);
		boolean isFree = true;
		if(this.getLotteryFreeTimes(entity) <= 0){
			isFree =false;
			List<RewardItem.Builder> costList = new ArrayList<RewardItem.Builder>();
			Reward.RewardItem.Builder costItem = RewardHelper.toRewardItem(kvCfg.getLotteryCostItem());
			costList.add(costItem);
			boolean consumeResult = this.getDataGeter().consumeItems
					(playerId, costList, HP.code.CHRONO_GIFT_UNLOCK_REQ_VALUE, Action.CHRONO_GIFT_UNLOCK_COST);
			if(!consumeResult){
				return;
			}
		}
		int lotteryTimeDay = entity.getLotteryCount() + 1;
		int lotteryTimeTotal = entity.getLotteryTotalCount() + 1;
		logger.info("lottery action,  playerId:{},lotteryCountDay:{},lotteryCountTotal:{},free:{}",
				playerId,lotteryTimeDay,lotteryTimeTotal,isFree);
		Map<Integer,BackGiftLotteryCfg> cfgMap = this.getLotteryConfigs(entity);
		List<BackGiftLotteryWeightCfg> lotteryCfg = HawkConfigManager.getInstance().
				getConfigIterator(BackGiftLotteryWeightCfg.class).toList();
		BackGiftLotteryWeightCfg weightCfg = null;
		for(BackGiftLotteryWeightCfg cfg : lotteryCfg){
			if(cfg.getLotteryNumList().contains(lotteryTimeTotal)){
				weightCfg = cfg;
			}
		}
		logger.info("BackGiftLotteryWeightCfg in lotteryCount,  playerId: "+ 
				"{},lotteryCount:{}"+ "cfg:{}", playerId,lotteryTimeTotal,weightCfg==null?0:weightCfg.getId());
		if(weightCfg == null){
			weightCfg = this.randomLotteryWeight(lotteryCfg);
		}
		logger.info("BackGiftLotteryWeightCfg randomLotteryWeight,  playerId: "+ 
				"{},lotteryCount:{}"+ "cfg:{}", playerId,lotteryTimeTotal,weightCfg.getId());
		BackGiftLotteryCfg cfg = cfgMap.get(weightCfg.getType());
		List<BackGift> chooseList = new ArrayList<BackGift>();
		for(BackGift gift : entity.getAwardList()){
			if(cfg != null && gift.getPoolType() == cfg.getType()){
				chooseList.add(gift);
			}
		}
		logger.info("BackGiftLotteryWeightCfg choseList,  playerId: "+ 
				"{},chooseList:{}"+ "cfgType:{}", playerId,chooseList.size(),weightCfg.getType());
		if(chooseList.size() == 0){
			chooseList.addAll(entity.getAwardList());
		}
		logger.info("BackGiftLotteryWeightCfg choseList add,  playerId: "+ 
				"{},chooseList:{}", playerId,chooseList.size());
		int randonIndex = HawkRand.randInt(chooseList.size()-1);
		logger.info("BackGiftLotteryWeightCfg randonIndex,  playerId: "+ 
				"{},randonIndex:{}", playerId,randonIndex);
		BackGift gift = chooseList.get(randonIndex);
		int giftIndex = 0;
		for(int i=0;i< entity.getAwardList().size();i++){
			BackGift choosegift = entity.getAwardList().get(i);
			if(choosegift.getItemId() == gift.getItemId()){
				giftIndex = i;
			}
		}
		List<RewardItem.Builder> rlist = new ArrayList<>();
		rlist.add(gift.getRewardBuilder());
		long curTime = HawkTime.getMillisecond();
		entity.setAwardIndex(giftIndex + 1);
		entity.addLotteryCount();
		if(isFree){
			//如果是免费抽奖就更新抽奖时间,消耗道具不更新免费抽奖时间
			entity.setLotteryTime(curTime);
		}
		entity.notifyUpdate();
		ActivityReward reward = new ActivityReward(rlist, Action.BACK_GIFT_LOTTERY_REWARD);
		reward.setOrginType(null, getActivityId());
		reward.setAlert(false);
		postReward(playerId, reward, false);
		
		BackGiftLotteryResp.Builder lbuilder = BackGiftLotteryResp.newBuilder();
		lbuilder.setItemIdex(randonIndex +1);
		int termId = this.getActivityTermId();
		this.getDataGeter().logBackGiftLottery(playerId, termId, entity.getBackCount(), isFree?1:0, entity.getLotteryCount());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.BACK_GIFT_LOTTERY_RESP_VALUE, lbuilder));
		this.syncActivityDataInfo(playerId);
	}
	
	/**
	 * 初始化
	 * @param backFlowPlayer
	 * @param entity
	 */
	public void initLottery(BackGiftEntity entity){
		//重置抽奖次数
		entity.setLotteryCount(0);
		entity.setLotteryTotalCount(0);
		entity.setLotteryTime(0);
		//重置刷新次数
		entity.setRefreshCount(0);
		entity.setRefreshTime(0);
		//清空抽奖记录
		entity.setAwardIndex(0);
		//重置转盘奖励
		List<BackGift> giftList = this.randomGfit(entity);
		entity.resetAwardList(giftList);
	} 
	
	
	
	
	/**
	 * 初始化转盘奖励
	 * @param backFlowPlayer
	 * @param entity
	 */
	private List<BackGift> randomGfit(BackGiftEntity entity){
		Map<Integer,BackGiftLotteryCfg> cfgs = this.getLotteryConfigs(entity);
		List<BackGiftLotteryCfg> cfgList = new ArrayList<BackGiftLotteryCfg>(cfgs.values());
		Collections.sort(cfgList, new Comparator<BackGiftLotteryCfg>() {
			@Override
			public int compare(BackGiftLotteryCfg arg0, BackGiftLotteryCfg arg1) {
				return arg0.getType() < arg1.getType()?-1:1 ;
			}
		});
		List<BackGift> awardList = new CopyOnWriteArrayList<BackGift>();
		int totalNum = cfgList.get(cfgList.size() -1).getLotteryNumEnd();
		int defaultNum = 0;
		for(int i=0;i<cfgList.size();i++){
			BackGiftLotteryCfg cfg = cfgList.get(i);
			if(i == cfgList.size() -1){
				//最后一个奖励池，随出的奖励个数
				defaultNum = totalNum;
			}
			List<BackGift>  ranList = this.getRandomAward(cfg, totalNum,defaultNum);
			totalNum -= ranList.size();
			awardList.addAll(ranList);
			if(totalNum == 0){
				break;
			}
		}
		return awardList;
		
	}
	
	
	
	
	
	/**
	 * 随机奖励
	 * @param cfg
	 * @param numLimit
	 * @param defaultNum
	 * @return
	 */
	public List<BackGift> getRandomAward(BackGiftLotteryCfg cfg,int numLimit,int defaultNum){
		List<BackGift> awards = new ArrayList<BackGift>();
		List<RewardItem.Builder> list = new ArrayList<RewardItem.Builder>();
		list.addAll(cfg.getAwardList());
		int randomCount = HawkRand.randInt(cfg.getLotteryNumStart(), cfg.getLotteryNumEnd());
		if(randomCount > numLimit){
			randomCount = numLimit;
		}
		if(defaultNum > 0){
			randomCount = defaultNum;
		}
		for(int i=0;i<randomCount;i++){
			if(list.size() == 0){
				break;
			}
			int randomIndex = HawkRand.randInt(list.size() -1);
			RewardItem.Builder builder = list.remove(randomIndex);
			if(builder != null){
				BackGift gift = new BackGift(cfg.getType(), builder.getItemType(), 
						builder.getItemId(), builder.getItemCount());
				awards.add(gift);
			}
		}
		return awards;
	}
	
	
	/**
	 * 随机奖品池
	 * @param weightCfgs
	 * @return
	 */
	public BackGiftLotteryWeightCfg randomLotteryWeight(List<BackGiftLotteryWeightCfg> weightCfgs){
		Random ran = new Random();
		final int denominator = weightCfgs.stream().mapToInt(BackGiftLotteryWeightCfg::getWeight).sum();
		int molecular =ran.nextInt(denominator);
		BackGiftLotteryWeightCfg result = null;
		for (BackGiftLotteryWeightCfg cfg : weightCfgs) {
			molecular = molecular - cfg.getWeight();
			if (molecular < 0) {
				result = cfg;
				break;
			}
		}
		return result;
	}
	
	
	


	/**
	 * 获取符合要求的奖品池
	 * @param backFlowPlayer
	 * @return
	 */
	public Map<Integer,BackGiftLotteryCfg> getLotteryConfigs(BackGiftEntity entity){
		List<BackGiftLotteryCfg> lotteryCfg = HawkConfigManager.getInstance().
				getConfigIterator(BackGiftLotteryCfg.class).toList();
		Map<Integer,BackGiftLotteryCfg> cfgs = new HashMap<>();
		for(BackGiftLotteryCfg cfg : lotteryCfg){
			if(cfg.isAdapt(entity)){
				cfgs.put(cfg.getType(), cfg);
			}
		}
		return cfgs;
	}

	
	/**
	 * 获取活动持续时间
	 * @param backFlowPlayer
	 * @return
	 */
	public BackGfitDateCfg getBackGfitDateCfg(BackFlowPlayer backFlowPlayer){
		List<BackGfitDateCfg> congfigs = HawkConfigManager.getInstance().
				getConfigIterator(BackGfitDateCfg.class).toList();
		for(BackGfitDateCfg cfg : congfigs){
			if(cfg.isAdapt(backFlowPlayer)){
				return cfg;
			}
		}
		return null;
	}

	
	/**
	 * 获取抽奖刷新CD时间点
	 * @param entity
	 * @return
	 */
	public long getLotteryCdTime(BackGiftEntity entity){
		int freeTime = this.getLotteryFreeTimes(entity);
		if(freeTime > 0){
			return 0;
		}
		BackGiftKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BackGiftKVCfg.class);
		return entity.getLotteryTime() + cfg.getLotteryLimitCd() * 1000;
	}
	
	
	/**
	 * 获取免费抽奖次数
	 * @param entity
	 * @return
	 */
	public int getLotteryFreeTimes(BackGiftEntity entity){
		int freeTimes = 0;
		BackGiftKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BackGiftKVCfg.class);
		if(entity.getLotteryCount() >= cfg.getCostFree()){
			freeTimes = 0;
		}else{
			freeTimes = cfg.getCostFree() - entity.getLotteryCount();
		}
		long curTime = HawkTime.getMillisecond();
		long cdTime = cfg.getLotteryLimitCd() * 1000;
		if(freeTimes == 0 && curTime > entity.getLotteryTime() + cdTime){
			freeTimes = 1;
		}
		return freeTimes;
	}
	
	/**
	 * 获取刷新CD时间点
	 * @param entity
	 * @return
	 */
	public long getRefreshCdTime(BackGiftEntity entity){
		BackGiftKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BackGiftKVCfg.class);
		if(entity.getRefreshTime() == 0){
			return 0;
		}
		long curTime = HawkTime.getMillisecond();
		if(curTime > entity.getRefreshTime() + cfg.getResetLimitCd() * 1000){
			return 0;
		}
		return entity.getRefreshTime() + cfg.getResetLimitCd() * 1000;
	}
	
	/**
	 * 获取免费次数
	 * @param entity
	 * @return
	 */
	public int getFreeRefreshCount(BackGiftEntity entity){
		BackGiftKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BackGiftKVCfg.class);
		int lastCount = cfg.getResetLimit() - entity.getRefreshCount();
		if(lastCount <  0){
			lastCount = 0;
		}
		return lastCount;
	}
	
	/**
	 * 数据同步
	 * @param playerId
	 * @param entity
	 */
	public void syncActivityInfo(String playerId,BackGiftEntity entity){
		BackGiftInfoResp.Builder builder = BackGiftInfoResp.newBuilder();
		for(BackGift gift : entity.getAwardList()){
			builder.addGifts(gift.getRewardBuilder());
		}
		builder.setFreeTimes(this.getLotteryFreeTimes(entity));
		builder.setLotteryCdTime(this.getLotteryCdTime(entity));
		builder.setRefreshTimes(this.getFreeRefreshCount(entity));
		builder.setRefreshCdTime(this.getRefreshCdTime(entity));
		builder.setLastLottery(entity.getAwardIndex());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.BACK_GIFT_INFO_RESP_VALUE, builder));
	}
	




	@Override
	public ActivityType getActivityType() {
		return ActivityType.BACK_GIFT;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		BackGiftActivity activity = new BackGiftActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<BackGiftEntity> queryList = HawkDBManager.getInstance()
				.query("from BackGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			BackGiftEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		BackGiftEntity entity = new BackGiftEntity(playerId, termId);
		return entity;
	}



	@Override
	public boolean isHidden(String playerId) {
		Optional<BackGiftEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return true;
		}
		long curTime = HawkTime.getMillisecond();
		BackGiftEntity entity = optional.get();
		if(curTime > entity.getOverTime() || 
				curTime < entity.getStartTime()){
			return true;
		}
		return super.isHidden(playerId);
	}


	
}
