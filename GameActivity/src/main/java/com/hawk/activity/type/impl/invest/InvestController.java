package com.hawk.activity.type.impl.invest;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.invest.cfg.InvestActivityKVCfg;
import com.hawk.activity.type.impl.invest.cfg.InvestActivityTimeCfg;

/**
 * 投资理财活动时间控制配置
 * 
 * @author lating
 *
 */
public class InvestController extends JoinCurrentTermTimeController {
	
	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return InvestActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		InvestActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InvestActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
