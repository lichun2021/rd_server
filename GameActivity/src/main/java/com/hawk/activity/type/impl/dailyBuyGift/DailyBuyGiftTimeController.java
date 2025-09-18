package com.hawk.activity.type.impl.dailyBuyGift;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dailyBuyGift.config.DailyBuyGiftKVCfg;
import com.hawk.activity.type.impl.dailyBuyGift.config.DailyBuyGiftTimeCfg;

public class DailyBuyGiftTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return DailyBuyGiftTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
    	DailyBuyGiftKVCfg config = HawkConfigManager.getInstance().getKVInstance(DailyBuyGiftKVCfg.class);
		if (config != null) {
			return config.getServerDelay();
		}
		return 0;
    }
}
