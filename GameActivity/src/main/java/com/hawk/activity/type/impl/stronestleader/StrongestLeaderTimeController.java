package com.hawk.activity.type.impl.stronestleader;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.stronestleader.cfg.ActivityCircularKVCfg;
import com.hawk.activity.type.impl.stronestleader.cfg.StrongestLeaderActivityTimeCfg;

public class StrongestLeaderTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return StrongestLeaderActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ActivityCircularKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ActivityCircularKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
