package com.hawk.activity.type.impl.timeLimitLogin.cfg;

import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.game.protocol.Common.KeyValuePairStrStr;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/time_limit_login/time_limit_login_cfg.xml")
public class TimeLimitLoginKVCfg extends HawkConfigBase {
	/**
	 * 服务器的时间
	 */
	private final long serverDelay;
	/**
	 * 时间范围
	 */
	private final String rewardTime;
	
	
	public TimeLimitLoginKVCfg(){
		serverDelay = 0l;
		rewardTime = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getRewardTime() {
		return rewardTime;
	}
	
	//取对应开启的时间
	public KeyValuePairStrStr.Builder getRewardTimeById(int id){
		if (id < 1) {
			return null;
		}
		List<String> list = SerializeHelper.stringToList(String.class, rewardTime, SerializeHelper.BETWEEN_ITEMS);
		if (list.size() < 2) {
			return null;
		}
		String timeStr = list.get(id - 1);
		KeyValuePairStrStr.Builder kevValPair = KeyValuePairStrStr.newBuilder();
		List<String> list2 = SerializeHelper.stringToList(String.class, timeStr, "-");
		if (list2.size() < 2) {
			return null;
		}
		kevValPair.setKey(list2.get(0));
		kevValPair.setValue(list2.get(1));
		return kevValPair;
	}
}
