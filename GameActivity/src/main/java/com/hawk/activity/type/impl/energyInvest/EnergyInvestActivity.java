package com.hawk.activity.type.impl.energyInvest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AddTavernScoreEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.EnergyInvestGiftBuyEvent;
import com.hawk.activity.event.impl.MedalFundGiftBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.energyInvest.cfg.EnergyInvestMenuCfg;
import com.hawk.activity.type.impl.energyInvest.cfg.EnergyInvestRewardCfg;
import com.hawk.activity.type.impl.energyInvest.cfg.EnergyInvestTimeCfg;
import com.hawk.activity.type.impl.energyInvest.entity.EnergyInvestEntity;
import com.hawk.game.protocol.Activity.BuyEnergyInvestInfo;
import com.hawk.game.protocol.Activity.EnergyInvestPageInfo;
import com.hawk.game.protocol.Activity.EnergyInvestStage;
import com.hawk.game.protocol.Activity.EnergyInvestState;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

public class EnergyInvestActivity extends ActivityBase{

	public EnergyInvestActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.ENERGY_INVEST_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		EnergyInvestActivity activity = new EnergyInvestActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<EnergyInvestEntity> queryList = HawkDBManager.getInstance()
				.query("from EnergyInvestEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			EnergyInvestEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		EnergyInvestEntity entity = new EnergyInvestEntity(playerId, termId);
		//initScoreInfo(entity);
		return entity;
	}

	
	public void initScoreInfo(EnergyInvestEntity entity){
		long termOpenTime = getActivityType().getTimeControl().getShowTimeByTermId(entity.getTermId(), entity.getPlayerId());
		int dayth = HawkTime.calcBetweenDays(new Date(termOpenTime), new Date(HawkTime.getMillisecond()));
		//活动开启获取积分初始化
		int totalScore = this.getDataGeter().getPlayerTavernBoxScore(entity.getPlayerId());
		logger.info("EnergyInvestActivity initScoreInfo dayth:{}, totalScore:{}", dayth, totalScore);
		entity.getDaliyTaskMap().put(dayth, totalScore);
		entity.notifyUpdate();
	}
	/**检查是否可以购买礼包
	 * @param playerId
	 * @param payforId
	 * @return
	 */
	public boolean canPayforGift(String playerId, String payforId) {
		Optional<EnergyInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		int giftId = EnergyInvestMenuCfg.getGiftId(payforId);
		EnergyInvestMenuCfg medalFundGiftCfg = HawkConfigManager.getInstance().getConfigByKey(EnergyInvestMenuCfg.class, giftId);
		if (medalFundGiftCfg == null) {
			return false;
		}
		EnergyInvestEntity entity = opEntity.get();
		Map<Integer, Integer> buyInfoMap = entity.getBuyInfoMap();
		//已经购买
		if (buyInfoMap.containsKey(giftId)) {
			return false;
		}
		//是否是投资期
		if (getEnergyInvestStage() != EnergyInvestStage.ENERGY_STAGE_INVEST) {
			return false;
		}
		return true;
	}
	
	
	/**收割期领取奖励
	 * @param playerId
	 */
	public Result<?> getMedalFundReward(String playerId, int giftId, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<EnergyInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		EnergyInvestEntity entity = opEntity.get();
		Map<Integer, Integer> buyInfoMap = entity.getBuyInfoMap();
		//未购买
		if (!buyInfoMap.containsKey(giftId)) {
			return Result.fail(Status.Error.ENERGY_INVEST_NO_BUY_VALUE);
		}
		//已经领过
		if (buyInfoMap.get(giftId) != EnergyInvestState.ENERGY_INVEST_BUY_VALUE) {
			return Result.fail(Status.Error.ENERGY_INVEST_HAVE_RECEIVED_VALUE);
		}
		//是否是收割期
		if (getEnergyInvestStage() != EnergyInvestStage.ENERGY_STAGE_PDELIVERY) {
			return Result.fail(Status.Error.ENERGY_INVEST_NO_TIME_LIMIT_VALUE);
		}
		List<RewardItem.Builder> rewardItemList = getEnergyInvestRewardByGift(entity, giftId);
		
		this.getDataGeter().takeReward(playerId, rewardItemList, Action.ENERGY_INVEST_DDLIVERY_AWARD, true);
		
		int termId = getActivityTermId();
		
		this.getDataGeter().logEnergyInvestRewardScoreInfo(playerId, termId, giftId, SerializeHelper.mapToString(entity.getDaliyTaskMap()));
		
		buyInfoMap.put(giftId, EnergyInvestState.ENERGY_INVEST_REWARD_VALUE);
		entity.notifyUpdate();
		//push
		syncActivityDataInfo(playerId);
		
		if(rewardItemList.isEmpty()){
			return Result.fail(Status.Error.ENERGY_INVEST_NO_REWARD_FOR_SCORE_VALUE);
		}
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
		Optional<EnergyInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return ;
		}
		EnergyInvestEntity entity = opEntity.get();
		EnergyInvestPageInfo.Builder builder  = genMedalFundPageInfo(entity);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.ENERGY_INVEST_PAGE_SYNC_VALUE, builder));
	}

	
	/**生成协议
	 * @param entity
	 * @return
	 */
	/**
	 * @param entity
	 * @return
	 */
	public EnergyInvestPageInfo.Builder genMedalFundPageInfo(EnergyInvestEntity entity){
		EnergyInvestPageInfo.Builder builder  = EnergyInvestPageInfo.newBuilder();
		int termId = getActivityTermId();
		EnergyInvestTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EnergyInvestTimeCfg.class, termId);
		builder.setBuyEndTime(cfg.getBuyEndTimeValue());
		builder.setStage(getEnergyInvestStage());
		//购买和每日任务积分信息
		Map<Integer, Integer> buyInfoMap = entity.getBuyInfoMap();
		for (Entry<Integer, Integer> entry : buyInfoMap.entrySet()) {
			BuyEnergyInvestInfo.Builder buyInfo = BuyEnergyInvestInfo.newBuilder();
			buyInfo.setBuyId(entry.getKey());
			buyInfo.setState(EnergyInvestState.valueOf(entry.getValue()));
			builder.addBuyInfo(buyInfo);
		}
		int openDays = getActivityBetweenDays();
		for (int i = 0; i < openDays; i++) {
			int score = getScoreDays(entity, i);
			builder.addDayScore(score);
		}
		return builder;
	}
	
	/**获取奖励
	 * @param entity
	 * @param giftId
	 * @return
	 */
	public List<RewardItem.Builder> getEnergyInvestRewardByGift(EnergyInvestEntity entity, int giftId){
		EnergyInvestMenuCfg menuCfg = HawkConfigManager.getInstance().getConfigByKey(EnergyInvestMenuCfg.class, giftId);
		List<Integer> socreList = menuCfg.getDailyScoreList();
		List<RewardItem.Builder> allRewardList = new ArrayList<>();
		
		ConfigIterator<EnergyInvestRewardCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(EnergyInvestRewardCfg.class);
		while (achieveIterator.hasNext()) {
			EnergyInvestRewardCfg cfg = achieveIterator.next();
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
	public int getScoreDays(EnergyInvestEntity entity, int dayth){
		Map<Integer,Integer> daliyMap = entity.getDaliyTaskMap();
		if (!daliyMap.containsKey(dayth)) {
			logger.info("getScoreDays is not containsKey  dayth:{}", dayth);
			return 0;
		}
		return daliyMap.get(dayth);
	}
	
	/**获取活动时间天数(积分结算期)
	 * @return
	 */
	public int getActivityBetweenDays(){
		int termId = getActivityTermId();
		EnergyInvestTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EnergyInvestTimeCfg.class, termId);
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
	public void onEnergyInvestGiftBuy(EnergyInvestGiftBuyEvent event){
		String playerId = event.getPlayerId();
		String payforId = event.getGiftId();
		int giftId = EnergyInvestMenuCfg.getGiftId(payforId);
		EnergyInvestMenuCfg medalFundGiftCfg = HawkConfigManager.getInstance().getConfigByKey(EnergyInvestMenuCfg.class, giftId);
		if(medalFundGiftCfg == null){
			logger.error("onEnergyInvestGiftBuy giftCfg is null playerId:{},payforId:{},giftId:{}", playerId, payforId, giftId);
			return;
		}
		Optional<EnergyInvestEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		EnergyInvestEntity entity = opEntity.get();
		//修改数据
		entity.getBuyInfoMap().put(giftId, EnergyInvestState.ENERGY_INVEST_BUY_VALUE);
		
		List<RewardItem.Builder> rewardList = medalFundGiftCfg.getBuyAwardList();
		
		entity.notifyUpdate();
		//发送奖励
		this.getDataGeter().takeReward(playerId, rewardList, Action.ENERGY_INVEST_BUY_AWARD, true);
		//发送奖励收据
		sendRewardByMail(playerId, rewardList);
		this.syncActivityDataInfo(playerId);
		logger.info("onEnergyInvestGiftBuy success playerId:{},payforId:{},giftId:{}", playerId,payforId,giftId);
	}
	
	/** 发送奖励mail
	 */
	public void sendRewardByMail(String playerId, List<RewardItem.Builder> rewardList){
		try {
			// 邮件发送奖励
			Object[] content = new Object[0];
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			//发邮件
			sendMailToPlayer(playerId, MailConst.MailId.ENERGY_INVEST_REWARD, title, subTitle, content, rewardList, true);
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
		Optional<EnergyInvestEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		EnergyInvestEntity entity = optional.get();
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
		
		logger.info("EnergyInvestActivity AddTavernScoreEvent playerId:{}, dayth:{}, totalScore:{}", playerId, dayth, totalScore);
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<EnergyInvestEntity> optional = getPlayerDataEntity(event.getPlayerId());
		if (!optional.isPresent()) {
			return;
		}
		if(!event.isCrossDay()){
			return;
		}
		syncActivityDataInfo(event.getPlayerId());
	}
	
	/**当前在活动的那个阶段
	 * @return
	 */
	public EnergyInvestStage getEnergyInvestStage(){
		long nowTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		EnergyInvestTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EnergyInvestTimeCfg.class, termId);
		if (nowTime > cfg.getStartTimeValue() && nowTime < cfg.getBuyEndTimeValue()) {
			return EnergyInvestStage.ENERGY_STAGE_INVEST;
		}else if(nowTime >= cfg.getBuyEndTimeValue() && nowTime < cfg.getEndTimeValue()){
			return EnergyInvestStage.ENERGY_STAGE_PAYBACK;
		}else if(nowTime >= cfg.getEndTimeValue() && nowTime < cfg.getHiddenTimeValue()){
			return EnergyInvestStage.ENERGY_STAGE_PDELIVERY;
		}
		return null;
	}
	
}
