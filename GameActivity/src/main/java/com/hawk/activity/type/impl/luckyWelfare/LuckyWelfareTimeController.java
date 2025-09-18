package com.hawk.activity.type.impl.luckyWelfare;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.luckyWelfare.cfg.LuckyWelfareActivityKVCfg;
import com.hawk.activity.type.impl.luckyWelfare.cfg.LuckyWelfareActivityTimeCfg;

public class LuckyWelfareTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return LuckyWelfareActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		LuckyWelfareActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LuckyWelfareActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
