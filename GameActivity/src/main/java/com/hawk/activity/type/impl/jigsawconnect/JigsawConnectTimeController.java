package com.hawk.activity.type.impl.jigsawconnect;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.jigsawconnect.cfg.JigsawConnectActivityKVCfg;
import com.hawk.activity.type.impl.jigsawconnect.cfg.JigsawConnectActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class JigsawConnectTimeController extends ExceptCurrentTermTimeController{

	@Override
	public long getServerDelay() {
		JigsawConnectActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(JigsawConnectActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return JigsawConnectActivityTimeCfg.class;
	}

}
