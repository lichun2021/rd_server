package com.hawk.activity.type.impl.deepTreasure.entity;


import com.alibaba.fastjson.JSON;

public class DeepTreasureBuff {
    private int id;//持续次数
    private int times;//持续次数

    public static DeepTreasureBuff valueOf(int id, int times) {
        return new DeepTreasureBuff(id, times);
    }

    public DeepTreasureBuff(int id, int times) {
        this.id = id;
        this.times = times;
    }

    public DeepTreasureBuff() {
    }

    public void mergeFrom(String jsonStr) {
        DeepTreasureBuff result = JSON.parseObject(jsonStr, DeepTreasureBuff.class);
        this.id = result.getId();
        this.times = result.getTimes();
    }

    /**
     * 序列化
     */
    public String serialize() {
        return JSON.toJSONString(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getTimes() {
        return times;
    }
}
