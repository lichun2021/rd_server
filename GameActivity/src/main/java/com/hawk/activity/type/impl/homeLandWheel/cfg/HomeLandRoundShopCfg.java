package com.hawk.activity.type.impl.homeLandWheel.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigManager;

import java.util.List;


/**
 * 命运轮抽商店兑换配置
 *
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "activity/homeland_round/homeland_round_shop.xml")
public class HomeLandRoundShopCfg extends AExchangeTipConfig {

    /**
     *
     */
    @Id
    private final int id;
    private final int times;
    private final String needItem;
    private final String gainItem;
    private List<Reward.RewardItem.Builder> needItemInfo;
    private List<Reward.RewardItem.Builder> gainItemInfo;

    public HomeLandRoundShopCfg() {
        id = 0;
        times = 0;
        needItem = "";
        gainItem = "";
    }

    @Override
    protected boolean assemble() {
        needItemInfo = RewardHelper.toRewardItemImmutableList(needItem);
        gainItemInfo = RewardHelper.toRewardItemImmutableList(gainItem);
        return true;
    }

    @Override
    protected final boolean checkValid() {
        return super.checkValid();
    }

    public int getId() {
        return id;
    }

    public int getTimes() {
        return times;
    }

    public List<Reward.RewardItem.Builder> getNeedItemInfo() {
        return needItemInfo;
    }

    public List<Reward.RewardItem.Builder> getGainItemInfo() {
        return gainItemInfo;
    }
}
