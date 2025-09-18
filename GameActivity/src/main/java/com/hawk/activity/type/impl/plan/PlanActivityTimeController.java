package com.hawk.activity.type.impl.plan;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.plan.cfg.PlanActivityKVCfg;
import com.hawk.activity.type.impl.plan.cfg.PlanActivityTimeCfg;

public class PlanActivityTimeController extends ExceptCurrentTermTimeController {

	public PlanActivityTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		PlanActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PlanActivityTimeCfg.class;
	}
}
