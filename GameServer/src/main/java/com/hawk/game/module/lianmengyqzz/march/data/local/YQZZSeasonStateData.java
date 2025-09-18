package com.hawk.game.module.lianmengyqzz.march.data.local;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import org.hawk.os.HawkOSOperator;

public class YQZZSeasonStateData implements IYQZZData {
    private static final String redisKey = "YQZZ_SEASON_STATE_DATA";

    private String serverId;
    private int season;
    private YQZZConst.YQZZSeasonState state;

    public YQZZSeasonStateData(){
        this.serverId = GsConfig.getInstance().getServerId();
        this.state = YQZZConst.YQZZSeasonState.HIDDEN;
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

    public YQZZConst.YQZZSeasonState getState() {
        return state;
    }

    public void setState(YQZZConst.YQZZSeasonState state) {
        this.state = state;
    }

    public static YQZZSeasonStateData loadData(String serverId){
        String key = redisKey  + ":" + serverId;
        String dataStr = RedisProxy.getInstance().getRedisSession().getString(key);
        StatisManager.getInstance().incRedisKey(redisKey);
        if(HawkOSOperator.isEmptyString(dataStr)){
            return null;
        }
        YQZZSeasonStateData data = new YQZZSeasonStateData();
        data.mergeFrom(dataStr);
        return data;
    }

    @Override
    public void saveRedis() {
        String key = redisKey  + ":" + this.serverId;
        RedisProxy.getInstance().getRedisSession().setString(key, this.serializ());
        StatisManager.getInstance().incRedisKey(redisKey);
    }

    @Override
    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("serverId", this.serverId);
        obj.put("season", this.season);
        obj.put("state", this.state.getValue());
        return obj.toString();
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        if (HawkOSOperator.isEmptyString(serialiedStr)) {
            return;
        }
        JSONObject obj = JSONObject.parseObject(serialiedStr);
        if(obj.containsKey("serverId")){
            this.serverId = obj.getString("serverId");
        }
        if(obj.containsKey("season")){
            this.season = obj.getIntValue("season");
        }
        if(obj.containsKey("state")){
            this.state = YQZZConst.YQZZSeasonState.valueOf(obj.getIntValue("state"));
        }
    }


    public void enter(YQZZSeasonStateData calDate){
        switch (state){
            case HIDDEN:{
                YQZZMatchService.getInstance().onSeasonEnd();
            }
            break;
            case OPEN:{
                season = calDate.season;
                YQZZMatchService.getInstance().onSeasonStart();
            }
            break;
            case REWARD:{
                YQZZMatchService.getInstance().onSeasonReward();
            }
            break;
        }
        saveRedis();
    }

    public void next(YQZZSeasonStateData calDate){
        switch (state){
            case HIDDEN:{
                state = YQZZConst.YQZZSeasonState.OPEN;
            }
            break;
            case OPEN:{
                state = YQZZConst.YQZZSeasonState.REWARD;
            }
            break;
            case REWARD:{
                state = YQZZConst.YQZZSeasonState.HIDDEN;
            }
            break;
        }
        enter(calDate);
    }
}
