package com.hawk.activity.type.impl.homeLandWheel.cfg;

import com.google.common.collect.ImmutableMap;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@HawkConfigManager.XmlResource(file = "activity/homeland_round/homeland_round_floor.xml")
public class HomeLandRoundActivityFloorCfg extends HawkConfigBase {
    /**
     * 活动期数
     */
    @Id
    private final int id;
    private final int floor;
    private final String resultWeight;
    private final String guaranteeWeight;
    private final int guarantee;
    private final String cost;
    private final String floorReward;
    private static Map<Integer, HomeLandRoundActivityFloorCfg> poolConfigMap = new HashMap<>();
    private Map<Integer, Integer> resultWeightMap; // Key: 层级变化, Value: 权重
    private Map<Integer, Integer> guaranteeWeightMap; // Key: 层级变化, Value: 保底权重
    private List<Reward.RewardItem.Builder> floorRewardMap;
    private List<Reward.RewardItem.Builder> costInfo;

    public HomeLandRoundActivityFloorCfg() {
        id = 0;
        floor = 0;
        resultWeight = "";
        floorReward = "";
        guaranteeWeight = "";
        cost = "";
        guarantee = 0;
    }

    public int getId() {
        return id;
    }

    private Map<Integer, Integer> parseWeights(String weightStr) {
        Map<Integer, Integer> map = new HashMap<>();
        if (weightStr == null || weightStr.isEmpty()) return map;
        for (String part : weightStr.split(",")) {
            String[] pair = part.split("_");
            map.put(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
        }
        return map;
    }

    @Override
    protected boolean assemble() {
        poolConfigMap.put(floor, this);
        resultWeightMap = ImmutableMap.copyOf(parseWeights(resultWeight));
        guaranteeWeightMap = ImmutableMap.copyOf(parseWeights(guaranteeWeight));
        floorRewardMap = RewardHelper.toRewardItemImmutableList(floorReward);
        costInfo = RewardHelper.toRewardItemImmutableList(cost);
        return true;
    }

    public static HomeLandRoundActivityFloorCfg getConfigList(int pool) {
        return poolConfigMap.get(pool);
    }

    public static int getMaxFloor() {
        return HawkConfigManager.getInstance().getConfigSize(HomeLandRoundActivityFloorCfg.class) - 1;
    }

    public Map<Integer, Integer> getResultWeightMap() {
        return resultWeightMap;
    }

    public Map<Integer, Integer> getGuaranteeWeightMap() {
        return guaranteeWeightMap;
    }

    public int getGuarantee() {
        return guarantee;
    }

    public List<Reward.RewardItem.Builder> getCostInfo() {
        return costInfo;
    }

    public List<Reward.RewardItem.Builder> getFloorRewardInfo() {
        return floorRewardMap;
    }
}
