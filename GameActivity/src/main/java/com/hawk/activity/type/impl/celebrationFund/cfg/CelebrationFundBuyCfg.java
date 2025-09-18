package com.hawk.activity.type.impl.celebrationFund.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 *庆典基金
 */
@HawkConfigManager.XmlResource(file = "activity/celebration_fund/celebration_fund_buy.xml")
public class CelebrationFundBuyCfg extends HawkConfigBase {
	/** 礼包id*/
	@Id
	private final int id;
	/**
	 * 下一档位的id
	 */
	private final int next;
	
	/** ios商品id*/
	private final String iosPayId;
	
	/** android商品id*/
	private final String androidPayID;
	
	/**礼包内包含的金条书*/
	private final String maxGoldValue;
	
	/**购买积分的单价*/
	private final String buyPrice;
	
	/**档位需要多少积分可以购买*/
	private final int needPoint;
	
	private static HashMap<String, CelebrationFundBuyCfg> configMap = new HashMap<>();
	/**
	 * 第一档位的配置
	 */
	private static CelebrationFundBuyCfg firstLevelConfig;
	
	public CelebrationFundBuyCfg() {
		id = 0;
		next = 0;
		iosPayId = "";
		androidPayID = "";
		maxGoldValue = "";
		buyPrice = "";
		needPoint = 0;
	}

	public int getId() {
		return id;
	}

	public int getNext() {
		return next;
	}

	public String getIosPayId() {
		return iosPayId;
	}

	public String getAndroidPayID() {
		return androidPayID;
	}

	public String getMaxGoldValue() {
		return maxGoldValue;
	}

	public String getBuyPrice() {
		return buyPrice;
	}
	
	public int getNeedPoint() {
		return needPoint;
	}

	@Override
	protected boolean assemble() {
		firstLevelConfig = null;
		configMap.put(iosPayId, this);
		configMap.put(androidPayID, this);
		return true;
	}
	
	public boolean checkValid() {
		if (firstLevelConfig != null) {
			return true;
		}
		
		Map<Integer, Integer> nextPreMap = new HashMap<>();
		List<Integer> allLevelId = new ArrayList<>(); 
		for (Entry<String, CelebrationFundBuyCfg> entry : configMap.entrySet()) {
			if (!allLevelId.contains(entry.getValue().getId())) {
				allLevelId.add(entry.getValue().getId());
			}
			
			if (entry.getValue().getNext() > 0) {
				nextPreMap.put(entry.getValue().getNext(), entry.getValue().getId());
			}
		}
		
		allLevelId.removeAll(nextPreMap.keySet());
		if (allLevelId.size() != 1) {
			return false;
		}
		
		firstLevelConfig = HawkConfigManager.getInstance().getConfigByKey(CelebrationFundBuyCfg.class, allLevelId.get(0));
		
		return true;
	}
	
	public static HashMap<String, CelebrationFundBuyCfg> getConfigMap() {
		return configMap;
	}
	
	public static CelebrationFundBuyCfg getConfigByGoodsId(String giftId) {
		return configMap.get(giftId);
	}
	
	public static CelebrationFundBuyCfg getFirstLevelConfig() {
		return firstLevelConfig;
	}
}
