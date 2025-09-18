package com.hawk.activity.type.impl.redblueticket;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.redblueticket.cfg.RedBlueTicketActivityKVCfg;
import com.hawk.activity.type.impl.redblueticket.cfg.RedBlueTicketActivityTimeCfg;

/**
 * 翻牌活动时间控制配置
 * 
 * @author lating
 *
 */
public class RedBlueTicketActivityController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RedBlueTicketActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		RedBlueTicketActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RedBlueTicketActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
