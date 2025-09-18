package com.hawk.activity.type.impl.newyearTreasure;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.newyearTreasure.cfg.NewyearTreasureActivityKVCfg;
import com.hawk.activity.type.impl.newyearTreasure.cfg.NewyearTreasureActivityTimeCfg;

public class NewyearTreasureTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return NewyearTreasureActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		NewyearTreasureActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(NewyearTreasureActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
