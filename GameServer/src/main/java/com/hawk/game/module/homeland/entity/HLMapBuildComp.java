package com.hawk.game.module.homeland.entity;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.module.homeland.cfg.HomeLandBuildingTypeCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.HomeLand;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HLMapBuildComp implements SerializJsonStrAble {

    public Map<String, HomeLandBuilding> buildingMap = new ConcurrentHashMap<>();

    @Override
    public void mergeFrom(String jsonStr) {
        JSONArray arr = JSONArray.parseArray(jsonStr);
        if (arr == null) {
            return;
        }
        arr.forEach(str -> {
            HomeLandBuilding building = new HomeLandBuilding();
            building.mergeFrom(str.toString());
            buildingMap.put(building.getUid(), building);
        });
    }

    @Override
    public String serializ() {
        JSONArray arr = new JSONArray();
        buildingMap.values().stream().map(HomeLandBuilding::serialize).forEach(arr::add);
        return arr.toJSONString();
    }

    public List<HomeLand.HomeLandBuildingPB> buildingPBList() {
        List<HomeLand.HomeLandBuildingPB> buildingPBList = new ArrayList<>();
        for (HomeLandBuilding building : buildingMap.values()) {
            buildingPBList.add(building.toPB());
        }
        return buildingPBList;
    }

    /**
     * 根据建筑类型获取最大生效繁荣度
     */
    public List<HomeLandBuilding> getMaxEffectBuilds() {
        List<HomeLandBuilding> maxEffectBuilds = new ArrayList<>();
        ConfigIterator<HomeLandBuildingTypeCfg> typeCfgConfigIterator = HawkConfigManager.getInstance().getConfigIterator(HomeLandBuildingTypeCfg.class);
        for (HomeLandBuildingTypeCfg buildingTypeCfg : typeCfgConfigIterator) {
            List<HomeLandBuilding> topBuildingsOfMultiTypes = getBuildingMap().values().stream()
                    .filter(building -> buildingTypeCfg.getBuildType() == building.getBuildType())
                    .sorted(Comparator.comparingInt((HomeLandBuilding building) -> building.getBuildCfg().getLevel()).reversed())
                    .limit(buildingTypeCfg.getMaxNumber())
                    .collect(Collectors.toList());
            maxEffectBuilds.addAll(topBuildingsOfMultiTypes);
        }
        return maxEffectBuilds;
    }
    /**
     * 获取当前繁荣度
     */
    public long getCurrentProsperity() {
        return getMaxEffectBuilds().stream().mapToInt(v -> v.getBuildCfg().getProsperity()).sum();
    }
    /**
     * 获取当前战力
     */
    public int getPower() {
        return getMaxEffectBuilds().stream().mapToInt(v -> v.getBuildCfg().getPower()).sum();
    }

    public Map<String, HomeLandBuilding> getBuildingMap() {
        return buildingMap;
    }
}
