package com.hawk.activity.type.impl.deepTreasure;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.deepTreasure.cfg.DeepTreasureActivityKVCfg;
import com.hawk.activity.type.impl.deepTreasure.cfg.DeepTreasureTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class DeepTreasureTimeController extends ExceptCurrentTermTimeController{

	@Override
	public long getServerDelay() {
		DeepTreasureActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DeepTreasureActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DeepTreasureTimeCfg.class;
	}

}
