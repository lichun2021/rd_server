package com.hawk.activity.type.impl.dressup.drawingsearch;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dressup.drawingsearch.cfg.DrawingSearchKVCfg;
import com.hawk.activity.type.impl.dressup.drawingsearch.cfg.DrawingSearchTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 装扮投放系列活动一:搜寻图纸
 * @author hf
 */
public class DrawingSearchTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DrawingSearchTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		DrawingSearchKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DrawingSearchKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
