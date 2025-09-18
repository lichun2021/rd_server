package com.hawk.activity.type.impl.cakeShare;

import java.util.Collection;
import java.util.Date;
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
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.CakeShareGetRewardEvent;
import com.hawk.activity.event.impl.CakeShareRefreshEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.cakeShare.cfg.CakeShareKVCfg;
import com.hawk.activity.type.impl.cakeShare.entity.CakeShareEntity;
import com.hawk.game.protocol.Activity.CakeShareGiftInfoResp;
import com.hawk.game.protocol.Activity.CakeShareLocationResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.PBCakeShare;
import com.hawk.gamelib.GameConst.MsgId;
/**
 * 周年庆-蛋糕分享
 * hf
 */
public class CakeShareActivity extends ActivityBase{

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public CakeShareActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.CAKE_SHARE_ACTIVITY;
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		CakeShareActivity activity = new CakeShareActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<CakeShareEntity> queryList = HawkDBManager.getInstance()
				.query("from CakeShareEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			CakeShareEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		CakeShareEntity entity = new CakeShareEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.CAKE_SHARE_INIT, () -> {
				this.syncActivityDataInfo(playerId);
			});
		}
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<CakeShareEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	/**
	 * 监听蛋糕刷新事件
	 */
	@Subscribe
	public void onCakeShareRefresh(CakeShareRefreshEvent event){
		logger.info("CakeShareActivity ,onCakeShareRefresh,playerId:{},cakeId:{}", event.getPlayerId(),event.getCakeId());
		this.syncActivityDataInfo(event.getPlayerId());
	}
	
	

	/**
	 * 监听蛋糕奖励获取事件
	 */
	@Subscribe
	public void onGetCakeShareReward(CakeShareGetRewardEvent event){
		String playerId = event.getPlayerId();
		long cakeId = event.getCakeId();
		Optional<CakeShareEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		//记录
		CakeShareEntity entity = opDataEntity.get();
		entity.addCakeAwardRecord(String.valueOf(cakeId));
		this.addCakeShareRewardRecord(cakeId, playerId);
		this.syncActivityInfo(playerId, entity);
		logger.info("CakeShareActivity, onGetCakeShareReward ,playerId:{},cakeId:{}", playerId,cakeId);
	}
	
	/**
	 * 获取蛋糕位置信息
	 * @param playerId
	 */
	public void getCakeSharePos(String playerId){
		HawkTuple2<Integer, Integer> pos = this.getDataGeter().getCakeSharePos();
		if(pos == null){
			logger.info("CakeShareActivity,getCakeSharePos pos null,playerId: "+ "{}", playerId);
			return;
		}
		CakeShareLocationResp.Builder builder = CakeShareLocationResp.newBuilder();
		builder.setPosX(pos.first);
		builder.setPosY(pos.second);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.CAKE_SHARE_LOCATION_RESP, builder));
		logger.info("CakeShareActivity,getCakeSharePos pos sucess,playerId:{},px:{},py:{}", playerId,pos.first,pos.second);
	}
	
	/**
	 * 获取蛋糕轮次
	 * @return
	 */
	public HawkTuple3<Integer, Long, Long> getAwardTurn(){
		long curTime = HawkTime.getMillisecond();
		int termId = this.getActivityTermId();
		long startTime = this.getTimeControl().getStartTimeByTermId(termId);
		long endTime = this.getTimeControl().getEndTimeByTermId(termId);
		int crossDays = (int) ((endTime - startTime)/HawkTime.DAY_MILLI_SECONDS + 3);
		
		long startAmZero = HawkTime.getAM0Date(new Date(startTime)).getTime();
		int turn = 0;
		CakeShareKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(CakeShareKVCfg.class);
		for(int i=0;i<crossDays;i++){
			for(int hour : cfg.getRefreshTimeList()){
				long awardStartTime = startAmZero + hour * HawkTime.HOUR_MILLI_SECONDS;
				long awardEndTime = awardStartTime + cfg.getRewardDuration();
				//不在活动时间内
				if(awardEndTime < startTime){
					continue;
				}
				//波数+1
				turn ++;
				if(curTime < awardEndTime){
					return HawkTuples.tuple(turn, awardStartTime, awardEndTime);
				}
			}
			//增加一天
			startAmZero += HawkTime.DAY_MILLI_SECONDS;
		}
		return null;
	}

	
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId, CakeShareEntity entity){
		CakeShareKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(CakeShareKVCfg.class);
		PBCakeShare.Builder builder = this.getDataGeter().getCakeShareInfo();
		CakeShareGiftInfoResp.Builder rbuilder = CakeShareGiftInfoResp.newBuilder();
		//世界点还没有刷新，直接给第一轮
		if(builder == null){
			HawkTuple3<Integer, Long, Long>  turnInfo = this.getAwardTurn();
			HawkTuple2<Integer, Integer> turnkAward = cfg.getTurnAward(turnInfo.first);
			rbuilder.setCakeTurn(turnInfo.first);
			rbuilder.setMarchReward(turnkAward.second);
			rbuilder.setStartRewardTime(turnInfo.second);
			rbuilder.setEndRewardTime(turnInfo.third);
			rbuilder.setCakeAward(0);
			rbuilder.setGiftCount(0);
		}else{
			int curCakeId = builder.getCakeId();
			//剩余礼包数量
			int giftLeft = cfg.getGiftCount() - builder.getAwardRecordsCount();
			giftLeft = Math.max(0, giftLeft);
			//是否已经领奖
			int cakeAward =1;
			if(!entity.getCakeGiftList().contains(String.valueOf(curCakeId)) && giftLeft > 0){
				cakeAward = 0;
			}
			//礼包奖励
			HawkTuple2<Integer, Integer> turnkAward = cfg.getTurnAward(curCakeId);
			//构造
			rbuilder.setCakeTurn(curCakeId);
			rbuilder.setMarchReward(turnkAward.second);
			rbuilder.setStartRewardTime(builder.getStartTime());
			rbuilder.setEndRewardTime(builder.getEndTime());
			rbuilder.setCakeAward(cakeAward);
			rbuilder.setGiftCount(giftLeft);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.CAKE_SHARE_INFO_RESP_VALUE, rbuilder));
	}
	
	/**
	 * 获取领奖记录
	 * @param cakeId
	 * @return
	 */
	public Map<String,String> getCakeShareRecord(long cakeId){
		String recordKey = this.getCakeShareRecordKey(cakeId);
		Map<String,String> awardRcecord = ActivityLocalRedis.getInstance().hgetAll(recordKey);
		return awardRcecord;
	}
	
	/**
	 * 添加领奖记录
	 * @param cakeId
	 * @param playerId
	 */
	public void addCakeShareRewardRecord(long cakeId,String playerId){
		long curTime = HawkTime.getMillisecond();
		String recordKey = this.getCakeShareRecordKey(cakeId);
		ActivityLocalRedis.getInstance().hsetWithExpire(recordKey, playerId, String.valueOf(curTime), (int)TimeUnit.DAYS.toSeconds(30));
	}
	

	public String getCakeShareRecordKey(long cakeId){
		int termId = this.getActivityTermId();
		String serverId =this.getDataGeter().getServerId();
		return ActivityRedisKey.CAKE_SHARE_RECORD + ":" + serverId + ":" + termId + ":" + cakeId;
	}
	
	
	/**根据配置获取下次刷新时间点
	 * @param resetTime
	 * @return
	 */
	public boolean isBetweenReceiveTime(){
		CakeShareKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(CakeShareKVCfg.class);
		long duration = cfg.getRewardDuration(); //持续时间
		List<Integer> refreshTimeList = cfg.getRefreshTimeList();
		for (int dayHour : refreshTimeList) {
			long nowTime = HawkTime.getMillisecond();
			long zoreTime = HawkTime.getAM0Date().getTime();
			long startTime = zoreTime + dayHour * HawkTime.HOUR_MILLI_SECONDS;
			long endTime = startTime + duration;
			if (nowTime >= startTime && nowTime <= endTime) {
				return true;
			}
		}
		return false;
	}
	
	
	
	
}
