package com.hawk.activity.type.impl.dressup.gunpowderrise;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dressup.gunpowderrise.cfg.GunpowderRiseKVCfg;
import com.hawk.activity.type.impl.dressup.gunpowderrise.cfg.GunpowderRiseTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
/**
 * 装扮投放系列活动四:浴火重生(硝烟再起)
 * @author hf
 */
public class GunpowderRiseTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GunpowderRiseTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		GunpowderRiseKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GunpowderRiseKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
