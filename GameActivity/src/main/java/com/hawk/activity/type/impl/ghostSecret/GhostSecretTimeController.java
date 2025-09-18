package com.hawk.activity.type.impl.ghostSecret;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.ghostSecret.cfg.GhostSecretKVCfg;
import com.hawk.activity.type.impl.ghostSecret.cfg.GhostSecretTimeCfg;

public class GhostSecretTimeController extends ExceptCurrentTermTimeController {

	public GhostSecretTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		GhostSecretKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GhostSecretKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GhostSecretTimeCfg.class;
	}
}
