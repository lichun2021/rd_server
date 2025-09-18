package com.hawk.activity.type.impl.heroWish;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.heroWish.cfg.HeroWishKVCfg;
import com.hawk.activity.type.impl.heroWish.cfg.HeroWishTimeCfg;


/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class HeroWishController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HeroWishTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		HeroWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HeroWishKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
