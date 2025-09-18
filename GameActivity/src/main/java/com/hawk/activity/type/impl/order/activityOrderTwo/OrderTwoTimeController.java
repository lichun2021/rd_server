package com.hawk.activity.type.impl.order.activityOrderTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.order.activityOrderTwo.cfg.OrderTwoActivityKVCfg;
import com.hawk.activity.type.impl.order.activityOrderTwo.cfg.OrderTwoActivityTimeCfg;

public class OrderTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return OrderTwoActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		OrderTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OrderTwoActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
