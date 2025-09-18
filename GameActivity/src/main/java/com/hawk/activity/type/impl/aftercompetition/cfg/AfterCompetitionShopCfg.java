package com.hawk.activity.type.impl.aftercompetition.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/after_competition_party/after_competition_party_shop.xml")
public class AfterCompetitionShopCfg extends HawkConfigBase {

	@Id 
	private final int id;
	
	/** 解锁分数 */
	private final int homageValue;
	
	/** 单次累计大赏所需购买数量 */
	private final int buyGoodsNeedCount;
	
	/** 赠送商品（关联award表） */
	private final int giveGoods;
	
	/** 出售商品（关联award表） */
	private final int getGoods;
	
	/** 大赏奖励（关联award表） */
	private final int redPackReward;
	
	/** 购买注水 */
	private final String shopAddValue;
	
	/** 单人可购买上限  */
	private final int buyTimesLimit;
	
	/** 单人可接受上限  */
	private final int getTimesLimit;
	
	private final String cost;
	private final String androidPayId;
	private final String iosPayId;
	
	private List<int[]> shopAddValList = new ArrayList<>();
	
	private static Map<String, Integer> payGift2CfgMap = new HashMap<>();

	public AfterCompetitionShopCfg() {
		this.id = 0;
		this.homageValue = 0;
		this.buyGoodsNeedCount = 0;
		this.giveGoods = 0;
		this.getGoods = 0;
		this.redPackReward = 0;
		this.shopAddValue = "";
		this.buyTimesLimit = 0;
		this.getTimesLimit = 0;
		this.cost = "";
		this.androidPayId = "";
		this.iosPayId = "";
	}
	
	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(androidPayId)) {
			payGift2CfgMap.put(androidPayId, id);
		}
		if (!HawkOSOperator.isEmptyString(iosPayId)) {
			payGift2CfgMap.put(iosPayId, id);
		}
		shopAddValList = SerializeHelper.str2intList(this.shopAddValue);
		return true;
	}

	public int getId() {
		return id;
	}

	public int getHomageValue() {
		return homageValue;
	}

	public int getBuyGoodsNeedCount() {
		return buyGoodsNeedCount;
	}

	public int getGiveGoods() {
		return giveGoods;
	}

	public int getGetGoods() {
		return getGoods;
	}

	public int getRedPackReward() {
		return redPackReward;
	}

	public String getShopAddValue() {
		return shopAddValue;
	}

	public int getBuyTimesLimit() {
		return buyTimesLimit;
	}

	public int getGetTimesLimit() {
		return getTimesLimit;
	}
	
	public String getCost() {
		return cost;
	}

	public String getAndroidPayId() {
		return androidPayId;
	}

	public String getIosPayId() {
		return iosPayId;
	}
	
	public boolean isPayRMB() {
		return !HawkOSOperator.isEmptyString(androidPayId) && !HawkOSOperator.isEmptyString(iosPayId);
	}

	public static int getGiftId(String payGiftId) {
		return payGift2CfgMap.getOrDefault(payGiftId, 0);
	}

	public List<int[]> getShopAddValList() {
		return shopAddValList;
	}

}
