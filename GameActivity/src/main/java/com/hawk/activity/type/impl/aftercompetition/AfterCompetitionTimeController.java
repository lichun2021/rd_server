package com.hawk.activity.type.impl.aftercompetition;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.aftercompetition.cfg.AfterCompetitionConstCfg;
import com.hawk.activity.type.impl.aftercompetition.cfg.AfterCompetitionTimeCfg;

/**
 * 赛后庆典 
 * 考虑controller类型
 */
public class AfterCompetitionTimeController extends JoinCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		AfterCompetitionConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(AfterCompetitionConstCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return AfterCompetitionTimeCfg.class;
	}

}
