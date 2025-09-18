package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

/**
 * 科技功能配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cyborg_build_buff_level_extra.xml")
public class CYBORGBuildBuffLevelExtraCfg extends HawkConfigBase {
	// <data id="10001" level="1" buffId="1" buffList="100_5000,136_5000,102_5000" />
	protected final int id;
	protected final int lostBuild;
	@Id
	protected final int level;
	protected final String buffList;

	private ImmutableMap<EffType, Integer> controleBuffMap;

	public CYBORGBuildBuffLevelExtraCfg() {
		this.id = 0;
		this.lostBuild = 0;
		this.level = 0;
		this.buffList = "";
	}

	@Override
	protected boolean assemble() {
		{
			Map<EffType, Integer> lsit = new HashMap<>();
			for (String xy : buffList.trim().split("\\,")) {
				String[] x_y = xy.split("_");
				if (x_y.length < 2) {
					continue;
				}
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.put(EffType.valueOf(pos[0]), pos[1]);
			}
			controleBuffMap = ImmutableMap.copyOf(lsit);
		}

		return true;
	}

	public int getId() {
		return id;
	}

	public int getLostBuild() {
		return lostBuild;
	}

	public int getLevel() {
		return level;
	}

	public ImmutableMap<EffType, Integer> getControleBuffMap() {
		return controleBuffMap;
	}

	public void setControleBuffMap(ImmutableMap<EffType, Integer> controleBuffMap) {
		this.controleBuffMap = controleBuffMap;
	}

	public String getBuffList() {
		return buffList;
	}

}
