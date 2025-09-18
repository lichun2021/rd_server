package com.hawk.activity.type.impl.machineLab;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.machineLab.cfg.MachineLabKVCfg;
import com.hawk.activity.type.impl.machineLab.cfg.MachineLabTimeCfg;


/**
 * 机甲研究所
 * 
 * @author che
 *
 */
public class MachineLabController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MachineLabTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		MachineLabKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MachineLabKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
