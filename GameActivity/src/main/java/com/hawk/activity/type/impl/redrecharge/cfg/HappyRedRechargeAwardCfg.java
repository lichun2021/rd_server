package com.hawk.activity.type.impl.redrecharge.cfg;

import java.util.HashMap;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/red_recharge/red_recharge_reward.xml")
public class HappyRedRechargeAwardCfg extends HawkConfigBase {
	
	@Id
	private final int id;
	
	private final int awardId;
	
	private final String redpacketitemId;
	
	private final int integral;
	
	private final String androidPayId;
	
	private final String iosPayId;
	
	private static Map<String, Integer> payGiftIdMap = new HashMap<String, Integer>();
	
	public HappyRedRechargeAwardCfg(){
		id = 0;
		awardId = 0;
		redpacketitemId = "";
		integral = 0;
		androidPayId = "";
		iosPayId = "";
	}

	@Override
	protected boolean assemble() {
		payGiftIdMap.put(androidPayId, id);
		payGiftIdMap.put(iosPayId, id);
		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public int getAwardId() {
		return awardId;
	}
	
	public String getRedpacketitemId() {
		return redpacketitemId;
	}
	
	@Override
	protected boolean checkValid() {
		if(!ConfigChecker.getDefaultChecker().chectAwardIdValid(awardId)){
			throw new RuntimeException(String.format("配置id:%d-%d,在award.xml中不存在", id, awardId));
		}
		
		return super.checkValid();
	}

	public String getAndroidPayId() {
		return androidPayId;
	}

	public String getIosPayId() {
		return iosPayId;
	}
	
	public static int getGiftId(String payGiftId) {
		return payGiftIdMap.getOrDefault(payGiftId, 0);
	}
	
	public int getIntegral() {
		return integral;
	}
	
}
