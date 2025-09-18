package com.hawk.game.module.homeland.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;

public class HomeLandDailyLikes implements SerializJsonStrAble {
    private String playerId;
    private long updateTime;

    public static HomeLandDailyLikes valueOf(String playerId) {
        return new HomeLandDailyLikes(playerId);
    }

    public HomeLandDailyLikes(String playerId) {
        this.playerId = playerId;
    }

    public HomeLandDailyLikes() {
    }

    /**
     * 序列化
     */
    @Override
    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("playerId", this.getPlayerId());
        obj.put("updateTime", this.getUpdateTime());
        return obj.toString();
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        HomeLandDailyLikes result = JSON.parseObject(serialiedStr, HomeLandDailyLikes.class);
        this.setUpdateTime(result.getUpdateTime());
        this.setPlayerId(result.getPlayerId());
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
