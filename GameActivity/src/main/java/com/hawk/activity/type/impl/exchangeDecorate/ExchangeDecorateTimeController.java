package com.hawk.activity.type.impl.exchangeDecorate;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.exchangeDecorate.cfg.ExchangeDecorateActivityKVCfg;
import com.hawk.activity.type.impl.exchangeDecorate.cfg.ExchangeDecorateActivityTimeCfg;

public class ExchangeDecorateTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ExchangeDecorateActivityTimeCfg.class;
	}
	@Override
	public long getServerDelay() {
		ExchangeDecorateActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ExchangeDecorateActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
