package com.hawk.activity.type.impl.celebrationCourse;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.celebrationCourse.cfg.CelebrationCourseActivityKVCfg;
import com.hawk.activity.type.impl.celebrationCourse.cfg.CelebrationCourseActivityTimeCfg;

public class CelebrationCourseTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return CelebrationCourseActivityTimeCfg.class;
	}
	@Override
	public long getServerDelay() {
		CelebrationCourseActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(CelebrationCourseActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
