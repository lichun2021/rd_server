package com.hawk.activity.type.impl.backImmigration;

import java.util.Date;
import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.backImmigration.cfg.BackImmgrationKVCfg;
import com.hawk.activity.type.impl.backImmigration.cfg.BackImmgrationTimeCfg;
import com.hawk.activity.type.impl.backSoldierExchange.cfg.BackSoldierExchangeKVCfg;
import com.hawk.game.protocol.Activity;

public class BackImmgrationActivityTimeController extends JoinCurrentTermTimeController {
    @Override
    public long getServerDelay() {
    	BackImmgrationKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BackImmgrationKVCfg.class);
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        return 0;
    }

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return BackImmgrationTimeCfg.class;
    }
    
    
    
    @Override
	public long getShowTimeByTermId(int termId, String playerId) {
    	BackImmgrationData data = this.getPlayerBackImmgrationData(playerId);
		return HawkTime.getAM0Date(new Date(data.getBackTime())).getTime();
	}

	@Override
	public long getStartTimeByTermId(int termId, String playerId) {
		return getShowTimeByTermId(termId, playerId);
	}

	@Override
	public long getEndTimeByTermId(int termId, String playerId) {
		BackImmgrationData data = this.getPlayerBackImmgrationData(playerId);
		BackSoldierExchangeKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackSoldierExchangeKVCfg.class);
		return HawkTime.getAM0Date(new Date(data.getBackTime())).getTime() +
		kvCfg.getContinueDays() * HawkTime.DAY_MILLI_SECONDS;
	}

	@Override
	public long getHiddenTimeByTermId(int termId, String playerId) {
		return getEndTimeByTermId(termId, playerId);
	}
	

	public BackImmgrationData getPlayerBackImmgrationData(String playerId){
		Optional<ActivityBase> optional = ActivityManager.getInstance().
				getActivity(Activity.ActivityType.BACK_IMMGRATION_VALUE);
		if(!optional.isPresent()){
			return null;
		}
		BackImmgrationActivity activity = (BackImmgrationActivity)optional.get();
		return activity.getPlayerBackImmgrationData(playerId);
	}

}
