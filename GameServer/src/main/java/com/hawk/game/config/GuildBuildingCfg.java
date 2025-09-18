package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 联盟领地配置
 *
 * @author hawk
 *
 */
@HawkConfigManager.XmlResource(file = "xml/territory_building.xml")
public class GuildBuildingCfg extends HawkConfigBase {
	@Id
	private final int id;
	private final int type;

	private final int level;
	private final int hospitalRate1; //堡垒伤兵
	private final int hospitalRate2; //援助伤兵
	private final int uraniumYield;
	private final int nuclearPowerYield;
	private final String cannonRate;

	private static int[] rates;

	public GuildBuildingCfg() {
		id = 0;
		type = 0;
		level = 0;
		hospitalRate1 = 0;
		hospitalRate2 = 0;
		uraniumYield = 0;
		nuclearPowerYield = 0;
		cannonRate = "";
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getLevel() {
		return level;
	}

	public int getHospitalRate1() {
		return hospitalRate1;
	}

	public int getHospitalRate2() {
		return hospitalRate2;
	}

	public int getUraniumYield() {
		return uraniumYield;
	}

	public int getNuclearPowerYield() {
		return nuclearPowerYield;
	}

	public static int[] getCannonRates() {
		return rates;
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(cannonRate)) {
			String[] array = cannonRate.split("_");
			if (array.length <= 0 || array.length % 2 == 1) {
				return false;
			}

			rates = new int[array.length];
			for (int i = 0; i < array.length; i++) {
				rates[i] = Integer.valueOf(array[i]);
			}
		}
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}
}
