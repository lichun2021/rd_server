package com.hawk.activity.type.impl.pointSprint;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.pointSprint.cfg.PointSprintKVCfg;
import com.hawk.activity.type.impl.pointSprint.cfg.PointSprintTimeCfg;

/**
 * 巅峰荣耀
 * @author Golden
 *
 */
public class PointSprintTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		PointSprintKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PointSprintKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PointSprintTimeCfg.class;
	}

}
