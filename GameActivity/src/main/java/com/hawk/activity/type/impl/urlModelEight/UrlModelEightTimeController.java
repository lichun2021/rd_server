package com.hawk.activity.type.impl.urlModelEight;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModelEight.cfg.UrlModelEightActivityKVCfg;
import com.hawk.activity.type.impl.urlModelEight.cfg.UrlModelEightActivityTimeCfg;

public class UrlModelEightTimeController extends ExceptCurrentTermTimeController {

	public UrlModelEightTimeController(){
		super();
	}
	
	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModelEightActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModelEightActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelEightActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
