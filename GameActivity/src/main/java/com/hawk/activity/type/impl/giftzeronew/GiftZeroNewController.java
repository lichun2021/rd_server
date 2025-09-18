package com.hawk.activity.type.impl.giftzeronew;

import org.hawk.config.HawkConfigBase;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.giftzeronew.cfg.GiftZeroNewActivityTimeCfg;

/**
 * 新0元礼包活动时间控制配置
 * 
 * @author lating
 *
 */
public class GiftZeroNewController extends ServerOpenTimeController {
	
	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GiftZeroNewActivityTimeCfg.class;
	}

}
