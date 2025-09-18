package com.hawk.activity.type.impl.travelshopAssist;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.travelshopAssist.cfg.TravelShopAssistKVCfg;
import com.hawk.activity.type.impl.travelshopAssist.cfg.TravelShopAssistTimeCfg;

public class TravelShopAssistTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return TravelShopAssistTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		TravelShopAssistKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(TravelShopAssistKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
