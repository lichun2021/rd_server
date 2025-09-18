package com.hawk.activity.type.impl.order.activityEquipOrder;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.order.activityEquipOrder.cfg.OrderEquipActivityKVCfg;
import com.hawk.activity.type.impl.order.activityEquipOrder.cfg.OrderEquipActivityTimeCfg;

public class OrderEquipTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return OrderEquipActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		OrderEquipActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OrderEquipActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
