package com.hawk.activity.type.impl.mergeAnnounce;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.mergeAnnounce.cfg.MergeAnnounceTimeCfg;


/**
 * 可拆分和服通告
 * 
 * @author che
 *
 */
public class MergeAnnounceController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MergeAnnounceTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

}
