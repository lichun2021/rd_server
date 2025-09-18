package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/***
 * 联盟反击
 * @author lwt
 * @date 2018年9月1日
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_Compensation.xml")
public class AllianceCompensationCfg extends HawkConfigBase {
	@Id
	protected final int cityLevel;// ="1"

	protected final double beatbackInjuredSoldierSpeedUpCoefficient;// ="0.2"
	protected final double beatbackInjuredSoldierResourceCoefficient;// ="0.2"
	protected final double beatbackdDeadSoldierSpeedUpCoefficient;// ="0.2"
	protected final double beatbackDeadSoldierResourceCoefficient;// ="0.2"

	public AllianceCompensationCfg() {
		cityLevel = 1;
		beatbackInjuredSoldierSpeedUpCoefficient = 0.2;
		beatbackInjuredSoldierResourceCoefficient = 0.2;
		beatbackdDeadSoldierSpeedUpCoefficient = 0.2;
		beatbackDeadSoldierResourceCoefficient = 0.2;
	}

	public int getCityLevel() {
		return cityLevel;
	}

	public double getBeatbackInjuredSoldierSpeedUpCoefficient() {
		return beatbackInjuredSoldierSpeedUpCoefficient;
	}

	public double getBeatbackInjuredSoldierResourceCoefficient() {
		return beatbackInjuredSoldierResourceCoefficient;
	}

	public double getBeatbackdDeadSoldierSpeedUpCoefficient() {
		return beatbackdDeadSoldierSpeedUpCoefficient;
	}

	public double getBeatbackDeadSoldierResourceCoefficient() {
		return beatbackDeadSoldierResourceCoefficient;
	}

}
