package com.hawk.activity.type.impl.urlModelOne;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.type.impl.urlModel379.cfg.UrlModel379KVCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModelOne.cfg.UrlModelOneActivityKVCfg;
import com.hawk.activity.type.impl.urlModelOne.cfg.UrlModelOneActivityTimeCfg;
import org.hawk.os.HawkTime;

import java.util.Optional;

public class UrlModelOneTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModelOneActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModelOneActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelOneActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
//	@Override
//	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
//		UrlModelOneActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelOneActivityKVCfg.class);
//		long openTime = HawkTime.parseTime(cfg.getServerOpenTime());
//		long endTime = HawkTime.parseTime(cfg.getServerEndTime());
//		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
//		if (openTime <= serverOpenTime && serverOpenTime <= endTime) {
//			return super.getTimeCfg(now);
//		}
//		return Optional.empty();
//	}
}
