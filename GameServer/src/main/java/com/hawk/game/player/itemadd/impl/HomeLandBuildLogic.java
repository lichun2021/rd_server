package com.hawk.game.player.itemadd.impl;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.module.homeland.entity.PlayerHomeLandEntity;
import com.hawk.game.module.homeland.cfg.HomeLandBuildingCfg;
import com.hawk.game.module.homeland.entity.HomeLandComponent;
import com.hawk.game.player.Player;
import com.hawk.game.player.itemadd.ItemAddLogic;
import com.hawk.game.protocol.HP;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;

public class HomeLandBuildLogic implements ItemAddLogic {

    @Override
    public void addLogic(Player player, int itemId, int addCount, Action action) {

        ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
        if (itemCfg == null) {
            return;
        }
        int buildId = itemCfg.getBuildId();
        HomeLandBuildingCfg buildCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandBuildingCfg.class, buildId);
        if (buildCfg == null) {
            return;
        }
        PlayerHomeLandEntity entity = player.getData().getHomeLandEntity();
        HomeLandComponent component = entity.getComponent();
        component.addWareHouse(buildCfg.getId(), buildCfg.getBuildType(), addCount);
        component.notifyWareHouseAndCollectChange(0);
    }
}
