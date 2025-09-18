package com.hawk.activity.type.impl.rechargeWelfare;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.rechargeWelfare.cfg.RechargeWelfareActivityKVCfg;
import com.hawk.activity.type.impl.rechargeWelfare.cfg.RechargeWelfareActivityTimerCfg;

/**
 * 充值福利活动时间控制器
 *
 */
public class RechargeWelfareTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		RechargeWelfareActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RechargeWelfareActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RechargeWelfareActivityTimerCfg.class;
	}

}
