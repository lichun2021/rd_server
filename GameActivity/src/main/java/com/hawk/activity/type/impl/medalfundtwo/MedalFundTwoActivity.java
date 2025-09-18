package com.hawk.activity.type.impl.medalfundtwo;

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
import com.hawk.activity.event.impl.MedalFundTwoGiftBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.medalfundtwo.cfg.MedalFundTwoMenuCfg;
import com.hawk.activity.type.impl.medalfundtwo.cfg.MedalFundTwoRewardCfg;
import com.hawk.activity.type.impl.medalfundtwo.cfg.MedalFundTwoTimeCfg;
import com.hawk.activity.type.impl.medalfundtwo.entity.MedalFundTwoEntity;
import com.hawk.game.protocol.Activity.BuyFundTwoInfo;
import com.hawk.game.protocol.Activity.MedalFundTwoBuyState;
import com.hawk.game.protocol.Activity.MedalFundTwoPageInfo;
import com.hawk.game.protocol.Activity.MedalFundTwoState;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 新版勋章投资
 * 
 * @author lating
 *
 */
public class MedalFundTwoActivity extends ActivityBase {

	public MedalFundTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.MEDAL_FUND_TWO_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		MedalFundTwoActivity activity = new MedalFundTwoActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<MedalFundTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from MedalFundTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			MedalFundTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		MedalFundTwoEntity entity = new MedalFundTwoEntity(playerId, termId);
		return entity;
	}

	
	public void initScoreInfo(MedalFundTwoEntity entity){
		long termOpenTime = getActivityType().getTimeControl().getShowTimeByTermId(entity.getTermId(), entity.getPlayerId());
		int dayth = HawkTime.calcBetweenDays(new Date(termOpenTime), new Date(HawkTime.getMillisecond()));
		//活动开启获取积分初始化
		int totalScore = this.getDataGeter().getPlayerTavernBoxScore(entity.getPlayerId());
		logger.info("MedalFundTwoActivity initScoreInfo, playerId: {}, dayth: {}, totalScore: {}", entity.getPlayerId(), dayth, totalScore);
		entity.getDaliyTaskMap().put(dayth, totalScore);
		entity.notifyUpdate();
	}
	
	/**检查是否可以购买礼包
	 * @param playerId
	 * @param payforId
	 * @return
	 */
	public boolean canPayforGift(String playerId, String payforId) {
		Optional<MedalFundTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		int giftId = MedalFundTwoMenuCfg.getGiftId(payforId);
		MedalFundTwoMenuCfg medalFundGiftCfg = HawkConfigManager.getInstance().getConfigByKey(MedalFundTwoMenuCfg.class, giftId);
		if (medalFundGiftCfg == null) {
			return false;
		}
		MedalFundTwoEntity entity = opEntity.get();
		Map<Integer, Integer> buyInfoMap = entity.getBuyInfoMap();
		//已经购买
		if (buyInfoMap.containsKey(giftId)) {
			return false;
		}
		//是否是投资期
		if (getMedalFundState() != MedalFundTwoState.MEDAL_FUND_TWO_INVEST) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * 收割期领取奖励
	 * 
	 * @param playerId
	 */
	public Result<?> getMedalFundReward(String playerId, int giftId, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<MedalFundTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		MedalFundTwoEntity entity = opEntity.get();
		Map<Integer, Integer> buyInfoMap = entity.getBuyInfoMap();
		//未购买
		if (!buyInfoMap.containsKey(giftId)) {
			return Result.fail(Status.Error.MEDAL_FUND_NO_BUY_VALUE);
		}
		//已经领过
		if (buyInfoMap.get(giftId) != MedalFundTwoBuyState.MEDAL_FUND_TWO_BUY_VALUE) {
			return Result.fail(Status.Error.MEDAL_FUND_HAVE_RECEIVED_VALUE);
		}
		//是否是收割期
		if (getMedalFundState() != MedalFundTwoState.MEDAL_FUND_TWO_PDELIVERY) {
			return Result.fail(Status.Error.MEDAL_FUND_NO_TIME_LIMIT_VALUE);
		}
		List<RewardItem.Builder> rewardItemList = getMedalFundRewardByGift(entity, giftId);
		
		this.getDataGeter().takeReward(playerId, rewardItemList, Action.MEDAL_FUND_TWO_DDLIVERY_AWARD, true);
		
		int termId = getActivityTermId();
		
		this.getDataGeter().logMedalFundRewardScoreInfo(playerId, termId, giftId, SerializeHelper.mapToString(entity.getDaliyTaskMap()), 1);
		
		buyInfoMap.put(giftId, MedalFundTwoBuyState.MEDAL_FUND_TWO_REWARD_VALUE);
		entity.notifyUpdate();
		//push
		syncActivityDataInfo(playerId);
		
		if(rewardItemList.isEmpty()){
			return Result.fail(Status.Error.MEDAL_FUND_NO_REWARD_FOR_SCORE_VALUE);
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
		Optional<MedalFundTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return ;
		}
		MedalFundTwoEntity entity = opEntity.get();
		MedalFundTwoPageInfo.Builder builder  = genMedalFundPageInfo(entity);
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.MEDAL_FUND_TWO_PAGE_SYNC_VALUE, builder));
	}

	
	/**生成协议
	 * @param entity
	 * @return
	 */
	public MedalFundTwoPageInfo.Builder genMedalFundPageInfo(MedalFundTwoEntity entity){
		MedalFundTwoPageInfo.Builder builder  = MedalFundTwoPageInfo.newBuilder();
		int termId = getActivityTermId();
		MedalFundTwoTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MedalFundTwoTimeCfg.class, termId);
		builder.setBuyEndTime(cfg.getBuyEndTimeValue());
		builder.setMedalFundState(getMedalFundState());
		//购买和每日任务积分信息
		Map<Integer, Integer> buyInfoMap = entity.getBuyInfoMap();
		for (Entry<Integer, Integer> entry : buyInfoMap.entrySet()) {
			BuyFundTwoInfo.Builder buyFundInfo = BuyFundTwoInfo.newBuilder();
			buyFundInfo.setBuyId(entry.getKey());
			buyFundInfo.setBuyState(MedalFundTwoBuyState.valueOf(entry.getValue()));
			builder.addBuyFundInfo(buyFundInfo);
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
	public List<RewardItem.Builder> getMedalFundRewardByGift(MedalFundTwoEntity entity, int giftId){
		MedalFundTwoMenuCfg menuCfg = HawkConfigManager.getInstance().getConfigByKey(MedalFundTwoMenuCfg.class, giftId);
		List<Integer> socreList = menuCfg.getDailyScoreList();
		List<RewardItem.Builder> allRewardList = new ArrayList<>();
		
		ConfigIterator<MedalFundTwoRewardCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(MedalFundTwoRewardCfg.class);
		while (achieveIterator.hasNext()) {
			MedalFundTwoRewardCfg cfg = achieveIterator.next();
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
	public int getScoreDays(MedalFundTwoEntity entity, int dayth){
		Map<Integer,Integer> daliyMap = entity.getDaliyTaskMap();
		if (!daliyMap.containsKey(dayth)) {
			logger.info("getScoreDays is not containsKey dayth: {}", dayth);
			return 0;
		}
		return daliyMap.get(dayth);
	}
	
	/**获取活动时间天数(积分结算期)
	 * @return
	 */
	public int getActivityBetweenDays(){
		int termId = getActivityTermId();
		MedalFundTwoTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MedalFundTwoTimeCfg.class, termId);
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
	public void onMedalFundGiftBuy(MedalFundTwoGiftBuyEvent event){
		String playerId = event.getPlayerId();
		String payforId = event.getGiftId();
		int giftId = MedalFundTwoMenuCfg.getGiftId(payforId);
		MedalFundTwoMenuCfg medalFundGiftCfg = HawkConfigManager.getInstance().getConfigByKey(MedalFundTwoMenuCfg.class, giftId);
		if(medalFundGiftCfg == null){
			logger.error("onMedalFundTwoGiftBuy giftCfg is null playerId: {}, payforId: {}, giftId: {}", playerId, payforId, giftId);
			return;
		}
		Optional<MedalFundTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		MedalFundTwoEntity entity = opEntity.get();
		//修改数据
		entity.getBuyInfoMap().put(giftId, MedalFundTwoBuyState.MEDAL_FUND_TWO_BUY_VALUE);
		
		List<RewardItem.Builder> rewardList = medalFundGiftCfg.getBuyAwardList();
		
		entity.notifyUpdate();
		//发送奖励
		this.getDataGeter().takeReward(playerId, rewardList, Action.MEDAL_FUND_TWO_BUY_AWARD, true);
		//发送奖励收据
		sendRewardByMail(playerId, rewardList);
		this.syncActivityDataInfo(playerId);
		logger.info("onMedalFundTwoGiftBuy success playerId: {}, payforId: {}, giftId: {}", playerId,payforId,giftId);
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
			sendMailToPlayer(playerId, MailConst.MailId.MEDAL_FUND_TWO_REWARD, title, subTitle, content, rewardList, true);
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
		Optional<MedalFundTwoEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		MedalFundTwoEntity entity = optional.get();
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
		
		logger.info("MedalFundTwoActivity AddTavernScoreEvent playerId: {}, dayth: {}, totalScore: {}", playerId, dayth, totalScore);
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<MedalFundTwoEntity> optional = getPlayerDataEntity(event.getPlayerId());
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
	public MedalFundTwoState getMedalFundState(){
		long nowTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		MedalFundTwoTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MedalFundTwoTimeCfg.class, termId);
		if (nowTime > cfg.getStartTimeValue() && nowTime < cfg.getBuyEndTimeValue()) {
			return MedalFundTwoState.MEDAL_FUND_TWO_INVEST;
		}else if(nowTime >= cfg.getBuyEndTimeValue() && nowTime < cfg.getEndTimeValue()){
			return MedalFundTwoState.MEDAL_FUND_TWO_PAYBACK;
		}else if(nowTime >= cfg.getEndTimeValue() && nowTime < cfg.getHiddenTimeValue()){
			return MedalFundTwoState.MEDAL_FUND_TWO_PDELIVERY;
		}
		return null;
	}
	
}
