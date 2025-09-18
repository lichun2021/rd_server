package com.hawk.activity.type.impl.drogenBoatFestival.exchange;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.drogenBoatFestival.exchange.cfg.DragonBoatExchangeCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.exchange.entity.DragonBoatExchangeEntity;
import com.hawk.game.protocol.Activity.DragonBoatExchangeInfoResp;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 端午兑换
 * @author che
 *
 */
public class DragonBoatExchangeActivity extends ActivityBase{

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public DragonBoatExchangeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DRAGON_BOAT_EXCHANGE_ACTIVITY;
	}

	
	
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DragonBoatExchangeActivity activity = new DragonBoatExchangeActivity(
				config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DragonBoatExchangeEntity> queryList = HawkDBManager.getInstance()
				.query("from DragonBoatExchangeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DragonBoatExchangeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DragonBoatExchangeEntity entity = new DragonBoatExchangeEntity(playerId, termId);
//		List<Integer> tmp = new CopyOnWriteArrayList<>();
//		ConfigIterator<DragonBoatExchangeCfg> iter = HawkConfigManager.getInstance().getConfigIterator(DragonBoatExchangeCfg.class);
//		while(iter.hasNext()){
//			DragonBoatExchangeCfg cfg = iter.next();
//			tmp.add(cfg.getId());
//		}
//		entity.setCares(tmp);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.DRAGON_BOAT_EXCHANGE_INIT, () -> {
				this.syncActivityDataInfo(playerId);
			});
		}
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<DragonBoatExchangeEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	/**
	 * 更新兑换关注
	 * @param playerId
	 * @param exchangeType
	 * @param care
	 * @param protocolType
	 */
	public void exchageCare(String playerId,int exchangeType,int care,int protocolType){
		Optional<DragonBoatExchangeEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		DragonBoatExchangeEntity entity = opDataEntity.get();
		if(care == 0){
			entity.removeCare(exchangeType);
		}
		if(care == 1){
			entity.addCare(exchangeType);
		}
		this.syncActivityInfo(playerId,entity);
	}

	/**
	 * 更新全部兑换关注
	 * @param playerId
	 * @param care
	 * @param protocolType
	 */
	public void exchangeAllCare(String playerId, int care, int protocolType){
		Optional<DragonBoatExchangeEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		DragonBoatExchangeEntity entity = opDataEntity.get();
		if(care == 0){
			entity.getCares().clear();
			entity.notifyUpdate();
		}
		if(care == 1){
			List<Integer> tmp = new CopyOnWriteArrayList<>();
			ConfigIterator<DragonBoatExchangeCfg> iter = HawkConfigManager.getInstance().getConfigIterator(DragonBoatExchangeCfg.class);
			while(iter.hasNext()){
				DragonBoatExchangeCfg cfg = iter.next();
				tmp.add(cfg.getId());
			}
			entity.setCares(tmp);
			entity.notifyUpdate();
		}
		this.syncActivityInfo(playerId,entity);
	}

	@Override
	public <T extends HawkDBEntity> Optional<T> getPlayerDataEntity(String playerId) {
		Optional<DragonBoatExchangeEntity> opDataEntity = getPlayerDataEntity(playerId, true);
		if(!opDataEntity.isPresent()){
			return Optional.empty();
		}
		DragonBoatExchangeEntity entity = opDataEntity.get();
		if(HawkTime.getMillisecond() - entity.getCreateTime() < 1000l){
			List<Integer> tmp = new CopyOnWriteArrayList<>();
			ConfigIterator<DragonBoatExchangeCfg> iter = HawkConfigManager.getInstance().getConfigIterator(DragonBoatExchangeCfg.class);
			while(iter.hasNext()){
				DragonBoatExchangeCfg cfg = iter.next();
				tmp.add(cfg.getId());
			}
			entity.setCares(tmp);
			entity.notifyUpdate();
		}
		return Optional.of((T) entity);
	}

	/**
	 * 道具兑换
	 * @param playerId
	 * @param protolType
	 */
	public void itemExchange(String playerId,int exchangeType,int exchangeCount,int protocolType){
		DragonBoatExchangeCfg config = HawkConfigManager.getInstance().
				getConfigByKey(DragonBoatExchangeCfg.class, exchangeType);
		if (config == null) {
			return;
		}
		Optional<DragonBoatExchangeEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		DragonBoatExchangeEntity entity = opDataEntity.get();
		int eCount = entity.getExchangeCount(exchangeType);
		if(eCount + exchangeCount > config.getExchangeCount()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
					Status.Error.DRAGON_BOAT_EXCHANGE_LIMIT_VALUE);
			logger.info("DragonBoatExchange,itemExchange,fail,countless,playerId: "
					+ "{},exchangeType:{},ecount:{}", playerId,exchangeType,eCount);
			return;
		}
		List<RewardItem.Builder> makeCost = RewardHelper.toRewardItemList(config.getPay());
		boolean cost = this.getDataGeter().cost(playerId,makeCost, exchangeCount, Action.DRAGON_BOAT_EXCHANGE_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		List<RewardItem.Builder> makeAchieve = RewardHelper.toRewardItemList(config.getGain());
		//增加兑换次数
		entity.addExchangeCount(exchangeType, exchangeCount);
		//发奖励
		this.getDataGeter().takeReward(playerId,makeAchieve, exchangeCount, Action.DRAGON_BOAT_EXCHANGE_ACHIEVE, true);
		//同步
		this.syncActivityInfo(playerId,entity);
		//日志记录
		int termId = this.getActivityTermId();
		this.getDataGeter().logDragonBoatExchange(playerId, termId, exchangeType, exchangeCount);
		logger.info("DragonBoatExchange,itemExchange,sucess,playerId: "
				+ "{},exchangeType:{},ecount:{}", playerId,exchangeType,eCount);
		
	}
	
	
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId,DragonBoatExchangeEntity entity){
		Map<Integer,Integer> echanges = entity.getExchanges();
		DragonBoatExchangeInfoResp.Builder builder = DragonBoatExchangeInfoResp.newBuilder();
		for(Entry<Integer, Integer> entry : echanges.entrySet()){
			KeyValuePairInt.Builder kbuilder = KeyValuePairInt.newBuilder();
			kbuilder.setKey(entry.getKey());
			kbuilder.setVal(entry.getValue());
			builder.addEchangeList(kbuilder);
		}
		for(Integer care : entity.getCares()){
			builder.addCareList(care);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.DRAGON_BOAT_EXCHANGE_INFO_RESP, builder));
		
	}

	
	
}
