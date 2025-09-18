package com.hawk.activity.type.impl.redkoi;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.redkoi.cfg.RedkoiActivityKVCfg;
import com.hawk.activity.type.impl.redkoi.cfg.RedkoiActivityTimeCfg;

public class RedkoiTimeController extends JoinCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		RedkoiActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RedkoiActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
				
				
				
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RedkoiActivityTimeCfg.class;
	}

	
}
