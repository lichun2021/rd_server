package com.hawk.activity.type.impl.plantsecret.cfg;

import com.google.common.collect.ImmutableList;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

import java.security.InvalidParameterException;
import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/taineng_secret/taineng_secret_box.xml")
public class PlantSecretBoxCfg extends HawkConfigBase {
    /**
     * 唯一ID
     */
    @Id
    private final int id;
    /**
     * 等级经验
     */
    private final int upgrade;
    /**
     * 奖励
     */
    private final String specialreward;
    /**
     * 奖励
     */
    private final String reward;

    private String specialrewardStr;
    private int specialrewardMinCount, specialrewardMaxCount;

    public PlantSecretBoxCfg() {
        id = 0;
        upgrade = 0;
        specialreward = "";
        reward = "";
    }

    public int getId() {
        return id;
    }

    public int getUpgrade() {
        return upgrade;
    }

    public List<RewardItem.Builder> getAwardList() {
        return RewardHelper.toRewardItemImmutableList(this.reward);
    }

    public List<RewardItem.Builder> getSpecialAwardList() {
        RewardItem.Builder builder = RewardHelper.toRewardItem(this.specialrewardStr);
        builder.setItemCount(HawkRand.randInt(specialrewardMinCount, specialrewardMaxCount));
        return ImmutableList.of(builder);
    }

    @Override
    public boolean assemble() {
        String[] strs = specialreward.split(",");
        specialrewardStr = strs[0];
        String[] str2 = strs[1].split("_");
        specialrewardMinCount = Integer.parseInt(str2[0]);
        specialrewardMaxCount = Integer.parseInt(str2[1]);
        return true;
    }

    @Override
    protected final boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
        if (!valid) {
            throw new InvalidParameterException(String.format("PlantSecretBoxCfg reward error, id: %s , needItem: %s", id, reward));
        }

        valid = ConfigChecker.getDefaultChecker().checkAwardsValid(specialrewardStr);
        if (!valid) {
            throw new InvalidParameterException(String.format("PlantSecretBoxCfg specialreward error, id: %s , needItem: %s", id, specialrewardStr));
        }
        return super.checkValid();
    }

}
