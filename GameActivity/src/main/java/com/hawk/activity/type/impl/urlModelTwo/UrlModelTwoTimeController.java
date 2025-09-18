package com.hawk.activity.type.impl.urlModelTwo;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModelTwo.cfg.UrlModelTwoActivityKVCfg;
import com.hawk.activity.type.impl.urlModelTwo.cfg.UrlModelTwoActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import java.util.Optional;

public class UrlModelTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModelTwoActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModelTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelTwoActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
//	@Override
//	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
//		UrlModelTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelTwoActivityKVCfg.class);
//		long openTime = HawkTime.parseTime(cfg.getServerOpenTime());
//		long endTime = HawkTime.parseTime(cfg.getServerEndTime());
//		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
//		if (openTime <= serverOpenTime && serverOpenTime <= endTime) {
//			return super.getTimeCfg(now);
//		}
//		return Optional.empty();
//	}
}
