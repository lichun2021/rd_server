package com.hawk.activity.type.impl.hotBloodWar;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.hotBloodWar.cfg.HotBloodWarKVCfg;
import com.hawk.activity.type.impl.hotBloodWar.cfg.HotBloodWarTimeCfg;


/**
 * 
 * @author che
 *
 */
public class HotBloodWarTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HotBloodWarTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		HotBloodWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HotBloodWarKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
	
	
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		//如果开服时间不在规定时间内，则不参与
		HotBloodWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HotBloodWarKVCfg.class);
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		if(serverOpenTime < cfg.getStartTimeValue() || serverOpenTime > cfg.getEndTimeValue()){
			return Optional.empty();
		}
		return super.getTimeCfg(now);
	}

}
