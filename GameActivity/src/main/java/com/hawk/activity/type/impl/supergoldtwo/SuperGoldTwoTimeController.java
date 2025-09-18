package com.hawk.activity.type.impl.supergoldtwo;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.supergoldtwo.cfg.SuperGoldTwoKVCfg;
import com.hawk.activity.type.impl.supergoldtwo.cfg.SuperGoldTwoTimeCfg;

public class SuperGoldTwoTimeController extends ServerOpenTimeController {
	
	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SuperGoldTwoTimeCfg.class;
	}

	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		SuperGoldTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SuperGoldTwoKVCfg.class);
		long startTime = cfg.getStartDateTime();
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		//在startTime时间后开服的才可以触发该活动
		if (startTime > serverOpenTime) {
			return Optional.empty();
		}
		return super.getTimeCfg(now);
	}
	
}
