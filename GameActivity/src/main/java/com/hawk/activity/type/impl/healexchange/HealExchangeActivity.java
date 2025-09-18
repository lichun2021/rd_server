package com.hawk.activity.type.impl.healexchange;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.healexchange.cfg.HealExchangeExchangeAwardCfg;
import com.hawk.activity.type.impl.healexchange.cfg.HealExchangeTimeCfg;
import com.hawk.activity.type.impl.healexchange.entity.HealExchangeEntity;
import com.hawk.game.protocol.Activity.PBHealExchangeExchange;
import com.hawk.game.protocol.Activity.PBHealExchangeInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

public class HealExchangeActivity extends ActivityBase {
	public final Logger logger = LoggerFactory.getLogger("Server");

	public HealExchangeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.HEAL_EXCHANGE_ACTIVITY;
	}

	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		HealExchangeActivity activity = new HealExchangeActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	public boolean isActivityClose(String playerId) {
		Optional<HealExchangeEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return true;
		}
		HealExchangeEntity entity = opDataEntity.get();
		if(entity.getActive() >0){
			return false;
		}
		boolean guildJoin = getDataGeter().isJoinCurrWar(getDataGeter().getGuildId(playerId));
		if (guildJoin) {
			return false;
		}
		
		return true;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		super.onPlayerLogin(playerId);
		if (this.isShow(playerId)) {
			Optional<HealExchangeEntity> opEntity = getPlayerDataEntity(playerId);
			if (opEntity.isPresent()) {
				opEntity.get().recordLoginDay();
			}
			syncActivityDataInfo(playerId);
		}
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		if (this.isShow(playerId)) {
			Optional<HealExchangeEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			HealExchangeEntity entity = opEntity.get();
			PBHealExchangeInfo.Builder builder = PBHealExchangeInfo.newBuilder();
			builder.setActive(entity.getActive() >0);
			Map<Integer, Integer> emap = entity.getExchangeNumMap();

			for (Entry<Integer, Integer> entry : emap.entrySet()) {
				PBHealExchangeExchange.Builder ebuilder = PBHealExchangeExchange.newBuilder();
				ebuilder.setExchangeId(entry.getKey());
				ebuilder.setNum(entry.getValue());
				builder.addExchanges(ebuilder);
			}
			HealExchangeTimeCfg tcfg = HawkConfigManager.getInstance().getConfigByKey(HealExchangeTimeCfg.class, getActivityTermId());
			builder.setBattleTime(tcfg.getBattleTime());
			// push
			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.HEAL_ITEM_EXECHANGE_INFO_SYNC, builder));
		}
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<HealExchangeEntity> queryList = HawkDBManager.getInstance()
				.query("from HealExchangeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			HealExchangeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		HealExchangeEntity entity = new HealExchangeEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onOpen() {
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		Optional<HealExchangeEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		HealExchangeEntity entity = optional.get();
		entity.recordLoginDay();
		this.syncActivityDataInfo(playerId);
	}


	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}

	/**
	 * 道具兑换
	 * @param playerId
	 * @param protolType
	 */
	public void itemExchange(String playerId, int exchangeId, int exchangeCount, int protocolType) {
		if (!isOpening(playerId)) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType, Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		HealExchangeExchangeAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(HealExchangeExchangeAwardCfg.class, exchangeId);
		if (config == null) {
			return;
		}
		if(config.getTermId() != getActivityTermId()){
			logger.info("HealExchangeActivity,itemExchange,fail,termErr,playerId: "
					+ "{},exchangeType:{} ", playerId, exchangeId);
			return;
		}
		Optional<HealExchangeEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		HealExchangeEntity entity = opDataEntity.get();
		if(entity.getActive() <=0){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
					protocolType, Status.Error.HEAL_EXCHANGE_NOT_ACTIV_VALUE);
			return;
		} 
		
		int eCount = entity.getExchangeCount(exchangeId);
		if (eCount + exchangeCount > config.getExchangeCount()) {
			logger.info("HealExchangeActivity,itemExchange,fail,countless,playerId: "
					+ "{},exchangeType:{},ecount:{}", playerId, exchangeId, eCount);
			return;
		}

		List<RewardItem.Builder> makeCost = RewardHelper.toRewardItemImmutableList(config.getPay());
		boolean cost = this.getDataGeter().cost(playerId, makeCost, exchangeCount, Action.HEAL_EXCAHNGE, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
					protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}

		// 增加兑换次数
		entity.addExchangeCount(exchangeId, exchangeCount);
		// 发奖励
		this.getDataGeter().takeReward(playerId, RewardHelper.toRewardItemImmutableList(config.getGain()),
				exchangeCount, Action.HEAL_EXCAHNGE, true);
		// 同步
		this.syncActivityDataInfo(playerId);
		logger.info("HealExchangeActivity,itemExchange,sucess,playerId: "
				+ "{},exchangeType:{},ecount:{}", playerId, exchangeId, eCount);

	}

}
