package com.hawk.activity.type.impl.accumulateRechargeTwo;

import org.hawk.config.HawkConfigBase;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.accumulateRechargeTwo.cfg.AccumulateRechargeTwoActivityTimeCfg;
/**
 * 累计充值2
 * @author Winder
 *
 */
public class AccumulateRechargeTwoTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return AccumulateRechargeTwoActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

}
