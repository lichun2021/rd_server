package com.hawk.activity.type.impl.customgift.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 定制礼包活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/custom_made/custom_made_gift.xml")
public class CustomGiftCfg extends HawkConfigBase {
	// 礼包ID
	@Id 
	private final int giftId;
	
	private final String androidPayId;
	
	private final String iosPayId;
	// 定制礼包可选奖励的数量
	private final int chooseItems;

	private final int moreTimeRate;
	
	private static Map<String, Integer> payGiftIdMap = new HashMap<String, Integer>();

	public CustomGiftCfg() {
		giftId = 0;
		androidPayId = "";
		iosPayId = "";
		chooseItems = 0;
		moreTimeRate = 0;
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

	public int getChooseItems() {
		return chooseItems;
	}

	public int getMoreTimeRate() {
		return moreTimeRate;
	}

	public static int getGiftId(String payGiftId) {
		if (!payGiftIdMap.containsKey(payGiftId)) {
			throw new RuntimeException("payGiftId not match customGiftId");
		}
		
		return payGiftIdMap.get(payGiftId);
	}
}
