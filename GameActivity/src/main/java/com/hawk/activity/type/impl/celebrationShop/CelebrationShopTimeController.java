package com.hawk.activity.type.impl.celebrationShop;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.celebrationShop.cfg.CelebrationShopActivityKVCfg;
import com.hawk.activity.type.impl.celebrationShop.cfg.CelebrationShopActivityTimeCfg;

public class CelebrationShopTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return CelebrationShopActivityTimeCfg.class;
	}
	@Override
	public long getServerDelay() {
		CelebrationShopActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(CelebrationShopActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
