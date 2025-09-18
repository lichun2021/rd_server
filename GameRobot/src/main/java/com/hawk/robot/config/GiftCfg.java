package com.hawk.robot.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/gift.xml")
public class GiftCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 组ID {@link GiftGroup}
	 */
	private final int groupId;
	/**
	 * 第几阶段
	 */
	private final int level;
	/**
	 *是否为最高档位
	 */
	private final int isMaxLevel;

	/**
	 *售卖货币（真实售卖价格）
	 */
	private final String price;
	/**
	 * 返还奖励
	 */
	private final String crystalReward;
	/**
	 *购物礼
	 */
	private final int allianceGift;

	/**
	 *特殊位道具
	 */
	private final String awardSpecialItems;
	/**
	 * 奖励
	 */
	private final String awardItems;

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

		return true;
	}

}
