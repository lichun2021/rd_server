package com.hawk.activity.type.impl.plantFortress;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.plantFortress.cfg.PlantFortressKVCfg;
import com.hawk.activity.type.impl.plantFortress.cfg.PlantFortressTimeCfg;


/**
 * 时空豪礼活动时间控制配置
 * 
 * @author che
 *
 */
public class PlantFortressController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PlantFortressTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		PlantFortressKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantFortressKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
