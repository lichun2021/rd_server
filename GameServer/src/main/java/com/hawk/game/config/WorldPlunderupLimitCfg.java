package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.protocol.Const.PlayerAttr;

@HawkConfigManager.XmlResource(file = "xml/world_plunderUpLimit.xml")
@HawkConfigBase.CombineId(fields = { "cityLevel", "vipLevel" })
public class WorldPlunderupLimitCfg extends HawkConfigBase {
	@Id
	protected final int id;// ="1"
	protected final int cityLevel;// ="1"
	protected final int vipLevel;// ="0"
	protected final int resourceWeight_1007;// ="9500000"
	protected final int resourceWeight_1008;// ="9500000"
	protected final int resourceWeight_1009;// ="395834"
	protected final int resourceWeight_1010;// ="1583336"

	public WorldPlunderupLimitCfg() {
		id = 1;
		cityLevel = 1;
		vipLevel = 0;
		resourceWeight_1007 = 100000;
		resourceWeight_1008 = 100000;
		resourceWeight_1009 = 100000;
		resourceWeight_1010 = 100000;

	}

	public int getPlunderupLimit(int resType) {
		switch (resType) {
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			return getResourceWeight_1007();

		case PlayerAttr.OIL_UNSAFE_VALUE:
			return getResourceWeight_1008();
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			return getResourceWeight_1009();
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			return getResourceWeight_1010();
		default:
			break;
		}
		return 0;
	}

	@Override
	protected boolean assemble() {

		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public int getCityLevel() {
		return cityLevel;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public int getResourceWeight_1007() {
		return resourceWeight_1007;
	}

	public int getResourceWeight_1008() {
		return resourceWeight_1008;
	}

	public int getResourceWeight_1009() {
		return resourceWeight_1009;
	}

	public int getResourceWeight_1010() {
		return resourceWeight_1010;
	}

}
