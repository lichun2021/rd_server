package com.hawk.game.module.lianmengtaiboliya.cfg;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.WeightAble;

@HawkConfigManager.XmlResource(file = "xml/tbly_build_skill.xml")
public class TBLYBuildSkillCfg extends HawkConfigBase implements WeightAble{
	// <data id="9" showType="2" effect="131_500" weight="500" />
	@Id
	private final int id;// = 1000
	private final int showType;
	private final int weight;
	private final String effect;// = 1427_5000,1428_5000,1429_8000

	private ImmutableMap<EffType, Integer> controleBuffMap;

	public TBLYBuildSkillCfg() {
		id = 0;
		showType = 500;
		weight = 0;
		effect = "1427_0";
	}

	@Override
	protected boolean assemble() {

		{
			Map<EffType, Integer> lsit = new HashMap<>();
			for (String xy : effect.trim().split("\\,")) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.put(EffType.valueOf(pos[0]), pos[1]);
			}
			controleBuffMap = ImmutableMap.copyOf(lsit);
		}

		return super.assemble();
	}

	public ImmutableMap<EffType, Integer> getControleBuffMap() {
		return controleBuffMap;
	}

	public void setControleBuffMap(ImmutableMap<EffType, Integer> controleBuffMap) {
		this.controleBuffMap = controleBuffMap;
	}

	public int getId() {
		return id;
	}

	public int getShowType() {
		return showType;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public String getEffect() {
		return effect;
	}

}
