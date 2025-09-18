package com.hawk.activity.type.impl.questTreasure;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.questTreasure.cfg.QuestTreasureKVCfg;
import com.hawk.activity.type.impl.questTreasure.cfg.QuestTreasureTimeCfg;

public class QuestTreasureTimeController extends ExceptCurrentTermTimeController {

	public QuestTreasureTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		QuestTreasureKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(QuestTreasureKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return QuestTreasureTimeCfg.class;
	}
}
