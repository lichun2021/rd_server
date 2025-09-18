package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;

import com.hawk.game.config.BuildingCfg;

@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class BuildingLevelUpMsg extends HawkMsg {
	/**
	 * 建筑类型
	 */
	private int buildingType;
	/**
	 * 当前等级
	 */
	private int curLeve;
	/**
	 * 这次的建筑配置.
	 */
	private BuildingCfg buildingCfg;	

	public BuildingLevelUpMsg() {
		
	}

	public int getBuildingType() {
		return buildingType;
	}

	public void setBuildingType(int buildingType) {
		this.buildingType = buildingType;
	}

	public int getCurLeve() {
		return curLeve;
	}

	public void setCurLeve(int curLeve) {
		this.curLeve = curLeve;
	}
	
	public static BuildingLevelUpMsg valueOf(int buildingType, int curLeve, BuildingCfg buildingCfg) {
		BuildingLevelUpMsg msg = HawkObjectPool.getInstance().borrowObject(BuildingLevelUpMsg.class);
		msg.buildingType = buildingType;
		msg.curLeve = curLeve;
		msg.buildingCfg = buildingCfg;
		return msg;
	}

	public BuildingCfg getBuildingCfg() {
		return buildingCfg;
	}
}
