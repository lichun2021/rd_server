package com.hawk.activity.type.impl.backFlow.developSput;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
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
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DevelopSpurtAdvancedUnlockEvent;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.activity.type.impl.backFlow.developSput.cfg.DevelopSpurtAchieveTaskConfig;
import com.hawk.activity.type.impl.backFlow.developSput.cfg.DevelopSpurtDateCfg;
import com.hawk.activity.type.impl.backFlow.developSput.cfg.DevelopSpurtSignInCfg;
import com.hawk.activity.type.impl.backFlow.developSput.cfg.DevelopSpurtTimeCfg;
import com.hawk.activity.type.impl.backFlow.developSput.entity.DevelopSpurtEntity;
import com.hawk.game.protocol.Activity.DevelopSpurtInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/***
 * 回归成就活动（发展冲刺）
 * @author yang.rao
 *
 */
public class DevelopSpurtActivity extends ActivityBase implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	public DevelopSpurtActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onPlayerLogin(String playerId) {
		
		Optional<DevelopSpurtEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		DevelopSpurtEntity entity = optional.get();
		BackFlowPlayer backFlowPlayer = this.getDataGeter().getBackFlowPlayer(playerId);
		if(backFlowPlayer == null){
			return;
		}
		//检查新开活动
		if(this.checkFitLostParams(backFlowPlayer,entity)){
			int backTimes = backFlowPlayer.getBackCount();
			DevelopSpurtDateCfg dataCfg = this.getDevelopSpurtDateCfg(backFlowPlayer);
			long startTime = HawkTime.getAM0Date(
					new Date(backFlowPlayer.getBackTimeStamp())).getTime();
			long continueTime = 0;
			int backType = 0;
			if(dataCfg != null){
				continueTime = dataCfg.getDuration() * HawkTime.DAY_MILLI_SECONDS  - 1000;
				backType = dataCfg.getId();
			}
			long overTime = startTime + continueTime;
			entity.setBackCount(backTimes);
			entity.setBackType(backType);
			entity.setStartTime(startTime);
			entity.setOverTime(overTime);
			entity.setLoginDays(1);
			entity.setLoginTime(HawkTime.getMillisecond());
			entity.setSignInDays(0);
			entity.setSignInTime(0);
			entity.setUnlockTime(0);
			initAchieve(entity);
			entity.notifyUpdate();
			logger.info("onPlayerLogin  checkFitLostParams init sucess,  playerId: "+ 
					"{},backCount:{},backType:{},backTime:{},startTime:{}.overTime:{}", 
					playerId,backTimes,backType,backFlowPlayer.getBackTimeStamp(),startTime,overTime);
			return;
		}
		reissueSignInAward(playerId, entity);
		if(!this.isHidden(playerId)){
			entity.setLoginTime(HawkTime.getMillisecond());
			entity.notifyUpdate();
		}
		
		
		
	}
	
	
	
	/**
	 * 检查参数
	 * @param backFlowPlayer
	 * @param entity
	 * @return
	 */
	public boolean checkFitLostParams(BackFlowPlayer backFlowPlayer,DevelopSpurtEntity entity) {
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
	 * 是否可以触发
	 * @return
	 */
	public boolean canTrigger(long backTime){
		int termId = this.getActivityTermId();
		DevelopSpurtTimeCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(DevelopSpurtTimeCfg.class, termId);
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
	/**
	 * 补发签到奖励
	 * 
	 * @param playerId
	 * @param entity
	 */
	private void reissueSignInAward(String playerId, DevelopSpurtEntity entity) {
		if (HawkTime.isSameDay(entity.getLoginTime(), entity.getSignInTime())) {
			logger.info("reissueSignInAward failed, sign already,loss data fail , playerId: "
					+ "{}", playerId);
			return;
		}
		if(HawkTime.isSameDay(entity.getLoginTime(), HawkTime.getMillisecond())){
			logger.info("reissueSignInAward failed, need next day,loss data fail , playerId: "
					+ "{}", playerId);
			return;
		}
		if (entity.getLoginTime() > entity.getSignInTime()) {
			// 在已签到天数的基础上又加一天
			int days = entity.getSignInDays() + 1;
			DevelopSpurtSignInCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopSpurtSignInCfg.class, days);
			if (cfg != null) {
				signAward(playerId, cfg, entity, entity.getLoginTime(),true);
			}
			logger.info("reissueSignInAward sucess, playerId:{},days: {},", playerId,days);
		}
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<DevelopSpurtEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		DevelopSpurtEntity entity = optional.get();
		this.syncSignInInfo(entity);
	}
	
	/**
	 * 同步签到信息
	 * 
	 * @param playerId
	 * @param entity
	 */
	private void syncSignInInfo(DevelopSpurtEntity entity) {
		
		DevelopSpurtInfoResp.Builder builder = DevelopSpurtInfoResp.newBuilder();
		boolean signInToday = HawkTime.isSameDay(entity.getLoginTime(), entity.getSignInTime());
		builder.setSignInDay(signInToday ? entity.getSignInDays() : entity.getSignInDays() + 1);
		builder.setUnlocked(entity.getUnlockTime() > 0);
		builder.setSignIn(signInToday);
		builder.setPlayerType(entity.getBackType());
		pushToPlayer(entity.getPlayerId(), HP.code.DEVELOP_SPURT_INFO_RESP_VALUE, builder);
	}
	
	/**
	 * 签到发奖
	 * 
	 * @param playerId
	 * @param cfg
	 * @param entity
	 */
	private void signAward(String playerId, DevelopSpurtSignInCfg cfg, DevelopSpurtEntity entity, long signInTime,boolean isMail) {
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		rewardList.addAll(cfg.getRewardList());
		// 如果进阶奖励也解锁了，需要将进阶奖励加上
		if (entity.getUnlockTime() > 0) {
			rewardList.addAll(cfg.getAdvancedRewardList());
		}
		if(isMail){
			this.getDataGeter().sendMail(playerId, MailId.DEVELOP_SPURT_REISSUE_GFIT, null, null, null,
					rewardList, false);
		}else{
			// 发奖
			ActivityReward reward = new ActivityReward(rewardList, Action.DEVELOP_SPURT_SIGN_REWARD);
			reward.setOrginType(RewardOrginType.SHOPPING_GIFT, getActivityId());
			reward.setAlert(true);
			postReward(playerId, reward, false);
		}
		// 更新签到时间
		entity.setSignInTime(signInTime);
		// 更新已签到天数
		entity.setSignInDays(entity.getSignInDays() + 1);
		entity.notifyUpdate();
	}
	
	/**
	 * 判断累计登录的进阶奖励是否已解锁
	 * 
	 * @param playerId
	 * @return
	 */
	public int checkAdvancedAwardUnlockedStatus(String playerId,String giftId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		if(isHidden(playerId)){
			logger.info("checkAdvancedAwardUnlockedStatus fail, hidden,"
					+ "playerId:{},giftId:{}", playerId,giftId);
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		Optional<DevelopSpurtEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		DevelopSpurtEntity entity = optional.get();
		if (entity.getUnlockTime() > 0) {
			logger.info("checkAdvancedAwardUnlockedStatus fail, unlock already,"
					+ "playerId:{},giftId:{},unlockTime:{}", playerId,giftId,entity.getUnlockTime());
			return Status.Error.DEVELOP_SPURT_ADVANCE_GIFT_BUY_VALUE;
		}
		return 0;
	}
	
	/**
	 *  签到发奖
	 *  
	 * @param playerId
	 * @return
	 */
	public int signIn(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<DevelopSpurtEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		DevelopSpurtEntity entity = optional.get();
		if (HawkTime.isSameDay(entity.getLoginTime(), entity.getSignInTime())) {
			logger.info("signIn fail, sign alrady,playerId:{}", playerId);
			return Status.Error.DEVELOP_SPURT_SIGN_TODAY_VALUE;
		}
		int days = entity.getSignInDays() + 1;
		DevelopSpurtSignInCfg signCfg = this.getSignConfig(entity, days);
		if(signCfg == null){
			return 0;
		}
		signAward(playerId, signCfg, entity, HawkTime.getMillisecond(),false);
		syncSignInInfo(entity);
		logger.info("signIn sucess,playerId:{},days:{}", playerId,days);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	public DevelopSpurtDateCfg getDevelopSpurtDateCfg(BackFlowPlayer backFlowPlayer){
		List<DevelopSpurtDateCfg> confgs = HawkConfigManager.getInstance().
				getConfigIterator(DevelopSpurtDateCfg.class).toList();
		for(DevelopSpurtDateCfg cfg : confgs){
			if(cfg.isAdapt(backFlowPlayer)){
				return cfg;
			}
		}
		return null;
	}
	
	
	public DevelopSpurtSignInCfg getSignConfig(DevelopSpurtEntity entity,int day){
		List<DevelopSpurtSignInCfg> kvConfig = HawkConfigManager.getInstance().
				getConfigIterator(DevelopSpurtSignInCfg.class).toList();
		for(DevelopSpurtSignInCfg cfg : kvConfig){
			if(entity.getBackType() == cfg.getPlayerType() && cfg.getDay() == day){
				return cfg;
			}
		}
		return null;
	}
	
	/**
	 *  直购回调处理
	 *  
	 * @param playerId
	 */
	@Subscribe
	public void unlockAdvanced(DevelopSpurtAdvancedUnlockEvent event) {
		logger.info("unlockAdvanced event,playerId:{}", event.getPlayerId());
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		if(this.isHidden(playerId)){
			logger.info("unlockAdvanced fail activity hidden,playerId:{}", event.getPlayerId());
			return;
		}
		Optional<DevelopSpurtEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		DevelopSpurtEntity entity = optional.get();
		entity.setUnlockTime(HawkTime.getMillisecond());
		// 补发进阶奖励
		{
			List<RewardItem.Builder> rewardList = new ArrayList<>();
			for (int day = 1; day <= entity.getSignInDays(); day++) {
				DevelopSpurtSignInCfg signCfg = this.getSignConfig(entity, day);
				rewardList.addAll(signCfg.getAdvancedRewardList());
			}
			
			// 发奖
			ActivityReward reward = new ActivityReward(rewardList, Action.DEVELOP_SPURT_SIGN_REWARD);
			reward.setOrginType(RewardOrginType.SHOPPING_GIFT, getActivityId());
			reward.setAlert(true);
			postReward(playerId, reward, false);
		}
		
		syncSignInInfo(entity);
		logger.info("unlockAdvanced event finish,playerId:{}", event.getPlayerId());
		
	}
	
	private void initAchieve(DevelopSpurtEntity entity){
		List<AchieveItem> achieveItemList = new CopyOnWriteArrayList<AchieveItem>();
		Iterator<DevelopSpurtAchieveTaskConfig> ite = HawkConfigManager.getInstance().getConfigIterator(DevelopSpurtAchieveTaskConfig.class);
		while(ite.hasNext()){
			DevelopSpurtAchieveTaskConfig config = ite.next();
			if(config.getPlayerType() == entity.getBackType()){
				AchieveItem item = AchieveItem.valueOf(config.getAchieveId());
				achieveItemList.add(item);
			}
		}
		entity.resetAchieveItemList(achieveItemList);
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), achieveItemList), true);
	}
	
	@Subscribe
	public void onContinutLoginEvent(ContinueLoginEvent event){
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		if(this.isHidden(playerId)){
			return;
		}
		Optional<DevelopSpurtEntity> opEntity = getPlayerDataEntity(playerId);
		DevelopSpurtEntity entity = opEntity.get();
		if (event.isCrossDay() && !this.isStartDay(entity)) {
			entity.setLoginDays(entity.getLoginDays() + 1);
			entity.setLoginTime(HawkTime.getMillisecond());
			entity.notifyUpdate();
			this.syncSignInInfo(entity);
		}
	}

	public boolean isStartDay(DevelopSpurtEntity entity){
		if(entity == null){
			return false;
		}
		long time = HawkTime.getMillisecond();
		if(HawkTime.isSameDay(entity.getStartTime(), time)){
			return true;
		}
		return false;
	}
	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId) && !isHidden(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isOpening(playerId) && !isHidden(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<DevelopSpurtEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		DevelopSpurtEntity entity = opEntity.get();
		AchieveItems achieveItems = new AchieveItems(entity.getAchieveItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		DevelopSpurtAchieveTaskConfig config = HawkConfigManager.getInstance().
				getConfigByKey(DevelopSpurtAchieveTaskConfig.class, achieveId);
		return config;
	}

	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		//打点任务
		return AchieveProvider.super.onAchieveFinished(playerId, achieveItem);
	}

	@Override
	public Action takeRewardAction() {
		return Action.DEVELOP_SPURT_ACHIEVE_REWARD;
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DEVELOP_SPURT;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DevelopSpurtActivity activity = new DevelopSpurtActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DevelopSpurtEntity> queryList = HawkDBManager.getInstance()
				.query("from DevelopSpurtEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DevelopSpurtEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DevelopSpurtEntity entity = new DevelopSpurtEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}

	@Override
	public boolean isHidden(String playerId) {
		Optional<DevelopSpurtEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return true;
		}
		long curTime = HawkTime.getMillisecond();
		DevelopSpurtEntity entity = optional.get();
		if(curTime > entity.getOverTime() || 
				curTime < entity.getStartTime()){
			return true;
		}
		return super.isHidden(playerId);
	}
	

}
