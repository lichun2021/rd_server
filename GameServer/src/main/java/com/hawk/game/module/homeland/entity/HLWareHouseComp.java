package com.hawk.game.module.homeland.entity;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.HomeLand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HLWareHouseComp implements SerializJsonStrAble {
    private final Map<Integer, HomeLandWareHouse> wareHouseMap = new ConcurrentHashMap<>();

    /**
     * 序列化
     */
    @Override
    public String serializ() {
        JSONArray arr = new JSONArray();
        wareHouseMap.values().stream().map(HomeLandWareHouse::serializ).forEach(arr::add);
        return arr.toJSONString();
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        JSONArray arr = JSONArray.parseArray(serialiedStr);
        if (arr == null) {
            return;
        }
        arr.forEach(str -> {
            HomeLandWareHouse wareHouse = new HomeLandWareHouse();
            wareHouse.mergeFrom(str.toString());
            wareHouseMap.put(wareHouse.getCfgId(), wareHouse);
        });
    }

    public List<HomeLand.HomeLandWareHousePB> buildingWarHousePBList() {
        List<HomeLand.HomeLandWareHousePB> buildingPBList = new ArrayList<>();
        wareHouseMap.forEach((key, value) -> {
            if (value.getCount() > 0) {
                buildingPBList.add(toWarHousePB(key, value.getCount()));
            }
        });
        return buildingPBList;
    }

    public HomeLand.HomeLandWareHousePB toWarHousePB(int configId, int itemCount) {
        HomeLand.HomeLandWareHousePB.Builder ware = HomeLand.HomeLandWareHousePB.newBuilder();
        ware.setBuildCfgId(configId);
        ware.setItemCount(itemCount);
        return ware.build();
    }

    public HomeLand.HomeLandWareHousePush.Builder buildWarHousePush() {
        HomeLand.HomeLandWareHousePush.Builder builder = HomeLand.HomeLandWareHousePush.newBuilder();
        builder.addAllBuilding(buildingWarHousePBList());
        return builder;
    }

    public Map<Integer, HomeLandWareHouse> getWareHouseMap() {
        return wareHouseMap;
    }

    public void removeWareHouse(int cfgId, int amount) {
        wareHouseMap.computeIfPresent(cfgId, (key, item) -> {
            if (item.getCount() < amount) {
                item.setCount(0);
            } else {
                item.setCount(item.getCount() - amount);
            }
            return item;
        });
    }

    public void addWareHouse(int cfgId, int amount) {
        HomeLandWareHouse item = wareHouseMap.computeIfAbsent(cfgId, HomeLandWareHouse::new);
        item.setCount(item.getCount() + amount);
    }
}
