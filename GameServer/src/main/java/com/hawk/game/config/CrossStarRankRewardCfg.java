package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/cross_star_rank_reward.xml")
public class CrossStarRankRewardCfg extends HawkConfigBase {
    @Id
    protected final int id;
    protected final int rankMin;
    protected final int rankMax;
    protected final String rewards;

    public CrossStarRankRewardCfg(){
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
