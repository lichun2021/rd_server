package com.hawk.activity.type.impl.warzonewealcopy;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.warzonewealcopy.cfg.WarzoneWealActivityKVCopyCfg;
import com.hawk.activity.type.impl.warzonewealcopy.cfg.WarzoneWealActivityTimeCopyCfg;

public class WarzoneWealCopyTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return WarzoneWealActivityTimeCopyCfg.class;
	}

	@Override
	public long getServerDelay() {
		WarzoneWealActivityKVCopyCfg cfg = HawkConfigManager.getInstance().getKVInstance(WarzoneWealActivityKVCopyCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
