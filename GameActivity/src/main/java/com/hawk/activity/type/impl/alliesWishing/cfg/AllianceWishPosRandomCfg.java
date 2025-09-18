package com.hawk.activity.type.impl.alliesWishing.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;

@HawkConfigManager.XmlResource(file = "activity/alliance_wish/alliance_wish_pos_pool.xml")
public class AllianceWishPosRandomCfg extends HawkConfigBase{
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;
	
	/**
	 * 数值
	 */
	private final String selectWeight;
	
	private Map<Integer,Integer> posWeight;

	public AllianceWishPosRandomCfg() {
		id = 0;
		selectWeight = "";
	}

	
	@Override
	protected boolean assemble() {
		Map<Integer,Integer> posWeightTemp = new HashMap<>();
		if(!HawkOSOperator.isEmptyString(this.selectWeight)){
			String[] arr = this.selectWeight.split(",");
			for(String posStr : arr){
				String[] posArr = posStr.split("_");
				posWeightTemp.put(Integer.parseInt(posArr[0]), Integer.parseInt(posArr[1]));
			}
		}
		this.posWeight = ImmutableMap.copyOf(posWeightTemp);
		return super.assemble();
	}
	

	public int getId() {
		return id;
	}
	
	
	public Map<Integer, Integer> getPosWeight() {
		return posWeight;
	}
	
	
	
	
	
}
