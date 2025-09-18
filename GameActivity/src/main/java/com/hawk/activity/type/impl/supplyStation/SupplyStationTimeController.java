package com.hawk.activity.type.impl.supplyStation;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.supplyStation.cfg.SupplyStationKVConfig;
import com.hawk.activity.type.impl.supplyStation.cfg.SupplyStationTimeConfig;

public class SupplyStationTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		SupplyStationKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(SupplyStationKVConfig.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SupplyStationTimeConfig.class;
	}

}
