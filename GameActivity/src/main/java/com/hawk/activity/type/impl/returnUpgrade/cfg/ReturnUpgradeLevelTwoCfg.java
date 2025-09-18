package com.hawk.activity.type.impl.returnUpgrade.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import java.security.InvalidParameterException;
import java.util.List;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/return_upgrade/return_upgrade_level_two.xml")
public class ReturnUpgradeLevelTwoCfg extends HawkConfigBase{
    @Id
    private final int playerId;
    private final String playerCost;

    private List<Reward.RewardItem.Builder> playerCostList;
    private long playerCostNum;

    public ReturnUpgradeLevelTwoCfg(){
        this.playerId = 0;
        this.playerCost = "";
    }

    @Override
    protected boolean assemble() {
        try {
            this.playerCostList = RewardHelper.toRewardItemImmutableList(this.playerCost);
            this.playerCostNum = playerCostList.get(0).getItemCount();
            return true;
        } catch (Exception arg1) {
            HawkException.catchException(arg1, new Object[0]);
            return false;
        }
    }

    /**
     * 检查道具合法性
     * @return 检查结果
     */
    @Override
    protected final boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(playerCost);
        if (!valid) {
            throw new InvalidParameterException(String.format("ReturnUpgradeLevelTwoCfg reward error, id: %s , needItem: %s", playerId, playerCost));
        }
        return super.checkValid();
    }

    public int getPlayerId(){
        return this.playerId;
    }

    public String getPlayerCost(){
        return this.playerCost;
    }

    public List<Reward.RewardItem.Builder> getPlayerCostList() {
        return playerCostList;
    }

    public long getPlayerCostNum() {
        return playerCostNum;
    }
}