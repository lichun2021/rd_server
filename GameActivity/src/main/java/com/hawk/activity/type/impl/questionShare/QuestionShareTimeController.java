package com.hawk.activity.type.impl.questionShare;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.questionShare.cfg.QuestionShareActivityTimeCfg;
import com.hawk.activity.type.impl.questionShare.cfg.QuestionShareKVCfg;

public class QuestionShareTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		QuestionShareKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(QuestionShareKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return QuestionShareActivityTimeCfg.class;
	}
}
