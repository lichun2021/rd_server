package com.hawk.activity.type.impl.loverMeet;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.loverMeet.cfg.LoverMeetKVCfg;
import com.hawk.activity.type.impl.loverMeet.cfg.LoverMeetTimeCfg;


/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class LoverMeetController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return LoverMeetTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		LoverMeetKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoverMeetKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
