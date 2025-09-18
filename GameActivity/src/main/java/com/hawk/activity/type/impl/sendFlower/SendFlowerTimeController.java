package com.hawk.activity.type.impl.sendFlower;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.sendFlower.cfg.SendFlowerActivityKVCfg;
import com.hawk.activity.type.impl.sendFlower.cfg.SendFlowerActivityTimeCfg;

public class SendFlowerTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SendFlowerActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		SendFlowerActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SendFlowerActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
