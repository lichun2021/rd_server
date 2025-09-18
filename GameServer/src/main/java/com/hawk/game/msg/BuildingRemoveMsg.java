package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.entity.BuildingBaseEntity;

/**
 * 拆除建筑
 *
 */
public class BuildingRemoveMsg extends HawkMsg {

	BuildingBaseEntity buildingEntity;

	private BuildingRemoveMsg() {

	}

	public static BuildingRemoveMsg valueOf(BuildingBaseEntity buildingEntity) {
		BuildingRemoveMsg msg = new BuildingRemoveMsg();
		msg.buildingEntity = buildingEntity;
		return msg;
	}

	public BuildingBaseEntity getBuildingEntity() {
		return buildingEntity;
	}

	public void setBuildingEntity(BuildingBaseEntity buildingEntity) {
		this.buildingEntity = buildingEntity;
	}
	
	
}
