package com.hawk.activity.type.impl.changeServer.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/changeserver/changeserver_cost_power.xml")
public class ChangeServerCostPowerCfg extends HawkConfigBase {
    @Id
    private final int id;
    private final int powerUpper;
    private final int powerLower;
    private final int cost;

    public ChangeServerCostPowerCfg(){
        id = 0;
        powerUpper = 0;
        powerLower = 0;
        cost = 0;
    }

    public int getId() {
        return id;
    }

    public int getPowerUpper() {
        return powerUpper;
    }

    public int getPowerLower() {
        return powerLower;
    }

    public int getCost() {
        return cost;
    }
}
