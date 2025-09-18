package com.hawk.activity.type.impl.diffInfoSave.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/save_gold/save_buy.xml")
public class DiffInfoSaveBuyCfg extends HawkConfigBase {
    @Id
    private final int id;
    //奖励金币数量
    private final int service;
    //权重
    private final int buyType;

    /**
     * 安卓礼包id
     */
    private final int androidPayID;
    /**
     * ios礼包id
     */
    private final int iosPayId;

    private final int type;

    private final int initialGoldValue;

    private final int maxGoldValue;

    private final int incremental;

    public DiffInfoSaveBuyCfg(){
        id = 0;
        service = 0;
        buyType = 0;
        androidPayID = 0;
        iosPayId = 0;
        type = 0;
        initialGoldValue = 0;
        maxGoldValue = 0;
        incremental = 0;
    }

    public int getId() {
        return id;
    }

    public boolean isService(){
        return service == 1;
    }

    public int getAndroidPayId() {
        return androidPayID;
    }

    public int getIosPayId() {
        return iosPayId;
    }

    public int getType() {
        return type;
    }

    public int getInitialGoldValue() {
        return initialGoldValue;
    }

    public int getMaxGoldValue() {
        return maxGoldValue;
    }

    public int getIncremental() {
        return incremental;
    }
}
