package com.hawk.activity.type.impl.rechargeQixi;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.rechargeQixi.cfg.RechargeQixiActivityKVCfg;
import com.hawk.activity.type.impl.rechargeQixi.cfg.RechargeQixiActivityTimeCfg;

/**
 * 七夕充值活动时间控制器
 */
public class RechargeQixiTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		RechargeQixiActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RechargeQixiActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RechargeQixiActivityTimeCfg.class;
	}

}
