package com.hawk.activity.type.impl.fireworks.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

/**
 * 作用号配置信息
 */
@HawkConfigManager.XmlResource(file = "activity/celebration_frieworks/celebration_frieworks_buff_lv.xml")
public class FireWorksBuffCfg extends HawkConfigBase {
	@Id
	private final int buffId;
	//等级
	private final int lv;
	//类型
	private final int type;
	//时间
	private final long time;

	private static List<Integer> buffTypeList = new ArrayList<>();
	
	private static Map<Integer, Integer> buffMaxLevel = new HashMap<>();
	
	public FireWorksBuffCfg() {
		buffId = 0;
		lv = 0;
		type = 0;
		time = 0;
	}

	@Override
	protected boolean assemble() {
		try {
			if (!buffTypeList.contains(type)) {
				buffTypeList.add(type);
			}
			if (!buffMaxLevel.containsKey(type)) {
				buffMaxLevel.put(type, lv);
			}else{
				//把最大的存里
				if (buffMaxLevel.get(type) < lv) {
					buffMaxLevel.put(type, lv);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	public int getType() {
		return type;
	}

	public static List<Integer> getBuffTypeList() {
		return buffTypeList;
	}

	public int getBuffId() {
		return buffId;
	}

	public int getLv() {
		return lv;
	}

	public long getTime() {
		return time * 1000;
	}

	public static Map<Integer, Integer> getBuffMaxLevel() {
		return buffMaxLevel;
	}
	
}
