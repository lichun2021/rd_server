package com.hawk.activity.type.impl.seasonActivity.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.List;

/**
 * 赛事活动战令礼包表
 */
@HawkConfigManager.XmlResource(file = "activity/season/season_authority.xml")
public class SeasonAuthorityCfg extends HawkConfigBase {
    @Id
    private final int id;

    private final int exp;

    private final int order;

    private final int supply;

    private final String reward;

    private List<Reward.RewardItem.Builder> rewardList;

    public SeasonAuthorityCfg(){
        id = 0;
        exp = 0;
        order = 0;
        supply = 0;
        reward = "";
    }

    @Override
    protected boolean assemble() {
        try {
            rewardList = RewardHelper.toRewardItemImmutableList(reward);
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }


    public int getId() {
        return id;
    }

    public int getExp() {
        return exp;
    }

    public int getOrder() {
        return order;
    }

    public int getSupply() {
        return supply;
    }

    public String getReward() {
        return reward;
    }

    public List<Reward.RewardItem.Builder> getRewardList() {
        return rewardList;
    }
}
