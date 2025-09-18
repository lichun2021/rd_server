package com.hawk.activity.type.impl.recallFriend;

import com.alibaba.fastjson.JSONObject;

/**
 * 召回信息对象
 * @author hf
 */
public class RecallInfo {

    /**
     * 召回的状态
     */
    private int state;
    /**
     * 主堡等级
     */
    private int facLv;

    public static RecallInfo valueOf(int state, int facLv) {
        RecallInfo recallInfo = new RecallInfo();
        recallInfo.setState(state);
        recallInfo.setFacLv(facLv);
        return recallInfo;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getFacLv() {
        return facLv;
    }

    public void setFacLv(int facLv) {
        this.facLv = facLv;
    }

    public String serialize() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("state", this.getState());
        jsonObject.put("facLv", this.getFacLv());
        return jsonObject.toJSONString();
    }

    public void mergeFrom(String info) {
        JSONObject jsonObject = JSONObject.parseObject(info);
        if (jsonObject.containsKey("state")){
            this.state = jsonObject.getIntValue("state");
        }
        if (jsonObject.containsKey("facLv")){
            this.facLv = jsonObject.getIntValue("facLv");
        }
    }
}
