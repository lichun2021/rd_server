package com.hawk.activity.type.impl.urlModelNine;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModelNine.cfg.UrlModelNineActivityKVCfg;
import com.hawk.activity.type.impl.urlModelNine.cfg.UrlModelNineActivityTimeCfg;

public class UrlModelNineTimeController extends ExceptCurrentTermTimeController {

	public UrlModelNineTimeController(){
		super();
	}
	
	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModelNineActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModelNineActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelNineActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
