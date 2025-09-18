package com.hawk.activity.type.impl.armiesMass;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.armiesMass.cfg.ArmiesMassKVCfg;
import com.hawk.activity.type.impl.armiesMass.cfg.ArmiesMassTimeCfg;


/**
 * 时空豪礼活动时间控制配置
 * 
 * @author che
 *
 */
public class ArmiesMassTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ArmiesMassTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ArmiesMassKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ArmiesMassKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
