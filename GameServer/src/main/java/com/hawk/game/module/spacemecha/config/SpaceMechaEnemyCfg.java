package com.hawk.game.module.spacemecha.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import com.google.common.base.Splitter;
import com.hawk.game.march.ArmyInfo;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 星甲召唤怪物配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/space_machine_enemy.xml")
public class SpaceMechaEnemyCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	protected final int type;
	/**
	 * 等级
	 */
	protected final int level;
	
	protected final int modelLevel;
	
	protected final int gridCnt;
	
	protected final int attackAim;
	
	protected final String heroList;
	
	protected final String soldierList;
	
	protected final int power;
	// 克制关系的作用号
	protected final String curbBuff;
	
	protected final String soldierEffect;
	
	protected final String icon;
	protected final String name;
	
	// 怪物兵力数据
	private List<ArmyInfo> armyList;
	
	private List<Integer> heroIdList = new ArrayList<>();
	
	/**
	 * 克制buff
	 */
	Map<Integer, Integer> curbBuffMap;
	private Map<Integer, Integer> soldierEffectMap;
	
	public SpaceMechaEnemyCfg() {
		id = 0;
		type = 0;
		level = 0;
		modelLevel = 0;
		gridCnt = 0;
		attackAim = 0;
		heroList = "";
		soldierList = "";
		power  = 0;
		curbBuff = "";
		icon = "";
		name = "";
		soldierEffect = "";
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}
	
	public int getModelLevel() {
		return modelLevel;
	}
	
	public int getGridCnt() {
		return gridCnt;
	}

	public int getType() {
		return type;
	}

	public int getAttackAim() {
		return attackAim;
	}

	public String getHeroList() {
		return heroList;
	}
	
	public String getSoldierList() {
		return soldierList;
	}

	public int getPower() {
		return power;
	}
	
	public String getCurbBuff() {
		return curbBuff;
	}
	
	public boolean assemble() {
		armyList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(soldierList)) {
			for (String army : Splitter.on("|").split(soldierList)) {
				String[] armyStrs = army.split("_");
				armyList.add(new ArmyInfo(Integer.parseInt(armyStrs[0]), Integer.parseInt(armyStrs[1])));
			}
		}
		
		heroIdList = SerializeHelper.stringToList(Integer.class, heroList, "\\|");
		curbBuffMap = SerializeHelper.stringToMap(curbBuff, Integer.class, Integer.class, "_", ",");
		soldierEffectMap = SerializeHelper.stringToMap(soldierEffect, Integer.class, Integer.class, "_", ",");
		return true;
	}
	
	public int getCurbBuff(int effId) {
		return curbBuffMap.getOrDefault(effId, 0);
	}
	
	public List<Integer> getHeroIdList() {
		return heroIdList;
	}

	public List<ArmyInfo> getArmyList() {
		List<ArmyInfo> list = new ArrayList<>();
		armyList.forEach(e -> list.add(e.getCopy()));
		return list;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public String getName() {
		return name;
	}
	
	public Map<Integer, Integer> getSoldierEffectMap() {
		return soldierEffectMap;
	}
	
}
