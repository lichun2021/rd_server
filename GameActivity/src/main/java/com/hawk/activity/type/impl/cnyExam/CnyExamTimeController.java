package com.hawk.activity.type.impl.cnyExam;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.cnyExam.cfg.CnyExamKvCfg;
import com.hawk.activity.type.impl.cnyExam.cfg.CnyExamTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class CnyExamTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return CnyExamTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        CnyExamKvCfg cfg = HawkConfigManager.getInstance().getKVInstance(CnyExamKvCfg.class);
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
