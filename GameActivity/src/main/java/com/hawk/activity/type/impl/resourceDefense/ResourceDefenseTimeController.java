package com.hawk.activity.type.impl.resourceDefense;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.resourceDefense.cfg.ResourceDefenseCfg;
import com.hawk.activity.type.impl.resourceDefense.cfg.ResourceDefenseTimeCfg;

/**
 * 资源保卫战
 * @author golden
 *
 */
public class ResourceDefenseTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		ResourceDefenseCfg cfg = HawkConfigManager.getInstance().getKVInstance(ResourceDefenseCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ResourceDefenseTimeCfg.class;
	}

}
