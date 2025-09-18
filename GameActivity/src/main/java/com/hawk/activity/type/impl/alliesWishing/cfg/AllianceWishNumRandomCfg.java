package com.hawk.activity.type.impl.alliesWishing.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;

@HawkConfigManager.XmlResource(file = "activity/alliance_wish/alliance_wish_num_pool.xml")
public class AllianceWishNumRandomCfg extends HawkConfigBase{
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;
	
	/**
	 * 数值
	 */
	private final String signWeight;

	
	private Map<Integer,Integer> numWeight;

	public AllianceWishNumRandomCfg() {
		id = 0;
		signWeight = "";
		
	}

	
	@Override
	protected boolean assemble() {
		Map<Integer,Integer> numWeightTemp = new HashMap<>();
		if(!HawkOSOperator.isEmptyString(this.signWeight)){
			String[] arr = this.signWeight.split(",");
			for(String posStr : arr){
				String[] posArr = posStr.split("_");
				numWeightTemp.put(Integer.parseInt(posArr[0]), Integer.parseInt(posArr[1]));
			}
		}
		this.numWeight = ImmutableMap.copyOf(numWeightTemp);
		return super.assemble();
	}
	

	public int getId() {
		return id;
	}
	
	
	public Map<Integer, Integer> getNumWeight() {
		return numWeight;
	}
	
	
	
	
	
}
