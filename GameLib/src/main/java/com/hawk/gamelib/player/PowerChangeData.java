package com.hawk.gamelib.player;

import com.hawk.log.LogConst.PowerType;

/**
 * 战力数值变化数据
 * @author Jesse
 *
 */
public class PowerChangeData {
	// 总战力变化值
	private int battleChange = 0;
	// 玩家战斗力
	private int playerBattleChange = 0;
	// 部队战斗力 
	private int armyBattleChange = 0;
	// 科技战斗力
	private int techBattleChange = 0;
	// 建筑战斗力
	private int buildBattleChange = 0;
	// 陷阱战斗力
	private int trapBattleChange = 0;
	// 英雄战斗力
	private int heroBattleChange = 0;
	// 月卡战斗力
	private int monthCardBattleChange = 0;
	//泰能科技战斗力
	private int plantScienceBattleChange = 0;
	// 机甲战力
	private int superSoldierBattleChange = 0;
	// 装备战斗力
	private int armourBattleChange = 0;
	// 远征科技战力
	private int crossTechBattleChange = 0;
	// 泰能强化
	private int plantTechBattleChange = 0;
	// 泰能战士
	private int plantSchoolBattleChange = 0;
    // 星能探索
	private int starExploreBattleChange = 0;
	// 超武底座战力变化
	private int manhattanBaseBattleChange = 0;
	// 超武战力变化
	private int manhattanSWBattleChange = 0;
	// 机甲核心科技战力变化
	private int mechaCoreTechPowerChange = 0;
	// 机甲核心模块战力变化
	private int mechaCoreModulePowerChange = 0;
	// 家园模块战力变化
	private int homeLandModulePowerChange = 0;

	public PowerChangeData(PowerData oldData, PowerData newData) {
		super();
		this.battleChange = (int) (newData.getBattlePoint() - oldData.getBattlePoint());
		this.playerBattleChange = newData.getPlayerBattlePoint() - oldData.getPlayerBattlePoint();
		this.armyBattleChange = (int) (newData.getArmyBattlePoint() - oldData.getArmyBattlePoint());
		this.techBattleChange = newData.getTechBattlePoint() - oldData.getTechBattlePoint();
		this.buildBattleChange = newData.getBuildBattlePoint() - oldData.getBuildBattlePoint();
		this.trapBattleChange = newData.getTrapBattlePoint() - oldData.getTrapBattlePoint();
		this.heroBattleChange = newData.getHeroBattlePoint() - oldData.getHeroBattlePoint();
		this.monthCardBattleChange = newData.getMonthCardBattlePoint() - oldData.getMonthCardBattlePoint();
		this.plantScienceBattleChange = newData.getPlantScienceBattlePoint() - oldData.getPlantScienceBattlePoint();
		
		this.superSoldierBattleChange = newData.getSuperSoldierBattlePoint() - oldData.getSuperSoldierBattlePoint();
		this.armourBattleChange = newData.getArmourBattlePoint() - oldData.getArmourBattlePoint();
		this.crossTechBattleChange = newData.getCrossTechBattlePoint() - oldData.getCrossTechBattlePoint();
		this.plantTechBattleChange = newData.getPlantTechBattlePoint() - oldData.getPlantTechBattlePoint();
		this.plantSchoolBattleChange = newData.getPlantSchoolBattlePoint() - oldData.getPlantSchoolBattlePoint();
		this.starExploreBattleChange = newData.getStarExploreBattlePoint() - oldData.getStarExploreBattlePoint();
		this.manhattanBaseBattleChange = newData.getManhattanBaseBattlePoint() - oldData.getManhattanBaseBattlePoint();
		this.manhattanSWBattleChange = newData.getManhattanSWBattlePoint() - oldData.getManhattanSWBattlePoint();
		this.mechaCoreTechPowerChange = newData.getMechaCoreTechPower() - oldData.getMechaCoreTechPower();
		this.mechaCoreModulePowerChange = newData.getMechaCoreModulePower() - oldData.getMechaCoreModulePower();
		this.homeLandModulePowerChange = newData.getHomeLandModulePower() - oldData.getHomeLandModulePower();
	}

	public int getBattleChange() {
		return battleChange;
	}

	public void setBattleChange(int battleChange) {
		this.battleChange = battleChange;
	}

	public int getPlayerBattleChange() {
		return playerBattleChange;
	}

	public void setPlayerBattleChange(int playerBattleChange) {
		this.playerBattleChange = playerBattleChange;
	}

	public int getArmyBattleChange() {
		return armyBattleChange;
	}

	public void setArmyBattleChange(int armyBattleChange) {
		this.armyBattleChange = armyBattleChange;
	}

	public int getTechBattleChange() {
		return techBattleChange;
	}

	public void setTechBattleChange(int techBattleChange) {
		this.techBattleChange = techBattleChange;
	}

	public int getBuildBattleChange() {
		return buildBattleChange;
	}

	public void setBuildBattleChange(int buildBattleChange) {
		this.buildBattleChange = buildBattleChange;
	}

	public int getTrapBattleChange() {
		return trapBattleChange;
	}

	public void setTrapBattleChange(int trapBattleChange) {
		this.trapBattleChange = trapBattleChange;
	}

	public int getHeroBattleChange() {
		return heroBattleChange;
	}

	public void setHeroBattleChange(int heroBattleChange) {
		this.heroBattleChange = heroBattleChange;
	}

	public int getMonthCardBattleChange() {
		return monthCardBattleChange;
	}

	public void setMonthCardBattleChange(int monthCardBattleChange) {
		this.monthCardBattleChange = monthCardBattleChange;
	}
	
	public int getPlantScienceBattlePoint() {
		return plantScienceBattleChange;
	}

	public void setPlantScienceBattlePoint(int plantScienceBattlePoint) {
		this.plantScienceBattleChange = plantScienceBattlePoint;
	}

	
	public int getPlantScienceBattleChange() {
		return plantScienceBattleChange;
	}

	public void setPlantScienceBattleChange(int plantScienceBattleChange) {
		this.plantScienceBattleChange = plantScienceBattleChange;
	}

	public int getSuperSoldierBattleChange() {
		return superSoldierBattleChange;
	}

	public void setSuperSoldierBattleChange(int superSoldierBattleChange) {
		this.superSoldierBattleChange = superSoldierBattleChange;
	}

	public int getArmourBattleChange() {
		return armourBattleChange;
	}

	public void setArmourBattleChange(int armourBattleChange) {
		this.armourBattleChange = armourBattleChange;
	}

	public int getCrossTechBattleChange() {
		return crossTechBattleChange;
	}

	public void setCrossTechBattleChange(int crossTechBattleChange) {
		this.crossTechBattleChange = crossTechBattleChange;
	}

	public int getPlantTechBattleChange() {
		return plantTechBattleChange;
	}

	public void setPlantTechBattleChange(int plantTechBattleChange) {
		this.plantTechBattleChange = plantTechBattleChange;
	}

	public int getPlantSchoolBattleChange() {
		return plantSchoolBattleChange;
	}

	public void setPlantSchoolBattleChange(int plantSchoolBattleChange) {
		this.plantSchoolBattleChange = plantSchoolBattleChange;
	}

	public int getStarExploreBattleChange() {
		return starExploreBattleChange;
	}

	public void setStarExploreBattleChange(int starExploreBattleChange) {
		this.starExploreBattleChange = starExploreBattleChange;
	}
	
	public int getManhattanBaseBattleChange() {
		return manhattanBaseBattleChange;
	}

	public void setManhattanBaseBattleChange(int manhattanBaseBattleChange) {
		this.manhattanBaseBattleChange = manhattanBaseBattleChange;
	}

	public int getManhattanSWBattleChange() {
		return manhattanSWBattleChange;
	}

	public void setManhattanSWBattleChange(int manhattanSWBattleChange) {
		this.manhattanSWBattleChange = manhattanSWBattleChange;
	}
	
	public int getMechaCoreTechPowerChange() {
		return mechaCoreTechPowerChange;
	}

	public void setMechaCoreTechPowerChange(int mechaCoreTechPowerChange) {
		this.mechaCoreTechPowerChange = mechaCoreTechPowerChange;
	}
	
	public int getMechaCoreModulePowerChange() {
		return mechaCoreModulePowerChange;
	}

	public void setMechaCoreModulePowerChange(int mechaCoreModulePowerChange) {
		this.mechaCoreModulePowerChange = mechaCoreModulePowerChange;
	}

	public int getHomeLandModulePowerChange() {
		return homeLandModulePowerChange;
	}

	public void setHomeLandModulePowerChange(int homeLandModulePowerChange) {
		this.homeLandModulePowerChange = homeLandModulePowerChange;
	}

	/**
	 * 战力是否下降
	 * @return
	 */
	public boolean isPowerReduce() {
		return battleChange < 0 || playerBattleChange < 0 || armyBattleChange < 0
				|| techBattleChange < 0 || buildBattleChange < 0 || trapBattleChange < 0
				|| heroBattleChange < 0 || monthCardBattleChange < 0 || plantScienceBattleChange <0
				|| superSoldierBattleChange < 0
				|| armourBattleChange < 0
				|| crossTechBattleChange < 0
				|| plantTechBattleChange < 0
				|| plantSchoolBattleChange < 0
				|| starExploreBattleChange < 0
				|| manhattanBaseBattleChange < 0
				|| manhattanSWBattleChange < 0
				|| mechaCoreTechPowerChange < 0
				|| mechaCoreModulePowerChange < 0
				|| homeLandModulePowerChange < 0;
	}
	
	/**
	 * 变化类型
	 * @return
	 */
	public PowerType getChangeType() {
		if (armyBattleChange != 0) {
			return PowerType.ARMY;
		}
		
		if (buildBattleChange != 0) {
			return PowerType.BUILDING;
		}
		
		if (techBattleChange != 0) {
			return PowerType.TECHNOLOGY;
		}
		
		if (heroBattleChange != 0) {
			return PowerType.HERO;
		}
		
		if (playerBattleChange != 0) {
			return PowerType.COMMANDER;
		}
		
		if (trapBattleChange != 0) {
			return PowerType.TRAP;
		}
		
		if (monthCardBattleChange != 0) {
			return PowerType.MONTHCARD;
		}
		if(plantScienceBattleChange != 0){
			return PowerType.PLANT_SCIENCE;
		}
		
		if (superSoldierBattleChange != 0) {
			return PowerType.SUPER_SOLDIER;
		}
		if (armourBattleChange != 0) {
			return PowerType.ARMOUR;
		}
		if (crossTechBattleChange != 0) {
			return PowerType.CROSS_TECH;
		}
		if (plantTechBattleChange != 0) {
			return PowerType.PLANT_TECH;
		}
		if (plantSchoolBattleChange != 0) {
			return PowerType.PLANT_SCHOOL;
		}
		if (starExploreBattleChange != 0) {
			return PowerType.STAR_EXPLORE;
		}
		
		if (manhattanBaseBattleChange != 0) {
			return PowerType.MANHATTAN_BASE;
		}
		if (manhattanSWBattleChange != 0) {
			return PowerType.MANHATTAN_SW;
		}
		if (mechaCoreTechPowerChange != 0) {
			return PowerType.MECHA_CORE_TECH;
		}
		if (mechaCoreModulePowerChange != 0) {
			return PowerType.MECHA_CORE_MODULE;
		}
		if (homeLandModulePowerChange != 0) {
			return PowerType.HOME_LAND_MODULE;
		}

		return PowerType.OTHER;
	}
	
	/**
	 * 战力变化数据累加
	 * @param changeData
	 */
	public void addChangeData(PowerChangeData changeData) {
		this.battleChange += changeData.battleChange;
		this.playerBattleChange += changeData.playerBattleChange;
		this.armyBattleChange += changeData.armyBattleChange;
		this.techBattleChange += changeData.techBattleChange;
		this.buildBattleChange += changeData.buildBattleChange;
		this.trapBattleChange += changeData.trapBattleChange;
		this.heroBattleChange += changeData.heroBattleChange;
		this.monthCardBattleChange += changeData.monthCardBattleChange;
		this.plantScienceBattleChange += changeData.plantScienceBattleChange;
		this.superSoldierBattleChange += changeData.superSoldierBattleChange;
		this.armourBattleChange += changeData.armourBattleChange;
		this.crossTechBattleChange += changeData.crossTechBattleChange;
		this.plantTechBattleChange += changeData.plantTechBattleChange;
		this.plantSchoolBattleChange += changeData.plantSchoolBattleChange;
		this.starExploreBattleChange += changeData.starExploreBattleChange;
		this.manhattanBaseBattleChange += changeData.manhattanBaseBattleChange;
		this.manhattanSWBattleChange += changeData.manhattanSWBattleChange;
		this.mechaCoreTechPowerChange += changeData.getMechaCoreTechPowerChange();
		this.mechaCoreModulePowerChange += changeData.getMechaCoreModulePowerChange();
		this.homeLandModulePowerChange += changeData.getHomeLandModulePowerChange();
	}
	
}
