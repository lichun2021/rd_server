package com.hawk.activity.type.impl.changeServer.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/changeserver/changeserver_cost_rank.xml")
public class ChangeServerCostRankCfg  extends HawkConfigBase {
    @Id
    private final int id;
    private final int rankUpper;
    private final int rankLower;
    private final int cost;

    public ChangeServerCostRankCfg(){
        id = 0;
        rankUpper = 0;
        rankLower = 0;
        cost = 0;
    }

    public int getId() {
        return id;
    }

    public int getRankUpper() {
        return rankUpper;
    }

    public int getRankLower() {
        return rankLower;
    }

    public int getCost() {
        return cost;
    }
}
