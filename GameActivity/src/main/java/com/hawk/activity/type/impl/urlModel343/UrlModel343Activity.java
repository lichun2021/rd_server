package com.hawk.activity.type.impl.urlModel343;

import com.hawk.activity.type.impl.urlReward.IURLReward;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.urlModel343.cfg.UrlModel343KVCfg;

public class UrlModel343Activity extends ActivityBase implements IURLReward<UrlModel343KVCfg> {

	public UrlModel343Activity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.URL_343;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new UrlModel343Activity(config.getActivityId(), activityEntity);
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
		long serverOpenTime = getDataGeter().getServerOpenTime(playerId);
		UrlModel343KVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModel343KVCfg.class);
		if (serverOpenTime < HawkTime.parseTime(cfg.getServerOpenTime())) {
			return true;
		}
		return super.isHidden(playerId);
	}
}
