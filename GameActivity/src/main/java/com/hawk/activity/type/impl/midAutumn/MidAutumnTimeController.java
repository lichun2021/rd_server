package com.hawk.activity.type.impl.midAutumn;

import com.hawk.activity.type.impl.midAutumn.cfg.MidAutumnActivityKVCfg;
import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.midAutumn.cfg.MidAutumnActivityTimeCfg;
import org.hawk.config.HawkConfigManager;

/**
 * 中秋庆典时间控制器
 * @author Winder
 *
 */
public class MidAutumnTimeController extends ExceptCurrentTermTimeController{

	@Override
	public long getServerDelay() {
		MidAutumnActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MidAutumnActivityKVCfg.class);
		if(cfg != null){
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MidAutumnActivityTimeCfg.class;
	}

}
