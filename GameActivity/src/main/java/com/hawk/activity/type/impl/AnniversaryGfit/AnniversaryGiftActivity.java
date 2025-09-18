package com.hawk.activity.type.impl.AnniversaryGfit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.IDIPGmRechargeEvent;
import com.hawk.activity.event.impl.LoginDaysActivityEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.AnniversaryGfit.cfg.AnniversaryGiftAchievecfg;
import com.hawk.activity.type.impl.AnniversaryGfit.entity.AnniversaryGiftEntity;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.parser.RechargeGfitPayCountParse;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 累积消耗活动
 * @author Jesse
 *
 */
public class AnniversaryGiftActivity extends ActivityBase implements AchieveProvider {

	public AnniversaryGiftActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
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
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ANNIVERSARY_GIFT_INIT, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		if (!event.isCrossDay()) {
			return;
		}
		
		String playerId = event.getPlayerId();
		Optional<AnniversaryGiftEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		AnniversaryGiftEntity dataEntity = opDataEntity.get();
		if(dataEntity.getInitTime() <= 0){
			//初始化数据
			this.initAchieveInfo(playerId);
		}
		//玩家数据里记录当前天的的日期，说明已经处理过每日登陆成就，不继续处理
        if (dataEntity.getLoginDaysList().contains(HawkTime.getYyyyMMddIntVal())) {
            return;
        }
        //记录当天日期
        dataEntity.recordLoginDay();
		this.refreshDailAchieveInfo(dataEntity);
		//处理一下每日直购的任务
		this.progressDailyAchieve(dataEntity);
	}
	
	
	
	
	@Subscribe
	public void onIDIPRecharge(IDIPGmRechargeEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		int val = event.getDiamondNum() / 10;
		if(val <= 0){
			return;
		}
		String playerId = event.getPlayerId();
		List<AchieveItem> itemList = new ArrayList<>();
		Optional<AchieveItems> opAchieveItems = this.getAchieveItems(playerId);
		if(!opAchieveItems.isPresent()){
			return;
		}
		AchieveItems achieveItems = opAchieveItems.get();
		itemList.addAll(achieveItems.getItems());
		if(itemList.isEmpty()){
			return;
		}
		//处理直购充值
		RechargeGfitPayCountParse parse = (RechargeGfitPayCountParse) AchieveContext.getParser(AchieveType.RECHARGE_GIFT_PAY_COUNT);
		List<AchieveItem> updates = new ArrayList<>();
		PayGiftBuyEvent buyEvent = new PayGiftBuyEvent(playerId,"0",val,event.getDiamondNum());
		for(AchieveItem item : itemList){
			AnniversaryGiftAchievecfg acfg = this.getAchieveCfg(item.getAchieveId());
			if(acfg.getAchieveType() != AchieveType.RECHARGE_GIFT_PAY_COUNT){
				continue;
			}
			parse.updateAchieveData(item, acfg, buyEvent, updates);
		}
		if(updates.size() > 0){
			achieveItems.getEntity().notifyUpdate();
			AchievePushHelper.pushAchieveUpdate(playerId, updates);
		}
	}
	
	
	
	public void progressDailyAchieve(AnniversaryGiftEntity entity){
	 	String redisKey = this.getDataGeter().getGmRechargeRedisKey();
        Map<String, String> rechargeDataMap = ActivityGlobalRedis.getInstance().hgetAll(redisKey + ":" + entity.getPlayerId());
        String todayTime = String.valueOf(HawkTime.getYyyyMMddIntVal());
        int redisDiamonds = Integer.parseInt(rechargeDataMap.getOrDefault(todayTime, "0"));
        int rechargeRmb = redisDiamonds / 10;
        if (redisDiamonds % 10 > 0) {
        	rechargeRmb += 1;
        }
        if(rechargeRmb <= 0){
        	return;
        }
        //处理直购充值
  		RechargeGfitPayCountParse parse = (RechargeGfitPayCountParse) AchieveContext.getParser(AchieveType.RECHARGE_GIFT_PAY_COUNT);
  		List<AchieveItem> updates = new ArrayList<>();
  		PayGiftBuyEvent buyEvent = new PayGiftBuyEvent(entity.getPlayerId(),"0",rechargeRmb,redisDiamonds);
  		for(AchieveItem item : entity.getItemListDaily()){
  			AnniversaryGiftAchievecfg acfg = this.getAchieveCfg(item.getAchieveId());
  			if(acfg.getAchieveType() != AchieveType.RECHARGE_GIFT_PAY_COUNT){
  				continue;
  			}
  			parse.updateAchieveData(item, acfg, buyEvent, updates);
  		}
  		if(updates.size() > 0){
  			entity.notifyUpdate();
  			AchievePushHelper.pushAchieveUpdate(entity.getPlayerId(), updates);
  		}
        
	}
	
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<AnniversaryGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		AnniversaryGiftEntity entity = opEntity.get();
		// 成就已初始化
		if (entity.getInitTime() > 0) {
			return;
		}
		long curTime = HawkTime.getMillisecond();
		// 初始添加成就项
		List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
		List<AchieveItem> itemListDaily = new CopyOnWriteArrayList<AchieveItem>();
		
		ConfigIterator<AnniversaryGiftAchievecfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(AnniversaryGiftAchievecfg.class);
		while (configIterator.hasNext()) {
			AnniversaryGiftAchievecfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			if(next.getReset() > 0){
				//每日任务
				itemListDaily.add(item);
			}else{
				itemList.add(item);
			}
		}
		entity.setInitTime(curTime);
		entity.recordLoginDay();
		entity.setItemList(itemList);
		entity.setItemListDaily(itemListDaily);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getAllAchieveList()), true);
		//每天登录
		ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, entity.getLoginDaysCount(), this.providerActivityId()), true);
	}
	
	
	/**
	 * 刷新任务
	 * @param entity
	 */
	private void refreshDailAchieveInfo(AnniversaryGiftEntity entity){
		// 初始添加成就项
		List<AchieveItem> itemListDaily = new CopyOnWriteArrayList<AchieveItem>();
		ConfigIterator<AnniversaryGiftAchievecfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(AnniversaryGiftAchievecfg.class);
		while (configIterator.hasNext()) {
			AnniversaryGiftAchievecfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			if(next.getReset() > 0){
				//每日任务
				itemListDaily.add(item);
			}
		}
		entity.setItemListDaily(itemListDaily);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), itemListDaily), true);
		ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(entity.getPlayerId(), entity.getLoginDaysCount(), this.providerActivityId()), true);
			
	}

	
	
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<AnniversaryGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		AnniversaryGiftEntity entity = opEntity.get();
		if(entity.getInitTime() <= 0){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getAllAchieveList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AnniversaryGiftAchievecfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(AnniversaryGiftAchievecfg.class, achieveId);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.ANNIVERSARY_GIFT;
	}
	
	public Action takeRewardAction() {
		return Action.ANNIVERSARY_GIFT_ACHIVE_REWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		AnniversaryGiftActivity activity = new AnniversaryGiftActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<AnniversaryGiftEntity> queryList = HawkDBManager.getInstance()
				.query("from AnniversaryGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			AnniversaryGiftEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		AnniversaryGiftEntity entity = new AnniversaryGiftEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

}
