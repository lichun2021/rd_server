package com.hawk.activity.type.impl.exchangeDecorate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.exchangeDecorate.cfg.ExchangeDecorateAchieveCfg;
import com.hawk.activity.type.impl.exchangeDecorate.cfg.ExchangeDecorateActivityKVCfg;
import com.hawk.activity.type.impl.exchangeDecorate.cfg.ExchangeDecorateLevelExpCfg;
import com.hawk.activity.type.impl.exchangeDecorate.cfg.ExchangeDecoratePackageCfg;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ActivityExchangeDecorateEntity;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ExchangeDecorateInfo;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ExchangeDecorateLimitInfo;
import com.hawk.activity.type.impl.exchangeDecorate.impl.IExchangeCommon;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.DecorateExchangeItemInfo;
import com.hawk.game.protocol.Activity.DecorateExchangeLevelInfo;
import com.hawk.game.protocol.Activity.DecorateExchangeLevelListResp;
import com.hawk.game.protocol.Activity.DecorateExchangeLimitTimeInfo;
import com.hawk.game.protocol.Activity.DecorateExchangeLimitTimeListResp;
import com.hawk.game.protocol.Activity.DecorateExchangeListResp;
import com.hawk.game.protocol.Activity.ExchangeCommonType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

public class ExchangeDecorateActivity extends ActivityBase implements AchieveProvider {
	public ExchangeDecorateActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	/**
	 * 获取本期活动的当前周数
	 * @return
	 */
	private int getBetweenWeeks() { 
		int termId = getActivityTermId();
		long startTime = getTimeControl().getStartTimeByTermId(termId);
		long baseTime = ActivityConst.BASE_MONDAY_TIME;
		long weekTime = ActivityConst.WEEK_MILLI_SECONDS;
		long startWeek = (startTime - baseTime) / weekTime;
		long currWeek = (HawkTime.getMillisecond() - baseTime) / weekTime;
		int cur = (int) Math.max(0, currWeek - startWeek + 1);
		if(cur > ExchangeDecorateAchieveCfg.maxCycle){
			cur = ExchangeDecorateAchieveCfg.maxCycle;
		}
		return cur;
	}
	
	@Override
	public boolean isActivityClose(String playerId) {
		return checkCiytLevel(playerId);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.EXCHANGE_DECORATE_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.EXCHANGE_DECORATE_REWARD;
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ExchangeDecorateActivity activity = new ExchangeDecorateActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityExchangeDecorateEntity> queryList = HawkDBManager.getInstance()
				.query("from ActivityExchangeDecorateEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ActivityExchangeDecorateEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityExchangeDecorateEntity entity = new ActivityExchangeDecorateEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpenForPlayer(String playerId) {
		Optional<ActivityExchangeDecorateEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		initAchieveInfo(playerId);
	}

	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<ActivityExchangeDecorateEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		ActivityExchangeDecorateEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getAchieveDayList().isEmpty()) {
			return;
		}
		int curWeek = getBetweenWeeks();
		// 初始添加成就项
		List<AchieveItem> items = initAchieiveData(true,curWeek);
		entity.setWeekNum(curWeek);
		entity.setAchieveDayList(items);
		entity.setAchieveDayRefreshTime(HawkTime.getNextAM0Date());
		
		List<AchieveItem> allItems = new ArrayList<>();
		allItems.addAll(items);
		
		List<AchieveItem> weekList = initAchieiveData(false,curWeek);
		entity.setAchieveWeekList(weekList);
		entity.setAchieveWeekRefreshTime(HawkTime.getYearWeek());
		
		allItems.addAll(weekList);
		
		//需要初始等级数据
		initLevelData(entity);
		
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, allItems), true);
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		//发送活动首页数据
		Optional<ActivityExchangeDecorateEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		ActivityExchangeDecorateEntity entity = opEntity.get();
		DecorateExchangeLevelListResp.Builder builder = DecorateExchangeLevelListResp.newBuilder();
		builder.setLevel(entity.getLevel());
		builder.setExp(entity.getExp());
		builder.setBuyLevelExpNum(entity.getWeekBuyExpNum());
		builder.setWeekIdx(entity.getWeekNum());
		for (ExchangeDecorateInfo levelInfo : entity.getLevelRewardList()) {
			DecorateExchangeLevelInfo.Builder levelBuilder = DecorateExchangeLevelInfo.newBuilder();
			levelBuilder.setLevelId(levelInfo.getLevelId());
			levelBuilder.setState(levelInfo.getState());
			builder.addInfos(levelBuilder);
		}
		
		this.getDataGeter().sendProtocol(playerId, HawkProtocol.valueOf(HP.code.DECORATE_EXCHANGE_LEVEL_LIST_RESP_VALUE, builder));
	}

	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		//TODO监测达成状态。手动领取
		if(checkCiytLevel(playerId)){
			return Result.success();
		}
		//非可领取状态直接返回
		if(achieveItem.getState() != AchieveState.NOT_REWARD_VALUE){
			return Result.success();
		}
		//修改成已经领取状态，下面发过exp奖励
		achieveItem.setState(AchieveState.TOOK_VALUE);
		
		Optional<ActivityExchangeDecorateEntity> opEntity = getPlayerDataEntity(playerId);
		ActivityExchangeDecorateEntity entity = opEntity.get();
		
		int addExp = 0;
		ExchangeDecorateAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ExchangeDecorateAchieveCfg.class, achieveItem.getAchieveId());
		addExp = cfg.getExp();
		if(cfg != null){
			addExp(entity,addExp);
			syncActivityDataInfo(playerId);
		}
		
		this.getDataGeter().logExchangeDecorateMission(playerId, entity.getTermId(), achieveItem.getAchieveId());
		this.getDataGeter().logExchangeDecorateLevel(playerId, entity.getTermId(), addExp, entity.getLevel(), entity.getExp());		
		return Result.success();
	}
	
	public void addExp(ActivityExchangeDecorateEntity entity, int addExp){
		//增加经验返回升级标记。后续要使用该标记验证
		int oldLevel = entity.getLevel(); 
		entity.increaseExp(addExp);
		//升级成功
		if(entity.getLevel()>oldLevel){
			//检查开启特惠
			openLevelLimitReward(entity,oldLevel);
			boolean onUp = false;
			for (int curLevel = oldLevel+1; curLevel <= entity.getLevel()+1; curLevel++) {
				//等级有变化
				if(onLockLevelData(entity,curLevel)){
					onUp = true;
					//将比当低的等级修改可领取
					for (ExchangeDecorateInfo info : entity.getLevelRewardList()) {
						if(info.getLevelId()<=entity.getLevel() && info.getState()!=3 ){
							info.setStateReward();
						}
					}
				}
			}
			
			//满级
			if(entity.getLevel()>= ExchangeDecorateLevelExpCfg.getMaxLevel()){
				ExchangeDecorateInfo info = entity.getLevelRewardList().get(entity.getLevelRewardList().size()-1);
				if(info!=null){
					onUp = true;
					info.setStateReward();
				}
			}
			
			if(onUp){
				syncActivityDataInfo(entity.getPlayerId());
				entity.notifyUpdate();
			}
		}
	}
	
	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return !isHidden(playerId);
	}

	/**
	 * 判断建筑工厂等级
	 * @return
	 */
	public boolean checkCiytLevel(String playerId){
		int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
		ExchangeDecorateActivityKVCfg sysCfg = HawkConfigManager.getInstance().getKVInstance(ExchangeDecorateActivityKVCfg.class);
		return cityLevel<sysCfg.getUnlockLevel();
	}
	
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		if(checkCiytLevel(playerId)){
			return Optional.empty();
		}
		Optional<ActivityExchangeDecorateEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		ActivityExchangeDecorateEntity playerDataEntity = opPlayerDataEntity.get();
		if (playerDataEntity.getAchieveDayList().isEmpty()) {
			initAchieveInfo(playerId);
		} 
		checkLoginTime(playerDataEntity, HawkTime.getMillisecond());
		
		AchieveItems items = new AchieveItems(playerDataEntity.getAchieveList(), playerDataEntity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(ExchangeDecorateAchieveCfg.class, achieveId);
		return config;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if(checkCiytLevel(playerId)){
			return;
		}
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		syncActivityDataInfo(playerId);
		exchangeLimitTimeList(playerId);
		exchangeList(playerId);
		if (!event.isCrossDay()) {
			return;
		}
		
		Optional<ActivityExchangeDecorateEntity> opEntity = getPlayerDataEntity(playerId);
		long now = HawkTime.getMillisecond();
		ActivityExchangeDecorateEntity entity = opEntity.get();
		if (event.isCrossDay() && !HawkTime.isSameDay(entity.getLoginRefreshTime(), now)) {
			entity.setLoginDays(entity.getLoginDays() + 1);
			entity.setLoginRefreshTime(now);
		}
		checkLoginTime(entity, HawkTime.getMillisecond());
	}
	
	private void checkLoginTime(ActivityExchangeDecorateEntity entity,long now){
		int newWeek = HawkTime.getYearWeek();
		boolean tag = false;
		int curWeek = getBetweenWeeks();
		//week
		if(newWeek!=entity.getAchieveWeekRefreshTime() && curWeek != entity.getWeekNum()){
			entity.setAchieveWeekRefreshTime( newWeek );
			entity.setWeekNum(curWeek);
			entity.setWeekBuyExpNum(0);
			List<AchieveItem> items = initAchieiveData(false,curWeek);
			entity.setAchieveWeekList(items);
			AchievePushHelper.pushAchieveUpdate(entity.getPlayerId(), items);
			tag = true;
		}
		//init未初始化成功 weekList与time 未初始成功。追加处理 
		if(entity.getAchieveWeekList().isEmpty()){
			entity.setAchieveWeekRefreshTime( newWeek );
			entity.setWeekNum(curWeek);
			List<AchieveItem> items = initAchieiveData(false,curWeek);
			entity.setAchieveWeekList(items);
			AchievePushHelper.pushAchieveUpdate(entity.getPlayerId(), items);
			tag = true;
		}
		//day刷新
		if(now>entity.getAchieveDayRefreshTime()){
			List<AchieveItem> items = initAchieiveData(true,curWeek);
			entity.setAchieveDayRefreshTime(HawkTime.getNextAM0Date());
			entity.setAchieveDayList(items);
			AchievePushHelper.pushAchieveUpdate(entity.getPlayerId(), items);
			tag = true;
		}
		//初始化1级不存在 修复
		if(!entity.getLevelRewardList().isEmpty()){
			ExchangeDecorateInfo info = entity.getLevelRewardList().get(0);
			
			ConfigIterator<ExchangeDecorateLevelExpCfg> levelExpIterator = HawkConfigManager.getInstance().getConfigIterator(ExchangeDecorateLevelExpCfg.class);
			List<ExchangeDecorateLevelExpCfg> list = levelExpIterator.toList();
			ExchangeDecorateLevelExpCfg cfg = list.get(0);
			//表示初始化不成功 检查level=1是否存在
			if(info.getLevelId() != cfg.getLevel()){
				ExchangeDecorateInfo newInfo = new ExchangeDecorateInfo();
				newInfo.setLevelId(cfg.getLevel());
				newInfo.setStateReward();
				
				List<ExchangeDecorateInfo> newRewardList = new CopyOnWriteArrayList<>();
				newRewardList.add(newInfo);
				
				cfg = list.get(1);
				if(info.getLevelId() != cfg.getLevel()){
					ExchangeDecorateInfo twoInfo = new ExchangeDecorateInfo();
					twoInfo.setLevelId(cfg.getLevel());
					twoInfo.setStateReward();
					newRewardList.add(twoInfo);
				}
				
				newRewardList.addAll(entity.getLevelRewardList());
				entity.setLevelRewardList(newRewardList);
				entity.notifyUpdate();
			}
		}else{
			//数据空的
			initLevelData(entity);
		}
		if(tag){
			syncActivityDataInfo(entity.getPlayerId());
		}
		checkMission(entity);
	}
	
	private void checkMission(ActivityExchangeDecorateEntity entity){
		//TODO 配合装扮修复数据使用
		//第1期活动，第3周 周任务
		if(getActivityTermId() == 1 && entity.getWeekNum() == 3 && getDataGeter().checkCyborgWar(entity.getPlayerId())){
			for (AchieveItem achieveItem : entity.getAchieveWeekList()) {
				if(achieveItem.getAchieveId() == 18930011 && achieveItem.getState() != AchieveState.TOOK_VALUE ){//指定任务 未完成
					achieveItem.setValue(0, 1);
					achieveItem.setState(AchieveState.NOT_REWARD_VALUE);
					onAchieveFinished(entity.getPlayerId(), achieveItem);
				}
			}
		}
	}
	
	/**
	 * 初始等级数据
	 */
	private void initLevelData(ActivityExchangeDecorateEntity entity){
		ConfigIterator<ExchangeDecorateLevelExpCfg> levelExpIterator = HawkConfigManager.getInstance().getConfigIterator(ExchangeDecorateLevelExpCfg.class);
		List<ExchangeDecorateLevelExpCfg> list = levelExpIterator.toList();
		for (int i = 0; i < 2; i++) {
			ExchangeDecorateLevelExpCfg cfg = list.get(i);
			ExchangeDecorateInfo info = new ExchangeDecorateInfo();
			info.setLevelId(cfg.getLevel());
			if(i == 0){
				info.setStateReward();
				entity.setExp(cfg.getLevelUpExp());
				entity.setLevel(1);
				openLevelLimitReward(entity,0);
			}else if(i==1){
				info.setStateInit();
			}
			entity.addLevelReward(info);
		}
	}
	
	
	/**
	 * 开启特惠奖励
	 * @param entity
	 */
	private void openLevelLimitReward(ActivityExchangeDecorateEntity entity,int oldLevel){
		int level = entity.getLevel();
		
		ConfigIterator<ExchangeDecoratePackageCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(ExchangeDecoratePackageCfg.class);
		List<ExchangeDecoratePackageCfg> list = cfgs.toList();
		
		List<ExchangeDecoratePackageCfg> tmpList = new ArrayList<>();
		
		for (int i = oldLevel+1; i <= entity.getLevel(); i++) {
			for (ExchangeDecoratePackageCfg cfg : list) {
				if(cfg.getNeedLevel() == i){
					tmpList.add(cfg);
				}
			}
		}
		
		if(!tmpList.isEmpty()){
			List<ExchangeDecorateLimitInfo> openList = new ArrayList<>();
			for (int i = oldLevel+1; i <= entity.getLevel(); i++) {
				ExchangeDecorateLimitInfo limitInfo = new ExchangeDecorateLimitInfo();
				limitInfo.setLevelId(i);
				if(entity.getLevelOpenExchangeList().contains(limitInfo)){
					continue;
				}
				for (ExchangeDecoratePackageCfg cfg : tmpList) {
					if(cfg.getNeedLevel()!=i){
						continue;
					}
					ExchangeDecorateInfo info = new ExchangeDecorateInfo();
					info.setLevelId(cfg.getId());
					info.setState(0);
					limitInfo.getInfos().add(info);
				}
				if(!limitInfo.getInfos().isEmpty()){
					openList.add(limitInfo);
				}
			}
			entity.getLevelOpenExchangeList().addAll(openList);
			
			exchangeLimitTimeList(entity.getPlayerId());
		}
		
	}
	
	/**
	 * 解锁等级数据
	 */
	private boolean onLockLevelData(ActivityExchangeDecorateEntity entity,int newLevel){
		boolean openTag = false;
		ConfigIterator<ExchangeDecorateLevelExpCfg> levelExpIterator = HawkConfigManager.getInstance().getConfigIterator(ExchangeDecorateLevelExpCfg.class);
		List<ExchangeDecorateLevelExpCfg> list = levelExpIterator.toList();
		for (ExchangeDecorateLevelExpCfg cfg : list) {
			if(cfg.getLevel() == newLevel){
				ExchangeDecorateInfo info = new ExchangeDecorateInfo();
				info.setLevelId(cfg.getLevel());
				info.setStateInit();
				if(!entity.getLevelRewardList().contains(info)){
					entity.addLevelReward(info);
					openTag = true;
				}
				break;
			}
		}
		return openTag;
	}
	
	/**
	 * @param isWeek true=day,false=周
	 * @param weekNumber 每x周
	 * @return 
	 */
	private List<AchieveItem> initAchieiveData(boolean isDay,int weekNumber){
		List<AchieveItem> items = new ArrayList<>();
		ConfigIterator<ExchangeDecorateAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(ExchangeDecorateAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			ExchangeDecorateAchieveCfg cfg = achieveIterator.next();
			if(cfg.getCycle() == weekNumber && cfg.isQuestDay()==isDay){
				items.add(AchieveItem.valueOf(cfg.getAchieveId()));
			}
		}
		return items;
	}

	/**
	 * 等级奖励
	 */
	public void exchangeLevelReward(String playerId,int levelId){
		if(checkCiytLevel(playerId)){
			return;
		}
		if (!isOpening(playerId)) {
			return;
		}
		Optional<ActivityExchangeDecorateEntity> opEntity = getPlayerDataEntity(playerId);
		ActivityExchangeDecorateEntity entity = opEntity.get();
		ExchangeDecorateInfo opInfo = null;
		for (ExchangeDecorateInfo levelInfo : entity.getLevelRewardList()) {
			if(levelInfo.getLevelId() == levelId){
				opInfo = levelInfo;
				break;
			}
		}
		if(opInfo == null){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.DECORATE_EXCHANGE_LEVEL_LIST_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_LEVEL_REWARD_YEST_VALUE);
			return;
		}
		if(opInfo.isStateReward()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.DECORATE_EXCHANGE_LEVEL_LIST_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_LEVEL_REWARD_YEST_VALUE);
			return;
		}
		ExchangeDecorateLevelExpCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ExchangeDecorateLevelExpCfg.class, opInfo.getLevelId());
		if(cfg == null){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.DECORATE_EXCHANGE_LEVEL_LIST_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_LEVEL_REWARD_ID_NOT_FIND_VALUE);
			return;
		}
		
		opInfo.setStateSuccess();
		entity.notifyUpdate();
		
		this.getDataGeter().takeReward(playerId, cfg.getNormalItemList(), 1,
				Action.EXCHANGE_DECORATE_LEVEL_REWARD, true, RewardOrginType.EXCHANGE_DECORATE_LEVEL_REWARD);
		
		syncActivityDataInfo(playerId);
	}
	/**
	 * 限时列表
	 */
	public void exchangeLimitTimeList(String playerId){
		Optional<ActivityExchangeDecorateEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityExchangeDecorateEntity entity = opEntity.get();
		if(!entity.getLevelOpenExchangeList().isEmpty()){
			//发送已经购买的id->数量
			DecorateExchangeLimitTimeListResp.Builder builder = DecorateExchangeLimitTimeListResp.newBuilder();
			for (ExchangeDecorateLimitInfo limitInfo: entity.getLevelOpenExchangeList()) {
				DecorateExchangeLimitTimeInfo.Builder limitInfoBuilder = DecorateExchangeLimitTimeInfo.newBuilder();
				limitInfoBuilder.setLevelId(limitInfo.getLevelId());
				for (ExchangeDecorateInfo info: limitInfo.getInfos()) {
					DecorateExchangeItemInfo.Builder itemInfoBuilder =  DecorateExchangeItemInfo.newBuilder();
					itemInfoBuilder.setItemId(info.getLevelId());
					itemInfoBuilder.setItemNum(info.getState());
					limitInfoBuilder.addInfos(itemInfoBuilder);
				}
				builder.addInfos(limitInfoBuilder);
			}
			pushToPlayer(playerId, HP.code.DECORATE_EXCHANGE_LIMIT_TIME_RESP_VALUE, builder);
		}else{
			responseSuccess(playerId, HP.code.DECORATE_EXCHANGE_LIMIT_TIME_RESP_VALUE);
		}
		
	}
	/**
	 * 通用奖励操作
	 */
	public void exchangeCommonReward(String playerId,ExchangeCommonType act,List<Integer> params){
		Optional<ActivityExchangeDecorateEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityExchangeDecorateEntity entity = opEntity.get();
		IExchangeCommon iExchangeCommon = IExchangeCommon.getExchangeType(act);
		iExchangeCommon.exchangeCommon(entity, params);
		
		responseSuccess(playerId, HP.code.EXCHANGE_COMMON_RESP_VALUE);
	}
	/**
	 * 装扮列表
	 */
	public void exchangeList(String playerId){
		Optional<ActivityExchangeDecorateEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		ActivityExchangeDecorateEntity entity = opEntity.get();
		if(!entity.getDecorateList().isEmpty()){
			DecorateExchangeListResp.Builder builder = DecorateExchangeListResp.newBuilder();
			
			for (ExchangeDecorateInfo info : entity.getDecorateList()) {
				DecorateExchangeItemInfo.Builder infoBuilder = DecorateExchangeItemInfo.newBuilder();
				infoBuilder.setItemId(info.getLevelId());
				infoBuilder.setItemNum(info.getState());
				builder.addInfos(infoBuilder);
			}
			pushToPlayer(playerId, HP.code.DECORATE_EXCHANGE_LIST_RESP_VALUE, builder);
		}else{
			responseSuccess(playerId, HP.code.DECORATE_EXCHANGE_LIST_RESP_VALUE);
		}
		
	}	
	
}
