package com.hawk.activity.type.impl.pioneergift;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.pioneergift.cfg.PioneerGiftActivityKVCfg;
import com.hawk.activity.type.impl.pioneergift.cfg.PioneerGiftActivityTimeCfg;

/**
 * 先锋豪礼活动时间控制配置
 * 
 * @author lating
 *
 */
public class PioneerGiftController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PioneerGiftActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		PioneerGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PioneerGiftActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
