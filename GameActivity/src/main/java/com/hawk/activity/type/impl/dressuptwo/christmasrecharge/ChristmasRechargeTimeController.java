package com.hawk.activity.type.impl.dressuptwo.christmasrecharge;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dressuptwo.christmasrecharge.cfg.ChristmasRechargeActivityKVCfg;
import com.hawk.activity.type.impl.dressuptwo.christmasrecharge.cfg.ChristmasRechargeActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class ChristmasRechargeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ChristmasRechargeActivityTimeCfg.class;
	}

	public long getServerDelay() {
		ChristmasRechargeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ChristmasRechargeActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
