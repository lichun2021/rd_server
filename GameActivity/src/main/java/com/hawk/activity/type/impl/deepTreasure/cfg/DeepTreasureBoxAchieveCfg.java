package com.hawk.activity.type.impl.deepTreasure.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import java.util.List;

/**
 * 勋章宝藏成就数据
 *
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "activity/hidden_treasure_new/hidden_treasure_box_award_new.xml")
public class DeepTreasureBoxAchieveCfg extends AchieveConfig {
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
    private final int buffId;
    private final String buffPram;

    private AchieveType achieveType;
    private List<RewardItem.Builder> rewardList;
    private List<Integer> conditionValueList;

    public DeepTreasureBoxAchieveCfg() {
        achieveId = 0;
        conditionType = AchieveType.DEEP_TREASURE_BOX_TIMES.getValue();
        conditionValue = "";
        rewards = "";
        buffId = 0;
        buffPram = "";
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
    public List<RewardItem.Builder> getRewardList() {
        return rewardList;
    }

    @Override
    public String getReward() {
        return rewards;
    }

    public int getBuffId() {
        return buffId;
    }
}
