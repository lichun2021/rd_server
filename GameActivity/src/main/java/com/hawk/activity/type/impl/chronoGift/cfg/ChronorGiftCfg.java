package com.hawk.activity.type.impl.chronoGift.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 时空好礼时空之门直购礼包
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/space_gift/space_gift_level.xml")
public class ChronorGiftCfg extends HawkConfigBase {
	// 礼包ID
	@Id 
	private final int giftId;
	
	private final String androidPayId;
	
	private final String iosPayId;
	
	private final String purchaseVipExp;
	
	private final String unlockItem;
	
	private final String freeRewards;
	// 定制礼包可选奖励的数量
	private final int chooseItems;
	
	private static Map<String, Integer> payGiftIdMap = new HashMap<String, Integer>();

	public ChronorGiftCfg() {
		giftId = 0;
		androidPayId = "";
		iosPayId = "";
		purchaseVipExp = "";
		unlockItem = "";
		freeRewards = "";
		chooseItems = 0;
	}
	
	@Override
	protected boolean assemble() {
		payGiftIdMap.put(androidPayId, giftId);
		payGiftIdMap.put(iosPayId, giftId);
		return super.assemble();
	}

	public int getGiftId() {
		return giftId;
	}

	public String getAndroidPayId() {
		return androidPayId;
	}

	public String getIosPayId() {
		return iosPayId;
	}
	
	
	public String getPurchaseVipExp() {
		return purchaseVipExp;
	}

	public String getUnlockItem() {
		return unlockItem;
	}

	public String getFreeRewards() {
		return freeRewards;
	}

	public int getChooseItems() {
		return chooseItems;
	}
	
	

	public static int getGiftId(String payGiftId) {
		if (!payGiftIdMap.containsKey(payGiftId)) {
			throw new RuntimeException("payGiftId not match customGiftId");
		}
		return payGiftIdMap.get(payGiftId);
	}
}
