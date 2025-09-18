package com.hawk.activity.type.impl.joybuy;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.joybuy.cfg.JoyBuyActivityTimeCfg;
import com.hawk.activity.type.impl.joybuy.cfg.JoyBuyExchangeActivityKVCfg;

public class JoyBuyTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return JoyBuyActivityTimeCfg.class;
	}
	@Override
	public long getServerDelay() {
		JoyBuyExchangeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(JoyBuyExchangeActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
