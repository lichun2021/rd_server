package com.hawk.game.module.lianmenxhjz.battleroom.cfg;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/xhjz_fuel_buff.xml")
public class XHJZFuelBuffCfg extends HawkConfigBase {

	@Id
	private final int id;
	private final String fuelRange;
	private final String effect;

	private Map<EffType, Integer> effectList;
	private HawkTuple2<Integer, Integer> minMax;
	public XHJZFuelBuffCfg() {
		id = 0;
		fuelRange = "";
		effect = "";
	}

	@Override
	protected boolean assemble() {

		{
			effectList = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(effect)) {
				String[] array = effect.split(",");
				for (String val : array) {
					String[] info = val.split("_");
					effectList.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
				}
			}
			effectList = ImmutableMap.copyOf(effectList);

		}
		{
			String[] array = fuelRange.split("_");
			minMax = HawkTuples.tuple(NumberUtils.toInt(array[0]), NumberUtils.toInt(array[1]));
		}
		return super.assemble();
	}

	public int getEffectVal(EffType eff) {
		return effectList.getOrDefault(eff, 0);
	}

	public Map<EffType, Integer> getEffectList() {
		return effectList;
	}

	public void setEffectList(Map<EffType, Integer> effectList) {
		this.effectList = effectList;
	}

	public int getId() {
		return id;
	}

	public String getFuelRange() {
		return fuelRange;
	}

	public String getEffect() {
		return effect;
	}

	public HawkTuple2<Integer, Integer> getMinMax() {
		return minMax;
	}

	public void setMinMax(HawkTuple2<Integer, Integer> minMax) {
		this.minMax = minMax;
	}

}
