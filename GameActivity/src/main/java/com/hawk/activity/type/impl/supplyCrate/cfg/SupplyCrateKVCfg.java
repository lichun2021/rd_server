package com.hawk.activity.type.impl.supplyCrate.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
* 本文件自动生成，会被覆盖，不要手改非自动生成部分
*/
@HawkConfigManager.KVResource(file = "activity/supply_crate/supply_crate_cfg.xml")
public class SupplyCrateKVCfg extends HawkConfigBase{
    private final String useItem;
    private final String buyGold;
    private final int award;
    private final String targetItem;
    private final int serverDelay;
    private final String finalMultiple;
    private final int boxMax;
    private final int vitTarget;
    private final int rankMax;
    private final String exchangeReward;
    private final int canBuyMinute;

    private List<Reward.RewardItem.Builder> useItemList;
    private List<Reward.RewardItem.Builder> buyGoldList;
    private int targetItemId;
    private int targetItemCount;
    private Map<Integer, Integer> finalMultipleMap;
    private int exchangeRewardId;
    private int useItemId;

    public SupplyCrateKVCfg(){
        this.useItem = "";
        this.buyGold = "";
        this.award = 0;
        this.targetItem = "";
        this.serverDelay = 0;
        this.finalMultiple = "";
        this.boxMax = 0;
        this.vitTarget = 200;
        this.rankMax = 120;
        this.exchangeReward = "";
        this.canBuyMinute = 1440;
    }

    public String getUseItem(){
        return this.useItem;
    }

    public String getBuyGold(){
        return this.buyGold;
    }

    public int getAward(){
        return this.award;
    }

    public String getTargetItem(){
        return this.targetItem;
    }

    public long getServerDelay(){
        return this.serverDelay * 1000l;
    }

    @Override
    protected boolean assemble() {
        try {
            this.useItemList = RewardHelper.toRewardItemImmutableList(this.useItem);
            this.buyGoldList = RewardHelper.toRewardItemImmutableList(this.buyGold);
            String[] split = targetItem.split("_");
            this.targetItemId = Integer.parseInt(split[1]);
            this.targetItemCount = Integer.parseInt(split[2]);
            this.finalMultipleMap = SerializeHelper.stringToMap(finalMultiple, Integer.class, Integer.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.BETWEEN_ITEMS);
            split = exchangeReward.split("_");
            this.exchangeRewardId = Integer.parseInt(split[1]);
            split = useItem.split("_");
            this.useItemId = Integer.parseInt(split[1]);
            return true;
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
    }

    public List<Reward.RewardItem.Builder> getUseItemList() {
        return useItemList;
    }

    public List<Reward.RewardItem.Builder> getBuyGoldList() {
        return buyGoldList;
    }

    public int getTargetItemId() {
        return targetItemId;
    }

    public int getTargetItemCount() {
        return targetItemCount;
    }

    public Map<Integer, Integer> getFinalMultipleMap() {
        return finalMultipleMap;
    }

    public int getBoxMax() {
        return boxMax;
    }

    public int getVitTarget() {
        return vitTarget;
    }

    public int getRankMax() {
        return rankMax;
    }

    public long getCanBuyTime() {
        return TimeUnit.MINUTES.toMillis(canBuyMinute);
    }

    public int getExchangeRewardId() {
        return exchangeRewardId;
    }

    public int getUseItemId() {
        return useItemId;
    }
}