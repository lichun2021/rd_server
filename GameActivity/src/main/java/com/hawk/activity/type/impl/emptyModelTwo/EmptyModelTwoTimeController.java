package com.hawk.activity.type.impl.emptyModelTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.emptyModelTwo.cfg.EmptyModelTwoActivityKVCfg;
import com.hawk.activity.type.impl.emptyModelTwo.cfg.EmptyModelTwoActivityTimeCfg;

public class EmptyModelTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EmptyModelTwoActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		EmptyModelTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EmptyModelTwoActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
