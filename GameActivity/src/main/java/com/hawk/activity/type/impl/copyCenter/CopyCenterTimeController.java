package com.hawk.activity.type.impl.copyCenter;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.copyCenter.cfg.CopyCenterActivityTimeCfg;
import com.hawk.activity.type.impl.copyCenter.cfg.CopyCenterKVCfg;

public class CopyCenterTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return CopyCenterActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		CopyCenterKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(CopyCenterKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
