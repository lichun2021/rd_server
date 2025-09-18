package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 大R复仇折扣商品信息
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/revenge_store_goods.xml")
public class RevengeStoreGoodsCfg extends HawkConfigBase {
    @Id
    protected final int id;
    // 道具id
    protected final int itemId;
    // 数量
    protected final int limitNum;
    // 死兵数量
    protected final int lossTroopsNum;
    // 折扣
    protected final int discount;
    // 价格
    protected final String price;

	public RevengeStoreGoodsCfg() {
        id = 0;
        itemId = 0;
        limitNum = 1;
        lossTroopsNum = 0;
        discount = 10000;
        price = "";
    }

    public int getId() {
        return id;
    }
    
    public int getItemId() {
		return itemId;
	}

	public int getLimitNum() {
		return limitNum;
	}

	public int getLossTroopsNum() {
		return lossTroopsNum;
	}

	public int getDiscount() {
		return discount;
	}

	public String getPrice() {
        return price;
    }

    @Override
    protected boolean assemble() {
    	
        return true;
    }

    @Override
    protected boolean checkValid() {
        ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
        if (itemCfg == null) {
        	throw new RuntimeException("ItemCfg not found itemId = " + itemId);
        }
        
        return true;
    }

}
