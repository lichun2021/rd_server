package com.hawk.activity.type.impl.dressup.firereignite;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.dressup.firereignite.cfg.FireReigniteActivityKVCfg;
import com.hawk.activity.type.impl.dressup.firereignite.cfg.FireReigniteExchangeCfg;
import com.hawk.activity.type.impl.dressup.firereignite.entity.FireReigniteEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.result.Result;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 装扮投放系列活动三:重燃战火
 * @author hf
 */
public class FireReigniteActivity extends ActivityBase {

	public FireReigniteActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.FIRE_REIGNITE_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		FireReigniteActivity activity = new FireReigniteActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<FireReigniteEntity> queryList = HawkDBManager.getInstance()
				.query("from FireReigniteEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			FireReigniteEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		FireReigniteEntity entity = new FireReigniteEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			Optional<FireReigniteEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_FIRE_REIGNITE, ()-> {
				Optional<FireReigniteEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					logger.error("on FireReigniteTwoActivity open init FireReigniteTwoEntity error, no entity created:" + playerId);
				}
				syncActivityInfo(playerId, opEntity.get());
			});
		}
	}

	/**
	 * 购买物品
	 * @param playerId
	 * @param num
	 * @return
	 */
	public Result<Integer> exchangeFireReigniteItem(String playerId, int index, int num) {
		//普通兑换只能一次一个 策划又改的,可以多个2021/11/18
	/*	if (index == 1 && num != 1){
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}*/
		FireReigniteActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FireReigniteActivityKVCfg.class);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<FireReigniteEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		FireReigniteEntity entity = opEntity.get();
		//index = 2是金条兑换有次数限制
		if (index == 2 && entity.getExchangeNum() + num > cfg.getExchangeLimit()){
			return Result.fail(Status.Error.FIRE_REIGNITE_EXCHANGE_LIMIT_VALUE);
		}
		FireReigniteExchangeCfg exchangeCfg = HawkConfigManager.getInstance().getConfigByKey(FireReigniteExchangeCfg.class, index);
		//购买消耗物品检测
		boolean flag = this.getDataGeter().cost(playerId, exchangeCfg.getPayItemList(), num, Action.FIRE_REIGNITE_BUY_COST, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//index = 2是金条兑换修改次数
		if (index == 2 ){
			entity.addExchangeNum(num);
		}
		//玩家增加经验
		int addExp = exchangeCfg.getExpGain() * num;
		entity.addExp(addExp);
		// 添加物品
		getDataGeter().takeReward(playerId, exchangeCfg.getGainItemList(), num, Action.FIRE_REIGNITE_BUY_REWARD, true, Reward.RewardOrginType.FIRE_REIGNITE_REWARD);
		logger.info("FireReigniteTwoActivity exchangeFireReigniteItem playerId:{}, index:{}, num:{}, exchangeNum:{}", playerId, index, num, entity.getExchangeNum());
		//同步
		syncActivityInfo(playerId, entity);
		return Result.success();
	}

	/**
	 * 领取宝箱
	 * @param playerId
	 * @return
	 */
	public Result<Integer> receiveBoxReward(String playerId) {
		FireReigniteActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FireReigniteActivityKVCfg.class);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<FireReigniteEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		FireReigniteEntity entity = opEntity.get();
  		//经验值
		int exp = entity.getExp();
		//可领取的宝箱数
		int canRewardBoxNum = exp / cfg.getBoxExp();
		//已经领取的宝箱数
		int receiveBoxNum = entity.getRecBoxNum();
		if (receiveBoxNum >= canRewardBoxNum){
			return Result.fail(Status.Error.FIRE_REIGNITE_REWARD_BOX_LIMIT_VALUE);
		}
		entity.addRecBoxNum(1);
		// 添加物品
		getDataGeter().takeReward(playerId, cfg.getBoxRewardList(), 1, Action.FIRE_REIGNITE_BOX_REWARD, true, Reward.RewardOrginType.FIRE_REIGNITE_REWARD);
		//tlog
		getDataGeter().logFireReigniteReceiveBox(playerId, getActivityTermId());
		logger.info("FireReigniteTwoActivity receiveBoxReward playerId:{}, exp:{},canRewardBoxNum:{},receiveBoxNum:{}", playerId, exp, canRewardBoxNum, receiveBoxNum);
		syncActivityInfo(playerId, entity);
		return Result.success();
	}
	/**
	 * 同步消息
	 * @param playerId
	 * @param entity
	 */
	public void syncActivityInfo(String playerId, FireReigniteEntity entity) {
		int exp = entity.getExp();
		int recBoxNum = entity.getRecBoxNum();
		int exchangeNum = entity.getExchangeNum();
		Activity.FireReigniteInfoSync.Builder builder = Activity.FireReigniteInfoSync.newBuilder();
		builder.setExp(exp);
		builder.setRecBoxNum(recBoxNum);
		builder.setExchangeNum(exchangeNum);
		pushToPlayer(playerId, HP.code.FIRE_REIGNITE_INFO_SYNC_VALUE, builder);
	}


	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}


}
