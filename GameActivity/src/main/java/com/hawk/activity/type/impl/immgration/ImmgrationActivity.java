package com.hawk.activity.type.impl.immgration;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkException;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityType;

public class ImmgrationActivity extends ActivityBase {

	public ImmgrationActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.IMMGRATION;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ImmgrationActivity activity = new ImmgrationActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		return null;
	}

	@Override
	public boolean isHidden(String playerId) {
		try {
			if (!getDataGeter().immgrationBackFlowCheck(playerId)) {
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return true;
		}
		return super.isHidden(playerId);
	}
}
