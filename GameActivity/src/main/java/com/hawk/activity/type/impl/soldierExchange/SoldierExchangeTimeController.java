package com.hawk.activity.type.impl.soldierExchange;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.soldierExchange.cfg.SoldierExchangeActivityTimeCfg;
import com.hawk.activity.type.impl.soldierExchange.cfg.SoldierExchangeKVCfg;

public class SoldierExchangeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SoldierExchangeActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		SoldierExchangeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SoldierExchangeKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
