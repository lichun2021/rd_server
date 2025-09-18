package com.hawk.activity.type.impl.drogenBoatFestival.exchange;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.drogenBoatFestival.exchange.cfg.DragonBoatExchangeKVCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.exchange.cfg.DragonBoatExchangeTimeCfg;


/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class DragonBoatExchangeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DragonBoatExchangeTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		DragonBoatExchangeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatExchangeKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
