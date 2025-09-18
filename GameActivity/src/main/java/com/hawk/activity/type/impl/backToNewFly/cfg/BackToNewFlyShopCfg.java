package com.hawk.activity.type.impl.backToNewFly.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
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
@HawkConfigManager.XmlResource(file = "activity/back_to_new_fly/back_to_new_fly_shop.xml")
public class BackToNewFlyShopCfg extends AExchangeTipConfig {
    @Id
    private final int id;

    /** 每份消耗物品 */
    private final String needItem;

    /** 每份获得物品 */
    private final String gainItem;

    /** 最大可兑换次数 */
    private final int times;

    /** 每份获得物品 */
    private List<Reward.RewardItem.Builder> gainItemList;

    /** 每份消耗物品 */
    private List<Reward.RewardItem.Builder> needItemList;


    public BackToNewFlyShopCfg(){
        this.id = 0;
        this.gainItem = "";
        this.needItem = "";
        this.times = 0;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getNeedItem() {
        return needItem;
    }

    public String getGainItem() {
        return gainItem;
    }

    public int getTimes() {
        return times;
    }

    /**
     * 解析配置
     * @return 解析结果
     */
    @Override
    protected boolean assemble() {
        try {
            this.gainItemList = RewardHelper.toRewardItemImmutableList(this.gainItem);
            this.needItemList = RewardHelper.toRewardItemImmutableList(this.needItem);
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
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(needItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("SeasonShopCfg reward error, id: %s , needItem: %s", id, needItem));
        }
        valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("SeasonShopCfg reward error, id: %s , gainItem: %s", id, gainItem));
        }
        return super.checkValid();
    }

    public List<Reward.RewardItem.Builder> getNeedItemList() {
        return needItemList;
    }

    public List<Reward.RewardItem.Builder> getGainItemList() {
        return gainItemList;
    }

}