package com.hawk.activity.type.impl.globalSign;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.globalSign.cfg.GlobalSignActivityKVCfg;
import com.hawk.activity.type.impl.globalSign.cfg.GlobalSignActivityTimerCfg;

/**
 * 祝福语活动时间控制器
 */
public class GlobalSignTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		GlobalSignActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GlobalSignActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GlobalSignActivityTimerCfg.class;
	}

}
