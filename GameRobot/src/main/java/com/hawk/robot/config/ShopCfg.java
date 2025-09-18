package com.hawk.robot.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 商城道具售卖信息配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/shop.xml")
public class ShopCfg extends HawkConfigBase {
	 @Id
    protected final int id;
    // 是否有效1有效0无效
    protected final int isUse;
    // 道具id
    protected final int shopItemID;
    // 数量
    protected final int number;
    // 购买等级
    protected final int buyLV;
    // 价格
    protected final String price;
    // 前后端加入限购字段buyCount，由策划配置，不配置就不限购，配置了就读取num，限购几次。
    protected final int buyCount;
    protected final int maxBuyTimes;

	// 总的商品表信息
    static Map<Integer, ShopCfg> showItemMap = new HashMap<Integer, ShopCfg>();
    
    static List<Integer> shopIdList = new ArrayList<>();

    public ShopCfg() {
    	id = 0;
        isUse = 0;
        shopItemID = 0;
        number = 0;
        buyLV = 0;
        price = "";
        maxBuyTimes = Integer.MAX_VALUE;
        buyCount = Integer.MAX_VALUE;
    }

    public int getId() {
        return id;
    }

    public int getIsUse() {
        return isUse;
    }

    public int getShopItemID() {
        return shopItemID;
    }

    public int getNumber() {
        return number;
    }

    public int getBuyLV() {
        return buyLV;
    }

    public String getPrice() {
        return price;
    }

    public int getBuyCount() {
        return buyCount;
    }
    
    public int getMaxBuyTimes() {
		return maxBuyTimes;
	}

    @Override
    protected boolean assemble() {
    	shopIdList.add(id);
        showItemMap.put(shopItemID, this);
        return true;
    }

    @Override
    protected boolean checkValid() {
        ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, shopItemID);
        if (itemCfg == null) {
            return false;
        }

        return true;
    }

    public static ShopCfg getShopCfgByItemId(int itemId) {
        return showItemMap.get(itemId);
    }
    
    public static ShopCfg getShopCfgByItemList(List<Integer> itemIds) {
    	if(itemIds == null || itemIds.size() <= 0) {
    		return null;
    	}
    	
    	Optional<ShopCfg> op = itemIds.stream().filter(e -> showItemMap.get(e) != null).map(e -> showItemMap.get(e)).findAny();
    	if(op.isPresent()) {
    		return op.get();
    	}
    	
    	return null;
    }
    
    public static List<Integer> getShopIdList() {
		return shopIdList;
	}

}
