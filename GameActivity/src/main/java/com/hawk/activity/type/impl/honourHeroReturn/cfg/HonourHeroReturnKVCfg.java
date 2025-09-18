package com.hawk.activity.type.impl.honourHeroReturn.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * @author richard
 */
@HawkConfigManager.KVResource(file = "activity/honour_hero_return/honour_hero_return_cfg.xml")
public class HonourHeroReturnKVCfg extends HawkConfigBase {
    /**
     * 服务器开服延时开启活动时间；单位:秒
     */
    private final int serverDelay;
    /**
     * 整个活动期间使用凭证抽奖的次数限制
     */
    private final int limitTimes;
    /**
     * 抽奖的固定奖励
     */
    private final String fixedLotteryRewards;
    /**
     * 单抽消耗
     */
    private final String oneCost;

    /**
     * 十连消耗
     */
    private final String tenCost;

    public HonourHeroReturnKVCfg() {
        serverDelay = 0;
        limitTimes = 0;
        fixedLotteryRewards = "";
        oneCost = "";
        tenCost = "";
    }

    public long getServerDelay() {
        return serverDelay  * 1000L;
    }

    public int getLimitTimes() {
        return limitTimes;
    }

    @Override
    protected boolean assemble() {
        return super.assemble();
    }

    @Override
    protected boolean checkValid() {
        return super.checkValid();
    }

    public List<RewardItem.Builder> getFixedLotteryRewards() {
        return RewardHelper.toRewardItemImmutableList(fixedLotteryRewards);
    }
    public List<RewardItem.Builder> getOneCost() {
        return RewardHelper.toRewardItemImmutableList(oneCost);
    }

    public List<RewardItem.Builder> getTenCost() {
        return RewardHelper.toRewardItemImmutableList(tenCost);
    }
}