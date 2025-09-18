package com.hawk.activity.type.impl.submarineWar.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


@HawkConfigManager.XmlResource(file = "activity/submarine_war/submarine_war_pass_authority.xml")
public class SubmarineWarPassAuthorityCfg extends HawkConfigBase {
	// 礼包ID
	@Id 
	private final int id;
	
	private final String androidPayId;
	
	private final String iosPayId;
	
	private final int exp;
	
	private static Map<String, Integer> payGiftIdMap = new HashMap<String, Integer>();
	
	public SubmarineWarPassAuthorityCfg() {
		id = 0;
		androidPayId = "";
		iosPayId = "";
		exp = 0;
	}
	
	@Override
	protected boolean assemble() {
		payGiftIdMap.put(androidPayId, id);
		payGiftIdMap.put(iosPayId, id);
		return true;
	}


	
	public static Map<String, Integer> getPayGiftIdMap() {
		return payGiftIdMap;
	}


	public int getId() {
		return id;
	}

	

	public String getAndroidPayId() {
		return androidPayId;
	}

	public String getIosPayId() {
		return iosPayId;
	}
	
	public int getExp() {
		return exp;
	}
	

	public static int getGiftId(String payGiftId) {
		if (!payGiftIdMap.containsKey(payGiftId)) {
			return 0;
		}
		return payGiftIdMap.get(payGiftId);
	}

}
