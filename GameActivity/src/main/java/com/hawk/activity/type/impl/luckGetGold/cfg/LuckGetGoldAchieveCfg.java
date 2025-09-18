package com.hawk.activity.type.impl.luckGetGold.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/luck_get_gold/luck_get_gold_achieve.xml")
public class LuckGetGoldAchieveCfg extends AchieveConfig {
    /** 成就id*/
    @Id
    private final int achieveId;
    /** 成就分类*/
    private final int type;
    /** 条件类型*/
    private final int conditionType;
    /** 条件值*/
    private final String conditionValue;
    /** 奖励列表*/
    private final String rewards;

    /** 成就类型 */
    private AchieveType achieveType;

    /** 成就奖励 */
    private List<Reward.RewardItem.Builder> rewardList;

    /** 成就值 */
    private List<Integer> conditionValueList;

    public LuckGetGoldAchieveCfg(){
        achieveId = 0;
        type = 0;
        conditionType = 0;
        conditionValue = "";
        rewards = "";
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
    public List<Reward.RewardItem.Builder> getRewardList() {
        return rewardList;
    }

    @Override
    public String getReward() {
        return rewards;
    }

    public int getType() {
        return type;
    }
}
