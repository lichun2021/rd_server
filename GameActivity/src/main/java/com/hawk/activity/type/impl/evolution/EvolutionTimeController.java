package com.hawk.activity.type.impl.evolution;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.evolution.cfg.EvolutionActivityKVCfg;
import com.hawk.activity.type.impl.evolution.cfg.EvolutionActivityTimeCfg;

public class EvolutionTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EvolutionActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		EvolutionActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EvolutionActivityKVCfg.class);
		long gtOpenTime = cfg.getServerOpenTimeValue(); //开服时间晚于此时间的服务器开启
		long ltOpenTime = cfg.getServerEndOpenTimeValue(); //开服时间早于此时间的服务器开启
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate(); //当前服务器开服时间
		//向策划确认过，这里是且的关系（如果开服时间点在这两者之间，就不用判断serverDelay，否则就需要判断serverDelay）
		if (gtOpenTime < serverOpenTime && serverOpenTime < ltOpenTime) {
			return 0;
		}
		
		return cfg.getServerDelay();
	}

}
