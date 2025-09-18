package com.hawk.activity.type.impl.seaTreasure;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.seaTreasure.cfg.SeaTreasureKVCfg;
import com.hawk.activity.type.impl.seaTreasure.cfg.SeaTreasureTimeCfg;

/**
 * 秘海珍寻
 * @author Golden
 *
 */
public class SeaTreasureTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		SeaTreasureKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SeaTreasureKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SeaTreasureTimeCfg.class;
	}
}
