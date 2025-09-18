package com.hawk.activity.type.impl.shopSkip;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.shopSkip.cfg.ShopSkipActivityKVCfg;
import com.hawk.activity.type.impl.shopSkip.cfg.ShopSkipActivityTimeCfg;

public class ShopSkipTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		ShopSkipActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShopSkipActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ShopSkipActivityTimeCfg.class;
	}

}
