package com.hawk.activity.type.impl.luckGetGold.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.hawk.os.HawkOSOperator;

import java.util.ArrayList;
import java.util.List;

public class LuckGetGoldAction {
    private String serverId = "";
    private String playerId = "";
    private String playerName = "";
    private int costGold = 0;
    private int lastPoolGold = 0;
    private int poolGold = 0;

    private List<Integer> drawNum = new ArrayList<>();
    private List<Integer> divide = new ArrayList<>();
    private List<Integer> drawGold = new ArrayList<>();


    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("serverId", this.serverId);
        obj.put("playerId", this.playerId);
        obj.put("playerName", this.playerName);
        obj.put("costGold", this.costGold);
        obj.put("lastPoolGold", this.lastPoolGold);
        obj.put("poolGold", this.poolGold);
        JSONArray drawNumArr = new JSONArray();
        if(this.drawNum!= null && !this.drawNum.isEmpty()){
            for(int cfgId : this.drawNum){
                drawNumArr.add(cfgId);
            }
        }
        obj.put("drawNum", drawNumArr.toJSONString());
        JSONArray divideArr = new JSONArray();
        if(this.divide!= null && !this.divide.isEmpty()){
            for(int cfgId : this.divide){
                divideArr.add(cfgId);
            }
        }
        obj.put("divide", divideArr.toJSONString());
        JSONArray drawGoldArr = new JSONArray();
        if(this.drawGold!= null && !this.drawGold.isEmpty()){
            for(int cfgId : this.drawGold){
                drawGoldArr.add(cfgId);
            }
        }
        obj.put("drawGold", drawGoldArr.toJSONString());
        return obj.toJSONString();
    }

    public void mergeFrom(String serialiedStr) {
        if (HawkOSOperator.isEmptyString(serialiedStr)) {
            return;
        }
        JSONObject obj = JSONObject.parseObject(serialiedStr);
        this.serverId = obj.getString("serverId");
        this.playerId = obj.getString("playerId");
        this.playerName = obj.getString("playerName");
        this.costGold = obj.getIntValue("costGold");
        this.lastPoolGold = obj.getIntValue("lastPoolGold");
        this.poolGold = obj.getIntValue("poolGold");
        if(obj.containsKey("drawNum")){
            String drawNumStr = obj.getString("drawNum");
            JSONArray arr = JSONArray.parseArray(drawNumStr);
            for(int i = 0; i < arr.size(); i++){
                int drawNumValue  = arr.getIntValue(i);
                this.drawNum.add(drawNumValue);
            }
        }
        if(obj.containsKey("divide")){
            String divideStr = obj.getString("divide");
            JSONArray arr = JSONArray.parseArray(divideStr);
            for(int i = 0; i < arr.size(); i++){
                int divideValue  = arr.getIntValue(i);
                this.divide.add(divideValue);
            }
        }
        if(obj.containsKey("drawGold")){
            String drawGoldStr = obj.getString("drawGold");
            JSONArray arr = JSONArray.parseArray(drawGoldStr);
            for(int i = 0; i < arr.size(); i++){
                int drawGoldValue  = arr.getIntValue(i);
                this.drawGold.add(drawGoldValue);
            }
        }
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getCostGold() {
        return costGold;
    }

    public void setCostGold(int costGold) {
        this.costGold = costGold;
    }

    public int getLastPoolGold() {
        return lastPoolGold;
    }

    public void setLastPoolGold(int lastPoolGold) {
        this.lastPoolGold = lastPoolGold;
    }

    public int getPoolGold() {
        return poolGold;
    }

    public void setPoolGold(int poolGold) {
        this.poolGold = poolGold;
    }

    public List<Integer> getDrawNum() {
        return drawNum;
    }

    public List<Integer> getDivide() {
        return divide;
    }

    public List<Integer> getDrawGold() {
        return drawGold;
    }
}
