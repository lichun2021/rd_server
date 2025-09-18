package com.hawk.activity.type.impl.plantSoldierFactory.entity;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.Activity;

public class PlantSoldierFactoryItemObj {
    private int pos;
    private int cfgId;
    private int state;

    public PlantSoldierFactoryItemObj(){

    }

    public PlantSoldierFactoryItemObj(int pos, int cfgId, Activity.PlantSoldierFactoryItemState state){
        this.pos = pos;
        this.cfgId = cfgId;
        this.state = state.getNumber();
    }

    public Activity.PlantSoldierFactoryItemState getPBState() {
        return Activity.PlantSoldierFactoryItemState.valueOf(state);
    }

    public Activity.PlantSoldierFactoryItem.Builder toPB(){
        Activity.PlantSoldierFactoryItem.Builder builder = Activity.PlantSoldierFactoryItem.newBuilder();
        builder.setPos(pos);
        builder.setCfgId(cfgId);
        builder.setState(getPBState());
        return builder;
    }

    public JSONObject serialize() {
        JSONObject obj = new JSONObject();
        obj.put("pos", pos);
        obj.put("cfgId", cfgId);
        obj.put("state", state);
        return obj;
    }

    public static PlantSoldierFactoryItemObj unSerialize(JSONObject json) {
        PlantSoldierFactoryItemObj obj = new PlantSoldierFactoryItemObj();
        obj.pos = json.getIntValue("pos");
        obj.cfgId = json.getIntValue("cfgId");
        obj.state = json.getIntValue("state");
        return obj;
    }

    public int getPos() {
        return pos;
    }

    public int getCfgId() {
        return cfgId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
