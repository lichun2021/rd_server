package com.hawk.activity.type.impl.exclusiveMomory;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.exclusiveMomory.cfg.ExclusiveMemoryKVCfg;
import com.hawk.activity.type.impl.exclusiveMomory.cfg.ExclusiveMemoryTimeCfg;


/**
 * @author che
 *
 */
public class ExclusiveMemoryController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ExclusiveMemoryTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ExclusiveMemoryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ExclusiveMemoryKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
