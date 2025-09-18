package com.hawk.activity.type.impl.medalfundtwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.medalfundtwo.cfg.MedalFundTwoKVCfg;
import com.hawk.activity.type.impl.medalfundtwo.cfg.MedalFundTwoTimeCfg;

public class MedalFundTwoTimeController extends ExceptCurrentTermTimeController {

	public MedalFundTwoTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		MedalFundTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MedalFundTwoKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MedalFundTwoTimeCfg.class;
	}
}
