package com.hawk.activity.type.impl.ordnanceFortress;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.ordnanceFortress.cfg.OrdnanceFortressKVCfg;
import com.hawk.activity.type.impl.ordnanceFortress.cfg.OrdnanceFortressTimeCfg;


/**
 * 时空豪礼活动时间控制配置
 * 
 * @author che
 *
 */
public class OrdnanceFortressController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return OrdnanceFortressTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		OrdnanceFortressKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OrdnanceFortressKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
