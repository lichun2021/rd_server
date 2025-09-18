package com.hawk.activity.type.impl.greatGift;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.GreatGiftBuyEvent;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.greatGift.cfg.GreatGiftActivityKVCfg;
import com.hawk.activity.type.impl.greatGift.cfg.GreatGiftActivityTimeCfg;
import com.hawk.activity.type.impl.greatGift.cfg.GreatGiftBagCfg;
import com.hawk.activity.type.impl.greatGift.cfg.GreatGiftChestCfg;
import com.hawk.activity.type.impl.greatGift.entity.GreatGiftEntity;
import com.hawk.game.protocol.Activity.greatGiftInfo;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

public class GreatGiftActivity extends ActivityBase {

	private final Logger logger = LoggerFactory.getLogger("Server");
	
	public GreatGiftActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public boolean isOpening(String playerId) {
		//精确判断活动是否结束
		long curTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		if(endTime < curTime){
			return false;
		}
		return super.isOpening(playerId);
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			Optional<GreatGiftEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				logger.error("on GreatGiftActivity open init GreatGiftEntity error, no entity created");
				return;
			}
			GreatGiftEntity entity = opEntity.get();
			//登录刷新一次可领取列表
			entity.setCanRecieveChestIds(getCanRecieveChestList(playerId));			
			pushToPlayer(playerId, HP.code.GREAT_GIFT_INFO_VALUE, (greatGiftInfo.Builder)syncActivityInfo(playerId).getRetObj());
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayerIds){
			callBack(playerId, GameConst.MsgId.ON_LUCKY_STAR_ACTIVITY_OPEN, () -> {
				Optional<GreatGiftEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					logger.error("on GreatGiftActivity open init GreatGiftEntity error, no entity created");
				}
				GreatGiftEntity entity = opEntity.get();
				entity.setCanRecieveChestIds(getCanRecieveChestList(playerId));			
				pushToPlayer(playerId, HP.code.GREAT_GIFT_INFO_VALUE, (greatGiftInfo.Builder)syncActivityInfo(playerId).getRetObj());
			});
		}
	}
	
	/***
	 * 同步活动界面信息
	 * @param playerId
	 * @return
	 */
	public Result<?> syncActivityInfo(String playerId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<GreatGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		return buildActivityInfo(playerId, opEntity.get());
	}
	
	/**
	 * 回滚
	 * @param entity
	 * @param giftId
	 */
	public void rollback(GreatGiftEntity entity, String giftId) {
		String playerId = entity.getPlayerId();
		List<String> bagList = entity.getBagList();
		String lastBoughtId = "";
		// 返回错误
		if (bagList.isEmpty() || !(lastBoughtId = bagList.get(bagList.size() -1)).equals(giftId)) {
			HawkLog.errPrintln("greatGift rollback failed, playerId: {}, giftId: {}, lastBoughtGift: {}", playerId, giftId, lastBoughtId);
			return;
		}

		bagList.remove(giftId);
		entity.getOutBagList().remove(giftId);
		entity.refreshBuyBag();
		entity.setCanRecieveChestIds(getCanRecieveChestList(playerId));		
		pushToPlayer(playerId, HP.code.GREAT_GIFT_INFO_VALUE, (greatGiftInfo.Builder)syncActivityInfo(playerId).getRetObj());
	}
	
	/***
	 * 超值好礼购买事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(GreatGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<GreatGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		GreatGiftEntity entity = opEntity.get();
		entity.userBuyBag(event.getGiftId());
		entity.setCanRecieveChestIds(getCanRecieveChestList(playerId));
		
		List<GreatGiftBagCfg> cfgList = GreatGiftBagCfg.getSortedCfgList(getDataGeter().getPlatform(playerId));
	    GreatGiftBagCfg configLastCfg = cfgList.get(cfgList.size() -1);
	    if (configLastCfg.getGiftId().equals(event.getGiftId())) {
	    	entity.setFinishTime(HawkTime.getMillisecond());
	    }
	    
		pushToPlayer(playerId, HP.code.GREAT_GIFT_INFO_VALUE, (greatGiftInfo.Builder)syncActivityInfo(playerId).getRetObj());
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<GreatGiftEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		if(!event.isCrossDay()){
			return;
		}
		//判断一下玩家前一天是否都买完了，如果是，则清理数据
		GreatGiftEntity entity = opEntity.get();
		if(allBuyBeforeDay(entity)){
			//记录一下昨天购买的日志
			logger.info("greatGiftActivity cross day clear buy msg. playerId:" + event.getPlayerId() + ", entity:" + entity);
			entity.crossDay();
			checkCacheRechargeGift(entity);
			entity.notifyUpdate();
			pushToPlayer(event.getPlayerId(), HP.code.GREAT_GIFT_INFO_VALUE, (greatGiftInfo.Builder)syncActivityInfo(event.getPlayerId()).getRetObj());
		}
	}
	
	/**
	 * 检测当天首次登录前玩家在外部购买的礼包
	 * 
	 * @param playerId
	 * @param entity
	 */
	private void checkCacheRechargeGift(GreatGiftEntity entity) {
		String playerId = entity.getPlayerId();
		try {
			String todayTime = String.valueOf(HawkTime.getYyyyMMddIntVal());
			String key = "gift_rechage_daily:" + this.getActivityId() + ":" + todayTime + ":" + playerId;
			Map<String, String> giftIdMap = ActivityGlobalRedis.getInstance().hgetAll(key);
	        if (giftIdMap.isEmpty()) {
	        	return;
	        }
	        
	        List<String> bagIdList = new ArrayList<>(giftIdMap.keySet());
	        bagIdList.sort(new Comparator<String>() {
				@Override
				public int compare(String id1, String id2) {
					GreatGiftBagCfg cfg1 = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, id1);
					GreatGiftBagCfg cfg2 = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, id2);
					return cfg1.getGiftStage() - cfg2.getGiftStage();
				}
	        });
	        
	        for (String giftId : bagIdList) {
	        	entity.userBuyBag(giftId);
	        	entity.setCanRecieveChestIds(getCanRecieveChestList(playerId));	
	        }
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/***
	 * 获取超值好礼下一次刷新的时间
	 * @param entity
	 * @return
	 */
	private long getNextFreshTime(GreatGiftEntity entity){
		if(allBuyBeforeDay(entity) && nextDayisOpen()){
			//返回下一个自然日的凌晨
			return HawkTime.getNextAM0Date();
		}
		return 0;
	}
	
	/***
	 * 判断活动在下一个自然日是否还是开启的状态
	 * @return
	 */
	private boolean nextDayisOpen(){
		GreatGiftActivityTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(GreatGiftActivityTimeCfg.class, getActivityTermId());
		if(timeCfg == null){
			logger.error("nextDayisOpen can't find GreatGiftActivityTimeCfg, termId:" + getActivityTermId());
			return false;
		}
		//结束时间大于下一个自然日的凌晨
		return timeCfg.getEndTimeValue() > HawkTime.getNextAM0Date();
	}
	
	public boolean allBuyBeforeDay(GreatGiftEntity entity){
		ConfigIterator<GreatGiftBagCfg> ite = HawkConfigManager.getInstance().getConfigIterator(GreatGiftBagCfg.class);
		String playerId = entity.getPlayerId();
		while(ite.hasNext()){
			GreatGiftBagCfg cfg = ite.next();
			//不是自己平台的不管
			if(!cfg.getChannelType().trim().equals(getDataGeter().getPlatform(playerId))){
				continue;
			}
			if(!entity.isBuy(cfg.getGiftId())){
				return false;
			}
		}
		
		return true;
	}
	
	/***
	 * 领取礼包奖励
	 * @param playerId
	 * @return
	 */
	public Result<?> onPlayerRecieveChest(String playerId, int chestId){
		if(!isOpening(playerId)){ 
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<GreatGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		GreatGiftEntity entity = opEntity.get();
		if(entity.isRecieve(chestId)){
			return Result.fail(Status.Error.ACTIVITY_REWARD_IS_TOOK_VALUE);
		}
		//判断能否领取
		if(!entity.getCanRecieveChestIds().contains(chestId)){
			return Result.fail(Status.Error.ACTIVITY_CAN_NOT_TAKE_REWARD_VALUE);
		}
		
		GreatGiftChestCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GreatGiftChestCfg.class, chestId);
		if(cfg == null){
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		//给奖励
		this.getDataGeter().takeReward(playerId, cfg.getRewardList(), 1, Action.GREAT_GIFT, true, RewardOrginType.ACTIVITY_REWARD);
		entity.recieveChest(chestId);
		//刷新可领取列表
		entity.setCanRecieveChestIds(getCanRecieveChestList(playerId));
		return syncActivityInfo(playerId);
	}
	
	/***
	 * 获取可以领取的宝箱列表(每次玩家购买了或者entity的列表为空就算一次)
	 * @param playerId
	 * @return
	 */
	private List<Integer> getCanRecieveChestList(String playerId){
		Optional<GreatGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		GreatGiftEntity entity = opEntity.get();
		List<Integer> list = new ArrayList<Integer>();
		ConfigIterator<GreatGiftChestCfg> ite = HawkConfigManager.getInstance().getConfigIterator(GreatGiftChestCfg.class);
		while(ite.hasNext()){
			GreatGiftChestCfg cfg = ite.next();
			if(!entity.isRecieve(cfg.getStageId()) && allBuy(entity, cfg)){
				list.add(cfg.getStageId());
			}
		}
		return list;
	}
	
	/***
	 * 判断cfg配置的礼包阶段是否都购买了
	 * @param entity
	 * @param cfg
	 * @return
	 */
	private boolean allBuy(GreatGiftEntity entity, GreatGiftChestCfg chestCfg){
		ConfigIterator<GreatGiftBagCfg> ite = HawkConfigManager.getInstance().getConfigIterator(GreatGiftBagCfg.class);
		List<GreatGiftBagCfg> platformCfgList = new ArrayList<>();
		while(ite.hasNext()){
			GreatGiftBagCfg cfg = ite.next();
			if(cfg.getChannelType().trim().equals(getDataGeter().getPlatform(entity.getPlayerId()))){
				platformCfgList.add(cfg);
			}
		}
		boolean containStage = false;
		for(GreatGiftBagCfg cfg : platformCfgList){
			if(chestCfg.getStageId() == cfg.getGiftStage() && !entity.isBuy(cfg.getGiftId())){
				return false;
			}
			if(cfg.getGiftStage() == chestCfg.getStageId()){
				containStage = true;
			}
		}
		if(!containStage){
			return false;
		}
		return true;
	}
	
	
	/***
	 * 能否购买礼包
	 * @param entity
	 * @param giftId
	 * @return
	 */
	public boolean canBuy(String playerId, String giftId){
		if(!isOpening(playerId)){ 
			return false;
		}
		Optional<GreatGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		GreatGiftEntity entity = opEntity.get();
		if(GreatGiftActivityKVCfg.getInstance().isOpenGradeBuy()){
			GreatGiftBagCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, giftId);
			ConfigIterator<GreatGiftBagCfg> ite = HawkConfigManager.getInstance().getConfigIterator(GreatGiftBagCfg.class);
			while(ite.hasNext()){
				GreatGiftBagCfg cfg = ite.next();
				if(!cfg.getChannelType().trim().equals(getDataGeter().getPlatform(playerId))){
					continue;
				}
				if(cfg.getGiftStage() < curCfg.getGiftStage() && !entity.isBuy(cfg.getGiftId())){
					return false;
				}
			}
		}
		return true;
	}
	
	/***
	 * 获取玩家可以购买的阶段礼包(isOpen == 1时才会需要获取可以购买的阶段数)
	 * @param playerId
	 * @return
	 */
	public int getCanBuyStage(String playerId){
		if(!isOpening(playerId)){ 
			return 0;
		}
		Optional<GreatGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return 0;
		}
		if(!GreatGiftActivityKVCfg.getInstance().isOpenGradeBuy()){
			return 0;
		}
		GreatGiftEntity entity = opEntity.get();
		List<String> list = entity.getBagList();
		int maxStage = 0; //当前购买的最大阶段
		for(String giftId : list){
			GreatGiftBagCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(GreatGiftBagCfg.class, giftId);
			if(curCfg == null || !curCfg.getChannelType().trim().equals(getDataGeter().getPlatform(playerId))){
				continue;
			}
			if(curCfg.getGiftStage() > maxStage){
				maxStage = curCfg.getGiftStage();
			}
		}
		return getMaxStage(maxStage, list, getDataGeter().getPlatform(playerId));
	}
	
	private int getMaxStage(int maxStage, List<String> buyList, String channelType){
		ConfigIterator<GreatGiftBagCfg> configIte = HawkConfigManager.getInstance().getConfigIterator(GreatGiftBagCfg.class);
		if(maxStage == 0){
			int minStage = Integer.MAX_VALUE;
			while(configIte.hasNext()){
				GreatGiftBagCfg cfg = configIte.next();
				if(!channelType.equals(cfg.getChannelType())){
					continue;
				}
				if(cfg.getGiftStage() < minStage){
					minStage = cfg.getGiftStage();
				}
			}
			return minStage;
		}else if(maxStage > 0){
			while(configIte.hasNext()){
				GreatGiftBagCfg cfg = configIte.next();
				if(!channelType.equals(cfg.getChannelType())){
					continue;
				}
				if(cfg.getGiftStage() == maxStage && !buyList.contains(cfg.getGiftId())){
					return maxStage;
				}
			}
			//返回下一个阶段
			return getNextStage(maxStage, channelType);
		}else {
			return -1;
		}
	}
	
	private int getNextStage(int curStage, String channelType){
		ConfigIterator<GreatGiftBagCfg> configIte = HawkConfigManager.getInstance().getConfigIterator(GreatGiftBagCfg.class);
		int next = Integer.MAX_VALUE;
		while(configIte.hasNext()){
			GreatGiftBagCfg cfg = configIte.next();
			if(!channelType.equals(cfg.getChannelType())){
				continue;
			}
			if(cfg.getGiftStage() > curStage && cfg.getGiftStage() < next){
				next = cfg.getGiftStage();
			}
		}
		if(next == Integer.MAX_VALUE){
			next = -1;
		}
		return next;
	}
	
	
	/***
	 * 构造客户端显示界面
	 * @param playerId
	 * @return
	 */
	private Result<?> buildActivityInfo(String playerId, GreatGiftEntity entity){
		greatGiftInfo.Builder build = greatGiftInfo.newBuilder();
		entity.buildResultInfo(build);
		if(GreatGiftActivityKVCfg.getInstance().isOpenGradeBuy()){
			build.setCanBuyGrade(getCanBuyStage(playerId));
		}
		long nextFreshTime = getNextFreshTime(entity);
		if(nextFreshTime > 0){
			build.setNextFreshTime(getNextFreshTime(entity));
		}
		return Result.success(build);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GREAT_GIFT;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		GreatGiftActivity activity = new GreatGiftActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GreatGiftEntity> queryList = HawkDBManager.getInstance()
				.query("from GreatGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GreatGiftEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GreatGiftEntity entity = new GreatGiftEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

}
