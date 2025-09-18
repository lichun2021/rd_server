package com.hawk.game.config;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

@HawkConfigManager.XmlResource(file = "xml/xhjz_shop.xml")
public class XHJZShopCfg extends HawkConfigBase {
    @Id
    private final int id;
    /** 每份消耗物品 */
    private final String cost;
    /** 每份获得物品 */
    private final String item;
    /** 最大可兑换次数 */
    private final int numLimit;
    /** 每份获得物品 */
    private List<Reward.RewardItem.Builder> needItemList;
    /** 每份消耗物品 */
    private List<Reward.RewardItem.Builder> gainItemList;

    public XHJZShopCfg(){
        id = 0;
        cost = "";
        item = "";
        numLimit = 0;
    }

    @Override
    protected boolean assemble() {
        try {
            this.gainItemList = RewardHelper.toRewardItemImmutableList(this.item);
            this.needItemList = RewardHelper.toRewardItemImmutableList(this.cost);
            return true;
        }catch (Exception e){
            HawkException.catchException(e);
            return false;
        }
    }

    @Override
    protected boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(cost);
        if (!valid) {
            throw new InvalidParameterException(String.format("XHJZShopCfg reward error, id: %s , cost: %s", id, cost));
        }
        valid = ConfigChecker.getDefaultChecker().checkAwardsValid(item);
        if (!valid) {
            throw new InvalidParameterException(String.format("XHJZShopCfg reward error, id: %s , item: %s", id, item));
        }
        return super.checkValid();
    }

    public int getId() {
        return id;
    }

    public String getCost() {
        return cost;
    }

    public String getItem() {
        return item;
    }

    public List<Reward.RewardItem.Builder> getNeedItemList() {
        return needItemList;
    }

    public List<Reward.RewardItem.Builder> getGainItemList() {
        return gainItemList;
    }

    public int getNumLimit() {
        return numLimit;
    }
}
