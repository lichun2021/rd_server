package com.hawk.activity.type.impl.starLightSign.entity;

import com.hawk.game.protocol.Activity;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

import java.util.ArrayList;
import java.util.List;

public class StarLightSignItem implements SplitEntity {
    private int type;
    private int rechargeType;
    private int isBuy;
    private List<Integer> reward;
    private int getCount;
    private int multiple;
    private int isGet;
    private int choose;

    public StarLightSignItem(){
        reward = new ArrayList<>();
    }

    @Override
    public StarLightSignItem newInstance() {
        return new StarLightSignItem();
    }

    @Override
    public void serializeData(List<Object> dataList) {
        dataList.add(type);
        dataList.add(rechargeType);
        dataList.add(isBuy);
        dataList.add(SerializeHelper.collectionToString(this.reward, SerializeHelper.BETWEEN_ITEMS));
        dataList.add(getCount);
        dataList.add(multiple);
        dataList.add(isGet);
        dataList.add(choose);
    }

    @Override
    public void fullData(DataArray dataArray) {
        dataArray.setSize(8);
        type = dataArray.getInt();
        rechargeType = dataArray.getInt();
        isBuy = dataArray.getInt();
        String rewardStr = dataArray.getString();
        this.reward = SerializeHelper.stringToList(Integer.class, rewardStr, SerializeHelper.BETWEEN_ITEMS);
        getCount = dataArray.getInt();
        multiple = dataArray.getInt();
        isGet = dataArray.getInt();
        choose = dataArray.getInt();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRechargeType() {
        return rechargeType;
    }

    public void setRechargeType(int rechargeType) {
        this.rechargeType = rechargeType;
    }

    public boolean isBuy() {
        return isBuy == 1;
    }

    public void setIsBuy(int isBuy) {
        this.isBuy = isBuy;
    }

    public List<Integer> getReward() {
        return reward;
    }

    public void setReward(List<Integer> reward) {
        this.reward = reward;
    }

    public int getGetCount() {
        return getCount;
    }

    public void setGetCount(int getCount) {
        this.getCount = getCount;
    }

    public float getMultiple() {
        return multiple / 10000f;
    }

    public void setMultiple(int multiple) {
        this.multiple = multiple;
    }

    public boolean isGet() {
        return isGet == 1;
    }

    public void setIsGet(int isGet) {
        this.isGet = isGet;
    }

    public int getChoose() {
        return choose;
    }

    public void setChoose(int choose) {
        this.choose = choose;
    }

    public Activity.StarlightSignInfo.Builder toPB(){
        Activity.StarlightSignInfo.Builder builder = Activity.StarlightSignInfo.newBuilder();
        builder.setType(Activity.StarlightSignType.valueOf(type));
        builder.setRechargeType(Activity.StarlightSignRechargeType.valueOf(rechargeType));
        builder.setIsbuy(isBuy());
        builder.addAllReward(reward);
        builder.setGetCount(getCount);
        builder.setMultiple(getMultiple());
        builder.setIsGet(isGet());
        builder.setChoose(choose);
        return builder;
    }
}
