package com.hawk.activity.type.impl.yuriAchieveTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.yuriAchieveTwo.cfg.YuriAchieveTwoActivityKVCfg;
import com.hawk.activity.type.impl.yuriAchieveTwo.cfg.YuriAchieveTwoActivityTimeCfg;

public class YuriAchieveTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return YuriAchieveTwoActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		YuriAchieveTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(YuriAchieveTwoActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
