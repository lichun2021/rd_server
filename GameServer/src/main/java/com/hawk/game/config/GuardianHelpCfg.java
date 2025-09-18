package com.hawk.game.config;

import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/guardian_help.xml")
public class GuardianHelpCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 物品的ID
	 */
	private final int itemId;
	/**
	 * 价格
	 */
	private final String price;
	/**
	 * 价格
	 */
	private List<ItemInfo> priceList;
	
	public GuardianHelpCfg() {
		this.id = 0;
		this.itemId = 0;
		this.price = "";
	}

	public int getId() {
		return id;
	}

	public int getItemId() {
		return itemId;
	}

	public String getPrice() {
		return price;
	}

	public List<ItemInfo> getPriceList() {
		return priceList;
	}
	
	@Override
	public boolean assemble() {
		this.priceList = Collections.synchronizedList(ItemInfo.valueListOf(price, SerializeHelper.SEMICOLON_ITEMS));
		
		return true;
	}
	
	@Override
	public boolean checkValid() {
		return ItemCfg.isExistItemId(itemId);		
	}
}
