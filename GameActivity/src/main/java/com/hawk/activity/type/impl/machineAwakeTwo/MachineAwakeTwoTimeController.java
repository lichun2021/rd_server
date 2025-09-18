package com.hawk.activity.type.impl.machineAwakeTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.machineAwakeTwo.cfg.MachineAwakeTwoActivityKVCfg;
import com.hawk.activity.type.impl.machineAwakeTwo.cfg.MachineAwakeTwoActivityTimeCfg;

public class MachineAwakeTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MachineAwakeTwoActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		MachineAwakeTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MachineAwakeTwoActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
