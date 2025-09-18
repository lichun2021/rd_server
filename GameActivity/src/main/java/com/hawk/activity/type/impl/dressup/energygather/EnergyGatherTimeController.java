package com.hawk.activity.type.impl.dressup.energygather;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dressup.energygather.cfg.EnergyGatherActivityKVCfg;
import com.hawk.activity.type.impl.dressup.energygather.cfg.EnergyGatherActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 装扮投放系列活动二:能量聚集
 * @author hf
 */
public class EnergyGatherTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EnergyGatherActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		EnergyGatherActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EnergyGatherActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
