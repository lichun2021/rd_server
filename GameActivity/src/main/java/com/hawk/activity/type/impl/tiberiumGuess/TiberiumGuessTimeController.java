package com.hawk.activity.type.impl.tiberiumGuess;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.tiberiumGuess.cfg.TblyGuessActivityTimeCfg;
import com.hawk.activity.type.impl.tiberiumGuess.cfg.TblyGuessActiviytKVCfg;

public class TiberiumGuessTimeController extends ExceptCurrentTermTimeController{

	@Override
	public long getServerDelay() {
		TblyGuessActiviytKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(TblyGuessActiviytKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return TblyGuessActivityTimeCfg.class;
	}

}
