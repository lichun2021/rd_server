package com.hawk.activity.type.impl.inviteMerge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/merge_invite/merge_invite_server_rank.xml")
public class InviteMergeServerRankCfg extends HawkConfigBase {
	@Id
	private final String id;
	
	/**
	 * 大区ID
	 */
	private final String areaId;
	
	/**
	 * 服务器ID
	 */
	private final String serverId;
	
	/**
	 * 战力
	 */
	private final long power;
	
	/**
	 * 排名
	 */
	private final int rank;
	/**
	 * 是否是国王
	 */
	private final boolean isKing;
	
	/**
	 * 区服类型
	 */
	private final int type;
	
	public InviteMergeServerRankCfg() {
		id = "";
		areaId = "";
		serverId = "";
		power = 0;
		rank = 0;
        isKing = false;
		type = 1;
	}

	public String getId() {
		return id;
	}

	public String getAreaId() {
		return areaId;
	}

	public String getServerId() {
		return serverId;
	}

	public long getPower() {
		return power;
	}

	public int getRank() {
		return rank;
	}

	public boolean isKing() {
		return isKing;
	}

	public int getType() {
		return type;
	}
}