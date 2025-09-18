package com.hawk.activity.type.impl.doubleGift;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.doubleGift.cfg.DoubleGiftActivityKVCfg;
import com.hawk.activity.type.impl.doubleGift.cfg.DoubleGiftActivityTimeCfg;

/**
 * 双享豪礼活动时间控制配置
 */
public class DoubleGiftTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DoubleGiftActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		DoubleGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DoubleGiftActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
