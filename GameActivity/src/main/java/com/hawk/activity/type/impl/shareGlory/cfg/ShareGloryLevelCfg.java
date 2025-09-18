package com.hawk.activity.type.impl.shareGlory.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;


/**
 * 双十一联盟欢庆 等级奖励配置
 *
 * @author richard
 */
@HawkConfigManager.XmlResource(file = "activity/alliance_share_glory/alliance_share_glory_lv.xml")
public class ShareGloryLevelCfg extends HawkConfigBase {
    // 奖励ID
    @Id
    private final int id;
    /**
     * 等级
     */
    private final int level;
    /**
     * 类型(A能量/B能量)EnergyType
     */
    private final int type;
    /**
     * 对应等级所需经验
     */
    private final int expNeed;
    /**
     * EnergyType类型下的当前类型的最大等级
     */
    private final int isMax;
    /**
     * 能量升级时的工会全员的固定奖励
     */
    private final String rewards;
    /**
     * 根据本活动监听的活动奖励的获得数量，用此数值的万分比进行返还，
     * 返还的就是监听的道具。
     * 同一个道具有可能是多个被监听活动的奖励，即有多个指定的获取来
     * 源，这里只关心总数量，乘以此数值的万分比
     */
    private final int rewardProportion;

    public ShareGloryLevelCfg() {
        id = 0;
        level = 0;
        type = 0;
        isMax = 0;
        expNeed = -1;
        rewards = "";
        rewardProportion = 0;
    }

    @Override
    protected boolean assemble() {
        return true;
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public int getType() {
        return type;
    }

    public int getExpNeed() {
        return expNeed;
    }

    public int getIsMax() {
        return isMax;
    }

    public String getRewards() {
        return rewards;
    }

    public int getRewardProportion() {
        return rewardProportion;
    }

    public List<RewardItem.Builder> getLevelupRewardsItemList() {
        return RewardHelper.toRewardItemImmutableList(this.rewards);
    }
}
