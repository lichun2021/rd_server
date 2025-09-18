package com.hawk.game.service.starwars;

import com.hawk.game.service.starwars.StarWarsConst.SWGroupType;

/**
 * 星球大战出战联盟数据
 * 
 * @author admin
 *
 */
public class SWGuildJoinInfo {

	public String id;

	public int zone;
	
	public int team;

	public String serverId;

	public SWGroupType group;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getZone() {
		return zone;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}
	
	public int getTeam() {
		return team;
	}

	public void setTeam(int team) {
		this.team = team;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public SWGroupType getGroup() {
		return group;
	}

	public void setGroup(SWGroupType group) {
		this.group = group;
	}

}
