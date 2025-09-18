package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/star_wars_part.xml")
public class StarWarsPartCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 平台 手Q，微信.
	 */
	private final String areaId;
	/**
	 * 分区(分四个大区)
	 */
	private final int zone;
	/**
	 * 分小组(分四个小组)
	 */
	private final int team;
	/**
	 * 区服ID列表.
	 */
	private final String serverIdList;
	/**
	 * 区服的列表.
	 */
	private List<String> serverList;
	/**
	 * 大洲名字.
	 */
	private final String zoneName;
	
	public StarWarsPartCfg() {
		this.id = 0;
		this.serverIdList = "";
		this.areaId = "";
		this.zone = 0;
		this.team = 0;
		this.zoneName = "";
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean assemble() {
		this.serverList = SerializeHelper.stringToList(String.class, this.serverIdList, SerializeHelper.BETWEEN_ITEMS);
		
		return true;
	}
	
	public int getId() {
		return id;
	}

	public int getZone() {
		return zone;
	}

	public int getTeam() {
		return team;
	}

	public List<String> getServerList() {
		return serverList;
	}

	public String getAreaId() {
		return areaId;
	}

	public String getZoneName() {
		return zoneName;
	}

	public String getServerIdList() {
		return serverIdList;
	}
}
