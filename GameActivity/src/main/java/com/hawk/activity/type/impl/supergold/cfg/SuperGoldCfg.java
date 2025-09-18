package com.hawk.activity.type.impl.supergold.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

@HawkConfigManager.XmlResource(file = "activity/super_gold/super_gold.xml")
public class SuperGoldCfg extends HawkConfigBase {
	
	@Id
	private final int itemId;
	
	private final String rateRange; //倍率的范围 (比如1.2_1.5)
	
	private final Integer rate; //触发的概率
	
	public SuperGoldCfg(){
		itemId = 0;
		rateRange = "";
		rate = 0;
	}

	public int getId() {
		return itemId;
	}

	public String getRateRange() {
		return rateRange;
	}

	public Integer getRate() {
		return rate;
	}
	
	public float[] getFloatRange(){
		String []range = rateRange.split("_");
		float[] result = new float[range.length];
		for(int i= 0 ; i < range.length ; i ++){
			result[i] = Float.parseFloat(range[i]);
		}
		return result;
	}

	/***
	 * 检测配置表是否有效
	 */
	@Override
	protected boolean checkValid() {
		try {
			String []range = rateRange.split("_");
			if(range.length <= 0){
				throw new HawkException("SuperGoldCfg Error: invalid rateRange:" + rateRange);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
