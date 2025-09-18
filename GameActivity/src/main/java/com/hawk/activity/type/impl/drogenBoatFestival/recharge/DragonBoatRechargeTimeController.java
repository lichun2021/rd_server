package com.hawk.activity.type.impl.drogenBoatFestival.recharge;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.drogenBoatFestival.recharge.cfg.DragonBoatRechargeKVCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.recharge.cfg.DragonBoatRechargeTimeCfg;

public class DragonBoatRechargeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DragonBoatRechargeTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		DragonBoatRechargeKVCfg cfg = HawkConfigManager.getInstance().
				getKVInstance(DragonBoatRechargeKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
