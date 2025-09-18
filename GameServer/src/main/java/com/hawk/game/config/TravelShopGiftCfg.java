package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.helper.HawkAssert;

import com.hawk.game.item.ItemInfo;

@XmlResource(file = "xml/travel_shop_gift.xml")
public class TravelShopGiftCfg extends HawkConfigBase {
	/**
	 * 配置ID
	 */
	@Id
	protected final int id;
	/**
	 * 权重
	 */
	protected final int weight;
	/**
	 * 价格
	 */
	protected final String price;
	/**
	 * 特殊奖励
	 */
	protected final String specialReward;
	/**
	 * 普通奖励
	 */
	protected final String ordinaryReward;
	/**
	 * 
	 */
	protected final String crystalReward;

	/** 联盟礼物 */
	protected final int allianceGift;
	/**
	 * 钻石还是普通
	 */
	private final int type;
	/**
	 * 是否是引导
	 */
	private final boolean isGuide;
	/**
	 * 每天的购买次数.
	 */
	private final int dailyBuyTimes;
	/**
	 * 琳琅特惠每日限购次数
	 */
	private final int linLangBuyTimes;
	
	private List<ItemInfo> specialRewardList;
	private List<ItemInfo> ordinaryRewardList;
	private List<ItemInfo> crystalRewardList;
	private List<ItemInfo> priceList;

	public TravelShopGiftCfg() {
		id = 0;
		weight = 0;
		price = "";
		specialReward = "";
		ordinaryReward = "";
		crystalReward = "";
		allianceGift = 0;
		type = 0;
		this.isGuide = false;
		this.dailyBuyTimes = 0;
		linLangBuyTimes = 0;
	}

	public int getId() {
		return id;
	}

	public int getWeight() {
		return weight;
	}

	public String getPrice() {
		return price;
	}

	public String getSpecialReward() {
		return specialReward;
	}

	public String getOrdinaryReward() {
		return ordinaryReward;
	}

	public String getCrystalReward() {
		return crystalReward;
	}

	public boolean assemble() {
		this.specialRewardList = ItemInfo.valueListOf(this.specialReward);
		this.ordinaryRewardList = ItemInfo.valueListOf(this.ordinaryReward);
		this.crystalRewardList = ItemInfo.valueListOf(this.crystalReward);
		this.priceList = ItemInfo.valueListOf(this.price);

		return true;
	}
	
	

	@Override
	protected boolean checkValid() {
		if (allianceGift > 0) {
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, allianceGift);
			HawkAssert.notNull(itemCfg, " itemcfg error cfgid = " + allianceGift);
			AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, itemCfg.getRewardId());
			HawkAssert.notNull(awardCfg, " awardcfg error cfgid = " + itemCfg.getRewardId());
		}
		return super.checkValid();
	}

	public List<ItemInfo> getSpecialRewardList() {
		return specialRewardList;
	}

	public List<ItemInfo> getOrdinaryRewardList() {
		return ordinaryRewardList;
	}

	public List<ItemInfo> getCrystalRewardList() {
		return crystalRewardList;
	}

	public List<ItemInfo> getPriceList() {
		return priceList;
	}

	public int getAllianceGift() {
		return allianceGift;
	}

	public int getType() {
		return type;
	}

	public boolean isGuide() {
		return isGuide;
	}

	public int getDailyBuyTimes() {
		return dailyBuyTimes;
	}

	public int getLinLangBuyTimes() {
		return linLangBuyTimes;
	}
}
