package com.hawk.activity.type.impl.xzq;

import org.hawk.db.HawkDBEntity;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityType;

public class XZQActivity extends ActivityBase {

	public XZQActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.XZQ_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new XZQActivity(config.getActivityId(), activityEntity);
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
	public void onPlayerMigrate(String playerId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public boolean isHidden(String playerId) {
		boolean open = this.getDataGeter().xzqOpen();
		return !open;
	}

}
