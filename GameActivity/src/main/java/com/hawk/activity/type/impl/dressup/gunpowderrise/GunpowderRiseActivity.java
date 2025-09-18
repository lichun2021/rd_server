
package com.hawk.activity.type.impl.dressup.gunpowderrise;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.dressup.gunpowderrise.cfg.GunpowderRiseExchangeCfg;
import com.hawk.activity.type.impl.dressup.gunpowderrise.entity.GunpowderRiseEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;


/**
 * 装扮投放系列活动四:浴火重生(硝烟再起)
 * @author hf
 */

public class GunpowderRiseActivity extends ActivityBase {

	public GunpowderRiseActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GUNPOWDER_RISE_ACTIVITY;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			Optional<GunpowderRiseEntity> opDataEntity = this.getPlayerDataEntity(playerId);
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
			callBack(playerId, GameConst.MsgId.ACHIEVE_INIT_GUNPOWDER_RISE, () -> {
				Optional<GunpowderRiseEntity> opEntity = getPlayerDataEntity(playerId);
				if (!opEntity.isPresent()) {
					logger.error("on GunpowderRiseTwoActivity open init GunpowderRiseTwoEntity error, no entity created:" + playerId);
				}
				syncActivityInfo(playerId, opEntity.get());
			});
		}
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new GunpowderRiseActivity(config.getActivityId(), activityEntity);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GunpowderRiseEntity> queryList = HawkDBManager.getInstance()
				.query("from GunpowderRiseEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GunpowderRiseEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GunpowderRiseEntity entity = new GunpowderRiseEntity(playerId, termId);
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
		Optional<GunpowderRiseEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		GunpowderRiseEntity entity = opt.get();
		if(tips > 0){
			entity.addTips(id);
		}else{
			entity.removeTips(id);
		}
		return Result.success();
	}
	/**
	 * 兑换物品
	 * @param playerId
	 * @param exchangeId
	 * @param num
	 * @return
	 */
	public Result<Integer> gunpowderRiseExchange(String playerId, int exchangeId, int num) {
		if (num < 0){
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		GunpowderRiseExchangeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GunpowderRiseExchangeCfg.class, exchangeId);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<GunpowderRiseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		GunpowderRiseEntity entity = opEntity.get();
		Integer buyNum = entity.getExchangeNumMap().get(exchangeId);
		int newNum = (buyNum == null ? 0 : buyNum) + num;
		if (newNum > cfg.getExchangeCount()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}

		boolean flag = this.getDataGeter().cost(playerId, cfg.getPayItemList(), num, Action.GUNPOWDER_RISE_COST, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.getExchangeNumMap().put(exchangeId, newNum);
		entity.notifyUpdate();
		// 添加物品
		getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.GUNPOWDER_RISE_REWARD, true, RewardOrginType.GUNPOWDER_RISE_REWARD);
		logger.info("GunpowderRiseTwoActivity  gunpowderRiseExchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
		syncActivityInfo(playerId, entity);

		return Result.success(newNum);
	}

	/**
	 * 活动信息同步
	 * @param playerId
	 * @param entity
	 */
	public void syncActivityInfo(String playerId, GunpowderRiseEntity entity) {
		Activity.GunpowderRiseInfoSync.Builder builder = Activity.GunpowderRiseInfoSync.newBuilder();
		Activity.PBGunpowderRiseExchange.Builder msgBuilder = null;
		if (entity.getExchangeNumMap() != null && !entity.getExchangeNumMap().isEmpty()) {
			for (Entry<Integer, Integer> entry : entity.getExchangeNumMap().entrySet()) {
				msgBuilder = Activity.PBGunpowderRiseExchange.newBuilder();
				msgBuilder.setExchangeId(entry.getKey());
				msgBuilder.setNum(entry.getValue());
				builder.addExchangeInfo(msgBuilder);
			}
		}
		for(Integer id : entity.getPlayerPoints()){
			builder.addTips(id);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.GUNPOWDER_RISE_INFO_SYNC_VALUE, builder));
	}
}

