package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/cross_conquer_rank_reward.xml")
public class CrossConquerRankRewardCfg extends HawkConfigBase {
    @Id
    protected final int id;
    protected final int rankMin;
    protected final int rankMax;
    protected final String rewards;

    public CrossConquerRankRewardCfg(){
        id = 0;
        rankMin = 0;
        rankMax = 0;
        rewards = "";
    }

    public int getRankMin() {
        return rankMin;
    }

    public int getRankMax() {
        return rankMax;
    }

    public String getRewards() {
        return rewards;
    }
}
