package com.hawk.activity.type.impl.urlModelTen;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModelTen.cfg.UrlModelTenActivityKVCfg;
import com.hawk.activity.type.impl.urlModelTen.cfg.UrlModelTenActivityTimeCfg;

public class UrlModelTenTimeController extends ExceptCurrentTermTimeController {

	public UrlModelTenTimeController(){
		super();
	}
	
	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModelTenActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModelTenActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelTenActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
