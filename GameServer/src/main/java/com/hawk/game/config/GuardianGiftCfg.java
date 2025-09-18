package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/guardian_gift.xml")
public class GuardianGiftCfg extends HawkConfigBase {
	@Id
	private final int giftId;
	/**
	 * 得到的物品
	 */
	private final String item;
	/**
	 * 价格
	 */
	private final String price;
	/**
	 * 每天的限制
	 */
	private final int dailyLimit;
	/**
	 * 增加的守护值
	 */
	private final int guardianValue;
	/**
	 * 得到的物品
	 */
	private List<ItemInfo> itemList;
	/**
	 * 价格
	 */
	private List<ItemInfo> priceList;
	
	public GuardianGiftCfg() {
		this.giftId = 0;
		this.item = "";
		this.price = "";
		this.dailyLimit = 0;
		this.guardianValue = 0;
	}
	
	public int getGiftId() {
		return giftId;
	}
	public String getItem() {
		return item;
	}
	public String getPrice() {
		return price;
	}
	public int getDailyLimit() {
		return dailyLimit;
	}
	public int getGuardianValue() {
		return guardianValue;
	}

	public List<ItemInfo> getItemList() {
		return itemList;
	}

	public List<ItemInfo> getPriceList() {
		return priceList;
	}
	
	public boolean assemble() {
		this.priceList = ItemInfo.valueListOf(price, SerializeHelper.SEMICOLON_ITEMS);
		this.itemList = ItemInfo.valueListOf(item, SerializeHelper.SEMICOLON_ITEMS);
		
		return true;
	}
	
	/**
	 * 是否是免费的.
	 * @return
	 */
	public boolean isFree() {
		return priceList.isEmpty();
	}
}
