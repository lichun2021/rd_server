package com.hawk.game.config;

import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.util.GameUtil;

@HawkConfigManager.XmlResource(file = "xml/cross_score_rank_buff.xml")
public class CrossScoreRankBuffCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	
	protected final String buff;
	
	private Map<Integer, Integer> effectMap;
	
	public CrossScoreRankBuffCfg() {
		id = 0;
		buff = "";
	}

	public int getId() {
		return id;
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
