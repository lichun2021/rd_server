package com.hawk.activity.type.impl.backSoldierExchange;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.backSoldierExchange.cfg.BackSoldierExchangeAchieveCfg;
import com.hawk.activity.type.impl.backSoldierExchange.cfg.BackSoldierExchangeKVCfg;
import com.hawk.activity.type.impl.backSoldierExchange.entity.BackSoldierExchangeEntity;
import com.hawk.activity.type.impl.soldierExchange.cfg.SoldierExchangeActivityTimeCfg;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SoldierExchange.PBSEActivitySync;
import com.hawk.game.protocol.SoldierExchange.PBSEResp;
import com.hawk.game.protocol.SoldierExchange.PBSEShopBuyCnt;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;

public class BackSoldierExchangeActivity extends ActivityBase implements AchieveProvider{
	private static final Logger logger = LoggerFactory.getLogger("Server");
	


	public BackSoldierExchangeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.BACK_SOLDIER_EXCHANGE;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		BackSoldierExchangeActivity activity =  new BackSoldierExchangeActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	
	@Override
	public void onPlayerLogin(String playerId) {
		int termId = this.getActivityTermId();
		if(termId <= 0){
			return;
		}
		long lastLogoutTime = this.getDataGeter().getPlayerLogoutTime(playerId);
		long curTime = HawkTime.getMillisecond();
		
		BackSoldierExchangeKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackSoldierExchangeKVCfg.class);
		Optional<BackSoldierExchangeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		BackSoldierExchangeEntity entity = opEntity.get();
		//活动持续中
		if(curTime < HawkTime.getAM0Date(new Date(entity.getBackTime())).getTime() +
				kvCfg.getContinueDays() * HawkTime.DAY_MILLI_SECONDS){
			logger.info("BackSoldierExchangeActivity onPlayerLogin inActivity playerId:{}, backTime:{}, logoutTime:{},backLogoutTime:{}", 
					 playerId, curTime,lastLogoutTime,entity.getLogoutTime());
			return;
		}
		//限定时间不开放
		if(curTime < HawkTime.getAM0Date(new Date(entity.getBackTime())).getTime() + 
				(kvCfg.getIntervalDays() + kvCfg.getContinueDays())  * HawkTime.DAY_MILLI_SECONDS){
			logger.info("BackSoldierExchangeActivity onPlayerLogin inActivity playerId:{}, backTime:{}, logoutTime:{}", 
					 playerId, entity.getBackTime(),lastLogoutTime);
			return;
		}
		//检查是否符合回流
		Date lossBegin = new Date(lastLogoutTime);
		Date lossOver = new Date(curTime);
		if(HawkTime.calcBetweenDays(lossBegin, lossOver) < kvCfg.getTriggerLossDays()){
			logger.info("BackSoldierExchangeActivity onPlayerLogin inActivity playerId:{}, backTime:{}, logoutTime:{}", 
					 playerId, curTime,lastLogoutTime);
			return;
		}
		//318活动时间内不开
		if(this.inActivity318(curTime)){
			logger.info("BackSoldierExchangeActivity onPlayerLogin inActivity318 playerId:{}, backTime:{}, logoutTime:{}", 
					 playerId, curTime,lastLogoutTime);
			return;
		}
		//主城等级
		int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
		if (kvCfg.getBuildMinLevel() > cityLevel) {
			logger.info("BackSoldierExchangeActivity onPlayerLogin cityLevel less playerId:{}, backTime:{}, logoutTime:{},cityLevel:{}", 
					 playerId, curTime,lastLogoutTime,cityLevel);
			return;
		}
		//初始化活动数据
		this.initActivityData(entity, curTime, lastLogoutTime);
		//日志
		this.logBackSoldierExchangeStart(termId, playerId, entity.getBackCount());
		logger.info("BackSoldierExchangeActivity onPlayerLogin sucess playerId:{}, backTime:{}, logoutTime:{},cityLevel:{}", 
				 playerId, curTime,lastLogoutTime,cityLevel);
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<BackSoldierExchangeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		BackSoldierExchangeEntity entity = opEntity.get();
		PBSEActivitySync.Builder builder = PBSEActivitySync.newBuilder();
		builder.setExchangeTimes(entity.getHistorList().size());
		builder.addAllHistroyId(entity.getHistorList());
		if(entity.getExchangeType()!=0){
			builder.setLastExchangeType(SoldierType.valueOf(entity.getExchangeType()));
		}
		for(Entry<Integer, Integer> ent : entity.getShopItems().entrySet()){
			builder.addShopBuycnt(PBSEShopBuyCnt.newBuilder()
					.setShopId(ent.getKey())
					.setBuyCnt(ent.getValue()));
		}
		
		for (String hisId : entity.getHistorList()) {
			byte[] bytes = ActivityGlobalRedis.getInstance().getRedisSession().getBytes(hisId.getBytes());
			PBSEResp.Builder his = PBSEResp.newBuilder();
			try {
				his.mergeFrom(bytes);
				builder.addHistroyDetail(his);
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
			}
		}
		builder.setCd(entity.getCoolTime());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.BACK_SOLDIER_EXCHANGE_INFO_SYNC, builder));

	}
	
	@Override
	public boolean isActivityClose(String playerId) {
		//主城等级
		BackSoldierExchangeKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackSoldierExchangeKVCfg.class);
		if (kvCfg.getBuildMinLevel() > this.getDataGeter().getConstructionFactoryLevel(playerId)) {
			return true;
		}
		
		Optional<BackSoldierExchangeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return true;
		}
		long curTime = HawkTime.getMillisecond();
		BackSoldierExchangeEntity entity = opEntity.get();
		
		//没有回流
		if(entity.getBackTime() <= 0){
			return true;
		}
		//是否在活动期限内
		long outTime = HawkTime.getAM0Date(new Date(entity.getBackTime())).getTime() + kvCfg.getContinueDays() * HawkTime.DAY_MILLI_SECONDS;
		if(curTime >= outTime){
			return true;
		}
		//318活动时间内不开
		if(this.inActivity318(curTime)){
			return true;
		}
		return super.isActivityClose(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<BackSoldierExchangeEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		BackSoldierExchangeEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			return Optional.empty();
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	

	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<BackSoldierExchangeEntity> opEntity = this.getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		BackSoldierExchangeEntity entity = opEntity.get();
		long loginTime = this.getDataGeter().getAccountLoginTime(event.getPlayerId());
		BackSoldierExchangeKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackSoldierExchangeKVCfg.class);
		long outTime = HawkTime.getAM0Date(new Date(entity.getBackTime())).getTime() + kvCfg.getContinueDays() * HawkTime.DAY_MILLI_SECONDS;
		if(loginTime > outTime){
			return;
		}
		this.checkActivityClose(event.getPlayerId());
	}
	
	
	
	/**
	 * 初始化
	 * @param entity
	 * @param curTine
	 * @param logoutTime
	 */
	public void initActivityData(BackSoldierExchangeEntity entity,long curTine,long logoutTime){
		entity.setBackCount(entity.getBackCount() + 1);
		entity.setBackTime(curTine);
		entity.setLogoutTime(logoutTime);
		this.initAchieveData(entity);
	}
	
	
	public void initAchieveData(BackSoldierExchangeEntity entity) {
		// 成就已初始化
		List<AchieveItem> itemList = new ArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<BackSoldierExchangeAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(BackSoldierExchangeAchieveCfg.class);
		while (configIterator.hasNext()) {
			BackSoldierExchangeAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			itemList.add(item);
		}
		entity.resetItemList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), entity.getItemList()), true);
	}

	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<BackSoldierExchangeEntity> queryList = HawkDBManager.getInstance().query("from BackSoldierExchangeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0 ) {
			BackSoldierExchangeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		BackSoldierExchangeEntity entity = new BackSoldierExchangeEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

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
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg = HawkConfigManager.getInstance().getConfigByKey(BackSoldierExchangeAchieveCfg.class, achieveId);
		return cfg;
	}

	@Override
	public Action takeRewardAction() {
		return Action.SOLDIER_EXCHANGE_ACHIEVE;
	}

	/**
	 * 是否在318活动时间内
	 * @param time
	 * @return
	 */
	public boolean inActivity318(long time){
		//是否和318的活动冲突，如果有则不开
		ConfigIterator<SoldierExchangeActivityTimeCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(SoldierExchangeActivityTimeCfg.class);
		while (configIterator.hasNext()) {
			SoldierExchangeActivityTimeCfg next = configIterator.next();
			if(next.getShowTimeValue() <= time && time <= next.getEndTimeValue() ){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 回流转兵种
	 * @param termId
	 * @param playerId
	 * @param backCount
	 */
    private void logBackSoldierExchangeStart(int termId,String playerId,int backCount){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId); //期数
        param.put("backCount", backCount); //邀请ID
        getDataGeter().logActivityCommon(playerId, LogInfoType.back_soldier_exchange_start, param);
    }
	
}
