package com.hawk.activity.type.impl.dividegold;

import com.hawk.activity.type.impl.dividegold.cfg.DivideGoldActivityKVCfg;
import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dividegold.cfg.DivideGoldActivityTimeCfg;
import org.hawk.config.HawkConfigManager;

public class DivideGoldTimeController extends ExceptCurrentTermTimeController{

	@Override
	public long getServerDelay() {
		DivideGoldActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DivideGoldActivityKVCfg.class);
		if(cfg != null){
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DivideGoldActivityTimeCfg.class;
	}

}
