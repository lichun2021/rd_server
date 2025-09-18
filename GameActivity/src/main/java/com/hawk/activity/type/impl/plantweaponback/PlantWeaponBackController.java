package com.hawk.activity.type.impl.plantweaponback;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.plantweaponback.cfg.PlantWeaponBackKVCfg;
import com.hawk.activity.type.impl.plantweaponback.cfg.PlantWeaponBackTimeCfg;

public class PlantWeaponBackController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		PlantWeaponBackKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantWeaponBackKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PlantWeaponBackTimeCfg.class;
	}

}
