package com.hawk.activity.type.impl.equipCraftsman.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 装备工匠抽奖配置
 * 
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/equip_craftsman/equip_craftsman_gacha.xml")
public class EquipCarftsmanGachaCfg extends HawkConfigBase {

	/** id*/
	@Id
	private final int id;

	/**
	 * 兵种类型
	 */
	private final int soldierType;
	
	/**
	 * 属性类型
	 */
	private final int attributeType;
	
	/**
	 * 属性值
	 */
	private final int attributeValue;
	
	/**
	 * 权重
	 */
	private final int craftsmanWeight;
	
	/**
	 * 对应装备属性表id
	 */
	private final int additionId;
	
	/**
	 * 传承消耗
	 */
	private final String inheritPrice;

	/**
	 * 是否是完美词条
	 */
	private final int perfect;
	
	/**
	 * 放弃词条奖励
	 */
	private final String reward;
	
	public EquipCarftsmanGachaCfg() {
		id = 0;
		soldierType = 0;
		attributeType = 0;
		attributeValue = 0;
		craftsmanWeight = 0;
		additionId = 0;
		inheritPrice = "";
		perfect = 0;
		reward = "";
	}

	public int getId() {
		return id;
	}

	public int getSoldierType() {
		return soldierType;
	}

	public int getAttributeType() {
		return attributeType;
	}

	public int getAttributeValue() {
		return attributeValue;
	}

	public int getCraftsmanWeight() {
		return craftsmanWeight;
	}

	public int getAdditionId() {
		return additionId;
	}

	public String getInheritPrice() {
		return inheritPrice;
	}

	public boolean isPerfect() {
		return perfect > 0;
	}

	public String getReward() {
		return reward;
	}
}
