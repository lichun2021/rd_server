package com.hawk.activity.type.impl.luckGetGold.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/luck_get_gold/luck_get_gold_draw.xml")
public class LuckGetGoldDrawCfg extends HawkConfigBase {
    @Id
    private final int id;

    private final int poolId;
    private final int weight;

    private final String gainItem;
    private final int isCore;

    /** 奖励物品 */
    private List<Reward.RewardItem.Builder> gainItemList;

    public LuckGetGoldDrawCfg(){
        id = 0;
        poolId = 0;
        weight = 0;
        gainItem = "";
        isCore = 0;
    }

    @Override
    protected boolean assemble() {
        try {
            this.gainItemList = RewardHelper.toRewardItemImmutableList(this.gainItem);
            return true;
        } catch (Exception arg1) {
            HawkException.catchException(arg1, new Object[0]);
            return false;
        }
    }

    @Override
    protected final boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("LuckGetGoldDrawCfg reward error, id: %s , gainItem: %s", id, gainItem));
        }
        return super.checkValid();
    }

    public int getId() {
        return id;
    }

    public int getPoolId() {
        return poolId;
    }

    public int getWeight() {
        return weight;
    }

    public List<Reward.RewardItem.Builder> getGainItemList() {
        return gainItemList;
    }

    public boolean isCore(){
        return isCore == 1;
    }
}
