package com.hawk.activity.type.impl.jigsawconnect326.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 * 双十一拼图活动配置
 * @author hf
 *
 */
@HawkConfigManager.KVResource(file = "activity/jx_jigsaw_connect/jx_jigsaw_connect_activity_cfg.xml")
public class JXJigsawConnectActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	
	/**
	 * 开服时间晚于此时间的服务器开启
	 */
	private final String serverOpenTime;

	/**
	 * 开服时间早于此时间的服务器开启
	 */
	private final String serverEndOpenTime;
	
	
	
	private long serverOpenTimeValue;
	private long serverEndOpenTimeValue;
	
	
	public JXJigsawConnectActivityKVCfg() {
		serverDelay = 0;
		serverOpenTime = "";
		serverEndOpenTime = "";
	}
	
	@Override
	protected boolean assemble() {
		if(!HawkOSOperator.isEmptyString(serverOpenTime)){
			this.serverOpenTimeValue = HawkTime.parseTime(serverOpenTime);
		}
		if(!HawkOSOperator.isEmptyString(serverEndOpenTime)){
			this.serverEndOpenTimeValue = HawkTime.parseTime(serverEndOpenTime);
		}
		return super.assemble();
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
	public long getServerEndOpenTimeValue() {
		return serverEndOpenTimeValue;
	}
	
	public long getServerOpenTimeValue() {
		return serverOpenTimeValue;
	}
	
}
