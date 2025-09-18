package com.hawk.activity.type.impl.medalAction;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.medalAction.cfg.MedalActionActivityKVCfg;
import com.hawk.activity.type.impl.medalAction.cfg.MedalActionLotteryTimeCfg;

public class MedalActionTimeController extends ExceptCurrentTermTimeController{

	@Override
	public long getServerDelay() {
		MedalActionActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MedalActionActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MedalActionLotteryTimeCfg.class;
	}

}
