package com.hawk.activity.type.impl.materialTransport;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.materialTransport.cfg.MaterialTransportKVCfg;
import com.hawk.activity.type.impl.materialTransport.cfg.MaterialTransportLotteryTimeCfg;

public class MaterialTransportTimeController extends ExceptCurrentTermTimeController{

	@Override
	public long getServerDelay() {
		MaterialTransportKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MaterialTransportKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MaterialTransportLotteryTimeCfg.class;
	}

}
