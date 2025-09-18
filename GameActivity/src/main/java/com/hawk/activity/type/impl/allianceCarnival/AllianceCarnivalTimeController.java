package com.hawk.activity.type.impl.allianceCarnival;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.allianceCarnival.cfg.AllianceCarnivalCfg;
import com.hawk.activity.type.impl.allianceCarnival.cfg.AllianceCarnivalTimeCfg;

/**
 * 联盟总动员
 * @author golden
 *
 */
public class AllianceCarnivalTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		AllianceCarnivalCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceCarnivalCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return AllianceCarnivalTimeCfg.class;
	}

	
}
