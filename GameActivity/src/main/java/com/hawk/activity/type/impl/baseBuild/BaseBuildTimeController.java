package com.hawk.activity.type.impl.baseBuild;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.baseBuild.cfg.BaseBuildActivityTimeCfg;
import com.hawk.activity.type.impl.baseBuild.cfg.BaseBuildKVCfg;

public class BaseBuildTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return BaseBuildActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}
	
	
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		BaseBuildKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BaseBuildKVCfg.class);
		long endTime = cfg.getEndDateTimeValue();
		long restartTime = cfg.getRestartDateTimeValue();
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		if (endTime <= serverOpenTime  && serverOpenTime <= restartTime) {
			return Optional.empty();
		}
		return super.getTimeCfg(now);
	}

}
