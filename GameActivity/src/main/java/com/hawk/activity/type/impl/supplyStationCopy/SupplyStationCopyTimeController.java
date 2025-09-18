package com.hawk.activity.type.impl.supplyStationCopy;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.supplyStationCopy.cfg.SupplyStationCopyTimeConfig;
import com.hawk.activity.type.impl.supplyStationCopy.cfg.SupplyStationKVCopyConfig;

public class SupplyStationCopyTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		SupplyStationKVCopyConfig cfg = HawkConfigManager.getInstance().getKVInstance(SupplyStationKVCopyConfig.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SupplyStationCopyTimeConfig.class;
	}


}
