package com.hawk.activity.type.impl.mechacoreexplore;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.mechacoreexplore.cfg.CoreExploreConstCfg;
import com.hawk.activity.type.impl.mechacoreexplore.cfg.CoreExploreTimeCfg;

public class CoreExploreTimeController extends JoinCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		CoreExploreConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(CoreExploreConstCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return CoreExploreTimeCfg.class;
	}

}
