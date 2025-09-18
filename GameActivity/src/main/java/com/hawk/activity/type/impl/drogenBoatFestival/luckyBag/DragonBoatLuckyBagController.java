package com.hawk.activity.type.impl.drogenBoatFestival.luckyBag;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.drogenBoatFestival.luckyBag.cfg.DragonBoatLuckyBagKVCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.luckyBag.cfg.DragonBoatLuckyBagTimeCfg;


/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class DragonBoatLuckyBagController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DragonBoatLuckyBagTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		DragonBoatLuckyBagKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatLuckyBagKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
