package com.hawk.activity.type.impl.redPackage;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.redPackage.cfg.RedPackageKVCfg;
import com.hawk.activity.type.impl.redPackage.cfg.RedPackageTimeCfg;

public class RedPackageTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		RedPackageKVCfg config = HawkConfigManager.getInstance().getKVInstance(RedPackageKVCfg.class);
		if(config != null){
			return config.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RedPackageTimeCfg.class;
	}

}
