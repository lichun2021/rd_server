package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "cfg/puidCtrl/payPuidCtrl.xml")
public class PayPuidCtrl extends HawkConfigBase {
    @Id
    protected final String puid;

    protected final String name;

    protected final int priority;

    protected final int vip;

    protected final int money;

    public PayPuidCtrl() {
        this.puid = "";
        this.name = "";
        this.priority = 0;
        this.vip = 0;
        this.money = 0;
    }

    public String getPuid() {
        return puid;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public int getVip() {
        return vip;
    }

    public int getMoney() {
        return money;
    }
}
