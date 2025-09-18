package com.hawk.activity.type.impl.honorRepay.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 荣耀返利活动配置
 */
@HawkConfigManager.XmlResource(file = "activity/honor_repay/honor_repay_gift.xml")
public class HonorRepayGiftCfg extends HawkConfigBase {
    // 礼包ID
    @Id
    private final int giftId;

    private final String androidPayId;

    private final String iosPayId;

    private final String fixedReward;

    private final int times;

    private List<RewardItem.Builder> fixedRewardList;

    private static Map<String, Integer> payGiftIdMap = new HashMap<String, Integer>();

    public HonorRepayGiftCfg() {
        giftId = 0;
        androidPayId = "";
        iosPayId = "";
        fixedReward = "";
        times = 0;
    }

    @Override
    protected boolean assemble() {
        try {
            payGiftIdMap.put(androidPayId, giftId);
            payGiftIdMap.put(iosPayId, giftId);
            fixedRewardList = RewardHelper.toRewardItemList(fixedReward);
            return true;
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }

    }

    public List<RewardItem.Builder> getBuyAwardList() {
        return fixedRewardList;
    }

    public static Map<String, Integer> getPayGiftIdMap() {
        return payGiftIdMap;
    }


    public int getGiftId() {
        return giftId;
    }

    public String getFixedReward() {
        return fixedReward;
    }

    public String getAndroidPayId() {
        return androidPayId;
    }

    public String getIosPayId() {
        return iosPayId;
    }

    public int getTimes() {
        return times;
    }

    public static int getGiftId(String payGiftId) {
        if (!payGiftIdMap.containsKey(payGiftId)) {
            return 0;
        }
        return payGiftIdMap.get(payGiftId);
    }

}
