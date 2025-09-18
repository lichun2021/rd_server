package com.hawk.activity.type.impl.playerComeBack;

import java.util.Optional;
import java.util.Map.Entry;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.playerComeBack.cfg.exchange.PlayerComeBackExchangeConfig;
import com.hawk.activity.type.impl.playerComeBack.entity.PlayerComeBackEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.ComeBackPlayerExchangeInfos;
import com.hawk.game.protocol.Activity.ComeBackPlayerExchangeMsg;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

/***
 * 专属军资 活动
 * @author yang.rao
 *
 */
public class ComeBackExchangeActivity extends ActivityBase {

	public ComeBackExchangeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PLAYER_COME_BACK_EXCHANGE;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if(!isOpening(playerId)){
			return;
		}
		if(!isHidden(playerId)){
			Optional<PlayerComeBackEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			PlayerComeBackEntity entity = opEntity.get();
			syncActivityInfo(playerId, entity);
		}
	}

	public Result<?> onPlayerExchange(String playerId, int exchangeId, int num){
		PlayerComeBackExchangeConfig cfg = HawkConfigManager.getInstance().getConfigByKey(PlayerComeBackExchangeConfig.class, exchangeId);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<PlayerComeBackEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		PlayerComeBackEntity entity = opEntity.get();
		Integer buyNum = entity.getExchangeNumMap().get(exchangeId);
		int newNum = (buyNum == null ? 0 : buyNum) + num;
		if (newNum > cfg.getLimit()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.COME_BACK_PLAYER_EXCHANGE, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		entity.getExchangeNumMap().put(exchangeId, newNum);
		entity.notifyUpdate();
		// 添加物品
		this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.COME_BACK_PLAYER_EXCHANGE, true, RewardOrginType.COME_BACK_PLAYER_EXCHANGE_REWARD);
		logger.info("ComeBackExchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
		//记录TLog日志
		getDataGeter().logComeBackExchange(playerId, exchangeId, num);
		this.syncActivityInfo(playerId, entity);
		return Result.success(newNum);
	}
	
	/***
	 * 刷新界面给客户端
	 * @param playerId
	 * @param entity
	 */
	private void syncActivityInfo(String playerId, PlayerComeBackEntity entity) {
		ComeBackPlayerExchangeInfos.Builder sbuilder = ComeBackPlayerExchangeInfos.newBuilder();
		ComeBackPlayerExchangeMsg.Builder msgBuilder = null;
		if (entity.getExchangeNumMap() != null && !entity.getExchangeNumMap().isEmpty()) {
			for (Entry<Integer, Integer> entry : entity.getExchangeNumMap().entrySet()) {
				msgBuilder = ComeBackPlayerExchangeMsg.newBuilder();
				msgBuilder.setExchangeId(entry.getKey());
				msgBuilder.setNum(entry.getValue());
				sbuilder.addExchangeInfo(msgBuilder);
			}
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PLAYER_COME_BACK_EXCHANGE_INFO_S_VALUE, sbuilder));
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ComeBackExchangeActivity activity = new ComeBackExchangeActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		HawkDBEntity entity = PlayerDataHelper.getInstance().getActivityDataEntity(playerId, ActivityType.PLAYER_COME_BACK_ACHIEVE);
		if(entity == null){
			//尝试通过id为105的活动，去加载一次entity(考虑到成就活动一直没更新，缓存可以被移除)
			Optional<ActivityBase> optional = ActivityManager.getInstance().getActivity(Activity.ActivityType.COME_BACK_PLAYER_ACHIEVE_VALUE);
			ComeBackAchieveTaskActivity activity = (ComeBackAchieveTaskActivity)optional.get();
			Optional<HawkDBEntity> dbOp = activity.getPlayerDataEntity(playerId);
			if(dbOp.isPresent()){
				return dbOp.get();
			}
			return null;
		}
		return entity;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		return null;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	@Override
	public boolean isHidden(String playerId) {
		Optional<PlayerComeBackEntity> optional = getPlayerDataEntity(playerId);
		if(!optional.isPresent()){
			return true;
		}
		PlayerComeBackEntity entity = optional.get();
		if(!entity.isInit()){ //都没有初始化
			return true;
		}
		return super.isHidden(playerId);
	}

}
