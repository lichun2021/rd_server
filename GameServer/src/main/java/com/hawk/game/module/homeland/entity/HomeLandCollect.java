package com.hawk.game.module.homeland.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;

public class HomeLandCollect implements SerializJsonStrAble {
    private int buildType;       //配置ID，对应静态配置表

    public static HomeLandCollect valueOf(int buildType) {
        return new HomeLandCollect(buildType);
    }

    public HomeLandCollect(int buildType) {
        this.buildType = buildType;
    }

    public HomeLandCollect() {
    }

    @Override
    public void mergeFrom(String jsonStr) {
        HomeLandCollect result = JSON.parseObject(jsonStr, HomeLandCollect.class);
        this.setBuildType(result.getBuildType());
    }

    @Override
    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("buildType", this.getBuildType());
        return obj.toString();
    }

    public int getBuildType() {
        return buildType;
    }

    public void setBuildType(int buildType) {
        this.buildType = buildType;
    }
}
