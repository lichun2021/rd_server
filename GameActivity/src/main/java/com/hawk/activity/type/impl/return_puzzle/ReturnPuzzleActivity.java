package com.hawk.activity.type.impl.return_puzzle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayReturnPuzzleEvent;
import com.hawk.activity.event.impl.ReturnPuzzleCheckShareEvent;
import com.hawk.activity.event.impl.ReturnPuzzleScoreEvent;
import com.hawk.activity.event.impl.ReturnPuzzleShareEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.activity.type.impl.return_puzzle.cfg.ReturnPuzzleAchieveCfg;
import com.hawk.activity.type.impl.return_puzzle.cfg.ReturnPuzzleChestCfg;
import com.hawk.activity.type.impl.return_puzzle.cfg.ReturnPuzzleDateCfg;
import com.hawk.activity.type.impl.return_puzzle.cfg.ReturnPuzzlePicCfg;
import com.hawk.activity.type.impl.return_puzzle.cfg.ReturnPuzzleTimeCfg;
import com.hawk.activity.type.impl.return_puzzle.entity.ReturnPuzzleEntity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.ReturnPuzzlePageInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 回流拼图活动
 * @author Jesse
 *
 */
public class ReturnPuzzleActivity extends ActivityBase implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	public ReturnPuzzleActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return !isHidden(playerId);
	}
	
	public void initData(String playerId){
		Optional<ReturnPuzzleEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		ReturnPuzzleEntity entity = optional.get();
		BackFlowPlayer backFlowPlayer = this.getDataGeter().getBackFlowPlayer(playerId);
		if(backFlowPlayer == null){
			return;
		}
		//检查新开活动
		if(this.checkFitLostParams(backFlowPlayer,entity)){
			int backTimes = backFlowPlayer.getBackCount();
			ReturnPuzzleDateCfg dataCfg = this.getBackDateCfg(backFlowPlayer);
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
			entity.notifyUpdate();
			//特别注意，一定要先创建回流基础数据，再根据基础数据生成相应成就，顺序必须保证
			entity.setCurDay(0);
			entity.setScore(0);
			if(backType>0)
				initAchieveItems(playerId);
			
			logger.info("onPlayerLogin  checkFitLostParams init sucess,  playerId: {},backCount:{},backType:{},backTime:{},startTime:{}.overTime:{}", 
					playerId,backTimes,backType,backFlowPlayer.getBackTimeStamp(),startTime,overTime);
			return;
		}		
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		initData(playerId);
		syncActivityDataInfo(playerId);
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_RETURN_PUZZLE, ()-> {
				initData(playerId);
			});
		}
	}
	
	/**
	 * 初始化成就配置项
	 * @param playerId
	 * @param isLogin
	 * @param entity
	 */
	private void initAchieveItems(String playerId) {
		Optional<ReturnPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return; 
		}
		ReturnPuzzleEntity entity = opEntity.get();
		// 成就已初始化
		if(entity.getCurDay() == 0 && entity.getBackType() > 0){//首次加载，之后判定时间重置相关信息
			int currDay = 1;
			// 初始添加成就项
			List<ReturnPuzzlePicCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ReturnPuzzlePicCfg.class).toList();
			List<AchieveItem> shareList = new CopyOnWriteArrayList<AchieveItem>();
			List<AchieveItem> dayList = new CopyOnWriteArrayList<AchieveItem>();
			for (ReturnPuzzlePicCfg cfg : configIterator) {
				
				if (cfg.getOrder() == currDay) {
					for (int i = 0; i < cfg.getPuzzleAchieveValueList().size(); i++) {
						AchieveItem item = AchieveItem.valueOf(cfg.getPuzzleAchieveValueList().get(i));
						dayList.add(item);
					}
				}
				
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				shareList.add(item);
			}
			
			List<AchieveItem> boxList = new CopyOnWriteArrayList<AchieveItem>(); 
			ConfigIterator<ReturnPuzzleChestCfg> scoreAchieveIt = HawkConfigManager.getInstance().getConfigIterator(ReturnPuzzleChestCfg.class);
			for (ReturnPuzzleChestCfg cfg : scoreAchieveIt) {
				if(cfg.getDateSet() == entity.getBackType()){				
					AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
					boxList.add(item);
				}
			}
logger.info("initAchieveItems init sucess,  playerId: {},backCount:{},backType:{},dayList:{},box:{},share:{}", 
					playerId,entity.getBackCount(),entity.getBackType(),entity.getItemList().size(),boxList.size(),shareList.size());
			entity.resetItemList(dayList);
			entity.setShareList(shareList);
			entity.setBoxList(boxList);
			entity.setCurDay(currDay);
			entity.setNextTime(getCurrentOrderTime(entity.getStartTime(),currDay));
			entity.notifyUpdate();
			// 初始化成就数据
			ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getAllList()) , true);
			
			ActivityManager.getInstance().postEvent(new LoginDayReturnPuzzleEvent(playerId, 1), true);
		}
	}
	
	/**
	 * @param startTime
	 * @param order
	 * @return 根据活动开启时计算该阶段结束时间
	 */
	private long getCurrentOrderTime(long startTime,int order ){	
		long nextTime = startTime;
		List<ReturnPuzzlePicCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ReturnPuzzlePicCfg.class).toList();
		for (ReturnPuzzlePicCfg cfg : configIterator) {
			if(cfg.getOrder() <= order){
				nextTime += (cfg.getTime()*1000);//转换成毫秒
			}
		}
		return nextTime;
	}
	
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<ReturnPuzzleEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		ReturnPuzzleEntity playerDataEntity = opPlayerDataEntity.get();
		
//		保证登录时、玩家在线开启活动时检测即可，这里的检测去掉，太频繁了
//		if(playerDataEntity.getOverTime()==0){//初始化
//			initData(playerId);
//		}
		AchieveItems items = new AchieveItems(playerDataEntity.getAllList(), playerDataEntity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(ReturnPuzzleAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(ReturnPuzzleChestCfg.class, achieveId);
			if(config == null){
				config = HawkConfigManager.getInstance().getConfigByKey(ReturnPuzzlePicCfg.class, achieveId);	
			}
		}
		return config;
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.RETURN_PUZZLE_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.RETURN_PUZZLE_ACHIEVE_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ReturnPuzzleActivity activity = new ReturnPuzzleActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ReturnPuzzleEntity> queryList = HawkDBManager.getInstance()
				.query("from ReturnPuzzleEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ReturnPuzzleEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ReturnPuzzleEntity entity = new ReturnPuzzleEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		ReturnPuzzlePageInfo.Builder builder = genPageInfo(playerId);
		if (builder != null) {
			pushToPlayer(playerId, HP.code.RETURN_PUZZLE_PAGE_INFO_SYNC_VALUE, builder);
		}
	}
	
	/**
	 * 构建主界面信息
	 * @param playerId
	 * @return
	 */
	public ReturnPuzzlePageInfo.Builder genPageInfo(String playerId) {
		Optional<ReturnPuzzleEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return null;
		}
		
		ReturnPuzzleEntity entity = opPlayerDataEntity.get();
		if(entity.getOverTime()==0){//初始化
			initData(playerId);
		}
		long curTime =HawkTime.getMillisecond();
		if( curTime > entity.getStartTime() && curTime < entity.getOverTime() ){			
			//检查下一轮时间是否完成
			if( curTime >= entity.getNextTime()){
				checkNextTime(entity);
			}
		}
		
		ReturnPuzzlePageInfo.Builder builder = ReturnPuzzlePageInfo.newBuilder();
		builder.setScore(entity.getScore());
		builder.setEndTime(entity.getNextTime());
		builder.setPicOrder(entity.getCurDay());
		builder.setBackType(entity.getBackType());
		return builder;
	}
	
	private void checkNextTime(ReturnPuzzleEntity entity){
		//跨天
		int currDay = HawkTime.getCrossDay(HawkTime.getMillisecond(), entity.getStartTime(), 0)+1;
		//取出昨天的成就，重置状态通知刷新
		List<AchieveItem> oldList = entity.getItemList();
		for (AchieveItem achieveItem : oldList) {
			achieveItem.setState(AchieveState.NOT_ACHIEVE_VALUE);
		}
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), oldList), true);
		
		// 需要刷新的普通任务列表
		List<AchieveItem> dayList = new CopyOnWriteArrayList<>();
		
		//触发onlogin是序问题，在初始化时判断是否有每日login,如果有发送消息
		boolean dayTag = false;
		
		List<ReturnPuzzlePicCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(ReturnPuzzlePicCfg.class).toList();
		for (ReturnPuzzlePicCfg cfg : configIterator) {
			if (cfg.getOrder() == currDay) {
				for (int i = 0; i < cfg.getPuzzleAchieveValueList().size(); i++) {
					int id = cfg.getPuzzleAchieveValueList().get(i);
					ReturnPuzzleAchieveCfg c = HawkConfigManager.getInstance().getConfigByKey(ReturnPuzzleAchieveCfg.class,id);
					if(c.getAchieveType().getValue() == AchieveType.LOGIN_DAYS_RETURN_PUZZLE.getValue()){
						dayTag = true;
					}
					AchieveItem item = AchieveItem.valueOf(id);					
					dayList.add(item);					
				}
				break;
			}
		}
		entity.resetItemList(dayList);
		entity.setCurDay(currDay);
		entity.setNextTime(getCurrentOrderTime(entity.getStartTime(),currDay));
		//切换完分享图，清除其他未领取奖励的分享状态，解决红点问题
		
		List<AchieveItem> loseList = new CopyOnWriteArrayList<>();
		for (AchieveItem achieveItem : entity.getShareList()) {
			if(achieveItem.getState() == AchieveState.NOT_REWARD_VALUE){
				achieveItem.setState(AchieveState.NOT_ACHIEVE_VALUE);
				loseList.add(achieveItem);
			} 
		}
		if(!loseList.isEmpty()){
			ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), loseList), true);
		}
		entity.notifyUpdate();
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), dayList), true);

		if(dayTag)
			ActivityManager.getInstance().postEvent(new LoginDayReturnPuzzleEvent(entity.getPlayerId(), 1), true);		
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<ReturnPuzzleEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		ReturnPuzzleEntity entity = opPlayerDataEntity.get();
		
		long curTime = HawkTime.getMillisecond();
		//判定活时是否在有效时间内
		if( !(curTime > entity.getStartTime() && curTime < entity.getOverTime()) ){
			return;
		}
		
		// 重置任务的数据
		if(HawkTime.getMillisecond() >= entity.getNextTime()){			
			checkNextTime(entity);
			syncActivityDataInfo(entity.getPlayerId());
		}
		entity.setLoginDay(entity.getLoginDay()+1);
		entity.notifyUpdate();
		ActivityManager.getInstance().postEvent(new LoginDayReturnPuzzleEvent(playerId, entity.getLoginDay()), true);
	}
	
	@Subscribe
	public void onEventCheckShareEvent(ReturnPuzzleCheckShareEvent event){
//		int orderIdx = event.getShareId();
		
		Optional<ReturnPuzzleEntity> opPlayerDataEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		ReturnPuzzleEntity entity = opPlayerDataEntity.get();
		
		int curId = 0;
		List<ReturnPuzzlePicCfg> configIterator =  HawkConfigManager.getInstance().getConfigIterator(ReturnPuzzlePicCfg.class).toList();
		for (ReturnPuzzlePicCfg returnPuzzlePicCfg : configIterator) {
			if(returnPuzzlePicCfg.getOrder() == entity.getCurDay()){
				curId = returnPuzzlePicCfg.getAchieveId();
				break;
			}
		}
		for (AchieveItem item : entity.getShareList()) {
			if(item.getAchieveId() == curId){
				if(item.getState() == AchieveState.TOOK_VALUE){
					//如已经领取什么都不做
					return;
				}
				
			}
		}
		
		//分享前检查当前时间段内成就是否全完成
		boolean bonusTag = false;
		for (AchieveItem achieveItem : entity.getItemList()) {
			if(achieveItem.getState() != AchieveState.TOOK_VALUE){
				bonusTag = true;
				break;
			}
		}
		//当日任务完成。
		if(bonusTag){			
			sendErrorAndBreak(entity.getPlayerId(), HP.code.HERO_SHARE_C_VALUE, Status.Error.RETURN_PUZZLE_MISSION_FAIL_VALUE);
			return;
		}
		ActivityManager.getInstance().postEvent(ReturnPuzzleShareEvent.valueOf(entity.getPlayerId(),entity.getCurDay()));
		shareSuccess(entity.getPlayerId());
	}
	
	/**
	 * 只有分享成功才会进入该逻辑
	 * @param playerId
	 */
	private void shareSuccess(String playerId){
		Optional<ReturnPuzzleEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		ReturnPuzzleEntity entity = opPlayerDataEntity.get();
		
		
		int curId = 0;
		List<ReturnPuzzlePicCfg> configIterator =  HawkConfigManager.getInstance().getConfigIterator(ReturnPuzzlePicCfg.class).toList();
		for (ReturnPuzzlePicCfg returnPuzzlePicCfg : configIterator) {
			if(returnPuzzlePicCfg.getOrder() == entity.getCurDay()){
				curId = returnPuzzlePicCfg.getAchieveId();
				break;
			}
		}
		AchieveItem achieveItem = null;
		for (AchieveItem item : entity.getShareList()) {
			if(item.getAchieveId() == curId){
				achieveItem = item;
				break;
			}
		}
		//根据order 查找出分享成就
		if(achieveItem==null){
			String result = "";
			for (AchieveItem item : entity.getShareList()) {
				result += item.getAchieveId()+":"+item.getState()+"_";
			}
			logger.info("shareSuccess checked playerId: {},currentAchieveId:{},order:{},shareList:{}",entity.getPlayerId(),curId,entity.getCurDay(),result);
			return;
		}
		if(achieveItem.getState() == AchieveState.TOOK_VALUE){
			return;
		}
		
		int addScore = 1;//分享成功
		entity.setScore(entity.getScore() + addScore);
		ActivityManager.getInstance().getDataGeter().logReturnPuzzleScore(playerId, getActivityTermId(),entity.getScore());
		
		//通知更新主界面
		syncActivityDataInfo(playerId);
		ActivityManager.getInstance().postEvent(new ReturnPuzzleScoreEvent(playerId, entity.getScore()));

		//手动设置成就状态
		List<AchieveItem> items = new ArrayList<>();
		achieveItem.setState(AchieveState.TOOK_VALUE);
		items.add(achieveItem);
		AchieveConfig achieveConfig = this.getAchieveCfg(achieveItem.getAchieveId());
		
		List<RewardItem.Builder> rewardList = this.getRewardList(playerId, achieveConfig);					
		if(!rewardList.isEmpty()){			
			this.getDataGeter().takeReward(playerId, rewardList, 1, Action.RETURN_PUZZLE_ACHIEVE_AWARD, true, RewardOrginType.ACTIVITY_REWARD);
		}
		
		if (!items.isEmpty()) {
			AchievePushHelper.pushAchieveUpdate(playerId, items);
		}
	}
	
	
	
	@Override
	public void onTakeRewardSuccess(String playerId) {
		//完成每日任务，检查任务是否全完成
		boolean bonusTag = true;
		
		Optional<ReturnPuzzleEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		ReturnPuzzleEntity entity = opPlayerDataEntity.get();
		
		for (AchieveItem curAchieveItem : entity.getItemList()) {
			if(curAchieveItem.getState() != AchieveState.TOOK_VALUE){
				bonusTag = false;
				break;
			}
		}
		if(bonusTag){
			
			int curId = 0;
			List<ReturnPuzzlePicCfg> configIterator =  HawkConfigManager.getInstance().getConfigIterator(ReturnPuzzlePicCfg.class).toList();
			for (ReturnPuzzlePicCfg returnPuzzlePicCfg : configIterator) {
				if(returnPuzzlePicCfg.getOrder() == entity.getCurDay()){
					curId = returnPuzzlePicCfg.getAchieveId();
					break;
				}
			}
			
			if(curId!=0){				
				for (AchieveItem dayAchieveItem : entity.getShareList()) {
					if(dayAchieveItem.getAchieveId() == curId && dayAchieveItem.getState()==AchieveState.NOT_ACHIEVE_VALUE){
						List<AchieveItem> items = new ArrayList<>();
						dayAchieveItem.setState(AchieveState.NOT_REWARD_VALUE);//设置宝箱状态
						items.add(dayAchieveItem);
						entity.notifyUpdate();
						AchievePushHelper.pushAchieveUpdate(entity.getPlayerId(), items);
						logger.info("onEventCheckShareEvent checked playerId: {}",entity.getPlayerId());
						break;
					}
				}
			}
		}
	}
	
	/**
	 * 检查参数
	 * @param backFlowPlayer
	 * @param entity
	 * @return
	 */
	public boolean checkFitLostParams(BackFlowPlayer backFlowPlayer,ReturnPuzzleEntity entity) {
		if(backFlowPlayer.getBackCount() <= entity.getBackCount()){
			logger.info("checkFitLostParams failed, BackCount data fail , playerId:{},backCount:{},entityBackCount:{}", backFlowPlayer.getPlayerId(),
					backFlowPlayer.getBackCount(),entity.getBackCount());
			return false;
		}
		long backTime = backFlowPlayer.getBackTimeStamp();
		//如果在活动中，只更新期数，不更新其他数据
		if(backTime < entity.getOverTime() && backTime > entity.getStartTime()){
			entity.setBackCount(backFlowPlayer.getBackCount());
			entity.notifyUpdate();
			logger.info("checkFitLostParams failed,in activity, playerId: {},backCount:{},backTime:{}", entity.getPlayerId(),entity.getBackCount(),backTime);
			return false;
		}
		//停止触发，只更新期数，不更新其他数据
		if(!this.canTrigger(backTime)){
			entity.setBackCount(backFlowPlayer.getBackCount());
			entity.notifyUpdate();
			logger.info("checkFitLostParams failed,can not Trigger, playerId: {},backCount:{},backTime:{}", entity.getPlayerId(),entity.getBackCount(),backTime);
			return false;
		}
		int lossDays = backFlowPlayer.getLossDays();
		logger.info("checkFitLostParams sucess,playerId:{},loss:{}", backFlowPlayer.getPlayerId(),lossDays);
		return true;
	}
	
	/**
	 * 是否可以触发
	 * @return
	 */
	public boolean canTrigger(long backTime){
		int termId = this.getActivityTermId();
		ReturnPuzzleTimeCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(ReturnPuzzleTimeCfg.class, termId);
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
	 * 获取活动持续时间
	 * @param backFlowPlayer
	 * @return
	 */
	public ReturnPuzzleDateCfg getBackDateCfg(BackFlowPlayer backFlowPlayer){
		List<ReturnPuzzleDateCfg> congfigs = HawkConfigManager.getInstance().
				getConfigIterator(ReturnPuzzleDateCfg.class).toList();
		for(ReturnPuzzleDateCfg cfg : congfigs){
			if(cfg.isAdapt(backFlowPlayer)){
				return cfg;
			}
		}
		return null;
	}
	
	@Override
	public boolean isHidden(String playerId) {
		Optional<ReturnPuzzleEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return true;
		}
		long curTime = HawkTime.getMillisecond();
		ReturnPuzzleEntity entity = optional.get();
		if(curTime > entity.getOverTime() || curTime < entity.getStartTime()){
			return true;
		}
		return super.isHidden(playerId);
	}	
	
	@Override
	public void onPlayerMigrate(String playerId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// TODO Auto-generated method stub
		
	}
}
