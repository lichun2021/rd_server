package com.hawk.activity.type.impl.urlModelSeven;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModelSeven.cfg.UrlModelSevenActivityKVCfg;
import com.hawk.activity.type.impl.urlModelSeven.cfg.UrlModelSevenActivityTimeCfg;

public class UrlModelSevenTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModelSevenActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModelSevenActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelSevenActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
	
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		UrlModelSevenActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelSevenActivityKVCfg.class);
		long svrOpenLimitTime = cfg.getServerOpenLimitTime();
		long svrOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		if (svrOpenTime > svrOpenLimitTime) {
			return super.getTimeCfg(now);
		}
		return Optional.empty();
	}

}
