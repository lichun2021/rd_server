package com.hawk.activity.type.impl.submarineWar.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/submarine_war/submarine_war_achieve.xml")
public class SubmarineWarAchieveCfg  extends AchieveConfig{
    /** 成就id*/
    @Id
    private final int achieveId;
    /** 条件类型*/
    private final int conditionType;
    /** 条件值*/
    private final String conditionValue;
    /** 奖励列表*/
    private final String rewards;
    
    private final int type;


    private final int refreshDays;
    
    
    private final String passExtraReward;
    
    /** 成就类型 */
    private AchieveType achieveType;
    /** 成就奖励 */
    private List<Reward.RewardItem.Builder> rewardList;
    /** 成就值 */
    private List<Integer> conditionValueList;
    
    /** 成就奖励 */
    private List<Reward.RewardItem.Builder> passExtraRewardList;

    public SubmarineWarAchieveCfg(){
        achieveId = 0;
        conditionType = 0;
        conditionValue = "";
        rewards = "";
        type = 0;
        refreshDays = 0;
        passExtraReward = "";
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
            
            passExtraRewardList = RewardHelper.toRewardItemImmutableList(passExtraReward);
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
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
    
    public int getRefreshDays() {
		return refreshDays;
	}
    
    public List<Reward.RewardItem.Builder> getPassExtraRewardList() {
		return passExtraRewardList;
	}
}