package com.hawk.activity.type.impl.militaryprepare;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.militaryprepare.cfg.MilitaryPrepareActivityKVCfg;
import com.hawk.activity.type.impl.militaryprepare.cfg.MilitaryPrepareActivityTimeCfg;

public class MilitaryPrepareTimeController extends ExceptCurrentTermTimeController{

	@Override
	public long getServerDelay() {
		MilitaryPrepareActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MilitaryPrepareActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MilitaryPrepareActivityTimeCfg.class;
	}

}
