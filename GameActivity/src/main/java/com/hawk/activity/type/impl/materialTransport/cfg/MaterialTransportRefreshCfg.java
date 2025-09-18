
package com.hawk.activity.type.impl.materialTransport.cfg;

import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 机甲破世 
 * @author RickMei 
 *
 */
@HawkConfigManager.XmlResource(file = "activity/material_transport/material_transport_refresh.xml")
public class MaterialTransportRefreshCfg extends HawkConfigBase {
	// <data id="1" refreshNumber="0" refreshQuality="3_9500|4_500" />
	@Id
	private final int id;
	
	private final int type; // 1 个人, 2 联盟 3 豪华

	private final String refreshQuality;

	private final int refreshNumber;
	private Map<Integer, Integer> refreshQualityMap;

	public MaterialTransportRefreshCfg() {
		id = 0;
		type = 1;
		refreshQuality = "";
		refreshNumber = 0;
	}

	@Override
	protected boolean assemble() {
		refreshQualityMap = SerializeHelper.cfgStr2Map(refreshQuality, SerializeHelper.ELEMENT_SPLIT, SerializeHelper.ATTRIBUTE_SPLIT);
		return true;
	}

	public int getId() {
		return id;
	}

	public Map<Integer, Integer> getRefreshQualityMap() {
		return refreshQualityMap;
	}

	public void setRefreshQualityMap(Map<Integer, Integer> refreshQualityMap) {
		this.refreshQualityMap = refreshQualityMap;
	}

	public String getRefreshQuality() {
		return refreshQuality;
	}

	public int getRefreshNumber() {
		return refreshNumber;
	}

	public int getType() {
		return type;
	}

}
