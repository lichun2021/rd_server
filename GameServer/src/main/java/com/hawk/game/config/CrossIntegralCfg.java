package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.game.crossactivity.CrossTargetType;
import com.hawk.game.util.GsConst;
import com.hawk.serialize.string.SerializeHelper;


/**
 * 跨服活动积分配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cross_integral.xml")
public class CrossIntegralCfg extends HawkConfigBase {
	
	/** 目标类型*/
	@Id
	private final int id;
	
	/** 名称*/
	private final String name;
	
	/** 积分值*/
	private final String scoreCof;
	
	/** 跨服倍率*/
	private final int crossScoreMag;
	
	/** 杀敌防守方积分倍率*/
	private final int crossScoreDefense;
	
	private List<Double> indexScoreList;
	
	private Map<Integer, Double> indexScoreMap;
	
	private Map<Integer, List<Double>> indexScoreListMap;
	
	private CrossTargetType targetType;
	
	public CrossIntegralCfg() {
		id = 0;
		name = "";
		scoreCof = "";
		crossScoreMag = 0;
		crossScoreDefense = GsConst.RANDOM_MYRIABIT_BASE;
	}
	
	@Override
	protected boolean assemble() {
		targetType = CrossTargetType.getType(id);
		if (targetType == null) {
			HawkLog.errPrintln("CrossTargetType not exist! id: {}", id);
			return false;
		}
		
		List<Double> indexScoreListTemp = new ArrayList<>();
		if (targetType.getConfigType() == CrossTargetType.ConfigType.LIST) {
			String[] arr = this.scoreCof.split(SerializeHelper.ATTRIBUTE_SPLIT);
			for(String str : arr){
				indexScoreListTemp.add(Double.valueOf(str));
			}
		}
		this.indexScoreList = indexScoreListTemp;
		
		
		Map<Integer,Double> indexScoreMapTemp = new HashMap<>();
		if (targetType.getConfigType() == CrossTargetType.ConfigType.MAP) {
			String[] arr = this.scoreCof.split(SerializeHelper.BETWEEN_ITEMS);
			for(String str : arr){
				String[] param = str.split(SerializeHelper.ATTRIBUTE_SPLIT);
				indexScoreMapTemp.put(Integer.valueOf(param[0]),Double.valueOf(param[1]));
			}
		}
		this.indexScoreMap = indexScoreMapTemp;
		
		Map<Integer, List<Double>> indexScoreListMapTemp = new HashMap<>();
		if(targetType.getConfigType() == CrossTargetType.ConfigType.OTHER){
			String src[] = scoreCof.split(SerializeHelper.BETWEEN_ITEMS);
			indexScoreListMap = new HashMap<Integer, List<Double>>();
			if(src.length != 2){
				throw new RuntimeException("CrossScoreCfg error, id:" + id);
			}
			/** 0为击杀积分系数列表    1为击伤积分系数列表 **/
			List<Double> list1= new ArrayList<>();
			String[] arr1 = src[0].split(SerializeHelper.ATTRIBUTE_SPLIT);
			for(String str : arr1){
				list1.add(Double.valueOf(str));
			}
			
			List<Double> list2= new ArrayList<>();
			String[] arr2 = src[1].split(SerializeHelper.ATTRIBUTE_SPLIT);
			for(String str : arr2){
				list2.add(Double.valueOf(str));
			}
			indexScoreListMapTemp.put(0,list1);
			indexScoreListMapTemp.put(1, list2);
		}
		this.indexScoreListMap = indexScoreListMapTemp;
		
		return true;
	}

	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}

	public String getScoreCof() {
		return scoreCof;
	}
	
	public List<Double> getIndexScoreList() {
		return indexScoreList;
	}
	
	public Map<Integer, Double> getIndexScoreMap() {
		return indexScoreMap;
	}
	
	public CrossTargetType getTargetType() {
		return targetType;
	}

	public Map<Integer, List<Double>> getIndexScoreListMap() {
		return indexScoreListMap;
	}

	public String getName() {
		return name;
	}

	public int getCrossScoreMag() {
		return crossScoreMag;
	}

	public int getCrossScoreDefense() {
		return crossScoreDefense;
	}
	
}
