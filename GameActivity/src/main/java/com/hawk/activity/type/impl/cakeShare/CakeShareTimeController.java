package com.hawk.activity.type.impl.cakeShare;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.cakeShare.cfg.CakeShareKVCfg;
import com.hawk.activity.type.impl.cakeShare.cfg.CakeShareTimeCfg;

/**
 * 蛋糕同享
 * hf
 */
public class CakeShareTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return CakeShareTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		CakeShareKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(CakeShareKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
