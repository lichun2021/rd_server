package com.hawk.game.module.lianmengyqzz.march.data.global;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import com.hawk.game.protocol.YQZZWar;
import com.hawk.gamelib.rank.RankScoreHelper;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.tuple.HawkTuple2;
import redis.clients.jedis.Tuple;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class YQZZSeasonGuild implements IYQZZData {
    private static final String redisKey = "YQZZ_SEASON_GUILD";
    private static final String redisRankKey = "YQZZ_SEASON_GUILD_RANK";

    private static long lastTickTime = 0;

    private static List<YQZZWar.PBYQZZLeagueWarGuildInfo> lastRankPb = new ArrayList<>();

    private String guildId;
    private String serverId;
    private String guildName;
    private String guildTag;
    private int guildFlag;
    private int season;
    private long totalPoint;

    private long kickoutPoint;

    private boolean isSeasonAward;

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getGuildName() {
        return guildName;
    }

    public void setGuildName(String guildName) {
        this.guildName = guildName;
    }

    public String getGuildTag() {
        return guildTag;
    }

    public void setGuildTag(String guildTag) {
        this.guildTag = guildTag;
    }

    public int getGuildFlag() {
        return guildFlag;
    }

    public void setGuildFlag(int guildFlag) {
        this.guildFlag = guildFlag;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    private List<Integer> reward = new ArrayList<>();

    public long getTotalPoint() {
        return totalPoint;
    }

    public void setTotalPoint(long totalPoint) {
        this.totalPoint = totalPoint;
    }

    public long getKickoutPoint() {
        return kickoutPoint;
    }

    public void setKickoutPoint(long kickoutPoint) {
        this.kickoutPoint = kickoutPoint;
    }

    public boolean isSeasonAward() {
        return isSeasonAward;
    }

    public void setSeasonAward(boolean seasonAward) {
        isSeasonAward = seasonAward;
    }

    public List<Integer> getReward() {
        return reward;
    }

    @Override
    public void saveRedis() {
        String key = redisKey  + ":" + this.season;
        RedisProxy.getInstance().getRedisSession().hSet(key, this.guildId, this.serializ(), YQZZConst.REDIS_DATA_EXPIRE_TIME);
        StatisManager.getInstance().incRedisKey(redisKey);
        if(getRankScore()>0){
            updateRank();
        }
    }

    @Override
    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("guildId", this.guildId);
        obj.put("serverId", this.serverId);
        obj.put("guildName", this.guildName);
        obj.put("guildTag", this.guildTag);
        obj.put("guildFlag", this.guildFlag);
        obj.put("season", this.season);
        obj.put("totalPoint", this.totalPoint);
        obj.put("kickoutPoint", this.kickoutPoint);
        obj.put("isSeasonAward", this.isSeasonAward);
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
        this.guildId = obj.getString("guildId");
        this.serverId = obj.getString("serverId");
        this.guildName = obj.getString("guildName");
        this.guildTag = obj.getString("guildTag");
        this.guildFlag = obj.getIntValue("guildFlag");
        this.season = obj.getIntValue("season");
        this.totalPoint = obj.getLongValue("totalPoint");
        this.kickoutPoint = obj.getLongValue("kickoutPoint");
        this.isSeasonAward = obj.getBooleanValue("isSeasonAward");
//        if(obj.containsKey("reward")){
//            String joinGuildsStr = obj.getString("reward");
//            JSONArray arr = JSONArray.parseArray(joinGuildsStr);
//            for(int i=0;i<arr.size();i++){
//                int cfgId  = arr.getIntValue(i);
//                this.reward.add(cfgId);
//            }
//        }
    }

    public static List<YQZZSeasonGuild> getRankList(int season){
        List<YQZZSeasonGuild> guilds = new ArrayList<>();
        Set<Tuple> rankSet = getRankSet(season);
        List<String> guildIds = new ArrayList<>();
        for (Tuple rank : rankSet) {
            guildIds.add(rank.getElement());
        }
        Map<String, YQZZSeasonGuild> guildMap = loadByGuildIds(season, guildIds);
        for (Tuple rank : rankSet) {
            YQZZSeasonGuild guild = guildMap.get(rank.getElement());
            guilds.add(guild);
        }
        return guilds;

    }

    public static List<YQZZWar.PBYQZZLeagueWarGuildInfo> getRankPBList(int season){
        long now = HawkTime.getMillisecond();
        if(now - lastTickTime < TimeUnit.MINUTES.toMinutes(1)){
            return lastRankPb;
        }
        lastTickTime = now;
        List<YQZZWar.PBYQZZLeagueWarGuildInfo> guilds = new ArrayList<>();
        Set<Tuple> rankSet = getRankSet(season);
        List<String> guildIds = new ArrayList<>();
        for (Tuple rank : rankSet) {
            guildIds.add(rank.getElement());
        }
        Map<String, YQZZSeasonGuild> guildMap = loadByGuildIds(season, guildIds);
        int rank = 1;
        for (Tuple rankData : rankSet) {
            YQZZSeasonGuild guild = guildMap.get(rankData.getElement());
            YQZZWar.PBYQZZLeagueWarGuildInfo.Builder info = guild.toRankPB(rank, RankScoreHelper.getRealScore((long) rankData.getScore()));
            guilds.add(info.build());
            rank++;
        }
        lastRankPb = guilds;
        return lastRankPb;
    }

    public YQZZWar.PBYQZZLeagueWarGuildInfo.Builder toRankPB(int rank, long score){
        YQZZWar.PBYQZZLeagueWarGuildInfo.Builder guildInfo = YQZZWar.PBYQZZLeagueWarGuildInfo.newBuilder();
        guildInfo.setServerId(serverId);
        guildInfo.setRank(rank);
        guildInfo.setGuildName(guildName);
        guildInfo.setGuildLeader("");
        guildInfo.setGuildFlag(guildFlag);
        guildInfo.setGuildTag(guildTag);
        guildInfo.setScore(score);
        return guildInfo;
    }

    public YQZZWar.PBYQZZLeagueWarGuildInfo.Builder getSelfRankPB(){
        RedisIndex index = ActivityGlobalRedis.getInstance().zrevrankAndScore(getRankKey(this.season), guildId);
        int rank = -1;
        long score = 0;
        if (index != null) {
            rank = index.getIndex().intValue() + 1;
            score = RankScoreHelper.getRealScore(index.getScore().longValue());
        }
        return toRankPB(rank, score);
    }

    public static HawkTuple2<Integer, Long> getSelfRank(int season, String guildId){
        RedisIndex index = ActivityGlobalRedis.getInstance().zrevrankAndScore(getRankKey(season), guildId);
        int rank = -1;
        long score = 0;
        if (index != null) {
            rank = index.getIndex().intValue() + 1;
            score = RankScoreHelper.getRealScore(index.getScore().longValue());
        }
        return new HawkTuple2<>(rank, score);
    }

    public static YQZZSeasonGuild loadByGuildId(int season, String guildId){
        String key = redisKey  + ":" + season;
        String guildStr = RedisProxy.getInstance().getRedisSession().hGet(key, guildId);
        if(HawkOSOperator.isEmptyString(guildStr)){
            return null;
        }
        YQZZSeasonGuild guild = new YQZZSeasonGuild();
        guild.mergeFrom(guildStr);
        return guild;
    }

    public static Map<String, YQZZSeasonGuild> loadByGuildIds(int season, List<String> guildIds){
        String key = redisKey  + ":" + season;
        Map<String, YQZZSeasonGuild> rlt = new HashMap<>();
        if(guildIds == null || guildIds.isEmpty()){
            return rlt;
        }
        List<String> list = RedisProxy.getInstance().getRedisSession()
                .hmGet(key, guildIds.toArray(new String[guildIds.size()]));
        for(String str : list){
            if(HawkOSOperator.isEmptyString(str)){
                continue;
            }
            YQZZSeasonGuild seasonGuild = new YQZZSeasonGuild();
            seasonGuild.mergeFrom(str);
            rlt.put(seasonGuild.getGuildId(), seasonGuild);
        }
        return rlt;
    }

    private void updateRank(){
        HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
        long rankScore = RankScoreHelper.calcSpecialRankScore(getRankScore());
        redisSession.zAdd(getRankKey(this.season), rankScore, getRankId());
    }

    public static Set<Tuple> getRankSet(int season){
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        Set<Tuple> rankSet = redisSession.zRevrangeWithScores(getRankKey(season), 0, Math.max((getRankSize() - 1), 0), 0);
        return rankSet;
    }


    public static String getRankKey(int season) {
        String key = redisRankKey  + ":" + season;
        return key;
    }

    public long getRankScore() {
        return kickoutPoint;
    }

    public String getRankId() {
        return guildId;
    }

    public static int getRankSize() {
        return 120;
    }
}
