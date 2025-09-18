package com.hawk.activity.type.impl.seasonActivity.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.List;

/**
 * 赛事活动战令等级表
 */
@HawkConfigManager.XmlResource(file = "activity/season/season_battlepass_level.xml")
public class SeasonBattlepassLevelCfg extends HawkConfigBase {
    @Id
    private final int level;

    private final int levelUpExp;

    private final String normalReward;

    private final String advReward;

    private final boolean normalbox;

    private final boolean advbox;

    private List<Reward.RewardItem.Builder> normalRewardList;

    private List<Reward.RewardItem.Builder> advRewardList;

    public SeasonBattlepassLevelCfg(){
        level = 0;
        levelUpExp = 0;
        normalReward = "";
        advReward = "";
        normalbox = false;
        advbox = false;
    }

    @Override
    protected boolean assemble() {
        try {
            normalRewardList = RewardHelper.toRewardItemImmutableList(normalReward);
            advRewardList = RewardHelper.toRewardItemImmutableList(advReward);
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    public int getLevel() {
        return level;
    }

    public int getLevelUpExp() {
        return levelUpExp;
    }

    public List<Reward.RewardItem.Builder> getNormalRewardList() {
        return normalRewardList;
    }

    public List<Reward.RewardItem.Builder> getAdvRewardList() {
        return advRewardList;
    }

    public boolean isNormalbox() {
        return normalbox;
    }

    public boolean isAdvbox() {
        return advbox;
    }
}
