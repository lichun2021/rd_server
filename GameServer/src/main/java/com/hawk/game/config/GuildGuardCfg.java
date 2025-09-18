package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 联盟据点配置
 *
 * @author hawk
 *
 */
@HawkConfigManager.XmlResource(file = "xml/territory_guard.xml")
public class GuildGuardCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 攻破次数
	private final int breakTimes;
	// 部队配置
	private final int armyId;
	// 医院等级
	private final int hospitalLevel;
	// 大使馆等级
	private final int embassyLevel;
	// 卫星通讯所等级
	private final int satelliteLevel;
	// 作战指挥部等级
	private final int commanderLevel;
	// 初始黄金
	private final int gold;
	// 初始石油
	private final int oil;
	// 初始钢铁
	private final int steel;
	// 初始合金
	private final int alloy;

	private final String size;
	private int[] bastionSize;

	public GuildGuardCfg() {
		id = 0;
		breakTimes = 0;
		armyId = 0;
		hospitalLevel = 0;
		embassyLevel = 0;
		satelliteLevel = 0;
		commanderLevel = 0;
		gold = 0;
		oil = 0;
		steel = 0;
		alloy = 0;
		size = "";
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(size)) {
			String[] strs = size.split("_");
			bastionSize = new int[strs.length];
			for (int i = 0; i < strs.length; i++) {
				bastionSize[i] = Integer.parseInt(strs[i]);
			}
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		if (bastionSize == null) {
			return false;
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getBreakTimes() {
		return breakTimes;
	}

	public int getArmyId() {
		return armyId;
	}

	public int getHospitalLevel() {
		return hospitalLevel;
	}

	public int getEmbassyLevel() {
		return embassyLevel;
	}

	public int getSatelliteLevel() {
		return satelliteLevel;
	}

	public int getCommanderLevel() {
		return commanderLevel;
	}

	public int getGold() {
		return gold;
	}

	public int getOil() {
		return oil;
	}

	public int getSteel() {
		return steel;
	}

	public int getAlloy() {
		return alloy;
	}

	public int[] getBastionSize() {
		return bastionSize;
	}
}