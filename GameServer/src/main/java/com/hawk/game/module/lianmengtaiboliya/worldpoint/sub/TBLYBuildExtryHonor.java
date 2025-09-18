package com.hawk.game.module.lianmengtaiboliya.worldpoint.sub;

import com.google.common.base.Objects;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.ITBLYBuilding;

public class TBLYBuildExtryHonor {
	private final ITBLYBuilding parent;
	private String ownerGuild; // 累计时间的联盟
	private long controlTime; // 累计控制时长

	public TBLYBuildExtryHonor(ITBLYBuilding parent) {
		this.parent = parent;
	}

	/** 增加控制时间*/
	public void incControlTime(String guildId, long timePass) {
		if (Objects.equal(guildId, ownerGuild)) {
			controlTime += timePass;
		}
	}

	/** 额外提供积分*/
	public double getHonor() {
		if (controlTime < parent.getPointTime() * 1000) {
			return 0;
		}

		double result = (controlTime - parent.getPointTime() * 1000) / 1000 * parent.getPointSpeed() + parent.getPointBase();
		result = Math.max(result, 0);
		result = Math.min(result, parent.getPointMax());
		return result;
	}

	/**变更控制者*/
	public double stateChangeControl(String guildId) {
		double result = 0;
		if (!Objects.equal(guildId, ownerGuild)) {
			result = getHonor();
			ownerGuild = guildId;
			controlTime = 0;
		}
		return result;
	}

	public boolean isMax() {
		return getHonor() >= parent.getPointMax();
	}

	public String getOwnerGuild() {
		return ownerGuild;
	}

	public void setOwnerGuild(String ownerGuild) {
		this.ownerGuild = ownerGuild;
	}

	public long getControlTime() {
		return controlTime;
	}

	public void setControlTime(long controlTime) {
		this.controlTime = controlTime;
	}

	public ITBLYBuilding getParent() {
		return parent;
	}

}
