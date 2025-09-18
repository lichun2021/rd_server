package com.hawk.game.config;

import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.util.GameUtil;

@HawkConfigManager.XmlResource(file = "xml/cross_fortress.xml")
public class CrossFortressCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	
	protected final int x;
	
	protected final int y;
	
	protected final String effect;
	
	private Map<Integer, Integer> effectMap;
	
	public CrossFortressCfg() {
		id = 0;
		x = 0;
		y = 0;
		effect = "";
	}

	public int getId() {
		return id;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getEffect() {
		return effect;
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
		effectMap = GameUtil.assambleEffectMap(effect);
		return true;
	}
}
