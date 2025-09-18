package com.hawk.activity.type.impl.supplyStation.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/supply_station/supply_station_activity_cfg.xml")
public class SupplyStationKVConfig extends HawkConfigBase {

	private final int serverDelay;
	
	/** 是否跨天重置，1重置0不重置 **/
	private final int isReset;
	
	private static SupplyStationKVConfig instance;
	
	public static SupplyStationKVConfig getInstance(){
		return instance;
	}
	
	public SupplyStationKVConfig(){
		serverDelay = 0;
		isReset = 0;
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
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
			throw new RuntimeException(String.format("supply_station_activity_cfg.xml 配置serverDelay出错:%d", serverDelay));
		}
		if(isReset != 0 && isReset != 1){
			throw new RuntimeException(String.format("supply_station_activity_cfg.xml 配置isReset出错:%d", isReset));
		}
		return super.checkValid();
	}
	
	
}
