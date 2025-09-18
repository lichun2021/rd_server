package com.hawk.activity.type.impl.bestprize;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.bestprize.cfg.BestPrizeKVCfg;
import com.hawk.activity.type.impl.bestprize.cfg.BestPrizeTimeCfg;

/**
 * 新春头奖专柜活动
 * @author lating
 *
 */
public class BestPrizeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		BestPrizeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BestPrizeKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return BestPrizeTimeCfg.class;
	}

}
