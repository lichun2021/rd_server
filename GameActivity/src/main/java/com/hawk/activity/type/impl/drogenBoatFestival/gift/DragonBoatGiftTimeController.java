package com.hawk.activity.type.impl.drogenBoatFestival.gift;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.cfg.DragonBoatGiftKVCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.cfg.DragonBoatGiftTimeCfg;


/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class DragonBoatGiftTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DragonBoatGiftTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		DragonBoatGiftKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatGiftKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
