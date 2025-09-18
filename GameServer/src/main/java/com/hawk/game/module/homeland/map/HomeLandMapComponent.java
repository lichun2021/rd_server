package com.hawk.game.module.homeland.map;


import com.hawk.game.module.homeland.cfg.HomeLandMapCfg;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkOSOperator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HomeLandMapComponent {
    private final Map<Integer, HomeLandMap> landMap = new ConcurrentHashMap<>();
    /**
     * 是否初始化
     */
    boolean initOK = false;

    public HomeLandMapComponent() {
    }

    public boolean init() {
        if (initOK) {
            return true;
        }
        ConfigIterator<HomeLandMapCfg> mapCfgs = HawkConfigManager.getInstance().getConfigIterator(HomeLandMapCfg.class);
        for (HomeLandMapCfg cfg : mapCfgs) {
            if (HawkOSOperator.isEmptyString(cfg.getBlock())) {
                continue;
            }
            HomeLandMap map = new HomeLandMap();
            //添加建筑
            map.init(cfg.getMapId());
            landMap.put(cfg.getMapId(), map);
        }
        initOK = true;
        return true;
    }

    public void addViewPoint(int themeId, IHomeLandPoint point) {
        if (!landMap.containsKey(themeId)) {
            return;
        }
        landMap.get(themeId).addViewPoint(point);
    }

    public HomeLandMap getMap(int themeId) {
        return landMap.get(themeId);
    }
}
