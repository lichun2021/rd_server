package com.hawk.activity.type.impl.deepTreasure.entity;

import com.alibaba.fastjson.JSON;

public class DeepTreasureBox {
    private int poolCfgId;
    private boolean open; // 是否打开

    public int getPoolCfgId() {
        return poolCfgId;
    }

    public void setPoolCfgId(int poolCfgId) {
        this.poolCfgId = poolCfgId;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }


    /**
     * 序列化
     */
    public String serialize() {
        return JSON.toJSONString(this);
    }

    public void mergeFrom(String jsonStr) {
        DeepTreasureBox result = JSON.parseObject(jsonStr, DeepTreasureBox.class);
        this.poolCfgId = result.getPoolCfgId();
        this.open = result.isOpen();
    }


}
