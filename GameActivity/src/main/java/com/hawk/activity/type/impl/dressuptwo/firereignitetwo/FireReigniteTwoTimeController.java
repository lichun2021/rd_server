package com.hawk.activity.type.impl.dressuptwo.firereignitetwo;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dressuptwo.firereignitetwo.cfg.FireReigniteTwoActivityKVCfg;
import com.hawk.activity.type.impl.dressuptwo.firereignitetwo.cfg.FireReigniteTwoActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 圣诞节系列活动二:冬日装扮活动
 * @author hf
 */
public class FireReigniteTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return FireReigniteTwoActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		FireReigniteTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FireReigniteTwoActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
