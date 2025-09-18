package com.hawk.activity.type.impl.fristRechagerThree;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.fristRechagerThree.cfg.FirstRechargeThreeKVCfg;
import com.hawk.activity.type.impl.fristRechagerThree.cfg.FirstRechargeThreeTimeCfg;

public class FirstRechargeThreeTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return FirstRechargeThreeTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
    	FirstRechargeThreeKVCfg config = HawkConfigManager.getInstance().getKVInstance(FirstRechargeThreeKVCfg.class);
		if (config != null) {
			return config.getServerDelay();
		}
		return 0;
    }
    
    
    @Override
    protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
    	long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
    	FirstRechargeThreeKVCfg config = HawkConfigManager.getInstance().getKVInstance(FirstRechargeThreeKVCfg.class);
		if(config.getServerOpenTimeValue()<=serverOpenTime && serverOpenTime <= config.getServerEndOpenTimeValue()){
			Optional<IActivityTimeCfg> tcfgop = super.getTimeCfg(now);
			return tcfgop;
		}
		return Optional.empty();
    }
}
