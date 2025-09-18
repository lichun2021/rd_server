package com.hawk.game.config;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;

/**
 * 物品兑换配置表
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/item_exchange.xml")
public class ItemExchangeCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 兑换所得的物品
	protected final String itemId;
	// 兑换所需要的物品
	protected final String exchangeItemId;
	// 可以兑换的次数上限（永久）
	protected final int canExchangetimes;
	
	private List<ItemInfo> toItemInfos;
	
	private List<ItemInfo> fromItemInfos;

	public ItemExchangeCfg() {
		id = 0;
		itemId = "";
		exchangeItemId = "";
		canExchangetimes = 0;
	}

	public int getId() {
		return id;
	}

	public String getItemId() {
		return itemId;
	}

	public String getExchangeItemId() {
		return exchangeItemId;
	}
	
	public int getCanExchangetimes() {
		return canExchangetimes;
	}
	
	public boolean assemble() {
		if (HawkOSOperator.isEmptyString(exchangeItemId)) {
			fromItemInfos = Collections.emptyList();
			toItemInfos = Collections.emptyList();
		} else {
			fromItemInfos = ItemInfo.valueListOf(exchangeItemId);
			if (HawkOSOperator.isEmptyString(itemId)) {
				toItemInfos = Collections.emptyList();
			} else {
				toItemInfos = ItemInfo.valueListOf(itemId);
			}
		}
		
		return true;
	}
	
	public List<ItemInfo> getExchangeFromItems() {
		return fromItemInfos.stream().map(ItemInfo::clone).collect(Collectors.toList());
	}
	
	
	public List<ItemInfo> getExchangeToItems() {
		return toItemInfos.stream().map(ItemInfo::clone).collect(Collectors.toList());
	}
}
