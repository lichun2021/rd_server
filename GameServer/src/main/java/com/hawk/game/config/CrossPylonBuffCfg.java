package com.hawk.game.config;

import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.util.GameUtil;

@HawkConfigManager.XmlResource(file = "xml/cross_pylon_buff.xml")
public class CrossPylonBuffCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	
	protected final int num;
	
	protected final String buff;
	
	private Map<Integer, Integer> effectMap;
	
	public CrossPylonBuffCfg() {
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

	public int getEffectVal(int effectType) {
		Integer effectValue = effectMap.get(effectType);
		if (effectValue == null) {
			return 0;
		}
		return effectValue;
	} 
	
	@Override
	protected boolean assemble() {
		effectMap = GameUtil.assambleEffectMap(buff);
		return true;
	}
}
