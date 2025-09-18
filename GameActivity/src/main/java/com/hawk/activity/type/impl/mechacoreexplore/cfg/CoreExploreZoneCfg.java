package com.hawk.activity.type.impl.mechacoreexplore.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

@HawkConfigManager.XmlResource(file = "activity/core_explore/core_explore_zone.xml")
public class CoreExploreZoneCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 行数范围（行数1，行数2）
	 */
	private final String row;
	/**
	 * 各列随机结果（空格、沙土、沙土附带矿石、石头、石头附带矿石、宝箱）的权重
	 */
	private final String weightColumn1;
	private final String weightColumn2;
	private final String weightColumn3;
	private final String weightColumn4;
	private final String weightColumn5;
	private final String weightColumn6;

	private int[] rows = new int[2];
	private Map<Integer, List<Integer>> columnObstacleMap = new HashMap<>();
	private Map<Integer, List<Integer>>  columnObstacleWeightMap = new HashMap<>();
	
	private static int forceLineCfgId;
	private static CoreExploreZoneCfg defaultLineCfg;
	
	
	public CoreExploreZoneCfg(){
		this.id = 0;
		this.row = "";
		this.weightColumn1 = "";
		this.weightColumn2 = "";
		this.weightColumn3 = "";
		this.weightColumn4 = "";
		this.weightColumn5 = "";
		this.weightColumn6 = "";
	}
	
	public boolean assemble() {
		parseColumn(weightColumn1, 1);
		parseColumn(weightColumn2, 2);
		parseColumn(weightColumn3, 3);
		parseColumn(weightColumn4, 4);
		parseColumn(weightColumn5, 5);
		parseColumn(weightColumn6, 6);
		
		if (HawkOSOperator.isEmptyString(row)) {
			forceLineCfgId = id;
			rows[0] = -1;
		    rows[1] = -1;
			return true;
		}
		String[] lineArr = row.split(",");
		if (lineArr.length != 2) {
			return false;
		}
		
		int startLine = Integer.parseInt(lineArr[0]);
		int endLine = Integer.parseInt(lineArr[1]);
		rows[0] = startLine;
		rows[1] = endLine;

		if (startLine == endLine && endLine == 0) {
			defaultLineCfg = this;
		}
		
		return true;
	}
	
	public boolean checkValid() {
		if (forceLineCfgId <= 0 || defaultLineCfg == null) {
			HawkLog.errPrintln("core_explore_zone.xml forceline or default line config miss");
			return false;
		}
		return true;
	}
	
	private void parseColumn(String weightColumnInfo, int column) {
		String[] infos = weightColumnInfo.split(",");
		if (infos.length < 7) {
			throw new RuntimeException("column info error: " + column + ", id: " + id);
		}
		
		List<Integer> obstacleList = new ArrayList<>();
		List<Integer> weightList = new ArrayList<>();
		columnObstacleMap.put(column, obstacleList);
		columnObstacleWeightMap.put(column, weightList);
		for(int i = 0; i < 7; i++) {
			obstacleList.add(i + 1);
			weightList.add(Integer.parseInt(infos[i]));
		}
	}
	
	public List<Integer> getColumnObstacleList(int column) {
		return columnObstacleMap.get(column);
	}
	
	public List<Integer> getColumnWeightList(int column) {
		return columnObstacleWeightMap.get(column);
	}
	
	public int getId() {
		return id;
	}

	public String getRow() {
		return row;
	}

	public String getWeightColumn1() {
		return weightColumn1;
	}

	public String getWeightColumn2() {
		return weightColumn2;
	}

	public String getWeightColumn3() {
		return weightColumn3;
	}

	public String getWeightColumn4() {
		return weightColumn4;
	}

	public String getWeightColumn5() {
		return weightColumn5;
	}

	public String getWeightColumn6() {
		return weightColumn6;
	}

	public static CoreExploreZoneCfg getConfig(int line) {
		if (line == 0) {
			return HawkConfigManager.getInstance().getConfigByKey(CoreExploreZoneCfg.class, forceLineCfgId);
		}
		
		ConfigIterator<CoreExploreZoneCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(CoreExploreZoneCfg.class);
		while (iterator.hasNext()) {
			CoreExploreZoneCfg cfg = iterator.next();
			if (cfg.rows[0] <= line && line <= cfg.rows[1]) {
				return cfg;
			}
		}
		
		return defaultLineCfg;
	}
	
}
