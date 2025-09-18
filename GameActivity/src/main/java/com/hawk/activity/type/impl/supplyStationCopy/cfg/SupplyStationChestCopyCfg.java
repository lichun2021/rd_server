package com.hawk.activity.type.impl.supplyStationCopy.cfg;

import java.util.HashMap;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/buy_gifts/buy_gifts.xml")
public class SupplyStationChestCopyCfg extends HawkConfigBase {
	
	@Id
	private final int id;
	
	private final int awardId;
	
	private final String androidPayId;
	
	private final String iosPayId;
	
	private static Map<String, Integer> payGiftIdMap = new HashMap<String, Integer>();
	
	public SupplyStationChestCopyCfg(){
		id = 0;
		awardId = 0;
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

	@Override
	protected boolean checkValid() {
		if(!ConfigChecker.getDefaultChecker().chectAwardIdValid(awardId)){
			throw new RuntimeException(String.format("配置id:%d,在award.xml中不存在", id));
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
		if (!payGiftIdMap.containsKey(payGiftId)) {
			return 0;
		}
		
		return payGiftIdMap.get(payGiftId);
	}
}
