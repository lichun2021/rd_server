package com.hawk.activity.type.impl.heroBackExchange.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/hero_exchange/hero_exchange_cfg.xml")
public class HeroBackExchangeKVConfig extends HawkConfigBase {
	
	private final int serverDelay;

	/** 是否跨天重置，1重置0不重置 **/
	private final int isReset;
	
	public HeroBackExchangeKVConfig(){
		serverDelay = 0;
		isReset = 0;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

	public int getIsReset() {
		return isReset;
	}

	public boolean isReset(){
		return isReset == 1;
	}

	@Override
	protected boolean checkValid() {
		if(serverDelay < 0){
			throw new RuntimeException(String.format("timelover_activity_cfg.xml 配置serverDelay出错:%d", serverDelay));
		}
		if(isReset != 0 && isReset != 1){
			throw new RuntimeException(String.format("timelover_activity_cfg.xml 配置isReset出错:%d", isReset));
		}
		return super.checkValid();
	}
}
