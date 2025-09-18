package com.hawk.activity.type.impl.supplyCrate.entity;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Reward;

public class SupplyCrateItemObj {
    private int pos;
    private int type;
    private int itemId;
    private int count;
    private boolean isGood;
    private boolean isDouble;
    private int weight;

    public SupplyCrateItemObj(){

    }

    public SupplyCrateItemObj(int pos, int type, int itemId, int count, boolean isGood, boolean isDouble, int weight) {
        this.pos = pos;
        this.type = type;
        this.itemId = itemId;
        this.count = count;
        this.isGood = isGood;
        this.isDouble = isDouble;
        this.weight = weight;
    }

    public static SupplyCrateItemObj crateNormalItem(String cfgStr){
        String[] split = cfgStr.split("_");

        if (split.length < 4) {
            return null;
        }
        SupplyCrateItemObj item = new SupplyCrateItemObj();
        item.type = Integer.parseInt(split[0]);
        item.itemId = Integer.parseInt(split[1]);
        item.count = Integer.parseInt(split[2]);
        item.weight = Integer.parseInt(split[3]);
        return item;
    }


    public static SupplyCrateItemObj crateCustomItem(String cfgStr){
        SupplyCrateItemObj item = crateNormalItem(cfgStr);
        if(item == null){
            return null;
        }
        item.isGood = true;
        return item;
    }

    public static SupplyCrateItemObj crateDoubleItem(int weight){
        SupplyCrateItemObj item = new SupplyCrateItemObj();
        item.isDouble = true;
        item.weight = weight;
        return item;
    }

    public SupplyCrateItemObj clone(){
        SupplyCrateItemObj item = new SupplyCrateItemObj(pos, type, itemId, count, isGood, isDouble, weight);
        return item;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }

    public int getType() {
        return type;
    }

    public int getItemId() {
        return itemId;
    }

    public int getCount() {
        return count;
    }

    public boolean isGood() {
        return isGood;
    }

    public boolean isDouble() {
        return isDouble;
    }

    public int getWeight() {
        return weight;
    }

    public Reward.RewardItem.Builder getReward(){
        Reward.RewardItem.Builder builder = Reward.RewardItem.newBuilder();
        builder.setItemType(type);
        builder.setItemId(itemId);
        builder.setItemCount(count);
        return builder;
    }

    public Activity.SupplyCrateItem.Builder toPB(){
        Activity.SupplyCrateItem.Builder builder = Activity.SupplyCrateItem.newBuilder();
        builder.setPos(pos);
        builder.setReward(getReward());
        builder.setIsGood(isGood);
        builder.setIsDouble(isDouble);
        return builder;
    }

    /**
     * 序列化
     * @param str
     */
    public JSONObject serialize() {
        JSONObject obj = new JSONObject();
        obj.put("pos", pos);
        obj.put("type", type);
        obj.put("itemId", itemId);
        obj.put("count", count);
        obj.put("isGood", isGood);
        obj.put("isDouble", isDouble);
        obj.put("weight", weight);
        return obj;
    }

    public static SupplyCrateItemObj unSerialize(JSONObject json) {
        SupplyCrateItemObj obj = new SupplyCrateItemObj();
        obj.pos = json.getIntValue("pos");
        obj.type = json.getIntValue("type");
        obj.itemId = json.getIntValue("itemId");
        obj.count = json.getIntValue("count");
        obj.isGood = json.getBooleanValue("isGood");
        obj.isDouble = json.getBooleanValue("isDouble");
        obj.weight = json.getIntValue("weight");
        return obj;
    }
}
