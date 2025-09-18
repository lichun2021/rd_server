package com.hawk.activity.type.impl.armiesMass.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.Activity.PBSculptureQuality;

/**
 * 时空好礼时空之门直购礼包道具
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/armies_mass/armies_mass_sculpture.xml")
public class ArmiesMassSculptureCfg extends HawkConfigBase {
	//阶段
	@Id
	private final int id;
	//雕像
	private final String pool;
	//个数
	private final int brandNumber;
	//中心品质
	private final int center;
	
	private Map<Integer,Integer> sculptureWeights = new HashMap<Integer,Integer>();
	
	public ArmiesMassSculptureCfg() {
		id = 0;
		pool = "";
		brandNumber = 0;
		center = 0;
	}
	
	@Override
	protected boolean assemble() {
		if(!HawkOSOperator.isEmptyString(this.pool)){
			String[] arr = this.pool.split(",");
			for(String str : arr){
				String[] weight = str.split("_");
				if(weight.length != 2){
					return false;
				}
				int quality = Integer.parseInt(weight[0]);
				if(PBSculptureQuality.valueOf(quality) == null){
					return false;
				}
				int value =  Integer.parseInt(weight[1]);
				sculptureWeights.put(quality, value);
			}
		}
		return true;
	}

	public Map<Integer, Integer> getSculptureWeights() {
		return sculptureWeights;
	}

	public void setSculptureWeights(Map<Integer, Integer> sculptureWeights) {
		this.sculptureWeights = sculptureWeights;
	}

	

	public int getId() {
		return id;
	}

	public String getPool() {
		return pool;
	}

	public int getBrandNumber() {
		return brandNumber;
	}

	public int getCenter() {
		return center;
	}

	

	
	
	
	
	
}
