package com.hawk.activity.type.impl.mergecompetition;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.mergecompetition.cfg.MergeCompetitionConstCfg;
import com.hawk.activity.type.impl.mergecompetition.cfg.MergeCompetitionTimeCfg;

public class MergeCompetitionTimeController extends ServerOpenTimeController {

	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MergeCompetitionTimeCfg.class;
	}

	public long getServerDelay() {
		MergeCompetitionConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(MergeCompetitionConstCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
