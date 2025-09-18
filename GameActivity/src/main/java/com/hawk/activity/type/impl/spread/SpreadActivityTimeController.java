package com.hawk.activity.type.impl.spread;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.spread.cfg.SpreadActivityTimeCfg;
import com.hawk.activity.type.impl.spread.cfg.SpreadKVCfg;

public class SpreadActivityTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		SpreadKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpreadKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
	
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		SpreadKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpreadKVCfg.class);
		long startTime = cfg.getStartDateTime(); // 5.18
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		// 在startTime时间后开服的才可以触发该活动
		if (startTime > serverOpenTime || now > serverOpenTime + TimeUnit.DAYS.toMillis(cfg.getEndDate())) {
			return Optional.empty();
		}
		return super.getTimeCfg(now);
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SpreadActivityTimeCfg.class;
	}
}
