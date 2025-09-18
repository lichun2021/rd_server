package com.hawk.game.module.lianmengyqzz.battleroom.cfg;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GameUtil;

@HawkConfigManager.XmlResource(file = "xml/moon_war_pylon_buff.xml")
public class YQZZPylonBuffCfg extends HawkConfigBase {

	@Id
	protected final int id;

	protected final int num;

	protected final String buff;

	private Map<Integer, Integer> effectMap;

	static TreeMap<Integer, YQZZPylonBuffCfg> map = new TreeMap<>();

	public YQZZPylonBuffCfg() {
		id = 0;
		num = 0;
		buff = "";
	}

	public int getId() {
		return id;
	}

	public int getNum() {
		return num;
	}

	public static int getEffectVal(int num, EffType effType) {
		Entry<Integer, YQZZPylonBuffCfg> cfgent = map.floorEntry(num);
		if (cfgent == null) {
			return 0;
		}
		return cfgent.getValue().effectMap.getOrDefault(effType.getNumber(), 0);
	}

	@Override
	protected boolean assemble() {
		effectMap = GameUtil.assambleEffectMap(buff);
		map.put(num, this);
		return true;
	}
}
