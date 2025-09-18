package com.hawk.game.module.dayazhizhan.battleroom.cfg;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GameUtil;

/** 安全协议id
 *
 * @author hawk */
@HawkConfigManager.XmlResource(file = "xml/dyzz_area.xml")
public class DYZZAreaCfg extends HawkConfigBase {
	@Id
	protected final String point;
	protected final int id;
	protected final int camp;
	protected final int area;
	protected final String buffList;
	private ImmutableMap<EffType, Integer> collectBuffMap;

	public DYZZAreaCfg() {
		id = 0;
		camp = 0;
		buffList = "";
		point = "";
		area = 0;
	}

	public static DYZZAreaCfg getPointArea(int pointId) {
		int[] xy = GameUtil.splitXAndY(pointId);
		String cfgId = xy[0] + "_" + xy[1];
		return HawkConfigManager.getInstance().getConfigByKey(DYZZAreaCfg.class, cfgId);
	}

	@Override
	protected boolean assemble() {
		{
			Map<EffType, Integer> lsit = new HashMap<>();
			for (String xy : Splitter.on(",").omitEmptyStrings().splitToList(buffList)) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.put(EffType.valueOf(pos[0]), pos[1]);
			}
			collectBuffMap = ImmutableMap.copyOf(lsit);
		}
		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public int getCamp() {
		return camp;
	}

	public String getBuffList() {
		return buffList;
	}

	public String getPoint() {
		return point;
	}

	public ImmutableMap<EffType, Integer> getCollectBuffMap() {
		return collectBuffMap;
	}

	public void setCollectBuffMap(ImmutableMap<EffType, Integer> collectBuffMap) {
		this.collectBuffMap = collectBuffMap;
	}

	public int getArea() {
		return area;
	}

}
