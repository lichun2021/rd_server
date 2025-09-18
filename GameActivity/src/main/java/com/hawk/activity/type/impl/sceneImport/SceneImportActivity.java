package com.hawk.activity.type.impl.sceneImport;

import com.hawk.activity.type.impl.urlModelOne.cfg.UrlModelOneActivityKVCfg;
import com.hawk.activity.type.impl.urlReward.IURLReward;
import org.hawk.db.HawkDBEntity;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityType;

public class SceneImportActivity extends ActivityBase implements IURLReward<UrlModelOneActivityKVCfg> {

	public SceneImportActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SCENE_IMPORT;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new SceneImportActivity(config.getActivityId(), activityEntity);
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

}
