package com.hawk.game.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import com.hawk.game.util.GsConst;

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
	protected final int cyLv31;
	protected final int cyLv32;
	protected final int cyLv33;
	protected final int cyLv34;
	protected final int cyLv35;
	protected final int cyLv36;
	protected final int cyLv37;
	protected final int cyLv38;
	protected final int cyLv39;
	protected final int cyLv40;
	protected final int cyLv41;
	protected final int cyLv42;
	protected final int cyLv43;
	protected final int cyLv44;
	protected final int cyLv45;
	protected final int cyLv46;
	protected final int cyLv47;
	protected final int cyLv48;
	protected final int cyLv49;
	protected final int cyLv50;

	// 大本对应等级能建造本建筑的数量
	protected Map<Integer, Integer> countLimit;
	
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
		cyLv31 = 0;
		cyLv32 = 0;
		cyLv33 = 0;
		cyLv34 = 0;
		cyLv35 = 0;
		cyLv36 = 0;
		cyLv37 = 0;
		cyLv38 = 0;
		cyLv39 = 0;
		cyLv40 = 0;
		cyLv41 = 0;
		cyLv42 = 0;
		cyLv43 = 0;
		cyLv44 = 0;
		cyLv45 = 0;
		cyLv46 = 0;
		cyLv47 = 0;
		cyLv48 = 0;
		cyLv49 = 0;
		cyLv50 = 0;
	}

	public int getLimit(int level){
		return countLimit.get(level);
	}
	
	@Override
	protected boolean assemble() {
		countLimit = new HashMap<Integer, Integer>();
		for(int lvl = 1; lvl <= GsConst.BUILDING_MAX_LEVEL; lvl++){
			try {
				Field field = getClass().getDeclaredField("cyLv" + lvl);
				countLimit.put(lvl, field.getInt(this));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return true;
	}
}
