package com.hawk.activity.type.impl.commonExchangeTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/universal_exchange_two/universal_exchange_two_cfg.xml")
public class CommonExchangeTwoKVConfig extends HawkConfigBase {

	private final int serverDelay;
	
	/** 购买的信息 是否跨天重置，1重置0不重置 **/
	private final int packageIsReset;
	
	/** 兑换是否重置 **/
	private final int exchangeIsReset;
	
	private static CommonExchangeTwoKVConfig instance;
	
	public static CommonExchangeTwoKVConfig getInstance(){
		return instance;
	}
	
	public CommonExchangeTwoKVConfig(){
		serverDelay = 0;
		packageIsReset = 0;
		exchangeIsReset = 0;
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public boolean isPachageReset(){
		return packageIsReset == 1;
	}
	
	public int getPackageIsReset() {
		return packageIsReset;
	}

	public boolean isExchangeReset(){
		return exchangeIsReset == 1;
	}
	
	public int getExchangeIsReset() {
		return exchangeIsReset;
	}

	@Override
	protected boolean checkValid() {
		if(serverDelay < 0){
			throw new RuntimeException(String.format("universal_exchange_package_cfg.xml 配置serverDelay出错:%d", serverDelay));
		}
		if(packageIsReset != 0 && packageIsReset != 1){
			throw new RuntimeException(String.format("universal_exchange_package_cfg.xml 配置isReset出错:%d", packageIsReset));
		}
		return super.checkValid();
	}
	
	
}
