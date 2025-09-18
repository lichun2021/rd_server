package com.hawk.activity.type.impl.roseGift.cfg;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

@HawkConfigManager.KVResource(file = "activity/rose_gift/rose_gift_kv_cfg.xml")
public class RoseGiftKVCfg extends HawkConfigBase {

    private final int activityId;
    /**
     * 起服延迟开放时间
     */
    private final int serverDelay;
    /**
     * 花瓣id
     */
    private final int petal;
    /**
     * 玫瑰id
     */
    private final int rose;

    /**
     * 抽奖消耗
     */
    private final String cost;

    /**
     * 注水
     */
    private final String waterFlood;

    /**
     * 抽奖消耗
     */
    private List<Reward.RewardItem.Builder> costItemList;

    private RangeMap<Integer, Integer> rangeMap;

    public RoseGiftKVCfg(){
        activityId = 319;
        serverDelay = 0;
        petal = 0;
        rose = 0;
        cost = "";
        waterFlood = "";
    }

    /**
     * 解析配置
     * @return 解析结果
     */
    @Override
    protected boolean assemble() {
        try {
            this.costItemList = RewardHelper.toRewardItemImmutableList(this.cost);
            rangeMap = TreeRangeMap.create();
            for(String rangeStr : waterFlood.split(";")){
                String arr[] = rangeStr.split("_");
                if(arr.length != 3){
                    throw new RuntimeException("string to RangeMap error" + rangeStr);
                }
                rangeMap.put(Range.closed(Integer.parseInt(arr[0]), Integer.parseInt(arr[1])), Integer.parseInt(arr[2]));
            }
            return true;
        } catch (Exception arg1) {
            HawkException.catchException(arg1, new Object[0]);
            return false;
        }
    }

    @Override
    protected final boolean checkValid() {
        boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(cost);
        if (!valid) {
            throw new InvalidParameterException(String.format("RoseGiftKVCfg reward error, cost: %s", cost));
        }
        return super.checkValid();
    }

    public long getServerDelay() {
        return serverDelay * 1000l;
    }

    public int getPetal() {
        return petal;
    }

    public int getRose() {
        return rose;
    }

    public List<Reward.RewardItem.Builder> getCostItemList() {
        return costItemList;
    }

    public int getWaterFlood(int hour) {
        Integer i = rangeMap.get(hour);
        return i == null ? 0 : i;
    }
}
