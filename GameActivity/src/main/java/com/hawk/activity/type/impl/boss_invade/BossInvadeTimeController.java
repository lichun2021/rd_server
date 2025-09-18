package com.hawk.activity.type.impl.boss_invade;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.boss_invade.cfg.BossInvadeActivityTimeCfg;

public class BossInvadeTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return BossInvadeActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

}
