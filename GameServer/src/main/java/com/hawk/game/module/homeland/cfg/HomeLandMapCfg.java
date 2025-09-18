package com.hawk.game.module.homeland.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 家园建筑配置
 *
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "xml/homeland_map.xml")
public class HomeLandMapCfg extends HawkConfigBase {
    @Id
    protected final int mapId;

    protected final String blockId;


    public HomeLandMapCfg() {
        mapId = 0;
        blockId = "";
    }

    /**
     * 该建筑是否为主建筑
     *
     * @return
     */

    @Override
    protected boolean assemble() {
        return true;
    }


    @Override
    protected boolean checkValid() {
        return true;
    }

    public int getMapId() {
        return mapId;
    }

    public String getBlock() {
        return blockId;
    }
}
