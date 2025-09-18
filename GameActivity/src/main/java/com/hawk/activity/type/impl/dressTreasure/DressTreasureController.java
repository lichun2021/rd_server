package com.hawk.activity.type.impl.dressTreasure;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dressTreasure.cfg.DressTreasureKVCfg;
import com.hawk.activity.type.impl.dressTreasure.cfg.DressTreasureTimeCfg;


/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class DressTreasureController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DressTreasureTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		DressTreasureKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DressTreasureKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
