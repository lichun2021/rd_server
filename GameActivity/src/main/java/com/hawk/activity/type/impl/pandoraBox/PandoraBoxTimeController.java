package com.hawk.activity.type.impl.pandoraBox;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.pandoraBox.cfg.PandoraBoxConfig;
import com.hawk.activity.type.impl.pandoraBox.cfg.PandoraBoxTimeCfg;

public class PandoraBoxTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PandoraBoxTimeCfg.class;
	}
	
	@Override
	public long getServerDelay() {
		PandoraBoxConfig cfg = HawkConfigManager.getInstance().getKVInstance(PandoraBoxConfig.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
