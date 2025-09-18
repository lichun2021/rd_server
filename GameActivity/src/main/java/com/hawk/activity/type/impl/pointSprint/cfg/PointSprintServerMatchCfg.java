package com.hawk.activity.type.impl.pointSprint.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;

@HawkConfigManager.XmlResource(file = "activity/point_sprint/point_sprint_server.xml")
public class PointSprintServerMatchCfg extends HawkConfigBase {
	/**
	 * 组
	 */
	@Id
	private final int groupId;

	/**
	 * 区ID
	 */
	private final String serverList;

	private List<String> sererListList;

	/**
	 * 构造
	 */
	public PointSprintServerMatchCfg() {
		groupId = 0;
		serverList = "";
	}

	@Override
	protected boolean assemble() {
		sererListList = Splitter.on("_").omitEmptyStrings().splitToList(serverList);
		return super.assemble();
	}

	public int getGroupId() {
		return groupId;
	}

	public String getServerList() {
		return serverList;
	}

	public List<String> getSererListList() {
		return sererListList;
	}

}
