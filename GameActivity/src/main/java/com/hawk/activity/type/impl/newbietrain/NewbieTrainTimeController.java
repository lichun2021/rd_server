package com.hawk.activity.type.impl.newbietrain;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import java.util.Optional;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.newbietrain.cfg.NewbieTrainKVCfg;
import com.hawk.activity.type.impl.newbietrain.cfg.NewbieTrainTimeCfg;

/**
 * 新兵作训活动时间控制
 */
public class NewbieTrainTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return NewbieTrainTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		NewbieTrainKVCfg config = HawkConfigManager.getInstance().getKVInstance(NewbieTrainKVCfg.class);
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		if (serverOpenTime < config.getServerOpenTimeValue()) {
			return Optional.empty();
		}
			
		return super.getTimeCfg(now);
	}
	
}
