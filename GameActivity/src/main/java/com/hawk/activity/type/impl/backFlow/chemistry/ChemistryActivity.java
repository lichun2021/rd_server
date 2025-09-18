package com.hawk.activity.type.impl.backFlow.chemistry;

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
import com.hawk.activity.event.impl.LoginDayChemistryEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.backFlow.chemistry.cfg.ChemistryAchieveConfig;
import com.hawk.activity.type.impl.backFlow.chemistry.cfg.ChemistryDateCfg;
import com.hawk.activity.type.impl.backFlow.chemistry.cfg.ChemistryTimeCfg;
import com.hawk.activity.type.impl.backFlow.chemistry.entity.ChemistryEntity;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.log.Action;

/***
 * 再续前缘
 * @author che
 *
 */
public class ChemistryActivity extends ActivityBase implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	public ChemistryActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onPlayerLogin(String playerId) {
		Optional<ChemistryEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		ChemistryEntity entity = optional.get();
		BackFlowPlayer backFlowPlayer = this.getDataGeter().getBackFlowPlayer(playerId);
		if(backFlowPlayer == null){
			return;
		}
		if(this.checkFitLostParams(backFlowPlayer,entity)){
			int backTimes = backFlowPlayer.getBackCount();
			ChemistryDateCfg dataCfg = this.getBackGfitDateCfg(backFlowPlayer);
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
			entity.setLoginDays(1);
			initAchieve(entity);
			entity.notifyUpdate();
			logger.info("onPlayerLogin  checkFitLostParams init sucess,  playerId: "+ 
					"{},backCount:{},backType:{},backTime:{},startTime:{}.overTime:{}", 
					playerId,backTimes,backType,backFlowPlayer.getBackTimeStamp(),startTime,overTime);
			ActivityManager.getInstance().postEvent(new LoginDayChemistryEvent(playerId, 1), true);
		}
		
	}
	
	
	
	
	public boolean checkFitLostParams(BackFlowPlayer backFlowPlayer,ChemistryEntity entity) {
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
		logger.info("checkFitLostParams sucess, playerId: "
				+ "{},loss:{}", backFlowPlayer.getPlayerId(),lossDays);
		return true;
	}
	
	
	/**
	 * 是否可以触发
	 * @return
	 */
	public boolean canTrigger(long backTime){
		int termId = this.getActivityTermId();
		ChemistryTimeCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(ChemistryTimeCfg.class, termId);
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
		Optional<ChemistryEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		ChemistryEntity entity = optional.get();
		if(!entity.isStartDay()){
			entity.setLoginDays(entity.getLoginDays() + 1);
			entity.notifyUpdate();
			ActivityManager.getInstance().postEvent(new LoginDayChemistryEvent(playerId, entity.getLoginDays()), true);
		}
		
	}
	
	
	
	private void initAchieve(ChemistryEntity entity){
		Iterator<ChemistryAchieveConfig> ite = HawkConfigManager.getInstance().getConfigIterator(ChemistryAchieveConfig.class);
		List<AchieveItem> alist = new CopyOnWriteArrayList<AchieveItem>();
		while(ite.hasNext()){
			ChemistryAchieveConfig config = ite.next();
			if(config.getPlayerType() == entity.getBackType()){
				AchieveItem item = AchieveItem.valueOf(config.getAchieveId());
				alist.add(item);
			}
		}
		entity.resetItemList(alist);
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), entity.getItemList()), true);
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
		Optional<ChemistryEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		ChemistryEntity entity = opEntity.get();
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		ChemistryAchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(ChemistryAchieveConfig.class, achieveId);
		return config;
	}

	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		return AchieveProvider.super.onAchieveFinished(playerId, achieveItem);
	}

	@Override
	public Action takeRewardAction() {
		return Action.CHEMISTRY_ACHIEVE_REWARD;
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.CHEMISTRY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ChemistryActivity activity = new ChemistryActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ChemistryEntity> queryList = HawkDBManager.getInstance()
				.query("from ChemistryEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ChemistryEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ChemistryEntity entity = new ChemistryEntity(playerId, termId);
		return entity;
	}



	@Override
	public boolean isHidden(String playerId) {
		Optional<ChemistryEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return true;
		}
		long curTime = HawkTime.getMillisecond();
		ChemistryEntity entity = optional.get();
		if(curTime > entity.getOverTime() || 
				curTime < entity.getStartTime()){
			return true;
		}
		return super.isHidden(playerId);
	}

	/**
	 * 获取活动持续时间
	 * @param backFlowPlayer
	 * @return
	 */
	public ChemistryDateCfg getBackGfitDateCfg(BackFlowPlayer backFlowPlayer){
		List<ChemistryDateCfg> congfigs = HawkConfigManager.getInstance().
				getConfigIterator(ChemistryDateCfg.class).toList();
		for(ChemistryDateCfg cfg : congfigs){
			if(cfg.isAdapt(backFlowPlayer)){
				return cfg;
			}
		}
		return null;
	}
}
