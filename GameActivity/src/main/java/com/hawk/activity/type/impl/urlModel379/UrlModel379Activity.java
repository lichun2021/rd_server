package com.hawk.activity.type.impl.urlModel379;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.urlModel379.cfg.UrlModel379KVCfg;
import com.hawk.activity.type.impl.urlReward.IURLReward;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;

public class UrlModel379Activity extends ActivityBase implements IURLReward<UrlModel379KVCfg> {

    public UrlModel379Activity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.URL_MODEL_379_ACTIVITY;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        return new UrlModel379Activity(config.getActivityId(), activityEntity);
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
