package com.hawk.activity.type.impl.beauty.finals;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.beauty.finals.cfg.BeautyContestFinalKVCfg;
import com.hawk.activity.type.impl.beauty.finals.cfg.BeautyContestFinalTimeCfg;

/**
 * 选美决赛
 * 
 * @author lating
 *
 */
public class BeautyContestFinalController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return BeautyContestFinalTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		BeautyContestFinalKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BeautyContestFinalKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
