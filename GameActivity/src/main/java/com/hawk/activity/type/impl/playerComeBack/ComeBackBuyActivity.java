package com.hawk.activity.type.impl.playerComeBack;

import java.util.List;
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
import com.hawk.activity.type.impl.playerComeBack.cfg.buy.PlayerComeBackBuyConfig;
import com.hawk.activity.type.impl.playerComeBack.entity.PlayerComeBackEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.ComeBackPlayerBuyInfo;
import com.hawk.game.protocol.Activity.ComeBackPlayerBuyMsg;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

/***
 * 低折回馈
 * @author yang.rao
 *
 */
public class ComeBackBuyActivity extends ActivityBase {

	public ComeBackBuyActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PLAYER_COME_BACK_BUY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ComeBackBuyActivity activity = new ComeBackBuyActivity(config.getActivityId(), activityEntity);
		return activity;
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

	public Result<?> onPlayerBuyChest(int chestId, int count, String playerId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<PlayerComeBackEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		PlayerComeBackEntity entity = opEntity.get();
		
		PlayerComeBackBuyConfig chestCfg = HawkConfigManager.getInstance().getConfigByKey(PlayerComeBackBuyConfig.class, chestId);
		if(chestCfg == null){
			logger.error("ComeBackBuyActivity send error chestId:" + chestId);
		    return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		if(!entity.canBuy(chestId, count, chestCfg.getLimit())){
			logger.error("ComeBackBuyActivity buy error, chestId:{}, count:{}, entity:{}", chestId, count, entity);
			return Result.fail(Status.Error.ITEM_BUY_COUNT_EXCEED_VALUE);
		}
		List<RewardItem.Builder> prize = chestCfg.buildPrize(count);
			//扣道具
		boolean flag = this.getDataGeter().cost(playerId, prize, Action.COME_BACK_PLAYER_BUY);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//给宝箱奖励
		logger.info("ComeBackBuyActivity player:{}, buy chest, chestId:{}, count:{}, entity:{}", playerId, chestId, count, entity);
		this.getDataGeter().takeReward(playerId, chestCfg.getGainItemList(), count, Action.COME_BACK_PLAYER_BUY, true, RewardOrginType.COME_BACK_PLAYER_BUY_REWARD);
		//更新数据到数据库
		entity.onPlayerBuy(chestId, count);
		entity.notifyUpdate();
		//记录TLog日志
		getDataGeter().logComeBackBuy(playerId, chestId, count);
		//同步界面信息
		syncActivityInfo(playerId, entity);
		return null;
	}
	
	public void syncActivityInfo(String playerId, PlayerComeBackEntity entity){
		ComeBackPlayerBuyInfo.Builder build = ComeBackPlayerBuyInfo.newBuilder();
		ComeBackPlayerBuyMsg.Builder msgBuilder = null;
		if (entity.getBuyMsg() != null && !entity.getBuyMsg().isEmpty()) {
			for (Entry<Integer, Integer> entry : entity.getBuyMsg().entrySet()) {
				msgBuilder = ComeBackPlayerBuyMsg.newBuilder();
				msgBuilder.setBuyId(entry.getKey());
				msgBuilder.setNum(entry.getValue());
				build.addItems(msgBuilder);
			}
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PLAYER_COME_BACK_BUY_INFO_S, build));
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		//从活动id为105的活动缓存获取entity
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
