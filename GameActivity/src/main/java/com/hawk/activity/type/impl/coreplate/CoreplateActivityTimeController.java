package com.hawk.activity.type.impl.coreplate;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.coreplate.cfg.CoreplateActivityKVConfig;
import com.hawk.activity.type.impl.coreplate.cfg.CoreplateActivityTimeCfg;

public class CoreplateActivityTimeController extends ServerOpenTimeController{

	@Override
	public long getServerDelay() {
		CoreplateActivityKVConfig kvConfig = HawkConfigManager.getInstance().getKVInstance(CoreplateActivityKVConfig.class);
		if (kvConfig == null) {
			return 0;
		}
		return kvConfig.getServerDelay();
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return CoreplateActivityTimeCfg.class;
	}
	
	
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		CoreplateActivityKVConfig kvConfig = HawkConfigManager.getInstance().getKVInstance(CoreplateActivityKVConfig.class);
		long startTimeLimit = kvConfig.getStartTimeLimitValue();
		long endTimeLimit = kvConfig.getEndTimeLimitValue();
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		if (serverOpenDate < startTimeLimit) {
			return Optional.empty();
		}
		if(serverOpenDate > endTimeLimit){
			return Optional.empty();
		}
		return super.getTimeCfg(now);
	}
	
	

}
