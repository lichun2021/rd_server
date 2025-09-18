package com.hawk.activity.type.impl.dailysign.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;


@HawkConfigManager.KVResource(file = "activity/daily_sign/daily_sign_cfg.xml")
public class DailySignKVCfg extends HawkConfigBase {

	/** 是否跨天重置，1重置0不重置 **/
	private final int isReset;
	
	private final int serverDelay;
	
	private final String firstOpenDate;
	
	private long firstOpenAM0MilSec;
	
	public DailySignKVCfg(){
		isReset = 0;
		serverDelay = 0;
		firstOpenDate = "2019_05_09";
		firstOpenAM0MilSec = 0;
	}

	public int getIsReset() {
		return isReset;
	}

	public boolean isReset(){
		return isReset == 1;
	}
	
	
	@Override
	protected boolean assemble() {
		firstOpenAM0MilSec = HawkTime.parseTime(firstOpenDate, "yyyy_MM_dd");
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		if(isReset != 0 && isReset != 1){
			throw new RuntimeException(String.format("daily_sign_cfg.xml 配置isReset出错:%d", isReset));
		}
		return super.checkValid();
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public String getFirstOpenDate() {
		return firstOpenDate;
	}

	public long getFirstOpenAM0MilSec() {
		return firstOpenAM0MilSec;
	}

}
