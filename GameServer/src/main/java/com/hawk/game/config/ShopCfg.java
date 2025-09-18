package com.hawk.game.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.item.ItemInfo;

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
    
    // 商品类型
    protected final int group;
    // 新上架标识，1标识新上架，0非新上架
    protected final int newShelfMark;
    // 商品上架时间
    protected final String showTime;
    // 商品下架时间
    protected final String hiddenTime;
    // 额外奖励物品
    protected final String extraItem;

	// 总的商品表信息
    static Map<Integer, ShopCfg> showItemMap = new HashMap<Integer, ShopCfg>();
    // 新上架商品类型
    static Set<Integer> newlyShopItemGroups = new HashSet<>();
    
    private List<ItemInfo> extraItemList;
    
    private long showTimeStamp;
    
	private long hiddenTimeStamp;

	public ShopCfg() {
        id = 0;
        isUse = 0;
        shopItemID = 0;
        number = 0;
        buyLV = 0;
        price = "";
        maxBuyTimes = Integer.MAX_VALUE;
        buyCount = Integer.MAX_VALUE;
        group = 0;
        newShelfMark = 0;
        showTime = "";
        hiddenTime = "";
        extraItem = "";
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
    
    public String getExtraItem() {
		return extraItem;
	}

    @Override
    protected boolean assemble() {
    	if (number == 1) {
    		showItemMap.put(shopItemID, this);
    	}
    	
    	if (newShelfMark == 1) {
    		newlyShopItemGroups.add(group);
    	}
    	
    	if (!HawkOSOperator.isEmptyString(showTime)) {
    		showTimeStamp = HawkTime.parseTime(showTime);
    	}
    	
    	if (!HawkOSOperator.isEmptyString(hiddenTime)) {
    		hiddenTimeStamp = HawkTime.parseTime(hiddenTime);
    	}
    	
    	if (HawkOSOperator.isEmptyString(extraItem)) {
    		extraItemList = Collections.emptyList();
    	} else {
    		extraItemList = ItemInfo.valueListOf(extraItem, ",");
    	}
    	
        return true;
    }

    @Override
    protected boolean checkValid() {
        ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, shopItemID);
        if (itemCfg == null) {
        	throw new RuntimeException("ItemCfg not found shopItemID = " + shopItemID);
        }
        
        if (showTimeStamp > hiddenTimeStamp) {
        	throw new RuntimeException("ItemCfg showTime or hiddenTime invalid -> " + id);
        }

        return true;
    }

    public static ShopCfg getShopCfgByItemId(int itemId) {
        return showItemMap.get(itemId);
    }

    public ItemInfo getPriceItemInfo() {
        return ItemInfo.valueOf(this.price);
    }

	public int getMaxBuyTimes() {
		return maxBuyTimes;
	}
	
	public static Set<Integer> getNewlyShopItemGroups() {
		if (newlyShopItemGroups.isEmpty()) {
			return Collections.emptySet();
		}
		
		return Collections.unmodifiableSet(newlyShopItemGroups);
	}
	
	public long getShowTimeStamp() {
		return showTimeStamp;
	}
	
    public long getHiddenTimeStamp() {
		return hiddenTimeStamp;
	}

	public List<ItemInfo> getExtraItemList() {
		if (extraItemList.isEmpty()) {
			return Collections.emptyList();
		}
		
		return extraItemList.stream().map(e -> e.clone()).collect(Collectors.toList());
	}

}
