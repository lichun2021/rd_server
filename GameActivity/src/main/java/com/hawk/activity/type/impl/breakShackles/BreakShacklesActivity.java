package com.hawk.activity.type.impl.breakShackles;

import org.hawk.db.HawkDBEntity;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityType;

/**
 * 冲破枷锁
 * @author che
 *
 */
public class BreakShacklesActivity extends ActivityBase {
	
	public BreakShacklesActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	
	public int getChooseTalentType(String playerId){
		return 0;
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.BREAK_SHACKLES_ACTIVITY;
	}
	
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		BreakShacklesActivity activity = new BreakShacklesActivity(config.getActivityId(), activityEntity);
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
	public void syncActivityDataInfo(String playerId) {
	}
	
	

	
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

}
