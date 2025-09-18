package com.hawk.activity.type.impl.presentrebate;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.presentrebate.cfg.PresentRebateActivityKVCfg;
import com.hawk.activity.type.impl.presentrebate.cfg.PresentRebateActivityTimeCfg;

public class PresentRebateTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PresentRebateActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		PresentRebateActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PresentRebateActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
