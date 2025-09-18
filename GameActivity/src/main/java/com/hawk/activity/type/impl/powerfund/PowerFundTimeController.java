package com.hawk.activity.type.impl.powerfund;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.RegisterTimeController;
import com.hawk.activity.type.impl.powerfund.cfg.PowerFundActivityKVCfg;
import com.hawk.activity.type.impl.powerfund.cfg.PowerFundActivityTimeCfg;

public class PowerFundTimeController extends RegisterTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PowerFundActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now, String playerId) {
		PowerFundActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PowerFundActivityKVCfg.class);
		long endTime = cfg.getEndDateTimeValue();
		long restartTime = cfg.getRestartDateTimeValue();
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		if (endTime <= serverOpenTime  && serverOpenTime <= restartTime) {
			return Optional.empty();
		}
		return super.getTimeCfg(now,playerId);
	}
}
