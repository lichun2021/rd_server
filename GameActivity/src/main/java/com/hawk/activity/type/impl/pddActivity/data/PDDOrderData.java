package com.hawk.activity.type.impl.pddActivity.data;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.Activity;
import org.hawk.os.HawkOSOperator;

public class PDDOrderData {
    private String playerId = "";
    private String orderId = "";
    private int termId = 0;
    private int cfgId = 0;
    private int state = 0;

    private int type = 0;

    private boolean isGet = false;
    private long creatTime = 0;
    private long endTime = 0;
    private long buyTime = 0;

    private long cancelTime = 0;
    private long successTime = 0;
    private String partnerId = "";

    private boolean isSend = false;

    public PDDOrderData(){

    }

    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("playerId", this.playerId);
        obj.put("orderId", this.orderId);
        obj.put("termId", this.termId);
        obj.put("cfgId", this.cfgId);
        obj.put("state", this.state);
        obj.put("type", this.type);
        obj.put("isGet", this.isGet);
        obj.put("creatTime", this.creatTime);
        obj.put("endTime", this.endTime);
        obj.put("buyTime", this.buyTime);
        obj.put("cancelTime", this.cancelTime);
        obj.put("successTime", this.successTime);
        obj.put("partnerId", this.partnerId);
        obj.put("isSend", this.isSend);
        return obj.toJSONString();
    }

    public void mergeFrom(String serialiedStr) {
        if(HawkOSOperator.isEmptyString(serialiedStr)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(serialiedStr);
        this.playerId = obj.getString("playerId");
        this.orderId = obj.getString("orderId");
        this.termId = obj.getIntValue("termId");
        this.cfgId = obj.getIntValue("cfgId");
        this.state = obj.getIntValue("state");
        this.type = obj.getIntValue("type");
        this.isGet = obj.getBooleanValue("isGet");
        this.creatTime = obj.getLongValue("creatTime");
        this.endTime = obj.getLongValue("endTime");
        this.buyTime = obj.getLongValue("buyTime");
        this.cancelTime = obj.getLongValue("cancelTime");
        this.successTime = obj.getLongValue("successTime");
        this.partnerId = obj.getString("partnerId");
        this.isSend = obj.getBooleanValue("isSend");
    }

    public Activity.PDDOrderInfo.Builder toPB(){
        Activity.PDDOrderInfo.Builder builder = Activity.PDDOrderInfo.newBuilder();
        builder.setOrderId(orderId);
        builder.setCfgId(cfgId);
        builder.setState(Activity.PDDOrderState.valueOf(state));
        builder.setType(Activity.PDDBuyType.valueOf(type));
        builder.setIsGet(isGet);
        builder.setCreatTime(creatTime);
        builder.setEndTime(endTime);
        builder.setBuyTime(buyTime);
        builder.setCancelTime(cancelTime);
        return builder;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public int getCfgId() {
        return cfgId;
    }

    public void setCfgId(int cfgId) {
        this.cfgId = cfgId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(long creatTime) {
        this.creatTime = creatTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(long buyTime) {
        this.buyTime = buyTime;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isGet() {
        return isGet;
    }

    public void setIsGet(boolean isGet) {
        this.isGet = isGet;
    }

    public long getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(long cancelTime) {
        this.cancelTime = cancelTime;
    }

    public long getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(long successTime) {
        this.successTime = successTime;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }
}
