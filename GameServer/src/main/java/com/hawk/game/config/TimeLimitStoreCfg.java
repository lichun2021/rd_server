package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import com.hawk.game.item.ItemInfo;

/**
 * 限时商店商品信息
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/goods_timelimit_store.xml")
public class TimeLimitStoreCfg extends HawkConfigBase {
	@Id
    protected final int id;
	/**
	 *  触发类型
	 */
	protected final int triggerType;
    /**
     *  物品信息
     */
    protected final String item;
    /**
     * 购买数量上限
     */
    protected final int limitNum;
    /**
     * 折扣
     */
    protected final int discount;
    /**
     * 物品出售单价
     */
    protected final String price;
   
    /**
     * 物品信息
     */
    private ItemInfo goodsItem;
    /**
     * 价格信息
     */
    private ItemInfo priceItem;
    
	public TimeLimitStoreCfg() {
		id = 0;
		triggerType = 0;
		item = "";
		limitNum = 0;
		discount = 10000;
		price = "";
    }
	
	public int getId() {
		return id;
	}

	public int getTriggerType() {
		return triggerType;
	}

	public String getItem() {
		return item;
	}

	public int getLimitNum() {
		return limitNum;
	}

	public int getDiscount() {
		return discount;
	}

	public String getPrice() {
		return price;
	}

	@Override
    protected boolean assemble() {
		if (HawkOSOperator.isEmptyString(item) || HawkOSOperator.isEmptyString(price)) {
			return false;
		}
		
		goodsItem = ItemInfo.valueOf(item);
		priceItem = ItemInfo.valueOf(price);
		if (goodsItem.getCount() <= 0 || priceItem.getCount() <= 0) {
			return false;
		}
		
        return true;
    }

    @Override
    protected boolean checkValid() {
        return true;
    }
    
    public ItemInfo getGoodsItem() {
    	return goodsItem.clone();
    }
    
    public ItemInfo getPriceItem() {
    	return priceItem.clone();
    }
    
}
