package com.hawk.activity.type.impl.submarineWar;

import java.util.Objects;
import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarKVCfg;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarTimeCfg;

public class SubmarineWarTimeController extends ExceptCurrentTermTimeController {

	
	
	@Override
	public long getServerDelay() {
		SubmarineWarKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SubmarineWarKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SubmarineWarTimeCfg.class;
	}
	
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		Optional<IActivityTimeCfg> opTimeCfg = super.getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			return opTimeCfg;
		}
		//如果和服时间在这期活动内，则不参与
		Long serverMergeTime = ActivityManager.getInstance().getDataGeter().getServerMergeTime();
		IActivityTimeCfg timecfg = opTimeCfg.get();
		if(Objects.nonNull(serverMergeTime) && 
				timecfg.getShowTimeValue()<=serverMergeTime && serverMergeTime <= timecfg.getHiddenTimeValue()){
			return Optional.empty();
		}
		return opTimeCfg;
	}
}
