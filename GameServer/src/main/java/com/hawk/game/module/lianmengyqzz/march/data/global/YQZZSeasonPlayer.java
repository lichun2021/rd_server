package com.hawk.game.module.lianmengyqzz.march.data.global;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import org.hawk.os.HawkOSOperator;

import java.util.ArrayList;
import java.util.List;

public class YQZZSeasonPlayer implements IYQZZData {
    private static final String redisKey = "YQZZ_SEASON_PLAYER";
    private String playerId;
    private String serverId;
    private int season;
    private long totalPoint;
    private List<Integer> reward = new ArrayList<>();

    private List<Integer> guildReward = new ArrayList<>();

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public long getTotalPoint() {
        return totalPoint;
    }

    public void setTotalPoint(long totalPoint) {
        this.totalPoint = totalPoint;
    }

    public List<Integer> getReward() {
        return reward;
    }

    public List<Integer> getGuildReward() {
        return guildReward;
    }

    @Override
    public void saveRedis() {
        String key = redisKey  + ":" + this.season;
        RedisProxy.getInstance().getRedisSession().hSet(key, this.playerId, this.serializ(), YQZZConst.REDIS_DATA_EXPIRE_TIME);
        StatisManager.getInstance().incRedisKey(redisKey);
    }

    @Override
    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("playerId", this.playerId);
        obj.put("serverId", this.serverId);
        obj.put("season", this.season);
        obj.put("totalPoint", this.totalPoint);
        JSONArray arr = new JSONArray();
        if(this.reward!= null && !this.reward.isEmpty()){
            for(int cfgId : this.reward){
                arr.add(cfgId);
            }
        }
        obj.put("reward", arr.toJSONString());

        JSONArray guildArr = new JSONArray();
        if(this.guildReward!= null && !this.guildReward.isEmpty()){
            for(int cfgId : this.guildReward){
                guildArr.add(cfgId);
            }
        }
        obj.put("guildReward", guildArr.toJSONString());
        return obj.toJSONString();
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        if(HawkOSOperator.isEmptyString(serialiedStr)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(serialiedStr);
        this.playerId = obj.getString("playerId");
        this.serverId = obj.getString("serverId");
        this.season = obj.getIntValue("season");
        this.totalPoint = obj.getLongValue("totalPoint");
        if(obj.containsKey("reward")){
            String joinGuildsStr = obj.getString("reward");
            JSONArray arr = JSONArray.parseArray(joinGuildsStr);
            for(int i=0;i<arr.size();i++){
                int cfgId  = arr.getIntValue(i);
                this.reward.add(cfgId);
            }
        }
        if(obj.containsKey("guildReward")){
            String guildRewardStr = obj.getString("guildReward");
            JSONArray arr = JSONArray.parseArray(guildRewardStr);
            for(int i=0;i<arr.size();i++){
                int cfgId  = arr.getIntValue(i);
                this.guildReward.add(cfgId);
            }
        }
    }

    public static YQZZSeasonPlayer loadByPlayerId(int season, String playerId){
        String key = redisKey  + ":" + season;

        String playerStr = RedisProxy.getInstance().getRedisSession().hGet(key, playerId);
        if(HawkOSOperator.isEmptyString(playerStr)){
            return null;
        }
        YQZZSeasonPlayer player = new YQZZSeasonPlayer();
        player.mergeFrom(playerStr);
        return player;
    }
}
