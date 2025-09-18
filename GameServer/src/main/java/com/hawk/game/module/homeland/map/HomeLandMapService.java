package com.hawk.game.module.homeland.map;


import com.hawk.game.module.homeland.cfg.HomeLandMapCfg;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkOSOperator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HomeLandMapService {
    private static HomeLandMapService instance;
    private final Map<Integer, HomeLandMapBlock> blockMap = new ConcurrentHashMap<>();
    /**
     * 是否初始化
     */
    boolean initOK = false;

    public static HomeLandMapService getInstance() {
        if (instance == null) {
            instance = new HomeLandMapService();
        }
        return instance;
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
            loadBlock(cfg.getMapId());
        }
        initOK = true;
        return true;
    }

    private void loadBlock(int theme) {
        HomeLandMapBlock block = new HomeLandMapBlock();
        block.init(theme);
        blockMap.put(theme, block);
    }

    public HomeLandMapBlock getBlock(int themeId) {
        return blockMap.get(themeId);
    }
}
