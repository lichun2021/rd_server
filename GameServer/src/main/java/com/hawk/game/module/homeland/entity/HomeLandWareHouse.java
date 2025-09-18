package com.hawk.game.module.homeland.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;

public class HomeLandWareHouse implements SerializJsonStrAble {
    private int cfgId;       //配置ID，对应静态配置表
    private int count;      //数量
    private long updateTime;      //更新时间

    public HomeLandWareHouse(int cfgId) {
        this.cfgId = cfgId;
    }

    public HomeLandWareHouse() {
    }

    /**
     * 序列化
     */
    @Override
    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("cfgId", this.getCfgId());
        obj.put("count", this.getCount());
        obj.put("updateTime", this.getUpdateTime());
        return obj.toString();
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        HomeLandWareHouse result = JSON.parseObject(serialiedStr, HomeLandWareHouse.class);
        this.setCfgId(result.getCfgId());
        this.setUpdateTime(result.getUpdateTime());
        this.setCount(result.getCount());
    }

    public int getCfgId() {
        return cfgId;
    }

    public void setCfgId(int cfgId) {
        this.cfgId = cfgId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
