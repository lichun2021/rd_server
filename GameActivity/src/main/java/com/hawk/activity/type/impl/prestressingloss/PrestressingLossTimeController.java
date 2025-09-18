package com.hawk.activity.type.impl.prestressingloss;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.prestressingloss.cfg.PrestressingLossActivityTimeCfg;
import com.hawk.activity.type.impl.prestressingloss.cfg.PrestressingLossKVCfg;

public class PrestressingLossTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PrestressingLossActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		PrestressingLossKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PrestressingLossKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
