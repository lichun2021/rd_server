package com.hawk.activity.type.impl.playerComeBack;

import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.result.Result;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.playerComeBack.cfg.reward.PlayerComeBackRewardConfig;
import com.hawk.activity.type.impl.playerComeBack.entity.PlayerComeBackEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.ComeBackPlayerRewardInfo;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

/****
 * 回归大礼 
 * ***(整个老玩家回归的判定等相关活动主体，在该活动实现，放在这里是因为该活动的活动号最小，调用顺序在其它活动之前)***
 * @author yang.rao
 *
 */
public class ComeBackRewardActivity extends ActivityBase {

	public ComeBackRewardActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public void onPlayerLogin(String playerId) {

		if (!isOpening(playerId)){
			return;
		}
		//如果活动是开启状态，给玩家同步领取状态信息
		if(!isHidden(playerId)){
			syncRewardStatus(playerId);
		}
	}
	

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PLAYER_COME_BACK_REWARD;
	}
	
	public Result<?> onPlayerRecieveGreatGift(String playerId, int rewardId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<PlayerComeBackEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		PlayerComeBackEntity entity = optional.get();
		if(!entity.checkHasGreatReward()){
			logger.info("ComeBackRewardActivity player recieve reward, but not exist:{}", playerId);
			return Result.fail(Status.Error.ACTIVITY_CAN_NOT_TAKE_REWARD_VALUE);
		}
		PlayerComeBackRewardConfig config = HawkConfigManager.getInstance().getConfigByKey(PlayerComeBackRewardConfig.class, rewardId);
		if (config == null){
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		//给奖励
		this.getDataGeter().takeReward(playerId, config.getRewardList(), 1, Action.COME_BACK_PLAYER_GREAT_AWARD, true, RewardOrginType.ACTIVITY_REWARD);
		//持久化数据
		entity.onPlayerTakeReward(rewardId);
		//记录TLog日志
		getDataGeter().logRecieveComeBackGreatReward(playerId);
		syncRewardStatus(playerId);
		return null;
	}
	
	/****
	 * 同步活动奖励领取状态
	 * @param playerId
	 */
	private void syncRewardStatus(String playerId){
		ComeBackPlayerRewardInfo.Builder build = ComeBackPlayerRewardInfo.newBuilder();
		Optional<PlayerComeBackEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		PlayerComeBackEntity entity = optional.get();
		entity.buildRewardBuilder(build);
		pushToPlayer(playerId, HP.code.PLAYER_COME_BACK_REWARD_INFO_S_VALUE, build);
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ComeBackRewardActivity activity = new ComeBackRewardActivity(config.getActivityId(), activityEntity);
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
