package com.hawk.activity.type.impl.virtualLaboratory;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.virtualLaboratory.cfg.VirtualLaboratoryActivityTimeCfg;
import com.hawk.activity.type.impl.virtualLaboratory.cfg.VirtualLaboratoryKVCfg;

public class VirtualLaboratoryTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return VirtualLaboratoryActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		VirtualLaboratoryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(VirtualLaboratoryKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
