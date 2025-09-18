package com.hawk.game.module.homeland.entity;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.HomeLand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HLCollectComp implements SerializJsonStrAble {
    private final Map<Integer, HomeLandCollect> buildingCollectList = new ConcurrentHashMap<>();

    /**
     * 序列化
     */
    @Override
    public String serializ() {
        JSONArray arr = new JSONArray();
        buildingCollectList.values().stream().map(HomeLandCollect::serializ).forEach(arr::add);
        return arr.toJSONString();
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        JSONArray arr = JSONArray.parseArray(serialiedStr);
        if (arr == null) {
            return;
        }
        arr.forEach(str -> {
            HomeLandCollect collect = new HomeLandCollect();
            collect.mergeFrom(str.toString());
            buildingCollectList.put(collect.getBuildType(), collect);
        });
    }

    public Map<Integer, HomeLandCollect> getBuildingCollectList() {
        return buildingCollectList;
    }

    public HomeLand.HomeLandBuildingCollectPush.Builder buildCollectPush() {
        HomeLand.HomeLandBuildingCollectPush.Builder builder = HomeLand.HomeLandBuildingCollectPush.newBuilder();
        HomeLand.HomeLandBuildingCollectPB.Builder pbBuilder = HomeLand.HomeLandBuildingCollectPB.newBuilder();
        buildingCollectList.values().forEach(v -> pbBuilder.addBuildCfgList(v.getBuildType()));
        builder.setCollect(pbBuilder);
        return builder;
    }
}
