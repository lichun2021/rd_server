package com.hawk.activity.type.impl.overlordBlessing;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.overlordBlessing.cfg.OverlordBlessingKVCfg;
import com.hawk.activity.type.impl.overlordBlessing.cfg.OverlordBlessingTimeCfg;

public class OverlordBlessingTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return OverlordBlessingTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		OverlordBlessingKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OverlordBlessingKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
	
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		boolean openServer = ActivityManager.getInstance().getDataGeter().isOverlordBlessingOpenServer();
		if (openServer) {
			return super.getTimeCfg(now);
		}
		return Optional.empty();
	}

}
