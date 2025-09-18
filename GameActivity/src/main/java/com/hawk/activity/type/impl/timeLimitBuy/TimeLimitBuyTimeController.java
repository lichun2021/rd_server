package com.hawk.activity.type.impl.timeLimitBuy;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.timeLimitBuy.cfg.TimeLimitBuyKVCfg;
import com.hawk.activity.type.impl.timeLimitBuy.cfg.TimeLimitBuyTimeCfg;

/**
 * 限时抢购
 * @author Golden
 *
 */
public class TimeLimitBuyTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		TimeLimitBuyKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(TimeLimitBuyKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return TimeLimitBuyTimeCfg.class;
	}

}
