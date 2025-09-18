package com.hawk.activity.type.impl.yurirevenge;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.yurirevenge.cfg.YuriRevengeActivityKVCfg;
import com.hawk.activity.type.impl.yurirevenge.cfg.YuriRevengeActivityTimeCfg;

public class YuriRevengeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return YuriRevengeActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		YuriRevengeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(YuriRevengeActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
	
	/**
	 * 获取下期活动的开启时间
	 * @param now
	 * @return
	 */
	public long getNextStartTimeByTermId(int termId){
		int nextTermId = termId + 1;
		IActivityTimeCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(YuriRevengeActivityTimeCfg.class, nextTermId);
		if(nextCfg == null){
			return Long.MAX_VALUE;
		}
		return nextCfg.getStartTimeValue();
	}
}
