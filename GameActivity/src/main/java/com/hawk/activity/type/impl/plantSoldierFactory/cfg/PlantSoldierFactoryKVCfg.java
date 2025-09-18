package com.hawk.activity.type.impl.plantSoldierFactory.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.List;

/**
* 本文件自动生成，会被覆盖，不要手改非自动生成部分
*/
@HawkConfigManager.KVResource(file = "activity/plant_soldier_factory/plant_soldier_factory_cfg.xml")
public class PlantSoldierFactoryKVCfg extends HawkConfigBase{
    private final int drawValueLimit;
    private final String drawItem;
    private final int grandTimesRefresh;
    private final int serverDelay;

    private List<Reward.RewardItem.Builder> useItemList;

    public PlantSoldierFactoryKVCfg(){
        this.drawValueLimit = 0;
        this.drawItem = "";
        this.grandTimesRefresh = 0;
        this.serverDelay = 0;
    }

    public int getDrawValueLimit(){
        return this.drawValueLimit;
    }

    public String getDrawItem(){
        return this.drawItem;
    }

    public int getGrandTimesRefresh(){
        return this.grandTimesRefresh;
    }

    public long getServerDelay(){
        return this.serverDelay * 1000l;
    }

    @Override
    protected boolean assemble() {
        try {
            this.useItemList = RewardHelper.toRewardItemImmutableList(this.drawItem);
            return true;
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
    }

    public List<Reward.RewardItem.Builder> getUseItemList() {
        return useItemList;
    }
}