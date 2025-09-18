package com.hawk.activity.type.impl.urlModelThree;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModelThree.cfg.UrlModelThreeActivityKVCfg;
import com.hawk.activity.type.impl.urlModelThree.cfg.UrlModelThreeActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import java.util.Optional;

public class UrlModelThreeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModelThreeActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModelThreeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelThreeActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
//	@Override
//	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
//		UrlModelThreeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelThreeActivityKVCfg.class);
//		long openTime = HawkTime.parseTime(cfg.getServerOpenTime());
//		long endTime = HawkTime.parseTime(cfg.getServerEndTime());
//		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
//		if (openTime <= serverOpenTime && serverOpenTime <= endTime) {
//			return super.getTimeCfg(now);
//		}
//		return Optional.empty();
//	}
}
