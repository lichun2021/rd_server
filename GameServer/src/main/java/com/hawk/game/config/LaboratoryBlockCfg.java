package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/laborary_block.xml")
public class LaboratoryBlockCfg extends HawkConfigBase implements HawkRandObj {
	@Id
	protected final int id; // 唯一
	protected final int type; // 不能随机到同类型
	protected final int weight; //
	protected final String effectList;// ="403_1000|404_2500"

	protected final String atkAttr;
	protected final String hpAttr;
	
	private ImmutableList<HawkTuple2<EffType, Integer>> buff;

	public LaboratoryBlockCfg() {
		id = 0;
		weight = 1;
		effectList = "";
		type = 0;
		atkAttr = "";
		hpAttr = "";
	}

	@Override
	protected boolean assemble() {
		{
			List<HawkTuple2<EffType, Integer>> result = new ArrayList<>();

			List<String> attrs = Splitter.on("|").omitEmptyStrings().splitToList(effectList);
			for (String str : attrs) {
				String[] arr = Splitter.on("_").omitEmptyStrings().splitToList(str).toArray(new String[5]);
				result.add(HawkTuples.tuple(EffType.valueOf(NumberUtils.toInt(arr[0])), NumberUtils.toInt(arr[1])));
			}
			buff = ImmutableList.copyOf(result);
		}
		return true;
	}

	public ImmutableList<HawkTuple2<EffType, Integer>> getBuff() {
		return buff;
	}

	public void setBuff(ImmutableList<HawkTuple3<Integer, Double, Double>> buff) {
		throw new UnsupportedOperationException();
	}

	public int getId() {
		return id;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public String getEffectList() {
		return effectList;
	}

	public int getType() {
		return type;
	}

	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
}
