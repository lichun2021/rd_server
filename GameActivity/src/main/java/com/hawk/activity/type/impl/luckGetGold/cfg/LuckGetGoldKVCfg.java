package com.hawk.activity.type.impl.luckGetGold.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@HawkConfigManager.KVResource(file = "activity/luck_get_gold/luck_get_gold_cfg.xml")
public class LuckGetGoldKVCfg extends HawkConfigBase {
    private final int activityId;
    /**
     * 起服延迟开放时间
     */
    private final int serverDelay;

    private final int freeCount;
    private final int threshold;

    private final String oneCost;
    private final String tenCost;

    private final int oneCostGold;
    private final int tenCostGold;

    private final String oneCostGet;
    private final String tenCostGet;
    //基地大于等于此等级才会开启此活动
    private final int baseLevel;
    //贵族大于等级此等级才会开启此互动
    private final int vipLevel;

    private final String calServer;
    private final int startGold;
    private final int dailyMax;


    private final long tickPeriod;
    private final int maxAction;

    private final long dailyStartTime;
    private final int dailyEndTime;

    private List<Reward.RewardItem.Builder> oneCostList;

    private List<Reward.RewardItem.Builder> tenCostList;

    private List<Reward.RewardItem.Builder> oneCostGetList;

    private List<Reward.RewardItem.Builder> tenCostGetList;
    private Map<String, String> calServerMap = new HashMap<>();

    public LuckGetGoldKVCfg(){
        activityId = 327;
        serverDelay = 0;
        freeCount = 0;
        threshold = 0;
        oneCost = "";
        tenCost = "";
        startGold = 0;
        oneCostGold = 0;
        tenCostGold = 0;
        oneCostGet = "";
        tenCostGet = "";
        baseLevel = 0;
        vipLevel = 8;
        calServer = "1_10001,2_20001";
        tickPeriod = 1000l;
        maxAction = 50;
        dailyMax = 1000;
        dailyStartTime = 10;
        dailyEndTime = 21;
    }

    @Override
    protected boolean assemble() {
        try {
            this.oneCostList = RewardHelper.toRewardItemImmutableList(this.oneCost);
            this.tenCostList = RewardHelper.toRewardItemImmutableList(this.tenCost);
            this.oneCostGetList = RewardHelper.toRewardItemImmutableList(this.oneCostGet);
            this.tenCostGetList = RewardHelper.toRewardItemImmutableList(this.tenCostGet);
            Map<String, String> calMap = new HashMap<>();
            if (!HawkOSOperator.isEmptyString(calServer)) {
                String[] areaStr = calServer.split(",");
                for (String str : areaStr) {
                    String[] serverStr = str.split("_");
                    calMap.put(serverStr[0], serverStr[1]);
                }
                this.calServerMap = calMap;
            }
            return true;
        } catch (Exception arg1) {
            HawkException.catchException(arg1, new Object[0]);
            return false;
        }
    }

    public long getServerDelay() {
        return serverDelay * 1000l;
    }

    public int getFreeCount() {
        return freeCount;
    }

    public int getThreshold() {
        return threshold;
    }

    public List<Reward.RewardItem.Builder> getOneCostList() {
        return oneCostList;
    }

    public List<Reward.RewardItem.Builder> getTenCostList() {
        return tenCostList;
    }

    public List<Reward.RewardItem.Builder> getOneCostGetList() {
        return oneCostGetList;
    }

    public List<Reward.RewardItem.Builder> getTenCostGetList() {
        return tenCostGetList;
    }

    public int getOneCostGold() {
        return oneCostGold;
    }

    public int getTenCostGold() {
        return tenCostGold;
    }

    public int getStartGold() {
        return startGold;
    }

    public int getBaseLevel() {
        return baseLevel;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public String getCalServer(String area){
        return calServerMap.getOrDefault(area,"");
    }

    public int getMaxAction() {
        return maxAction;
    }

    public long getTickPeriod() {
        return tickPeriod;
    }

    public int getDailyMax() {
        return dailyMax;
    }

    public long getDailyStartTime() {
        return dailyStartTime;
    }

    public int getDailyEndTime() {
        return dailyEndTime;
    }
}
