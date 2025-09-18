package com.hawk.activity.type.impl.supplyStationTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.supplyStationTwo.cfg.SupplyStationTwoKVConfig;
import com.hawk.activity.type.impl.supplyStationTwo.cfg.SupplyStationTwoTimeConfig;

public class SupplyStationTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		SupplyStationTwoKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(SupplyStationTwoKVConfig.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SupplyStationTwoTimeConfig.class;
	}

}
