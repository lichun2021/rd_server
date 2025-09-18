package com.hawk.robot.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

/**
 * 建筑工厂限制
 *
 * @author david
 *
 */
@HawkConfigManager.XmlResource(file = "xml/build_limit.xml")
public class BuildLimitCfg extends HawkConfigBase {
	@Id
	protected final int id = 0;
	//建筑工厂等级
	protected final int cyLv1;
	protected final int cyLv2;
	protected final int cyLv3;
	protected final int cyLv4;
	protected final int cyLv5;
	protected final int cyLv6;
	protected final int cyLv7;
	protected final int cyLv8;
	protected final int cyLv9;
	protected final int cyLv10;
	protected final int cyLv11;
	protected final int cyLv12;
	protected final int cyLv13;
	protected final int cyLv14;
	protected final int cyLv15;
	protected final int cyLv16;
	protected final int cyLv17;
	protected final int cyLv18;
	protected final int cyLv19;
	protected final int cyLv20;
	protected final int cyLv21;
	protected final int cyLv22;
	protected final int cyLv23;
	protected final int cyLv24;
	protected final int cyLv25;
	protected final int cyLv26;
	protected final int cyLv27;
	protected final int cyLv28;
	protected final int cyLv29;
	protected final int cyLv30;

	// 大本对应等级能建造本建筑的数量
	protected Map<Integer, Integer> countLimit;
	protected static Map<Integer, Map<Integer, Integer>> cyLvBuildLimitCountMap = new HashMap<>();
	
	public BuildLimitCfg() {
		cyLv1 = 0;
		cyLv2 = 0;
		cyLv3 = 0;
		cyLv4 = 0;
		cyLv5 = 0;
		cyLv6 = 0;
		cyLv7 = 0;
		cyLv8 = 0;
		cyLv9 = 0;
		cyLv10 = 0;
		cyLv11 = 0;
		cyLv12 = 0;
		cyLv13 = 0;
		cyLv14 = 0;
		cyLv15 = 0;
		cyLv16 = 0;
		cyLv17 = 0;
		cyLv18 = 0;
		cyLv19 = 0;
		cyLv20 = 0;
		cyLv21 = 0;
		cyLv22 = 0;
		cyLv23 = 0;
		cyLv24 = 0;
		cyLv25 = 0;
		cyLv26 = 0;
		cyLv27 = 0;
		cyLv28 = 0;
		cyLv29 = 0;
		cyLv30 = 0;
	}

	public int getLimit(int level){
		return countLimit.get(level);
	}
	
	/**
	 * 获取对应大本等级的各类buildLimit数量
	 * @param cityLevel
	 * @return
	 */
	public static Map<Integer, Integer> getBuildLimitCountMap(int cityLevel) {
		return cyLvBuildLimitCountMap.get(cityLevel);
	}
	
	@Override
	protected boolean assemble() {
		countLimit = new HashMap<Integer, Integer>();
		for(int lvl = 1; lvl <= 30; lvl++){
			try {
				Field field = getClass().getDeclaredField("cyLv" + lvl);
				int count = field.getInt(this);
				countLimit.put(lvl, count);
				if(count > 0) {
					Map<Integer, Integer> limitCountMap = cyLvBuildLimitCountMap.get(lvl);
					if(limitCountMap == null) {
						limitCountMap = new HashMap<>();
						cyLvBuildLimitCountMap.put(lvl, limitCountMap);
					}
					Field idField = getClass().getDeclaredField("id");
					int idValue = idField.getInt(this);
					limitCountMap.put(idValue, count);
				}
				
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return true;
	}
}
