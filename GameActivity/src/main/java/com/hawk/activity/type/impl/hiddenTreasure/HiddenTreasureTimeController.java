package com.hawk.activity.type.impl.hiddenTreasure;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.hiddenTreasure.cfg.HiddenTreasureActivityKVCfg;
import com.hawk.activity.type.impl.hiddenTreasure.cfg.HiddenTreasureTimeCfg;

public class HiddenTreasureTimeController extends ExceptCurrentTermTimeController{

	@Override
	public long getServerDelay() {
		HiddenTreasureActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HiddenTreasureActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HiddenTreasureTimeCfg.class;
	}

}
