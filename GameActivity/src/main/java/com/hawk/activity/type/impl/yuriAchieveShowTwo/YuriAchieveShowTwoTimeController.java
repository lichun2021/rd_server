package com.hawk.activity.type.impl.yuriAchieveShowTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.yuriAchieveShowTwo.cfg.YuriAchieveShowTwoActivityKVCfg;
import com.hawk.activity.type.impl.yuriAchieveShowTwo.cfg.YuriAchieveShowTwoActivityTimeCfg;

public class YuriAchieveShowTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return YuriAchieveShowTwoActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		YuriAchieveShowTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(YuriAchieveShowTwoActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
