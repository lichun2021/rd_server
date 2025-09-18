package com.hawk.activity.type.impl.celebrationFund;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.celebrationFund.cfg.CelebrationFundKVCfg;
import com.hawk.activity.type.impl.celebrationFund.cfg.CelebrationFundTimeCfg;

/**
 * 周年庆庆典基金
 * LiJialaing，FangWeijie
 */
public class CelebrationFundTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return CelebrationFundTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		CelebrationFundKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(CelebrationFundKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
