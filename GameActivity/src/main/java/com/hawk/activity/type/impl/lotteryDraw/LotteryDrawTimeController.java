package com.hawk.activity.type.impl.lotteryDraw;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.lotteryDraw.cfg.LotteryDrawActivityKVCfg;
import com.hawk.activity.type.impl.lotteryDraw.cfg.LotteryDrawActivityTimeCfg;

public class LotteryDrawTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return LotteryDrawActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		LotteryDrawActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LotteryDrawActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
