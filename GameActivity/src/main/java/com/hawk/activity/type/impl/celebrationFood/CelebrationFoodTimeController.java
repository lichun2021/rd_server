package com.hawk.activity.type.impl.celebrationFood;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.celebrationFood.cfg.CelebrationFoodKVCfg;
import com.hawk.activity.type.impl.celebrationFood.cfg.CelebrationFoodTimeCfg;

/**
 * 庆典美食
 * hf
 */
public class CelebrationFoodTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return CelebrationFoodTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		CelebrationFoodKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(CelebrationFoodKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
