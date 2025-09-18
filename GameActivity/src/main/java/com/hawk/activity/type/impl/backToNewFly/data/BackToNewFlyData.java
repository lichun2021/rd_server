package com.hawk.activity.type.impl.backToNewFly.data;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.redis.ActivityGlobalRedis;
import org.hawk.os.HawkOSOperator;

public class BackToNewFlyData {
    public static final String redisKey = "BACK_TO_NEW_FLY";

    private String openId = "";
    private String playerId = "";
    private int backCount = 0;

    private int baseLevel = 0;
    private long startTime = 0;
    private long overTime = 0;
    private long updateTime = 0;

    public BackToNewFlyData(){

    }

    public BackToNewFlyData(String openId, String playerId, int backCount, int baseLevel, long startTime, long overTime, long updateTime) {
        this.openId = openId;
        this.playerId = playerId;
        this.backCount = backCount;
        this.baseLevel = baseLevel;
        this.startTime = startTime;
        this.overTime = overTime;
        this.updateTime = updateTime;
    }

    public String getOpenId() {
        return openId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getBackCount() {
        return backCount;
    }

    public int getBaseLevel() {
        return baseLevel;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getOverTime() {
        return overTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("openId", this.openId);
        obj.put("playerId", this.playerId);
        obj.put("backCount", this.backCount);
        obj.put("baseLevel", this.baseLevel);
        obj.put("startTime", this.startTime);
        obj.put("overTime", this.overTime);
        obj.put("updateTime", this.updateTime);
        return obj.toJSONString();
    }

    public void mergeFrom(String serialiedStr) {
        if (HawkOSOperator.isEmptyString(serialiedStr)) {
            return;
        }
        JSONObject obj = JSONObject.parseObject(serialiedStr);
        this.openId = obj.getString("openId");
        this.playerId = obj.getString("playerId");
        this.backCount = obj.getIntValue("backCount");
        this.baseLevel = obj.getIntValue("baseLevel");
        this.startTime = obj.getLongValue("startTime");
        this.overTime = obj.getLongValue("overTime");
        this.updateTime = obj.getLongValue("updateTime");
    }

    public void save(){
        ActivityGlobalRedis.getInstance().set(redisKey + ":" + openId, serializ());
    }

    public static BackToNewFlyData load(String openId){
        String serialiedStr = ActivityGlobalRedis.getInstance().get(redisKey + ":" + openId);
        if (HawkOSOperator.isEmptyString(serialiedStr)) {
            return null;
        }
        BackToNewFlyData data = new BackToNewFlyData();
        data.mergeFrom(serialiedStr);
        return data;
    }
}
