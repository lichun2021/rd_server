package com.hawk.game.module.lianmengyqzz.march.data.global;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import org.hawk.os.HawkOSOperator;
import org.hawk.redis.HawkRedisSession;
import org.hawk.tuple.HawkTuple2;
import redis.clients.jedis.Tuple;

import java.util.*;

public class YQZZSeasonServer implements IYQZZData {
    private static final String redisKey = "YQZZ_SEASON_SERVER";

    private static final String redisFinalRankKey = "YQZZ_SEASON_FINAL_RANK";

    private static final String redisGroupRankKey = "YQZZ_SEASON_GROUP_RANK";

    private String serverId;
    private String serverName = "";
    private String leaderName = "";

    private String senderId = "";
    private int season;
    private boolean isAdvance;

    private long score;
    private long totalPoint;
    private int lastRank;
    private int groupRank;
    private int kickoutRank;
    private List<Integer> reward = new ArrayList<>();

    private boolean isSeasonAward;

    private long power;

    public YQZZSeasonServer(){
        this.serverId = GsConfig.getInstance().getServerId();
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName == null ? "":serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getLeaderName() {
        return leaderName == null ? "":leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public boolean isAdvance() {
        return isAdvance;
    }

    public void setAdvance(boolean isAdvance) {
        this.isAdvance = isAdvance;
    }

    public void addScore(long score){
        this.score += score;
    }

    public long getScore() {
        return score;
    }

    public int getLastRank() {
        return lastRank;
    }

    public void setLastRank(int lastRank) {
        this.lastRank = lastRank;
    }

    public int getGroupRank() {
        return groupRank;
    }

    public void setGroupRank(int groupRank) {
        this.groupRank = groupRank;
    }

    public int getKickoutRank() {
        return kickoutRank;
    }

    public void setKickoutRank(int kickoutRank) {
        this.kickoutRank = kickoutRank;
    }

    public long getTotalPoint() {
        return totalPoint;
    }

    public void setTotalPoint(long totalPoint) {
        this.totalPoint = totalPoint;
    }

    public boolean isSeasonAward() {
        return isSeasonAward;
    }

    public void setSeasonAward(boolean seasonAward) {
        isSeasonAward = seasonAward;
    }

    public long getPower() {
        return power;
    }

    public void setPower(long power) {
        this.power = power;
    }

    public List<Integer> getReward() {
        return reward;
    }

    @Override
    public void saveRedis() {
        String key = redisKey  + ":" + this.season;
        RedisProxy.getInstance().getRedisSession().hSet(key, this.serverId, this.serializ(), YQZZConst.REDIS_DATA_EXPIRE_TIME);
        StatisManager.getInstance().incRedisKey(redisKey);
    }

    @Override
    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("serverId", this.serverId);
        obj.put("serverName", this.serverName);
        obj.put("leaderName", this.leaderName);
        obj.put("senderId", this.senderId == null ? "" : senderId);
        obj.put("season", this.season);
        obj.put("isAdvance",this.isAdvance);
        obj.put("score", this.score);
        obj.put("totalPoint", this.totalPoint);
        obj.put("lastRank", this.lastRank);
        obj.put("groupRank", this.groupRank);
        obj.put("kickoutRank", this.kickoutRank);
        obj.put("isSeasonAward", this.isSeasonAward);
        obj.put("power", this.power);
        JSONArray arr = new JSONArray();
        if(this.reward!= null && !this.reward.isEmpty()){
            for(int cfgId : this.reward){
                arr.add(cfgId);
            }
        }
        obj.put("reward", arr.toJSONString());
        return obj.toJSONString();
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        if(HawkOSOperator.isEmptyString(serialiedStr)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(serialiedStr);
        this.serverId = obj.getString("serverId");
        this.serverName = obj.getString("serverName");
        this.leaderName = obj.getString("leaderName");
        this.senderId = obj.getString("senderId");
        this.season = obj.getIntValue("season");
        this.isAdvance = obj.getBooleanValue("isAdvance");
        this.score = obj.getLongValue("score");
        this.totalPoint = obj.getLongValue("totalPoint");
        this.lastRank = obj.getIntValue("lastRank");
        this.groupRank = obj.getIntValue("groupRank");
        this.kickoutRank = obj.getIntValue("kickoutRank");
        this.isSeasonAward = obj.getBooleanValue("isSeasonAward");
        this.power = obj.getLongValue("power");
        if(obj.containsKey("reward")){
            String joinGuildsStr = obj.getString("reward");
            JSONArray arr = JSONArray.parseArray(joinGuildsStr);
            for(int i=0;i<arr.size();i++){
                int cfgId  = arr.getIntValue(i);
                this.reward.add(cfgId);
            }
        }
    }

    public static Map<String, YQZZSeasonServer> loadAll(int season){
        String key = redisKey  + ":" + season;
        Map<String, YQZZSeasonServer> rlt = new HashMap<>();
        Map<String,String> map = RedisProxy.getInstance().getRedisSession().hGetAll(key, YQZZConst.REDIS_DATA_EXPIRE_TIME);
        for(Map.Entry<String, String> entry : map.entrySet()){
            String value = entry.getValue();
            if(HawkOSOperator.isEmptyString(value)){
                continue;
            }
            YQZZSeasonServer seasonServer = new YQZZSeasonServer();
            seasonServer.mergeFrom(value);
            rlt.put(seasonServer.getServerId(), seasonServer);
        }
        return rlt;
    }

    public static YQZZSeasonServer loadByServerId(int season, String serverId){
        String key = redisKey  + ":" + season;

        String serverStr = RedisProxy.getInstance().getRedisSession().hGet(key, serverId);
        if(HawkOSOperator.isEmptyString(serverStr)){
            return null;
        }
        YQZZSeasonServer seasonServer = new YQZZSeasonServer();
        seasonServer.mergeFrom(serverStr);
        return seasonServer;
    }

    public static Map<String, YQZZSeasonServer> loadByServerIds(int season, List<String> serverIds){
        String key = redisKey  + ":" + season;
        Map<String, YQZZSeasonServer> rlt = new HashMap<>();
        if(serverIds == null || serverIds.isEmpty()){
            return rlt;
        }
        List<String> list = RedisProxy.getInstance().getRedisSession()
                .hmGet(key, serverIds.toArray(new String[serverIds.size()]));
        for(String str : list){
            if(HawkOSOperator.isEmptyString(str)){
               continue;
            }
            YQZZSeasonServer seasonServer = new YQZZSeasonServer();
            seasonServer.mergeFrom(str);
            rlt.put(seasonServer.getServerId(), seasonServer);
        }
        return rlt;
    }

    public static void updateFinalRank(int season, String serverId, int rank){
        HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
        redisSession.hSet(getFinalRankKey(season), serverId, String.valueOf(rank));
    }

    public static void updateGroupRank(int season, String serverId, int rank){
        HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
        redisSession.hSet(getGroupRankKey(season), serverId, String.valueOf(rank));
    }

    public static String getFinalRankKey(int season) {
        String key = redisFinalRankKey  + ":" + season;
        return key;
    }

    public static String getGroupRankKey(int season) {
        String key = redisGroupRankKey  + ":" + season;
        return key;
    }

    public static List<HawkTuple2<String, Integer>> getFinalRank(int season){
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        Map<String,String> map = redisSession.hGetAll(getFinalRankKey(season));
        List<HawkTuple2<String, Integer>> list = new ArrayList<>();
        for(Map.Entry<String, String> entry : map.entrySet()){
            list.add(new HawkTuple2<>(entry.getKey(), Integer.parseInt(entry.getValue())));
        }
        Collections.sort(list, new Comparator<HawkTuple2<String, Integer>>(){
            @Override
            public int compare(HawkTuple2<String, Integer> o1, HawkTuple2<String, Integer> o2) {
                if(o1.second != o2.second){
                    return o1.second < o2.second ? -1 : 1;
                }
                return 0;
            }
        });
        return list;
    }

    public static List<HawkTuple2<String, Integer>> getGroupRank(int season){
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        Map<String,String> map = redisSession.hGetAll(getGroupRankKey(season));
        List<HawkTuple2<String, Integer>> list = new ArrayList<>();
        for(Map.Entry<String, String> entry : map.entrySet()){
            list.add(new HawkTuple2<>(entry.getKey(), Integer.parseInt(entry.getValue())));
        }
        Collections.sort(list, new Comparator<HawkTuple2<String, Integer>>(){
            @Override
            public int compare(HawkTuple2<String, Integer> o1, HawkTuple2<String, Integer> o2) {
                if(o1.second != o2.second){
                    return o1.second < o2.second ? -1 : 1;
                }
                return 0;
            }
        });
        return list;
    }

    public static int getFinalSelfRank(int season, String serverId){
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        String rankStr = redisSession.hGet(getFinalRankKey(season), serverId);
        if(HawkOSOperator.isEmptyString(rankStr)){
            return -1;
        }
        return Integer.parseInt(rankStr);
    }

    public static int getGroupSelfRank(int season, String serverId){
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        String rankStr = redisSession.hGet(getGroupRankKey(season), serverId);
        if(HawkOSOperator.isEmptyString(rankStr)){
            return -1;
        }
        return Integer.parseInt(rankStr);
    }
}
