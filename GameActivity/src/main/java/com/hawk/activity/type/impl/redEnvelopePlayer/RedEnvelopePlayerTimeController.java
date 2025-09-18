package com.hawk.activity.type.impl.redEnvelopePlayer;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.redEnvelopePlayer.cfg.RedEnvelopePlayerKVCfg;
import com.hawk.activity.type.impl.redEnvelopePlayer.cfg.RedEnvelopePlayerTimeCfg;

public class RedEnvelopePlayerTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		RedEnvelopePlayerKVCfg config = HawkConfigManager.getInstance().getKVInstance(RedEnvelopePlayerKVCfg.class);
		if(config != null){
			return config.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RedEnvelopePlayerTimeCfg.class;
	}

}
