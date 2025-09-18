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
@HawkConfigManager.XmlResource(file = "activity/return_upgrade/return_upgrade_shop.xml")
public class ReturnUpgradeShopCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final String buildLevel;
    private final int buyTimes;
    private final String gainItem;
    private final int itemType;
    private final String payItem;

    private int buildLevelMin;
    private int buildLevelMax;

    /** 每份获得物品 */
    private List<Reward.RewardItem.Builder> gainItemList;

    /** 每份消耗物品 */
    private List<Reward.RewardItem.Builder> payItemList;

    public ReturnUpgradeShopCfg(){
        this.id = 0;
        this.buildLevel = "";
        this.buyTimes = 0;
        this.gainItem = "";
        this.itemType = 0;
        this.payItem = "";
    }

    @Override
    protected boolean assemble() {
        try {
            this.gainItemList = RewardHelper.toRewardItemImmutableList(this.gainItem);
            this.payItemList = RewardHelper.toRewardItemImmutableList(this.payItem);
            String [] buildLevelArr = this.buildLevel.split("_");
            this.buildLevelMin = Integer.valueOf(buildLevelArr[0]);
            this.buildLevelMax = Integer.valueOf(buildLevelArr[1]);
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
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("ReturnUpgradeShopCfg reward error, id: %s , needItem: %s", id, gainItem));
        }
        valid = ConfigChecker.getDefaultChecker().checkAwardsValid(payItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("ReturnUpgradeShopCfg reward error, id: %s , gainItem: %s", id, payItem));
        }
        return super.checkValid();
    }

    public int getId(){
        return this.id;
    }

    public String getBuildLevel(){
        return this.buildLevel;
    }

    public int getBuyTimes(){
        return this.buyTimes;
    }

    public String getGainItem(){
        return this.gainItem;
    }

    public int getItemType(){
        return this.itemType;
    }

    public String getPayItem(){
        return this.payItem;
    }

    public List<Reward.RewardItem.Builder> getGainItemList() {
        return gainItemList;
    }

    public List<Reward.RewardItem.Builder> getPayItemList() {
        return payItemList;
    }

    public int getBuildLevelMin() {
        return buildLevelMin;
    }

    public int getBuildLevelMax() {
        return buildLevelMax;
    }
}