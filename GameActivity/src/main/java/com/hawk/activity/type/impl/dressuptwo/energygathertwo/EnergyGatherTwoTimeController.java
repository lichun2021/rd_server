package com.hawk.activity.type.impl.dressuptwo.energygathertwo;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dressuptwo.energygathertwo.cfg.EnergyGatherTwoActivityKVCfg;
import com.hawk.activity.type.impl.dressuptwo.energygathertwo.cfg.EnergyGatherTwoActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 圣诞节系列活动一:冰雪计划活动
 * @author hf
 */
public class EnergyGatherTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EnergyGatherTwoActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		EnergyGatherTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EnergyGatherTwoActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
