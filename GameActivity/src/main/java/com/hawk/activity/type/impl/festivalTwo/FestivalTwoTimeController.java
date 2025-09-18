package com.hawk.activity.type.impl.festivalTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.festivalTwo.cfg.FestivalTwoActivityKVCfg;
import com.hawk.activity.type.impl.festivalTwo.cfg.FestivalTwoActivityTimeCfg;

public class FestivalTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return FestivalTwoActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		FestivalTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FestivalTwoActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
