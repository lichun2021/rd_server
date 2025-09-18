package com.hawk.activity.type.impl.starLightSign.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/starlight_sign/sign_reward.xml")
public class StarLightSignRewardCfg extends HawkConfigBase {
    @Id
    private final int id;

    private final int day;

    private final int type;

    private final String Ordinaryrewards;

    private final String Juniorrewards;

    private final String Intermediaterewards;

    private final String AdvancedRewards;

    private List<Reward.RewardItem.Builder> ordinaryRewardList;

    private List<Reward.RewardItem.Builder> juniorRewardList;

    private List<Reward.RewardItem.Builder> intermediateRewardList;

    private List<Reward.RewardItem.Builder> AdvancedRewardList;

    public StarLightSignRewardCfg(){
        id = 0;
        day = 0;
        type = 0;
        Ordinaryrewards = "";
        Juniorrewards = "";
        Intermediaterewards = "";
        AdvancedRewards = "";

    }

    /**
     * 解析
     * @return 解析结果
     */
    @Override
    protected boolean assemble() {
        try {
            ordinaryRewardList = RewardHelper.toRewardItemImmutableList(Ordinaryrewards);
            juniorRewardList = RewardHelper.toRewardItemImmutableList(Juniorrewards);
            intermediateRewardList = RewardHelper.toRewardItemImmutableList(Intermediaterewards);
            AdvancedRewardList = RewardHelper.toRewardItemImmutableList(AdvancedRewards);
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
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(Ordinaryrewards);
        valid = valid && ConfigChecker.getDefaultChecker().checkAwardsValid(Juniorrewards);
        valid = valid && ConfigChecker.getDefaultChecker().checkAwardsValid(Intermediaterewards);
        valid = valid && ConfigChecker.getDefaultChecker().checkAwardsValid(AdvancedRewards);
        if (!valid) {
            throw new InvalidParameterException(String.format("SeasonKingAwardCfg reward error, id: %s", id));
        }
        return super.checkValid();
    }

    public int getDay() {
        return day;
    }

    public int getType() {
        return type;
    }

    public List<Reward.RewardItem.Builder> getOrdinaryRewardList() {
        return ordinaryRewardList;
    }

    public List<Reward.RewardItem.Builder> getJuniorRewardList() {
        return juniorRewardList;
    }

    public List<Reward.RewardItem.Builder> getIntermediateRewardList() {
        return intermediateRewardList;
    }

    public List<Reward.RewardItem.Builder> getAdvancedRewardList() {
        return AdvancedRewardList;
    }
}
