package com.hawk.activity.type.impl.starLightSign.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/starlight_sign/mission_reward.xml")
public class StarLightSignMissionRateCfg extends HawkConfigBase {
    @Id
    private final int id;

    private final int value;

    private final String rewardId;

    private final int markup;

    private List<Reward.RewardItem.Builder> rewardList;

    public StarLightSignMissionRateCfg(){
        id = 0;
        value = 0;
        rewardId = "";
        markup = 0;
    }

    /**
     * 解析
     * @return 解析结果
     */
    @Override
    protected boolean assemble() {
        try {
            rewardList = RewardHelper.toRewardItemImmutableList(rewardId);
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
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewardId);
        if (!valid) {
            throw new InvalidParameterException(String.format("SeasonKingAwardCfg reward error, id: %s , reward: %s", id, rewardId));
        }
        return super.checkValid();
    }

    public List<Reward.RewardItem.Builder> getRewardList() {
        return rewardList;
    }

    public int getValue() {
        return value;
    }

    public int getMarkup() {
        return markup;
    }
}
