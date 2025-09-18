package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import com.hawk.game.item.ItemInfo;

/**
 * VIP商城配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/vipShop.xml")
public class VipShopCfg extends HawkConfigBase implements Comparable<VipShopCfg>{
	@Id
	// 商城配置id
	protected final int id;
	// 商城物品id
	protected final String itemId;
	// vip等级
	protected final int vipLevel;
	// 商城物品打折后的价格
	protected final String price;
	// 商城物品出售折扣
	protected final float discount;
	// 购买次数上限
	protected final int num;
	// 礼包被随机到的权重
	protected final int weight;
	
	private ItemInfo costItem;
	
	private ItemInfo holdItem;//itemId转换对象
	/**
	 * 排序字段.
	 */
	private final int priority;
	
	public VipShopCfg() {
		id = 0;
		itemId = "";
		vipLevel = 0;
		price = "";
		discount = 0f;
		num = 1;
		weight = 100;
		priority = 0;
	}
	
	public int getId() {
		return id;
	}

	public String getItemId() {
		return itemId;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public String getPrice() {
		return price;
	}

	public float getDiscount() {
		return discount;
	}
	
	public ItemInfo getPriceItem() {
		return costItem.clone();
	}
	
	public ItemInfo getHoldItem() {
		return holdItem.clone();
	}

	public int getNum() {
		return num;
	}
	
	public int getWeight() {
		return weight;
	}
	
	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(price)) {
			costItem = ItemInfo.valueOf(price);
		}
		
		if (costItem == null) {
			return false;
		}
		
		if (!HawkOSOperator.isEmptyString(itemId)) {
			holdItem = ItemInfo.valueOf(itemId);
		}
		
		if (holdItem == null) {
			return false;
		}
		
		
		return true;
	}

	@Override
	protected boolean checkValid() {
		if (itemId == "" || vipLevel == 0 || HawkOSOperator.isEmptyString(price)) {
			return false;
		}
		
		return true;
	}	

	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(VipShopCfg o) {
		return priority - o.priority ;
	}
}
