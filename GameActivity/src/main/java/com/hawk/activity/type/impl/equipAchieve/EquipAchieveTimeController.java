package com.hawk.activity.type.impl.equipAchieve;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.equipAchieve.cfg.EquipAchieveActivityTimeCfg;

public class EquipAchieveTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EquipAchieveActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

}
