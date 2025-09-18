package com.hawk.activity.type.impl.homeland.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@HawkConfigManager.XmlResource(file = "activity/act_home382/act_home382_pattern.xml")
public class HomeLandPuzzleActivityPoolCfg extends HawkConfigBase {
    /**
     * 活动期数
     */
    @Id
    private final int id;
    private final int poolType;
    private final int weight;
    private final String rewards;
    private final String pattern;
    private static Map<Integer, List<HomeLandPuzzleActivityPoolCfg>> poolConfigMap = new HashMap<>();
    protected List<Reward.RewardItem.Builder> itemInfo;
    protected Reward.RewardItem.Builder patternItem;

    public HomeLandPuzzleActivityPoolCfg() {
        id = 0;
        weight = 0;
        poolType = 0;
        rewards = "";
        pattern = "";
    }

    @Override
    protected boolean assemble() {
        List<HomeLandPuzzleActivityPoolCfg> poolList = poolConfigMap.computeIfAbsent(poolType, k -> new ArrayList<>());
        poolList.add(this);
        itemInfo = RewardHelper.toRewardItemImmutableList(rewards);
        patternItem = RewardHelper.toRewardItem(pattern);
        return true;
    }

    public int getPool() {
        return poolType;
    }

    public int getWeight() {
        return weight;
    }

    public String getItem() {
        return rewards;
    }

    public int getId() {
        return id;
    }

    public static List<HomeLandPuzzleActivityPoolCfg> getConfigList(int pool) {
        return poolConfigMap.get(pool);
    }

    public List<Reward.RewardItem.Builder> getItemInfo() {
        return itemInfo;
    }
    public Reward.RewardItem.Builder getPatternItem() {
        return patternItem;
    }

    public String getPattern() {
        return pattern;
    }

    public String getRewards() {
        return rewards;
    }
}
