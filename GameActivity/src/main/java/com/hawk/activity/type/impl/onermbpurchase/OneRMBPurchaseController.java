package com.hawk.activity.type.impl.onermbpurchase;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.onermbpurchase.cfg.OneRMBPurchaseActivityKVCfg;
import com.hawk.activity.type.impl.onermbpurchase.cfg.OneRMBPurchaseActivityTimeCfg;

/**
 * 一元购活动时间控制配置
 * 
 * @author lating
 *
 */
public class OneRMBPurchaseController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return OneRMBPurchaseActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		OneRMBPurchaseActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OneRMBPurchaseActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
