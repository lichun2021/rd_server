package com.hawk.activity.type.impl.plantSoldierFactory.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/plant_soldier_factory/plant_soldier_factory_shop.xml")
public class PlantSoldierFactoryShopCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final String getItem;
    private final int isRefresh;
    private final int shopItemType;
    private final int times;
    private final String payItem;
    private final String androidPayID;
    private final String iosPayId;
    private final int payQuota;

    private static String freeAward;
    private static Map<String, PlantSoldierFactoryShopCfg> buyItemGoodsMap = new HashMap<>();

    public PlantSoldierFactoryShopCfg(){
        this.id = 0;
        this.getItem = "";
        this.isRefresh = 0;
        this.shopItemType = 0;
        this.times = 0;
        this.payItem = "";
        this.androidPayID = "";
        this.iosPayId = "";
        this.payQuota = 0;
    }

    public boolean assemble() {
        if (shopItemType == 0) {
            freeAward = getItem;
        }
        buyItemGoodsMap.put(androidPayID, this);
        buyItemGoodsMap.put(iosPayId, this);
        return true;
    }

    @Override
    protected final boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(getItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("PlantSoldierFactoryShopCfg reward error, id: %s , reward: %s", id, getItem));
        }
        return super.checkValid();
    }

    public int getId(){
        return this.id;
    }

    public String getGetItem(){
        return this.getItem;
    }

    public int getIsRefresh(){
        return this.isRefresh;
    }

    public int getShopItemType(){
        return this.shopItemType;
    }

    public int getTimes(){
        return this.times;
    }

    public String getPayItem(){
        return this.payItem;
    }

    public String getAndroidPayID(){
        return this.androidPayID;
    }

    public String getIosPayId(){
        return this.iosPayId;
    }

    public int getPayQuota(){
        return this.payQuota;
    }

    public static PlantSoldierFactoryShopCfg getConfig(String goodsId) {
        return buyItemGoodsMap.get(goodsId);
    }

    public static String getFreeAward() {
        return freeAward;
    }
}