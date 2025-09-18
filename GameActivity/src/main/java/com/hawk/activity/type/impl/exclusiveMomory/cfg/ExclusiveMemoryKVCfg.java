package com.hawk.activity.type.impl.exclusiveMomory.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * @author che
 */
@HawkConfigManager.KVResource(file = "activity/exclusive_memory/exclusive_memory_cfg.xml")
public class  ExclusiveMemoryKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;
	
	
	
	
	public ExclusiveMemoryKVCfg(){
		serverDelay =0;
	}
	
	
	@Override
	protected boolean assemble() {
		return super.assemble();
	}
	
	
	
	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}


	public long getServerDelay() {
		return serverDelay * 1000L;
	}
	
	
		
}