package com.hawk.game.module.homeland.cfg;

import com.google.common.collect.ImmutableList;
import com.hawk.game.item.ItemInfo;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkOSOperator;

import java.util.List;
import java.util.Optional;

@HawkConfigManager.KVResource(file = "xml/homeland_const.xml")
public class HomeLandConstKVCfg extends HawkConfigBase {
    private final int baseLimit;
    private final String mapRange;
    private final String currencyId;
    private final String cost1;
    private final String cost2;
    private final int mainBuildtype;
    private final int awardId;
    private final int shopFreeTimes;
    private final int shopFreeCd;
    private final int shopMaxTimes;
    private final int shareCdTime;
    private final int thumbsUpMaxTimes;
    private final int gacha_pool_super;
    private final int gacha_k;
    private final int gacha_m;
    private final String gacha_pool_normal;
    private final int gacha_cost_step;
    private final int gacha_cost_max;
    private final String send_currency;
    private final int collectRecruitPushCd;
    protected ItemInfo costItem;
    protected ItemInfo costTenItem;
    protected ItemInfo currencyItem;
    protected List<ItemInfo> sendCurrency;

    public HomeLandConstKVCfg() {
        this.baseLimit = 0;
        this.mapRange = "";
        this.currencyId = "30000_6000001_1";
        this.mainBuildtype = 0;
        this.cost1 = "";
        this.cost2 = "";
        this.awardId = 0;
        this.shopFreeTimes = 0;
        this.shopFreeCd = 0;
        this.shopMaxTimes = 0;
        this.shareCdTime = 0;
        this.thumbsUpMaxTimes = 0;
        this.gacha_pool_super = 0;
        this.gacha_k = 0;
        this.gacha_m = 0;
        this.gacha_pool_normal = "";
        this.gacha_cost_step = 0;
        this.gacha_cost_max = 0;
        this.send_currency = "";
        this.collectRecruitPushCd = 0;
    }

    @Override
    protected boolean assemble() {
        if (!HawkOSOperator.isEmptyString(cost1)) {
            costItem = new ItemInfo();
            costItem.init(cost1);
        }
        if (!HawkOSOperator.isEmptyString(cost2)) {
            costTenItem = new ItemInfo();
            costTenItem.init(cost2);
        }
        if (!HawkOSOperator.isEmptyString(currencyId)) {
            currencyItem = new ItemInfo();
            currencyItem.init(currencyId);
        }
        sendCurrency = ImmutableList.copyOf(ItemInfo.valueListOf(send_currency));
        return super.assemble();
    }

    @Override
    protected boolean checkValid() {
        return super.checkValid();
    }

    public ItemInfo getCostItem() {
        return costItem;
    }

    public ItemInfo getExchangeTenItem() {
        return costTenItem;
    }

    public int getBaseLimit() {
        return baseLimit;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public ItemInfo getCurrencyItem() {
        return currencyItem;
    }

    public int getMainBuildType() {
        return mainBuildtype;
    }

    public int getInitMainBuildId() {
        ConfigIterator<HomeLandBuildingCfg> build = HawkConfigManager.getInstance().getConfigIterator(HomeLandBuildingCfg.class);
        Optional<HomeLandBuildingCfg> main = build.stream().filter(v -> v.getBuildType() == this.mainBuildtype && v.getLevel() == 1).findAny();
        return main.map(HomeLandBuildingCfg::getId).orElse(100101);
    }

    public String getMapRange() {
        return mapRange;
    }

    public int getAwardId() {
        return awardId;
    }

    public int getShopFreeTimes() {
        return shopFreeTimes;
    }

    public int getShopFreeCd() {
        return shopFreeCd * 1000;
    }

    public int getShopMaxTimes() {
        return shopMaxTimes;
    }

    public int getShareCdTime() {
        return shareCdTime * 1000;
    }

    public int getThumbsUpMaxTimes() {
        return thumbsUpMaxTimes;
    }

    public int getGachaCostMax() {
        return gacha_cost_max;
    }

    public int getGachaCostStep() {
        return gacha_cost_step;
    }

    public String getGachaNormalPool() {
        return gacha_pool_normal;
    }

    public double getGachaM() {
        return gacha_m / 10000.0;
    }

    public double getGachaK() {
        return gacha_k / 10000.0;
    }

    public int getGachaSuperPool() {
        return gacha_pool_super;
    }

    public List<ItemInfo> getSendCurrency() {
        return sendCurrency;
    }

    public int getCollectRecruitPushCd() {
        return collectRecruitPushCd * 1000;
    }
}
