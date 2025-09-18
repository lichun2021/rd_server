package com.hawk.activity.type.impl.returnUpgrade.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.List;

/**
* 本文件自动生成，会被覆盖，不要手改非自动生成部分
*/
@HawkConfigManager.KVResource(file = "activity/return_upgrade/return_upgrade_cfg.xml")
public class ReturnUpgradeKvCfg extends HawkConfigBase{
    private final int playerBuildRanking;
    private final int itemLimit;
    private final String itemCost;
    private final int playerRanking;
    private final String itemUse;
    private final int serverDelay;
    private final int rankingDelay;

    /** 消耗道具 */
    private List<Reward.RewardItem.Builder> itemUseList;

    /** 道具售价 */
    private List<Reward.RewardItem.Builder> itemCostList;

    public ReturnUpgradeKvCfg(){
        this.playerBuildRanking = 0;
        this.itemLimit = 0;
        this.itemCost = "";
        this.playerRanking = 0;
        this.itemUse = "";
        this.serverDelay = 0;
        this.rankingDelay = 0;
    }

    public int getPlayerBuildRanking(){
        return this.playerBuildRanking;
    }

    public int getItemLimit(){
        return this.itemLimit;
    }

    public String getItemCost(){
        return this.itemCost;
    }

    public int getPlayerRanking(){
        return this.playerRanking;
    }

    public String getItemUse(){
        return this.itemUse;
    }

    public int getServerDelay(){
        return this.serverDelay;
    }

    public int getRankingDelay() {
        return rankingDelay;
    }

    public List<Reward.RewardItem.Builder> getItemUseList() {
        return itemUseList;
    }

    public List<Reward.RewardItem.Builder> getItemCostList() {
        return itemCostList;
    }

    @Override
    protected boolean assemble() {
        try {
            this.itemUseList = RewardHelper.toRewardItemImmutableList(this.itemUse);
            this.itemCostList = RewardHelper.toRewardItemImmutableList(this.itemCost);
            return true;
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
    }

}