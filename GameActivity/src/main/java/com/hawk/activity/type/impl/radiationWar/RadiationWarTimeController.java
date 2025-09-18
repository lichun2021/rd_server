package com.hawk.activity.type.impl.radiationWar;
import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.radiationWar.cfg.RadiationWarActivityKVCfg;
import com.hawk.activity.type.impl.radiationWar.cfg.RadiationWarActivityTimeCfg;

public class RadiationWarTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RadiationWarActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		RadiationWarActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RadiationWarActivityKVCfg.class);
		long startTime = cfg.getStartDateTime(); //5.18
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		//在startTime时间后开服的才可以触发该活动
		if (startTime > serverOpenTime) {
			return Optional.empty();
		}
		return super.getTimeCfg(now);
	}
	
	

}
