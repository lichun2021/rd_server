package com.hawk.activity.type.impl.redrecharge;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.redrecharge.cfg.HappyRedRechargeKVConfig;
import com.hawk.activity.type.impl.redrecharge.cfg.HappyRedRechargeTimeConfig;

public class HappyRedRechargeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		HappyRedRechargeKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(HappyRedRechargeKVConfig.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HappyRedRechargeTimeConfig.class;
	}


}
