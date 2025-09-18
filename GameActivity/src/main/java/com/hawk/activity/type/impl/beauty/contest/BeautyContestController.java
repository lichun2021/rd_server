package com.hawk.activity.type.impl.beauty.contest;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.beauty.contest.cfg.BeautyContestKVCfg;
import com.hawk.activity.type.impl.beauty.contest.cfg.BeautyContestTimeCfg;

/**
 * 选美初赛
 * 
 * @author lating
 *
 */
public class BeautyContestController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return BeautyContestTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		BeautyContestKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BeautyContestKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
