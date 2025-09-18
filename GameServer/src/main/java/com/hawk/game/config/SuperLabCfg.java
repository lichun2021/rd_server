package com.hawk.game.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/super_lab.xml")
public class SuperLabCfg extends HawkConfigBase {

	@Id
	protected final int id;// ="1"
	protected final String effectList;// ="301_5000|303_5000"
	protected final String needMaterial;// ="30000_1480001_1,30000_1480002_1"

	private ImmutableMap<EffType, Integer> effectMap;

	public SuperLabCfg() {
		this.id = 0;
		this.effectList = "";
		this.needMaterial = "";
	}

	@Override
	protected boolean assemble() {
		List<String> attrs = Splitter.on("|").omitEmptyStrings().splitToList(effectList);
		Map<EffType, Integer> map = new HashMap<>();
		for (String str : attrs) {
			String[] arr = Splitter.on("_").omitEmptyStrings().splitToList(str).toArray(new String[2]);
			map.put(EffType.valueOf(Integer.parseInt(arr[0])), Integer.parseInt(arr[1]));
		}
		this.effectMap = ImmutableMap.copyOf(map);
		return super.assemble();
	}
	
	public EffType[] effArr() {
		return effectMap.keySet().toArray(new EffType[effectMap.size()]);
	}

	public int getEffect(EffType eff) {
		return effectMap.getOrDefault(eff, 0);
	}

	public int getId() {
		return id;
	}

	public String getEffectList() {
		return effectList;
	}

	public String getNeedMaterial() {
		return needMaterial;
	}

}
