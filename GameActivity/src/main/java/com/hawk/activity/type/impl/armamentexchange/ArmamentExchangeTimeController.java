package com.hawk.activity.type.impl.armamentexchange;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.armamentexchange.cfg.ArmamentExchangeActivityKVCfg;
import com.hawk.activity.type.impl.armamentexchange.cfg.ArmamentExchangeActivityTimeCfg;

public class ArmamentExchangeTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ArmamentExchangeActivityTimeCfg.class;
	}
	@Override
	public long getServerDelay() {
		ArmamentExchangeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ArmamentExchangeActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
