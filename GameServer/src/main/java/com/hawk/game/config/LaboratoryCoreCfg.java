package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/laborary_core.xml")
public class LaboratoryCoreCfg extends HawkConfigBase {
	@Id
	protected final int id; // 唯一
	protected final int index; // 类型 1~6
	protected final int type;
	protected final int level;
	protected final int lockNum; // 锁定能量源
	protected final String effectList;// ="403_1000|404_2500"

	private ImmutableList<HawkTuple2<EffType, Integer>> buff;

	public LaboratoryCoreCfg() {
		id = 0;
		index = 0;
		level = 0;
		lockNum = 0;
		effectList = "";
		type = 0;
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

	public int getIndex() {
		return index;
	}

	public int getLevel() {
		return level;
	}

	public int getLockNum() {
		return lockNum;
	}

}
