package com.hawk.game.config;

import com.hawk.game.item.ItemInfo;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.ArrayList;
import java.util.List;

@HawkConfigManager.XmlResource(file = "xml/xhjz_league_target_reward.xml")
public class XHJZSeasonTargetRewardCfg extends HawkConfigBase {
    /** 活动期数*/
    @Id
    private final int id;
    private final int type;
    private final int target;
    private final String reward;

    /**
     * 任务奖励列表
     */
    private List<ItemInfo> rewardItems;

    public XHJZSeasonTargetRewardCfg(){
        id = 0;
        type = 0;
        target = 0;
        reward = "";

    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public int getTarget() {
        return target;
    }

    public String getReward() {
        return reward;
    }

    public List<ItemInfo> getRewardItem() {
        List<ItemInfo> copy = new ArrayList<>();
        for (ItemInfo item : rewardItems) {
            copy.add(item.clone());
        }
        return copy;
    }

    protected boolean assemble() {
        this.rewardItems = ItemInfo.valueListOf(this.reward);
        return true;
    }

    public static boolean doAssemble() {
        return true;
    }
}
