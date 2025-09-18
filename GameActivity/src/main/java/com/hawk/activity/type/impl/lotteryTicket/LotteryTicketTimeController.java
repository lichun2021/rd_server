package com.hawk.activity.type.impl.lotteryTicket;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.lotteryTicket.config.LotteryTicketKVCfg;
import com.hawk.activity.type.impl.lotteryTicket.config.LotteryTicketTimeCfg;

public class LotteryTicketTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return LotteryTicketTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
    	LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
		if (config != null) {
			return config.getServerDelay();
		}
		return 0;
    }
}
