package com.hawk.activity.type.impl.alliesWishing;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.alliesWishing.cfg.AllianceWishKVCfg;
import com.hawk.activity.type.impl.alliesWishing.cfg.AllianceWishTimeCfg;


/**
 * 梦君祝福
 * 
 * @author che
 *
 */
public class AllianceWishController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return AllianceWishTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
