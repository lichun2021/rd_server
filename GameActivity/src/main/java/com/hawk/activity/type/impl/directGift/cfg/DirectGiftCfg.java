package com.hawk.activity.type.impl.directGift.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.HashMap;
import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/direct_gift/direct_gift.xml")
public class DirectGiftCfg extends HawkConfigBase {

    @Id
    private final int id;

    private final int playerType;

    private final int limit;
    /**
     * ios商品id
     */
    private final String iosPayId;

    /**
     * android商品id
     */
    private final String androidPayId;

    /**
     * 奖品
     */
    private final String rewards;

    private List<Reward.RewardItem.Builder> rewardList;

    private final static HashMap<String, DirectGiftCfg> configMap = new HashMap<>();

    public DirectGiftCfg() {
        id = 0;
        playerType = 0;
        limit = 0;
        iosPayId = "";
        androidPayId = "";
        rewards = "";
    }

    public int getId() {
        return id;
    }

    public int getPlayerType() {
        return playerType;
    }

    public int getLimit() {
        return limit;
    }

    public String getIosPayId() {
        return iosPayId;
    }

    public String getAndroidPayID() {
        return androidPayId;
    }

    public String getRewards() {
        return rewards;
    }

    @Override
    protected boolean assemble() {
        rewardList = RewardHelper.toRewardItemImmutableList(rewards);
        configMap.put(androidPayId, this);
        configMap.put(iosPayId, this);
        return true;
    }

    public List<Reward.RewardItem.Builder> getRewardList() {
        return rewardList;
    }

    public static HashMap<String, DirectGiftCfg> getConfigMap() {
        return configMap;
    }

    public static DirectGiftCfg getConfigBuyGoodsId(String goodsId) {
        return configMap.get(goodsId);
    }
    
    public String getPayGiftIdByChannel(String channel) {
    	return "android".equals(channel) ? androidPayId : iosPayId;
    }
}
