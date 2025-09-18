package com.hawk.activity.type.impl.rewardOrder;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.rewardOrder.cfg.RewardOrderActivityCfg;
import com.hawk.activity.type.impl.rewardOrder.cfg.RewardOrderTimeCfg;

public class RewardOrderTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RewardOrderTimeCfg.class;
	}
	
	@Override
	public long getServerDelay() {
		RewardOrderActivityCfg cfg = HawkConfigManager.getInstance().getKVInstance(RewardOrderActivityCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
