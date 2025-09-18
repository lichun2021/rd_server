package com.hawk.activity.type.impl.homeland.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.HashMap;
import java.util.Map;


/**
 * 心愿庄园购买礼包配置
 *
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "activity/act_home382/act_home382_shop.xml")
public class HomeLandPuzzleShopCfg extends HawkConfigBase {

    /**
     *
     */
    @Id
    private final int id;
    private final int shopItemType;
    private final int times;
    private final int refreshType;
    private final String payItem;
    private final String getItem;
    private final String iosPayId;
    private final String androidPayID;
    private final int payQuota;
    private static Map<String, Integer> payGiftIdMap = new HashMap<String, Integer>();
    public HomeLandPuzzleShopCfg() {
        id = 0;
        shopItemType = 0;
        times = 0;
        refreshType = 0;
        payQuota = 0;
        payItem = "";
        getItem = "";
        iosPayId = "";
        androidPayID = "";
    }

    @Override
    protected boolean assemble() {
        try {
            payGiftIdMap.put(androidPayID, id);
            payGiftIdMap.put(iosPayId, id);
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    @Override
    protected final boolean checkValid() {
        return super.checkValid();
    }

    public int getId() {
        return id;
    }

    public int getShopItemType() {
        return shopItemType;
    }

    public int getTimes() {
        return times;
    }

    public int getRefreshType() {
        return refreshType;
    }

    public String getPayItem() {
        return payItem;
    }

    public String getGetItem() {
        return getItem;
    }

    public String getIosPayId() {
        return iosPayId;
    }

    public String getAndroidPayID() {
        return androidPayID;
    }

    public int getPayQuota() {
        return payQuota;
    }
    public static int getGiftId(String payGiftId) {
        if (!payGiftIdMap.containsKey(payGiftId)) {
            throw new RuntimeException("payGiftId not match customGiftId");
        }
        return payGiftIdMap.get(payGiftId);
    }
}
