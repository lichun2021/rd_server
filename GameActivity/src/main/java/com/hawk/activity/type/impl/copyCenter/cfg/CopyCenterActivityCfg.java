package com.hawk.activity.type.impl.copyCenter.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/copy_center/copy_center_activity.xml")
public class CopyCenterActivityCfg extends HawkConfigBase {

	@Id
	private final int id;
	private final int heroId;
	private final int color;
	private final int level;
	private final int star;

	public CopyCenterActivityCfg() {
		id = 0;
		heroId = 0;
		color = 0;
		level = 0;
		star = 0;
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

	public int getColor() {
		return color;
	}

	public int getLevel() {
		return level;
	}

	public int getStar() {
		return star;
	}

	public int getHeroId() {
		return heroId;
	}

}
