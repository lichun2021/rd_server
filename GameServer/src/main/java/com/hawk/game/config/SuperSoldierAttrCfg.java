package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

@HawkConfigManager.XmlResource(file = "xml/supersoldier_attr.xml")
public class SuperSoldierAttrCfg extends HawkConfigBase {
	@Id
	private final int id;
	private final String attrMarchEffect;// "103_50|105_80"
	protected final int marchUsed;

	private ImmutableList<HawkTuple2<Integer, Double>> attrMarchEffectList;

	public SuperSoldierAttrCfg() {
		this.id = 0;
		this.attrMarchEffect = "";
		this.marchUsed = 50;
	}

	@Override
	protected boolean assemble() {
		List<String> attrs = Splitter.on("|").omitEmptyStrings().splitToList(attrMarchEffect);
		List<HawkTuple2<Integer, Double>> list = new ArrayList<>(attrs.size());
		for (String str : attrs) {
			String[] arr = Splitter.on("_").omitEmptyStrings().splitToList(str).toArray(new String[2]);
			list.add(HawkTuples.tuple(NumberUtils.toInt(arr[0]), NumberUtils.toDouble(arr[1])));
		}
		this.attrMarchEffectList = ImmutableList.copyOf(list);

		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public ImmutableList<HawkTuple2<Integer, Double>> getAttrMarchEffectList() {
		return attrMarchEffectList;
	}

	public void setAttrMarchEffectList(ImmutableList<HawkTuple2<Integer, Double>> attrEffectList) {
		throw new UnsupportedOperationException();
	}

	public int getMarchUsed() {
		return marchUsed;
	}

}
