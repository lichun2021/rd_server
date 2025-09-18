package com.hawk.activity.type.impl.planetexploration.entity;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 星能矿采集信息
 * @author lating
 */
public class PlanetCollectInfo implements SplitEntity {
    
    private int posX;
    private int posY;
    
    private Map<Long, Integer> collectTimeCountMap = new ConcurrentHashMap<>();

    public PlanetCollectInfo() {
    }

    public static PlanetCollectInfo valueOf(int posX, int posY) {
        PlanetCollectInfo collectInfo = new PlanetCollectInfo();
        collectInfo.posX = posX;
        collectInfo.posY = posY;
        return collectInfo;
    }

    @Override
    public SplitEntity newInstance() {
        return new PlanetCollectInfo();
    }

    @Override
    public void serializeData(List<Object> dataList) {
        dataList.add(this.posX);
        dataList.add(this.posY);
        dataList.add(SerializeHelper.mapToString(this.collectTimeCountMap, ":", ",", ";"));
    }

    @Override
    public void fullData(DataArray dataArray) {
        dataArray.setSize(3);
        this.posX = dataArray.getInt();
        this.posY = dataArray.getInt();
        String str = dataArray.getString();
        this.collectTimeCountMap.clear();
        this.collectTimeCountMap.putAll(SerializeHelper.stringToMap(str, Long.class, Integer.class, ":", ",", ";", null));
    }

    @Override
    public String toString() {
        return "[]";
    }

	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

	public Map<Long, Integer> getCollectTimeCountMap() {
		return collectTimeCountMap;
	}

	public List<Long> getSortedTimeList() {
		if (collectTimeCountMap.isEmpty()) {
			return Collections.emptyList();
		}
		
		return collectTimeCountMap.keySet().stream().sorted().collect(Collectors.toList());
	}

}
