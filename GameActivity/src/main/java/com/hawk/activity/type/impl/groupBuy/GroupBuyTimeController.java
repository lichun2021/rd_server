package com.hawk.activity.type.impl.groupBuy;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.groupBuy.cfg.GroupBuyActivityKVCfg;
import com.hawk.activity.type.impl.groupBuy.cfg.GroupBuyActivityTimerCfg;

/**
 * 祝福语活动时间控制器
 */
public class GroupBuyTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		GroupBuyActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GroupBuyActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GroupBuyActivityTimerCfg.class;
	}

}
