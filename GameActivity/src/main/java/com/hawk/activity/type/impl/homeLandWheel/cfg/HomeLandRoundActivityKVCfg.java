package com.hawk.activity.type.impl.homeLandWheel.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.List;

@HawkConfigManager.KVResource(file = "activity/homeland_round/homeland_round_base.xml")
public class HomeLandRoundActivityKVCfg extends HawkConfigBase {
    /**
     * 服务器开服延时开启活动时间
     */
    private final int serverDelay;
    private final int termLimit;
    private final String coinPrice;
    private final String coinItemId;
    private final int baseLimit;
    private final int coinBuyLimit;
    private List<Reward.RewardItem.Builder> coinPriceInfo;
    private List<Reward.RewardItem.Builder> coinItemInfo;

    public HomeLandRoundActivityKVCfg() {
        serverDelay = 0;
        termLimit = 0;
        coinPrice = "";
        baseLimit = 0;
        coinBuyLimit = 0;
        coinItemId = "";
    }

    @Override
    protected boolean assemble() {
        coinPriceInfo = RewardHelper.toRewardItemImmutableList(coinPrice);
        coinItemInfo = RewardHelper.toRewardItemImmutableList(coinItemId);
        return true;
    }

    public long getServerDelay() {
        return serverDelay * 1000l;
    }

    public int getTermLimit() {
        return termLimit;
    }

    public int getBaseLimit() {
        return baseLimit;
    }

    public int getCoinBuyLimit() {
        return coinBuyLimit;
    }

    public List<Reward.RewardItem.Builder> getCoinPriceInfo() {
        return coinPriceInfo;
    }

    public List<Reward.RewardItem.Builder> getCoinItemInfo() {
        return coinItemInfo;
    }
}
