package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 情报中心等级配置
 * 
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/agency_level.xml")
public class AgencyLevelCfg extends HawkConfigBase {

	/**
	 * 等级
	 */
	@Id
	private final int lv;
	
	/**
	 * 经验
	 */
	private final int exp;
	
	/**
	 * 刷新事件数量
	 */
	private final int eventCount;
	
	
	private final int weightGroup;
	
	


	/**
	 * 升级条件类型
	 */
	private final int levelUpLimitType;
	
	/**
	 * 升级条件参数
	 */
	private final String levelUpLimitParams;
	
	
	/**
	 * 刷新范围
	 */
	private final String range;
	
	/**
	 * 初始化事件
	 */
	private final String initEvents;
	
	
	
	
	private final double timeModulus1;
	private final double timeModulus2;
	private final double timeModulus3;
	
	
	private List<Integer> initEventsList;
	
	private List<int[]> levelUpLimitParamList;
	
	
	private Map<Integer,int[]> rangeMap;
	
	public AgencyLevelCfg() {
		lv = 0;
		exp = 0;
		eventCount = 0;
		weightGroup = 0;
		levelUpLimitParams = "";
		initEvents = "";
		levelUpLimitType = 0;
		timeModulus1 = 0;
		timeModulus2 = 0;
		timeModulus3 = 0;
		range = "";
	}
	
	@Override
	protected boolean assemble() {
		
		List<Integer> initEventsList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(initEvents)) {
			String[] events = initEvents.split(",");
			for (int i = 0; i < events.length; i++) {
				initEventsList.add(Integer.valueOf(events[i]));
			}
		}
		this.initEventsList = initEventsList;
		
		
		List<int[]> initLevelUpLimitParamList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(levelUpLimitParams)) {
			String[] paramArr = levelUpLimitParams.split(";");
			for (String paramStr :paramArr ) {
				String[] arr = paramStr.split(",");
				int[] params = new int[arr.length];
				for(int i =0;i < arr.length;i++){
					if(HawkOSOperator.isEmptyString(arr[i])){
						return false;
					}
					params[i] = Integer.parseInt(arr[i]);
				}
				initLevelUpLimitParamList.add(params);
			}
		}
		this.levelUpLimitParamList = initLevelUpLimitParamList;
		
		Map<Integer,int[]> initRangeMap = new HashMap<>();
		if(!HawkOSOperator.isEmptyString(range)){
			String[] paramArr = range.split(","); 
			for (String paramStr :paramArr ) {
				String[] arr = paramStr.split("_");
				int key = Integer.parseInt(arr[0]);
				int[] params = new int[arr.length-1];
				for(int i = 1;i < arr.length; i++){
					if(HawkOSOperator.isEmptyString(arr[i])){
						return false;
					}
					params[i-1] = Integer.parseInt(arr[i]);
				}
				initRangeMap.put(key,params);
			}
		}
		this.rangeMap = initRangeMap;
		return true;
	}

	public int getLv() {
		return lv;
	}

	
	

	public int getEventCount() {
		return eventCount;
	}
	
	

	public int getWeightGroup() {
		return weightGroup;
	}

	public int getExp() {
		return exp;
	}
	
	
	
	public double getTimeModulus1() {
		return timeModulus1;
	}

	public double getTimeModulus2() {
		return timeModulus2;
	}

	public double getTimeModulus3() {
		return timeModulus3;
	}

	public List<Integer> getInitEventsList() {
		return new ArrayList<>(initEventsList);
	}

	public int getLevelUpLimitType() {
		return levelUpLimitType;
	}

	public List<int[]> getLevelUpLimitParamList() {
		return levelUpLimitParamList;
	}
	
	public int[] getRange(int type) {
		return rangeMap.get(type);
	}

}
