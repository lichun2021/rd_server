package com.hawk.game.config;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 赛博之战商店配置
 *
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cyborg_shop.xml")
public class CyborgShopCfg extends HawkConfigBase {
    @Id
    private final int id;
    // 商店商品道具
    private final String item;
    // 购买商品消耗
    private final String cost;
    // 限购次数(0-无限制)
    private final int numLimit;
    
    /** 专属赛季*/
    private final int limitSeason;
   
    private List<ItemInfo> shopItems;
    
    private List<ItemInfo> consumeItems;
    
    public CyborgShopCfg() {
        id = 0;
        item = "";
        cost = "";
        numLimit = 0;
        limitSeason = 0;
    }

    public int getId() {
        return id;
    }

	public String getItem() {
		return item;
	}

	public String getCost() {
		return cost;
	}

	public int getLimitSeason() {
		return limitSeason;
	}

	@Override
    protected boolean assemble() {
		shopItems = ItemInfo.valueListOf(item);
		consumeItems =  ItemInfo.valueListOf(cost);
        return true;
    }

    @Override
    protected boolean checkValid() {
        return true;
    }
    
    public int getNumLimit() {
		return numLimit;
	}

	public List<ItemInfo> getShopItems() {
    	if (shopItems == null) {
    		return Collections.emptyList();
    	}
    	
    	return shopItems.stream().map(ItemInfo::clone).collect(Collectors.toList());
    }
    
    public List<ItemInfo> getConsumeItems() {
    	if (consumeItems == null) {
    		return Collections.emptyList();
    	}
    	
    	return consumeItems.stream().map(ItemInfo::clone).collect(Collectors.toList());
    }

}
