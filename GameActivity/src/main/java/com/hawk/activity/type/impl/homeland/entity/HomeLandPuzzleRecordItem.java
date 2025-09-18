package com.hawk.activity.type.impl.homeland.entity;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.ActivityHomeLandPuzzle;
import com.hawk.game.protocol.Reward;

import java.util.stream.Collectors;

public class HomeLandPuzzleRecordItem {
    //图案配置id
    private int cfgId;
    //获得数量
    private int itemCount;
    //組合数量
    private int combines;
    //大奖数量
    private int prize;

    private String reward;

    public static HomeLandPuzzleRecordItem valueOf(int id, int amount) {
        return new HomeLandPuzzleRecordItem(id, amount);
    }

    public HomeLandPuzzleRecordItem(int cfgId, int amount) {
        this.cfgId = cfgId;
        this.itemCount = amount;
    }

    public HomeLandPuzzleRecordItem() {
    }

    public ActivityHomeLandPuzzle.HomeLandPuzzleItemPB buildItem() {
        ActivityHomeLandPuzzle.HomeLandPuzzleItemPB.Builder builder = ActivityHomeLandPuzzle.HomeLandPuzzleItemPB.newBuilder();
        builder.setItemCount(itemCount);
        builder.setCfgId(cfgId);
        builder.setCombinations(combines);
        builder.setGrandPrize(prize);
        builder.addAllReward(RewardHelper.toRewardItemImmutableList(reward).stream().map(Reward.RewardItem.Builder::build).collect(Collectors.toList()));
        return builder.build();
    }

    public void mergeFrom(String jsonStr) {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        if (jsonObject != null) {
            this.cfgId = jsonObject.getInteger("cfgId");
            this.itemCount = jsonObject.getInteger("itemCount");
            this.combines = jsonObject.getInteger("combines");
            this.prize = jsonObject.getInteger("prize");
            this.reward = jsonObject.getString("reward");
        }
    }

    /**
     * 序列化
     */
    public String serialize() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cfgId", this.cfgId);
        jsonObject.put("combines", this.combines);
        jsonObject.put("prize", this.prize);
        jsonObject.put("reward", this.reward);
        jsonObject.put("itemCount", this.itemCount);
        return jsonObject.toJSONString();
    }

    public int getCfgId() {
        return cfgId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getCombines() {
        return combines;
    }

    public void setCombines(int combines) {
        this.combines = combines;
    }

    public int getPrize() {
        return prize;
    }

    public void setPrize(int prize) {
        this.prize = prize;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }
}
