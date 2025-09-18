package com.hawk.activity.type.impl.newyearlottery;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.newyearlottery.cfg.NewyearLotteryActivityKVCfg;
import com.hawk.activity.type.impl.newyearlottery.cfg.NewyearLotteryActivityTimeCfg;

public class NewyearLotteryTimeController extends ExceptCurrentTermTimeController {

	public NewyearLotteryTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		NewyearLotteryActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(NewyearLotteryActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return NewyearLotteryActivityTimeCfg.class;
	}
}
