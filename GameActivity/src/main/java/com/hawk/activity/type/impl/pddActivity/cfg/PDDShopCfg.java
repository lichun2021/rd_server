package com.hawk.activity.type.impl.pddActivity.cfg;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@HawkConfigManager.XmlResource(file = "activity/pdd/pdd_shop.xml")
public class PDDShopCfg extends HawkConfigBase {
    @Id
    private final int id;

    private final int day;

    /** 单独购买每份消耗物品 */
    private final String aloneNeedItem;

    /** 每份消耗物品 */
    private final String needItem;

    /** 每份获得物品 */
    private final String gainItem;

    /** 最大可兑换次数 */
    private final int times;

    private final int endTime;

    private final String successTime;
    private final String waterFloodWx;
    private final String waterFloodQq;

    /** 每份获得物品 */
    private List<Reward.RewardItem.Builder> gainItemList;

    /** 每份消耗物品 */
    private List<Reward.RewardItem.Builder> needItemList;

    /** 单独购买每份消耗物品 */
    private List<Reward.RewardItem.Builder> aloneNeedItemList;

    private RangeMap<Integer, HawkTuple2<Long, Integer>> waterFloodWxMap;

    private RangeMap<Integer, HawkTuple2<Long, Integer>> waterFloodQqMap;

    public PDDShopCfg(){
        id = 0;
        day = 1;
        aloneNeedItem = "";
        needItem = "";
        gainItem = "";
        times = 0;
        endTime = 0;
        successTime = "";
        waterFloodWx = "";
        waterFloodQq = "";
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
            this.aloneNeedItemList = RewardHelper.toRewardItemImmutableList(this.aloneNeedItem);
            waterFloodWxMap = TreeRangeMap.create();
            for(String rangeStr : waterFloodWx.split(";")){
                String arr[] = rangeStr.split("_");
                if(arr.length != 4){
                    throw new RuntimeException("string to RangeMap error" + rangeStr);
                }
                waterFloodWxMap.put(Range.closed(Integer.parseInt(arr[0]), Integer.parseInt(arr[1])), new HawkTuple2<>(Long.parseLong(arr[2]) * 1000l,Integer.parseInt(arr[3])));
            }
            waterFloodQqMap = TreeRangeMap.create();
            for(String rangeStr : waterFloodQq.split(";")){
                String arr[] = rangeStr.split("_");
                if(arr.length != 4){
                    throw new RuntimeException("string to RangeMap error" + rangeStr);
                }
                waterFloodQqMap.put(Range.closed(Integer.parseInt(arr[0]), Integer.parseInt(arr[1])), new HawkTuple2<>(Long.parseLong(arr[2]) * 1000l,Integer.parseInt(arr[3])));
            }
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
            throw new InvalidParameterException(String.format("PDDShopCfg reward error, id: %s , needItem: %s", id, needItem));
        }
        valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("PDDShopCfg reward error, id: %s , gainItem: %s", id, gainItem));
        }
        valid = ConfigChecker.getDefaultChecker().checkAwardsValid(aloneNeedItem);
        if (!valid) {
            throw new InvalidParameterException(String.format("PDDShopCfg reward error, id: %s , gainItem: %s", id, aloneNeedItem));
        }
        return super.checkValid();
    }

    public int getId() {
        return id;
    }

    public int getDay() {
        return day;
    }

    public long getEndTime() {
        return endTime * 1000l;
    }

    public int getTimes() {
        return times;
    }

    public List<Reward.RewardItem.Builder> getAloneNeedItemList() {
        return aloneNeedItemList;
    }

    public List<Reward.RewardItem.Builder> getNeedItemList() {
        return needItemList;
    }

    public List<Reward.RewardItem.Builder> getGainItemList() {
        return gainItemList;
    }

    public long getSuccessTime(){
        try {
            String [] successArr = successTime.split("_");
            int min = Integer.parseInt(successArr[0]);
            int max = Integer.parseInt(successArr[1]);
            int success = HawkRand.randInt(min, max);
            return success * 1000l;
        }catch (Exception e){
            HawkException.catchException(e);
            return getEndTime();
        }
    }

    public HawkTuple2<Long, Integer> getWaterFlood(int areaId, int hour) {
        //1微信 2手Q
        if(areaId == 1){
            HawkTuple2<Long, Integer> i = waterFloodWxMap.get(hour);
            return i == null ? new HawkTuple2<>(600000l, 5) : i;
        }else {
            HawkTuple2<Long, Integer> i = waterFloodQqMap.get(hour);
            return i == null ? new HawkTuple2<>(600000l, 5) : i;
        }

    }
}
