package com.hawk.activity.type.impl.jijiaSkin.cfg;

import com.google.common.base.Splitter;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.List;

/**
* 本文件自动生成，会被覆盖，不要手改非自动生成部分
*/
@HawkConfigManager.KVResource(file = "activity/jijia_skin_activity/jijia_skin_activity_cfg.xml")
public class JijiaSkinActivityKVCfg extends HawkConfigBase{
    private final String extReward;
    private final String treasureCost;
    private final int maxRefresh;
    private final int refreshReset;
    private final int serverDelay;
    private final String refreshCost;
    private final String multipleItem;
    private final String itemOnecePrice;
    private final String getItem;

    private List<String> treasureCostList;
    private List<String> refreshCostList;

    private Reward.RewardItem.Builder getItemList;

    public JijiaSkinActivityKVCfg(){
        serverDelay = 0;
        multipleItem = "1150068,1150069,1150070";
        treasureCost = "10000_1010_0,10000_1010_1,10000_1010_2,10000_1010_3,10000_1010_4,10000_1010_5,10000_1010_6,10000_1010_7,10000_1010_8,10000_1010_9";
        refreshReset = 36000;
        maxRefresh = 5;
        refreshCost = "10000_1010_0,10000_1010_1,10000_1010_2,10000_1010_3,10000_1010_4,10000_1010_5,10000_1010_6,10000_1010_7,10000_1010_8,10000_1010_9";
        itemOnecePrice = "10000_1000_56";
        extReward = "30000_840172_1";
        getItem = "30000_840172_1";
    }

    @Override
    protected boolean assemble() {
        try {
            treasureCostList = Splitter.on(";").omitEmptyStrings().splitToList(treasureCost);
            refreshCostList = Splitter.on(",").omitEmptyStrings().splitToList(refreshCost);
            getItemList = RewardHelper.toRewardItem(getItem);
            return super.assemble();
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
    }

    /** 是否倍数道具 */
    public boolean isMultipleItem(int itemId) {
        return multipleItem.contains(itemId + "");
    }

    /** 翻牌消耗 */
    public String getTreasureCost(int index) {
        index = Math.min(index, treasureCostList.size() - 1);
        return treasureCostList.get(index);
    }

    /** 刷新消耗 */
    public String getRefreshCost(int index) {
        index = Math.min(index, refreshCostList.size() - 1);
        return refreshCostList.get(index);
    }

    public long getServerDelay() {
        return serverDelay * 1000L;
    }

    @Override
    protected boolean checkValid() {
        return super.checkValid();
    }

    public String getMultipleItem() {
        return multipleItem;
    }

    public String getItemOnecePrice() {
        return itemOnecePrice;
    }

    public String getExtReward() {
        return extReward;
    }

    public int getRefreshReset() {
        return refreshReset;
    }

    public int getMaxRefresh() {
        return maxRefresh;
    }

    public String getTreasureCost() {
        return treasureCost;
    }

    public String getRefreshCost() {
        return refreshCost;
    }

    public String getGetItem() {
        return getItem;
    }

    public Reward.RewardItem.Builder getGetItemList() {
        return getItemList;
    }

}