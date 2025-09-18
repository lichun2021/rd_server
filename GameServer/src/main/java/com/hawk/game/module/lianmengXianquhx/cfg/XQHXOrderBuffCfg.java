package com.hawk.game.module.lianmengXianquhx.cfg;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/xqhx_order_buff.xml")
public class XQHXOrderBuffCfg extends HawkConfigBase {
	@Id
	private final int id;
	private final int order;
	private final String effect;

	private ImmutableMap<EffType, Integer> controleBuffMap = ImmutableMap.of();

	public XQHXOrderBuffCfg() {
		id = 0;
		order = 0;
		effect = "";
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

	public int getEffVal(EffType eff) {
		return controleBuffMap.getOrDefault(eff, 0);
	}

	public int getId() {
		return id;
	}

	public int getOrder() {
		return order;
	}

	public String getEffect() {
		return effect;
	}

}
