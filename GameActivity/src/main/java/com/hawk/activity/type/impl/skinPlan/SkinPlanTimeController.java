package com.hawk.activity.type.impl.skinPlan;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.skinPlan.cfg.SkinPlanActivityKVCfg;
import com.hawk.activity.type.impl.skinPlan.cfg.SkinPlanActivityTimerCfg;

/**
 * 皮肤计划活动时间控制器
 * @author Winder
 *
 */
public class SkinPlanTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		SkinPlanActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SkinPlanActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SkinPlanActivityTimerCfg.class;
	}

}
