package com.hawk.activity.type.impl.heavenBlessing.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@HawkConfigManager.XmlResource(file = "activity/heaven_blessing/heaven_blessing_achieve_cfg.xml")
public class HeavenBlessingAchieveCfg extends AchieveConfig {
    /**
     * 成就id
     */
    @Id
    private final int achieveId;
    /**
     * 条件类型
     */
    private final int conditionType;
    /**
     * 条件值
     */
    private final String conditionValue;
    /**
     * 奖励列表
     */
    private final String rewards;
    /**
     * 随机奖励权重
     */
    private final String probability;

    /**
     * 成就类型
     */
    private AchieveType achieveType;
    /**
     * 奖励列表
     */
    private List<Reward.RewardItem.Builder> rewardList;
    /**
     * 奖励权重
     */
    private List<Integer> probabilityList;
    /**
     * 条件值列表
     */
    private List<Integer> conditionValueList;

    public HeavenBlessingAchieveCfg() {
        achieveId = 0;
        conditionType = 0;
        conditionValue = "";
        rewards = "";
        probability = "";
    }

    @Override
    protected boolean assemble() {
        try {
            achieveType = AchieveType.getType(conditionType);
            if (achieveType == null) {
                HawkLog.errPrintln("achieve type not found! type: {}", conditionType);
                return false;
            }
            rewardList = RewardHelper.toRewardItemImmutableList(rewards);
            conditionValueList = SerializeHelper.stringToList(Integer.class, conditionValue, SerializeHelper.ATTRIBUTE_SPLIT);
            if(achieveType == AchieveType.HEAVEN_BLESSING_PAY){
                probabilityList = SerializeHelper.stringToList(Integer.class, probability, SerializeHelper.BETWEEN_ITEMS);
                if(probabilityList.size() != rewardList.size()){
                    return false;
                }
            }
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    public String getRewards() {
        return rewards;
    }

    @Override
    public int getAchieveId() {
        return achieveId;
    }

    @Override
    public AchieveType getAchieveType() {
        return achieveType;
    }

    @Override
    public List<Integer> getConditionValues() {
        return conditionValueList;
    }

    @Override
    public List<Reward.RewardItem.Builder> getRewardList() {
        if(achieveType == AchieveType.HEAVEN_BLESSING_PAY){
            Reward.RewardItem.Builder builder = HawkRand.randomWeightObject(rewardList, probabilityList);
            if(builder == null){
                Random random = new Random();
                int index = random.nextInt(rewardList.size());
                builder = rewardList.get(index);
            }
            List<Reward.RewardItem.Builder> itemList = new ArrayList<>();
            itemList.add(builder);
            return itemList;
        }
        return rewardList;
    }

    @Override
    public String getReward() {
        return rewards;
    }
}
