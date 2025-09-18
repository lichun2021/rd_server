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
@HawkConfigManager.XmlResource(file = "activity/return_upgrade/return_upgrade_level.xml")
public class ReturnUpgradeLevelCfg extends HawkConfigBase{
    @Id
    private final int buildId;
    private final String buildCost;
    private final String techCost;
    private final String techId;


    private List<Reward.RewardItem.Builder> buildCostList;
    private List<Reward.RewardItem.Builder> techCostList;

    private long buildCostNum;
    private long techCostNum;

    public ReturnUpgradeLevelCfg(){
        this.buildId = 0;
        this.buildCost = "";
        this.techCost = "";
        this.techId = "";
    }

    @Override
    protected boolean assemble() {
        try {
            this.buildCostList = RewardHelper.toRewardItemImmutableList(this.buildCost);
            this.techCostList = RewardHelper.toRewardItemImmutableList(this.techCost);
            this.buildCostNum = buildCostList.get(0).getItemCount();
            this.techCostNum = techCostList.get(0).getItemCount();
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
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(buildCost);
        if (!valid) {
            throw new InvalidParameterException(String.format("ReturnUpgradeLevelCfg reward error, id: %s , needItem: %s", buildId, buildCost));
        }
        valid = ConfigChecker.getDefaultChecker().checkAwardsValid(techCost);
        if (!valid) {
            throw new InvalidParameterException(String.format("ReturnUpgradeLevelCfg reward error, id: %s , gainItem: %s", buildId, techCost));
        }
        return super.checkValid();
    }

    public int getBuildId(){
        return this.buildId;
    }

    public String getBuildCost(){
        return this.buildCost;
    }

    public String getTechCost(){
        return this.techCost;
    }

    public String getTechId(){
        return this.techId;
    }

    public List<Reward.RewardItem.Builder> getBuildCostList() {
        return buildCostList;
    }

    public List<Reward.RewardItem.Builder> getTechCostList() {
        return techCostList;
    }

    public long getBuildCostNum() {
        return buildCostNum;
    }

    public long getTechCostNum() {
        return techCostNum;
    }
}