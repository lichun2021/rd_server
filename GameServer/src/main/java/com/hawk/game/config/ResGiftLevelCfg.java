package com.hawk.game.config;

import java.util.List;
import com.hawk.game.item.ItemInfo;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;

@HawkConfigManager.XmlResource(file = "xml/res_gift_level.xml")
public class ResGiftLevelCfg extends HawkConfigBase {
	/**
	 *礼包id
	 */
	
	@Id
	private final int id;

	/**
	 *所属资源类型
	 */
	private final int resType;

	/**
	 *礼包档位
	 */
	private final int level;

	/**
	 *是否最高档位
	 */
	private final int isMaxLevel;

	/**
	 *价格
	 */
	private final String price;

	/**
	 *返还黄金
	 */
	private final String crystalReward;

	/**
	 *返还特殊道具
	 */
	private final String specialReward;

	/**
	 *返还普通道具
	 */
	private final String ordinaryReward;

	/**
	 *联盟礼物
	 */
	private final int allianceGift;




	private List<ItemInfo> priceList;

	private List<ItemInfo> crystalRewardList;

	private List<ItemInfo> specialRewardList;

	private List<ItemInfo> ordinaryRewardList;

	public ResGiftLevelCfg() {
		this.id = 0;
		this.resType = 0;
		this.level = 0;
		this.isMaxLevel = 0;
		this.price = "";
		this.crystalReward = "";
		this.specialReward = "";
		this.ordinaryReward = "";
		this.allianceGift = 0;
	}

	public int getId() {
		return id;
	}

	public int getResType() {
		return resType;
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

	public String getSpecialReward() {
		return specialReward;
	}

	public String getOrdinaryReward() {
		return ordinaryReward;
	}

	public int getAllianceGift() {
		return allianceGift;
	}



	@Override
	public boolean assemble() {
		this.priceList = ItemInfo.valueListOf(this.price);
		this.crystalRewardList = ItemInfo.valueListOf(this.crystalReward);
		this.specialRewardList = ItemInfo.valueListOf(this.specialReward);
		this.ordinaryRewardList = ItemInfo.valueListOf(this.ordinaryReward);
		return true;
	}
	
	@Override
	public boolean checkValid() {
		if (allianceGift > 0) {
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, allianceGift);
			HawkAssert.notNull(itemCfg, " itemcfg error cfgid = " + allianceGift);
			AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, itemCfg.getRewardId());
			HawkAssert.notNull(awardCfg, " awardcfg error cfgid = " + itemCfg.getRewardId());
		}
		
		return true;
	}

	public List<ItemInfo> getPriceList() {
		return priceList;
	}

	public List<ItemInfo> getCrystalRewardList() {
		return crystalRewardList;
	}

	public List<ItemInfo> getSpecialRewardList() {
		return specialRewardList;
	}

	public List<ItemInfo> getOrdinaryRewardList() {
		return ordinaryRewardList;
	}

}