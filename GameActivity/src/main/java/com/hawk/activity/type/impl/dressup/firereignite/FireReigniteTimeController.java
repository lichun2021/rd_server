package com.hawk.activity.type.impl.dressup.firereignite;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dressup.firereignite.cfg.FireReigniteActivityKVCfg;
import com.hawk.activity.type.impl.dressup.firereignite.cfg.FireReigniteActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 装扮投放系列活动三:重燃战火
 * @author hf
 */
public class FireReigniteTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return FireReigniteActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		FireReigniteActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FireReigniteActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
