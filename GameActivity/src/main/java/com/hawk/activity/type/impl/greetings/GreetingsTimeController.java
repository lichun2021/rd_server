package com.hawk.activity.type.impl.greetings;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.greetings.cfg.GreetingsActivityKVCfg;
import com.hawk.activity.type.impl.greetings.cfg.GreetingsActivityTimerCfg;

/**
 * 祝福语活动时间控制器
 */
public class GreetingsTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		GreetingsActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GreetingsActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GreetingsActivityTimerCfg.class;
	}

}
