package com.hawk.activity.type.impl.commonExchangeTwo;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.commonExchangeTwo.cfg.CommonActivityExchangeTwoConfig;
import com.hawk.activity.type.impl.commonExchangeTwo.cfg.CommonExchangeTwoChestCfg;
import com.hawk.activity.type.impl.commonExchangeTwo.cfg.CommonExchangeTwoKVConfig;
import com.hawk.activity.type.impl.commonExchangeTwo.entity.CommonExchangeTwoEntity;
import com.hawk.game.protocol.Activity.DomeExchangeMsg;
import com.hawk.game.protocol.Activity.SupplyStationInfo;
import com.hawk.game.protocol.Activity.SupplyStationItem;
import com.hawk.game.protocol.Activity.domeExchangeSyncInfoSyn;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

public class CommonExchangeTwoActivity extends ActivityBase {

	public CommonExchangeTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.COMMON_EXCHANGE_TWO_ACTIVITY;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			syncActivityInfo(playerId);
			Optional<CommonExchangeTwoEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			this.syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayerIds){
			callBack(playerId, GameConst.MsgId.COMMON_EXCHANGE_TWO_INIT, () -> {
				syncActivityInfo(playerId);
				Optional<CommonExchangeTwoEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					logger.error("on CommonExchangeTwoActivity open init CommonExchangeTwoEntity error, no entity created:" + playerId);
				}
				this.syncActivityInfo(playerId, opEntity.get());
			});
		}
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<CommonExchangeTwoEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		if(!event.isCrossDay()){
			return;
		}
		CommonExchangeTwoKVConfig config = CommonExchangeTwoKVConfig.getInstance();
		if(!config.isPachageReset()){
			return;
		}
		CommonExchangeTwoEntity entity = opEntity.get();
		//记录一下清空之前的购买记录
		logger.info("CommonExchangeActivity before clear entity msg:{}", entity);
		entity.crossDay();
		entity.notifyUpdate();
		syncActivityInfo(event.getPlayerId());
	}

	/***
	 * 客户端勾提醒兑换
	 * @param playerId
	 * 
	 * @param tips : 0为去掉 1为增加
	 */
	public Result<?> reqActivityTips(String playerId, int id, int tips){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<CommonExchangeTwoEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		CommonExchangeTwoEntity entity = opt.get();
		if(tips > 0){
			entity.addTips(id);
		}else{
			entity.removeTips(id);
		}
		return Result.success();
	}
	
	public Result<Integer> brokenExchange(String playerId, int exchangeId, int num) {
		CommonActivityExchangeTwoConfig cfg = HawkConfigManager.getInstance().getConfigByKey(CommonActivityExchangeTwoConfig.class, exchangeId);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<CommonExchangeTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		CommonExchangeTwoEntity entity = opEntity.get();
		Integer buyNum = entity.getExchangeNumMap().get(exchangeId);
		int newNum = (buyNum == null ? 0 : buyNum) + num;
		if (newNum > cfg.getTimes()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}

		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.COMMON_EXCHANGE_TWO_EXCHANGE, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.getExchangeNumMap().put(exchangeId, newNum);
		entity.notifyUpdate();
		// 添加物品
		this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.COMMON_EXCHANGE_TWO_EXCHANGE, true, RewardOrginType.ACTIVITY_REWARD);
		logger.info("CommonExchangeActivity playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
		this.syncActivityInfo(playerId, entity);

		return Result.success(newNum);
	}
	
	/***
	 * 兑换的详细信息
	 * @param playerId
	 * @param entity
	 */
	private void syncActivityInfo(String playerId, CommonExchangeTwoEntity entity) {
		domeExchangeSyncInfoSyn.Builder sbuilder = domeExchangeSyncInfoSyn.newBuilder();
		DomeExchangeMsg.Builder msgBuilder = null;
		if (entity.getExchangeNumMap() != null && !entity.getExchangeNumMap().isEmpty()) {
			for (Entry<Integer, Integer> entry : entity.getExchangeNumMap().entrySet()) {
				msgBuilder = DomeExchangeMsg.newBuilder();
				msgBuilder.setExchangeId(entry.getKey());
				msgBuilder.setNum(entry.getValue());
				sbuilder.addExchangeInfo(msgBuilder);
			}
		}
		for(Integer id : entity.getPlayerPoints()){
			sbuilder.addTips(id);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.COMMON_EXCHANGE_TWO_TIPS_INFO_S_VALUE, sbuilder));
	}
	
	public Result<?> onPlayerBuyChest(int chestId, int count, String playerId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<CommonExchangeTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		CommonExchangeTwoEntity entity = opEntity.get();
		
		CommonExchangeTwoChestCfg chestCfg = HawkConfigManager.getInstance().getConfigByKey(CommonExchangeTwoChestCfg.class, chestId);
		if(chestCfg == null){
			logger.error("CommonExchangeActivity send error chestId:" + chestId);
		    return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		if(!entity.canBuy(chestId, count, chestCfg.getLimit())){
			logger.error("CommonExchangeActivity buy error, chestId:{}, count:{}, entity:{}", chestId, count, entity);
			return Result.fail(Status.Error.ITEM_BUY_COUNT_EXCEED_VALUE);
		}
		List<RewardItem.Builder> prize = chestCfg.buildPrize(count);
			//扣道具
		boolean flag = this.getDataGeter().cost(playerId, prize, Action.COMMON_EXCHANGE_TWO_BUY);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//给宝箱奖励
		logger.info("CommonExchangeActivity player:{}, buy chest, chestId:{}, count:{}, entity:{}", playerId, chestId, count, entity);
		this.getDataGeter().sendAwardFromAwardCfg(chestCfg.getAwardId(), count, playerId, true, Action.COMMON_EXCHANGE_TWO_BUY, RewardOrginType.SHOPPING_GIFT);
		//更新数据到数据库
		entity.onPlayerBuy(chestId, count);
		entity.notifyUpdate();
		//同步界面信息
		syncActivityInfo(playerId);
		return null;
	}

	/***
	 * 购买的详细信息 
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId){
		if(!isOpening(playerId)){
			return;
		}
		Optional<CommonExchangeTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("CommonExchangeTwoEntity init error, can't init entity, playerId:{}", playerId);
			return;
		}
		CommonExchangeTwoEntity entity = opEntity.get();
		
		SupplyStationInfo.Builder build = SupplyStationInfo.newBuilder();
		ConfigIterator<CommonExchangeTwoChestCfg> ite = HawkConfigManager.getInstance().getConfigIterator(CommonExchangeTwoChestCfg.class);
		while(ite.hasNext()){
			CommonExchangeTwoChestCfg config = ite.next();
			SupplyStationItem.Builder item = SupplyStationItem.newBuilder();
			item.setId(config.getId());
			item.setBuyCnt(entity.getBuyCnt(config.getId()));
			build.addItems(item);
		}
		//同步信息
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.COMMON_EXCHANGE_TWO_INFO_S_VALUE, build));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		CommonExchangeTwoActivity activity = new CommonExchangeTwoActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<CommonExchangeTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from CommonExchangeTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			CommonExchangeTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		CommonExchangeTwoEntity entity = new CommonExchangeTwoEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}

}
