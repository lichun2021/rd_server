package com.hawk.activity.type.impl.blackTech;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.blackTech.cfg.BlackTechKVCfg;
import com.hawk.activity.type.impl.blackTech.cfg.BlackTechTimeCfg;

public class BlackTechActivityTimeController extends ExceptCurrentTermTimeController {

	public BlackTechActivityTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		BlackTechKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BlackTechKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return BlackTechTimeCfg.class;
	}
}
