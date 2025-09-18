package com.hawk.activity.type.impl.equipCraftsman.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 装备工匠
 * 
 * @author Golden
 *
 */
@HawkConfigManager.KVResource(file = "activity/equip_craftsman/equip_craftsman_cfg.xml")
public class EquipCarftsmanKVCfg extends HawkConfigBase {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	/**
	 * 抽奖基础消耗
	 */
	private final String costItem;
	
	/**
	 * 单个道具价格
	 */
	private final String itemPrice;
	
	/**
	 * 购买1次获得固定奖励
	 */
	private final String extReward;
	
	/**
	 * 锁定兵种类型消耗倍率加成
	 */
	private final int soldierRate;
	
	/**
	 * 锁定属性类型消耗倍率加成
	 */
	private final int attrRate;
	
	/**
	 * 单期活动限定传承次数
	 */
	private final int inheritNum;
	
	/**
	 * 单例
	 */
	private static EquipCarftsmanKVCfg instance = null;

	public static EquipCarftsmanKVCfg getInstance() {
		return instance;
	}
	
	public EquipCarftsmanKVCfg() {
		instance = this;
		serverDelay = 0;
		costItem = "10000_1000_10";
		soldierRate = 12;
		attrRate = 3;
		inheritNum = 4;
		itemPrice = "";
		extReward = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getCostItem() {
		return costItem;
	}

	public int getSoldierRate() {
		return soldierRate;
	}

	public int getAttrRate() {
		return attrRate;
	}

	public int getInheritNum() {
		return inheritNum;
	}

	public String getItemPrice() {
		return itemPrice;
	}

	public String getExtReward() {
		return extReward;
	}
}
