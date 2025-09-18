package com.hawk.activity.type.impl.plantweapon;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.plantweapon.cfg.PlantWeaponKVCfg;
import com.hawk.activity.type.impl.plantweapon.cfg.PlantWeaponTimeCfg;

public class PlantWeaponTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		PlantWeaponKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantWeaponKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PlantWeaponTimeCfg.class;
	}

}
