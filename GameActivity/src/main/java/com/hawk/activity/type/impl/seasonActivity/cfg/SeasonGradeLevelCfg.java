package com.hawk.activity.type.impl.seasonActivity.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.List;

/**
 * 赛事活动段位配置表
 */
@HawkConfigManager.XmlResource(file = "activity/season/season_grade_level.xml")
public class SeasonGradeLevelCfg extends HawkConfigBase {
    @Id
    private final int gradeLevel;

    private final int levelUpExp;

    private final String normalReward;

    private List<Reward.RewardItem.Builder> normalRewardList;

    public SeasonGradeLevelCfg(){
        gradeLevel = 0;
        levelUpExp = 0;
        normalReward = "";
    }

    @Override
    protected boolean assemble() {
        try {
            normalRewardList = RewardHelper.toRewardItemImmutableList(normalReward);
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public int getLevelUpExp() {
        return levelUpExp;
    }

    public List<Reward.RewardItem.Builder> getNormalRewardList() {
        return normalRewardList;
    }
}
