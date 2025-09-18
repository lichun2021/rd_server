package com.hawk.activity.type.impl.flightplan;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.flightplan.cfg.FlightPlanActivityKVCfg;
import com.hawk.activity.type.impl.flightplan.cfg.FlightPlanActivityTimeCfg;

public class FlightPlanTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return FlightPlanActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		FlightPlanActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FlightPlanActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		
		return 0;
	}

}
