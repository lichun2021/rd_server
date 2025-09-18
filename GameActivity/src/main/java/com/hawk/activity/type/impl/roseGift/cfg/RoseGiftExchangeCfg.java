package com.hawk.activity.type.impl.roseGift.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/rose_gift/rose_gift_exchange.xml")
public class RoseGiftExchangeCfg extends HawkConfigBase {
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

    public RoseGiftExchangeCfg(){
        id = 0;
        needItem = "";
        gainItem = "";
        times = 0;
    }

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
            throw new InvalidParameterException(String.format("RoseGiftExchangeCfg reward error, id: %s , needItem: %s", id, needItem));
        }
        valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("RoseGiftExchangeCfg reward error, id: %s , gainItem: %s", id, gainItem));
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
