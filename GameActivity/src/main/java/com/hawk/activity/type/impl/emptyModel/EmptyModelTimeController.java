package com.hawk.activity.type.impl.emptyModel;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.emptyModel.cfg.EmptyModelActivityKVCfg;
import com.hawk.activity.type.impl.emptyModel.cfg.EmptyModelActivityTimeCfg;

public class EmptyModelTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EmptyModelActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		EmptyModelActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EmptyModelActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
