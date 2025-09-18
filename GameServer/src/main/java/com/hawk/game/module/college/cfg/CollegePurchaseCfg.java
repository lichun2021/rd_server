package com.hawk.game.module.college.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 军事学院直购商品配置
 * @author lating
 */
@HawkConfigManager.XmlResource(file = "xml/college_purchase.xml")
public class CollegePurchaseCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 付费类型：0免费，1直购购买，2金条购买
	 */
	private final int shopItemType;
	/**
	 * 是否必出
	 */
	private final int fixed;
	/**
	 * 限购次数
	 */
	private final int times;
	/**
	 * 1每周刷新，2终身一次
	 */
	private final int refreshType;
	/**
	 * 学院等级
	 */
	private final int limitLevel;
	/**
	 * 是否推荐
	 */
	private final int sellTags;
	/**
	 * 刷出的权重
	 */
	private final int weight;
	private final int payQuota;
	private final String payItem;
	private final int awardID;
	/**
	 * 开服后的天数
	 */
	private final String limitDay;
	private final String iosPayId;
	private final String androidPayID;
	
	
	private final String dispenseReward;
	
	private int startDay;
	private int endDay;
	private static Map<String, Integer> payGift2CfgIdMap = new HashMap<>();
	
	public CollegePurchaseCfg() {
		id = 0;
		shopItemType = 0;
		fixed = 0;
		times = 0;
		refreshType = 0;
		limitLevel = 0;
		sellTags = 0;
		weight = 0;
		payQuota = 0;
		payItem = "";
		awardID = 0;
		limitDay = "";
		iosPayId = "";
		androidPayID = "";
		dispenseReward= "";
	}
	
	@Override
	protected boolean assemble() {
		if (HawkOSOperator.isEmptyString(limitDay)) {
			return false;
		}
		String[] splites = limitDay.split("_");
		startDay = Integer.parseInt(splites[0]);
		endDay = splites.length > 1 ? Integer.parseInt(splites[1]) : Integer.MAX_VALUE;
		if (endDay <= startDay) {
			return false;
		}
		
		if (!HawkOSOperator.isEmptyString(androidPayID) && !HawkOSOperator.isEmptyString(iosPayId)) {
			payGift2CfgIdMap.put(androidPayID, id);
			payGift2CfgIdMap.put(iosPayId, id);
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}
	
	public int getId() {
		return id;
	}

	public int getShopItemType() {
		return shopItemType;
	}

	public int getFixed() {
		return fixed;
	}

	public int getTimes() {
		return times;
	}

	public int getRefreshType() {
		return refreshType;
	}

	public int getLimitLevel() {
		return limitLevel;
	}

	public int getSellTags() {
		return sellTags;
	}

	public int getWeight() {
		return weight;
	}

	public int getPayQuota() {
		return payQuota;
	}
	
	public String getPayItem() {
		return payItem;
	}

	public int getAwardID() {
		return awardID;
	}

	public String getLimitDay() {
		return limitDay;
	}

	public String getIosPayId() {
		return iosPayId;
	}

	public String getAndroidPayID() {
		return androidPayID;
	}
	
	public int getStartDay() {
		return startDay;
	}
	
	public int getEndDay() {
		return endDay;
	}
	
	public static int getCfgIdByPayId(String giftId) {
		return payGift2CfgIdMap.getOrDefault(giftId, 0);
	}
	
	public String getDispenseReward() {
		return dispenseReward;
	}
	
}
