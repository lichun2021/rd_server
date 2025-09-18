package com.hawk.game.config;

import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;

import java.security.InvalidParameterException;

public class ShopItemBaseCfg extends HawkConfigBase {
    @Id
    private final int id;
    /** 每份消耗物品 */
    private final String needItem;
    /** 每份获得物品 */
    private final String gainItem;
    /** 最大可兑换次数 */
    private final int times;
    private final int limitSeason;

    public ShopItemBaseCfg(){
        id = 0;
        needItem = "";
        gainItem = "";
        times = 0;
        limitSeason = 0;
    }

    @Override
    protected boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(needItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("ShopItemBaseCfg reward error, id: %s , cost: %s", id, needItem));
        }
        valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("ShopItemBaseCfg reward error, id: %s , item: %s", id, gainItem));
        }
        return super.checkValid();
    }

    public int getId() {
        return id;
    }

    public String getNeedItem() {
        return needItem;
    }

    public String getGainItem() {
        return gainItem;
    }

    public int getTimes() {
        return times;
    }

    public int getLimitSeason() {
        return limitSeason;
    }
}
