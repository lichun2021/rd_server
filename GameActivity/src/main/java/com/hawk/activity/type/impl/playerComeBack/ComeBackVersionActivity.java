package com.hawk.activity.type.impl.playerComeBack;

import java.util.Optional;
import org.hawk.db.HawkDBEntity;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.playerComeBack.entity.PlayerComeBackEntity;
import com.hawk.game.protocol.Activity;

/***
 * 新版本爆料(无活动内容，策划配置一个前端跳转链接)
 * @author yang.rao
 *
 */
public class ComeBackVersionActivity extends ActivityBase {

	public ComeBackVersionActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.PLAYER_COME_BACK_VERSION;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ComeBackVersionActivity activity = new ComeBackVersionActivity(config.getActivityId(), activityEntity);
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
