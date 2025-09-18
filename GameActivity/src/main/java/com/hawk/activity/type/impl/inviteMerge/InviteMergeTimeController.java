package com.hawk.activity.type.impl.inviteMerge;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.ITimeController;
import com.hawk.activity.type.impl.inviteMerge.cfg.InviteMergeTimeCfg;

public class InviteMergeTimeController extends ITimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return InviteMergeTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}
}
