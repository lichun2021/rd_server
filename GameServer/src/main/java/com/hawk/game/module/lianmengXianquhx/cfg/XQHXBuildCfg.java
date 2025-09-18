package com.hawk.game.module.lianmengXianquhx.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/xqhx_build.xml")
public class XQHXBuildCfg extends HawkConfigBase {
	@Id
	private final int buildId;
	private final int buildTypeId;
	private final int coordinateX;
	private final int coordinateY;

	public XQHXBuildCfg() {
		buildId = 0;
		buildTypeId = 0;
		coordinateX = 0;
		coordinateY = 0;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

	public int getBuildId() {
		return buildId;
	}

	public int getBuildTypeId() {
		return buildTypeId;
	}

	public int getCoordinateX() {
		return coordinateX;
	}

	public int getCoordinateY() {
		return coordinateY;
	}

}
