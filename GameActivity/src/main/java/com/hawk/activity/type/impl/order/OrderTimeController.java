package com.hawk.activity.type.impl.order;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.order.cfg.OrderActivityKVCfg;
import com.hawk.activity.type.impl.order.cfg.OrderActivityTimeCfg;

public class OrderTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return OrderActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		OrderActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OrderActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
