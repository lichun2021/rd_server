package com.hawk.activity.type.impl.equipTech;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.equipTech.cfg.EquipTechActivityKVConfig;
import com.hawk.activity.type.impl.equipTech.cfg.EquipTechActivityTimeCfg;

public class EquipTechActivityTimeController extends ExceptCurrentTermTimeController{

	@Override
	public long getServerDelay() {
		EquipTechActivityKVConfig kvConfig = HawkConfigManager.getInstance().getKVInstance(EquipTechActivityKVConfig.class);
		if (kvConfig == null) {
			return 0;
		}
		return kvConfig.getServerDelay();
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EquipTechActivityTimeCfg.class;
	}

}
