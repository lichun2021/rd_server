package com.hawk.activity.type.impl.pddActivity.data;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.Activity;
import org.hawk.os.HawkOSOperator;

public class PDDUserData {
    private String playerId = "";
    private String name = "";
    private int icon = 0;
    private String pfIcon = "";

    public PDDUserData(){

    }

    public String serializ() {
        JSONObject obj = new JSONObject();
        obj.put("playerId", this.playerId);
        obj.put("name", this.name);
        obj.put("icon", this.icon);
        obj.put("pfIcon", this.pfIcon);
        return obj.toJSONString();
    }

    public void mergeFrom(String serialiedStr) {
        if(HawkOSOperator.isEmptyString(serialiedStr)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(serialiedStr);
        this.playerId = obj.getString("playerId");
        this.name = obj.getString("name");
        this.icon = obj.getIntValue("icon");
        this.pfIcon = obj.getString("pfIcon");
    }

    public Activity.PDDMember.Builder toPB(){
        Activity.PDDMember.Builder builder = Activity.PDDMember.newBuilder();
        builder.setPlayerId(playerId);
        builder.setName(name);
        builder.setIcon(icon);
        builder.setPfIcon(pfIcon);
        return builder;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getPfIcon() {
        return pfIcon;
    }

    public void setPfIcon(String pfIcon) {
        this.pfIcon = pfIcon;
    }
}