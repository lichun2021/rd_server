package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 国家商店表（国家仓库中）
 * @author lating
 * @since 2022年4月14日
 */
@HawkConfigManager.XmlResource(file = "xml/nation_warehouse_shop.xml")
public class NationWarehouseShopCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	/** 兑换所得物品 */
	protected final String itemId;
	/** 军衔等级  */
	protected final int militaryLevel;
	/** 兑换消耗 */
	protected final String exchangeItem;
	/** 每周可兑换的次数  */
	protected final int num;
	
	protected final int priority;
	
	public NationWarehouseShopCfg() {
		this.id = 0;
		this.itemId = "";
		this.militaryLevel = 0;
		this.exchangeItem = "";
		this.num = 0;
		this.priority = 0;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	public String getItemId() {
		return itemId;
	}

	public int getMilitaryLevel() {
		return militaryLevel;
	}

	public String getExchangeItem() {
		return exchangeItem;
	}

	public int getNum() {
		return num;
	}

	public int getPriority() {
		return priority;
	}
	
}
