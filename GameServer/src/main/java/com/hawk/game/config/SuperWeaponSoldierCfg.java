package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.march.ArmyInfo;

/**
 * 超级武器士兵配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/super_barrack_soldier.xml")
public class SuperWeaponSoldierCfg extends HawkConfigBase {
	
	@Id
	protected final int level;
	
	/**
	 * 英雄
	 */
	protected final String heroList;
	/**
	 * 士兵
	 */
	protected final String soldierList;
	
	/**
	 * 英雄id列表
	 */
	private List<Integer> heroIdList;
	
	/**
	 * npc部队信息
	 */
	private List<ArmyInfo> armyList;
	
	public SuperWeaponSoldierCfg() {
		this.level = 0;
		this.heroList = "";
		this.soldierList = "";
	}
	
	public int getLevel() {
		return level;
	}

	public List<Integer> getHeroIdList() {
		return heroIdList;
	}

	public List<ArmyInfo> getArmyList() {
		List<ArmyInfo> list = new ArrayList<>();
		armyList.forEach(e -> list.add(e.getCopy()));
		return list;
	}
	
	@Override
	protected boolean assemble() {
		
		heroIdList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(heroList)) {
			String[] heros = heroList.split("\\|");
			for (int i = 0; i < heros.length; i++) {
				heroIdList.add(Integer.valueOf(heros[i]));
			}
		}
		
		armyList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(soldierList)) {
			String[] soldiers = soldierList.split("\\|");
			for (int i = 0; i < soldiers.length; i++) {
				String[] soldier = soldiers[i].split("_");
				armyList.add(new ArmyInfo(Integer.parseInt(soldier[0]), Integer.parseInt(soldier[1])));
			}
		}
		return true;
	}
}
