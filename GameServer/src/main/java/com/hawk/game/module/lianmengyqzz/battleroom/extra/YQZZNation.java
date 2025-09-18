package com.hawk.game.module.lianmengyqzz.battleroom.extra;

import com.google.common.base.MoreObjects;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZ_CAMP;

public class YQZZNation {
	private YQZZ_CAMP camp;
	private String serverId;
	private int nationLevel;
	private String presidentId = "";
	private String presidentName = "";

	public final static YQZZNation defaultInstance;
	static {
		defaultInstance = new YQZZNation();
		defaultInstance.camp = YQZZ_CAMP.A;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("camp", camp)
				.add("serverId", serverId)
				.add("nationLevel", nationLevel)
				.add("presidentId", presidentId)
				.add("presidentName", presidentName)
				.toString();
	}

	public YQZZ_CAMP getCamp() {
		return camp;
	}

	public void setCamp(YQZZ_CAMP camp) {
		this.camp = camp;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public static YQZZNation getDefaultInstance() {
		return defaultInstance;
	}

	public int getNationLevel() {
		return nationLevel;
	}

	public void setNationLevel(int nationLevel) {
		this.nationLevel = nationLevel;
	}

	public String getPresidentId() {
		return presidentId;
	}

	public void setPresidentId(String presidentId) {
		this.presidentId = presidentId;
	}

	public String getPresidentName() {
		return presidentName;
	}

	public void setPresidentName(String presidentName) {
		this.presidentName = presidentName;
	}

}
