package com.hawk.game.module.mechacore.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 模块属性池子
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/mecha_core_additional_pool.xml")
public class MechaCoreAddtionalPoolCfg extends HawkConfigBase {

	@Id
	protected final int id;

	protected final String type; //type="6_10000;6_8000,5_2000;6_4000,5_6000;6_4000,5_4000,4_2000;6_3000,5_4000,4_3000;6_3000,5_3000,4_3000,3_1000"
	
	protected final int value;
	
	List<Map<Integer, Integer>> attrList = new ArrayList<>();
	
	public MechaCoreAddtionalPoolCfg() {
		id = 0;
		type = "";
		value = 0;
	}
	
	@Override
	protected boolean assemble() {
		String[] groups = type.split(";");
		for (String group : groups) {
			String[] qualityArr = group.split(",");
			Map<Integer, Integer> map = new HashMap<>();
			for (String qualityInfo : qualityArr) {
				String[] infos = qualityInfo.split("_");
				map.put(Integer.parseInt(infos[0]), Integer.parseInt(infos[1]));
			}
			attrList.add(map);
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public int getValue() {
		return value;
	}

	public List<Map<Integer, Integer>> getAttrList() {
		return attrList;
	}
}
