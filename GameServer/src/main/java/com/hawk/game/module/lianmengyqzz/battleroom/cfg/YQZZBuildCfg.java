package com.hawk.game.module.lianmengyqzz.battleroom.cfg;

import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

@HawkConfigManager.XmlResource(file = "xml/moon_war_build.xml")
public class YQZZBuildCfg extends HawkConfigBase {
	@Id
	private final int buildId;
	private final int buildTypeId;
	private final int coordinateX;
	private final int coordinateY;
	private final String linkBuildList;
	private final int camp;
	private final int circle;
	private ImmutableList<Integer> linkList;

	public YQZZBuildCfg() {
		buildId = 0;
		buildTypeId = 0;
		coordinateX = 0;
		coordinateY = 0;
		linkBuildList = "";
		camp = 0;
		circle =1;
	}

	@Override
	protected boolean assemble() {
		List<Integer> linkIdList = Splitter.on(",").splitToList(linkBuildList).stream().map(Integer::valueOf).collect(Collectors.toList());
		linkList = ImmutableList.copyOf(linkIdList);
		return true;
	}

	public ImmutableList<Integer> getLinkList() {
		return linkList;
	}

	public void setLinkList(ImmutableList<Integer> linkList) {
		this.linkList = linkList;
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

	public String getLinkBuildList() {
		return linkBuildList;
	}

	public int getCamp() {
		return camp;
	}

//	public int getCircle() {
//		return circle;
//	}

}
