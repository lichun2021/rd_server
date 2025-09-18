package com.hawk.activity.type.impl.copyCenter.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

@HawkConfigManager.XmlResource(file = "activity/copy_center/copy_center_chip.xml")
public class CopyCenterChipCfg extends HawkConfigBase implements HawkRandObj{

	@Id
	private final int id;
	private final int chipNum;
	private final int weight;

	public CopyCenterChipCfg() {
		id = 0;
		chipNum = 0;
		weight = 0;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getChipNum() {
		return chipNum;
	}

	@Override
	public int getWeight() {
		return weight;
	}

}
