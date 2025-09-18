package com.hawk.activity.type.impl.luckyBox;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.luckyBox.cfg.LuckyBoxKVCfg;
import com.hawk.activity.type.impl.luckyBox.cfg.LuckyBoxTimeCfg;


/**
 * 幸运转盘
 * 活动开启时间控制，服务器开服后活动延迟开启的时间
 * @author che
 *
 */
public class LuckBoxController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return LuckyBoxTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		LuckyBoxKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LuckyBoxKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
