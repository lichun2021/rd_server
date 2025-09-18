package com.hawk.activity.type.impl.dressuptwo.gunpowderrisetwo;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dressuptwo.gunpowderrisetwo.cfg.GunpowderRiseTwoKVCfg;
import com.hawk.activity.type.impl.dressuptwo.gunpowderrisetwo.cfg.GunpowderRiseTwoTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 圣诞节系列活动三:冰雪商城活动
 * @author hf
 */
public class GunpowderRiseTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GunpowderRiseTwoTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		GunpowderRiseTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GunpowderRiseTwoKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
