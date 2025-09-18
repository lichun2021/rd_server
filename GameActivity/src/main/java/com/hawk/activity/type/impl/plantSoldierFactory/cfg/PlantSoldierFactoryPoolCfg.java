package com.hawk.activity.type.impl.plantSoldierFactory.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/plant_soldier_factory/plant_soldier_factory_pool.xml")
public class PlantSoldierFactoryPoolCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final int poolType;
    private final String reward;
    private final int weight;
    private final int groupSign;

    private List<Reward.RewardItem.Builder> rewardList;

    private static Map<PlantSoldierFactoryPoolCfg, Integer> awardWeightMap = new HashMap<>();
    private static Map<Integer,Map<PlantSoldierFactoryPoolCfg, Integer>> bighAwardWeightMap = new HashMap<>();

    public PlantSoldierFactoryPoolCfg(){
        this.id = 0;
        this.poolType = 0;
        this.reward = "";
        this.weight = 0;
        this.groupSign = 0;
    }

    public static boolean doAssemble() {
        Map<PlantSoldierFactoryPoolCfg, Integer> tmp = new HashMap<>();
        Map<Integer,Map<PlantSoldierFactoryPoolCfg, Integer>> bigTmp = new HashMap<>();
        ConfigIterator<PlantSoldierFactoryPoolCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(PlantSoldierFactoryPoolCfg.class);
        for(PlantSoldierFactoryPoolCfg cfg : iterator){
            if(cfg.getPoolType() == 1){
                tmp.put(cfg, cfg.getWeight());
            }
            if(cfg.getPoolType() == 2){
                if(!bigTmp.containsKey(cfg.getGroupSign())){
                    bigTmp.put(cfg.getGroupSign(), new HashMap<>());
                }
                bigTmp.get(cfg.getGroupSign()).put(cfg, cfg.getWeight());
            }
        }
        awardWeightMap = tmp;
        bighAwardWeightMap = bigTmp;
        return true;
    }

    @Override
    protected boolean assemble() {
        rewardList = RewardHelper.toRewardItemImmutableList(reward);
        return true;
    }

    public int getId(){
        return this.id;
    }

    public int getPoolType(){
        return this.poolType;
    }

    public String getReward(){
        return this.reward;
    }

    public int getWeight(){
        return this.weight;
    }

    public int getGroupSign(){
        return this.groupSign;
    }

    public List<Reward.RewardItem.Builder> getRewardList() {
        return rewardList;
    }

    public static Map<PlantSoldierFactoryPoolCfg, Integer> getAwardWeightMap() {
        return awardWeightMap;
    }

    public static Map<PlantSoldierFactoryPoolCfg, Integer> getBighAwardWeightMap(int groupSign) {
        return bighAwardWeightMap.get(groupSign);
    }
}