package com.hawk.activity.type.impl.airdrop;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.airdrop.cfg.AirdropSupplyActivityKVCfg;
import com.hawk.activity.type.impl.airdrop.cfg.AirdropSupplyActivityTimerCfg;

/**
 * 空投补给活动时间控制器
 *
 */
public class AirdropSupplyTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		AirdropSupplyActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AirdropSupplyActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return AirdropSupplyActivityTimerCfg.class;
	}

}
