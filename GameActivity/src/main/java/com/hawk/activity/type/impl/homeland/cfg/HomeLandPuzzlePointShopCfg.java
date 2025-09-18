package com.hawk.activity.type.impl.homeland.cfg;

import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;


/**
 * 商城兑换礼包配置
 *
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "activity/act_home382/act_home382_pointsShop.xml")
public class HomeLandPuzzlePointShopCfg extends AExchangeTipConfig {

    /**
     *
     */
    @Id
    private final int id;
    private final int times;
    private final int refreshType;
    private final String needItem;
    private final String gainItem;

    public HomeLandPuzzlePointShopCfg() {
        id = 0;
        times = 0;
        refreshType = 0;
        needItem = "";
        gainItem = "";
    }

    @Override
    protected boolean assemble() {
        try {

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


    public int getTimes() {
        return times;
    }

    public int getRefreshType() {
        return refreshType;
    }

    public String getNeedItem() {
        return needItem;
    }

    public String getGainItem() {
        return gainItem;
    }
}
