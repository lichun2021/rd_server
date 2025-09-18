package com.hawk.activity.type.impl.luckyStar;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.luckyStar.cfg.LuckyStarActivityTimeCfg;
import com.hawk.activity.type.impl.luckyStar.cfg.LuckyStarKVCfg;

public class LuckyStartTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		LuckyStarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LuckyStarKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return LuckyStarActivityTimeCfg.class;
	}

}
