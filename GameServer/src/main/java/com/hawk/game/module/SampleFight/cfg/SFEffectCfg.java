package com.hawk.game.module.SampleFight.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.MoreObjects;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/sf_effect.xml")
public class SFEffectCfg extends HawkConfigBase {
	@Id
	protected final int id;//
	protected final int value;// ="2000"
	private EffType eff;

	private static Map<EffType, Integer> effmap = new HashMap<>();

	public SFEffectCfg() {
		id = 0;
		value = 0;
	}

	@Override
	protected boolean assemble() {
		eff = EffType.valueOf(id);
		effmap.put(eff, value);
		return super.assemble();
	}

	public static int getEffval(EffType eff) {
		return effmap.getOrDefault(eff, 0);
	}

	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}

	@Override
	public String toString() {
		String str = MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("value", value)
				.toString();
		return str;
	}
}
