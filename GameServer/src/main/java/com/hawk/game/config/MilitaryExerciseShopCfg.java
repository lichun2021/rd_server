package com.hawk.game.config;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 军演商店信息配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/militaryExerciseShop.xml")
public class MilitaryExerciseShopCfg extends HawkConfigBase {
    @Id
    protected final int id;
    // 解锁时间（距离开服时间，单位秒）
    protected final int unlockTime;
    // 商店商品道具
    protected final String item;
    // 购买商品消耗
    protected final String cost;
    // 购买次数上限
    protected final int convertUpLimit;
   
    private List<ItemInfo> shopItems;
    
    private List<ItemInfo> consumeItems;
    
    public MilitaryExerciseShopCfg() {
        id = 0;
        unlockTime = 0;
        convertUpLimit = 0;
        item = "";
        cost = "";
    }

    public int getId() {
        return id;
    }
    
    public int getUnlockTime() {
		return unlockTime;
	}

	public String getItem() {
		return item;
	}

	public String getCost() {
		return cost;
	}

	public int getConvertUpLimit() {
		return convertUpLimit;
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
