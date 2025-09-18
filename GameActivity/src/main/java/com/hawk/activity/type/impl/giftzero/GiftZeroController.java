package com.hawk.activity.type.impl.giftzero;

import org.hawk.config.HawkConfigBase;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.giftzero.cfg.GiftZeroActivityTimeCfg;

/**
 * 0元礼包活动时间控制配置
 * 
 * @author lating
 *
 */
public class GiftZeroController extends ServerOpenTimeController {
	
	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GiftZeroActivityTimeCfg.class;
	}

}
