package com.hawk.activity.type.impl.machineAwake;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.machineAwake.cfg.MachineAwakeActivityKVCfg;
import com.hawk.activity.type.impl.machineAwake.cfg.MachineAwakeActivityTimeCfg;

public class MachineAwakeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MachineAwakeActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		MachineAwakeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MachineAwakeActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
