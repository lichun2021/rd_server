package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;


/**
 * 联盟锦标赛连胜减益配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/championship_debuff.xml")
public class ChampionshipDebuffCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	
	/** 排名区间*/
	private final String range;
	
	/** 减益效果*/
	private final String eff;
	
	private HawkTuple2<Integer, Integer> rangeTuple;
	
	private Map<Integer, Integer> effMap;
	
	public ChampionshipDebuffCfg() {
		id = 0;
		range = "";
		eff = "";
	}
	
	public int getId() {
		return id;
	}

	public HawkTuple2<Integer, Integer> getRangeTuple() {
		return rangeTuple;
	}

	public Map<Integer, Integer> getEffMap() {
		return effMap;
	}

	@Override
	protected boolean assemble() {
		try {
			List<EffectObject> effList = GameUtil.assambleEffectObject(eff);
			Map<Integer, Integer> map = new HashMap<>();
			if (effList != null && !effList.isEmpty()) {
				for (EffectObject effObj : effList) {
					map.put(effObj.getEffectType(), effObj.getEffectValue());
					// 减益效果超过100%
					if (effObj.getEffectValue() >= GsConst.EFF_RATE) {
						throw new InvalidParameterException(String.format("ChampionshipDebuffCfg eff error, id: %s, eff: %s", id, eff));
					}
				}
			}
			effMap = map;
			if (range.contains("_")) {
				String[] rankArr = range.split("_");
				rangeTuple = new HawkTuple2<Integer, Integer>(Integer.valueOf(rankArr[0]), Integer.valueOf(rankArr[1]));
			} else {
				rangeTuple = new HawkTuple2<Integer, Integer>(Integer.valueOf(range), Integer.valueOf(range));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("ChampionshipDebuffCfg error, id: {}, range: {}", id, range);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		if (rangeTuple.first > rangeTuple.second) {
			throw new InvalidParameterException(String.format("ChampionshipDebuffCfg range error, id: %s, range: %s", id, range));
		}
		return super.checkValid();
	}
}
