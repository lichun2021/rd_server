package com.hawk.activity.type.impl.newbietrain;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.NewbieTrainEvent;
import com.hawk.activity.event.impl.RandomHeroEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.newbietrain.cfg.NewbieTrainAchieveCfg;
import com.hawk.activity.type.impl.newbietrain.cfg.NewbieTrainGiftCfg;
import com.hawk.activity.type.impl.newbietrain.cfg.NewbieTrainKVCfg;
import com.hawk.activity.type.impl.newbietrain.cfg.NewbieTrainRewardCfg;
import com.hawk.activity.type.impl.newbietrain.entity.NewbieTrainEntity;
import com.hawk.activity.type.impl.newbietrain.entity.NewbieTrainInfo;
import com.hawk.activity.type.impl.newbietrain.entity.TrainRecordInfo;
import com.hawk.game.protocol.Activity.NoviceGiftBuyResp;
import com.hawk.game.protocol.Activity.NoviceTrainActivityInfo;
import com.hawk.game.protocol.Activity.NoviceTrainRecordResp;
import com.hawk.game.protocol.Activity.NoviceTrainResp;
import com.hawk.game.protocol.Activity.NoviceTrainSelectResp;
import com.hawk.game.protocol.Activity.NoviceTrainType;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.GameConst;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 新兵作训
 * 
 * author: lating
 */
public class NewbieTrainActivity extends ActivityBase implements AchieveProvider {
    
    public NewbieTrainActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.NEWBIE_TRAIN_ACTIVITY;
    }

    
    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
    
    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
    	NewbieTrainActivity activity = new NewbieTrainActivity(config.getActivityId(), activityEntity); 
    	AchieveContext.registeProvider(activity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<NewbieTrainEntity> queryList = HawkDBManager.getInstance()
                .query("from NewbieTrainEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
        	NewbieTrainEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
    	NewbieTrainEntity entity = new NewbieTrainEntity(playerId, termId);
        return entity;
    }
    
    @Override
	public void onOpen() {
		Set<String> playerIds = this.getDataGeter().getOnlinePlayers();
		for (String playerId : playerIds) {
			this.callBack(playerId, GameConst.MsgId.NEWBIE_TRAIN_ACTIVITY_OPEN, ()->{
				activityOpen(playerId);
			});
		}
	}
    
    /**
     * 活动开启
     * @param playerId
     */
    private void activityOpen(String playerId) {
    	Optional<NewbieTrainEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		NewbieTrainEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(playerId);
		}
		
		entity.setDailyLoginTime(HawkTime.getMillisecond());
		syncActivityInfo(playerId, entity);
    }
    
    /**
     * 判断活动是否开启
     */
    public boolean isOpening(String playerId) {
    	int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
    	NewbieTrainKVCfg config = HawkConfigManager.getInstance().getKVInstance(NewbieTrainKVCfg.class);
    	if (cityLevel < config.getBuildMin()) {
    		return false;
    	}
    	
    	return super.isOpening(playerId);
    }
    
    @Subscribe
	public void onEvent(ContinueLoginEvent event){
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<NewbieTrainEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		NewbieTrainEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(playerId);
		}
		
		if (!HawkTime.isToday(entity.getDailyLoginTime())) {
			entity.setDailyLoginTime(HawkTime.getMillisecond());
			for (NewbieTrainInfo info : entity.getTrainInfoMap().values()) {
				info.getGiftIdList().clear();
			}
			entity.notifyUpdate();
		}
		
		syncActivityInfo(playerId, entity);
	}

    /**
     * 同步活动数据
     */
    @Override
    public void syncActivityDataInfo(String playerId) {
        Optional<NewbieTrainEntity> opEntity = getPlayerDataEntity(playerId);
        if (opEntity.isPresent()) {
            syncActivityInfo(playerId, opEntity.get());
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
	
	@Override
	public Action takeRewardAction() {
		return Action.NEWBIE_TRAIN_ACHIEVE;
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(NewbieTrainAchieveCfg.class, achieveId);
	}
	
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<NewbieTrainEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		
		NewbieTrainEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}
	
	/***
	 * 初始化成就
	 * @param playerId
	 * @return
	 */
	private void initAchieveItems(String playerId) {
		Optional<NewbieTrainEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		NewbieTrainEntity entity = opEntity.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		boolean specialServer = this.getDataGeter().isProprietaryServer();
		ConfigIterator<NewbieTrainAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(NewbieTrainAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while (configIterator.hasNext()) {
			NewbieTrainAchieveCfg cfg = configIterator.next();	
			if (specialServer && cfg.getService() == 0 || (!specialServer && cfg.getService() != 0)) {
				continue;
			}
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());				
			itemList.add(item);
		}
		
		entity.setItemList(itemList);
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	// 1.请求活动数据; done
	// 2.英雄选择或装备选择请求; done 
	// 3.作训请求; done
	// 4.购买礼包请求; done
	// 5.作训记录数据请求  done
	// 6.英雄招募或装备打造事件监听  done 

	@Subscribe
	public void onGachaNotify(RandomHeroEvent event){
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<NewbieTrainEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		NewbieTrainEntity entity = opEntity.get();
		boolean sync = updateGachaTimes(entity, event);
		if (sync) {
			syncActivityInfo(playerId, entity);
		}
	}
	
	/**
	 * 更新次数
	 * @param entity
	 * @param config
	 * @param trainType
	 * @param gachaTimes
	 * @param specialServer
	 * @return
	 */
	private boolean updateGachaTimes(NewbieTrainEntity entity, RandomHeroEvent event) {
		int trainType = -1;
		NewbieTrainKVCfg config = HawkConfigManager.getInstance().getKVInstance(NewbieTrainKVCfg.class);
		// 高级英雄招募
		if(event.getGachaType() == GachaType.ADVANCE_ONE_VALUE || event.getGachaType() == GachaType.ADVANCE_TEN_VALUE){
			trainType = NoviceTrainType.TYPE_HERO_VALUE;
		} else if (event.getGachaType() == GachaType.ARMOUR_ONE_VALUE || event.getGachaType() == GachaType.ARMOUR_TEN_VALUE) {
			// 装备打造
			if (this.getDataGeter().getConstructionFactoryLevel(entity.getPlayerId()) >= config.getEquipMin()) {
				trainType = NoviceTrainType.TYPE_EQUIP_VALUE;
			}
		}
		
		if (trainType < 0) {
			return false;
		}
		
		int gachaTimes = event.getCount();
		NewbieTrainInfo info = entity.getTrainInfo(trainType);
		info.addGachaTimes(gachaTimes);
		info.addGachaTimesTotal(gachaTimes);
		boolean specialServer = this.getDataGeter().isProprietaryServer();
		// 判断作训次数是否增加
		int times = config.getGacha2TrainTimes(specialServer, trainType);
		int ratio = info.getGachaTimes() / times;
		if (ratio > 0) {
			info.addTrainTimesTotal(ratio);
			info.addTrainTimesRemain(ratio);
			info.addGachaTimes(0 - times * ratio);
		}
		
		entity.notifyUpdate();
		HawkLog.logPrintln("newbie train activity update trainTimes, playerId: {}, gachaType: {}, trainType: {}, addTimes: {}, trainTimesTotal: {}, trainTimesRemain: {}, gachaTimes: {}, gachaTimes: {}", 
				entity.getPlayerId(), event.getGachaType(), trainType, ratio, info.getTrainTimesTotal(), info.getTrainTimesRemain(), info.getGachaTimes(), info.getGachaTimesTotal());
		return true;
	}

	/**
	 * 选择作训的英雄对象或装备兵种对象
	 * @param trainType
	 * @param objId
	 */
	public void selectTrainObject(String playerId, int trainType, int objId, int protocol) {
		HawkTuple2<Integer,NewbieTrainEntity> tuple = operationCheck(playerId, trainType);
		if (tuple.first != 0) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, tuple.first);
			return;
		}
		
		// 不是策划配置的id，不合法
		if (!NewbieTrainRewardCfg.getTrainObjIdSet(trainType).contains(objId)) {
			HawkLog.errPrintln("select train object failed, playerId: {}, trainType: {}, invalid objId: {}", playerId, trainType, objId);
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		
		NewbieTrainEntity entity = tuple.second;
		NewbieTrainInfo info = entity.getTrainInfo(trainType);
		int oldObjId = info.getTrainObjectId();
		info.setTrainObjectId(objId);
		entity.notifyUpdate();
		syncActivityInfo(playerId, entity);
		NoviceTrainSelectResp.Builder builder = NoviceTrainSelectResp.newBuilder();
		builder.setType(NoviceTrainType.valueOf(trainType));
		builder.setSelectId(objId);
		pushToPlayer(playerId, HP.code2.NOVICE_TRAIN_SELECT_RESP_VALUE, builder);
		HawkLog.logPrintln("select train object success, playerId: {}, trainType: {}, objId: {}, oldObjId: {}", playerId, trainType, objId, oldObjId);
	}
	
	/**
	 * 作训请求
	 * @param playerId
	 * @param trainType
	 * @param times
	 */
	@SuppressWarnings("deprecation")
	public void doTrain(String playerId, int trainType, int times, int protocol) {
		NewbieTrainKVCfg config = HawkConfigManager.getInstance().getKVInstance(NewbieTrainKVCfg.class);
		// 参数不合法
		if (times <= 0 || times > config.getTrainMax()) {
			HawkLog.errPrintln("newbie train doTrain failed, playerId: {}, invalid times: {}", playerId, times);
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		HawkTuple2<Integer,NewbieTrainEntity> tuple = operationCheck(playerId, trainType);
		if (tuple.first != 0) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, tuple.first);
			return;
		}
		
		NewbieTrainEntity entity = tuple.second;
		NewbieTrainInfo info = entity.getTrainInfo(trainType);
		int objId = info.getTrainObjectId();
		// 还未选择作训对象
		if (objId <= 0) {
			HawkLog.errPrintln("newbie train doTrain failed, playerId: {}, trainType: {}, invalid objId: {}", playerId, trainType, objId);
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, Status.Error.NEWBIE_TRAIN_SELECT_NEED_VALUE);
			return;
		}
		
		// 作训次数不够
		int remainTimes = info.getTrainTimesRemain();
		if (remainTimes < times) {
			HawkLog.errPrintln("newbie train doTrain failed, playerId: {}, trainType: {}, remainTimes: {}, times: {}", playerId, trainType, remainTimes, times);
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, Status.Error.NEWBIE_TRAIN_TIMES_ENOUGH_VALUE);
			return;
		}
		
		// 作训、发奖、抛出成就事件、记录作训数据
		int time = HawkTime.getSeconds();
		List<TrainRecordInfo> recordList = new ArrayList<>();
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		for (int i = 0; i < times; i++) {
			String items = NewbieTrainRewardCfg.randomAward(trainType, objId);
			if (items == null) {
				HawkLog.errPrintln("newbie train doTrain failed, playerId: {}, trainType: {}, random award items: {}", playerId, trainType, items);
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, Status.SysError.CONFIG_ERROR_VALUE);
				return;
			}
			
			List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemList(items);
			rewardList.addAll(rewardItems);
			RewardItem.Builder first = rewardItems.get(0);
			TrainRecordInfo record = TrainRecordInfo.valueOf(first.getItemId(), (int)first.getItemCount(), time);
			recordList.add(record);
		}
		
		info.getTrainRecordList().addAll(recordList);
		while (info.getTrainRecordList().size() > config.getRecordMax()) {
			info.getTrainRecordList().remove(0);
		}
		
		rewardList = RewardHelper.mergeRewardItem(rewardList);
		info.addTrainTimesRemain(0 - times);
		entity.notifyUpdate();
		
		//发送奖励
		this.getDataGeter().takeReward(playerId, rewardList, 1,  Action.NEWBIE_TRAIN_REWARD, false);  
		//作训事件
		ActivityManager.getInstance().postEvent(new NewbieTrainEvent(playerId, trainType, times));
		// 记录打点
		this.getDataGeter().logNewbieTrain(playerId, trainType, times, info.getTrainTimesRemain(), info.getGachaTimes(), info.getGachaTimesTotal());
		
		NoviceTrainResp.Builder respBuilder = NoviceTrainResp.newBuilder();
		respBuilder.setType(NoviceTrainType.valueOf(trainType));
		rewardList.forEach(e -> respBuilder.addRewardItem(e));
		pushToPlayer(playerId, HP.code2.NOVICE_TRAIN_RESP_VALUE, respBuilder);
		syncActivityInfo(playerId, entity);
		
		HawkLog.logPrintln("newbie train success, playerId: {}, trainType: {}, objId: {}, times: {}, remainTimes: {}", playerId, trainType, objId, times, info.getTrainTimesRemain());
	}

	/**
	 * 购买礼包
	 * @param playerId
	 * @param giftId
	 * @param count
	 */
	public void buyGift(String playerId, int trainType, int giftId, int protocol) {
		NewbieTrainGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(NewbieTrainGiftCfg.class, giftId);
		if (giftCfg == null) {
			HawkLog.errPrintln("newbie train buyGift failed, playerId: {}, invalid giftId: {}", playerId, giftId);
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		
		boolean specialServer = this.getDataGeter().isProprietaryServer();
		// 判断giftId跟作训类型是否匹配
		if ((specialServer && giftCfg.getService() == 0) || (!specialServer && giftCfg.getService() != 0)) {
			HawkLog.errPrintln("newbie train buyGift failed, specialServer not match, playerId: {}, giftId: {}", playerId, giftId);
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, Status.Error.NEWBIE_TRAIN_GIFT_SVR_ERROR_VALUE);
			return;
		}
		
		// 判断giftId跟作训类型是否匹配
		if (giftCfg.getType() != trainType) {
			HawkLog.errPrintln("newbie train buyGift failed, traintype not match, playerId: {}, giftId: {}", playerId, giftId);
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, Status.Error.NEWBIE_TRAIN_GIFT_TYPE_ERROR_VALUE);
			return;
		}
		
		HawkTuple2<Integer,NewbieTrainEntity> tuple = operationCheck(playerId, trainType);
		if (tuple.first != 0) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, tuple.first);
			return;
		}
		
		NewbieTrainEntity entity = tuple.second;
		NewbieTrainInfo info = entity.getTrainInfo(trainType);
		// 已经买过了
		if (info.getGiftIdList().contains(giftId)) {
			HawkLog.errPrintln("newbie train buyGift failed, gift bought already, playerId: {}, giftId: {}", playerId, giftId);
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, Status.Error.NEWBIE_TRAIN_GIFT_BOUGHT_VALUE);
			return;
		}
		
		if (!giftCfg.isFree()) {
			ImmutableList<RewardItem.Builder> consumeList = RewardHelper.toRewardItemImmutableList(giftCfg.getPrice());
			boolean consumeResult = getDataGeter().consumeItems(playerId, consumeList, HP.code2.NOVICE_TRAIN_GIFT_BUY_REQ_VALUE, Action.NEWBIE_TRAIN_GIFT);
			if (!consumeResult) {
				HawkLog.errPrintln("newbie train buyGift failed, consume failed, playerId: {}, giftId: {}", playerId, giftId);
				return;
			}	
		}
		
		//发送奖励
		ImmutableList<RewardItem.Builder> rewardList = RewardHelper.toRewardItemImmutableList(giftCfg.getRewards());
		this.getDataGeter().takeReward(playerId, rewardList, 1,  Action.NEWBIE_TRAIN_GIFT, true); 
		
		info.getGiftIdList().add(giftId);
		entity.notifyUpdate();
		syncActivityInfo(playerId, entity);
		
		NoviceGiftBuyResp.Builder builder = NoviceGiftBuyResp.newBuilder();
		builder.setType(NoviceTrainType.valueOf(trainType));
		builder.setGiftId(giftId);
		pushToPlayer(playerId, HP.code2.NOVICE_TRAIN_GIFT_BUY_RESP_VALUE, builder);
		HawkLog.logPrintln("newbie train buyGift success, playerId: {}, trainType: {}, giftId: {}", playerId, trainType, giftId);
	}
	
	/**
	 * 作训记录数据请求
	 * @param playerId
	 * @param trainType
	 */
	public void trainRecordReq(String playerId, int trainType, int protocol) {
		HawkTuple2<Integer,NewbieTrainEntity> tuple = operationCheck(playerId, trainType);
		if (tuple.first != 0) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, tuple.first);
			return;
		}
		
		NewbieTrainEntity entity = tuple.second;
		NewbieTrainInfo info = entity.getTrainInfo(trainType);
		NoviceTrainRecordResp.Builder respBuilder = NoviceTrainRecordResp.newBuilder();
		respBuilder.setType(NoviceTrainType.valueOf(trainType));
		for (TrainRecordInfo recordInfo : info.getTrainRecordList()) {
			respBuilder.addData(recordInfo.toBuilder());
		}
		pushToPlayer(playerId, HP.code2.NOVICE_TRAIN_RECORD_RESP_VALUE, respBuilder);
	}
	
	/**
	 * 协议操作前检查
	 * @param playerId
	 * @param trainType
	 * @return
	 */
	private HawkTuple2<Integer,NewbieTrainEntity> operationCheck(String playerId, int trainType) {
		if (!isOpening(playerId)) {
			HawkLog.errPrintln("newbie train operationCheck failed, activity not open, playerId: {}", playerId);
			return new HawkTuple2<>(Status.Error.ACTIVITY_NOT_OPEN_VALUE, null);
		}
		Optional<NewbieTrainEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("newbie train operationCheck failed, data error, playerId: {}", playerId);
			return new HawkTuple2<>(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE, null);
		}
		
		NewbieTrainEntity entity = opEntity.get();
		NewbieTrainKVCfg config = HawkConfigManager.getInstance().getKVInstance(NewbieTrainKVCfg.class);
		int buildLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
		// 装备兵种作训的条件还不满足
		if (trainType == NoviceTrainType.TYPE_EQUIP_VALUE && buildLevel < config.getEquipMin()) {
			HawkLog.errPrintln("newbie train operationCheck failed, playerId: {}, trainType: {}", playerId, trainType);
			return new HawkTuple2<>(Status.Error.NEWBIE_TRAIN_COND_ERROR_VALUE, null);
		}
		
		return new HawkTuple2<>(0, entity);
	}
	
	/**
     * 给前端发数据
     */
    public void syncActivityInfo(String playerId, NewbieTrainEntity entity) {
    	if (entity.getDailyLoginTime() <= 0) {
    		entity.setDailyLoginTime(HawkTime.getMillisecond());
    	}
    	NoviceTrainActivityInfo.Builder sync = NoviceTrainActivityInfo.newBuilder();
    	boolean specialServer = this.getDataGeter().isProprietaryServer();
    	sync.setSpecialServer(specialServer ? 1 : 0);
    	for (NoviceTrainType type : NoviceTrainType.values()) {
    		NewbieTrainInfo info = entity.getTrainInfo(type.getNumber());
    		sync.addInfo(info.toBuilder());
    	}
    	
        pushToPlayer(playerId, HP.code2.NOVICE_TRAIN_ACTIVITY_INFO_SYNC_VALUE, sync);
    }
}
