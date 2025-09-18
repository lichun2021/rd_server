package com.hawk.activity.type.impl.alliancecelebrate;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.alliancecelebrate.cfg.AllianceCelebrateKVCfg;
import com.hawk.activity.type.impl.alliancecelebrate.cfg.AllianceCelebrateTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 时空豪礼活动时间控制配置
 * 
 * @author che
 *
 */
public class AllianceCelebrateTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return AllianceCelebrateTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		AllianceCelebrateKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceCelebrateKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
