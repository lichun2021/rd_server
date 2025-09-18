package com.hawk.activity.type.impl.groupPurchase;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.groupPurchase.cfg.GroupPurchaseActivityTimeCfg;

public class GroupPurchaseTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GroupPurchaseActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}
}
