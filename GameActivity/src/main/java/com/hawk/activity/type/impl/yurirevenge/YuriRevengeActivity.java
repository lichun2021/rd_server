package com.hawk.activity.type.impl.yurirevenge;

import org.hawk.db.HawkDBEntity;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.msg.YuriRevengeStateChangeMsg;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.ActivityState;

/**
 * 尤里复仇
 * @author admin
 */
public class YuriRevengeActivity extends ActivityBase {

	public YuriRevengeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.YURI_REVENGE_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		YuriRevengeActivity activity = new YuriRevengeActivity(config.getActivityId(), activityEntity);
		return activity;
	}


	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		return null;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		
	}

	@Override
	public void onShow() {
		HawkTaskManager.getInstance().postMsg(HawkXID.valueOf(ObjType.MANAGER, ObjType.ID_SERVER_ACTIVITY),
				YuriRevengeStateChangeMsg.valueOf(getActivityEntity().getTermId(), ActivityState.SHOW));
		super.onShow();
	}

	@Override
	public void onEnd() {
		HawkTaskManager.getInstance().postMsg(HawkXID.valueOf(ObjType.MANAGER, ObjType.ID_SERVER_ACTIVITY),
				YuriRevengeStateChangeMsg.valueOf(getActivityEntity().getTermId(), ActivityState.END));
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		return null;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean handleForMergeServer() {
		if (this.getActivityEntity().getActivityState() == com.hawk.activity.type.ActivityState.HIDDEN) {
			return true;
		}
		
		this.getDataGeter().yuriRevengeSendRewardByMergeServer(this.getActivityTermId());
		
		return true;
	}
}
