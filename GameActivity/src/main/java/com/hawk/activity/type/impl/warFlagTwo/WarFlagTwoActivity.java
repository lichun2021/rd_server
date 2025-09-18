package com.hawk.activity.type.impl.warFlagTwo;

import java.util.ArrayList;
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

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.warFlagTwo.cfg.WarFlagExchangeTwoConfig;
import com.hawk.activity.type.impl.warFlagTwo.entity.WarFlagTwoEntity;
import com.hawk.game.protocol.Activity.WarFlagTwoMsg;
import com.hawk.game.protocol.Activity.WarFlagTwoSyncInfoSyn;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

/**
 * 鹊桥会
 * @author golden
 *
 */
public class WarFlagTwoActivity extends ActivityBase {

	public WarFlagTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.WAR_FLAG_TWO_ACTIVITY;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			Optional<WarFlagTwoEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayerIds){
			callBack(playerId, GameConst.MsgId.WAR_FLAG_EXCHANGE_ACTIVITY_OPEN, () -> {
				Optional<WarFlagTwoEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					logger.error("on WarFlagTwoActivity open init WarFlagTwoEntity error, no entity created:" + playerId);
				}
				syncActivityInfo(playerId, opEntity.get());
			});
		}
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new WarFlagTwoActivity(config.getActivityId(), activityEntity);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<WarFlagTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from WarFlagTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			WarFlagTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		WarFlagTwoEntity entity = new WarFlagTwoEntity(playerId, termId);
		ConfigIterator<WarFlagExchangeTwoConfig> ite = HawkConfigManager.getInstance().getConfigIterator(WarFlagExchangeTwoConfig.class);
		List<Integer> ids = new ArrayList<Integer>();
		while(ite.hasNext()){
			WarFlagExchangeTwoConfig config = ite.next();
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
	
	/***
	 * 客户端勾提醒兑换
	 */
	public Result<?> reqActivityTips(String playerId, int id, int tips){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<WarFlagTwoEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		WarFlagTwoEntity entity = opt.get();
		if(tips > 0){
			entity.addTips(id);
		}else{
			entity.removeTips(id);
		}
		return Result.success();
	}
	
	public Result<Integer> brokenExchange(String playerId, int exchangeId, int num) {
		WarFlagExchangeTwoConfig cfg = HawkConfigManager.getInstance().getConfigByKey(WarFlagExchangeTwoConfig.class, exchangeId);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<WarFlagTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		WarFlagTwoEntity entity = opEntity.get();
		Integer buyNum = entity.getExchangeNumMap().get(exchangeId);
		int newNum = (buyNum == null ? 0 : buyNum) + num;
		if (newNum > cfg.getTimes()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}

		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.WAR_FLAG_TWO_EXCHANGE_TWO, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.getExchangeNumMap().put(exchangeId, newNum);
		entity.notifyUpdate();
		// 添加物品
		getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.WAR_FLAG_TWO_EXCHANGE_TWO, true, RewardOrginType.WARFLAG_TWO_EXCHANGE_REWARD);
		logger.info("war_flag_exchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
		syncActivityInfo(playerId, entity);

		return Result.success(newNum);
	}
	
	public void syncActivityInfo(String playerId, WarFlagTwoEntity entity) {
		WarFlagTwoSyncInfoSyn.Builder sbuilder = WarFlagTwoSyncInfoSyn.newBuilder();
		WarFlagTwoMsg.Builder msgBuilder = null;
		if (entity.getExchangeNumMap() != null && !entity.getExchangeNumMap().isEmpty()) {
			for (Entry<Integer, Integer> entry : entity.getExchangeNumMap().entrySet()) {
				msgBuilder = WarFlagTwoMsg.newBuilder();
				msgBuilder.setExchangeId(entry.getKey());
				msgBuilder.setNum(entry.getValue());
				sbuilder.addExchangeInfo(msgBuilder);
			}
		}
		for(Integer id : entity.getPlayerPoints()){
			sbuilder.addTips(id);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.WAR_FLAG_TWO_TIPS_INFO_S_VALUE, sbuilder));
	}
}
