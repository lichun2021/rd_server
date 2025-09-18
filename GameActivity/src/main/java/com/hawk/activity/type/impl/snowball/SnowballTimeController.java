package com.hawk.activity.type.impl.snowball;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.snowball.cfg.SnowballCfg;
import com.hawk.activity.type.impl.snowball.cfg.SnowballTimeCfg;

/**
 * 雪球大战
 * @author golden
 *
 */
public class SnowballTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		SnowballCfg cfg = HawkConfigManager.getInstance().getKVInstance(SnowballCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SnowballTimeCfg.class;
	}

	
}