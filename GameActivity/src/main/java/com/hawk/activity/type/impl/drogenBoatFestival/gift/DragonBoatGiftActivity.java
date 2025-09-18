package com.hawk.activity.type.impl.drogenBoatFestival.gift;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.DragonBoatGiftAchieveEvent;
import com.hawk.activity.event.impl.DragonBoatRefreshEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.cfg.DragonBoatGiftKVCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.entity.DragonBoatGiftEntity;
import com.hawk.game.protocol.Activity.DragonBoatGiftInfoResp;
import com.hawk.game.protocol.Activity.DragonBoatLocationResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.World.PBDragonBoat;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 端午-龙船送礼
 * @author che
 *
 */
public class DragonBoatGiftActivity extends ActivityBase{

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public DragonBoatGiftActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DRAGON_BOAT_GIFT_ACTIVITY;
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DragonBoatGiftActivity activity = new DragonBoatGiftActivity(
				config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DragonBoatGiftEntity> queryList = HawkDBManager.getInstance()
				.query("from DragonBoatGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DragonBoatGiftEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DragonBoatGiftEntity entity = new DragonBoatGiftEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.DRAGON_BOAT_GIFT_INIT, () -> {
				this.syncActivityDataInfo(playerId);
			});
		}
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<DragonBoatGiftEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	
	
	/**
	 * 监听龙船刷新事件
	 */
	@Subscribe
	public void onDragonBoatRefresh(DragonBoatRefreshEvent event){
		logger.info("DragonBoatGift,onDragonBoatRefresh,playerId: "
				+ "{},boatId:{}", event.getPlayerId(),event.getBoatId());
		this.syncActivityDataInfo(event.getPlayerId());
	}
	
	

	/**
	 * 监听龙船奖励获取事件
	 */
	@Subscribe
	public void onDragonBoatGiftAchieve(DragonBoatGiftAchieveEvent event){
		String playerId = event.getPlayerId();
		long boatId = event.getBoatId();
		Optional<DragonBoatGiftEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		//记录
		DragonBoatGiftEntity entity = opDataEntity.get();
		entity.addBoatAwardRecord(String.valueOf(boatId));
		this.addDragonBoatGiftRecord(boatId, playerId);
		this.syncActivityInfo(playerId, entity);
		logger.info("DragonBoatGift,onDragonBoatGiftAchieve,playerId: "
				+ "{},boatId:{}", playerId,boatId);
	}
	
	/**
	 * 获取龙船位置信息
	 * @param playerId
	 */
	public void getDragonBoatPos(String playerId){
		HawkTuple2<Integer, Integer> pos = this.getDataGeter().getDragonBoatPos();
		if(pos == null){
			logger.info("DragonBoatGift,getDragonBoatPos pos null,playerId: "
					+ "{}", playerId);
			return;
		}
		DragonBoatLocationResp.Builder builder = DragonBoatLocationResp.newBuilder();
		builder.setPosX(pos.first);
		builder.setPosY(pos.second);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.DRAGON_BOAT_LOCATION_RESP, builder));
		logger.info("DragonBoatGift,getDragonBoatPos pos sucess,playerId:{},px:{},py:{}", 
				playerId,pos.first,pos.second);
	}
	
	
	
	public void loginAwardAchieve(String playerId){
		Optional<DragonBoatGiftEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		DragonBoatGiftKVCfg cfg = HawkConfigManager.getInstance().
				getKVInstance(DragonBoatGiftKVCfg.class);
		DragonBoatGiftEntity entity = opPlayerDataEntity.get();
		if(entity.getLoginAward() > 0){
			return;
		}
		entity.setLoginAward(1);
		List<RewardItem.Builder>  awardList = RewardHelper.toRewardItemList(cfg.getLoginAward());
		this.getDataGeter().takeReward(playerId,awardList, 1, Action.DRAGON_BOAT_GIFT_LOGIN_AWARD, true);
		this.syncActivityInfo(playerId, entity);
	}
	
	/**
	 * 获取当前龙船ID
	 * @return
	 */
	public long getDragonBoatId(){
		DragonBoatGiftKVCfg config = HawkConfigManager.getInstance().
				getKVInstance(DragonBoatGiftKVCfg.class);
		List<Integer> refreshTimeArr = config.getRefreshTimeList();
		int hour = HawkTime.getHour();
		int refreshIndex = -1;
		long refreshTime = 0;//12  20
		for(int i=0;i<refreshTimeArr.size();i++){
			int refreshhour = refreshTimeArr.get(i);
			if(hour < refreshhour){
				refreshIndex = i;
				break;
			}
		}
		if(refreshIndex == 0){
			int rhour = refreshTimeArr.get(refreshTimeArr.size() - 1);
			long time = HawkTime.getAM0Date().getTime();
			time -= HawkTime.DAY_MILLI_SECONDS;
			time += rhour * HawkTime.HOUR_MILLI_SECONDS;
			refreshTime = time;
		}else if(refreshIndex == -1){
			int rhour = refreshTimeArr.get(refreshTimeArr.size() - 1);
			long time = HawkTime.getAM0Date().getTime();
			time += rhour * HawkTime.HOUR_MILLI_SECONDS;
			refreshTime = time;
		}else{
			int rhour = refreshTimeArr.get(refreshIndex-1);
			long time = HawkTime.getAM0Date().getTime();
			time += rhour * HawkTime.HOUR_MILLI_SECONDS;
			refreshTime = time;
		}
		return refreshTime;
	}
	
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId,DragonBoatGiftEntity entity){
		DragonBoatGiftKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatGiftKVCfg.class);
		PBDragonBoat.Builder builder = this.getDataGeter().getDragonBoatInfo();
		DragonBoatGiftInfoResp.Builder rbuilder = DragonBoatGiftInfoResp.newBuilder();
		int giftLeft = 0;
		int boatAward = 1;
		if(builder != null){
			giftLeft = cfg.getGiftCount() - builder.getAwardRecordsCount();
			giftLeft = Math.max(0, giftLeft);
			long curBoatId = builder.getBoatId();
			if(!entity.getBoatGiftList().contains(String.valueOf(curBoatId))
					&& giftLeft > 0){
				boatAward = 0;
			}
		}
		rbuilder.setLoginAward(entity.getLoginAward());
		rbuilder.setBoatAward(boatAward);
		rbuilder.setGiftCount(giftLeft);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.DRAGON_BOAT_GIFT_INFO_RESP, rbuilder));
	}
	
	/**
	 * 获取领奖记录
	 * @param boatId
	 * @return
	 */
	public Map<String,String> getDragonBoatGiftRecord(long boatId){
		String recordKey = this.getDragonBoatGiftRecordKey(boatId);
		Map<String,String> awardRcecord = ActivityLocalRedis.getInstance().hgetAll(recordKey);
		return awardRcecord;
	}
	
	/**
	 * 添加领奖记录
	 * @param boatId
	 * @param playerId
	 */
	public void addDragonBoatGiftRecord(long boatId,String playerId){
		long curTime = HawkTime.getMillisecond();
		String recordKey = this.getDragonBoatGiftRecordKey(boatId);
		ActivityLocalRedis.getInstance().hsetWithExpire(
				recordKey, playerId, String.valueOf(curTime), (int)TimeUnit.DAYS.toSeconds(30));
	}
	

	public String getDragonBoatGiftRecordKey(long boatId){
		int termId = this.getActivityTermId();
		String serverId =this.getDataGeter().getServerId();
		return ActivityRedisKey.DRAGON_BOAT_GIFT_RECORD+":"+serverId+":"+termId+":"+boatId;
	}
	
}
