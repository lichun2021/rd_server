package com.hawk.activity.type.impl.recoveryExchange.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * @author richard
 * 道具回收活动基础配置
 */
@HawkConfigManager.KVResource(file = "activity/recovery_exchange/recovery_exchange_cfg.xml")
public class RecoveryExchangeKVCfg extends HawkConfigBase {
    /**
     * 服务器开服延时开启活动时间；单位:秒
     */
    private final int serverDelay;
    /**
     * 道具回收积分道具,本玩法中的积分
     */
    private final String recoveryItem;

    public RecoveryExchangeKVCfg() {
        serverDelay = 0;
        recoveryItem = "";
    }

    public long getServerDelay() {
        return serverDelay * 1000L;
    }

    @Override
    protected boolean assemble() {
        return super.assemble();
    }

    @Override
    protected boolean checkValid() {
        return super.checkValid();
    }

    public List<RewardItem.Builder> getRecoveryItem() {
        return RewardHelper.toRewardItemImmutableList(recoveryItem);
    }
}