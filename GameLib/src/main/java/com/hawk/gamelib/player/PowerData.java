package com.hawk.gamelib.player;

/**
 * 战力数值
 * 
 * @author PhilChen
 *
 */
public class PowerData {
	// 是否初始化完整战力
	private boolean inited = false;
	// 总战力
	private long battlePoint = 0;
	// 玩家战斗力
	private int playerBattlePoint = 0;
	// 部队战斗力
	private long armyBattlePoint = 0;
	// 科技战斗力
	private int techBattlePoint = 0;
	// 建筑战斗力
	private int buildBattlePoint = 0;
	// 陷阱战斗力
	private int trapBattlePoint = 0;
	// 英雄战斗力
	private int heroBattlePoint = 0;
	// 机甲战力
	private int superSoldierBattlePoint;
	// 月卡战斗力
	private int monthCardBattlePoint = 0;
	// 铠甲战斗力
	private int armourBattlePoint = 0;
	// 远征科技战力
	private int crossTechBattlePoint = 0;
	// 装备科技战力
	private int equipResearchPoint = 0;
	// 泰能强化
	private int plantTechBattlePoint = 0;
	// 泰能战士
	private int plantSchoolBattlePoint;
	//泰能兵科技树科技
	private int plantScienceBattlePoint = 0;
	//星能探索
	private int starExploreBattlePoint = 0;
	//超武底座战力
	private int manhattanBaseBattlePoint = 0;
	//超武战力
	private int manhattanSWBattlePoint = 0;
	//机甲核心科技战力
	private int mechaCoreTechPower = 0;
	//机甲核心模块战力
	private int mechaCoreModulePower = 0;
	//家园模块战力
	private int homeLandModulePower = 0;

	/**
	 * 复制战力数据
	 * @return
	 */
	public PowerData getCopy(){
		PowerData copy = new PowerData();
		copy.setBattlePoint(battlePoint);
		copy.setArmyBattlePoint(armyBattlePoint);
		copy.setBuildBattlePoint(buildBattlePoint);
		copy.setHeroBattlePoint(heroBattlePoint);
		copy.setMonthCardBattlePoint(monthCardBattlePoint);
		copy.setPlayerBattlePoint(playerBattlePoint);
		copy.setTechBattlePoint(techBattlePoint);
		copy.setTrapBattlePoint(trapBattlePoint);
		copy.setArmourBattlePoint(armourBattlePoint);
		copy.setCrossTechBattlePoint(crossTechBattlePoint);
		copy.setEquipResearchPoint(equipResearchPoint);
		copy.setPlantTechBattlePoint(plantTechBattlePoint);
		copy.setPlantSchoolBattlePoint(plantSchoolBattlePoint);
		copy.setPlantScienceBattlePoint(plantScienceBattlePoint);
		copy.setStarExploreBattlePoint(starExploreBattlePoint);
		copy.setManhattanBaseBattlePoint(manhattanBaseBattlePoint);
		copy.setManhattanSWBattlePoint(manhattanSWBattlePoint);
		copy.setMechaCoreTechPower(mechaCoreTechPower);
		copy.setMechaCoreModulePower(mechaCoreModulePower);
		copy.setHomeLandModulePower(homeLandModulePower);
		return copy;
	}
	
	public boolean isInited() {
		return inited;
	}

	public void setInited(boolean inited) {
		this.inited = inited;
	}

	public long getBattlePoint() {
		return battlePoint;
	}

	public long setBattlePoint(long battlePoint) {
		this.battlePoint = battlePoint;
		return this.battlePoint;
	}
	
	public int getPlayerBattlePoint() {
		return playerBattlePoint;
	}

	public int setPlayerBattlePoint(int playerBattlePoint) {
		this.playerBattlePoint = playerBattlePoint;
		return this.playerBattlePoint;
	}

	public long getArmyBattlePoint() {
		return armyBattlePoint;
	}

	public long setArmyBattlePoint(long armyBattlePoint) {
		this.armyBattlePoint = armyBattlePoint;
		return this.armyBattlePoint;
	}

	public int getTechBattlePoint() {
		return techBattlePoint;
	}

	public int setTechBattlePoint(int techBattlePoint) {
		this.techBattlePoint = techBattlePoint;
		return this.techBattlePoint;
	}

	public int getBuildBattlePoint() {
		return buildBattlePoint;
	}

	public int setBuildBattlePoint(int buildBattlePoint) {
		this.buildBattlePoint = buildBattlePoint;
		return this.buildBattlePoint;
	}

	public int getTrapBattlePoint() {
		return trapBattlePoint;
	}

	public int setTrapBattlePoint(int trapBattlePoint) {
		this.trapBattlePoint = trapBattlePoint;
		return this.trapBattlePoint;
	}

	public int getHeroBattlePoint() {
		return heroBattlePoint;
	}

	public void setHeroBattlePoint(int heroBattlePoint) {
		this.heroBattlePoint = heroBattlePoint;
	}
	
	public int getMonthCardBattlePoint() {
		return monthCardBattlePoint;
	}

	public void setMonthCardBattlePoint(int monthCardBattlePoint) {
		this.monthCardBattlePoint = monthCardBattlePoint;
	}

	public long getTotalPoint() {
		return armyBattlePoint + playerBattlePoint + buildBattlePoint + 
				techBattlePoint + trapBattlePoint + heroBattlePoint + 
				superSoldierBattlePoint + monthCardBattlePoint + 
				armourBattlePoint + crossTechBattlePoint + 
				equipResearchPoint + plantTechBattlePoint + 
				plantScienceBattlePoint + plantSchoolBattlePoint +
				starExploreBattlePoint + manhattanBaseBattlePoint + manhattanSWBattlePoint +
				mechaCoreTechPower + mechaCoreModulePower + homeLandModulePower;
	}

	public int getSuperSoldierBattlePoint() {
		return superSoldierBattlePoint;
	}

	public void setSuperSoldierBattlePoint(int superSoldierBattlePoint) {
		this.superSoldierBattlePoint = superSoldierBattlePoint;
	}

	public int getArmourBattlePoint() {
		return armourBattlePoint;
	}

	public void setArmourBattlePoint(int armourBattlePoint) {
		this.armourBattlePoint = armourBattlePoint;
	}

	public int getCrossTechBattlePoint() {
		return crossTechBattlePoint;
	}

	public void setCrossTechBattlePoint(int crossTechBattlePoint) {
		this.crossTechBattlePoint = crossTechBattlePoint;
	}

	public int getEquipResearchPoint() {
		return equipResearchPoint;
	}

	public void setEquipResearchPoint(int equipResearchPoint) {
		this.equipResearchPoint = equipResearchPoint;
	}

	public void setPlantTechBattlePoint(int power) {
		this.plantTechBattlePoint = power;
	}

	public int getPlantTechBattlePoint() {
		return plantTechBattlePoint;
	}

	public int getPlantSchoolBattlePoint() {
		return plantSchoolBattlePoint;
	}

	public void setPlantSchoolBattlePoint(int plantSchoolBattlePoint) {
		this.plantSchoolBattlePoint = plantSchoolBattlePoint;
	}

	public int getPlantScienceBattlePoint() {
		return plantScienceBattlePoint;
	}

	public void setPlantScienceBattlePoint(int plantScienceBattlePoint) {
		this.plantScienceBattlePoint = plantScienceBattlePoint;
	}

	public int getStarExploreBattlePoint() {
		return starExploreBattlePoint;
	}

	public void setStarExploreBattlePoint(int starExploreBattlePoint) {
		this.starExploreBattlePoint = starExploreBattlePoint;
	}

	public int getManhattanBaseBattlePoint() {
		return manhattanBaseBattlePoint;
	}

	public void setManhattanBaseBattlePoint(int manhattanBaseBattlePoint) {
		this.manhattanBaseBattlePoint = manhattanBaseBattlePoint;
	}

	public int getManhattanSWBattlePoint() {
		return manhattanSWBattlePoint;
	}

	public void setManhattanSWBattlePoint(int manhattanSWBattlePoint) {
		this.manhattanSWBattlePoint = manhattanSWBattlePoint;
	}

	public int getMechaCoreTechPower() {
		return mechaCoreTechPower;
	}

	public void setMechaCoreTechPower(int mechaCoreTechPower) {
		this.mechaCoreTechPower = mechaCoreTechPower;
	}

	public int getMechaCoreModulePower() {
		return mechaCoreModulePower;
	}

	public void setMechaCoreModulePower(int mechaCoreModulePower) {
		this.mechaCoreModulePower = mechaCoreModulePower;
	}

	public int getHomeLandModulePower() {
		return homeLandModulePower;
	}

	public void setHomeLandModulePower(int homeLandModulePower) {
		this.homeLandModulePower = homeLandModulePower;
	}
}
