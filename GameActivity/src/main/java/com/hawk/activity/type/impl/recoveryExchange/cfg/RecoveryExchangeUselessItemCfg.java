package com.hawk.activity.type.impl.recoveryExchange.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.List;

/**
 * @author richard
 * 道具回收和赎回配置
 */
@HawkConfigManager.XmlResource(file = "activity/recovery_exchange/recovery_exchange_uselessItem.xml")
public class RecoveryExchangeUselessItemCfg extends HawkConfigBase{
    @Id
    private final int id;
    /**可回收的物品*/
    private final String itemId;
    /**可回收的物品回收的价格*/
    private final String recoveryIntegral;
    /**可回收的物品赎回的价格*/
    private final String redeemIntegral;
    /**可回收的物品*/
    public RecoveryExchangeUselessItemCfg() {
        this.id = 0;
        this.itemId = "";
        this.recoveryIntegral = "";
        this.redeemIntegral = "";
    }

    public int getId() {
        return id;
    }

    public List<RewardItem.Builder> getItemId(){
        return RewardHelper.toRewardItemImmutableList(this.itemId);
    }

    public List<RewardItem.Builder> getRecoveryIntegral(){
        return RewardHelper.toRewardItemImmutableList(this.recoveryIntegral);
    }

    public List<RewardItem.Builder> getRedeemIntegral(){
        return RewardHelper.toRewardItemImmutableList(this.redeemIntegral);
    }

    @Override
    protected boolean assemble() {
        return super.assemble();
    }
}
