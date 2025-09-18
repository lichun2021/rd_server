package com.hawk.activity.type.impl.celebrationFood.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;


/**
 * 庆典美食
 */
@HawkConfigManager.KVResource(file = "activity/celebration_share_cake/celebration_share_cake_cfg.xml")
public class CelebrationFoodKVCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;
	private final String iosPayId;
	private final String androidPayId;
	
	private List<Integer> iosPayIdList;
	private List<Integer> androidPayIdList;
	
	public CelebrationFoodKVCfg() {
		serverDelay = 0;
		iosPayId = "";
		androidPayId = "";
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public List<Integer> getIosPayId() {
		return iosPayIdList;
	}

	public List<Integer> getAndroidPayId() {
		return androidPayIdList;
	}
	
	public boolean assemble() {
		if (HawkOSOperator.isEmptyString(iosPayId) 
				|| HawkOSOperator.isEmptyString(androidPayId)) {
			return false;
		}
		
		String[] iosGiftIds = iosPayId.split(",");
		iosPayIdList = new ArrayList<Integer>(iosGiftIds.length);
		for (int i = 0; i < iosGiftIds.length; i++) {
			iosPayIdList.add(Integer.parseInt(iosGiftIds[i]));
		}
		
		String[] androidGiftIds = androidPayId.split(",");
		androidPayIdList = new ArrayList<Integer>(androidGiftIds.length);
		for (int i = 0; i < androidGiftIds.length; i++) {
			androidPayIdList.add(Integer.parseInt(androidGiftIds[i]));
		}
		return true;
	}
}
