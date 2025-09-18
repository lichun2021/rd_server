package com.hawk.activity.type.impl.domeExchange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.domeExchange.cfg.DomeActivityExchangeConfig;
import com.hawk.activity.type.impl.domeExchange.entity.DomeExchangeEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.DomeExchangeMsg;
import com.hawk.game.protocol.Activity.domeExchangeSyncInfoSyn;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

public class DomeExchangeActivity extends ActivityBase {

	public DomeExchangeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.DOME_EXCHANGE;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			Optional<DomeExchangeEntity> opDataEntity = this.getPlayerDataEntity(playerId);
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
			callBack(playerId, GameConst.MsgId.ON_DOME_EXCHANGE_ACTIVITY_OPEN, () -> {
				Optional<DomeExchangeEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					logger.error("on DomeExchangeActivity open init DomeExchangeEntity error, no entity created:" + playerId);
				}
				this.syncActivityInfo(playerId, opEntity.get());
			});
		}
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
		Optional<DomeExchangeEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		DomeExchangeEntity entity = opt.get();
		if(tips > 0){
			entity.addTips(id);
		}else{
			entity.removeTips(id);
		}
		return Result.success();
	}
	
	public Result<Integer> brokenExchange(String playerId, int exchangeId, int num) {
		DomeActivityExchangeConfig cfg = HawkConfigManager.getInstance().getConfigByKey(DomeActivityExchangeConfig.class, exchangeId);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<DomeExchangeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		DomeExchangeEntity entity = opEntity.get();
		Integer buyNum = entity.getExchangeNumMap().get(exchangeId);
		int newNum = (buyNum == null ? 0 : buyNum) + num;
		if (newNum > cfg.getTimes()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}

		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.DOME_EXCHANGE, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.getExchangeNumMap().put(exchangeId, newNum);
		entity.notifyUpdate();
		// 添加物品
		this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.DOME_EXCHANGE, true, RewardOrginType.DOME_EXCHANGE_REWARD);
		logger.info("dome_exchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
		this.syncActivityInfo(playerId, entity);

		return Result.success(newNum);
	}
	
	private void syncActivityInfo(String playerId, DomeExchangeEntity entity) {
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
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.DOME_EXCHANGE_TIPS_INFO_S_VALUE, sbuilder));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DomeExchangeActivity activity = new DomeExchangeActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DomeExchangeEntity> queryList = HawkDBManager.getInstance()
				.query("from DomeExchangeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DomeExchangeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DomeExchangeEntity entity = new DomeExchangeEntity(playerId, termId);
		ConfigIterator<DomeActivityExchangeConfig> ite = HawkConfigManager.getInstance().getConfigIterator(DomeActivityExchangeConfig.class);
		List<Integer> ids = new ArrayList<Integer>();
		while(ite.hasNext()){
			DomeActivityExchangeConfig config = ite.next();
			ids.add(config.getId());
		}
		entity.initTips(ids);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

}
