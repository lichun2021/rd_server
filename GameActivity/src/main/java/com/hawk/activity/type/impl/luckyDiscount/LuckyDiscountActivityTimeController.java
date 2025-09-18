package com.hawk.activity.type.impl.luckyDiscount;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.luckyDiscount.cfg.LuckyDiscountKVCfg;
import com.hawk.activity.type.impl.luckyDiscount.cfg.LuckyDiscountTimeCfg;

public class LuckyDiscountActivityTimeController extends ExceptCurrentTermTimeController {

	public LuckyDiscountActivityTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		LuckyDiscountKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LuckyDiscountKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return LuckyDiscountTimeCfg.class;
	}
}
