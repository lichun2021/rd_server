package com.hawk.activity.type.impl.supplyStation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.supplyStation.cfg.SupplyStationChestCfg;
import com.hawk.activity.type.impl.supplyStation.cfg.SupplyStationKVConfig;
import com.hawk.activity.type.impl.supplyStation.entity.SupplyStationEntity;
import com.hawk.game.protocol.Activity.SupplyStationInfo;
import com.hawk.game.protocol.Activity.SupplyStationItem;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class SupplyStationActivity extends ActivityBase {
	
	private static final Logger logger = LoggerFactory.getLogger("Server");

	public SupplyStationActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SUPPLY_STATION_ACTIVITY;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			syncActivityInfo(playerId);
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayerIds){
			callBack(playerId, GameConst.MsgId.SUPPLY_STATION_INIT, () -> {
				syncActivityInfo(playerId);
			});
		}
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<SupplyStationEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		if(!event.isCrossDay()){
			return;
		}
		SupplyStationKVConfig config = SupplyStationKVConfig.getInstance();
		if(!config.isReset()){
			return;
		}
		SupplyStationEntity entity = opEntity.get();
		//记录一下清空之前的购买记录
		logger.info("SupplyStationActivity before clear entity msg:{}", entity);
		entity.crossDay();
		entity.notifyUpdate();
		syncActivityInfo(event.getPlayerId());
	}

	public Result<?> onPlayerBuyChest(int chestId, int count, String playerId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<SupplyStationEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		SupplyStationEntity entity = opEntity.get();
		SupplyStationChestCfg chestCfg = HawkConfigManager.getInstance().getConfigByKey(SupplyStationChestCfg.class, chestId);
		if(chestCfg == null){
			logger.error("SupplyStationActivity send error chestId:" + chestId);
		    return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		if(!entity.canBuy(chestId, count, chestCfg.getLimit())){
			logger.error("SupplyStationActivity buy error, chestId:{}, count:{}, entity:{}", chestId, count, entity);
			return Result.fail(Status.Error.ITEM_BUY_COUNT_EXCEED_VALUE);
		}
		List<RewardItem.Builder> prize = chestCfg.buildPrize(count);
			//扣道具
		boolean flag = this.getDataGeter().cost(playerId, prize, Action.SUPPLY_STATION_COST);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//给宝箱奖励
		logger.info("SupplyStationActivity player:{}, buy chest, chestId:{}, count:{}", playerId, chestId, count);
		this.getDataGeter().sendAwardFromAwardCfg(chestCfg.getAwardId(), count, playerId, true, Action.SUPPLY_STATION_REWARD, RewardOrginType.SHOPPING_GIFT);
		//更新数据到数据库
		entity.onPlayerBuy(chestId, count);
		entity.notifyUpdate();
		//同步界面信息
		syncActivityInfo(playerId);
		return null;
	}

	public void syncActivityInfo(String playerId){
		if(!isOpening(playerId)){
			return;
		}
		Optional<SupplyStationEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("SupplyStationEntity init error, can't init entity, playerId:{}", playerId);
			return;
		}
		SupplyStationEntity entity = opEntity.get();
		
		SupplyStationInfo.Builder build = SupplyStationInfo.newBuilder();
		ConfigIterator<SupplyStationChestCfg> ite = HawkConfigManager.getInstance().getConfigIterator(SupplyStationChestCfg.class);
		while(ite.hasNext()){
			SupplyStationChestCfg config = ite.next();
			SupplyStationItem.Builder item = SupplyStationItem.newBuilder();
			item.setId(config.getId());
			item.setBuyCnt(entity.getBuyCnt(config.getId()));
			build.addItems(item);
		}
		//同步信息
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.SUPPLY_STATION_INFO_S_VALUE, build));
	}
	
//	/***
//	 * 构建宝箱奖励
//	 * @param chestCfg
//	 * @param count
//	 * @return
//	 */
//	private List<RewardItem.Builder> buildChestReward(SupplyStationChestCfg chestCfg, int count){
//		if(chestCfg == null || count <= 0){
//			return null;
//		}
//		List<RewardItem.Builder> reward = new ArrayList<RewardItem.Builder>();
//		for(int i = 0 ; i < count ; i ++){
//			reward.addAll(chestCfg.buildOrdinaryReward()); //先把普通奖励弄进来
//			reward.addAll(getChanceReward(chestCfg)); //概率性道具加入
//		}
//		return reward;
//	}
	
//	/***
//	 * 构建一次机会物品
//	 * @param chestCfg
//	 * @return
//	 */
//	private List<RewardItem.Builder> getChanceReward(SupplyStationChestCfg chestCfg){
//		if(chestCfg == null){
//			return null;
//		}
//		Map<String, Integer> map = new HashMap<>();
//		String[] chance_rewards = chestCfg.getChanceRewards();
//		Integer[] rates = chestCfg.getChanceRates();
//		if(chance_rewards.length != rates.length){
//			HawkLog.errPrintln("SupplyStationActivity chance reward error. config error:{}" + chestCfg);
//			return null;
//		}
//		for(int i = 0 ; i < chance_rewards.length ; i ++){
//			map.put(chance_rewards[i], rates[i]);
//		}
//		//随机出来的物品
//		String reward = HawkRand.randomWeightObject(map);
//		return RewardHelper.toRewardItemList(reward);
//	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SupplyStationActivity activity = new SupplyStationActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SupplyStationEntity> queryList = HawkDBManager.getInstance()
				.query("from SupplyStationEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SupplyStationEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SupplyStationEntity entity = new SupplyStationEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

}
