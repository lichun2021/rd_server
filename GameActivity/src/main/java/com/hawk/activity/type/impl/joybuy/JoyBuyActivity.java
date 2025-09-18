package com.hawk.activity.type.impl.joybuy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayJoyBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.joybuy.cfg.JoyBuyAchieveCfg;
import com.hawk.activity.type.impl.joybuy.cfg.JoyBuyExchangeActivityKVCfg;
import com.hawk.activity.type.impl.joybuy.cfg.JoyBuyExchangeCfg;
import com.hawk.activity.type.impl.joybuy.entity.ActivityJoyBuyEntity;
import com.hawk.activity.type.impl.joybuy.entity.JoyBuyExchangeItem;
import com.hawk.game.protocol.Activity.JoyBuyExchangeListResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

public class JoyBuyActivity extends ActivityBase implements AchieveProvider {

	public JoyBuyActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.JOY_BUY_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.JOY_BUY_ACHIEVE_REWARD;
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		JoyBuyActivity activity = new JoyBuyActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityJoyBuyEntity> queryList = HawkDBManager.getInstance()
				.query("from ActivityJoyBuyEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ActivityJoyBuyEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityJoyBuyEntity entity = new ActivityJoyBuyEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpenForPlayer(String playerId) {
		Optional<ActivityJoyBuyEntity> opEntity = getPlayerDataEntity(playerId);
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
		Optional<ActivityJoyBuyEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		ActivityJoyBuyEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<JoyBuyAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(JoyBuyAchieveCfg.class);
		while (configIterator.hasNext()) {
			JoyBuyAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		
		//刷新兑换列表
		refreshExchange(entity);
		//下次刷新时间点
		entity.setExchangeNextTime(setNextRefreshTime());
		
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		ActivityManager.getInstance().postEvent(new LoginDayJoyBuyEvent(playerId, 1), true);
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void clearExtraAchieveInfo(String playerId) {
		Optional<ActivityJoyBuyEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		ActivityJoyBuyEntity entity = opEntity.get();
		List<AchieveItem> items = entity.getItemList();
		Map<Integer, AchieveItem> itemMap = new HashMap<>();
		for (AchieveItem item : items) {
			int achieveId = item.getAchieveId();
			if (itemMap.containsKey(achieveId)) {
				AchieveItem oldItem = itemMap.get(achieveId);
				if (item.getState() > oldItem.getState()) {
					itemMap.put(achieveId, item);
				}
			} else {
				itemMap.put(achieveId, item);
			}
		}
		List<AchieveItem> newList = new ArrayList<>(itemMap.values());
		entity.setItemList(newList);
		entity.notifyUpdate();
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
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
		Optional<ActivityJoyBuyEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		ActivityJoyBuyEntity playerDataEntity = opPlayerDataEntity.get();
		ConfigIterator<JoyBuyAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(JoyBuyAchieveCfg.class);
		int achieveSize = configIterator.size();
		if (playerDataEntity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		} else if (playerDataEntity.getItemList().size() > achieveSize) {
			clearExtraAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(JoyBuyAchieveCfg.class, achieveId);
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
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		exchangeList(playerId);
		if (!event.isCrossDay()) {
			return;
		}
		Optional<ActivityJoyBuyEntity> opEntity = getPlayerDataEntity(playerId);
		long now = HawkTime.getMillisecond();
		ActivityJoyBuyEntity entity = opEntity.get();
		
		if (event.isCrossDay()){
			List<AchieveItem> items = new ArrayList<>();
			ConfigIterator<JoyBuyAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(JoyBuyAchieveCfg.class);
			while (achieveIterator.hasNext()) {
				JoyBuyAchieveCfg cfg = achieveIterator.next();
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				items.add(item);
			}
			entity.setItemList(items);
			// 推送给客户端
			AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
		}
		
		if (event.isCrossDay() && !HawkTime.isSameDay(entity.getLoginRefreshTime(), now)) {
			entity.setExchangeNumber(0);
			entity.setLoginDays(entity.getLoginDays() + 1);
			entity.setLoginRefreshTime(now);
		}
		ActivityManager.getInstance().postEvent(new LoginDayJoyBuyEvent(playerId, entity.getLoginDays()), true);
	}

	//设置下次刷新时间
	public long setNextRefreshTime(){
		JoyBuyExchangeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(JoyBuyExchangeActivityKVCfg.class);
		int[] time_ary = cfg.getResetTimeAry();
//		9_12_18_21
		int day = 0;
		int hour= 0;
		boolean tag = false;
		Calendar calendar = HawkTime.getCalendar(false);
		int val = calendar.get(Calendar.HOUR_OF_DAY);
		for (int i = 0; i < time_ary.length; i++) {
			if(i+1 >= time_ary.length)
				break;
			if(val>=time_ary[i] && val<=time_ary[i+1]){
				tag = true;
				hour = time_ary[i+1];
				if(hour == val){
					//时间点在边界需要特殊处理
					if(i+2 < time_ary.length){
						hour = time_ary[i+2];
					}else{
						day = 1;
						hour = time_ary[0];
					}
				}
				break;
			}
		}
		if(!tag){
			//最大时间的下一轮需要添加1天
			if(val>time_ary[time_ary.length-1]){
				day = 1;
			}
			hour = time_ary[0];
		}
		
		if(day>0){
			calendar.add(Calendar.DATE, day);
		}
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}
	
	
	/**
	 * 随机奖池
	 * @return groupId
	 */
	public List<JoyBuyExchangeCfg> randomGroup(){
		JoyBuyExchangeActivityKVCfg sysCfg = HawkConfigManager.getInstance().getKVInstance(JoyBuyExchangeActivityKVCfg.class);
		int poolList[][] = sysCfg.getExchangePoolList();
		List<JoyBuyExchangeCfg> exchangeList = new ArrayList<>();
		
		for (int[] val : poolList) {
			int poolId =val[0];
			int poolSize=val[1];//次数
			
			List<JoyBuyExchangeCfg> exchangeCfgs = new ArrayList<>( JoyBuyExchangeCfg.getGroupExchange(poolId));
			for(int i = 0;i< poolSize;i++){
				if(exchangeCfgs.isEmpty()){
					break;
				}
				
				JoyBuyExchangeCfg cfg = HawkRand.randomWeightObject(exchangeCfgs);
				exchangeList.add(cfg);
				exchangeCfgs.remove(cfg);
			}
		}
		
		Collections.shuffle(exchangeList);
		return exchangeList;
		
	}
	
	public void refreshExchange(ActivityJoyBuyEntity playerDataEntity){
		playerDataEntity.getExchangeObjectList().clear();
		List<JoyBuyExchangeCfg> list = randomGroup();
		for (JoyBuyExchangeCfg cfg : list) {
			JoyBuyExchangeItem item = new JoyBuyExchangeItem();
			item.setExchangeId(cfg.getId());
			item.setExchangeSingeCurNumber(0);
			playerDataEntity.getExchangeObjectList().add(item);
		}
		playerDataEntity.notifyUpdate();
	}
	
	//兑换列表
	public void exchangeList(String playerId){
		Optional<ActivityJoyBuyEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return;
		}
		ActivityJoyBuyEntity playerDataEntity = opPlayerDataEntity.get();
		long currentTime = HawkTime.getMillisecond();
		if(currentTime > playerDataEntity.getExchangeNextTime()){
			//下次刷新时间点
			playerDataEntity.setExchangeNextTime(setNextRefreshTime());
			playerDataEntity.setExchangeRefreshNum(0);
			//刷新兑换列表
			refreshExchange(playerDataEntity);
			playerDataEntity.notifyUpdate();
		}
		
		JoyBuyExchangeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(JoyBuyExchangeActivityKVCfg.class);
		JoyBuyExchangeListResp.Builder builder = JoyBuyExchangeListResp.newBuilder();
		builder.setExchangeCurNumber(playerDataEntity.getExchangeNumber());
		builder.setExchangeMaxNumber(cfg.getDailyExchangeTimes());
		builder.setRemRefreshNumber( cfg.getRefreshCostList().size()-playerDataEntity.getExchangeRefreshNum() );
		builder.setNextRefreshTime(playerDataEntity.getExchangeNextTime());
		for (JoyBuyExchangeItem item : playerDataEntity.getExchangeObjectList()) {
			builder.addInfos(item.totoBuilder());
		}
		pushToPlayer(playerId, HP.code.JOY_BUY_EXCHANGE_LIST_RESP_VALUE, builder);
	}
	
	public void exchangeOperation(String playerId,int exchangeId,int exchangeNumber){
		Optional<ActivityJoyBuyEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.JOY_BUY_EXCHANGE_OPERATION_REQ_VALUE,
					Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			return;
		}
		ActivityJoyBuyEntity playerDataEntity = opPlayerDataEntity.get();
		
		JoyBuyExchangeActivityKVCfg sysCfg = HawkConfigManager.getInstance().getKVInstance(JoyBuyExchangeActivityKVCfg.class);
		if(playerDataEntity.getExchangeNumber()>=sysCfg.getDailyExchangeTimes()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.JOY_BUY_EXCHANGE_OPERATION_REQ_VALUE,
					Status.Error.JOY_BUY_EXCHANGE_DAY_NUMBER_OVERTOP_VALUE);
			return;
		}
		JoyBuyExchangeItem operItem = null;
		for (JoyBuyExchangeItem item : playerDataEntity.getExchangeObjectList()) {
			if(item.getExchangeId() == exchangeId){
				operItem = item;
				break;
			}
		}
		//异常 未查找到兑换数据
		if(operItem == null){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.JOY_BUY_EXCHANGE_OPERATION_REQ_VALUE,Status.Error.JOY_BUY_EXCHANGE_NOT_EXIST_VALUE);
			return;
		}
		JoyBuyExchangeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(JoyBuyExchangeCfg.class, operItem.getExchangeId());
		if(operItem.getExchangeSingeCurNumber()+exchangeNumber> cfg.getTimes()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.JOY_BUY_EXCHANGE_OPERATION_REQ_VALUE,Status.Error.JOY_BUY_EXCHANGE_NUMBER_OVERTOP_VALUE);
			return;
		}
		
		//消耗检查
//		boolean flag = this.getDataGeter().consumeItems(playerId, cfg.getNeedItemList(),HP.code.JOY_BUY_EXCHANGE_OPERATION_RESP_VALUE, Action.JOY_BUY_CONSUME);
		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), exchangeNumber, Action.JOY_BUY_CONSUME, true);
		if (!flag) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.JOY_BUY_EXCHANGE_OPERATION_REQ_VALUE,Status.Error.JOY_BUY_EXCHANGE_COST_ITEM_ERROR_VALUE);
			return;
		}

		// 发奖
		this.getDataGeter().takeReward(playerId, cfg.getGainItemItemList(), exchangeNumber,
				Action.JOY_BUY_REWARD, true, RewardOrginType.JOY_BUY_EXCHANGE_REWARD);
		operItem.addExchangeSingeCurNumber(exchangeNumber);
		playerDataEntity.setExchangeNumber(playerDataEntity.getExchangeNumber()+exchangeNumber);
		
		responseSuccess(playerId, HP.code.JOY_BUY_EXCHANGE_OPERATION_RESP_VALUE);
		
		exchangeList(playerId);
	}
	
	public void exchangeRefresh(String playerId){
		Optional<ActivityJoyBuyEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.JOY_BUY_EXCHANGE_REFRESH_REQ_VALUE,
			Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			return;
		}
		ActivityJoyBuyEntity playerDataEntity = opPlayerDataEntity.get();
		JoyBuyExchangeActivityKVCfg sysCfg = HawkConfigManager.getInstance().getKVInstance(JoyBuyExchangeActivityKVCfg.class);
		
		if(playerDataEntity.getExchangeRefreshNum() >= sysCfg.getRefreshCostList().size()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.JOY_BUY_EXCHANGE_REFRESH_REQ_VALUE,Status.Error.JOY_BUY_EXCHANGE_REFRESH_NUMBER_OVERTOP_VALUE);
			return;
		}
		
		RewardItem.Builder costBuilder =  sysCfg.getRefreshCostList().get(playerDataEntity.getExchangeRefreshNum());
		
		//消耗检查
		boolean flag = this.getDataGeter().consumeItems(playerId,Arrays.asList(costBuilder),HP.code.JOY_BUY_EXCHANGE_REFRESH_RESP_VALUE,Action.JOY_BUY_REFRESHCOST_CONSUME);
		if (!flag) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.JOY_BUY_EXCHANGE_REFRESH_REQ_VALUE,Status.Error.JOY_BUY_EXCHANGE_REFRESH_COST_ERROR_VALUE);
			return;
		}
		
		playerDataEntity.setExchangeRefreshNum(playerDataEntity.getExchangeRefreshNum()+1);
		responseSuccess(playerId, HP.code.JOY_BUY_EXCHANGE_REFRESH_RESP_VALUE);
		//刷新兑换列表
		refreshExchange(playerDataEntity);
		
		//刷新列表
		exchangeList(playerId);
	}
}
