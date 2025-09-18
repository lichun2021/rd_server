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
@HawkConfigManager.XmlResource(file = "xml/cyborg_build_buff.xml")
@HawkConfigBase.CombineId(fields = { "buffId", "level" })
public class CYBORGBuildBuffCfg extends HawkConfigBase {
	// <data id="10001" level="1" buffId="1" buffList="100_5000,136_5000,102_5000" />
	protected final int id;
	protected final int buffId;
	protected final int level;

	/**
	 * 号令作用号
	 */
	protected final String buffList;

	private ImmutableMap<EffType, Integer> controleBuffMap;
	
	public static int maxLevel;

	public CYBORGBuildBuffCfg() {
		this.id = 0;
		this.buffId = 0;
		this.level = 0;
		this.buffList = "";
	}

	public int getId() {
		return id;
	}

	@Override
	protected boolean assemble() {
		{
			Map<EffType, Integer> lsit = new HashMap<>();
			for (String xy : buffList.trim().split("\\,")) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.put(EffType.valueOf(pos[0]), pos[1]);
			}
			controleBuffMap = ImmutableMap.copyOf(lsit);
		}
		maxLevel = Math.max(maxLevel, level);
		return true;
	}

	public ImmutableMap<EffType, Integer> getControleBuffMap() {
		return controleBuffMap;
	}

	public void setControleBuffMap(ImmutableMap<EffType, Integer> controleBuffMap) {
		this.controleBuffMap = controleBuffMap;
	}

	public int getBuffId() {
		return buffId;
	}

	public int getLevel() {
		return level;
	}

	public String getBuffList() {
		return buffList;
	}

}
