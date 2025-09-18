package com.hawk.activity.type.impl.urlModelThree;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.urlModelThree.cfg.UrlModelThreeActivityKVCfg;
import com.hawk.activity.type.impl.urlReward.IURLReward;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;

public class UrlModelThreeActivity extends ActivityBase implements IURLReward<UrlModelThreeActivityKVCfg> {

    public UrlModelThreeActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.URL_MODEL_THREE_ACTIVITY;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        return new UrlModelThreeActivity(config.getActivityId(), activityEntity);
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
