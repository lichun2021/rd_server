package com.hawk.game.config;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.hawk.game.util.GameUtil;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@HawkConfigManager.KVResource(file = "xml/xhjz_const.xml")
public class XHJZConstCfg extends HawkConfigBase {
    protected final int teamCommanderLimit;
    protected final int teamMemberLimit;
    protected final int teamPreparationLimit;
    protected final int teamNumLimit;
    protected final String warTimeHour;
    protected final int forceMoveBackTime;
    /**
     *回原服最小时间,
     */
    private final int minBackServerWaitTime;
    /**
     *回原服最大时间,
     */
    private final int maxBackServerWaitTime;
    protected final String teamNameNumLimit;
    protected final String createTeamCost;
    protected final int signRankLimit;
    protected final int serverDelay;

    protected final float recordPointParam;
    protected final String recordPointLimit;
    protected final int recordPointCount;
    protected final int matchParam;
    protected final String shopStartTime;
    protected final int shopRefreshTime;
    protected final String openServer;
    protected final int battleTime;

    protected final int preparationTime;
    protected final int playerMax;
    protected final int memberSignupLimit;
    private final String serverMatchOpenDays;

    private int warCount;
    private Map<Integer, Long> warTimeMap = new HashMap<>();
    private Set<String> openServerIdSet = new HashSet<>();
    private long shopStartTimeValue;
    private HawkTuple2<Integer, Integer> teamNameLimit = new HawkTuple2<>(1,8);
    private HawkTuple2<Float, Float> recordPointLimitParam = new HawkTuple2<>(-0.3f,0.3f);
    private RangeMap<Integer, Integer> serverMatchOpenDayRangeMap;

    public XHJZConstCfg() {
        this.teamCommanderLimit = 0;
        this.teamMemberLimit = 0;
        this.teamPreparationLimit = 0;
        this.teamNumLimit = 0;
        this.warTimeHour = "";
        this.forceMoveBackTime = 30000;
        this.minBackServerWaitTime = 1000;
        this.maxBackServerWaitTime = 5000;
        this.teamNameNumLimit = "";
        this.createTeamCost = "";
        this.signRankLimit = 0;
        this.serverDelay = 0;
        this.recordPointParam = 0;
        this.recordPointLimit = "";
        this.recordPointCount = 0;
        this.matchParam = 0;
        this.shopStartTime = "";
        this.shopRefreshTime = 0;
        this.openServer = "";
        this.battleTime = 2400;
        this.preparationTime = 300;
        this.playerMax = 30;
        this.memberSignupLimit = 0;
        this.serverMatchOpenDays = "1_90,91_365,366_999999";
    }

    @Override
    protected boolean assemble() {
        String [] split = warTimeHour.split("_");
        warCount = split.length;
        int i = 1;
        Map<Integer, Long> tmpWarTimeMap = new HashMap<>();
        for(String str : split){
            String [] time = str.split(":");
            tmpWarTimeMap.put(i, TimeUnit.HOURS.toMillis(Integer.parseInt(time[0])) + TimeUnit.MINUTES.toMillis(Integer.parseInt(time[1])));
            i++;
        }
        warTimeMap =tmpWarTimeMap;
        openServerIdSet = SerializeHelper.stringToSet(String.class, openServer, ",");
        this.shopStartTimeValue = HawkTime.parseTime(shopStartTime);
        String[] nameSplit = teamNameNumLimit.split("_");
        teamNameLimit = new HawkTuple2<>(Integer.parseInt(nameSplit[0]), Integer.parseInt(nameSplit[1]));
        String[] recordPointLimitSplit = recordPointLimit.split(";");
        recordPointLimitParam = new HawkTuple2<>(Float.parseFloat(recordPointLimitSplit[0]), Float.parseFloat(recordPointLimitSplit[1]));
        RangeMap<Integer, Integer> serverMatchOpenDayRangeMapTmp = TreeRangeMap.create();
        if(!HawkOSOperator.isEmptyString(this.serverMatchOpenDays)){
            String[] timeStrs = serverMatchOpenDays.split(",");
            int j = 0;
            for (String timeStr : timeStrs) {
                String[] strs = timeStr.split("_");
                int min = Integer.valueOf(strs[0]);
                int max = Integer.valueOf(strs[1]);
                serverMatchOpenDayRangeMapTmp.put(Range.closed(min, max), j);
                j++;
            }
        }
        this.serverMatchOpenDayRangeMap = serverMatchOpenDayRangeMapTmp;
        return super.assemble();
    }

    public int getTeamCommanderLimit() {
        return teamCommanderLimit;
    }

    public int getTeamMemberLimit() {
        return teamMemberLimit;
    }

    public int getTeamPreparationLimit() {
        return teamPreparationLimit;
    }

    public int getTeamNumLimit() {
        return teamNumLimit;
    }

    public String getCreateTeamCost() {
        return createTeamCost;
    }

    public int getWarCount() {
        return warCount;
    }

    public long getWarTime(int timeIndex){
        return warTimeMap.getOrDefault(timeIndex, -1L);
    }

    public int getForceMoveBackTime() {
        return forceMoveBackTime;
    }

    public int getMinBackServerWaitTime() {
        return minBackServerWaitTime;
    }

    public int getMaxBackServerWaitTime() {
        return maxBackServerWaitTime;
    }

    public Set<String> getOpenServerIdSet() {
        return openServerIdSet;
    }

    public long getServerDelay() {
        return serverDelay * 1000L;
    }

    public long getBattleTime() {
        return battleTime * 1000L;
    }

    public HawkTuple2<Integer, Integer> getTeamNameNumLimit() {
        return teamNameLimit;
    }

    public int getRecordPointCount() {
        return recordPointCount;
    }

    public long getPreparationTime() {
        return preparationTime * 1000L;
    }

    public int getSignRankLimit() {
        return signRankLimit;
    }

    public int getPlayerMax() {
        return playerMax;
    }

    public int getMemberSignupLimit() {
        return memberSignupLimit;
    }

    public long getShopStartTime() {
        return shopStartTimeValue;
    }

    public long getShopRefreshTime() {
        return TimeUnit.DAYS.toMillis(shopRefreshTime);
    }

    public int getMatchParam() {
        return matchParam;
    }

    public float getRecordPointParam() {
        return recordPointParam;
    }

    public HawkTuple2<Float, Float> getRecordPointLimitParam() {
        return recordPointLimitParam;
    }

    public int serverMatchOpenDayWeight(){
        int day = GameUtil.getServerOpenDay();
        Integer weight = serverMatchOpenDayRangeMap.get(day);
        if(weight == null){
            return 0;
        }else {
            return weight;
        }
    }
}
