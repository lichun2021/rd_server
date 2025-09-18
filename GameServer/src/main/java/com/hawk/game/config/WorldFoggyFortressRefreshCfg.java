package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;

import com.hawk.game.protocol.Const.ResourceZone;

/**
 *
 * @author zhenyu.shang
 * @since 2018年4月25日
 */
@HawkConfigManager.XmlResource(file = "xml/world_foggyFortress_refresh.xml")
public class WorldFoggyFortressRefreshCfg extends HawkConfigBase{
	
	protected final int openServiceTimeLowerLimit;
	
	protected final String resArea1;
	protected final String resArea2;
	protected final String resArea3;
	protected final String resArea4;
	protected final String resArea5;
	protected final String resArea6;
	protected final String resArea7;
	
	private Map<Integer, Integer> resArea1WeightMap = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> resArea2WeightMap = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> resArea3WeightMap = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> resArea4WeightMap = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> resArea5WeightMap = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> resArea6WeightMap = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> resArea7WeightMap = new HashMap<Integer, Integer>();

	public WorldFoggyFortressRefreshCfg() {
		openServiceTimeLowerLimit = 0;
		resArea1 = "";
		resArea2 = "";
		resArea3 = "";
		resArea4 = "";
		resArea5 = "";
		resArea6 = "";
		resArea7 = "";
	}

	public int getOpenServiceTimeLowerLimit() {
		return openServiceTimeLowerLimit;
	}

	public String getResArea1() {
		return resArea1;
	}

	public String getResArea2() {
		return resArea2;
	}

	public String getResArea3() {
		return resArea3;
	}

	public String getResArea4() {
		return resArea4;
	}

	public String getResArea5() {
		return resArea5;
	}

	public String getResArea6() {
		return resArea6;
	}

	public String getResArea7() {
		return resArea7;
	}
	
	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(resArea1)) {
			String[] split = resArea1.split(";");
			for (int i = 0; i < split.length; i++) {
				int id = Integer.parseInt(split[i].split("_")[0]);
				int weight = Integer.parseInt(split[i].split("_")[1]);
				resArea1WeightMap.put(id, weight);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(resArea2)) {
			String[] split = resArea2.split(";");
			for (int i = 0; i < split.length; i++) {
				int id = Integer.parseInt(split[i].split("_")[0]);
				int weight = Integer.parseInt(split[i].split("_")[1]);
				resArea2WeightMap.put(id, weight);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(resArea3)) {
			String[] split = resArea3.split(";");
			for (int i = 0; i < split.length; i++) {
				int id = Integer.parseInt(split[i].split("_")[0]);
				int weight = Integer.parseInt(split[i].split("_")[1]);
				resArea3WeightMap.put(id, weight);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(resArea4)) {
			String[] split = resArea4.split(";");
			for (int i = 0; i < split.length; i++) {
				int id = Integer.parseInt(split[i].split("_")[0]);
				int weight = Integer.parseInt(split[i].split("_")[1]);
				resArea4WeightMap.put(id, weight);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(resArea5)) {
			String[] split = resArea5.split(";");
			for (int i = 0; i < split.length; i++) {
				int id = Integer.parseInt(split[i].split("_")[0]);
				int weight = Integer.parseInt(split[i].split("_")[1]);
				resArea5WeightMap.put(id, weight);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(resArea6)) {
			String[] split = resArea6.split(";");
			for (int i = 0; i < split.length; i++) {
				int id = Integer.parseInt(split[i].split("_")[0]);
				int weight = Integer.parseInt(split[i].split("_")[1]);
				resArea6WeightMap.put(id, weight);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(resArea7)) {
			String[] split = resArea7.split(";");
			for (int i = 0; i < split.length; i++) {
				int id = Integer.parseInt(split[i].split("_")[0]);
				int weight = Integer.parseInt(split[i].split("_")[1]);
				resArea7WeightMap.put(id, weight);
			}
		}
		return super.assemble();
	}
	
	@Override
	protected boolean checkValid() {
		for (Integer foggyId : resArea1WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			if (pointCfg == null) {
				return false;
			}
		}
		for (Integer foggyId : resArea2WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			if (pointCfg == null) {
				return false;
			}
		}
		for (Integer foggyId : resArea3WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			if (pointCfg == null) {
				return false;
			}
		}
		for (Integer foggyId : resArea4WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			if (pointCfg == null) {
				return false;
			}
		}
		for (Integer foggyId : resArea5WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			if (pointCfg == null) {
				return false;
			}
		}
		for (Integer foggyId : resArea6WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			if (pointCfg == null) {
				return false;
			}
		}
		for (Integer foggyId : resArea7WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			if (pointCfg == null) {
				return false;
			}
		}
		return true;
	}
	
	public List<Integer> getFoggyIds(int zoneId){
		List<Integer> list = new ArrayList<Integer>();
		Map<Integer, Integer> randomMap = getZoneMap(zoneId);
		for (Integer forggyId : randomMap.keySet()) {
			list.add(forggyId);
		}
		return list;
	}
	
	public int getRandomFoggy(int zoneId){
		return HawkRand.randomWeightObject(getZoneMap(zoneId));
	}
	
	private Map<Integer, Integer> getZoneMap(int zoneId){
		Map<Integer, Integer> randomMap = null;
		switch (zoneId) {
		case ResourceZone.ZONE_1_VALUE:
			randomMap = getResArea1WeightMap();
			break;
		case ResourceZone.ZONE_2_VALUE:
			randomMap = getResArea2WeightMap();	
			break;
		case ResourceZone.ZONE_3_VALUE:
			randomMap = getResArea3WeightMap();
			break;
		case ResourceZone.ZONE_4_VALUE:
			randomMap = getResArea4WeightMap();
			break;
		case ResourceZone.ZONE_5_VALUE:
			randomMap = getResArea5WeightMap();
			break;
		case ResourceZone.ZONE_6_VALUE:
			randomMap = getResArea6WeightMap();
			break;
		case ResourceZone.ZONE_BLACK_VALUE:
			randomMap = getResArea7WeightMap();
			break;
		default:
			break;
		}
		HawkAssert.notNull(randomMap);
		return randomMap;
	}

	public Map<Integer, Integer> getResArea1WeightMap() {
		return resArea1WeightMap;
	}

	public Map<Integer, Integer> getResArea2WeightMap() {
		return resArea2WeightMap;
	}

	public Map<Integer, Integer> getResArea3WeightMap() {
		return resArea3WeightMap;
	}

	public Map<Integer, Integer> getResArea4WeightMap() {
		return resArea4WeightMap;
	}

	public Map<Integer, Integer> getResArea5WeightMap() {
		return resArea5WeightMap;
	}

	public Map<Integer, Integer> getResArea6WeightMap() {
		return resArea6WeightMap;
	}

	public Map<Integer, Integer> getResArea7WeightMap() {
		return resArea7WeightMap;
	}
	
	public int getMaxLevel() {
		int maxLevel = 0;
		for (Integer foggyId : resArea1WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			maxLevel = Math.max(pointCfg.getLevel(), maxLevel);
		}
		for (Integer foggyId : resArea2WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			maxLevel = Math.max(pointCfg.getLevel(), maxLevel);
		}
		for (Integer foggyId : resArea3WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			maxLevel = Math.max(pointCfg.getLevel(), maxLevel);
		}
		for (Integer foggyId : resArea4WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			maxLevel = Math.max(pointCfg.getLevel(), maxLevel);
		}
		for (Integer foggyId : resArea5WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			maxLevel = Math.max(pointCfg.getLevel(), maxLevel);
		}
		for (Integer foggyId : resArea6WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			maxLevel = Math.max(pointCfg.getLevel(), maxLevel);
		}
		for (Integer foggyId : resArea7WeightMap.keySet()) {
			FoggyFortressCfg pointCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, foggyId);
			maxLevel = Math.max(pointCfg.getLevel(), maxLevel);
		}
		
		return maxLevel;
	}
}
