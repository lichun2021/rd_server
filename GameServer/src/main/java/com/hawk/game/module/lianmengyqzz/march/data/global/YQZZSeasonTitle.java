package com.hawk.game.module.lianmengyqzz.march.data.global;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import org.hawk.os.HawkOSOperator;

import java.util.HashMap;
import java.util.Map;

public class YQZZSeasonTitle implements IYQZZData {
    private static final String redisKey = "YQZZ_SEASON_TITLE";

    private String serverId;

    private int rank;

    private int nationHonor;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getNationHonor() {
        return nationHonor;
    }

    public void setNationHonor(int nationHonor) {
        this.nationHonor = nationHonor;
    }


    @Override
    public void saveRedis() {
        RedisProxy.getInstance().getRedisSession().hSet(redisKey, this.serverId, this.serializ());
        StatisManager.getInstance().incRedisKey(redisKey);
    }

    @Override
    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("serverId", this.serverId);
        obj.put("rank", this.rank);
        obj.put("nationHonor", this.nationHonor);
        return obj.toJSONString();
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        if(HawkOSOperator.isEmptyString(serialiedStr)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(serialiedStr);
        this.serverId = obj.getString("serverId");
        this.rank = obj.getIntValue("rank");
        this.nationHonor = obj.getIntValue("nationHonor");
    }

    public static Map<String, YQZZSeasonTitle> loadAll(){
        Map<String, YQZZSeasonTitle> rlt = new HashMap<>();
        Map<String,String> map = RedisProxy.getInstance().getRedisSession().hGetAll(redisKey);
        for(Map.Entry<String, String> entry : map.entrySet()) {
            String value = entry.getValue();
            if (HawkOSOperator.isEmptyString(value)) {
                continue;
            }
            YQZZSeasonTitle title = new YQZZSeasonTitle();
            title.mergeFrom(value);
            rlt.put(title.getServerId(), title);
        }
        return rlt;
    }

    public static void delAll(){
        RedisProxy.getInstance().getRedisSession().del(redisKey);
    }
}
