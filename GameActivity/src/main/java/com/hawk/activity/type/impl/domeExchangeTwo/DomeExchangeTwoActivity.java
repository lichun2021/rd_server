package com.hawk.activity.type.impl.domeExchangeTwo;

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
import com.hawk.activity.type.impl.domeExchangeTwo.cfg.DomeActivityExchangeTwoConfig;
import com.hawk.activity.type.impl.domeExchangeTwo.entity.DomeExchangeTwoEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.DomeExchangeMsg;
import com.hawk.game.protocol.Activity.domeExchangeSyncInfoSyn;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

public class DomeExchangeTwoActivity extends ActivityBase {

	public DomeExchangeTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.DOME_EXCHANGE_TWO;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			Optional<DomeExchangeTwoEntity> opDataEntity = this.getPlayerDataEntity(playerId);
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
				Optional<DomeExchangeTwoEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					logger.error("on DomeExchangeTwoActivity open init DomeExchangeEntity error, no entity created:" + playerId);
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
		Optional<DomeExchangeTwoEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		DomeExchangeTwoEntity entity = opt.get();
		if(tips > 0){
			entity.addTips(id);
		}else{
			entity.removeTips(id);
		}
		return Result.success();
	}
	
	public Result<Integer> brokenExchange(String playerId, int exchangeId, int num) {
		DomeActivityExchangeTwoConfig cfg = HawkConfigManager.getInstance().getConfigByKey(DomeActivityExchangeTwoConfig.class, exchangeId);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<DomeExchangeTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		DomeExchangeTwoEntity entity = opEntity.get();
		Integer buyNum = entity.getExchangeNumMap().get(exchangeId);
		int newNum = (buyNum == null ? 0 : buyNum) + num;
		if (newNum > cfg.getTimes()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}

		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.DOME_EXCHANGE_TWO, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.getExchangeNumMap().put(exchangeId, newNum);
		entity.notifyUpdate();
		// 添加物品
		this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.DOME_EXCHANGE_TWO, true, RewardOrginType.DOME_EXCHANGE_REWARD);
		logger.info("dome_exchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
		this.syncActivityInfo(playerId, entity);

		return Result.success(newNum);
	}
	
	private void syncActivityInfo(String playerId, DomeExchangeTwoEntity entity) {
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
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.DOME_EXCHANGE_TWO_TIPS_INFO_S_VALUE, sbuilder));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DomeExchangeTwoActivity activity = new DomeExchangeTwoActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DomeExchangeTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from DomeExchangeTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DomeExchangeTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DomeExchangeTwoEntity entity = new DomeExchangeTwoEntity(playerId, termId);
		ConfigIterator<DomeActivityExchangeTwoConfig> ite = HawkConfigManager.getInstance().getConfigIterator(DomeActivityExchangeTwoConfig.class);
		List<Integer> ids = new ArrayList<Integer>();
		while(ite.hasNext()){
			DomeActivityExchangeTwoConfig config = ite.next();
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
