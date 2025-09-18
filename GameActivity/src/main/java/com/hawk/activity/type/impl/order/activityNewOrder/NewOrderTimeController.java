package com.hawk.activity.type.impl.order.activityNewOrder;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.order.activityNewOrder.cfg.NewOrderActivityKVCfg;
import com.hawk.activity.type.impl.order.activityNewOrder.cfg.NewOrderActivityTimeCfg;

public class NewOrderTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return NewOrderActivityTimeCfg.class;
	}

	
	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		NewOrderActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(NewOrderActivityKVCfg.class);
		long startTime = cfg.getStartDateValue();
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		if (serverOpenTime < startTime) {
			return Optional.empty();
		}
		
		NewOrderActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(NewOrderActivityKVCfg.class);
		if (serverOpenTime >= kvCfg.getCloseTimeBeginValue() && serverOpenTime < kvCfg.getCloseTimeEndValue()) {
			return Optional.empty();
		}
		
		return super.getTimeCfg(now);
	}
}
