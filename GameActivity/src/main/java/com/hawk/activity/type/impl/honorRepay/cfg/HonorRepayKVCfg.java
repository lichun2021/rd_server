package com.hawk.activity.type.impl.honorRepay.cfg;

import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/honor_repay/honor_repay_cfg.xml")
public class HonorRepayKVCfg extends HawkConfigBase {

    //# 服务器开服延时开启活动时间；单位:秒
    private final int serverDelay;
    // 每份返利道具购买消耗
    private final String repayCost;
    //每份返利道具购买立刻获得奖励
    private final String currentAward;
    //每份返利道具购买后每次返利获得奖励
    private final String repayAwardPerTime;
    //每份返利道具返利次数
    private final int repayTimes;
    //购买阶段开始及持续时间（以starttime做偏移）
    private final String purchaseTime;
    //返利4阶段时间（以starttime做偏移））
    private final String repayTimeRange;
    //购买限制次数
    private final int limitBuyTimes;

    //购买过期，道具兑换金条
    private final String convertedGold;
    private List<RewardItem.Builder> repayRewardList;

    private List<String> repayTimeRangeList;

    public HonorRepayKVCfg(){
        serverDelay = 0;
        repayCost = "";
        currentAward = "";
        repayAwardPerTime ="";
        repayTimes = 0;
        purchaseTime = "";
        repayTimeRange = "";
        limitBuyTimes = 0;
        convertedGold = "";
    }

    @Override
    protected boolean assemble() {
        try {
            repayRewardList = RewardHelper.toRewardItemList(repayAwardPerTime);
            repayTimeRangeList = SerializeHelper.stringToList(String.class, repayTimeRange, SerializeHelper.BETWEEN_ITEMS);
            if (repayTimeRangeList.size() > 4) {
                logger.error("HonorRepayKVCfg assemble faild  repayTimeRangeList.size() > 4 ");
                return false;
            }
            return true;
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }

    }

    public long getServerDelay() {
        return ((long)serverDelay) * 1000;
    }

    public String getRepayCost() {
        return repayCost;
    }

    public String getCurrentAward() {
        return currentAward;
    }

    public String getRepayAwardPerTime() {
        return repayAwardPerTime;
    }

    public int getRepayTimes() {
        return repayTimes;
    }

    public String getPurchaseTime() {
        return purchaseTime;
    }

    public String getRepayTimeRange() {
        return repayTimeRange;
    }

    public int getLimitBuyTimes() {
        return limitBuyTimes;
    }

    public List<RewardItem.Builder> getRepayRewardList() {
        return repayRewardList;
    }

    public List<String> getRepayTimeRangeList() {
        return repayTimeRangeList;
    }

    public String getConvertedGold() {
        return convertedGold;
    }
}
