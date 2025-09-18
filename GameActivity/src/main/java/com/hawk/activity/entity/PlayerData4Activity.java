package com.hawk.activity.entity;

/**
 * 从GameServer那里获取Activity工程需要的数据
 * @author jm
 *
 */
public class PlayerData4Activity {
	/**
	 * 玩家ID
	 */
	private String playerId;
	/**
	 * 建筑战力
	 */
	private int buildingBattlePoint;
	/**
	 * 科技战力
	 */
	private int techBattlePoint;
	
	
	private int plantScienceBattlePoint;
	
	public int getBuildingBattlePoint() {
		return buildingBattlePoint;
	}
	public void setBuildingBattlePoint(int buildingBattlePoint) {
		this.buildingBattlePoint = buildingBattlePoint;
	}
	public int getTechBattlePoint() {
		return techBattlePoint;
	}
	public void setTechBattlePoint(int techBattlePoint) {
		this.techBattlePoint = techBattlePoint;
	}
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public int getPlantScienceBattlePoint() {
		return plantScienceBattlePoint;
	}
	public void setPlantScienceBattlePoint(int plantScienceBattlePoint) {
		this.plantScienceBattlePoint = plantScienceBattlePoint;
	}
	
	
}
