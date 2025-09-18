package com.hawk.game.module.homeland.cfg;

import com.google.common.collect.ImmutableList;
import com.hawk.game.item.ItemInfo;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家园抽奖
 *
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "xml/homeland_gacha_pool.xml")
public class HomeLandGachaPoolCfg extends HawkConfigBase {
    @Id
    protected final int id;
    protected final int reward;

    public HomeLandGachaPoolCfg() {
        id = 0;
        reward = 0;
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

    public int getId() {
        return id;
    }

    public int getReward() {
        return reward;
    }

}
