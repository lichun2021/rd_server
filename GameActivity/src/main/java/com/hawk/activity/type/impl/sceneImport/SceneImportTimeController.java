package com.hawk.activity.type.impl.sceneImport;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.sceneImport.cfg.SceneImportKVCfg;
import com.hawk.activity.type.impl.sceneImport.cfg.SceneImportTimeCfg;

public class SceneImportTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SceneImportTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		SceneImportKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SceneImportKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
