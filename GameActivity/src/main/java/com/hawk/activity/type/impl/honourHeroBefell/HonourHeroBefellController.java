package com.hawk.activity.type.impl.honourHeroBefell;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.honourHeroBefell.cfg.HonourHeroBefellKVCfg;
import com.hawk.activity.type.impl.honourHeroBefell.cfg.HonourHeroBefellTimeCfg;


/**
 * 
 * @author che
 *
 */
public class HonourHeroBefellController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HonourHeroBefellTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		HonourHeroBefellKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourHeroBefellKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
