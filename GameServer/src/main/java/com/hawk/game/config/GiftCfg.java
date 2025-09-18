package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;
import com.hawk.game.item.ItemInfo;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;

@HawkConfigManager.XmlResource(file = "xml/gift.xml")
public class GiftCfg extends HawkConfigBase {
	/**
	 * 礼包档位id
	 */
	@Id
	private final int id;

	/**
	 * 所属组合id
	 */
	private final int groupId;

	/**
	 * 档位等级
	 */
	private final int level;

	/**
	 * 是否为最高档位
	 */
	private final int isMaxLevel;

	/**
	 * 售卖货币（真实售卖价格）
	 */
	private final String price;

	/**
	 * 购买返还水晶
	 */
	private final String crystalReward;

	/**
	 * 购物礼
	 */
	private final int allianceGift;

	/**
	 * 特殊位道具
	 */
	private final String awardSpecialItems;

	/**
	 * 普通位道具
	 */
	private final String awardItems;
	/**
	 * iosPayId
	 */
	private final String iosPayId;
	/**
	 * 
	 */
	private final String androidPayId;
	
	private List<ItemInfo> priceList;

	private List<ItemInfo> crystalRewardList;

	private List<ItemInfo> awardSpecialItemsList;

	private List<ItemInfo> awardItemsList;
	
	public GiftCfg() {
		this.id = 0;
		this.groupId = 0;
		this.level = 0;
		this.isMaxLevel = 0;
		this.price = "";
		this.crystalReward = "";
		this.allianceGift = 0;
		this.awardSpecialItems = "";
		this.awardItems = "";
		this.iosPayId = "";
		this.androidPayId = "";
	}

	public int getId() {
		return id;
	}

	public int getGroupId() {
		return groupId;
	}

	public int getLevel() {
		return level;
	}

	public int getIsMaxLevel() {
		return isMaxLevel;
	}

	public String getPrice() {
		return price;
	}

	public String getCrystalReward() {
		return crystalReward;
	}

	public int getAllianceGift() {
		return allianceGift;
	}

	public String getAwardSpecialItems() {
		return awardSpecialItems;
	}

	public String getAwardItems() {
		return awardItems;
	}

	@Override
	public boolean assemble() {
		this.priceList = ItemInfo.valueListOf(this.price);
		this.crystalRewardList = ItemInfo.valueListOf(this.crystalReward);
		this.awardSpecialItemsList = ItemInfo.valueListOf(this.awardSpecialItems);
		this.awardItemsList = ItemInfo.valueListOf(this.awardItems);
		
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

	public List<ItemInfo> getPriceList() {
		return priceList;
	}
	public List<ItemInfo> getCopyPriceList() {
		List<ItemInfo> list = new ArrayList<>();
		for (ItemInfo itemInfo : priceList) {
			list.add(itemInfo.clone());
		}
		return list;
	}

	public List<ItemInfo> getCrystalRewardList() {
		return crystalRewardList;
	}

	public List<ItemInfo> getAwardSpecialItemsList() {
		return awardSpecialItemsList;
	}

	public List<ItemInfo> getAwardItemsList() {
		return awardItemsList;
	}

	public String getIosPayId() {
		return iosPayId;
	}

	public String getAndroidPayId() {
		return androidPayId;
	}
	
	//根据类型获取代金券条件数量
	public long getPriceByType(int type){
		for (ItemInfo itemInfo : this.priceList) {
			if (itemInfo.getItemId() == type ) {
				return itemInfo.getCount();
			}
		}
		return 0;
	}
	
}