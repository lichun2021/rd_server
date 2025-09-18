package com.hawk.activity.type.impl.seasonActivity.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/season/season_king_award.xml")
public class SeasonKingAwardCfg extends HawkConfigBase {
    @Id
    private final int id;

    //最高排名
    private final int rankUpper;

    //最低排名
    private final int rankLower;

    //奖励
    private final String reward;

    //奖励
    private List<Reward.RewardItem.Builder> rewardList;

    public SeasonKingAwardCfg(){
        id = 0;
        rankUpper = 0;
        rankLower = 0;
        reward = "";
    }

    /**
     * 解析
     * @return 解析结果
     */
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

    /**
     * 检查道具合法性
     * @return 检查结果
     */
    @Override
    protected final boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
        if (!valid) {
            throw new InvalidParameterException(String.format("SeasonKingAwardCfg reward error, id: %s , reward: %s", id, reward));
        }
        return super.checkValid();
    }

    public List<Reward.RewardItem.Builder> getRewardList() {
        return rewardList;
    }

    public int getId() {
        return id;
    }

    public int getRankUpper() {
        return rankUpper;
    }

    public int getRankLower() {
        return rankLower;
    }
}
