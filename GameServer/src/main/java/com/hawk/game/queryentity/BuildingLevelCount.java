package com.hawk.game.queryentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BuildingLevelCount {
	@Id
	@Column(name = "level")
	private int level;

	@Column(name = "count")
	private int count;

	public int getLevel() {
		return level;
	}

	public int getCount() {
		return count;
	}
}