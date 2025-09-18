package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/world_dress_god.xml")
public class DressGodCfg extends HawkConfigBase {
    @Id
    protected final int id;
    /**
     * 数量
     */
    protected final int needCount;
    /**
     * 装扮id
     */
    protected final int dressId;

    public DressGodCfg(){
        id = 0;
        needCount = 0;
        dressId = 0;
    }

    public int getNeedCount() {
        return needCount;
    }

    public int getDressId() {
        return dressId;
    }
}
